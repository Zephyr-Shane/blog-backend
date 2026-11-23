package com.szenith.ai.domain.blog.service;

import com.szenith.ai.domain.blog.adapter.port.MediaReferencePort;
import com.szenith.ai.domain.blog.adapter.port.UploadPort;
import com.szenith.ai.domain.blog.model.valobj.MediaReference;
import com.szenith.ai.domain.blog.model.valobj.UploadedResource;
import com.szenith.ai.types.enums.ResponseCode;
import com.szenith.ai.types.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 媒体库应用服务：列表、引用查询、安全删除。
 * 删除必须以资源 UUID 反查内容引用，避免误删导致前台图片失效；
 * 引用扫描规则封装在 MediaReferencePort，领域层不感知具体表结构。
 */
@Service
@RequiredArgsConstructor
public class MediaLibraryService {

    private final UploadPort uploadPort;
    private final MediaReferencePort references;

    /** 媒体库列表：只返回元数据，不带二进制 */
    public List<Map<String, Object>> list(String category) {
        return uploadPort.list(category).stream()
                .map(UploadedResource::toMeta)
                .toList();
    }

    /** 列出引用该资源的内容，供前端删除前提示 */
    public List<Map<String, Object>> findReferences(String resourceId) {
        return references.findByResourceId(resourceId).stream()
                .map(MediaReference::toMap)
                .toList();
    }

    /** 安全删除：被引用则拒绝；不存在则 404 */
    @Transactional
    public void delete(String resourceId) {
        List<MediaReference> refs = references.findByResourceId(resourceId);
        if (!refs.isEmpty()) {
            String detail = refs.stream().limit(3)
                    .map(r -> r.getType() + "《" + r.getName() + "》·" + r.getField())
                    .collect(Collectors.joining("、"));
            throw new AppException(ResponseCode.CONFLICT,
                    "该资源仍被 " + refs.size() + " 处内容引用，无法删除：" + detail
                            + (refs.size() > 3 ? " 等" : ""));
        }
        if (!uploadPort.delete(resourceId)) {
            throw new AppException(ResponseCode.NOT_FOUND, "资源不存在");
        }
    }
}
