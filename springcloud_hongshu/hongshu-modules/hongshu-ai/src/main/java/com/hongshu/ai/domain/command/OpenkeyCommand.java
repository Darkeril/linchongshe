package com.hongshu.ai.domain.command;

import com.hongshu.ai.common.CommonCommand;
import lombok.Data;

import java.io.Serializable;

/**
 * openai token对象 Command
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@Data
public class OpenkeyCommand extends CommonCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * appid
     */
    private String appId;

    /**
     * appKey
     */
    private String appKey;

    /**
     * app密钥
     */
    private String appSecret;


    /**
     * 总额度
     */
    private Long totalTokens;

    /**
     * 已用额度
     */
    private Long usedTokens;

    /**
     * 剩余额度
     */
    private Long surplusTokens;

    /**
     * 是否可用 0 禁用 1 启用
     */
    private Integer status;

    /**
     * 模型
     */
    private String model;

    /**
     * 备注
     */
    private String remark;

}
