CREATE TABLE `user` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                        `uuid` CHAR(36) NOT NULL COMMENT '用户身份标识',
                        `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
                        `nickname` VARCHAR(32) NOT NULL COMMENT '昵称',
                        `email` VARCHAR(64) DEFAULT NULL COMMENT '邮箱',
                        `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_uuid` (`uuid`),
                        UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';