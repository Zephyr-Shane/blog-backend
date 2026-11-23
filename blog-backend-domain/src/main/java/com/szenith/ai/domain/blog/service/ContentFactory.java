package com.szenith.ai.domain.blog.service;

import com.szenith.ai.domain.blog.model.entity.*;
import com.szenith.ai.domain.blog.model.valobj.CoAuthor;
import com.szenith.ai.domain.blog.model.valobj.Tag;
import com.szenith.ai.domain.blog.model.valobj.DomainEnums;
import com.szenith.ai.domain.blog.model.valobj.SocialLink;

import java.time.Instant;
import java.util.*;

/**
 * 内容对象工厂：把前端提交的 Map 转为强类型实体，集中处理反序列化与默认值。
 * 与仓储的 Map→实体 反序列化对称，是新写入的必经转换点。
 */
public final class ContentFactory {

    private ContentFactory() {}

    public static ContentItem create(String type, Map<String, Object> b) {
        return switch (type) {
            case "post" -> post(b);
            case "moment" -> moment(b);
            case "interview" -> interview(b);
            case "link" -> link(b);
            default -> throw new IllegalArgumentException("非内容类型: " + type);
        };
    }

    public static Post post(Map<String, Object> b) {
        Post p = new Post();
        p.setTitle(s(b, "title")); p.setSlug(s(b, "slug")); p.setExcerpt(s(b, "excerpt"));
        p.setContent(s(b, "content")); p.setCoverImage(s(b, "coverImage"));
        p.setCategory(def(s(b, "category"), "tech"));
        p.setStatus(def(s(b, "status"), DomainEnums.Status.DRAFT.value()));
        p.setTags(tags(b)); p.setCoAuthors(coAuthors(b));
        p.setPublishedAt(s(b, "publishedAt"));
        p.setAllowComments(bool(b, "allowComments", true));
        p.setCommentCount(num(b, "commentCount")); p.setLikeCount(num(b, "likeCount"));
        String now = Instant.now().toString();
        p.setCreatedAt(def(s(b, "createdAt"), now)); p.setUpdatedAt(now);
        return p;
    }

    public static Moment moment(Map<String, Object> b) {
        Moment m = new Moment();
        m.setContent(s(b, "content")); m.setImages(strList(b.get("images")));
        m.setMood(def(s(b, "mood"), "calm")); m.setLocation(s(b, "location"));
        m.setStatus(def(s(b, "status"), DomainEnums.Status.DRAFT.value()));
        m.setTags(tags(b)); m.setPublishedAt(s(b, "publishedAt"));
        m.setAllowComments(bool(b, "allowComments", true));
        m.setCommentCount(num(b, "commentCount")); m.setLikeCount(num(b, "likeCount"));
        String now = Instant.now().toString();
        m.setCreatedAt(def(s(b, "createdAt"), now)); m.setUpdatedAt(now);
        return m;
    }

    public static Interview interview(Map<String, Object> b) {
        Interview i = new Interview();
        i.setKind(def(s(b, "kind"), "experience"));
        i.setTitle(s(b, "title")); i.setContent(s(b, "content"));
        i.setCompany(s(b, "company")); i.setPosition(s(b, "position"));
        i.setResult(s(b, "result")); i.setKnowledgePoint(s(b, "knowledgePoint"));
        i.setDifficulty(s(b, "difficulty"));
        i.setStatus(def(s(b, "status"), DomainEnums.Status.DRAFT.value()));
        i.setTags(tags(b)); i.setPublishedAt(s(b, "publishedAt"));
        i.setAllowComments(bool(b, "allowComments", true));
        i.setCommentCount(num(b, "commentCount")); i.setLikeCount(num(b, "likeCount"));
        String now = Instant.now().toString();
        i.setCreatedAt(def(s(b, "createdAt"), now)); i.setUpdatedAt(now);
        return i;
    }

