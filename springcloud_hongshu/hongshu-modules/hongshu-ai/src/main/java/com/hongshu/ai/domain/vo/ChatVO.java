package com.hongshu.ai.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天摘要对象 VO
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 聊天编号
     */
    private String chatNumber;

    /**
     * 角色id
     */
    private Long assistantId;
    private String assistantTitle;

    /**
     * 提示词
     */
    private String prompt;

    /**
     * 会员id
     */
    private Long userId;

    /**
     * 会员昵称
     */
    private String userName;

    /**
     * 聊天摘要
     */
    private String title;

    /**
     * 使用的模型名称（从消息记录中获取）
     */
    private String model;

    /**
     * 使用的模型版本（从消息记录中获取）
     */
    private String modelVersion;

    /**
     * 模型logo图标（从gpt_model表获取）
     */
    private String modelIcon;

}
