-- 迁移：blog_upload.id 由自增 BIGINT 改为应用生成的 UUID 字符串
-- 同时把已有内容中形如 /media/{旧自增ID} 的引用替换为 /media/{新UUID}
-- 执行一次即可；执行前请先备份 blog_upload 与内容表。

-- 1) 旧 ID -> 新 UUID 映射表
DROP TABLE IF EXISTS `_mig_upload_id`;
CREATE TABLE `_mig_upload_id` (
  `old_id` VARCHAR(36) NOT NULL,
  `new_id` VARCHAR(36) NOT NULL,
  PRIMARY KEY (`old_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `_mig_upload_id` (`old_id`, `new_id`)
SELECT CAST(`id` AS CHAR), UUID() FROM `blog_upload` WHERE `id` REGEXP '^[0-9]+$';

-- 2) 替换内容表里的资源引用（用 REGEXP_LIKE 保证数字边界，避免 7 误匹配 70）
UPDATE `blog_post` p JOIN `_mig_upload_id` m
  ON REGEXP_LIKE(IFNULL(p.`cover_image`, ''), CONCAT('/media/', m.`old_id`, '([^0-9]|$)'))
SET p.`cover_image` = REPLACE(p.`cover_image`, CONCAT('/media/', m.`old_id`), CONCAT('/media/', m.`new_id`));

UPDATE `blog_post` p JOIN `_mig_upload_id` m
  ON REGEXP_LIKE(IFNULL(p.`content`, ''), CONCAT('/media/', m.`old_id`, '([^0-9]|$)'))
SET p.`content` = REPLACE(p.`content`, CONCAT('/media/', m.`old_id`), CONCAT('/media/', m.`new_id`));

UPDATE `blog_moment` b JOIN `_mig_upload_id` m
  ON REGEXP_LIKE(IFNULL(b.`images`, ''), CONCAT('/media/', m.`old_id`, '([^0-9]|$)'))
SET b.`images` = REPLACE(b.`images`, CONCAT('/media/', m.`old_id`), CONCAT('/media/', m.`new_id`));

UPDATE `blog_moment` b JOIN `_mig_upload_id` m
  ON REGEXP_LIKE(IFNULL(b.`content`, ''), CONCAT('/media/', m.`old_id`, '([^0-9]|$)'))
SET b.`content` = REPLACE(b.`content`, CONCAT('/media/', m.`old_id`), CONCAT('/media/', m.`new_id`));

UPDATE `blog_interview` i JOIN `_mig_upload_id` m
  ON REGEXP_LIKE(IFNULL(i.`content`, ''), CONCAT('/media/', m.`old_id`, '([^0-9]|$)'))
SET i.`content` = REPLACE(i.`content`, CONCAT('/media/', m.`old_id`), CONCAT('/media/', m.`new_id`));

UPDATE `blog_link` l JOIN `_mig_upload_id` m
  ON REGEXP_LIKE(IFNULL(l.`logo`, ''), CONCAT('/media/', m.`old_id`, '([^0-9]|$)'))
SET l.`logo` = REPLACE(l.`logo`, CONCAT('/media/', m.`old_id`), CONCAT('/media/', m.`new_id`));

UPDATE `blog_link` l JOIN `_mig_upload_id` m
  ON REGEXP_LIKE(IFNULL(l.`description`, ''), CONCAT('/media/', m.`old_id`, '([^0-9]|$)'))
SET l.`description` = REPLACE(l.`description`, CONCAT('/media/', m.`old_id`), CONCAT('/media/', m.`new_id`));

UPDATE `blog_site_config` s JOIN `_mig_upload_id` m
  ON REGEXP_LIKE(IFNULL(s.`avatar`, ''), CONCAT('/media/', m.`old_id`, '([^0-9]|$)'))
SET s.`avatar` = REPLACE(s.`avatar`, CONCAT('/media/', m.`old_id`), CONCAT('/media/', m.`new_id`));

-- 3) 改主键类型并回填 UUID（顺序不能颠倒：先有映射再改列）
ALTER TABLE `blog_upload` MODIFY COLUMN `id` VARCHAR(36) NOT NULL;
UPDATE `blog_upload` u JOIN `_mig_upload_id` m ON u.`id` = m.`old_id` COLLATE utf8mb4_unicode_ci SET u.`id` = m.`new_id`;

DROP TABLE `_mig_upload_id`;

-- 4) 校验：应全部为 36 位 UUID
SELECT `id`, `category`, `original_name` FROM `blog_upload` ORDER BY `created_at` DESC;
