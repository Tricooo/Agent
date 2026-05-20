package com.tricoq.domain.agent.service.rag.rewrite.factory;

import com.tricoq.domain.agent.service.rag.rewrite.QueryRewriter;
import com.tricoq.domain.agent.service.rag.rewrite.enums.RewritePolicy;
import com.tricoq.domain.agent.service.rag.rewrite.pipeline.RewritePipeline;
import com.tricoq.domain.agent.service.rag.rewrite.strategy.QueryRewriteStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/14/26
 */
@Service
@RequiredArgsConstructor
public class QueryRewriterFactory {

    private final Map<String, QueryRewriteStrategy> queryRewriteStrategies;

    public QueryRewriter getQueryRewriter(String policy) {
        RewritePolicy rewritePolicy = RewritePolicy.getByPolicyOrDefault(policy);
        List<QueryRewriteStrategy> strategies = rewritePolicy.getStrategyBeanNames().stream()
                .map(queryRewriteStrategies::get)
                .filter(Objects::nonNull)
                .toList();

        if (!strategies.isEmpty()) {
            return new RewritePipeline(rewritePolicy, strategies);
        }

        QueryRewriteStrategy passthrough = queryRewriteStrategies.get(
                RewritePolicy.PASSTHROUGH.getStrategyBeanNames().getFirst()
        );
        if (passthrough == null) {
            throw new IllegalStateException("PassthroughQueryRewriter strategy not found");
        }
        return new RewritePipeline(RewritePolicy.PASSTHROUGH, List.of(passthrough));
    }
}
