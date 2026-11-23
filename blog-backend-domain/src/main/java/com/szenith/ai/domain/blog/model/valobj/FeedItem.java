package com.szenith.ai.domain.blog.model.valobj;

import com.szenith.ai.domain.blog.model.entity.ContentItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 首页混排时间线条目 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedItem {
    private String type;
    private ContentItem data;
    private String publishedAt;
}
