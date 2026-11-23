package com.szenith.ai.domain.blog.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private Long id;
    private String targetType;
    private Long targetId;
    private Long parentId;
    /** 匿名访客标识（前端 localStorage），用于标识评论者；作者回复时为空 */
    private String visitorId;
    private String authorName;
    private String authorEmail;
    private String content;
    private String status;
    private String createdAt;
}
