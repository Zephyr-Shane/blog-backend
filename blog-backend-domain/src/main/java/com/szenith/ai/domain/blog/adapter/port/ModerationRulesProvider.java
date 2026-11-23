package com.szenith.ai.domain.blog.adapter.port;

import com.szenith.ai.domain.blog.model.valobj.ModerationRules;

/** 评论校验规则来源，由基础设施层从配置读取 */
public interface ModerationRulesProvider {
    ModerationRules rules();
}
