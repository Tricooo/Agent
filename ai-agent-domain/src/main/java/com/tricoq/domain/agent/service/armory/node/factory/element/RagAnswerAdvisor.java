package com.tricoq.domain.agent.service.armory.node.factory.element;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.tricoq.domain.agent.model.dto.RerankResult;
import com.tricoq.domain.agent.model.dto.RewriteResult;
import com.tricoq.domain.agent.model.entity.VectorKeywordEntity;
import com.tricoq.domain.agent.model.enums.KeyWordPolicy;
import com.tricoq.domain.agent.model.valobj.RagObservationKeys;
import com.tricoq.domain.agent.model.valobj.RagObservationKeys.AdvisorContext;
import com.tricoq.domain.agent.model.valobj.RagObservationKeys.DocumentMetadata;
import com.tricoq.domain.agent.model.valobj.RagObservationKeys.Qa;
import com.tricoq.domain.agent.model.valobj.RetrievalOptionsVO;
import com.tricoq.domain.agent.service.rag.rerank.DocumentReranker;
import com.tricoq.domain.agent.service.rag.rerank.PassthroughDocumentReranker;
import com.tricoq.domain.agent.service.rag.rerank.enums.RerankQueryPolicy;
import com.tricoq.domain.agent.service.rag.rewrite.QueryRewriter;
import com.tricoq.domain.agent.service.rag.rewrite.enums.RewritePolicy;
import com.tricoq.domain.agent.service.rag.rewrite.pipeline.RewritePipeline;
import com.tricoq.domain.agent.service.rag.rewrite.strategy.impl.PassthroughQueryRewriter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionConverter;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.ai.vectorstore.pgvector.PgVectorFilterExpressionConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RAG 顾问
 * 提供上传文档/个人知识库检索的能力
 * <p>
 * rag基本链路
 * Retrieval：找到了哪些 chunk
 * Context Assembly：怎么把 chunk 放进 prompt
 * Generation：模型怎么基于 chunk 回答
 * Attribution：回答引用了哪些 chunk
 *
 * @author trico qiang
 * @date 10/28/25
 */
@Slf4j
public class RagAnswerAdvisor implements BaseAdvisor {

    private final VectorStore vectorStore;
    private final SearchRequest searchRequest;
    private final String userTextAdvisor;
    private final RetrievalOptionsVO retrievalOptions;
    private final RetrievalTopKPlan retrievalTopKPlan;
    private final DocumentReranker documentReranker;
    private final QueryRewriter queryRewriter;

    private static final int DEFAULT_MAX_CONTEXT_CHARS = 6000;
    private static final int DEFAULT_MAX_RERANK_QUERY_CHARS = 1000;
    private static final int SALIENCE_ADJACENT_MAX_CHARS = 1400;
    private static final double DEFAULT_QUERY_FUSION_RERANK_WEIGHT = 0.3D;
    private static final String CHUNK_TRUNCATED_NOTICE = "\n...[chunk truncated]...\n";
    private static final int MIN_CHUNK_HEAD_CHARS = 1000;
    private static final String EMPTY_RETRIEVAL_CONTEXT =
            "未检索到满足当前知识库过滤条件和相似度阈值的知识片段。请明确告知用户：当前知识库没有可用上下文，不能基于知识库回答该问题。";

    private static final Pattern KEYWORD_TOKEN_PATTERN = Pattern.compile("[A-Za-z0-9_./-]+");
    // 命中任一结构特征 → 强标识符
    // 含下划线、点号、斜线或连字符：query_prometheus, node.cpu, silver-river-42
    // 数字+字母混合：silver42, CN7319
    // 字母+数字混合：ARCHIVE-CN-7319
    private static final Pattern STRONG_TOKEN_PATTERN = Pattern.compile(
            ".*[_.\\-/].*" + "|.*\\d+.*[a-zA-Z].*" + "|.*[a-zA-Z].*\\d+.*"
    );
    private static final int MAX_KEYWORD_TERMS = 12;
    private static final int MIN_KEYWORD_LENGTH = 2;
    private static final int MIN_STRONG_KEYWORD_LENGTH = 5;
    private static final int DEFAULT_CANDIDATE_TOP_K = 20;
    private static final int COVERAGE_GUARD_PROTECTED_VARIANT_RANK = 2;
    private static final int EVIDENCE_COVERAGE_GUARD_MAX_CANDIDATE_RANK = 8;
    private static final int EVIDENCE_COVERAGE_GUARD_MAX_ADDED = 1;
    private static final String COVERAGE_GUARD_REASON_QUERY_VARIANT = "query_variant";
    private static final String COVERAGE_GUARD_REASON_EVIDENCE_TYPE = "evidence_type";
    private static final FilterExpressionConverter PG_FILTER_EXPRESSION_CONVERTER = new PgVectorFilterExpressionConverter();
    private static final String RETRIEVAL_MODE_HYBRID = "HYBRID";
    private static final String RETRIEVAL_SOURCE_VECTOR = "VECTOR";
    private static final String RETRIEVAL_SOURCE_KEYWORD = "KEYWORD";
    private static final String RETRIEVAL_SOURCE_VECTOR_KEYWORD = "VECTOR_KEYWORD";
    private static final String RERANK_MODE_PASSTHROUGH = "PASSTHROUGH";
    private static final String KEYWORD_SKIPPED_REASON_NO_KEYWORD_QUERY = "NO_KEYWORD_QUERY";
    private static final String KEYWORD_SKIPPED_REASON_NO_KEYWORD_RESULTS = "NO_KEYWORD_RESULTS";


