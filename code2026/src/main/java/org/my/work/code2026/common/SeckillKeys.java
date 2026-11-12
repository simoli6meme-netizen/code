package org.my.work.code2026.common;

/**
 * 秒杀相关的 Redis Key 统一在这里生成，避免两个类写错格式导致读写不一致。
 * <p>
 * key 里带 {活动ID} 这层花括号是 Redis Cluster 的 hash tag：
 * 同一场活动的库存和用户集合会落到同一个分片，Lua 脚本才能一次操作两个 key。
 **/
public final class SeckillKeys {

    private SeckillKeys() {
    }

    /** 活动剩余库存（String） */
    public static String stock(Integer activityId) {
        return "seckill:stock:{" + activityId + "}";
    }

    /** 已抢到该活动的用户集合（Set） */
    public static String users(Integer activityId) {
        return "seckill:users:{" + activityId + "}";
    }

    /** 异步下单结果（String：PENDING / SUCCESS:订单ID:订单号 / FAIL:原因） */
    public static String result(Integer activityId, Integer userId) {
        return "seckill:result:{" + activityId + "}:" + userId;
    }
}
