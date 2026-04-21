package com.hongshu.idle.service.idle.impl;

import cn.hutool.core.collection.CollectionUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.core.constant.NoteConstant;
import com.hongshu.common.core.context.AuthContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongshu.common.core.enums.AuditStatusEnum;
import com.hongshu.common.core.enums.ProductEnum;
import com.hongshu.common.core.exception.HongshuException;
import com.hongshu.common.core.utils.ConvertUtils;
import com.hongshu.common.core.utils.RedisUtils;
import com.hongshu.common.core.utils.bean.BeanUtils;
import com.hongshu.idle.domain.dto.EsProductDTO;
import com.hongshu.idle.domain.dto.SystemConfigDTO;
import com.hongshu.idle.domain.entity.IdleCategory;
import com.hongshu.idle.domain.entity.IdleProduct;
import com.hongshu.idle.domain.entity.WebUser;
import com.hongshu.idle.domain.vo.IdleProductVO;
import com.hongshu.idle.mapper.idle.IdleProductMapper;
import com.hongshu.idle.mapper.sys.SysIdleCategoryMapper;
import com.hongshu.idle.mapper.web.WebUserMapper;
import com.hongshu.idle.service.IRecommendProductService;
import com.hongshu.idle.service.idle.IIdleEsProductService;
import com.hongshu.idle.service.recommendation.LightweightProductRecommendService;
import com.hongshu.idle.service.sys.ISysSystemConfigService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * ES
 *
 * @author: hongshu
 */
@Service
@Slf4j
public class IdleEsProductServiceImpl extends ServiceImpl<IdleProductMapper, IdleProduct> implements IIdleEsProductService {

    private static final int RECOMMEND_GUEST_POOL = 400;
    private static final String CACHE_KEY_RECOMMEND = "idle:recommend:product:";
    private static final String CACHE_KEY_VIDEO = "idle:video:product:";
    private static final long RECOMMEND_CACHE_TTL_SEC = 300;
    private static final long VIDEO_CACHE_TTL_SEC = 180;
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    @Autowired
    private IRecommendProductService recommendService;
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    @Autowired
    private ISysSystemConfigService systemConfigService;
    @Autowired
    private IdleProductMapper productMapper;
    @Autowired
    private LightweightProductRecommendService lightweightProductRecommendService;
    @Autowired
    private WebUserMapper userMapper;
    @Autowired
    private SysIdleCategoryMapper idleCategoryMapper;
    @Autowired
    private RedisUtils redisUtils;


    /**
     * 搜索对应的商品
     *
     * @param currentPage  当前页
     * @param pageSize     分页数
     * @param esProductDTO 笔记
     */
    @Override
    public Page<IdleProductVO> getProductByDTO(long currentPage, long pageSize, EsProductDTO esProductDTO) {
        Page<IdleProductVO> page = new Page<>();

        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        String queryPrimary = systemConfig.getQueryPrimary();

        // ES检索
        if ("es".equals(queryPrimary)) {
            return getProductByDTOFromES(currentPage, pageSize, esProductDTO);
        }
        // MySQL检索
        else if ("mysql".equals(queryPrimary)) {
            return getProductByDTOFromMysql(currentPage, pageSize, esProductDTO);
        } else {
            return getProductByDTOFromES(currentPage, pageSize, esProductDTO);
        }

//        Page<IdleProductVO> page = new Page<>();
//        List<IdleProductVO> idleProductVOList = new ArrayList<>();
//        try {
//            SearchRequest.Builder builder = new SearchRequest.Builder().index(NoteConstant.PRODUCT_INDEX);
//            if (StringUtils.isNotBlank(esProductDTO.getKeyword())) {
//                builder.query(q -> q.bool(b -> b
//                        .should(h -> h.match(f -> f.field("title").boost(1f).query(esProductDTO.getKeyword())))
//                        .should(h -> h.match(f -> f.field("username").boost(0.5f).query(esProductDTO.getKeyword())))
//                        .should(h -> h.match(f -> f.field("content").boost(1f).query(esProductDTO.getKeyword())))
//                        .should(h -> h.match(f -> f.field("tags").boost(4f).query(esProductDTO.getKeyword())))
//                        .should(h -> h.match(f -> f.field("categoryName").boost(2f).query(esProductDTO.getKeyword())))
//                        .should(h -> h.match(f -> f.field("categoryParentName").boost(1.5f).query(esProductDTO.getKeyword())))
//                ));
//            }
//            if (StringUtils.isNotBlank(esProductDTO.getCpid()) && StringUtils.isNotBlank(esProductDTO.getCid())) {
//                builder.query(q -> q.bool(b -> b
//                        .must(h -> h.match(m -> m.field("cid").query(esProductDTO.getCid())))
//                        .must(h -> h.match(m -> m.field("cpid").query(esProductDTO.getCpid())))
//                ));
//            } else if (StringUtils.isNotBlank(esProductDTO.getCpid())) {
//                builder.query(h -> h.match(m -> m.field("cpid").query(esProductDTO.getCpid())));
//                builder.sort(h -> h.field(m -> m.field("time").order(SortOrder.Desc)));
//            }
//
//            if (esProductDTO.getType() == 1) {
//                builder.sort(o -> o.field(f -> f.field("likeCount").order(SortOrder.Desc)));
//            } else if (esProductDTO.getType() == 2) {
//                builder.sort(o -> o.field(f -> f.field("time").order(SortOrder.Desc)));
//            }
//            builder.from((int) (currentPage - 1) * (int) pageSize);
//            builder.size((int) pageSize);
//            SearchRequest searchRequest = builder.build();
//            SearchResponse<IdleProductVO> searchResponse = elasticsearchClient.search(searchRequest, IdleProductVO.class);
//            TotalHits totalHits = searchResponse.hits().total();
//            page.setTotal(Objects.requireNonNull(totalHits).value());
//            page.setCountId(esProductDTO.getCpid());
//            List<Hit<IdleProductVO>> hits = searchResponse.hits().hits();
//            for (Hit<IdleProductVO> hit : hits) {
//                IdleProductVO idleProductVO = hit.source();
//                idleProductVOList.add(idleProductVO);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new HongshuException("es查找数据异常");
//        }
//        page.setRecords(idleProductVOList);
//        return page;
    }

