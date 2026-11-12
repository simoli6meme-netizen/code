-- ============================================================
-- 校园小卖部购物与配送系统 —— 全新安装建表脚本
--
-- 用法（MySQL 8.0+，在仓库根目录执行）：
--   mysql -uroot -p < code2026/sql/schema.sql
--   mysql -uroot -p < code2026/sql/data.sql
--
-- 说明：
--   1. 已包含秒杀活动表、orders.close_deadline 列及全部索引，
--      全新安装不需要再执行任何 patch_*.sql / seckill.sql / order_timeout.sql。
--   2. 全部使用 CREATE TABLE IF NOT EXISTS，可重复执行、不会清空已有数据；
--      脚本不含 DROP TABLE。已有旧库请走 README.md 里的「升级路径」。
--   3. 业务表之间没有外键，建表顺序无依赖；字符集 utf8mb4。
-- ============================================================

CREATE DATABASE IF NOT EXISTS code2026
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE code2026;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `admin` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '名称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '角色',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `username` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='管理员信息';
CREATE TABLE IF NOT EXISTS `carousel` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `goods_id` int DEFAULT NULL COMMENT '商品',
  `img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图片',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图信息';
CREATE TABLE IF NOT EXISTS `cart` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `num` int DEFAULT NULL COMMENT '数量',
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车信息';
CREATE TABLE IF NOT EXISTS `category` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类';
CREATE TABLE IF NOT EXISTS `collect` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏';
CREATE TABLE IF NOT EXISTS `comment` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `score` double(10,1) DEFAULT NULL COMMENT '评分',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '评价内容',
  `user_id` int DEFAULT NULL COMMENT '评价人',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '时间',
  `order_id` int DEFAULT NULL COMMENT '订单ID',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单评价';
CREATE TABLE IF NOT EXISTS `delivery` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(255) DEFAULT NULL COMMENT '账号',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `role` varchar(255) DEFAULT NULL COMMENT '角色',
  `phone` varchar(255) DEFAULT NULL COMMENT '手机号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS `goods` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '名称',
  `img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图片',
  `price` decimal(10,2) DEFAULT NULL COMMENT '价格',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '简介',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '详情',
  `store` int DEFAULT '0' COMMENT '库存',
  `category_id` int DEFAULT NULL COMMENT '分类ID',
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '上架状态',
  `views` int DEFAULT '0',
  `sale_count` int DEFAULT NULL COMMENT '销量',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建时间',
  `recommend` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '推荐',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品信息';
CREATE TABLE IF NOT EXISTS `order_detail` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `goods_id` int DEFAULT NULL COMMENT '商品ID',
  `num` int DEFAULT NULL COMMENT '商品数量',
  `order_id` int DEFAULT NULL COMMENT '订单ID',
  `goods_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品图片',
  `goods_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品名称',
  `goods_price` decimal(10,2) DEFAULT NULL COMMENT '商品单价',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单详情';
CREATE TABLE IF NOT EXISTS `orders` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '订单编号',
  `total` decimal(10,2) DEFAULT NULL COMMENT '总价格',
  `user_id` int DEFAULT NULL COMMENT '下单人ID',
  `delivery_id` int DEFAULT NULL COMMENT '外卖员ID',
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '状态',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '下单时间',
  `close_deadline` datetime DEFAULT NULL COMMENT '自动关单截止时间，NULL 表示历史订单不参与',
  `deliver_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '配送类型',
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收货地址',
  `address_x` double DEFAULT NULL COMMENT '收货地址地图X坐标',
  `address_y` double DEFAULT NULL COMMENT '收货地址地图Y坐标',
  `deliver` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '配送信息',
  PRIMARY KEY (`id`),
  KEY `idx_orders_delivery` (`delivery_id`),
  KEY `idx_close_deadline` (`close_deadline`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=67 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单信息';
CREATE TABLE IF NOT EXISTS `recharge` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `money` decimal(10,2) DEFAULT NULL COMMENT '充值金额',
  `user_id` int DEFAULT NULL COMMENT '充值人',
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付方式',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '充值时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值记录';
CREATE TABLE IF NOT EXISTS `seckill_activity` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `goods_id` int NOT NULL COMMENT '商品ID',
  `seckill_price` decimal(10,2) NOT NULL COMMENT '秒杀价',
  `stock` int NOT NULL DEFAULT '0' COMMENT '活动库存（创建活动时从商品库存划拨）',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` varchar(10) NOT NULL DEFAULT '启用' COMMENT '启用 / 停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_goods` (`goods_id`),
  KEY `idx_time` (`start_time`,`end_time`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀活动';
CREATE TABLE IF NOT EXISTS `seckill_record` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `activity_id` int NOT NULL COMMENT '活动ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `order_id` int DEFAULT NULL COMMENT '生成的订单ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`,`user_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀成功记录';
CREATE TABLE IF NOT EXISTS `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `name` varchar(50) NOT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `role` varchar(20) NOT NULL,
  `account` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
