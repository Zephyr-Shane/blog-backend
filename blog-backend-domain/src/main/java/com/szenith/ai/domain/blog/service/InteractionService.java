package com.szenith.ai.domain.blog.service;

import com.szenith.ai.domain.blog.adapter.port.CommentRepository;
import com.szenith.ai.domain.blog.adapter.port.ContentRepository;
import com.szenith.ai.domain.blog.adapter.port.LikeRepository;
import com.szenith.ai.domain.blog.model.entity.Comment;
import com.szenith.ai.domain.blog.model.entity.ContentItem;
import com.szenith.ai.domain.blog.model.entity.Interview;
import com.szenith.ai.domain.blog.model.entity.Like;
import com.szenith.ai.domain.blog.model.entity.Link;
import com.szenith.ai.domain.blog.model.entity.Post;
import com.szenith.ai.domain.blog.model.event.CommentCreatedEvent;
import com.szenith.ai.domain.blog.model.event.LikeCreatedEvent;
import com.szenith.ai.domain.blog.model.valobj.DomainEnums;
import com.szenith.ai.types.enums.ResponseCode;
import com.szenith.ai.types.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 互动领域服务：评论与点赞。
 * 规则：
 * - 评论提交即 approved（即时显示），带匿名 visitorId；作者回复通过 parentId 楼中楼。
 * - 点赞按访客唯一 toggle（再点取消），无假基数。
 * - 计数以关联表为真相源，互动后在同一事务内回写内容表 like_count/comment_count。
 * - 互动动作发布领域事件，通知落库由 AFTER_COMMIT 监听器负责。
 */
@Service
@RequiredArgsConstructor
public class InteractionService {

    private final CommentRepository comments;
    private final LikeRepository likes;
    private final Map<String, ContentRepository<? extends ContentItem>> contentRepos;
    private final ApplicationEventPublisher publisher;

    public List<Comment> listComments(String targetType, Long targetId) {
        if (targetType == null || targetType.isEmpty() || targetId == null) {
            throw new AppException(ResponseCode.BAD_REQUEST, "targetType 与 targetId 必填");
        }
        return comments.listApproved(targetType, targetId);
    }

    /** 当前访客的点赞状态 + 真实计数（页面加载时回显"已赞"） */
    public Map<String, Object> likeState(String visitorId, String targetType, Long targetId) {
        if (targetType == null || targetId == null) {
            throw new AppException(ResponseCode.BAD_REQUEST, "targetType 与 targetId 必填");
        }
        boolean liked = visitorId != null && !visitorId.isBlank()
                && likes.find(visitorId, targetType, targetId) != null;
        long count = likes.countByTarget(targetType, targetId);
        return Map.of("liked", liked, "likeCount", count);
    }

    /** 发表评论：即时显示（approved），带 visitorId；同事务回写评论数并发布通知事件 */
    @Transactional
    public Comment postComment(Map<String, Object> body) {
        String targetType = str(body, "targetType");
        Object tid = body.get("targetId");
        if (!DomainEnums.TargetType.contains(targetType) || !(tid instanceof Number)) {
            throw new AppException(ResponseCode.BAD_REQUEST, "targetType 或 targetId 非法");
        }
        String content = str(body, "content");
        if (content == null || content.isBlank()) {
            throw new AppException(ResponseCode.BAD_REQUEST, "评论内容不能为空");
        }
        long targetId = ((Number) tid).longValue();
        String authorName = def(str(body, "authorName"), "匿名访客");
        String visitorId = def(str(body, "visitorId"), "");
        String now = Instant.now().toString();

        Comment c = Comment.builder()
                .targetType(targetType)
                .targetId(targetId)
                .parentId(body.get("parentId") instanceof Number n ? n.longValue() : null)
                .visitorId(visitorId)
                .authorName(authorName)
                .authorEmail(str(body, "authorEmail"))
                .content(content)
                .status(DomainEnums.CommentStatus.APPROVED.value())
                .createdAt(now)
                .build();
        Comment saved = comments.save(c);

        // 真相源回写：评论数 = 该内容 approved 评论数
        recount(targetType, targetId, "commentCount", comments.countApproved(targetType, targetId));

        // 发布领域事件：作者回复（有 parentId）不发访客通知，避免噪音
        if (c.getParentId() == null) {
            publisher.publishEvent(CommentCreatedEvent.builder()
                    .authorName(authorName)
                    .content(content)
                    .targetType(targetType)
                    .targetId(targetId)
                    .targetTitle(titleOf(targetType, targetId))
                    .createdAt(now)
                    .build());
        }
        return saved;
    }

    /** 点赞 toggle：已点赞则取消，否则新增；同事务回写点赞数并发布通知事件 */
    @Transactional
    public Map<String, Object> toggleLike(Map<String, Object> body) {
        String visitorId = str(body, "visitorId");
        String targetType = str(body, "targetType");
        Object tid = body.get("targetId");
        if (visitorId == null || visitorId.isBlank()
                || !DomainEnums.TargetType.contains(targetType) || !(tid instanceof Number)) {
            throw new AppException(ResponseCode.BAD_REQUEST, "visitorId / targetType / targetId 必填且合法");
        }
        long targetId = ((Number) tid).longValue();
        Like existing = likes.find(visitorId, targetType, targetId);
        boolean liked;
        if (existing != null) {
            likes.delete(visitorId, targetType, targetId);
            liked = false;
        } else {
            likes.save(Like.builder()
                    .visitorId(visitorId).targetType(targetType).targetId(targetId)
                    .createdAt(Instant.now().toString()).build());
            liked = true;
        }
        long likeCount = likes.countByTarget(targetType, targetId);
        recount(targetType, targetId, "likeCount", likeCount);

        if (liked) {
            publisher.publishEvent(LikeCreatedEvent.builder()
                    .visitorId(visitorId)
                    .targetType(targetType)
                    .targetId(targetId)
                    .targetTitle(titleOf(targetType, targetId))
                    .createdAt(Instant.now().toString())
                    .build());
        }
        return Map.of("liked", liked, "likeCount", likeCount);
    }

    /** 以关联表真实值回写内容表冗余计数字段（同事务，列表页直接读冗余值） */
    private void recount(String targetType, long targetId, String field, long count) {
        ContentRepository<? extends ContentItem> repo = contentRepos.get(targetType);
        if (repo == null || repo.findById(targetId) == null) return;
        Map<String, Object> patch = new LinkedHashMap<>();
        patch.put(field, (int) count);
        repo.update(targetId, patch);
    }

    /** 取内容标题用于通知文案；不同实体类型字段不同，用 instanceof 只读适配 */
    private String titleOf(String targetType, long targetId) {
        ContentRepository<? extends ContentItem> repo = contentRepos.get(targetType);
        if (repo == null) return null;
        ContentItem item = repo.findById(targetId);
        if (item instanceof Post p) return p.getTitle();
        if (item instanceof Interview i) return i.getTitle();
        if (item instanceof Link l) return l.getName();
        return null;
    }

    private static String str(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? null : v.toString();
    }

    private static String def(String v, String fallback) {
        return v == null || v.isBlank() ? fallback : v;
    }
}
