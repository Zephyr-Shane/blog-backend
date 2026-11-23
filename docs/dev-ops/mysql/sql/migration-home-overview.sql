-- ============================================================
-- 首页侧栏（参考 jiewen.run 布局）迁移脚本
-- 变更：blog_site_config 新增 announcement 配置列（JSON TEXT）
-- 说明：仅加列，不改既有数据；公告缺省为 NULL，前台自动隐藏公告卡片。
-- 执行前建议备份：mysqldump -uroot -p zephyr_blog blog_site_config > backup.sql
-- ============================================================

ALTER TABLE `blog_site_config`
    ADD COLUMN `announcement` TEXT NULL
    COMMENT '站点公告配置 JSON：title/content/updatedAt/visible';

-- 可选：写入一条默认公告（按需执行，默认关闭以免打扰现有站点）
-- UPDATE `blog_site_config` SET `announcement` =
--   '{"title":"站点公告","content":"欢迎来到我的博客。","visible":true,"updatedAt":"2026-11-20"}' WHERE id = 1;
