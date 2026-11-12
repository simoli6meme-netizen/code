-- ============================================================
-- 秒杀模块建表脚本（code2026）
-- 在 code2026 库中执行一次即可，重复执行不会重复扣减商品库存
-- ============================================================

CREATE TABLE IF NOT EXISTS seckill_activity
(
    id            INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    goods_id      INT          NOT NULL COMMENT '商品ID',
    seckill_price DECIMAL(10, 2) NOT NULL COMMENT '秒杀价',
    stock         INT          NOT NULL DEFAULT 0 COMMENT '活动库存（创建活动时从商品库存划拨）',
    start_time    DATETIME     NOT NULL COMMENT '开始时间',
    end_time      DATETIME     NOT NULL COMMENT '结束时间',
    status        VARCHAR(10)  NOT NULL DEFAULT '启用' COMMENT '启用 / 停用',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_goods (goods_id),
    KEY idx_time (start_time, end_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='秒杀活动';

-- 一人一单的最终兜底：数据库唯一索引。
-- Redis 的 Set 只是前置拦截，真正防止重复下单的是这个约束。
CREATE TABLE IF NOT EXISTS seckill_record
(
    id          INT UNSIGNED NOT NULL AUTO_INCREMENT,
    activity_id INT          NOT NULL COMMENT '活动ID',
    user_id     INT          NOT NULL COMMENT '用户ID',
    order_id    INT          DEFAULT NULL COMMENT '生成的订单ID',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_user (activity_id, user_id),
    KEY idx_user (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='秒杀成功记录';

-- ============================================================
-- 示例活动：给「全麦欧包(id=14)」配一场 1 元秒杀，库存 50 件
-- @need = 0 时才插入，避免重复执行导致库存被多次划拨
-- ============================================================
SET @need := (SELECT COUNT(*) FROM seckill_activity WHERE goods_id = 14);

INSERT INTO seckill_activity (goods_id, seckill_price, stock, start_time, end_time)
SELECT 14, 1.00, 50, DATE_SUB(NOW(), INTERVAL 10 MINUTE), DATE_ADD(NOW(), INTERVAL 7 DAY)
FROM DUAL
WHERE @need = 0;

UPDATE goods
SET store = store - 50
WHERE id = 14
  AND @need = 0
  AND store >= 50;

SELECT id, goods_id, seckill_price, stock, start_time, end_time
FROM seckill_activity;
