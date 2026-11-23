package com.szenith.ai.infrastructure.adapter;

import com.szenith.ai.config.CommentModerationProperties;
import com.szenith.ai.domain.blog.adapter.port.ModerationRulesProvider;
import com.szenith.ai.domain.blog.model.valobj.ModerationRules;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModerationRulesConfigAdapter implements ModerationRulesProvider {

    private final CommentModerationProperties properties;

    @Override
    public ModerationRules rules() {
        return properties.toRules();
    }
}
