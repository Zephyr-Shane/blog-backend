package com.szenith.ai.domain.blog.service;

import com.szenith.ai.domain.blog.adapter.port.*;
import com.szenith.ai.domain.blog.model.entity.*;
import com.szenith.ai.domain.blog.model.valobj.DomainEnums;
import com.szenith.ai.types.enums.ResponseCode;
import com.szenith.ai.types.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 后台通用资源服务：以策略映射统一 7 种资源的 CRUD。
 * 每类资源实现 ResourceOps，服务本身只做分发与校验，杜绝 7 套重复样板代码。
 */
@Service
@RequiredArgsConstructor
public class AdminResourceService {

    private final Map<String, ContentRepository<? extends ContentItem>> contentRepos;
    private final TagRepository tags;
    private final CommentRepository comments;
    private final SupportRepository support;

    /** 资源操作策略：屏蔽各仓储的泛型差异 */
    private interface ResourceOps {
        List<?> list();
        Object get(Long id);
        Object create(Map<String, Object> body);
        Object update(Long id, Map<String, Object> body);
        boolean delete(Long id);
    }

    private Map<String, ResourceOps> ops() {
        Map<String, ResourceOps> m = new LinkedHashMap<>();
        for (Map.Entry<String, ContentRepository<? extends ContentItem>> e : contentRepos.entrySet()) {
            ContentRepository<ContentItem> repo = (ContentRepository<ContentItem>) e.getValue();
            String type = e.getKey();
            m.put(type, new ResourceOps() {
                public List<?> list() { return repo.listAll(); }
                public Object get(Long id) { return require(repo.findById(id)); }
                public Object create(Map<String, Object> b) { return repo.save(ContentFactory.create(type, b)); }
                public Object update(Long id, Map<String, Object> b) { return require(repo.update(id, b)); }
                public boolean delete(Long id) { return repo.delete(id); }
            });
        }
        m.put("tag", new ResourceOps() {
            public List<?> list() { return tags.listAll(); }
            public Object get(Long id) { return require(tags.findById(id)); }
            public Object create(Map<String, Object> b) { return tags.save(ContentFactory.tag(b)); }
            public Object update(Long id, Map<String, Object> b) { return require(tags.update(id, b)); }
            public boolean delete(Long id) { return tags.delete(id); }
        });
        m.put("comment", new ResourceOps() {
            public List<?> list() { return comments.listAll(); }
            public Object get(Long id) { return require(comments.findById(id)); }
            public Object create(Map<String, Object> b) {
                Comment c = comments.save(ContentFactory.comment(b));
                recount(c);
                return c;
            }
            public Object update(Long id, Map<String, Object> b) {
                Comment c = comments.update(id, b);
                recount(c);
                return require(c);
            }
            public boolean delete(Long id) {
                Comment c = comments.findById(id);
                boolean done = comments.delete(id);
                if (done) recount(c);
                return done;
            }
        });
        m.put("user", new ResourceOps() {
            public List<?> list() { return support.listUsers(); }
            public Object get(Long id) { return require(support.findUserById(id)); }
            public Object create(Map<String, Object> b) { return support.saveUser(ContentFactory.user(b)); }
            public Object update(Long id, Map<String, Object> b) { return require(support.updateUser(id, b)); }
            public boolean delete(Long id) { return support.deleteUser(id); }
        });
        return m;
    }

    public List<?> list(String resource) {
        return strategy(resource).list();
    }

    public Object get(String resource, Long id) {
        return strategy(resource).get(id);
    }

    public Object create(String resource, Map<String, Object> body) {
        return strategy(resource).create(body);
    }

    /** PUT 语义为部分更新：只改 body 中出现的字段 */
    public Object update(String resource, Long id, Map<String, Object> body) {
        return strategy(resource).update(id, body);
    }

    public void delete(String resource, Long id) {
        if (!strategy(resource).delete(id)) throw notFound();
    }

    /** 评论审核：支持隐藏后的恢复（approved）与垃圾/隐藏标记 */
    public Object reviewComment(Long id, String status) {
        if (!"approved".equals(status) && !"spam".equals(status) && !"hidden".equals(status)) {
            throw new AppException(ResponseCode.BAD_REQUEST, "status 只能是 approved、spam 或 hidden");
        }
        Comment c = comments.update(id, Map.of("status", status));
        if (c == null) throw notFound();
        recount(c);
        return c;
    }

    /** 评论状态变化后同步内容表冗余计数 */
    private void recount(Comment c) {
        if (c == null || c.getTargetId() == null) return;
        ContentRepository<? extends ContentItem> repo = contentRepos.get(c.getTargetType());
        if (repo == null) return;
        repo.update(c.getTargetId(), Map.of("commentCount", comments.countApproved(c.getTargetType(), c.getTargetId())));
    }

    public Object getSiteConfig() {
        return support.getSiteConfig();
    }

    public Object updateSiteConfig(Map<String, Object> patch) {
        return support.updateSiteConfig(patch);
    }

    public Map<String, Object> stats() {
        com.szenith.ai.domain.blog.model.valobj.AdminStats s = new com.szenith.ai.domain.blog.model.valobj.AdminStats();
        s.setTotalContents(contentRepos.values().stream().mapToLong(ContentRepository::countAll).sum());
        s.setPublished(contentRepos.values().stream()
                .mapToLong(r -> r.countByStatus(DomainEnums.Status.PUBLISHED.value())).sum());
        s.setDrafts(contentRepos.values().stream()
                .mapToLong(r -> r.countByStatus(DomainEnums.Status.DRAFT.value())).sum());
        s.setPendingComments(comments.countByStatus(DomainEnums.CommentStatus.PENDING.value()));
        return s.toMap();
    }

    private ResourceOps strategy(String resource) {
        ResourceOps op = ops().get(resource);
        if (op == null) throw new AppException(ResponseCode.BAD_REQUEST, "未知资源: " + resource);
        return op;
    }

    private static Object require(Object v) {
        return Optional.ofNullable(v).orElseThrow(() -> new AppException(ResponseCode.NOT_FOUND, "资源不存在"));
    }

    private AppException notFound() {
        return new AppException(ResponseCode.NOT_FOUND, "资源不存在");
    }
}
