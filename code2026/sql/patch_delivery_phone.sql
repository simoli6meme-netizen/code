-- 外卖员（delivery）表补丁：新增 phone（手机号）字段
-- 前端外卖员管理模块（账号 / 姓名 / 手机号 / 头像 / 角色）需要该字段
-- 若执行报错 "Duplicate column name 'phone'" 说明字段已存在，可忽略。

ALTER TABLE `delivery`
    ADD COLUMN `phone` VARCHAR(20) NULL DEFAULT NULL COMMENT '手机号' AFTER `role`;
