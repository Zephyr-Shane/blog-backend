package com.szenith.ai.domain.blog.model.entity;

import com.szenith.ai.domain.blog.model.valobj.CoAuthor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Post extends ContentItem {
    private String title;
    private String slug;
    private String excerpt;
    private String content;
    private String coverImage;
    private String category;
    private List<CoAuthor> coAuthors;

    @Override
    public String type() { return "post"; }
}
