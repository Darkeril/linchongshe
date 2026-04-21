package com.hongshu.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.domain.entity.Redemption;
import com.hongshu.ai.domain.vo.RedemptionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 兑换码 Mapper 接口
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
public interface RedemptionMapper extends BaseMapper<Redemption> {

    /**
     * 分页查询兑换码列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return
     */
    IPage<RedemptionVO> pageRedemption(IPage page, @Param("q") Query query);

    /**
     * 查询兑换码列表
     *
     * @param query 查询条件
     * @return
     */
    List<RedemptionVO> listRedemption(@Param("q") Query query);

    /**
     * 查询兑换码
     *
     * @param query 查询条件
     * @return
     */
    RedemptionVO getRedemption(@Param("q") Query query);

}
