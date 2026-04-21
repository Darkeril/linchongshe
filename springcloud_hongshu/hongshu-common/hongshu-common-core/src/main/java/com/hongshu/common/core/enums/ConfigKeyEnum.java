package com.hongshu.common.core.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统配置键枚举
 */
public enum ConfigKeyEnum {

    // 网站配置
    WEBSITE_RECORD_NUMBER("website.recordNumber", "备案号"),
    WEBSITE_LOGO("website.logo", "网站LOGO"),
    WEBSITE_NAME("website.name", "网站名称"),
    WEBSITE_AUTHOR("website.author", "网站作者"),
    WEBSITE_TITLE("website.title", "网站标题"),
    WEBSITE_DESCRIPTION("website.description", "网站描述"),
    WEBSITE_COPYRIGHT("website.copyright", "版权申明"),
    WEBSITE_AI("website.aiUrl", "AI路径"),
    WEBSITE_WECHAT_QR_CODE("website.wechatQrCode", "微信二维码"),
    WEBSITE_ALIPAY_REWARD_QR_CODE("website.alipayRewardQrCode", "支付宝打赏二维码"),
    WEBSITE_WECHAT_REWARD_QR_CODE("website.wechatRewardQrCode", "微信打赏二维码"),
    WEBSITE_FOLLOW_US("website.followUs", "关注我们"),

    // 系统配置
    OSS_TYPE("oss.type", "OSS存储类型"),
    SMS_PRIMARY("sms.primary", "短信服务商优先级"),
    MQ_TYPE("mq.type", "消息队列类型"),
    QUERY_PRIMARY("query.primary", "搜索类型优先级"),
    AMAP_ENABLED("amap.enabled", "高德地图是否启用"),
    BLACKLIST_ENABLED("blacklist.enabled", "黑名单功能开关"),
    CONTENT_AUDIT_ENABLED("content.audit.enabled", "内容审核功能开关"),
    PRODUCT_AUDIT_ENABLED("product.audit.enabled", "商品审核功能开关"),
    USER_AUDIT_ENABLED("user.audit.enabled", "用户审核功能开关"),
    COMMENT_AUDIT_ENABLED("comment.audit.enabled", "评论审核功能开关"),

    // 百度千帆审核配置
    BAIDU_QIANFAN_ENABLED("baidu.qianfan.enabled", "百度千帆审核开关"),
    BAIDU_QIANFAN_ACCESS_KEY("baidu.qianfan.access.key", "百度千帆AccessKey"),
    BAIDU_QIANFAN_SECRET_KEY("baidu.qianfan.secret.key", "百度千帆SecretKey"),
    BAIDU_QIANFAN_TEXT_ENDPOINT("baidu.qianfan.text.endpoint", "文本审核接口地址"),
    BAIDU_QIANFAN_IMAGE_ENDPOINT("baidu.qianfan.image.endpoint", "图片审核接口地址"),
    BAIDU_QIANFAN_VIDEO_ENDPOINT("baidu.qianfan.video.endpoint", "视频审核接口地址"),
    
    // 审核模式配置
    AUDIT_MODE("audit.mode", "审核模式：manual=人工，auto=自动，hybrid=混合"),
    AUTO_AUDIT_THRESHOLD("auto.audit.threshold", "自动审核阈值"),

    // 本地存储配置
    LOCAL_PICTURE_BASE_URL("local.picture.base.url", "本地文件访问域名"),
    FILE_UPLOAD_PATH("file.upload.path", "文件上传路径"),
    LOCAL_STORAGE_ENABLED("local.storage.enabled", "本地存储是否启用"),

    // 验证码配置
    CAPTCHA_CACHE_TYPE("aj.captcha.cache-type", "验证码缓存类型"),
    CAPTCHA_TYPE("aj.captcha.type", "验证码类型"),
    CAPTCHA_WATER_MARK("aj.captcha.water-mark", "水印文字"),
    CAPTCHA_SLIP_OFFSET("aj.captcha.slip-offset", "滑动拼图误差偏移量"),
    CAPTCHA_AES_STATUS("aj.captcha.aes-status", "AES加密状态"),
    CAPTCHA_INTERFERENCE_OPTIONS("aj.captcha.interference-options", "滑动干扰项"),
    CAPTCHA_JIGSAW("aj.captcha.jigsaw", "滑动图片路径"),
    CAPTCHA_PIC_CLICK("aj.captcha.pic-click", "点击图片路径"),
    CAPTCHA_ENABLED("aj.captcha.enabled", "验证码功能开关"),