    /**
     * 从ES搜索商品
     */
    private Page<IdleProductVO> getProductByDTOFromES(long currentPage, long pageSize, EsProductDTO esProductDTO) {
        Page<IdleProductVO> page = new Page<>();
        List<IdleProductVO> idleProductVOList = new ArrayList<>();
        try {
            SearchRequest.Builder builder = new SearchRequest.Builder().index(NoteConstant.PRODUCT_INDEX);
            // 构建bool查询
            BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
            // 关键词搜索
            if (StringUtils.isNotBlank(esProductDTO.getKeyword())) {
                boolBuilder.must(m -> m.bool(bb -> bb
                        .should(h -> h.match(f -> f.field("title").boost(1f).query(esProductDTO.getKeyword())))
                        .should(h -> h.match(f -> f.field("username").boost(0.5f).query(esProductDTO.getKeyword())))
                        .should(h -> h.match(f -> f.field("content").boost(1f).query(esProductDTO.getKeyword())))
                        .should(h -> h.match(f -> f.field("tags").boost(4f).query(esProductDTO.getKeyword())))
                        .should(h -> h.match(f -> f.field("categoryName").boost(2f).query(esProductDTO.getKeyword())))
                        .should(h -> h.match(f -> f.field("categoryParentName").boost(1.5f).query(esProductDTO.getKeyword())))
                ));
            }
            // 分类条件
            if (StringUtils.isNotBlank(esProductDTO.getCpid()) && StringUtils.isNotBlank(esProductDTO.getCid())) {
                boolBuilder.must(h -> h.match(m -> m.field("cid").query(esProductDTO.getCid())));
                boolBuilder.must(h -> h.match(m -> m.field("cpid").query(esProductDTO.getCpid())));
            } else if (StringUtils.isNotBlank(esProductDTO.getCpid())) {
                boolBuilder.must(h -> h.match(m -> m.field("cpid").query(esProductDTO.getCpid())));
            } else if (StringUtils.isNotBlank(esProductDTO.getCid())) {
                boolBuilder.must(h -> h.match(m -> m.field("cid").query(esProductDTO.getCid())));
            }
            // 只查询可售的商品（status=0），已售出或被锁定的商品不显示
            boolBuilder.must(m -> m.term(t -> t.field("status").value(0)));
            // 设置查询
            builder.query(q -> q.bool(boolBuilder.build()));
            // 🔧 修复：使用正常的pageSize，而不是3倍数据
            // 排序逻辑
            if (esProductDTO.getType() == 1) {
                // 图片类型按点赞数排序，保持热度排序
                builder.sort(o -> o.field(f -> f.field("likeCount").order(SortOrder.Desc)));
            } else if (esProductDTO.getType() == 2) {
                // 视频类型按时间排序
                builder.sort(o -> o.field(f -> f.field("time").order(SortOrder.Desc)));
            } else {
                // 其他情况按时间排序
                builder.sort(h -> h.field(m -> m.field("time").order(SortOrder.Desc)));
            }
            // 🔧 修复：使用正确的分页参数
            builder.from((int) (currentPage - 1) * (int) pageSize);
            builder.size((int) pageSize); // 使用正常的pageSize

            SearchRequest searchRequest = builder.build();
            SearchResponse<IdleProductVO> searchResponse = elasticsearchClient.search(searchRequest, IdleProductVO.class);
            TotalHits totalHits = searchResponse.hits().total();
            page.setTotal(Objects.requireNonNull(totalHits).value());
            page.setCountId(esProductDTO.getCpid());
            List<Hit<IdleProductVO>> hits = searchResponse.hits().hits();
            for (Hit<IdleProductVO> hit : hits) {
                IdleProductVO idleProductVO = hit.source();
                idleProductVOList.add(idleProductVO);
            }

            // 🔧 修复：移除后端随机打乱逻辑，由前端处理随机性
            // 这样可以确保分页逻辑正确，避免数据重复和分页失效
        } catch (Exception e) {
            e.printStackTrace();
            throw new HongshuException("es查找数据异常");
        }
        // 🔧 修复：填充用户信息（ES中没有存储用户头像和昵称）
        this.enrichUserInfo(idleProductVOList);
        // 🔧 修复：确保返回的current字段正确
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        page.setRecords(idleProductVOList);
        return page;
    }

