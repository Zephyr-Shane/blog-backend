package com.szenith.ai.domain.blog.model.valobj;

import java.util.List;

/**
 * 评论批量校验规则（值对象）。规则由外部配置提供，领域服务只消费，不依赖 Spring 配置类。
 */
public record ModerationRules(
        boolean enabled,
        List<String> sensitiveWords,
        List<String> blockedDomains,
        int minLength,
        int maxLinks,
        int duplicateThreshold,
        int scanDays,
        int limit
) {
    public static ModerationRules defaults() {
        return new ModerationRules(
                true,
                List.of("博彩", "赌博", "刷单", "代开发票", "加微信"),
                List.of("bit.ly", "tinyurl.com"),
                2, 1, 3, 7, 500
        );
    }
}
