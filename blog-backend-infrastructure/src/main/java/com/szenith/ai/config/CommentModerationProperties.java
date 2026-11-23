package com.szenith.ai.config;

import com.szenith.ai.domain.blog.model.valobj.ModerationRules;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/** 评论校验规则配置：blog.moderation.*，可用yml覆盖，缺省值保证开箱即用 */
@Data
@ConfigurationProperties(prefix = "blog.moderation")
public class CommentModerationProperties {

    private boolean enabled = true;
    private List<String> sensitiveWords = new ArrayList<>(List.of("博彩", "赌博", "刷单", "代开发票", "加微信"));
    private List<String> blockedDomains = new ArrayList<>(List.of("bit.ly", "tinyurl.com"));
    private int minLength = 2;
    private int maxLinks = 1;
    private int duplicateThreshold = 3;
    private int scanDays = 7;
    private int limit = 500;
    /** 定时周期（毫秒），默认 5 分钟 */
    private long fixedDelayMs = 300000L;
    /** 首次执行延迟（毫秒），默认 1 分钟，避免启动时抢资源 */
    private long initialDelayMs = 60000L;

    public ModerationRules toRules() {
        return new ModerationRules(
                enabled, sensitiveWords, blockedDomains,
                minLength, maxLinks, duplicateThreshold, scanDays, limit
        );
    }
}
