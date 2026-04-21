package com.hongshu.web.manager.factory;

import com.hongshu.common.core.constant.Constants;
import com.hongshu.common.core.enums.TerminalTypeEnum;
import com.hongshu.common.core.utils.LogUtils;
import com.hongshu.common.core.utils.ServletUtils;
import com.hongshu.common.core.utils.SpringUtils;
import com.hongshu.common.core.utils.StringUtils;
import com.hongshu.common.core.utils.ip.AddressUtils;
import com.hongshu.common.core.utils.ip.IpUtils;
import com.hongshu.web.domain.entity.WebLoginInfor;
import com.hongshu.web.service.sys.ISysLoginInforService;
import eu.bitwalker.useragentutils.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.TimerTask;

/**
 * 异步工厂（产生任务用）
 *
 
 */
public class AsyncFactory {

    private static final Logger web_user_logger = LoggerFactory.getLogger("web-user");

    /**
     * 记录登录信息
     *
     * @param uid      用户ID
     * @param username 用户名
     * @param status   状态
     * @param message  消息
     * @param args     列表
     * @return 任务task
     */
    public static TimerTask recordLoginInfor(String terminal, final String uid, final String username, final String status, final String message,
                                             final Object... args) {
        final UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        final String ip = IpUtils.getIpAddr();
        return new TimerTask() {
            @Override
            public void run() {
                String address = AddressUtils.getRealAddressByIP(ip);
                String s = LogUtils.getBlock(ip) +
                        address +
                        LogUtils.getBlock(username) +
                        LogUtils.getBlock(status) +
                        LogUtils.getBlock(message);
                // 打印信息到日志
                web_user_logger.info(s, args);
                // 获取客户端操作系统
                String os = userAgent.getOperatingSystem().getName();
                // 获取客户端浏览器
                String browser = userAgent.getBrowser().getName();
                // 封装对象
                WebLoginInfor loginInfor = new WebLoginInfor();
                // 登录标识
                loginInfor.setFromType(TerminalTypeEnum.fromValue(terminal).getCode());
                loginInfor.setUid(Long.valueOf(uid));
                loginInfor.setUsername(username);
                loginInfor.setIpaddr(ip);
                loginInfor.setLoginLocation(address);
                loginInfor.setBrowser(browser);
                loginInfor.setOs(os);
                loginInfor.setMsg(message);
                // 日志状态
                if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER)) {
                    loginInfor.setStatus(String.valueOf(Constants.SUCCESS));
                } else if (Constants.LOGIN_FAIL.equals(status)) {
                    loginInfor.setStatus(String.valueOf(Constants.FAIL));
                }
                loginInfor.setLoginTime(new Date());
                loginInfor.setCreateTime(new Date());
                loginInfor.setUpdateTime(new Date());
                // 插入数据
                SpringUtils.getBean(ISysLoginInforService.class).insertLoginInfor(loginInfor);
            }
        };
    }
}
