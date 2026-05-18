package com.tricoq.domain.agent.service.rag.rewrite.factory;

import com.tricoq.domain.agent.service.rag.rewrite.QueryRewriter;
import com.tricoq.domain.agent.service.rag.rewrite.enums.RewritePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/14/26
 */
@Service
@RequiredArgsConstructor
public class QueryRewriterFactory {

    private final Map<String, QueryRewriter> queryRewriters;

    public QueryRewriter getQueryRewriter(String policy) {
        RewritePolicy rewritePolicy = RewritePolicy.getByPolicyOrDefault(policy);
        QueryRewriter rewriter = queryRewriters.get(rewritePolicy.getBeanName());
        if (rewriter != null) {
            return rewriter;
        }

        QueryRewriter fallback = queryRewriters.get(RewritePolicy.PASSTHROUGH.getBeanName());
        if (fallback == null) {
            throw new IllegalStateException("PassthroughQueryRewriter bean not found");
        }
        return fallback;
    }
}
