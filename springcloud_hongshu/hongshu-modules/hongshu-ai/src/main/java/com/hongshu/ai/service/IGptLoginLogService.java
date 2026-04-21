package com.hongshu.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.domain.entity.GptLoginLog;
import com.hongshu.ai.domain.vo.LoginLogVO;
import com.hongshu.common.core.enums.ResponseInfo;

import java.util.List;

/**
 * 登录日志 服务类
 *
 * @author: myj
 * @date: 2024/4/1
 * @version: 1.0.0
 */
public interface IGptLoginLogService extends IService<GptLoginLog> {

    /**
     * 获取登录日志分页信息
     * 在线用户列表
     *
     * @param query 查询条件
     * @return
     */
    ResponseInfo<IPageInfo<LoginLogVO>> pageLoginLog(Query query);


    /**
     * 获取登录日志列表
     *
     * @param query 查询条件
     * @return
     */
    ResponseInfo<List<LoginLogVO>> listLoginLog(Query query);


    /**
     * 添加登录日志
     *
     * @param log 登录日志信息
     * @return
     */
    ResponseInfo saveLoginLog(GptLoginLog log);

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
    ResponseInfo saveLoginLog(Long gptUserId, String sessionId, String username, Integer status, String authorization, String msg);

    /**
     * 根据id批量删除某条登录日志
     *
     * @param ids 日志id数组
     * @return
     */
    ResponseInfo removeLoginLogByIds(List<Long> ids);

    /**
     * 清空登录日志
     *
     * @return
     */
    ResponseInfo cleanLoginLog();

    /**
     * 强退用户
     *
     * @param ids 登录信息id
     * @return
     */
    ResponseInfo forceLogout(List<Long> ids);

    /**
     * 禁用登录信息
     *
     * @param gptUserId 用户id
     * @return
     */
    ResponseInfo disableLogin(Long gptUserId);

    /**
     * 禁用登录信息
     *
     * @param gptUserId 用户id
     * @param sessionId 当前登录会话标识
     * @return
     */
    ResponseInfo disableLogin(Long gptUserId, String sessionId);

}
