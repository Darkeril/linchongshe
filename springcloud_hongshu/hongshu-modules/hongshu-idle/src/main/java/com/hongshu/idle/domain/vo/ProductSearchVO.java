package com.hongshu.idle.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * ES-商品
 *
 * @author: hongshu
 */
@Data
@Accessors(chain = true)
public class ProductSearchVO implements Serializable {

    private String id;

    private String uid;

    private String cpid;

    private String categoryParentName;

    private String cid;

    private String categoryName;

    private String title;

    private String description;

    private String cover;

    private Integer noteCoverHeight;

    private String urls;

    private String price;

    private String originalPrice;

    private String type;

    private String productType;

    private String postType;

    private String username;

    private String avatar;

    private String status;

    private String auditStatus;

    private Boolean isCollection;

    private Long likeCount;

    private String province;

    private String city;

    private String district;

    private Long viewCount;

    private Long time;

    private Boolean isLoading;
}
