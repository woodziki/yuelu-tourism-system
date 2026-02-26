package com.yuelu.interceptor;

import com.yuelu.exception.AuthException;
import com.yuelu.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 拦截器：校验请求头中的 Token 是否合法。
 *
 * <p>从 Header「Authorization: Bearer &lt;token&gt;」中取出 Token，
 * 校验通过则放行；未携带或无效则抛出 AuthException，由全局异常处理器返回「请先登录」。</p>
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String PREFIX_BEARER = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String auth = request.getHeader(HEADER_AUTHORIZATION);
        String token = null;
        if (auth != null && auth.startsWith(PREFIX_BEARER)) {
            token = auth.substring(PREFIX_BEARER.length()).trim();
        }
        if (!jwtUtil.verify(token)) {
            throw new AuthException();
        }
        return true;
    }
}
