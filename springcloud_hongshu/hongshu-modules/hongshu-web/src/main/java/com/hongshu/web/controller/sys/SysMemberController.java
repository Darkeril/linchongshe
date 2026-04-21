package com.hongshu.web.controller.sys;

import cn.hutool.json.JSONUtil;
import com.hongshu.common.core.annotation.Log;
import com.hongshu.common.core.enums.BusinessType;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.utils.poi.ExcelUtil;
import com.hongshu.common.core.web.controller.BaseController;
import com.hongshu.common.core.web.domain.AjaxResult;
import com.hongshu.common.core.web.page.TableDataInfo;
import com.hongshu.web.domain.Query;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.service.sys.ISysMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 会员操作处理
 *

 */
@RestController
@RequestMapping("/member")
public class SysMemberController extends BaseController {

    @Autowired
    private ISysMemberService memberService;


    /**
     * 获取会员列表
     */
    @PreAuthorize("@ss.hasPermi('web:member:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam Map map) {
        this.startPage();
        List<WebUser> userList = memberService.selectMemberList(new Query(map));
        return getDataTable(userList);
    }

    /**
     * 根据会员编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('web:member:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(memberService.selectMemberById(id));
    }

    /**
     * 新增会员
     */
    @PreAuthorize("@ss.hasPermi('web:member:add')")
    @Log(title = "会员管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestParam("user") String user,
                          @RequestParam(value = "file", required = false) MultipartFile file) {
        WebUser webUser = JSONUtil.toBean(user, WebUser.class);
        return toAjax(memberService.insertMember(webUser, file));
    }

    /**
     * 修改会员
     */
    @PreAuthorize("@ss.hasPermi('web:member:edit')")
    @Log(title = "会员管理", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public AjaxResult edit(@RequestParam("user") String user,
                           @RequestParam(value = "file", required = false) MultipartFile file) {
        WebUser webUser = JSONUtil.toBean(user, WebUser.class);
        return toAjax(memberService.updateMember(webUser, file));
    }

    /**
     * 删除会员
     */
    @PreAuthorize("@ss.hasPermi('web:member:remove')")
    @Log(title = "会员管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(memberService.deleteMemberByIds(ids));
    }

    /**
     * 导出会员列表
     */
    @Log(title = "会员管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('web:member:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, Query query) {
        List<WebUser> userList = memberService.selectMemberList(query);
        ExcelUtil<WebUser> util = new ExcelUtil<>(WebUser.class);
        util.exportExcel(response, userList, "会员数据");
    }

    /**
     * 批量禁用会员
     */
    @PreAuthorize("@ss.hasPermi('web:member:disable')")
    @Log(title = "会员管理", businessType = BusinessType.DELETE)
    @PostMapping("/disable/{ids}")
    public Result<?> disable(@PathVariable Long[] ids) {
        memberService.disableMemberByIds(ids);
        return Result.ok();
    }

    /**
     * 更新会员状态
     */
    @PreAuthorize("@ss.hasPermi('web:member:edit')")
    @Log(title = "会员管理", businessType = BusinessType.UPDATE)
    @PostMapping("/updateStatus")
    public AjaxResult updateStatus(@RequestParam Long id, @RequestParam String status) {
        memberService.updateStatus(id, status);
        return AjaxResult.success();
    }

    /**
     * 待审核用户：审核通过
     */
    @PreAuthorize("@ss.hasPermi('web:user:audit')")
    @Log(title = "会员用户审核", businessType = BusinessType.UPDATE)
    @PostMapping("/audit/approve/{id}")
    public AjaxResult approvePending(@PathVariable Long id) {
        memberService.approvePendingUser(id);
        return AjaxResult.success();
    }

    /**
     * 待审核用户：审核拒绝（账号禁用）
     */
    @PreAuthorize("@ss.hasPermi('web:user:audit')")
    @Log(title = "会员用户审核", businessType = BusinessType.UPDATE)
    @PostMapping("/audit/reject/{id}")
    public AjaxResult rejectPending(@PathVariable Long id) {
        memberService.rejectPendingUser(id);
        return AjaxResult.success();
    }

//    /**
//     * 获取会员选择框列表
//     */
//    @GetMapping("/optionselect")
//    public AjaxResult optionselect() {
//        List<WebUser> users = memberService.selectMemberAll();
//        return success(users);
//    }
}
