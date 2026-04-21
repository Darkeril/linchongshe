package com.hongshu.common.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 按 IP 聚合登录次数（工作台高频访问展示）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpLoginFrequencyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 登录 IP */
    private String ipaddr;

    /** 该 IP 在统计区间内的登录次数 */
    private Long loginCount;

    /** 代表性登录地点（同 IP 取一条非空文案） */
    private String location;
}
