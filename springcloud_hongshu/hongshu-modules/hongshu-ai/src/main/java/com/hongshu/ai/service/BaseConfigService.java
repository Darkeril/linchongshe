package com.hongshu.ai.service;

/**
 * 配置服务
 *
 * @author: Yang
 * @date: 2024/5/29
 * @version: 1.0.0
 */
public interface BaseConfigService {

    /**
     * 根据配置名称获取配置信息
     *
     * @param name 配置名称
     * @return
     */
    <T> T getBaseConfigByName(String name, Class<T> tClass);

}
