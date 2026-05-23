package com.tricoq.domain.agent.service.rag.rewrite.strategy.impl;

import com.tricoq.domain.agent.model.request.StructuredInvocationRequest;
import com.tricoq.domain.agent.service.rag.rewrite.enums.RewritePolicy;
import com.tricoq.domain.agent.service.rag.rewrite.model.RewriteContext;
import com.tricoq.domain.agent.service.rag.rewrite.model.LlmRewriteResponse;
import com.tricoq.domain.agent.service.rag.rewrite.strategy.AbstractQueryRewriter;
import com.tricoq.domain.agent.spi.LlmInvocationFacade;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/19/26
 */
@Component
@RequiredArgsConstructor
public class LlmQueryRewriter extends AbstractQueryRewriter {

    private final LlmInvocationFacade facade;

    private static final String RAG_REWRITE_QUERY_SUFFIX = "-rag-rewrite";

    private static final String QUERY_REWRITE_PROMPT_TEMPLATE = """
            你是 RAG 检索 query 规划器。目标是提升知识库召回，不要回答问题。

            请基于用户原始问题生成 2 个互补的检索 query：
            1. 第一个 query 偏“实体/术语召回”：保留核心对象，补充可能出现的专有名词、参数名、函数名、英文关键词或同义表达。
            2. 第二个 query 偏“答案证据召回”：补充文档里可能承载答案的证据类型，例如 步骤、配置、参数、示例、原因、判断标准、限制、阈值、范围、异常或边界情况。

            可选领域提示（来自当前知识库配置）：
            %s

            输出要求：
            - 只输出 rewrittenQueries 字段；字段值是字符串数组。
            - 不要重复输出原始问题本身。
            - 不要输出“知识库、相关文档、指南、资料、信息、内容”等低信号泛词，除非它们和具体技术词组合后仍有明确检索价值。

            用户原始问题：
            %s
            """;

    private static final int MAX_DOMAIN_HINT_COUNT = 20;
    private static final int MAX_DOMAIN_HINT_CHARS = 80;

    private static final List<String> LOW_SIGNAL_PHRASES = List.of(
            "知识库中关于", "知识库", "相关文档", "文档", "指南", "资料", "信息", "内容"
    );

    @Override
    protected RewritePolicy rewritePolicy() {
        return RewritePolicy.LLM_MULTI_QUERY;
    }

    @Override
    protected List<String> doRewrite(String userText, RewriteContext context) {
        if (context == null) {
            throw new IllegalArgumentException("rewrite context is null");
        }
        String extraClientId = context.getExtraClientId();
        if (StringUtils.isBlank(extraClientId)) {
            throw new IllegalArgumentException("Extra client id is empty");
        }
        LlmRewriteResponse llmRewriteResponse = facade.invokeStructured(StructuredInvocationRequest
                .<LlmRewriteResponse>builder()
                .operationName("rag.query.rewrite")
                .clientId(extraClientId)
                .prompt(buildPrompt(userText, context))
                .roleSuffix(RAG_REWRITE_QUERY_SUFFIX)
                .responseType(LlmRewriteResponse.class)
                .retrieveSize(1024)
                .validate(LlmRewriteResponse::validate)
                .maxAttempts(2)
                .timeoutMillis(30000L)
                .build());

        return normalizeRewrittenQueries(userText, llmRewriteResponse.getRewrittenQueries());
    }

    @Override
    protected String noRewriteVariantReason(String userText) {
        return "LLM_REWRITE_NO_VARIANT";
    }

    private String buildPrompt(String userText, RewriteContext context) {
        return QUERY_REWRITE_PROMPT_TEMPLATE.formatted(formatDomainHints(context), userText);
    }

    private String formatDomainHints(RewriteContext context) {
        if (context == null) {
            return "- 暂无额外领域提示，只基于原问题做通用检索规划。";
        }
        List<String> sourceHints = CollectionUtils.isNotEmpty(context.getSelectedProfileHints())
                ? context.getSelectedProfileHints()
                : context.getQueryRewriteDomainHints();
        if (sourceHints == null) {
            return "- 暂无额外领域提示，只基于原问题做通用检索规划。";
        }
        List<String> domainHints = sourceHints.stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .distinct()
                .limit(MAX_DOMAIN_HINT_COUNT)
                .map(hint -> "- " + StringUtils.abbreviate(hint, MAX_DOMAIN_HINT_CHARS))
                .toList();
        if (domainHints.isEmpty()) {
            return "- 暂无额外领域提示，只基于原问题做通用检索规划。";
        }
        return String.join(System.lineSeparator(), domainHints);
    }

    private List<String> normalizeRewrittenQueries(String userText, List<String> rewrittenQueries) {
        if (rewrittenQueries == null) {
            return List.of();
        }
        Set<String> queries = new LinkedHashSet<>();
        for (String query : rewrittenQueries) {
            String normalizedQuery = normalizeQuery(query);
            if (usefulQuery(userText, normalizedQuery)) {
                queries.add(normalizedQuery);
            }
        }
        return List.copyOf(queries);
    }

    private String normalizeQuery(String query) {
        String normalizedQuery = StringUtils.trimToEmpty(query)
                .replaceFirst("^\\s*(?:[-*]|\\d+[.)、])\\s*", "");
        for (String phrase : LOW_SIGNAL_PHRASES) {
            normalizedQuery = StringUtils.replace(normalizedQuery, phrase, " ");
        }
        return normalizedQuery.replaceAll("\\s+", " ").trim();
    }

    private boolean usefulQuery(String userText, String query) {
        if (StringUtils.isBlank(query)) {
            return false;
        }
        return !StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(userText), query);
    }
}
