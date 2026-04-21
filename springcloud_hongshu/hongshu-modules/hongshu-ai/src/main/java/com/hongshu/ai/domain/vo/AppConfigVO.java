package com.hongshu.ai.domain.vo;

import com.hongshu.ai.domain.dto.config.AppInfoDTO;
import com.hongshu.ai.domain.dto.config.BaseInfoDTO;
import com.hongshu.ai.domain.dto.config.ExtraInfoDTO;
import com.hongshu.ai.domain.dto.config.WxInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * app配置信息
 *
 * @author: myj
 * @date: 2023/5/4
 * @version: 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppConfigVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 基础配置信息
     */
    private BaseInfoDTO baseInfo;

    /**
     * 额外配置信息
     */
    private ExtraInfoDTO extraInfo;

    /**
     * app信息
     */
    private AppInfoDTO appInfo;

    /**
     * 微信信息
     */
    private WxInfoDTO wxInfo;

    /**
     * 主要模型
     */
    private AssistantVO mainAssistant;

    /**
     * 所有模型
     */
    private List<AssistantVO> assistants;

}
