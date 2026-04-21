package com.hongshu.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.hongshu.common.core.constant.Constants;
import com.hongshu.common.core.constant.SecurityConstants;
import com.hongshu.common.core.utils.StringUtils;
import com.hongshu.common.core.utils.ip.AddressUtils;
import com.hongshu.common.core.utils.ip.IpUtils;
import com.hongshu.system.api.RemoteLogService;
import com.hongshu.system.api.domain.SysLogininfor;

/**
 * 记录日志方法
 *

 */
@Component
public class SysRecordLogService
{
    private static final Logger log = LoggerFactory.getLogger(SysRecordLogService.class);

    @Autowired
    private RemoteLogService remoteLogService;

    /**
     * 记录登录信息
     *
     * @param username 用户名
     * @param status 状态
     * @param message 消息内容
     * @return
     */
    public void recordLogininfor(String username, String status, String message)
    {
        SysLogininfor logininfor = new SysLogininfor();
        logininfor.setUserName(username);
        String ip = IpUtils.getIpAddr();
        logininfor.setIpaddr(ip);
        // 获取IP地址对应的地理位置
        String location = AddressUtils.getRealAddressByIP(ip);
        if (StringUtils.isBlank(location)) {
            location = "未知";
        }
        logininfor.setLoginLocation(location);
        log.debug("记录登录日志 - 用户名: {}, IP: {}, 地点: {}", username, ip, location);
        logininfor.setMsg(message);
        // 日志状态
        if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER))
        {
            logininfor.setStatus(Constants.LOGIN_SUCCESS_STATUS);
        }
        else if (Constants.LOGIN_FAIL.equals(status))
        {
            logininfor.setStatus(Constants.LOGIN_FAIL_STATUS);
        }
        remoteLogService.saveLogininfor(logininfor, SecurityConstants.INNER);
    }
}
