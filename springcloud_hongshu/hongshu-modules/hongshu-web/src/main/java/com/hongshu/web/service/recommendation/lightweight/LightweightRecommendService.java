package com.hongshu.web.service.recommendation.lightweight;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hongshu.common.core.enums.AuditStatusEnum;
import com.hongshu.web.domain.entity.*;
import com.hongshu.web.domain.vo.NoteVo;
import com.hongshu.web.mapper.web.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 轻量级推荐服务 - 适合小红书项目
 * 基于标签+分类+关注+热门的四维度推荐算法
 *

 * @date 2024/01/01
 */
@Service
@Slf4j
public class LightweightRecommendService {

    /**
     * 各召回通道下限：避免 pageSize 较小时（如 10）整池只有几十条，去重/过滤后更少。
     */
    private static final int MIN_RECALL_TAG = 120;
    private static final int MIN_RECALL_CATEGORY = 100;
    private static final int MIN_RECALL_FOLLOW = 60;
    private static final int MIN_RECALL_HOT = 120;

    /** 新用户 / 兜底：仅查固定条数会导致 total≤100、翻页很快见底，适当放大 */
    private static final int HOT_RANDOM_POOL = 400;

    /** 标签召回：参与 IN 查询的最大标签数，避免 tag 维度过大拖慢 SQL */
    private static final int MAX_TAG_IDS_FOR_RECALL = 48;
    /** 标签-笔记关系表单次最多扫描行数（无索引时全表关联极慢） */
    private static final int TAG_RELATION_SCAN_LIMIT = 8000;
    /** 行为特征聚合：单次 IN 列表过大时 MySQL 分组成本高，超出部分按无行为特征打分 */
    private static final int MAX_NIDS_FOR_BEHAVIOR_AGG = 320;

    /** 召回阶段只需要打分和展示所需的字段，跳过 content 等大文本 */
    private static final String[] NOTE_RECALL_COLUMNS = {
            "id", "title", "note_cover", "note_cover_height", "uid", "urls",
            "note_type", "like_count", "collection_count", "comment_count", "view_count",
            "cid", "cpid", "create_time", "update_time", "time", "address", "pinned",
            "tags", "audit_status", "status", "deleted"
    };

    @Autowired
    private WebNoteMapper noteMapper;
    @Autowired
    private WebTagNoteRelationMapper tagNoteRelationMapper;
    @Autowired
    private WebLikeOrCollectionMapper likeMapper;
    @Autowired
    private WebFollowerMapper followerMapper;
    @Autowired
    private WebUserMapper userMapper;
    @Autowired
    private NoteBehaviorFeatureService noteBehaviorFeatureService;
    @Autowired
    private UserInterestProfileService userInterestProfileService;


