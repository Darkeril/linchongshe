package com.hongshu.web.service;

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
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.common.core.enums.AuditStatusEnum;
import com.hongshu.common.core.utils.bean.BeanUtils;
import com.hongshu.web.domain.entity.WebLikeOrCollection;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.mapper.web.WebLikeOrCollectionMapper;
import com.hongshu.web.mapper.web.WebNoteMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


/**
 * Mahout协同过滤推荐服务（优化版）
 * 
 * 使用Apache Mahout实现基于用户的协同过滤推荐
 * 
 * 优化内容：
 * 1. 添加新用户检测和热门内容兜底
 * 2. 添加用户信息查询（avatar、username）
 * 3. 改进异常处理和降级机制
 * 4. 优化MySQL模式的推荐逻辑
 *
 * @author: hongshu
 */
@Service
@Slf4j
public class RecommendNoteServiceImpl extends ServiceImpl<WebLikeOrCollectionMapper, WebLikeOrCollection> implements IRecommendNoteService {

    private static final long MODEL_CACHE_TTL_MS = 10 * 60 * 1000L;

    private volatile DataModel cachedNoteModel;
    private volatile long cachedNoteModelTimestamp;
    private volatile DataModel cachedUserModel;
    private volatile long cachedUserModelTimestamp;

    @Autowired
    private DataSource dataSource;
    @Autowired
    private WebUserMapper userMapper;
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    @Autowired
    private WebNoteMapper noteMapper;
    @Autowired
    private WebLikeOrCollectionMapper likeOrCollectionMapper;


    /**
     * 推荐笔记（优化版）
     * 使用Mahout协同过滤算法
     *
     * @param userId 用户ID
     */
    @Override
    public List<NoteSearchVo> getRecommendNote(long userId) {
        log.info("Mahout协同过滤推荐 - 用户ID: {}", userId);
        
        // 优化1: 检查用户是否为新用户
        if (isNewUser(userId)) {
            log.info("检测到新用户: {}, 使用热门内容兜底", userId);
            return getHotNotesForNewUser(50);
        }
        
        List<NoteSearchVo> noteSearchVoList;
        try {
            DataModel model = getNoteDataModel();
            
            UserSimilarity similarity = new UncenteredCosineSimilarity(model);
            
            // Step 3: 定义邻居（最相似的10个用户）
            NearestNUserNeighborhood neighborhood = new NearestNUserNeighborhood(10, similarity, model);
            
            // Step 4: 创建基于用户的协同过滤推荐器
            Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
            
            // Step 5: 生成推荐（Top 10）
            List<RecommendedItem> recommendations = recommender.recommend(userId, 10);
            
            // 优化2: 检查推荐结果是否为空
            if (recommendations == null || recommendations.isEmpty()) {
                log.warn("Mahout协同过滤无推荐结果，使用热门内容兜底");
                return getHotNotesForNewUser(50);
            }
            
            List<Long> recommendedNoteIds = recommendations.stream()
                    .map(RecommendedItem::getItemID)
                    .collect(Collectors.toList());
            
            log.info("Mahout推荐笔记ID: {}", recommendedNoteIds);
            
            // Step 6: 获取推荐笔记详情（优先从MySQL，ES作为备选）
            noteSearchVoList = getNoteDetails(recommendedNoteIds);
            
            // 优化3: 如果获取详情失败，使用热门内容兜底
            if (noteSearchVoList == null || noteSearchVoList.isEmpty()) {
                log.warn("获取推荐笔记详情失败，使用热门内容兜底");
                return getHotNotesForNewUser(50);
            }
            
            // 随机排序
            Collections.shuffle(noteSearchVoList, new Random());
            
        } catch (Exception e) {
            log.error("Mahout协同过滤推荐异常，降级到热门内容", e);
            // 优化4: 异常时返回热门内容，而不是空列表
            return getHotNotesForNewUser(50);
        }
        
        log.info("Mahout推荐成功，返回 {} 条笔记", noteSearchVoList.size());
        return noteSearchVoList;
    }

