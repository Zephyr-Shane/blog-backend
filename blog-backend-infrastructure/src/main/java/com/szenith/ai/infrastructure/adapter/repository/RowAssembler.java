package com.szenith.ai.infrastructure.adapter.repository;

import com.szenith.ai.domain.blog.model.entity.ContentItem;
import com.szenith.ai.domain.blog.model.valobj.CoAuthor;
import com.szenith.ai.domain.blog.model.valobj.Tag;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.*;

/** 内容仓储装配助手：公共字段与 JSON 字段的双向转换，供四个仓储复用 */
final class RowAssembler {

    private RowAssembler() {}

    static void fillCommon(ContentItem c, Map<String, Object> r) {
        c.setId(n(r.get("id")));
        c.setStatus(str(r.get("status")));
        c.setPublishedAt(str(r.get("publishedAt")));
        c.setAllowComments(r.get("allowComments") != null && Integer.valueOf(1).equals(r.get("allowComments"))
                || Boolean.TRUE.equals(r.get("allowComments")));
        c.setCommentCount(n(r.get("commentCount")).intValue());
        c.setLikeCount(n(r.get("likeCount")).intValue());
        c.setCreatedAt(str(r.get("createdAt")));
        c.setUpdatedAt(str(r.get("updatedAt")));
    }

    static List<Tag> tags(Map<String, Object> r) {
        return parseList(r.get("tags"), new TypeReference<List<Tag>>() {});
    }

    static List<CoAuthor> coAuthors(Map<String, Object> r) {
        return parseList(r.get("coAuthors"), new TypeReference<List<CoAuthor>>() {});
    }

    static List<String> strings(Map<String, Object> r, String key) {
        return parseList(r.get(key), new TypeReference<List<String>>() {});
    }

    static <T> List<T> parseList(Object raw, TypeReference<List<T>> type) {
        if (raw == null) return new ArrayList<>();
        if (raw instanceof List<?> list) {
            List<T> out = new ArrayList<>();
            for (Object o : list) out.add((T) o);
            return out;
        }
        try {
            List<T> parsed = JSON.parseObject(raw.toString(), type);
            return parsed == null ? new ArrayList<>() : parsed;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /** 实体公共字段 → 行 Map（列名 snake_case） */
    static void fillCommonRow(ContentItem c, Map<String, Object> row) {
        row.put("status", c.getStatus());
        row.put("published_at", c.getPublishedAt());
        row.put("allow_comments", c.isAllowComments() ? 1 : 0);
        row.put("comment_count", c.getCommentCount());
        row.put("like_count", c.getLikeCount());
        row.put("created_at", c.getCreatedAt());
        row.put("updated_at", c.getUpdatedAt());
        if (c.getId() != null) row.put("id", c.getId());
    }

    static String json(Object v) { return JSON.toJSONString(v == null ? List.of() : v); }

    static String str(Object v) { return v == null ? null : v.toString(); }

    static Long n(Object v) {
        if (v instanceof Number num) return num.longValue();
        return null;
    }
}
