package org.my.work.code2026.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import jakarta.annotation.Resource;
import org.my.work.code2026.common.AsyncConfig;
import org.my.work.code2026.common.GoodsCache;
import org.my.work.code2026.common.OrderDeadline;
import org.my.work.code2026.common.SeckillKeys;
import org.my.work.code2026.entity.OrderDetail;
import org.my.work.code2026.entity.Orders;
import org.my.work.code2026.entity.SeckillActivity;
import org.my.work.code2026.entity.User;
import org.my.work.code2026.exception.CustomException;
import org.my.work.code2026.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Date;

/**
 * 秒杀订单落库（异步执行）。
 * <p>
 * 为什么单独一个类：
 * 1. @Async 和 @Transactional 都靠代理生效，同一个类里 this 调用会绕过代理，注解直接失效；
 * 2. 这里用 TransactionTemplate 显式开事务，比再拆一层类更直观。
 **/
@Service
public class SeckillOrderService {

    private static final Logger log = LoggerFactory.getLogger(SeckillOrderService.class);
    private static final Duration RESULT_TTL = Duration.ofMinutes(30);

    @Resource
    private SeckillActivityMapper activityMapper;
    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private OrderDetailMapper orderDetailMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private StringRedisTemplate redis;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private GoodsCache goodsCache;
    @Resource
    private OrderDeadline orderDeadline;

    /**
     * Redis 扣减成功后，把真正的订单写进 MySQL。
     * 这一步失败必须补偿 Redis，否则会出现「库存扣了但订单没生成」。
     */
    @Async(AsyncConfig.SECKILL_EXECUTOR)
    public void createOrderAsync(Integer activityId, Integer userId) {
        try {
            CreatedOrder created = transactionTemplate.execute(status -> doCreate(activityId, userId));
            writeResult(activityId, userId,
                    "SUCCESS:" + created.getId() + ":" + created.getOrderNo());
        } catch (DuplicateKeyException e) {
            // 唯一索引冲突：这个人之前已经抢到过，把这次多扣的库存还回去，用户标记保留
            giveBackStock(activityId);
            writeResult(activityId, userId, "FAIL:每人限抢一件，你之前已经抢到过");
        } catch (Exception e) {
            // 其它任何失败：库存还回去 + 放开该用户，让他可以重试
            giveBackStock(activityId);
            redis.opsForSet().remove(SeckillKeys.users(activityId), String.valueOf(userId));
            String msg = e instanceof CustomException ? ((CustomException) e).getMsg() : "下单失败，请稍后重试";
            log.warn("秒杀下单失败 activityId={}, userId={}, msg={}", activityId, userId, msg, e);
            writeResult(activityId, userId, "FAIL:" + msg);
        }
    }

    /**
     * 真正的事务方法：扣活动库存 → 写抢购记录 → 扣余额 → 建订单 → 建明细 → 增销量。
     * 任何一步失败，整笔回滚，不会出现「扣了钱没订单」。
     */
    private CreatedOrder doCreate(Integer activityId, Integer userId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new CustomException("活动不存在");
        }

        // 1. 数据库库存兜底（Redis 挂了或超卖时，这里是最后一道闸）
        if (activityMapper.deductStock(activityId) == 0) {
            throw new CustomException("库存已售完");
        }

        // 2. 抢购记录：uk_activity_user 唯一索引冲突会抛 DuplicateKeyException，兜底一人一单
        activityMapper.insertRecord(activityId, userId);

        // 3. 扣余额（条件更新，余额不足返回 0）
        BigDecimal price = activity.getSeckillPrice();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new CustomException("用户不存在");
        }
        if (userMapper.deductAccount(userId, price) == 0) {
            throw new CustomException("余额不足，请先充值");
        }

        // 4. 生成订单：秒杀默认到店自提，避免依赖收货地址
        Orders orders = new Orders();
        orders.setOrderNo(DateUtil.format(new Date(), "yyyyMMdd") + RandomUtil.randomNumbers(6));
        orders.setTotal(price);
        orders.setUserId(userId);
        orders.setStatus(OrdersService.STATUS_PENDING);
        orders.setTime(DateUtil.now());
        orders.setCloseDeadline(orderDeadline.next());   // 秒杀单同样参与超时关单
        orders.setDeliverType("自提");
        orders.setAddress("到店自提（秒杀活动）");
        ordersMapper.insert(orders);

        // 5. 订单明细
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orders.getId());
        detail.setGoodsId(activity.getGoodsId());
        detail.setGoodsName(activity.getGoodsName());
        detail.setGoodsImg(activity.getGoodsImg());
        detail.setGoodsPrice(price);
        detail.setNum(1);
        orderDetailMapper.insert(detail);

        // 6. 回填订单ID + 增加商品销量
        activityMapper.updateRecordOrderId(activityId, userId, orders.getId());
        goodsMapper.increaseSaleCount(activity.getGoodsId(), 1);
        goodsCache.evictAllAfterCommit();   // 销量变了，提交后失效商品缓存

        return new CreatedOrder(orders.getId(), orders.getOrderNo());
    }

    private void giveBackStock(Integer activityId) {
        redis.opsForValue().increment(SeckillKeys.stock(activityId));
    }

    private void writeResult(Integer activityId, Integer userId, String value) {
        redis.opsForValue().set(SeckillKeys.result(activityId, userId), value, RESULT_TTL);
    }

    /** 下单结果，用事务方法返回，避免多次查库 */
    private static class CreatedOrder {
        private final Integer id;
        private final String orderNo;

        CreatedOrder(Integer id, String orderNo) {
            this.id = id;
            this.orderNo = orderNo;
        }

        Integer getId() {
            return id;
        }

        String getOrderNo() {
            return orderNo;
        }
    }
}
