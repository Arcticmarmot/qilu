CREATE TABLE `notification` (
                                `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
                                `receiver_uuid` CHAR(36) NOT NULL COMMENT '接收通知的用户ID',
                                `actor_uuid` CHAR(36) NOT NULL COMMENT '触发动作的用户ID',
                                `type` VARCHAR(32) NOT NULL COMMENT '通知类型：POST_LIKED、POST_COMMENTED等',
                                `entity_type` VARCHAR(32) NOT NULL COMMENT '关联实体类型：POST、COMMENT',
                                `entity_id` BIGINT NOT NULL COMMENT '关联实体ID',
                                `biz_key` VARCHAR(128) NOT NULL COMMENT '通知幂等键',
                                `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
                                `read_at` DATETIME DEFAULT NULL COMMENT '已读时间',
                                `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_biz_key` (`biz_key`),
                                KEY `idx_receiver_created_at` (`receiver_uuid`, `created_at`),
                                KEY `idx_receiver_is_read_created_at` (`receiver_uuid`, `is_read`, `created_at`),
                                KEY `idx_receiver_type_created_at` (`receiver_uuid`, `type`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';