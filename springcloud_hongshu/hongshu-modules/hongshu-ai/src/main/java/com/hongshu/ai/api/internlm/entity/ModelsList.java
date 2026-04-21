package com.hongshu.ai.api.internlm.entity;

import lombok.Data;

import java.util.List;

/**
 * 模型列表
 *
 * @author: Yang
 * @date: 2024/3/25
 * @version: 1.2.0
 */
@Data
public class ModelsList {

    /**
     * 模型数据
     */
    private List<Model> data;

}
