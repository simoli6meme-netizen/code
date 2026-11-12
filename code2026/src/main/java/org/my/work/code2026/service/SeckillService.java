package org.my.work.code2026.service;

import jakarta.annotation.Resource;
import org.my.work.code2026.common.GoodsCache;
import org.my.work.code2026.common.SeckillKeys;
import org.my.work.code2026.entity.Goods;
import org.my.work.code2026.entity.SeckillActivity;
import org.my.work.code2026.exception.CustomException;
import org.my.work.code2026.mapper.GoodsMapper;
import org.my.work.code2026.mapper.SeckillActivityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 秒杀业务。
 * <p>
 * 核心思路：Redis 做「闸门」，MySQL 做「账本」。
 * 抢购请求只碰 Redis（Lua 原子扣减），扣成功才异步落库，
 * 数据库压力从「并发请求数」降成「成功订单数」。
 **/
@Service
public class SeckillService {

    private static final Logger log = LoggerFactory.getLogger(SeckillService.class);

    @Resource
    private SeckillActivityMapper activityMapper;
    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private StringRedisTemplate redis;
    @Resource
    private DefaultRedisScript<Long> seckillScript;
    @Resource
    private SeckillOrderService orderService;
    @Resource
    private GoodsCache goodsCache;

    /**
     * 活动列表：数据库出基础信息，Redis 出实时剩余库存和「我是否抢过」。
     */
    public List<SeckillActivity> list(Integer userId) {
        List<SeckillActivity> list = activityMapper.selectAll();
        for (SeckillActivity activity : list) {
            try {
                String remain = redis.opsForValue().get(SeckillKeys.stock(activity.getId()));
                if (remain == null) {
                    // 活动刚建或 Redis 重启过，按数据库库存补一次
                    preheat(activity.getId());
                    remain = redis.opsForValue().get(SeckillKeys.stock(activity.getId()));
                }
                activity.setRemain(remain == null ? activity.getStock() : Integer.parseInt(remain));
                if (userId != null) {
                    activity.setBought(Boolean.TRUE.equals(redis.opsForSet()
                            .isMember(SeckillKeys.users(activity.getId()), String.valueOf(userId))));
                }
            } catch (Exception e) {
                // Redis 不可用时降级：用数据库库存展示，至少页面还能打开
                log.warn("读取秒杀缓存失败，降级为数据库库存 activityId={}", activity.getId(), e);
                activity.setRemain(activity.getStock());
                activity.setBought(false);
            }
        }
        return list;
    }

    /**
     * 创建活动：把商品库存「划拨」到活动库存里，避免和正常下单重复售卖同一批货。
     */
    @Transactional
    public SeckillActivity create(SeckillActivity activity) {
        if (activity.getGoodsId() == null) {
            throw new CustomException("请选择商品");
        }
        if (activity.getStock() == null || activity.getStock() <= 0) {
            throw new CustomException("活动库存必须大于 0");
        }
        if (activity.getSeckillPrice() == null || activity.getSeckillPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("秒杀价必须大于 0");
        }
        if (activity.getStartTime() == null || activity.getEndTime() == null) {
            throw new CustomException("请填写活动起止时间");
        }
        Goods goods = goodsMapper.selectById(activity.getGoodsId());
        if (goods == null) {
            throw new CustomException("商品不存在");
        }
        if (activity.getSeckillPrice().compareTo(goods.getPrice()) >= 0) {
            throw new CustomException("秒杀价必须低于商品原价");
        }
        if (goodsMapper.deductStore(activity.getGoodsId(), activity.getStock()) == 0) {
            throw new CustomException("商品库存不足，无法划拨活动库存");
        }
        activity.setStatus("启用");
        activityMapper.insert(activity);
        // 先清掉可能残留的同 id 缓存，否则 setIfAbsent 会跳过预热、沿用旧库存
        redis.delete(SeckillKeys.stock(activity.getId()));
        redis.delete(SeckillKeys.users(activity.getId()));
        preheat(activity.getId());
        // 划拨库存改了 goods.store，事务提交后失效商品缓存
        goodsCache.evictAllAfterCommit();
        return activityMapper.selectById(activity.getId());
    }

    /** 管理端：启用 / 停用（不动库存） */
    public void toggleStatus(Integer id, String status) {
        if (!"启用".equals(status) && !"停用".equals(status)) {
            throw new CustomException("状态只能是「启用」或「停用」");
        }
        if (activityMapper.selectById(id) == null) {
            throw new CustomException("活动不存在");
        }
        activityMapper.updateStatus(id, status);
    }

