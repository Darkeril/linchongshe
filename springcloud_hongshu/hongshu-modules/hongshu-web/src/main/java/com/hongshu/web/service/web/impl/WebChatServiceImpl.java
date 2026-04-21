package com.hongshu.web.service.web.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.core.constant.ImConstant;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.common.core.domain.CountMessage;
import com.hongshu.common.core.domain.Message;
import com.hongshu.common.core.enums.MessageTypeEnum;
import com.hongshu.common.core.exception.BusinessException;
import com.hongshu.common.core.utils.ConvertUtils;
import com.hongshu.common.core.utils.RedisUtils;
import com.hongshu.web.domain.entity.*;
import com.hongshu.web.domain.vo.ChatProductRelationVO;
import com.hongshu.web.domain.vo.ChatUserRelationVo;
import com.hongshu.web.domain.vo.SystemNotification;
import com.hongshu.web.factory.ChatCountMessage;
import com.hongshu.web.factory.ChatGroupMessage;
import com.hongshu.web.factory.ChatUserMessage;
import com.hongshu.web.factory.MessageFactory;
import com.hongshu.web.mapper.idle.IdleProductMapper;
import com.hongshu.web.mapper.web.WebChatGroupMemberMapper;
import com.hongshu.web.mapper.web.WebChatMapper;
import com.hongshu.web.mapper.web.WebChatUserRelationMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.service.web.IAICustomerServiceService;
import com.hongshu.web.service.web.IWebChatService;
import com.hongshu.web.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 聊天
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class WebChatServiceImpl extends ServiceImpl<WebChatMapper, WebChat> implements IWebChatService {

    @Autowired
    private WebUserMapper userMapper;
    @Autowired
    private WebChatMapper chatMapper;
    @Autowired
    private IdleProductMapper productMapper;
    @Autowired
    private WebSocketServer webSocketServer;
    @Autowired
    private WebChatUserRelationMapper chatUserRelationMapper;
    @Autowired
    private WebChatGroupMemberMapper groupMemberMapper;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired(required = false)
    private IAICustomerServiceService aiCustomerServiceService;


    /**
     * 发送消息
     *
     * @param message 消息
     */
    @Override
    public void sendMsg(Message message) {
        // 如果是群聊消息，先不发送给单个用户，而是通过群聊消息工厂处理
        if (message.getChatType() != null && message.getChatType() == 1) {
            // 群聊消息
            MessageFactory messageFactory = null;
            switch (message.getMsgType()) {
                case 1: // 文本消息
                case 2: // 图片消息
                case 3: // 语音消息
                case 4: // 视频消息
                    messageFactory = new ChatGroupMessage(webSocketServer, chatMapper, chatUserRelationMapper, groupMemberMapper, userMapper);
                    break;
                default:
                    break;
            }
            if (messageFactory != null) {
                messageFactory.sendMessage(message);
            }
        } else {
            // 私聊消息（原有逻辑）
            // 🔧 注意：Message类没有id和timestamp字段，所以推送的消息不包含这些信息
            // 前端需要通过内容+发送者+接收者来判断是否重复
            webSocketServer.sendInfo(message);
            MessageFactory messageFactory = null;

            // 过滤发送的请求类型
            Integer privateMsgType = message.getMsgType();
            if (privateMsgType != null) {
                switch (privateMsgType) {
                    case 0:
                        // 计数类消息（未读数聚合）
                        messageFactory = new ChatCountMessage(redisUtils);
                        break;
                    case 1: // 文本
                    case 2: // 图片
                    case 3: // 语音
                    case 4: // 视频
                        // 私聊普通消息（含语音、视频）统一走 ChatUserMessage，负责落库与会话关系
                        messageFactory = new ChatUserMessage(webSocketServer, chatMapper, userMapper, chatUserRelationMapper);
                        break;
                    default:
                        break;
                }
            }
            if (messageFactory != null) {
                messageFactory.sendMessage(message);
            }

            // 🔧 AI客服自动回复：如果消息是发送给客服的，且AI客服已启用，则自动回复
            // 支持文本消息（msgType=1）、图片消息（msgType=2）、语音消息（msgType=3）、视频消息（msgType=4）
            if (CUSTOMER_SERVICE_ID.equals(message.getAcceptUid())
                    && message.getMsgType() != null
                    && (message.getMsgType() == 1
                        || message.getMsgType() == 2
                        || message.getMsgType() == 3
                        || message.getMsgType() == 4) // 文本、图片、语音、视频消息
                    && message.getProductId() == null
                    && message.getGroupChatId() == null
                    && aiCustomerServiceService != null
                    && aiCustomerServiceService.isAICustomerServiceEnabled()) {
                // 异步调用AI生成回复，避免阻塞主流程
                String sendUid = message.getSendUid();
                Integer msgType = message.getMsgType();
                String content = String.valueOf(message.getContent());
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    try {
                        // 获取历史聊天记录（最近10条，用于上下文理解）
                        List<WebChat> chatHistory = chatMapper.selectList(
                                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WebChat>()
                                        .and(wrapper -> wrapper
                                                .or(e -> e
                                                        .eq("send_uid", sendUid)
                                                        .eq("accept_uid", CUSTOMER_SERVICE_ID)
                                                )
                                                .or(e -> e
                                                        .eq("send_uid", CUSTOMER_SERVICE_ID)
                                                        .eq("accept_uid", sendUid)
                                                )
                                        )
                                        .isNull("product_id")
                                        .isNull("group_chat_id")
                                        .orderByDesc("timestamp")
                                        .last("LIMIT 10")
                        );

                        // 构建历史对话列表（格式：["用户: xxx", "客服: xxx", ...]）
                        List<String> historyList = new ArrayList<>();
                        for (int i = chatHistory.size() - 1; i >= 0; i--) {
                            WebChat chat = chatHistory.get(i);
                            String role = CUSTOMER_SERVICE_ID.equals(chat.getSendUid()) ? "客服" : "用户";
                            // 根据消息类型格式化历史消息
                            String historyContent = chat.getContent();
                            if (chat.getMsgType() != null) {
                                if (chat.getMsgType() == 2) {
                                    historyContent = "[图片] " + historyContent;
                                } else if (chat.getMsgType() == 4) {
                                    historyContent = "[视频] " + historyContent;
                                }
                            }
                            historyList.add(role + ": " + historyContent);
                        }

                        String aiResponse;
                        // 根据消息类型调用不同的AI方法
                        if (msgType == 2) {
                            // 图片消息：content是图片URL
                            String imageUrl = content;
                            String userText = ""; // 图片消息通常没有文本
                            aiResponse = aiCustomerServiceService.generateAIResponseWithMedia(
                                    sendUid, userText, imageUrl, null, historyList);
                        } else if (msgType == 3) {
                            // 语音消息：content 是语音 URL，当前不直接做语音识别，而是提示用户使用文字描述
                            String audioUrl = content;
                            Integer audioTime = message.getAudioTime();
                            StringBuilder userTextBuilder = new StringBuilder();
                            userTextBuilder.append("用户发送了一条语音消息，语音文件地址为：")
                                    .append(audioUrl)
                                    .append("。语音时长约为");
                            if (audioTime != null && audioTime > 0) {
                                userTextBuilder.append(audioTime).append("秒");
                            } else {
                                userTextBuilder.append("数秒");
                            }
                            userTextBuilder.append("。请你用友好、简短的语气回复用户，说明当前系统暂时无法直接解析语音内容，并请用户用简短的文字再次描述一下问题。");
                            String userText = userTextBuilder.toString();
                            aiResponse = aiCustomerServiceService.generateAIResponse(
                                    sendUid, userText, historyList);
                        } else if (msgType == 4) {
                            // 视频消息：content是视频URL
                            String videoUrl = content;
                            String userText = ""; // 视频消息通常没有文本
                            aiResponse = aiCustomerServiceService.generateAIResponseWithMedia(
                                    sendUid, userText, null, videoUrl, historyList);
                        } else {
                            // 文本消息
                            aiResponse = aiCustomerServiceService.generateAIResponse(sendUid, content, historyList);
                        }

                        // 构建AI回复消息
                        Message aiReplyMessage = new Message();
                        aiReplyMessage.setSendUid(CUSTOMER_SERVICE_ID);
                        aiReplyMessage.setAcceptUid(sendUid);
                        aiReplyMessage.setContent(aiResponse);
                        aiReplyMessage.setMsgType(1); // 文本消息
                        aiReplyMessage.setChatType(0); // 私聊

                        // 发送AI回复
                        sendMsg(aiReplyMessage);

                        log.info("AI客服自动回复成功: userId={}, msgType={}, content={}, aiResponse={}",
                                sendUid, msgType, content, aiResponse);
                    } catch (Exception e) {
                        log.error("AI客服自动回复失败: userId={}, msgType={}, content={}",
                                sendUid, msgType, content, e);
                    }
                });
            }
        }
    }

    /**
     * 获取所有聊天记录
     *
     * @param currentPage 分页
     * @param pageSize    分页数
     * @param acceptUid   接收方用户ID（私聊时使用）
     * @param groupChatId 群聊ID（群聊时使用）
     */
    @Override
    public Page<WebChat> getAllChatRecord(long currentPage, long pageSize, String acceptUid, String groupChatId) {
        String currentUid = AuthContextHolder.getUserId();
        QueryWrapper<WebChat> queryWrapper = new QueryWrapper<WebChat>();

        if (groupChatId != null && !groupChatId.isEmpty()) {
            // 群聊消息查询
            queryWrapper.eq("group_chat_id", groupChatId)
                    .eq("chat_type", 1) // 群聊
                    .orderByAsc("timestamp");
        } else {
            // 私聊消息查询（原有逻辑）
            queryWrapper.isNull("product_id")
                    .and(wrapper -> wrapper
                            .or(e -> e
                                    .eq("send_uid", currentUid)
                                    .eq("accept_uid", acceptUid)
                            )
                            .or(e -> e
                                    .eq("send_uid", acceptUid)
                                    .eq("accept_uid", currentUid)
                            )
                    )
                    .orderByAsc("timestamp");
        }
        return this.page(new Page<>((int) currentPage, (int) pageSize), queryWrapper);
    }

    /**
     * 获取商品聊天记录
     *
     * @param currentPage 分页
     * @param pageSize    分页数
     * @param acceptUid   接收方用户ID
     */
    @Override
    public Page<WebChat> getProductChatRecord(long currentPage, long pageSize, String acceptUid, String productId) {
//        String currentUid = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String currentUid = AuthContextHolder.getUserId();

        QueryWrapper<WebChat> queryWrapper = new QueryWrapper<WebChat>()
                // 添加商品ID过滤条件
                .eq("product_id", productId)
                // 使用嵌套条件处理发送者和接收者的关系
                .and(wrapper -> wrapper
                        .or(e -> e
                                .eq("send_uid", currentUid)
                                .eq("accept_uid", acceptUid)
                        )
                        .or(e -> e
                                .eq("send_uid", acceptUid)
                                .eq("accept_uid", currentUid)
                        )
                )
                .orderByAsc("timestamp");
        return this.page(new Page<>((int) currentPage, (int) pageSize), queryWrapper);
    }

    /**
     * 获取当前用户下所有聊天的用户信息
     */
    @Override
    public List<ChatUserRelationVo> getChatUserList() {
//        String currentUid = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String currentUid = AuthContextHolder.getUserId();
        List<ChatUserRelationVo> result = new ArrayList<>();
        // 只查询私聊记录，排除群聊（chat_type != 1 或 chat_type is null）、商品聊天（product_id is null）和客服（send_uid != 0）
        List<WebChatUserRelation> chatUserRelationList = chatUserRelationMapper
                .selectList(new QueryWrapper<WebChatUserRelation>()
                        .eq("accept_uid", currentUid)
                        .ne("send_uid", CUSTOMER_SERVICE_ID) // 排除客服消息
                        .and(wrapper -> wrapper
                                .isNull("chat_type")
                                .or()
                                .eq("chat_type", 0)
                        )
                        .isNull("product_id")
                        .isNull("group_chat_id") // 确保不是群聊
                        .orderByDesc("timestamp"));
        if (chatUserRelationList.isEmpty()) {
            return result;
        }
        Set<String> uids = chatUserRelationList.stream()
                .map(WebChatUserRelation::getSendUid)
                .filter(uid -> !CUSTOMER_SERVICE_ID.equals(uid)) // 过滤掉客服ID，因为客服不在用户表中
                .collect(Collectors.toSet());
        Map<String, WebUser> userMap = uids.isEmpty()
                ? new HashMap<>()
                : userMapper.selectBatchIds(uids).stream().collect(Collectors.toMap(WebUser::getId, user -> user));

        // 使用 Map 去重，保留每个用户最新的记录
        Map<String, ChatUserRelationVo> uniqueMap = new HashMap<>();
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
        result = new ArrayList<>(uniqueMap.values());
        result.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        return result;
    }

    @Override
    public List<ChatProductRelationVO> getProductChatUserList() {
//        String currentUid = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String currentUid = AuthContextHolder.getUserId();
        List<ChatProductRelationVO> result = new ArrayList<>();

        // 获取聊天关系列表
        List<WebChatUserRelation> chatUserRelationList = chatUserRelationMapper
                .selectList(new QueryWrapper<WebChatUserRelation>()
                        .eq("accept_uid", currentUid)
                        .orderByDesc("timestamp")
                        .isNotNull("product_id"));

        if (chatUserRelationList.isEmpty()) {
            return result;
        }

        // 获取用户信息
        Set<String> uids = chatUserRelationList.stream()
                .map(WebChatUserRelation::getSendUid)
                .collect(Collectors.toSet());
        Map<String, WebUser> userMap = userMapper.selectBatchIds(uids)
                .stream()
                .collect(Collectors.toMap(WebUser::getId, user -> user));

        // 获取商品信息
        Set<String> productIds = chatUserRelationList.stream()
                .map(WebChatUserRelation::getProductId)
                .collect(Collectors.toSet());
        Map<String, IdleProduct> productMap = productMapper.selectBatchIds(productIds)
                .stream()
                .collect(Collectors.toMap(IdleProduct::getId, product -> product));

        chatUserRelationList.forEach(item -> {
            ChatProductRelationVO chatProductRelationVO = ConvertUtils.sourceToTarget(item, ChatProductRelationVO.class);
            // 设置用户信息
            WebUser user = userMap.get(item.getSendUid());
            chatProductRelationVO.setUid(String.valueOf(user.getId()));
            chatProductRelationVO.setUsername(user.getUsername());
            chatProductRelationVO.setAvatar(user.getAvatar());

            // 设置商品信息
            IdleProduct product = productMap.get(item.getProductId());
            if (product != null) {
                chatProductRelationVO.setProductId(product.getId());
                chatProductRelationVO.setProductName(product.getTitle());
                chatProductRelationVO.setProductImage(product.getCover());
                chatProductRelationVO.setPrice(product.getPrice());
            }

            result.add(chatProductRelationVO);
        });
        return result;
    }

    /**
     * 获取所有聊天记录数量
     */
    @Override
    public CountMessage getCountMessage() {
//        String currentUid = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String currentUid = AuthContextHolder.getUserId();
        String messageCountKey = ImConstant.MESSAGE_COUNT_KEY + currentUid;
        if (Boolean.FALSE.equals(redisUtils.hasKey(messageCountKey))) {
            CountMessage countMessage = new CountMessage();
            countMessage.setFollowCount(0L);
            countMessage.setCommentCount(0L);
            countMessage.setLikeOrCollectionCount(0L);
            return countMessage;
        }
        String json = redisUtils.get(messageCountKey);
        return JSONUtil.toBean(json, CountMessage.class);
    }

    /**
     * 清除聊天数量
     *
     * @param sendUid 发送方用户ID
     * @param type    类型
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearMessageCount(String sendUid, Integer type, String productId, String groupChatId) {
//        String currentUid = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String currentUid = AuthContextHolder.getUserId();
        MessageTypeEnum messageType = MessageTypeEnum.valueOf(type);

        try {
            switch (messageType) {
                case CHAT:
                    // 如果是群聊，清除群聊未读数
                    if (groupChatId != null && !groupChatId.isEmpty()) {
                        this.clearGroupChatMessageCount(groupChatId, currentUid);
                    } else {
                        // 私聊
                        // 🔧 客服场景特殊处理：如果是管理员清除客服的未读数，需要特殊处理
                        // 因为管理员ID不是客服ID(0)，所以需要直接清除 send_uid = sendUid(用户ID), accept_uid = 0 的记录
                        if (productId == null && groupChatId == null) {
                            // 可能是客服场景，检查是否是清除用户发送给客服的未读数
                            // 直接清除 send_uid = sendUid, accept_uid = 0 的记录
                            QueryWrapper<WebChatUserRelation> customerServiceWrapper = new QueryWrapper<WebChatUserRelation>()
                                    .eq("send_uid", sendUid)
                                    .eq("accept_uid", CUSTOMER_SERVICE_ID)
                                    .isNull("product_id")
                                    .isNull("group_chat_id");
                            WebChatUserRelation customerServiceRelation = chatUserRelationMapper.selectOne(customerServiceWrapper);
                            if (customerServiceRelation != null) {
                                customerServiceRelation.setCount(0);
                                chatUserRelationMapper.updateById(customerServiceRelation);
                                log.info("清除客服未读数成功: userId={}, count=0", sendUid);
                            }
                        }
                        this.clearChatMessageCount(sendUid, currentUid, messageType, productId);
                    }
                    break;
                case PRODUCT_CHAT:
                    this.clearChatMessageCount(sendUid, currentUid, messageType, productId);
                    break;
                case LIKE:
                case COMMENT:
                case FOLLOW:
                    this.clearOtherMessageCount(sendUid, messageType);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported message type: " + type);
            }

            // 同步更新缓存
            this.updateMessageCountCache(sendUid, messageType);

        } catch (Exception e) {
            log.error("Clear message count failed: ", e);
            throw new BusinessException("清除消息计数失败");
        }
    }

    /**
     * 清除群聊未读消息数
     */
    private void clearGroupChatMessageCount(String groupChatId, String currentUid) {
        QueryWrapper<WebChatGroupMember> wrapper = new QueryWrapper<WebChatGroupMember>()
                .eq("group_chat_id", groupChatId)
                .eq("user_id", currentUid);

        WebChatGroupMember member = groupMemberMapper.selectOne(wrapper);
        if (member != null) {
            member.setUnreadCount(0);
            groupMemberMapper.updateById(member);
            log.info("清除群聊未读消息数成功: groupChatId=" + groupChatId + ", userId=" + currentUid);
        } else {
            log.warn("群成员不存在: groupChatId=" + groupChatId + ", userId=" + currentUid);
        }
    }

    private void clearChatMessageCount(String sendUid, String currentUid, MessageTypeEnum type, String productId) {
        QueryWrapper<WebChatUserRelation> wrapper = new QueryWrapper<WebChatUserRelation>()
                .eq("send_uid", sendUid)
                .eq("accept_uid", currentUid)
                .isNull("group_chat_id"); // 确保只查询私聊记录，排除群聊记录

        if (type == MessageTypeEnum.PRODUCT_CHAT) {
            wrapper.eq("product_id", productId);
        } else {
            wrapper.isNull("product_id");
        }

        // 使用 selectList 然后取第一条，避免 TooManyResultsException
        List<WebChatUserRelation> relations = chatUserRelationMapper.selectList(wrapper);
        WebChatUserRelation chatUserRelation = relations != null && !relations.isEmpty() ? relations.get(0) : null;
        if (chatUserRelation != null) {
            chatUserRelation.setCount(0);
            chatUserRelationMapper.updateById(chatUserRelation);

            // 更新缓存中的聊天消息计数
            String chatCountKey = ImConstant.MESSAGE_COUNT_KEY + currentUid + ":" + sendUid;
            redisUtils.set(chatCountKey, "0");
        }

        // 🔧 客服场景特殊处理：如果 currentUid 是客服ID(0)，还需要清除反向记录
        // 即 send_uid = sendUid(用户ID), accept_uid = 0 的记录
        if (CUSTOMER_SERVICE_ID.equals(currentUid)) {
            QueryWrapper<WebChatUserRelation> reverseWrapper = new QueryWrapper<WebChatUserRelation>()
                    .eq("send_uid", sendUid)
                    .eq("accept_uid", CUSTOMER_SERVICE_ID)
                    .isNull("group_chat_id");

            if (type == MessageTypeEnum.PRODUCT_CHAT) {
                reverseWrapper.eq("product_id", productId);
            } else {
                reverseWrapper.isNull("product_id");
            }

            List<WebChatUserRelation> reverseRelations = chatUserRelationMapper.selectList(reverseWrapper);
            WebChatUserRelation reverseRelation = reverseRelations != null && !reverseRelations.isEmpty() ? reverseRelations.get(0) : null;
            if (reverseRelation != null) {
                reverseRelation.setCount(0);
                chatUserRelationMapper.updateById(reverseRelation);
            }
        }
    }

    private void clearOtherMessageCount(String sendUid, MessageTypeEnum type) {
        String messageCountKey = ImConstant.MESSAGE_COUNT_KEY + sendUid;
        String json = redisUtils.get(messageCountKey);
        CountMessage countMessage = json != null ?
                JSONUtil.toBean(json, CountMessage.class) : new CountMessage();

        // 更新缓存
        switch (type) {
            case LIKE:
                countMessage.setLikeOrCollectionCount(0L);
                break;
            case COMMENT:
                countMessage.setCommentCount(0L);
                break;
            case FOLLOW:
                countMessage.setFollowCount(0L);
                break;
        }

        // 持久化到数据库
        this.saveMessageCountToDb(sendUid, type);

        // 更新Redis缓存
        redisUtils.set(messageCountKey, JSONUtil.toJsonStr(countMessage));
    }

    private void updateMessageCountCache(String sendUid, MessageTypeEnum type) {
        String messageCountKey = ImConstant.MESSAGE_COUNT_KEY + sendUid;
        String json = redisUtils.get(messageCountKey);
        if (json != null) {
            CountMessage countMessage = JSONUtil.toBean(json, CountMessage.class);
            switch (type) {
                case CHAT:
                    countMessage.setChatCount(0L);
                    break;
                case PRODUCT_CHAT:
                    countMessage.setProductChatCount(0L);
                    break;
            }
            redisUtils.set(messageCountKey, JSONUtil.toJsonStr(countMessage));
        }
    }

    // 将消息计数持久化到数据库
    private void saveMessageCountToDb(String userId, MessageTypeEnum type) {
//        // 实现消息计数的数据库持久化逻辑
//        MessageCount messageCount = new MessageCount();
//        messageCount.setUserId(userId);
//        messageCount.setType(type.value);
//        messageCount.setCount(0L);
//        messageCount.setUpdateTime(new Date());
//
//        // 使用 saveOrUpdate 更新数据库记录
//        messageCountMapper.saveOrUpdate(messageCount);
    }

    /**
     * 关闭聊天
     *
     * @param sendUid 发送方用户ID
     */
    @Override
    public boolean closeChat(String sendUid) {
        try {
            webSocketServer.onClose(sendUid);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 获取系统通知
     */
    @Override
    public SystemNotification getSystemNotification() {
        // 从Redis获取系统通知，如果不存在则返回默认通知
        String systemNotificationKey = "system:notification";
        String json = redisUtils.get(systemNotificationKey);

        if (json != null) {
            try {
                return JSONUtil.toBean(json, SystemNotification.class);
            } catch (Exception e) {
                // 如果解析失败，返回默认通知
            }
        }

        // 返回默认系统通知
        return new SystemNotification();
    }

    /**
     * 更新系统通知
     */
    @Override
    public void updateSystemNotification(SystemNotification systemNotification) {
        systemNotification.applyContentBase64IfPresent();
        // 更新Redis中的系统通知
        String systemNotificationKey = "system:notification";
        String json = JSONUtil.toJsonStr(systemNotification);
        redisUtils.set(systemNotificationKey, json);

        // 通过WebSocket推送系统通知给所有在线用户
        if (systemNotification.getEnabled()) {
            webSocketServer.sendSystemNotification(
                    systemNotification.getContent(),
                    systemNotification.getImageUrl());
        }
    }

    /**
     * 客服用户ID常量（使用0作为客服ID，因为数据库字段是整数类型）
     */
    private static final String CUSTOMER_SERVICE_ID = "0";

    /**
     * 获取所有与客服聊天的用户列表（用于admin管理端）
     */
    @Override
    public List<ChatUserRelationVo> getCustomerServiceUserList() {
        List<ChatUserRelationVo> result = new ArrayList<>();
        // 查询所有与客服（customer_service）聊天的用户
        List<WebChatUserRelation> chatUserRelationList = chatUserRelationMapper
                .selectList(new QueryWrapper<WebChatUserRelation>()
                        .and(wrapper -> wrapper
                                .eq("accept_uid", CUSTOMER_SERVICE_ID)
                                .or()
                                .eq("send_uid", CUSTOMER_SERVICE_ID)
                        )
                        .and(w -> w
                                .isNull("chat_type")
                                .or()
                                .eq("chat_type", 0)
                        )
                        .isNull("product_id")
                        .isNull("group_chat_id")
                        .orderByDesc("timestamp"));

        if (chatUserRelationList.isEmpty()) {
            return result;
        }

        // 获取所有与客服聊天的用户ID（排除客服自己）
        Set<String> uids = chatUserRelationList.stream()
                .map(item -> {
                    // 如果accept_uid是客服，则取send_uid；否则取accept_uid
                    if (CUSTOMER_SERVICE_ID.equals(item.getAcceptUid())) {
                        return item.getSendUid();
                    } else {
                        return item.getAcceptUid();
                    }
                })
                .filter(uid -> !CUSTOMER_SERVICE_ID.equals(uid))
                .collect(Collectors.toSet());

        if (uids.isEmpty()) {
            return result;
        }

        Map<String, WebUser> userMap = userMapper.selectBatchIds(uids).stream()
                .collect(Collectors.toMap(WebUser::getId, user -> user));

        // 为每个用户创建一条记录，取最新的消息
        Map<String, WebChatUserRelation> latestMessageMap = new HashMap<>();
        for (WebChatUserRelation item : chatUserRelationList) {
            String userId = CUSTOMER_SERVICE_ID.equals(item.getAcceptUid())
                    ? item.getSendUid()
                    : item.getAcceptUid();

            if (CUSTOMER_SERVICE_ID.equals(userId)) {
                continue; // 跳过客服自己
            }

            WebChatUserRelation existing = latestMessageMap.get(userId);
            if (existing == null || item.getTimestamp() > existing.getTimestamp()) {
                latestMessageMap.put(userId, item);
            }
        }

        // 构建返回结果
        for (Map.Entry<String, WebChatUserRelation> entry : latestMessageMap.entrySet()) {
            String userId = entry.getKey();
            WebChatUserRelation item = entry.getValue();
            WebUser user = userMap.get(userId);

            if (user != null) {
                ChatUserRelationVo vo = ConvertUtils.sourceToTarget(item, ChatUserRelationVo.class);
                vo.setUid(String.valueOf(user.getId()));
                vo.setUsername(user.getUsername());
                vo.setAvatar(user.getAvatar());

                // 未读消息数计算：对于客服场景，需要统计用户发送给客服但客服未读的消息数
                // 查询条件：send_uid = 用户ID, accept_uid = 客服ID(0)
                // 查询 WebChatUserRelation 表中 send_uid = 用户ID, accept_uid = 0 的记录的 count 字段
                QueryWrapper<WebChatUserRelation> countWrapper = new QueryWrapper<WebChatUserRelation>()
                        .eq("send_uid", userId)
                        .eq("accept_uid", CUSTOMER_SERVICE_ID)
                        .isNull("product_id")
                        .isNull("group_chat_id");
                WebChatUserRelation userToServiceRelation = chatUserRelationMapper.selectOne(countWrapper);
                Integer unreadCount = 0;
                if (userToServiceRelation != null && userToServiceRelation.getCount() != null) {
                    unreadCount = userToServiceRelation.getCount();
                }


                vo.setCount(unreadCount);
                result.add(vo);
            }
        }

        // 按最后消息时间排序
        result.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

        return result;
    }

    /**
     * 获取客服聊天记录（用于admin管理端）
     */
    @Override
    public Page<WebChat> getCustomerServiceChatRecord(long currentPage, long pageSize, String userId) {
        QueryWrapper<WebChat> queryWrapper = new QueryWrapper<WebChat>()
                .and(wrapper -> wrapper
                        .or(e -> e
                                .eq("send_uid", userId)
                                .eq("accept_uid", CUSTOMER_SERVICE_ID)
                        )
                        .or(e -> e
                                .eq("send_uid", CUSTOMER_SERVICE_ID)
                                .eq("accept_uid", userId)
                        )
                )
                .isNull("product_id")
                .isNull("group_chat_id")
                .orderByAsc("timestamp");

        // 🔧 当客服查看消息时，自动清除该用户的未读数
        QueryWrapper<WebChatUserRelation> countWrapper = new QueryWrapper<WebChatUserRelation>()
                .eq("send_uid", userId)
                .eq("accept_uid", CUSTOMER_SERVICE_ID)
                .isNull("product_id")
                .isNull("group_chat_id");
        WebChatUserRelation userToServiceRelation = chatUserRelationMapper.selectOne(countWrapper);
        if (userToServiceRelation != null && userToServiceRelation.getCount() != null && userToServiceRelation.getCount() > 0) {
            userToServiceRelation.setCount(0);
            chatUserRelationMapper.updateById(userToServiceRelation);
            log.info("客服查看消息，自动清除未读数: userId={}, count=0", userId);
        }

        return this.page(new Page<>((int) currentPage, (int) pageSize), queryWrapper);
    }

    /**
     * 客服发送消息（用于admin管理端）
     */
    @Override
    public void sendCustomerServiceMessage(Message message) {
        // 设置发送者为客服
        message.setSendUid(CUSTOMER_SERVICE_ID);
        // 调用原有的发送消息逻辑
        sendMsg(message);
    }
}
