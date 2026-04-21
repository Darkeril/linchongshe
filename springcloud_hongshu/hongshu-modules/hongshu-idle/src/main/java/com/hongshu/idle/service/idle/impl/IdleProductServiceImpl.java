package com.hongshu.idle.service.idle.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.audit.service.CommonAuditService;
import com.hongshu.common.core.constant.NoteConstant;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.common.core.enums.AuditStatusEnum;
import com.hongshu.common.core.enums.ProductTypeEnum;
import com.hongshu.common.core.enums.ResultCodeEnum;
import com.hongshu.common.core.enums.TerminalTypeEnum;
import com.hongshu.common.core.exception.HongshuException;
import com.hongshu.common.core.utils.ConvertUtils;
import com.hongshu.common.redis.service.RedisService;
import com.hongshu.idle.async.ProductAsyncTask;
import com.hongshu.idle.domain.dto.EsProductDTO;
import com.hongshu.idle.domain.dto.ProductAppDTO;
import com.hongshu.idle.domain.dto.ProductDTO;
import com.hongshu.idle.domain.entity.IdleCollection;
import com.hongshu.idle.domain.entity.IdleProduct;
import com.hongshu.idle.domain.entity.IdleUser;
import com.hongshu.idle.domain.vo.IdleProductVO;
import com.hongshu.idle.mapper.idle.IdleCollectionMapper;
import com.hongshu.idle.mapper.idle.IdleProductMapper;
import com.hongshu.idle.mapper.idle.IdleUserMapper;
import com.hongshu.idle.service.idle.IIdleProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 商品
 *
 * @author: hongshu
 */
@Service
@Slf4j
public class IdleProductServiceImpl extends ServiceImpl<IdleProductMapper, IdleProduct> implements IIdleProductService {

    @Autowired
    private IdleUserMapper userMapper;
    @Autowired
    private IdleCollectionMapper collectionMapper;
    @Autowired
    private IdleProductMapper productMapper;
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ProductAsyncTask productAsyncTask;
    @Autowired
    private CommonAuditService commonAuditService;


    /**
     * 获取商品
     *
     * @param productId 商品ID
     */
    @Override
    public IdleProductVO getProductById(String productId) {
//        String currentUserId = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String currentUserId = AuthContextHolder.getUserId();
        IdleProduct product = productMapper.selectById(productId);
        if (ObjectUtils.isEmpty(product)) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        IdleUser user = userMapper.selectById(product.getUid());
        List<IdleCollection> collectionList = collectionMapper
                .selectList(new QueryWrapper<IdleCollection>()
                        .eq("collection_id", productId)
                        .eq("uid", currentUserId));
        Set<Integer> types = collectionList.stream().map(IdleCollection::getType).collect(Collectors.toSet());

        IdleProductVO productVO = ConvertUtils.sourceToTarget(product, IdleProductVO.class);
        productVO.setUsername(user.getUsername())
                .setAvatar(user.getAvatar())
                .setTime(product.getUpdateTime().getTime())
                .setIsCollection(types.contains(1));

        return productVO;
    }

//    /**
//     * 获取推荐商品
//     *
//     * @param currentPage 当前页
//     * @param pageSize    分页数
//     */
//    @Override
//    public Page<IdleProductVO> getRecommendProduct(long currentPage, long pageSize) {
//        Page<IdleProductVO> page = new Page<>();
//        List<IdleProductVO> noteSearchVOList = new ArrayList<>();
//        try {
//            SearchRequest.Builder builder = new SearchRequest.Builder().index(NoteConstant.PRODUCT_INDEX);
//            builder.size(1000);
//            SearchRequest searchRequest = builder.build();
//            SearchResponse<IdleProductVO> searchResponse = elasticsearchClient.search(searchRequest, IdleProductVO.class);
//            TotalHits totalHits = searchResponse.hits().total();
//            //得到所有的数据
//            List<Hit<IdleProductVO>> hits = searchResponse.hits().hits();
//            if (CollectionUtil.isNotEmpty(hits)) {
//                for (Hit<IdleProductVO> hit : hits) {
//                    IdleProductVO noteSearchVo = hit.source();
//                    noteSearchVOList.add(noteSearchVo);
//                }
//
//                Collections.shuffle(noteSearchVOList);
//                List<List<IdleProductVO>> partition = Lists.partition(noteSearchVOList, (int) pageSize);
//                List<IdleProductVO> idleProductVOS = partition.get((int) currentPage - 1);
//                page.setTotal(totalHits != null ? totalHits.value() : 0);
//                page.setRecords(idleProductVOS);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return page;
//    }

