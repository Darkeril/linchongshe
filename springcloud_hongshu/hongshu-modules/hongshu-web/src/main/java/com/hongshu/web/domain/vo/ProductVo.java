package com.hongshu.web.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * 商品
 *
 * @author: hongshu
 */
@Data
@Accessors(chain = true)
public class ProductVo implements Serializable {

    private String id;
    private String title;
    private String cover;
//    private BigDecimal price;
//    private BigDecimal originalPrice;
    private String price;
    private String originalPrice;
    private Integer soldCount;
}
