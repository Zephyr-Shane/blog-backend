package com.szenith.ai.infrastructure.util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SQL 列白名单工具：所有动态列名/SET 子句必须经过白名单过滤，杜绝 SQL 注入。
 */
public final class SqlColumns {

    private SqlColumns() {}

    /** 内容表各类型的可写列（snake_case） */
    public static final Map<String, List<String>> CONTENT_COLUMNS = Map.of(
        "post", List.of("title","slug","excerpt","content","cover_image","status","category",
                "published_at","allow_comments","comment_count","like_count","co_authors","created_at","updated_at"),
        "moment", List.of("content","images","mood","location","status","published_at",
                "allow_comments","comment_count","like_count","created_at","updated_at"),
        "interview", List.of("kind","title","content","company","position","result","knowledge_point",
                "difficulty","status","published_at","allow_comments","comment_count","like_count","created_at","updated_at"),
        "link", List.of("name","url","description","logo","category","group_name","status",
                "published_at","allow_comments","comment_count","like_count","created_at","updated_at")
    );

    public static final List<String> TAG_COLUMNS = List.of("name","slug","category");
    public static final List<String> COMMENT_COLUMNS = List.of(
            "target_type","target_id","parent_id","visitor_id","author_name","author_email","content","status","created_at");
    public static final List<String> USER_COLUMNS = List.of("email","name","role","password_hash");
    public static final List<String> CONFIG_COLUMNS = List.of(
            "site_name","subtitle","description","author_name","author_bio","avatar","logo",
            "social_links","article_signature","interview_enabled","card_slots","announcement");

    private static final Map<String, String> TABLE_OF = Map.of(
            "post", "blog_post", "moment", "blog_moment",
            "interview", "blog_interview", "link", "blog_link");

    public static String table(String type) {
        String t = TABLE_OF.get(type);
        if (t == null) throw new IllegalArgumentException("未知内容类型: " + type);
        return t;
    }

    /** 列名 → 前端 payload 键名的特例映射（默认 snake→camel，此处仅列例外） */
    private static final Map<String, String> PAYLOAD_KEY_ALIAS = Map.of("group_name", "group");

    /** 从 payload 过滤出合法的 snake_case 列；jsonFields 需先序列化为字符串 */
    public static Map<String, Object> filter(Map<String, Object> body, List<String> allowed) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (String col : allowed) {
            String camel = PAYLOAD_KEY_ALIAS.getOrDefault(col, camel(col));
            if (body.containsKey(camel) && body.get(camel) != null) {
                out.put(col, normalize(body.get(camel)));
            }
        }
        return out;
    }

    /** 生成 SET 子句：col = #{v0}, col2 = #{v1}... 值由调用方按顺序放入 ParamMap */
    public static String setClause(Map<String, Object> columns, Map<String, Object> paramSink) {
        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, Object> e : columns.entrySet()) {
            String key = "v" + parts.size();
            paramSink.put(key, e.getValue());
            parts.add(e.getKey() + " = #{vals." + key + "}");
        }
        return String.join(", ", parts);
    }

    private static Object normalize(Object v) {
        if (v instanceof Boolean b) return b ? 1 : 0;
        if (v instanceof Map || v instanceof List) return com.alibaba.fastjson.JSON.toJSONString(v);
        return v;
    }

    private static String camel(String snake) {
        StringBuilder sb = new StringBuilder();
        boolean up = false;
        for (char c : snake.toCharArray()) {
            if (c == '_') { up = true; continue; }
            sb.append(up ? Character.toUpperCase(c) : c);
            up = false;
        }
        return sb.toString();
    }

    /**
     * MyBatis 返回 Map 的键仍是下划线（map-underscore-to-camel-case 对 POJO 生效、对 Map 不生效），
     * 这里统一转成驼峰，使领域层拿到与契约一致的字段名。
     */
    public static Map<String, Object> row(Map<String, Object> raw) {
        if (raw == null) return null;
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : raw.entrySet()) {
            out.put(camel(e.getKey()), e.getValue());
        }
        return out;
    }
}
