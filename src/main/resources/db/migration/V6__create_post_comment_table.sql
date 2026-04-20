CREATE TABLE `post_comment` (
                                `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
                                `post_id` BIGINT NOT NULL COMMENT '被评论的帖子ID',
                                `post_author_uuid` CHAR(36) NOT NULL COMMENT '帖子作者用户ID',
                                `user_uuid` CHAR(36) NOT NULL COMMENT '评论者用户ID',
                                `content` VARCHAR(1024) NOT NULL COMMENT '评论内容',
                                `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0删除 1正常',
                                `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (`id`),
                                KEY `idx_post_status_created_at` (`post_id`, `status`, `created_at`),
                                KEY `idx_user_status_created_at` (`user_uuid`, `status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子一级评论表';