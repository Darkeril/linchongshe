package com.hongshu.web.service.web.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.hongshu.common.core.utils.RedisUtils;
import com.hongshu.web.service.web.ISystemNotificationService;
import com.hongshu.web.domain.vo.SystemNotification;
import com.hongshu.web.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 系统通知服务实现类
 *
 * @author: hongshu
 * @date: 2024-01-15
 * @version: 1.0.0
 */
@Service
public class SystemNotificationServiceImpl implements ISystemNotificationService {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private WebSocketServer webSocketServer;

    private static final String NOTIFICATION_LIST_KEY = "system:notification:list";
    private static final String NOTIFICATION_KEY_PREFIX = "system:notification:";

    @Override
    public List<SystemNotification> getSystemNotificationList() {
        try {
            // 从Redis获取所有系统通知的ID列表
            Set<String> notificationIds = redisUtils.setMembers(NOTIFICATION_LIST_KEY);
            List<SystemNotification> list = new ArrayList<>();
            
            if (notificationIds != null && !notificationIds.isEmpty()) {
                for (String id : notificationIds) {
                    String notificationJson = redisUtils.get(NOTIFICATION_KEY_PREFIX + id);
                    if (notificationJson != null) {
                        SystemNotification notification = JSONUtil.toBean(notificationJson, SystemNotification.class);
                        list.add(notification);
                    }
                }
            }
            
            // 按创建时间倒序排列
            list.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public boolean addSystemNotification(SystemNotification systemNotification) {
        try {
            systemNotification.applyContentBase64IfPresent();
            // 生成唯一ID
            String id = IdUtil.simpleUUID();
            systemNotification.setId(id);
            systemNotification.setTimestamp(System.currentTimeMillis());
            
            // 保存到Redis
            String notificationJson = JSONUtil.toJsonStr(systemNotification);
            redisUtils.set(NOTIFICATION_KEY_PREFIX + id, notificationJson);
            redisUtils.sAdd(NOTIFICATION_LIST_KEY, id);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateSystemNotification(SystemNotification systemNotification) {
        try {
            systemNotification.applyContentBase64IfPresent();
            if (systemNotification.getId() == null || systemNotification.getId().isEmpty()) {
                return false;
            }
            
            // 更新Redis中的数据
            systemNotification.setTimestamp(System.currentTimeMillis());
            String notificationJson = JSONUtil.toJsonStr(systemNotification);
            redisUtils.set(NOTIFICATION_KEY_PREFIX + systemNotification.getId(), notificationJson);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteSystemNotification(String id) {
        try {
            // 从Redis中删除
            redisUtils.delete(NOTIFICATION_KEY_PREFIX + id);
            redisUtils.sRemove(NOTIFICATION_LIST_KEY, id);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean pushSystemNotification(String id) {
        try {
            // 从Redis获取通知信息
            String notificationJson = redisUtils.get(NOTIFICATION_KEY_PREFIX + id);
            if (notificationJson == null) {
                return false;
            }
            
            SystemNotification notification = JSONUtil.toBean(notificationJson, SystemNotification.class);
            if (!notification.getEnabled()) {
                return false;
            }
            
            // 通过WebSocket推送系统通知（含可选配图）
            webSocketServer.sendSystemNotification(
                    notification.getContent(),
                    notification.getImageUrl());
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
