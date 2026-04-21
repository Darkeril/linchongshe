//package com.hongshu.chat.service.impl;
//
//import cn.hutool.core.lang.UUID;
//import com.alibaba.dashscope.aigc.generation.Generation;
//import com.alibaba.dashscope.aigc.generation.models.QwenParam;
//import com.alibaba.dashscope.common.Message;
//import com.alibaba.dashscope.common.MessageManager;
//import com.alibaba.fastjson.JSON;
//import com.hongshu.chat.api.base.config.InitToken;
//import com.hongshu.chat.api.base.config.LocalCache;
//import com.hongshu.chat.api.base.entity.ChatData;
//import com.hongshu.chat.api.base.enums.ChatContentEnum;
//import com.hongshu.chat.api.base.enums.ChatModelEnum;
//import com.hongshu.chat.api.base.enums.ChatRoleEnum;
//import com.hongshu.chat.api.base.listener.SSEListener;
//import com.hongshu.chat.api.moonshot.MoonshotClient;
//import com.hongshu.chat.api.moonshot.constant.MoonshotApiVersion;
//import com.hongshu.chat.api.moonshot.entity.ChatCompletionMessage;
//import com.hongshu.chat.api.openai.OpenAiStreamClient;
//import com.hongshu.chat.api.openai.entity.chat.ChatCompletion;
//import com.hongshu.chat.api.openai.entity.chat.OpenAiMessage;
//import com.hongshu.chat.api.qianwen.QianWenClient;
//import com.hongshu.chat.api.qianwen.listener.QianWenSseListener;
//import com.hongshu.chat.api.wenxin.WenXinClient;
//import com.hongshu.chat.api.wenxin.constant.ModelE;
//import com.hongshu.chat.api.wenxin.entity.ImageResponse;
//import com.hongshu.chat.api.wenxin.entity.ImagesBody;
//import com.hongshu.chat.api.wenxin.entity.MessageItem;
//import com.hongshu.chat.api.xfyun.SparkClient;
//import com.hongshu.chat.api.xfyun.constant.SparkApiVersion;
//import com.hongshu.chat.api.xfyun.entity.SparkMessage;
//import com.hongshu.chat.api.xfyun.entity.request.SparkRequest;
//import com.hongshu.chat.api.xfyun.listener.SparkSseListener;
//import com.hongshu.chat.api.zhipu.ZhiPuClient;
//import com.hongshu.chat.common.constant.RedisConstants;
//import com.hongshu.chat.common.enums.StatusEnum;
//import com.hongshu.chat.common.exception.ErrorException;
//import com.hongshu.chat.common.security.UserDetail;
//import com.hongshu.chat.common.utils.RedisUtilss;
//import com.hongshu.chat.domain.command.ChatMessageCommand;
//import com.hongshu.chat.domain.dto.ChatMessageDTO;
//import com.hongshu.chat.domain.vo.ModelVO;
//import com.hongshu.chat.domain.vo.OpenkeyVO;
//import com.hongshu.chat.common.enums.ChatStatusEnum;
//import com.hongshu.chat.service.IChatMessageService;
//import com.hongshu.chat.service.IGptService;
//import com.hongshu.chat.service.SseService;
//import com.hongshu.common.core.constant.StringPoolConstant;
//import com.hongshu.common.core.enums.ResponseInfo;
//import com.hongshu.common.core.exception.BusinessException;
//import com.hongshu.common.core.utils.ApplicationContextUtil;
//import com.hongshu.common.core.validator.ValidatorUtil;
//import com.zhipu.oapi.Constants;
//import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
//import com.zhipu.oapi.service.v4.model.ChatMessage;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.Semaphore;
//
///**
// * 描述：
// *
// * @author myj
// * @date 2023-04-08
// */
//@Slf4j
//@Service
//public class SseServiceImpl implements SseService {
//
//    private static final String[] drawingWords = {"画画", "作画", "画图", "绘画", "描绘"};
//    private static final String[] drawingInstructions = {"请画", "画一", "画个",};
//    private static OpenAiStreamClient openAiStreamClient;
//    private static WenXinClient wenXinClient;
//    private static ZhiPuClient zhiPuClient;
//    private static QianWenClient qianWenClient;
//    private static SparkClient sparkClient;
//    private static MoonshotClient moonshotClient;
//    private final IGptService gptService;
//
//    @Autowired
//    public SseServiceImpl(IGptService gptService, OpenAiStreamClient openAiStreamClient, WenXinClient wenXinClient,
//                          ZhiPuClient zhiPuClient, QianWenClient qianWenClient, SparkClient sparkClient, MoonshotClient moonshotClient) {
//        this.gptService = gptService;
//        SseServiceImpl.openAiStreamClient = openAiStreamClient;
//        SseServiceImpl.wenXinClient = wenXinClient;
//        SseServiceImpl.zhiPuClient = zhiPuClient;
//        SseServiceImpl.qianWenClient = qianWenClient;
//        SseServiceImpl.sparkClient = sparkClient;
//        SseServiceImpl.moonshotClient = moonshotClient;
//    }
//
//    @Autowired
//    private RedisUtilss redisUtil;
//    @Autowired
//    private InitToken initToken;
//
//
//    @Override
//    public SseEmitter createSse(String uid) {
//        // 默认30秒超时,设置为0L则永不超时
//        SseEmitter sseEmitter = new SseEmitter(0L);
//        // 完成后回调
//        sseEmitter.onCompletion(() -> {
//            log.info("[{}]结束连接", uid);
//            LocalCache.CACHE.remove(uid);
//        });
//        // 超时回调
//        sseEmitter.onTimeout(() -> {
//            log.info("[{}]连接超时", uid);
//        });
//        // 异常回调
//        sseEmitter.onError(
//                throwable -> {
//                    try {
//                        log.info("[{}]连接异常,{}", uid, throwable.toString());
//                        sseEmitter.send(SseEmitter.event()
//                                .id(uid)
//                                .name("发生异常！")
//                                .data(OpenAiMessage.builder().content("发生异常请重试！").build())
//                                .reconnectTime(3000));
//                        LocalCache.CACHE.put(uid, sseEmitter);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//        );
//        try {
//            sseEmitter.send(SseEmitter.event().reconnectTime(5000));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        LocalCache.CACHE.put(uid, sseEmitter);
//        log.info("[{}]创建sse连接成功！", uid);
//        return sseEmitter;
//    }
//
//    @Override
//    public void closeSse(String uid) {
//        SseEmitter sse = (SseEmitter) LocalCache.CACHE.get(uid);
//        if (sse != null) {
//            sse.complete();
//            // 移除
//            LocalCache.CACHE.remove(uid);
//        }
//    }
//
//    @Override
//    public void sseChat(UserDetail user, String conversationId, HttpServletResponse response) {
//        ChatMessageDTO chatMessage = gptService.getMessageByConverstationId(conversationId);
//        ModelVO model = gptService.getModel(chatMessage.getModel()).getData();
//        if (ValidatorUtil.isNull(model)) {
//            throw new BusinessException("模型已经不存在啦，请切换模型进行回复～");
//        }
//        if (StatusEnum.DISABLED.getValue().equals(model.getStatus())) {
//            throw new BusinessException("该模型已被禁用，请切换模型进行回复～");
//        }
//        String prompt = chatMessage.getContent();
//        String version = model.getVersion();
//        ChatModelEnum modelEnum = ChatModelEnum.getEnum(chatMessage.getModel());
//        SseEmitter sseEmitter = createSse(user.getUid());
//        if (sseEmitter == null) {
//            log.error("聊天消息推送失败uid:[{}],没有创建连接，请重试。", user.getUid());
//            throw new BusinessException("聊天消息推送失败uid:[{}],没有创建连接，请重试。~");
//        }
//        List<ChatMessageDTO> chatMessages = gptService.listMessageByConverstationId(user.getId(), conversationId);
//        if (ValidatorUtil.isNullIncludeArray(chatMessages)) {
//            throw new BusinessException("消息发送失败");
//        }
//        // ChatGPT、文心一言统一在SSEListener中处理流式返回，通义千问与讯飞星火/智谱清言单独处理
//        Boolean flag = false;
//        try {
//            switch (modelEnum) {
//                case CHAT_GPT:
//                    flag = sseByOpenAi(response, sseEmitter, chatMessages, user.getUid(), chatMessage.getChatId(), conversationId, prompt, version);
//                    break;
//                case WENXIN:
//                    flag = sseByWenXin(response, sseEmitter, chatMessages, user.getUid(), chatMessage.getChatId(), conversationId, prompt, version);
//                    break;
//                case QIANWEN:
//                    flag = sseByQianWen(response, sseEmitter, chatMessages, user.getUid(), chatMessage.getChatId(), conversationId, prompt, version);
//                    break;
//                case SPARK:
//                    flag = sseBySpark(response, sseEmitter, chatMessages, user.getUid(), chatMessage.getChatId(), conversationId, prompt, version);
//                    break;
//                case ZHIPU:
//                    flag = sseByZhiPu(response, sseEmitter, chatMessages, user.getUid(), chatMessage.getChatId(), conversationId, prompt, version);
//                    break;
//                case MOONSHOT:
//                    flag = sseByMoonshot(response, sseEmitter, chatMessages, user.getUid(), chatMessage.getChatId(), conversationId, prompt, version);
//                    break;
//                default:
//                    throw new BusinessException("未知的模型类型，功能暂未接入。");
//            }
//            Integer status = ChatStatusEnum.SUCCESS.getValue();
//            if (flag) {
//                status = ChatStatusEnum.ERROR.getValue();
//            }
//            gptService.updateMessageStatus(conversationId, status);
//        } catch (BusinessException e) {
//            flag = true;
//            throw e;
//        } catch (Exception e) {
//            e.printStackTrace();
//            flag = true;
//            throw new ErrorException();
//        } finally {
//            if (flag) {
//                gptService.restoreNum(user.getId());
//            }
//        }
//    }
//
//    /**
//     * ChatGPT流式输出
//     *
//     * @param uid
//     * @param prompt
//     * @return
//     */
//    @SneakyThrows
//    private Boolean sseByOpenAi(HttpServletResponse response, SseEmitter sseEmitter, List<ChatMessageDTO> chatMessages,
//                                String uid, Long chatId, String conversationId, String prompt, String version) {
//        // 验证Redis中密钥是否存在
//        this.checkRedisToken(chatId, conversationId, ChatModelEnum.CHAT_GPT.getValue(), version);
//        openAiStreamClient = initToken.openAiStreamClient();
//
//        List<com.hongshu.chat.api.openai.entity.chat.Message> messages = new ArrayList<>();
//        chatMessages.stream().forEach(v -> {
//            com.hongshu.chat.api.openai.entity.chat.Message currentMessage = com.hongshu.chat.api.openai.entity.chat.Message.builder().content(v.getContent()).role(v.getRole()).build();
//            messages.add(currentMessage);
//        });
//        SSEListener sseListener = new SSEListener(response, sseEmitter, chatId, conversationId, ChatModelEnum.CHAT_GPT.getValue(), version);
//        ChatCompletion completion = ChatCompletion.builder()
//                .messages(messages)
//                .model(ValidatorUtil.isNotNull(version) ? version : ChatCompletion.Model.GPT_3_5_TURBO_0613.getName())
//                .build();
//        openAiStreamClient.streamChatCompletion(completion, sseListener);
//        sseListener.getCountDownLatch().await();
//        return sseListener.getError();
//    }
//
//    /**
//     * 文心一言流式输出
//     *
//     * @param uid
//     * @param prompt
//     * @return
//     */
//    @SneakyThrows
//    private Boolean sseByWenXin(HttpServletResponse response, SseEmitter sseEmitter, List<ChatMessageDTO> chatMessages,
//                                String uid, Long chatId, String conversationId, String prompt, String version) {
//        // 验证Redis中密钥是否存在
//        this.checkRedisToken(chatId, conversationId, ChatModelEnum.WENXIN.getValue(), version);
//        wenXinClient = initToken.wenXinClient();
//
//        if (isDraw(prompt)) {
//            return imageByWenXin(sseEmitter, response, chatId, conversationId, prompt);
//        }
//        List<MessageItem> messages = new ArrayList<>();
//        chatMessages.stream().filter(d -> !d.getRole().equals(ChatRoleEnum.SYSTEM.getValue())).forEach(v -> {
//            messages.add(MessageItem.builder().role(v.getRole()).content(v.getContent()).build());
//        });
//        SSEListener sseListener = new SSEListener(response, sseEmitter, chatId, conversationId, ChatModelEnum.WENXIN.getValue(), version);
//        ModelE model = ValidatorUtil.isNotNull(version) ? ModelE.getEnum(version) : ModelE.ERNIE_Bot_turbo;
//        if (ValidatorUtil.isNull(model)) {
//            throw new BusinessException("文心大模型不存在，请检查模型名称。");
//        }
//        wenXinClient.streamChat(messages, sseListener, model);
//        sseListener.getCountDownLatch().await();
//        return sseListener.getError();
//    }
//
//    /**
//     * 文心一言文生图
//     *
//     * @param
//     * @param prompt
//     * @return
//     */
//    @SneakyThrows
//    private Boolean imageByWenXin(SseEmitter sseEmitter, HttpServletResponse response, Long chatId, String conversationId, String prompt) {
//        // 通知客户端返回为图片
//        ChatData chatData = ChatData.builder().id(conversationId).conversationId(conversationId)
//                .parentMessageId(conversationId)
//                .role(ChatRoleEnum.ASSISTANT.getValue()).contentType(ChatContentEnum.IMAGE.getValue()).build();
//        response.getWriter().write(JSON.toJSONString(chatData));
//        response.getWriter().flush();
//
//        // 请求生成图片 并返回base64信息
//        ImagesBody body = ImagesBody.builder().prompt(prompt).build();
//        ResponseInfo<ImageResponse> imageResponse = wenXinClient.image(body);
//        if (!imageResponse.isSuccess()) {
//            throw new BusinessException(imageResponse.getMsg());
//        }
//        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
//        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
//        response.setStatus(HttpStatus.OK.value());
//        ImageResponse image = imageResponse.getData();
//        ChatMessageCommand chatMessage = ChatMessageCommand.builder().chatId(chatId).messageId(image.getId()).parentMessageId(conversationId)
//                .model(ChatModelEnum.WENXIN.getValue()).modelVersion(ModelE.STABLE_DIFFUSION_XL.getLabel())
//                .content(JSON.toJSONString(image.getData())).contentType(ChatContentEnum.IMAGE.getValue()).role(ChatRoleEnum.ASSISTANT.getValue())
//                .status(ChatStatusEnum.SUCCESS.getValue()).usedTokens(image.getUsage().getTotalTokens())
//                .build();
//        ApplicationContextUtil.getBean(IChatMessageService.class).saveChatMessage(chatMessage);
//        chatData.setContent(image.getData());
//        response.getWriter().write("\n" + JSON.toJSONString(chatData));
//        response.getWriter().flush();
//        return false;
//    }
//
//    /**
//     * 通议千问流式输出
//     *
//     * @param uid
//     * @param prompt
//     * @return
//     */
//    @SneakyThrows
//    private Boolean sseByQianWen(HttpServletResponse response, SseEmitter sseEmitter, List<ChatMessageDTO> chatMessages,
//                                 String uid, Long chatId, String conversationId, String prompt, String version) {
//        // 验证Redis中密钥是否存在
//        this.checkRedisToken(chatId, conversationId, ChatModelEnum.QIANWEN.getValue(), version);
//        qianWenClient = initToken.qianWenClient();
//
//        MessageManager msgManager = new MessageManager(20);
//        chatMessages.forEach(v -> {
//            msgManager.add(Message.builder().role(v.getRole()).content(v.getContent()).build());
//        });
//        Generation gen = new Generation();
//        QwenParam param = QwenParam.builder().apiKey(qianWenClient.getAppKey())
//                .model(ValidatorUtil.isNotNull(version) ? version : Generation.Models.QWEN_TURBO)
//                .messages(msgManager.get())
//                .resultFormat(QwenParam.ResultFormat.MESSAGE)
//                .topP(0.8)
//                .enableSearch(true)
//                .build();
//        Semaphore semaphore = new Semaphore(0);
//        QianWenSseListener sseListener = new QianWenSseListener(response, semaphore, chatId, conversationId, version);
//        gen.streamCall(param, sseListener);
//        semaphore.acquire();
//        return sseListener.getError();
//    }
//
//    /**
//     * 讯飞星火流式输出
//     *
//     * @param uid
//     * @param prompt
//     * @return
//     */
//    @SneakyThrows
//    private Boolean sseBySpark(HttpServletResponse response, SseEmitter sseEmitter, List<ChatMessageDTO> chatMessages,
//                               String uid, Long chatId, String conversationId, String prompt, String version) {
//        // 验证Redis中密钥是否存在
//        this.checkRedisToken(chatId, conversationId, ChatModelEnum.SPARK.getValue(), version);
//        sparkClient = initToken.sparkClient();
//
//        List<SparkMessage> messages = new ArrayList<>();
//        chatMessages.forEach(v -> {
//            SparkMessage currentMessage = new SparkMessage(v.getRole(), v.getContent());
//            messages.add(currentMessage);
//        });
//        SparkRequest sparkRequest = SparkRequest.builder()
//                .messages(messages)
//                // 模型回答的tokens的最大长度,非必传,取值为[1,4096],默认为2048
//                .maxTokens(2048)
//                // 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
//                .temperature(0.5)
//                // 指定请求版本，默认使用最新2.0版本
//                .apiVersion(ValidatorUtil.isNotNull(version) ? SparkApiVersion.getEnum(version) : SparkApiVersion.V2_0)
//                .chatId(chatId.toString())
//                .build();
//        sparkRequest.getHeader().setAppId(sparkClient.appid);
//        SparkSseListener sseListener = new SparkSseListener(sparkRequest, response, chatId, conversationId, version);
//        sparkClient.streamChat(sparkRequest, sseListener);
//        sseListener.getCountDownLatch().await();
//        return sseListener.getError();
//    }
//
//    /**
//     * 智谱清言流式输出
//     *
//     * @param uid
//     * @param prompt
//     * @return
//     */
//    @SneakyThrows
//    private Boolean sseByZhiPu(HttpServletResponse response, SseEmitter sseEmitter, List<ChatMessageDTO> chatMessages,
//                               String uid, Long chatId, String conversationId, String prompt, String version) {
//        // 验证Redis中密钥是否存在
//        this.checkRedisToken(chatId, conversationId, ChatModelEnum.ZHIPU.getValue(), version);
//        zhiPuClient = initToken.zhiPuClient();
//
//        List<ChatMessage> messages = new ArrayList<>();
//        chatMessages.stream().filter(d -> !d.getRole().equals(ChatRoleEnum.SYSTEM.getValue())).forEach(v -> {
//            messages.add(new ChatMessage(v.getRole(), v.getContent()));
//        });
//        String modelVersion = ValidatorUtil.isNotNull(version) ? version : Constants.ModelChatGLM3TURBO;
//        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
//                .model(modelVersion)
//                .stream(Boolean.TRUE)
//                .messages(messages)
//                .build();
//        return SseServiceImpl.zhiPuClient.streamChat(response, chatCompletionRequest, chatId, conversationId, modelVersion);
//    }
//
//    /**
//     * 月之暗面流式输出
//     *
//     * @param uid
//     * @param prompt
//     * @return
//     */
//    @SneakyThrows
//    private Boolean sseByMoonshot(HttpServletResponse response, SseEmitter sseEmitter, List<ChatMessageDTO> chatMessages,
//                                  String uid, Long chatId, String conversationId, String prompt, String version) {
//        // 验证Redis中密钥是否存在
//        this.checkRedisToken(chatId, conversationId, ChatModelEnum.MOONSHOT.getValue(), version);
//        moonshotClient = initToken.moonshotClient();
//
//        List<ChatCompletionMessage> messages = new ArrayList<>();
//        chatMessages.stream().filter(d -> !d.getRole().equals(ChatRoleEnum.SYSTEM.getValue())).forEach(v -> {
//            messages.add(new ChatCompletionMessage(v.getRole(), v.getContent()));
//        });
//        String modelVersion = ValidatorUtil.isNotNull(version) ? version : MoonshotApiVersion.API_MODEL_8K;
//        com.hongshu.chat.api.moonshot.entity.ChatCompletionRequest chatCompletionRequest = com.hongshu.chat.api.moonshot.entity.ChatCompletionRequest.builder()
//                .model(modelVersion)
//                .messages(messages)
//                .build();
//        return moonshotClient.streamChat(response, chatCompletionRequest, chatId, conversationId, modelVersion);
//    }
//
//
//    /**
//     * 验证密钥
//     */
//    private void checkRedisToken(Long chatId, String conversationId, String chatModel, String version) {
//        OpenkeyVO openkeyVO = redisUtil.get(RedisConstants.CHAT_MODEL + StringPoolConstant.COLON + chatModel, OpenkeyVO.class);
//        if (ValidatorUtil.isNull(openkeyVO)) {
//            ChatMessageCommand chatMessage = ChatMessageCommand.builder()
//                    .chatId(chatId)
//                    .messageId(String.valueOf(UUID.fastUUID()))
//                    .parentMessageId(conversationId)
//                    .model(chatModel)
//                    .modelVersion(version)
//                    .content("未加载到密钥信息")
//                    .contentType(ChatContentEnum.TEXT.getValue())
//                    .role(ChatRoleEnum.ASSISTANT.getValue())
//                    .finishReason("noToken")
//                    .status(ChatStatusEnum.ERROR.getValue())
//                    .appKey("")
//                    .usedTokens(0L)
//                    .build();
//            ApplicationContextUtil.getBean(IChatMessageService.class).saveChatMessage(chatMessage);
//            gptService.updateMessageStatus(conversationId, ChatStatusEnum.ERROR.getValue());
//            throw new BusinessException("未加载到密钥信息");
//        }
//    }
//
//    /**
//     * 判断是否需要画画
//     *
//     * @param prompt 输入内容
//     * @return
//     */
//    private Boolean isDraw(String prompt) {
//        // 检查是否有明确的画画词汇
//        for (String word : drawingWords) {
//            if (prompt.contains(word)) {
//                return true;
//            }
//        }
//        // 检查是否有指导性的画画语句
//        for (String instruction : drawingInstructions) {
//            if (prompt.contains(instruction)) {
//                return true;
//            }
//        }
//        // 检查是否有描述画画的动作的词或其他相关名词
//        // 这里只是一个示例，实际上需要更复杂的词性标注和语境分析
//        if (prompt.contains("画") || prompt.contains("绘画")) {
//            return true;
//        }
//        return false;
//    }
//
//}
