package com.hongshu.ai.domain.command;

import com.hongshu.ai.common.CommonCommand;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户修改密码信息
 *
 * @author: Yang
 * @date: 2023/01/31
 * @version: 1.0.0
 */
@Data
public class UserPasswordCommand extends CommonCommand implements Serializable {

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
