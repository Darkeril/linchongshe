package com.hongshu.web.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalConfigDTO {

    private String localPictureBaseUrl;

    private String fileUploadPath;

    private Boolean isEnabled;
}
