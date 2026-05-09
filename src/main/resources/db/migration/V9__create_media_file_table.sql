CREATE TABLE `media_file` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '媒体文件ID',
                              `user_uuid` CHAR(36) NOT NULL COMMENT '上传用户ID',
                              `object_key` VARCHAR(255) NOT NULL COMMENT '对象存储Key',
                              `url` VARCHAR(512) NOT NULL COMMENT '公开访问URL',
                              `content_type` VARCHAR(64) NOT NULL COMMENT '文件类型：image/jpeg、image/png、image/webp',
                              `size` BIGINT NOT NULL COMMENT '文件大小，单位字节',
                              `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0已删除 1已使用 2未使用',
                              `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_object_key` (`object_key`),
                              KEY `idx_user_status_created_at` (`user_uuid`, `status`, `created_at`),
                              KEY `idx_status_created_at` (`status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='媒体文件表';