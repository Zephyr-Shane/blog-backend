package com.szenith.ai.domain.blog.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 站内通知：由业务领域事件在事务提交后落库，供后台铃铛展示。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private String id;
    /** 通知类型：comment / like / system */
    private String type;
    private String title;
    private String content;
    /** 跳转目标：post/moment/interview/link，可为空 */
    private String targetType;
    private Long targetId;
    private boolean read;
    private String createdAt;
}
