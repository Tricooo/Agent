package com.tricoq.domain.agent.service.rag.rewrite.strategy.impl;

import com.tricoq.domain.agent.service.rag.rewrite.enums.RewritePolicy;
import com.tricoq.domain.agent.service.rag.rewrite.model.RewriteContext;
import com.tricoq.domain.agent.service.rag.rewrite.strategy.AbstractQueryRewriter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/15/26
 */
@Component
public class PassthroughQueryRewriter extends AbstractQueryRewriter {

    @Override
    protected RewritePolicy rewritePolicy() {
        return RewritePolicy.PASSTHROUGH;
    }

    @Override
    protected List<String> doRewrite(String userText, RewriteContext context) {
        return List.of();
    }

    @Override
    protected String noRewriteVariantReason(String userText) {
        return null;
    }
}
