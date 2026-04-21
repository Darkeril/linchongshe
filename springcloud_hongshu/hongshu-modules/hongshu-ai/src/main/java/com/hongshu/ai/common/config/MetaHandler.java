package com.hongshu.ai.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.hongshu.ai.common.security.JwtTokenUtils;
import com.hongshu.ai.common.security.UserDetail;
import com.hongshu.common.core.validator.ValidatorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mybatis-Plus字段自动填充
 *
 * @author: myj
 * @date: 2024/4/1
 * @version: 1.0.0
 */
@Component
@Slf4j
public class MetaHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 获取当前登录用户
        String currentUser = getCurrentUser();

        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createUser", String.class, currentUser);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateUser", String.class, currentUser);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 获取当前登录用户
        String currentUser = getCurrentUser();

        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateUser", String.class, currentUser);
    }

    /**
     * 获取当前登录用户
     *
     * @return 当前用户名，如果未登录则返回 "System"
     */
    private String getCurrentUser() {
        try {
            UserDetail userDetail = JwtTokenUtils.getLoginUser();
            if (ValidatorUtil.isNotNull(userDetail)) {
                return userDetail.getUsername();
            }
        } catch (Exception e) {
            log.debug("获取当前登录用户失败，使用默认值: {}", e.getMessage());
        }
        return "System";
    }

}

