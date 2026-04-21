package com.hongshu.web.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 聊天消息VO（包含发送者信息）
 *
 * @author: hongshu
 */
@Data
public class ChatMessageVo implements Serializable {
    private String id;
    private String productId;
    private String sendUid;
    private String acceptUid;
    private String content;
    private Integer chatType;
    private String groupChatId;
    private Integer msgType;
    private Long timestamp;
    private Integer audioTime;
    
    // 发送者信息（群聊时需要）
    private String senderName;
    private String senderAvatar;
}


