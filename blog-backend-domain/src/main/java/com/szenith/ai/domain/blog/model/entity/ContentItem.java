package com.szenith.ai.domain.blog.model.entity;

import com.szenith.ai.domain.blog.model.valobj.Tag;
import lombok.Data;

import java.util.List;

/**
 * 内容抽象基类：承载所有内容类型共有的字段（发布状态、标签、互动计数、时间戳）。
 * 前台混排、点赞、评论、标签聚合都依赖这一统一抽象。
 */
@Data
public abstract class ContentItem {
    private Long id;
    private String status;
    private List<Tag> tags;
    private String publishedAt;
    private boolean allowComments;
    private int commentCount;
    private int likeCount;
    private String createdAt;
    private String updatedAt;

    /** 内容类型标识，用于 feed 与评论/点赞的 targetType */
    public abstract String type();
}
