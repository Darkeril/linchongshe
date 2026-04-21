package com.hongshu.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.domain.command.OpenkeyCommand;
import com.hongshu.ai.domain.entity.Openkey;
import com.hongshu.ai.domain.vo.OpenkeyVO;
import com.hongshu.common.core.enums.ResponseInfo;

import java.util.List;

/**
 * openai token 服务类
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
public interface IOpenkeyService extends IService<Openkey> {

    /**
     * 查询openai token分页列表
     *
     * @param query 查询条件
     * @return openai token集合
     */
    ResponseInfo<IPageInfo<OpenkeyVO>> pageOpenkey(Query query);

    /**
     * 查询openai token列表
     *
     * @param query 查询条件
     * @return openai token集合
     */
    ResponseInfo<List<OpenkeyVO>> listOpenkey(Query query);

    /**
     * 根据主键查询openai token
     *
     * @param id openai token主键
     * @return openai token
     */
    ResponseInfo<OpenkeyVO> getOpenkeyById(Long id);

    /**
     * 新增openai token
     *
     * @param command openai token
     * @return 结果
     */
    ResponseInfo saveOpenkey(OpenkeyCommand command);

    /**
     * 修改openai token
     *
     * @param command openai token
     * @return 结果
     */
    ResponseInfo updateOpenkey(OpenkeyCommand command);

    /**
     * 批量删除openai token
     *
     * @param ids 需要删除的openai token主键集合
     * @return 结果
     */
    ResponseInfo removeOpenkeyByIds(List<Long> ids);

    /**
     * 删除openai token信息
     *
     * @param id openai token主键
     * @return 结果
     */
    ResponseInfo removeOpenkeyById(Long id);

}
