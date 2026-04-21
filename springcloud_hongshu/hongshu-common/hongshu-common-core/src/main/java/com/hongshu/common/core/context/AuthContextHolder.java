package com.hongshu.common.core.context;

/**
 * 用户认证上下文Holder
 * 使用ThreadLocal存储当前线程的用户ID
 *

 */
public class AuthContextHolder {

    private AuthContextHolder() {
        // 工具类，禁止实例化
    }

    // 用户ID
    private static final ThreadLocal<String> userId = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(String userId) {
        AuthContextHolder.userId.set(userId);
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public static String getUserId() {
        return userId.get();
    }

    /**
     * 清除当前用户ID
     */
    public static void removeUserId() {
        userId.remove();
    }
}









