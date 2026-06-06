import asyncio
import json
import logging
import aio_pika
from typing import Dict, Any

from app.config import settings
from services.vector_store import vector_store_service

logger = logging.getLogger(__name__)

async def process_vector_message(message_body: bytes):
    """
    处理向量化同步消息
    期望的消息格式 (JSON):
    {
        "text": "要向量化的文本",
        "metadata": {"bizId": "123", "bizType": "article"}
    }
    或者批量:
    {
        "texts": ["文本1", "文本2"],
        "metadatas": [{"bizId": "1"}, {"bizId": "2"}]
    }
    """
    try:
        data: Dict[str, Any] = json.loads(message_body.decode())
        
        action = data.get("action", "add")
        
        # 兼容 Java 端传来的枚举 code
        action_map = {"1": "add", "2": "update", "3": "delete"}
        if action in action_map:
            action = action_map[action]

        # 统一获取 biz_id 和 biz_type (无论是根节点还是 metadata)
        metadata = data.get("metadata", {})
        biz_id = data.get("bizId") or metadata.get("bizId")
        biz_type = data.get("bizType") or metadata.get("bizType")
        
        # 1. 处理删除逻辑
        if action == "delete":
            if not biz_id or not biz_type:
                logger.warning(f"MQ 删除消息缺少 bizId 或 bizType: {data}")
                return False
            logger.info(f"开始处理向量删除任务, bizId: {biz_id}, bizType: {biz_type}")
            await vector_store_service.delete_by_biz(biz_id, biz_type)
            logger.info(f"向量删除任务完成: {biz_id}")
            return True

        # 2. 如果是 update，先执行删除旧数据
        if action == "update":
            if biz_id and biz_type:
                logger.info(f"更新操作：先删除旧向量数据, bizId: {biz_id}, bizType: {biz_type}")
                await vector_store_service.delete_by_biz(biz_id, biz_type)
        
        # 3. 处理新增/更新的入库逻辑
        texts = []
        metadatas = []
        
        if "texts" in data:
            texts = data["texts"]
            metadatas = data.get("metadatas")
        elif "text" in data:
            texts = [data["text"]]
            if "metadata" in data:
                metadatas = [data["metadata"]]
            else:
                metadatas = None
        else:
            logger.warning(f"MQ 消息格式错误，缺少 text/texts 字段: {data}")
            return False
            
        logger.info(f"开始处理向量化任务, 包含 {len(texts)} 条文本数据 (操作类型: {action})")
        
        # 调用向量库服务存入数据
        await vector_store_service.add_documents(texts=texts, metadatas=metadatas)
        logger.info(f"向量化任务处理完成, 成功入库: {metadatas}")
        return True
        
    except json.JSONDecodeError:
        logger.error(f"MQ 消息非合法 JSON 格式: {message_body}")
        return False
    except Exception as e:
        logger.error(f"处理向量化消息失败: {e}", exc_info=True)
        raise e

