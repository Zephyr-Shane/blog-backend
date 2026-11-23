package com.szenith.ai.infrastructure.adapter.repository;

import com.szenith.ai.domain.blog.adapter.port.ContentRepository;
import com.szenith.ai.domain.blog.model.entity.ContentItem;
import com.szenith.ai.domain.blog.model.valobj.Tag;
import com.szenith.ai.infrastructure.dao.ContentMapper;
import com.szenith.ai.infrastructure.dao.SupportMapper;
import com.szenith.ai.infrastructure.util.SqlColumns;

import java.util.*;
import java.util.function.Function;

/**
 * 内容仓储抽象实现：泛型基类承载全部 CRUD 与标签批量装配，
 * 子类只提供类型标识与 Map→实体装配器。四类内容共享一套逻辑。
 */
public abstract class AbstractContentRepository<T extends ContentItem> implements ContentRepository<T> {

    protected final ContentMapper mapper;
    protected final SupportMapper supportMapper;
    private final String type;
    private final Function<Map<String, Object>, T> assembler;

    protected AbstractContentRepository(ContentMapper mapper, SupportMapper supportMapper,
                                        String type, Function<Map<String, Object>, T> assembler) {
        this.mapper = mapper;
        this.supportMapper = supportMapper;
        this.type = type;
        this.assembler = assembler;
    }

    private String table() { return SqlColumns.table(type); }

    private T toEntity(Map<String, Object> row) {
        return row == null ? null : assembler.apply(SqlColumns.row(row));
    }

    /** 批量装配标签：一次查询取回所有目标标签，避免 N+1 */
    private void attachTags(List<T> items) {
        if (items.isEmpty()) return;
        List<Long> ids = items.stream().map(ContentItem::getId).filter(Objects::nonNull).toList();
        if (ids.isEmpty()) return;
        Map<Long, List<Tag>> grouped = new HashMap<>();
        for (Map<String, Object> r : supportMapper.findTagsByTargets(type, ids)) {
            Long targetId = RowAssembler.n(r.get("targetId"));
            if (targetId == null) continue;
            Tag tag = Tag.builder()
                    .id(RowAssembler.n(r.get("id")))
                    .name(RowAssembler.str(r.get("name")))
                    .slug(RowAssembler.str(r.get("slug")))
                    .category(RowAssembler.str(r.get("category")))
                    .build();
            grouped.computeIfAbsent(targetId, k -> new ArrayList<>()).add(tag);
        }
        for (T item : items) {
            item.setTags(grouped.getOrDefault(item.getId(), List.of()));
        }
    }

    @Override
    public List<T> listPublished() {
        List<T> list = mapper.listPublished(table()).stream().map(this::toEntity).toList();
        attachTags(list);
        return list;
    }

    @Override
    public List<T> listAll() {
        List<T> list = mapper.listAll(table()).stream().map(this::toEntity).toList();
        attachTags(list);
        return list;
    }

    @Override
    public T findById(Long id) {
        T item = toEntity(mapper.findById(table(), id));
        if (item != null) attachTags(List.of(item));
        return item;
    }

    @Override
    public T save(T item) {
        Map<String, Object> row = toRow(item);
        sanitizeContent(row);
        mapper.insert(table(), row);
        Object generatedId = row.get("id");
        if (generatedId instanceof Number n) item.setId(n.longValue());
        return item;
    }

    protected abstract Map<String, Object> toRow(T item);

    @Override
    public T update(Long id, Map<String, Object> patch) {
        Map<String, Object> columns = SqlColumns.filter(patch, SqlColumns.CONTENT_COLUMNS.get(type));
        if (columns.isEmpty()) return findById(id);
        sanitizeContent(columns);
        Map<String, Object> vals = new HashMap<>();
        String sets = SqlColumns.setClause(columns, vals);
        mapper.update(table(), id, sets, vals);
        return findById(id);
    }

    /** 正文入库前消毒（评审 P1-3） */
    private void sanitizeContent(Map<String, Object> row) {
        Object content = row.get("content");
        if (content != null) {
            row.put("content", com.szenith.ai.infrastructure.util.HtmlSanitizer.clean(content.toString()));
        }
    }

    @Override
    public boolean delete(Long id) {
        return mapper.delete(table(), id) > 0;
    }

    @Override
    public long countAll() { return mapper.countAll(table()); }

    @Override
    public long countByStatus(String status) { return mapper.countByStatus(table(), status); }
}
