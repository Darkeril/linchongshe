package com.hongshu.web.service.web.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MessageManager;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hongshu.web.domain.entity.WebSystemConfig;
import com.hongshu.web.mapper.sys.SysSystemConfigMapper;
import com.hongshu.web.service.web.IAICustomerServiceService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * AI客服服务实现类
 * 使用通义千问API进行自动回复
 *
 * @author hongshu
 */
@Slf4j
@Service
public class AICustomerServiceServiceImpl implements IAICustomerServiceService {

    @Autowired
    private SysSystemConfigMapper systemConfigMapper;

    /**
     * AI客服开关配置键
     */
    private static final String AI_CUSTOMER_SERVICE_ENABLED_KEY = "ai.customer.service.enabled";

    /**
     * 通义千问API Key配置键
     */
    private static final String TONGYI_API_KEY = "tongyi.api.key";

    /**
     * 通义千问视觉模型（支持图片和视频理解）
     */
    private static final String QWEN_VL_MODEL = "qwen-vl-max";

    /**
     * DashScope API 基础URL（兼容OpenAI格式）
     */
    private static final String DASHSCOPE_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    /**
     * HTTP客户端（用于多模态请求）
     */
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    /**
     * 客服系统提示词
     */
    private static final String SYSTEM_PROMPT = "你是一个专业、友好、耐心的在线客服助手。你的职责是：\n" +
            "1. 礼貌、热情地回答用户的问题\n" +
            "2. 提供准确、有用的信息\n" +
            "3. 如果遇到无法解决的问题，引导用户联系人工客服\n" +
            "4. 保持专业和友好的语气\n" +
            "5. 回复要简洁明了，不要过于冗长\n" +
            "请用中文回复用户的问题。";

    @Override
    public boolean isAICustomerServiceEnabled() {
        try {
            WebSystemConfig config = systemConfigMapper.selectOne(
                    new LambdaQueryWrapper<WebSystemConfig>()
                            .eq(WebSystemConfig::getConfigKey, AI_CUSTOMER_SERVICE_ENABLED_KEY)
            );
            return config != null && "true".equalsIgnoreCase(config.getConfigValue());
        } catch (Exception e) {
            log.error("检查AI客服开关状态失败", e);
            return false;
        }
    }

