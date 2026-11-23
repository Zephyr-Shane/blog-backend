package com.szenith.ai.domain.blog.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteConfig {
    private String siteName;
    private String subtitle;
    private String description;
    private String authorName;
    private String authorBio;
    private String avatar;
    /** 站点 logo 图片地址，用于导航栏与浏览器图标 */
    private String logo;
    private List<SocialLink> socialLinks;
    private String articleSignature;
    /** 面试栏目是否显示 */
    private boolean interviewEnabled;
    /** 卡片插槽配置（按 post/moment/interview/link 分组的 JSON 对象） */
    private Map<String, Object> cardSlots;
    /** 站点公告配置 JSON：title / content / updatedAt / visible */
    private Map<String, Object> announcement;
}
