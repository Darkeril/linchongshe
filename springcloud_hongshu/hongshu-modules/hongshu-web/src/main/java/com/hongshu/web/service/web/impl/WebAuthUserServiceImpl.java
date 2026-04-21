package com.hongshu.web.service.web.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.audit.dto.AutoAuditResult;
import com.hongshu.common.audit.enums.AuditModeEnum;
import com.hongshu.common.audit.enums.AutoAuditResultEnum;
import com.hongshu.common.audit.service.CommonAuditService;
import com.hongshu.common.core.constant.AuthConstant;
import com.hongshu.common.core.constant.Constants;
import com.hongshu.common.core.constant.TokenConstant;
import com.hongshu.common.core.enums.ResultCodeEnum;
import com.hongshu.common.core.enums.TerminalTypeEnum;
import com.hongshu.common.core.exception.HongshuException;
import com.hongshu.common.core.utils.*;
import com.hongshu.common.core.utils.ip.AddressUtils;
import com.hongshu.common.core.utils.ip.IpUtils;
import com.hongshu.web.async.ChatAsyncService;
import com.hongshu.web.async.SmsAsyncService;
import com.hongshu.web.domain.dto.AuthUserDTO;
import com.hongshu.web.domain.dto.WeChatLoginDTO;
import com.hongshu.web.domain.entity.WebIpBlacklist;
import com.hongshu.web.domain.entity.WebSystemConfig;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.domain.vo.QrCodeConfirmVO;
import com.hongshu.web.manager.AsyncManager;
import com.hongshu.web.manager.factory.AsyncFactory;
import com.hongshu.web.mapper.sys.SysSystemConfigMapper;
import com.hongshu.web.mapper.web.WebIpBlacklistMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.service.web.IWebAuthUserService;
import com.hongshu.web.service.sys.ISysSystemConfigService;
import com.hongshu.web.utils.WeChatUtil;
import com.hongshu.web.rocketmq.StatisticsServiceV2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * 权限
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class WebAuthUserServiceImpl extends ServiceImpl<WebUserMapper, WebUser> implements IWebAuthUserService {

    @Resource
    private RedisUtils redisUtils;
    @Autowired
    private WebIpBlacklistMapper ipBlacklistMapper;
    @Autowired
    private SysSystemConfigMapper systemConfigMapper;
    @Autowired(required = false)
    private CaptchaService captchaService;
    @Autowired
    private SmsAsyncService smsAsyncService;
    @Autowired
    private ChatAsyncService chatAsyncService;
    @Autowired
    private ISysSystemConfigService systemConfigService;

    @Autowired
    private WeChatUtil weChatUtil;

    @Autowired
    private StatisticsServiceV2 statisticsServiceV2;

    @Autowired
    private IpProtectionService ipProtectionService;

    @Autowired(required = false)
    private CommonAuditService commonAuditService;

    private Searcher searcher;

    /** 审核日志 content_type，与审核日志页、web_audit_log 约定一致 */
    private static final String AUDIT_CONTENT_TYPE_USER = "user";

    // Redis中验证码的key前缀
    private static final String VERIFY_CODE_PREFIX = "sms:verify:";
    private static final String VERIFY_IP_PREFIX = "sms:ip:";
    // 验证码有效期（分钟）
    private static final long VERIFY_CODE_EXPIRE_MINUTES = 5;
    // 同一手机号发送验证码的间隔时间（秒）
    private static final long SEND_CODE_INTERVAL_SECONDS = 60;

    /**
     * 校验 Web 用户是否允许登录与续期令牌（封禁、待审核）
     */
    private void assertWebUserAllowedLogin(WebUser user) {
        if (user == null) {
            return;
        }
        if ("1".equals(user.getStatus())) {
            throw new HongshuException(ResultCodeEnum.ERROR_ACCOUNT_SUSPENDED);
        }
        if ("2".equals(user.getStatus())) {
            throw new HongshuException(ResultCodeEnum.ERROR_ACCOUNT_PENDING_AUDIT);
        }
    }

    /**
     * 首次自动注册且待审核：不发放 token，由 Controller 返回 517
     */
    private Map<String, Object> buildRegisterPendingAuditMap(WebUser user) {
        Map<String, Object> m = new HashMap<>(4);
        m.put(AuthConstant.REGISTER_PENDING_AUDIT_FLAG, Boolean.TRUE);
        m.put("phone", user.getPhone());
        m.put("username", user.getUsername());
        m.put("id", user.getId());
        return m;
    }

    /**
     * 用户审核开启且当前为待审核时：在开启「内容审核」的前提下调用百度千帆等对昵称/简介/头像等进行自动审核，
     * 写入审核日志；仅当结果为通过时自动将用户状态改为正常，否则仍待人工审核。
     */
    private void runUserRegistrationAutoAuditIfNeeded(WebUser user) {
        if (user == null || !"2".equals(user.getStatus())) {
            return;
        }
        if (commonAuditService == null) {
            log.warn("CommonAuditService 未注入，跳过用户注册自动审核");
            return;
        }
        if (!Boolean.TRUE.equals(commonAuditService.isContentAuditEnabled())) {
            log.debug("内容审核未开启，用户注册不进行自动审核，保持待人工审核");
            return;
        }
        try {
            String text = buildUserAuditText(user);
            List<String> urls = buildUserAuditImageUrls(user);
            AuditModeEnum auditMode = commonAuditService.getAuditMode();
            AutoAuditResult auditResult = commonAuditService.performAuditWithMode(
                    text, urls, AUDIT_CONTENT_TYPE_USER, auditMode);
            commonAuditService.saveAuditLog(user.getId(), AUDIT_CONTENT_TYPE_USER, auditResult);
            if (auditResult.getResult() == AutoAuditResultEnum.PASS) {
                user.setStatus("0");
                user.setUpdateTime(new Date());
                this.updateById(user);
                log.info("用户注册自动审核通过，userId={}", user.getId());
            }
        } catch (Exception e) {
            log.error("用户注册自动审核异常，userId={}", user.getId(), e);
        }
    }

    private String buildUserAuditText(WebUser user) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(user.getUsername())) {
            sb.append(user.getUsername());
        }
        if (StringUtils.isNotBlank(user.getDescription())) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(user.getDescription());
        }
        if (sb.length() == 0) {
            sb.append("user");
        }
        return sb.toString();
    }

    private List<String> buildUserAuditImageUrls(WebUser user) {
        List<String> urls = new ArrayList<>(2);
        if (StringUtils.isNotBlank(user.getAvatar()) && !AuthConstant.DEFAULT_AVATAR.equals(user.getAvatar())) {
            urls.add(user.getAvatar());
        }
        if (StringUtils.isNotBlank(user.getUserCover())
                && !AuthConstant.DEFAULT_COVER.equals(user.getUserCover())
                && !Objects.equals(user.getUserCover(), user.getAvatar())) {
            urls.add(user.getUserCover());
        }
        return urls;
    }

    /**
     * 用户登录
     *
     * @param authUserDTO 用户
     */
    @Override
    @Transactional
    public Map<String, Object> login(AuthUserDTO authUserDTO) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(authUserDTO.getPhone())) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        // 获取验证码配置，判断是否需要滑块校验
        boolean captchaEnabled = this.isCaptchaEnabled();
        // 若未注入验证码服务，则自动跳过验证码校验，避免启动失败
        if (captchaEnabled && captchaService == null) {
            log.warn("验证码服务未注入，跳过验证码校验");
            captchaEnabled = false;
        }
        // 如果验证码功能启用，才进行滑块校验
        if (captchaEnabled) {
            // 校验滑块
            CaptchaVO captchaVO = new CaptchaVO();
            captchaVO.setCaptchaVerification(authUserDTO.getCaptchaVerification());
            ResponseModel response = captchaService.verification(captchaVO);
            log.info("密码登录 - captchaVerification: {}", authUserDTO.getCaptchaVerification());
            log.info("密码登录 - captchaService response: {}", response);
            if (!"0000".equals(response.getRepCode())) {
                throw new HongshuException(ResultCodeEnum.ERROR_BLOCKPUZZLE);
            }
        } else {
            log.info("验证码功能已禁用，跳过滑块校验");
        }
        // 查询用户
        WebUser authUser = this.getOne(new QueryWrapper<WebUser>().eq("phone", authUserDTO.getPhone()));
        // 用户不存在-自动注册新用户
        if (authUser == null) {
            authUser = this.register(authUserDTO);
            if ("2".equals(authUser.getStatus())) {
                return buildRegisterPendingAuditMap(authUser);
            }
        }
        // 用户存在-登录
        else {
            assertWebUserAllowedLogin(authUser);
            // 校验IP是否合法（只检查封禁状态的记录）
            boolean exists = ipBlacklistMapper.exists(
                    new QueryWrapper<WebIpBlacklist>()
                            .eq("ip_address", IpUtils.getIpAddr())
                            .eq("status", "1")
            );
            if (exists) {
                throw new HongshuException(ResultCodeEnum.ERROR_IP_BANNED);
            }
            // 校验密码是否正确
            if (!authUser.getPassword().equals(SecureUtil.md5(authUserDTO.getPassword()))) {
                ipProtectionService.recordAndCheck(IpUtils.getIpAddr(), false);
                throw new HongshuException(ResultCodeEnum.ERROR_PASSWORD);
            }
            authUser.setLoginDate(new Date());
            authUser.setLoginIp(IpUtils.getIpAddr());
            authUser.setAddress(AddressUtils.getRealAddressByIP(authUser.getLoginIp()));
            // 更新省市区信息
            this.parseIpToLocation(authUser.getLoginIp(), authUser);
            this.updateById(authUser);
        }
        // 记录成功登录
        ipProtectionService.recordAndCheck(IpUtils.getIpAddr(), true);
        // Redis缓存Token
        this.setUserInfoAndToken(map, authUser, authUserDTO.getTerminal());
        // 保存日志
        AsyncManager.me().execute(AsyncFactory.recordLoginInfor(
                authUserDTO.getTerminal(),
                authUser.getId(),
                authUser.getUsername(),
                Constants.LOGIN_SUCCESS,
                MessageUtils.message("user.login.success")));
        return map;
    }

    /**
     * 验证码登录
     *
     * @param authUserDTO 用户
     */
    @Override
    public Map<String, Object> loginByCode(AuthUserDTO authUserDTO) {
        Map<String, Object> map = new HashMap<>();
        // 1. 参数校验
        String phone = authUserDTO.getPhone();
        if (StringUtils.isBlank(phone)) {
            throw new HongshuException("手机号不能为空");
        }
        String code = authUserDTO.getCode();
        if (StringUtils.isBlank(code)) {
            throw new HongshuException("验证码不能为空");
        }
        // 4. 手机号验证码登录
        this.verifyCode(phone, code);
        // 5. 获取用户信息
        WebUser currentUser = this.getUserByPhone(phone);
        // 用户不存在-自动注册新用户
        if (ObjectUtil.isEmpty(currentUser)) {
            currentUser = this.register(authUserDTO);
            if ("2".equals(currentUser.getStatus())) {
                return buildRegisterPendingAuditMap(currentUser);
            }
        }
        // 用户存在-登录
        else {
            assertWebUserAllowedLogin(currentUser);
            // 校验IP是否合法（只检查封禁状态的记录）
            boolean exists = ipBlacklistMapper.exists(
                    new QueryWrapper<WebIpBlacklist>()
                            .eq("ip_address", IpUtils.getIpAddr())
                            .eq("status", "1")
            );
            if (exists) {
                throw new HongshuException(ResultCodeEnum.ERROR_IP_BANNED);
            }
            currentUser.setLoginDate(new Date());
            currentUser.setLoginIp(IpUtils.getIpAddr());
            currentUser.setAddress(AddressUtils.getRealAddressByIP(currentUser.getLoginIp()));
            // 更新省市区信息
            this.parseIpToLocation(currentUser.getLoginIp(), currentUser);
            this.updateById(currentUser);
        }
        // 记录成功登录
        ipProtectionService.recordAndCheck(IpUtils.getIpAddr(), true);
        // 6. Redis缓存Token
        this.setUserInfoAndToken(map, currentUser, authUserDTO.getTerminal());
        log.info("用户验证码登录成功: account={}, userId={}", phone, currentUser.getId());
        // 7. 保存日志
        AsyncManager.me().execute(AsyncFactory.recordLoginInfor(
                authUserDTO.getTerminal(),
                currentUser.getId(),
                currentUser.getUsername(),
                Constants.LOGIN_SUCCESS,
                MessageUtils.message("user.login.success")));
        return map;
    }

    /**
     * 发送验证码（手机号维度+IP维度双重限制）
     *
     * @param phone 手机号
     */
    @Override
    public boolean sendVerifyCode(String phone, String captchaVerification) {
        // 1. 校验手机号
        if (StringUtils.isEmpty(phone)) {
            throw new HongshuException(ResultCodeEnum.ERROR_PHONE);
        }
        // 2. 获取验证码配置，判断是否需要滑块校验
        boolean captchaEnabled = this.isCaptchaEnabled();
        if (captchaEnabled && captchaService == null) {
            log.warn("验证码服务未注入，跳过验证码校验");
            captchaEnabled = false;
        }
        // 3. 如果验证码功能启用，才进行滑块校验
        if (captchaEnabled) {
            if (StringUtils.isBlank(captchaVerification)) {
                throw new HongshuException("滑块验证码不能为空");
            }
            // 校验滑块
            CaptchaVO captchaVO = new CaptchaVO();
            captchaVO.setCaptchaVerification(captchaVerification);
            ResponseModel response = captchaService.verification(captchaVO);
            log.info("发送验证码 - captchaVerification: {}", captchaVerification);
            log.info("发送验证码 - captchaService response: {}", response);
            if (!"0000".equals(response.getRepCode())) {
                throw new HongshuException(ResultCodeEnum.ERROR_BLOCKPUZZLE);
            }
        } else {
            log.info("验证码功能已禁用，跳过滑块校验");
        }
        // 4. IP限流
        String ip = IpUtils.getIpAddr();
        String ipKey = VERIFY_IP_PREFIX + ip;
        Long ipCount = redisUtils.incrBy(ipKey, 1);
        if (ipCount == 1) {
            redisUtils.expire(ipKey, 1, TimeUnit.HOURS); // 设置1小时过期
        }
        if (ipCount > 20) {
            throw new HongshuException(ResultCodeEnum.ERROR_IP_COUNT);
        }
        // 5. 检查是否在限制时间内
        String intervalKey = VERIFY_CODE_PREFIX + "interval:" + phone;
        if (!redisUtils.setIfAbsent(intervalKey, "1", SEND_CODE_INTERVAL_SECONDS)) {
            log.warn("发送验证码太频繁: phone={}", phone);
            throw new HongshuException(ResultCodeEnum.ERROR_CODE_COUNT);
        }
        // 6. 生成验证码
        String code = this.generateRandomCode();
        // 7. 异步发送短信
        smsAsyncService.sendVerificationCodeAsync(phone, intervalKey, code);
        return true;
    }

    /**
     * 判断验证码功能是否启用
     */
    private boolean isCaptchaEnabled() {
        try {
            // 从配置表中获取验证码开关状态
            WebSystemConfig config = systemConfigMapper
                    .selectOne(new LambdaQueryWrapper<WebSystemConfig>()
                            .eq(WebSystemConfig::getConfigKey, "aj.captcha.enabled"));

            if (config != null) {
                return "1".equals(config.getConfigValue());
            }
            // 如果配置不存在，默认启用验证码（保持向后兼容）
            return true;
        } catch (Exception e) {
            log.error("获取验证码配置失败", e);
            // 出现异常时默认启用验证码，确保安全性
            return true;
        }
    }

    /**
     * 演示账户登录
     *
     * @param authUserDTO 用户
     */
    @Override
    @Transactional
    public Map<String, Object> demoLogin(AuthUserDTO authUserDTO) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(authUserDTO.getPhone())) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        // 查询用户
        WebUser authUser = this.getOne(new QueryWrapper<WebUser>().eq("phone", authUserDTO.getPhone()));
        assertWebUserAllowedLogin(authUser);
        // 校验IP是否合法（只检查封禁状态的记录）
        boolean exists = ipBlacklistMapper.exists(
                new QueryWrapper<WebIpBlacklist>()
                        .eq("ip_address", IpUtils.getIpAddr())
                        .eq("status", "1")
        );
        if (exists) {
            throw new HongshuException(ResultCodeEnum.ERROR_IP_BANNED);
        }
        // 校验密码是否正确
        if (!authUser.getPassword().equals(SecureUtil.md5(authUserDTO.getPassword()))) {
            ipProtectionService.recordAndCheck(IpUtils.getIpAddr(), false);
            throw new HongshuException(ResultCodeEnum.ERROR_PASSWORD);
        }
        authUser.setLoginDate(new Date());
        authUser.setLoginIp(IpUtils.getIpAddr());
        authUser.setAddress(AddressUtils.getRealAddressByIP(authUser.getLoginIp()));
        // 更新省市区信息
        this.parseIpToLocation(authUser.getLoginIp(), authUser);
        this.updateById(authUser);
        // 记录成功登录
        ipProtectionService.recordAndCheck(IpUtils.getIpAddr(), true);
        // Redis缓存Token
        this.setUserInfoAndToken(map, authUser, authUserDTO.getTerminal());
        // 保存日志
        AsyncManager.me().execute(AsyncFactory.recordLoginInfor(
                authUserDTO.getTerminal(),
                authUser.getId(),
                authUser.getUsername(),
                Constants.LOGIN_SUCCESS,
                MessageUtils.message("user.login.success")));
        return map;
    }

    /**
     * 生成短信验证码
     */
    private String generateRandomCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    /**
     * 验证码校验
     */
    private void verifyCode(String phone, String code) {
        String key = VERIFY_CODE_PREFIX + "code:" + phone;
        String savedCode = redisUtils.get(key);
        if (StringUtils.isEmpty(savedCode)) {
            log.warn("验证码不存在或已过期: account={}", phone);
            throw new HongshuException(ResultCodeEnum.ERROR_CODE_NULL);
        }
        if (!code.equals(savedCode)) {
            log.warn("验证码有误，请检查重新输入: account={}", phone);
            throw new HongshuException(ResultCodeEnum.ERROR_CODE_ERROR);
        }
        // 验证通过后删除验证码
        redisUtils.delete(key);
    }

    /**
     * 根据账号获取用户
     */
    private WebUser getUserByPhone(String phone) {
        return this.getOne(new LambdaQueryWrapper<WebUser>()
                .eq(WebUser::getPhone, phone)
                .or()
                .eq(WebUser::getEmail, phone)
                .last("LIMIT 1"));
    }

    /**
     * 注册新用户
     */
    private WebUser registerUser(AuthUserDTO authUserDTO) {
        return this.register(authUserDTO);
    }


    /**
     * 根据token获取当前用户
     *
     * @param accessToken accessToken
     */
    @Override
    public WebUser getUserInfoByToken(String accessToken) {
        String userId = JwtUtils.getUserId(accessToken);
        WebUser user = this.getById(userId);
        assertWebUserAllowedLogin(user);
        return user;
    }

    /**
     * 用户注册
     *
     * @param authUserDTO 前台传递用户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WebUser register(AuthUserDTO authUserDTO) {
        // 先查一遍，防止并发下重复注册
        WebUser exist = this.getOne(new QueryWrapper<WebUser>().eq("phone", authUserDTO.getPhone()));
        if (exist != null) {
            // 可以直接返回 exist，或者抛出“手机号已注册”异常
            throw new HongshuException("手机号已注册");
        }
        WebUser user = ConvertUtils.sourceToTarget(authUserDTO, WebUser.class);
        user.setFromType(TerminalTypeEnum.fromValue(authUserDTO.getTerminal()).getCode());
        user.setHsId(Long.valueOf(RandomUtil.randomNumbers(10)));
        user.setUsername(this.generateUsername());
        user.setAvatar(AuthConstant.DEFAULT_AVATAR);
        user.setUserCover(AuthConstant.DEFAULT_COVER);
        user.setPassword(SecureUtil.md5(authUserDTO.getPassword()));

        // 根据用户审核开关设置状态
        Boolean userAuditEnabled = systemConfigService.getSystemConfig().getUserAuditEnabled();
        if (Boolean.TRUE.equals(userAuditEnabled)) {
            user.setStatus("2"); // 待审核状态
            log.info("用户审核开启，新注册用户设置为待审核状态: {}", authUserDTO.getPhone());
        } else {
            user.setStatus("0"); // 正常状态
            log.info("用户审核关闭，新注册用户直接设置为正常状态: {}", authUserDTO.getPhone());
        }

        user.setDeleted(0); // 未删除状态
        user.setLoginIp(IpUtils.getIpAddr());
        user.setAddress(AddressUtils.getRealAddressByIP(user.getLoginIp()));
        // 解析IP获取省市区信息
        this.parseIpToLocation(user.getLoginIp(), user);
        user.setLoginDate(new Date());
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        this.save(user);
        runUserRegistrationAutoAuditIfNeeded(user);
        //异步注册chat-ai用户
        chatAsyncService.registerChatUser(authUserDTO.getPhone(), authUserDTO.getPassword());

        // 统计：新用户注册后，及时刷新工作台/首页统计缓存
        try {
            statisticsServiceV2.notifyStatisticsUpdate("user", "create");
        } catch (Exception e) {
            log.warn("刷新统计缓存失败（不影响注册流程）: {}", e.getMessage());
        }
        return user;
    }

    public String generateUsername() {
        String prefix = "小蓝薯";
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return prefix + sb;
    }

    /**
     * 用户是否注册
     *
     * @param authUserDTO 用户
     */
    @Override
    public boolean isRegister(AuthUserDTO authUserDTO) {
        long count = this.count(new QueryWrapper<WebUser>().eq("phone", authUserDTO.getPhone()).or().eq("email", authUserDTO.getEmail()));
        return count > 0;
    }

    /**
     * 退出登录
     *
     * @param userId 用户ID
     */
    @Override
    public void loginOut(String userId, String terminal) {
        // 检查userId是否为空
        if (userId == null || userId.trim().isEmpty()) {
            log.warn("退出登录失败：用户ID为空");
            return;
        }

        String userKey = AuthConstant.USER_KEY + userId;
        String refreshTokenStartTimeKey = AuthConstant.REFRESH_TOKEN_START_TIME + userId;
        List<String> keyList = new ArrayList<>();
        keyList.add(userKey);
        keyList.add(refreshTokenStartTimeKey);
        redisUtils.delete(keyList);

        // 保存日志
        WebUser authUser = this.getOne(new QueryWrapper<WebUser>().eq("id", userId));
        if (authUser != null) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfor(
                    terminal,
                    authUser.getId(),
                    authUser.getUsername(),
                    Constants.LOGOUT,
                    MessageUtils.message("user.logout.success")));
        } else {
            log.warn("退出登录失败：找不到用户ID为 {} 的用户记录", userId);
        }
    }

    /**
     * 修改密码
     *
     * @param authUserDTO 用户
     */
    @Override
    public boolean updatePassword(AuthUserDTO authUserDTO) {
        if (!authUserDTO.getPassword().equals(authUserDTO.getCheckPassword())) {
            return false;
        }
        String pwd = SecureUtil.md5(authUserDTO.getPassword());
        WebUser user;
        if (StringUtils.isBlank(authUserDTO.getId())) {
            user = this.getOne(new QueryWrapper<WebUser>().eq("phone", authUserDTO.getPhone()).or().eq("email", authUserDTO.getEmail()));
        } else {
            user = this.getById(authUserDTO.getId());
        }
        user.setPassword(pwd);
        return this.updateById(user);
    }

    /**
     * 刷新token
     *
     * @param refreshToken refreshToken
     */
    @Override
    public Map<String, Object> refreshToken(String refreshToken) {
        HashMap<String, Object> res = new HashMap<>(2);
        String userId = JwtUtils.getUserId(refreshToken);
        log.info("userId:{}", userId);
        WebUser webUser = this.getById(userId);
        assertWebUserAllowedLogin(webUser);
        // 创建新的accessToken
        String accessToken = JwtUtilss.getJwtToken(userId, AuthConstant.ACCESS_TOKEN_EXPIRATION_TIME);

        // minTimeOfRefreshToken  最短刷新时间
        // refreshTokenStartTime  刷新令牌开始时间

        // 下面判断是否刷新 refreshToken，如果refreshToken 快过期了 需要重新生成一个替换掉
        // refreshToken 有效时长是应该为accessToken有效时长的2倍
        long minTimeOfRefreshToken = 2 * AuthConstant.ACCESS_TOKEN_EXPIRATION_TIME;
        String refreshTokenStr = redisUtils.get(AuthConstant.REFRESH_TOKEN_START_TIME + userId);

        if (StringUtils.isBlank(refreshTokenStr)) {
            return res;
        }

        // refreshToken创建的起始时间点
        long refreshTokenStartTime = Long.parseLong(refreshTokenStr);
        // (refreshToken上次创建的时间点 + refreshToken的有效时长 - 当前时间点) 表示refreshToken还剩余的有效时长，如果小于2倍accessToken时长 ，则刷新 refreshToken
        if (refreshTokenStartTime + AuthConstant.REFRESH_TOKEN_EXPIRATION_TIME - System.currentTimeMillis() <= minTimeOfRefreshToken) {
            // 刷新refreshToken
            refreshToken = JwtUtilss.getJwtToken(userId, refreshTokenStartTime);
            redisUtils.setEx(AuthConstant.REFRESH_TOKEN_START_TIME + userId, String.valueOf(System.currentTimeMillis()), AuthConstant.REFRESH_TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);
        }

        res.put(TokenConstant.ACCESS_TOKEN, accessToken);
        res.put(TokenConstant.REFRESH_TOKEN, refreshToken);
        return res;
    }

    /**
     * 生成二维码
     */
    @Override
    public Map<String, Object> getQrCode() {
        // 1. 生成唯一token
        String loginToken = UUID.randomUUID().toString();
        // 2. 存入Redis，设置有效期（如5分钟）
        redisUtils.set(loginToken, "pending", 300);

        // 3. 生成二维码内容（直接包含loginToken，方便UniApp解析）
        String qrContent = "hongshu://login?loginToken=" + loginToken;

        // 4. 生成二维码图片（base64）
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 使用Hutool生成二维码
        QrCodeUtil.generate(qrContent, 300, 300, "png", out);
        String base64 = Base64.getEncoder().encodeToString(out.toByteArray());
        String qrcodeUrl = "data:image/png;base64," + base64;

        // 5. 返回给前端
        Map<String, Object> result = new HashMap<>();
        result.put("loginToken", loginToken);
        result.put("qrcodeUrl", qrcodeUrl);
        return result;
    }

    /**
     * 校验数据
     */
    private boolean checkCode(AuthUserDTO authUserDTO) {
        String code = "";
        if (StringUtils.isNotBlank(redisUtils.get(AuthConstant.CODE + authUserDTO.getPhone()))) {
            code = redisUtils.get(AuthConstant.CODE + authUserDTO.getPhone());
        } else if (StringUtils.isNotBlank(redisUtils.get(AuthConstant.CODE + authUserDTO.getEmail()))) {
            code = redisUtils.get(AuthConstant.CODE + authUserDTO.getEmail());
        }
        return StringUtils.isBlank(code) || !code.equals(authUserDTO.getCode());
    }

    /**
     * 缓存token
     */
    private void setUserInfoAndToken(Map<String, Object> map, WebUser authUser, String terminal) {
        String accessToken = JwtUtilss.getJwtToken(String.valueOf(authUser.getId()), AuthConstant.ACCESS_TOKEN_EXPIRATION_TIME);
        String refreshToken = JwtUtilss.getJwtToken(String.valueOf(authUser.getId()), AuthConstant.REFRESH_TOKEN_EXPIRATION_TIME);
        //缓存当前登录用户 refreshToken 创建的起始时间，这个会在刷新accessToken方法中 判断是否要重新生成(刷新)refreshToken时用到
        redisUtils.setEx(AuthConstant.REFRESH_TOKEN_START_TIME + authUser.getId(), String.valueOf(System.currentTimeMillis()), AuthConstant.REFRESH_TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);
        //将用户信息保存在redis中
        map.put(TokenConstant.ACCESS_TOKEN, accessToken);
        map.put(TokenConstant.REFRESH_TOKEN, refreshToken);
        map.put(AuthConstant.USER_INFO, authUser);
    }

    /**
     * 移动端扫描二维码（第一步）
     */
    public Map<String, Object> scanQrCode(String loginToken) {
        Map<String, Object> result = new HashMap<>();

        // 检查二维码是否还有效
        String status = redisUtils.get(loginToken);
        log.info("扫描二维码 - loginToken: {}, Redis状态: {}", loginToken, status);

        if (status == null) {
            log.info("二维码已失效 - loginToken: {}, 原因: Redis中无数据", loginToken);
            result.put("success", false);
            result.put("message", "二维码已失效");
            return result;
        }

        // 如果状态是"pending"，标记为已扫描
        if ("pending".equals(status)) {
            log.info("二维码状态为pending，标记为已扫描 - loginToken: {}", loginToken);
            redisUtils.set(loginToken, "scanned", 120); // 再保留2分钟
            result.put("success", true);
            result.put("message", "扫描成功，等待确认");
            return result;
        }

        // 如果状态是"scanned"，说明已经扫描过了，直接返回成功
        if ("scanned".equals(status)) {
            log.info("二维码状态为scanned，直接返回成功 - loginToken: {}", loginToken);
            result.put("success", true);
            result.put("message", "已扫描，等待确认登录");
            return result;
        }

        // 其他状态（如"confirmed"或用户ID），说明已经登录过了
        log.info("二维码状态异常 - loginToken: {}, 状态: {}", loginToken, status);
        result.put("success", false);
        result.put("message", "二维码已失效");
        return result;
    }

    /**
     * 移动端确认二维码登录（第二步）
     */
    @Override
    public Map<String, Object> confirmQrCodeLogin(QrCodeConfirmVO vo) {
        Map<String, Object> result = new HashMap<>();
        String loginToken = vo.getLoginToken();
        String userId = vo.getUserId();

        // 检查二维码是否已扫描
        String status = redisUtils.get(loginToken);
        if (status == null || !"scanned".equals(status)) {
            result.put("success", false);
            result.put("message", "请先扫描二维码");
            return result;
        }

        // 标记为已确认，存userId
        redisUtils.set(loginToken, userId, 120); // 再保留2分钟
        result.put("success", true);
        result.put("message", "确认登录成功");
        return result;
    }

    /**
     * 检查二维码状态
     */
    @Override
    public Map<String, Object> checkQrCodeStatus(String loginToken) {
        Map<String, Object> result = new HashMap<>();
        String val = redisUtils.get(loginToken);

        if (val == null) {
            result.put("status", "expired");
            result.put("message", "二维码已过期");
        } else if ("pending".equals(val)) {
            result.put("status", "pending");
            result.put("message", "等待扫码");
        } else if ("scanned".equals(val)) {
            result.put("status", "scanned");
            result.put("message", "等待确认登录");
        } else {
            // val为userId，已确认
            result.put("status", "confirmed");
            result.put("userId", val);
            result.put("message", "登录成功");
        }

        return result;
    }

    /**
     * 二维码登录成功，获取用户信息和token
     */
    @Override
    public Map<String, Object> qrCodeLogin(String userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 根据userId获取用户信息
            WebUser user = baseMapper.selectById(userId);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }
            assertWebUserAllowedLogin(user);

            // 生成token
            String accessToken = JwtUtilss.getJwtToken(user.getId().toString(), 24 * 60 * 60 * 1000L); // 24小时
            String refreshToken = JwtUtilss.getJwtToken(user.getId().toString(), 7 * 24 * 60 * 60 * 1000L); // 7天

            // 构建用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getUsername()); // 使用username作为nickname
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());

            // 返回结果
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);
            result.put("userInfo", userInfo);
            result.put("success", true);
            result.put("message", "登录成功");

            log.info("二维码登录成功 - userId: {}, username: {}", userId, user.getUsername());

        } catch (Exception e) {
            log.error("二维码登录失败 - userId: {}", userId, e);
            result.put("success", false);
            result.put("message", "登录失败");
        }

        return result;
    }

    /**
     * 初始化 ip2region searcher
     */
    @PostConstruct
    public void init() throws Exception {
        // 1. 读取 classpath 下的 ip2region.xdb
        ClassPathResource resource = new ClassPathResource("ip2region.xdb");
        // 2. 拷贝到临时文件
        File tempFile = File.createTempFile("ip2region", ".xdb");
        try (InputStream in = resource.getInputStream();
             OutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        // 3. 用临时文件路径初始化
        this.searcher = Searcher.newWithFileOnly(tempFile.getAbsolutePath());
    }

    /**
     * 从IP地址解析省市区信息并设置到用户对象
     *
     * @param ip   IP地址
     * @param user 用户对象
     */
    private void parseIpToLocation(String ip, WebUser user) {
        if (ip == null || ip.isEmpty() || searcher == null) {
            return;
        }
        // 内网IP不解析
        if (IpUtils.internalIp(ip)) {
            return;
        }
        try {
            String ipInfo = searcher.search(ip);
            // ipInfo 格式: "中国|0|浙江省|杭州市|电信"
            String[] parts = ipInfo.split("\\|");
            if (parts.length >= 4) {
                String province = parts[2].trim();
                String city = parts[3].trim();
                // 处理直辖市
                if (isDirectCity(province)) {
                    user.setProvince(province);
                    user.setCity(province); // 直辖市省和市相同
                    user.setDistrict("");
                } else {
                    user.setProvince(province);
                    user.setCity(city.equals("0") ? province : city);
                    user.setDistrict(""); // ip2region 不提供区信息，设为空
                }
            }
        } catch (Exception e) {
            log.error("解析IP地址失败: {}", ip, e);
        }
    }

    /**
     * 判断是否为直辖市
     */
    private boolean isDirectCity(String province) {
        return province.equals("北京市") ||
                province.equals("上海市") ||
                province.equals("天津市") ||
                province.equals("重庆市");
    }

    /**
     * 微信小程序登录
     *
     * @param weChatLoginDTO 微信登录信息
     */
    @Override
    @Transactional
    public Map<String, Object> wechatLogin(WeChatLoginDTO weChatLoginDTO) {
        Map<String, Object> map = new HashMap<>();

        try {
            // 1. 参数校验
            if (StringUtils.isBlank(weChatLoginDTO.getCode())) {
                throw new HongshuException("微信登录code不能为空");
            }

            // 2. 通过code获取微信openid
            Map<String, String> weChatInfo = weChatUtil.getWeChatOpenId(weChatLoginDTO.getCode());
            String openid = weChatInfo.get("openid");

            if (StringUtils.isBlank(openid)) {
                throw new HongshuException("获取微信openid失败");
            }

            // 3. 查询用户（通过openid）
            WebUser user = this.getOne(new LambdaQueryWrapper<WebUser>()
                    .eq(WebUser::getOpenId, openid)
                    .last("LIMIT 1"));

            // 4. 用户不存在，自动注册新用户
            if (user == null) {
                user = new WebUser();
                user.setOpenId(openid);
                user.setFromType(TerminalTypeEnum.APPLET.getCode()); // 小程序登录来源

                // 设置用户信息（如果有）
                if (weChatLoginDTO.getUserInfo() != null) {
                    WeChatLoginDTO.WeChatUserInfo userInfo = weChatLoginDTO.getUserInfo();
                    user.setUsername(StringUtils.isNotBlank(userInfo.getNickName())
                            ? userInfo.getNickName()
                            : this.generateUsername());
                    user.setAvatar(StringUtils.isNotBlank(userInfo.getAvatarUrl())
                            ? userInfo.getAvatarUrl()
                            : AuthConstant.DEFAULT_AVATAR);
                    // 性别转换：0-未知，1-男，2-女
                    if (userInfo.getGender() != null) {
                        user.setGender(userInfo.getGender() == 1 ? "1" : (userInfo.getGender() == 2 ? "2" : "0"));
                    }
                    if (StringUtils.isNotBlank(userInfo.getCity())) {
                        user.setCity(userInfo.getCity());
                    }
                    if (StringUtils.isNotBlank(userInfo.getProvince())) {
                        user.setProvince(userInfo.getProvince());
                    }
                } else {
                    // 没有用户信息，使用默认值
                    user.setUsername(this.generateUsername());
                    user.setAvatar(AuthConstant.DEFAULT_AVATAR);
                }

                user.setHsId(Long.valueOf(RandomUtil.randomNumbers(10)));
                user.setUserCover(AuthConstant.DEFAULT_COVER);

                // 处理手机号（如果有）
                if (StringUtils.isNotBlank(weChatLoginDTO.getPhoneCode())) {
                    try {
                        String phoneNumber = weChatUtil.getPhoneNumber(weChatLoginDTO.getPhoneCode());
                        if (StringUtils.isNotBlank(phoneNumber)) {
                            user.setPhone(phoneNumber);
                            log.info("微信登录 - 获取并设置手机号: phoneNumber={}", phoneNumber);
                        }
                    } catch (Exception e) {
                        log.warn("获取手机号失败，继续登录流程: {}", e.getMessage());
                        // 获取手机号失败不影响登录流程
                    }
                }

                // 根据用户审核开关设置状态
                Boolean userAuditEnabled = systemConfigService.getSystemConfig().getUserAuditEnabled();
                if (Boolean.TRUE.equals(userAuditEnabled)) {
                    user.setStatus("2"); // 待审核状态
                    log.info("用户审核开启，微信登录新用户设置为待审核状态");
                } else {
                    user.setStatus("0"); // 正常状态
                    log.info("用户审核关闭，微信登录新用户直接设置为正常状态");
                }

                user.setDeleted(0); // 未删除状态
                user.setLoginIp(IpUtils.getIpAddr());
                user.setAddress(AddressUtils.getRealAddressByIP(user.getLoginIp()));
                // 解析IP获取省市区信息
                this.parseIpToLocation(user.getLoginIp(), user);
                user.setLoginDate(new Date());
                user.setCreateTime(new Date());
                user.setUpdateTime(new Date());

                this.save(user);
                runUserRegistrationAutoAuditIfNeeded(user);

                log.info("微信登录 - 新用户自动注册: openid={}, userId={}, username={}",
                        openid, user.getId(), user.getUsername());
                if ("2".equals(user.getStatus())) {
                    return buildRegisterPendingAuditMap(user);
                }
            } else {
                // 5. 用户存在，更新用户信息
                // 校验IP是否合法
                boolean exists = ipBlacklistMapper.exists(
                        new QueryWrapper<WebIpBlacklist>().eq("ip_address", IpUtils.getIpAddr())
                );
                if (exists) {
                    throw new HongshuException(ResultCodeEnum.ERROR_IP_BANNED);
                }

                // 更新用户信息（如果有新的用户信息）
                boolean needUpdate = false;
                if (weChatLoginDTO.getUserInfo() != null) {
                    WeChatLoginDTO.WeChatUserInfo userInfo = weChatLoginDTO.getUserInfo();
                    if (StringUtils.isNotBlank(userInfo.getNickName()) &&
                            !userInfo.getNickName().equals(user.getUsername())) {
                        user.setUsername(userInfo.getNickName());
                        needUpdate = true;
                    }
                    if (StringUtils.isNotBlank(userInfo.getAvatarUrl()) &&
                            !userInfo.getAvatarUrl().equals(user.getAvatar())) {
                        user.setAvatar(userInfo.getAvatarUrl());
                        needUpdate = true;
                    }
                    if (userInfo.getGender() != null) {
                        String genderStr = userInfo.getGender() == 1 ? "1" : (userInfo.getGender() == 2 ? "2" : "0");
                        if (!genderStr.equals(user.getGender())) {
                            user.setGender(genderStr);
                            needUpdate = true;
                        }
                    }
                }

                // 处理手机号（如果有，且用户还没有手机号）
                if (StringUtils.isNotBlank(weChatLoginDTO.getPhoneCode()) && 
                    StringUtils.isBlank(user.getPhone())) {
                    try {
                        String phoneNumber = weChatUtil.getPhoneNumber(weChatLoginDTO.getPhoneCode());
                        if (StringUtils.isNotBlank(phoneNumber)) {
                            user.setPhone(phoneNumber);
                            needUpdate = true;
                            log.info("微信登录 - 更新手机号: phoneNumber={}", phoneNumber);
                        }
                    } catch (Exception e) {
                        log.warn("获取手机号失败: {}", e.getMessage());
                        // 获取手机号失败不影响登录流程
                    }
                }

                // 更新登录信息
                user.setLoginDate(new Date());
                user.setLoginIp(IpUtils.getIpAddr());
                user.setAddress(AddressUtils.getRealAddressByIP(user.getLoginIp()));
                user.setUpdateTime(new Date());
                needUpdate = true;

                if (needUpdate) {
                    this.updateById(user);
                }

                log.info("微信登录 - 老用户登录: openid={}, userId={}, username={}",
                        openid, user.getId(), user.getUsername());
            }

            assertWebUserAllowedLogin(user);

            // 记录成功登录
            ipProtectionService.recordAndCheck(IpUtils.getIpAddr(), true);

            // 6. 生成token
            this.setUserInfoAndToken(map, user, "app");

            // 7. 保存登录日志
            AsyncManager.me().execute(AsyncFactory.recordLoginInfor(
                    "app",
                    user.getId(),
                    user.getUsername(),
                    Constants.LOGIN_SUCCESS,
                    MessageUtils.message("user.login.success")));

            log.info("微信登录成功: userId={}, username={}", user.getId(), user.getUsername());

        } catch (HongshuException e) {
            log.error("微信登录失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("微信登录异常", e);
            throw new HongshuException("微信登录失败: " + e.getMessage());
        }

        return map;
    }

    /**
     * 生成微信网页授权URL
     *
     * @param redirectUri 回调地址
     * @return 授权URL
     */
    @Override
    public String generateWeChatAuthUrl(String redirectUri) {
        try {
            return weChatUtil.generateWeChatAuthUrl(redirectUri, null);
        } catch (Exception e) {
            log.error("生成微信授权URL失败: redirectUri={}", redirectUri, e);
            throw new HongshuException("生成微信授权URL失败: " + e.getMessage());
        }
    }

    /**
     * 微信网页登录（通过code）
     *
     * @param code 微信授权码
     * @return 登录结果（包含token和用户信息）
     */
    @Override
    @Transactional
    public Map<String, Object> wechatWebLogin(String code) {
        Map<String, Object> map = new HashMap<>();

        try {
            // 1. 参数校验
            if (StringUtils.isBlank(code)) {
                throw new HongshuException("微信授权码不能为空");
            }

            // 2. 通过code获取微信access_token和openid
            Map<String, String> weChatInfo = weChatUtil.getWeChatWebAccessToken(code);
            String openid = weChatInfo.get("openid");
            String accessToken = weChatInfo.get("access_token");

            if (StringUtils.isBlank(openid)) {
                throw new HongshuException("获取微信openid失败");
            }

            // 3. 通过access_token获取用户信息
            Map<String, Object> userInfoMap = null;
            try {
                userInfoMap = weChatUtil.getWeChatUserInfo(accessToken, openid);
            } catch (Exception e) {
                log.warn("获取微信用户信息失败，将使用openid登录: {}", e.getMessage());
                // 如果获取用户信息失败，仍然可以使用openid登录
            }

            // 4. 查询用户（通过openid）
            WebUser user = this.getOne(new LambdaQueryWrapper<WebUser>()
                    .eq(WebUser::getOpenId, openid)
                    .last("LIMIT 1"));

            // 5. 用户不存在，自动注册新用户
            if (user == null) {
                user = new WebUser();
                user.setOpenId(openid);
                user.setFromType(TerminalTypeEnum.WEB.getCode()); // Web登录来源

                // 设置用户信息（如果有）
                if (userInfoMap != null) {
                    String nickname = (String) userInfoMap.get("nickname");
                    String headimgurl = (String) userInfoMap.get("headimgurl");
                    Integer sex = (Integer) userInfoMap.get("sex");
                    String city = (String) userInfoMap.get("city");
                    String province = (String) userInfoMap.get("province");

                    user.setUsername(StringUtils.isNotBlank(nickname)
                            ? nickname
                            : this.generateUsername());
                    user.setAvatar(StringUtils.isNotBlank(headimgurl)
                            ? headimgurl
                            : AuthConstant.DEFAULT_AVATAR);
                    // 性别转换：1-男，2-女，0-未知
                    if (sex != null) {
                        user.setGender(sex == 1 ? "1" : (sex == 2 ? "2" : "0"));
                    }
                    if (StringUtils.isNotBlank(city)) {
                        user.setCity(city);
                    }
                    if (StringUtils.isNotBlank(province)) {
                        user.setProvince(province);
                    }
                } else {
                    // 没有用户信息，使用默认值
                    user.setUsername(this.generateUsername());
                    user.setAvatar(AuthConstant.DEFAULT_AVATAR);
                }

                user.setHsId(Long.valueOf(RandomUtil.randomNumbers(10)));
                user.setUserCover(AuthConstant.DEFAULT_COVER);

                // 根据用户审核开关设置状态
                Boolean userAuditEnabled = systemConfigService.getSystemConfig().getUserAuditEnabled();
                if (Boolean.TRUE.equals(userAuditEnabled)) {
                    user.setStatus("2"); // 待审核状态
                    log.info("用户审核开启，微信网页登录新用户设置为待审核状态");
                } else {
                    user.setStatus("0"); // 正常状态
                    log.info("用户审核关闭，微信网页登录新用户直接设置为正常状态");
                }

                user.setDeleted(0); // 未删除状态
                user.setLoginIp(IpUtils.getIpAddr());
                user.setAddress(AddressUtils.getRealAddressByIP(user.getLoginIp()));
                // 解析IP获取省市区信息
                this.parseIpToLocation(user.getLoginIp(), user);
                user.setLoginDate(new Date());
                user.setCreateTime(new Date());
                user.setUpdateTime(new Date());

                this.save(user);
                runUserRegistrationAutoAuditIfNeeded(user);

                log.info("微信网页登录 - 新用户自动注册: openid={}, userId={}, username={}",
                        openid, user.getId(), user.getUsername());
                if ("2".equals(user.getStatus())) {
                    return buildRegisterPendingAuditMap(user);
                }
            } else {
                // 6. 用户存在，更新用户信息
                // 校验IP是否合法
                boolean exists = ipBlacklistMapper.exists(
                        new QueryWrapper<WebIpBlacklist>().eq("ip_address", IpUtils.getIpAddr())
                );
                if (exists) {
                    throw new HongshuException(ResultCodeEnum.ERROR_IP_BANNED);
                }

                // 更新用户信息（如果有新的用户信息）
                boolean needUpdate = false;
                if (userInfoMap != null) {
                    String nickname = (String) userInfoMap.get("nickname");
                    String headimgurl = (String) userInfoMap.get("headimgurl");
                    Integer sex = (Integer) userInfoMap.get("sex");

                    if (StringUtils.isNotBlank(nickname) &&
                            !nickname.equals(user.getUsername())) {
                        user.setUsername(nickname);
                        needUpdate = true;
                    }
                    if (StringUtils.isNotBlank(headimgurl) &&
                            !headimgurl.equals(user.getAvatar())) {
                        user.setAvatar(headimgurl);
                        needUpdate = true;
                    }
                    if (sex != null) {
                        String genderStr = sex == 1 ? "1" : (sex == 2 ? "2" : "0");
                        if (!genderStr.equals(user.getGender())) {
                            user.setGender(genderStr);
                            needUpdate = true;
                        }
                    }
                }

                // 更新登录信息
                user.setLoginDate(new Date());
                user.setLoginIp(IpUtils.getIpAddr());
                user.setAddress(AddressUtils.getRealAddressByIP(user.getLoginIp()));
                user.setUpdateTime(new Date());
                needUpdate = true;

                if (needUpdate) {
                    this.updateById(user);
                }

                log.info("微信网页登录 - 老用户登录: openid={}, userId={}, username={}",
                        openid, user.getId(), user.getUsername());
            }

            assertWebUserAllowedLogin(user);

            // 记录成功登录
            ipProtectionService.recordAndCheck(IpUtils.getIpAddr(), true);

            // 7. 生成token
            this.setUserInfoAndToken(map, user, "web");

            // 8. 保存登录日志
            AsyncManager.me().execute(AsyncFactory.recordLoginInfor(
                    "web",
                    user.getId(),
                    user.getUsername(),
                    Constants.LOGIN_SUCCESS,
                    MessageUtils.message("user.login.success")));

            log.info("微信网页登录成功: userId={}, username={}", user.getId(), user.getUsername());

        } catch (HongshuException e) {
            log.error("微信网页登录失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("微信网页登录异常", e);
            throw new HongshuException("微信网页登录失败: " + e.getMessage());
        }

        return map;
    }

    /**
     * 获取微信回调地址（从配置中读取）
     *
     * @return 回调地址
     */
    @Override
    public String getWeChatCallbackUrl() {
        try {
            WebSystemConfig config = systemConfigMapper.selectOne(
                    new LambdaQueryWrapper<WebSystemConfig>()
                            .eq(WebSystemConfig::getConfigKey, "wechat.web.callback.url")
            );
            if (config != null && StringUtils.isNotBlank(config.getConfigValue())) {
                return config.getConfigValue();
            }
            return null;
        } catch (Exception e) {
            log.error("获取微信回调地址配置失败", e);
            return null;
        }
    }
}
