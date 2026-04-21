package com.hongshu.web.mapper.web;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hongshu.web.domain.entity.WebAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * Web模块审核日志Mapper接口
 *
 * @author hongshu
 */
@Mapper
public interface WebAuditLogMapper extends BaseMapper<WebAuditLog> {
}
