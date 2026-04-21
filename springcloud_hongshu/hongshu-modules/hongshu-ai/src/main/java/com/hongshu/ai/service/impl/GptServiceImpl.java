package com.hongshu.ai.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.ResponseInfo;
import com.hongshu.ai.common.constant.BaseConfigConstant;
import com.hongshu.ai.common.enums.ChatModelEnum;
import com.hongshu.ai.common.enums.ChatRoleEnum;
import com.hongshu.ai.common.enums.ChatStatusEnum;
import com.hongshu.ai.common.enums.StatusEnum;
import com.hongshu.ai.controller.BaseEntity;
import com.hongshu.ai.domain.command.ChatCommand;
import com.hongshu.ai.domain.command.ChatMessageCommand;
import com.hongshu.ai.domain.dto.ChatMessageDTO;
import com.hongshu.ai.domain.dto.ModelDTO;
import com.hongshu.ai.domain.dto.config.AppInfoDTO;
import com.hongshu.ai.domain.entity.Assistant;
import com.hongshu.ai.domain.entity.User;
import com.hongshu.ai.domain.vo.ChatVO;
import com.hongshu.ai.mapper.AssistantMapper;
import com.hongshu.ai.mapper.ModelMapper;
import com.hongshu.ai.mapper.UserMapper;
import com.hongshu.ai.service.BaseConfigService;
import com.hongshu.ai.service.GptService;
import com.hongshu.ai.service.IChatMessageService;
import com.hongshu.ai.service.IChatService;
import com.hongshu.common.core.exception.exceptionType.BusinessException;
import com.hongshu.common.core.utils.DozerUtil;
import com.hongshu.common.core.validator.ValidatorUtil;
import com.hongshu.common.core.validator.base.BaseAssert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * chatgpt接口实现类
 *
 * @author: Yang
 * @date: 2023/5/5
 * @version: 1.0.0
 */
@Slf4j
@Service
public class GptServiceImpl implements GptService {

    @Autowired
    private IChatService chatService;
    @Autowired
    private IChatMessageService chatMessageService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private AssistantMapper assistantMapper;
    @Autowired
    private BaseConfigService baseConfigService;

    @Override
    public ModelDTO getModel(String model) {
        Query query = new Query();
        query.put("model", model);
        return DozerUtil.convertor(modelMapper.getModel(query), ModelDTO.class);
    }

