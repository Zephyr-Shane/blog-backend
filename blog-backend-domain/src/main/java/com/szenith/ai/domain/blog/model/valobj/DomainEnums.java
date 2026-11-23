package com.szenith.ai.domain.blog.model.valobj;

import java.util.Arrays;
import java.util.List;

/**
 * 领域枚举常量：字段值以小写字符串存储，严格对齐 API 契约。
 */
public final class DomainEnums {

    private DomainEnums() {}

    public enum Status {
        DRAFT("draft"), PUBLISHED("published");
        private final String v;
        Status(String v) { this.v = v; }
        public String value() { return v; }
        public static boolean isPublished(String s) { return PUBLISHED.v.equals(s); }
    }

    public enum CommentStatus {
        PENDING("pending"), APPROVED("approved"), SPAM("spam"), HIDDEN("hidden");
        private final String v;
        CommentStatus(String v) { this.v = v; }
        public String value() { return v; }
    }

    public enum TargetType {
        POST("post"), MOMENT("moment"), INTERVIEW("interview"), LINK("link");
        private final String v;
        TargetType(String v) { this.v = v; }
        public String value() { return v; }
        public static boolean contains(String s) {
            return Arrays.stream(values()).anyMatch(e -> e.v.equals(s));
        }
    }

    /** 后台通用资源名（单数，与前端 fetch 一致） */
    public enum ResourceType {
        POST("post"), MOMENT("moment"), INTERVIEW("interview"), LINK("link"),
        TAG("tag"), COMMENT("comment"), USER("user");
        private final String v;
        ResourceType(String v) { this.v = v; }
        public String value() { return v; }
        public static boolean contains(String s) {
            return Arrays.stream(values()).anyMatch(e -> e.v.equals(s));
        }
    }

    public enum Mood {
        CALM("calm"), HAPPY("happy"), SOLITUDE("solitude"), THOUGHT("thought"), MELANCHOLY("melancholy");
        private final String v;
        Mood(String v) { this.v = v; }
        public String value() { return v; }
        public static boolean contains(String s) {
            return s == null || Arrays.stream(values()).anyMatch(e -> e.v.equals(s));
        }
    }

    public enum InterviewKind {
        EXPERIENCE("experience"), QUESTION("question"), PREP("prep");
        private final String v;
        InterviewKind(String v) { this.v = v; }
        public String value() { return v; }
    }

    public enum TagCategory {
        TECH("tech"), LIFE("life"), GENERAL("general");
        private final String v;
        TagCategory(String v) { this.v = v; }
        public String value() { return v; }
    }

    public static List<String> targetTypes() {
        return Arrays.stream(TargetType.values()).map(TargetType::value).toList();
    }
}