    /**
     * 获取推荐笔记（轻量级版本）
     *
     * @param userId 用户ID
     * @param page   当前页
     * @param size   每页大小
     * @return 推荐笔记分页
     */
    public Page<NoteVo> getRecommendNotes(String userId, long page, long size) {
        log.info("轻量级推荐 - 用户: {}, 页码: {}, 大小: {}", userId, page, size);

        // 兜底：page / size 非法时给默认值
        if (page <= 0) {
            page = 1;
        }
        if (size <= 0) {
            size = 20;
        }

        if (StringUtils.isBlank(userId)) {
            // 未登录用户：热门内容 + 随机内容（不做 uid 过滤）
            return getHotAndRandomNotes(null, page, size);
        }

        // 检查用户是否为新用户（无历史行为）
        boolean isNewUser = checkIfNewUser(userId);
        if (isNewUser) {
            log.info("检测到新用户: {}, 使用热门+最新内容推荐", userId);
            // 新用户：仍然使用热门+最新，但排除自己的笔记
            return getHotAndRandomNotes(userId, page, size);
        }

        // 构建用户兴趣画像（最近 7 天推荐流观看行为）
        UserInterestProfileService.UserInterestProfile userProfile =
                userInterestProfileService.getProfile(userId, UserInterestProfileService.DEFAULT_WINDOW_DAYS);

        // 一次性查询最近500条点赞/收藏，召回用前200条，过滤用全集（消除重复查询）
        List<String> allLikedIds = likeMapper.selectList(
                        new QueryWrapper<WebLikeOrCollection>()
                                .eq("uid", userId)
                                .in("type", Arrays.asList(1, 3))
                                .select("like_or_collection_id")
                                .orderByDesc("create_time")
                                .last("LIMIT 500")
                ).stream()
                .map(WebLikeOrCollection::getLikeOrCollectionId)
                .collect(Collectors.toList());

        List<String> recentLikedForRecall = allLikedIds.size() > 200
                ? allLikedIds.subList(0, 200) : allLikedIds;
        Set<String> likedNoteIds = new HashSet<>(allLikedIds);

        List<NoteWithScore> candidates = new ArrayList<>();

        int limTag = (int) Math.max(size * 4, MIN_RECALL_TAG);
        int limCat = (int) Math.max(size * 3, MIN_RECALL_CATEGORY);
        int limFollow = (int) Math.max(size * 2, MIN_RECALL_FOLLOW);
        int limHot = (int) Math.max(size * 3, MIN_RECALL_HOT);

        CompletableFuture<List<NoteWithScore>> fTag =
                CompletableFuture.supplyAsync(() -> recallByUserTags(userId, limTag, recentLikedForRecall));
        CompletableFuture<List<NoteWithScore>> fCat =
                CompletableFuture.supplyAsync(() -> recallByUserCategories(userId, limCat, recentLikedForRecall));
        CompletableFuture<List<NoteWithScore>> fFollow =
                CompletableFuture.supplyAsync(() -> recallByFollowing(userId, limFollow));
        CompletableFuture<List<NoteWithScore>> fHot =
                CompletableFuture.supplyAsync(() -> recallHotNotes(limHot));

        CompletableFuture.allOf(fTag, fCat, fFollow, fHot).join();

        List<NoteWithScore> tagNotes = fTag.join();
        List<NoteWithScore> categoryNotes = fCat.join();
        List<NoteWithScore> followNotes = fFollow.join();
        List<NoteWithScore> hotNotes = fHot.join();

        candidates.addAll(tagNotes);
        log.info("标签召回: {} 条", tagNotes.size());
        candidates.addAll(categoryNotes);
        log.info("分类召回: {} 条", categoryNotes.size());
        candidates.addAll(followNotes);
        log.info("关注召回: {} 条", followNotes.size());
        candidates.addAll(hotNotes);
        log.info("热门召回: {} 条", hotNotes.size());

        if (candidates.isEmpty()) {
            log.warn("四路召回均为空，直接使用热门+随机推荐");
            return getHotAndRandomNotes(userId, page, size);
        }

        Map<String, NoteWithScore> noteMap = new HashMap<>();
        for (NoteWithScore note : candidates) {
            String noteId = String.valueOf(note.getNote().getId());
            if (!noteMap.containsKey(noteId) ||
                    noteMap.get(noteId).getScore() < note.getScore()) {
                noteMap.put(noteId, note);
            }
        }

        List<NoteWithScore> filtered = noteMap.values().stream()
                .filter(n -> !likedNoteIds.contains(String.valueOf(n.getNote().getId())))
                .collect(Collectors.toList());

        log.info("去重和过滤后: {} 条", filtered.size());

        // 6.1 如果召回结果太少（不足一页），补充热门内容
        if (filtered.size() < size) {
            log.warn("召回结果太少({})，补充热门内容，目标至少一页 size={}", filtered.size(), size);
            List<NoteWithScore> supplementHotNotes = recallHotNotes((int) Math.max(size * 3, MIN_RECALL_HOT));

            // 去重后添加
            Set<String> existingIds = filtered.stream()
                    .map(n -> String.valueOf(n.getNote().getId()))
                    .collect(Collectors.toSet());

            for (NoteWithScore hotNote : supplementHotNotes) {
                String noteId = String.valueOf(hotNote.getNote().getId());
                if (!existingIds.contains(noteId) && !likedNoteIds.contains(noteId)) {
                    filtered.add(hotNote);
                    existingIds.add(noteId);
                    // 最多补充到 3 倍页大小，避免一次返回过多
                    if (filtered.size() >= size * 3) {
                        break;
                    }
                }
            }
            log.info("补充后总数: {} 条", filtered.size());
        }

        // 7. 基于行为特征 + 用户兴趣重新打分（曝光 / 观看 / 时长 / 完播 + 兴趣分类）
        // 收集所有候选笔记ID
        List<String> candidateIds = filtered.stream()
                .map(n -> String.valueOf(n.getNote().getId()))
                .collect(Collectors.toList());
        // 近 7 天行为特征（nid 过多时聚合 SQL 成本高，超出部分按无行为特征参与打分）
        List<String> idsForBehavior = candidateIds.size() <= MAX_NIDS_FOR_BEHAVIOR_AGG
                ? candidateIds
                : candidateIds.subList(0, MAX_NIDS_FOR_BEHAVIOR_AGG);
        Map<String, NoteBehaviorFeatureService.NoteBehaviorFeature> featureMap =
                noteBehaviorFeatureService.getFeaturesForNotes(idsForBehavior, NoteBehaviorFeatureService.DEFAULT_WINDOW_DAYS);

        for (NoteWithScore n : filtered) {
            String nid = String.valueOf(n.getNote().getId());
            NoteBehaviorFeatureService.NoteBehaviorFeature feature = featureMap.get(nid);
            double newScore = calculateScoreWithBehaviorAndInterest(
                    n.getNote(), feature, userProfile);
            n.setScore(newScore);
        }

        // 8. 按分数排序
        filtered.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // 9. 打散（同一作者不连续）
        List<NoteWithScore> shuffled = shuffleByAuthor(filtered);

        // 10. 分页返回
        return buildPageResult(shuffled, page, size);
    }


