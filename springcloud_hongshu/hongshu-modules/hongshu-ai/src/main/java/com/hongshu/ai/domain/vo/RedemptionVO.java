package com.hongshu.ai.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 兑换码对象 VO
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@Data
public class RedemptionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 兑换码
     */
    private String code;

    /**
     * 可兑次数
     */
    private Long num;

    /**
     * 兑换人
     */
    private Long userId;

    /**
     * 兑换人
     */
    private String userName;

    /**
     * 兑换时间
     */
    private LocalDateTime receiveTime;

    /**
     * 状态 0 未兑换 1 已兑换
     */
    private Integer status;

}
