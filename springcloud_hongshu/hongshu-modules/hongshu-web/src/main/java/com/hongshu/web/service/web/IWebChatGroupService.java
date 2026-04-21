package com.hongshu.web.service.web;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.web.domain.entity.WebChatGroup;
import com.hongshu.web.domain.vo.ChatGroupMemberVo;
import com.hongshu.web.domain.vo.ChatGroupVo;

import java.util.List;

/**
 * 群聊服务接口
 *
 * @author: hongshu
 */
public interface IWebChatGroupService extends IService<WebChatGroup> {

    /**
     * 创建群聊
     *
     * @param groupName 群名称
     * @param groupAvatar 群头像
     * @param groupDescription 群简介
     * @param memberIds 初始成员ID列表（不包括创建者）
     * @return 群聊信息
     */
    ChatGroupVo createGroup(String groupName, String groupAvatar, String groupDescription, List<String> memberIds);

    /**
     * 加入群聊
     *
     * @param groupChatId 群聊ID
     * @return 是否成功
     */
    boolean joinGroup(String groupChatId);

    /**
     * 退出群聊
     *
     * @param groupChatId 群聊ID
     * @return 是否成功
     */
    boolean quitGroup(String groupChatId);

    /**
     * 解散群聊（仅群主）
     *
     * @param groupChatId 群聊ID
     * @return 是否成功
     */
    boolean dissolveGroup(String groupChatId);

    /**
     * 移除群成员（群主或管理员）
     *
     * @param groupChatId 群聊ID
     * @param userId 被移除的用户ID
     * @return 是否成功
     */
    boolean removeMember(String groupChatId, String userId);

    /**
     * 获取我的群聊列表
     *
     * @return 群聊列表
     */
    List<ChatGroupVo> getMyGroupList();

    /**
     * 获取群聊详情
     *
     * @param groupChatId 群聊ID
     * @return 群聊信息
     */
    ChatGroupVo getGroupDetail(String groupChatId);

    /**
     * 获取群成员列表
     *
     * @param groupChatId 群聊ID
     * @return 成员列表
     */
    List<ChatGroupMemberVo> getGroupMemberList(String groupChatId);

    /**
     * 更新群信息（群主或管理员）
     *
     * @param groupChatId 群聊ID
     * @param groupName 群名称
     * @param groupAvatar 群头像
     * @param groupDescription 群简介
     * @return 是否成功
     */
    boolean updateGroupInfo(String groupChatId, String groupName, String groupAvatar, String groupDescription);

    /**
     * 设置群昵称
     *
     * @param groupChatId 群聊ID
     * @param groupNickname 群昵称
     * @return 是否成功
     */
    boolean setGroupNickname(String groupChatId, String groupNickname);

    /**
     * 设置免打扰
     *
     * @param groupChatId 群聊ID
     * @param mute 是否免打扰（0：否 1：是）
     * @return 是否成功
     */
    boolean setMute(String groupChatId, Integer mute);

    /**
     * 获取所有群聊列表（管理端）
     *
     * @param groupName 群名称（可选）
     * @param ownerId 群主ID（可选）
     * @param status 群状态（可选）
     * @return 群聊列表
     */
    List<ChatGroupVo> getAllGroupList(String groupName, String ownerId, Integer status);
}

