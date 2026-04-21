package com.hongshu.idle.controller.sys;

import cn.hutool.json.JSONUtil;
import com.hongshu.common.core.web.controller.BaseController;
import com.hongshu.common.core.web.domain.AjaxResult;
import com.hongshu.common.core.annotation.Log;
import com.hongshu.common.core.enums.BusinessType;
import com.hongshu.idle.domain.entity.IdleCategory;
import com.hongshu.idle.service.sys.ISysIdleNavbarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 导航栏信息
 *

 */
@RestController
@RequestMapping("/navbar")
public class SysIdleNavbarController extends BaseController {

    @Autowired
    private ISysIdleNavbarService idleNavbarService;


    /**
     * 获取导航栏列表
     */
    @PreAuthorize("@ss.hasPermi('web:navbar:list')")
    @GetMapping("/list")
    public AjaxResult list(IdleCategory category) {
        List<IdleCategory> categoryList = idleNavbarService.selectNavbarList(category);
        return success(categoryList);
    }

    /**
     * 根据导航栏编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('web:navbar:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(idleNavbarService.selectById(id));
    }

    /**
     * 加载导航栏列表树
     */
    @GetMapping(value = "/navbarTreeSelect")
    public AjaxResult roleMenuTreeSelect(IdleCategory category) {
        List<IdleCategory> categoryList = idleNavbarService.selectNavbarList(category);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("categoryList", idleNavbarService.buildNavbarTreeSelect(categoryList));
        return ajax;
    }

    /**
     * 获取菜单下拉树列表
     */
    @GetMapping("/treeSelect")
    public AjaxResult treeSelect(IdleCategory category) {
        List<IdleCategory> categoryList = idleNavbarService.selectNavbarList(category);
        return success(idleNavbarService.buildNavbarTreeSelect(categoryList));
    }

    /**
     * 新增导航栏
     */
    @PreAuthorize("@ss.hasPermi('web:navbar:add')")
    @Log(title = "导航栏管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestParam("category") String category, @RequestParam("file") MultipartFile file) {
//        if (!navbarService.checkWebNavbarNameUnique(category)) {
//            return error("新增导航栏'" + category.getTitle() + "'失败，导航栏名称已存在");
//        }
//        category.setCreator(getUsername());
        IdleCategory webCategory = JSONUtil.toBean(category, IdleCategory.class);
        return toAjax(idleNavbarService.insertNavbar(webCategory, file));
    }

    /**
     * 修改导航栏
     */
    @PreAuthorize("@ss.hasPermi('web:navbar:edit')")
    @Log(title = "导航栏管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestParam("category") String category, @RequestParam(value = "file", required = false) MultipartFile file) {
//    public AjaxResult edit(@Validated @RequestBody WebCategory category) {
//        if (!navbarService.checkWebNavbarNameUnique(category)) {
//            return error("修改导航栏'" + category.getTitle() + "'失败，导航栏名称已存在");
//        } else if (category.getId().equals(category.getPid())) {
//            return error("修改导航栏'" + category.getTitle() + "'失败，上级导航栏不能选择自己");
//        }
//        category.setUpdater(getUsername());
        IdleCategory webCategory = JSONUtil.toBean(category, IdleCategory.class);
        return toAjax(idleNavbarService.updateNavbar(webCategory, file));
    }

    /**
     * 删除导航栏
     */
    @PreAuthorize("@ss.hasPermi('web:navbar:remove')")
    @Log(title = "导航栏管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        if (idleNavbarService.hasChildById(id)) {
            return warn("存在子导航栏,不允许删除");
        }
        return toAjax(idleNavbarService.deleteById(id));
    }

    /**
     * 更新导航栏状态
     */
    @PostMapping("/updateStatus")
    public AjaxResult updateStatus(@RequestParam Long id, @RequestParam String status) {
        idleNavbarService.updateStatus(id, status);
        return AjaxResult.success();
    }
}