    /**
     * 管理端：删除活动。
     * 已经有人抢购的活动不允许删（订单明细会失去活动来源），只能停用；
     * 无人抢购时把剩余配额归还商品普通库存，并清掉 Redis 缓存。
     */
    @Transactional
    public void remove(Integer id) {
        SeckillActivity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new CustomException("活动不存在");
        }
        if (!activityMapper.selectRecordUserIds(id).isEmpty()) {
            throw new CustomException("已有用户抢购，此活动只能停用，不能删除");
        }
        goodsMapper.restoreStore(activity.getGoodsId(), activity.getStock());
        activityMapper.deleteById(id);
        redis.delete(SeckillKeys.stock(id));
        redis.delete(SeckillKeys.users(id));
        log.info("秒杀活动已删除并归还库存 activityId={}, goodsId={}, 归还数量={}",
                id, activity.getGoodsId(), activity.getStock());
        goodsCache.evictAllAfterCommit();
    }

    /**
     * 预热：把数据库库存刷进 Redis，并重建「一人一单」标记。
     * setIfAbsent 很关键——只有 key 不存在时才写，不会把已经扣减过的库存重置回初始值。
     */
    public void preheat(Integer activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new CustomException("活动不存在");
        }
        redis.opsForValue().setIfAbsent(SeckillKeys.stock(activityId), String.valueOf(activity.getStock()));

        List<Integer> userIds = activityMapper.selectRecordUserIds(activityId);
        if (!userIds.isEmpty()) {
            String[] members = userIds.stream().map(String::valueOf).toArray(String[]::new);
            redis.opsForSet().add(SeckillKeys.users(activityId), members);
        }
        log.info("秒杀活动预热完成 activityId={}, dbStock={}, 已抢用户数={}",
                activityId, activity.getStock(), userIds.size());
    }

    /**
     * 抢购：一次 Lua 调用完成「判重 + 判库存 + 扣减 + 记录」，然后异步下单。
     */
    public Map<String, Object> seckill(Integer activityId, Integer userId) {
        if (userId == null) {
            throw new CustomException("请先登录");
        }
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new CustomException("活动不存在");
        }
        if (!"启用".equals(activity.getStatus())) {
            throw new CustomException("活动已停用");
        }
        if ("未开始".equals(activity.getState())) {
            throw new CustomException("活动还没开始，请稍等");
        }
        if ("已结束".equals(activity.getState())) {
            throw new CustomException("活动已结束");
        }

        long remain = executeScript(activityId, userId);
        if (remain == -3L) {
            // 没预热过，补一次再重试
            preheat(activityId);
            remain = executeScript(activityId, userId);
        }
        if (remain == -1L) {
            throw new CustomException("手慢了，已经被抢光");
        }
        if (remain == -2L) {
            throw new CustomException("每人限抢一件，你已经抢到过了");
        }

        // 扣减成功：先返回「排队中」，订单交给线程池异步落库
        redis.opsForValue().set(SeckillKeys.result(activityId, userId), "PENDING", Duration.ofMinutes(30));
        orderService.createOrderAsync(activityId, userId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "PENDING");
        data.put("remain", remain);
        data.put("tip", "抢购成功，正在生成订单");
        return data;
    }

    /**
     * 查询异步下单结果，前端拿到 PENDING 就隔几百毫秒再轮询一次。
     */
    public Map<String, Object> result(Integer activityId, Integer userId) {
        String value = redis.opsForValue().get(SeckillKeys.result(activityId, userId));
        Map<String, Object> data = new LinkedHashMap<>();
        if (value == null) {
            data.put("status", "NOT_FOUND");
            return data;
        }
        if ("PENDING".equals(value)) {
            data.put("status", "PENDING");
            return data;
        }
        if (value.startsWith("SUCCESS:")) {
            String[] parts = value.split(":");
            data.put("status", "SUCCESS");
            data.put("orderId", parts[1]);
            data.put("orderNo", parts[2]);
            return data;
        }
        data.put("status", "FAIL");
        data.put("msg", value.length() > 5 ? value.substring(5) : "下单失败");
        return data;
    }

    /** 执行 Lua，返回 >=0 剩余库存 或 -1 / -2 / -3 */
    private long executeScript(Integer activityId, Integer userId) {
        try {
            Long result = redis.execute(seckillScript,
                    List.of(SeckillKeys.stock(activityId), SeckillKeys.users(activityId)),
                    String.valueOf(userId));
            return result == null ? -3L : result;
        } catch (Exception e) {
            log.error("执行秒杀脚本失败 activityId={}, userId={}", activityId, userId, e);
            throw new CustomException("秒杀服务暂时不可用，请稍后重试");
        }
    }
}
