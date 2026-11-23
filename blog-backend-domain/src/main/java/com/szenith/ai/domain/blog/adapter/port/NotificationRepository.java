package com.szenith.ai.domain.blog.adapter.port;

import com.szenith.ai.domain.blog.model.entity.Notification;

import java.util.List;

public interface NotificationRepository {
    Notification save(Notification notification);

    /** 分页：按 created_at 倒序；status 支持 all / unread / read */
    List<Notification> list(int page, int pageSize, String status);

    /** 按状态统计总数 */
    long countAll(String status);

    long countUnread();

    Notification findById(String id);

    boolean markRead(String id);

    int markAllRead();
}
