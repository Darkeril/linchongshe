package com.hongshu.web.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 群成员信息VO
 *
 * @author: hongshu
 */
@Data
public class ChatGroupMemberVo implements Serializable {

    /**
     * 成员ID
     */
    private String id;

    /**
     * 群聊ID
     */
    private String groupChatId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 成员角色（0：群主 1：管理员 2：普通成员）
     */
    private Integer role;

    /**
     * 群昵称
     */
    private String groupNickname;

    /**
     * 未读消息数量
     */
    private Integer unreadCount;

    /**
     * 是否免打扰（0：否 1：是）
     */
    private Integer mute;

    /**
     * 加入时间
     */
    private Date joinTime;
}


