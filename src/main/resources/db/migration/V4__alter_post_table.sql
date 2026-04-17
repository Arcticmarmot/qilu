ALTER TABLE `post`
    ADD COLUMN `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数' AFTER `visibility`;