    /**
     * 基于用户标签召回
     *
     * @param likedNoteIds 最近点赞/收藏笔记 id（与 {@link #recallByUserCategories} 共用同一次查询结果）
     */
    private List<NoteWithScore> recallByUserTags(String userId, int limit, List<String> likedNoteIds) {
        try {
            if (likedNoteIds == null || likedNoteIds.isEmpty()) {
                log.debug("用户 {} 没有点赞/收藏记录", userId);
                return Collections.emptyList();
            }

            // 2. 获取这些笔记的标签ID
            List<String> tagIds = tagNoteRelationMapper.selectList(
                    new QueryWrapper<WebTagNoteRelation>()
                            .in("nid", likedNoteIds)
            ).stream()
                    .map(WebTagNoteRelation::getTid)
                    .distinct()
                    .collect(Collectors.toList());

            if (tagIds.isEmpty()) {
                log.debug("用户 {} 喜欢的笔记没有标签", userId);
                return Collections.emptyList();
            }

            if (tagIds.size() > MAX_TAG_IDS_FOR_RECALL) {
                tagIds = new ArrayList<>(tagIds.subList(0, MAX_TAG_IDS_FOR_RECALL));
            }

            log.debug("用户 {} 的兴趣标签: {}", userId, tagIds);

            // 3. 根据标签获取推荐笔记，排除当前用户自己的笔记（SQL 侧限制扫描行数）
            int relationCap = Math.min(limit * 25, TAG_RELATION_SCAN_LIMIT);
            List<String> recommendNoteIds = tagNoteRelationMapper.selectList(
                    new QueryWrapper<WebTagNoteRelation>()
                            .in("tid", tagIds)
                            .last("LIMIT " + relationCap)
            ).stream()
                    .map(WebTagNoteRelation::getNid)
                    .filter(nid -> !likedNoteIds.contains(nid)) // 排除已互动的
                    .distinct()
                    .limit(limit)
                    .collect(Collectors.toList());

            if (recommendNoteIds.isEmpty()) {
                return Collections.emptyList();
            }

            List<WebNote> notes = noteMapper.selectList(
                    new QueryWrapper<WebNote>()
                            .select(NOTE_RECALL_COLUMNS)
                            .in("id", recommendNoteIds)
                            .eq("deleted", 0)
                            .eq("audit_status", AuditStatusEnum.PASS.getCode())
                            .ne("uid", userId)
            );

            return notes.stream()
                    .map(note -> new NoteWithScore(note, 0.0))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("标签召回异常", e);
            return Collections.emptyList();
        }
    }


