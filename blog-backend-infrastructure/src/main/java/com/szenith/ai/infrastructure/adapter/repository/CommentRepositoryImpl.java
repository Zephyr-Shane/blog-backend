package com.szenith.ai.infrastructure.adapter.repository;

import com.szenith.ai.domain.blog.adapter.port.CommentRepository;
import com.szenith.ai.domain.blog.model.entity.Comment;
import com.szenith.ai.domain.blog.service.ContentFactory;
import com.szenith.ai.infrastructure.dao.SupportMapper;
import com.szenith.ai.infrastructure.util.SqlColumns;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {

    private final SupportMapper mapper;

    private Comment toComment(Map<String, Object> raw) {
        if (raw == null) return null;
        Map<String, Object> r = SqlColumns.row(raw);
        return Comment.builder()
                .id(RowAssembler.n(r.get("id")))
                .targetType(RowAssembler.str(r.get("targetType")))
                .targetId(RowAssembler.n(r.get("targetId")))
                .parentId(RowAssembler.n(r.get("parentId")))
                .visitorId(RowAssembler.str(r.get("visitorId")))
                .authorName(RowAssembler.str(r.get("authorName")))
                .authorEmail(RowAssembler.str(r.get("authorEmail")))
                .content(RowAssembler.str(r.get("content")))
                .status(RowAssembler.str(r.get("status")))
                .createdAt(RowAssembler.str(r.get("createdAt")))
                .build();
    }

    @Override
    public List<Comment> listAll() {
        return mapper.listComments().stream().map(this::toComment).toList();
    }

    @Override
    public List<Comment> listApproved(String targetType, Long targetId) {
        return mapper.listApprovedComments(targetType, targetId).stream().map(this::toComment).toList();
    }

    @Override
    public Comment findById(Long id) { return toComment(mapper.findCommentById(id)); }

    @Override
    public Comment save(Comment c) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("target_type", c.getTargetType());
        row.put("target_id", c.getTargetId());
        row.put("parent_id", c.getParentId());
        row.put("visitor_id", c.getVisitorId() == null ? "" : c.getVisitorId());
        row.put("author_name", c.getAuthorName() == null ? "" : c.getAuthorName());
        row.put("author_email", c.getAuthorEmail() == null ? "" : c.getAuthorEmail());
        row.put("content", c.getContent());
        row.put("status", c.getStatus());
        row.put("created_at", c.getCreatedAt());
        mapper.insertComment(row);
        if (row.get("id") instanceof Number n) c.setId(n.longValue());
        return c;
    }

    @Override
    public Comment update(Long id, Map<String, Object> patch) {
        Map<String, Object> columns = SqlColumns.filter(patch, SqlColumns.COMMENT_COLUMNS);
        if (!columns.isEmpty()) {
            Map<String, Object> vals = new HashMap<>();
            mapper.updateComment(id, SqlColumns.setClause(columns, vals), vals);
        }
        return findById(id);
    }

    @Override
    public boolean delete(Long id) { return mapper.deleteComment(id) > 0; }

    @Override
    public long countByStatus(String status) { return mapper.countCommentsByStatus(status); }

    @Override
    public long countApproved(String targetType, Long targetId) {
        return mapper.countApprovedComments(targetType, targetId);
    }

    @Override
    public List<Comment> listByStatusSince(String status, String since, int limit) {
        return mapper.listCommentsByStatusSince(status, since, limit).stream().map(this::toComment).toList();
    }

    @Override
    public List<Comment> listRecentApproved(int limit) {
        return mapper.listRecentApprovedComments(limit).stream().map(this::toComment).toList();
    }
}
