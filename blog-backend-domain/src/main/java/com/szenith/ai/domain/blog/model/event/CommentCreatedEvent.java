package com.szenith.ai.domain.blog.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论发表领域事件。业务动作只负责发布，通知落库由 AFTER_COMMIT 监听器完成，
 * 从而避免"业务回滚却发出通知"，也保证新增通知类型无需改动业务代码。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreatedEvent {
    private String authorName;
    private String content;
    private String targetType;
    private Long targetId;
    /** 跳转用的内容标题（由领域服务查得） */
    private String targetTitle;
    private String createdAt;
}
