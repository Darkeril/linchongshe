package com.hongshu.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.hongshu.system.domain.SysNotice;

/**
 * 通知公告表 数据层
 *
 
 */
public interface SysNoticeMapper
{
    /**
     * 查询公告信息
     *
     * @param noticeId 公告ID
     * @return 公告信息
     */
    public SysNotice selectNoticeById(Long noticeId);

    /**
     * 查询公告列表
     *
     * @param notice 公告信息
     * @return 公告集合
     */
    public List<SysNotice> selectNoticeList(SysNotice notice);

    /**
     * 新增公告
     *
     * @param notice 公告信息
     * @return 结果
     */
    public int insertNotice(SysNotice notice);

    /**
     * 修改公告
     *
     * @param notice 公告信息
     * @return 结果
     */
    public int updateNotice(SysNotice notice);

    /**
     * 批量删除公告
     *
     * @param noticeId 公告ID
     * @return 结果
     */
    public int deleteNoticeById(Long noticeId);

    /**
     * 批量删除公告信息
     *
     * @param noticeIds 需要删除的公告ID
     * @return 结果
     */
    public int deleteNoticeByIds(Long[] noticeIds);

    /**
     * 查询最新的正常状态公告（带当前用户已读标记）
     */
    public List<Map<String, Object>> selectNoticeTopWithRead(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 查询已读记录数（判断是否已读）
     */
    public int countNoticeRead(@Param("userId") Long userId, @Param("noticeId") Long noticeId);

    /**
     * 插入已读记录
     */
    public int insertNoticeRead(@Param("userId") Long userId, @Param("noticeId") Long noticeId);
}
