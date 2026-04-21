package com.hongshu.web.service.recommendation.lightweight;

import com.hongshu.web.mapper.web.WebNoteBehaviorMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 笔记行为特征服务
 * 基于 web_note_behavior 表，按笔记维度聚合最近 N 天的曝光 / 观看 / 时长 / 完播等指标，
 * 为首页推荐重排提供更细粒度的质量信号。
 */
@Service
@Slf4j
public class NoteBehaviorFeatureService {

    /**
     * 默认统计窗口：最近 7 天
     */
    public static final int DEFAULT_WINDOW_DAYS = 7;

    private static final long FEATURE_CACHE_TTL_MS = 10 * 60 * 1000L;

    private volatile Map<String, NoteBehaviorFeature> cachedFeatures;
    private volatile long cachedFeaturesTimestamp;

    @Autowired
    private WebNoteBehaviorMapper webNoteBehaviorMapper;

    /**
     * 批量获取一批笔记在最近 N 天的行为特征
     *
     * @param noteIds    笔记 ID 列表（字符串形式）
     * @param windowDays 统计窗口天数
     * @return key 为笔记 ID 的特征 Map
     */
    public Map<String, NoteBehaviorFeature> getFeaturesForNotes(List<String> noteIds, int windowDays) {
        Map<String, NoteBehaviorFeature> result = new HashMap<>();
        if (noteIds == null || noteIds.isEmpty()) {
            return result;
        }

        Set<String> uniqueIds = new HashSet<>(noteIds);

        Map<String, NoteBehaviorFeature> cached = cachedFeatures;
        if (cached != null && (System.currentTimeMillis() - cachedFeaturesTimestamp) < FEATURE_CACHE_TTL_MS) {
            for (String nid : uniqueIds) {
                NoteBehaviorFeature f = cached.get(nid);
                if (f != null) {
                    result.put(nid, f);
                }
            }
            if (!result.isEmpty()) {
                log.debug("行为特征命中缓存, 请求{}条, 命中{}条", uniqueIds.size(), result.size());
                return result;
            }
        }

        if (windowDays <= 0) {
            windowDays = DEFAULT_WINDOW_DAYS;
        }

        Date endTime = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(endTime);
        cal.add(Calendar.DAY_OF_MONTH, -windowDays);
        Date startTime = cal.getTime();

        try {
            List<String> nids = new ArrayList<>(uniqueIds);
            List<Map<String, Object>> rows = webNoteBehaviorMapper.aggregateNoteFeaturesByNids(nids, startTime, endTime);
            if (rows == null || rows.isEmpty()) {
                return result;
            }

            for (Map<String, Object> row : rows) {
                if (row == null) {
                    continue;
                }
                Object nidObj = row.get("nid");
                if (nidObj == null) {
                    continue;
                }
                String nid = String.valueOf(nidObj);

                long exposureVal = toLong(row.get("exposure"));
                long viewVal = toLong(row.get("views"));
                long totalDurationSec = toLong(row.get("totalDurationSec"));
                long watchCount = toLong(row.get("watchCount"));
                long completedCount = toLong(row.get("completedCount"));

                double ctr = 0.0;
                if (exposureVal > 0) {
                    ctr = viewVal * 1.0 / exposureVal;
                }

                double avgWatchDurationSec = 0.0;
                double completionRate = 0.0;
                if (watchCount > 0) {
                    avgWatchDurationSec = totalDurationSec * 1.0 / watchCount;
                    completionRate = completedCount * 1.0 / watchCount;
                }

                NoteBehaviorFeature feature = new NoteBehaviorFeature();
                feature.setNoteId(nid);
                feature.setExposure(exposureVal);
                feature.setViews(viewVal);
                feature.setCtr(ctr);
                feature.setAvgWatchDurationSec(avgWatchDurationSec);
                feature.setCompletionRate(completionRate);

                result.put(nid, feature);
            }
        } catch (Exception e) {
            log.error("批量统计笔记行为特征失败", e);
        }

        if (!result.isEmpty()) {
            Map<String, NoteBehaviorFeature> merged = cached != null ? new HashMap<>(cached) : new HashMap<>();
            merged.putAll(result);
            cachedFeatures = merged;
            cachedFeaturesTimestamp = System.currentTimeMillis();
        }

        return result;
    }

    private long toLong(Object v) {
        if (v == null) {
            return 0L;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception ignore) {
            return 0L;
        }
    }

    /**
     * 笔记行为特征
     */
    @Data
    public static class NoteBehaviorFeature {
        /**
         * 笔记 ID
         */
        private String noteId;
        /**
         * 最近 N 天曝光次数
         */
        private long exposure;
        /**
         * 最近 N 天观看次数
         */
        private long views;
        /**
         * 近 N 天 CTR（view / exposure），范围 0~1
         */
        private double ctr;
        /**
         * 近 N 天平均观看时长（秒）
         */
        private double avgWatchDurationSec;
        /**
         * 近 N 天完播率（完播次数 / 观看次数），范围 0~1
         */
        private double completionRate;
    }
}

