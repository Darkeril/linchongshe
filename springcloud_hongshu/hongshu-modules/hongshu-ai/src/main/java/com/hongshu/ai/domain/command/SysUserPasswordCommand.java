package com.hongshu.ai.domain.command;

import com.hongshu.ai.common.CommonCommand;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户修改密码信息
 *
 * @author: myj
 * @date: 2024/4/1
 * @version: 1.0.0
 */
@Data
public class SysUserPasswordCommand extends CommonCommand implements Serializable {

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
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

}
