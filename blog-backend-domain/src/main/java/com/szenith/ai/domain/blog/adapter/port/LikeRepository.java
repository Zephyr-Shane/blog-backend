package com.szenith.ai.domain.blog.adapter.port;

import com.szenith.ai.domain.blog.model.entity.Like;

public interface LikeRepository {
    /** 按匿名访客+目标唯一查询 */
    Like find(String visitorId, String targetType, Long targetId);

    Like save(Like like);

    boolean delete(String visitorId, String targetType, Long targetId);

    long countByTarget(String targetType, Long targetId);
}
