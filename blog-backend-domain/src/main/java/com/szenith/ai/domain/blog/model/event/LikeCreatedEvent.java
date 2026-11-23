package com.szenith.ai.domain.blog.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 点赞领域事件（仅新增赞时发布，取消赞不通知）。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeCreatedEvent {
    private String visitorId;
    private String targetType;
    private Long targetId;
    private String targetTitle;
    private String createdAt;
}
