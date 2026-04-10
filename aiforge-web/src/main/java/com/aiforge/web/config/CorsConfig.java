package com.aiforge.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: wengrunyang
 * @Description: 跨域处理配置 (CORS)
 * @DateTime: 2026/4/10 23:38
 **/
@Configuration
public class CorsConfig implements WebMvcConfigurer {

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
}
