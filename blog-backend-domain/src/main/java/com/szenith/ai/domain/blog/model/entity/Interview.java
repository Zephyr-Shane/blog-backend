package com.szenith.ai.domain.blog.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Interview extends ContentItem {
    /** experience / question / prep */
    private String kind;
    private String title;
    private String content;
    private String company;
    private String position;
    /** pass / fail / ongoing */
    private String result;
    private String knowledgePoint;
    /** easy / medium / hard */
    private String difficulty;

    @Override
    public String type() { return "interview"; }
}
