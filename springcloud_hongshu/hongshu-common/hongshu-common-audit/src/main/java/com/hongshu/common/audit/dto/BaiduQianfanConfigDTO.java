package com.hongshu.common.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 百度千帆审核配置DTO
 *
 * @author hongshu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaiduQianfanConfigDTO {

    /**
     * 是否启用百度千帆审核
     */
    private Boolean enabled;

    /**
     * 审核模式：manual(纯人工)、auto(纯自动)、hybrid(混合)
     */
    private String auditMode;

    /**
     * API Key
     */
    private String accessKey;

    /**
     * Secret Key
     */
    private String secretKey;

    /**
     * 文本审核接口地址
     */
    private String textEndpoint;

    /**
     * 图片审核接口地址
     */
    private String imageEndpoint;

    /**
     * 视频审核接口地址
     */
    private String videoEndpoint;

    /**
     * 自动审核阈值（0-1之间，超过此值将被拒绝）
     */
    private Double autoAuditThreshold;

    /**
     * 获取默认配置
     */
    public static BaiduQianfanConfigDTO getDefault() {
        return BaiduQianfanConfigDTO.builder()
                .enabled(false)
                .auditMode("manual")
                .textEndpoint("https://aip.baidubce.com/rest/2.0/solution/v1/text_censor/v2/user_defined")
                .imageEndpoint("https://aip.baidubce.com/rest/2.0/solution/v1/img_censor/v2/user_defined")
                .videoEndpoint("https://aip.baidubce.com/rest/2.0/solution/v1/video_censor/v2/user_defined")
                .autoAuditThreshold(0.8)
                .build();
    }
}
