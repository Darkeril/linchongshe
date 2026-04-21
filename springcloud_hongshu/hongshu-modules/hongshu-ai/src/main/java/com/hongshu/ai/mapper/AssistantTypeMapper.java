package com.hongshu.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.domain.entity.AssistantType;
import com.hongshu.ai.domain.vo.AssistantTypeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 助手分类 Mapper 接口
 *
 * @author: myj
 * @date: 2023-11-22
 * @version: 1.0.0
 */
public interface AssistantTypeMapper extends BaseMapper<AssistantType> {

    /**
     * 分页查询助手分类列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return
     */
    IPage<AssistantTypeVO> pageAssistantType(IPage page, @Param("q") Query query);

    /**
     * 查询助手分类列表
     *
     * @param query 查询条件
     * @return
     */
    List<AssistantTypeVO> listAssistantType(@Param("q") Query query);

    /**
     * 查询助手分类
     *
     * @param query 查询条件
     * @return
     */
    AssistantTypeVO getAssistantType(@Param("q") Query query);

}