    /**
     * 推荐用户
     *
     * @param userId 用户ID
     */
    @Override
    public List<WebUser> getRecommendUser(long userId) {
        List<WebUser> userList;
        try {
            DataModel model = getUserDataModel();
            
            UserSimilarity similarity = new UncenteredCosineSimilarity(model);
            // Step 3: 定义邻居：使用 NearestNUserNeighborhood 定义用户邻居。
            NearestNUserNeighborhood neighborhood = new NearestNUserNeighborhood(10, similarity, model);
            //  Step 4: 创建推荐器：基于用户的相似度，创建基于用户的推荐器。
            Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
            // Step 5: 生成推荐：为指定用户生成推荐用户列表。
            List<RecommendedItem> recommendations = recommender.recommend(userId, 10);
            List<Long> recommendedUserIds = recommendations.stream()
                    .map(RecommendedItem::getItemID)
                    .collect(Collectors.toList());
            // Step 6: 获取推荐笔记详情：从 Elasticsearch 中获取推荐笔记。
//            SearchRequest searchRequest = this.buildSearchRequest(recommendedUserIds);
            if (CollectionUtil.isNotEmpty(recommendedUserIds)) {
                userList = userMapper.selectBatchIds(recommendedUserIds);
            } else {
                userList = userMapper.selectList(new QueryWrapper<>());
            }
            // Step 7: 筛选出所需的数据。
//            SearchResponse<WebUser> searchResponse = elasticsearchClient.search(searchRequest, WebUser.class);
//            userList = searchResponse.hits().hits().stream()
//                    .map(Hit::source)
//                    .collect(Collectors.toList());
            // 随机排序
            Collections.shuffle(userList, new Random());
        } catch (Exception e) {
            log.error("获取推荐用户时发生异常：", e);
            return Collections.emptyList();
        }
        return userList;
    }

    /**
     * 获取推荐笔记列表
     */
    private SearchRequest buildSearchRequest(List<Long> recommendedNoteIds) {
        // 推荐列表为空，默认随机加载50条数据
        if (CollectionUtil.isEmpty(recommendedNoteIds)) {
            return SearchRequest.of(s -> s
                    .index(NoteConstant.NOTE_INDEX)
                    .size(50)
            );
        } else {
            // 推荐列表不为空，加载推荐数据
            List<FieldValue> fieldValues = recommendedNoteIds.stream()
                    .map(FieldValue::of)
                    .collect(Collectors.toList());
            return SearchRequest.of(s -> s
                    .index(NoteConstant.NOTE_INDEX)
                    .query(q -> q
                            .terms(t -> t
                                    .field("id")
                                    .terms(terms -> terms.value(fieldValues))
                            )
                    )
                    .size(recommendedNoteIds.size())
            );
        }
    }

