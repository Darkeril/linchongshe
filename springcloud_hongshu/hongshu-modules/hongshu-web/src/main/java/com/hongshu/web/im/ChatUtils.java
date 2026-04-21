package com.hongshu.web.im;

import cn.hutool.json.JSONUtil;
import com.hongshu.common.core.constant.ImConstant;
import com.hongshu.common.core.domain.Message;
import com.hongshu.common.core.utils.RedisUtils;
import com.hongshu.web.service.web.IWebChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author hognshu
 */
@Slf4j
@Component
public class ChatUtils {

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    IWebChatService chatService;

    /**
     * 发送消息通知
     *
     * @param userId 接收者ID
     * @param type   0 点赞 1 评论 2 关注
     */
    public void sendMessage(String userId, Integer type) {
        String messageCountKey = ImConstant.MESSAGE_COUNT_KEY + userId;
        CountMessage countMessage;
        if (Boolean.TRUE.equals(redisUtils.hasKey(messageCountKey))) {
            String json = redisUtils.get(messageCountKey);
            countMessage = JSONUtil.toBean(json, CountMessage.class);
        } else {
            countMessage = new CountMessage();
            countMessage.setLikeOrCollectionCount(0L);
            countMessage.setFollowCount(0L);
            countMessage.setCommentCount(0L);
            redisUtils.set(messageCountKey, JSONUtil.toJsonStr(countMessage));
        }

        switch (type) {
            case 0:
                countMessage.setLikeOrCollectionCount(countMessage.getLikeOrCollectionCount() + 1);
                break;
            case 1:
                countMessage.setCommentCount(countMessage.getCommentCount() + 1);
                break;
            default:
                countMessage.setFollowCount(countMessage.getFollowCount() + 1);
                break;
        }
        Message message = new Message();
        message.setContent(countMessage);
        message.setMsgType(0);
        message.setAcceptUid(userId);
        chatService.sendMsg(message);
    }

    /**
     * 用户上线时推送计数消息
     * 在 WebSocket 连接建立时调用，主动推送离线期间的计数
     *
     * @param userId 用户ID
     */
    public void pushCountMessageOnConnect(String userId) {
        try {
            String messageCountKey = ImConstant.MESSAGE_COUNT_KEY + userId;
            
            // 从Redis获取计数
            if (Boolean.TRUE.equals(redisUtils.hasKey(messageCountKey))) {
                String json = redisUtils.get(messageCountKey);
                CountMessage countMessage = JSONUtil.toBean(json, CountMessage.class);
                
                // 检查是否有未读消息
                boolean hasUnread = (countMessage.getLikeOrCollectionCount() != null && countMessage.getLikeOrCollectionCount() > 0)
                        || (countMessage.getCommentCount() != null && countMessage.getCommentCount() > 0)
                        || (countMessage.getFollowCount() != null && countMessage.getFollowCount() > 0);
                
                if (hasUnread) {
                    // 有未读消息，主动推送
                    Message message = new Message();
                    message.setContent(countMessage);
                    message.setMsgType(0);
                    message.setAcceptUid(userId);
                    chatService.sendMsg(message);
                    
                    log.info("用户上线，主动推送计数消息: userId={}, likeCount={}, commentCount={}, followCount={}", 
                            userId, 
                            countMessage.getLikeOrCollectionCount(),
                            countMessage.getCommentCount(),
                            countMessage.getFollowCount());
                } else {
                    log.debug("用户上线，无未读消息: userId={}", userId);
                }
            } else {
                log.debug("用户上线，Redis中无计数数据: userId={}", userId);
            }
        } catch (Exception e) {
            log.error("推送上线计数消息失败: userId={}", userId, e);
        }
    }
}
