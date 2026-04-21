package com.hongshu.web.service.web.impl;

import cn.hutool.core.collection.CollectionUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.core.constant.NoteConstant;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.common.core.enums.AuditStatusEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongshu.common.core.utils.ConvertUtils;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.common.core.exception.HongshuException;
import com.hongshu.common.core.utils.DozerUtil;
import com.hongshu.common.core.utils.RedisUtils;
import com.hongshu.common.core.utils.bean.BeanUtils;
import com.hongshu.common.core.utils.ip.AddressUtils;
import com.hongshu.common.core.utils.ip.IpUtils;
import com.hongshu.web.domain.NearbyNotesPage;
import com.hongshu.web.domain.dto.EsNoteDTO;
import com.hongshu.web.domain.dto.SystemConfigDTO;
import com.hongshu.web.domain.entity.WebCategory;
import com.hongshu.web.domain.entity.WebLikeOrCollection;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.domain.vo.NoteVo;
import com.hongshu.web.mapper.web.WebCategoryMapper;
import com.hongshu.web.mapper.web.WebLikeOrCollectionMapper;
import com.hongshu.web.mapper.web.WebNoteMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.service.IRecommendNoteService;
import com.hongshu.web.service.recommendation.lightweight.LightweightRecommendService;
import com.hongshu.web.service.sys.ISysSystemConfigService;
import com.hongshu.web.service.web.IWebEsNoteService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ES
 *
 * @author: hongshu
 */
@Service
@Slf4j
public class WebEsNoteServiceImpl extends ServiceImpl<WebNoteMapper, WebNote> implements IWebEsNoteService {

    private static final String CACHE_KEY_RECOMMEND = "recommend:note:";
    private static final String CACHE_KEY_VIDEO = "video:note:";
    private static final long RECOMMEND_CACHE_TTL_SEC = 300;
    private static final long VIDEO_CACHE_TTL_SEC = 180;

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    @Autowired
    private WebUserMapper userMapper;
    @Autowired
    private WebCategoryMapper categoryMapper;
    @Autowired
    private WebNoteMapper noteMapper;
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    @Autowired
    private IRecommendNoteService recommendService;
    @Autowired
    private WebLikeOrCollectionMapper likeOrCollectionMapper;
    @Autowired
    private ISysSystemConfigService systemConfigService;
    @Autowired
    private LightweightRecommendService lightweightRecommendService;
    @Autowired
    private RedisUtils redisUtils;


    /**
     * 搜索对应的笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param esNoteDTO   笔记
     */
    @Override
    public Page<NoteSearchVo> getNoteByDTO(long currentPage, long pageSize, EsNoteDTO esNoteDTO) {

        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        String queryPrimary = systemConfig.getQueryPrimary();

        // ES检索
        if ("es".equals(queryPrimary)) {
            return getNoteByDTOFromES(currentPage, pageSize, esNoteDTO);
        }
        // MySQL检索
        else if ("mysql".equals(queryPrimary)) {
            return getNoteByDTOFromMysql(currentPage, pageSize, esNoteDTO);
        } else {
            return getNoteByDTOFromES(currentPage, pageSize, esNoteDTO);
        }

        //        Page<NoteSearchVo> page = new Page<>();
//        List<NoteSearchVo> noteSearchVoList = new ArrayList<>();
//        try {
//            SearchRequest.Builder builder = new SearchRequest.Builder().index(NoteConstant.NOTE_INDEX);
//            // 构建bool查询
//            BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
//            // 1. type条件
//            if (esNoteDTO.getType() != null && esNoteDTO.getType() != 0) {
//                // 只在type=1或2时加must
//                boolBuilder.must(m -> m.match(f -> f.field("noteType").query(esNoteDTO.getType())));
//            }
//            // 2. keyword相关should
//            if (StringUtils.isNotBlank(esNoteDTO.getKeyword())) {
//                boolBuilder.must(m -> m.bool(bb -> bb
//                        .should(h -> h.match(f -> f.field("title").boost(1f).query(esNoteDTO.getKeyword())))
//                        .should(h -> h.match(f -> f.field("username").boost(0.5f).query(esNoteDTO.getKeyword())))
//                        .should(h -> h.match(f -> f.field("content").boost(1f).query(esNoteDTO.getKeyword())))
//                        .should(h -> h.match(f -> f.field("tags").boost(4f).query(esNoteDTO.getKeyword())))
//                        .should(h -> h.match(f -> f.field("categoryName").boost(2f).query(esNoteDTO.getKeyword())))
//                        .should(h -> h.match(f -> f.field("categoryParentName").boost(1.5f).query(esNoteDTO.getKeyword())))
//                ));
//            }
//            // 3. 分类条件
//            if (StringUtils.isNotBlank(esNoteDTO.getCpid()) && StringUtils.isNotBlank(esNoteDTO.getCid())) {
//                boolBuilder.must(h -> h.match(m -> m.field("cid").query(esNoteDTO.getCid())));
//                boolBuilder.must(h -> h.match(m -> m.field("cpid").query(esNoteDTO.getCpid())));
//            } else if (StringUtils.isNotBlank(esNoteDTO.getCpid())) {
//                boolBuilder.must(h -> h.match(m -> m.field("cpid").query(esNoteDTO.getCpid())));
//            }
//            // 设置查询
//            builder.query(q -> q.bool(boolBuilder.build()));
//            // 排序
//            if (esNoteDTO.getType() != null && esNoteDTO.getType() == 1) {
//                builder.sort(o -> o.field(f -> f.field("likeCount").order(SortOrder.Desc)));
//            } else if (esNoteDTO.getType() != null && esNoteDTO.getType() == 2) {
//                builder.sort(o -> o.field(f -> f.field("time").order(SortOrder.Desc)));
//            } else {
//                // 默认排序
//                builder.sort(o -> o.field(f -> f.field("time").order(SortOrder.Desc)));
//            }
//            builder.from((int) (currentPage - 1) * (int) pageSize);
//            builder.size((int) pageSize);
//
//            SearchRequest searchRequest = builder.build();
//            SearchResponse<NoteSearchVo> searchResponse = elasticsearchClient.search(searchRequest, NoteSearchVo.class);
//            TotalHits totalHits = searchResponse.hits().total();
//            page.setTotal(Objects.requireNonNull(totalHits).value());
//            page.setCountId(esNoteDTO.getCpid());
//            List<Hit<NoteSearchVo>> hits = searchResponse.hits().hits();
//            for (Hit<NoteSearchVo> hit : hits) {
//                NoteSearchVo noteSearchVo = hit.source();
//                noteSearchVoList.add(noteSearchVo);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new HongshuException("es查找数据异常");
//        }
//        page.setRecords(noteSearchVoList);
//        return page;
    }

