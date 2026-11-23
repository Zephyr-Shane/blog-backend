package com.szenith.ai.infrastructure.service;

import com.szenith.ai.domain.blog.adapter.port.MediaReferencePort;
import com.szenith.ai.domain.blog.model.valobj.MediaReference;
import com.szenith.ai.infrastructure.dao.MediaReferenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** 媒体引用查询适配器：把 Mapper 的只读扫描结果转换为领域对象 */
@Service
@RequiredArgsConstructor
public class MediaReferenceAdapter implements MediaReferencePort {

    private final MediaReferenceMapper mapper;

    @Override
    public List<MediaReference> findByResourceId(String resourceId) {
        if (resourceId == null || resourceId.isBlank()) return List.of();
        return mapper.selectByResourceId(resourceId.trim());
    }
}
