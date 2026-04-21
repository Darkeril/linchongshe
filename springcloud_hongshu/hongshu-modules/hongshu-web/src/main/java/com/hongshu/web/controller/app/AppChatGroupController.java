package com.hongshu.web.controller.app;

import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.enums.ResultCodeEnum;
import com.hongshu.web.domain.dto.CreateGroupDTO;
import com.hongshu.web.domain.vo.ChatGroupMemberVo;
import com.hongshu.web.domain.vo.ChatGroupVo;
import com.hongshu.web.service.web.IWebChatGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 群聊Controller
 *
 * @author: hongshu
 */
@Slf4j
@RequestMapping("/app/im/group")
@RestController
public class AppChatGroupController {

    @Autowired
    private IWebChatGroupService groupService;

    /**
     * 创建群聊
     *
     * @param dto 创建群聊参数
     * @return 群聊信息
     */
    @PostMapping("create")
    public Result<ChatGroupVo> createGroup(@RequestBody CreateGroupDTO dto) {
        try {
            log.info("创建群聊请求 - 群名称: {}, 群头像: {}, 群简介: {}", 
                    dto.getGroupName(), dto.getGroupAvatar(), dto.getGroupDescription());
            log.info("创建群聊请求 - 成员数量: {}, 成员ID列表: {}", 
                    dto.getMemberIds() != null ? dto.getMemberIds().size() : 0,
                    dto.getMemberIds());
            
            if (dto.getMemberIds() != null && !dto.getMemberIds().isEmpty()) {
                log.info("成员ID详情: {}", String.join(", ", dto.getMemberIds()));
            }
            
            ChatGroupVo group = groupService.createGroup(
                    dto.getGroupName(), 
                    dto.getGroupAvatar(), 
                    dto.getGroupDescription(), 
                    dto.getMemberIds());
            
            log.info("创建群聊成功 - 群ID: {}, 成员数: {}", 
                    group.getId(), group.getMemberCount());
            return Result.ok(group);
        } catch (Exception e) {
            log.error("创建群聊失败", e);
            return Result.build(null, ResultCodeEnum.FAIL.getCode(), e.getMessage());
        }
    }

    /**
     * 加入群聊
     *
     * @param groupChatId 群聊ID
     * @return 是否成功
     */
    @PostMapping("join/{groupChatId}")
    public Result<?> joinGroup(@PathVariable String groupChatId) {
        try {
            boolean success = groupService.joinGroup(groupChatId);
            return success ? Result.ok() : Result.build(null, ResultCodeEnum.FAIL.getCode(), "加入群聊失败");
        } catch (Exception e) {
            return Result.build(null, ResultCodeEnum.FAIL.getCode(), e.getMessage());
        }
    }

    /**
     * 退出群聊
     *
     * @param groupChatId 群聊ID
     * @return 是否成功
     */
    @PostMapping("quit/{groupChatId}")
    public Result<?> quitGroup(@PathVariable String groupChatId) {
        try {
            boolean success = groupService.quitGroup(groupChatId);
            return success ? Result.ok() : Result.build(null, ResultCodeEnum.FAIL.getCode(), "退出群聊失败");
        } catch (Exception e) {
            return Result.build(null, ResultCodeEnum.FAIL.getCode(), e.getMessage());
        }
    }

    /**
     * 解散群聊（仅群主）
     *
     * @param groupChatId 群聊ID
     * @return 是否成功
     */
    @PostMapping("dissolve/{groupChatId}")
    public Result<?> dissolveGroup(@PathVariable String groupChatId) {
        try {
            boolean success = groupService.dissolveGroup(groupChatId);
            return success ? Result.ok() : Result.build(null, ResultCodeEnum.FAIL.getCode(), "解散群聊失败");
        } catch (Exception e) {
            return Result.build(null, ResultCodeEnum.FAIL.getCode(), e.getMessage());
        }
    }

