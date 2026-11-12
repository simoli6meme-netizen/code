-- ============================================================
-- 秒杀扣减脚本：判断重复 + 判断库存 + 扣减 + 记录用户，四步在 Redis 里原子完成
-- 为什么要 Lua：如果拆成 SISMEMBER、GET、DECR 三次请求，中间会被其他请求插入，
-- 最终扣成负数，所以必须让 Redis 单线程一次性执行完这段逻辑。
--
-- KEYS[1] 活动库存   seckill:stock:{活动ID}
-- KEYS[2] 已抢用户   seckill:users:{活动ID}
-- ARGV[1] 用户ID
--
-- 返回值：>=0 剩余库存；-1 已售罄；-2 重复抢购；-3 活动未预热
-- ============================================================

local stockKey = KEYS[1]
local userKey = KEYS[2]
local userId = ARGV[1]

-- 1. 一人一单：先判断，避免重复用户把库存白白扣掉
if redis.call('SISMEMBER', userKey, userId) == 1 then
    return -2
end

-- 2. 活动没预热过（或 Redis 被清空），交给 Java 侧去预热后重试
local stock = redis.call('GET', stockKey)
if not stock then
    return -3
end

-- 3. 库存不足
if tonumber(stock) <= 0 then
    return -1
end

-- 4. 扣库存 + 记住这个用户
redis.call('DECR', stockKey)
redis.call('SADD', userKey, userId)
return tonumber(stock) - 1