    /**
     * 从ES搜索笔记
     */
    private Page<NoteSearchVo> getNoteByDTOFromES(long currentPage, long pageSize, EsNoteDTO esNoteDTO) {
        Page<NoteSearchVo> page = new Page<>();
        List<NoteSearchVo> noteSearchVoList = new ArrayList<>();

        try {
            SearchRequest.Builder builder = new SearchRequest.Builder().index(NoteConstant.NOTE_INDEX);
            // 构建bool查询
            BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
            // 1. type条件
            if (esNoteDTO.getType() != null && esNoteDTO.getType() != 0) {
                // 只在type=1或2时加must
                boolBuilder.must(m -> m.match(f -> f.field("noteType").query(esNoteDTO.getType())));
            }
            // 2. keyword相关should
            if (StringUtils.isNotBlank(esNoteDTO.getKeyword())) {
                boolBuilder.must(m -> m.bool(bb -> bb
                        .should(h -> h.match(f -> f.field("title").boost(1f).query(esNoteDTO.getKeyword())))
                        .should(h -> h.match(f -> f.field("username").boost(0.5f).query(esNoteDTO.getKeyword())))
                        .should(h -> h.match(f -> f.field("content").boost(1f).query(esNoteDTO.getKeyword())))
                        .should(h -> h.match(f -> f.field("tags").boost(4f).query(esNoteDTO.getKeyword())))
                        .should(h -> h.match(f -> f.field("categoryName").boost(2f).query(esNoteDTO.getKeyword())))
                        .should(h -> h.match(f -> f.field("categoryParentName").boost(1.5f).query(esNoteDTO.getKeyword())))
                ));
            }
            // 3. 分类条件
            if (StringUtils.isNotBlank(esNoteDTO.getCpid()) && StringUtils.isNotBlank(esNoteDTO.getCid())) {
                boolBuilder.must(h -> h.match(m -> m.field("cid").query(esNoteDTO.getCid())));
                boolBuilder.must(h -> h.match(m -> m.field("cpid").query(esNoteDTO.getCpid())));
            } else if (StringUtils.isNotBlank(esNoteDTO.getCpid())) {
                boolBuilder.must(h -> h.match(m -> m.field("cpid").query(esNoteDTO.getCpid())));
            }
            // 设置查询
            builder.query(q -> q.bool(boolBuilder.build()));
            // 🔧 修复：使用正常的pageSize，而不是3倍数据
            // 排序逻辑
            if (esNoteDTO.getType() != null && esNoteDTO.getType() == 1) {
                // 图片类型按点赞数排序，保持热度排序
                builder.sort(o -> o.field(f -> f.field("likeCount").order(SortOrder.Desc)));
            } else {
                // 视频类型和其他情况按时间排序
                builder.sort(o -> o.field(f -> f.field("time").order(SortOrder.Desc)));
            }
            // 🔧 修复：使用正确的分页参数
            builder.from((int) (currentPage - 1) * (int) pageSize);
            builder.size((int) pageSize); // 使用正常的pageSize

            SearchRequest searchRequest = builder.build();
            SearchResponse<NoteSearchVo> searchResponse = elasticsearchClient.search(searchRequest, NoteSearchVo.class);
            TotalHits totalHits = searchResponse.hits().total();
            page.setTotal(Objects.requireNonNull(totalHits).value());
            page.setCountId(esNoteDTO.getCpid());
            List<Hit<NoteSearchVo>> hits = searchResponse.hits().hits();
            for (Hit<NoteSearchVo> hit : hits) {
                NoteSearchVo noteSearchVo = hit.source();

                // 字段映射：ES字段 -> 前端期望字段
                if (noteSearchVo != null) {
                    // 设置默认高度
                    if (noteSearchVo.getNoteCoverHeight() == null || noteSearchVo.getNoteCoverHeight() == 0) {
                        noteSearchVo.setNoteCoverHeight(800);
                    }

                    // 解析urls字段为imgUrls数组
                    if (noteSearchVo.getUrls() != null && !noteSearchVo.getUrls().isEmpty()) {
                        try {
                            // urls字段是JSON字符串，需要解析为List<String>
                            String urlsJson = noteSearchVo.getUrls();
                            if (urlsJson.startsWith("[") && urlsJson.endsWith("]")) {
                                // 简单的JSON解析，提取URL
                                urlsJson = urlsJson.substring(1, urlsJson.length() - 1);
                                String[] urlArray = urlsJson.split(",");
                                List<String> imgUrls = new ArrayList<>();
                                for (String url : urlArray) {
                                    String cleanUrl = url.trim().replaceAll("^\"|\"$", "");
                                    if (!cleanUrl.isEmpty()) {
                                        imgUrls.add(cleanUrl);
                                    }
                                }
                                noteSearchVo.setImgUrls(imgUrls);
                            }
                        } catch (Exception e) {
                            log.warn("解析urls字段失败: {}", noteSearchVo.getUrls(), e);
                        }
                    }
                }

                noteSearchVoList.add(noteSearchVo);
            }

            // 🔧 修复：移除后端随机打乱逻辑，由前端处理随机性
            // 这样可以确保分页逻辑正确，避免数据重复和分页失效
        } catch (Exception e) {
            e.printStackTrace();
            throw new HongshuException("es查找数据异常");
        }
        // 🔧 修复：确保返回的current字段正确
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        page.setRecords(noteSearchVoList);
        return page;
    }

