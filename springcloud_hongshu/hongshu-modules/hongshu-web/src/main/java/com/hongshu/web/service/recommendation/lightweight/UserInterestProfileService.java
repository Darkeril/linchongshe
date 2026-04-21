package com.hongshu.web.service.recommendation.lightweight;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.domain.entity.WebNoteBehavior;
import com.hongshu.web.mapper.web.WebNoteBehaviorMapper;
import com.hongshu.web.mapper.web.WebNoteMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 用户兴趣画像服务
 * 基于 web_note_behavior + web_note，在最近 N 天内统计用户在推荐流中的观看/完播行为，
 * 推断其偏好的一级分类（cpid），为首页推荐排序提供个性化信号。
 */
@Service
@Slf4j
public class UserInterestProfileService {

    /**
     * 默认统计窗口：最近 7 天
     */
    public static final int DEFAULT_WINDOW_DAYS = 7;

    private static final long PROFILE_CACHE_TTL_MS = 10 * 60 * 1000L;

    private final ConcurrentHashMap<String, CachedProfile> profileCache = new ConcurrentHashMap<>();

    @Autowired
    private WebNoteBehaviorMapper webNoteBehaviorMapper;

    @Autowired
    private WebNoteMapper webNoteMapper;

    /**
     * 构建指定用户在最近 N 天的兴趣画像
     *
     * @param userId     用户ID
     * @param windowDays 统计天数
     * @return 兴趣画像（可能为空）
     */
    public UserInterestProfile getProfile(String userId, int windowDays) {
        UserInterestProfile profile = new UserInterestProfile();
        profile.setUserId(userId);
        profile.setTopCategoryIds(Collections.emptySet());

        if (userId == null || userId.isEmpty()) {
            return profile;
        }
        if (windowDays <= 0) {
            windowDays = DEFAULT_WINDOW_DAYS;
        }

        String cacheKey = userId + ":" + windowDays;
        CachedProfile cp = profileCache.get(cacheKey);
        if (cp != null && (System.currentTimeMillis() - cp.timestamp) < PROFILE_CACHE_TTL_MS) {
            return cp.profile;
        }

        Date endTime = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(endTime);
        cal.add(Calendar.DAY_OF_MONTH, -windowDays);
        Date startTime = cal.getTime();

        try {
            // 1. 最近 N 天，在推荐流场景下（scene = recommend）的观看 / watch 行为
            // 限制行数，避免高活用户一次拉全表行为导致接口过慢
            List<WebNoteBehavior> behaviors = webNoteBehaviorMapper.selectList(
                    new QueryWrapper<WebNoteBehavior>()
                            .eq("uid", userId)
                            .in("event_type", Arrays.asList("view", "watch"))
                            .eq("scene", "recommend")
                            .ge("create_time", startTime)
                            .le("create_time", endTime)
                            .select("nid", "uid")
                            .orderByDesc("create_time")
                            .last("LIMIT 5000")
            );

            if (behaviors == null || behaviors.isEmpty()) {
                return profile;
            }

            // 2. 找出涉及到的笔记ID
            Set<String> noteIds = behaviors.stream()
                    .map(WebNoteBehavior::getNid)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (noteIds.isEmpty()) {
                return profile;
            }

            // 3. 查询这些笔记的一级分类 cpid
            List<WebNote> notes = webNoteMapper.selectList(
                    new QueryWrapper<WebNote>()
                            .in("id", noteIds)
                            .eq("deleted", 0)
                            .select("id", "cpid")
            );
            if (notes == null || notes.isEmpty()) {
                return profile;
            }

            Map<String, Long> categoryCount = notes.stream()
                    .filter(n -> n.getCpid() != null && !n.getCpid().isEmpty())
                    .collect(Collectors.groupingBy(WebNote::getCpid, Collectors.counting()));
            if (categoryCount.isEmpty()) {
                return profile;
            }

            // 4. 取 TopK 分类作为兴趣分类（例如前 5 个）
            int topK = 5;
            List<Map.Entry<String, Long>> sorted = new ArrayList<>(categoryCount.entrySet());
            sorted.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
            Set<String> topCategories = sorted.stream()
                    .limit(topK)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            profile.setTopCategoryIds(topCategories);
            profileCache.put(cacheKey, new CachedProfile(profile, System.currentTimeMillis()));
            return profile;
        } catch (Exception e) {
            log.error("构建用户兴趣画像失败, uid={}", userId, e);
            profileCache.put(cacheKey, new CachedProfile(profile, System.currentTimeMillis()));
            return profile;
        }
    }

    private static class CachedProfile {
        final UserInterestProfile profile;
        final long timestamp;
        CachedProfile(UserInterestProfile profile, long timestamp) {
            this.profile = profile;
            this.timestamp = timestamp;
        }
    }

    /**
     * 用户兴趣画像
     */
    @Data
    public static class UserInterestProfile {
        /**
         * 用户ID
         */
        private String userId;
        /**
         * 用户在推荐流中最感兴趣的一级分类ID集合（cpid）
         */
        private Set<String> topCategoryIds;
    }
}

