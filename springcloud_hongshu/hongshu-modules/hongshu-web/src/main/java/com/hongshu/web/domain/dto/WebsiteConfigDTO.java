package com.hongshu.web.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebsiteConfigDTO {

    private String recordNumber;

    private String logo;

    private String name;

    private String author;

    private String title;

    private String description;

    private String copyright;

    private String aiUrl;

    private String wechatQrCode;

    private String followUs;

    private String alipayRewardQrCode;

    private String wechatRewardQrCode;
}
