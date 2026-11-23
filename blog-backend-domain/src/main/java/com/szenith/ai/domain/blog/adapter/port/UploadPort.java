package com.szenith.ai.domain.blog.adapter.port;

import com.szenith.ai.domain.blog.model.valobj.UploadedResource;

import java.util.List;
import java.util.Optional;

/**
 * 上传资源端口：领域定义契约，基础设施实现持久化细节（落库）。
 * 资源 id 为应用生成的 UUID（非自增），既是公开标识也是引用判断依据。
 * category 区分上传形式：avatar/cover/content/logo/gallery/other。
 */
public interface UploadPort {
    /** 保存上传资源，返回含可访问 URL 的领域对象 */
    UploadedResource save(String category, String originalName, String mimeType, long size, byte[] bytes);

    /** 按 UUID 读取（含二进制），供资源直出接口使用 */
    Optional<UploadedResource> findById(String id);

    /** 媒体库列表：category 为空则全部；返回不含二进制的元数据由调用方 toMeta() */
    List<UploadedResource> list(String category);

    /** 按 UUID 删除上传资源 */
    boolean delete(String id);
}