    // 高德地图配置
    AMAP_KEY("amap.key", "高德地图key"),
    AMAP_SECURITY_KEY("amap.security-key", "高德地图安全密钥"),

    // 演示账号配置
    DEMO_ACCOUNT_ENABLED("demo.account.enabled", "演示账号功能开关"),
    DEMO_ACCOUNT_USERNAME("demo.account.username", "演示账号用户名"),
    DEMO_ACCOUNT_PASSWORD("demo.account.password", "演示账号密码"),
    DEMO_ACCOUNT_DESCRIPTION("demo.account.description", "演示账号描述"),
    DEMO_ACCOUNT_PERMISSIONS("demo.account.permissions", "演示账号权限范围"),
    DEMO_ACCOUNT_EXPIRE_TIME("demo.account.expireTime", "演示账号有效期"),

    // 演示站配置
    DEMO_SITE_ENABLED("demo.site.enabled", "演示站功能开关"),
    DEMO_SITE_MOBILE_URL("demo.site.mobileUrl", "移动端演示站地址"),
    DEMO_SITE_WEB_URL("demo.site.webUrl", "Web端演示站地址"),
    DEMO_SITE_ADMIN_URL("demo.site.adminUrl", "管理端演示站地址"),
    DEMO_SITE_GITEE_URL("demo.site.giteeUrl", "Gitee项目地址"),
    DEMO_SITE_GITHUB_URL("demo.site.githubUrl", "GitHub项目地址"),
    DEMO_SITE_DOC_URL("demo.site.docUrl", "文档地址"),
    DEMO_SITE_ARCO_ADMIN_URL("demo.site.arcoAdminUrl", "Arco管理端演示站地址"),
    DEMO_SITE_DESCRIPTION("demo.site.description", "演示站描述信息"),
    DEMO_SITE_ENABLE_STATS("demo.site.enableStats", "演示站访问统计开关"),

    // 微信登录配置
    WECHAT_LOGIN_ENABLED("wechat.login.enabled", "微信登录功能开关"),
    WECHAT_MINIAPP_APPID("wechat.miniapp.appid", "微信小程序AppID"),
    WECHAT_MINIAPP_APPSECRET("wechat.miniapp.appsecret", "微信小程序AppSecret"),
    WECHAT_WEB_APPID("wechat.web.appid", "微信网页应用AppID"),
    WECHAT_WEB_APPSECRET("wechat.web.appsecret", "微信网页应用AppSecret"),
    WECHAT_WEB_CALLBACK_URL("wechat.web.callback.url", "微信网页应用回调地址"),

    // IM 微信小程序配置（与主小程序独立）
    WECHAT_IM_LOGIN_ENABLED("wechat.im.login.enabled", "IM微信登录功能开关"),
    WECHAT_IM_MINIAPP_APPID("wechat.im.miniapp.appid", "IM微信小程序AppID"),
    WECHAT_IM_MINIAPP_APPSECRET("wechat.im.miniapp.appsecret", "IM微信小程序AppSecret"),

    // 市集轮播图（JSON 数组存储）
    CAROUSEL_MALL("carousel.mall", "市集页轮播图配置"),

    /** 管理端 Arco 工作台右侧轮播图（JSON 数组，结构与市集轮播单项一致） */
    CAROUSEL_WORKBENCH("carousel.workbench", "管理端工作台轮播图配置"),

    /** 管理端 Arco 登录页左侧圆形视频/图片区（JSON 数组，建议 7 条） */
    ADMIN_LOGIN_PROMO("admin.login.promo", "管理端登录页圆形展示区配置"),

    // 自动封禁配置
    AUTOBAN_ENABLED("blacklist.autoban.enabled", "自动封禁总开关"),
    AUTOBAN_FAIL_THRESHOLD("blacklist.autoban.failThreshold", "登录失败次数阈值"),
    AUTOBAN_SUCCESS_THRESHOLD("blacklist.autoban.successThreshold", "登录成功次数阈值"),
    AUTOBAN_TIME_WINDOW_MINUTES("blacklist.autoban.timeWindowMinutes", "检测时间窗口(分钟)"),
    AUTOBAN_BAN_HOURS("blacklist.autoban.banHours", "自动封禁时长(小时，0=永久)");


