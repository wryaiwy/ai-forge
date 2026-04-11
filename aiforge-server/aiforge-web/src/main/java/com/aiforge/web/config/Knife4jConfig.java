package com.aiforge.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: wengrunyang
 * @Description: 文档配置类
 * @DateTime: 2026/4/10 23:39
 **/
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AiForge 平台接口文档")
                        .version("1.0")
                        .description("基于 Spring Boot 3 + Vue 2 的全栈 AI 开发平台")
                        .contact(new Contact().name("YourName")));
    }
}