package com.hongshu.web.mapper.web;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hongshu.web.domain.entity.WebNoteBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 笔记行为记录 Mapper
 */
@Mapper
public interface WebNoteBehaviorMapper extends BaseMapper<WebNoteBehavior> {

    /**
     * 统计某创作者在时间范围内的曝光数（按 nid 去重）
     */
    Long countExposureByCreatorAndTimeRange(@Param("creatorUid") String creatorUid,
                                            @Param("startTime") Date startTime,
                                            @Param("endTime") Date endTime);

    /**
     * 统计某创作者在时间范围内的观看数（view 事件条数）
     */
    Long countViewByCreatorAndTimeRange(@Param("creatorUid") String creatorUid,
                                        @Param("startTime") Date startTime,
                                        @Param("endTime") Date endTime);

    /**
     * 按来源统计观看数（创作者+时间范围）
     */
    List<Map<String, Object>> countViewByScene(@Param("creatorUid") String creatorUid,
                                               @Param("startTime") Date startTime,
                                               @Param("endTime") Date endTime);

    /**
     * 按小时统计观看数（0-23，创作者+时间范围）
     */
    List<Map<String, Object>> countViewByHour(@Param("creatorUid") String creatorUid,
                                              @Param("startTime") Date startTime,
                                              @Param("endTime") Date endTime);

    /**
     * 观看时长与完播：总时长(秒)、次数、完播次数
     */
    Map<String, Object> sumDurationAndCompletion(@Param("creatorUid") String creatorUid,
                                                 @Param("startTime") Date startTime,
                                                 @Param("endTime") Date endTime);

    /**
     * 按自然日统计观看数（用于趋势图），返回 date(yyyy-MM-dd)、cnt
     */
    List<Map<String, Object>> countViewByDay(@Param("creatorUid") String creatorUid,
                                             @Param("startTime") Date startTime,
                                             @Param("endTime") Date endTime);

    /**
     * 统计某创作者在时间范围内某类行为总数（如 share）
     */
    Long countEventByCreatorAndTimeRange(@Param("creatorUid") String creatorUid,
                                         @Param("eventType") String eventType,
                                         @Param("startTime") Date startTime,
                                         @Param("endTime") Date endTime);

    /**
     * 按自然日统计某类行为（创作者维度）
     */
    List<Map<String, Object>> countEventByDay(@Param("creatorUid") String creatorUid,
                                              @Param("eventType") String eventType,
                                              @Param("startTime") Date startTime,
                                              @Param("endTime") Date endTime);

    // ========== 按单条笔记 nid 统计（笔记数据详情页） ==========

    Long countExposureByNidAndTimeRange(@Param("nid") String nid,
                                        @Param("startTime") Date startTime,
                                        @Param("endTime") Date endTime);

    Long countViewByNidAndTimeRange(@Param("nid") String nid,
                                    @Param("startTime") Date startTime,
                                    @Param("endTime") Date endTime);

    List<Map<String, Object>> countViewBySceneByNid(@Param("nid") String nid,
                                                     @Param("startTime") Date startTime,
                                                     @Param("endTime") Date endTime);

    List<Map<String, Object>> countViewByHourByNid(@Param("nid") String nid,
                                                    @Param("startTime") Date startTime,
                                                    @Param("endTime") Date endTime);

    Map<String, Object> sumDurationAndCompletionByNid(@Param("nid") String nid,
                                                       @Param("startTime") Date startTime,
                                                       @Param("endTime") Date endTime);

    Long countShareByNidAndTimeRange(@Param("nid") String nid,
                                     @Param("startTime") Date startTime,
                                     @Param("endTime") Date endTime);

    Long countWatchExitWithinSecByNid(@Param("nid") String nid,
                                      @Param("sec") Integer sec,
                                      @Param("startTime") Date startTime,
                                      @Param("endTime") Date endTime);

    /**
     * 批量按笔记维度聚合行为特征（用于推荐重排）
     * 返回字段：
     * - nid
     * - exposure（曝光次数）
     * - views（观看次数）
     * - totalDurationSec（总观看时长，秒）
     * - watchCount（watch 次数）
     * - completedCount（完播次数）
     */
    List<Map<String, Object>> aggregateNoteFeaturesByNids(@Param("nids") List<String> nids,
                                                          @Param("startTime") Date startTime,
                                                          @Param("endTime") Date endTime);

    List<Map<String, Object>> countViewByDayByNid(@Param("nid") String nid,
                                                   @Param("startTime") Date startTime,
                                                   @Param("endTime") Date endTime);

    /** 按自然日统计曝光数（单条笔记，用于曝光趋势） */
    List<Map<String, Object>> countExposureByDayByNid(@Param("nid") String nid,
                                                       @Param("startTime") Date startTime,
                                                       @Param("endTime") Date endTime);
}
