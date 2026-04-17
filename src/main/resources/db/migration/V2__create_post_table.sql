CREATE TABLE `post` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
                        `user_uuid` CHAR(36) NOT NULL COMMENT '作者用户ID',
                        `title` VARCHAR(128) DEFAULT NULL COMMENT '标题',
                        `content` TEXT NOT NULL COMMENT '正文内容',
                        `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0删除 1正常',
                        `visibility` TINYINT NOT NULL DEFAULT 1 COMMENT '可见范围：1公开 2仅自己',
                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
                        PRIMARY KEY (`id`),
                        KEY `idx_user_id` (`user_uuid`),
                        KEY `idx_status_created_at` (`status`, `created_at`),
                        KEY `idx_status_visibility_created_at` (`status`, `visibility`, `created_at`),
                        KEY `idx_user_status_created_at` (`user_uuid`, `status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子主表';