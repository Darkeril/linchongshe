package com.hongshu.web.mapper.web;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hongshu.web.domain.dto.IpStatDTO;
import com.hongshu.web.domain.entity.DailyStatistics;
import com.hongshu.web.domain.entity.WebUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * @author: hongshu
 */
@Mapper
public interface WebUserMapper extends BaseMapper<WebUser> {

    /**
     * 根据条件分页查询角色数据
     *
     * @param user 会员
     * @return 角色数据集合信息
     */
    List<WebUser> getUserList(WebUser user);

    @Select("SELECT COUNT(*) FROM web_user WHERE DATE(create_time) = CURDATE() AND deleted = 0")
    int getTodayNewUserCount();

    @Select("WITH RECURSIVE dates AS (" +
            "    SELECT #{startDate} as date " +
            "    UNION ALL " +
            "    SELECT DATE_ADD(date, INTERVAL 1 DAY) " +
            "    FROM dates " +
            "    WHERE date < #{endDate} " +
            ") " +
            "SELECT " +
            "    dates.date as date, " +
            "    COUNT(DISTINCT u.id) as userCount, " +
            "    (SELECT COUNT(*) FROM web_note WHERE DATE(create_time) = dates.date AND deleted = 0) as noteCount, " +
            "    (SELECT COUNT(*) FROM idle_product WHERE DATE(create_time) = dates.date AND deleted = 0) as productCount " +
            "FROM dates " +
            "LEFT JOIN web_user u ON DATE(u.create_time) = dates.date AND u.deleted = 0 " +
            "GROUP BY dates.date " +
            "ORDER BY dates.date")
    List<DailyStatistics> getStatisticsByDateRange(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    @Select("SELECT login_ip, COUNT(*) as count FROM web_user GROUP BY login_ip")
    List<IpStatDTO> selectUserIpStats();
}
