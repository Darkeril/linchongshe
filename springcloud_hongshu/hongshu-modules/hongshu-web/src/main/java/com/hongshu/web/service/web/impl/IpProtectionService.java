package com.hongshu.web.service.web.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hongshu.common.core.enums.ConfigKeyEnum;
import com.hongshu.common.core.utils.RedisUtils;
import com.hongshu.system.api.RemoteNoticeService;
import com.hongshu.web.domain.dto.AutobanConfigDTO;
import com.hongshu.web.domain.entity.WebIpBlacklist;
import com.hongshu.web.domain.entity.WebSystemConfig;
import com.hongshu.web.mapper.sys.SysSystemConfigMapper;
import com.hongshu.web.mapper.web.WebIpBlacklistMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * IP保护服务：实时检测登录频率，超阈值自动封禁
 */
@Slf4j
@Service
public class IpProtectionService {

    private static final String IP_LOGIN_FAIL_PREFIX = "ip:login:fail:";
    private static final String IP_LOGIN_SUCCESS_PREFIX = "ip:login:success:";

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private WebIpBlacklistMapper ipBlacklistMapper;

    @Autowired
    private SysSystemConfigMapper systemConfigMapper;

    @Autowired
    private RemoteNoticeService remoteNoticeService;

    /**
     * 记录登录尝试并检查是否需要自动封禁
     *
     * @param ip      客户端IP
     * @param success 是否登录成功
     */
    public void recordAndCheck(String ip, boolean success) {
        AutobanConfigDTO config = getAutobanConfig();
        if (!Boolean.TRUE.equals(config.getEnabled())) {
            return;
        }

        String key = success ? IP_LOGIN_SUCCESS_PREFIX + ip : IP_LOGIN_FAIL_PREFIX + ip;
        Long count = redisUtils.incrBy(key, 1);

        if (count == 1) {
            redisUtils.expire(key, config.getTimeWindowMinutes(), TimeUnit.MINUTES);
        }

        int threshold = success ? config.getSuccessThreshold() : config.getFailThreshold();
        if (count >= threshold) {
            autoBan(ip, success, count, config);
        }
    }

    /**
     * 执行自动封禁
     */
    private void autoBan(String ip, boolean success, long count, AutobanConfigDTO config) {
        boolean alreadyBanned = ipBlacklistMapper.exists(
                new LambdaQueryWrapper<WebIpBlacklist>()
                        .eq(WebIpBlacklist::getIpAddress, ip)
                        .eq(WebIpBlacklist::getStatus, "1")
        );
        if (alreadyBanned) {
            return;
        }

        String type = success ? "登录成功" : "登录失败";
        String reason = String.format("[自动] %d分钟内%s%d次，触发自动封禁",
                config.getTimeWindowMinutes(), type, count);

        WebIpBlacklist blacklistItem = new WebIpBlacklist();
        blacklistItem.setIpAddress(ip);
        blacklistItem.setReason(reason);
        blacklistItem.setStatus("1");
        blacklistItem.setBanTime(new Date());
        blacklistItem.setCreateTime(new Date());
        blacklistItem.setUpdateTime(new Date());

        if (config.getBanHours() != null && config.getBanHours() > 0) {
            blacklistItem.setExpireTime(LocalDateTime.now().plusHours(config.getBanHours()));
        }

        try {
            ipBlacklistMapper.insert(blacklistItem);
            log.warn("IP自动封禁: ip={}, reason={}", ip, reason);

            sendAutobanNotice(ip, reason, config);
        } catch (Exception e) {
            log.error("IP自动封禁写入失败: ip={}", ip, e);
        }
    }

    /**
     * 向管理端通知公告发送自动封禁告警
     */
    private void sendAutobanNotice(String ip, String reason, AutobanConfigDTO config) {
        try {
            String banDuration = (config.getBanHours() != null && config.getBanHours() > 0)
                    ? config.getBanHours() + "小时"
                    : "永久";
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String content = String.format(
                    "<p><b>IP地址：</b>%s</p>" +
                    "<p><b>触发规则：</b>%s</p>" +
                    "<p><b>封禁时长：</b>%s</p>" +
                    "<p><b>封禁时间：</b>%s</p>" +
                    "<p>请前往 系统配置 → 黑名单配置 查看详情。</p>",
                    ip, reason, banDuration, now);
            remoteNoticeService.addNotice("IP自动封禁告警：" + ip, "3", content);
        } catch (Exception e) {
            log.warn("自动封禁通知发送失败（不影响封禁）: ip={}", ip, e);
        }
    }