    public static Link link(Map<String, Object> b) {
        Link l = new Link();
        l.setName(s(b, "name")); l.setUrl(s(b, "url")); l.setDescription(s(b, "description"));
        l.setLogo(s(b, "logo")); l.setCategory(def(s(b, "category"), "friend"));
        l.setGroup(s(b, "group"));
        l.setStatus(def(s(b, "status"), DomainEnums.Status.DRAFT.value()));
        l.setTags(tags(b)); l.setPublishedAt(s(b, "publishedAt"));
        l.setAllowComments(bool(b, "allowComments", true));
        l.setCommentCount(num(b, "commentCount")); l.setLikeCount(num(b, "likeCount"));
        String now = Instant.now().toString();
        l.setCreatedAt(def(s(b, "createdAt"), now)); l.setUpdatedAt(now);
        return l;
    }

    public static Tag tag(Map<String, Object> b) {
        return Tag.builder()
                .name(s(b, "name")).slug(s(b, "slug"))
                .category(def(s(b, "category"), "general")).build();
    }

    public static Comment comment(Map<String, Object> b) {
        return Comment.builder()
                .targetType(def(s(b, "targetType"), "post"))
                .targetId(b.get("targetId") instanceof Number n ? n.longValue() : 0L)
                .parentId(b.get("parentId") instanceof Number n ? n.longValue() : null)
                .visitorId(s(b, "visitorId"))
                .authorName(s(b, "authorName")).authorEmail(s(b, "authorEmail"))
                .content(s(b, "content"))
                .status(def(s(b, "status"), DomainEnums.CommentStatus.APPROVED.value()))
                .createdAt(def(s(b, "createdAt"), Instant.now().toString())).build();
    }

    public static User user(Map<String, Object> b) {
        return User.builder().email(s(b, "email")).name(s(b, "name"))
                .role(def(s(b, "role"), "admin"))
                .passwordHash(def(s(b, "passwordHash"), "")).build();
    }

    @SuppressWarnings("unchecked")
    private static List<Tag> tags(Map<String, Object> b) {
        Object raw = b.get("tags");
        if (!(raw instanceof List<?> list)) return new ArrayList<>();
        List<Tag> out = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof Map<?, ?>) {
                Map<String, Object> m = (Map<String, Object>) o;
                Tag t = new Tag();
                Object id = m.get("id");
                t.setId(id instanceof Number n ? n.longValue() : null);
                t.setName(String.valueOf(m.getOrDefault("name", "")));
                t.setSlug(String.valueOf(m.getOrDefault("slug", "")));
                t.setCategory(String.valueOf(m.getOrDefault("category", "general")));
                out.add(t);
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static List<CoAuthor> coAuthors(Map<String, Object> b) {
        Object raw = b.get("coAuthors");
        if (!(raw instanceof List<?> list)) return new ArrayList<>();
        List<CoAuthor> out = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof Map<?, ?>) {
                Map<String, Object> m = (Map<String, Object>) o;
                out.add(CoAuthor.builder()
                        .name(String.valueOf(m.getOrDefault("name", "")))
                        .blogUrl(String.valueOf(m.getOrDefault("blogUrl", ""))).build());
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public static List<SocialLink> socialLinks(Object raw) {
        if (!(raw instanceof List<?> list)) return new ArrayList<>();
        List<SocialLink> out = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof Map<?, ?>) {
                Map<String, Object> m = (Map<String, Object>) o;
                out.add(SocialLink.builder()
                        .label(String.valueOf(m.getOrDefault("label", "")))
                        .url(String.valueOf(m.getOrDefault("url", ""))).build());
            }
        }
        return out;
    }

    private static List<String> strList(Object raw) {
        if (!(raw instanceof List<?> list)) return new ArrayList<>();
        return list.stream().map(String::valueOf).toList();
    }

    private static String s(Map<String, Object> b, String k) {
        Object v = b.get(k);
        return v == null ? null : v.toString();
    }

    private static String def(String v, String d) { return v == null || v.isBlank() ? d : v; }

    private static boolean bool(Map<String, Object> b, String k, boolean d) {
        Object v = b.get(k);
        if (v == null) return d;
        if (v instanceof Boolean bo) return bo;
        return Boolean.parseBoolean(v.toString());
    }

    private static int num(Map<String, Object> b, String k) {
        Object v = b.get(k);
        return v instanceof Number n ? n.intValue() : 0;
    }
}
