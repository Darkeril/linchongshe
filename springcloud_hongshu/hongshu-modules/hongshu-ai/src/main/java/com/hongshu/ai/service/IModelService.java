package com.hongshu.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.ResponseInfo;
import com.hongshu.ai.domain.command.ModelCommand;
import com.hongshu.ai.domain.entity.Model;
import com.hongshu.ai.domain.vo.ModelVO;

import java.util.List;

/**
 * 大模型信息 服务类
 *
 * @author: myj
 * @date: 2023-12-01
 * @version: 1.0.0
 */
public interface IModelService extends IService<Model> {

    /**
     * 查询大模型信息分页列表
     *
     * @param query 查询条件
     * @return 大模型信息集合
     */
    ResponseInfo<IPageInfo<ModelVO>> pageModel(Query query);

    /**
     * 查询大模型信息列表
     *
     * @param query 查询条件
     * @return 大模型信息集合
     */
    ResponseInfo<List<ModelVO>> listModel(Query query);

    /**
     * 根据主键查询大模型信息
     *
     * @param id 大模型信息主键
     * @return 大模型信息
     */
    ResponseInfo<ModelVO> getModelById(Long id);

    /**
     * 新增大模型信息
     *
     * @param command 大模型信息
     * @return 结果
     */
    ResponseInfo saveModel(ModelCommand command);

    /**
     * 修改大模型信息
     *
     * @param command 大模型信息
     * @return 结果
     */
    ResponseInfo updateModel(ModelCommand command);

    /**
     * 批量删除大模型信息
     *
     * @param ids 需要删除的大模型信息主键集合
     * @return 结果
     */
    ResponseInfo removeModelByIds(List<Long> ids);

    /**
     * 删除大模型信息信息
     *
     * @param id 大模型信息主键
     * @return 结果
     */
    ResponseInfo removeModelById(Long id);

}
