package com.hongshu.web.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通义千问内容生成响应DTO
 *
 * @author hongshu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TongYiContentResponseDTO {

    /**
     * 生成的标题列表
     */
    private List<String> titles;

    /**
     * 生成的正文内容
     */
    private String content;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 请求ID
     */
    private String requestId;
}

