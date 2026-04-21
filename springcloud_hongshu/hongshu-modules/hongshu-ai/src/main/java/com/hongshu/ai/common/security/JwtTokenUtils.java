package com.hongshu.ai.common.security;

import com.hongshu.ai.common.constant.AuthConstant;
import com.hongshu.common.core.constant.StringPoolConstant;
import com.hongshu.common.core.exception.ValidateException;
import com.hongshu.common.core.utils.JwtUtilss;
import com.hongshu.common.core.validator.ValidatorUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

/**
 * JWT工具类
 *
 * @author: myj
 * @date: 2024/4/1
 * @version: 1.0.0
 */
public class JwtTokenUtils {

    /**
     * 密钥
     *
     * 说明：
     *  - 为了与 web / app 登录保持一致，这里复用通用 JwtUtilss 的 APP_SECRET
     *  - 这样由 /web 或 /app 登录返回的 accessToken 也可以在 chat 模块中被正确解析
     */
    private static final String SECRET = JwtUtilss.APP_SECRET;

    /**
     * 有效期7天
     */
    public static final long EXPIRE_TIME = 7 * 24 * 3600 * 1000;

    /**
     * 获取登录用户信息
     *
     * @return
     */
    public static UserDetail getLoginUser() {
        return getUserDetail(getAuthentication());
    }

    /**
     * 生成令牌
     *
     * @return 令牌
     */
    public static String generateToken(Authentication authentication) {
        UserDetail userDetail = getUserDetail(authentication);
        Map<String, Object> claims = new HashMap<>(16);
        claims.put("id", userDetail.getId());
        claims.put("uid", userDetail.getUid());
        claims.put("sessionId", userDetail.getSessionId());
        claims.put("username", userDetail.getUsername());
        claims.put("admind", userDetail.getAdmind());
        claims.put("role", userDetail.getRole());
        return Jwts.builder()
                .setClaims(claims)
                // 用户名写入标题
                .setSubject(userDetail.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET)  // 改为 HS256 与 web 保持一致
                .compact();
    }

    /**
     * 根据请求令牌获取登录认证信息
     *
     * @param
     * @return
     */
    public static Authentication getAuthenticationeFromToken(String token) {
        if (ValidatorUtil.isNull(token)) {
            throw new ValidateException("token不能为空");
        }
        Authentication authentication = getAuthentication();
        if (ValidatorUtil.isNotNull(authentication)) {
            return authentication;
        }
        //解析 Token
        UserDetail userDetail = getUserDetailFromToken(token);
        // 得到 权限（角色）
        //Set<String> permissions = JSON.parseObject(JSON.toJSONString(claims.get("authorities")), Set.class);
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (1 == userDetail.getRole()) {
//            Set<String> permissions = ApplicationContextUtil.getBean(ResourceServiceImpl.class).listButtonResourceBySysUser(userDetail.getId(), userDetail.getUsername()).getData();
//            Optional.ofNullable(permissions).ifPresent(v -> v.stream().forEach(d -> authorities.add(new SimpleGrantedAuthority(d))));
        }
        // 返回验证令牌
        return new UsernamePasswordAuthenticationToken(userDetail, null, authorities);
    }

    /**
     * 从令牌中获取用户信息
     *
     * @param token 令牌
     * @return 数据声明
     */
    public static UserDetail getUserDetailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);

        // 兼容两种 token 结构：
        // 1）PC 后台签发：包含 id/uid/sessionId/username/admind/role 等字段
        // 2）web/uniapp 登录返回的 accessToken：只包含 userId 字段

        Object idClaim = claims.get("id");
        Object userIdClaim = claims.get("userId");

        // 情况一：PC 后台 token（原有逻辑）
        if (idClaim != null) {
            Long id = Long.valueOf(idClaim.toString());
            String sessionId = Optional.ofNullable(claims.get("sessionId")).map(v -> v.toString()).orElse(StringPoolConstant.EMPTY);
            String username = Optional.ofNullable(claims.get("username")).map(v -> v.toString()).orElse(StringPoolConstant.EMPTY);
            String uid = Optional.ofNullable(claims.get("uid")).map(v -> v.toString()).orElse(StringPoolConstant.EMPTY);
            Boolean admind = Optional.ofNullable(claims.get("admind")).map(v -> Boolean.valueOf(v.toString())).orElse(false);
            Integer role = Optional.ofNullable(claims.get("role")).map(v -> Integer.valueOf(v.toString())).orElse(0);
            return new UserDetail(id, uid, sessionId, username, admind, role);
        }

        // 情况二：web/uniapp accessToken，只携带 userId
        if (userIdClaim != null) {
            Long id = Long.valueOf(userIdClaim.toString());
            String sessionId = StringPoolConstant.EMPTY;
            String username = StringPoolConstant.EMPTY;
            String uid = StringPoolConstant.EMPTY;
            Boolean admind = false;
            Integer role = 0;
            return new UserDetail(id, uid, sessionId, username, admind, role);
        }

        // 其它未知结构，视为无效
        throw new ValidateException("token中缺少必要的用户信息");
    }


    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    private static Claims getClaimsFromToken(String token) {
        // 解析 Token
        Claims claims = Jwts.parser()
                // 验签
                .setSigningKey(SECRET)
                // 去掉 Bearer
                .parseClaimsJws(token.replace(AuthConstant.JWT_TOKEN_PREFIX, StringPoolConstant.EMPTY))
                .getBody();
        return claims;
    }

    /**
     * 获取 Authentication
     *
     * @return
     */
    private static Authentication getAuthentication() {
        if (SecurityContextHolder.getContext() == null) {
            return null;
        }
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取登录用户信息
     *
     * @param authentication 认证信息
     * @return
     */
    public static UserDetail getUserDetail(Authentication authentication) {
        if (ValidatorUtil.isNull(authentication)) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (ValidatorUtil.isNotNull(principal) && principal instanceof UserDetail) {
            return ((UserDetail) principal);
        }
        return null;
    }

}
