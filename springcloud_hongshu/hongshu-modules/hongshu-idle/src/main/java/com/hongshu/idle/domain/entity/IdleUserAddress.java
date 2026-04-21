package com.hongshu.idle.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hongshu.idle.domain.HongshuBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("idle_user_address")
public class IdleUserAddress extends HongshuBaseEntity {

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private String uid;

    /**
     * 姓名
     */
    @Schema(description = "姓名")
    private String name;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 省份
     */
    @Schema(description = "省份")
    private String province;

    /**
     * 城市
     */
    @Schema(description = "城市")
    private String city;

    /**
     * 区县
     */
    @Schema(description = "区县")
    private String district;

    /**
     * 详细地址
     */
    @Schema(description = "详细地址")
    private String address;

}
