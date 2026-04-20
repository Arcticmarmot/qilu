ALTER TABLE `post`
    ADD COLUMN `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数' AFTER `like_count`;