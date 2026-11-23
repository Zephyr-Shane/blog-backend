-- ============================================================
-- 阿申の小破站 · 线上数据库初始化脚本
-- ============================================================
-- 用途：全新数据库初始化（只建表结构 + 保留管理员账号）
-- 目标库：zephyr_blog   字符集：utf8mb4 / utf8mb4_unicode_ci
-- MySQL 8.x
--
-- 说明：
--   1. 已剔除全部种子/演示数据（文章、动态、标签、评论、站点配置等）。
--   2. 三个历史 migration 的结构改动已并入下方 CREATE TABLE：
--      - visitor_id / idx_comment_visitor（互动迁移）
--      - blog_notification 表（通知中心迁移）
--      - announcement 列（首页侧栏迁移）
--      - blog_upload.id = VARCHAR(36) UUID（上传迁移）
--      因此无需再单独执行 migration-*.sql。
--   3. 仅保留 1 条管理员账号，明文密码 admin123（SHA-256 无盐存储）。
--      ⚠ 上线后请第一时间登录后台修改密码，生产环境勿沿用默认弱口令。
--
-- 执行：mysql -uroot -p < blog-init-prod.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS `zephyr_blog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `zephyr_blog`;

-- ---------- 清理旧表（仅在重复初始化时需要，表不存在时 IF EXISTS 保证安全） ----------
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `blog_content_tag`;
DROP TABLE IF EXISTS `blog_like`;
DROP TABLE IF EXISTS `blog_comment`;
DROP TABLE IF EXISTS `blog_notification`;
DROP TABLE IF EXISTS `blog_upload`;
DROP TABLE IF EXISTS `blog_tag`;
DROP TABLE IF EXISTS `blog_post`;
DROP TABLE IF EXISTS `blog_moment`;
DROP TABLE IF EXISTS `blog_interview`;
DROP TABLE IF EXISTS `blog_link`;
DROP TABLE IF EXISTS `blog_site_config`;
DROP TABLE IF EXISTS `blog_user`;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------- 表结构 ----------

CREATE TABLE `blog_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL DEFAULT '',
  `slug` VARCHAR(255) NOT NULL DEFAULT '',
  `excerpt` TEXT,
  `content` LONGTEXT,
  `cover_image` VARCHAR(512) DEFAULT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'draft',
  `category` VARCHAR(32) NOT NULL DEFAULT 'tech',
  `published_at` VARCHAR(32) DEFAULT NULL,
  `allow_comments` TINYINT(1) NOT NULL DEFAULT 1,
  `comment_count` INT NOT NULL DEFAULT 0,
  `like_count` INT NOT NULL DEFAULT 0,
  `co_authors` TEXT,
  `created_at` VARCHAR(32) NOT NULL,
  `updated_at` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_slug` (`slug`),
  KEY `idx_post_status_pub` (`status`, `published_at`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `blog_moment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `content` TEXT,
  `images` TEXT,
  `mood` VARCHAR(16) NOT NULL DEFAULT 'calm',
  `location` VARCHAR(255) DEFAULT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'draft',
  `published_at` VARCHAR(32) DEFAULT NULL,
  `allow_comments` TINYINT(1) NOT NULL DEFAULT 1,
  `comment_count` INT NOT NULL DEFAULT 0,
  `like_count` INT NOT NULL DEFAULT 0,
  `created_at` VARCHAR(32) NOT NULL,
  `updated_at` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_moment_status_pub` (`status`, `published_at`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `blog_interview` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `kind` VARCHAR(16) NOT NULL DEFAULT 'experience',
  `title` VARCHAR(255) NOT NULL DEFAULT '',
  `content` LONGTEXT,
  `company` VARCHAR(128) DEFAULT NULL,
  `position` VARCHAR(128) DEFAULT NULL,
  `result` VARCHAR(16) DEFAULT NULL,
  `knowledge_point` VARCHAR(128) DEFAULT NULL,
  `difficulty` VARCHAR(16) DEFAULT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'draft',
  `published_at` VARCHAR(32) DEFAULT NULL,
  `allow_comments` TINYINT(1) NOT NULL DEFAULT 1,
  `comment_count` INT NOT NULL DEFAULT 0,
  `like_count` INT NOT NULL DEFAULT 0,
  `created_at` VARCHAR(32) NOT NULL,
  `updated_at` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_interview_status_pub` (`status`, `published_at`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `blog_link` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL DEFAULT '',
  `url` VARCHAR(512) NOT NULL DEFAULT '',
  `description` TEXT,
  `logo` VARCHAR(512) DEFAULT NULL,
  `category` VARCHAR(16) NOT NULL DEFAULT 'friend',
  `group_name` VARCHAR(64) DEFAULT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'draft',
  `published_at` VARCHAR(32) DEFAULT NULL,
  `allow_comments` TINYINT(1) NOT NULL DEFAULT 1,
  `comment_count` INT NOT NULL DEFAULT 0,
  `like_count` INT NOT NULL DEFAULT 0,
  `created_at` VARCHAR(32) NOT NULL,
  `updated_at` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_link_status_cat` (`status`, `category`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `blog_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL DEFAULT '',
  `slug` VARCHAR(64) NOT NULL DEFAULT '',
  `category` VARCHAR(16) NOT NULL DEFAULT 'general',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_slug` (`slug`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `blog_content_tag` (
  `target_type` VARCHAR(16) NOT NULL,
  `target_id` BIGINT NOT NULL,
  `tag_id` BIGINT NOT NULL,
  PRIMARY KEY (`target_type`, `target_id`, `tag_id`),
  KEY `idx_ct_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `blog_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `target_type` VARCHAR(16) NOT NULL,
  `target_id` BIGINT NOT NULL,
  `parent_id` BIGINT DEFAULT NULL,
  `visitor_id` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '匿名访客标识（前端 localStorage）',
  `author_name` VARCHAR(64) NOT NULL DEFAULT '',
  `author_email` VARCHAR(128) NOT NULL DEFAULT '',
  `content` TEXT,
  `status` VARCHAR(16) NOT NULL DEFAULT 'approved',
  `created_at` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_comment_target` (`target_type`, `target_id`, `status`),
  KEY `idx_comment_visitor` (`visitor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `blog_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `visitor_id` VARCHAR(64) NOT NULL,
  `target_type` VARCHAR(16) NOT NULL,
  `target_id` BIGINT NOT NULL,
  `created_at` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_like` (`visitor_id`, `target_type`, `target_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `blog_notification` (
  `id` VARCHAR(36) NOT NULL COMMENT 'UUID，应用生成',
  `type` VARCHAR(32) NOT NULL COMMENT '通知类型：comment / like / system',
  `title` VARCHAR(255) NOT NULL,
  `content` VARCHAR(512) DEFAULT NULL,
  `target_type` VARCHAR(16) DEFAULT NULL COMMENT '跳转目标类型：post/moment/interview/link',
  `target_id` BIGINT DEFAULT NULL COMMENT '跳转目标 ID',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notif_read_time` (`is_read`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `blog_site_config` (
  `id` TINYINT NOT NULL DEFAULT 1,
  `site_name` VARCHAR(128) NOT NULL DEFAULT '',
  `subtitle` VARCHAR(255) DEFAULT '',
  `description` TEXT,
  `author_name` VARCHAR(128) DEFAULT '',
  `author_bio` TEXT,
  `avatar` VARCHAR(512) DEFAULT NULL,
  `logo` VARCHAR(512) DEFAULT NULL COMMENT '站点 logo 图片地址（导航栏/浏览器图标）',
  `social_links` TEXT,
  `article_signature` VARCHAR(128) DEFAULT '',
  `interview_enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `card_slots` TEXT COMMENT '卡片插槽配置 JSON（按内容类型：post/moment/interview/link）',
  `announcement` TEXT COMMENT '站点公告配置 JSON：title/content/updatedAt/visible',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `blog_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(128) NOT NULL DEFAULT '',
  `name` VARCHAR(64) NOT NULL DEFAULT '',
  `role` VARCHAR(16) NOT NULL DEFAULT 'admin',
  `password_hash` VARCHAR(128) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `blog_upload` (
  `id` VARCHAR(36) NOT NULL COMMENT '资源公开唯一 ID（UUID），由应用生成',
  `category` VARCHAR(20) NOT NULL DEFAULT 'other' COMMENT '上传形式：avatar/cover/content/logo/gallery/other',
  `original_name` VARCHAR(255) NOT NULL,
  `stored_name` VARCHAR(255) NOT NULL,
  `mime_type` VARCHAR(64) NOT NULL,
  `size` INT NOT NULL,
  `data` LONGBLOB NOT NULL COMMENT '文件二进制',
  `created_at` VARCHAR(40) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_upload_category` (`category`),
  KEY `idx_upload_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上传资源（二进制落库）';

-- ---------- 仅保留管理员账号（明文密码：admin123） ----------
INSERT INTO `blog_user` (`id`,`email`,`name`,`role`,`password_hash`) VALUES
 (1,'admin@zephyr.dev','Zephyr-Shane','admin','240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9');

-- ---------- 站点配置：必须留一条，否则 getSiteConfig() 返回 null，前端读 interviewEnabled 直接崩 ----------
INSERT INTO `blog_site_config` (`id`,`site_name`,`subtitle`,`description`,`author_name`,`author_bio`,`avatar`,`logo`,`social_links`,`article_signature`,`interview_enabled`,`card_slots`,`announcement`) VALUES
 (1,'阿申の小破站','静水流深，字落云烟','在代码逻辑与烟火日常之间寻找平衡，一名全栈工程师与独立作者。','Zephyr-Shane','白天沉浸在终端与代码之间，夜晚写写文字、手冲咖啡、漫步西湖。','/images/avatar.svg',NULL,'[{"label":"GitHub","url":"https://github.com"},{"label":"Twitter / X","url":"https://x.com"},{"label":"即刻","url":"https://okjk.co"},{"label":"RSS","url":"/rss.xml"}]','Zephyr-Shane',1,NULL,NULL);
