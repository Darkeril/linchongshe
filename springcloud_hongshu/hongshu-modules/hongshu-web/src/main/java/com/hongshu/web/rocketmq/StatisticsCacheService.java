package com.hongshu.web.rocketmq;

import com.hongshu.common.redis.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 统计缓存服务
 * 使用Redis缓存统计数据，提升性能
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class StatisticsCacheService {

    @Autowired
    private RedisService redisService;

    // Redis键前缀
    private static final String BASIC_STATS_KEY = "statistics:basic";
    private static final String CATEGORY_STATS_PREFIX = "statistics:category:";
    private static final String WEEK_VISIT_KEY = "statistics:week:visit";
    /** 每日新增趋势缓存：按天数分键，避免 7 日与 30 日混用 */
    private static final String DAILY_NEW_DATA_KEY_PREFIX = "statistics:daily:new";
    private static final String HOT_NOTE_PREFIX = "statistics:hot:note:";

    // 计数器键
    private static final String USER_COUNT_KEY = "statistics:count:user";
    private static final String NOTE_COUNT_KEY = "statistics:count:note";
    private static final String COMMENT_COUNT_KEY = "statistics:count:comment";
    private static final String PRODUCT_COUNT_KEY = "statistics:count:product";
    private static final String VISIT_COUNT_KEY = "statistics:count:visit";

    // 缓存过期时间
    private static final long BASIC_STATS_EXPIRE = 300L; // 5分钟
    private static final long CATEGORY_STATS_EXPIRE = 600L; // 10分钟
    private static final long DAILY_STATS_EXPIRE = 300L; // 5分钟
    private static final long HOT_NOTE_EXPIRE = 600L; // 10分钟

    /**
     * 缓存基础统计数据
     *
     * @param statistics 统计数据
     */
    public void cacheBasicStatistics(Map<String, Object> statistics) {
        redisService.setCacheObject(BASIC_STATS_KEY, statistics, BASIC_STATS_EXPIRE, TimeUnit.SECONDS);
        log.debug("缓存基础统计数据: {}", statistics);
    }

    /**
     * 获取基础统计数据
     *
     * @return 统计数据
     */
    public Map<String, Object> getBasicStatistics() {
        Map<String, Object> data = redisService.getCacheObject(BASIC_STATS_KEY);
        return data != null ? data : new HashMap<>();
    }

    /**
     * 缓存分类统计数据
     *
     * @param type 类型：note-笔记，product-商品
     * @param data 统计数据
     */
    public void cacheCategoryStatistics(String type, List<Map<String, Object>> data) {
        String key = CATEGORY_STATS_PREFIX + type;
        redisService.setCacheObject(key, data, CATEGORY_STATS_EXPIRE, TimeUnit.SECONDS);
        log.debug("缓存分类统计数据: type={}, size={}", type, data.size());
    }

    /**
     * 获取分类统计数据
     *
     * @param type 类型：note-笔记，product-商品
     * @return 统计数据
     */
    public List<Map<String, Object>> getCategoryStatistics(String type) {
        String key = CATEGORY_STATS_PREFIX + type;
        List<Map<String, Object>> data = redisService.getCacheObject(key);
        return data != null ? data : new java.util.ArrayList<>();
    }

    /**
     * 缓存周访问统计
     *
     * @param data 访问统计数据
     */
    public void cacheWeekVisit(Map<String, Object> data) {
        redisService.setCacheObject(WEEK_VISIT_KEY, data, DAILY_STATS_EXPIRE, TimeUnit.SECONDS);
        log.debug("缓存周访问统计: {}", data);
    }

    /**
     * 获取周访问统计
     *
     * @return 访问统计数据
     */
    public Map<String, Object> getWeekVisit() {
        Map<String, Object> data = redisService.getCacheObject(WEEK_VISIT_KEY);
        return data != null ? data : new HashMap<>();
    }

    private String dailyNewDataRedisKey(int days) {
        return DAILY_NEW_DATA_KEY_PREFIX + ":" + days;
    }

    /**
     * 缓存每日新增数据（按统计窗口天数）
     */
    public void cacheDailyNewData(int days, Object data) {
        redisService.setCacheObject(dailyNewDataRedisKey(days), data, DAILY_STATS_EXPIRE, TimeUnit.SECONDS);
        log.debug("缓存每日新增数据: days={}", days);
    }

    /**
     * 获取每日新增数据缓存
     */
    public <T> T getDailyNewData(Class<T> clazz, int days) {
        return redisService.getCacheObject(dailyNewDataRedisKey(days));
    }

    /**
     * 缓存热门笔记
     *
     * @param noteType 笔记类型
     * @param notes    热门笔记列表
     */
    public void cacheHotNotes(String noteType, List<?> notes) {
        String key = HOT_NOTE_PREFIX + noteType;
        redisService.setCacheObject(key, notes, HOT_NOTE_EXPIRE, TimeUnit.SECONDS);
        log.debug("缓存热门笔记: type={}, size={}", noteType, notes.size());
    }

    /**
     * 获取热门笔记
     *
     * @param noteType 笔记类型
     * @return 热门笔记列表
     */
    public <T> List<T> getHotNotes(String noteType) {
        String key = HOT_NOTE_PREFIX + noteType;
        List<T> notes = redisService.getCacheObject(key);
        return notes != null ? notes : new java.util.ArrayList<>();
    }

    // ==================== 计数器相关方法 ====================

    /**
     * 递增用户数
     *
     * @return 递增后的数量
     */
    public Long incrementUserCount() {
        return redisService.increment(USER_COUNT_KEY);
    }

    /**
     * 递减用户数
     *
     * @return 递减后的数量
     */
    public Long decrementUserCount() {
        return redisService.decrement(USER_COUNT_KEY);
    }

    /**
     * 递增笔记数
     *
     * @return 递增后的数量
     */
    public Long incrementNoteCount() {
        return redisService.increment(NOTE_COUNT_KEY);
    }

    /**
     * 递减笔记数
     *
     * @return 递减后的数量
     */
    public Long decrementNoteCount() {
        return redisService.decrement(NOTE_COUNT_KEY);
    }

    /**
     * 递增评论数
     *
     * @return 递增后的数量
     */
    public Long incrementCommentCount() {
        return redisService.increment(COMMENT_COUNT_KEY);
    }

    /**
     * 递减评论数
     *
     * @return 递减后的数量
     */
    public Long decrementCommentCount() {
        return redisService.decrement(COMMENT_COUNT_KEY);
    }

    /**
     * 递增商品数
     *
     * @return 递增后的数量
     */
    public Long incrementProductCount() {
        return redisService.increment(PRODUCT_COUNT_KEY);
    }

    /**
     * 递减商品数
     *
     * @return 递减后的数量
     */
    public Long decrementProductCount() {
        return redisService.decrement(PRODUCT_COUNT_KEY);
    }

    /**
     * 递增访问数
     *
     * @return 递增后的数量
     */
    public Long incrementVisitCount() {
        return redisService.increment(VISIT_COUNT_KEY);
    }

    /**
     * 获取用户数
     *
     * @return 用户数
     */
    public Long getUserCount() {
        Long count = redisService.getCacheObject(USER_COUNT_KEY);
        return count != null ? count : 0L;
    }

    /**
     * 设置用户数
     *
     * @param count 用户数
     */
    public void setUserCount(Long count) {
        redisService.setCacheObject(USER_COUNT_KEY, count);
    }

    /**
     * 获取笔记数
     *
     * @return 笔记数
     */
    public Long getNoteCount() {
        Long count = redisService.getCacheObject(NOTE_COUNT_KEY);
        return count != null ? count : 0L;
    }

    /**
     * 设置笔记数
     *
     * @param count 笔记数
     */
    public void setNoteCount(Long count) {
        redisService.setCacheObject(NOTE_COUNT_KEY, count);
    }

    /**
     * 获取评论数
     *
     * @return 评论数
     */
    public Long getCommentCount() {
        Long count = redisService.getCacheObject(COMMENT_COUNT_KEY);
        return count != null ? count : 0L;
    }

    /**
     * 设置评论数
     *
     * @param count 评论数
     */
    public void setCommentCount(Long count) {
        redisService.setCacheObject(COMMENT_COUNT_KEY, count);
    }

    /**
     * 获取商品数
     *
     * @return 商品数
     */
    public Long getProductCount() {
        Long count = redisService.getCacheObject(PRODUCT_COUNT_KEY);
        return count != null ? count : 0L;
    }

    /**
     * 设置商品数
     *
     * @param count 商品数
     */
    public void setProductCount(Long count) {
        redisService.setCacheObject(PRODUCT_COUNT_KEY, count);
    }

    /**
     * 获取访问数
     *
     * @return 访问数
     */
    public Long getVisitCount() {
        Long count = redisService.getCacheObject(VISIT_COUNT_KEY);
        return count != null ? count : 0L;
    }

    /**
     * 设置访问数
     *
     * @param count 访问数
     */
    public void setVisitCount(Long count) {
        redisService.setCacheObject(VISIT_COUNT_KEY, count);
    }

    /**
     * 清除所有统计缓存
     */
    public void clearAllStatistics() {
        redisService.deleteObject(BASIC_STATS_KEY);
        redisService.deleteObject(WEEK_VISIT_KEY);
        redisService.deleteObject(DAILY_NEW_DATA_KEY_PREFIX);
        redisService.deleteObject(dailyNewDataRedisKey(7));
        redisService.deleteObject(dailyNewDataRedisKey(30));

        // 清除分类统计
        redisService.deleteObject(CATEGORY_STATS_PREFIX + "note");
        redisService.deleteObject(CATEGORY_STATS_PREFIX + "product");
        // 工作台「用户地理分布」整表聚合快照（与 WebUserServiceImpl 中 key 一致）
        redisService.deleteObject("dashboard:userLocations:snapshot");

        log.info("清除所有统计缓存");
    }

    /**
     * 刷新基础统计数据
     */
    public void refreshBasicStatistics() {
        redisService.deleteObject(BASIC_STATS_KEY);
        log.info("刷新基础统计数据缓存");
    }

    /**
     * 刷新每日新增数据缓存，使下次请求重新查询实时数据
     */
    public void refreshDailyNewData() {
        redisService.deleteObject(dailyNewDataRedisKey(7));
        redisService.deleteObject(dailyNewDataRedisKey(30));
        log.debug("刷新每日新增数据缓存");
    }
}

