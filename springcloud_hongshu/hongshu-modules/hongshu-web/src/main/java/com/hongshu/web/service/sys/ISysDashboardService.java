package com.hongshu.web.service.sys;

import com.hongshu.common.core.domain.IpLoginFrequencyVO;
import com.hongshu.common.core.domain.LoginLocationStatVO;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.web.domain.vo.DailyNewDataVO;
import com.hongshu.web.domain.vo.UserLocationVO;

import java.util.List;
import java.util.Map;

/**
 * 首页RestApi 业务层
 *

 */
public interface ISysDashboardService {

    Map<String, Object> getInitData();

    Map<String, Object> getVisitByWeek();

    List<Map<String, Object>> getNoteCountByType();

    List<Map<String, Object>> getProductCountByType();

    /** @param days 仅 7 或 30 */
    Map<String, Object> getOrderData(int days);

    DailyNewDataVO getDailyNewData();

    List<Map<String, Object>> getNoteCountByCategory();

    List<Map<String, Object>> getProductCountByCategory();

    Map<String, Object> getNoteContributeCount();

    List<NoteSearchVo> getHotNote(String noteType);

    List<UserLocationVO> getUserLocations();

    /**
     * Web 端登录日志：近若干天按地点聚合 TopN
     *
     * @param days  统计天数，含今天，默认 7
     * @param limit 返回条数上限
     */
    List<LoginLocationStatVO> getWebLoginLocationStats(int days, int limit);

    /**
     * 会员移动端登录（App/小程序）按地点聚合 TopN，时间窗口与 Web 端一致
     */
    List<LoginLocationStatVO> getMobileLoginLocationStats(int days, int limit);

    /**
     * 高频登录 IP（web_login_info 全渠道），含地点与次数
     */
    List<IpLoginFrequencyVO> getIpLoginFrequencyStats(int days, int limit);
}