    /**
     * 搜索对应的商品
     *
     * @param currentPage  当前页
     * @param pageSize     分页数
     * @param esProductDTO 商品
     */
    @Override
    public Page<IdleProductVO> getProduct(long currentPage, long pageSize, EsProductDTO esProductDTO) {
        Page<IdleProductVO> page = new Page<>();
        List<IdleProductVO> idleProductVOList = new ArrayList<>();
        try {
            // 构建查询条件
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
            boolQueryBuilder.must(m -> m.term(t -> t.field("status").value(0)));

            // 添加 title 查询条件（全文匹配）
            if (StringUtils.isNotBlank(esProductDTO.getKeyword())) {
                boolQueryBuilder.must(m -> m.match(t -> t.field("title").query(esProductDTO.getKeyword())));
            }
            // 添加 cpid 查询条件（精确匹配）
            if (StringUtils.isNotBlank(esProductDTO.getCpid())) {
                boolQueryBuilder.must(m -> m.term(t -> t.field("cpid").value(esProductDTO.getCpid())));
            }
            // 添加 cid 查询条件（精确匹配）
            if (StringUtils.isNotBlank(esProductDTO.getCid())) {
                boolQueryBuilder.must(m -> m.term(t -> t.field("cid").value(esProductDTO.getCid())));
            }

            // 构建搜索请求
            SearchRequest.Builder builder = new SearchRequest.Builder()
                    .index(NoteConstant.PRODUCT_INDEX)
                    .query(q -> q.bool(boolQueryBuilder.build()))
                    .from((int) ((currentPage - 1) * pageSize))
                    .size((int) pageSize);

            SearchRequest searchRequest = builder.build();
            SearchResponse<IdleProductVO> searchResponse = elasticsearchClient.search(searchRequest, IdleProductVO.class);

            // 获取总命中数
            TotalHits totalHits = searchResponse.hits().total();
            // 获取命中结果
            List<Hit<IdleProductVO>> hits = searchResponse.hits().hits();

            if (CollectionUtil.isNotEmpty(hits)) {
                for (Hit<IdleProductVO> hit : hits) {
                    IdleProductVO noteSearchVo = hit.source();
                    idleProductVOList.add(noteSearchVo);
                }
                // 设置分页结果
                page.setTotal(totalHits != null ? totalHits.value() : 0);
                // 🔧 修复：填充用户信息（ES中没有存储用户头像和昵称）
                this.enrichUserInfo(idleProductVOList);
                page.setRecords(idleProductVOList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return page;
    }

    /**
     * 查找商品
     *
     * @param keyword 关键词
     */
    @Override
    public Page<IdleProductVO> getProductByKeyword(long currentPage, long pageSize, String keyword) {
        // 创建返回的VO分页对象
        Page<IdleProductVO> voPage = new Page<>(currentPage, pageSize);
        // 获取当前用户信息
//        String userId = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String currentUid = AuthContextHolder.getUserId();
        // 查询商品数据，只查询可售的商品（status=0），已售出或被锁定的商品不显示
        Page<IdleProduct> productPage = productMapper.selectPage(
                new Page<>((int) currentPage, (int) pageSize),
                new QueryWrapper<IdleProduct>()
                        .like("title", keyword)
                        .eq("status", 0)
        );
        // 转换数据
        List<IdleProductVO> voList = productPage.getRecords().stream().map(product -> {
                    // 使用 ConvertUtils 转换对象
                    IdleProductVO productVO = ConvertUtils.sourceToTarget(product, IdleProductVO.class);
                    // 设置用户信息
                    IdleUser productUser = userMapper.selectById(product.getUid());
                    if (productUser != null) {
                        productVO.setUsername(productUser.getUsername());
                        productVO.setAvatar(productUser.getAvatar());
                    }
                    return productVO;
                })
                .collect(Collectors.toList());
        // 设置分页信息和数据
        voPage.setRecords(voList);
        voPage.setTotal(productPage.getTotal());
        voPage.setCurrent(productPage.getCurrent());
        voPage.setSize(productPage.getSize());
        return voPage;
    }

    /**
     * 新增闲置商品
     *
     * @param terminal  来源标识
     * @param requestId 唯一标识
     * @param noteData  商品对象
     * @param coverFile 封面图
     * @param files     图片文件
     */
    @Override
    public void saveProductByDTO(String terminal, String requestId, String noteData, MultipartFile coverFile, MultipartFile[] files) {
        // 幂等性校验，防止重复提交
        String redisKey = "product:save:" + requestId;
        if (Boolean.TRUE.equals(redisService.hasKey(redisKey))) {
            throw new RuntimeException("请勿重复提交");
        }
        // 设置标记，5分钟后自动过期
        redisService.setCacheObject(redisKey, "1", 5L, TimeUnit.MINUTES);

        // 保存封面到本地临时目录
        String coverLocalPath = this.saveToLocalTemp(coverFile);
        // 保存内容文件到本地临时目录
        List<String> fileLocalPaths = new ArrayList<>();
        for (MultipartFile file : files) {
            fileLocalPaths.add(this.saveToLocalTemp(file));
        }

        // 调用异步任务
        String currentUid = AuthContextHolder.getUserId();
        productAsyncTask.addProduct(terminal, currentUid, noteData, coverLocalPath, fileLocalPaths);
    }

    @Override
    public void saveProduct(String terminal, String requestId, ProductAppDTO productDTO) {
        if ("1".equals(productDTO.getProductType())) {
            // 图片类型
            if (productDTO.getUrls() == null) {
                throw new RuntimeException("未上传任何图片");
            }
        } else if ("2".equals(productDTO.getProductType())) {
            // 视频类型
            if (productDTO.getUrls() == null) {
                throw new RuntimeException("请上传一个视频文件");
            }
        }
        // 幂等性校验，防止重复提交
        String redisKey = "product:save:" + requestId;
        if (Boolean.TRUE.equals(redisService.hasKey(redisKey))) {
            throw new RuntimeException("请勿重复提交");
        }
        // 设置标记，5分钟后自动过期
        redisService.setCacheObject(redisKey, "1", 5L, TimeUnit.MINUTES);

        String currentUid = AuthContextHolder.getUserId();
        try {
            // 更新用户商品数量
            IdleUser user = userMapper.selectById(currentUid);
            user.setProductCount(user.getProductCount() + 1);
            userMapper.updateById(user);

            // 保存商品
            IdleProduct product = ConvertUtils.sourceToTarget(productDTO, IdleProduct.class);
            product.setUid(currentUid);
            String urls = JSONUtil.toJsonStr(productDTO.getUrls().toArray());
            product.setUrls(urls);
            product.setFromType(TerminalTypeEnum.fromValue(terminal).getCode());
            product.setAuthor(user.getUsername());
            product.setDescription(productDTO.getContent());
            product.setProductType(Integer.valueOf(productDTO.getProductType()));
            product.setCreator(user.getUsername());
            product.setCreateTime(new Date());
            product.setUpdateTime(new Date());

            // 获取审核配置（使用 CommonAuditService 统一处理）
            Boolean auditEnabled = commonAuditService.isContentAuditEnabled();

            // 设置审核状态
            if (Boolean.TRUE.equals(auditEnabled)) {
                product.setAuditStatus(AuditStatusEnum.REVIEW.getCode()); // 先设为审核中
                product.setType(ProductTypeEnum.PEND_LIST.getCode());
            } else {
                product.setAuditStatus(AuditStatusEnum.PASS.getCode());
                product.setType(ProductTypeEnum.ONLINE.getCode());
            }

            // 保存商品
            productMapper.insert(product);

            String productId = product.getId();
            if (productId != null) {
                // 如果审核未开启，商品直接通过，同步到ES
                if (!Boolean.TRUE.equals(auditEnabled)) {
                    // 商品直接通过，同步到ES（使用异步任务中的同步方法）
                    try {
                        productAsyncTask.syncProductToEs(product);
                        log.info("商品发布成功并同步到ES，商品ID: {}", productId);
                    } catch (Exception e) {
                        log.error("商品同步到ES失败，商品ID: {}", productId, e);
                        // ES同步失败不影响主流程，只记录日志
                    }
                } else {
                    // 异步执行审核（不阻塞发布流程）
                    List<String> dataList = new ArrayList<>();
                    if (productDTO.getUrls() != null && !productDTO.getUrls().isEmpty()) {
                        dataList.addAll(productDTO.getUrls());
                    }
                    // 异步审核
                    productAsyncTask.auditProductAsync(productId, product.getDescription(), dataList, product.getProductType());
                }
            }
        } catch (Exception e) {
            log.error("商品保存失败", e);
            throw new RuntimeException("商品保存失败: " + e.getMessage());
        }
    }

    /**
     * 保存文件到本地临时目录
     */
    private String saveToLocalTemp(MultipartFile file) {
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File localFile = new File(tempDir, fileName);
        try {
            file.transferTo(localFile);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
        return localFile.getAbsolutePath();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductByDTO(String productData, MultipartFile coverFile, MultipartFile[] files) {
        String currentUid = AuthContextHolder.getUserId();
        // 解析前端传来的数据
        ProductDTO productDTO = JSONUtil.toBean(productData, ProductDTO.class);
        // 查询原有商品
        IdleProduct product = productMapper.selectById(productDTO.getId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!product.getUid().equals(currentUid)) {
            throw new RuntimeException("无权编辑该商品");
        }
        // 保存封面到本地临时目录（如果有上传新封面）
        String coverLocalPath = null;
        if (coverFile != null && !coverFile.isEmpty()) {
            coverLocalPath = this.saveToLocalTemp(coverFile);
        }
        // 保存内容文件到本地临时目录（如果有上传新媒体）
        List<String> fileLocalPaths = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    fileLocalPaths.add(this.saveToLocalTemp(file));
                }
            }
        }

        // 调用异步处理
        productAsyncTask.updateProduct(productDTO, product, coverLocalPath, fileLocalPaths);
    }

    /**
     * 置顶商品
     *
     * @param productId 商品ID
     */
    @Override
    public boolean pinnedProduct(String productId) {
//        String currentUid = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String currentUid = AuthContextHolder.getUserId();
        IdleProduct product = this.getById(productId);
        if ("1".equals(product.getPinned())) {
            product.setPinned("0");
        } else {
            List<IdleProduct> productList = this.list(new QueryWrapper<IdleProduct>().eq("uid", currentUid));
            if (CollectionUtil.isNotEmpty(productList)) {
                long count = productList.stream().filter(item -> "1".equals(item.getPinned())).count();
                if (count >= 3) {
                    throw new HongshuException("最多只能置顶3个笔记");
                }
                product.setPinned("1");
                product.setUpdateTime(new Date());
            }
        }
        return updateById(product);
    }

    @Override
    public Object getIdleCount(String userId) {
        int publishCount = Math.toIntExact(productMapper.selectCount(
                new QueryWrapper<IdleProduct>()
                        .eq("uid", userId)));
        int sellCount = Math.toIntExact(productMapper.selectCount(
                new QueryWrapper<IdleProduct>()
                        .eq("uid", userId)
                        .isNotNull("buy_uid")
                        .and(wrapper -> wrapper
                                .eq("status", 1)
                                .or()
                                .eq("status", 3)
                                .or()
                                .eq("status", 4)
                        )));
        int buyCount = Math.toIntExact(productMapper.selectCount(
                new QueryWrapper<IdleProduct>()
                        .eq("buy_uid", userId)
                        .isNotNull("buy_uid")
                        .and(wrapper -> wrapper
                                .eq("status", 1)
                                .or()
                                .eq("status", 3)
                                .or()
                                .eq("status", 4)
                        )));
        int collectCount = Math.toIntExact(collectionMapper.selectCount(
                new QueryWrapper<IdleCollection>()
                        .eq("uid", userId)));
        Map<String, Object> result = new HashMap<>(3);
        result.put("publishCount", publishCount);
        result.put("sellCount", sellCount);
        result.put("buyCount", buyCount);
        result.put("collectCount", collectCount);
        return result;
    }

    /**
     * 填充商品列表的用户信息（头像和昵称）
     * ES中的数据可能不包含用户信息，需要从数据库中查询补充
     *
     * @param productList 商品列表
     */
    private void enrichUserInfo(List<IdleProductVO> productList) {
        if (productList == null || productList.isEmpty()) {
            return;
        }

        log.debug("开始填充用户信息，商品数量: {}", productList.size());

        for (IdleProductVO product : productList) {
            try {
                // 如果已经有用户信息，跳过
                if (StringUtils.isNotBlank(product.getAvatar()) && StringUtils.isNotBlank(product.getUsername())) {
                    continue;
                }

                // 查询用户信息
                if (StringUtils.isNotBlank(product.getUid())) {
                    IdleUser user = userMapper.selectOne(
                            new QueryWrapper<IdleUser>()
                                    .eq("id", product.getUid())
                    );
                    if (user != null) {
                        product.setAvatar(user.getAvatar());
                        product.setUsername(user.getUsername());
                        log.debug("填充用户信息成功: productId={}, uid={}, username={}",
                                product.getId(), product.getUid(), user.getUsername());
                    } else {
                        log.warn("未找到用户信息: productId={}, uid={}", product.getId(), product.getUid());
                    }
                }
            } catch (Exception e) {
                log.error("填充用户信息失败: productId={}, uid={}", product.getId(), product.getUid(), e);
            }
        }

        log.debug("用户信息填充完成");
    }
}
