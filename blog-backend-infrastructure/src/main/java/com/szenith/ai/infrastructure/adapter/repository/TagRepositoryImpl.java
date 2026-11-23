package com.szenith.ai.infrastructure.adapter.repository;

import com.szenith.ai.domain.blog.adapter.port.TagRepository;
import com.szenith.ai.domain.blog.model.valobj.Tag;
import com.szenith.ai.infrastructure.dao.SupportMapper;
import com.szenith.ai.infrastructure.util.SqlColumns;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class TagRepositoryImpl implements TagRepository {

    private final SupportMapper mapper;

    private Tag toTag(Map<String, Object> r) {
        if (r == null) return null;
        Map<String, Object> row = SqlColumns.row(r);
        return Tag.builder()
                .id(RowAssembler.n(row.get("id")))
                .name(RowAssembler.str(row.get("name")))
                .slug(RowAssembler.str(row.get("slug")))
                .category(RowAssembler.str(row.get("category")))
                .build();
    }

    @Override
    public List<Tag> listAll() {
        return mapper.listTags().stream().map(this::toTag).toList();
    }

    @Override
    public Tag findById(Long id) { return toTag(mapper.findTagById(id)); }

    @Override
    public Tag findBySlug(String slug) { return toTag(mapper.findTagBySlug(slug)); }

    @Override
    public Tag save(Tag tag) {
        Map<String, Object> row = new LinkedHashMap<>();
        if (tag.getId() != null) row.put("id", tag.getId());
        row.put("name", tag.getName());
        row.put("slug", tag.getSlug());
        row.put("category", tag.getCategory());
        mapper.insertTag(row);
        if (row.get("id") instanceof Number n) tag.setId(n.longValue());
        return tag;
    }

    @Override
    public Tag update(Long id, Map<String, Object> patch) {
        Map<String, Object> columns = SqlColumns.filter(patch, SqlColumns.TAG_COLUMNS);
        if (!columns.isEmpty()) {
            Map<String, Object> vals = new HashMap<>();
            mapper.updateTag(id, SqlColumns.setClause(columns, vals), vals);
        }
        return findById(id);
    }

    @Override
    public boolean delete(Long id) { return mapper.deleteTag(id) > 0; }

    @Override
    public Map<Long, List<Tag>> findTagsByTargets(String targetType, List<Long> targetIds) {
        Map<Long, List<Tag>> grouped = new HashMap<>();
        for (Map<String, Object> r : mapper.findTagsByTargets(targetType, targetIds)) {
            Long tid = RowAssembler.n(r.get("targetId"));
            if (tid == null) continue;
            grouped.computeIfAbsent(tid, k -> new ArrayList<>()).add(
                    Tag.builder()
                            .id(RowAssembler.n(r.get("id")))
                            .name(RowAssembler.str(r.get("name")))
                            .slug(RowAssembler.str(r.get("slug")))
                            .category(RowAssembler.str(r.get("category")))
                            .build());
        }
        return grouped;
    }
}
