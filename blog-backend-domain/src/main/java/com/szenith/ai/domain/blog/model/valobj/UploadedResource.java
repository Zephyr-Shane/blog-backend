package com.szenith.ai.domain.blog.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上传资源领域模型。二进制本体（bytes）仅为传输需要，通常不参与序列化输出。
 * category 区分上传形式，便于媒体库分类管理与生命周期差异处理。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadedResource {
    /** 公开唯一 ID（UUID），用于资源直出 URL 与引用判断，不使用数据库自增序号 */
    private String id;
    /** 上传形式：avatar/cover/content/logo/gallery/other */
    private String category;
    /** 原始文件名 */
    private String originalName;
    /** 存储文件名（UUID） */
    private String storedName;
    private String mimeType;
    private long size;
    /** 二进制本体 */
    private byte[] data;
    /** 可直接访问的资源 URL（由基础设施按 public-url 生成） */
    private String url;
    private String createdAt;

    /** 媒体库展示用的轻量视图：剔除二进制，避免列表接口把字节吐出去 */
    public java.util.Map<String, Object> toMeta() {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", id);
        m.put("category", category);
        m.put("fileName", originalName);
        m.put("mimeType", mimeType);
        m.put("size", size);
        m.put("url", url);
        m.put("createdAt", createdAt);
        return m;
    }
}
