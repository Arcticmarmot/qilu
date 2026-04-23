CREATE TABLE `comment_notification` (
                                     `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
                                     `comment_id` BIGINT NOT NULL COMMENT '评论ID',
                                     `receiver_uuid` CHAR(36) NOT NULL COMMENT '接收通知的用户ID',
                                     `actor_uuid` CHAR(36) NOT NULL COMMENT '触发动作的用户ID',
                                     `post_id` BIGINT NOT NULL COMMENT '关联帖子ID',
                                     `content_preview` VARCHAR(255) NOT NULL COMMENT '评论内容预览',
                                     `biz_key` VARCHAR(128) NOT NULL COMMENT '通知幂等键',
                                     `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
                                     `read_at` DATETIME DEFAULT NULL COMMENT '已读时间',
                                     `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_biz_key` (`biz_key`),
                                     KEY `idx_receiver_created_at` (`receiver_uuid`, `created_at`),
                                     KEY `idx_receiver_is_read_created_at` (`receiver_uuid`, `is_read`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论通知表';