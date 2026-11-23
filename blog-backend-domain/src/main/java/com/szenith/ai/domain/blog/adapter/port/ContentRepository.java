package com.szenith.ai.domain.blog.adapter.port;

import com.szenith.ai.domain.blog.model.entity.ContentItem;

import java.util.List;

/**
 * 内容仓储端口：四类内容（post/moment/interview/link）的统一抽象。
 * 泛型参数 T 让领域服务无需感知具体 SQL 表，切存储不改领域代码。
 */
public interface ContentRepository<T extends ContentItem> {

    /** 已发布内容，按发布时间倒序 */
    List<T> listPublished();

    /** 全部内容（后台用），按创建时间倒序 */
    List<T> listAll();

    T findById(Long id);

    T save(T item);

    /** 部分更新：仅更新 payload 中出现的字段，避免清空未提交字段（P1-4） */
    T update(Long id, java.util.Map<String, Object> patch);

    boolean delete(Long id);

    long countAll();

    long countByStatus(String status);
}
