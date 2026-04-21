package com.hongshu.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.domain.entity.Model;
import com.hongshu.ai.domain.vo.ModelVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 大模型信息 Mapper 接口
 *
 * @author: myj
 * @date: 2023-12-01
 * @version: 1.0.0
 */
public interface ModelMapper extends BaseMapper<Model> {

    /**
     * 分页查询大模型信息列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return
     */
    IPage<ModelVO> pageModel(IPage page, @Param("q") Query query);

    /**
     * 查询大模型信息列表
     *
     * @param query 查询条件
     * @return
     */
    List<ModelVO> listModel(@Param("q") Query query);

    /**
     * 查询大模型信息
     *
     * @param query 查询条件
     * @return
     */
    ModelVO getModel(@Param("q") Query query);

}
