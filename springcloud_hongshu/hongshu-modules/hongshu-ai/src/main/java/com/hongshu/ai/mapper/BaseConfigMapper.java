package com.hongshu.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.domain.entity.BaseConfig;
import com.hongshu.ai.domain.vo.BaseConfigVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 基础配置 Mapper 接口
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
public interface BaseConfigMapper extends BaseMapper<BaseConfig> {

    /**
     * 分页查询基础配置列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return
     */
    IPage<BaseConfigVO> pageBaseConfig(IPage page, @Param("q") Query query);

    /**
     * 查询基础配置列表
     *
     * @param query 查询条件
     * @return
     */
    List<BaseConfigVO> listBaseConfig(@Param("q") Query query);

    /**
     * 查询基础配置
     *
     * @param query 查询条件
     * @return
     */
    BaseConfigVO getBaseConfig(@Param("q") Query query);

}
