package com.hongshu.common.core.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class Message implements Serializable {

    private static final long serialVersionUID = 2405172041950251807L;

    private String sendUid;

    private String acceptUid;

    private Object content;

    private Integer msgType;

    private Integer chatType;

    private String productId;

    /**
     * 群聊ID（当chatType=1时使用）
     */
    private String groupChatId;
    
    /**
     * 发送者名称（群聊时需要）
     */
    private String senderName;
    
    /**
     * 发送者头像（群聊时需要）
     */
    private String senderAvatar;

    /**
     * 语音时长（单位：秒）
     */
    private Integer audioTime;

    /**
     * 系统通知等场景下的配图地址（可选）
     */
    private String imageUrl;
}
