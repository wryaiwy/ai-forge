package com.aiforge.biz.service.impl;

import com.aiforge.biz.entity.SeckillPackage;
import com.aiforge.biz.enums.SeckillLuaResultEnum;
import com.aiforge.biz.enums.SeckillPackageStatusEnum;
import com.aiforge.biz.mapper.BizSeckillPackageMapper;
import com.aiforge.biz.service.BizSeckillPackageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import com.aiforge.biz.dto.SeckillMessageDTO;
import com.aiforge.common.result.Result;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class BizSeckillPackageServiceImpl extends ServiceImpl<BizSeckillPackageMapper, SeckillPackage>
        implements BizSeckillPackageService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT; // 用于封装要执行的 Lua 脚本
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>(); // 初始化 Lua 脚本
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua")); // 设置 Lua 脚本文件路径
        SECKILL_SCRIPT.setResultType(Long.class); // 设置结果类型为 Long
    }

    // 本地缓存：记录套餐是否已售罄，防止 Redis 过载
    private final Map<Long, Boolean> emptyStockMap = new ConcurrentHashMap<>();

    /**
     * 定时任务预热缓存
     * 每 5 分钟执行一次，找出未来一小时内的活动推入 Redis
     */
    @PostConstruct // 保证项目启动时立刻预热一次，防止重启后 Map 为空误杀合法请求
    // @Scheduled(cron = "0 0/5 * * * ?")
    @Scheduled(cron = "0 0 0 1 1 ?")
    public void preHeatSeckillPackages() {
        log.info("开始执行秒杀套餐缓存预热...");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        LambdaQueryWrapper<SeckillPackage> lqw = new LambdaQueryWrapper<>();
        lqw.le(SeckillPackage::getStartTime, oneHourLater)
                .ge(SeckillPackage::getEndTime, now)
                .eq(SeckillPackage::getStatus, SeckillPackageStatusEnum.ONLINE.getCode());

        List<SeckillPackage> packages = this.list(lqw);

        for (SeckillPackage pack : packages) {
            String stockKey = "seckill:stock:" + pack.getPackageId();
            Boolean hasKey = stringRedisTemplate.hasKey(stockKey);
            if (Boolean.FALSE.equals(hasKey)) {
                // 计算过期时间：活动结束时间 + 24 小时作为缓冲
                long ttlSeconds = Duration.between(now, pack.getEndTime()).getSeconds() + 86400;
                stringRedisTemplate.opsForValue().set(stockKey, pack.getAvailableStock().toString(), ttlSeconds, TimeUnit.SECONDS);
            }
            // 预热时初始化本地售罄标志为 false
            emptyStockMap.put(pack.getPackageId(), false);
        }
    }

    /**
     * 执行秒杀下单（用户端）
     */
    @Override
    public Result<String> doSeckill(Long packageId, Long userId) {
        // 1. 本地内存标记，防穿透（拦截不存在的 key 打到 Redis）
        // 如果连 Key 都没有，说明是非法 ID 或者活动压根没预热/未开始，直接拦截，防止 OOM！
        if (!emptyStockMap.containsKey(packageId)) {
            return Result.fail("活动不存在或尚未开始！");
        }
        // 如果值是 true，说明售罄，直接拦截
        if (emptyStockMap.get(packageId)) {
            return Result.fail("手慢了，该套餐已被抢空！");
        }

        // 2. 调用 Lua 脚本进行预扣减
        List<String> keys = Collections.singletonList(packageId.toString());
        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT, keys, userId.toString());

        // 3. 根据 Lua 返回值判断结果
        SeckillLuaResultEnum luaResult = SeckillLuaResultEnum.getByCode(result);
        if (luaResult == SeckillLuaResultEnum.STOCK_NOT_ENOUGH) {
            // 记录售罄，后续请求将被直接挡在本地
            emptyStockMap.put(packageId, true);
            return Result.fail("手慢了，该套餐已被抢空！");
        }
        if (luaResult == SeckillLuaResultEnum.ALREADY_PURCHASED) {
            return Result.fail("您已经抢购过该套餐，每人限购一次！");
        }

        // 4. 扣减库存、记录用户均成功。
        String orderNo = UUID.randomUUID().toString().replace("-", "");

        // 5. 将创单指令丢给 RabbitMQ 异步处理
        SeckillMessageDTO message = new SeckillMessageDTO(userId, packageId, orderNo);
        rabbitTemplate.convertAndSend("seckill.exchange", "seckill.order.create", message);

        // 6. 立即告诉前端
        return Result.success("抢购成功，系统正在为您生成订单中..." + orderNo);
    }
}
