package com.hongshu.web.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.hongshu.web.mapper.sys.SysSystemConfigMapper;
import com.hongshu.web.domain.entity.WebSystemConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hongshu.common.core.utils.RedisUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

/**
 * 微信工具类
 *
 * @author: hongshu
 */
@Slf4j
@Component
public class WeChatUtil {

    @Autowired
    private SysSystemConfigMapper systemConfigMapper;

    @Autowired
    private RedisUtils redisUtils;

    // 微信API地址
    private static final String WECHAT_API_URL = "https://api.weixin.qq.com/sns/jscode2session";
    // 微信网页授权API地址
    private static final String WECHAT_WEB_AUTH_URL = "https://open.weixin.qq.com/connect/qrconnect";
    private static final String WECHAT_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";
    private static final String WECHAT_USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo";
    // 微信JS-SDK相关API
    private static final String WECHAT_ACCESS_TOKEN_API = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String WECHAT_JSAPI_TICKET_API = "https://api.weixin.qq.com/cgi-bin/ticket/getticket";
    
    // Redis缓存Key前缀
    private static final String REDIS_KEY_ACCESS_TOKEN = "wechat:jsapi:access_token:";
    private static final String REDIS_KEY_JSAPI_TICKET = "wechat:jsapi:jsapi_ticket:";

