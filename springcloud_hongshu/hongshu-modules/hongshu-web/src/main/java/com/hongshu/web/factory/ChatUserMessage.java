package com.hongshu.web.factory;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.common.core.domain.Message;
import com.hongshu.common.core.utils.ConvertUtils;
import com.hongshu.web.domain.entity.WebChat;
import com.hongshu.web.domain.entity.WebChatUserRelation;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.domain.vo.ChatUserRelationVo;
import com.hongshu.web.mapper.web.WebChatMapper;
import com.hongshu.web.mapper.web.WebChatUserRelationMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author: hongshu
 */
@Slf4j
public class ChatUserMessage implements MessageFactory {

    private final WebSocketServer webSocketServer;

    private final WebChatMapper chatMapper;

    private final WebUserMapper userMapper;

    private final WebChatUserRelationMapper chatUserRelationMapper;

    public ChatUserMessage(WebSocketServer webSocketServer, WebChatMapper chatMapper, WebUserMapper userMapper, WebChatUserRelationMapper chatUserRelationMapper) {
        this.webSocketServer = webSocketServer;
        this.chatMapper = chatMapper;
        this.userMapper = userMapper;
        this.chatUserRelationMapper = chatUserRelationMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(Message message) {
        try {
            String content = String.valueOf(message.getContent());
            this.saveMessage(message, 0);
            this.saveMessage(message, 1);

            this.getUserChatList(message, 0);
            this.getUserChatList(message, 1);

            WebChat chat = ConvertUtils.sourceToTarget(message, WebChat.class);
            long timestamp = System.currentTimeMillis();
            chat.setTimestamp(timestamp);
            chat.setContent(content);
            // 补充语音时长（如果有）
            if (message.getAudioTime() != null) {
                chat.setAudioTime(message.getAudioTime());
            }
            chatMapper.insert(chat);
            
            // 🔧 修复：推送消息时，需要包含ID和时间戳，以便前端去重
            // 由于Message类没有id和timestamp字段，我们需要通过其他方式传递
            // 方案：在推送消息之前，创建一个新的Message对象，将chat的ID和时间戳信息通过content的扩展字段传递
            // 但实际上，前端应该通过内容+发送者+接收者来判断是否重复
            // 这里我们确保推送的消息包含时间戳信息（通过其他方式，比如在content中添加元数据）
            // 但更简单的方法是：前端通过内容+发送者+接收者+时间容差来判断是否重复
        } catch (Exception e) {
            log.error("发送消息失败", e);
            throw new RuntimeException("发送消息失败");
        }
    }

    private void saveMessage(Message message, Integer type) {
        String content = String.valueOf(message.getContent());
        String sendUid = type == 0 ? message.getSendUid() : message.getAcceptUid();
        String acceptUid = type == 0 ? message.getAcceptUid() : message.getSendUid();

        QueryWrapper<WebChatUserRelation> queryWrapper = new QueryWrapper<WebChatUserRelation>()
                .eq("send_uid", sendUid)
                .eq("accept_uid", acceptUid);

        // 如果是商品聊天，添加商品ID条件
        if (message.getProductId() != null) {
            queryWrapper.eq("product_id", message.getProductId())
                    .isNull("group_chat_id"); // 商品聊天不是群聊
        } else if (message.getGroupChatId() != null) {
            // 如果是群聊，添加群聊ID条件
            queryWrapper.eq("group_chat_id", message.getGroupChatId())
                    .isNull("product_id"); // 群聊不是商品聊天
        } else {
            // 私信消息：既不是商品聊天，也不是群聊
            queryWrapper.isNull("product_id")
                    .isNull("group_chat_id")
                    .and(wrapper -> wrapper
                            .isNull("chat_type")
                            .or()
                            .eq("chat_type", 0)
                    );
        }

        // 使用 selectList 然后取第一条，避免 TooManyResultsException
        List<WebChatUserRelation> relations = chatUserRelationMapper.selectList(queryWrapper);
        WebChatUserRelation chatUserRelation = relations.isEmpty() ? null : relations.get(0);

        if (chatUserRelation != null) {
            // 已存在聊天关系，直接更新
            chatUserRelation.setContent(content);
            chatUserRelation.setTimestamp(System.currentTimeMillis());
            chatUserRelation.setCount(type == 0 ? chatUserRelation.getCount() + 1 : 0);
            chatUserRelation.setMsgType(message.getMsgType());
            chatUserRelationMapper.updateById(chatUserRelation);
        } else {
            // 只有在以下两种情况下才新建聊天关系：
            // 1. 是商品聊天
            // 2. 是首次普通聊天
            chatUserRelation = new WebChatUserRelation();
            chatUserRelation.setSendUid(sendUid);
            chatUserRelation.setAcceptUid(acceptUid);
            chatUserRelation.setCount(type == 0 ? 1 : 0);
            chatUserRelation.setContent(content);
            chatUserRelation.setTimestamp(System.currentTimeMillis());
            chatUserRelation.setProductId(message.getProductId());
            chatUserRelationMapper.insert(chatUserRelation);
        }
    }

    /**
     * 客服用户ID常量（使用0作为客服ID，因为数据库字段是整数类型）
     */
    private static final String CUSTOMER_SERVICE_ID = "0";

    public void getUserChatList(Message message, Integer type) {
        String acceptUid = type == 0 ? message.getSendUid() : message.getAcceptUid();
        // 🔧 修复：只查询用户接收到的消息（accept_uid = 用户ID），排除：
        // 1. 客服消息（send_uid != 客服ID）
        // 2. 用户发送给客服的消息（如果accept_uid是客服ID，则不查询，因为这是用户发送的消息，不应该出现在"我的消息"列表中）
        // 3. 群聊和商品聊天
        
        // 如果acceptUid是客服ID，说明这是用户发送给客服的消息，不应该更新用户列表
        if (CUSTOMER_SERVICE_ID.equals(acceptUid)) {
            // 用户发送给客服的消息，不更新用户列表（避免客服对话出现在"我的消息"列表中）
            return;
        }
        
        // 只查询私聊记录，排除群聊、商品聊天和客服消息（与 getChatUserList 保持一致）
        List<WebChatUserRelation> chatUserRelationList = chatUserRelationMapper.selectList(
                new QueryWrapper<WebChatUserRelation>()
                        .eq("accept_uid", acceptUid)
                        .ne("send_uid", CUSTOMER_SERVICE_ID) // 排除客服消息
                        .and(wrapper -> wrapper
                                .isNull("chat_type")
                                .or()
                                .eq("chat_type", 0)
                        )
                        .isNull("product_id")
                        .isNull("group_chat_id") // 确保不是群聊
                        .orderByDesc("timestamp"));
        Set<String> uids = chatUserRelationList.stream().map(WebChatUserRelation::getSendUid).collect(Collectors.toSet());
        
        // 过滤掉客服ID，因为客服不在用户表中
        Set<String> realUserIds = uids.stream()
                .filter(uid -> !CUSTOMER_SERVICE_ID.equals(uid))
                .collect(Collectors.toSet());
        
        Map<String, WebUser> userMap = realUserIds.isEmpty() 
                ? new java.util.HashMap<>() 
                : userMapper.selectBatchIds(realUserIds).stream().collect(Collectors.toMap(WebUser::getId, user -> user));
        
        // 使用 Map 去重，保留每个用户最新的记录
        Map<String, ChatUserRelationVo> uniqueMap = new java.util.HashMap<>();
        chatUserRelationList.forEach(item -> {
            String sendUid = item.getSendUid();
            // 再次过滤客服消息（双重保险）
            if (CUSTOMER_SERVICE_ID.equals(sendUid)) {
                return; // 跳过客服消息
            }
            
            ChatUserRelationVo chatUserRelationVo = ConvertUtils.sourceToTarget(item, ChatUserRelationVo.class);
            WebUser user = userMap.get(sendUid);
            if (user != null) {
                chatUserRelationVo.setUid(String.valueOf(user.getId()));
                chatUserRelationVo.setUsername(user.getUsername());
                chatUserRelationVo.setAvatar(user.getAvatar());
                
                // 如果已存在该用户的记录，比较时间戳，保留最新的
                ChatUserRelationVo existing = uniqueMap.get(sendUid);
                if (existing == null || item.getTimestamp() > existing.getTimestamp()) {
                    uniqueMap.put(sendUid, chatUserRelationVo);
                }
            }
        });
        
        // 转换为列表并按时间戳排序
        List<ChatUserRelationVo> chatUserRelationVoList = new ArrayList<>(uniqueMap.values());
        chatUserRelationVoList.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

        Message currentUserMessage = new Message();
        currentUserMessage.setAcceptUid(acceptUid);
        currentUserMessage.setContent(chatUserRelationVoList);
        currentUserMessage.setMsgType(5);
        webSocketServer.sendInfo(currentUserMessage);
    }
}
