package com.hongshu.web.domain.vo;

import lombok.Data;

@Data
public class QrCodeConfirmVO {

    private String loginToken;
    private String userId;

}