    /**
     * 从MySQL搜索笔记
     */
    private Page<NoteSearchVo> getNoteByDTOFromMysql(long currentPage, long pageSize, EsNoteDTO esNoteDTO) {
        Page<NoteSearchVo> page = new Page<>();

        // 构建查询条件
        QueryWrapper<WebNote> queryWrapper = new QueryWrapper<WebNote>()
                .eq("status", "1")
                .eq("audit_status", AuditStatusEnum.PASS.getCode());
        // 1. 笔记类型条件
        if (esNoteDTO.getType() != null && esNoteDTO.getType() != 0) {
            queryWrapper.eq("note_type", esNoteDTO.getType().toString());
        }
        // 2. 关键词搜索
        if (StringUtils.isNotBlank(esNoteDTO.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like("title", esNoteDTO.getKeyword())
                    .or().like("content", esNoteDTO.getKeyword())
                    .or().like("tags", esNoteDTO.getKeyword())
                    .or().like("author", esNoteDTO.getKeyword())
            );
        }
        // 3. 分类条件
        if (StringUtils.isNotBlank(esNoteDTO.getCpid()) && StringUtils.isNotBlank(esNoteDTO.getCid())) {
            queryWrapper.eq("cpid", esNoteDTO.getCpid())
                    .eq("cid", esNoteDTO.getCid());
        } else if (StringUtils.isNotBlank(esNoteDTO.getCpid())) {
            queryWrapper.eq("cpid", esNoteDTO.getCpid());
        }
        // 🔧 修复：使用正常的pageSize，而不是3倍数据
        // 排序逻辑
        if (esNoteDTO.getType() != null && esNoteDTO.getType() == 1) {
            // 图片类型按点赞数排序，保持热度排序
            queryWrapper.orderByDesc("like_count");
        } else {
            // 视频类型和其他情况按时间排序
            queryWrapper.orderByDesc("time");
        }

        // 🔧 修复：使用正常的pageSize进行分页查询
        Page<WebNote> notePage = noteMapper.selectPage(new Page<>(currentPage, pageSize), queryWrapper);
        List<NoteSearchVo> noteSearchVoList = toNoteSearchVosWithUsersBatch(notePage.getRecords());

        // 🔧 修复：移除后端随机打乱逻辑，由前端处理随机性
        // 这样可以确保分页逻辑正确，避免数据重复和分页失效

        // 🔧 修复：确保返回的current字段正确
        page.setCurrent(currentPage);
        page.setSize(pageSize); // 返回实际请求的pageSize
        page.setTotal(notePage.getTotal());
        page.setCountId(esNoteDTO.getCpid());
        page.setRecords(noteSearchVoList);
        return page;
    }

    /**
     * 搜索对应的笔记
     *
     * @param esNoteDTO 笔记
     * @return
     */
    @Override
    public List<WebCategory> getCategoryAgg(EsNoteDTO esNoteDTO) {
        QueryWrapper<WebCategory> qw = new QueryWrapper<>();
        // 避免 keyword 为空时生成 title LIKE '%%' 扫全表（分类表大时也会拖慢）
        if (esNoteDTO != null && StringUtils.isNotBlank(esNoteDTO.getKeyword())) {
            qw.like("title", esNoteDTO.getKeyword());
        }
        return categoryMapper.selectList(qw);
    }

