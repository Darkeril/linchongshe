package com.hongshu.web.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 待实现功能VO
 *
 * @author: hongshu
 */
@Data
@Accessors(chain = true)
public class PlannedFeatureVO implements Serializable {

    private Long id;

    private String title;

    private String description;

    private String status;
}

