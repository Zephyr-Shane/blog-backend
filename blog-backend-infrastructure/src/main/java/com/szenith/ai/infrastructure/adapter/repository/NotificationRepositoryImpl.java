package com.szenith.ai.infrastructure.adapter.repository;

import com.szenith.ai.domain.blog.adapter.port.NotificationRepository;
import com.szenith.ai.domain.blog.model.entity.Notification;
import com.szenith.ai.infrastructure.dao.NotificationMapper;
import com.szenith.ai.infrastructure.util.SqlColumns;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationMapper mapper;

    private Notification toNotification(Map<String, Object> raw) {
        if (raw == null) return null;
        Map<String, Object> r = SqlColumns.row(raw);
        Object isRead = r.get("isRead");
        boolean read = Boolean.TRUE.equals(isRead) || Integer.valueOf(1).equals(isRead);
        return Notification.builder()
                .id(value(r.get("id")))
                .type(value(r.get("type")))
                .title(value(r.get("title")))
                .content(value(r.get("content")))
                .targetType(value(r.get("targetType")))
                .targetId(r.get("targetId") instanceof Number n ? n.longValue() : null)
                .read(read)
                .createdAt(value(r.get("createdAt")))
                .build();
    }

    @Override
    public Notification save(Notification n) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", n.getId());
        row.put("type", n.getType());
        row.put("title", n.getTitle());
        row.put("content", n.getContent());
        row.put("targetType", n.getTargetType());
        row.put("targetId", n.getTargetId());
        row.put("isRead", n.isRead() ? 1 : 0);
        row.put("createdAt", n.getCreatedAt());
        mapper.insert(row);
        return n;
    }

    @Override
    public List<Notification> list(int page, int pageSize, String status) {
        int offset = Math.max(0, (page - 1) * pageSize);
        return mapper.list(offset, pageSize, status).stream().map(this::toNotification).toList();
    }

    @Override
    public long countAll(String status) { return mapper.countAll(status); }

    @Override
    public long countUnread() { return mapper.countUnread(); }

    @Override
    public Notification findById(String id) { return toNotification(mapper.findById(id)); }

    @Override
    public boolean markRead(String id) { return mapper.markRead(id) > 0; }

    @Override
    public int markAllRead() { return mapper.markAllRead(); }

    private static String value(Object v) { return v == null ? null : v.toString(); }
}