    /**
     * 获取推荐笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @SneakyThrows
    @Override
    public Page<NoteSearchVo> getRecommendNote(long currentPage, long pageSize) {
        Page<NoteSearchVo> page = new Page<>();

        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        String queryPrimary = systemConfig.getQueryPrimary();

        // 轻量级推荐（推荐使用）
        if ("lightweight".equals(queryPrimary)) {
            log.info("使用轻量级推荐策略");
            String userId = null;
            try {
                userId = AuthContextHolder.getUserId();
            } catch (Exception e) {
                log.debug("用户未登录");
            }

            String cacheKey = CACHE_KEY_RECOMMEND + (userId != null ? userId : "anon") + ":" + currentPage + ":" + pageSize;
            Page<NoteSearchVo> cached = getPageFromCache(cacheKey);
            if (cached != null) {
                Collections.shuffle(cached.getRecords(), ThreadLocalRandom.current());
                log.info("推荐笔记命中缓存, key={}", cacheKey);
                return cached;
            }

            Page<NoteVo> lightweightPage =
                    lightweightRecommendService.getRecommendNotes(userId, currentPage, pageSize);
            Page<NoteSearchVo> resultPage = convertNoteVoPageToNoteSearchVoPage(lightweightPage);
            if (resultPage != null && resultPage.getRecords() != null && !resultPage.getRecords().isEmpty()) {
                putPageToCache(cacheKey, resultPage, RECOMMEND_CACHE_TTL_SEC);
                Collections.shuffle(resultPage.getRecords(), ThreadLocalRandom.current());
            }
            return resultPage;
        }
        // ES检索
        else if ("es".equals(queryPrimary)) {
            return this.getRecommendNoteFromES(currentPage, pageSize);
        }
        // MySQL检索
        else if ("mysql".equals(queryPrimary)) {
            return this.getRecommendNoteFromMysql(currentPage, pageSize);
        }

        return page;
//        Page<NoteSearchVo> page = new Page<>();
//
//        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
//        String queryPrimary = systemConfig.getQueryPrimary();
//        // ES检索
//        if ("es".equals(queryPrimary)) {
//            String userId = AuthContextHolder.getUserId();
//            // 用户ID为空 默认随机加载100条数据
//            List<NoteSearchVo> recommendList;
//            if (StringUtils.isBlank(userId)) {
//                SearchRequest searchRequest = SearchRequest.of(s -> s
//                        .index(NoteConstant.NOTE_INDEX)
//                        .size(100));
//                SearchResponse<NoteSearchVo> searchResponse = elasticsearchClient.search(searchRequest, NoteSearchVo.class);
//                recommendList = searchResponse.hits().hits().stream()
//                        .map(Hit::source)
//                        .collect(Collectors.toList());
//                // 随机排序
//                Collections.shuffle(recommendList, new Random());
//            } else {
//                // 获取推荐列表
//                recommendList = recommendService.getRecommendNote(Long.parseLong(userId));
//            }
//            List<List<NoteSearchVo>> partition = Lists.partition(recommendList, (int) pageSize);
//            // 如果 currentPage 超出范围，返回空记录
//            if (currentPage > partition.size() || currentPage <= 0) {
//                page.setTotal(0);
//                page.setRecords(Collections.emptyList());
//                return page;
//            }
//            List<NoteSearchVo> noteSearchVos = partition.get((int) currentPage - 1);
//            page.setTotal(recommendList.size());
//            page.setRecords(noteSearchVos);
//            return page;
//        }
//        // MySQL检索
//        else if ("mysql".equals(queryPrimary)) {
//            String userId = AuthContextHolder.getUserId();
//            if (StringUtils.isBlank(userId)) {
//                // 用户未登录，随机获取100条公开笔记
//                List<NoteSearchVo> randomNotes = noteMapper.selectList(
//                                new QueryWrapper<WebNote>()
//                                        .eq("audit_status", AuditStatusEnum.PASS.getCode())
//                                        .orderBy(true, false, "RAND()")
//                                        .last("LIMIT " + 100)
//                        ).stream()
//                        .map(this::convertToNoteVo)
//                        .collect(Collectors.toList());
//                // 随机排序
//                Collections.shuffle(randomNotes, new Random());
//                // 分页处理
//                List<List<NoteSearchVo>> partition = Lists.partition(randomNotes, (int) pageSize);
//                if (currentPage > partition.size() || currentPage <= 0) {
//                    page.setTotal(0);
//                    page.setRecords(Collections.emptyList());
//                    return page;
//                }
//                List<NoteSearchVo> noteSearchVos = partition.get((int) currentPage - 1);
//                page.setTotal(randomNotes.size());
//                page.setRecords(noteSearchVos);
//            } else {
//                // 用户已登录，通过推荐算法获取推荐列表
//                page = recommendService.getRecommendNoteFromMysql(userId, currentPage, pageSize);
//            }
//            return page;
//        }
//        return page;
    }

    /**
     * 获取视频笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @Override
    public Page<NoteSearchVo> getVideoNote(long currentPage, long pageSize) {
        String cacheKey = CACHE_KEY_VIDEO + currentPage + ":" + pageSize;
        Page<NoteSearchVo> cached = getPageFromCache(cacheKey);
        if (cached != null) {
            Collections.shuffle(cached.getRecords(), ThreadLocalRandom.current());
            log.info("视频笔记命中缓存, key={}", cacheKey);
            return cached;
        }

        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        String queryPrimary = systemConfig.getQueryPrimary();

        Page<NoteSearchVo> result;
        if ("es".equals(queryPrimary)) {
            result = getVideoNoteFromES(currentPage, pageSize);
        } else if ("mysql".equals(queryPrimary)) {
            result = getVideoNoteFromMysql(currentPage, pageSize);
        } else {
            result = getVideoNoteFromES(currentPage, pageSize);
        }

        if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
            putPageToCache(cacheKey, result, VIDEO_CACHE_TTL_SEC);
        }
        return result;
    }

    /**
     * 从ES获取视频笔记
     */
    private Page<NoteSearchVo> getVideoNoteFromES(long currentPage, long pageSize) {
        Page<NoteSearchVo> page = new Page<>();
        List<NoteSearchVo> noteSearchVoList = new ArrayList<>();

        try {
            SearchRequest.Builder builder = new SearchRequest.Builder().index(NoteConstant.NOTE_INDEX);
            // 构建bool查询
            BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
            // 固定查询视频类型（type=2）
            boolBuilder.must(m -> m.match(f -> f.field("noteType").query(2)));
            // 只查询审核通过的笔记
            boolBuilder.must(m -> m.match(f -> f.field("auditStatus").query(AuditStatusEnum.PASS.getCode())));
            // 只查询正常状态的笔记
            boolBuilder.must(m -> m.match(f -> f.field("status").query("1")));

            // 🔧 修复：直接使用 pageSize 查询，不再查询3倍数据
            // 随机排序应该在前端处理，而不是后端，避免分页问题
            SearchRequest searchRequest = builder
                    .query(q -> q.bool(boolBuilder.build()))
                    .from((int) ((currentPage - 1) * pageSize))
                    .size((int) pageSize) // 直接使用 pageSize
                    .sort(s -> s.field(f -> f.field("time").order(SortOrder.Desc)))
                    .build();

            SearchResponse<NoteSearchVo> searchResponse = elasticsearchClient.search(searchRequest, NoteSearchVo.class);

            // 处理搜索结果
            for (Hit<NoteSearchVo> hit : searchResponse.hits().hits()) {
                NoteSearchVo noteSearchVo = hit.source();
                if (noteSearchVo != null) {
                    // 设置默认高度
                    if (noteSearchVo.getNoteCoverHeight() == null || noteSearchVo.getNoteCoverHeight() == 0) {
                        noteSearchVo.setNoteCoverHeight(800);
                    }
                    // 解析urls字段为imgUrls数组
                    if (noteSearchVo.getUrls() != null && !noteSearchVo.getUrls().isEmpty()) {
                        try {
                            String urlsJson = noteSearchVo.getUrls();
                            if (urlsJson.startsWith("[") && urlsJson.endsWith("]")) {
                                urlsJson = urlsJson.substring(1, urlsJson.length() - 1);
                                String[] urlArray = urlsJson.split(",");
                                List<String> imgUrls = new ArrayList<>();
                                for (String url : urlArray) {
                                    String cleanUrl = url.trim().replaceAll("^\"|\"$", "");
                                    if (!cleanUrl.isEmpty()) {
                                        imgUrls.add(cleanUrl);
                                    }
                                }
                                noteSearchVo.setImgUrls(imgUrls);
                            }
                        } catch (Exception e) {
                            log.warn("解析urls字段失败: {}", noteSearchVo.getUrls(), e);
                        }
                    }
                    noteSearchVoList.add(noteSearchVo);
                }
            }

            // 本页结果内随机顺序：刷新/再次请求顺序会变；分页仍按 ES 的 time 倒序取窗口，避免跨页语义混乱
            if (!noteSearchVoList.isEmpty()) {
                Collections.shuffle(noteSearchVoList, ThreadLocalRandom.current());
            }
            log.info("✅ ES视频笔记查询完成，数据量={}, pageSize={}, currentPage={}", noteSearchVoList.size(), pageSize, currentPage);

            // 设置分页信息
            page.setCurrent(currentPage);
            page.setSize(pageSize);
            TotalHits totalHits = searchResponse.hits().total();
            page.setTotal(totalHits != null ? totalHits.value() : 0);
            page.setRecords(noteSearchVoList);
        } catch (Exception e) {
            log.error("从ES获取视频笔记失败", e);
            page.setCurrent(currentPage);
            page.setSize(pageSize);
            page.setTotal(0);
            page.setRecords(Collections.emptyList());
        }
        return page;
    }

    /**
     * 从MySQL获取视频笔记
     */
    private Page<NoteSearchVo> getVideoNoteFromMysql(long currentPage, long pageSize) {
        Page<NoteSearchVo> page = new Page<>();

        QueryWrapper<WebNote> queryWrapper = new QueryWrapper<WebNote>()
                .select("id", "title", "note_cover", "note_cover_height", "uid", "urls",
                        "note_type", "like_count", "view_count", "cid", "cpid",
                        "audit_status", "status", "time", "address", "pinned", "tags")
                .eq("deleted", 0)
                .eq("status", "1")
                .eq("audit_status", AuditStatusEnum.PASS.getCode())
                .eq("note_type", "2")
                .orderByDesc("time");

        Page<WebNote> notePage = noteMapper.selectPage(new Page<>(currentPage, pageSize), queryWrapper);

        List<NoteSearchVo> noteSearchVoList = toNoteSearchVosWithUsersBatch(notePage.getRecords());

        if (!noteSearchVoList.isEmpty()) {
            Collections.shuffle(noteSearchVoList, ThreadLocalRandom.current());
        }
        log.info("✅ MySQL视频笔记查询完成，数据量={}, pageSize={}, currentPage={}", noteSearchVoList.size(), pageSize, currentPage);

        // 🔧 修复：使用传入的 currentPage，而不是 notePage.getCurrent()
        // 因为 notePage.getCurrent() 可能因为分页逻辑问题而不准确
        page.setCurrent(currentPage);
        page.setSize(pageSize); // 返回实际请求的pageSize
        page.setTotal(notePage.getTotal());
        page.setRecords(noteSearchVoList);
        return page;
    }

