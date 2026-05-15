package com.aiforge.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @Description: Agent 客户端配置
 */
@Configuration
public class AgentClientConfig {

    /**
     * Agent 服务器地址
     */
    @Value("${aiforge.agent.base-url:http://localhost:8000}")
    private String agentBaseUrl;

    /**
     * 构建 Agent 客户端
     */
    @Bean
    public WebClient agentWebClient() {
        return WebClient.builder()
                .baseUrl(agentBaseUrl)
                // 配置最大响应体大小为 10MB
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }
}
