package com.hongshu.web.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 群聊信息VO
 *
 * @author: hongshu
 */
@Data
public class ChatGroupVo implements Serializable {

    /**
     * 群聊ID
     */
    private String id;

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
     * 群主用户名
     */
    private String ownerName;

    /**
     * 群主头像
     */
    private String ownerAvatar;

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

    /**
     * 最后一条消息内容
     */
    private String lastMessage;

    /**
     * 最后一条消息发送者用户ID（用于判断是否为自己发送，自己发则只显示消息）
     */
    private String lastMessageSenderId;

    /**
     * 最后一条消息发送者昵称（他人发送时展示「发送者：消息」）
     */
    private String lastMessageSenderName;

    /**
     * 最后一条消息时间
     */
    private Long lastMessageTime;

    /**
     * 未读消息数量
     */
    private Integer unreadCount;

    /**
     * 创建时间
     */
    private Date createTime;
}

