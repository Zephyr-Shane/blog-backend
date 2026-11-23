package com.szenith.ai.domain.blog.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Moment extends ContentItem {
    private String content;
    private List<String> images;
    private String mood;
    private String location;

    @Override
    public String type() { return "moment"; }
}
