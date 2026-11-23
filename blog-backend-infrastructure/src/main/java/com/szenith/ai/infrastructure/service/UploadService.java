package com.szenith.ai.infrastructure.service;

import com.szenith.ai.domain.blog.adapter.port.UploadPort;
import com.szenith.ai.domain.blog.model.valobj.UploadedResource;
import com.szenith.ai.infrastructure.dao.UploadMapper;
import com.szenith.ai.types.enums.ResponseCode;
import com.szenith.ai.types.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * 上传资源服务：二进制统一落库（blog_upload.data），不再写文件系统。
 * 前端项目自带的静态资源（/images/*.svg）属于前端代码库，不经由此处。
 */
@Service
@RequiredArgsConstructor
public class UploadService implements UploadPort {

    private static final Set<String> ALLOWED_MIME = Set.of(
            "image/png", "image/jpeg", "image/webp", "image/gif", "image/svg+xml");
    private static final Set<String> ALLOWED_CATEGORY = Set.of(
            "avatar", "cover", "content", "logo", "gallery", "other");
    private static final String DEFAULT_CATEGORY = "other";

    private final UploadMapper mapper;

    @Value("${blog.upload.public-url}")
    private String publicUrl;

    @Value("${blog.upload.max-size-mb}")
    private long maxMb;

    @Override
    public UploadedResource save(String category, String originalName, String mimeType, long size, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new AppException(ResponseCode.BAD_REQUEST, "文件不能为空");
        }
        if (mimeType == null || !ALLOWED_MIME.contains(mimeType)) {
            throw new AppException(ResponseCode.BAD_REQUEST, "仅允许 png/jpg/jpeg/webp/gif/svg 图片");
        }
        if (size > maxMb * 1024 * 1024) {
            throw new AppException(ResponseCode.BAD_REQUEST, "文件超过 " + maxMb + "MB 限制");
        }
        String cat = (category == null || category.isBlank())
                ? DEFAULT_CATEGORY : category.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_CATEGORY.contains(cat)) {
            throw new AppException(ResponseCode.BAD_REQUEST, "未知上传形式: " + category
                    + "，允许 " + ALLOWED_CATEGORY);
        }

        // 资源 id 使用应用生成的 UUID：不暴露自增序号，跨环境/跨库不会冲突，
        // 用户内容以 /media/{uuid} 引用，媒体库删除的引用判断也以此 id 为准。
        String resourceId = UUID.randomUUID().toString();
        String storedName = resourceId.replace("-", "") + "." + extensionOf(mimeType, originalName);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", resourceId);
        row.put("category", cat);
        row.put("originalName", originalName == null ? storedName : originalName);
        row.put("storedName", storedName);
        row.put("mimeType", mimeType);
        row.put("size", bytes.length);
        row.put("data", bytes);
        row.put("createdAt", Instant.now().toString());
        mapper.insert(row);

        return UploadedResource.builder()
                .id(resourceId)
                .category(cat)
                .originalName(String.valueOf(row.get("originalName")))
                .storedName(storedName)
                .mimeType(mimeType)
                .size(bytes.length)
                .url(urlOf(resourceId))
                .createdAt(String.valueOf(row.get("createdAt")))
                .build();
    }

    @Override
    public Optional<UploadedResource> findById(String id) {
        UploadedResource r = mapper.selectById(id);
        if (r == null) return Optional.empty();
        r.setUrl(urlOf(r.getId()));
        return Optional.of(r);
    }

    @Override
    public List<UploadedResource> list(String category) {
        String cat = (category == null || category.isBlank()) ? null : category.trim().toLowerCase(Locale.ROOT);
        List<UploadedResource> rows = mapper.selectList(cat);
        rows.forEach(r -> r.setUrl(urlOf(r.getId())));
        return rows;
    }

    @Override
    public boolean delete(String id) {
        return mapper.deleteById(id) > 0;
    }

    private String urlOf(String id) {
        String base = publicUrl == null ? "" : publicUrl.replaceAll("/+$", "");
        return base + "/" + id;
    }

    private String extensionOf(String mime, String original) {
        return switch (mime) {
            case "image/png" -> "png";
            case "image/jpeg" -> "jpg";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            case "image/svg+xml" -> "svg";
            default -> {
                String n = original == null ? "" : original;
                yield n.contains(".") ? n.substring(n.lastIndexOf('.') + 1) : "img";
            }
        };
    }
}
