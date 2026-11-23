package com.szenith.ai.domain.blog.adapter.port;

import com.szenith.ai.domain.blog.model.entity.Comment;

import java.util.List;
import java.util.Map;

public interface CommentRepository {
    List<Comment> listAll();

    /** 前台可见评论：指定目标 + 仅 approved */
    List<Comment> listApproved(String targetType, Long targetId);

    Comment findById(Long id);

    Comment save(Comment comment);

    Comment update(Long id, Map<String, Object> patch);

    boolean delete(Long id);

    long countByStatus(String status);

    /** 指定内容前台可见（approved）评论数，用于计数回写 */
    long countApproved(String targetType, Long targetId);

    /** 定时校验用：取指定状态、指定时间之后的评论 */
    List<Comment> listByStatusSince(String status, String since, int limit);

    /** 首页侧栏：最近 N 条前台可见评论（approved），按时间倒序 */
    List<Comment> listRecentApproved(int limit);
}
