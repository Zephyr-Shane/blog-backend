package com.szenith.ai.infrastructure.adapter.repository;

import com.szenith.ai.domain.blog.model.entity.Interview;
import com.szenith.ai.infrastructure.dao.ContentMapper;
import com.szenith.ai.infrastructure.dao.SupportMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository("interview")
public class InterviewRepository extends AbstractContentRepository<Interview> {

    public InterviewRepository(ContentMapper mapper, SupportMapper supportMapper) {
        super(mapper, supportMapper, "interview", InterviewRepository::assemble);
    }

    private static Interview assemble(Map<String, Object> r) {
        Interview i = new Interview();
        RowAssembler.fillCommon(i, r);
        i.setKind(RowAssembler.str(r.get("kind")));
        i.setTitle(RowAssembler.str(r.get("title")));
        i.setContent(RowAssembler.str(r.get("content")));
        i.setCompany(RowAssembler.str(r.get("company")));
        i.setPosition(RowAssembler.str(r.get("position")));
        i.setResult(RowAssembler.str(r.get("result")));
        i.setKnowledgePoint(RowAssembler.str(r.get("knowledgePoint")));
        i.setDifficulty(RowAssembler.str(r.get("difficulty")));
        i.setTags(RowAssembler.tags(r));
        return i;
    }

    @Override
    protected Map<String, Object> toRow(Interview i) {
        Map<String, Object> row = new HashMap<>();
        RowAssembler.fillCommonRow(i, row);
        row.put("kind", i.getKind());
        row.put("title", i.getTitle());
        row.put("content", i.getContent());
        row.put("company", i.getCompany());
        row.put("position", i.getPosition());
        row.put("result", i.getResult());
        row.put("knowledge_point", i.getKnowledgePoint());
        row.put("difficulty", i.getDifficulty());
        return row;
    }
}
