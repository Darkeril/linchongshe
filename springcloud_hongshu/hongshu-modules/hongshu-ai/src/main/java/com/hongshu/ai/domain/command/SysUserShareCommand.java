package com.hongshu.ai.domain.command;

import com.hongshu.ai.common.CommonCommand;
import lombok.Data;

import java.io.Serializable;

/**
 * 账户分销
 *
 * @author: myj
 * @date: 2024/4/1
 * @version: 1.0.0
 */
@Data
public class SysUserShareCommand extends CommonCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Long id;

    /**
     * 密码
     */
    private String password;

    /**
     * 新账号
     */
    private String account;

    /**
     * 分享额度
     */
    private Integer limit;

}