    /**
     * 从ES获取推荐笔记
     */
    private Page<NoteSearchVo> getRecommendNoteFromES(long currentPage, long pageSize) {
        String userId = AuthContextHolder.getUserId();
        List<NoteSearchVo> recommendList;

        if (StringUtils.isBlank(userId)) {
            // 用户未登录，随机获取50条数据
            recommendList = getRandomNotesFromES(50);
        } else {
            // 用户已登录，通过推荐算法获取推荐列表
            recommendList = recommendService.getRecommendNote(Long.parseLong(userId));
            // 🔧 修复：对推荐列表进行随机打乱，增加刷新时的变化
            if (recommendList != null && !recommendList.isEmpty()) {
                log.info("🔄 推荐列表应用随机排序，原始数据量={}, pageSize={}", recommendList.size(), pageSize);
                Collections.shuffle(recommendList, new Random(System.currentTimeMillis()));
                log.info("✅ 推荐列表随机排序完成");
            }
        }

        return buildPageFromList(recommendList, currentPage, pageSize);
    }

    /**
     * 从MySQL获取推荐笔记
     */
    private Page<NoteSearchVo> getRecommendNoteFromMysql(long currentPage, long pageSize) {
        String userId = AuthContextHolder.getUserId();

        if (StringUtils.isBlank(userId)) {
            // 用户未登录，随机获取50条公开笔记
            List<NoteSearchVo> randomNotes = getRandomNotesFromMysql(50);
            return buildPageFromList(randomNotes, currentPage, pageSize);
        } else {
            // 用户已登录，通过推荐算法获取推荐列表
            Page<NoteSearchVo> page = recommendService.getRecommendNoteFromMysql(userId, currentPage, pageSize);
            // 🔧 修复：对推荐结果进行随机打乱，增加刷新时的变化
            if (page != null && page.getRecords() != null && !page.getRecords().isEmpty()) {
                log.info("🔄 MySQL推荐列表应用随机排序，原始数据量={}, pageSize={}", page.getRecords().size(), pageSize);
                List<NoteSearchVo> records = page.getRecords();
                Collections.shuffle(records, new Random(System.currentTimeMillis()));
                page.setRecords(records);
                log.info("✅ MySQL推荐列表随机排序完成");
            }
            return page;
        }
    }

    /**
     * 从ES随机获取指定数量的笔记
     */
    private List<NoteSearchVo> getRandomNotesFromES(int limit) {
        // 构建查询条件：只查询未删除且审核通过的笔记
        SearchRequest.Builder builder = new SearchRequest.Builder().index(NoteConstant.NOTE_INDEX);
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
        // 只查询未删除的笔记（deleted = 0）
        boolBuilder.must(m -> m.term(t -> t.field("deleted").value(0)));
        // 只查询审核通过的笔记（与其他ES查询保持一致，使用match）
        boolBuilder.must(m -> m.match(f -> f.field("auditStatus").query(AuditStatusEnum.PASS.getCode())));
        
        SearchRequest searchRequest = builder
                .query(q -> q.bool(boolBuilder.build()))
                .size(limit)
                .build();
        
        SearchResponse<NoteSearchVo> searchResponse = null;
        try {
            searchResponse = elasticsearchClient.search(searchRequest, NoteSearchVo.class);
        } catch (IOException e) {
            log.error("从ES获取随机笔记失败", e);
            return Collections.emptyList();
        }
        
        if (searchResponse == null || searchResponse.hits() == null) {
            log.warn("ES查询返回结果为空");
            return Collections.emptyList();
        }
        
        List<NoteSearchVo> recommendList = searchResponse.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 字段映射：ES字段 -> 前端期望字段
        for (NoteSearchVo noteSearchVo : recommendList) {
            if (noteSearchVo != null) {
                // 设置默认高度
                if (noteSearchVo.getNoteCoverHeight() == null || noteSearchVo.getNoteCoverHeight() == 0) {
                    noteSearchVo.setNoteCoverHeight(800);
                }

                // 解析urls字段为imgUrls数组
                if (noteSearchVo.getUrls() != null && !noteSearchVo.getUrls().isEmpty()) {
                    try {
                        String urlsJson = noteSearchVo.getUrls();
                        if (urlsJson.startsWith("[") && urlsJson.endsWith("]")) {
                            urlsJson = urlsJson.substring(1, urlsJson.length() - 1);
                            String[] urlArray = urlsJson.split(",");
                            List<String> imgUrls = new ArrayList<>();
                            for (String url : urlArray) {
                                String cleanUrl = url.trim().replaceAll("^\"|\"$", "");
                                if (!cleanUrl.isEmpty()) {
                                    imgUrls.add(cleanUrl);
                                }
                            }
                            noteSearchVo.setImgUrls(imgUrls);
                        }
                    } catch (Exception e) {
                        log.warn("解析urls字段失败: {}", noteSearchVo.getUrls(), e);
                    }
                }
            }
        }

        // 随机排序
        Collections.shuffle(recommendList, new Random());
        return recommendList;
    }

    /**
     * 从MySQL随机获取指定数量的公开笔记
     */
    private List<NoteSearchVo> getRandomNotesFromMysql(int limit) {
        List<WebNote> rows = noteMapper.selectList(
                new QueryWrapper<WebNote>()
                        .select("id", "title", "note_cover", "note_cover_height", "uid", "urls",
                                "note_type", "like_count", "view_count", "cid", "cpid",
                                "audit_status", "status", "time", "address", "pinned", "tags")
                        .eq("deleted", 0)
                        .eq("status", "1")
                        .eq("audit_status", AuditStatusEnum.PASS.getCode())
                        .orderByDesc("like_count")
                        .last("LIMIT " + (limit * 3))
        );
        if (rows.size() > limit) {
            Collections.shuffle(rows, ThreadLocalRandom.current());
            rows = rows.subList(0, limit);
        }
        return toNoteSearchVosWithUsersBatch(rows);
    }

