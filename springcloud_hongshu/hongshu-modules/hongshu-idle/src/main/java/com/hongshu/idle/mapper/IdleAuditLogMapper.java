package com.hongshu.idle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hongshu.idle.domain.entity.IdleAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * Idle模块审核日志Mapper接口
 *
 * @author hongshu
 */
@Mapper
public interface IdleAuditLogMapper extends BaseMapper<IdleAuditLog> {
}
