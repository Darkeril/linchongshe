package com.hongshu.web.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hongshu.web.domain.HongshuBaseEntity;
import lombok.Data;

/**
 * 产品
 *
 
 */
@Data
@TableName("idle_product")
public class IdleProduct extends HongshuBaseEntity {

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 购买者用户ID
     */
    private String buyUid;

    /**
     * 父级ID
     */
    private String cpid;

    /**
     * 二级分类ID
     */
    private String cid;

    /**
     * 发布者
     */
    private String author;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 封面图片
     */
    private String cover;

    /**
     * 图片集合
     */
    private String urls;

    /**
     * 图片数量
     */
    private Integer count;

    /**
     * 价格
     */
    private String price;

    /**
     * 原始价格
     */
    private String originalPrice;

    /**
     * 类型（上新、卖出、买到）
     */
    private Integer type;

    /**
     * 商品内容类型（图片、视频、动图）
     */
    private Integer productType;

    /**
     * 发货方式(0邮寄 1自提)
     */
    private Integer postType;

    /**
     * 想要的数量
     */
    private Long likeCount;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 是否置顶
     */
    private String pinned;

    /**
     * 审核状态
     */
    private String auditStatus;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区
     */
    private String district;

    /**
     * 位置
     */
    private String address;

    /**
     * 删除标识（0：正常 1：删除）
     */
    private Integer deleted;
}
