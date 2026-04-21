package com.hongshu.web.controller.sys;

import com.hongshu.common.core.enums.Result;
import com.hongshu.web.domain.vo.SystemNotification;
import com.hongshu.web.service.web.ISystemNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统通知管理控制器
 *
 * @author: hongshu
 * @date: 2024-01-15
 * @version: 1.0.0
 */
@RestController
@RequestMapping("/system/notification")
public class SystemNotificationController {

    @Autowired
    private ISystemNotificationService systemNotificationService;

    /**
     * 获取系统通知列表
     */
    @GetMapping("/list")
    public Result<?> getSystemNotificationList() {
        List<SystemNotification> list = systemNotificationService.getSystemNotificationList();
        return Result.ok(list);
    }

    /**
     * 添加系统通知
     */
    @PostMapping
    public Result<?> addSystemNotification(@RequestBody SystemNotification systemNotification) {
        boolean success = systemNotificationService.addSystemNotification(systemNotification);
        if (success) {
            return Result.ok("系统通知添加成功");
        } else {
            return Result.fail("系统通知添加失败");
        }
    }

    /**
     * 更新系统通知
     */
    @PutMapping
    public Result<?> updateSystemNotification(@RequestBody SystemNotification systemNotification) {
        boolean success = systemNotificationService.updateSystemNotification(systemNotification);
        if (success) {
            return Result.ok("系统通知更新成功");
        } else {
            return Result.fail("系统通知更新失败");
        }
    }

    /**
     * 删除系统通知
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteSystemNotification(@PathVariable String id) {
        boolean success = systemNotificationService.deleteSystemNotification(id);
        if (success) {
            return Result.ok("系统通知删除成功");
        } else {
            return Result.fail("系统通知删除失败");
        }
    }

    /**
     * 推送系统通知
     */
    @PostMapping("/{id}/push")
    public Result<?> pushSystemNotification(@PathVariable String id) {
        boolean success = systemNotificationService.pushSystemNotification(id);
        if (success) {
            return Result.ok("系统通知推送成功");
        } else {
            return Result.fail("系统通知推送失败");
        }
    }
}