async def start_rabbitmq_consumer():
    """
    启动 RabbitMQ 消费者，以常驻后台任务运行
    高级重试与死信队列架构
    1. 主队列消费失败 -> 拒绝并不重入队(requeue=False) -> 进入重试死信交换机(Retry DLX)
    2. 重试队列设置 TTL(如 10 秒) 和死信回主队列。消息停留 10 秒后自动回主队列重新消费。
    3. 检查 x-death 请求头，如果重试超过最大次数(如 3 次)，说明是毒药消息，
       则手动发送到最终死信队列(Final DLQ)，并发送告警，防止死循环雪崩。
    """
    connection = None
    MAX_RETRY = 3
    
    while True:
        try:
            logger.info(f"正在连接 RabbitMQ: {settings.MQ_HOST}:{settings.MQ_PORT}...")
            amqp_url = f"amqp://{settings.MQ_USER}:{settings.MQ_PASSWORD}@{settings.MQ_HOST}:{settings.MQ_PORT}/"
            connection = await aio_pika.connect_robust(amqp_url)
            
            async with connection:
                channel = await connection.channel()
                
                # --- 1. 声明最终死信队列 (用于存放彻底没救的消息) ---
                dead_exchange = await channel.declare_exchange("vector_dead_exchange", aio_pika.ExchangeType.DIRECT, durable=True)
                dead_queue = await channel.declare_queue("vector_dead_queue", durable=True)
                await dead_queue.bind(dead_exchange, routing_key="vector_dead_key")
                
                # --- 2. 声明延迟重试队列 (利用 TTL 暂存失败消息) ---
                retry_exchange = await channel.declare_exchange("vector_retry_exchange", aio_pika.ExchangeType.DIRECT, durable=True)
                # 重试队列的死信交换机指向主交换机 (即 TTL 到期后回主队列)
                retry_queue = await channel.declare_queue(
                    "vector_retry_queue", 
                    durable=True,
                    arguments={
                        "x-message-ttl": 10000, # 延迟 10 秒重试
                        "x-dead-letter-exchange": "vector_sync_exchange",
                        "x-dead-letter-routing-key": "vector_sync_key"
                    }
                )
                await retry_queue.bind(retry_exchange, routing_key="vector_retry_key")
                
                # --- 3. 声明主队列 (携带死信配置指向重试队列) ---
                main_exchange = await channel.declare_exchange("vector_sync_exchange", aio_pika.ExchangeType.DIRECT, durable=True)
                main_queue = await channel.declare_queue(
                    settings.MQ_QUEUE_VECTOR_SYNC,
                    durable=True,
                    arguments={
                        "x-dead-letter-exchange": "vector_retry_exchange",
                        "x-dead-letter-routing-key": "vector_retry_key"
                    }
                )
                await main_queue.bind(main_exchange, routing_key="vector_sync_key")
                
                logger.info(f"RabbitMQ 连接成功。开始监听: {settings.MQ_QUEUE_VECTOR_SYNC}")
                
                # 消费消息
                async with main_queue.iterator() as queue_iter:
                    async for message in queue_iter:
                        async with message.process(ignore_processed=True): # ignore_processed 允许我们手动控制 ACK/NACK
                            try:
                                # 解析重试次数
                                retry_count = 0
                                if message.headers and "x-death" in message.headers:
                                    # x-death 是个列表，取出总死信次数
                                    death_info = message.headers["x-death"]
                                    if death_info and isinstance(death_info, list):
                                        retry_count = sum(int(item.get("count", 0)) for item in death_info)
                                
                                if retry_count >= MAX_RETRY:
                                    logger.error(f"消息重试达到最大次数 ({MAX_RETRY})，丢入最终死信队列告警人工处理! 消息内容: {message.body}")
                                    # 将消息扔进最终死信队列
                                    await dead_exchange.publish(
                                        aio_pika.Message(body=message.body, delivery_mode=aio_pika.DeliveryMode.PERSISTENT),
                                        routing_key="vector_dead_key"
                                    )
                                    # 主队列直接 ACK，结束这个毒药消息的生命周期
                                    await message.ack()
                                    # TODO: 在这里可以触发飞书/钉钉 webhook 告警
                                    continue

                                # 正常处理业务逻辑
                                await process_vector_message(message.body)
                                
                                # 处理成功，手动 ACK
                                await message.ack()
                                
                            except Exception as e:
                                logger.warning(f"处理失败 (第 {retry_count + 1} 次尝试): {e}")
                                # 处理失败，拒绝消息，且不重入当前队列 (requeue=False)
                                # 这会触发配置的 DLX，将其投递到 vector_retry_exchange
                                await message.reject(requeue=False)
                                
        except Exception as e:
            logger.error(f"RabbitMQ 消费者运行异常: {e}")
            logger.info("5秒后尝试重新连接...")
            await asyncio.sleep(5)
        finally:
            if connection and not connection.is_closed:
                await connection.close()