    /**
     * 从MySQL搜索商品
     */
    private Page<IdleProductVO> getProductByDTOFromMysql(long currentPage, long pageSize, EsProductDTO esProductDTO) {
        Page<IdleProductVO> page = new Page<>();
        // 构建查询条件
        QueryWrapper<IdleProduct> queryWrapper = new QueryWrapper<IdleProduct>()
                .eq("deleted", 0)
                .eq("status", 0) // 只查询可售的商品（status=0），已售出或被锁定的商品不显示
                .eq("audit_status", "APPROVED"); // 假设审核通过的商品状态
        // 关键词搜索
        if (StringUtils.isNotBlank(esProductDTO.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like("title", esProductDTO.getKeyword())
                    .or().like("content", esProductDTO.getKeyword())
                    .or().like("tags", esProductDTO.getKeyword())
                    .or().like("username", esProductDTO.getKeyword())
            );
        }
        // 分类条件
        if (StringUtils.isNotBlank(esProductDTO.getCpid()) && StringUtils.isNotBlank(esProductDTO.getCid())) {
            queryWrapper.eq("cpid", esProductDTO.getCpid())
                    .eq("cid", esProductDTO.getCid());
        } else if (StringUtils.isNotBlank(esProductDTO.getCpid())) {
            queryWrapper.eq("cpid", esProductDTO.getCpid());
        }
        // 🔧 修复：使用正常的pageSize，而不是3倍数据
        // 排序逻辑
        if (esProductDTO.getType() == 1) {
            // 按点赞数排序，保持热度排序
            queryWrapper.orderByDesc("like_count");
        } else if (esProductDTO.getType() == 2) {
            // 按时间排序
            queryWrapper.orderByDesc("time");
        } else {
            // 默认按时间排序
            queryWrapper.orderByDesc("time");
        }

        // 🔧 修复：使用正常的pageSize进行分页查询
        Page<IdleProduct> productPage = productMapper.selectPage(new Page<>(currentPage, pageSize), queryWrapper);
        // 转换结果
        List<IdleProductVO> productVOList = productPage.getRecords().stream()
                .map(this::convertToProductVO)
                .collect(Collectors.toList());

        // 🔧 修复：移除后端随机打乱逻辑，由前端处理随机性
        // 这样可以确保分页逻辑正确，避免数据重复和分页失效

        // 🔧 修复：填充用户信息（确保用户信息完整）
        enrichUserInfo(productVOList);

        // 🔧 修复：确保返回的current字段正确
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        page.setTotal(productPage.getTotal());
        page.setCountId(esProductDTO.getCpid());
        page.setRecords(productVOList);
        return page;
    }

