package com.hongshu.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.domain.command.AssistantTypeCommand;
import com.hongshu.ai.domain.entity.AssistantType;
import com.hongshu.ai.domain.vo.AssistantTypeVO;
import com.hongshu.common.core.enums.ResponseInfo;

import java.util.List;

/**
 * 助手分类 服务类
 *
 * @author: myj
 * @date: 2023-11-22
 * @version: 1.0.0
 */
public interface IAssistantTypeService extends IService<AssistantType> {

    /**
     * 查询助手分类分页列表
     *
     * @param query 查询条件
     * @return 助手分类集合
     */
    ResponseInfo<IPageInfo<AssistantTypeVO>> pageAssistantType(Query query);

    /**
     * 查询助手分类列表
     *
     * @param query 查询条件
     * @return 助手分类集合
     */
    ResponseInfo<List<AssistantTypeVO>> listAssistantType(Query query);

    /**
     * 根据主键查询助手分类
     *
     * @param id 助手分类主键
     * @return 助手分类
     */
    ResponseInfo<AssistantTypeVO> getAssistantTypeById(Long id);

    /**
     * 新增助手分类
     *
     * @param command 助手分类
     * @return 结果
     */
    ResponseInfo saveAssistantType(AssistantTypeCommand command);

    /**
     * 修改助手分类
     *
     * @param command 助手分类
     * @return 结果
     */
    ResponseInfo updateAssistantType(AssistantTypeCommand command);

    /**
     * 批量删除助手分类
     *
     * @param ids 需要删除的助手分类主键集合
     * @return 结果
     */
    ResponseInfo removeAssistantTypeByIds(List<Long> ids);

    /**
     * 删除助手分类信息
     *
     * @param id 助手分类主键
     * @return 结果
     */
    ResponseInfo removeAssistantTypeById(Long id);

}
