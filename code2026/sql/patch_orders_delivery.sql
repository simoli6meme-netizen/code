-- 订单（orders）补丁：支持外卖员接单与「我的地址」地图坐标
-- 1) delivery_id：接单后绑定的外卖员
-- 2) address_x / address_y：用户在「我的地址」选择的校园地图坐标（用于规划配送路线）
-- 若执行报错 "Duplicate column name" 说明字段已存在，可忽略。

ALTER TABLE `orders`
    ADD COLUMN `delivery_id` INT NULL DEFAULT NULL COMMENT '外卖员ID' AFTER `user_id`,
    ADD COLUMN `address_x`   DOUBLE NULL DEFAULT NULL COMMENT '收货地址地图X坐标' AFTER `address`,
    ADD COLUMN `address_y`   DOUBLE NULL DEFAULT NULL COMMENT '收货地址地图Y坐标' AFTER `address_x`;

-- 便于按外卖员查询
ALTER TABLE `orders`
    ADD INDEX `idx_orders_delivery` (`delivery_id`);