    /**
     * 从列表构建分页结果
     */
    private Page<NoteSearchVo> buildPageFromList(List<NoteSearchVo> noteList, long currentPage, long pageSize) {
        Page<NoteSearchVo> page = new Page<>();
        List<List<NoteSearchVo>> partition = Lists.partition(noteList, (int) pageSize);

        // 如果 currentPage 超出范围，返回空记录
        if (currentPage > partition.size() || currentPage <= 0) {
            page.setTotal(0);
            page.setRecords(Collections.emptyList());
            return page;
        }

        List<NoteSearchVo> noteSearchVos = partition.get((int) currentPage - 1);
        page.setTotal(noteList.size());
        page.setRecords(noteSearchVos);
        return page;
    }

    /**
     * 列表转 VO 并批量补全用户（避免 MySQL 路径下每条笔记一次 select user 的 N+1）
     */
    private List<NoteSearchVo> toNoteSearchVosWithUsersBatch(List<WebNote> notes) {
        if (notes == null || notes.isEmpty()) {
            return Collections.emptyList();
        }
        List<NoteSearchVo> vos = new ArrayList<>(notes.size());
        for (WebNote note : notes) {
            NoteSearchVo vo = new NoteSearchVo();
            BeanUtils.copyProperties(note, vo);
            if (vo.getNoteCoverHeight() == null || vo.getNoteCoverHeight() == 0) {
                vo.setNoteCoverHeight(800);
            }
            vos.add(vo);
        }
        Set<String> uidSet = notes.stream()
                .map(WebNote::getUid)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (uidSet.isEmpty()) {
            return vos;
        }
        try {
            List<WebUser> users = userMapper.selectList(
                    new QueryWrapper<WebUser>()
                            .select("id", "username", "avatar")
                            .in("id", uidSet));
            Map<String, WebUser> userMap = users.stream()
                    .collect(Collectors.toMap(u -> String.valueOf(u.getId()), u -> u, (a, b) -> a));
            for (int i = 0; i < notes.size(); i++) {
                String uid = notes.get(i).getUid();
                if (StringUtils.isBlank(uid)) {
                    continue;
                }
                WebUser u = userMap.get(uid);
                if (u != null) {
                    vos.get(i).setAvatar(u.getAvatar());
                    vos.get(i).setUsername(u.getUsername());
                } else {
                    log.debug("未找到用户信息: uid={}", uid);
                }
            }
        } catch (Exception e) {
            log.error("批量查询用户信息失败", e);
        }
        return vos;
    }

