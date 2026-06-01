package com.aiforge.biz.listener;

import com.aiforge.biz.dto.SeckillMessageDTO;
import com.aiforge.biz.entity.SeckillOrder;
import com.aiforge.biz.entity.SeckillPackage;
import com.aiforge.biz.mapper.BizSeckillOrderMapper;
import com.aiforge.biz.mapper.BizSeckillPackageMapper;
import com.aiforge.common.constant.MqConstants;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillOrderConsumer {

    private final BizSeckillPackageMapper packageMapper;
    private final BizSeckillOrderMapper orderMapper;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    // 本地缓存：避免创单时频繁查询数据库获取套餐价格
    private final Map<Long, SeckillPackage> packageCache = new ConcurrentHashMap<>();

    /**
     * 监听普通队列，真正落盘创建数据库订单
     */
    @RabbitListener(queues = MqConstants.SECKILL_ORDER_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void createOrderInDb(SeckillMessageDTO message) {
        try {
            // 1. 幂等性判断：检查订单是否已存在，防止 MQ 消息重复投递
            Long count = orderMapper.selectCount(new LambdaQueryWrapper<SeckillOrder>()
                    .eq(SeckillOrder::getOrderNo, message.getOrderNo()));
            if (count > 0) {
                log.info("订单已存在，属于 MQ 重复投递，直接丢弃消息: {}", message.getOrderNo());
                return;
            }

            Long packageId = message.getPackageId();

            // 2. 查询套餐信息，获取秒杀价格（使用本地缓存兜底，减轻 DB 读取压力）
            SeckillPackage seckillPackage = packageCache.computeIfAbsent(packageId, packageMapper::selectById);
            if (seckillPackage == null) {
                log.error("未找到对应的秒杀套餐: {}", packageId);
                return;
            }

            // 3. 数据库扣库存（带上可用库存 > 0 的乐观锁）
            int updatedCount = packageMapper.update(null, new LambdaUpdateWrapper<SeckillPackage>()
                    .setSql("available_stock = available_stock - 1")
                    .eq(SeckillPackage::getPackageId, packageId)
                    .gt(SeckillPackage::getAvailableStock, 0));

            if (updatedCount == 0) {
                log.warn("扣减 MySQL 库存失败, 套餐已售罄: {}", packageId);
                return;
            }

            // 4. 生成 MySQL 订单 (待支付状态)
            SeckillOrder order = new SeckillOrder();
            order.setOrderNo(message.getOrderNo());
            order.setUserId(message.getUserId());
            order.setPackageId(packageId);
            order.setPayAmount(seckillPackage.getPrice()); // 设置支付金额
            order.setStatus(0); // 0-待支付
            orderMapper.insert(order);

            // 3. 创单成功后，发往 TTL 缓冲队列，设置过期时间
            // 只要消息在这个队列里存活 15 分钟没人消费，就会掉入 DLX 死信队列
            rabbitTemplate.convertAndSend(MqConstants.SECKILL_TTL_EXCHANGE, MqConstants.SECKILL_TTL_ROUTING_KEY,
                    message, msg -> {
                        msg.getMessageProperties().setExpiration(String.valueOf(15 * 60 * 1000)); // 15分钟过期
                        return msg;
                    });
            log.info("秒杀订单[{}]已入库，并送入 TTL 队列等待支付校验", message.getOrderNo());

        } catch (Exception e) {
            log.error("秒杀异步创单异常", e);
        }
    }

    /**
     * 监听死信队列，15 分钟后触发，检查未支付就回滚
     */
    @RabbitListener(queues = MqConstants.SECKILL_DLX_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void cancelUnpaidOrder(SeckillMessageDTO message) {
        // 1. 查出该订单
        SeckillOrder order = orderMapper.selectOne(new LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getOrderNo, message.getOrderNo()));

        // 2. 如果订单仍是待支付状态
        if (order != null && order.getStatus() == 0) {
            // 2.1. 修改订单为已取消 (2)
            order.setStatus(2);
            orderMapper.updateById(order);

            // 2.2. MySQL 库存 +1
            packageMapper.update(null, new LambdaUpdateWrapper<SeckillPackage>()
                    .setSql("available_stock = available_stock + 1")
                    .eq(SeckillPackage::getPackageId, message.getPackageId()));

            // 2.3. Redis 库存 +1
            stringRedisTemplate.opsForValue().increment("seckill:stock:" + message.getPackageId());

            // 2.4. 把用户从 Redis 已购名单中删除，使其可以重新抢购
            stringRedisTemplate.opsForSet().remove("seckill:users:" + message.getPackageId(),
                    message.getUserId().toString());

            log.info("秒杀订单[{}]超时未支付，DLX 死信队列触发，已自动取消并回补所有库存！", message.getOrderNo());
        }
    }
}
