ALTER TABLE `post`
    ADD COLUMN `content_snippet` VARCHAR(255) NOT NULL COMMENT '帖子内容预览' AFTER `content`;