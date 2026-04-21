package com.hongshu.idle.service;

import cn.hutool.core.collection.CollectionUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.core.constant.NoteConstant;
import com.hongshu.common.core.enums.AuditStatusEnum;
import com.hongshu.common.core.utils.bean.BeanUtils;
import com.hongshu.idle.domain.entity.IdleCollection;
import com.hongshu.idle.domain.entity.IdleProduct;
import com.hongshu.idle.domain.vo.IdleProductVO;
import com.hongshu.idle.mapper.idle.IdleCollectionMapper;
import com.hongshu.idle.mapper.idle.IdleProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Mahout协同过滤商品推荐服务（优化版）
 * 
 * 使用Apache Mahout实现基于用户的协同过滤推荐
 * 
 * 优化内容：
 * 1. 添加新用户检测和热门内容兜底
 * 2. 添加用户信息查询（avatar、username）
 * 3. 改进异常处理和降级机制
 * 4. 优化MySQL模式的推荐逻辑
 * 5. 添加在售状态过滤
 *
 * @author: hongshu
 */
@Service
@Slf4j
public class RecommendProductServiceImpl extends ServiceImpl<IdleCollectionMapper, IdleCollection> implements IRecommendProductService {

    private static final long MODEL_CACHE_TTL_MS = 10 * 60 * 1000L;
    private volatile DataModel cachedDataModel;
    private volatile long cachedDataModelTimestamp;

    @Autowired
    private DataSource dataSource;
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    @Autowired
    private IdleCollectionMapper collectionMapper;
    @Autowired
    private IdleProductMapper productMapper;
    @Autowired
    private com.hongshu.idle.mapper.web.WebUserMapper userMapper;


    /**
     * 推荐商品（优化版）
     * 使用Mahout协同过滤算法
     *
     * @param userId 用户ID
     */
    @Override
    public List<IdleProductVO> getRecommendProduct(long userId) {
        log.info("Mahout协同过滤商品推荐 - 用户ID: {}", userId);
        
        // 优化1: 检查用户是否为新用户
        if (isNewUser(userId)) {
            log.info("检测到新用户: {}, 使用热门商品兜底", userId);
            return getHotProductsForNewUser(50);
        }
        
        List<IdleProductVO> idleProductVOList;
        try {
            DataModel model = getDataModel();
            
            // Step 2: 计算用户相似度（未中心化余弦相似度）
            UserSimilarity similarity = new UncenteredCosineSimilarity(model);
            
            // Step 3: 定义邻居（最相似的10个用户）
            NearestNUserNeighborhood neighborhood = new NearestNUserNeighborhood(10, similarity, model);
            
            // Step 4: 创建基于用户的协同过滤推荐器
            Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
            
            // Step 5: 生成推荐（Top 10）
            // Top-N 过小会导致 ES 推荐列表只有几条，分页/刷新体验差
            List<RecommendedItem> recommendations = recommender.recommend(userId, 50);
            
            // 优化2: 检查推荐结果是否为空
            if (recommendations == null || recommendations.isEmpty()) {
                log.warn("Mahout协同过滤无推荐结果，使用热门商品兜底");
                return getHotProductsForNewUser(50);
            }
            
            List<Long> recommendedProductIds = recommendations.stream()
                    .map(RecommendedItem::getItemID)
                    .collect(Collectors.toList());
            
            log.info("Mahout推荐商品ID: {}", recommendedProductIds);
            
            // Step 6: 获取推荐商品详情（优先从MySQL）
            idleProductVOList = getProductDetails(recommendedProductIds);
            
            // 优化3: 如果获取详情失败，使用热门商品兜底
            if (idleProductVOList == null || idleProductVOList.isEmpty()) {
                log.warn("获取推荐商品详情失败，使用热门商品兜底");
                return getHotProductsForNewUser(50);
            }
            
            // 随机排序
            Collections.shuffle(idleProductVOList, new Random());
            
        } catch (Exception e) {
            log.error("Mahout协同过滤商品推荐异常，降级到热门商品", e);
            // 优化4: 异常时返回热门商品，而不是空列表
            return getHotProductsForNewUser(50);
        }
        
        log.info("Mahout商品推荐成功，返回 {} 个商品", idleProductVOList.size());
        return idleProductVOList;
    }

