package com.hongshu.ai.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.ResponseInfo;
import com.hongshu.ai.common.enums.ChatContentEnum;
import com.hongshu.ai.common.enums.ChatRoleEnum;
import com.hongshu.ai.common.enums.ChatStatusEnum;
import com.hongshu.ai.common.enums.IntegerEnum;
import com.hongshu.ai.common.exception.ErrorException;
import com.hongshu.ai.domain.command.ChatCommand;
import com.hongshu.ai.domain.command.ChatMessageCommand;
import com.hongshu.ai.domain.dto.ChatMessageDTO;
import com.hongshu.ai.domain.entity.Assistant;
import com.hongshu.ai.domain.entity.Chat;
import com.hongshu.ai.domain.entity.ChatMessage;
import com.hongshu.ai.domain.vo.ChatMessageVO;
import com.hongshu.ai.mapper.AssistantMapper;
import com.hongshu.ai.mapper.ChatMapper;
import com.hongshu.ai.mapper.ChatMessageMapper;
import com.hongshu.ai.service.IChatMessageService;
import com.hongshu.common.core.utils.DozerUtil;
import com.hongshu.common.core.validator.ValidatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对话消息 服务实现类
 *
 * @author: Yang
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private ChatMapper chatMapper;
    @Autowired
    private AssistantMapper assistantMapper;


    /**
     * 根据id获取对话消息信息
     *
     * @param id 对话消息id
     * @return
     */
    private ChatMessage getChatMessage(Long id) {
        ChatMessage chatMessage = chatMessageMapper.selectById(id);
        if (ValidatorUtil.isNull(chatMessage)) {
            throw new ErrorException("对话消息信息不存在，无法操作");
        }
        return chatMessage;
    }

    /**
     * 根据id获取对话消息信息
     *
     * @param id 对话消息id
     * @return
     */
    private ChatMessage getChatMessageByMessageId(String messageId) {
        ChatMessage chatMessage = chatMessageMapper.selectOne(new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getMessageId, messageId));
        if (ValidatorUtil.isNull(chatMessage)) {
            throw new ErrorException("对话消息信息不存在，无法操作");
        }
        return chatMessage;
    }

    @Override
    public ResponseInfo<IPageInfo<ChatMessageVO>> pageChatMessage(Query query) {
        IPage<ChatMessageVO> iPage = chatMessageMapper.pageChatMessage(new Page<>(query.getCurrent(), query.getSize()), query);
        return ResponseInfo.success(new IPageInfo(iPage.getCurrent(), iPage.getSize(), iPage.getTotal(), iPage.getRecords()));
    }

    @Override
    public ResponseInfo<List<ChatMessageVO>> listChatMessage(Query query) {
        return ResponseInfo.success(chatMessageMapper.listChatMessage(query));
    }

    @Override
    public ResponseInfo<List<ChatMessageDTO>> listChatMessage(Long chatId) {
        List<ChatMessage> chatMessages = chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getChatId, chatId).eq(ChatMessage::getStatus, IntegerEnum.TWO.getValue())
                .orderByDesc(ChatMessage::getId).last("limit 10"));
        chatMessages = chatMessages.stream().sorted(Comparator.comparing(ChatMessage::getId)).collect(Collectors.toList());
        return ResponseInfo.success(DozerUtil.convertor(chatMessages, ChatMessageDTO.class));
    }

    @Override
    public ResponseInfo<ChatMessageVO> getChatMessageById(Long id) {
        return ResponseInfo.success(DozerUtil.convertor(getChatMessage(id), ChatMessageVO.class));
    }

    @Override
    public ResponseInfo<Long> getChatIdByMessageId(String messageId) {
        ChatMessage chatMessage = getChatMessageByMessageId(messageId);
        return ResponseInfo.success(chatMessage.getChatId());
    }

    @Override
    public ResponseInfo<ChatMessageDTO> getChatByMessageId(String messageId) {
        ChatMessage chatMessage = getChatMessageByMessageId(messageId);
        return ResponseInfo.success(DozerUtil.convertor(chatMessage, ChatMessageDTO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo<String> saveChatMessage(ChatCommand command) {
        Chat chat = chatMapper.selectOne(new LambdaQueryWrapper<Chat>().eq(Chat::getChatNumber, command.getChatNumber()));
        if (ValidatorUtil.isNull(chat)) {
            return ResponseInfo.validateFail("对话不存在，请刷新页面");
        }
        ChatMessage chatMessage = ChatMessage.builder()
                .chatId(chat.getId()).messageId(command.getConversationId()).model(command.getModel()).modelVersion(command.getModelVersion())
                .content(command.getPrompt()).contentType(ChatContentEnum.TEXT.getValue()).role(ChatRoleEnum.USER.getValue()).status(ChatStatusEnum.REPLY.getValue())
                .build();
        chatMessage.setCreateUser(command.getOperater());
        chatMessageMapper.insert(chatMessage);
        return ResponseInfo.success(chatMessage.getMessageId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo saveChatMessage(ChatMessageCommand command) {
        ChatMessage chatMessage = DozerUtil.convertor(command, ChatMessage.class);
        chatMessageMapper.insert(chatMessage);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo<List<ChatMessageDTO>> saveChatMessage(ChatCommand command, Long chatId, String messageId) {
        List<ChatMessageDTO> messages = listChatMessage(chatId).getData();
        ChatMessage chatMessage = ChatMessage.builder()
                .createUser(command.getOperater()).chatId(chatId).messageId(UUID.fastUUID().toString())
                .model(command.getModel()).modelVersion(command.getModelVersion())
                .content(command.getPrompt()).role(ChatRoleEnum.USER.getValue())
                .status(ChatStatusEnum.REPLY.getValue())
                .build();
        chatMessageMapper.insert(chatMessage);
        messages.add(DozerUtil.convertor(chatMessage, ChatMessageDTO.class));
        if (ValidatorUtil.isNotNullAndZero(command.getAssistantId())) {
            Assistant assistant = assistantMapper.selectById(command.getAssistantId());
            if (ValidatorUtil.isNotNull(assistant.getSystemPrompt())) {
                messages.add(0, ChatMessageDTO.builder().role(ChatRoleEnum.SYSTEM.getValue()).content(assistant.getSystemPrompt()).build());
            }
        } else if (ValidatorUtil.isNotNull(command.getSystemPrompt())) {
            messages.add(0, ChatMessageDTO.builder().role(ChatRoleEnum.SYSTEM.getValue()).content(command.getSystemPrompt()).build());
        }
        return ResponseInfo.success(messages);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo updateMessageStatus(String messageId, Integer status) {
        chatMessageMapper.updateMessageStatus(messageId, status);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo updateMessageUsedTokens(String messageId, Long usedTokens) {
        chatMessageMapper.updateMessageUsedTokens(messageId, usedTokens);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo removeChatMessageByIds(List<Long> ids) {
        chatMessageMapper.deleteBatchIds(ids);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo removeChatMessageByMessageId(String messageId) {
        ChatMessage chatMessage = getChatMessageByMessageId(messageId);
        chatMessageMapper.delete(new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getMessageId, messageId));
        chatMessageMapper.delete(new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getMessageId, chatMessage.getParentMessageId()));
        return ResponseInfo.success();
    }

}
