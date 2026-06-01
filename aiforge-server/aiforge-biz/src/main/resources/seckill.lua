local packageId = KEYS[1]
local userId = ARGV[1]

local stockKey = 'seckill:stock:' .. packageId
local userKey = 'seckill:users:' .. packageId

-- 1. 判断用户是否已经购买过 (一人一单)
if redis.call('SISMEMBER', userKey, userId) == 1 then
    return 2 -- 返回 2 代表已经购买过
end

-- 2. 判断库存是否充足
local stock = tonumber(redis.call('GET', stockKey))
if stock == nil or stock <= 0 then
    return 1 -- 返回 1 代表库存不足/售罄
end

-- 3. 执行秒杀操作
redis.call('DECR', stockKey)
redis.call('SADD', userKey, userId)

-- 4. 内存兜底：如果是第一个抢购成功的用户（集合刚被创建），给这个 Set 加个过期时间（比如 24 小时），防止内存一直不释放
if redis.call('SCARD', userKey) == 1 then
    redis.call('EXPIRE', userKey, 86400)
end

return 0 -- 返回 0 代表抢购成功
