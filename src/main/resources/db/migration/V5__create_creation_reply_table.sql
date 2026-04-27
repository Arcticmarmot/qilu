CREATE TABLE `creation_reply` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '回复ID',
                                 `post_id` BIGINT NOT NULL COMMENT '所属帖子ID',
                                 `root_comment_id` BIGINT NOT NULL COMMENT '所属评论ID',
                                 `parent_reply_id` BIGINT DEFAULT NULL COMMENT '被回复的回复ID',
                                 `user_uuid` CHAR(36) NOT NULL COMMENT '回复作者用户ID',
                                 `target_user_uuid` CHAR(36) NOT NULL COMMENT '被回复的用户ID',
                                 `content` VARCHAR(1024) NOT NULL COMMENT '回复内容',
                                 `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
                                 `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0删除 1正常',
                                 `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_comment_status_created_at` (`root_comment_id`, `status`, `created_at`),
                                 KEY `idx_post_comment_status_created_at` (`post_id`, `root_comment_id`, `status`, `created_at`),
                                 KEY `idx_user_status_created_at` (`user_uuid`, `status`, `created_at`),
                                 KEY `idx_target_user_status_created_at` (`target_user_uuid`, `status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论回复表';