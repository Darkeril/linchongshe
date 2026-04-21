package com.hongshu.web.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.common.core.validator.ValidatorUtil;
import com.hongshu.web.domain.dto.DemoAccountConfigDTO;
import com.hongshu.web.domain.entity.WebFollower;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.domain.vo.*;
import com.hongshu.web.mapper.web.WebFollowerMapper;
import com.hongshu.web.mapper.web.WebNoteBehaviorMapper;
import com.hongshu.web.mapper.web.WebNoteMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.service.sys.IAnalyticsService;
import com.hongshu.web.service.sys.ISysSystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据中心-数据分析 Service 实现
 */
@Slf4j
@Service
public class AnalyticsServiceImpl implements IAnalyticsService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM-dd");
    /**
     * 笔记类型：0 图文，1 视频，3 live图
     */
    private static final String NOTE_TYPE_VIDEO = "1";
    private static final String NOTE_TYPE_LIVE = "3";

    @Autowired
    private WebUserMapper userMapper;
    @Autowired
    private WebNoteMapper noteMapper;
    @Autowired
    private WebFollowerMapper followerMapper;
    @Autowired
    private WebNoteBehaviorMapper noteBehaviorMapper;
    @Autowired
    private ISysSystemConfigService systemConfigService;


    @Override
    public AnalyticsDataVo getAnalytics(String uid, int days) {
        String creatorUid = this.resolveCreatorUid(uid);
        AnalyticsDataVo vo = new AnalyticsDataVo();
        vo.setView(buildViewEmpty());
        vo.setInteract(buildTabEmpty());
        vo.setFans(buildTabEmpty());
        vo.setPublish(buildTabEmpty());
        if (ValidatorUtil.isNull(creatorUid)) {
            return vo;
        }
        if (days != 7 && days != 30) {
            days = 7;
        }
        Date endTime = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -days);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startTime = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -days);
        Date prevStartTime = cal.getTime();
        Date prevEndTime = new Date(startTime.getTime() - 1);

        List<WebNote> notesInRange = listNotesInRange(creatorUid, startTime, endTime);
        List<WebNote> notesPrevRange = listNotesInRange(creatorUid, prevStartTime, prevEndTime);
        Map<LocalDate, List<WebNote>> notesByDate = groupNotesByDate(notesInRange);

        List<WebFollower> followersInRange = listFollowersInRange(creatorUid, startTime, endTime);
        List<WebFollower> followersPrevRange = listFollowersInRange(creatorUid, prevStartTime, prevEndTime);
        Map<LocalDate, Long> newFansByDate = followersInRange.stream()
                .filter(f -> f.getCreateTime() != null)
                .collect(Collectors.groupingBy(
                        f -> f.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                        Collectors.counting()));

        LocalDate startDate = startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = endTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        List<String> dates = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            dates.add(d.format(DATE_FMT));
        }

        long totalViews = notesInRange.stream().mapToLong(n -> n.getViewCount() != null ? n.getViewCount() : 0L).sum();
        long prevTotalViews = notesPrevRange.stream().mapToLong(n -> n.getViewCount() != null ? n.getViewCount() : 0L).sum();
        // 行为表统计（曝光、观看、来源、时段、时长完播）
        Long behaviorExposure = noteBehaviorMapper.countExposureByCreatorAndTimeRange(creatorUid, startTime, endTime);
        Long behaviorViews = noteBehaviorMapper.countViewByCreatorAndTimeRange(creatorUid, startTime, endTime);
        Long prevBehaviorExposure = noteBehaviorMapper.countExposureByCreatorAndTimeRange(creatorUid, prevStartTime, prevEndTime);
        Long prevBehaviorViews = noteBehaviorMapper.countViewByCreatorAndTimeRange(creatorUid, prevStartTime, prevEndTime);
        if (behaviorExposure == null) behaviorExposure = 0L;
        if (behaviorViews == null) behaviorViews = 0L;
        if (prevBehaviorExposure == null) prevBehaviorExposure = 0L;
        if (prevBehaviorViews == null) prevBehaviorViews = 0L;
        long totalLike = notesInRange.stream().mapToLong(n -> n.getLikeCount() != null ? n.getLikeCount() : 0L).sum();
        long totalComment = notesInRange.stream().mapToLong(n -> n.getCommentCount() != null ? n.getCommentCount() : 0L).sum();
        long totalCollect = notesInRange.stream().mapToLong(n -> n.getCollectionCount() != null ? n.getCollectionCount() : 0L).sum();
        long prevLike = notesPrevRange.stream().mapToLong(n -> n.getLikeCount() != null ? n.getLikeCount() : 0L).sum();
        long prevComment = notesPrevRange.stream().mapToLong(n -> n.getCommentCount() != null ? n.getCommentCount() : 0L).sum();
        long prevCollect = notesPrevRange.stream().mapToLong(n -> n.getCollectionCount() != null ? n.getCollectionCount() : 0L).sum();
        long newFans = followersInRange.size();
        long prevNewFans = followersPrevRange.size();
        long totalFansNow = followerMapper.selectCount(new QueryWrapper<WebFollower>().eq("fid", creatorUid));
        long publishCount = notesInRange.size();
        long publishPrev = notesPrevRange.size();
        long videoCount = notesInRange.stream().filter(n -> NOTE_TYPE_VIDEO.equals(n.getNoteType())).count();
        long liveCount = notesInRange.stream().filter(n -> NOTE_TYPE_LIVE.equals(n.getNoteType())).count();
        long imageCount = notesInRange.size() - videoCount - liveCount;
        long videoPrev = notesPrevRange.stream().filter(n -> NOTE_TYPE_VIDEO.equals(n.getNoteType())).count();
        long livePrev = notesPrevRange.stream().filter(n -> NOTE_TYPE_LIVE.equals(n.getNoteType())).count();
        long imagePrev = notesPrevRange.size() - videoPrev - livePrev;

        vo.setView(buildView(creatorUid, notesInRange, notesByDate, dates, startTime, endTime,
                totalViews, prevTotalViews, behaviorExposure, prevBehaviorExposure, behaviorViews, prevBehaviorViews));
        vo.setInteract(buildInteract(creatorUid, startTime, endTime, totalLike, totalComment, totalCollect, notesByDate, dates, prevLike, prevComment, prevCollect));
        vo.setFans(buildFans(totalFansNow, newFans, newFansByDate, dates, prevNewFans));
        vo.setPublish(buildPublish(publishCount, videoCount, imageCount, liveCount, notesByDate, dates, publishPrev, videoPrev, imagePrev, livePrev));
        return vo;
    }

    private List<WebNote> listNotesInRange(String uid, Date start, Date end) {
        QueryWrapper<WebNote> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(WebNote::getUid, uid)
                .eq(WebNote::getDeleted, 0)
                .ge(WebNote::getCreateTime, start)
                .le(WebNote::getCreateTime, end);
        return noteMapper.selectList(qw);
    }

    private List<WebFollower> listFollowersInRange(String fid, Date start, Date end) {
        QueryWrapper<WebFollower> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(WebFollower::getFid, fid)
                .ge(WebFollower::getCreateTime, start)
                .le(WebFollower::getCreateTime, end);
        return followerMapper.selectList(qw);
    }

    private Map<LocalDate, List<WebNote>> groupNotesByDate(List<WebNote> notes) {
        return notes.stream()
                .filter(n -> n.getCreateTime() != null)
                .collect(Collectors.groupingBy(
                        n -> n.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
    }

    private static int ring(long current, long previous) {
        if (previous == 0) return 0;
        return (int) Math.round((current - previous) * 100.0 / previous);
    }

    private AnalyticsViewVo buildView(String creatorUid,
                                      List<WebNote> notesInRange,
                                      Map<LocalDate, List<WebNote>> notesByDate,
                                      List<String> dates,
                                      Date startTime,
                                      Date endTime,
                                      long totalViewsNote,
                                      long prevTotalViewsNote,
                                      long behaviorExposure,
                                      long prevBehaviorExposure,
                                      long behaviorViews,
                                      long prevBehaviorViews) {
        AnalyticsViewVo vo = new AnalyticsViewVo();
        long exposure = behaviorExposure;
        long viewCount = behaviorViews;
        String coverCtr = exposure > 0 ? String.format("%.1f%%", viewCount * 100.0 / exposure) : "0%";
        int ringExposure = ring(behaviorExposure, prevBehaviorExposure);
        int ringView = ring(behaviorViews, prevBehaviorViews);

        List<Map<String, Object>> sceneList = noteBehaviorMapper.countViewByScene(creatorUid, startTime, endTime);
        List<AnalyticsNameValueVo> source = new ArrayList<>();
        if (sceneList != null) {
            for (Map<String, Object> row : sceneList) {
                Object name = row.get("scene");
                Object cnt = row.get("cnt");
                source.add(nameValue(name != null ? name.toString() : "other", cnt instanceof Number ? ((Number) cnt).longValue() : 0L));
            }
        }
        if (source.isEmpty()) {
            source.add(nameValue("其他来源", viewCount));
        }

        List<Integer> timeSlot = new ArrayList<>(24);
        for (int i = 0; i < 24; i++) timeSlot.add(0);
        List<Map<String, Object>> hourList = noteBehaviorMapper.countViewByHour(creatorUid, startTime, endTime);
        if (hourList != null) {
            for (Map<String, Object> row : hourList) {
                Object h = row.get("hour");
                Object cnt = row.get("cnt");
                if (h instanceof Number) {
                    int idx = ((Number) h).intValue();
                    if (idx >= 0 && idx < 24) {
                        timeSlot.set(idx, cnt instanceof Number ? ((Number) cnt).intValue() : 0);
                    }
                }
            }
        }

        Map<String, Object> durationResult = noteBehaviorMapper.sumDurationAndCompletion(creatorUid, startTime, endTime);
        long totalDurationSec = 0L;
        long watchCount = 0L;
        long completedCount = 0L;
        if (durationResult != null) {
            Object t = durationResult.get("totalDurationSec");
            Object w = durationResult.get("watchCount");
            Object c = durationResult.get("completedCount");
            totalDurationSec = t instanceof Number ? ((Number) t).longValue() : 0L;
            watchCount = w instanceof Number ? ((Number) w).longValue() : 0L;
            completedCount = c instanceof Number ? ((Number) c).longValue() : 0L;
        }
        String avgDurationStr = watchCount > 0 ? (totalDurationSec / watchCount) + "秒" : "0秒";
        // 观看总时长：按小时展示，保留 1 位小数
        double totalHours = totalDurationSec / 3600.0;
        String totalDurationStr = String.format("%.1f小时", totalHours);
        String completionRateStr = watchCount > 0 ? String.format("%.1f%%", completedCount * 100.0 / watchCount) : "0%";

        vo.setOverview(Arrays.asList(
                item("曝光数", exposure, ringExposure),
                item("观看数", viewCount, ringView),
                item("封面点击率", coverCtr, ringView),
                item("平均观看时长", avgDurationStr, 0),
                item("观看总时长", totalDurationStr, 0),
                item("视频完播率", completionRateStr, 0)
        ));

        List<Long> trend = new ArrayList<>();
        List<Map<String, Object>> viewByDay = noteBehaviorMapper.countViewByDay(creatorUid, startTime, endTime);
        Map<String, Long> viewCountByDate = new HashMap<>();
        if (viewByDay != null) {
            for (Map<String, Object> row : viewByDay) {
                Object d = row.get("date");
                Object cnt = row.get("cnt");
                if (d != null)
                    viewCountByDate.put(d.toString(), cnt instanceof Number ? ((Number) cnt).longValue() : 0L);
            }
        }
        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i < dates.size(); i++) {
            LocalDate date = parseDateKey(dates.get(i), currentYear);
            String dateKey = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            trend.add(viewCountByDate.getOrDefault(dateKey, 0L));
        }
        vo.setTrend(trend);
        vo.setSource(source);
        vo.setTimeSlot(timeSlot);
        return vo;
    }

    /**
     * 将 "MM-dd" 转为当年 LocalDate
     */
    private LocalDate parseDateKey(String dateStr, int year) {
        if (dateStr == null || dateStr.length() < 5) return LocalDate.now();
        String[] parts = dateStr.split("-");
        if (parts.length >= 2) {
            int month = Integer.parseInt(parts[0]);
            int day = Integer.parseInt(parts[1]);
            return LocalDate.of(year, month, day);
        }
        return LocalDate.now();
    }

    private AnalyticsTabVo buildInteract(String creatorUid, Date startTime, Date endTime,
                                         long like, long comment, long collect,
                                         Map<LocalDate, List<WebNote>> notesByDate,
                                         List<String> dates,
                                         long prevLike, long prevComment, long prevCollect) {
        AnalyticsTabVo vo = new AnalyticsTabVo();
        Long shareObj = noteBehaviorMapper.countEventByCreatorAndTimeRange(creatorUid, "share", startTime, endTime);
        long share = shareObj != null ? shareObj : 0L;
        int ringLike = ring(like, prevLike);
        int ringComment = ring(comment, prevComment);
        int ringCollect = ring(collect, prevCollect);
        vo.setOverview(Arrays.asList(
                item("点赞数", like, ringLike),
                item("评论数", comment, ringComment),
                item("收藏数", collect, ringCollect),
                item("分享数", share, 0)
        ));
        List<Long> trend = new ArrayList<>();
        List<Map<String, Object>> shareByDay = noteBehaviorMapper.countEventByDay(creatorUid, "share", startTime, endTime);
        Map<String, Long> shareCountByDate = new HashMap<>();
        if (shareByDay != null) {
            for (Map<String, Object> row : shareByDay) {
                Object d = row.get("date");
                Object cnt = row.get("cnt");
                if (d != null) {
                    shareCountByDate.put(d.toString(), cnt instanceof Number ? ((Number) cnt).longValue() : 0L);
                }
            }
        }
        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i < dates.size(); i++) {
            LocalDate date = parseDateKey(dates.get(i), currentYear);
            List<WebNote> dayNotes = notesByDate.getOrDefault(date, Collections.emptyList());
            long v = dayNotes.stream().mapToLong(n ->
                    (n.getLikeCount() != null ? n.getLikeCount() : 0L)
                            + (n.getCommentCount() != null ? n.getCommentCount() : 0L)
                            + (n.getCollectionCount() != null ? n.getCollectionCount() : 0L)).sum();
            String dateKey = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            v += shareCountByDate.getOrDefault(dateKey, 0L);
            trend.add(v);
        }
        vo.setTrend(trend);
        return vo;
    }

    private AnalyticsTabVo buildFans(long totalFansNow, long newFans, Map<LocalDate, Long> newFansByDate, List<String> dates, long prevNewFans) {
        AnalyticsTabVo vo = new AnalyticsTabVo();
        int ringNew = ring(newFans, prevNewFans);
        vo.setOverview(Arrays.asList(
                item("总粉丝", totalFansNow, 0),
                item("新增关注", newFans, ringNew)
        ));
        List<Long> trend = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i < dates.size(); i++) {
            LocalDate date = parseDateKey(dates.get(i), currentYear);
            trend.add(newFansByDate.getOrDefault(date, 0L));
        }
        vo.setTrend(trend);
        return vo;
    }

    private AnalyticsTabVo buildPublish(long total, long video, long image, long live,
                                        Map<LocalDate, List<WebNote>> notesByDate,
                                        List<String> dates,
                                        long prevTotal, long videoPrev, long imagePrev, long livePrev) {
        AnalyticsTabVo vo = new AnalyticsTabVo();
        int ringTotal = ring(total, prevTotal);
        vo.setOverview(Arrays.asList(
                item("总发布", total, ringTotal),
                item("发布视频", video, ring(video, videoPrev)),
                item("发布图文", image, ring(image, imagePrev)),
                item("发布Live", live, ring(live, livePrev))
        ));
        List<Long> trend = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i < dates.size(); i++) {
            LocalDate date = parseDateKey(dates.get(i), currentYear);
            int count = notesByDate.getOrDefault(date, Collections.emptyList()).size();
            trend.add((long) count);
        }
        vo.setTrend(trend);
        return vo;
    }

    private AnalyticsOverviewItemVo item(String label, Object value, int ring) {
        AnalyticsOverviewItemVo o = new AnalyticsOverviewItemVo();
        o.setLabel(label);
        o.setValue(value);
        o.setRing(ring);
        o.setRingText(ring == 0 ? "环比 -" : (ring > 0 ? "环比 +" + ring + "%" : "环比 " + ring + "%"));
        return o;
    }

    private AnalyticsNameValueVo nameValue(String name, long value) {
        AnalyticsNameValueVo o = new AnalyticsNameValueVo();
        o.setName(name);
        o.setValue(value);
        return o;
    }

    private AnalyticsViewVo buildViewEmpty() {
        AnalyticsViewVo v = new AnalyticsViewVo();
        v.setOverview(Collections.emptyList());
        v.setTrend(Collections.emptyList());
        v.setSource(Collections.emptyList());
        v.setTimeSlot(Collections.emptyList());
        return v;
    }

    private AnalyticsTabVo buildTabEmpty() {
        AnalyticsTabVo t = new AnalyticsTabVo();
        t.setOverview(Collections.emptyList());
        t.setTrend(Collections.emptyList());
        return t;
    }

    private String resolveCreatorUid(String uid) {
        // 1. 前端显式传入 uid 时，优先使用
        if (ValidatorUtil.isNotNull(uid) && !uid.isEmpty()) {
            return uid;
        }

        // 2. 如果是演示环境且配置了演示账号用户名，尝试按用户名查找对应的用户ID
        try {
            DemoAccountConfigDTO demoConfig = systemConfigService.getDemoAccountConfig();
            if (demoConfig != null
                    && Boolean.TRUE.equals(demoConfig.getEnabled())
                    && demoConfig.getUsername() != null
                    && !demoConfig.getUsername().isEmpty()) {
                // 根据演示账号用户名查找用户ID
                WebUser user = userMapper.selectOne(
                        new QueryWrapper<WebUser>()
                                .lambda()
                                .eq(WebUser::getPhone, demoConfig.getUsername())
                );
                if (user != null && ValidatorUtil.isNotNull(user.getId())) {
                    return String.valueOf(user.getId());
                }
            }
        } catch (Exception e) {
            // 配置缺失或查询异常时，继续尝试用当前登录用户
            log.warn("解析演示账号配置失败，回退到当前登录用户: {}", e.getMessage());
        }

        // 3. 回退：使用当前登录用户ID
        try {
            return AuthContextHolder.getUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
