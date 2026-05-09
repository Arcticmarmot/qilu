CREATE TABLE `post_media` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子媒体关联ID',
                              `post_id` BIGINT NOT NULL COMMENT '帖子ID',
                              `media_id` BIGINT NOT NULL COMMENT '媒体文件ID',
                              `sort_order` INT NOT NULL DEFAULT 0 COMMENT '展示顺序',
                              `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_post_media` (`post_id`, `media_id`),
                              UNIQUE KEY `uk_post_sort_order` (`post_id`, `sort_order`),
                              KEY `idx_post_sort_order` (`post_id`, `sort_order`),
                              KEY `idx_media_id` (`media_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子媒体关联表';