    /**
     * 通过code获取微信openid和session_key
     *
     * @param code 微信登录code
     * @return 包含openid和session_key的Map
     */
    public Map<String, String> getWeChatOpenId(String code) {
        Map<String, String> result = new HashMap<>();

        try {
            // 1. 从配置表中获取微信小程序配置
            String appId = getWeChatConfig("wechat.miniapp.appid");
            String appSecret = getWeChatConfig("wechat.miniapp.appsecret");

            if (StringUtils.isBlank(appId) || StringUtils.isBlank(appSecret)) {
                log.error("微信小程序配置未设置: appId={}, appSecret={}", appId, appSecret);
                throw new RuntimeException("微信小程序配置未设置，请联系管理员");
            }

            // 2. 构建请求URL
            String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    WECHAT_API_URL, appId, appSecret, code);

            log.info("调用微信API获取openid: url={}", url.replace(appSecret, "***"));

            // 3. 使用HttpUtil调用微信API（使用Hutool工具类）
            cn.hutool.http.HttpResponse response = cn.hutool.http.HttpUtil.createGet(url).execute();
            String responseBody = response.body();

            log.info("微信API响应: {}", responseBody);

            // 4. 解析响应
            JSONObject jsonObject = JSON.parseObject(responseBody);

            // 5. 检查错误
            if (jsonObject.containsKey("errcode")) {
                Integer errcode = jsonObject.getInteger("errcode");
                String errmsg = jsonObject.getString("errmsg");
                log.error("微信API返回错误: errcode={}, errmsg={}", errcode, errmsg);
                throw new RuntimeException("微信登录失败: " + errmsg);
            }

            // 6. 获取openid和session_key
            String openid = jsonObject.getString("openid");
            String sessionKey = jsonObject.getString("session_key");

            if (StringUtils.isBlank(openid)) {
                log.error("微信API返回的openid为空");
                throw new RuntimeException("微信登录失败: 获取openid失败");
            }

            result.put("openid", openid);
            result.put("session_key", sessionKey);

            log.info("成功获取微信openid: openid={}", openid);

        } catch (Exception e) {
            log.error("获取微信openid失败: code={}", code, e);
            throw new RuntimeException("获取微信openid失败: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * 从配置表中获取微信配置
     *
     * @param configKey 配置键
     * @return 配置值
     */
    private String getWeChatConfig(String configKey) {
        try {
            WebSystemConfig config = systemConfigMapper.selectOne(
                    new LambdaQueryWrapper<WebSystemConfig>()
                            .eq(WebSystemConfig::getConfigKey, configKey)
            );

            if (config != null && StringUtils.isNotBlank(config.getConfigValue())) {
                return config.getConfigValue();
            }

            log.warn("微信配置不存在: configKey={}", configKey);
            return null;
        } catch (Exception e) {
            log.error("获取微信配置失败: configKey={}", configKey, e);
            return null;
        }
    }

    /**
     * 生成微信网页授权URL
     *
     * @param redirectUri 回调地址
     * @param state 状态参数（可选，用于防止CSRF攻击）
     * @return 授权URL
     */
    public String generateWeChatAuthUrl(String redirectUri, String state) {
        try {
            // 从配置表中获取微信开放平台配置（网页应用）
            String appId = getWeChatConfig("wechat.web.appid");
            
            if (StringUtils.isBlank(appId)) {
                log.error("微信网页应用配置未设置: appId为空");
                throw new RuntimeException("微信网页应用配置未设置，请联系管理员");
            }

            // 如果没有传入state，生成一个随机state
            if (StringUtils.isBlank(state)) {
                state = java.util.UUID.randomUUID().toString().replace("-", "");
            }

            // redirectUri已经在Controller层处理，这里直接使用

            // URL编码redirectUri（微信要求）
            String encodedRedirectUri = java.net.URLEncoder.encode(redirectUri, "UTF-8");

            // 构建授权URL
            // scope=snsapi_login 表示使用网页授权登录
            String url = String.format("%s?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s#wechat_redirect",
                    WECHAT_WEB_AUTH_URL, appId, encodedRedirectUri, state);

            log.info("生成微信授权URL: redirectUri={}, encodedRedirectUri={}, state={}", redirectUri, encodedRedirectUri, state);
            return url;
        } catch (Exception e) {
            log.error("生成微信授权URL失败: redirectUri={}", redirectUri, e);
            throw new RuntimeException("生成微信授权URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过code获取微信网页授权的access_token和openid
     *
     * @param code 授权码
     * @return 包含access_token、openid等信息的Map
     */
    public Map<String, String> getWeChatWebAccessToken(String code) {
        Map<String, String> result = new HashMap<>();

        try {
            // 1. 从配置表中获取微信开放平台配置（网页应用）
            String appId = getWeChatConfig("wechat.web.appid");
            String appSecret = getWeChatConfig("wechat.web.appsecret");

            if (StringUtils.isBlank(appId) || StringUtils.isBlank(appSecret)) {
                log.error("微信网页应用配置未设置: appId={}, appSecret={}", appId, appSecret);
                throw new RuntimeException("微信网页应用配置未设置，请联系管理员");
            }

            // 2. 构建请求URL
            String url = String.format("%s?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                    WECHAT_ACCESS_TOKEN_URL, appId, appSecret, code);

            log.info("调用微信API获取access_token: url={}", url.replace(appSecret, "***"));

            // 3. 使用HttpUtil调用微信API
            cn.hutool.http.HttpResponse response = cn.hutool.http.HttpUtil.createGet(url).execute();
            String responseBody = response.body();

            log.info("微信API响应: {}", responseBody);

            // 4. 解析响应
            JSONObject jsonObject = JSON.parseObject(responseBody);

            // 5. 检查错误
            if (jsonObject.containsKey("errcode")) {
                Integer errcode = jsonObject.getInteger("errcode");
                String errmsg = jsonObject.getString("errmsg");
                log.error("微信API返回错误: errcode={}, errmsg={}", errcode, errmsg);
                throw new RuntimeException("微信登录失败: " + errmsg);
            }

            // 6. 获取access_token和openid
            String accessToken = jsonObject.getString("access_token");
            String openid = jsonObject.getString("openid");
            String refreshToken = jsonObject.getString("refresh_token");
            String expiresIn = jsonObject.getString("expires_in");

            if (StringUtils.isBlank(accessToken) || StringUtils.isBlank(openid)) {
                log.error("微信API返回的access_token或openid为空");
                throw new RuntimeException("微信登录失败: 获取access_token或openid失败");
            }

            result.put("access_token", accessToken);
            result.put("openid", openid);
            result.put("refresh_token", refreshToken);
            result.put("expires_in", expiresIn);

            log.info("成功获取微信access_token: openid={}", openid);

        } catch (Exception e) {
            log.error("获取微信access_token失败: code={}", code, e);
            throw new RuntimeException("获取微信access_token失败: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * 通过access_token和openid获取微信用户信息
     *
     * @param accessToken 访问令牌
     * @param openid 用户唯一标识
     * @return 包含用户信息的Map
     */
    public Map<String, Object> getWeChatUserInfo(String accessToken, String openid) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 构建请求URL
            String url = String.format("%s?access_token=%s&openid=%s",
                    WECHAT_USER_INFO_URL, accessToken, openid);

            log.info("调用微信API获取用户信息: openid={}", openid);

            // 2. 使用HttpUtil调用微信API
            cn.hutool.http.HttpResponse response = cn.hutool.http.HttpUtil.createGet(url).execute();
            String responseBody = response.body();

            log.info("微信用户信息API响应: {}", responseBody);

            // 3. 解析响应
            JSONObject jsonObject = JSON.parseObject(responseBody);

            // 4. 检查错误
            if (jsonObject.containsKey("errcode")) {
                Integer errcode = jsonObject.getInteger("errcode");
                String errmsg = jsonObject.getString("errmsg");
                log.error("微信API返回错误: errcode={}, errmsg={}", errcode, errmsg);
                throw new RuntimeException("获取微信用户信息失败: " + errmsg);
            }

            // 5. 获取用户信息
            result.put("openid", jsonObject.getString("openid"));
            result.put("nickname", jsonObject.getString("nickname"));
            result.put("headimgurl", jsonObject.getString("headimgurl"));
            result.put("sex", jsonObject.getInteger("sex")); // 1-男，2-女，0-未知
            result.put("province", jsonObject.getString("province"));
            result.put("city", jsonObject.getString("city"));
            result.put("country", jsonObject.getString("country"));

            log.info("成功获取微信用户信息: openid={}, nickname={}", openid, jsonObject.getString("nickname"));

        } catch (Exception e) {
            log.error("获取微信用户信息失败: accessToken={}, openid={}", accessToken, openid, e);
            throw new RuntimeException("获取微信用户信息失败: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * 通过手机号授权code获取用户手机号
     * 
     * @param phoneCode 手机号授权code（通过getPhoneNumber获取）
     * @return 手机号
     */
    public String getPhoneNumber(String phoneCode) {
        try {
            // 1. 从配置表中获取微信小程序配置
            String appId = getWeChatConfig("wechat.miniapp.appid");
            String appSecret = getWeChatConfig("wechat.miniapp.appsecret");

            if (StringUtils.isBlank(appId) || StringUtils.isBlank(appSecret)) {
                log.error("微信小程序配置未设置: appId={}, appSecret={}", appId, appSecret);
                throw new RuntimeException("微信小程序配置未设置，请联系管理员");
            }

            // 2. 获取access_token
            String accessTokenUrl = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                    appId, appSecret);
            
            cn.hutool.http.HttpResponse tokenResponse = cn.hutool.http.HttpUtil.createGet(accessTokenUrl).execute();
            String tokenResponseBody = tokenResponse.body();
            JSONObject tokenJson = JSON.parseObject(tokenResponseBody);
            
            if (tokenJson.containsKey("errcode")) {
                Integer errcode = tokenJson.getInteger("errcode");
                String errmsg = tokenJson.getString("errmsg");
                log.error("获取access_token失败: errcode={}, errmsg={}", errcode, errmsg);
                throw new RuntimeException("获取access_token失败: " + errmsg);
            }
            
            String accessToken = tokenJson.getString("access_token");
            if (StringUtils.isBlank(accessToken)) {
                log.error("access_token为空");
                throw new RuntimeException("获取access_token失败");
            }

            // 3. 调用微信接口获取手机号
            String phoneUrl = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + accessToken;
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("code", phoneCode);
            
            cn.hutool.http.HttpResponse phoneResponse = cn.hutool.http.HttpUtil.createPost(phoneUrl)
                    .body(requestBody.toJSONString())
                    .contentType("application/json")
                    .execute();
            
            String phoneResponseBody = phoneResponse.body();
            log.info("微信获取手机号API响应: {}", phoneResponseBody);
            
            JSONObject phoneJson = JSON.parseObject(phoneResponseBody);
            
            // 4. 检查错误
            if (phoneJson.containsKey("errcode") && phoneJson.getInteger("errcode") != 0) {
                Integer errcode = phoneJson.getInteger("errcode");
                String errmsg = phoneJson.getString("errmsg");
                log.error("获取手机号失败: errcode={}, errmsg={}", errcode, errmsg);
                throw new RuntimeException("获取手机号失败: " + errmsg);
            }
            
            // 5. 解析手机号
            JSONObject phoneInfo = phoneJson.getJSONObject("phone_info");
            if (phoneInfo == null) {
                log.error("手机号信息为空");
                throw new RuntimeException("获取手机号失败: 手机号信息为空");
            }
            
            String phoneNumber = phoneInfo.getString("purePhoneNumber");
            if (StringUtils.isBlank(phoneNumber)) {
                log.error("手机号为空");
                throw new RuntimeException("获取手机号失败: 手机号为空");
            }
            
            log.info("成功获取手机号: phoneNumber={}", phoneNumber);
            return phoneNumber;
            
        } catch (Exception e) {
            log.error("获取手机号失败: phoneCode={}", phoneCode, e);
            throw new RuntimeException("获取手机号失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取微信JS-SDK签名配置
     *
     * @param url 当前页面的完整URL（去掉hash部分）
     * @return 包含appId、timestamp、nonceStr、signature的Map
     */
    public Map<String, String> getWeChatJSSDKConfig(String url) {
        Map<String, String> result = new HashMap<>();

        try {
            // 1. 从配置表中获取微信公众号配置（网页应用）
            String appId = getWeChatConfig("wechat.web.appid");
            String appSecret = getWeChatConfig("wechat.web.appsecret");

            if (StringUtils.isBlank(appId) || StringUtils.isBlank(appSecret)) {
                log.error("微信网页应用配置未设置: appId={}, appSecret={}", appId, appSecret);
                throw new RuntimeException("微信网页应用配置未设置，请联系管理员");
            }

            // 2. 获取access_token（带缓存）
            String accessToken = getAccessToken(appId, appSecret);

            // 3. 获取jsapi_ticket（带缓存）
            String jsapiTicket = getJsapiTicket(accessToken, appId);

            // 4. 生成随机字符串和时间戳
            String nonceStr = generateNonceStr();
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

            // 5. 生成签名
            String signature = generateSignature(jsapiTicket, nonceStr, timestamp, url);

            // 6. 返回配置
            result.put("appId", appId);
            result.put("timestamp", timestamp);
            result.put("nonceStr", nonceStr);
            result.put("signature", signature);

            log.info("成功生成微信JS-SDK配置: appId={}, url={}", appId, url);

        } catch (Exception e) {
            log.error("获取微信JS-SDK配置失败: url={}", url, e);
            throw new RuntimeException("获取微信JS-SDK配置失败: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * 获取access_token（带缓存）
     *
     * @param appId     公众号AppID
     * @param appSecret 公众号AppSecret
     * @return access_token
     */
    private String getAccessToken(String appId, String appSecret) {
        String cacheKey = REDIS_KEY_ACCESS_TOKEN + appId;
        
        // 先从缓存获取
        String cachedToken = redisUtils.get(cacheKey);
        if (StringUtils.isNotBlank(cachedToken)) {
            log.debug("从缓存获取access_token: appId={}", appId);
            return cachedToken;
        }

        // 缓存不存在，调用微信API获取
        try {
            String url = String.format("%s?grant_type=client_credential&appid=%s&secret=%s",
                    WECHAT_ACCESS_TOKEN_API, appId, appSecret);

            log.info("调用微信API获取access_token: appId={}", appId);

            cn.hutool.http.HttpResponse response = cn.hutool.http.HttpUtil.createGet(url).execute();
            String responseBody = response.body();
            log.debug("微信access_token API响应: {}", responseBody);

            JSONObject jsonObject = JSON.parseObject(responseBody);

            // 检查错误
            if (jsonObject.containsKey("errcode")) {
                Integer errcode = jsonObject.getInteger("errcode");
                String errmsg = jsonObject.getString("errmsg");
                log.error("获取access_token失败: errcode={}, errmsg={}", errcode, errmsg);
                throw new RuntimeException("获取access_token失败: " + errmsg);
            }

            String accessToken = jsonObject.getString("access_token");
            Integer expiresIn = jsonObject.getInteger("expires_in");

            if (StringUtils.isBlank(accessToken)) {
                log.error("access_token为空");
                throw new RuntimeException("获取access_token失败: access_token为空");
            }

            // 缓存access_token（设置过期时间为expiresIn-200秒，提前刷新）
            int cacheTime = (expiresIn != null && expiresIn > 200) ? expiresIn - 200 : 7000;
            redisUtils.setEx(cacheKey, accessToken, cacheTime, TimeUnit.SECONDS);
            log.info("成功获取并缓存access_token: appId={}, expiresIn={}", appId, expiresIn);

            return accessToken;

        } catch (Exception e) {
            log.error("获取access_token失败: appId={}", appId, e);
            throw new RuntimeException("获取access_token失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取jsapi_ticket（带缓存）
     *
     * @param accessToken access_token
     * @param appId       公众号AppID（用于缓存key）
     * @return jsapi_ticket
     */
    private String getJsapiTicket(String accessToken, String appId) {
        String cacheKey = REDIS_KEY_JSAPI_TICKET + appId;

        // 先从缓存获取
        String cachedTicket = redisUtils.get(cacheKey);
        if (StringUtils.isNotBlank(cachedTicket)) {
            log.debug("从缓存获取jsapi_ticket: appId={}", appId);
            return cachedTicket;
        }

        // 缓存不存在，调用微信API获取
        try {
            String url = String.format("%s?type=jsapi&access_token=%s", WECHAT_JSAPI_TICKET_API, accessToken);

            log.info("调用微信API获取jsapi_ticket: appId={}", appId);

            cn.hutool.http.HttpResponse response = cn.hutool.http.HttpUtil.createGet(url).execute();
            String responseBody = response.body();
            log.debug("微信jsapi_ticket API响应: {}", responseBody);

            JSONObject jsonObject = JSON.parseObject(responseBody);

            // 检查错误
            if (jsonObject.containsKey("errcode") && jsonObject.getInteger("errcode") != 0) {
                Integer errcode = jsonObject.getInteger("errcode");
                String errmsg = jsonObject.getString("errmsg");
                log.error("获取jsapi_ticket失败: errcode={}, errmsg={}", errcode, errmsg);
                throw new RuntimeException("获取jsapi_ticket失败: " + errmsg);
            }

            String jsapiTicket = jsonObject.getString("ticket");
            Integer expiresIn = jsonObject.getInteger("expires_in");

            if (StringUtils.isBlank(jsapiTicket)) {
                log.error("jsapi_ticket为空");
                throw new RuntimeException("获取jsapi_ticket失败: jsapi_ticket为空");
            }

            // 缓存jsapi_ticket（设置过期时间为expiresIn-200秒，提前刷新）
            int cacheTime = (expiresIn != null && expiresIn > 200) ? expiresIn - 200 : 7000;
            redisUtils.setEx(cacheKey, jsapiTicket, cacheTime, TimeUnit.SECONDS);
            log.info("成功获取并缓存jsapi_ticket: appId={}, expiresIn={}", appId, expiresIn);

            return jsapiTicket;

        } catch (Exception e) {
            log.error("获取jsapi_ticket失败: appId={}", appId, e);
            throw new RuntimeException("获取jsapi_ticket失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成随机字符串
     *
     * @return 随机字符串
     */
    private String generateNonceStr() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 生成签名
     *
     * @param jsapiTicket jsapi_ticket
     * @param nonceStr    随机字符串
     * @param timestamp   时间戳
     * @param url         当前页面URL
     * @return 签名
     */
    private String generateSignature(String jsapiTicket, String nonceStr, String timestamp, String url) {
        try {
            // 1. 参数排序（字典序）
            TreeMap<String, String> params = new TreeMap<>();
            params.put("jsapi_ticket", jsapiTicket);
            params.put("noncestr", nonceStr);
            params.put("timestamp", timestamp);
            params.put("url", url);

            // 2. 拼接字符串
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            }
            String string1 = sb.toString();

            log.debug("签名原始字符串: {}", string1);

            // 3. SHA1加密
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(string1.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            String signature = hexString.toString();

            log.debug("生成的签名: {}", signature);
            return signature;

        } catch (Exception e) {
            log.error("生成签名失败: jsapiTicket={}, nonceStr={}, timestamp={}, url={}", 
                    jsapiTicket, nonceStr, timestamp, url, e);
            throw new RuntimeException("生成签名失败: " + e.getMessage(), e);
        }
    }
}


