    public RagAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest) {
        this(vectorStore, searchRequest, new RetrievalOptionsVO(), new PassthroughDocumentReranker());
    }

    public RagAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest,
                            RetrievalOptionsVO retrievalOptions) {
        this(vectorStore, searchRequest, retrievalOptions, new PassthroughDocumentReranker());
    }

    public RagAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest,
                            RetrievalOptionsVO retrievalOptions, DocumentReranker documentReranker) {
        this(vectorStore, searchRequest, retrievalOptions, documentReranker, passthroughQueryRewriter());
    }

    public RagAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest,
                            RetrievalOptionsVO retrievalOptions, DocumentReranker documentReranker,
                            QueryRewriter queryRewriter) {
        this.vectorStore = vectorStore;
        this.searchRequest = searchRequest;
        this.retrievalOptions = retrievalOptions == null ? new RetrievalOptionsVO() : retrievalOptions;
        this.documentReranker = documentReranker == null ? new PassthroughDocumentReranker() : documentReranker;
        this.queryRewriter = queryRewriter == null ? passthroughQueryRewriter() : queryRewriter;
        this.retrievalTopKPlan = buildTopKPlan(searchRequest.getTopK());
        this.userTextAdvisor = buildUserTextAdvisor(this.retrievalOptions.isContextSalienceEnabled());

    }

    private static QueryRewriter passthroughQueryRewriter() {
        return new RewritePipeline(RewritePolicy.PASSTHROUGH, List.of(new PassthroughQueryRewriter()));
    }

    private static String buildUserTextAdvisor(boolean contextSalienceEnabled) {
        String salienceInstruction = contextSalienceEnabled ? """
                If the context contains formulas, thresholds, ranges, judgement standards,
                status levels, parameters, examples, or troubleshooting steps, include those
                evidence items explicitly when they are relevant to the user question.
                Preserve code, PromQL, formulas, configuration keys, identifiers, and
                threshold literals exactly as they appear in the context when quoting them.
                Do not invent missing ranges or standards; if a requested item is absent from
                the context, say that the context does not provide it.
                """ : "";
        return """
                
                Context information is below, surrounded by ---------------------
                Each context chunk is prefixed with a citation number like [1], [2].
                When using context information, prefer mentioning the citation number.
                %s
                ---------------------
                {%s}
                ---------------------
                
                Given the context and provided history information and not prior knowledge,
                reply to the user comment. If the answer is not in the context, inform
                the user that you can't answer the question.
                """.formatted(salienceInstruction, AdvisorContext.QUESTION_ANSWER_CONTEXT);
    }

    private RetrievalTopKPlan buildTopKPlan(int topK) {
        int finalTopK = Math.max(topK, 1);
        int candidateTopK = Math.max(finalTopK, DEFAULT_CANDIDATE_TOP_K);
        return new RetrievalTopKPlan(
                finalTopK,
                candidateTopK,
                retrievalOptions.effectiveVectorTopK(candidateTopK),
                retrievalOptions.effectiveKeywordTopK(candidateTopK)
        );
    }


    /**
     * Logic to be executed before the rest of the advisor chain is called.
     */
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        //复制上下文 防止修改原始上下文 别的顾问可能也会用到原始上下文 不要造成污染
        //  - prompt — 就是发给 AI 模型的 Prompt 对象，里面包含一组 Message（SystemMessage、UserMessage、AssistantMessage 等）加上可选的
        //  ChatOptions。这是模型实际看到的对话内容。
        //  - context — 一个 Map<String, Object>，是 Advisor 链中各节点之间传递数据的载体。它不会发给模型，而是在 Advisor 链内部流转。比如你代码里往 context
        //  放 RAG 观测字段和 prompt 上下文字段，后续的 after() 方法或其他 Advisor 可以从 context 中读取这些数据。
        Map<String, Object> unmodifiedContext = Map.copyOf(chatClientRequest.context());
        Map<String, Object> context = new HashMap<>(unmodifiedContext);

        String userText = chatClientRequest.prompt().getUserMessage().getText();
        String advisedUserText = userText + System.lineSeparator() + userTextAdvisor;

        SearchRequest request = buildSearchRequest(userText, context);
        double candidateSimilarityThreshold = candidateSimilarityThreshold(request);
        RewriteResult rewriteResult = rewriteQuery(userText);
        List<String> queryVariants = normalizeQueryVariants(rewriteResult, userText);
        RerankQueryPlan rerankQueryPlan = buildRerankQueryPlan(userText, queryVariants);

        List<Document> documents = tagBeforeRerankRank(retrieveDocuments(queryVariants, request));

        if (CollectionUtils.isEmpty(documents)) {
            // 空召回不可静默，是 RAG 链路重要状态；同时不能退化成普通聊天，仍要把“无可用上下文”的边界写进 prompt。
            String emptyContext = EMPTY_RETRIEVAL_CONTEXT;
            HashMap<String, Object> emptyRetrievalContext = new HashMap<>(unmodifiedContext);
            putRewriteObservations(emptyRetrievalContext, rewriteResult, queryVariants);
            emptyRetrievalContext.put(Qa.PRE_RERANK_DOCUMENTS, List.of());
            emptyRetrievalContext.put(Qa.RETRIEVED_DOCUMENTS, List.of());
            emptyRetrievalContext.put(Qa.RETRIEVAL_EMPTY, true);
            emptyRetrievalContext.put(AdvisorContext.QUESTION_ANSWER_CONTEXT, emptyContext);
            emptyRetrievalContext.put(Qa.RETRIEVED_DOCUMENT_COUNT, 0);
            emptyRetrievalContext.put(Qa.CONTEXT_MAX_CHARS, DEFAULT_MAX_CONTEXT_CHARS);
            emptyRetrievalContext.put(Qa.CONTEXT_ACTUAL_CHARS, emptyContext.length());
            emptyRetrievalContext.put(Qa.CONTEXT_SELECTED_COUNT, 0);
            emptyRetrievalContext.put(Qa.CONTEXT_DROPPED_COUNT, 0);
            emptyRetrievalContext.put(Qa.CONTEXT_TRUNCATED, false);
            emptyRetrievalContext.put(Qa.CONTEXT_SALIENCE_CUES, List.of());
            emptyRetrievalContext.put(Qa.CONTEXT_SALIENCE_EXPANSION_COUNT, 0);
            emptyRetrievalContext.put(Qa.SIMILARITY_THRESHOLD, request.getSimilarityThreshold());
            emptyRetrievalContext.put(Qa.CANDIDATE_SIMILARITY_THRESHOLD, candidateSimilarityThreshold);
            emptyRetrievalContext.put(Qa.RERANK_APPLIED, false);
            emptyRetrievalContext.put(Qa.RERANK_MODE, RERANK_MODE_PASSTHROUGH);
            emptyRetrievalContext.put(Qa.RERANK_CANDIDATE_COUNT, 0);
            emptyRetrievalContext.put(Qa.RERANK_FINAL_COUNT, 0);
            emptyRetrievalContext.put(Qa.RERANK_FAILURE_REASON, "");
            emptyRetrievalContext.put(Qa.RERANK_MODEL_NAME, "");
            emptyRetrievalContext.put(Qa.RERANK_ENDPOINT, "");
            emptyRetrievalContext.put(Qa.RERANK_QUERY_POLICY, rerankQueryPlan.getPolicy());
            emptyRetrievalContext.put(Qa.RERANK_QUERY_TEXT, rerankQueryPlan.getOriginQueryText());
            emptyRetrievalContext.put(Qa.RERANK_COVERAGE_GUARD_APPLIED, false);
            emptyRetrievalContext.put(Qa.RERANK_COVERAGE_GUARD_ADDED_COUNT, 0);

            PromptTemplate promptTemplate = new PromptTemplate(advisedUserText);
            String rendered = promptTemplate.render(Map.of(AdvisorContext.QUESTION_ANSWER_CONTEXT, emptyContext));

            log.info("RAG检索为空: query={}, filterExpression={}, similarityThreshold={}",
                    userText, request.getFilterExpression(), request.getSimilarityThreshold());

            List<Message> instructions = new ArrayList<>(chatClientRequest.prompt().getInstructions());
            instructions.set(instructions.size() - 1, new UserMessage(rendered));

            return ChatClientRequest.builder()
                    .prompt(Prompt.builder()
                            .messages(instructions)
                            .chatOptions(chatClientRequest.prompt().getOptions())
                            .build())
                    .context(emptyRetrievalContext)
                    .build();
        }

        int finalTopK = retrievalTopKPlan.finalTopK();
        RerankResult rerankResult = rerankDocuments(rerankQueryPlan, documents, retrievalTopKPlan);
        CoverageGuardResult coverageGuardResult = applyRerankCoverageGuard(
                rerankResult.getDocuments(), documents, rerankResult, finalTopK, userText, queryVariants, rewriteResult);
        List<Document> rerankDocuments = coverageGuardResult.documents();

        //documentContext 很长时要做裁剪/摘要（Top-K、去重、截断），否则可能超长或稀释关键信息
        //编号是建立模型引用的基础，模型可以确定编号，系统也能找到对应的引用---用于解决可溯源
        RenderedDocumentContext renderedContext = renderDocumentContext(rerankDocuments, DEFAULT_MAX_CONTEXT_CHARS);
        String documentContext = renderedContext.context();
        Double minScore = minScore(rerankDocuments);
        Double maxScore = maxScore(rerankDocuments);
        Map<String, Object> advisedUserParams = new HashMap<>(unmodifiedContext);
        putRewriteObservations(advisedUserParams, rewriteResult, queryVariants);
        //给LLM看
        advisedUserParams.put(AdvisorContext.QUESTION_ANSWER_CONTEXT, documentContext);
        advisedUserParams.put(Qa.CONTEXT_MAX_CHARS, DEFAULT_MAX_CONTEXT_CHARS);
        advisedUserParams.put(Qa.CONTEXT_ACTUAL_CHARS, documentContext.length());
        advisedUserParams.put(Qa.CONTEXT_SELECTED_COUNT, renderedContext.selectedCount());
        advisedUserParams.put(Qa.CONTEXT_DROPPED_COUNT, renderedContext.droppedCount());
        advisedUserParams.put(Qa.CONTEXT_TRUNCATED, renderedContext.truncated());
        advisedUserParams.put(Qa.CONTEXT_SALIENCE_CUES, renderedContext.salienceCues());
        advisedUserParams.put(Qa.CONTEXT_SALIENCE_EXPANSION_COUNT, renderedContext.salienceExpansionCount());
        advisedUserParams.put(Qa.SIMILARITY_THRESHOLD, request.getSimilarityThreshold());
        advisedUserParams.put(Qa.CANDIDATE_SIMILARITY_THRESHOLD, candidateSimilarityThreshold);
        advisedUserParams.put(Qa.MIN_RETRIEVED_SCORE, minScore);
        advisedUserParams.put(Qa.MAX_RETRIEVED_SCORE, maxScore);
        advisedUserParams.put(Qa.RERANK_APPLIED, rerankResult.isApplied());
        advisedUserParams.put(Qa.RERANK_MODE, rerankResult.getMode());
        advisedUserParams.put(Qa.RERANK_CANDIDATE_COUNT, rerankResult.getCandidateCount());
        advisedUserParams.put(Qa.RERANK_FINAL_COUNT, rerankDocuments.size());
        advisedUserParams.put(Qa.RERANK_FAILURE_REASON, StringUtils.defaultString(rerankResult.getFailureReason()));
        advisedUserParams.put(Qa.RERANK_MODEL_NAME, StringUtils.defaultString(rerankResult.getModelName()));
        advisedUserParams.put(Qa.RERANK_ENDPOINT, StringUtils.defaultString(rerankResult.getEndpoint()));
        advisedUserParams.put(Qa.RERANK_QUERY_POLICY, rerankQueryPlan.getPolicy());
        advisedUserParams.put(Qa.RERANK_QUERY_TEXT, CollectionUtils.isEmpty(rerankQueryPlan.getQueryTextSet()) ?
                rerankQueryPlan.getOriginQueryText() :
                StringUtils.join(rerankQueryPlan.getQueryTextSet(), System.lineSeparator()));
        advisedUserParams.put(Qa.RERANK_COVERAGE_GUARD_APPLIED, coverageGuardResult.applied());
        advisedUserParams.put(Qa.RERANK_COVERAGE_GUARD_ADDED_COUNT, coverageGuardResult.addedCount());


        //给人看 便于看到引用的文本
        advisedUserParams.put(Qa.PRE_RERANK_DOCUMENTS, documents);
        advisedUserParams.put(Qa.RETRIEVED_DOCUMENTS, rerankDocuments);
        advisedUserParams.put(Qa.RETRIEVED_DOCUMENT_COUNT, rerankDocuments.size());
        advisedUserParams.put(Qa.RETRIEVAL_EMPTY, false);

        PromptTemplate promptTemplate = new PromptTemplate(advisedUserText);
        String rendered = promptTemplate.render(Map.of(AdvisorContext.QUESTION_ANSWER_CONTEXT, documentContext));

        log.info("RAG检索结果: query={}, queryVariants={}, retrieved={}, selected={}, dropped={}, truncated={}, empty={}, similarityThreshold={}, minScore={}, maxScore={}, coverageGuardApplied={}, coverageGuardAdded={}",
                userText,
                queryVariants.size(),
                documents.size(),
                renderedContext.selectedCount(),
                renderedContext.droppedCount(),
                renderedContext.truncated(),
                false,
                request.getSimilarityThreshold(),
                minScore,
                maxScore,
                coverageGuardResult.applied(),
                coverageGuardResult.addedCount());

        //整个发给LLM的提示词序列，包括SystemMessage UserMessage AssistantMessage
        List<Message> instructions = new ArrayList<>(chatClientRequest.prompt().getInstructions());
        instructions.set(instructions.size() - 1, new UserMessage(rendered));

        return ChatClientRequest.builder()
                .prompt(Prompt.builder()
                        .messages(instructions)
                        .chatOptions(chatClientRequest.prompt().getOptions())
                        .build())
                .context(advisedUserParams)
                .build();
    }

    private SearchRequest buildSearchRequest(String userText, Map<String, Object> context) {
        return SearchRequest.from(searchRequest).query(userText)
                .topK(retrievalTopKPlan.finalTopK())
                .filterExpression(doGetFilterExpression(context)).build();
    }

    private RewriteResult rewriteQuery(String userText) {
        try {
            RewriteResult rewriteResult = queryRewriter.rewrite(userText);
            if (rewriteResult != null) {
                return rewriteResult;
            }
        } catch (Exception e) {
            log.warn("query rewrite failed, fallback to passthrough: query={}, rewriter={}",
                    userText, queryRewriter.getClass().getSimpleName(), e);
        }
        return passthroughRewriteResult(userText);
    }

    private RewriteResult passthroughRewriteResult(String userText) {
        return RewriteResult.builder()
                .originUserText(userText)
                .queryVariantTexts(List.of(userText))
                .requestedPolicy(RewritePolicy.PASSTHROUGH.getPolicyName())
                .rewriteMode(RewritePolicy.PASSTHROUGH.getPolicyName())
                .failureReason("REWRITE_FALLBACK_PASSTHROUGH")
                .elapsedMs(0L)
                .attemptTrace("PASSTHROUGH:PASSTHROUGH(REWRITE_FALLBACK_PASSTHROUGH)")
                .build();
    }

    private List<String> normalizeQueryVariants(RewriteResult rewriteResult, String userText) {
        Set<String> variants = new LinkedHashSet<>();
        addQueryVariant(variants, userText);
        if (rewriteResult != null && CollectionUtils.isNotEmpty(rewriteResult.getQueryVariantTexts())) {
            for (String variant : rewriteResult.getQueryVariantTexts()) {
                addQueryVariant(variants, variant);
            }
        }
        return List.copyOf(variants);
    }

    private static void addQueryVariant(Set<String> variants, String variant) {
        if (StringUtils.isBlank(variant)) {
            return;
        }
        variants.add(variant.trim());
    }

    private void putRewriteObservations(Map<String, Object> params, RewriteResult rewriteResult, List<String> queryVariants) {
        String rewriteMode = rewriteResult == null
                ? RewritePolicy.PASSTHROUGH.getPolicyName()
                : StringUtils.defaultIfBlank(rewriteResult.getRewriteMode(), RewritePolicy.PASSTHROUGH.getPolicyName());
        params.put(Qa.QUERY_REWRITE_MODE, rewriteMode);
        params.put(Qa.QUERY_REWRITE_REQUESTED_POLICY, rewriteResult == null
                ? RewritePolicy.PASSTHROUGH.getPolicyName()
                : StringUtils.defaultIfBlank(rewriteResult.getRequestedPolicy(), rewriteMode));
        params.put(Qa.QUERY_VARIANT_COUNT, queryVariants.size());
        params.put(Qa.QUERY_VARIANT_TEXTS, queryVariants);
        params.put(Qa.QUERY_REWRITE_FAILURE_REASON, rewriteResult == null
                ? ""
                : StringUtils.defaultString(rewriteResult.getFailureReason()));
        params.put(Qa.QUERY_REWRITE_ELAPSED_MS, rewriteResult == null || rewriteResult.getElapsedMs() == null
                ? 0L
                : rewriteResult.getElapsedMs());
        params.put(Qa.QUERY_REWRITE_ATTEMPT_TRACE, rewriteResult == null
                ? ""
                : StringUtils.defaultString(rewriteResult.getAttemptTrace()));
        params.put(Qa.QUERY_REWRITE_PROFILE_SOURCE, rewriteResult == null
                ? ""
                : StringUtils.defaultString(rewriteResult.getProfileSource()));
        params.put(Qa.QUERY_REWRITE_PROFILE_VERSION, rewriteResult == null
                ? ""
                : StringUtils.defaultString(rewriteResult.getProfileVersion()));
        params.put(Qa.QUERY_REWRITE_SELECTED_PROFILE_HINTS, rewriteResult == null
                || CollectionUtils.isEmpty(rewriteResult.getSelectedProfileHints())
                ? List.of()
                : rewriteResult.getSelectedProfileHints());
    }

    private RerankQueryPlan buildRerankQueryPlan(String userText, List<String> queryVariants) {
        RerankQueryPolicy policy = retrievalOptions.effectiveRerankQueryPolicy();
        return switch (policy) {
            case ORIGINAL -> new RerankQueryPlan(policy.getPolicyName(), userText);
            case ORIGINAL_PLUS_VARIANT -> {
                Set<String> rerankQueries = buildRerankQueries(userText, queryVariants);
                String rerankQueryText = limitRerankQuery(String.join(System.lineSeparator(), rerankQueries));
                yield new RerankQueryPlan(policy.getPolicyName(), rerankQueryText);
            }
            case PER_VARIANT_RERANK_RRF, FUSION_AWARE_RERANK_RRF -> {
                Set<String> rerankQueries = buildRerankQueries(userText, queryVariants);
                yield new RerankQueryPlan(policy.getPolicyName(), userText, rerankQueries);
            }
        };
    }

    private Set<String> buildRerankQueries(String userText, List<String> queryVariants) {
        Set<String> rerankQueries = new LinkedHashSet<>();
        addQueryVariant(rerankQueries, userText);
        if (CollectionUtils.isNotEmpty(queryVariants)) {
            for (String queryVariant : queryVariants) {
                addQueryVariant(rerankQueries, queryVariant);
            }
        }
        return rerankQueries;
    }

    private String limitRerankQuery(String rerankQueryText) {
        if (StringUtils.length(rerankQueryText) <= DEFAULT_MAX_RERANK_QUERY_CHARS) {
            return rerankQueryText;
        }
        return StringUtils.substring(rerankQueryText, 0, DEFAULT_MAX_RERANK_QUERY_CHARS);
    }

    private List<Document> tagBeforeRerankRank(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return List.of();
        }

        List<Document> taggedDocuments = new ArrayList<>(documents.size());
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            Map<String, Object> metadata = new HashMap<>(document.getMetadata());
            metadata.put(DocumentMetadata.BEFORE_RERANK_RANK, i + 1);
            taggedDocuments.add(document.mutate()
                    .metadata(metadata)
                    .build());
        }
        return taggedDocuments;
    }

    private List<Document> retrieveDocuments(List<String> queryVariants, SearchRequest request) {
        if (CollectionUtils.isEmpty(queryVariants)) {
            return List.of();
        }

        List<List<Document>> variantResults = new ArrayList<>(queryVariants.size());
        for (int i = 0; i < queryVariants.size(); i++) {
            String queryVariant = queryVariants.get(i);
            SearchRequest variantRequest = SearchRequest.from(request)
                    .query(queryVariant)
                    .build();
            List<Document> variantDocuments = retrieveDocuments(queryVariant, variantRequest);
            variantResults.add(tagQueryVariant(variantDocuments, i + 1, queryVariant));
        }

        return mergeQueryVariantResults(variantResults, retrievalTopKPlan.candidateTopK());
    }

    private List<Document> tagQueryVariant(List<Document> documents, int variantIndex, String variantText) {
        if (CollectionUtils.isEmpty(documents)) {
            return List.of();
        }

        List<Document> taggedDocuments = new ArrayList<>(documents.size());
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            Map<String, Object> metadata = new HashMap<>(document.getMetadata());
            metadata.put(DocumentMetadata.QUERY_VARIANT_INDEX, variantIndex);
            metadata.put(DocumentMetadata.QUERY_VARIANT_TEXT, variantText);
            metadata.put(DocumentMetadata.QUERY_VARIANT_RANK, i + 1);
            taggedDocuments.add(document.mutate()
                    .metadata(metadata)
                    .build());
        }
        return taggedDocuments;
    }

    private List<Document> mergeQueryVariantResults(List<List<Document>> variantResults, int topK) {
        if (CollectionUtils.isEmpty(variantResults) || topK <= 0) {
            return List.of();
        }

        Map<String, QueryFusionDocumentCandidate> candidates = new HashMap<>();
        for (List<Document> documents : variantResults) {
            if (CollectionUtils.isEmpty(documents)) {
                continue;
            }
            for (int i = 0; i < documents.size(); i++) {
                Document document = documents.get(i);
                String key = mergedDocumentKey(document);
                if (StringUtils.isBlank(key)) {
                    continue;
                }
                QueryFusionDocumentCandidate candidate = candidates.get(key);
                if (candidate == null) {
                    candidate = new QueryFusionDocumentCandidate(key, document);
                    candidates.put(key, candidate);
                }
                int rank = i + 1;
                candidate.addScore(calculateScore(rank));
                candidate.recordHit(document, rank);
            }
        }

        if (MapUtils.isEmpty(candidates)) {
            return List.of();
        }

        List<QueryFusionDocumentCandidate> candidateList = new ArrayList<>(candidates.values());
        candidateList.sort(
                Comparator.comparing(QueryFusionDocumentCandidate::getScore).reversed()
                        .thenComparing(QueryFusionDocumentCandidate::getBestQueryVariantRank)
                        .thenComparing(QueryFusionDocumentCandidate::getFirstQueryVariantIndex)
                        .thenComparing(QueryFusionDocumentCandidate::getKey)
        );

        List<Document> fusedDocuments = new ArrayList<>(Math.min(topK, candidateList.size()));
        for (int i = 0; i < candidateList.size() && fusedDocuments.size() < topK; i++) {
            fusedDocuments.add(toQueryFusionDocument(candidateList.get(i), i + 1));
        }
        return fusedDocuments;
    }

    private String mergedDocumentKey(Document document) {
        String key = documentKey(document);
        if (StringUtils.isNotBlank(key)) {
            return key;
        }
        return "text:" + StringUtils.defaultString(document.getText()).hashCode();
    }

    private List<Document> retrieveDocuments(String userText, SearchRequest request) {
        SearchRequest vectorRequest = buildVectorCandidateRequest(request);
        if (!retrievalOptions.isHybridMode()) {
            return vectorStore.similaritySearch(vectorRequest);
        }

        List<Document> vectorDocuments = vectorStore.similaritySearch(vectorRequest);
        String keywordQuery = buildKeywordQuery(userText);

        SearchRequest keywordRequest = SearchRequest.from(request)
                .topK(retrievalTopKPlan.keywordTopK())
                .build();
        List<Document> keywordDocuments = keywordSearch(keywordQuery, keywordRequest);
        String keywordSkippedReason = keywordSkippedReason(keywordQuery, keywordDocuments);

        return rrMerge(vectorDocuments, keywordDocuments, retrievalTopKPlan.candidateTopK(), keywordSkippedReason);
    }

    private SearchRequest buildVectorCandidateRequest(SearchRequest request) {
        return SearchRequest.from(request)
                .topK(retrievalTopKPlan.vectorTopK())
                .similarityThreshold(candidateSimilarityThreshold(request))
                .build();
    }

    private double candidateSimilarityThreshold(SearchRequest request) {
        return retrievalOptions.effectiveCandidateSimilarityThreshold(request.getSimilarityThreshold());
    }

    private List<Document> rrMerge(List<Document> vectorDocuments, List<Document> keywordDocuments, int topK,
                                   String keywordSkippedReason) {
        //score = 1 / (rrfK + rank)
        if (topK <= 0) {
            return List.of();
        }

        Map<String, RrfDocumentCandidate> candidates = new HashMap<>();

        addRrfScores(candidates, vectorDocuments, RETRIEVAL_SOURCE_VECTOR);
        addRrfScores(candidates, keywordDocuments, RETRIEVAL_SOURCE_KEYWORD);

        if (MapUtils.isEmpty(candidates)) {
            return List.of();
        }

        List<RrfDocumentCandidate> candidateList = new ArrayList<>(candidates.values());
        candidateList.sort(Comparator.comparing(RrfDocumentCandidate::getScore).reversed());

        return candidateList.stream()
                .limit(topK)
                .map(candidate -> toRrfDocument(candidate, keywordSkippedReason))
                .toList();
    }

    private void addRrfScores(Map<String, RrfDocumentCandidate> candidates, List<Document> documents, String retrievalSource) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            String key = documentKey(document);
            if (StringUtils.isBlank(key)) {
                continue;
            }
            RrfDocumentCandidate candidate = candidates.get(key);
            if (candidate == null) {
                candidate = new RrfDocumentCandidate(document);
                candidates.put(key, candidate);
            }
            int rank = i + 1;
            candidate.addScore(calculateScore(rank));
            candidate.recordSource(retrievalSource, rank, document.getScore());
        }
    }

    private RerankResult rerankDocuments(RerankQueryPlan plan, List<Document> candidates,
                                         RetrievalTopKPlan retrievalTopKPlan) {
        RerankQueryPolicy rerankQueryPolicy = RerankQueryPolicy.getByPolicyOrDefault(plan.getPolicy());
        return switch (rerankQueryPolicy) {
            case ORIGINAL, ORIGINAL_PLUS_VARIANT ->
                    documentReranker.rerank(plan.getOriginQueryText(), candidates, retrievalTopKPlan.finalTopK());
            case PER_VARIANT_RERANK_RRF, FUSION_AWARE_RERANK_RRF ->
                    rerankPerVariantWithRrf(plan, candidates, retrievalTopKPlan);
        };
    }

    private RerankResult rerankPerVariantWithRrf(RerankQueryPlan plan, List<Document> candidates,
                                                 RetrievalTopKPlan retrievalTopKPlan) {
        int finalTopK = retrievalTopKPlan.finalTopK();
        if (CollectionUtils.isEmpty(candidates) || finalTopK <= 0) {
            return documentReranker.rerank(plan.getOriginQueryText(), candidates, finalTopK);
        }

        List<String> queryTexts = new ArrayList<>(plan.getQueryTextSet());
        if (CollectionUtils.isEmpty(queryTexts)) {
            return documentReranker.rerank(plan.getOriginQueryText(), candidates, finalTopK);
        }

        Map<String, RerankFusionDocumentCandidate> fusionCandidates = new HashMap<>();
        List<RerankResult> rerankResults = new ArrayList<>(queryTexts.size());
        for (int i = 0; i < queryTexts.size(); i++) {
            RerankResult rerankResult = documentReranker.rerank(
                    queryTexts.get(i),
                    candidates,
                    retrievalTopKPlan.candidateTopK()
            );
            if (rerankResult == null) {
                continue;
            }
            rerankResults.add(rerankResult);
            if (!rerankResult.isApplied()) {
                continue;
            }
            addRerankFusionScores(fusionCandidates, rerankResult.getDocuments(), i + 1);
        }

        if (MapUtils.isEmpty(fusionCandidates)) {
            return documentReranker.rerank(plan.getOriginQueryText(), candidates, finalTopK);
        }

        RerankQueryPolicy rerankQueryPolicy = RerankQueryPolicy.getByPolicyOrDefault(plan.getPolicy());
        List<RerankFusionDocumentCandidate> candidateList = new ArrayList<>(fusionCandidates.values());
        if (RerankQueryPolicy.FUSION_AWARE_RERANK_RRF == rerankQueryPolicy) {
            applyQueryFusionBoost(candidateList);
        }
        candidateList.sort(Comparator.comparing(RerankFusionDocumentCandidate::getScore).reversed()
                .thenComparing(RerankFusionDocumentCandidate::getBestRerankRank)
                .thenComparing(RerankFusionDocumentCandidate::getFirstQueryVariantIndex)
                .thenComparing(RerankFusionDocumentCandidate::getKey));

        int limit = Math.min(finalTopK, candidateList.size());
        List<Document> fusedDocuments = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            fusedDocuments.add(toRerankFusionDocument(candidateList.get(i), i + 1, rerankQueryPolicy));
        }

        return RerankResult.builder()
                .documents(fusedDocuments)
                .applied(true)
                .mode(rerankQueryPolicy.getPolicyName())
                .candidateCount(candidates.size())
                .finalCount(fusedDocuments.size())
                .failureReason(firstRerankFailureReason(rerankResults))
                .modelName(firstRerankModelName(rerankResults))
                .endpoint(firstRerankEndpoint(rerankResults))
                .build();
    }

    private void addRerankFusionScores(Map<String, RerankFusionDocumentCandidate> candidates,
                                       List<Document> documents,
                                       int queryVariantIndex) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            String key = documentKey(document);
            if (StringUtils.isBlank(key)) {
                continue;
            }
            RerankFusionDocumentCandidate candidate = candidates.get(key);
            if (candidate == null) {
                candidate = new RerankFusionDocumentCandidate(key, document);
                candidates.put(key, candidate);
            }
            int rerankRank = i + 1;
            candidate.addScore(calculateScore(rerankRank));
            candidate.recordHit(document, queryVariantIndex, rerankRank);
        }
    }

    private void applyQueryFusionBoost(List<RerankFusionDocumentCandidate> candidateList) {
        if (CollectionUtils.isEmpty(candidateList)) {
            return;
        }
        for (RerankFusionDocumentCandidate candidate : candidateList) {
            Integer queryFusionRank = metadataInteger(candidate.getDocument().getMetadata(), DocumentMetadata.QUERY_FUSION_RANK);
            if (queryFusionRank == null || queryFusionRank <= 0) {
                continue;
            }
            candidate.setQueryFusionBoostScore(DEFAULT_QUERY_FUSION_RERANK_WEIGHT * calculateScore(queryFusionRank));
        }
    }

    private Document toRerankFusionDocument(RerankFusionDocumentCandidate candidate, int rerankRank,
                                            RerankQueryPolicy rerankQueryPolicy) {
        Document document = candidate.getDocument();
        Map<String, Object> metadata = new HashMap<>(document.getMetadata());
        metadata.put(DocumentMetadata.RERANK_RANK, rerankRank);
        metadata.put(DocumentMetadata.RERANK_APPLIED, true);
        metadata.put(DocumentMetadata.RERANK_MODE, rerankQueryPolicy.getPolicyName());
        metadata.put(DocumentMetadata.RERANK_SCORE, candidate.getScore());
        metadata.put(DocumentMetadata.RERANK_FUSION_SCORE, candidate.getRerankFusionScore());
        metadata.put(DocumentMetadata.RERANK_VARIANT_HIT_COUNT, candidate.getHitCount());
        metadata.put(DocumentMetadata.BEST_RERANK_VARIANT_RANK, candidate.getBestRerankRank());
        metadata.put(DocumentMetadata.RERANK_VARIANT_INDEXES, candidate.getQueryVariantIndexes());
        metadata.put(DocumentMetadata.RERANK_VARIANT_HITS, candidate.getRerankVariantHits());
        if (RerankQueryPolicy.FUSION_AWARE_RERANK_RRF == rerankQueryPolicy) {
            metadata.put(DocumentMetadata.QUERY_FUSION_BOOST_SCORE, candidate.getQueryFusionBoostScore());
            metadata.put(DocumentMetadata.FUSION_AWARE_SCORE, candidate.getScore());
            metadata.put(DocumentMetadata.FUSION_AWARE_WEIGHT, DEFAULT_QUERY_FUSION_RERANK_WEIGHT);
        }

        return document.mutate()
                .metadata(metadata)
                .score(candidate.getScore())
                .build();
    }

    private String firstRerankFailureReason(List<RerankResult> rerankResults) {
        if (CollectionUtils.isEmpty(rerankResults)) {
            return null;
        }
        for (RerankResult rerankResult : rerankResults) {
            if (rerankResult != null && StringUtils.isNotBlank(rerankResult.getFailureReason())) {
                return rerankResult.getFailureReason();
            }
        }
        return null;
    }

    private String firstRerankModelName(List<RerankResult> rerankResults) {
        if (CollectionUtils.isEmpty(rerankResults)) {
            return null;
        }
        for (RerankResult rerankResult : rerankResults) {
            if (rerankResult != null && StringUtils.isNotBlank(rerankResult.getModelName())) {
                return rerankResult.getModelName();
            }
        }
        return null;
    }

    private String firstRerankEndpoint(List<RerankResult> rerankResults) {
        if (CollectionUtils.isEmpty(rerankResults)) {
            return null;
        }
        for (RerankResult rerankResult : rerankResults) {
            if (rerankResult != null && StringUtils.isNotBlank(rerankResult.getEndpoint())) {
                return rerankResult.getEndpoint();
            }
        }
        return null;
    }

    private CoverageGuardResult applyRerankCoverageGuard(
            List<Document> rerankDocuments,
            List<Document> candidates,
            RerankResult rerankResult,
            int topK,
            String userText,
            List<String> queryVariants,
            RewriteResult rewriteResult) {
        List<Document> finalDocuments = CollectionUtils.isEmpty(rerankDocuments) ? List.of() : rerankDocuments;
        if (rerankResult == null || !rerankResult.isApplied()
                || CollectionUtils.isEmpty(finalDocuments)
                || CollectionUtils.isEmpty(candidates)
                || topK <= 0) {
            return new CoverageGuardResult(finalDocuments, false, 0);
        }

        List<String> profileHints = rewriteResult == null
                ? List.of()
                : rewriteResult.getSelectedProfileHints();
        ContextSalienceSupport.Analysis evidenceIntent = ContextSalienceSupport.analyzeIntent(
                userText, queryVariants, profileHints);
        Map<String, CoverageGuardCandidate> protectedCandidates = coverageGuardProtectedCandidates(
                candidates, finalDocuments, evidenceIntent);
        if (protectedCandidates.isEmpty()) {
            return new CoverageGuardResult(finalDocuments, false, 0);
        }

        Set<String> protectedKeys = new HashSet<>(protectedCandidates.keySet());
        List<Document> guardedDocuments = new ArrayList<>(finalDocuments.subList(0, Math.min(topK, finalDocuments.size())));
        Set<String> presentKeys = new HashSet<>();
        for (Document document : guardedDocuments) {
            String key = coverageGuardDocumentKey(document);
            if (StringUtils.isNotBlank(key)) {
                presentKeys.add(key);
            }
        }

        int addedCount = 0;
        String rerankMode = StringUtils.defaultIfBlank(rerankResult.getMode(), RERANK_MODE_PASSTHROUGH);
        for (Map.Entry<String, CoverageGuardCandidate> entry : protectedCandidates.entrySet()) {
            if (presentKeys.contains(entry.getKey())) {
                continue;
            }

            int rank;
            Document guardedDocument;
            CoverageGuardCandidate protectedCandidate = entry.getValue();
            if (guardedDocuments.size() < topK) {
                rank = guardedDocuments.size() + 1;
                guardedDocument = markCoverageGuardAdded(protectedCandidate.document(), rank,
                        rerankResult.isApplied(), rerankMode, protectedCandidate.reason(), protectedCandidate.cues());
                guardedDocuments.add(guardedDocument);
            } else {
                int replacementIndex = findCoverageGuardReplacementIndex(guardedDocuments, protectedKeys);
                if (replacementIndex < 0) {
                    continue;
                }
                Document replacedDocument = guardedDocuments.get(replacementIndex);
                String replacedKey = coverageGuardDocumentKey(replacedDocument);
                if (StringUtils.isNotBlank(replacedKey)) {
                    presentKeys.remove(replacedKey);
                }
                rank = replacementIndex + 1;
                guardedDocument = markCoverageGuardAdded(protectedCandidate.document(), rank,
                        rerankResult.isApplied(), rerankMode, protectedCandidate.reason(), protectedCandidate.cues());
                guardedDocuments.set(replacementIndex, guardedDocument);
            }

            presentKeys.add(entry.getKey());
            addedCount++;
        }

        if (addedCount == 0) {
            return new CoverageGuardResult(finalDocuments, false, 0);
        }
        return new CoverageGuardResult(guardedDocuments, true, addedCount);
    }

    private Map<String, CoverageGuardCandidate> coverageGuardProtectedCandidates(
            List<Document> candidates,
            List<Document> finalDocuments,
            ContextSalienceSupport.Analysis evidenceIntent) {
        Map<String, CoverageGuardCandidate> protectedCandidates = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(candidates)) {
            return protectedCandidates;
        }

        Set<String> finalDocumentKeys = coverageGuardDocumentKeys(finalDocuments);
        addQueryVariantCoverageGuardCandidates(protectedCandidates, candidates);
        addEvidenceCoverageGuardCandidates(protectedCandidates, candidates, finalDocuments, finalDocumentKeys, evidenceIntent);
        return protectedCandidates;
    }

    private void addQueryVariantCoverageGuardCandidates(
            Map<String, CoverageGuardCandidate> protectedCandidates,
            List<Document> candidates) {
        for (Document candidate : candidates) {
            Integer queryVariantIndex = metadataInteger(candidate, DocumentMetadata.QUERY_VARIANT_INDEX);
            Integer queryVariantRank = metadataInteger(candidate, DocumentMetadata.QUERY_VARIANT_RANK);
            if (queryVariantIndex == null || queryVariantRank == null) {
                continue;
            }
            if (queryVariantIndex <= 1 || queryVariantRank > COVERAGE_GUARD_PROTECTED_VARIANT_RANK) {
                continue;
            }

            String key = coverageGuardDocumentKey(candidate);
            if (StringUtils.isNotBlank(key)) {
                protectedCandidates.putIfAbsent(
                        key,
                        new CoverageGuardCandidate(candidate, COVERAGE_GUARD_REASON_QUERY_VARIANT, List.of())
                );
            }
        }
    }

    private void addEvidenceCoverageGuardCandidates(
            Map<String, CoverageGuardCandidate> protectedCandidates,
            List<Document> candidates,
            List<Document> finalDocuments,
            Set<String> finalDocumentKeys,
            ContextSalienceSupport.Analysis evidenceIntent) {
        if (CollectionUtils.isEmpty(ContextSalienceSupport.coverageCues(evidenceIntent))) {
            return;
        }
        Set<String> finalSourcePaths = sourcePaths(finalDocuments);
        if (finalSourcePaths.isEmpty()) {
            return;
        }

        int addedCount = 0;
        for (int i = 0; i < candidates.size() && i < EVIDENCE_COVERAGE_GUARD_MAX_CANDIDATE_RANK; i++) {
            Document candidate = candidates.get(i);
            String key = coverageGuardDocumentKey(candidate);
            if (StringUtils.isBlank(key) || protectedCandidates.containsKey(key)
                    || finalDocumentKeys.contains(key)
                    || !finalSourcePaths.contains(sourcePath(candidate))) {
                continue;
            }

            ContextSalienceSupport.Analysis evidenceAnalysis = ContextSalienceSupport.analyze(candidate.getText());
            if (!ContextSalienceSupport.matchesCoverageIntent(evidenceIntent, evidenceAnalysis)) {
                continue;
            }

            protectedCandidates.put(
                    key,
                    new CoverageGuardCandidate(
                            candidate,
                            COVERAGE_GUARD_REASON_EVIDENCE_TYPE,
                            ContextSalienceSupport.coverageCues(evidenceAnalysis)
                    )
            );
            addedCount++;
            if (addedCount >= EVIDENCE_COVERAGE_GUARD_MAX_ADDED) {
                return;
            }
        }
    }

    private Set<String> coverageGuardDocumentKeys(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return Set.of();
        }
        Set<String> keys = new HashSet<>();
        for (Document document : documents) {
            String key = coverageGuardDocumentKey(document);
            if (StringUtils.isNotBlank(key)) {
                keys.add(key);
            }
        }
        return keys;
    }

    private int findCoverageGuardReplacementIndex(List<Document> documents, Set<String> protectedKeys) {
        for (int i = documents.size() - 1; i >= 0; i--) {
            String key = coverageGuardDocumentKey(documents.get(i));
            if (StringUtils.isBlank(key) || !protectedKeys.contains(key)) {
                return i;
            }
        }
        return -1;
    }

    private Set<String> sourcePaths(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return Set.of();
        }
        Set<String> sourcePaths = new HashSet<>();
        for (Document document : documents) {
            String sourcePath = sourcePath(document);
            if (StringUtils.isNotBlank(sourcePath)) {
                sourcePaths.add(sourcePath);
            }
        }
        return sourcePaths;
    }

    private String sourcePath(Document document) {
        if (document == null || MapUtils.isEmpty(document.getMetadata())) {
            return StringUtils.EMPTY;
        }
        return metadataText(document.getMetadata(), DocumentMetadata.SOURCE_PATH);
    }

    private Document markCoverageGuardAdded(Document document,
                                            int rerankRank,
                                            boolean rerankApplied,
                                            String rerankMode,
                                            String coverageGuardReason,
                                            List<String> coverageGuardCues) {
        Map<String, Object> metadata = new HashMap<>(document.getMetadata());
        metadata.put(DocumentMetadata.RERANK_RANK, rerankRank);
        metadata.put(DocumentMetadata.RERANK_APPLIED, rerankApplied);
        metadata.put(DocumentMetadata.RERANK_MODE, StringUtils.defaultIfBlank(rerankMode, RERANK_MODE_PASSTHROUGH));
        metadata.put(DocumentMetadata.COVERAGE_GUARD_ADDED, true);
        metadata.put(DocumentMetadata.COVERAGE_GUARD_REASON, StringUtils.defaultString(coverageGuardReason));
        if (!CollectionUtils.isEmpty(coverageGuardCues)) {
            metadata.put(DocumentMetadata.COVERAGE_GUARD_CUES, coverageGuardCues);
        }
        return document.mutate()
                .metadata(metadata)
                .build();
    }

    private Integer metadataInteger(Document document, String key) {
        if (document == null || MapUtils.isEmpty(document.getMetadata())) {
            return null;
        }
        Object value = document.getMetadata().get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null || StringUtils.isBlank(value.toString())) {
            return null;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String coverageGuardDocumentKey(Document document) {
        String key = documentKey(document);
        if (StringUtils.isNotBlank(key)) {
            return key;
        }
        if (document == null) {
            return StringUtils.EMPTY;
        }
        return "text#" + Objects.hashCode(document.getText());
    }

    private Document toRrfDocument(RrfDocumentCandidate candidate, String keywordSkippedReason) {
        Document document = candidate.getDocument();
        Map<String, Object> metadata = new HashMap<>(document.getMetadata());
        metadata.put(DocumentMetadata.RETRIEVAL_MODE, RETRIEVAL_MODE_HYBRID);
        metadata.put(DocumentMetadata.RETRIEVAL_SOURCE, candidate.retrievalSource());
        metadata.put(DocumentMetadata.RRF_SCORE, candidate.getScore());
        metadata.put(DocumentMetadata.RRF_K, retrievalOptions.effectiveRrfK());
        addMetadataIfPresent(metadata, DocumentMetadata.KEYWORD_SKIPPED_REASON, keywordSkippedReason);
        addMetadataIfPresent(metadata, DocumentMetadata.VECTOR_RANK, candidate.getVectorRank());
        addMetadataIfPresent(metadata, DocumentMetadata.VECTOR_SCORE, candidate.getVectorScore());
        addMetadataIfPresent(metadata, DocumentMetadata.KEYWORD_RANK, candidate.getKeywordRank());
        addMetadataIfPresent(metadata, DocumentMetadata.KEYWORD_SCORE, candidate.getKeywordScore());

        return document.mutate()
                .metadata(metadata)
                .score(candidate.getScore())
                .build();
    }

    private Document toQueryFusionDocument(QueryFusionDocumentCandidate candidate, int fusionRank) {
        Document document = candidate.getDocument();
        Map<String, Object> metadata = new HashMap<>(document.getMetadata());
        metadata.put(DocumentMetadata.QUERY_FUSION_SCORE, candidate.getScore());
        metadata.put(DocumentMetadata.QUERY_FUSION_RANK, fusionRank);
        metadata.put(DocumentMetadata.QUERY_VARIANT_HIT_COUNT, candidate.getHitCount());
        metadata.put(DocumentMetadata.BEST_QUERY_VARIANT_RANK, candidate.getBestQueryVariantRank());
        metadata.put(DocumentMetadata.QUERY_VARIANT_INDEXES, candidate.getQueryVariantIndexes());
        metadata.put(DocumentMetadata.QUERY_VARIANT_HITS, candidate.getQueryVariantHits());

        return document.mutate()
                .metadata(metadata)
                .score(candidate.getScore())
                .build();
    }

    private void addMetadataIfPresent(Map<String, Object> metadata, String key, Object value) {
        if (value != null) {
            metadata.put(key, value);
        }
    }

    private double calculateScore(int rank) {
        BigDecimal score = BigDecimal.ONE.divide(
                BigDecimal.valueOf(retrievalOptions.effectiveRrfK() + rank),
                5,
                RoundingMode.HALF_UP
        );
        return score.doubleValue();
    }

    private String documentKey(Document document) {
        if (document == null) {
            return StringUtils.EMPTY;
        }
        if (StringUtils.isNotBlank(document.getId())) {
            return document.getId();
        }

        Map<String, Object> metadata = document.getMetadata();
        if (MapUtils.isEmpty(metadata)) {
            return StringUtils.EMPTY;
        }

        Object sourcePath = metadata.get(DocumentMetadata.SOURCE_PATH);
        Object chunkIndex = metadata.get(DocumentMetadata.CHUNK_INDEX);
        if (sourcePath == null || chunkIndex == null) {
            return StringUtils.EMPTY;
        }

        String sourcePathText = sourcePath.toString();
        String chunkIndexText = chunkIndex.toString();
        if (StringUtils.isBlank(sourcePathText) || StringUtils.isBlank(chunkIndexText)) {
            return StringUtils.EMPTY;
        }

        return sourcePathText + "#" + chunkIndexText;
    }

    private record RetrievalTopKPlan(
            int finalTopK,
            int candidateTopK,
            int vectorTopK,
            int keywordTopK
    ) {
    }

    private record CoverageGuardResult(
            List<Document> documents,
            boolean applied,
            int addedCount
    ) {
    }

    private record CoverageGuardCandidate(
            Document document,
            String reason,
            List<String> cues
    ) {
    }

    @Getter
    private static class RerankQueryPlan {
        private final String policy;
        private final String originQueryText;
        private final Set<String> queryTextSet;

        private RerankQueryPlan(String policy, String originQueryText) {
            this(policy, originQueryText, Collections.emptySet());
        }

        private RerankQueryPlan(String policy, String originQueryText, Set<String> queryTextSet) {
            this.policy = policy;
            this.originQueryText = originQueryText;
            this.queryTextSet = new LinkedHashSet<>();
            addQueryVariant(this.queryTextSet, this.originQueryText);
            this.queryTextSet.addAll(queryTextSet);
        }
    }

    private static class QueryFusionDocumentCandidate {
        private final String key;
        private final Document document;
        private final Map<Integer, QueryVariantHit> queryVariantHits = new LinkedHashMap<>();
        private double score;
        private int bestQueryVariantRank = Integer.MAX_VALUE;
        private int firstQueryVariantIndex = Integer.MAX_VALUE;

        private QueryFusionDocumentCandidate(String key, Document document) {
            this.key = key;
            this.document = document;
        }

        private String getKey() {
            return key;
        }

        private Document getDocument() {
            return document;
        }

        private double getScore() {
            return score;
        }

        private int getBestQueryVariantRank() {
            return bestQueryVariantRank;
        }

        private int getFirstQueryVariantIndex() {
            return firstQueryVariantIndex;
        }

        private int getHitCount() {
            return queryVariantHits.size();
        }

        private List<Integer> getQueryVariantIndexes() {
            return List.copyOf(queryVariantHits.keySet());
        }

        private List<Map<String, Object>> getQueryVariantHits() {
            List<Map<String, Object>> hits = new ArrayList<>(queryVariantHits.size());
            for (QueryVariantHit hit : queryVariantHits.values()) {
                Map<String, Object> values = new LinkedHashMap<>();
                values.put("index", hit.index());
                values.put("rank", hit.rank());
                values.put("text", hit.text());
                hits.add(values);
            }
            return hits;
        }

        private void addScore(double score) {
            this.score += score;
        }

        private void recordHit(Document document, int fallbackRank) {
            Map<String, Object> metadata = document.getMetadata();
            Integer variantIndex = metadataInteger(metadata, DocumentMetadata.QUERY_VARIANT_INDEX);
            Integer variantRank = metadataInteger(metadata, DocumentMetadata.QUERY_VARIANT_RANK);
            String variantText = metadataText(metadata, DocumentMetadata.QUERY_VARIANT_TEXT);
            int effectiveRank = variantRank == null ? fallbackRank : variantRank;

            if (variantIndex != null) {
                QueryVariantHit existing = queryVariantHits.get(variantIndex);
                if (existing == null || effectiveRank < existing.rank()) {
                    queryVariantHits.put(variantIndex, new QueryVariantHit(variantIndex, effectiveRank, variantText));
                }
                firstQueryVariantIndex = Math.min(firstQueryVariantIndex, variantIndex);
            }
            bestQueryVariantRank = Math.min(bestQueryVariantRank, effectiveRank);
        }
    }

    private record QueryVariantHit(
            int index,
            int rank,
            String text
    ) {
    }

    private static class RerankFusionDocumentCandidate {
        private final String key;
        private Document document;
        private final Map<Integer, Integer> rerankVariantRanks = new LinkedHashMap<>();
        private double rerankFusionScore;
        private double queryFusionBoostScore;
        private int bestRerankRank = Integer.MAX_VALUE;
        private int firstQueryVariantIndex = Integer.MAX_VALUE;

        private RerankFusionDocumentCandidate(String key, Document document) {
            this.key = key;
            this.document = document;
        }

        private String getKey() {
            return key;
        }

        private Document getDocument() {
            return document;
        }

        private double getScore() {
            return rerankFusionScore + queryFusionBoostScore;
        }

        private double getRerankFusionScore() {
            return rerankFusionScore;
        }

        private double getQueryFusionBoostScore() {
            return queryFusionBoostScore;
        }

        private int getBestRerankRank() {
            return bestRerankRank;
        }

        private int getFirstQueryVariantIndex() {
            return firstQueryVariantIndex;
        }

        private int getHitCount() {
            return rerankVariantRanks.size();
        }

        private List<Integer> getQueryVariantIndexes() {
            return List.copyOf(rerankVariantRanks.keySet());
        }

        private List<Map<String, Object>> getRerankVariantHits() {
            List<Map<String, Object>> hits = new ArrayList<>(rerankVariantRanks.size());
            for (Map.Entry<Integer, Integer> entry : rerankVariantRanks.entrySet()) {
                Map<String, Object> values = new LinkedHashMap<>();
                values.put("index", entry.getKey());
                values.put("rank", entry.getValue());
                hits.add(values);
            }
            return hits;
        }

        private void addScore(double score) {
            this.rerankFusionScore += score;
        }

        private void setQueryFusionBoostScore(double queryFusionBoostScore) {
            this.queryFusionBoostScore = queryFusionBoostScore;
        }

        private void recordHit(Document document, int queryVariantIndex, int rerankRank) {
            Integer existingRank = rerankVariantRanks.get(queryVariantIndex);
            if (existingRank == null || rerankRank < existingRank) {
                rerankVariantRanks.put(queryVariantIndex, rerankRank);
            }
            firstQueryVariantIndex = Math.min(firstQueryVariantIndex, queryVariantIndex);
            if (rerankRank < bestRerankRank) {
                bestRerankRank = rerankRank;
                this.document = document;
            }
        }
    }

    private static class RrfDocumentCandidate {
        private final Document document;
        private double score;
        private Integer vectorRank;
        private Double vectorScore;
        private Integer keywordRank;
        private Double keywordScore;

        private RrfDocumentCandidate(Document document) {
            this.document = document;
        }

        private Document getDocument() {
            return document;
        }

        private double getScore() {
            return score;
        }

        private Integer getVectorRank() {
            return vectorRank;
        }

        private Double getVectorScore() {
            return vectorScore;
        }

        private Integer getKeywordRank() {
            return keywordRank;
        }

        private Double getKeywordScore() {
            return keywordScore;
        }

        private void addScore(double score) {
            this.score += score;
        }

        private void recordSource(String retrievalSource, int rank, Double sourceScore) {
            if (RETRIEVAL_SOURCE_VECTOR.equals(retrievalSource)) {
                vectorRank = rank;
                vectorScore = sourceScore;
                return;
            }
            if (RETRIEVAL_SOURCE_KEYWORD.equals(retrievalSource)) {
                keywordRank = rank;
                keywordScore = sourceScore;
            }
        }

        private String retrievalSource() {
            if (vectorRank != null && keywordRank != null) {
                return RETRIEVAL_SOURCE_VECTOR_KEYWORD;
            }
            if (vectorRank != null) {
                return RETRIEVAL_SOURCE_VECTOR;
            }
            return RETRIEVAL_SOURCE_KEYWORD;
        }
    }

    private static String metadataText(Map<String, Object> metadata, String key) {
        if (MapUtils.isEmpty(metadata)) {
            return StringUtils.EMPTY;
        }
        Object value = metadata.get(key);
        return value == null ? StringUtils.EMPTY : value.toString();
    }

    private static Integer metadataInteger(Map<String, Object> metadata, String key) {
        if (MapUtils.isEmpty(metadata)) {
            return null;
        }
        Object value = metadata.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null || StringUtils.isBlank(value.toString())) {
            return null;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }


    /**
     * Logic to be executed after the rest of the advisor chain is called.
     */
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        if (chatClientResponse == null || chatClientResponse.chatResponse() == null) {
            return chatClientResponse;
        }
        Map<String, Object> responseContext = chatClientResponse.context() == null
                ? Map.of()
                : chatClientResponse.context();

        ChatResponse.Builder responseBuilder = ChatResponse.builder().from(chatClientResponse.chatResponse());
        responseContext.forEach((key, value) -> {
            if (StringUtils.startsWith(key, RagObservationKeys.QA_PREFIX) && value != null) {
                responseBuilder.metadata(key, value);
            }
        });

        return ChatClientResponse.builder()
                .chatResponse(responseBuilder.build())
                .context(responseContext)
                .build();
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(before(chatClientRequest, callAdvisorChain));
        return after(chatClientResponse, callAdvisorChain);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        return BaseAdvisor.super.adviseStream(chatClientRequest, streamAdvisorChain);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private Filter.Expression doGetFilterExpression(Map<String, Object> context) {
        return (context.containsKey(Qa.FILTER_EXPRESSION) &&
                StringUtils.isNotBlank(context.get(Qa.FILTER_EXPRESSION).toString()))
                ? (new FilterExpressionTextParser().parse(context.get(Qa.FILTER_EXPRESSION).toString()))
                : searchRequest.getFilterExpression();

    }


    // V1 先使用字符预算，后续可替换为 token 预算或语义边界裁剪
    private RenderedDocumentContext renderDocumentContext(List<Document> documents, int maxContextChars) {
        StringBuilder builder = new StringBuilder();
        LinkedHashSet<String> salienceCues = new LinkedHashSet<>();
        Set<String> renderedSourceChunkKeys = sourceChunkKeys(documents);
        boolean truncated = false;
        int selectedCount = 0;
        int salienceExpansionCount = 0;
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            //score ≈ 1 - distance 用于判断召回结果是否足够可信 distance位于metadata中
            log.info("RAG召回文档: index={}, score={}, metadata={}, preview={}",
                    i + 1,
                    document.getScore(),
                    document.getMetadata(),
                    StringUtils.abbreviate(document.getText(), 200));
            // Step 3.3 渲染端追加 chunk 来源行（PLAN.md §9.1 落点 C），让 LLM 引用 "[i+1]" 时能看到溯源
            String sourceLine = buildSourceLine(document.getMetadata());
            String salienceLine = "";
            String adjacentContext = "";
            if (retrievalOptions.isContextSalienceEnabled()) {
                ContextSalienceSupport.Analysis salienceAnalysis = ContextSalienceSupport.analyze(document.getText());
                salienceCues.addAll(salienceAnalysis.cues());
                salienceLine = ContextSalienceSupport.renderCueLine(salienceAnalysis);
                adjacentContext = buildAdjacentSalienceContext(document, salienceAnalysis, renderedSourceChunkKeys);
                if (StringUtils.isNotBlank(adjacentContext)) {
                    salienceExpansionCount++;
                }
            }
            String renderedDocument = "["
                    + (i + 1)
                    + "]"
                    + (sourceLine.isEmpty() ? "" : " " + sourceLine)
                    + (salienceLine.isEmpty() ? "" : " " + salienceLine)
                    + System.lineSeparator()
                    + adjacentContext
                    + document.getText()
                    + System.lineSeparator()
                    + System.lineSeparator();

            int remainingChars = maxContextChars - builder.length();
            String safeRenderedDocument = truncateChunkIfNeeded(renderedDocument, remainingChars);
            if (StringUtils.isBlank(safeRenderedDocument)) {
                break;
            }
            selectedCount++;
            builder.append(safeRenderedDocument);
            if (safeRenderedDocument.length() < renderedDocument.length()) {
                //被截断就不用进行下一轮了
                truncated = true;
                break;
            }
        }
        return new RenderedDocumentContext(
                builder.toString(),
                selectedCount,
                documents.size() - selectedCount,
                truncated,
                List.copyOf(salienceCues),
                salienceExpansionCount
        );


    }

    private String buildAdjacentSalienceContext(Document document,
                                                ContextSalienceSupport.Analysis salienceAnalysis,
                                                Set<String> renderedSourceChunkKeys) {
        if (salienceAnalysis == null || !salienceAnalysis.needsPreviousChunkContext()) {
            return "";
        }
        Optional<Document> previousChunk = findPreviousSourceChunk(document);
        if (previousChunk.isEmpty()) {
            return "";
        }
        Document adjacentDocument = previousChunk.get();
        String adjacentKey = sourceChunkKey(adjacentDocument.getMetadata());
        if (StringUtils.isNotBlank(adjacentKey) && renderedSourceChunkKeys.contains(adjacentKey)) {
            return "";
        }
        String sourceLine = buildSourceLine(adjacentDocument.getMetadata());
        return "[相邻证据片段] "
                + (sourceLine.isEmpty() ? "" : sourceLine + " ")
                + "用于还原被 chunk 边界切开的范围/阈值标准："
                + System.lineSeparator()
                + StringUtils.abbreviate(adjacentDocument.getText(), SALIENCE_ADJACENT_MAX_CHARS)
                + System.lineSeparator()
                + "[当前召回片段]"
                + System.lineSeparator();
    }

    private Optional<Document> findPreviousSourceChunk(Document document) {
        if (document == null || MapUtils.isEmpty(document.getMetadata())) {
            return Optional.empty();
        }
        if (vectorStore == null) {
            return Optional.empty();
        }
        Map<String, Object> metadata = document.getMetadata();
        String knowledge = metadataText(metadata, DocumentMetadata.KNOWLEDGE);
        String sourcePath = metadataText(metadata, DocumentMetadata.SOURCE_PATH);
        Integer chunkIndex = metadataInteger(metadata, DocumentMetadata.CHUNK_INDEX);
        if (StringUtils.isAnyBlank(knowledge, sourcePath) || chunkIndex == null || chunkIndex <= 0) {
            return Optional.empty();
        }

        JdbcTemplate template = vectorStore.getNativeClient().filter(JdbcTemplate.class::isInstance)
                .map(JdbcTemplate.class::cast)
                .orElse(null);
        if (template == null) {
            return Optional.empty();
        }

        String sql = """
                SELECT id::text AS id, content, metadata::text AS metadata
                FROM vector_store_openai
                WHERE metadata->>'knowledge' = ?
                  AND metadata->>'sourcePath' = ?
                  AND (metadata->>'chunkIndex')::int = ?
                LIMIT 1
                """;
        RowMapper<Document> rowMapper = (rs, rowNum) -> Document.builder()
                .id(rs.getString("id"))
                .text(rs.getString("content"))
                .metadata(parseMetadata(rs.getString("metadata")))
                .build();
        List<Document> results = template.query(sql, rowMapper, knowledge, sourcePath, chunkIndex - 1);
        return results.stream().findFirst();
    }

    private String truncateChunkIfNeeded(String renderedDocument, int remainingChars) {
        if (renderedDocument.length() <= remainingChars) {
            return renderedDocument;
        }

        if (remainingChars <= CHUNK_TRUNCATED_NOTICE.length()) {
            return "";
        }

        int available = remainingChars - CHUNK_TRUNCATED_NOTICE.length();
        int headChars = Math.min(MIN_CHUNK_HEAD_CHARS, available);

        return renderedDocument.substring(0, headChars)
                + CHUNK_TRUNCATED_NOTICE;
    }

    /**
     * 构造 chunk 来源行（Step 3.3，PLAN.md §9.1 落点 C）。
     * 在多 chunk 上下文里让 LLM 引用 "[1]" / "[2]" 时能同时看到 chunk 来源文档 / 块序号 / 章节路径，
     * 提升 attribution 能力 + 评测时直接定位 chunk-doc 双向关系。
     * <p>
     * Phase A 仅 sourcePath / chunkIndex / totalChunks 有值；
     * parentSection / headingPath 在 Phase A 留空（Phase B 自写 splitter 后才填值），渲染时 graceful 跳过。
     */
    private String buildSourceLine(Map<String, Object> metadata) {
        Object sourcePath = metadata.get(DocumentMetadata.SOURCE_PATH);
        if (sourcePath == null || StringUtils.isBlank(sourcePath.toString())) {
            return "";
        }

        StringBuilder sb = new StringBuilder("(来自: ");
        sb.append(sourcePath);

        Object chunkIndex = metadata.get(DocumentMetadata.CHUNK_INDEX);
        Object totalChunks = metadata.get(DocumentMetadata.TOTAL_CHUNKS);
        if (chunkIndex != null && totalChunks != null) {
            sb.append(", chunk ").append(chunkIndex).append("/").append(totalChunks);
        }

        Object parentSection = metadata.get(DocumentMetadata.PARENT_SECTION);
        if (parentSection != null && StringUtils.isNotBlank(parentSection.toString())) {
            sb.append(", 章节: ").append(parentSection);
        }

        sb.append(")");
        return sb.toString();
    }

    private Set<String> sourceChunkKeys(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return Set.of();
        }
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        for (Document document : documents) {
            if (document == null) {
                continue;
            }
            String key = sourceChunkKey(document.getMetadata());
            if (StringUtils.isNotBlank(key)) {
                keys.add(key);
            }
        }
        return keys;
    }

    private String sourceChunkKey(Map<String, Object> metadata) {
        String sourcePath = metadataText(metadata, DocumentMetadata.SOURCE_PATH);
        Integer chunkIndex = metadataInteger(metadata, DocumentMetadata.CHUNK_INDEX);
        if (StringUtils.isBlank(sourcePath) || chunkIndex == null) {
            return "";
        }
        return sourcePath + "#" + chunkIndex;
    }

    private Double minScore(List<Document> documents) {
        Double minScore = null;
        for (Document document : documents) {
            Double score = document.getScore();
            if (score == null) {
                continue;
            }
            minScore = minScore == null ? score : Math.min(minScore, score);
        }
        return minScore;
    }

    private Double maxScore(List<Document> documents) {
        Double maxScore = null;
        for (Document document : documents) {
            Double score = document.getScore();
            if (score == null) {
                continue;
            }
            maxScore = maxScore == null ? score : Math.max(maxScore, score);
        }
        return maxScore;
    }

    private record RenderedDocumentContext(
            String context,
            int selectedCount,
            int droppedCount,
            boolean truncated,
            List<String> salienceCues,
            int salienceExpansionCount
    ) {
    }


    private List<Document> keywordSearch(String keywordQuery, SearchRequest request) {
        if (StringUtils.isBlank(keywordQuery)) {
            return List.of();
        }

        JdbcTemplate template = vectorStore.getNativeClient().filter(JdbcTemplate.class::isInstance)
                .map(JdbcTemplate.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("向量数据库jdbcTemplate缺失"));

        Filter.Expression filterExpression = request.getFilterExpression();
        String sql = buildKeywordQuerySql(filterExpression != null);
        int keywordTopK = retrievalOptions.effectiveKeywordTopK(request.getTopK());
        Object[] queryArgs = buildKeywordQueryArgs(keywordQuery, filterExpression, keywordTopK);

        RowMapper<VectorKeywordEntity> rowMapper = (rs, rowNum) -> {
            VectorKeywordEntity entity = new VectorKeywordEntity();
            entity.setId(rs.getString("id"));
            entity.setContent(rs.getString("content"));
            entity.setMetaData(rs.getString("metadata"));
            entity.setKeywordScore(rs.getDouble("keyword_score"));
            return entity;
        };

        List<VectorKeywordEntity> results = template.query(sql, rowMapper, queryArgs);

        List<Document> documents = new ArrayList<>();
        for (VectorKeywordEntity result : results) {
            String metaData = result.getMetaData();
            Map<String, Object> metadata = parseMetadata(metaData);
            documents.add(Document.builder()
                    .id(result.getId())
                    .text(result.getContent())
                    .metadata(metadata)
                    .score(result.getKeywordScore())
                    .build());
        }
        return documents;
    }

    private String keywordSkippedReason(String keywordQuery, List<Document> keywordDocuments) {
        if (StringUtils.isBlank(keywordQuery)) {
            return KEYWORD_SKIPPED_REASON_NO_KEYWORD_QUERY;
        }
        if (CollectionUtils.isEmpty(keywordDocuments)) {
            return KEYWORD_SKIPPED_REASON_NO_KEYWORD_RESULTS;
        }
        return null;
    }

    private String buildKeywordQuerySql(boolean hasFilterExpression) {
        StringBuilder sql = new StringBuilder("""
                WITH keyword_query AS (
                    SELECT websearch_to_tsquery('simple', ?) AS fts_query
                )
                SELECT id::text AS id,
                       content,
                       metadata::text AS metadata,
                       ts_rank_cd(to_tsvector('simple', content), keyword_query.fts_query) AS keyword_score
                FROM vector_store_openai, keyword_query
                WHERE to_tsvector('simple', content) @@ keyword_query.fts_query
                """);
        if (hasFilterExpression) {
            sql.append(" AND metadata::jsonb @@ CAST(? AS jsonpath)\n");
        }
        sql.append("""
                ORDER BY keyword_score DESC
                LIMIT ?
                """);
        return sql.toString();
    }

    private Object[] buildKeywordQueryArgs(String keywordQuery, Filter.Expression filterExpression, int keywordTopK) {
        if (filterExpression == null) {
            return new Object[]{keywordQuery, keywordTopK};
        }
        String jsonPathFilter = PG_FILTER_EXPRESSION_CONVERTER.convertExpression(filterExpression);
        return new Object[]{keywordQuery, jsonPathFilter, keywordTopK};
    }

    /**
     * 把用户的自然语言问题，转成 PG FTS 更容易检索的关键词查询串
     *
     * @param userText 用户问题
     * @return 查询字符串
     */
    private String buildKeywordQuery(String userText) {
        if (StringUtils.isBlank(userText)) {
            return StringUtils.EMPTY;
        }

        Set<String> keywords = new LinkedHashSet<>();
        Matcher matcher = KEYWORD_TOKEN_PATTERN.matcher(userText);

        while (matcher.find() && keywords.size() < MAX_KEYWORD_TERMS) {
            addStrongKeyword(keywords, matcher.group());
        }

        if (keywords.isEmpty() && KeyWordPolicy.FALLBACK.equals(retrievalOptions.getKeyWordPolicy())) {
            matcher.reset();
            while (matcher.find() && keywords.size() < MAX_KEYWORD_TERMS) {
                addKeyword(keywords, matcher.group());
            }
        }

        return String.join(" OR ", keywords);
    }

    /**
     * 需要区分泛词和强token
     *
     * @param keywords 集合
     * @param rawToken 原始keyword
     */
    private void addStrongKeyword(Set<String> keywords, String rawToken) {
        if (StringUtils.isBlank(rawToken)) {
            return;
        }

        String token = rawToken.trim();
        if (isStrongKeyword(token)) {
            keywords.add(token);
        }
    }

    /**
     * 用于拆分A_B这种词，原词+拆后的词都作为keyword
     *
     * @param keywords 集合
     * @param rawToken 原始keyword
     */
    private void addKeyword(Set<String> keywords, String rawToken) {
        if (StringUtils.isBlank(rawToken)) {
            return;
        }

        String token = rawToken.trim();
        if (isUsefulKeyword(token)) {
            keywords.add(token);
        }

        for (String part : token.split("[_./-]+")) {
            if (isUsefulKeyword(part)) {
                keywords.add(part);
            }
        }
    }


    private boolean isUsefulKeyword(String token) {
        return StringUtils.isNotBlank(token)
                && token.length() >= MIN_KEYWORD_LENGTH;
    }

    private boolean isStrongKeyword(String token) {
        if (StringUtils.isNotBlank(token) && token.length() >= MIN_STRONG_KEYWORD_LENGTH) {
            return STRONG_TOKEN_PATTERN.matcher(token).matches();
        }
        return false;
    }

    private Map<String, Object> parseMetadata(String metadataJson) {
        if (StringUtils.isBlank(metadataJson)) {
            return Map.of();
        }
        return JSON.parseObject(metadataJson, new TypeReference<>() {
        });
    }


}
