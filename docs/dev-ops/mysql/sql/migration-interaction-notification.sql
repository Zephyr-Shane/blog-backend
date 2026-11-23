-- ============================================================
-- 互动系统 + 通知中心迁移（2026-11-20）
-- 内容：
--   1. blog_comment 增加 visitor_id（匿名访客标识）+ 索引，存量回填
--   2. 四张内容表 like_count / comment_count 用关联表真实 COUNT 回填
--   3. 新建 blog_notification 通知表
-- 执行前请先备份（本次备份：/tmp/wb-blog-backup/before-interaction-notification.sql）
-- ============================================================

-- ---------- 1. 评论表增加 visitor_id ----------
ALTER TABLE `blog_comment`
  ADD COLUMN `visitor_id` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '匿名访客标识（前端 localStorage）' AFTER `parent_id`,
  ADD KEY `idx_comment_visitor` (`visitor_id`);

-- 存量评论回填一个占位访客（历史数据无访客标识），避免空值；作者回复(original id=2)单独标注
UPDATE `blog_comment` SET `visitor_id` = CONCAT('legacy-', `id`) WHERE `visitor_id` = '';

-- ---------- 2. 计数回填：以关联表真实值覆盖种子写死的假数据 ----------
-- comment_count 只统计前台可见的 approved 评论；like_count 统计全部点赞
UPDATE `blog_post` p SET
  p.`comment_count` = (SELECT COUNT(*) FROM `blog_comment` c WHERE c.`target_type`='post'     AND c.`target_id`=p.`id` AND c.`status`='approved'),
  p.`like_count`    = (SELECT COUNT(*) FROM `blog_like`    l WHERE l.`target_type`='post'     AND l.`target_id`=p.`id`);
UPDATE `blog_moment` m SET
  m.`comment_count` = (SELECT COUNT(*) FROM `blog_comment` c WHERE c.`target_type`='moment'   AND c.`target_id`=m.`id` AND c.`status`='approved'),
  m.`like_count`    = (SELECT COUNT(*) FROM `blog_like`    l WHERE l.`target_type`='moment'   AND l.`target_id`=m.`id`);
UPDATE `blog_interview` i SET
  i.`comment_count` = (SELECT COUNT(*) FROM `blog_comment` c WHERE c.`target_type`='interview' AND c.`target_id`=i.`id` AND c.`status`='approved'),
  i.`like_count`    = (SELECT COUNT(*) FROM `blog_like`    l WHERE l.`target_type`='interview' AND l.`target_id`=i.`id`);
UPDATE `blog_link` l SET
  l.`comment_count` = (SELECT COUNT(*) FROM `blog_comment` c WHERE c.`target_type`='link'     AND c.`target_id`=l.`id` AND c.`status`='approved'),
  l.`like_count`    = (SELECT COUNT(*) FROM `blog_like`    k WHERE k.`target_type`='link'     AND k.`target_id`=l.`id`);

-- ---------- 3. 通知表 ----------
CREATE TABLE IF NOT EXISTS `blog_notification` (
  `id`          VARCHAR(36) NOT NULL COMMENT 'UUID，应用生成',
  `type`        VARCHAR(32) NOT NULL COMMENT '通知类型：comment / like / system',
  `title`       VARCHAR(255) NOT NULL,
  `content`     VARCHAR(512) DEFAULT NULL,
  `target_type` VARCHAR(16) DEFAULT NULL COMMENT '跳转目标类型：post/moment/interview/link',
  `target_id`   BIGINT DEFAULT NULL COMMENT '跳转目标 ID',
  `is_read`     TINYINT(1) NOT NULL DEFAULT 0,
  `created_at`  VARCHAR(32) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notif_read_time` (`is_read`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
