package com.hongshu.web.task;

import com.hongshu.web.rocketmq.StatisticsCacheService;
import com.hongshu.web.rocketmq.StatisticsServiceV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 统计数据定时同步任务
 * 定期刷新统计数据缓存
 *
 * @author: hongshu
 */
@Slf4j
@Component
public class StatisticsSyncTask {

    @Autowired
    private StatisticsCacheService cacheService;

    @Autowired
    private StatisticsServiceV2 statisticsServiceV2;

    /**
     * 定时刷新基础统计数据
     * 每10分钟执行一次
     */
    @Scheduled(fixedDelay = 600000) // 10分钟 = 600000毫秒
    public void refreshBasicStatistics() {
        try {
            log.info("开始刷新基础统计数据...");

            // 清除缓存，下次访问时会重新加载
            cacheService.refreshBasicStatistics();

            log.info("基础统计数据缓存已刷新");
        } catch (Exception e) {
            log.error("刷新基础统计数据失败", e);
        }
    }

    /**
     * 定时刷新分类统计数据
     * 每15分钟执行一次
     */
    @Scheduled(fixedDelay = 900000) // 15分钟 = 900000毫秒
    public void refreshCategoryStatistics() {
        try {
            log.info("开始刷新分类统计数据...");

            // 清除并重新加载笔记分类统计
            statisticsServiceV2.getCategoryStatistics("note");

            // 清除并重新加载商品分类统计
            statisticsServiceV2.getCategoryStatistics("product");

            log.info("分类统计数据已刷新");
        } catch (Exception e) {
            log.error("刷新分类统计数据失败", e);
        }
    }

    /**
     * 定时刷新每日新增数据
     * 每30分钟执行一次
     */
    @Scheduled(fixedDelay = 1800000) // 30分钟 = 1800000毫秒
    public void refreshDailyNewData() {
        try {
            log.info("开始刷新每日新增数据...");

            // 重新加载每日新增数据
            statisticsServiceV2.getDailyNewData();

            log.info("每日新增数据已刷新");
        } catch (Exception e) {
            log.error("刷新每日新增数据失败", e);
        }
    }

    /**
     * 定时刷新热门笔记
     * 每20分钟执行一次
     */
    @Scheduled(fixedDelay = 1200000) // 20分钟 = 1200000毫秒
    public void refreshHotNotes() {
        try {
            log.info("开始刷新热门笔记...");

            // 刷新不同类型的热门笔记
            String[] noteTypes = {"", "video", "图文"};

            for (String noteType : noteTypes) {
                try {
                    statisticsServiceV2.getHotNotes(noteType);
                } catch (Exception e) {
                    log.error("刷新热门笔记失败: type={}", noteType, e);
                }
            }

            log.info("热门笔记已刷新");
        } catch (Exception e) {
            log.error("刷新热门笔记失败", e);
        }
    }

    /**
     * 定时清理过期的统计缓存
     * 每天凌晨5点执行
     */
    @Scheduled(cron = "0 0 5 * * ?")
    public void cleanExpiredStatisticsCache() {
        try {
            log.info("开始清理过期的统计缓存...");

            // Redis的过期键会自动删除，这里只是记录日志
            log.info("统计缓存清理完成");
        } catch (Exception e) {
            log.error("清理统计缓存失败", e);
        }
    }

    /**
     * 定时预热热点数据
     * 每天早上8点执行
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void preloadHotData() {
        try {
            log.info("开始预热热点统计数据...");

            // 预加载基础统计数据
            statisticsServiceV2.getBasicStatistics();

            // 预加载分类统计数据
            statisticsServiceV2.getCategoryStatistics("note");
            statisticsServiceV2.getCategoryStatistics("product");

            // 预加载周访问统计
            statisticsServiceV2.getWeekVisit();

            // 预加载每日新增数据
            statisticsServiceV2.getDailyNewData();

            log.info("热点统计数据预热完成");
        } catch (Exception e) {
            log.error("预热统计数据失败", e);
        }
    }
}


