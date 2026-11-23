package com.szenith.ai.infrastructure.adapter.repository;

import com.szenith.ai.domain.blog.model.entity.Moment;
import com.szenith.ai.infrastructure.dao.ContentMapper;
import com.szenith.ai.infrastructure.dao.SupportMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository("moment")
public class MomentRepository extends AbstractContentRepository<Moment> {

    public MomentRepository(ContentMapper mapper, SupportMapper supportMapper) {
        super(mapper, supportMapper, "moment", MomentRepository::assemble);
    }

    private static Moment assemble(Map<String, Object> r) {
        Moment m = new Moment();
        RowAssembler.fillCommon(m, r);
        m.setContent(RowAssembler.str(r.get("content")));
        m.setImages(RowAssembler.strings(r, "images"));
        m.setMood(RowAssembler.str(r.get("mood")));
        m.setLocation(RowAssembler.str(r.get("location")));
        m.setTags(RowAssembler.tags(r));
        return m;
    }

    @Override
    protected Map<String, Object> toRow(Moment m) {
        Map<String, Object> row = new HashMap<>();
        RowAssembler.fillCommonRow(m, row);
        row.put("content", m.getContent());
        row.put("images", RowAssembler.json(m.getImages()));
        row.put("mood", m.getMood());
        row.put("location", m.getLocation());
        return row;
    }
}
