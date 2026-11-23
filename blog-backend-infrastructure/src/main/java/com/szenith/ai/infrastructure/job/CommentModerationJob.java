package com.szenith.ai.infrastructure.job;

import com.szenith.ai.domain.blog.adapter.port.ModerationRulesProvider;
import com.szenith.ai.domain.blog.service.CommentModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/** 定时批量校验评论：不逐条人工审核，命中规则自动隐藏，后台可复核恢复 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentModerationJob {

    private final CommentModerationService service;
    private final ModerationRulesProvider rulesProvider;

    @Scheduled(
            fixedDelayString = "${blog.moderation.fixed-delay-ms:300000}",
            initialDelayString = "${blog.moderation.initial-delay-ms:60000}"
    )
    public void run() {
        try {
            Map<String, Object> result = service.scan(rulesProvider.rules());
            Object hidden = result.get("hidden");
            if (hidden instanceof Number n && n.intValue() > 0) {
                log.info("评论定时校验：扫描 {} 条，隐藏 {} 条；{}",
                        result.get("scanned"), hidden, result.get("reasons"));
            }
        } catch (Exception e) {
            // 单次失败不能影响后续周期
            log.warn("评论定时校验执行失败", e);
        }
    }
}