    /**
     * 移除群成员（群主或管理员）
     *
     * @param groupChatId 群聊ID
     * @param userId 被移除的用户ID
     * @return 是否成功
     */
    @PostMapping("removeMember")
    public Result<?> removeMember(@RequestParam String groupChatId, @RequestParam String userId) {
        try {
            boolean success = groupService.removeMember(groupChatId, userId);
            return success ? Result.ok() : Result.build(null, ResultCodeEnum.FAIL.getCode(), "移除成员失败");
        } catch (Exception e) {
            return Result.build(null, ResultCodeEnum.FAIL.getCode(), e.getMessage());
        }
    }

    /**
     * 获取我的群聊列表
     *
     * @return 群聊列表
     */
    @GetMapping("myList")
    public Result<List<ChatGroupVo>> getMyGroupList() {
        try {
            List<ChatGroupVo> list = groupService.getMyGroupList();
            return Result.ok(list);
        } catch (Exception e) {
            return Result.build(null, ResultCodeEnum.FAIL.getCode(), e.getMessage());
        }
    }

    /**
     * 获取群聊详情
     *
     * @param groupChatId 群聊ID
     * @return 群聊信息
     */
    @GetMapping("detail/{groupChatId}")
    public Result<ChatGroupVo> getGroupDetail(@PathVariable String groupChatId) {
        try {
            ChatGroupVo group = groupService.getGroupDetail(groupChatId);
            return Result.ok(group);
        } catch (Exception e) {
            return Result.build(null, ResultCodeEnum.FAIL.getCode(), e.getMessage());
        }
    }

    /**
     * 获取群成员列表
     *
     * @param groupChatId 群聊ID
     * @return 成员列表
     */
    @GetMapping("members/{groupChatId}")
    public Result<List<ChatGroupMemberVo>> getGroupMemberList(@PathVariable String groupChatId) {
        try {
            List<ChatGroupMemberVo> list = groupService.getGroupMemberList(groupChatId);
            return Result.ok(list);
        } catch (Exception e) {
            return Result.build(null, ResultCodeEnum.FAIL.getCode(), e.getMessage());
        }
    }

    /**
     * 更新群信息（群主或管理员）
     *
     * @param groupChatId 群聊ID
     * @param groupName 群名称
     * @param groupAvatar 群头像
     * @param groupDescription 群简介
     * @return 是否成功
     */
    @PostMapping("updateInfo")
    public Result<?> updateGroupInfo(
            @RequestParam String groupChatId,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) String groupAvatar,
            @RequestParam(required = false) String groupDescription) {
        try {
            boolean success = groupService.updateGroupInfo(groupChatId, groupName, groupAvatar, groupDescription);
            return success ? Result.ok() : Result.build(null, ResultCodeEnum.FAIL.getCode(), "更新群信息失败");
        } catch (Exception e) {
            return Result.build(null, ResultCodeEnum.FAIL.getCode(), e.getMessage());
        }
    }

    /**
     * 设置群昵称
     *
     * @param groupChatId 群聊ID
     * @param groupNickname 群昵称
     * @return 是否成功
     */
    @PostMapping("setNickname")
    public Result<?> setGroupNickname(@RequestParam String groupChatId, @RequestParam String groupNickname) {
        try {
            boolean success = groupService.setGroupNickname(groupChatId, groupNickname);
            return success ? Result.ok() : Result.build(null, ResultCodeEnum.FAIL.getCode(), "设置群昵称失败");
        } catch (Exception e) {
            return Result.build(null, ResultCodeEnum.FAIL.getCode(), e.getMessage());
        }
    }

    /**
     * 设置免打扰
     *
     * @param groupChatId 群聊ID
     * @param mute 是否免打扰（0：否 1：是）
     * @return 是否成功
     */
    @PostMapping("setMute")
    public Result<?> setMute(@RequestParam String groupChatId, @RequestParam Integer mute) {
        try {
            boolean success = groupService.setMute(groupChatId, mute);
            return success ? Result.ok() : Result.build(null, ResultCodeEnum.FAIL.getCode(), "设置免打扰失败");
        } catch (Exception e) {
            return Result.build(null, ResultCodeEnum.FAIL.getCode(), e.getMessage());
        }
    }
}

