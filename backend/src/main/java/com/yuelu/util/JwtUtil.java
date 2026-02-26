package com.yuelu.util;

import cn.hutool.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类（基于 Hutool）。
 *
 * <p>登录成功后签发 Token，拦截器里解析并校验签名与过期时间。
 * 密钥与过期时间从 application.yml 读取。</p>
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:yuelu-tourism-jwt-secret-key-32bytes!!}")
    private String secret;

    @Value("${jwt.expire-hours:72}")
    private int expireHours;

    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";

    /**
     * 生成 Token，payload 中包含 userId、username 和过期时间。
     */
    public String createToken(Long userId, String username) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(KEY_USER_ID, userId);
        payload.put(KEY_USERNAME, username);
        payload.put("exp", new Date(System.currentTimeMillis() + expireHours * 3600_000L));
        return JWTUtil.createToken(payload, secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解析并校验 Token（签名 + 是否过期）。
     * 校验通过返回 true，否则抛出异常（由拦截器捕获后统一返回「请先登录」）。
     */
    public boolean verify(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            return JWTUtil.verify(token, secret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Token 中取出 userId（不校验签名，仅解析；校验由 verify 在拦截器里做）。
     */
    public Long getUserId(String token) {
        try {
            Object id = JWTUtil.parseToken(token).getPayload().getClaim(KEY_USER_ID);
            return id == null ? null : ((Number) id).longValue();
        } catch (Exception e) {
            return null;
        }
    }
}