    private final String key;
    private final String description;

    ConfigKeyEnum(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }


    /**
     * 根据key获取枚举
     */
    public static ConfigKeyEnum fromKey(String key) {
        for (ConfigKeyEnum configKey : values()) {
            if (configKey.getKey().equals(key)) {
                return configKey;
            }
        }
        return null;
    }

    /**
     * 获取网站配置相关的键
     */
    public static List<String> getWebsiteConfigKeys() {
        return Arrays.stream(values())
                .filter(config -> config.getKey().startsWith("website."))
                .map(ConfigKeyEnum::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取系统配置相关的键
     */
//    public static List<String> getSystemConfigKeys() {
//        return Arrays.stream(values())
//                .filter(config -> Arrays.asList("oss.type", "sms.primary", "mq.type",
//                                "query.primary", "amap.enabled", "blacklist.enabled")
//                        .contains(config.getKey()))
//                .map(ConfigKeyEnum::getKey)
//                .collect(Collectors.toList());
//    }
    public static List<String> getSystemConfigKeys() {
        return Arrays.asList(
                OSS_TYPE.getKey(),
                SMS_PRIMARY.getKey(),
                MQ_TYPE.getKey(),
                QUERY_PRIMARY.getKey(),
                AMAP_ENABLED.getKey(),
                BLACKLIST_ENABLED.getKey(),
                CONTENT_AUDIT_ENABLED.getKey(),
                PRODUCT_AUDIT_ENABLED.getKey(),
                USER_AUDIT_ENABLED.getKey(),
                COMMENT_AUDIT_ENABLED.getKey(),
                BAIDU_QIANFAN_ENABLED.getKey(),
                BAIDU_QIANFAN_ACCESS_KEY.getKey(),
                BAIDU_QIANFAN_SECRET_KEY.getKey(),
                BAIDU_QIANFAN_TEXT_ENDPOINT.getKey(),
                BAIDU_QIANFAN_IMAGE_ENDPOINT.getKey(),
                BAIDU_QIANFAN_VIDEO_ENDPOINT.getKey(),
                AUDIT_MODE.getKey(),
                AUTO_AUDIT_THRESHOLD.getKey()
        );
    }

    /**
     * 获取本地存储配置相关的键
     */
    public static List<String> getLocalConfigKeys() {
        return Arrays.stream(values())
                .filter(config -> config.getKey().startsWith("local.") ||
                        config.getKey().equals("file.upload.path"))
                .map(ConfigKeyEnum::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取验证码配置相关的键
     */
    public static List<String> getCaptchaConfigKeys() {
        return Arrays.stream(values())
                .filter(config -> config.getKey().startsWith("aj.captcha."))
                .map(ConfigKeyEnum::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取高德地图配置相关的键
     */
    public static List<String> getAmapConfigKeys() {
        return Arrays.stream(values())
                .filter(config -> config.getKey().startsWith("amap."))
                .map(ConfigKeyEnum::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取演示账号配置相关的键
     */
    public static List<String> getDemoAccountConfigKeys() {
        return Arrays.stream(values())
                .filter(config -> config.getKey().startsWith("demo.account."))
                .map(ConfigKeyEnum::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取百度千帆配置相关的键
     */
    public static List<String> getBaiduQianfanConfigKeys() {
        return Arrays.asList(
            BAIDU_QIANFAN_ENABLED.getKey(),
            BAIDU_QIANFAN_ACCESS_KEY.getKey(),
            BAIDU_QIANFAN_SECRET_KEY.getKey(),
            BAIDU_QIANFAN_TEXT_ENDPOINT.getKey(),
            BAIDU_QIANFAN_IMAGE_ENDPOINT.getKey(),
            BAIDU_QIANFAN_VIDEO_ENDPOINT.getKey(),
            AUDIT_MODE.getKey(),
            AUTO_AUDIT_THRESHOLD.getKey()
        );
    }

    /**
     * 获取演示站配置相关的键
     */
    public static List<String> getDemoSiteConfigKeys() {
        return Arrays.stream(values())
                .filter(config -> config.getKey().startsWith("demo.site."))
                .map(ConfigKeyEnum::getKey)
                .collect(Collectors.toList());
    }
}
