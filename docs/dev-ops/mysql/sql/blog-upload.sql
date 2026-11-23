-- 交互产生的上传资源表：二进制统一落库，不落文件系统
-- 前端项目自带的静态资源（/images/*.svg）不放这里，它们属于前端代码库
-- id 为应用生成的 UUID（公开唯一标识），同时用于 GET /media/{id} 与引用判断，
-- 不再使用数据库自增 ID，避免暴露内部序号与跨库冲突。
CREATE TABLE IF NOT EXISTS `blog_upload` (
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
