ALTER TABLE `like_notification`
    ADD COLUMN `entity_snippet` VARCHAR(255) NOT NULL COMMENT '实体预览' AFTER `entity_id`;
ALTER TABLE `comment_notification`
    ADD COLUMN `post_snippet` VARCHAR(255) NOT NULL COMMENT '帖子预览' AFTER `post_id`;
ALTER TABLE `reply_notification`
    ADD COLUMN `entity_snippet` VARCHAR(255) NOT NULL COMMENT '帖子预览' AFTER `entity_id`;