package com.hongshu.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.domain.command.BaseConfigCommand;
import com.hongshu.ai.domain.entity.BaseConfig;
import com.hongshu.ai.domain.vo.BaseConfigVO;
import com.hongshu.common.core.enums.ResponseInfo;

import java.util.List;

/**
 * 基础配置 服务类
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
public interface IBaseConfigService extends IService<BaseConfig> {

    /**
     * 查询基础配置分页列表
     *
     * @param query 查询条件
     * @return 基础配置集合
     */
    ResponseInfo<IPageInfo<BaseConfigVO>> pageBaseConfig(Query query);

    /**
     * 查询基础配置列表
     *
     * @param query 查询条件
     * @return 基础配置集合
     */
    ResponseInfo<List<BaseConfigVO>> listBaseConfig(Query query);

    /**
     * 根据主键查询基础配置
     *
     * @param id 基础配置主键
     * @return 基础配置
     */
    ResponseInfo<BaseConfigVO> getBaseConfigById(Long id);

    /**
     * 根据配置名称获取配置信息
     *
     * @param name 配置名称
     * @return
     */
    <T> T getBaseConfigByName(String name, Class<T> tClass);

    /**
     * 根据配置名称获取配置信息
     *
     * @param name 配置名称
     * @return
     */
    ResponseInfo test(String name);

    /**
     * 新增基础配置
     *
     * @param command 基础配置
     * @return 结果
     */
    ResponseInfo saveBaseConfig(BaseConfigCommand command);

    /**
     * 修改基础配置
     *
     * @param command 基础配置
     * @return 结果
     */
    ResponseInfo updateBaseConfig(BaseConfigCommand command);

    /**
     * 批量删除基础配置
     *
     * @param ids 需要删除的基础配置主键集合
     * @return 结果
     */
    ResponseInfo removeBaseConfigByIds(List<Long> ids);

    /**
     * 删除基础配置信息
     *
     * @param id 基础配置主键
     * @return 结果
     */
    ResponseInfo removeBaseConfigById(Long id);

}
