package com.hongshu.web.controller.sys;

import com.hongshu.common.core.web.controller.BaseController;
import com.hongshu.common.core.web.domain.AjaxResult;
import com.hongshu.common.core.web.page.TableDataInfo;
import com.hongshu.common.core.annotation.Log;
import com.hongshu.common.core.enums.BusinessType;
import com.hongshu.web.domain.vo.ChatGroupMemberVo;
import com.hongshu.web.domain.vo.ChatGroupVo;
import com.hongshu.web.service.web.IWebChatGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 群聊管理Controller
 *
 * @author: hongshu
 */
@RestController
@RequestMapping("/chatGroup")
public class SysChatGroupController extends BaseController {

    @Autowired
    private IWebChatGroupService groupService;

    /**
     * 获取群聊列表（管理端）
     */
    @PreAuthorize("@ss.hasPermi('web:chatGroup:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam Map<String, Object> map) {
        this.startPage();
        String groupName = (String) map.get("groupName");
        String ownerId = (String) map.get("ownerId");
        Integer status = map.get("status") != null ? Integer.valueOf(map.get("status").toString()) : null;
        List<ChatGroupVo> groupList = groupService.getAllGroupList(groupName, ownerId, status);
        return getDataTable(groupList);
    }

    /**
     * 根据群聊ID获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('web:chatGroup:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable String id) {
        ChatGroupVo group = groupService.getGroupDetail(id);
        return success(group);
    }

    /**
     * 获取群成员列表
     */
    @PreAuthorize("@ss.hasPermi('web:chatGroup:query')")
    @GetMapping("/members/{groupId}")
    public AjaxResult getMembers(@PathVariable String groupId) {
        List<ChatGroupMemberVo> members = groupService.getGroupMemberList(groupId);
        return success(members);
    }

    /**
     * 删除/解散群聊（管理端）
     */
    @PreAuthorize("@ss.hasPermi('web:chatGroup:remove')")
    @Log(title = "群聊管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        int successCount = 0;
        for (String id : ids) {
            if (groupService.dissolveGroup(id)) {
                successCount++;
            }
        }
        return toAjax(successCount);
    }

    /**
     * 移除群成员（管理端）
     */
    @PreAuthorize("@ss.hasPermi('web:chatGroup:edit')")
    @Log(title = "群聊管理", businessType = BusinessType.UPDATE)
    @PostMapping("/removeMember")
    public AjaxResult removeMember(@RequestParam String groupId, @RequestParam String userId) {
        boolean success = groupService.removeMember(groupId, userId);
        return toAjax(success ? 1 : 0);
    }
}

