package com.hongshu.web.service.web;

/**
 * AI客服服务接口
 * 用于处理AI自动回复功能
 *
 * @author hongshu
 */
public interface IAICustomerServiceService {

    /**
     * 检查AI客服是否启用
     *
     * @return true-已启用，false-未启用
     */
    boolean isAICustomerServiceEnabled();

    /**
     * 使用AI自动回复用户消息
     *
     * @param userId 用户ID
     * @param userMessage 用户发送的消息内容
     * @param chatHistory 历史聊天记录（用于上下文理解）
     * @return AI回复的内容
     */
    String generateAIResponse(String userId, String userMessage, java.util.List<String> chatHistory);

    /**
     * 使用AI自动回复用户消息（支持多模态：图片和视频）
     *
     * @param userId 用户ID
     * @param userMessage 用户发送的消息内容（可为空，如果只有图片/视频）
     * @param imageUrl 图片URL（可为空）
     * @param videoUrl 视频URL（可为空）
     * @param chatHistory 历史聊天记录（用于上下文理解）
     * @return AI回复的内容
     */
    String generateAIResponseWithMedia(String userId, String userMessage, String imageUrl, String videoUrl, java.util.List<String> chatHistory);

    /**
     * 更新AI客服开关状态
     *
     * @param enabled 是否启用
     */
    void updateAICustomerServiceStatus(boolean enabled);
}

