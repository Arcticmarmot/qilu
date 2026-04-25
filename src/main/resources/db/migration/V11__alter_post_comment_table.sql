ALTER TABLE `post_comment`
    ADD COLUMN `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数' AFTER `content`;
ALTER TABLE `post_comment`
    ADD COLUMN `reply_count` INT NOT NULL DEFAULT 0 COMMENT '回复数' AFTER `like_count`;
ALTER TABLE `comment_reply`
    ADD COLUMN `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数' AFTER `content`;