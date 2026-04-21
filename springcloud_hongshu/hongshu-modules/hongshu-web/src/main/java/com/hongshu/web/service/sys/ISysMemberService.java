package com.hongshu.web.service.sys;

import com.hongshu.web.domain.Query;
import com.hongshu.web.domain.entity.WebUser;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 会员信息 服务层
 *

 */
public interface ISysMemberService {

    /**
     * 查询会员信息集合
     *
     * @param query 会员信息
     */
    List<WebUser> selectMemberList(Query query);

    /**
     * 查询所有会员
     */
    List<WebUser> selectMemberAll();

    /**
     * 通过会员ID查询会员信息
     *
     * @param id 会员ID
     */
    WebUser selectMemberById(Long id);

    /**
     * 删除会员信息
     *
     * @param id 会员ID
     */
    int deleteMemberById(Long id);

    /**
     * 新增保存会员信息
     *
     * @param user 会员信息
     */
    int insertMember(WebUser user, MultipartFile file);

    /**
     * 修改保存会员信息
     *
     * @param user 会员信息
     */
    int updateMember(WebUser user, MultipartFile file);

    /**
     * 批量删除会员信息
     *
     * @param ids 需要删除的会员ID
     */
    int deleteMemberByIds(Long[] ids);

    Object getMemberCount(int enable);

    /**
     * 批量禁用会员
     *
     * @param ids 需要禁用的会员ID
     */
    void disableMemberByIds(Long[] ids);

    /**
     * 更新会员状态
     */
    void updateStatus(Long id, String status);

    /**
     * 待审核用户审核通过（status 2 -> 0）
     */
    void approvePendingUser(Long id);

    /**
     * 待审核用户审核拒绝（status 2 -> 1 禁用）
     */
    void rejectPendingUser(Long id);
}
