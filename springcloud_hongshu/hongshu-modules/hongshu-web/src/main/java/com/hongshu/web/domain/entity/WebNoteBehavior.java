package com.hongshu.web.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hongshu.web.domain.HongshuBaseEntity;
import lombok.Data;

/**
 * 笔记行为记录（曝光、观看来源、观看时段、时长/完播）
 * 用于数据中心-数据分析的曝光数、观看来源、24h 时段、人均时长、完播率
 */
@Data
@TableName("web_note_behavior")
public class WebNoteBehavior extends HongshuBaseEntity {

    /**
     * 事件类型：exposure-曝光 view-观看 watch-观看时长/完播
     */
    private String eventType;
    /**
     * 笔记ID
     */
    private String nid;
    /**
     * 用户ID（谁产生的行为）
     */
    private String uid;
    /**
     * 来源场景：search-搜索 recommend-推荐 follow-关注流 profile-个人页 other
     */
    private String scene;
    /**
     * 观看时长（秒），仅 watch 时有值
     */
    private Integer durationSec;
    /**
     * 是否完播：0-否 1-是，仅 watch 时有效
     */
    private Integer completed;
}
