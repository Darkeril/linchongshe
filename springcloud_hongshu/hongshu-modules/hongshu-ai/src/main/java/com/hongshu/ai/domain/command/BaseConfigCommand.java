package com.hongshu.ai.domain.command;

import com.hongshu.ai.common.CommonCommand;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 基础配置对象 Command
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@Data
public class BaseConfigCommand extends CommonCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 配置键值
     */
    @NotBlank(message = "配置键值不能为空")
    private String name;

    /**
     * 配置内容
     */
    private String data;

}
