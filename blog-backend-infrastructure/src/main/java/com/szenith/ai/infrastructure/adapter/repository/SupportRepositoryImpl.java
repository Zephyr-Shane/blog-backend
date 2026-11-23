package com.szenith.ai.infrastructure.adapter.repository;

import com.szenith.ai.domain.blog.adapter.port.SupportRepository;
import com.szenith.ai.domain.blog.model.entity.User;
import com.szenith.ai.domain.blog.model.valobj.SiteConfig;
import com.szenith.ai.domain.blog.model.valobj.SocialLink;
import com.szenith.ai.domain.blog.service.ContentFactory;
import com.szenith.ai.infrastructure.dao.SupportMapper;
import com.szenith.ai.infrastructure.util.SqlColumns;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class SupportRepositoryImpl implements SupportRepository {

    private final SupportMapper mapper;

    @Override
    public SiteConfig getSiteConfig() {
        Map<String, Object> raw = SqlColumns.row(mapper.getSiteConfig());
        if (raw == null || raw.isEmpty()) return null;
        SiteConfig cfg = new SiteConfig();
        cfg.setSiteName(RowAssembler.str(raw.get("siteName")));
        cfg.setSubtitle(RowAssembler.str(raw.get("subtitle")));
        cfg.setDescription(RowAssembler.str(raw.get("description")));
        cfg.setAuthorName(RowAssembler.str(raw.get("authorName")));
        cfg.setAuthorBio(RowAssembler.str(raw.get("authorBio")));
        cfg.setAvatar(RowAssembler.str(raw.get("avatar")));
        cfg.setLogo(RowAssembler.str(raw.get("logo")));
        Object social = raw.get("socialLinks");
        List<SocialLink> links = new ArrayList<>();
        if (social != null) {
            try {
                List<SocialLink> parsed = JSON.parseObject(social.toString(), new TypeReference<List<SocialLink>>() {});
                if (parsed != null) links.addAll(parsed);
            } catch (Exception ignored) {}
        }
        cfg.setSocialLinks(links);
        cfg.setArticleSignature(RowAssembler.str(raw.get("articleSignature")));
        Object enabled = raw.get("interviewEnabled");
        cfg.setInterviewEnabled(enabled == null || Boolean.TRUE.equals(enabled)
                || Integer.valueOf(1).equals(enabled));
        Object slots = raw.get("cardSlots");
        if (slots != null) {
            try {
                Map<String, Object> parsed = JSON.parseObject(slots.toString(), new TypeReference<Map<String, Object>>() {});
                cfg.setCardSlots(parsed);
            } catch (Exception ignored) {}
        }
        Object announcement = raw.get("announcement");
        if (announcement != null) {
            try {
                Map<String, Object> parsed = JSON.parseObject(announcement.toString(), new TypeReference<Map<String, Object>>() {});
                cfg.setAnnouncement(parsed);
            } catch (Exception ignored) {}
        }
        return cfg;
    }

    @Override
    public SiteConfig updateSiteConfig(Map<String, Object> patch) {
        // 将嵌套 socialLinks 序列化为 JSON 字符串
        Map<String, Object> normalized = new HashMap<>(patch);
        if (normalized.containsKey("socialLinks") && !(normalized.get("socialLinks") instanceof String)) {
            normalized.put("socialLinks", JSON.toJSONString(normalized.get("socialLinks")));
        }
        Map<String, Object> columns = SqlColumns.filter(normalized, SqlColumns.CONFIG_COLUMNS);
        if (!columns.isEmpty()) {
            Map<String, Object> vals = new HashMap<>();
            mapper.updateSiteConfig(SqlColumns.setClause(columns, vals), vals);
        }
        return getSiteConfig();
    }

    @Override
    public List<User> listUsers() {
        return mapper.listUsers().stream().map(this::toUser).toList();
    }

    @Override
    public User findUserById(Long id) { return toUser(mapper.findUserById(id)); }

    @Override
    public User findUserByEmail(String email) { return toUser(mapper.findUserByEmail(email)); }

    private User toUser(Map<String, Object> raw) {
        if (raw == null) return null;
        Map<String, Object> r = SqlColumns.row(raw);
        return User.builder()
                .id(RowAssembler.n(r.get("id")))
                .email(RowAssembler.str(r.get("email")))
                .name(RowAssembler.str(r.get("name")))
                .role(RowAssembler.str(r.get("role")))
                .passwordHash(RowAssembler.str(r.get("passwordHash")))
                .build();
    }

    @Override
    public User saveUser(User user) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("email", user.getEmail());
        row.put("name", user.getName());
        row.put("role", user.getRole());
        row.put("password_hash", user.getPasswordHash());
        mapper.insertUser(row);
        if (row.get("id") instanceof Number n) user.setId(n.longValue());
        return user;
    }

    @Override
    public User updateUser(Long id, Map<String, Object> patch) {
        Map<String, Object> body = new HashMap<>(patch);
        if (body.containsKey("password") && !body.containsKey("passwordHash")) {
            body.put("passwordHash", sha256(String.valueOf(body.get("password"))));
        }
        Map<String, Object> columns = SqlColumns.filter(body, SqlColumns.USER_COLUMNS);
        if (!columns.isEmpty()) {
            Map<String, Object> vals = new HashMap<>();
            mapper.updateUser(id, SqlColumns.setClause(columns, vals), vals);
        }
        return findUserById(id);
    }

    @Override
    public boolean deleteUser(Long id) { return mapper.deleteUser(id) > 0; }

    private static String sha256(String s) {
        try {
            return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                    .digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