    @Override
    public Long saveChat(ChatCommand command) {
        return chatService.saveChat(command).getData();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String chatMessage(ChatCommand command) {
        return chatMessageService.saveChatMessage(command).getData();
    }

    @Override
    public ChatMessageDTO getMessageByConverstationId(String conversationId) {
        ChatMessageDTO chatMessage = chatMessageService.getChatByMessageId(conversationId).getData();
        return chatMessage;
    }

    @Override
    public List<ChatMessageDTO> listMessageByConverstationId(String uid, String conversationId) {
        User user = userMapper.getUserByUid(uid);
        // 如果用户不存在（web/uniapp用户），默认不使用上下文
        Boolean context = (user != null && user.getContext() != null) ? user.getContext() : false;
        ChatMessageDTO chatMessage = chatMessageService.getChatByMessageId(conversationId).getData();
        List<ChatMessageDTO> chatMessages = new ArrayList<>();
        if (ValidatorUtil.isNotNull(context) && context) {
            chatMessages = chatMessageService.listChatMessage(chatMessage.getChatId()).getData();
        }
        chatMessages.add(chatMessage);
        ChatVO chat = chatService.getChatById(chatMessage.getChatId()).getData();
        if (ValidatorUtil.isNotNull(chat) && ValidatorUtil.isNotNullAndZero(chat.getAssistantId())) {
            Assistant assistant = assistantMapper.selectById(chat.getAssistantId());
            if (ValidatorUtil.isNotNull(assistant.getSystemPrompt())) {
                chatMessages.add(0, ChatMessageDTO.builder().role(ChatRoleEnum.SYSTEM.getValue()).content(assistant.getSystemPrompt()).build());
            }
        }
        return chatMessages;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveChatMessage(ChatMessageCommand command) {
        ResponseInfo response = chatMessageService.saveChatMessage(command);
        if (!response.isSuccess()) {
            return;
        }
        if (!ChatRoleEnum.ASSISTANT.getValue().equals(command.getRole())) {
            return;
        }
        if (!ChatStatusEnum.SUCCESS.getValue().equals(command.getStatus())) {
            return;
        }
        // 扣除用户电量（仅对 chat 模块的用户）
        User user = userMapper.getUserByChatId(command.getChatId());
        if (user != null) {
            UpdateWrapper<User> uw = new UpdateWrapper<>();
            uw.lambda().set(User::getNum, user.getNum() - 1).eq(BaseEntity::getId, user.getId());
            userMapper.update(null, uw);
        }
        // web/uniapp 用户没有电量限制，跳过扣除
    }

    @Override
    public List<ChatMessageDTO> saveChatMessage(ChatCommand command, Long chatId, String messageId) {
        return chatMessageService.saveChatMessage(command, chatId, messageId).getData();
    }

    /**
     * 校验对话数据
     */
    @Override
    public ChatCommand validateGptCommand(ChatCommand command) {
        BaseAssert.isBlankOrNull(command.getModel(), "缺少模型信息");
        BaseAssert.isBlankOrNull(command.getPrompt(), "缺少prompt");
        ModelDTO model = getModel(command.getModel());
        if (ValidatorUtil.isNull(model)) {
            throw new BusinessException("模型已经不存在啦，请切换模型进行回复～");
        }
        if (StatusEnum.DISABLED.getValue().equals(model.getStatus())) {
            throw new BusinessException("该模型已被禁用，请切换模型进行回复～");
        }
        ChatModelEnum modelEnum = ChatModelEnum.getEnum(command.getModel());
        if (ValidatorUtil.isNull(modelEnum)) {
            throw new BusinessException("未知的模型类型，功能未接入");
        }
        // 兼容老版本，没有选择版本时使用模型默认版本
        if (ValidatorUtil.isNull(command.getModelVersion())) {
            ModelDTO modelDTO = getModel(command.getModel());
            command.setModelVersion(modelDTO.getVersion());
        }
        String messageId = UUID.fastUUID().toString();
        command.setConversationId(messageId);
        //校验用户
        if (ValidatorUtil.isNull(command.getApi()) || !command.getApi()) {
            validateUser(command);
        }
        return command;
    }

    /**
     * 校验账户
     *
     * @param command
     */
    private void validateUser(ChatCommand command) {
        User user = userMapper.selectById(command.getUserId());
        // 如果 chat 模块的 user 表中没有该用户记录，说明是从 web/uniapp 登录的用户
        // 这种情况下跳过用户验证，允许直接使用（因为已经通过 JWT 认证）
        if (ValidatorUtil.isNull(user)) {
            return;
        }
        // 是否限制访问
        AppInfoDTO appInfo = baseConfigService.getBaseConfigByName(BaseConfigConstant.APP_INFO, AppInfoDTO.class);
        if (ValidatorUtil.isNotNull(appInfo) && ValidatorUtil.isNotNull(appInfo.getIsGPTLimit()) && appInfo.getIsGPTLimit().equals(StatusEnum.ENABLED.getValue())) {
            return;
        }
        if (user.getNum() < 1) {
            throw new BusinessException("电量不足，开通会员享受更多权益");
        }
    }

    @Override
    public void updateMessageStatus(String messageId, Integer status) {
        chatMessageService.updateMessageStatus(messageId, status);
    }

    @Override
    public void updateMessageUsedTokens(String messageId, Long usedTokens) {
        chatMessageService.updateMessageUsedTokens(messageId, usedTokens);
    }


}
