package com.yuelu.config;

import com.yuelu.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册 JWT 拦截器并配置白名单。
 *
 * <p>白名单（不校验 Token）：登录、注册、景点与线路的查询接口、评论列表接口。
 * 其余接口（如收藏、发表评论等）需要携带合法 Token 才能访问。</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/spot/**",
                        "/route/**",
                        "/comment/list",
                        "/error"
                );
    }
}