    /**
     * 读取自动封禁配置
     */
    public AutobanConfigDTO getAutobanConfig() {
        List<String> keys = Arrays.asList(
                ConfigKeyEnum.AUTOBAN_ENABLED.getKey(),
                ConfigKeyEnum.AUTOBAN_FAIL_THRESHOLD.getKey(),
                ConfigKeyEnum.AUTOBAN_SUCCESS_THRESHOLD.getKey(),
                ConfigKeyEnum.AUTOBAN_TIME_WINDOW_MINUTES.getKey(),
                ConfigKeyEnum.AUTOBAN_BAN_HOURS.getKey()
        );

        LambdaQueryWrapper<WebSystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WebSystemConfig::getConfigKey, keys);
        List<WebSystemConfig> configs = systemConfigMapper.selectList(wrapper);

        AutobanConfigDTO dto = new AutobanConfigDTO();
        dto.setEnabled(false);
        dto.setFailThreshold(10);
        dto.setSuccessThreshold(30);
        dto.setTimeWindowMinutes(60);
        dto.setBanHours(24);

        for (WebSystemConfig c : configs) {
            ConfigKeyEnum ck = ConfigKeyEnum.fromKey(c.getConfigKey());
            if (ck == null) continue;
            try {
                switch (ck) {
                    case AUTOBAN_ENABLED:
                        dto.setEnabled("1".equals(c.getConfigValue()));
                        break;
                    case AUTOBAN_FAIL_THRESHOLD:
                        dto.setFailThreshold(Integer.parseInt(c.getConfigValue()));
                        break;
                    case AUTOBAN_SUCCESS_THRESHOLD:
                        dto.setSuccessThreshold(Integer.parseInt(c.getConfigValue()));
                        break;
                    case AUTOBAN_TIME_WINDOW_MINUTES:
                        dto.setTimeWindowMinutes(Integer.parseInt(c.getConfigValue()));
                        break;
                    case AUTOBAN_BAN_HOURS:
                        dto.setBanHours(Integer.parseInt(c.getConfigValue()));
                        break;
                    default:
                        break;
                }
            } catch (NumberFormatException e) {
                log.warn("自动封禁配置值解析失败: key={}, value={}", c.getConfigKey(), c.getConfigValue());
            }
        }
        return dto;
    }

    /**
     * 更新自动封禁配置
     */
    public void updateAutobanConfig(AutobanConfigDTO dto) {
        updateConfigValue(ConfigKeyEnum.AUTOBAN_ENABLED.getKey(),
                Boolean.TRUE.equals(dto.getEnabled()) ? "1" : "0");
        if (dto.getFailThreshold() != null) {
            updateConfigValue(ConfigKeyEnum.AUTOBAN_FAIL_THRESHOLD.getKey(),
                    String.valueOf(dto.getFailThreshold()));
        }
        if (dto.getSuccessThreshold() != null) {
            updateConfigValue(ConfigKeyEnum.AUTOBAN_SUCCESS_THRESHOLD.getKey(),
                    String.valueOf(dto.getSuccessThreshold()));
        }
        if (dto.getTimeWindowMinutes() != null) {
            updateConfigValue(ConfigKeyEnum.AUTOBAN_TIME_WINDOW_MINUTES.getKey(),
                    String.valueOf(dto.getTimeWindowMinutes()));
        }
        if (dto.getBanHours() != null) {
            updateConfigValue(ConfigKeyEnum.AUTOBAN_BAN_HOURS.getKey(),
                    String.valueOf(dto.getBanHours()));
        }
    }

    private void updateConfigValue(String configKey, String configValue) {
        LambdaQueryWrapper<WebSystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WebSystemConfig::getConfigKey, configKey);
        WebSystemConfig config = systemConfigMapper.selectOne(wrapper);

        if (config != null) {
            config.setConfigValue(configValue);
            config.setUpdateTime(new Date());
            systemConfigMapper.updateById(config);
        } else {
            WebSystemConfig newConfig = new WebSystemConfig();
            newConfig.setConfigKey(configKey);
            newConfig.setConfigValue(configValue);
            newConfig.setConfigGroup("blacklist");
            newConfig.setCreateTime(new Date());
            newConfig.setUpdateTime(new Date());
            systemConfigMapper.insert(newConfig);
        }
    }
}
