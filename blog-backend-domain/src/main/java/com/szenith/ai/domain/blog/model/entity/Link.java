package com.szenith.ai.domain.blog.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Link extends ContentItem {
    private String name;
    private String url;
    private String description;
    private String logo;
    /** entertainment / friend */
    private String category;
    private String group;

    @Override
    public String type() { return "link"; }
}
