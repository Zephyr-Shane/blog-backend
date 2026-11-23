package com.szenith.ai.domain.blog.service;

import com.szenith.ai.domain.blog.adapter.port.CommentRepository;
import com.szenith.ai.domain.blog.adapter.port.ContentRepository;
import com.szenith.ai.domain.blog.adapter.port.SupportRepository;
import com.szenith.ai.domain.blog.adapter.port.TagRepository;
import com.szenith.ai.domain.blog.model.entity.*;
import com.szenith.ai.domain.blog.model.valobj.SiteConfig;
import com.szenith.ai.domain.blog.model.valobj.SiteOverview;
import com.szenith.ai.domain.blog.model.valobj.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 首页侧栏只读投影：把公告、真实统计、常用标签、最新评论一次组装好。
 * 只消费领域端口，不感知 MyBatis / 表结构；前台一次请求即可拿到全部侧栏数据。
 */
@Service
@RequiredArgsConstructor
public class SiteOverviewService {

    private static final int TAG_LIMIT = 12;
    private static final int COMMENT_LIMIT = 5;

    private final Map<String, ContentRepository<? extends ContentItem>> contentRepos;
    private final TagRepository tags;
    private final CommentRepository comments;
    private final SupportRepository support;

    public SiteOverview overview() {
        SiteConfig cfg = support.getSiteConfig();
        boolean interviewVisible = cfg == null || cfg.isInterviewEnabled();

        // 一次遍历完成：分类型计数、标签引用计数、内容标题索引（供最新评论跳转）
        Map<String, Long> counts = new LinkedHashMap<>();
        Map<String, Long> tagCount = new HashMap<>();
        Map<String, String> tagName = new HashMap<>();
        Map<String, Map<Long, String[]>> titleIndex = new HashMap<>();

        for (Map.Entry<String, ContentRepository<? extends ContentItem>> entry : contentRepos.entrySet()) {
            String type = entry.getKey();
            List<? extends ContentItem> list = entry.getValue().listPublished();
            counts.put(type, (long) list.size());
            Map<Long, String[]> index = new HashMap<>();
            for (ContentItem item : list) {
                String[] titleSlug = titleOf(item);
                if (titleSlug != null) index.put(item.getId(), titleSlug);
                if (item.getTags() != null) {
                    for (Tag t : item.getTags()) {
                        if (t == null || t.getSlug() == null) continue;
                        tagCount.merge(t.getSlug(), 1L, Long::sum);
                        tagName.putIfAbsent(t.getSlug(), t.getName());
                    }
                }
            }
            titleIndex.put(type, index);
            // 面试栏目整体隐藏时，不对外暴露其存在
            if ("interview".equals(type) && !interviewVisible) counts.put(type, 0L);
        }

        List<SiteOverview.PopularTag> popular = tagCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(TAG_LIMIT)
                .map(e -> new SiteOverview.PopularTag(
                        tagName.getOrDefault(e.getKey(), e.getKey()), e.getKey(), e.getValue()))
                .toList();

        List<SiteOverview.RecentComment> recent = comments.listRecentApproved(COMMENT_LIMIT).stream()
                .map(c -> {
                    String[] ts = titleIndex.getOrDefault(c.getTargetType(), Map.of())
                            .get(c.getTargetId());
                    return new SiteOverview.RecentComment(
                            c.getId(),
                            displayName(c.getAuthorName()),
                            excerpt(c.getContent(), 60),
                            c.getCreatedAt(),
                            c.getTargetType(),
                            c.getTargetId(),
                            ts == null ? "" : ts[0],
                            ts == null ? "" : ts[1]);
                })
                .toList();

        SiteOverview.Stats stats = new SiteOverview.Stats(
                counts.getOrDefault("post", 0L),
                counts.getOrDefault("moment", 0L),
                counts.getOrDefault("interview", 0L),
                counts.getOrDefault("link", 0L),
                comments.countByStatus("approved"),
                (long) tags.listAll().size());

        return new SiteOverview(announcement(cfg), stats, popular, recent);
    }

    /** 公告存站点配置 JSON：缺省或关闭时返回空公告，前台自行隐藏模块 */
    private SiteOverview.Announcement announcement(SiteConfig cfg) {
        if (cfg == null || cfg.getAnnouncement() == null) return SiteOverview.Announcement.empty();
        Map<String, Object> a = cfg.getAnnouncement();
        Object visible = a.get("visible");
        boolean on = Boolean.TRUE.equals(visible) || "true".equals(String.valueOf(visible));
        return new SiteOverview.Announcement(
                str(a.get("title")), str(a.get("content")), str(a.get("updatedAt")), on);
    }

    /** 各类内容的展示标题与文章 slug（其余类型 slug 为空，由前端按栏目跳转） */
    private String[] titleOf(ContentItem item) {
        if (item instanceof Post p) return new String[]{p.getTitle(), p.getSlug()};
        if (item instanceof Interview i) return new String[]{i.getTitle(), ""};
        if (item instanceof Link l) return new String[]{l.getName(), ""};
        if (item instanceof Moment m) return new String[]{excerpt(m.getContent(), 24), ""};
        return null;
    }

    private static String displayName(String name) {
        return name == null || name.isBlank() ? "匿名访客" : name;
    }

    private static String excerpt(String raw, int max) {
        if (raw == null) return "";
        String plain = raw.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
        return plain.length() <= max ? plain : plain.substring(0, max) + "…";
    }

    private static String str(Object v) {
        return v == null ? "" : String.valueOf(v);
    }
}
