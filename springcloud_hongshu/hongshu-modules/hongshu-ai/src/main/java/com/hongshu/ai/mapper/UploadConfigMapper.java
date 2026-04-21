package com.hongshu.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.domain.entity.UploadConfig;
import com.hongshu.ai.domain.vo.UploadConfigVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 缩略图配置 Mapper 接口
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
public interface UploadConfigMapper extends BaseMapper<UploadConfig> {

    /**
     * 分页查询缩略图配置列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return
     */
    IPage<UploadConfigVO> pageUploadConfig(IPage page, @Param("q") Query query);

    /**
     * 查询缩略图配置列表
     *
     * @param query 查询条件
     * @return
     */
    List<UploadConfigVO> listUploadConfig(@Param("q") Query query);

    /**
     * 查询缩略图配置
     *
     * @param query 查询条件
     * @return
     */
    UploadConfigVO getUploadConfig(@Param("q") Query query);

}
