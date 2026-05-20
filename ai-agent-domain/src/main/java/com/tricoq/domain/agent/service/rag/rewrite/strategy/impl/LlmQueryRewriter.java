package com.tricoq.domain.agent.service.rag.rewrite.strategy.impl;

import com.tricoq.domain.agent.service.rag.rewrite.enums.RewritePolicy;
import com.tricoq.domain.agent.service.rag.rewrite.strategy.AbstractQueryRewriter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/19/26
 */
@Component
public class LlmQueryRewriter extends AbstractQueryRewriter {

    @Override
    protected RewritePolicy rewritePolicy() {
        return RewritePolicy.LLM_MULTI_QUERY;
    }

    @Override
    protected List<String> doRewrite(String userText) {
        // TODO Step 6.7: 调轻量模型生成检索 query，当前只保留策略骨架。
        return List.of();
    }

    @Override
    protected String noRewriteVariantReason(String userText) {
        return "LLM_REWRITE_NOT_IMPLEMENTED";
    }
}
