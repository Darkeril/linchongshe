package com.hongshu.web.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaConfigDTO {

    private String cacheType;

    private String type;

    private String waterMark;

    private Integer slipOffset;

    private Boolean aesStatus;

    private Integer interferenceOptions;

    private String jigsaw;

    private String picClick;

    private Boolean isEnabled;
}
