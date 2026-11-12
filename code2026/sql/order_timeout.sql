-- ============================================================
-- 超时自动关单：给 orders 增加「关单截止时间」列
--
-- 关键设计：只加列、绝不回填历史数据。
-- 历史订单的 close_deadline 保持 NULL，定时任务只扫非 NULL 的记录，
-- 因此脚本执行后不会把库里已有的「待接单」老订单批量取消。
-- 本脚本可重复执行（列和索引存在时自动跳过）。
-- ============================================================

SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'orders'
      AND COLUMN_NAME = 'close_deadline'
);

SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `orders` ADD COLUMN `close_deadline` DATETIME NULL COMMENT ''自动关单截止时间，NULL 表示历史订单不参与'' AFTER `time`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 定时扫描走索引，避免每 30 秒全表扫一次订单表
SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'orders'
      AND INDEX_NAME = 'idx_close_deadline'
);
SET @ddl2 := IF(@idx_exists = 0,
    'CREATE INDEX idx_close_deadline ON `orders` (close_deadline, status)',
    'SELECT 1');
PREPARE stmt2 FROM @ddl2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- 确认：应该看到 close_deadline 列，且所有历史订单该字段都是 NULL
SELECT COUNT(*) AS total_orders,
       SUM(close_deadline IS NOT NULL) AS orders_with_deadline
FROM `orders`;
