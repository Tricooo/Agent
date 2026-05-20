package com.tricoq.domain.agent.service.rag.rewrite.strategy;

import com.tricoq.domain.agent.service.rag.rewrite.enums.RewritePolicy;
import com.tricoq.domain.agent.service.rag.rewrite.enums.RewriteStatus;
import com.tricoq.domain.agent.service.rag.rewrite.model.RewriteAttempt;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @description: query rewrite 单策略模板，负责计时、异常捕获和 query 规范化。
 * @author：trico qiang
 * @date: 5/19/26
 */
public abstract class AbstractQueryRewriter implements QueryRewriteStrategy {

    private static final int DEFAULT_MAX_QUERY_VARIANT_COUNT = 3;
    private static final int DEFAULT_MAX_QUERY_CHARS = 240;

    @Override
    public final RewritePolicy policy() {
        return rewritePolicy();
    }

    @Override
    public final RewriteAttempt attempt(String userText) {
        long start = System.nanoTime();
        String originUserText = StringUtils.trimToEmpty(userText);

        try {
            List<String> rewrittenQueries = doRewrite(originUserText);
            List<String> queryVariants = normalizeQueryVariants(originUserText, rewrittenQueries);
            RewriteStatus status = resolveStatus(queryVariants);
            String failureReason = status == RewriteStatus.REWRITTEN ? null : noRewriteVariantReason(originUserText);
            return buildAttempt(queryVariants, status, failureReason, start);
        } catch (Exception e) {
            return buildAttempt(
                    normalizeQueryVariants(originUserText, List.of()),
                    RewriteStatus.FAILED,
                    failureReason("REWRITE_EXCEPTION", e),
                    start
            );
        }
    }

    protected abstract RewritePolicy rewritePolicy();

    /**
     * 单个策略只负责尝试生成补充检索 query；原始问题由模板统一放在第一位。
     */
    protected abstract List<String> doRewrite(String userText);

    protected String noRewriteVariantReason(String userText) {
        return StringUtils.isBlank(userText) ? "BLANK_QUERY" : "NO_REWRITE_VARIANT";
    }

    protected int maxQueryVariantCount() {
        return DEFAULT_MAX_QUERY_VARIANT_COUNT;
    }

    protected int maxQueryChars() {
        return DEFAULT_MAX_QUERY_CHARS;
    }

    private List<String> normalizeQueryVariants(String originUserText, List<String> rewrittenQueries) {
        Set<String> variants = new LinkedHashSet<>();
        addVariant(variants, originUserText);

        if (CollectionUtils.isNotEmpty(rewrittenQueries)) {
            for (String query : rewrittenQueries) {
                addVariant(variants, query);
                if (variants.size() >= maxQueryVariantCount()) {
                    break;
                }
            }
        }

        return new ArrayList<>(variants);
    }

    private void addVariant(Set<String> variants, String text) {
        if (StringUtils.isBlank(text) || variants.size() >= maxQueryVariantCount()) {
            return;
        }
        variants.add(StringUtils.abbreviate(text.trim(), maxQueryChars()));
    }

    private RewriteStatus resolveStatus(List<String> queryVariants) {
        if (rewritePolicy() == RewritePolicy.PASSTHROUGH) {
            return RewriteStatus.PASSTHROUGH;
        }
        return queryVariants.size() > 1 ? RewriteStatus.REWRITTEN : RewriteStatus.NO_MATCH;
    }

    private RewriteAttempt buildAttempt(List<String> queryVariants, RewriteStatus status,
                                        String failureReason, long startNanos) {
        return RewriteAttempt.builder()
                .policy(rewritePolicy())
                .status(status)
                .queryVariantTexts(queryVariants)
                .failureReason(failureReason)
                .elapsedMs(elapsedMs(startNanos))
                .build();
    }

    private Long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    private String failureReason(String code, Exception e) {
        if (e == null) {
            return code;
        }
        String message = StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName());
        return code + ":" + message;
    }
}
