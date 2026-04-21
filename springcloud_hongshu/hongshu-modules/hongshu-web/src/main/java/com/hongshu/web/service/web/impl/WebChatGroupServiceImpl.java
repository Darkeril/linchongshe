package com.hongshu.web.service.web.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.core.constant.AuthConstant;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.common.core.exception.BusinessException;
import com.hongshu.common.core.utils.ConvertUtils;
import com.hongshu.web.domain.entity.WebChatGroup;
import com.hongshu.web.domain.entity.WebChatGroupMember;
import com.hongshu.web.domain.entity.WebChatUserRelation;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.domain.vo.ChatGroupMemberVo;
import com.hongshu.web.domain.vo.ChatGroupVo;
import com.hongshu.web.mapper.web.WebChatGroupMapper;
import com.hongshu.web.mapper.web.WebChatGroupMemberMapper;
import com.hongshu.web.mapper.web.WebChatUserRelationMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.service.web.IWebChatGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 群聊服务实现类
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class WebChatGroupServiceImpl extends ServiceImpl<WebChatGroupMapper, WebChatGroup> implements IWebChatGroupService {

    @Autowired
    private WebChatGroupMapper groupMapper;

    @Autowired
    private WebChatGroupMemberMapper memberMapper;

    @Autowired
    private WebChatUserRelationMapper chatUserRelationMapper;

    @Autowired
    private WebUserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatGroupVo createGroup(String groupName, String groupAvatar, String groupDescription, List<String> memberIds) {
        String currentUserId = AuthContextHolder.getUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }

        // 创建群聊
        WebChatGroup group = new WebChatGroup();
        String groupId = IdUtil.simpleUUID();
        group.setId(groupId);
        group.setGroupName(groupName != null ? groupName : "群聊");
        // 设置默认群头像（如果未提供）
        if (groupAvatar == null || groupAvatar.isEmpty()) {
            group.setGroupAvatar(AuthConstant.DEFAULT_GROUP_AVATAR); // 默认群头像路径
        } else {
            group.setGroupAvatar(groupAvatar);
        }
        group.setGroupDescription(groupDescription);
        group.setOwnerId(currentUserId);
        group.setMemberCount(1); // 创建者自己
        group.setMaxMemberCount(500);
        group.setStatus(0);
        group.setCreator(currentUserId);
        group.setCreateTime(new Date());
        groupMapper.insert(group);

        // 添加创建者为群主
        WebChatGroupMember ownerMember = new WebChatGroupMember();
        ownerMember.setId(IdUtil.simpleUUID());
        ownerMember.setGroupChatId(groupId);
        ownerMember.setUserId(currentUserId);
        ownerMember.setRole(0); // 群主
        ownerMember.setUnreadCount(0);
        ownerMember.setMute(0);
        ownerMember.setJoinTime(new Date());
        ownerMember.setCreator(currentUserId);
        ownerMember.setCreateTime(new Date());
        memberMapper.insert(ownerMember);

        // 添加其他成员
        if (memberIds != null && !memberIds.isEmpty()) {
            log.info("开始添加群成员，成员数量: {}", memberIds.size());
            int addedCount = 0;
            for (String memberId : memberIds) {
                if (memberId == null || memberId.trim().isEmpty()) {
                    log.warn("跳过空的成员ID");
                    continue;
                }
                String trimmedMemberId = memberId.trim();
                if (!trimmedMemberId.equals(currentUserId)) { // 排除创建者自己
                    try {
                        WebChatGroupMember member = new WebChatGroupMember();
                        String groupMemberId = IdUtil.simpleUUID();
                        member.setId(groupMemberId);
                        member.setGroupChatId(groupId);
                        member.setUserId(trimmedMemberId);
                        member.setRole(2); // 普通成员
                        member.setUnreadCount(0);
                        member.setMute(0);
                        member.setJoinTime(new Date());
                        member.setCreator(currentUserId);
                        member.setCreateTime(new Date());

                        log.info("准备插入群成员 - id={}, groupChatId={}, userId={}, role={}", 
                                groupMemberId, groupId, trimmedMemberId, member.getRole());
                        
                        int insertResult = memberMapper.insert(member);
                        log.info("插入群成员结果 - userId={}, insertResult={}", trimmedMemberId, insertResult);
                        
                        if (insertResult > 0) {
                            addedCount++;
                            log.info("添加群成员成功: userId={}, groupChatId={}, groupMemberId={}", 
                                    trimmedMemberId, groupId, groupMemberId);
                        } else {
                            log.error("添加群成员失败: userId={}, 插入结果={}", trimmedMemberId, insertResult);
                        }
                    } catch (Exception e) {
                        log.error("添加群成员异常: userId={}, groupChatId={}", trimmedMemberId, groupId, e);
                        throw new BusinessException("添加群成员失败: " + trimmedMemberId + ", " + e.getMessage());
                    }
                } else {
                    log.info("跳过创建者自己: {}", trimmedMemberId);
                }
            }
            group.setMemberCount(group.getMemberCount() + addedCount);
            groupMapper.updateById(group);
            log.info("群成员添加完成，实际添加: {} 人，总成员数: {}", addedCount, group.getMemberCount());
        } else {
            log.info("没有需要添加的成员");
        }

        return getGroupDetail(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinGroup(String groupChatId) {
        String currentUserId = AuthContextHolder.getUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }

        // 检查群是否存在
        WebChatGroup group = groupMapper.selectById(groupChatId);
        if (group == null) {
            throw new BusinessException("群聊不存在");
        }
        if (group.getStatus() == 1) {
            throw new BusinessException("群聊已解散");
        }
        if (group.getMemberCount() >= group.getMaxMemberCount()) {
            throw new BusinessException("群聊人数已满");
        }

        // 检查是否已经是成员
        QueryWrapper<WebChatGroupMember> memberWrapper = new QueryWrapper<>();
        memberWrapper.eq("group_chat_id", groupChatId)
                .eq("user_id", currentUserId);
        WebChatGroupMember existingMember = memberMapper.selectOne(memberWrapper);
        if (existingMember != null) {
            throw new BusinessException("您已经是群成员");
        }

        // 添加成员
        WebChatGroupMember member = new WebChatGroupMember();
        member.setId(IdUtil.simpleUUID());
        member.setGroupChatId(groupChatId);
        member.setUserId(currentUserId);
        member.setRole(2); // 普通成员
        member.setUnreadCount(0);
        member.setMute(0);
        member.setJoinTime(new Date());
        member.setCreator(currentUserId);
        member.setCreateTime(new Date());
        memberMapper.insert(member);

        // 更新群成员数量
        group.setMemberCount(group.getMemberCount() + 1);
        groupMapper.updateById(group);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitGroup(String groupChatId) {
        String currentUserId = AuthContextHolder.getUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }

        // 检查是否是群主
        WebChatGroup group = groupMapper.selectById(groupChatId);
        if (group == null) {
            throw new BusinessException("群聊不存在");
        }
        if (group.getOwnerId().equals(currentUserId)) {
            throw new BusinessException("群主不能退出，请先解散群聊");
        }

        // 移除成员
        QueryWrapper<WebChatGroupMember> memberWrapper = new QueryWrapper<>();
        memberWrapper.eq("group_chat_id", groupChatId)
                .eq("user_id", currentUserId);
        int deleted = memberMapper.delete(memberWrapper);
        if (deleted > 0) {
            // 更新群成员数量
            group.setMemberCount(group.getMemberCount() - 1);
            groupMapper.updateById(group);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean dissolveGroup(String groupChatId) {
        String currentUserId = AuthContextHolder.getUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }

        // 检查是否是群主
        WebChatGroup group = groupMapper.selectById(groupChatId);
        if (group == null) {
            throw new BusinessException("群聊不存在");
        }
        if (!group.getOwnerId().equals(currentUserId)) {
            throw new BusinessException("只有群主可以解散群聊");
        }

        // 更新群状态为已解散
        group.setStatus(1);
        groupMapper.updateById(group);

        // 删除所有成员关系
        QueryWrapper<WebChatGroupMember> memberWrapper = new QueryWrapper<>();
        memberWrapper.eq("group_chat_id", groupChatId);
        memberMapper.delete(memberWrapper);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeMember(String groupChatId, String userId) {
        String currentUserId = AuthContextHolder.getUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }

        // 检查权限
        WebChatGroup group = groupMapper.selectById(groupChatId);
        if (group == null) {
            throw new BusinessException("群聊不存在");
        }

        // 检查是否是群主或管理员
        QueryWrapper<WebChatGroupMember> currentMemberWrapper = new QueryWrapper<>();
        currentMemberWrapper.eq("group_chat_id", groupChatId)
                .eq("user_id", currentUserId);
        WebChatGroupMember currentMember = memberMapper.selectOne(currentMemberWrapper);
        if (currentMember == null) {
            throw new BusinessException("您不是群成员");
        }
        if (currentMember.getRole() != 0 && currentMember.getRole() != 1) {
            throw new BusinessException("您没有权限移除成员");
        }

        // 不能移除群主
        if (group.getOwnerId().equals(userId)) {
            throw new BusinessException("不能移除群主");
        }

        // 移除成员
        QueryWrapper<WebChatGroupMember> memberWrapper = new QueryWrapper<>();
        memberWrapper.eq("group_chat_id", groupChatId)
                .eq("user_id", userId);
        int deleted = memberMapper.delete(memberWrapper);
        if (deleted > 0) {
            // 更新群成员数量
            group.setMemberCount(group.getMemberCount() - 1);
            groupMapper.updateById(group);
            return true;
        }
        return false;
    }

    @Override
    public List<ChatGroupVo> getMyGroupList() {
        String currentUserId = AuthContextHolder.getUserId();
        if (currentUserId == null) {
            return new ArrayList<>();
        }

        // 查询我加入的群聊
        QueryWrapper<WebChatGroupMember> memberWrapper = new QueryWrapper<>();
        memberWrapper.eq("user_id", currentUserId);
        List<WebChatGroupMember> myMembers = memberMapper.selectList(memberWrapper);
        if (myMembers.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> groupIds = myMembers.stream()
                .map(WebChatGroupMember::getGroupChatId)
                .collect(Collectors.toList());

        // 查询群聊信息
        List<WebChatGroup> groups = groupMapper.selectBatchIds(groupIds);
        groups = groups.stream()
                .filter(g -> g.getStatus() == 0) // 只返回正常状态的群
                .collect(Collectors.toList());

        // 获取群主信息
        List<String> ownerIds = groups.stream()
                .map(WebChatGroup::getOwnerId)
                .distinct()
                .collect(Collectors.toList());
        Map<String, WebUser> ownerMap = userMapper.selectBatchIds(ownerIds).stream()
                .collect(Collectors.toMap(WebUser::getId, user -> user));

        // 获取每个群的最新消息和未读数
        List<ChatGroupVo> result = new ArrayList<>();
        for (WebChatGroup group : groups) {
            ChatGroupVo vo = ConvertUtils.sourceToTarget(group, ChatGroupVo.class);
            WebUser owner = ownerMap.get(group.getOwnerId());
            if (owner != null) {
                vo.setOwnerName(owner.getUsername());
            }

            // 获取该群的最新消息
            QueryWrapper<WebChatUserRelation> relationWrapper = new QueryWrapper<>();
            relationWrapper.eq("group_chat_id", group.getId())
                    .orderByDesc("timestamp")
                    .last("LIMIT 1");
            WebChatUserRelation lastRelation = chatUserRelationMapper.selectOne(relationWrapper);
            if (lastRelation != null) {
                vo.setLastMessage(lastRelation.getContent());
                vo.setLastMessageTime(lastRelation.getTimestamp());
                String senderId = lastRelation.getSendUid();
                if (senderId != null) {
                    vo.setLastMessageSenderId(senderId);
                    WebUser sender = userMapper.selectById(senderId);
                    if (sender != null) {
                        vo.setLastMessageSenderName(sender.getUsername());
                    }
                }
            }

            // 获取当前用户的未读数
            WebChatGroupMember myMember = myMembers.stream()
                    .filter(m -> m.getGroupChatId().equals(group.getId()))
                    .findFirst()
                    .orElse(null);
            if (myMember != null) {
                vo.setUnreadCount(myMember.getUnreadCount());
            }

            result.add(vo);
        }

        // 按最后消息时间排序
        result.sort((a, b) -> {
            Long timeA = a.getLastMessageTime() != null ? a.getLastMessageTime() : 0L;
            Long timeB = b.getLastMessageTime() != null ? b.getLastMessageTime() : 0L;
            return Long.compare(timeB, timeA);
        });

        return result;
    }

    @Override
    public ChatGroupVo getGroupDetail(String groupChatId) {
        WebChatGroup group = groupMapper.selectById(groupChatId);
        if (group == null) {
            throw new BusinessException("群聊不存在");
        }

        ChatGroupVo vo = ConvertUtils.sourceToTarget(group, ChatGroupVo.class);
        WebUser owner = userMapper.selectById(group.getOwnerId());
        if (owner != null) {
            vo.setOwnerName(owner.getUsername());
        }

        return vo;
    }

    @Override
    public List<ChatGroupMemberVo> getGroupMemberList(String groupChatId) {
        QueryWrapper<WebChatGroupMember> memberWrapper = new QueryWrapper<>();
        memberWrapper.eq("group_chat_id", groupChatId)
                .orderByAsc("role") // 先按角色排序（群主、管理员、普通成员）
                .orderByAsc("join_time"); // 再按加入时间排序
        List<WebChatGroupMember> members = memberMapper.selectList(memberWrapper);

        if (members.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取用户信息
        List<String> userIds = members.stream()
                .map(WebChatGroupMember::getUserId)
                .collect(Collectors.toList());
        Map<String, WebUser> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(WebUser::getId, user -> user));

        // 转换为VO
        List<ChatGroupMemberVo> result = new ArrayList<>();
        for (WebChatGroupMember member : members) {
            ChatGroupMemberVo vo = ConvertUtils.sourceToTarget(member, ChatGroupMemberVo.class);
            WebUser user = userMap.get(member.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setAvatar(user.getAvatar());
            }
            result.add(vo);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateGroupInfo(String groupChatId, String groupName, String groupAvatar, String groupDescription) {
        String currentUserId = AuthContextHolder.getUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }

        // 检查权限
        WebChatGroup group = groupMapper.selectById(groupChatId);
        if (group == null) {
            throw new BusinessException("群聊不存在");
        }

        QueryWrapper<WebChatGroupMember> memberWrapper = new QueryWrapper<>();
        memberWrapper.eq("group_chat_id", groupChatId)
                .eq("user_id", currentUserId);
        WebChatGroupMember member = memberMapper.selectOne(memberWrapper);
        if (member == null) {
            throw new BusinessException("您不是群成员");
        }
        if (member.getRole() != 0 && member.getRole() != 1) {
            throw new BusinessException("您没有权限修改群信息");
        }

        // 更新群信息
        if (groupName != null) {
            group.setGroupName(groupName);
        }
        if (groupAvatar != null) {
            group.setGroupAvatar(groupAvatar);
        }
        if (groupDescription != null) {
            group.setGroupDescription(groupDescription);
        }
        groupMapper.updateById(group);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setGroupNickname(String groupChatId, String groupNickname) {
        String currentUserId = AuthContextHolder.getUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }

        QueryWrapper<WebChatGroupMember> memberWrapper = new QueryWrapper<>();
        memberWrapper.eq("group_chat_id", groupChatId)
                .eq("user_id", currentUserId);
        WebChatGroupMember member = memberMapper.selectOne(memberWrapper);
        if (member == null) {
            throw new BusinessException("您不是群成员");
        }

        member.setGroupNickname(groupNickname);
        memberMapper.updateById(member);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setMute(String groupChatId, Integer mute) {
        String currentUserId = AuthContextHolder.getUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }

        QueryWrapper<WebChatGroupMember> memberWrapper = new QueryWrapper<>();
        memberWrapper.eq("group_chat_id", groupChatId)
                .eq("user_id", currentUserId);
        WebChatGroupMember member = memberMapper.selectOne(memberWrapper);
        if (member == null) {
            throw new BusinessException("您不是群成员");
        }

        member.setMute(mute);
        memberMapper.updateById(member);

        return true;
    }

    @Override
    public List<ChatGroupVo> getAllGroupList(String groupName, String ownerId, Integer status) {
        QueryWrapper<WebChatGroup> wrapper = new QueryWrapper<>();
        
        if (groupName != null && !groupName.trim().isEmpty()) {
            wrapper.like("group_name", groupName);
        }
        if (ownerId != null && !ownerId.trim().isEmpty()) {
            wrapper.eq("owner_id", ownerId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        wrapper.orderByDesc("create_time");
        List<WebChatGroup> groups = groupMapper.selectList(wrapper);
        
        // 转换为VO
        List<ChatGroupVo> result = new ArrayList<>();
        for (WebChatGroup group : groups) {
            ChatGroupVo vo = ConvertUtils.sourceToTarget(group, ChatGroupVo.class);
            if (vo != null) {
                // 获取群主信息
                WebUser owner = userMapper.selectById(group.getOwnerId());
                if (owner != null) {
                    vo.setOwnerName(owner.getUsername());
                    vo.setOwnerAvatar(owner.getAvatar());
                }
                result.add(vo);
            }
        }
        
        return result;
    }
}