    /**
     * 获取推荐商品列表
     */
    private SearchRequest buildProductSearchRequest(List<Long> recommendedProductIds) {
        // 推荐列表为空，默认随机加载50条数据
        if (CollectionUtil.isEmpty(recommendedProductIds)) {
            return SearchRequest.of(s -> s
                    .index(NoteConstant.PRODUCT_INDEX)
                    .size(50)
            );
        } else {
            // 推荐列表不为空，加载推荐数据
            List<FieldValue> fieldValues = recommendedProductIds.stream()
                    .map(FieldValue::of)
                    .collect(Collectors.toList());
            return SearchRequest.of(s -> s
                    .index(NoteConstant.PRODUCT_INDEX)
                    .query(q -> q
                            .terms(t -> t
                                    .field("id")
                                    .terms(terms -> terms.value(fieldValues))
                            )
                    )
                    .size(recommendedProductIds.size())
            );
        }
    }

    /**
     * 从MySQL获取推荐商品（优化版）
     *
     * @param userId      用户ID
     * @param currentPage 当前页
     * @param pageSize    页大小
     * @return 分页结果
     */
    @Override
    public Page<IdleProductVO> getRecommendProductFromMysql(String userId, long currentPage, long pageSize) {
        log.info("Mahout MySQL商品推荐 - 用户: {}, 页码: {}, 大小: {}", userId, currentPage, pageSize);
        
        Page<IdleProductVO> page = new Page<>(currentPage, pageSize);

        // 优化1: 检查用户是否为新用户
        long userBehaviorCount = collectionMapper.selectCount(
                new QueryWrapper<IdleCollection>()
                        .eq("uid", userId)
                        .in("type", Arrays.asList(1, 3))
        );
        
        if (userBehaviorCount == 0) {
            log.info("检测到新用户: {}, 使用热门商品", userId);
            return getHotProductsPage(currentPage, pageSize);
        }

        // 获取用户点赞和收藏的商品ID列表
        List<String> collectedProductIds = collectionMapper.selectList(
                        new QueryWrapper<IdleCollection>()
                                .eq("uid", userId)
                                .in("type", Arrays.asList(1, 3)) // 1：点赞 3：收藏
                                .select("collection_id")
                ).stream()
                .map(IdleCollection::getCollectionId)
                .collect(Collectors.toList());
                
        // 获取用户点赞/收藏的商品的作者ID列表
        List<String> authorIds = Collections.emptyList();
        if (!collectedProductIds.isEmpty()) {
            authorIds = productMapper.selectList(
                            new QueryWrapper<IdleProduct>()
                                    .in("id", collectedProductIds)
                                    .select("DISTINCT uid")
                    ).stream()
                    .map(IdleProduct::getUid)
                    .collect(Collectors.toList());
        }
        
        // 兴趣分类：同时收集 cid、cpid；原先用 DISTINCT cpid 再 in("cid") 会与字段语义不一致、召回偏少
        List<String> categoryIds = Collections.emptyList();
        if (!collectedProductIds.isEmpty()) {
            Set<String> catSet = new HashSet<>();
            List<IdleProduct> prefProducts = productMapper.selectList(
                    new QueryWrapper<IdleProduct>().in("id", collectedProductIds));
            for (IdleProduct p : prefProducts) {
                if (p.getCid() != null && !p.getCid().trim().isEmpty()) {
                    catSet.add(p.getCid());
                }
                if (p.getCpid() != null && !p.getCpid().trim().isEmpty()) {
                    catSet.add(p.getCpid());
                }
            }
            categoryIds = new ArrayList<>(catSet);
        }
        
        // 构建查询条件
        QueryWrapper<IdleProduct> queryWrapper = new QueryWrapper<IdleProduct>()
                .eq("deleted", 0)
                .eq("audit_status", AuditStatusEnum.PASS.getCode())
                .eq("status", 0); // 优化：只推荐在售商品
                
        // 如果有用户行为数据，基于用户偏好推荐：同作者 OR (cid 命中 OR cpid 命中)
        if (!authorIds.isEmpty() || !categoryIds.isEmpty()) {
            List<String> finalAuthorIds = authorIds;
            List<String> finalCategoryIds = categoryIds;
            queryWrapper.and(wrapper -> {
                if (!finalAuthorIds.isEmpty()) {
                    wrapper.in("uid", finalAuthorIds);
                }
                if (!finalCategoryIds.isEmpty()) {
                    if (!finalAuthorIds.isEmpty()) {
                        wrapper.or();
                    }
                    wrapper.nested(n -> n.in("cid", finalCategoryIds).or().in("cpid", finalCategoryIds));
                }
            });
        } else {
            // 优化2: 如果无偏好数据，返回热门商品
            log.warn("用户 {} 无偏好数据，返回热门商品", userId);
            return getHotProductsPage(currentPage, pageSize);
        }
        
        // 排除用户已经点赞/收藏的商品
        if (!collectedProductIds.isEmpty()) {
            queryWrapper.notIn("id", collectedProductIds);
        }
        
        // 排除用户自己的商品
        queryWrapper.ne("uid", userId);
        
        // 按点赞数、更新时间排序
        queryWrapper.orderByDesc("like_count")
                .orderByDesc("update_time");

        // 执行分页查询
        Page<IdleProduct> productPage = productMapper.selectPage(new Page<>(currentPage, pageSize), queryWrapper);
        
        // 转换结果
        page.setCurrent(productPage.getCurrent());
        page.setSize(productPage.getSize());
        page.setTotal(productPage.getTotal());
        List<IdleProductVO> voList = productPage.getRecords().stream()
                .map(this::convertToProductVO)
                .collect(Collectors.toList());
        enrichUserInfo(voList);
        page.setRecords(voList);
                
        log.info("Mahout MySQL商品推荐成功，返回 {} 个商品", page.getRecords().size());
        return page;
    }

    
    /**
     * 检查用户是否为新用户（优化新增）
     */
    private boolean isNewUser(long userId) {
        try {
            long count = collectionMapper.selectCount(
                    new QueryWrapper<IdleCollection>()
                            .eq("uid", userId)
                            .in("type", Arrays.asList(1, 3))
            );
            return count == 0;
        } catch (Exception e) {
            log.error("检查新用户失败", e);
            return false;
        }
    }
    
    
    /**
     * 获取推荐商品详情（优化新增）
     * 优先从MySQL查询，ES作为备选
     */
    private List<IdleProductVO> getProductDetails(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            // 优先从MySQL获取
            List<IdleProduct> products = productMapper.selectList(
                    new QueryWrapper<IdleProduct>()
                            .in("id", productIds)
                            .eq("deleted", 0)
                            .eq("status", 0) // 只要在售商品
                            .eq("audit_status", AuditStatusEnum.PASS.getCode())
            );
            
            if (products != null && !products.isEmpty()) {
                log.info("从MySQL获取推荐商品详情成功，数量: {}", products.size());
                List<IdleProductVO> vos = products.stream()
                        .map(this::convertToProductVO)
                        .collect(Collectors.toList());
                enrichUserInfo(vos);
                return vos;
            }
        } catch (Exception e) {
            log.warn("从MySQL获取商品详情失败，尝试ES", e);
        }
        
