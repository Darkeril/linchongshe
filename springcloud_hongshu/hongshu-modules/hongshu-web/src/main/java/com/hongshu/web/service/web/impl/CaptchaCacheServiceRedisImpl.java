package com.hongshu.web.service.web.impl;

//import com.anji.captcha.service.CaptchaCacheService;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.core.script.DefaultRedisScript;
//import org.springframework.data.redis.core.script.RedisScript;
//
//import java.util.Collections;
//import java.util.concurrent.TimeUnit;
//
///**
// * 对于分布式部署的应用，我们建议应用自己实现CaptchaCacheService，比如用Redis，参考service/spring-boot代码示例。
// * 如果应用是单点的，也没有使用redis，那默认使用内存。
// * 内存缓存只适合单节点部署的应用，否则验证码生产与验证在节点之间信息不同步，导致失败。
// * <p>
// * ☆☆☆ SPI： 在resources目录新建META-INF.services文件夹(两层)，参考当前服务resources。
// *
// * @author Devli
// * @Title: 使用redis缓存
// * @date 2020-05-12
// */
//public class CaptchaCacheServiceRedisImpl implements CaptchaCacheService {
//
//    @Override
//    public String type() {
//        return "redis";
//    }
//
//    private static final String LUA_SCRIPT = "local key = KEYS[1] " +
//            "local incrementValue = tonumber(ARGV[1]) " +
//            "if redis.call('EXISTS', key) == 1 then " +
//            "    return redis.call('INCRBY', key, incrementValue) " +
//            "else " +
//            "    return incrementValue " +
//            "end";
//
//    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
//        this.stringRedisTemplate = stringRedisTemplate;
//    }
//
//    private StringRedisTemplate stringRedisTemplate;
//
//    @Override
//    public void set(String key, String value, long expiresInSeconds) {
//        stringRedisTemplate.opsForValue().set(key, value, expiresInSeconds, TimeUnit.SECONDS);
//    }
//
//    @Override
//    public boolean exists(String key) {
//        return stringRedisTemplate.hasKey(key);
//    }
//
//    @Override
//    public void delete(String key) {
//        stringRedisTemplate.delete(key);
//    }
//
//    @Override
//    public String get(String key) {
//        return stringRedisTemplate.opsForValue().get(key);
//    }
//
//    @Override
//    public Long increment(String key, long val) {
//        // 执行 Lua 脚本
//        RedisScript<Long> script = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);
//        // 执行 Lua 脚本
//        return stringRedisTemplate.execute(
//                script,
//                Collections.singletonList(key),
//                String.valueOf(val)
//        );
//    }
//
//    @Override
//    public void setExpire(String key, long l) {
//        stringRedisTemplate.expire(key, l, TimeUnit.SECONDS);
//    }
//}


import com.anji.captcha.service.CaptchaCacheService;
import com.hongshu.common.core.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * AJ-Captcha 缓存服务的 Redis 实现 (最终修复版)
 * 通过 ApplicationContextAware 获取 Spring 容器中的 RedisUtils 实例，
 * 并将所有缓存操作委托给 RedisUtils。
 */
@Component
public class CaptchaCacheServiceRedisImpl implements CaptchaCacheService, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(CaptchaCacheServiceRedisImpl.class);

    private static ApplicationContext applicationContext;

    // 使用 volatile 确保多线程可见性
    private volatile RedisUtils redisUtils;

    @Override
    public String type() {
        return "redis";
    }

    /**
     * 通过双重检查锁定（DCL）懒汉式加载 RedisUtils Bean，确保线程安全且高效。
     */
    private RedisUtils getRedisUtils() {
        if (redisUtils == null) {
            synchronized (this) {
                if (redisUtils == null) {
                    if (applicationContext == null) {
                        log.error("!!! ApplicationContext is NULL. CaptchaCacheServiceRedisImpl was not initialized by Spring. Ensure it's under a @ComponentScan path.");
                        throw new IllegalStateException("ApplicationContext not initialized");
                    }
                    log.info("Captcha service is lazily initializing RedisUtils from Spring Context...");
                    // 从 Spring 容器中获取你项目中的 RedisUtils 实例
                    this.redisUtils = applicationContext.getBean(RedisUtils.class);
                    log.info("RedisUtils bean obtained successfully for captcha service.");
                }
            }
        }
        return redisUtils;
    }

    @Override
    public void set(String key, String value, long expiresInSeconds) {
        log.info("CACHE SET: key=[{}], ttl=[{}]s", key, expiresInSeconds);
        try {
            // 委托给 RedisUtils 的 setEx 方法
            getRedisUtils().setEx(key, value, expiresInSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("CACHE SET: Failed for key [{}]", key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        log.info("CACHE EXISTS: Checking for key=[{}]", key);
        try {
            return getRedisUtils().hasKey(key);
        } catch (Exception e) {
            log.error("CACHE EXISTS: Failed for key [{}]", key, e);
            return false;
        }
    }

    @Override
    public void delete(String key) {
        log.info("CACHE DELETE: key=[{}]", key);
        try {
            getRedisUtils().delete(key);
        } catch (Exception e) {
            log.error("CACHE DELETE: Failed for key [{}]", key, e);
        }
    }

    @Override
    public String get(String key) {
        log.info("CACHE GET: key=[{}]", key);
        try {
            String value = getRedisUtils().get(key);
            log.info("CACHE GET: For key=[{}], found value=[{}]", key, value);
            return value;
        } catch (Exception e) {
            log.error("CACHE GET: Failed for key [{}]", key, e);
            return null;
        }
    }

    @Override
    public Long increment(String key, long val) {
        log.info("CACHE INCREMENT: key=[{}], value=[{}]", key, val);
        try {
            return getRedisUtils().incrBy(key, val);
        } catch (Exception e) {
            log.error("CACHE INCREMENT: Failed for key [{}]", key, e);
            return -1L;
        }
    }

    @Override
    public void setExpire(String key, long l) {
        log.info("CACHE SET_EXPIRE: key=[{}], ttl=[{}]s", key, l);
        try {
            getRedisUtils().expire(key, l, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("CACHE SET_EXPIRE: Failed for key [{}]", key, e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        // 当 Spring 容器创建这个 Bean 时，会自动调用此方法注入上下文
        if (CaptchaCacheServiceRedisImpl.applicationContext == null) {
            log.info("ApplicationContext injected into CaptchaCacheServiceRedisImpl.");
            CaptchaCacheServiceRedisImpl.applicationContext = context;
        }
    }
}
