package com.szenith.ai.infrastructure.adapter.repository;

import com.szenith.ai.domain.blog.adapter.port.LikeRepository;
import com.szenith.ai.domain.blog.model.entity.Like;
import com.szenith.ai.infrastructure.dao.SupportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class LikeRepositoryImpl implements LikeRepository {

    private final SupportMapper mapper;

    @Override
    public Like find(String visitorId, String targetType, Long targetId) {
        Map<String, Object> r = mapper.findLike(visitorId, targetType, targetId);
        if (r == null) return null;
        return Like.builder()
                .id(RowAssembler.n(r.get("id")))
                .visitorId(RowAssembler.str(r.get("visitorId")))
                .targetType(RowAssembler.str(r.get("targetType")))
                .targetId(RowAssembler.n(r.get("targetId")))
                .createdAt(RowAssembler.str(r.get("createdAt")))
                .build();
    }

    @Override
    public Like save(Like like) {
        Map<String, Object> row = new HashMap<>();
        row.put("visitor_id", like.getVisitorId());
        row.put("target_type", like.getTargetType());
        row.put("target_id", like.getTargetId());
        row.put("created_at", like.getCreatedAt());
        mapper.insertLike(row);
        if (row.get("id") instanceof Number n) like.setId(n.longValue());
        return like;
    }

    @Override
    public boolean delete(String visitorId, String targetType, Long targetId) {
        return mapper.deleteLike(visitorId, targetType, targetId) > 0;
    }

    @Override
    public long countByTarget(String targetType, Long targetId) {
        return mapper.countLikesByTarget(targetType, targetId);
    }
}
