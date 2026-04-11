package com.aiforge.web.config;

import com.aiforge.web.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: wengrunyang
 * @Description: 跨域处理配置 (CORS)
 * @DateTime: 2026/4/10 23:38
 **/
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 设置允许跨域的路径
        registry.addMapping("/**")
                // 设置允许跨域请求的域名（Vue3 默认通常是 http://localhost:5173）
                // 开发阶段可以直接写 "*" 或使用 allowedOriginPatterns
                .allowedOriginPatterns("*")
                // 是否允许证书（Cookies）
                .allowCredentials(true)
                // 设置允许的方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 设置允许的 header 属性
                .allowedHeaders("*")
                // 跨域允许时间
                .maxAge(3600);
    }

    /**
     * 注册拦截器配置
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // 拦截所有路径
                .addPathPatterns("/**")
                // 排除不需要拦截的路径（白名单）
                .excludePathPatterns(
                        // 1. 登录、退出、注册接口
                        "/auth/login",
                        "/auth/logout",
                        "/auth/register",

                        // 2. 静态资源与系统默认路径
                        "/favicon.ico",   // 放行网页图标
                        "/error",         // 放行 SpringBoot 底层的错误转发路径

                        // 3. Knife4j / Swagger 3 接口文档核心路径
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/swagger-ui/**",
                        "/swagger-resources/**"
                );
    }
}
