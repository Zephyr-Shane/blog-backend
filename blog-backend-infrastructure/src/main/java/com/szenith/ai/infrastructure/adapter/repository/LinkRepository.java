package com.szenith.ai.infrastructure.adapter.repository;

import com.szenith.ai.domain.blog.model.entity.Link;
import com.szenith.ai.infrastructure.dao.ContentMapper;
import com.szenith.ai.infrastructure.dao.SupportMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository("link")
public class LinkRepository extends AbstractContentRepository<Link> {

    public LinkRepository(ContentMapper mapper, SupportMapper supportMapper) {
        super(mapper, supportMapper, "link", LinkRepository::assemble);
    }

    private static Link assemble(Map<String, Object> r) {
        Link l = new Link();
        RowAssembler.fillCommon(l, r);
        l.setName(RowAssembler.str(r.get("name")));
        l.setUrl(RowAssembler.str(r.get("url")));
        l.setDescription(RowAssembler.str(r.get("description")));
        l.setLogo(RowAssembler.str(r.get("logo")));
        l.setCategory(RowAssembler.str(r.get("category")));
        l.setGroup(RowAssembler.str(r.get("groupName")));
        l.setTags(RowAssembler.tags(r));
        return l;
    }

    @Override
    protected Map<String, Object> toRow(Link l) {
        Map<String, Object> row = new HashMap<>();
        RowAssembler.fillCommonRow(l, row);
        row.put("name", l.getName());
        row.put("url", l.getUrl());
        row.put("description", l.getDescription());
        row.put("logo", l.getLogo());
        row.put("category", l.getCategory());
        row.put("group_name", l.getGroup());
        return row;
    }
}
