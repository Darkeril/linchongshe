package com.hongshu.web.mapper.web;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hongshu.web.domain.entity.WebFollower;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

/**
 * @author: hongshu
 */
@Mapper
public interface WebFollowerMapper extends BaseMapper<WebFollower> {

    Long countByFidAndCreateTimeRange(@Param("fid") String fid,
                                      @Param("startTime") Date startTime,
                                      @Param("endTime") Date endTime);
}
