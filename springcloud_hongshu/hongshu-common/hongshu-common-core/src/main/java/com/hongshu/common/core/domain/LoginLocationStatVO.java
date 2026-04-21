package com.hongshu.common.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录日志按地点聚合（工作台统计）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginLocationStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 登录地点文案（与日志表 login_location 一致） */
    private String location;

    /** 该地点登录次数 */
    private Long loginCount;
}
