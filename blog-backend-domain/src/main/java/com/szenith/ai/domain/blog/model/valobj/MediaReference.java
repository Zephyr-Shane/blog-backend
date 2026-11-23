package com.szenith.ai.domain.blog.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 媒体资源引用描述：说明某个上传资源被哪条内容的哪个字段引用。
 * 用于删除前的安全校验，让用户明确知道"为什么删不掉"。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaReference {
    /** 内容类型：post / moment / interview / link / site-config */
    private String type;
    /** 内容标题或摘要（用于展示） */
    private String name;
    /** 引用字段：coverImage / content / images / logo / avatar 等 */
    private String field;

    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", type);
        m.put("name", name);
        m.put("field", field);
        return m;
    }
}
