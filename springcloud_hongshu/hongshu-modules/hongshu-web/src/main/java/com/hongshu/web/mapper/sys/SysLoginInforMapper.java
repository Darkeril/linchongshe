package com.hongshu.web.mapper.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hongshu.common.core.domain.IpLoginFrequencyVO;
import com.hongshu.common.core.domain.LoginLocationStatVO;
import com.hongshu.web.domain.entity.WebLoginInfor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * 系统访问日志情况信息 数据层
 *
 
 */
@Mapper
public interface SysLoginInforMapper extends BaseMapper<WebLoginInfor> {

    /**
     * 获取IP数目
     *
     * @param startTime
     * @param endTime
     * @return
     */
    @Select("SELECT COUNT(ipaddr) FROM (SELECT ipaddr FROM web_login_info WHERE create_time >= #{startTime} AND create_time <= #{endTime} GROUP BY ipaddr) AS tmp")
    Integer getIpCount(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Delete("DELETE FROM web_login_info")
    void cleanLoginInfor();

    /**
     * Web 端（浏览器 H5/PC）：from_type=0，历史数据 from_type 为空视为 Web
     */
    @Select("SELECT CASE WHEN login_location IS NULL OR TRIM(login_location) = '' THEN '未知' ELSE TRIM(login_location) END AS location, "
            + "COUNT(*) AS loginCount FROM web_login_info "
            + "WHERE (status = '0' OR status = '200') AND COALESCE(login_time, create_time) >= #{startTime} "
            + "AND (from_type IS NULL OR from_type = 0) "
            + "GROUP BY CASE WHEN login_location IS NULL OR TRIM(login_location) = '' THEN '未知' ELSE TRIM(login_location) END "
            + "ORDER BY loginCount DESC LIMIT #{limit}")
    List<LoginLocationStatVO> selectWebLoginLocationStats(@Param("startTime") Date startTime,
                                                          @Param("limit") int limit);

    /**
     * 移动端：App(1) + 小程序(2)，与 {@link com.hongshu.common.core.enums.TerminalTypeEnum} 一致
     */
    @Select("SELECT CASE WHEN login_location IS NULL OR TRIM(login_location) = '' THEN '未知' ELSE TRIM(login_location) END AS location, "
            + "COUNT(*) AS loginCount FROM web_login_info "
            + "WHERE (status = '0' OR status = '200') AND COALESCE(login_time, create_time) >= #{startTime} "
            + "AND from_type IN (1, 2) "
            + "GROUP BY CASE WHEN login_location IS NULL OR TRIM(login_location) = '' THEN '未知' ELSE TRIM(login_location) END "
            + "ORDER BY loginCount DESC LIMIT #{limit}")
    List<LoginLocationStatVO> selectMobileLoginLocationStats(@Param("startTime") Date startTime,
                                                             @Param("limit") int limit);

    /**
     * 会员登录日志按 IP 聚合：高频 IP + 地点（Web/App/小程序 全渠道）
     */
    @Select("SELECT "
            + "CASE WHEN ipaddr IS NULL OR TRIM(ipaddr) = '' THEN '未知' ELSE TRIM(ipaddr) END AS ipaddr, "
            + "COUNT(*) AS loginCount, "
            + "MAX(CASE WHEN login_location IS NULL OR TRIM(login_location) = '' THEN '未解析' "
            + "ELSE TRIM(login_location) END) AS location "
            + "FROM web_login_info "
            + "WHERE (status = '0' OR status = '200') AND COALESCE(login_time, create_time) >= #{startTime} "
            + "GROUP BY CASE WHEN ipaddr IS NULL OR TRIM(ipaddr) = '' THEN '未知' ELSE TRIM(ipaddr) END "
            + "ORDER BY loginCount DESC LIMIT #{limit}")
    List<IpLoginFrequencyVO> selectIpLoginFrequencyStats(@Param("startTime") Date startTime,
                                                         @Param("limit") int limit);

//    /**
//     * 新增系统登录日志
//     *
//     * @param loginInfor 访问日志对象
//     */
//    void insertLoginInfor(WebLoginInfor loginInfor);
//
//    /**
//     * 查询系统登录日志集合
//     *
//     * @param loginInfor 访问日志对象
//     * @return 登录记录集合
//     */
//    List<WebLoginInfor> selectLoginInforList(WebLoginInfor loginInfor);
//
//    /**
//     * 批量删除系统登录日志
//     *
//     * @param infoIds 需要删除的登录日志ID
//     * @return 结果
//     */
//    int deleteLoginInforByIds(Long[] infoIds);
//
//    /**
//     * 清空系统登录日志
//     *
//     */
//    void cleanLoginInfor();
}
