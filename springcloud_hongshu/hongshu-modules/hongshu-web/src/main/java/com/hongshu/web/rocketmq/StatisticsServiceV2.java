package com.hongshu.web.rocketmq;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.enums.AuditStatusEnum;
import com.hongshu.common.core.enums.EStatus;
import com.hongshu.system.api.RemoteChatService;
import com.hongshu.system.api.RemoteIdleService;
import com.hongshu.web.domain.entity.DailyStatistics;
import com.hongshu.web.domain.vo.DailyNewDataVO;
import com.hongshu.web.domain.vo.NoteInteractionEfficiencyVO;
import com.hongshu.web.domain.vo.WorkbenchSidebarSummaryVO;
import com.hongshu.web.mapper.idle.IdleProductMapper;
import com.hongshu.web.mapper.idle.IdleProductOrderMapper;
import com.hongshu.web.mapper.web.WebNoteMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.service.sys.ISysCommentService;
import com.hongshu.web.service.sys.ISysMemberService;
import com.hongshu.web.service.sys.ISysNoteService;
import com.hongshu.web.service.sys.ISysVisitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 统计服务V2（优化版）
 * 使用Redis缓存 + RocketMQ异步更新
 * 注意：直接调用底层服务，避免循环依赖
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class StatisticsServiceV2 {

    @Autowired
    private StatisticsCacheService cacheService;

    @Autowired(required = false)
    private StatisticsProducerService producerService;

    // 直接注入底层服务，避免循环依赖
    @Autowired
    private ISysNoteService noteService;

    @Autowired
    private ISysCommentService commentService;

    @Autowired
    private ISysVisitService visitService;

    @Autowired
    private ISysMemberService memberService;

    @Autowired
    private RemoteIdleService remoteIdleService;

    @Autowired
    private RemoteChatService remoteChatService;

    @Autowired
    private WebUserMapper userMapper;

    @Autowired
    private WebNoteMapper webNoteMapper;

    @Autowired
    private IdleProductMapper idleProductMapper;

    @Autowired
    private IdleProductOrderMapper idleProductOrderMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取基础统计数据（优化版）
     *
     * @return 统计数据
     */
    public Map<String, Object> getBasicStatistics() {
        try {
            // 1. 先从Redis获取
            Map<String, Object> cachedStats = cacheService.getBasicStatistics();

            if (!cachedStats.isEmpty()) {
                log.debug("基础统计数据缓存命中");
                return cachedStats;
            }

            // 2. Redis未命中，直接查询底层服务（避免循环依赖）
            Map<String, Object> statistics = queryBasicStatisticsFromDatabase();

            // 3. 缓存到Redis
            cacheService.cacheBasicStatistics(statistics);

            log.debug("基础统计数据从数据库加载并缓存");
            return statistics;
        } catch (Exception e) {
            log.error("获取基础统计数据失败", e);
            // 降级：直接查询数据库
            return queryBasicStatisticsFromDatabase();
        }
    }

    /**
     * 从数据库查询基础统计数据
     *
     * @return 统计数据
     */
    private Map<String, Object> queryBasicStatisticsFromDatabase() {
        Map<String, Object> map = new HashMap<>();
        map.put("visitCount", visitService.getWebVisitCount());
        map.put("userCount", memberService.getMemberCount(0));
        map.put("blogCount", noteService.getNoteCount(EStatus.ENABLE));
        map.put("commentCount", commentService.getCommentCount(EStatus.ENABLE));
        map.put("productCount", remoteIdleService.getProductCount().getData());
        map.put("orderCount", idleProductOrderMapper.countTotal());
        map.put("gptModelCount", remoteChatService.getGptModelCount().getData());
        return map;
    }

    /**
     * 获取分类统计数据（优化版）
     *
     * @param type 类型：note-笔记，product-商品
     * @return 统计数据
     */
    public List<Map<String, Object>> getCategoryStatistics(String type) {
        try {
            // 1. 先从Redis获取
            List<Map<String, Object>> cachedData = cacheService.getCategoryStatistics(type);

            if (!cachedData.isEmpty()) {
                log.debug("分类统计数据缓存命中: type={}", type);
                return cachedData;
            }

            // 2. Redis未命中，直接查询底层服务
            List<Map<String, Object>> data;
            if ("note".equals(type)) {
                data = noteService.getNoteCountByCategory();
            } else if ("product".equals(type)) {
                R<?> r = remoteIdleService.getProductCountByCategory();
                Object obj = r.getData();
                if (obj == null) {
                    data = Collections.emptyList();
                } else {
                    data = objectMapper.convertValue(obj, new TypeReference<List<Map<String, Object>>>() {});
                }
            } else {
                return new java.util.ArrayList<>();
            }

            // 3. 缓存到Redis
            if (!data.isEmpty()) {
                cacheService.cacheCategoryStatistics(type, data);
            }

            log.debug("分类统计数据从数据库加载并缓存: type={}", type);
            return data;
        } catch (Exception e) {
            log.error("获取分类统计数据失败: type={}", type, e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 获取周访问统计（优化版）
     *
     * @return 访问统计数据
     */
    public Map<String, Object> getWeekVisit() {
        try {
            // 1. 先从Redis获取
            Map<String, Object> cachedData = cacheService.getWeekVisit();

            if (!cachedData.isEmpty()) {
                log.debug("周访问统计缓存命中");
                return cachedData;
            }

            // 2. Redis未命中，直接查询底层服务
            Map<String, Object> data = visitService.getVisitByWeek();

            // 3. 缓存到Redis
            cacheService.cacheWeekVisit(data);

            log.debug("周访问统计从数据库加载并缓存");
            return data;
        } catch (Exception e) {
            log.error("获取周访问统计失败", e);
            return new java.util.HashMap<>();
        }
    }

    /**
     * 获取每日新增数据（优化版），默认近 7 个自然日（含今天）
     */
    public DailyNewDataVO getDailyNewData() {
        return getDailyNewData(7);
    }

    /**
     * 获取每日新增数据（近 {@code days} 个自然日含今天，仅支持 7 或 30）
     */
    public DailyNewDataVO getDailyNewData(int days) {
        if (days != 7 && days != 30) {
            days = 7;
        }
        int cacheDays = days;
        try {
            DailyNewDataVO cachedData = cacheService.getDailyNewData(DailyNewDataVO.class, cacheDays);

            if (cachedData != null) {
                log.debug("每日新增数据缓存命中: days={}", cacheDays);
                return cachedData;
            }

            DailyNewDataVO data = queryDailyNewDataFromDatabase(cacheDays);

            cacheService.cacheDailyNewData(cacheDays, data);

            log.debug("每日新增数据从数据库加载并缓存: days={}", cacheDays);
            return data;
        } catch (Exception e) {
            log.error("获取每日新增数据失败", e);
            return null;
        }
    }

    /**
     * 从数据库查询每日新增数据；SQL 区间为 [startDay, endDay) 形式，故结束日传 endDay+1 以包含今天。
     */
    private DailyNewDataVO queryDailyNewDataFromDatabase(int days) {
        LocalDate endDay = LocalDate.now();
        LocalDate startDay = endDay.minusDays(days - 1L);

        try {
            int todayNewUsers = userMapper.getTodayNewUserCount();
            int todayNewOrders = (int) idleProductOrderMapper.countCreatedToday();
            int todayNewNotes = (int) webNoteMapper.countCreatedToday();
            int todayNewProducts = (int) idleProductMapper.countCreatedToday();
            List<DailyStatistics> statistics = userMapper.getStatisticsByDateRange(startDay, endDay.plusDays(1));
            return buildDailyNewDataVO(todayNewUsers, todayNewOrders, todayNewNotes, todayNewProducts, startDay, endDay, statistics);
        } catch (Exception e) {
            log.error("查询每日新增数据失败", e);
            throw new RuntimeException("获取统计数据失败", e);
        }
    }

    /**
     * 按自然日连续输出，缺日补 0；与前端滚动横轴一一对应。
     */
    private DailyNewDataVO buildDailyNewDataVO(int todayNewUsers, int todayNewOrders,
                                               int todayNewNotes, int todayNewProducts,
                                               LocalDate startDay, LocalDate endDay,
                                               List<DailyStatistics> statistics) {
        Map<LocalDate, DailyStatistics> byDay = new HashMap<>();
        if (statistics != null) {
            for (DailyStatistics stat : statistics) {
                if (stat != null && stat.getDate() != null) {
                    byDay.put(stat.getDate().toLocalDate(), stat);
                }
            }
        }

        List<String> dates = new ArrayList<>();
        List<Integer> userCounts = new ArrayList<>();
        List<Integer> noteCounts = new ArrayList<>();
        List<Integer> productCounts = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (LocalDate d = startDay; !d.isAfter(endDay); d = d.plusDays(1)) {
            dates.add(formatter.format(d));
            DailyStatistics stat = byDay.get(d);
            userCounts.add(stat != null && stat.getUserCount() != null ? stat.getUserCount() : 0);
            noteCounts.add(stat != null && stat.getNoteCount() != null ? stat.getNoteCount() : 0);
            productCounts.add(stat != null && stat.getProductCount() != null ? stat.getProductCount() : 0);
        }

        return DailyNewDataVO.builder()
                .todayNewUsers(todayNewUsers)
                .todayNewNotes(todayNewNotes)
                .todayNewProducts(todayNewProducts)
                .todayNewOrders(todayNewOrders)
                .dates(dates)
                .users(userCounts)
                .notes(noteCounts)
                .products(productCounts)
                .build();
    }

    /**
     * 工作台-笔记互动效率（按统计窗口内「新创建且审核通过」的笔记聚合点赞/评论/浏览）
     *
     * @param days 仅支持 7 或 30
     */
    public NoteInteractionEfficiencyVO getNoteInteractionEfficiency(int days) {
        if (days != 7 && days != 30) {
            days = 7;
        }
        String audit = AuditStatusEnum.PASS.getCode();
        ZoneId z = ZoneId.systemDefault();
        LocalDate endDay = LocalDate.now();
        LocalDate startDay = endDay.minusDays(days - 1L);

        java.util.Date startTime = java.util.Date.from(startDay.atStartOfDay(z).toInstant());
        java.util.Date endTime = java.util.Date.from(endDay.plusDays(1).atStartOfDay(z).toInstant());

        LocalDate prevEnd = startDay.minusDays(1);
        LocalDate prevStart = prevEnd.minusDays(days - 1L);
        java.util.Date prevStartTime = java.util.Date.from(prevStart.atStartOfDay(z).toInstant());
        java.util.Date prevEndTime = java.util.Date.from(prevEnd.plusDays(1).atStartOfDay(z).toInstant());

        Map<String, Object> cur = webNoteMapper.sumInteractionInRange(startTime, endTime, audit);
        Map<String, Object> prev = webNoteMapper.sumInteractionInRange(prevStartTime, prevEndTime, audit);
        if (cur == null) {
            cur = Collections.emptyMap();
        }
        if (prev == null) {
            prev = Collections.emptyMap();
        }

        long likes = toLong(cur.get("likes"));
        long comments = toLong(cur.get("comments"));
        long collects = toLong(cur.get("collects"));
        long views = toLong(cur.get("views"));
        long noteCount = toLong(cur.get("noteCount"));

        long pLikes = toLong(prev.get("likes"));
        long pComments = toLong(prev.get("comments"));
        long pCollects = toLong(prev.get("collects"));
        long pViews = toLong(prev.get("views"));

        double viewsDenom = Math.max(views, 1L);
        double rate = (likes + comments) * 100.0 / viewsDenom;
        double pDenom = Math.max(pViews, 1L);
        double pRate = (pLikes + pComments) * 100.0 / pDenom;

        NoteInteractionEfficiencyVO.Summary summary = new NoteInteractionEfficiencyVO.Summary();
        summary.setLikeCount(likes);
        summary.setCommentCount(comments);
        summary.setCollectionCount(collects);
        summary.setInteractionRatePct(Math.round(rate * 100.0) / 100.0);
        summary.setLikeMomPct(momPctLong(likes, pLikes));
        summary.setCommentMomPct(momPctLong(comments, pComments));
        summary.setCollectionMomPct(momPctLong(collects, pCollects));
        summary.setRateMomPct(momPctDouble(rate, pRate));

        List<Map<String, Object>> daily = webNoteMapper.sumInteractionGroupByDay(startTime, endTime, audit);
        Map<LocalDate, long[]> byDay = new HashMap<>();
        if (daily != null) {
            for (Map<String, Object> row : daily) {
                LocalDate ld = toLocalDate(row.get("d"));
                if (ld == null) {
                    continue;
                }
                byDay.put(ld, new long[]{
                        toLong(row.get("likes")),
                        toLong(row.get("comments")),
                        toLong(row.get("views"))
                });
            }
        }

        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("MM-dd");
        List<String> labels = new ArrayList<>();
        List<Double> rates = new ArrayList<>();
        for (LocalDate d = startDay; !d.isAfter(endDay); d = d.plusDays(1)) {
            labels.add(d.format(dayFmt));
            long[] v = byDay.getOrDefault(d, new long[3]);
            double dr = (v[0] + v[1]) * 100.0 / Math.max(v[2], 1L);
            rates.add(Math.round(dr * 100.0) / 100.0);
        }

        List<Map<String, Object>> topRows = webNoteMapper.topCategoryPublishInRange(startTime, endTime, audit, 3);
        List<NoteInteractionEfficiencyVO.TopTypeItem> topTypes = new ArrayList<>();
        if (topRows != null) {
            for (Map<String, Object> row : topRows) {
                NoteInteractionEfficiencyVO.TopTypeItem t = new NoteInteractionEfficiencyVO.TopTypeItem();
                Object nameObj = row.get("name");
                t.setName(nameObj != null ? String.valueOf(nameObj) : "未分类");
                t.setCount(toLong(row.get("cnt")));
                double pct = noteCount <= 0 ? 0 : t.getCount() * 100.0 / noteCount;
                t.setPercent(Math.round(pct * 10.0) / 10.0);
                topTypes.add(t);
            }
        }

        return NoteInteractionEfficiencyVO.builder()
                .summary(summary)
                .trendLabels(labels)
                .trendRates(rates)
                .topTypes(topTypes)
                .build();
    }

    private static long toLong(Object v) {
        if (v == null) {
            return 0L;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return 0L;
        }
    }

    private static LocalDate toLocalDate(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof java.sql.Date) {
            return ((java.sql.Date) o).toLocalDate();
        }
        if (o instanceof java.util.Date) {
            return ((java.util.Date) o).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }

    private static Double momPctLong(long cur, long prev) {
        if (prev == 0) {
            return null;
        }
        return Math.round((cur - prev) * 10000.0 / prev) / 100.0;
    }

    private static Double momPctDouble(double cur, double prev) {
        if (prev == 0) {
            return null;
        }
        return Math.round((cur - prev) * 10000.0 / prev) / 100.0;
    }

    /**
     * 获取热门笔记（优化版）
     *
     * @param noteType 笔记类型
     * @return 热门笔记列表
     */
    public List<NoteSearchVo> getHotNotes(String noteType) {
        try {
            // 1. 先从Redis获取
            List<NoteSearchVo> cachedNotes = cacheService.getHotNotes(noteType);

            if (!cachedNotes.isEmpty()) {
                log.debug("热门笔记缓存命中: type={}", noteType);
                return cachedNotes;
            }

            // 2. Redis未命中，直接查询底层服务
            List<NoteSearchVo> notes = noteService.getHotNote(noteType);

            // 3. 缓存到Redis
            if (!notes.isEmpty()) {
                cacheService.cacheHotNotes(noteType, notes);
            }

            log.debug("热门笔记从数据库加载并缓存: type={}", noteType);
            return notes;
        } catch (Exception e) {
            log.error("获取热门笔记失败: type={}", noteType, e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 通知统计更新
     *
     * @param type   统计类型
     * @param action 操作类型
     */
    public void notifyStatisticsUpdate(String type, String action) {
        notifyStatisticsUpdate(type, action, null);
    }

    /**
     * 通知统计更新
     *
     * @param type   统计类型
     * @param action 操作类型
     * @param id     关联ID
     */
    public void notifyStatisticsUpdate(String type, String action, String id) {
        try {
            if (producerService != null) {
                producerService.sendStatisticsUpdateMessage(type, action, id);
            } else {
                // RocketMQ不可用，直接更新缓存
                updateCacheDirectly(type, action);
            }
        } catch (Exception e) {
            log.error("通知统计更新失败: type={}, action={}", type, action, e);
        }
    }

    /**
     * 直接更新缓存（RocketMQ不可用时）
     *
     * @param type   统计类型
     * @param action 操作类型
     */
    private void updateCacheDirectly(String type, String action) {
        switch (type) {
            case "user":
                if ("create".equals(action)) {
                    cacheService.incrementUserCount();
                } else if ("delete".equals(action)) {
                    cacheService.decrementUserCount();
                }
                break;

            case "note":
                if ("create".equals(action)) {
                    cacheService.incrementNoteCount();
                } else if ("delete".equals(action)) {
                    cacheService.decrementNoteCount();
                }
                break;

            case "comment":
                if ("create".equals(action)) {
                    cacheService.incrementCommentCount();
                } else if ("delete".equals(action)) {
                    cacheService.decrementCommentCount();
                }
                break;

            case "product":
                if ("create".equals(action)) {
                    cacheService.incrementProductCount();
                } else if ("delete".equals(action)) {
                    cacheService.decrementProductCount();
                }
                break;

            case "visit":
                if ("create".equals(action)) {
                    cacheService.incrementVisitCount();
                }
                break;
        }

        // 清除基础统计缓存和每日新增缓存，确保下次请求拿到实时数据
        cacheService.refreshBasicStatistics();
        cacheService.refreshDailyNewData();
    }

    /**
     * 工作台右侧：待审核聚合 + 今日核心数字（与登录地点并列展示）
     */
    public WorkbenchSidebarSummaryVO getWorkbenchSidebarSummary() {
        WorkbenchSidebarSummaryVO vo = new WorkbenchSidebarSummaryVO();
        String review = AuditStatusEnum.REVIEW.getCode();
        vo.setPendingNotes(webNoteMapper.countByAuditStatus(review));
        vo.setPendingProducts(idleProductMapper.countByAuditStatus(review));
        vo.setTodayRegistrations(userMapper.getTodayNewUserCount());
        vo.setTodayNotes(webNoteMapper.countCreatedToday());
        vo.setTodayNewProducts(idleProductMapper.countCreatedToday());
        vo.setTodayOrders(idleProductOrderMapper.countCreatedToday());
        return vo;
    }

    /**
     * 刷新所有统计缓存
     */
    public void refreshAllStatistics() {
        try {
            log.info("开始刷新所有统计缓存...");
            cacheService.clearAllStatistics();
            log.info("所有统计缓存已清除");
        } catch (Exception e) {
            log.error("刷新统计缓存失败", e);
        }
    }
}

