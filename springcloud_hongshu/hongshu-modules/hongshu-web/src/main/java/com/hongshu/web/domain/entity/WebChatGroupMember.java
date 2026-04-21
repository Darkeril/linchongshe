package com.hongshu.web.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 群成员
 *
 * @author: hongshu
 */
@Data
@TableName("web_chat_group_member")
public class WebChatGroupMember implements Serializable {

    /**
     * 主键ID
     */
    private String id;

    /**
     * 群聊ID
     */
    @TableField("group_chat_id")
    private String groupChatId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

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
    @TableField(fill = FieldFill.INSERT)
    private Date joinTime;

    /**
     * 创建者
     */
    @TableField(fill = FieldFill.INSERT)
    private String creator;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}

