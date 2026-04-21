package com.hongshu.system.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.hongshu.common.core.domain.LoginLocationStatVO;
import com.hongshu.system.service.ISysLogininforService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hongshu.system.api.domain.SysLogininfor;
import com.hongshu.system.mapper.SysLogininforMapper;

/**
 * 系统访问日志情况信息 服务层处理
 *

 */
@Service
public class SysLogininforServiceImpl implements ISysLogininforService
{

    @Autowired
    private SysLogininforMapper logininforMapper;

    /**
     * 新增系统登录日志
     *
     * @param logininfor 访问日志对象
     */
    @Override
    public int insertLogininfor(SysLogininfor logininfor)
    {
        return logininforMapper.insertLogininfor(logininfor);
    }

    /**
     * 查询系统登录日志集合
     *
     * @param logininfor 访问日志对象
     * @return 登录记录集合
     */
    @Override
    public List<SysLogininfor> selectLogininforList(SysLogininfor logininfor)
    {
        return logininforMapper.selectLogininforList(logininfor);
    }

    /**
     * 批量删除系统登录日志
     *
     * @param infoIds 需要删除的登录日志ID
     * @return 结果
     */
    @Override
    public int deleteLogininforByIds(Long[] infoIds)
    {
        return logininforMapper.deleteLogininforByIds(infoIds);
    }

    /**
     * 清空系统登录日志
     */
    @Override
    public void cleanLogininfor()
    {
        logininforMapper.cleanLogininfor();
    }

    @Override
    public List<LoginLocationStatVO> selectLoginLocationStats(int days, int limit)
    {
        if (days < 1)
        {
            days = 7;
        }
        if (days > 90)
        {
            days = 90;
        }
        if (limit < 1)
        {
            limit = 8;
        }
        if (limit > 30)
        {
            limit = 30;
        }
        Date start = Date.from(LocalDate.now()
                .minusDays((long) days - 1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());
        List<LoginLocationStatVO> list = logininforMapper.selectAdminLoginLocationStats(start, limit);
        return list != null ? list : Collections.emptyList();
    }
}
