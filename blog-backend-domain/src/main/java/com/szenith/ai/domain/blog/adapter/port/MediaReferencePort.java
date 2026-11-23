package com.szenith.ai.domain.blog.adapter.port;

import com.szenith.ai.domain.blog.model.valobj.MediaReference;

import java.util.List;

/**
 * 媒体引用查询端口：领域只关心"资源 id 被哪些内容引用"，
 * 具体扫哪些表、哪些字段由基础设施实现，新增内容类型时只改适配器/SQL。
 */
public interface MediaReferencePort {
    List<MediaReference> findByResourceId(String resourceId);
}
