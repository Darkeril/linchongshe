package com.hongshu.system.mapper;

import java.util.Date;
import java.util.List;

import com.hongshu.common.core.domain.LoginLocationStatVO;
import com.hongshu.system.api.domain.SysLogininfor;

import org.apache.ibatis.annotations.Param;

/**
 * 系统访问日志情况信息 数据层
 *

 */
public interface SysLogininforMapper
{
    /**
     * 新增系统登录日志
     *
     * @param logininfor 访问日志对象
     */
    public int insertLogininfor(SysLogininfor logininfor);

    /**
     * 查询系统登录日志集合
     *
     * @param logininfor 访问日志对象
     * @return 登录记录集合
     */
    public List<SysLogininfor> selectLogininforList(SysLogininfor logininfor);

    /**
     * 批量删除系统登录日志
     *
     * @param infoIds 需要删除的登录日志ID
     * @return 结果
     */
    public int deleteLogininforByIds(Long[] infoIds);

    /**
     * 清空系统登录日志
     *
     * @return 结果
     */
    public int cleanLogininfor();

    /**
     * 管理端登录日志：近若干天按登录地点聚合 TopN（成功 status=0）
     */
    List<LoginLocationStatVO> selectAdminLoginLocationStats(@Param("startTime") Date startTime,
                                                          @Param("limit") int limit);
}
