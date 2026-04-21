package com.hongshu.web.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 系统通知VO
 *
 * @author: hongshu
 * @date: 2024-01-15
 * @version: 1.0.0
 */
@Data
public class SystemNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    private String id;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 正文 UTF-8 再 Base64，避免 HTML 引号在链路中被破坏导致 JSON 无法解析；非空时解码后覆盖 content。
     */
    private String contentBase64;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 推送说明
     */
    private String description;

    /**
     * 配图地址（可选，推送时随 WebSocket 下发）
     */
    private String imageUrl;

    public SystemNotification() {
        this.content = "欢迎注册来因系统❤️";
        this.timestamp = System.currentTimeMillis();
        this.enabled = true;
    }

    public SystemNotification(String content, Long timestamp, Boolean enabled) {
        this.content = content;
        this.timestamp = timestamp;
        this.enabled = enabled;
    }

    /**
     * 若 {@link #contentBase64} 非空则解码为正文并清空该字段，避免写入 Redis 的 JSON 携带临时字段。
     */
    public void applyContentBase64IfPresent() {
        if (contentBase64 != null && !contentBase64.isEmpty()) {
            String trimmed = contentBase64.trim();
            if (!trimmed.isEmpty()) {
                byte[] decoded = Base64.getDecoder().decode(trimmed);
                this.content = new String(decoded, StandardCharsets.UTF_8);
            }
        }
        this.contentBase64 = null;
    }
}
