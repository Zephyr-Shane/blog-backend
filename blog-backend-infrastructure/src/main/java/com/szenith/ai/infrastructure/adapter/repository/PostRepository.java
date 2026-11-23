package com.szenith.ai.infrastructure.adapter.repository;

import com.szenith.ai.domain.blog.model.entity.Post;
import com.szenith.ai.infrastructure.dao.ContentMapper;
import com.szenith.ai.infrastructure.dao.SupportMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository("post")
public class PostRepository extends AbstractContentRepository<Post> {

    public PostRepository(ContentMapper mapper, SupportMapper supportMapper) {
        super(mapper, supportMapper, "post", PostRepository::assemble);
    }

    private static Post assemble(Map<String, Object> r) {
        Post p = new Post();
        RowAssembler.fillCommon(p, r);
        p.setTitle(RowAssembler.str(r.get("title")));
        p.setSlug(RowAssembler.str(r.get("slug")));
        p.setExcerpt(RowAssembler.str(r.get("excerpt")));
        p.setContent(RowAssembler.str(r.get("content")));
        p.setCoverImage(RowAssembler.str(r.get("coverImage")));
        p.setCategory(RowAssembler.str(r.get("category")));
        p.setCoAuthors(RowAssembler.coAuthors(r));
        p.setTags(RowAssembler.tags(r));
        return p;
    }

    @Override
    protected Map<String, Object> toRow(Post p) {
        Map<String, Object> row = new HashMap<>();
        RowAssembler.fillCommonRow(p, row);
        row.put("title", p.getTitle());
        row.put("slug", p.getSlug());
        row.put("excerpt", p.getExcerpt());
        row.put("content", p.getContent());
        row.put("cover_image", p.getCoverImage());
        row.put("category", p.getCategory());
        row.put("co_authors", RowAssembler.json(p.getCoAuthors()));
        return row;
    }
}
