package com.szenith.ai.domain.blog.model.valobj;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/** 仪表盘统计 */
@Data
public class AdminStats {
    private long totalContents;
    private long published;
    private long drafts;
    private long pendingComments;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("totalContents", totalContents);
        map.put("published", published);
        map.put("drafts", drafts);
        map.put("pendingComments", pendingComments);
        return map;
    }
}
