package com.hongshu.web.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * 地址
 *
 * @author: hongshu
 */
@Data
@AllArgsConstructor
public class LocationInfo {

    private String city;
    private Double longitude;
    private Double latitude;
}
