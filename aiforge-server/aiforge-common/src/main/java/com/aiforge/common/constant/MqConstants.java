package com.aiforge.common.constant;

/**
 * MQ 常量定义
 * 集中管理所有 RabbitMQ 相关的 Exchange、Queue 和 RoutingKey
 */
public interface MqConstants {

    /**
     * 【旧版兼容 / Python消费者专用】向量同步队列
     * 作用：底层的 Python 服务（rabbitmq_consumer.py）直接监听此队列。
     * 消息格式：必须包含 text、metadata 等特定键的 JSON 字典。
     */
    String VECTOR_SYNC_QUEUE = "aiforge_vector_sync_queue";

    /**
     * 【AI Agent】异步工具执行队列
     * 作用：Python 端发送异步执行工具指令，Java 端监听此队列并执行
     */
    String ASYNC_TOOL_QUEUE = "aiforge.async.tool.queue";

    // ================= 文章发布广播 MQ 常量 =================

    /**
     * 【文章发布广播】广播交换机 (Fanout)
     * 作用：文章新增/更新/删除时，向此交换机投递一条 ArticleMessage 消息。所有绑定到此交换机的队列都会收到该消息（一发多接）。
     */
    String ARTICLE_EXCHANGE = "article.fanout.exchange";

    /**
     * 【文章同步 ES】队列
     * 作用：绑定在 ARTICLE_EXCHANGE 上。Java 的 EsArticleConsumer 监听此队列，负责将文章数据落盘到 Elasticsearch，供全局搜索使用。
     */
    String ARTICLE_ES_QUEUE = "article.es.sync.queue";

    /**
     * 【文章同步知识库】过渡队列
     * 作用：绑定在 ARTICLE_EXCHANGE 上。Java 的 KnowledgeArticleConsumer 监听此队列，拿到 ArticleMessage 后，将其转换为 Python 服务所需的格式，并转发给 VECTOR_SYNC_QUEUE。
     */
    String ARTICLE_VECTOR_QUEUE = "article.vector.sync.queue";

    // ================= 秒杀业务 MQ 常量 =================

    /**
     * 【秒杀】主交换机 (Direct)
     * 作用：处理秒杀下单的核心交换机。
     */
    String SECKILL_EXCHANGE = "seckill.exchange";

    /**
     * 【秒杀】普通创单队列
     * 作用：秒杀接口收到请求后，发消息到此队列。消费者异步扣减库存并真正在数据库中创建订单。
     */
    String SECKILL_ORDER_QUEUE = "seckill.order.queue";

    /**
     * 【秒杀】普通创单路由键
     */
    String SECKILL_ORDER_ROUTING_KEY = "seckill.order.create";

    /**
     * 【秒杀】TTL 死信缓冲交换机 (Direct)
     * 作用：订单创建后，将消息发送到此交换机，再进入 TTL 队列开始倒计时。
     */
    String SECKILL_TTL_EXCHANGE = "seckill.ttl.exchange";

    /**
     * 【秒杀】TTL 缓冲队列 (无消费者)
     * 作用：该队列没有消费者，只配置了消息的存活时间 (TTL) 以及死信去向。消息过期后，自动转发到死信交换机 (DLX)。用于实现“订单超时未支付自动取消”功能。
     */
    String SECKILL_TTL_QUEUE = "seckill.ttl.queue";

    /**
     * 【秒杀】TTL 缓冲队列路由键
     */
    String SECKILL_TTL_ROUTING_KEY = "seckill.order.ttl";

    /**
     * 【秒杀】死信交换机 DLX (Direct)
     * 作用：接收来自 TTL 缓冲队列里过期的（未支付的）订单消息。
     */
    String SECKILL_DLX_EXCHANGE = "seckill.dlx.exchange";

    /**
     * 【秒杀】死信队列
     * 作用：消费者监听此队列，收到消息后去数据库检查订单状态。如果仍未支付，则关单并恢复库存。
     */
    String SECKILL_DLX_QUEUE = "seckill.dlx.queue";

    /**
     * 【秒杀】死信队列路由键
     */
    String SECKILL_DLX_ROUTING_KEY = "seckill.order.dlx";
}
