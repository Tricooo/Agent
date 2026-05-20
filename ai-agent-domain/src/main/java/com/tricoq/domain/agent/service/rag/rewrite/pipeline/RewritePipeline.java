package com.tricoq.domain.agent.service.rag.rewrite.pipeline;

import com.tricoq.domain.agent.model.dto.RewriteResult;
import com.tricoq.domain.agent.service.rag.rewrite.QueryRewriter;
import com.tricoq.domain.agent.service.rag.rewrite.enums.RewritePolicy;
import com.tricoq.domain.agent.service.rag.rewrite.enums.RewriteStatus;
import com.tricoq.domain.agent.service.rag.rewrite.model.RewriteAttempt;
import com.tricoq.domain.agent.service.rag.rewrite.strategy.QueryRewriteStrategy;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: query rewrite 编排器，按策略链选择第一个有效 rewrite 结果。
 * @author：trico qiang
 * @date: 5/19/26
 */
public class RewritePipeline implements QueryRewriter {

    private final RewritePolicy requestedPolicy;
    private final List<QueryRewriteStrategy> strategies;

    public RewritePipeline(RewritePolicy requestedPolicy, List<QueryRewriteStrategy> strategies) {
        this.requestedPolicy = requestedPolicy == null ? RewritePolicy.PASSTHROUGH : requestedPolicy;
        this.strategies = strategies == null ? List.of() : List.copyOf(strategies);
    }

    @Override
    public RewriteResult rewrite(String userText) {
        long start = System.nanoTime();
        List<RewriteAttempt> attempts = new ArrayList<>();

        for (QueryRewriteStrategy strategy : strategies) {
            if (strategy == null) {
                continue;
            }
            RewriteAttempt attempt = strategy.attempt(userText);
            if (attempt == null) {
                continue;
            }
            attempts.add(attempt);

            if (attempt.effectiveRewrite() || attempt.passthrough()) {
                return toResult(userText, attempt, attempts, start);
            }
        }

        RewriteAttempt fallbackAttempt = RewriteAttempt.builder()
                .policy(RewritePolicy.PASSTHROUGH)
                .status(RewriteStatus.PASSTHROUGH)
                .queryVariantTexts(List.of(StringUtils.trimToEmpty(userText)))
                .failureReason("NO_REWRITE_STRATEGY_AVAILABLE")
                .build();
        attempts.add(fallbackAttempt);
        return toResult(userText, fallbackAttempt, attempts, start);
    }

    private RewriteResult toResult(String userText, RewriteAttempt selectedAttempt,
                                   List<RewriteAttempt> attempts, long startNanos) {
        List<String> queryVariants = CollectionUtils.isEmpty(selectedAttempt.getQueryVariantTexts())
                ? List.of(StringUtils.trimToEmpty(userText))
                : selectedAttempt.getQueryVariantTexts();
        RewritePolicy actualPolicy = selectedAttempt.getPolicy() == null
                ? RewritePolicy.PASSTHROUGH
                : selectedAttempt.getPolicy();
        String attemptTrace = attemptTrace(attempts);

        return RewriteResult.builder()
                .originUserText(StringUtils.trimToEmpty(userText))
                .requestedPolicy(requestedPolicy.getPolicyName())
                .rewriteMode(actualPolicy.getPolicyName())
                .queryVariantTexts(queryVariants)
                .failureReason(failureReason(selectedAttempt, attempts))
                .elapsedMs(elapsedMs(startNanos))
                .attemptTrace(attemptTrace)
                .build();
    }

    private String failureReason(RewriteAttempt selectedAttempt, List<RewriteAttempt> attempts) {
        if (selectedAttempt.effectiveRewrite() && attempts.size() == 1) {
            return null;
        }
        if (selectedAttempt.passthrough()
                && attempts.size() == 1
                && requestedPolicy == RewritePolicy.PASSTHROUGH) {
            return null;
        }
        return attemptTrace(attempts);
    }

    private String attemptTrace(List<RewriteAttempt> attempts) {
        if (CollectionUtils.isEmpty(attempts)) {
            return "";
        }
        return attempts.stream()
                .map(RewriteAttempt::traceText)
                .collect(Collectors.joining(" -> "));
    }

    private Long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }
}
