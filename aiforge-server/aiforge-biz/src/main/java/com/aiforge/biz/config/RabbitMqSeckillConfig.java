package com.aiforge.biz.config;

import com.aiforge.common.constant.MqConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqSeckillConfig {

    /**
     * 配置 JSON 消息转换器，替换默认的 JDK 序列化，解决 SecurityException
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 1. 普通创单交换机和队列
    @Bean
    public DirectExchange seckillExchange() {
        return new DirectExchange(MqConstants.SECKILL_EXCHANGE);
    }

    @Bean
    public Queue seckillOrderQueue() {
        return new Queue(MqConstants.SECKILL_ORDER_QUEUE);
    }

    @Bean
    public Binding seckillOrderBinding() {
        return BindingBuilder.bind(seckillOrderQueue()).to(seckillExchange())
                .with(MqConstants.SECKILL_ORDER_ROUTING_KEY);
    }

    // 2. 声明死信交换机 (DLX) 和 死信队列 (真正的消费队列)
    @Bean
    public DirectExchange seckillDlxExchange() {
        return new DirectExchange(MqConstants.SECKILL_DLX_EXCHANGE);
    }

    @Bean
    public Queue seckillDlxQueue() {
        return new Queue(MqConstants.SECKILL_DLX_QUEUE);
    }

    @Bean
    public Binding seckillDlxBinding() {
        return BindingBuilder.bind(seckillDlxQueue()).to(seckillDlxExchange())
                .with(MqConstants.SECKILL_DLX_ROUTING_KEY);
    }

    // 3. 声明 TTL 缓冲队列 (无消费者, 到期后被转发到死信队列)
    @Bean
    public DirectExchange seckillTtlExchange() {
        return new DirectExchange(MqConstants.SECKILL_TTL_EXCHANGE);
    }

    @Bean
    public Queue seckillTtlQueue() {
        Map<String, Object> args = new HashMap<>();
        // 当消息在这个队列中过期时，转发给死信交换机
        args.put("x-dead-letter-exchange", MqConstants.SECKILL_DLX_EXCHANGE);
        // 转发时携带的死信路由键
        args.put("x-dead-letter-routing-key", MqConstants.SECKILL_DLX_ROUTING_KEY);

        return QueueBuilder.durable(MqConstants.SECKILL_TTL_QUEUE).withArguments(args).build();
    }

    @Bean
    public Binding seckillTtlBinding() {
        return BindingBuilder.bind(seckillTtlQueue()).to(seckillTtlExchange())
                .with(MqConstants.SECKILL_TTL_ROUTING_KEY);
    }
}