    @Override
    public String generateAIResponse(String userId, String userMessage, List<String> chatHistory) {
        try {
            // 获取通义千问API Key
            String apiKey = getTongYiApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                log.warn("通义千问API Key未配置，无法使用AI客服");
                return "抱歉，AI客服暂时无法使用，请稍后再试或联系人工客服。";
            }

            // 构建消息列表
            MessageManager msgManager = new MessageManager(20);
            
            // 添加系统提示词
            msgManager.add(Message.builder()
                    .role("system")
                    .content(SYSTEM_PROMPT)
                    .build());

            // 添加历史对话（如果有）
            if (chatHistory != null && !chatHistory.isEmpty()) {
                // 历史记录格式：["用户: xxx", "客服: xxx", ...]
                // 需要转换为Message格式
                for (int i = 0; i < chatHistory.size(); i++) {
                    String historyMsg = chatHistory.get(i);
                    if (historyMsg.startsWith("用户:")) {
                        msgManager.add(Message.builder()
                                .role("user")
                                .content(historyMsg.substring(3).trim())
                                .build());
                    } else if (historyMsg.startsWith("客服:")) {
                        msgManager.add(Message.builder()
                                .role("assistant")
                                .content(historyMsg.substring(3).trim())
                                .build());
                    }
                }
            }

            // 添加当前用户消息
            msgManager.add(Message.builder()
                    .role("user")
                    .content(userMessage)
                    .build());

            // 调用通义千问API
            Generation gen = new Generation();
            QwenParam param = QwenParam.builder()
                    .apiKey(apiKey)
                    .model(Generation.Models.QWEN_TURBO) // 使用通义千问Turbo模型，响应更快
                    .messages(msgManager.get())
                    .resultFormat(QwenParam.ResultFormat.MESSAGE)
                    .topP(0.8)
                    .temperature(0.7f) // 适中的温度，保持回复的创造性和准确性平衡
                    .enableSearch(true) // 启用搜索增强
                    .build();

            GenerationResult response = gen.call(param);
            List<GenerationOutput.Choice> choices = response.getOutput().getChoices();
            
            if (choices != null && !choices.isEmpty()) {
                String aiResponse = choices.get(0).getMessage().getContent();
                log.info("AI客服回复成功: userId={}, userMessage={}, aiResponse={}", userId, userMessage, aiResponse);
                return aiResponse;
            } else {
                log.warn("AI客服回复为空: userId={}, userMessage={}", userId, userMessage);
                return "抱歉，我暂时无法理解您的问题，请稍后再试或联系人工客服。";
            }

        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            log.error("AI客服调用失败: userId={}, userMessage={}, error={}", userId, userMessage, e.getMessage(), e);
            return "抱歉，AI客服暂时无法使用，请稍后再试或联系人工客服。";
        } catch (Exception e) {
            log.error("AI客服处理异常: userId={}, userMessage={}", userId, userMessage, e);
            return "抱歉，系统出现异常，请稍后再试或联系人工客服。";
        }
    }

    @Override
    public void updateAICustomerServiceStatus(boolean enabled) {
        try {
            WebSystemConfig config = systemConfigMapper.selectOne(
                    new LambdaQueryWrapper<WebSystemConfig>()
                            .eq(WebSystemConfig::getConfigKey, AI_CUSTOMER_SERVICE_ENABLED_KEY)
            );

            if (config != null) {
                // 更新现有配置
                config.setConfigValue(String.valueOf(enabled));
                config.setUpdateTime(new java.util.Date());
                systemConfigMapper.updateById(config);
            } else {
                // 创建新配置
                config = new WebSystemConfig();
                config.setConfigKey(AI_CUSTOMER_SERVICE_ENABLED_KEY);
                config.setConfigValue(String.valueOf(enabled));
                config.setConfigGroup("customer_service");
                config.setRemark("AI客服开关：true-启用，false-禁用");
                config.setIsSensitive(false);
                config.setCreateTime(new java.util.Date());
                config.setUpdateTime(new java.util.Date());
                systemConfigMapper.insert(config);
            }
            log.info("AI客服开关状态已更新: enabled={}", enabled);
        } catch (Exception e) {
            log.error("更新AI客服开关状态失败: enabled={}", enabled, e);
            throw new RuntimeException("更新AI客服开关状态失败", e);
        }
    }

    /**
     * 获取通义千问API Key
     */
    private String getTongYiApiKey() {
        try {
            WebSystemConfig config = systemConfigMapper.selectOne(
                    new LambdaQueryWrapper<WebSystemConfig>()
                            .eq(WebSystemConfig::getConfigKey, TONGYI_API_KEY)
            );

            if (config != null && config.getConfigValue() != null && !config.getConfigValue().isEmpty()) {
                return config.getConfigValue();
            }

            // 备用方案：从 gpt_openkey 表查询
            try {
                String apiKey = systemConfigMapper.selectTongYiApiKeyFromOpenkey();
                if (apiKey != null && !apiKey.isEmpty()) {
                    return apiKey;
                }
            } catch (Exception e) {
                log.debug("从 gpt_openkey 表获取API Key失败: {}", e.getMessage());
            }

            return null;
        } catch (Exception e) {
            log.error("获取通义千问API Key失败", e);
            return null;
        }
    }

    @Override
    public String generateAIResponseWithMedia(String userId, String userMessage, String imageUrl, String videoUrl, List<String> chatHistory) {
        try {
            // 获取通义千问API Key
            String apiKey = getTongYiApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                log.warn("通义千问API Key未配置，无法使用AI客服");
                return "抱歉，AI客服暂时无法使用，请稍后再试或联系人工客服。";
            }

            // 检查是否有图片或视频
            boolean hasImage = imageUrl != null && !imageUrl.isEmpty() && 
                    (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"));
            boolean hasVideo = videoUrl != null && !videoUrl.isEmpty() && 
                    (videoUrl.startsWith("http://") || videoUrl.startsWith("https://"));

            if (!hasImage && !hasVideo) {
                // 如果没有图片和视频，使用文本模式
                return generateAIResponse(userId, userMessage != null ? userMessage : "", chatHistory);
            }

            // 构建content数组（通义千问VL模型需要使用content数组格式）
            JSONArray contentArray = new JSONArray();

            // 构建用户消息文本
            StringBuilder userText = new StringBuilder();
            if (userMessage != null && !userMessage.trim().isEmpty()) {
                userText.append(userMessage);
            } else {
                // 如果没有文本，添加默认提示
                if (hasImage && hasVideo) {
                    userText.append("请查看我发送的图片和视频，并回复相关问题。");
                } else if (hasImage) {
                    userText.append("请查看我发送的图片，并回复相关问题。");
                } else if (hasVideo) {
                    userText.append("请查看我发送的视频，并回复相关问题。");
                }
            }

            // 添加文本内容
            if (userText.length() > 0) {
                JSONObject textContent = new JSONObject();
                textContent.put("type", "text");
                textContent.put("text", userText.toString());
                contentArray.add(textContent);
            }

            // 添加图片URL
            if (hasImage) {
                JSONObject imageContent = new JSONObject();
                imageContent.put("type", "image_url");
                JSONObject imageUrlObj = new JSONObject();
                imageUrlObj.put("url", imageUrl);
                imageContent.put("image_url", imageUrlObj);
                contentArray.add(imageContent);
                log.debug("AI客服添加图片URL: {}", imageUrl);
            }

            // 添加视频URL
            if (hasVideo) {
                JSONObject videoContent = new JSONObject();
                videoContent.put("type", "video_url");
                JSONObject videoUrlObj = new JSONObject();
                videoUrlObj.put("url", videoUrl);
                videoContent.put("video_url", videoUrlObj);
                contentArray.add(videoContent);
                log.debug("AI客服添加视频URL: {}", videoUrl);
            }

            // 构建消息列表
            JSONArray messages = new JSONArray();

            // 添加系统提示词
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", SYSTEM_PROMPT);
            messages.add(systemMessage);

            // 添加历史对话（如果有）
            if (chatHistory != null && !chatHistory.isEmpty()) {
                for (int i = 0; i < chatHistory.size(); i++) {
                    String historyMsg = chatHistory.get(i);
                    JSONObject historyMessage = new JSONObject();
                    if (historyMsg.startsWith("用户:")) {
                        historyMessage.put("role", "user");
                        historyMessage.put("content", historyMsg.substring(3).trim());
                    } else if (historyMsg.startsWith("客服:")) {
                        historyMessage.put("role", "assistant");
                        historyMessage.put("content", historyMsg.substring(3).trim());
                    }
                    if (historyMessage.containsKey("role")) {
                        messages.add(historyMessage);
                    }
                }
            }

            // 添加当前用户消息（包含图片/视频）
            JSONObject userMessageObj = new JSONObject();
            userMessageObj.put("role", "user");
            userMessageObj.put("content", contentArray);
            messages.add(userMessageObj);

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", QWEN_VL_MODEL);
            requestBody.put("messages", messages);

            String requestBodyStr = requestBody.toJSONString();
            log.debug("AI客服多模态请求体: {}", requestBodyStr);

            // 发送HTTP请求
            RequestBody body = RequestBody.create(requestBodyStr, MediaType.parse("application/json; charset=utf-8"));
            Request httpRequest = new Request.Builder()
                    .url(DASHSCOPE_BASE_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            log.info("调用通义千问视觉模型进行AI客服回复，模型: {}", QWEN_VL_MODEL);
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    log.error("AI客服HTTP请求失败: code={}, body={}", response.code(), errorBody);
                    return "抱歉，AI客服暂时无法使用，请稍后再试或联系人工客服。";
                }

                String responseBody = response.body().string();
                log.debug("AI客服HTTP响应: {}", responseBody);

                JSONObject responseJson = JSONObject.parseObject(responseBody);
                JSONArray choices = responseJson.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject message = firstChoice.getJSONObject("message");
                    String aiResponse = message.getString("content");
                    log.info("AI客服多模态回复成功: userId={}, hasImage={}, hasVideo={}, aiResponse={}", 
                            userId, hasImage, hasVideo, aiResponse);
                    return aiResponse;
                } else {
                    log.warn("AI客服多模态回复为空: userId={}", userId);
                    return "抱歉，我暂时无法理解您的问题，请稍后再试或联系人工客服。";
                }
            }

        } catch (IOException e) {
            log.error("AI客服多模态请求IO异常: userId={}, error={}", userId, e.getMessage(), e);
            return "抱歉，AI客服暂时无法使用，请稍后再试或联系人工客服。";
        } catch (Exception e) {
            log.error("AI客服多模态处理异常: userId={}", userId, e);
            return "抱歉，系统出现异常，请稍后再试或联系人工客服。";
        }
    }
}

