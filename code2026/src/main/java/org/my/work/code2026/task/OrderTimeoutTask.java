package org.my.work.code2026.task;

import jakarta.annotation.Resource;
import org.my.work.code2026.mapper.OrdersMapper;
import org.my.work.code2026.service.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 超时自动关单：每 30 秒扫一次「已到截止时间仍是待接单」的订单，取消并退款。
 * <p>
 * 为什么用定时扫表而不是延迟队列：项目里订单量很小，扫表实现最简单、不引入 MQ；
 * 加上 idx_close_deadline 索引后每轮只扫极少量行。面试要能说出取舍——
 * 订单量大时改 Redis ZSet / Redisson 延迟队列 / MQ 延迟消息，并处理消息丢失与幂等消费。
 * <p>
 * 幂等靠 OrdersMapper.closeIfPending 的条件更新：多实例同时扫到同一单，只有一个能关成功。
 **/
@Component
public class OrderTimeoutTask {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutTask.class);

    @Value("${order.close-enabled:true}")
    private boolean enabled;

    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private OrdersService ordersService;

    /** 启动 20 秒后才第一次执行，给应用留足初始化时间 */
    @Scheduled(fixedDelayString = "${order.scan-interval-ms:30000}", initialDelay = 20000)
    public void closeExpiredOrders() {
        if (!enabled) {
            return;
        }
        List<Integer> ids;
        try {
            ids = ordersMapper.selectExpiredIds();
        } catch (Exception e) {
            log.error("扫描超时订单失败", e);
            return;
        }
        if (ids.isEmpty()) {
            return;
        }
        log.info("扫描到 {} 笔超时未接单订单", ids.size());
        for (Integer id : ids) {
            try {
                ordersService.closeExpired(id);
            } catch (Exception e) {
                // 单笔失败不中断整批；事务已回滚，下一轮扫描会重试
                log.error("自动关单失败 orderId={}", id, e);
            }
        }
    }
}
