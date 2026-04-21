package com.hongshu.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.domain.entity.GptLoginLog;
import com.hongshu.ai.domain.vo.LoginLogVO;
import org.apache.ibatis.annotations.Param;

/**
 * 登录日志 Mapper 接口
 *
 * @author: myj
 * @date: 2024/4/1
 * @version: 1.0.0
 */
public interface GptLoginLogMapper extends BaseMapper<GptLoginLog> {

    /**
     * 禁用登录信息
     *
     * @param gptUserId 用户id
     */
    void disableLogin(@Param("gptUserId") Long gptUserId);

    /**
     * 禁用登录信息
     *
     * @param gptUserId 用户id
     * @param sessionId 当前会话标识
     */
    void disableLoginBySession(@Param("gptUserId") Long gptUserId, @Param("sessionId") String sessionId);

    /**
     * 禁用登录信息
     *
     * @param id 登录id
     */
    void disableLoginById(@Param("id") Long id);

    /**
     * 清空登录日志
     */
    void cleanLoginLog();

    /**
     * 获取登录日志分页信息
     */
    IPage<LoginLogVO> pageLoginLogVO(IPage<LoginLogVO> page, @Param("q") Query query);
}
