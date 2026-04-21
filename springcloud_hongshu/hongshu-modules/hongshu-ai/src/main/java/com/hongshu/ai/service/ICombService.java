package com.hongshu.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.domain.command.CombCommand;
import com.hongshu.ai.domain.entity.Comb;
import com.hongshu.ai.domain.vo.CombVO;
import com.hongshu.common.core.enums.ResponseInfo;

import java.util.List;

/**
 * 会员套餐 服务类
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
public interface ICombService extends IService<Comb> {

    /**
     * 查询会员套餐分页列表
     *
     * @param query 查询条件
     * @return 会员套餐集合
     */
    ResponseInfo<IPageInfo<CombVO>> pageComb(Query query);

    /**
     * 查询会员套餐列表
     *
     * @param query 查询条件
     * @return 会员套餐集合
     */
    ResponseInfo<List<CombVO>> listComb(Query query);

    /**
     * 根据主键查询会员套餐
     *
     * @param id 会员套餐主键
     * @return 会员套餐
     */
    ResponseInfo<CombVO> getCombById(Long id);

    /**
     * 新增会员套餐
     *
     * @param command 会员套餐
     * @return 结果
     */
    ResponseInfo saveComb(CombCommand command);

    /**
     * 修改会员套餐
     *
     * @param command 会员套餐
     * @return 结果
     */
    ResponseInfo updateComb(CombCommand command);

    /**
     * 批量删除会员套餐
     *
     * @param ids 需要删除的会员套餐主键集合
     * @return 结果
     */
    ResponseInfo removeCombByIds(List<Long> ids);

    /**
     * 删除会员套餐信息
     *
     * @param id 会员套餐主键
     * @return 结果
     */
    ResponseInfo removeCombById(Long id);

}
