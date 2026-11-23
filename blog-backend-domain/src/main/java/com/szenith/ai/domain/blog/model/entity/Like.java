package com.szenith.ai.domain.blog.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Like {
    private Long id;
    private String visitorId;
    private String targetType;
    private Long targetId;
    private String createdAt;
}
