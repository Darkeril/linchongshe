package com.hongshu.web.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hongshu.web.domain.HongshuBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 群聊信息
 *
 * @author: hongshu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("web_chat_group")
public class WebChatGroup extends HongshuBaseEntity {

    /**
     * 群聊名称
     */
    private String groupName;

    /**
     * 群聊头像
     */
    private String groupAvatar;

    /**
     * 群聊简介
     */
    private String groupDescription;

    /**
     * 群主用户ID
     */
    private String ownerId;

    /**
     * 群成员数量
     */
    private Integer memberCount;

    /**
     * 最大成员数量
     */
    private Integer maxMemberCount;

    /**
     * 群状态（0：正常 1：已解散）
     */
    private Integer status;
}


