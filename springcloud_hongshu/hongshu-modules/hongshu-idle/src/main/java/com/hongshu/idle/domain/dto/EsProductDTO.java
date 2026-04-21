package com.hongshu.idle.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * ES-商品
 *

 */
@Data
public class EsProductDTO implements Serializable {

    private String keyword;

    // 0是指全部 1是指点赞排序 2是指时间排序
    private Integer type;

    private String cid;

    private String cpid;
}