    /**
     * 获取推荐商品
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @SneakyThrows
    @Override
    public Page<IdleProductVO> getRecommendProduct(long currentPage, long pageSize) {
        Page<IdleProductVO> page = new Page<>();

        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        String queryPrimary = systemConfig.getQueryPrimary();

        // 轻量级推荐（推荐使用）
        if ("lightweight".equals(queryPrimary)) {
            log.info("使用轻量级商品推荐策略");
            String userId = null;
            try {
                userId = AuthContextHolder.getUserId();
            } catch (Exception e) {
                log.debug("用户未登录");
            }

            String cacheKey = CACHE_KEY_RECOMMEND + (userId != null ? userId : "anon") + ":" + currentPage + ":" + pageSize;
            Page<IdleProductVO> cached = getProductPageFromCache(cacheKey);
            if (cached != null) {
                Collections.shuffle(cached.getRecords(), ThreadLocalRandom.current());
                log.info("推荐商品命中缓存, key={}", cacheKey);
                return cached;
            }

            Page<IdleProductVO> resultPage = lightweightProductRecommendService.getRecommendProducts(userId, currentPage, pageSize);
            if (resultPage != null) {
                resultPage.setCurrent(currentPage);
                resultPage.setSize(pageSize);
                if (resultPage.getRecords() != null && !resultPage.getRecords().isEmpty()) {
                    putProductPageToCache(cacheKey, resultPage, RECOMMEND_CACHE_TTL_SEC);
                }
            }
            return resultPage;
        }
        // ES检索
        else if ("es".equals(queryPrimary)) {
            return getRecommendProductFromES(currentPage, pageSize);
        }
        // MySQL检索
        else if ("mysql".equals(queryPrimary)) {
            return getRecommendProductFromMysql(currentPage, pageSize);
        }

        return page;
//        Page<IdleProductVO> page = new Page<>();
//
//        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
//        String queryPrimary = systemConfig.getQueryPrimary();
//        // ES检索
//        if ("es".equals(queryPrimary)) {
//            String userId = AuthContextHolder.getUserId();
//            List<IdleProductVO> recommendList;
//            // 用户ID为空 默认随机加载100条数据
//            if (StringUtils.isBlank(userId)) {
//                SearchRequest searchRequest = SearchRequest.of(s -> s
//                        .index(NoteConstant.PRODUCT_INDEX)
//                        .size(100));
//                SearchResponse<IdleProductVO> searchResponse = elasticsearchClient.search(searchRequest, IdleProductVO.class);
//                recommendList = searchResponse.hits().hits().stream()
//                        .map(Hit::source)
//                        .collect(Collectors.toList());
//                // 随机排序
//                Collections.shuffle(recommendList, new Random());
//            } else {
//                // 获取推荐列表
//                recommendList = recommendService.getRecommendProduct(Long.parseLong(userId));
//            }
//            List<List<IdleProductVO>> partition = Lists.partition(recommendList, (int) pageSize);
//            // 如果 currentPage 超出范围，返回空记录
//            if (currentPage > partition.size() || currentPage <= 0) {
//                page.setTotal(0);
//                page.setRecords(Collections.emptyList());
//                return page;
//            }
//            List<IdleProductVO> idleProductVOList = partition.get((int) currentPage - 1);
//            page.setTotal(recommendList.size());
//            page.setRecords(idleProductVOList);
//            return page;
//        }
//        // MySQL检索
//        else if ("mysql".equals(queryPrimary)) {
//            String userId = AuthContextHolder.getUserId();
//            if (StringUtils.isBlank(userId)) {
//                // 用户未登录，随机获取100条公开笔记
//                List<IdleProductVO> randomProducts = productMapper.selectList(
//                                new QueryWrapper<IdleProduct>()
//                                        .eq("audit_status", AuditStatusEnum.PASS.getCode())
//                                        .orderBy(true, false, "RAND()")
//                                        .last("LIMIT " + 100)
//                        ).stream()
//                        .map(this::convertToProductVO)
//                        .collect(Collectors.toList());
//                // 随机排序
//                Collections.shuffle(randomProducts, new Random());
//                // 分页处理
//                List<List<IdleProductVO>> partition = Lists.partition(randomProducts, (int) pageSize);
//                if (currentPage > partition.size() || currentPage <= 0) {
//                    page.setTotal(0);
//                    page.setRecords(Collections.emptyList());
//                    return page;
//                }
//                List<IdleProductVO> noteSearchVos = partition.get((int) currentPage - 1);
//                page.setTotal(randomProducts.size());
//                page.setRecords(noteSearchVos);
//            } else {
//                // 用户已登录，通过推荐算法获取推荐列表
//                page = recommendService.getRecommendProductFromMysql(userId, currentPage, pageSize);
//            }
//            return page;
//        }
//        return page;
    }

    /**
     * 获取视频商品
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @Override
    public Page<IdleProductVO> getVideoProduct(long currentPage, long pageSize) {
        String cacheKey = CACHE_KEY_VIDEO + currentPage + ":" + pageSize;
        Page<IdleProductVO> cached = getProductPageFromCache(cacheKey);
        if (cached != null) {
            Collections.shuffle(cached.getRecords(), ThreadLocalRandom.current());
            log.info("视频商品命中缓存, key={}", cacheKey);
            return cached;
        }

        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        String queryPrimary = systemConfig.getQueryPrimary();

        Page<IdleProductVO> result;
        if ("es".equals(queryPrimary)) {
            result = getVideoProductFromES(currentPage, pageSize);
        } else if ("mysql".equals(queryPrimary)) {
            result = getVideoProductFromMysql(currentPage, pageSize);
        } else {
            result = getVideoProductFromES(currentPage, pageSize);
        }

        if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
            putProductPageToCache(cacheKey, result, VIDEO_CACHE_TTL_SEC);
        }
        return result;
    }

    /**
     * 从ES获取视频商品
     */
    private Page<IdleProductVO> getVideoProductFromES(long currentPage, long pageSize) {
        Page<IdleProductVO> page = new Page<>();
        List<IdleProductVO> idleProductVOList = new ArrayList<>();

        try {
            SearchRequest.Builder builder = new SearchRequest.Builder().index(NoteConstant.PRODUCT_INDEX);
            // 构建bool查询
            BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

            // 固定查询视频类型（productType=2）
            // ES中可能存储为整数或字符串，先尝试整数类型（因为IdleProductVO中productType是Integer）
            int videoType = Integer.parseInt(ProductEnum.VIDEO.getCode());
            boolBuilder.must(m -> m.term(t -> t.field("productType").value(videoType)));

            // 只查询审核通过的商品（auditStatus="1"）
            String passStatus = AuditStatusEnum.PASS.getCode();
            boolBuilder.must(m -> m.term(t -> t.field("auditStatus").value(passStatus)));

            // 只查询可售的商品（status=0），已售出或被锁定的商品不显示
            boolBuilder.must(m -> m.term(t -> t.field("status").value(0)));

            SearchRequest searchRequest = builder
                    .query(q -> q.bool(boolBuilder.build()))
                    .from((int) ((currentPage - 1) * pageSize))
                    .size((int) pageSize)
                    .sort(s -> s.field(f -> f.field("updateTime").order(SortOrder.Desc)))
                    .build();

            log.info("ES查询视频商品 - productType: {} (int), auditStatus: {} (string)",
                    videoType, passStatus);

            SearchResponse<IdleProductVO> searchResponse = elasticsearchClient.search(searchRequest, IdleProductVO.class);

            // 处理搜索结果
            for (Hit<IdleProductVO> hit : searchResponse.hits().hits()) {
                IdleProductVO idleProductVO = hit.source();
                if (idleProductVO != null) {
                    idleProductVOList.add(idleProductVO);
                }
            }

            enrichUserInfo(idleProductVOList);

            // 设置分页信息
            page.setCurrent(currentPage);
            page.setSize(pageSize);
            TotalHits totalHits = searchResponse.hits().total();
            long total = totalHits != null ? totalHits.value() : 0;
            page.setTotal(total);
            page.setRecords(idleProductVOList);

            log.info("ES查询视频商品结果 - 总数: {}, 当前页数据量: {}", total, idleProductVOList.size());

            // 如果查询结果为0，尝试用字符串类型查询（用于调试）
            if (total == 0) {
                log.warn("使用整数类型查询结果为0，尝试使用字符串类型查询...");
                try {
                    BoolQuery.Builder strBoolBuilder = new BoolQuery.Builder();
                    String videoTypeStr = String.valueOf(videoType);
                    strBoolBuilder.must(m -> m.term(t -> t.field("productType").value(videoTypeStr)));
                    strBoolBuilder.must(m -> m.term(t -> t.field("auditStatus").value(passStatus)));

                    // 创建新的builder，不能复用之前的builder
                    SearchRequest.Builder strBuilder = new SearchRequest.Builder().index(NoteConstant.PRODUCT_INDEX);
                    SearchRequest strSearchRequest = strBuilder
                            .query(q -> q.bool(strBoolBuilder.build()))
                            .from((int) ((currentPage - 1) * pageSize))
                            .size((int) pageSize)
                            .sort(s -> s.field(f -> f.field("updateTime").order(SortOrder.Desc)))
                            .build();

                    SearchResponse<IdleProductVO> strSearchResponse = elasticsearchClient.search(strSearchRequest, IdleProductVO.class);
                    TotalHits strTotalHits = strSearchResponse.hits().total();
                    long strTotal = strTotalHits != null ? strTotalHits.value() : 0;
                    log.info("使用字符串类型查询结果 - 总数: {}", strTotal);

                    if (strTotal > 0) {
                        // 如果字符串类型查询有结果，使用字符串类型的结果
                        idleProductVOList.clear();
                        for (Hit<IdleProductVO> hit : strSearchResponse.hits().hits()) {
                            IdleProductVO idleProductVO = hit.source();
                            if (idleProductVO != null) {
                                idleProductVOList.add(idleProductVO);
                            }
                        }
                        // 🔧 修复：填充用户信息（ES中没有存储用户头像和昵称）
                        enrichUserInfo(idleProductVOList);
                        page.setTotal(strTotal);
                        page.setRecords(idleProductVOList);
                    }
                } catch (Exception e) {
                    log.warn("尝试字符串类型查询时出错: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("从ES获取视频商品失败", e);
            page.setCurrent(currentPage);
            page.setSize(pageSize);
            page.setTotal(0);
            page.setRecords(Collections.emptyList());
        }
        return page;
    }

    /**
     * 从MySQL获取视频商品
     */
    private Page<IdleProductVO> getVideoProductFromMysql(long currentPage, long pageSize) {
        Page<IdleProductVO> page = new Page<>();

        QueryWrapper<IdleProduct> queryWrapper = new QueryWrapper<IdleProduct>()
                .eq("deleted", 0)
                .eq("status", 0)
                .eq("audit_status", AuditStatusEnum.PASS.getCode())
                .eq("product_type", 2)
                .orderByDesc("update_time");

        Page<IdleProduct> productPage = productMapper.selectPage(new Page<>(currentPage, pageSize), queryWrapper);

        List<IdleProductVO> productVOList = productPage.getRecords().stream()
                .map(this::convertToProductVO)
                .collect(Collectors.toList());

        enrichUserInfo(productVOList);

        page.setCurrent(productPage.getCurrent());
        page.setSize(pageSize);
        page.setTotal(productPage.getTotal());
        page.setRecords(productVOList);
        return page;
    }

    /**
     * 从ES获取推荐商品
     */
    private Page<IdleProductVO> getRecommendProductFromES(long currentPage, long pageSize) {
        String userId = AuthContextHolder.getUserId();
        List<IdleProductVO> recommendList;

        if (StringUtils.isBlank(userId)) {
            // 用户未登录，随机获取100条数据
            recommendList = getRandomProductsFromES(RECOMMEND_GUEST_POOL);
        } else {
            // 用户已登录，通过推荐算法获取推荐列表
            recommendList = recommendService.getRecommendProduct(Long.parseLong(userId));
            // 🔧 修复：对推荐列表进行随机打乱，增加刷新时的变化
            if (recommendList != null && !recommendList.isEmpty()) {
                log.info("🔄 推荐商品列表应用随机排序，原始数据量={}, pageSize={}", recommendList.size(), pageSize);
                Collections.shuffle(recommendList, new Random(System.currentTimeMillis()));
                log.info("✅ 推荐商品列表随机排序完成");
            }
        }

        return buildProductPageFromList(recommendList, currentPage, pageSize);
    }

    /**
     * 从MySQL获取推荐商品
     */
    private Page<IdleProductVO> getRecommendProductFromMysql(long currentPage, long pageSize) {
        String userId = AuthContextHolder.getUserId();

        if (StringUtils.isBlank(userId)) {
            // 用户未登录，随机获取100条公开商品
            List<IdleProductVO> randomProducts = getRandomProductsFromMysql(RECOMMEND_GUEST_POOL);
            return buildProductPageFromList(randomProducts, currentPage, pageSize);
        } else {
            // 用户已登录，通过推荐算法获取推荐列表
            Page<IdleProductVO> page = recommendService.getRecommendProductFromMysql(userId, currentPage, pageSize);
            // 🔧 修复：对推荐结果进行随机打乱，增加刷新时的变化
            if (page != null && page.getRecords() != null && !page.getRecords().isEmpty()) {
                log.info("🔄 MySQL推荐商品列表应用随机排序，原始数据量={}, pageSize={}", page.getRecords().size(), pageSize);
                List<IdleProductVO> records = page.getRecords();
                Collections.shuffle(records, new Random(System.currentTimeMillis()));
                // 🔧 修复：填充用户信息（确保用户信息完整）
                enrichUserInfo(records);
                page.setRecords(records);
                log.info("✅ MySQL推荐商品列表随机排序完成");
            }
            return page;
        }
    }

    /**
     * 从ES随机获取指定数量的商品
     */
    private List<IdleProductVO> getRandomProductsFromES(int limit) {
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(NoteConstant.PRODUCT_INDEX)
                .size(limit));
        SearchResponse<IdleProductVO> searchResponse = null;
        try {
            searchResponse = elasticsearchClient.search(searchRequest, IdleProductVO.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<IdleProductVO> recommendList = searchResponse.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
        // 随机排序
        Collections.shuffle(recommendList, new Random());
        return recommendList;
    }

    /**
     * 从MySQL随机获取指定数量的公开商品
     */
    private List<IdleProductVO> getRandomProductsFromMysql(int limit) {
        List<IdleProduct> rows = productMapper.selectList(
                new QueryWrapper<IdleProduct>()
                        .eq("deleted", 0)
                        .eq("status", 0)
                        .eq("audit_status", AuditStatusEnum.PASS.getCode())
                        .orderByDesc("like_count")
                        .last("LIMIT " + (limit * 2))
        );
        if (rows.size() > limit) {
            Collections.shuffle(rows, ThreadLocalRandom.current());
            rows = rows.subList(0, limit);
        }
        return rows.stream()
                .map(this::convertToProductVO)
                .collect(Collectors.toList());
    }

    /**
     * 从商品列表构建分页结果
     */
    private Page<IdleProductVO> buildProductPageFromList(List<IdleProductVO> productList, long currentPage, long pageSize) {
        Page<IdleProductVO> page = new Page<>();
        List<List<IdleProductVO>> partition = Lists.partition(productList, (int) pageSize);

        // 如果 currentPage 超出范围，返回空记录
        if (currentPage > partition.size() || currentPage <= 0) {
            page.setTotal(0);
            page.setRecords(Collections.emptyList());
            return page;
        }

        List<IdleProductVO> idleProductVOList = partition.get((int) currentPage - 1);
        // 🔧 修复：填充用户信息（确保用户信息完整）
        enrichUserInfo(idleProductVOList);
        page.setTotal(productList.size());
        page.setRecords(idleProductVOList);
        return page;
    }

    /**
     * 将IdleProduct实体转换为IdleProductVO（优化版）
     * 添加用户信息查询（avatar、username）
     */
    private IdleProductVO convertToProductVO(IdleProduct product) {
        IdleProductVO vo = new IdleProductVO();
        BeanUtils.copyProperties(product, vo);
        return vo;
    }

    /**
     * 获取热榜笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @Override
    public Page<IdleProductVO> getHotProduct(long currentPage, long pageSize) {
        Page<IdleProductVO> page = new Page<>(currentPage, pageSize);
        List<IdleProductVO> idleProductVOList = new ArrayList<>();

        try {
            // 构建搜索请求
            SearchRequest.Builder builder = new SearchRequest.Builder()
                    .index(NoteConstant.PRODUCT_INDEX)
                    .from(Math.toIntExact((currentPage - 1) * pageSize)) // 设置分页起始点
                    .size(Math.toIntExact(pageSize)) // 设置分页大小
                    .sort(s -> s.field(f -> f.field("likeCount").order(SortOrder.Desc))); // 按 likeCount 降序排序

            SearchRequest searchRequest = builder.build();

            // 执行搜索请求
            SearchResponse<IdleProductVO> searchResponse = elasticsearchClient.search(searchRequest, IdleProductVO.class);
            TotalHits totalHits = searchResponse.hits().total();

            // 获取搜索结果
            List<Hit<IdleProductVO>> hits = searchResponse.hits().hits();
            if (CollectionUtil.isNotEmpty(hits)) {
                for (Hit<IdleProductVO> hit : hits) {
                    IdleProductVO idleProductVO = hit.source();
                    idleProductVOList.add(idleProductVO);
                }
            }

            // 设置分页结果
            page.setTotal(totalHits != null ? totalHits.value() : 0);
            page.setRecords(idleProductVOList);

        } catch (Exception e) {
            e.printStackTrace();
            // 这里可以进一步处理异常，比如记录日志或者抛出自定义异常
        }
        return page;
    }


    /**
     * 增加笔记
     *
     * @param idleProductVO 笔记
     */
    @Override
    public void addProduct(IdleProductVO idleProductVO) {
        try {
            CreateResponse createResponse = elasticsearchClient
                    .create(e -> e.index(NoteConstant.PRODUCT_INDEX).id(idleProductVO.getId()).document(idleProductVO));
            log.info("createResponse.result{}", createResponse.result());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改笔记
     *
     * @param idleProductVO 笔记
     */
    @Override
    public void updateProduct(IdleProductVO idleProductVO) {
        try {
            UpdateResponse<IdleProductVO> updateResponse = elasticsearchClient
                    .update(e -> e.index(NoteConstant.PRODUCT_INDEX)
                            .id(idleProductVO.getId())
                            .doc(idleProductVO)
                            .docAsUpsert(true), IdleProductVO.class); // 使用 upsert，如果文档不存在则自动创建
            log.info("updateResponse.result() = " + updateResponse.result());
        } catch (Exception e) {
            log.error("更新商品到ES失败，商品ID: {}", idleProductVO.getId(), e);
        }
    }

    /**
     * 删除es中的笔记
     *
     * @param productId 笔记 ID
     */
    @Override
    public void deleteProduct(String productId) {
        if (StringUtils.isBlank(productId)) {
            return;
        }
        try {
            DeleteResponse deleteResponse = elasticsearchClient
                    .delete(e -> e.index(NoteConstant.PRODUCT_INDEX).id(String.valueOf(productId)));
            log.info("ES 删除商品 result={}, id={}", deleteResponse.result(), productId);
        } catch (ElasticsearchException ex) {
            if (ex.status() == 404) {
                log.debug("ES 文档不存在，忽略删除, productId={}", productId);
                return;
            }
            log.error("ES 删除商品失败, productId={}", productId, ex);
        } catch (Exception e) {
            log.error("ES 删除商品失败, productId={}", productId, e);
        }
    }

    @Override
    public void applyProductEsIndexState(String productId) {
        if (StringUtils.isBlank(productId)) {
            return;
        }
        IdleProduct product = productMapper.selectById(productId);
        if (product == null || (product.getDeleted() != null && product.getDeleted() == 1)) {
            deleteProduct(productId);
            return;
        }
        if (!AuditStatusEnum.PASS.getCode().equals(product.getAuditStatus())) {
            deleteProduct(productId);
            return;
        }
        if (product.getStatus() == null || product.getStatus() != 0) {
            deleteProduct(productId);
            return;
        }
        IdleProductVO vo = buildIdleProductVoForEs(product);
        if (vo == null) {
            return;
        }
        updateProduct(vo);
    }

    /**
     * 从库表组装 ES 文档（与发布同步字段一致，含 likeCount 等最新列）。
     */
    private IdleProductVO buildIdleProductVoForEs(IdleProduct product) {
        WebUser user = userMapper.selectById(product.getUid());
        if (user == null) {
            log.warn("用户不存在，无法同步到ES，用户ID: {}", product.getUid());
            return null;
        }
        IdleCategory parentCategory = null;
        if (StringUtils.isNotBlank(product.getCpid())) {
            parentCategory = idleCategoryMapper.selectByCpid(product.getCpid());
            if (parentCategory == null) {
                log.warn("父分类不存在，无法同步到ES，cpid: {}", product.getCpid());
                return null;
            }
        }
        IdleProductVO productVO = ConvertUtils.sourceToTarget(product, IdleProductVO.class);
        if (StringUtils.isNotBlank(product.getCid()) && !"0".equals(product.getCid())) {
            IdleCategory category = idleCategoryMapper.selectByPid(product.getCid());
            if (category != null) {
                productVO.setCategoryName(category.getTitle());
            }
        }
        long time = System.currentTimeMillis();
        if (product.getUpdateTime() != null) {
            time = product.getUpdateTime().getTime();
        } else if (product.getCreateTime() != null) {
            time = product.getCreateTime().getTime();
        }
        productVO.setUsername(user.getUsername())
                .setAvatar(user.getAvatar())
                .setTime(time);
        if (parentCategory != null) {
            productVO.setCategoryParentName(parentCategory.getTitle());
        }
        return productVO;
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

        Set<String> uidSet = productList.stream()
                .filter(p -> StringUtils.isNotBlank(p.getUid())
                        && (StringUtils.isBlank(p.getAvatar()) || StringUtils.isBlank(p.getUsername())))
                .map(IdleProductVO::getUid)
                .collect(Collectors.toSet());

        if (uidSet.isEmpty()) {
            return;
        }

        try {
            Map<String, WebUser> userMap = userMapper.selectList(
                    new QueryWrapper<WebUser>()
                            .select("id", "username", "avatar")
                            .in("id", uidSet)
            ).stream().collect(Collectors.toMap(u -> String.valueOf(u.getId()), u -> u, (a, b) -> a));

            for (IdleProductVO product : productList) {
                if (StringUtils.isNotBlank(product.getAvatar()) && StringUtils.isNotBlank(product.getUsername())) {
                    continue;
                }
                WebUser user = userMap.get(product.getUid());
                if (user != null) {
                    product.setAvatar(user.getAvatar());
                    product.setUsername(user.getUsername());
                }
            }
        } catch (Exception e) {
            log.error("批量填充用户信息失败", e);
        }
    }

    // ---- Redis 缓存辅助 ----

    private static class CachedProductPage {
        public long current;
        public long size;
        public long total;
        public List<IdleProductVO> records;
    }

    private Page<IdleProductVO> getProductPageFromCache(String key) {
        try {
            Object raw = redisUtils.get(key);
            if (raw == null) return null;
            String json = raw instanceof String ? (String) raw : JSON_MAPPER.writeValueAsString(raw);
            CachedProductPage cp = JSON_MAPPER.readValue(json, CachedProductPage.class);
            Page<IdleProductVO> page = new Page<>(cp.current, cp.size, cp.total);
            page.setRecords(cp.records != null ? cp.records : new ArrayList<>());
            return page;
        } catch (Exception e) {
            log.debug("读取商品缓存失败, key={}", key, e);
            return null;
        }
    }

    private void putProductPageToCache(String key, Page<IdleProductVO> page, long ttlSec) {
        try {
            CachedProductPage cp = new CachedProductPage();
            cp.current = page.getCurrent();
            cp.size = page.getSize();
            cp.total = page.getTotal();
            cp.records = page.getRecords();
            String json = JSON_MAPPER.writeValueAsString(cp);
            redisUtils.set(key, json, ttlSec);
        } catch (Exception e) {
            log.debug("写入商品缓存失败, key={}", key, e);
        }
    }
}
