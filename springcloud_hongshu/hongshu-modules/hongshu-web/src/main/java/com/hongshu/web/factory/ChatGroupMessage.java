package com.hongshu.web.factory;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.common.core.domain.Message;
import com.hongshu.common.core.utils.ConvertUtils;
import com.hongshu.web.domain.entity.WebChat;
import com.hongshu.web.domain.entity.WebChatGroupMember;
import com.hongshu.web.domain.entity.WebChatUserRelation;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.mapper.web.WebChatGroupMemberMapper;
import com.hongshu.web.mapper.web.WebChatMapper;
import com.hongshu.web.mapper.web.WebChatUserRelationMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 群聊消息处理工厂
 *
 * @author: hongshu
 */
@Slf4j
public class ChatGroupMessage implements MessageFactory {

    private final WebSocketServer webSocketServer;
    private final WebChatMapper chatMapper;
    private final WebChatUserRelationMapper chatUserRelationMapper;
    private final WebChatGroupMemberMapper groupMemberMapper;
    private final WebUserMapper userMapper;

    public ChatGroupMessage(WebSocketServer webSocketServer, WebChatMapper chatMapper,
                            WebChatUserRelationMapper chatUserRelationMapper,
                            WebChatGroupMemberMapper groupMemberMapper,
                            WebUserMapper userMapper) {
        this.webSocketServer = webSocketServer;
        this.chatMapper = chatMapper;
        this.chatUserRelationMapper = chatUserRelationMapper;
        this.groupMemberMapper = groupMemberMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(Message message) {
        try {
            String content = String.valueOf(message.getContent());
            String groupChatId = message.getGroupChatId();

            if (groupChatId == null || groupChatId.isEmpty()) {
                throw new RuntimeException("群聊ID不能为空");
            }

            // 1. 保存群聊消息到web_chat表
            WebChat chat = ConvertUtils.sourceToTarget(message, WebChat.class);
            chat.setTimestamp(System.currentTimeMillis());
            chat.setContent(content);
            chat.setChatType(1); // 群聊
            chat.setGroupChatId(groupChatId);
            // 补充语音时长（如果有）
            if (message.getAudioTime() != null) {
                chat.setAudioTime(message.getAudioTime());
            }
            // 群聊消息的accept_uid应该为空，因为消息是发送给所有成员的
            chat.setAcceptUid(null);
            chatMapper.insert(chat);

            // 2. 获取发送者信息
            WebUser sender = userMapper.selectById(message.getSendUid());
            String senderName = sender != null ? sender.getUsername() : "";
            String senderAvatar = sender != null ? sender.getAvatar() : "";
            
            // 3. 获取群成员列表（排除发送者自己）
            QueryWrapper<WebChatGroupMember> memberWrapper = new QueryWrapper<>();
            memberWrapper.eq("group_chat_id", groupChatId);
            List<WebChatGroupMember> members = groupMemberMapper.selectList(memberWrapper);

            // 4. 为每个群成员更新或创建聊天关系记录
            // 对于群聊，关系记录应该是：sendUid=群聊ID（或最后发送者），acceptUid=成员ID
            for (WebChatGroupMember member : members) {
                String memberUserId = member.getUserId();

                // 查询或创建聊天关系（使用groupChatId和acceptUid作为唯一标识）
                QueryWrapper<WebChatUserRelation> relationWrapper = new QueryWrapper<>();
                relationWrapper.eq("accept_uid", memberUserId)
                        .eq("group_chat_id", groupChatId)
                        .eq("chat_type", 1);
                
                WebChatUserRelation relation = chatUserRelationMapper.selectOne(relationWrapper);
                
                if (relation != null) {
                    // 更新现有关系
                    relation.setSendUid(message.getSendUid()); // 更新为最后发送者
                    relation.setContent(content);
                    relation.setTimestamp(System.currentTimeMillis());
                    // 只有不是发送者自己才增加未读数
                    if (!memberUserId.equals(message.getSendUid())) {
                        relation.setCount(relation.getCount() + 1);
                    } else {
                        relation.setCount(0); // 发送者自己的未读数为0
                    }
                    relation.setMsgType(message.getMsgType());
                    chatUserRelationMapper.updateById(relation);
                } else {
                    // 创建新关系
                    relation = new WebChatUserRelation();
                    relation.setSendUid(message.getSendUid());
                    relation.setAcceptUid(memberUserId);
                    relation.setGroupChatId(groupChatId);
                    relation.setChatType(1);
                    relation.setContent(content);
                    relation.setTimestamp(System.currentTimeMillis());
                    // 只有不是发送者自己才设置未读数
                    relation.setCount(memberUserId.equals(message.getSendUid()) ? 0 : 1);
                    relation.setMsgType(message.getMsgType());
                    chatUserRelationMapper.insert(relation);
                }

                // 5. 更新群成员的未读数（如果成员没有免打扰，且不是发送者自己）
                if (!memberUserId.equals(message.getSendUid())) {
                    if (member.getMute() == null || member.getMute() == 0) {
                        int currentUnread = member.getUnreadCount() != null ? member.getUnreadCount() : 0;
                        member.setUnreadCount(currentUnread + 1);
                        groupMemberMapper.updateById(member);
                    }

                    // 6. 通过WebSocket推送消息给每个成员（排除发送者自己）
                    Message memberMessage = new Message();
                    memberMessage.setSendUid(message.getSendUid());
                    memberMessage.setAcceptUid(memberUserId);
                    memberMessage.setContent(content);
                    memberMessage.setMsgType(message.getMsgType());
                    memberMessage.setChatType(1);
                    memberMessage.setGroupChatId(groupChatId);
                    // 填充发送者信息
                    memberMessage.setSenderName(senderName);
                    memberMessage.setSenderAvatar(senderAvatar);
                    webSocketServer.sendInfo(memberMessage);
                }
            }

            // 7. 更新群聊关系列表（给所有成员推送更新后的群聊列表）
            updateGroupChatList(groupChatId, members.stream()
                    .map(WebChatGroupMember::getUserId)
                    .collect(Collectors.toList()));

        } catch (Exception e) {
            log.error("发送群聊消息失败", e);
            throw new RuntimeException("发送群聊消息失败: " + e.getMessage());
        }
    }

    /**
     * 更新群聊列表（推送消息类型5给所有成员）
     */
    private void updateGroupChatList(String groupChatId, List<String> memberUserIds) {
        // 这里可以获取群聊的最新消息列表，然后推送给所有成员
        // 暂时简化处理，只推送更新通知
        for (String userId : memberUserIds) {
            Message updateMessage = new Message();
            updateMessage.setAcceptUid(userId);
            updateMessage.setContent("group_list_update");
            updateMessage.setMsgType(5); // 自定义消息类型，用于更新列表
            updateMessage.setChatType(1);
            updateMessage.setGroupChatId(groupChatId);
            webSocketServer.sendInfo(updateMessage);
        }
    }
}

