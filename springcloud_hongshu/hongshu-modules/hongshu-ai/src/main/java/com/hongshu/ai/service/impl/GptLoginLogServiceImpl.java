package com.hongshu.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.constant.RedisConstants;
import com.hongshu.ai.common.enums.StatusEnum;
import com.hongshu.ai.common.security.JwtTokenUtils;
import com.hongshu.ai.common.utils.AddressUtil;
import com.hongshu.ai.common.utils.RedisUtilss;
import com.hongshu.ai.domain.entity.GptLoginLog;
import com.hongshu.ai.domain.vo.LoginLogVO;
import com.hongshu.ai.mapper.GptLoginLogMapper;
import com.hongshu.ai.service.IGptLoginLogService;
import com.hongshu.common.core.constant.StringPoolConstant;
import com.hongshu.common.core.enums.ResponseInfo;
import com.hongshu.common.core.utils.ApplicationContextUtil;
import com.hongshu.common.core.utils.DateUtil;
import com.hongshu.common.core.utils.DozerUtil;
import com.hongshu.common.core.utils.ip.IPUtil;
import com.hongshu.common.core.validator.ValidatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志 服务实现类
 *
 * @author: myj
 * @date: 2024/4/1
 * @version: 1.0.0
 */
@Service
public class GptLoginLogServiceImpl extends ServiceImpl<GptLoginLogMapper, GptLoginLog> implements IGptLoginLogService {

    @Autowired
    private GptLoginLogMapper gptloginLogMapper;
    @Autowired
    private RedisUtilss redisUtil;


    /**
     * 获取筛选条件
     *
     * @param query
     * @return
     */
    private QueryWrapper<GptLoginLog> getQw(Query query) {
        QueryWrapper<GptLoginLog> qw = new QueryWrapper<>();
        qw.lambda().like(ValidatorUtil.isNotNull(query.get("address")), GptLoginLog::getAddress, query.get("address"));
        qw.lambda().like(ValidatorUtil.isNotNull(query.get("username")), GptLoginLog::getUsername, query.get("username"));
        qw.lambda().like(ValidatorUtil.isNotNull(query.getStatus()), GptLoginLog::getStatus, query.getStatus());
        qw.lambda().ge(ValidatorUtil.isNotNull(query.getStartDate()), GptLoginLog::getCreateTime, query.getStartDate());
        qw.lambda().le(ValidatorUtil.isNotNull(query.getEndDate()), GptLoginLog::getCreateTime, query.getEndDate());
        // 获取在线用户信息
        if (ValidatorUtil.isNotNull(query.get("online"))) {
            qw.lambda().eq(GptLoginLog::getStatus, StatusEnum.ENABLED.getValue());
            qw.lambda().eq(GptLoginLog::getEnabled, StatusEnum.ENABLED.getValue());
            qw.lambda().ge(GptLoginLog::getExpireTime, DateUtil.getCurrentDateTime());

        }
        qw.lambda().orderByDesc(GptLoginLog::getId);
        return qw;
    }

    /**
     * 获取登录日志分页信息
     * 在线用户列表
     *
     * @param query 查询条件
     * @return
     */
    @Override
    public ResponseInfo<IPageInfo<LoginLogVO>> pageLoginLog(Query query) {
//        IPage<LoginLog> iPage = loginLogMapper.selectPage(new Page<LoginLog>(query.getCurrent(), query.getSize()), getQw(query));
        IPage<LoginLogVO> iPage = gptloginLogMapper.pageLoginLogVO(new Page<LoginLogVO>(query.getCurrent(), query.getSize()), query);
        return ResponseInfo.success(IPageInfo.getIPage(iPage.getCurrent(), iPage.getSize(), iPage.getTotal(), iPage.getRecords(), LoginLogVO.class));
    }

    /**
     * 获取登录日志列表
     *
     * @param query 查询条件
     * @return
     */
    @Override
    public ResponseInfo<List<LoginLogVO>> listLoginLog(Query query) {
        return ResponseInfo.success(DozerUtil.convertor(gptloginLogMapper.selectList(this.getQw(query)), LoginLogVO.class));
    }

    /**
     * 添加登录日志
     *
     * @param log 登录日志信息
     * @return
     */
    @Override
    public ResponseInfo saveLoginLog(GptLoginLog log) {
        gptloginLogMapper.insert(log);
        return ResponseInfo.success();
    }

    /**
     * 添加登录日志
     *
     * @param gptUserId     用户id
     * @param sessionId     会话标识
     * @param username      用户名
     * @param status        状态
     * @param authorization 身份标识
     * @param msg           登录信息
     * @return
     */
    @Override
    public ResponseInfo saveLoginLog(Long gptUserId, String sessionId, String username, Integer status, String authorization, String msg) {
        HttpServletRequest request = ApplicationContextUtil.getRequest();
        String userAgentStr = request.getHeader(Header.USER_AGENT.getValue());
        final UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
        final String ip = IPUtil.getIpAddr(request);
        final String addresses = AddressUtil.getAddresses(ip, "utf-8");
        String urlStr = request.getRequestURL().toString();
        String domain = StrUtil.removeSuffix(urlStr, URLUtil.url(urlStr).getPath());
        GptLoginLog log = GptLoginLog.builder()
                .expireTime(LocalDateTime.now().plusSeconds(JwtTokenUtils.EXPIRE_TIME / 1000L))
                .gptUserId(gptUserId)
                .sessionId(sessionId)
                .username(username)
                .ip(ip)
                .address(addresses)
                .domain(domain)
                .browser(userAgent.getBrowser().getName())
                .os(userAgent.getPlatform().getName())
                .status(status)
                .authorization(authorization)
                .userAgent(userAgentStr)
                .msg(msg)
                .build();
        return saveLoginLog(log);
    }

    /**
     * 根据id批量删除某条登录日志
     *
     * @param ids 日志id数组
     * @return
     */
    @Override
    public ResponseInfo removeLoginLogByIds(List<Long> ids) {
        gptloginLogMapper.deleteBatchIds(ids);
        return ResponseInfo.success();
    }

    /**
     * 清空登录日志
     *
     * @return
     */
    @Override
    public ResponseInfo cleanLoginLog() {
        gptloginLogMapper.cleanLoginLog();
        return ResponseInfo.success();
    }

    /**
     * 强退用户
     *
     * @param ids 登录信息id
     * @return
     */
    @Override
    public ResponseInfo forceLogout(List<Long> ids) {
        List<GptLoginLog> loginLogs = gptloginLogMapper.selectBatchIds(ids);
        loginLogs.stream().forEach(v -> {
            String key = RedisConstants.LOGIN_TOKEN_KEY + v.getGptUserId() + StringPoolConstant.COLON + v.getSessionId();
            redisUtil.del(key);
            gptloginLogMapper.disableLoginById(v.getId());
        });
        return ResponseInfo.success();
    }

    /**
     * 禁用登录信息
     *
     * @param gptUserId 用户id
     * @return
     */
    @Override
    public ResponseInfo disableLogin(Long gptUserId) {
        gptloginLogMapper.disableLogin(gptUserId);
        return ResponseInfo.success();
    }

    /**
     * 禁用登录信息
     *
     * @param gptUserId 用户id
     * @param sessionId 当前登录会话标识
     * @return
     */
    @Override
    public ResponseInfo disableLogin(Long gptUserId, String sessionId) {
        gptloginLogMapper.disableLoginBySession(gptUserId, sessionId);
        return ResponseInfo.success();
    }

}
