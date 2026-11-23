package com.szenith.ai.infrastructure.event;

import com.szenith.ai.domain.blog.adapter.port.NotificationRepository;
import com.szenith.ai.domain.blog.model.entity.Notification;
import com.szenith.ai.domain.blog.model.event.CommentCreatedEvent;
import com.szenith.ai.domain.blog.model.event.LikeCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.UUID;

/**
 * 通知落库监听器：只在业务事务成功提交后（AFTER_COMMIT）生成通知，
 * 避免"业务回滚却发出通知"。新增通知类型只需加一个监听方法，不改业务代码。
 */
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationRepository notifications;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onComment(CommentCreatedEvent e) {
        String title = safe(e.getTargetTitle());
        String who = safe(e.getAuthorName());
        notifications.save(Notification.builder()
                .id(UUID.randomUUID().toString())
                .type("comment")
                .title("新评论 · " + title)
                .content(who + "：" + excerpt(e.getContent()))
                .targetType(e.getTargetType())
                .targetId(e.getTargetId())
                .read(false)
                .createdAt(e.getCreatedAt() == null ? Instant.now().toString() : e.getCreatedAt())
                .build());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLike(LikeCreatedEvent e) {
        String title = safe(e.getTargetTitle());
        notifications.save(Notification.builder()
                .id(UUID.randomUUID().toString())
                .type("like")
                .title("新赞 · " + title)
                .content("有访客点赞了这条内容")
                .targetType(e.getTargetType())
                .targetId(e.getTargetId())
                .read(false)
                .createdAt(e.getCreatedAt() == null ? Instant.now().toString() : e.getCreatedAt())
                .build());
    }

    private static String safe(String s) { return s == null || s.isBlank() ? "未命名内容" : s; }

    private static String excerpt(String content) {
        if (content == null) return "";
        String oneLine = content.replaceAll("\\s+", " ").trim();
        return oneLine.length() <= 60 ? oneLine : oneLine.substring(0, 60) + "…";
    }
}