        // MySQL失败，尝试从ES获取
        try {
            SearchRequest searchRequest = this.buildProductSearchRequest(productIds);
            SearchResponse<IdleProductVO> searchResponse = elasticsearchClient.search(searchRequest, IdleProductVO.class);
            List<IdleProductVO> productVOs = searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .filter(product -> product.getStatus() == 0) // 只要在售商品
                    .collect(Collectors.toList());
            log.info("从ES获取推荐商品详情成功，数量: {}", productVOs.size());
            return productVOs;
        } catch (Exception e) {
            log.error("从ES获取商品详情也失败", e);
            return Collections.emptyList();
        }
    }
    
    
    /**
     * 为新用户获取热门商品（优化新增）
     */
    private List<IdleProductVO> getHotProductsForNewUser(int limit) {
        try {
            log.info("获取热门商品，数量: {}", limit);
            
            // 获取热门商品（按想要数量降序）
            List<IdleProduct> hotProducts = productMapper.selectList(
                    new QueryWrapper<IdleProduct>()
                            .eq("deleted", 0)
                            .eq("audit_status", AuditStatusEnum.PASS.getCode())
                            .eq("status", 0) // 在售状态
                            .orderByDesc("like_count", "create_time")
                            .last("LIMIT " + (limit / 2))
            );
            
            // 获取最新商品
            List<IdleProduct> newProducts = productMapper.selectList(
                    new QueryWrapper<IdleProduct>()
                            .eq("deleted", 0)
                            .eq("audit_status", AuditStatusEnum.PASS.getCode())
                            .eq("status", 0) // 在售状态
                            .orderByDesc("create_time")
                            .last("LIMIT " + (limit / 2))
            );
            
            // 合并去重
            java.util.Set<String> productIds = new java.util.HashSet<>();
            List<IdleProduct> allProducts = new java.util.ArrayList<>();
            
            for (IdleProduct product : hotProducts) {
                if (productIds.add(String.valueOf(product.getId()))) {
                    allProducts.add(product);
                }
            }
            
            for (IdleProduct product : newProducts) {
                if (productIds.add(String.valueOf(product.getId()))) {
                    allProducts.add(product);
                }
            }
            
            // 随机打乱
            Collections.shuffle(allProducts);
            
            List<IdleProductVO> vos = allProducts.stream()
                    .map(this::convertToProductVO)
                    .collect(Collectors.toList());
            enrichUserInfo(vos);
            return vos;
                    
        } catch (Exception e) {
            log.error("获取热门商品失败", e);
            return Collections.emptyList();
        }
    }
    
    
    /**
     * 获取热门商品分页（优化新增）
     */
    private Page<IdleProductVO> getHotProductsPage(long currentPage, long pageSize) {
        try {
            Page<IdleProduct> productPage = productMapper.selectPage(
                    new Page<>(currentPage, pageSize),
                    new QueryWrapper<IdleProduct>()
                            .eq("audit_status", AuditStatusEnum.PASS.getCode())
                            .eq("status", 0) // 在售状态
                            .orderByDesc("like_count", "create_time")
            );
            
            Page<IdleProductVO> page = new Page<>(productPage.getCurrent(), productPage.getSize());
            page.setTotal(productPage.getTotal());
            List<IdleProductVO> vos = productPage.getRecords().stream()
                    .map(this::convertToProductVO)
                    .collect(Collectors.toList());
            enrichUserInfo(vos);
            page.setRecords(vos);
            
            log.info("返回热门商品分页，当前页: {}, 数量: {}", currentPage, page.getRecords().size());
            return page;
        } catch (Exception e) {
            log.error("获取热门商品分页失败", e);
            return new Page<>(currentPage, pageSize);
        }
    }


    /**
     * 将IdleProduct实体转换为IdleProductVO（优化版）
     */
    private IdleProductVO convertToProductVO(IdleProduct product) {
        IdleProductVO vo = new IdleProductVO();
        BeanUtils.copyProperties(product, vo);
        return vo;
    }

    private void enrichUserInfo(List<IdleProductVO> productList) {
        if (productList == null || productList.isEmpty()) return;
        Set<String> uidSet = productList.stream()
                .filter(p -> p.getUid() != null)
                .map(IdleProductVO::getUid)
                .collect(java.util.stream.Collectors.toSet());
        if (uidSet.isEmpty()) return;
        try {
            Map<String, com.hongshu.idle.domain.entity.WebUser> userMap = userMapper.selectList(
                    new QueryWrapper<com.hongshu.idle.domain.entity.WebUser>()
                            .select("id", "username", "avatar")
                            .in("id", uidSet)
            ).stream().collect(java.util.stream.Collectors.toMap(
                    u -> String.valueOf(u.getId()), u -> u, (a, b) -> a));
            for (IdleProductVO vo : productList) {
                com.hongshu.idle.domain.entity.WebUser u = userMap.get(vo.getUid());
                if (u != null) {
                    vo.setAvatar(u.getAvatar());
                    vo.setUsername(u.getUsername());
                }
            }
        } catch (Exception e) {
            log.error("批量填充用户信息失败", e);
        }
    }

    private DataModel getDataModel() throws Exception {
        DataModel m = cachedDataModel;
        if (m != null && (System.currentTimeMillis() - cachedDataModelTimestamp) < MODEL_CACHE_TTL_MS) {
            return m;
        }
        m = new MySQLJDBCDataModel(dataSource, "idle_collection", "uid", "collection_id", "type", "create_time");
        cachedDataModel = m;
        cachedDataModelTimestamp = System.currentTimeMillis();
        return m;
    }
}
