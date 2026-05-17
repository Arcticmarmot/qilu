CREATE TABLE `operator` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
                              `uuid` CHAR(36) NOT NULL COMMENT '管理员身份标识',
                              `username` VARCHAR(32) NOT NULL COMMENT '管理员用户名',
                              `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
                              `role` VARCHAR(32) NOT NULL DEFAULT 'ADMIN' COMMENT '角色：ROOT ADMIN',
                              `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
                              `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_uuid` (`uuid`),
                              UNIQUE KEY `uk_username` (`username`),
                              KEY `idx_status_created_at` (`status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员用户表';

INSERT INTO `operator` (
    `uuid`,
    `username`,
    `password_hash`,
    `role`,
    `status`
) VALUES (
     UUID(),
     'admin',
     '$2a$10$P.EcIltyuPSM2o5AHJiZpeZtsK100.SL6ehwsWcbJsZhR3brglD12',
     'ROOT',
     1
);