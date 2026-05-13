CREATE TABLE `voucher` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '兑换券ID',
                           `title` VARCHAR(128) NOT NULL COMMENT '兑换券标题',
                           `description` VARCHAR(512) DEFAULT NULL COMMENT '兑换券描述',
                           `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
                           `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`),
                           KEY `idx_status_created_at` (`status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='兑换券表';

CREATE TABLE `voucher_seckill` (
                                   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '秒杀活动ID',
                                   `voucher_id` BIGINT NOT NULL COMMENT '兑换券ID',
                                   `total_stock` INT NOT NULL COMMENT '活动总库存',
                                   `remaining_stock` INT NOT NULL COMMENT '数据库剩余库存',
                                   `start_time` DATETIME NOT NULL COMMENT '秒杀开始时间',
                                   `end_time` DATETIME NOT NULL COMMENT '秒杀结束时间',
                                   `redeem_deadline` DATETIME NOT NULL COMMENT '兑换截止时间',
                                   `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
                                   `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_voucher_id` (`voucher_id`),
                                   KEY `idx_status_start_end` (`status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='兑换券秒杀活动表';

CREATE TABLE `voucher_order` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
                                 `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
                                 `voucher_id` BIGINT NOT NULL COMMENT '兑换券ID',
                                 `seckill_id` BIGINT NOT NULL COMMENT '秒杀活动ID',
                                 `user_uuid` CHAR(36) NOT NULL COMMENT '用户UUID',
                                 `redeem_code` VARCHAR(64) NOT NULL COMMENT '兑换码',
                                 `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1待使用 2已使用 3已过期 4已取消',
                                 `expire_at` DATETIME NOT NULL COMMENT '过期时间',
                                 `used_at` DATETIME DEFAULT NULL COMMENT '核销时间',
                                 `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_order_no` (`order_no`),
                                 UNIQUE KEY `uk_redeem_code` (`redeem_code`),
                                 UNIQUE KEY `uk_user_seckill` (`user_uuid`, `seckill_id`),
                                 KEY `idx_user_created_at` (`user_uuid`, `created_at`),
                                 KEY `idx_seckill_created_at` (`seckill_id`, `created_at`),
                                 KEY `idx_status_expire_at` (`status`, `expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='兑换券订单表';