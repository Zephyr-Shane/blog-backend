package com.szenith.ai.domain.blog.service;

import com.szenith.ai.domain.blog.adapter.port.NotificationRepository;
import com.szenith.ai.domain.blog.model.entity.Notification;
import com.szenith.ai.types.enums.ResponseCode;
import com.szenith.ai.types.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/** 通知查询与已读领域服务。通知的"落库"由基础设施层监听器负责。 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notifications;

    public Map<String, Object> list(int page, int pageSize, String status) {
        int p = Math.max(1, page);
        int size = Math.min(100, Math.max(1, pageSize));
        String filter = status == null || status.isBlank() ? "all" : status;
        List<Notification> items = notifications.list(p, size, filter);
        long total = notifications.countAll(filter);
        return Map.of(
                "list", items,
                "total", total,
                "page", p,
                "pageSize", size,
                "unread", notifications.countUnread()
        );
    }

    public Map<String, Object> unreadCount() {
        return Map.of("unread", notifications.countUnread());
    }

    public Notification markRead(String id) {
        Notification n = notifications.findById(id);
        if (n == null) throw new AppException(ResponseCode.NOT_FOUND, "通知不存在");
        notifications.markRead(id);
        n.setRead(true);
        return n;
    }

    public Map<String, Object> markAllRead() {
        int n = notifications.markAllRead();
        return Map.of("updated", n);
    }
}