    /**
     * 基于用户分类召回
     */
    private List<NoteWithScore> recallByUserCategories(String userId, int limit, List<String> likedNoteIds) {
        try {
            if (likedNoteIds == null || likedNoteIds.isEmpty()) {
                return Collections.emptyList();
            }

            List<WebNote> likedNotes = noteMapper.selectList(
                    new QueryWrapper<WebNote>()
                            .select("id", "cid", "cpid")
                            .in("id", likedNoteIds)
                            .eq("deleted", 0)
            );
            // 同时收集二级分类 cid 与一级分类 cpid（不少笔记只填 cpid，仅用 cid 会导致兴趣集合过窄、召回过少）
            Set<String> categoryIds = likedNotes.stream()
                    .flatMap(n -> Arrays.stream(new String[]{n.getCid(), n.getCpid()}))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());

            if (categoryIds.isEmpty()) {
                return Collections.emptyList();
            }

            log.debug("用户 {} 的兴趣分类(cid/cpid): {}", userId, categoryIds);

            List<WebNote> notes = noteMapper.selectList(
                    new QueryWrapper<WebNote>()
                            .select(NOTE_RECALL_COLUMNS)
                            .eq("deleted", 0)
                            .eq("audit_status", AuditStatusEnum.PASS.getCode())
                            .ne("uid", userId)
                            .and(w -> w.in("cid", categoryIds).or().in("cpid", categoryIds))
                            .orderByDesc("like_count", "create_time")
                            .last("LIMIT " + limit)
            );

            // 过滤掉已经互动过的
            Set<String> likedIdSet = new HashSet<>(likedNoteIds);
            return notes.stream()
                    .filter(note -> !likedIdSet.contains(String.valueOf(note.getId())))
                    .map(note -> new NoteWithScore(note, 0.0))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("分类召回异常", e);
            return Collections.emptyList();
        }
    }


    /**
     * 基于关注召回
     */
    private List<NoteWithScore> recallByFollowing(String userId, int limit) {
        try {
            List<String> followingUserIds = followerMapper.selectList(
                    new QueryWrapper<WebFollower>()
                            .select("fid")
                            .eq("uid", userId)
            ).stream()
                    .map(WebFollower::getFid)
                    .collect(Collectors.toList());

            if (followingUserIds.isEmpty()) {
                log.debug("用户 {} 没有关注任何人", userId);
                return Collections.emptyList();
            }

            List<WebNote> notes = noteMapper.selectList(
                    new QueryWrapper<WebNote>()
                            .select(NOTE_RECALL_COLUMNS)
                            .in("uid", followingUserIds)
                            .eq("deleted", 0)
                            .eq("audit_status", AuditStatusEnum.PASS.getCode())
                            .orderByDesc("create_time")
                            .last("LIMIT " + limit)
            );

            return notes.stream()
                    .map(note -> new NoteWithScore(note, 0.0))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("关注召回异常", e);
            return Collections.emptyList();
        }
    }


    /**
     * 热门召回
     */
    private List<NoteWithScore> recallHotNotes(int limit) {
        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

            List<WebNote> notes = noteMapper.selectList(
                    new QueryWrapper<WebNote>()
                            .select(NOTE_RECALL_COLUMNS)
                            .eq("deleted", 0)
                            .eq("audit_status", AuditStatusEnum.PASS.getCode())
                            .ge("create_time", sevenDaysAgo)
                            .orderByDesc("like_count", "collection_count", "comment_count")
                            .last("LIMIT " + limit)
            );

            if (notes.size() < limit) {
                Set<String> have = notes.stream()
                        .map(n -> String.valueOf(n.getId()))
                        .collect(Collectors.toSet());
                int need = limit - notes.size();
                List<WebNote> more = noteMapper.selectList(
                        new QueryWrapper<WebNote>()
                                .select(NOTE_RECALL_COLUMNS)
                                .eq("deleted", 0)
                                .eq("audit_status", AuditStatusEnum.PASS.getCode())
                                .ge("create_time", thirtyDaysAgo)
                                .lt("create_time", sevenDaysAgo)
                                .orderByDesc("like_count", "collection_count", "comment_count")
                                .last("LIMIT " + (need * 2))
                );
                for (WebNote n : more) {
                    if (have.add(String.valueOf(n.getId()))) {
                        notes.add(n);
                        if (notes.size() >= limit) {
                            break;
                        }
                    }
                }
            }
            if (notes.size() < limit) {
                Set<String> have = notes.stream()
                        .map(n -> String.valueOf(n.getId()))
                        .collect(Collectors.toSet());
                int need = limit - notes.size();
                List<WebNote> more = noteMapper.selectList(
                        new QueryWrapper<WebNote>()
                                .select(NOTE_RECALL_COLUMNS)
                                .eq("deleted", 0)
                                .eq("audit_status", AuditStatusEnum.PASS.getCode())
                                .orderByDesc("like_count", "collection_count", "comment_count")
                                .last("LIMIT " + (need * 2))
                );
                for (WebNote n : more) {
                    if (have.add(String.valueOf(n.getId()))) {
                        notes.add(n);
                        if (notes.size() >= limit) {
                            break;
                        }
                    }
                }
            }

            return notes.stream()
                    .map(note -> new NoteWithScore(note, 0.0))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("热门召回异常", e);
            return Collections.emptyList();
        }
    }


    /**
     * 基于互动质量 + 新鲜度 + 行为特征 + 用户兴趣 计算综合评分
     */
    private double calculateScoreWithBehaviorAndInterest(WebNote note,
                                                         NoteBehaviorFeatureService.NoteBehaviorFeature feature,
                                                         UserInterestProfileService.UserInterestProfile profile) {
        if (note == null) {
            return 0.0;
        }

        long likeCount = note.getLikeCount() == null ? 0L : note.getLikeCount();
        long collectCount = note.getCollectionCount() == null ? 0L : note.getCollectionCount();
        long commentCount = note.getCommentCount() == null ? 0L : note.getCommentCount();

        // 1) 互动质量分（0-100）：点赞 1x + 收藏 2x + 评论 3x，限制上限
        double qualityScore = (likeCount * 1.0
                + collectCount * 2.0
                + commentCount * 3.0) / 100.0;
        qualityScore = Math.min(100, qualityScore);

        // 2) 新鲜度分（0-100）：越新分越高
        double freshnessScore = 100.0;
        if (note.getCreateTime() != null) {
            long hoursSinceCreate = ChronoUnit.HOURS.between(
                    note.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                    LocalDateTime.now()
            );
            if (hoursSinceCreate > 1) {
                if (hoursSinceCreate <= 24) {
                    freshnessScore = 80;
                } else if (hoursSinceCreate <= 72) {
                    freshnessScore = 60;
                } else if (hoursSinceCreate <= 168) {
                    freshnessScore = 40;
                } else {
                    freshnessScore = 20;
                }
            }
        }

        // 3) 行为质量分（0-100）：基于近 7 天 CTR / 完播率 / 平均观看时长
        double behaviorScore = 0.0;
        if (feature != null) {
            // CTR 与完播率按 0~1 映射到 0~100，并做简单权重
            double ctrScore = Math.min(1.0, Math.max(0.0, feature.getCtr())) * 100.0;
            double completionScore = Math.min(1.0, Math.max(0.0, feature.getCompletionRate())) * 100.0;

            // 平均观看时长：假设 60 秒视为满分，超过部分不再放大
            double normalizedDuration = Math.min(60.0, Math.max(0.0, feature.getAvgWatchDurationSec()));
            double durationScore = (normalizedDuration / 60.0) * 100.0;

            behaviorScore = ctrScore * 0.4 + completionScore * 0.4 + durationScore * 0.2;
        }

        // 4) 兴趣匹配分（0-100）：命中用户 Top 分类则高分，否则中性
        double interestScore = 50.0;
        if (profile != null && profile.getTopCategoryIds() != null && !profile.getTopCategoryIds().isEmpty()) {
            String cpid = note.getCpid();
            if (cpid != null && profile.getTopCategoryIds().contains(cpid)) {
                // 命中用户在推荐流中高频观看的一级分类，给较高得分
                interestScore = 90.0;
            } else {
                // 未命中兴趣分类，略低于中性
                interestScore = 60.0;
            }
        }

        // 5) 综合分：互动(30%) + 新鲜度(20%) + 行为(30%) + 兴趣(20%)
        double finalScore = qualityScore * 0.30
                + freshnessScore * 0.20
                + behaviorScore * 0.30
                + interestScore * 0.20;

        log.debug("笔记 {} 综合评分 - 质量:{}, 新鲜度:{}, 行为:{}, 兴趣:{}, 最终:{}",
                note.getId(), qualityScore, freshnessScore, behaviorScore, interestScore, finalScore);

        return finalScore;
    }


    /**
     * 打散：同一作者不连续出现
     */
    private List<NoteWithScore> shuffleByAuthor(List<NoteWithScore> notes) {
        List<NoteWithScore> result = new ArrayList<>();
        Set<String> recentAuthors = new HashSet<>();
        Queue<NoteWithScore> pending = new LinkedList<>();

        int windowSize = 3; // 3条内不重复作者

        for (NoteWithScore note : notes) {
            String author = note.getNote().getUid();

            if (!recentAuthors.contains(author)) {
                result.add(note);
                recentAuthors.add(author);

                // 维护滑动窗口
                if (result.size() > windowSize) {
                    String oldAuthor = result.get(result.size() - windowSize - 1)
                            .getNote().getUid();
                    recentAuthors.remove(oldAuthor);
                }
            } else {
                // 暂存，等待后续插入
                pending.offer(note);
            }
        }

        // 将暂存的笔记尽量插入
        while (!pending.isEmpty()) {
            NoteWithScore note = pending.poll();
            result.add(note);
        }

        return result;
    }


    /**
     * 检查是否为新用户（无历史行为）
     */
    private boolean checkIfNewUser(String userId) {
        try {
            long count = likeMapper.selectCount(
                    new QueryWrapper<WebLikeOrCollection>()
                            .eq("uid", userId)
                            .in("type", Arrays.asList(1, 3))
            );

            long followCount = followerMapper.selectCount(
                    new QueryWrapper<WebFollower>()
                            .eq("uid", userId)
            );

            // 无点赞收藏且无关注，判定为新用户
            boolean isNew = (count == 0 && followCount == 0);
            if (isNew) {
                log.info("用户 {} 无历史行为数据，判定为新用户", userId);
            }
            return isNew;
        } catch (Exception e) {
            log.error("检查新用户失败", e);
            return false;
        }
    }


    /**
     * 热门 + 随机内容（可指定排除某个用户自己的笔记）
     * ✅ 使用固定的查询数量，确保不同端查询到的数据量一致
     */
    private Page<NoteVo> getHotAndRandomNotes(String excludeUserId, long page, long size) {
        // 固定池过小会导致 total 很小、翻页很快无数据；在「多端 total 一致」与「池子够大」间取折中
        int fixedQueryLimit = HOT_RANDOM_POOL;
        int hotLimit = fixedQueryLimit / 2;
        int recentLimit = fixedQueryLimit - hotLimit;
        
        List<WebNote> hotNotes = noteMapper.selectList(
                new QueryWrapper<WebNote>()
                        .select(NOTE_RECALL_COLUMNS)
                        .eq("deleted", 0)
                        .eq("audit_status", AuditStatusEnum.PASS.getCode())
                        .ne(StringUtils.isNotBlank(excludeUserId), "uid", excludeUserId)
                        .orderByDesc("like_count", "collection_count")
                        .last("LIMIT " + hotLimit)
        );

        List<WebNote> recentNotes = noteMapper.selectList(
                new QueryWrapper<WebNote>()
                        .select(NOTE_RECALL_COLUMNS)
                        .eq("deleted", 0)
                        .eq("audit_status", AuditStatusEnum.PASS.getCode())
                        .ne(StringUtils.isNotBlank(excludeUserId), "uid", excludeUserId)
                        .orderByDesc("create_time")
                        .last("LIMIT " + recentLimit)
        );

        // 合并去重
        Set<String> noteIds = new HashSet<>();
        List<WebNote> allNotes = new ArrayList<>();

        for (WebNote note : hotNotes) {
            if (noteIds.add(String.valueOf(note.getId()))) {
                allNotes.add(note);
            }
        }

        for (WebNote note : recentNotes) {
            if (noteIds.add(String.valueOf(note.getId()))) {
                allNotes.add(note);
            }
        }

        // 每次请求独立随机：避免 new Random(page) 导致第 1 页刷新顺序永远不变
        Collections.shuffle(allNotes, ThreadLocalRandom.current());

        // 分页
        int start = (int) ((page - 1) * size);
        int end = Math.min(start + (int) size, allNotes.size());

        List<WebNote> pageNotes;
        if (start >= allNotes.size()) {
            // 如果请求的页码超出范围，返回空列表
            pageNotes = Collections.emptyList();
        } else {
            pageNotes = allNotes.subList(start, end);
        }

        // 转换为VO
        List<NoteVo> noteVos = convertToVos(pageNotes);

        Page<NoteVo> result = new Page<>(page, size);
        result.setRecords(noteVos);
        // ✅ 优化：设置总数为实际查询到的数量，而不是分页后的数量
        // 这样前端可以知道还有更多数据可以加载
        result.setTotal(allNotes.size());
        log.info("热门随机推荐 - 查询:{}条, 去重后:{}条, 当前页:{}条", 
                hotLimit + recentLimit, allNotes.size(), noteVos.size());
        return result;
    }


    /**
     * 构建分页结果
     */
    private Page<NoteVo> buildPageResult(List<NoteWithScore> notes, long page, long size) {
        // 分页
        int start = (int) ((page - 1) * size);
        int end = Math.min(start + (int) size, notes.size());

        List<NoteWithScore> pageNotes = notes.subList(
                Math.min(start, notes.size()),
                end
        );

        // 转换为VO（批量补全用户信息，避免 N+1）
        List<WebNote> pageEntities = pageNotes.stream()
                .map(NoteWithScore::getNote)
                .collect(Collectors.toList());
        List<NoteVo> noteVos = convertToVos(pageEntities);

        Page<NoteVo> result = new Page<>(page, size);
        result.setRecords(noteVos);
        result.setTotal(notes.size());

        log.info("推荐结果 - 总数:{}, 当前页:{}, 返回:{} 条", notes.size(), page, noteVos.size());

        return result;
    }


    /**
     * 转换为VO（批量）
     */
    private List<NoteVo> convertToVos(List<WebNote> notes) {
        if (notes == null || notes.isEmpty()) {
            return Collections.emptyList();
        }

        List<NoteVo> vos = notes.stream()
                .map(note -> {
                    NoteVo vo = new NoteVo();
                    vo.setId(String.valueOf(note.getId()));
                    vo.setTitle(note.getTitle());
                    vo.setContent(note.getContent());
                    vo.setNoteCover(note.getNoteCover());
                    vo.setUid(note.getUid());
                    vo.setCid(note.getCid());
                    vo.setCpid(note.getCpid());
                    vo.setUrls(note.getUrls());
                    vo.setCount(note.getCount());
                    vo.setLikeCount(note.getLikeCount());
                    vo.setCollectionCount(note.getCollectionCount());
                    vo.setCommentCount(note.getCommentCount());
                    vo.setViewCount(note.getViewCount());
                    vo.setNoteType(note.getNoteType());
                    return vo;
                })
                .collect(Collectors.toList());

        Set<String> uids = notes.stream()
                .map(WebNote::getUid)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (uids.isEmpty()) {
            return vos;
        }

        Map<String, WebUser> userMap = userMapper.selectList(
                new QueryWrapper<WebUser>().in("id", uids)
        ).stream().collect(Collectors.toMap(u -> String.valueOf(u.getId()), u -> u, (a, b) -> a));

        for (NoteVo vo : vos) {
            WebUser user = userMap.get(vo.getUid());
            if (user != null) {
                vo.setAvatar(user.getAvatar());
                vo.setUsername(user.getUsername());
            }
        }
        return vos;
    }


    /**
     * 转换为VO
     */
    private NoteVo convertToVo(WebNote note) {
        NoteVo vo = new NoteVo();
        vo.setId(String.valueOf(note.getId()));
        vo.setTitle(note.getTitle());
        vo.setContent(note.getContent());
        vo.setNoteCover(note.getNoteCover());
        vo.setUid(note.getUid());
        vo.setCid(note.getCid());
        vo.setCpid(note.getCpid());
        vo.setUrls(note.getUrls());
        vo.setCount(note.getCount());
        vo.setLikeCount(note.getLikeCount());
        vo.setCollectionCount(note.getCollectionCount());
        vo.setCommentCount(note.getCommentCount());
        vo.setViewCount(note.getViewCount());
        vo.setNoteType(note.getNoteType());

        // 单条转换不主动查用户信息（避免 N+1），批量转换时由 convertToVos 统一补全

        return vo;
    }


    /**
     * 内部类：带评分的笔记
     */
    @Data
    @AllArgsConstructor
    private static class NoteWithScore {
        private WebNote note;
        private double score;
    }
}

