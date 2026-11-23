-- 阿申の小破站 博客数据库（API-CONTRACT v1.0）
-- MySQL 8.x  charset utf8mb4

CREATE DATABASE IF NOT EXISTS `zephyr_blog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `zephyr_blog`;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `blog_content_tag`;
DROP TABLE IF EXISTS `blog_like`;
DROP TABLE IF EXISTS `blog_comment`;
DROP TABLE IF EXISTS `blog_notification`;
DROP TABLE IF EXISTS `blog_tag`;
DROP TABLE IF EXISTS `blog_post`;
DROP TABLE IF EXISTS `blog_moment`;
DROP TABLE IF EXISTS `blog_interview`;
DROP TABLE IF EXISTS `blog_link`;
DROP TABLE IF EXISTS `blog_site_config`;
DROP TABLE IF EXISTS `blog_user`;
SET FOREIGN_KEY_CHECKS = 1;

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

-- ============ 种子数据 ============
INSERT INTO `blog_tag` (`id`,`name`,`slug`,`category`) VALUES
 (1,'React','react','tech'),(2,'状态机','fsm','tech'),(3,'前端架构','frontend-architecture','tech'),
 (4,'Go','go','tech'),(5,'Redis','redis','tech'),(6,'浏览器内核','browser-kernel','tech'),
 (7,'性能优化','performance','tech'),(8,'独立开发','indie-dev','general'),
 (9,'咖啡','coffee','life'),(10,'手冲','pour-over','life'),(11,'西湖漫步','west-lake','life'),(12,'读书','reading','life');

INSERT INTO `blog_post` (`id`,`title`,`slug`,`excerpt`,`content`,`cover_image`,`status`,`category`,`published_at`,`allow_comments`,`comment_count`,`like_count`,`co_authors`,`created_at`,`updated_at`) VALUES
 (1,'深入现代前端架构：用状态机重构复杂的异步交互流','fsm-architecture','在处理多模态协同与断点续传等复杂界面时，传统的状态拼凑往往导致不可控的边界分支。本文通过 XState 与真实生产案例，梳理有限状态机的声明式抽象。','<p>在现代复杂 Web 应用的演化过程中，用户交互界面的逻辑密度呈指数级上升。表单联动、异步竞态、断网重试与并发取消交织在一起，常常使原本条理明晰的组件逐步蜕变为不可捉摸的“状态怪物”。</p><h2>一、状态膨胀与布尔值泥潭</h2><p>回忆一下典型的异步数据加载逻辑：我们习惯性地声明 <code>isLoading</code>、<code>isError</code>、<code>data</code> 与 <code>isSuccess</code>。从数学排列角度看，四个独立的布尔变量拥有 2^4 = 16 种排列组合。</p><blockquote>好代码如同手作匠人雕琢的实木家具——榫卯严丝合缝，不喧宾夺主。</blockquote><p>有限状态机用显式转移取代隐式布尔组合，让每一种可能的状态都清晰可枚举。</p>','/images/post-fsm.svg','published','tech','2024-05-18T10:30:00+08:00',1,0,86,'[{"name":"阿棠","blogUrl":"https://example.com/atang"}]','2024-05-16T09:00:00+08:00','2024-05-18T10:30:00+08:00'),
 (2,'从零搭建设计系统：Token、组件与一致性','design-system-from-scratch','设计系统不是组件库的别名，而是一套关于一致性的契约。聊聊我如何从 DESIGN.md 出发搭建可维护的 token 体系。','<p>设计系统的价值不在组件数量，而在于让每一个决策都有据可循。本文从颜色、字体、圆角三个维度拆解 token 的设计方法。</p>','/images/post-design-system.svg','published','tech','2024-04-22T20:10:00+08:00',1,0,42,'[]','2024-04-20T12:00:00+08:00','2024-04-22T20:10:00+08:00'),
 (3,'Redis 缓存设计的几个坑，我踩过了','redis-cache-pitfalls','缓存穿透、击穿、雪崩，这些概念背得滚瓜烂熟，真到线上还是栽跟头。记录一次真实的排查过程。','<p>理论的完备不等于工程的稳妥。本文记录一次因热点 Key 过期导致的缓存击穿事故，以及最终的解决方案。</p>',NULL,'draft','tech',NULL,1,0,0,'[]','2024-06-01T11:00:00+08:00','2024-06-02T09:30:00+08:00');

INSERT INTO `blog_moment` (`id`,`content`,`images`,`mood`,`location`,`status`,`published_at`,`allow_comments`,`comment_count`,`like_count`,`created_at`,`updated_at`) VALUES
 (1,'午后雷阵雨停了，空气里全是泥土和青草的湿气。窗台上的迷迭香抽了新芽，磨了一杯耶加雪菲，坐在窗边看水珠从叶片滑落，突然觉得哪怕代码写不出来也没什么大不了的。','["/images/moment-rosemary.svg"]','calm','西溪湿地旁的小书房','published','2024-05-18T08:20:00+08:00',1,0,34,'2024-05-18T08:20:00+08:00','2024-05-18T08:20:00+08:00'),
 (2,'下班在菜市场淘到了一把水灵灵的茼蒿和新鲜的菌菇，炖了一锅热腾腾的豆腐菌菇汤。热气把眼镜蒙上一层白雾的那一刻，感觉一天消耗的电量被瞬间充满。','["/images/moment-soup.svg"]','happy','厨房手记','published','2024-05-17T21:40:00+08:00',1,0,28,'2024-05-17T21:40:00+08:00','2024-05-17T21:40:00+08:00'),
 (3,'重新翻开去年买的那本《设计心理学》，很多当时觉得平淡的句子，如今都能对上工作中遇到的具体困境。书还是要隔一段时间重读。','[]','thought',NULL,'published','2024-05-15T22:10:00+08:00',1,0,19,'2024-05-15T22:10:00+08:00','2024-05-15T22:10:00+08:00');

INSERT INTO `blog_interview` (`id`,`kind`,`title`,`content`,`company`,`position`,`result`,`knowledge_point`,`difficulty`,`status`,`published_at`,`allow_comments`,`comment_count`,`like_count`,`created_at`,`updated_at`) VALUES
 (1,'experience','蚂蚁集团 · 高级前端架构师','<p>共四轮技术面 + HR。核心考察微前端沙箱隔离机制、前端监控全链路指标治理与千人团队研发效能架构，深挖产出与落地难度。</p>','蚂蚁集团','高级前端架构师','pass',NULL,NULL,'published','2024-04-10T14:00:00+08:00',1,0,142,'2024-04-08T10:00:00+08:00','2024-04-10T14:00:00+08:00'),
 (2,'question','React 中 useEffect 的依赖数组是如何比对的？','<p>考察 Object.is 浅比较、闭包陷阱与清理时机。答案要点：每次渲染对依赖项做 Object.is 比较，引用类型需注意 memo 化。</p>','字节跳动',NULL,NULL,'React Hooks','medium','published','2024-04-05T19:00:00+08:00',1,0,56,'2024-04-05T19:00:00+08:00','2024-04-05T19:00:00+08:00'),
 (3,'prep','我的前端面试复习清单','<p>按知识点分块：JavaScript 基础、浏览器原理、框架原理、工程化、算法。每周滚动复习一次，标记薄弱项。</p>',NULL,NULL,NULL,NULL,NULL,'published','2024-03-28T10:00:00+08:00',1,0,31,'2024-03-28T10:00:00+08:00','2024-03-28T10:00:00+08:00');

INSERT INTO `blog_link` (`id`,`name`,`url`,`description`,`logo`,`category`,`group_name`,`status`,`published_at`,`allow_comments`,`comment_count`,`like_count`,`created_at`,`updated_at`) VALUES
 (1,'云游僧的终端','https://example.com/cloud','深入 WebKit 渲染引擎与跨端架构，文笔极其犀利硬核。','/images/link-cloud.svg','friend','技术同好','published','2024-02-01T10:00:00+08:00',1,0,12,'2024-02-01T10:00:00+08:00','2024-02-01T10:00:00+08:00'),
 (2,'Poolsuite FM','https://poolsuite.net','一个把复古夏日氛围做到极致的在线电台，界面本身就是一件数字手工艺品。','/images/link-poolsuite.svg','entertainment',NULL,'published','2024-05-10T15:00:00+08:00',1,0,45,'2024-05-10T15:00:00+08:00','2024-05-10T15:00:00+08:00');

INSERT INTO `blog_content_tag` (`target_type`,`target_id`,`tag_id`) VALUES
 ('post',1,1),('post',1,2),('post',1,3),('post',2,3),('post',2,7),('post',3,5),
 ('interview',1,3),('interview',2,1),('link',1,6);

INSERT INTO `blog_comment` (`id`,`target_type`,`target_id`,`parent_id`,`visitor_id`,`author_name`,`author_email`,`content`,`status`,`created_at`) VALUES
 (1,'post',1,NULL,'legacy-1','阿棠','atang@example.com','状态机这段讲得很透，尤其是布尔值组合爆炸那部分，深有同感。','approved','2024-05-19T09:12:00+08:00'),
 (2,'post',1,1,'legacy-2','Zephyr-Shane','zephyr@example.com','谢谢！后面会写一篇 XState 实战的续篇。','approved','2024-05-19T10:30:00+08:00'),
 (3,'moment',1,NULL,'legacy-3','小满','xiaoman@example.com','迷迭香抽新芽这个细节好治愈。','approved','2024-05-18T12:00:00+08:00');

INSERT INTO `blog_site_config` (`id`,`site_name`,`subtitle`,`description`,`author_name`,`author_bio`,`avatar`,`logo`,`social_links`,`article_signature`,`interview_enabled`,`card_slots`) VALUES
 (1,'阿申の小破站','静水流深，字落云烟','在代码逻辑与烟火日常之间寻找平衡，一名全栈工程师与独立作者。','Zephyr-Shane','白天沉浸在终端与代码之间，夜晚写写文字、手冲咖啡、漫步西湖。','/images/avatar.svg',NULL,'[{"label":"GitHub","url":"https://github.com"},{"label":"Twitter / X","url":"https://x.com"},{"label":"即刻","url":"https://okjk.co"},{"label":"RSS","url":"/rss.xml"}]','Zephyr-Shane',1,NULL);

INSERT INTO `blog_user` (`id`,`email`,`name`,`role`,`password_hash`) VALUES
 (1,'admin@zephyr.dev','Zephyr-Shane','admin','240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9');
