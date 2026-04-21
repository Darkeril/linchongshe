package com.hongshu.web.service.web;

import com.hongshu.web.domain.vo.SystemNotification;

import java.util.List;

/**
 * 系统通知服务接口
 *
 * @author: hongshu
 * @date: 2024-01-15
 * @version: 1.0.0
 */
public interface ISystemNotificationService {

    /**
     * 获取系统通知列表
     */
    List<SystemNotification> getSystemNotificationList();

    /**
     * 添加系统通知
     */
    boolean addSystemNotification(SystemNotification systemNotification);

    /**
     * 更新系统通知
     */
    boolean updateSystemNotification(SystemNotification systemNotification);

    /**
     * 删除系统通知
     */
    boolean deleteSystemNotification(String id);

    /**
     * 推送系统通知
     */
    boolean pushSystemNotification(String id);
}




