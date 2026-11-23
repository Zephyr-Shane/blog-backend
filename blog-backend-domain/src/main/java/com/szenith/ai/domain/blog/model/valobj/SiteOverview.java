package com.szenith.ai.domain.blog.model.valobj;

import java.util.List;

/**
 * 首页侧栏聚合视图：一次请求返回公告、统计、常用标签与最新评论。
 * 只读投影，不对应数据库表；由 SiteOverviewService 从各内容/评论端口组装。
 */
public record SiteOverview(
        Announcement announcement,
        Stats stats,
        List<PopularTag> popularTags,
        List<RecentComment> recentComments
) {
    /** 站点公告：内容由后台站点配置维护，visible=false 时前台不展示 */
    public record Announcement(String title, String content, String updatedAt, boolean visible) {
        public static Announcement empty() {
            return new Announcement("", "", "", false);
        }
    }

    /** 全部为真实计数，不含未埋点的访问量（避免展示假数据） */
    public record Stats(long posts, long moments, long interviews, long links, long comments, long tags) {}

    /** 常用标签：按已发布内容引用次数降序 */
    public record PopularTag(String name, String slug, long count) {}

    /** 最新评论：只暴露公开展示字段，不携带邮箱与访客标识 */
    public record RecentComment(
            Long id,
            String authorName,
            String content,
            String createdAt,
            String targetType,
            Long targetId,
            String targetTitle,
            String targetSlug
    ) {}
}
