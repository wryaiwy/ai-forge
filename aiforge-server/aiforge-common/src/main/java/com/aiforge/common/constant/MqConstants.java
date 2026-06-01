package com.aiforge.common.constant;

/**
 * MQ 常量
 */
public interface MqConstants {

    // 向量同步队列
    String VECTOR_SYNC_QUEUE = "aiforge_vector_sync_queue";

    // ================= 秒杀业务 MQ 常量 =================

    // 普通创单队列
    String SECKILL_EXCHANGE = "seckill.exchange";
    String SECKILL_ORDER_QUEUE = "seckill.order.queue";
    String SECKILL_ORDER_ROUTING_KEY = "seckill.order.create";

    // TTL 缓冲队列（无消费者，用于死信倒计时）
    String SECKILL_TTL_EXCHANGE = "seckill.ttl.exchange";
    String SECKILL_TTL_QUEUE = "seckill.ttl.queue";
    String SECKILL_TTL_ROUTING_KEY = "seckill.order.ttl";

    // DLX 死信队列（超时未支付订单处理队列）
    String SECKILL_DLX_EXCHANGE = "seckill.dlx.exchange";
    String SECKILL_DLX_QUEUE = "seckill.dlx.queue";
    String SECKILL_DLX_ROUTING_KEY = "seckill.order.dlx";
}
