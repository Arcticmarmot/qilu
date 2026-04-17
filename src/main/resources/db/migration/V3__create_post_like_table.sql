CREATE TABLE `post_like` (
                             `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
                             `post_id` BIGINT NOT NULL COMMENT '帖子ID',
                             `user_uuid` CHAR(36) NOT NULL COMMENT '用户ID',
                             `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0取消点赞 1已点赞',
                             `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uk_post_user` (`post_id`, `user_uuid`),
                             KEY `idx_user_status_created_at` (`user_uuid`, `status`, `created_at`),
                             KEY `idx_post_status_created_at` (`post_id`, `status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞表';