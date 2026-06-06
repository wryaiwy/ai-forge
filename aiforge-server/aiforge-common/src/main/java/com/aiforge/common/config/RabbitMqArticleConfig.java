package com.aiforge.common.config;

import com.aiforge.common.constant.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文章相关 RabbitMQ 配置
 */
@Configuration
public class RabbitMqArticleConfig {

    /**
     * 文章相关交换机
     * @return 文章相关交换机
     */
    @Bean
    public FanoutExchange articleExchange() {
        return new FanoutExchange(MqConstants.ARTICLE_EXCHANGE, true, false);
    }

    /**
     * 文章ES队列
     * @return 文章ES队列
     */
    @Bean
    public Queue articleEsQueue() {
        return new Queue(MqConstants.ARTICLE_ES_QUEUE, true);
    }

    /**
     * 文章向量队列
     * @return 文章向量队列
     */
    @Bean
    public Queue articleVectorQueue() {
        return new Queue(MqConstants.ARTICLE_VECTOR_QUEUE, true);
    }

    /**
     * 文章ES队列绑定交换机
     * @param articleEsQueue 文章ES队列
     * @param articleExchange 文章相关交换机
     * @return 文章ES队列绑定交换机的绑定关系
     */
    @Bean
    public Binding bindingArticleEsQueue(Queue articleEsQueue, FanoutExchange articleExchange) {
        return BindingBuilder.bind(articleEsQueue).to(articleExchange);
    }

    /**
     * 文章向量队列绑定交换机
     * @param articleVectorQueue 文章向量队列
     * @param articleExchange 文章相关交换机
     * @return 文章向量队列绑定交换机的绑定关系
     */
    @Bean
    public Binding bindingArticleVectorQueue(Queue articleVectorQueue, FanoutExchange articleExchange) {
        return BindingBuilder.bind(articleVectorQueue).to(articleExchange);
    }
}
