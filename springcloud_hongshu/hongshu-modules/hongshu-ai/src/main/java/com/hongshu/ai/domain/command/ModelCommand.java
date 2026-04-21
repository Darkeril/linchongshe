package com.hongshu.ai.domain.command;

import com.hongshu.ai.common.CommonCommand;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 大模型信息对象 Command
 *
 * @author: myj
 * @date: 2023-12-01
 * @version: 1.0.0
 */
@Data
public class ModelCommand extends CommonCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 模型名称
     */
    @NotBlank(message = "模型名称不能为空")
    private String name;

    /**
     * 模型logo
     */
    private String icon;

    /**
     * 模型编码（业务/API 使用）
     */
    @NotBlank(message = "模型编码不能为空")
    private String model;

    /**
     * 模型版本
     */
    private String version;

    /**
     * 状态 0 禁用 1 启用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

}