    /**
     * 从MySQL获取推荐笔记（优化版）
     *
     * @param userId      用户ID
     * @param currentPage 当前页
     * @param pageSize    页大小
     * @return 分页结果
     */
    @Override
    public Page<NoteSearchVo> getRecommendNoteFromMysql(String userId, long currentPage, long pageSize) {
        log.info("Mahout MySQL推荐 - 用户: {}, 页码: {}, 大小: {}", userId, currentPage, pageSize);
        
        Page<NoteSearchVo> page = new Page<>(currentPage, pageSize);

        // 优化1: 检查用户是否为新用户
        long userBehaviorCount = likeOrCollectionMapper.selectCount(
                new QueryWrapper<WebLikeOrCollection>()
                        .eq("uid", userId)
                        .in("type", Arrays.asList(1, 3))
        );
        
        if (userBehaviorCount == 0) {
            log.info("检测到新用户: {}, 使用热门笔记", userId);
            return getHotNotesPage(currentPage, pageSize);
        }

        // 获取用户点赞和收藏的笔记ID列表
        List<String> likedOrCollectedNoteIds = likeOrCollectionMapper.selectList(
                        new QueryWrapper<WebLikeOrCollection>()
                                .eq("uid", userId)
                                .in("type", Arrays.asList(1, 3)) // 1：点赞图片 3：收藏图片
                                .select("like_or_collection_id")
                ).stream()
                .map(WebLikeOrCollection::getLikeOrCollectionId)
                .collect(Collectors.toList());

        // 获取用户点赞/收藏的笔记的作者ID列表
        List<String> authorIds = Collections.emptyList();
        if (!likedOrCollectedNoteIds.isEmpty()) {
            authorIds = noteMapper.selectList(
                            new QueryWrapper<WebNote>()
                                    .in("id", likedOrCollectedNoteIds)
                                    .select("DISTINCT uid")
                    ).stream()
                    .map(WebNote::getUid)
                    .collect(Collectors.toList());
        }

        // 获取用户点赞/收藏的笔记的分类ID列表
        List<String> categoryIds = Collections.emptyList();
        if (!likedOrCollectedNoteIds.isEmpty()) {
            categoryIds = noteMapper.selectList(
                            new QueryWrapper<WebNote>()
                                    .in("id", likedOrCollectedNoteIds)
                                    .select("DISTINCT cpid")
                    ).stream()
                    .map(WebNote::getCpid)
                    .collect(Collectors.toList());
        }

        // 构建查询条件
        QueryWrapper<WebNote> queryWrapper = new QueryWrapper<WebNote>()
                .eq("deleted", 0)
                .eq("audit_status", AuditStatusEnum.PASS.getCode());

        // 如果有用户行为数据，基于用户偏好推荐
        if (!authorIds.isEmpty() || !categoryIds.isEmpty()) {
            List<String> finalAuthorIds = authorIds;
            List<String> finalCategoryIds = categoryIds;
            queryWrapper.and(wrapper -> {
                if (!finalAuthorIds.isEmpty()) {
                    wrapper.or().in("uid", finalAuthorIds);
                }
                if (!finalCategoryIds.isEmpty()) {
                    wrapper.or().in("cid", finalCategoryIds);
                }
            });
        } else {
            // 优化2: 如果无偏好数据，返回热门笔记
            log.warn("用户 {} 无偏好数据，返回热门笔记", userId);
            return getHotNotesPage(currentPage, pageSize);
        }

        // 排除用户已经点赞/收藏的笔记
        if (!likedOrCollectedNoteIds.isEmpty()) {
            queryWrapper.notIn("id", likedOrCollectedNoteIds);
        }

        // 排除用户自己的笔记
        queryWrapper.ne("uid", userId);

        // 按点赞数、收藏数、创建时间排序
        queryWrapper.orderByDesc("like_count")
                .orderByDesc("collection_count")
                .orderByDesc("update_time");

        // 执行分页查询
        Page<WebNote> notePage = noteMapper.selectPage(new Page<>(currentPage, pageSize), queryWrapper);

        // 转换结果
        page.setCurrent(notePage.getCurrent());
        page.setSize(notePage.getSize());
        page.setTotal(notePage.getTotal());
        page.setRecords(notePage.getRecords().stream()
                .map(this::convertToSearchVo)
                .collect(Collectors.toList()));

        log.info("Mahout MySQL推荐成功，返回 {} 条笔记", page.getRecords().size());
        return page;
    }

    
    /**
     * 检查用户是否为新用户（优化新增）
     */
    private boolean isNewUser(long userId) {
        try {
            long count = likeOrCollectionMapper.selectCount(
                    new QueryWrapper<WebLikeOrCollection>()
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
     * 获取推荐笔记详情（优化新增）
     * 优先从MySQL查询，如果MySQL查询失败则尝试ES
     */
    private List<NoteSearchVo> getNoteDetails(List<Long> noteIds) {
        if (noteIds == null || noteIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            // 优先从MySQL获取，添加删除和审核状态筛选
            List<WebNote> notes = noteMapper.selectList(
                    new QueryWrapper<WebNote>()
                            .in("id", noteIds)
                            .eq("deleted", 0)
                            .eq("audit_status", AuditStatusEnum.PASS.getCode())
            );
            if (notes != null && !notes.isEmpty()) {
                log.info("从MySQL获取推荐笔记详情成功，数量: {}", notes.size());
                return notes.stream()
                        .map(this::convertToSearchVo)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("从MySQL获取笔记详情失败，尝试ES", e);
        }
        
        // MySQL失败，尝试从ES获取
        try {
            SearchRequest searchRequest = this.buildSearchRequest(noteIds);
            SearchResponse<NoteSearchVo> searchResponse = elasticsearchClient.search(searchRequest, NoteSearchVo.class);
            List<NoteSearchVo> noteSearchVos = searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
            log.info("从ES获取推荐笔记详情成功，数量: {}", noteSearchVos.size());
            return noteSearchVos;
        } catch (Exception e) {
            log.error("从ES获取笔记详情也失败", e);
            return Collections.emptyList();
        }
    }
    
    
    /**
     * 为新用户获取热门笔记（优化新增）
     */
    private List<NoteSearchVo> getHotNotesForNewUser(int limit) {
        try {
            log.info("获取热门笔记，数量: {}", limit);
            
            // 获取热门笔记（按点赞数降序）
            List<WebNote> hotNotes = noteMapper.selectList(
                    new QueryWrapper<WebNote>()
                            .eq("deleted", 0)
                            .eq("audit_status", AuditStatusEnum.PASS.getCode())
                            .orderByDesc("like_count", "collection_count", "create_time")
                            .last("LIMIT " + (limit / 2))
            );
            
            // 获取最新笔记
            List<WebNote> newNotes = noteMapper.selectList(
                    new QueryWrapper<WebNote>()
                            .eq("deleted", 0)
                            .eq("audit_status", AuditStatusEnum.PASS.getCode())
                            .orderByDesc("create_time")
                            .last("LIMIT " + (limit / 2))
            );
            
            // 合并去重
            java.util.Set<String> noteIds = new java.util.HashSet<>();
            List<WebNote> allNotes = new java.util.ArrayList<>();
            
            for (WebNote note : hotNotes) {
                if (noteIds.add(String.valueOf(note.getId()))) {
                    allNotes.add(note);
                }
            }
            
            for (WebNote note : newNotes) {
                if (noteIds.add(String.valueOf(note.getId()))) {
                    allNotes.add(note);
                }
            }
            
            // 随机打乱
            Collections.shuffle(allNotes);
            
            // 转换为VO
            return allNotes.stream()
                    .map(this::convertToSearchVo)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("获取热门笔记失败", e);
            return Collections.emptyList();
        }
    }
    
    
    /**
     * 获取热门笔记分页（优化新增）
     */
    private Page<NoteSearchVo> getHotNotesPage(long currentPage, long pageSize) {
        try {
            Page<WebNote> notePage = noteMapper.selectPage(
                    new Page<>(currentPage, pageSize),
                    new QueryWrapper<WebNote>()
                            .eq("deleted", 0)
                            .eq("audit_status", AuditStatusEnum.PASS.getCode())
                            .orderByDesc("like_count", "collection_count", "create_time")
            );
            
            Page<NoteSearchVo> page = new Page<>(notePage.getCurrent(), notePage.getSize());
            page.setTotal(notePage.getTotal());
            page.setRecords(notePage.getRecords().stream()
                    .map(this::convertToSearchVo)
                    .collect(Collectors.toList()));
            
            log.info("返回热门笔记分页，当前页: {}, 数量: {}", currentPage, page.getRecords().size());
            return page;
        } catch (Exception e) {
            log.error("获取热门笔记分页失败", e);
            return new Page<>(currentPage, pageSize);
        }
    }


    /**
     * 将WebNote实体转换为NoteSearchVo（优化版）
     */
    private NoteSearchVo convertToSearchVo(WebNote note) {
        NoteSearchVo vo = new NoteSearchVo();
        BeanUtils.copyProperties(note, vo);
        
        // 优化：添加用户信息查询
        try {
            if (note.getUid() != null) {
                WebUser user = userMapper.selectOne(
                        new QueryWrapper<WebUser>()
                                .eq("id", note.getUid())
                );
                if (user != null) {
                    vo.setAvatar(user.getAvatar());
                    vo.setUsername(user.getUsername());
                } else {
                    log.debug("未找到用户信息: uid={}", note.getUid());
                }
            }
        } catch (Exception e) {
            log.error("查询用户信息失败: uid={}", note.getUid(), e);
        }
        
        // 设置默认高度
        if (vo.getNoteCoverHeight() == null || vo.getNoteCoverHeight() == 0) {
            vo.setNoteCoverHeight(800);
        }
        
        return vo;
    }

    private DataModel getNoteDataModel() throws Exception {
        DataModel m = cachedNoteModel;
        if (m != null && (System.currentTimeMillis() - cachedNoteModelTimestamp) < MODEL_CACHE_TTL_MS) {
            return m;
        }
        m = new MySQLJDBCDataModel(
                dataSource, "web_like_or_collection", "uid",
                "like_or_collection_id", "type", null);
        cachedNoteModel = m;
        cachedNoteModelTimestamp = System.currentTimeMillis();
        return m;
    }

    private DataModel getUserDataModel() throws Exception {
        DataModel m = cachedUserModel;
        if (m != null && (System.currentTimeMillis() - cachedUserModelTimestamp) < MODEL_CACHE_TTL_MS) {
            return m;
        }
        m = new MySQLJDBCDataModel(
                dataSource, "web_follower", "uid",
                "fid", "type", null);
        cachedUserModel = m;
        cachedUserModelTimestamp = System.currentTimeMillis();
        return m;
    }
}