    /**
     * 获取推荐用户
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @SneakyThrows
    @Override
    public Page<WebUser> getRecommendUser(long currentPage, long pageSize) {
        Page<WebUser> page = new Page<>();
//        String userId = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String userId = AuthContextHolder.getUserId();
        // 用户ID为空 默认随机加载100条数据
        List<WebUser> recommendList;
        if (StringUtils.isBlank(userId)) {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(NoteConstant.NOTE_INDEX)
                    .size(100));
            SearchResponse<WebUser> searchResponse = elasticsearchClient.search(searchRequest, WebUser.class);
            recommendList = searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
            // 随机排序
            Collections.shuffle(recommendList, new Random());
        } else {
            // 获取推荐列表
            recommendList = recommendService.getRecommendUser(Long.parseLong(userId));
        }
        List<List<WebUser>> partition = Lists.partition(recommendList, (int) pageSize);
        // 如果 currentPage 超出范围，返回空记录
        if (currentPage > partition.size() || currentPage <= 0) {
            page.setTotal(0);
            page.setRecords(Collections.emptyList());
            return page;
        }
        List<WebUser> userList = partition.get((int) currentPage - 1);
        page.setTotal(recommendList.size());
        page.setRecords(userList);
        return page;
    }

    /**
     * 获取热榜笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @Override
    public Page<NoteSearchVo> getHotNote(long currentPage, long pageSize) {
        Page<NoteSearchVo> page = new Page<>(currentPage, pageSize);
        List<NoteSearchVo> noteSearchVoList = new ArrayList<>();

        try {
            // 构建搜索请求
            SearchRequest.Builder builder = new SearchRequest.Builder()
                    .index(NoteConstant.NOTE_INDEX)
                    .from(Math.toIntExact((currentPage - 1) * pageSize)) // 设置分页起始点
                    .size(Math.toIntExact(pageSize)) // 设置分页大小
                    .sort(s -> s.field(f -> f.field("likeCount").order(SortOrder.Desc))); // 按 likeCount 降序排序

            SearchRequest searchRequest = builder.build();

            // 执行搜索请求
            SearchResponse<NoteSearchVo> searchResponse = elasticsearchClient.search(searchRequest, NoteSearchVo.class);
            TotalHits totalHits = searchResponse.hits().total();

            // 获取搜索结果
            List<Hit<NoteSearchVo>> hits = searchResponse.hits().hits();
            if (CollectionUtil.isNotEmpty(hits)) {
                for (Hit<NoteSearchVo> hit : hits) {
                    NoteSearchVo noteSearchVo = hit.source();
                    noteSearchVoList.add(noteSearchVo);
                }
            }

            // 设置分页结果
            page.setTotal(totalHits != null ? totalHits.value() : 0);
            page.setRecords(noteSearchVoList);

        } catch (Exception e) {
            e.printStackTrace();
            // 这里可以进一步处理异常，比如记录日志或者抛出自定义异常
        }
        return page;
    }


    /**
     * 增加笔记
     *
     * @param noteSearchVo 笔记
     */
    @Override
    public void addNote(NoteSearchVo noteSearchVo) {
        try {
            CreateResponse createResponse = elasticsearchClient.create(e -> e.index(NoteConstant.NOTE_INDEX).id(noteSearchVo.getId()).document(noteSearchVo));
            log.info("createResponse.result{}", createResponse.result());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void upsertNoteFromDbById(String noteId) {
        if (StringUtils.isBlank(noteId)) {
            return;
        }
        WebNote note = noteMapper.selectById(noteId);
        if (note == null || (note.getDeleted() != null && note.getDeleted() != 0)) {
            log.debug("笔记不存在或已逻辑删除，跳过 ES upsert，noteId={}", noteId);
            return;
        }
        if (!AuditStatusEnum.PASS.getCode().equals(note.getAuditStatus())) {
            log.debug("笔记未审核通过，跳过 ES upsert，noteId={}", noteId);
            return;
        }
        WebUser user = userMapper.selectById(note.getUid());
        if (user == null) {
            log.warn("用户不存在，无法同步到ES，noteId={}，uid={}", noteId, note.getUid());
            return;
        }
        if (StringUtils.isBlank(note.getCpid())) {
            log.warn("父分类为空，无法同步到ES，noteId={}", noteId);
            return;
        }
        WebCategory parentCategory = categoryMapper.selectById(note.getCpid());
        if (parentCategory == null) {
            log.warn("父分类不存在，无法同步到ES，noteId={}，cpid={}", noteId, note.getCpid());
            return;
        }
        NoteSearchVo noteSearchVo = ConvertUtils.sourceToTarget(note, NoteSearchVo.class);
        if (StringUtils.isNotBlank(note.getCid())) {
            WebCategory sub = categoryMapper.selectById(note.getCid());
            if (sub != null) {
                noteSearchVo.setCategoryName(sub.getTitle());
            }
        }
        long like = note.getLikeCount() != null ? note.getLikeCount() : 0L;
        long view = note.getViewCount() != null ? note.getViewCount() : 0L;
        noteSearchVo.setUsername(user.getUsername())
                .setAvatar(user.getAvatar())
                .setLikeCount(like)
                .setViewCount(view)
                .setCategoryParentName(parentCategory.getTitle())
                .setTime(System.currentTimeMillis());
        updateNote(noteSearchVo);
    }

    /**
     * 修改笔记（使用 upsert，如果文档不存在则自动创建）
     *
     * @param noteSearchVo 笔记
     */
    @Override
    public void updateNote(NoteSearchVo noteSearchVo) {
        try {
            // 使用 upsert：如果文档不存在则自动创建
            UpdateResponse<NoteSearchVo> updateResponse = elasticsearchClient.update(
                e -> e.index(NoteConstant.NOTE_INDEX)
                    .id(noteSearchVo.getId())
                    .doc(noteSearchVo)
                    .docAsUpsert(true), // 启用 upsert：文档不存在时自动创建
                NoteSearchVo.class
            );
            log.info("ES文档更新/创建成功，笔记ID: {}, 结果: {}", noteSearchVo.getId(), updateResponse.result());
        } catch (IOException e) {
            log.error("更新ES文档失败，笔记ID: {}", noteSearchVo.getId(), e);
            throw new RuntimeException("更新ES文档失败", e);
        } catch (Exception e) {
            log.error("更新ES文档失败，笔记ID: {}", noteSearchVo.getId(), e);
            throw e;
        }
    }

    /**
     * 删除es中的笔记
     *
     * @param noteId 笔记 ID
     */
    @Override
    public void deleteNote(String noteId) {
        if (StringUtils.isBlank(noteId)) {
            return;
        }
        try {
            DeleteResponse deleteResponse = elasticsearchClient.delete(e -> e.index(NoteConstant.NOTE_INDEX).id(String.valueOf(noteId)));
            log.info("ES 删除笔记 result={}, id={}", deleteResponse.result(), noteId);
        } catch (ElasticsearchException ex) {
            if (ex.status() == 404) {
                log.debug("ES 文档不存在，忽略删除, noteId={}", noteId);
                return;
            }
            log.error("ES 删除笔记失败, noteId={}", noteId, ex);
        } catch (Exception e) {
            log.error("ES 删除笔记失败, noteId={}", noteId, e);
        }
    }

    /**
     * 批量增加笔记
     */
    @Override
    public void addNoteBulkData() {
        List<WebNote> noteList = noteMapper.selectList(new QueryWrapper<WebNote>()
                .eq("audit_status", 1)
                .eq("deleted", 0));
        List<NoteSearchVo> noteSearchVoList = DozerUtil.convertor(noteList, NoteSearchVo.class);
        for (NoteSearchVo noteSearchVo : noteSearchVoList) {
            WebUser user = userMapper.selectOne(new QueryWrapper<WebUser>().eq("id", noteSearchVo.getUid()));
            if (user != null) {
                noteSearchVo.setAvatar(user.getAvatar());
                noteSearchVo.setUsername(user.getUsername());

                // 是否点赞
                List<WebLikeOrCollection> likeOrCollections = likeOrCollectionMapper.selectList(new QueryWrapper<WebLikeOrCollection>().eq("uid", user.getId()).eq("type", 1));
                List<String> likeOrCollectionIds = likeOrCollections.stream().map(WebLikeOrCollection::getLikeOrCollectionId).collect(Collectors.toList());
                noteSearchVo.setIsLike(likeOrCollectionIds.contains(noteSearchVo.getId()));
            } else {
                noteSearchVo.setAvatar(null);
                noteSearchVo.setUsername(null);
                noteSearchVo.setIsLike(false);
            }
        }
        try {
            List<BulkOperation> result = new ArrayList<>();
            for (NoteSearchVo noteSearchVo : noteSearchVoList) {
                result.add(new BulkOperation.Builder().create(
                        d -> d.document(noteSearchVo).id(noteSearchVo.getId()).index(NoteConstant.NOTE_INDEX)).build());
            }
            BulkResponse bulkResponse = elasticsearchClient.bulk(e -> e.index(NoteConstant.NOTE_INDEX).operations(result));
            log.info("createResponse.result{}", bulkResponse.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清空笔记
     */
    @Override
    public void delNoteBulkData() {
        try {
            // 检查索引是否存在
            if (elasticsearchClient.indices().exists(existsRequest -> existsRequest.index(NoteConstant.NOTE_INDEX)).value()) {
                // 删除索引
                DeleteIndexRequest deleteIndexRequest = DeleteIndexRequest.of(builder -> builder.index(NoteConstant.NOTE_INDEX));
                elasticsearchClient.indices().delete(deleteIndexRequest);
                log.info("成功删除索引: {}", NoteConstant.NOTE_INDEX);
            } else {
                log.info("索引 {} 不存在，无需删除", NoteConstant.NOTE_INDEX);
            }
            // 重新创建索引
            CreateIndexRequest createIndexRequest = CreateIndexRequest.of(builder -> builder.index(NoteConstant.NOTE_INDEX));
            elasticsearchClient.indices().create(createIndexRequest);
            log.info("成功创建索引: {}", NoteConstant.NOTE_INDEX);
        } catch (Exception e) {
            log.error("Error occurred while deleting and recreating index: {}", NoteConstant.NOTE_INDEX, e);
        }
    }

    /**
     * 重置
     */
    @Override
    public void refreshNoteData() {
        this.delNoteBulkData();
        this.addNoteBulkData();
    }

    /**
     * 查询附近数据
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    public NearbyNotesPage<NoteSearchVo> getNotesNearBy(long currentPage, long pageSize) {
        NearbyNotesPage<NoteSearchVo> page = new NearbyNotesPage<>((int) currentPage, (int) pageSize);

        String cityName = this.getCurrentCity();
        log.info("获取到的城市名称: {}", cityName);
        page.setCityName(cityName != null ? cityName : "附近");

        if (StringUtils.isNotBlank(cityName)) {
            // 调用Mapper方法，传入审核状态参数
            Page<NoteSearchVo> notePage = noteMapper.selectNotesWithUserInfo(
                    new Page<>((int) currentPage, (int) pageSize),
                    cityName,
                    AuditStatusEnum.PASS.getCode()  // 传入审核状态
            );

            page.setTotal(notePage.getTotal());

            // 🔧 修复：对附近笔记进行随机打乱，增加刷新时的变化
            if (notePage.getRecords() != null && !notePage.getRecords().isEmpty()) {
                log.info("🔄 附近笔记列表应用随机排序，原始数据量={}, pageSize={}", notePage.getRecords().size(), pageSize);
                List<NoteSearchVo> records = notePage.getRecords();
                Collections.shuffle(records, new Random(System.currentTimeMillis()));
                page.setRecords(records);
                log.info("✅ 附近笔记列表随机排序完成");
            } else {
                page.setRecords(notePage.getRecords());
            }
        }
        return page;
    }

    private static final String IP_CITY_CACHE_PREFIX = "ip_city:";
    private static final long CACHE_EXPIRE_HOURS = 24;

    private String getCurrentCity() {
        try {
            String ip = IpUtils.getIpAddr();
            log.info("获取到的ip: {}", ip);

            // 添加空IP检查
            if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                log.warn("获取到无效IP: {}", ip);
                return null;
            }

            // 先从缓存查询
            String cacheKey = IP_CITY_CACHE_PREFIX + ip;
            String cachedCity = redisUtils.get(cacheKey);
            if (StringUtils.isNotBlank(cachedCity)) {
                log.info("从缓存获取城市信息: {}", cachedCity);
                return cachedCity;
            }

            String address = AddressUtils.getRealAddressByIP(ip);
            log.info("获取到的地址: {}", address);

            String city = this.extractCity(address);

            // 缓存结果
            if (StringUtils.isNotBlank(city)) {
                redisUtils.set(cacheKey, city, CACHE_EXPIRE_HOURS * 3600); // 转换为秒
                log.info("城市信息已缓存: {}", city);
            }

            return city;
        } catch (Exception e) {
            log.error("获取当前位置失败", e);
            return null;
        }
    }

    private String extractCity(String fullAddress) {
        if (StringUtils.isBlank(fullAddress)) {
            return null;
        }
        fullAddress = fullAddress.trim();
        // 直辖市直接返回
        String[] directCities = {"北京", "上海", "天津", "重庆"};
        for (String city : directCities) {
            if (fullAddress.contains(city)) {
                return city;
            }
        }
        // 使用更精确的正则匹配
        Pattern pattern = Pattern.compile("([^省]+省)?([^市]+市)");
        Matcher matcher = pattern.matcher(fullAddress);
        if (matcher.find()) {
            String city = matcher.group(2);
            if (city != null) {
                // 统一处理：trim + 去掉"市"字 + 再次trim
                return city.trim().replace("市", "").trim();
            }
        }
        return null;
    }

    /**
     * 将 NoteVo 的 Page 转换为 NoteSearchVo 的 Page
     */
    private Page<NoteSearchVo> convertNoteVoPageToNoteSearchVoPage(Page<com.hongshu.web.domain.vo.NoteVo> noteVoPage) {
        Page<NoteSearchVo> page = new Page<>();
        page.setCurrent(noteVoPage.getCurrent());
        page.setSize(noteVoPage.getSize());
        page.setTotal(noteVoPage.getTotal());

        List<NoteSearchVo> noteSearchVos = new ArrayList<>();
        for (com.hongshu.web.domain.vo.NoteVo noteVo : noteVoPage.getRecords()) {
            NoteSearchVo searchVo = new NoteSearchVo();
            searchVo.setId(noteVo.getId());
            searchVo.setTitle(noteVo.getTitle());
            searchVo.setContent(noteVo.getContent());
            searchVo.setNoteCover(noteVo.getNoteCover());
            searchVo.setNoteType(noteVo.getNoteType());
            searchVo.setUid(noteVo.getUid());
            searchVo.setUsername(noteVo.getUsername());
            searchVo.setAvatar(noteVo.getAvatar());
            searchVo.setUrls(noteVo.getUrls());
            searchVo.setCid(noteVo.getCid());
            searchVo.setCpid(noteVo.getCpid());
            searchVo.setLikeCount(noteVo.getLikeCount());
            searchVo.setViewCount(noteVo.getViewCount());

            // 设置默认高度
            if (searchVo.getNoteCoverHeight() == null || searchVo.getNoteCoverHeight() == 0) {
                searchVo.setNoteCoverHeight(800);
            }

            noteSearchVos.add(searchVo);
        }

        page.setRecords(noteSearchVos);
        return page;
    }

    private Page<NoteSearchVo> getPageFromCache(String key) {
        try {
            String json = redisUtils.get(key);
            if (StringUtils.isBlank(json)) {
                return null;
            }
            CachedPage cp = JSON_MAPPER.readValue(json, CachedPage.class);
            Page<NoteSearchVo> page = new Page<>(cp.current, cp.size);
            page.setTotal(cp.total);
            page.setRecords(cp.records != null ? cp.records : Collections.emptyList());
            return page;
        } catch (Exception e) {
            log.debug("读取推荐缓存失败, key={}", key, e);
            return null;
        }
    }

    private void putPageToCache(String key, Page<NoteSearchVo> page, long ttlSec) {
        try {
            CachedPage cp = new CachedPage();
            cp.current = page.getCurrent();
            cp.size = page.getSize();
            cp.total = page.getTotal();
            cp.records = page.getRecords();
            String json = JSON_MAPPER.writeValueAsString(cp);
            redisUtils.setEx(key, json, ttlSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.debug("写入推荐缓存失败, key={}", key, e);
        }
    }

    private static class CachedPage {
        public long current;
        public long size;
        public long total;
        public List<NoteSearchVo> records;
    }

}
