package com.hongshu.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.domain.entity.UploadFile;
import com.hongshu.ai.domain.vo.UploadFileVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件管理 Mapper 接口
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
public interface UploadFileMapper extends BaseMapper<UploadFile> {

    /**
     * 分页查询文件管理列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return
     */
    IPage<UploadFileVO> pageFile(IPage page, @Param("q") Query query);

    /**
     * 查询文件管理列表
     *
     * @param query 查询条件
     * @return
     */
    List<UploadFileVO> listFile(@Param("q") Query query);

    /**
     * 查询文件管理
     *
     * @param query 查询条件
     * @return
     */
    UploadFileVO getFile(@Param("q") Query query);

}
