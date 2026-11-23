package com.szenith.ai.domain.blog.adapter.port;

import com.szenith.ai.domain.blog.model.valobj.Tag;

import java.util.List;
import java.util.Map;

public interface TagRepository {
    List<Tag> listAll();

    Tag findById(Long id);

    Tag findBySlug(String slug);

    Tag save(Tag tag);

    Tag update(Long id, Map<String, Object> patch);

    boolean delete(Long id);

    /** 取某类内容关联的标签，批量装配用 */
    Map<Long, List<Tag>> findTagsByTargets(String targetType, List<Long> targetIds);
}
