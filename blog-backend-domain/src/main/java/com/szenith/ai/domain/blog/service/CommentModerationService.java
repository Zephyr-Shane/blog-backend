package com.szenith.ai.domain.blog.service;

import com.szenith.ai.domain.blog.adapter.port.CommentRepository;
import com.szenith.ai.domain.blog.adapter.port.ContentRepository;
import com.szenith.ai.domain.blog.model.entity.Comment;
import com.szenith.ai.domain.blog.model.entity.ContentItem;
import com.szenith.ai.domain.blog.model.valobj.DomainEnums;
import com.szenith.ai.domain.blog.model.valobj.ModerationRules;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 评论批量校验：定时扫描近期已展示评论，按可配置规则识别垃圾内容并隐藏。
 * 只读领域端口、不依赖基础设施；命中后回写内容表计数，保证前台计数与可见评论一致。
 */
@Service
@RequiredArgsConstructor
public class CommentModerationService {

    private final CommentRepository comments;
    private final Map<String, ContentRepository<? extends ContentItem>> contentRepos;

    private static final Pattern LINK = Pattern.compile("(https?://|www\\.)", Pattern.CASE_INSENSITIVE);

    /** 扫描并隐藏违规评论，返回统计与命中原因（供后台手动触发时展示） */
    public Map<String, Object> scan(ModerationRules rules) {
        if (rules == null || !rules.enabled()) {
            return Map.of("enabled", false, "scanned", 0, "hidden", 0, "reasons", List.of());
        }

        String since = OffsetDateTime.now()
                .minusDays(Math.max(1, rules.scanDays()))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        List<Comment> candidates = comments.listByStatusSince(
                DomainEnums.CommentStatus.APPROVED.value(), since, Math.max(1, rules.limit()));

        Map<String, Long> frequency = new HashMap<>();
        for (Comment c : candidates) {
            String key = normalize(c.getContent());
            if (!key.isBlank()) frequency.merge(key, 1L, Long::sum);
        }

        int hidden = 0;
        List<String> reasons = new ArrayList<>();
        for (Comment c : candidates) {
            String reason = violation(c.getContent(), rules, frequency);
            if (reason == null) continue;
            comments.update(c.getId(), Map.of("status", DomainEnums.CommentStatus.HIDDEN.value()));
            recount(c);
            hidden++;
            if (reasons.size() < 20) reasons.add("评论 #" + c.getId() + "：" + reason);
        }

        return Map.of(
                "enabled", true,
                "scanned", candidates.size(),
                "hidden", hidden,
                "reasons", reasons
        );
    }

    /** 命中规则返回原因，否则 null */
    private String violation(String raw, ModerationRules rules, Map<String, Long> frequency) {
        String content = raw == null ? "" : raw.trim();
        if (content.length() < Math.max(1, rules.minLength())) return "内容过短";

        for (String word : rules.sensitiveWords()) {
            if (word != null && !word.isBlank() && content.contains(word)) return "命中敏感词「" + word + "」";
        }
        String lower = content.toLowerCase();
        for (String domain : rules.blockedDomains()) {
            if (domain != null && !domain.isBlank() && lower.contains(domain.toLowerCase())) return "命中受限域名「" + domain + "」";
        }

        int links = LINK.matcher(content).results().mapToInt(m -> 1).sum();
        if (links > rules.maxLinks()) return "外链数量超过 " + rules.maxLinks();

        String key = normalize(content);
        if (!key.isBlank() && frequency.getOrDefault(key, 0L) >= Math.max(2, rules.duplicateThreshold())) {
            return "重复灌水内容";
        }
        return null;
    }

    /** 隐藏后同步内容表冗余计数，前台展示与关联表保持一致 */
    private void recount(Comment c) {
        if (c.getTargetId() == null) return;
        ContentRepository<? extends ContentItem> repo = contentRepos.get(c.getTargetType());
        if (repo == null) return;
        long count = comments.countApproved(c.getTargetType(), c.getTargetId());
        repo.update(c.getTargetId(), Map.of("commentCount", count));
    }

    /** 归一化用于重复检测：忽略空白、标点与大小写 */
    private String normalize(String s) {
        if (s == null) return "";
        return s.replaceAll("[\\s\\p{Punct}\\u3000-\\u303F]+", "").toLowerCase();
    }
}
