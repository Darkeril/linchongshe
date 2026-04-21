package com.hongshu.ai.domain.command;

import com.hongshu.ai.common.CommonCommand;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 兑换码对象 Command
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@Data
public class RedemptionCommand extends CommonCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 兑换码
     */
    private String code;

    /**
     * 可兑电量
     */
    private Long num;

    /**
     * 兑换人ID
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
     * 状态
     */
    private Integer status;

}
