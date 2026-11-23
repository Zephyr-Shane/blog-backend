package com.szenith.ai.domain.blog.service;

import com.szenith.ai.domain.blog.adapter.port.ContentRepository;
import com.szenith.ai.domain.blog.adapter.port.TagRepository;
import com.szenith.ai.domain.blog.model.entity.*;
import com.szenith.ai.domain.blog.model.valobj.DomainEnums;
import com.szenith.ai.domain.blog.model.valobj.FeedItem;
import com.szenith.ai.domain.blog.model.valobj.SiteConfig;
import com.szenith.ai.domain.blog.adapter.port.SupportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 前台只读查询领域服务。
 * 业务规则：仅返回 published；面试栏目可按站点配置整体隐藏；标签/搜索跨栏目聚合。
 */
@Service
@RequiredArgsConstructor
public class ContentQueryService {

    private final ContentRepository<Post> posts;
    private final ContentRepository<Moment> moments;
    private final ContentRepository<Interview> interviews;
    private final ContentRepository<Link> links;
    private final TagRepository tags;
    private final SupportRepository support;

    /** 面试栏目开关：关闭时所有前台查询都不返回面试内容 */
    private boolean interviewVisible() {
        SiteConfig cfg = support.getSiteConfig();
        return cfg == null || cfg.isInterviewEnabled();
    }

    public List<Post> listPosts(String tagSlug) {
        List<Post> list = new ArrayList<>(posts.listPublished());
        if (tagSlug != null && !tagSlug.isEmpty()) {
            list.removeIf(p -> !hasTag(p.getTags(), tagSlug));
        }
        return sortByPublishedDesc(list);
    }

    public List<Moment> listMoments(String mood) {
        List<Moment> list = new ArrayList<>(moments.listPublished());
        if (mood != null && !mood.isEmpty()) {
            list.removeIf(m -> !mood.equals(m.getMood()));
        }
        return sortByPublishedDesc(list);
    }

    public List<Interview> listInterviews(String kind) {
        if (!interviewVisible()) return List.of();
        List<Interview> list = new ArrayList<>(interviews.listPublished());
        if (kind != null && !kind.isEmpty()) {
            list.removeIf(i -> !kind.equals(i.getKind()));
        }
        return sortByPublishedDesc(list);
    }

    public List<Link> listLinks(String category) {
        List<Link> list = new ArrayList<>(links.listPublished());
        if (category != null && !category.isEmpty()) {
            list.removeIf(l -> !category.equals(l.getCategory()));
        }
        return sortByPublishedDesc(list);
    }

    public Post findPostBySlug(String slug) {
        Post p = posts.listPublished().stream()
                .filter(x -> slug.equals(x.getSlug())).findFirst().orElse(null);
        return p;
    }

    /** 首页混排：四类已发布内容按 publishedAt 倒序 */
    public List<FeedItem> getFeed() {
        List<FeedItem> items = new ArrayList<>();
        collect(items, posts.listPublished());
        collect(items, moments.listPublished());
        collect(items, links.listPublished());
        if (interviewVisible()) collect(items, interviews.listPublished());
        items.sort(Comparator.comparing(
                (FeedItem f) -> f.getPublishedAt() == null ? "" : f.getPublishedAt()).reversed());
        return items;
    }

    private void collect(List<FeedItem> sink, List<? extends ContentItem> source) {
        for (ContentItem c : source) {
            if (c.getPublishedAt() != null) {
                sink.add(FeedItem.builder()
                        .type(c.type()).data(c).publishedAt(c.getPublishedAt()).build());
            }
        }
    }

    /** 标签聚合：返回四类内容分组 */
    public Map<String, List<? extends ContentItem>> getTagContents(String slug) {
        Map<String, List<? extends ContentItem>> result = new LinkedHashMap<>();
        result.put("posts", filterByTag(listPosts(null), slug));
        result.put("moments", filterByTag(listMoments(null), slug));
        result.put("interviews", filterByTag(listInterviews(null), slug));
        result.put("links", filterByTag(listLinks(null), slug));
        return result;
    }

    /** 全站搜索：对内容字段做大小写不敏感匹配 */
    public Map<String, List<? extends ContentItem>> search(String q) {
        String kw = q == null ? "" : q.toLowerCase(Locale.ROOT);
        Map<String, List<? extends ContentItem>> result = new LinkedHashMap<>();
        result.put("posts",  listPosts(null).stream().filter(c -> matches(c, kw)).toList());
        result.put("moments", listMoments(null).stream().filter(c -> matches(c, kw)).toList());
        result.put("interviews", listInterviews(null).stream().filter(c -> matches(c, kw)).toList());
        result.put("links", listLinks(null).stream().filter(c -> matches(c, kw)).toList());
        return result;
    }

    public List<?> getTags() {
        return tags.listAll();
    }

    private boolean matches(ContentItem c, String kw) {
        if (kw.isEmpty()) return true;
        // 复用领域实体.toString() 覆盖全部字段，等价于 mock 的 JSON.stringify 搜索
        return c.toString().toLowerCase(Locale.ROOT).contains(kw);
    }

    private <T extends ContentItem> List<T> filterByTag(List<T> list, String slug) {
        return list.stream().filter(c -> hasTag(c.getTags(), slug)).toList();
    }

    private boolean hasTag(List<?> itemTags, String slug) {
        if (itemTags == null) return false;
        return itemTags.stream().anyMatch(t -> slug.equals(((com.szenith.ai.domain.blog.model.valobj.Tag) t).getSlug()));
    }

    private <T extends ContentItem> List<T> sortByPublishedDesc(List<T> list) {
        list.sort(Comparator.comparing(
                (T c) -> c.getPublishedAt() == null ? "" : c.getPublishedAt()).reversed());
        return list;
    }

    /** 供后台统计使用 */
    public long countAllContents() {
        return posts.countAll() + moments.countAll() + interviews.countAll() + links.countAll();
    }

    public long countPublished() {
        return posts.countByStatus(DomainEnums.Status.PUBLISHED.value())
                + moments.countByStatus(DomainEnums.Status.PUBLISHED.value())
                + interviews.countByStatus(DomainEnums.Status.PUBLISHED.value())
                + links.countByStatus(DomainEnums.Status.PUBLISHED.value());
    }
}
