package com.hongshu.web.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 地址
 *
 * @author: hongshu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLocationVO {

    private String city;
    private Double longitude;
    private Double latitude;
    private Integer count;
}
