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
import com.tricoq.domain.agent.service.rag.rewrite.PassthroughQueryRewriter;
import com.tricoq.domain.agent.service.rag.rewrite.QueryRewriter;
import com.tricoq.domain.agent.service.rag.rewrite.enums.RewritePolicy;
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
        this(vectorStore, searchRequest, retrievalOptions, documentReranker, new PassthroughQueryRewriter());
    }

    public RagAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest,
                            RetrievalOptionsVO retrievalOptions, DocumentReranker documentReranker,
                            QueryRewriter queryRewriter) {
        this.vectorStore = vectorStore;
        this.searchRequest = searchRequest;
        this.retrievalOptions = retrievalOptions == null ? new RetrievalOptionsVO() : retrievalOptions;
        this.documentReranker = documentReranker == null ? new PassthroughDocumentReranker() : documentReranker;
        this.queryRewriter = queryRewriter == null ? new PassthroughQueryRewriter() : queryRewriter;
        this.retrievalTopKPlan = buildTopKPlan(searchRequest.getTopK());
        this.userTextAdvisor = """
                
                Context information is below, surrounded by ---------------------
                Each context chunk is prefixed with a citation number like [1], [2].
                When using context information, prefer mentioning the citation number.
                
                ---------------------
                {%s}
                ---------------------
                
                Given the context and provided history information and not prior knowledge,
                reply to the user comment. If the answer is not in the context, inform
                the user that you can't answer the question.
                """.formatted(AdvisorContext.QUESTION_ANSWER_CONTEXT);

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
            emptyRetrievalContext.put(Qa.SIMILARITY_THRESHOLD, request.getSimilarityThreshold());
            emptyRetrievalContext.put(Qa.CANDIDATE_SIMILARITY_THRESHOLD, candidateSimilarityThreshold);
            emptyRetrievalContext.put(Qa.RERANK_APPLIED, false);
            emptyRetrievalContext.put(Qa.RERANK_MODE, RERANK_MODE_PASSTHROUGH);
            emptyRetrievalContext.put(Qa.RERANK_CANDIDATE_COUNT, 0);
            emptyRetrievalContext.put(Qa.RERANK_FINAL_COUNT, 0);
            emptyRetrievalContext.put(Qa.RERANK_FAILURE_REASON, "");
            emptyRetrievalContext.put(Qa.RERANK_MODEL_NAME, "");
            emptyRetrievalContext.put(Qa.RERANK_ENDPOINT, "");

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

        RerankResult rerankResult = rerankDocuments(userText, documents, retrievalTopKPlan.finalTopK());
        List<Document> rerankDocuments = rerankResult.getDocuments();

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
        advisedUserParams.put(Qa.SIMILARITY_THRESHOLD, request.getSimilarityThreshold());
        advisedUserParams.put(Qa.CANDIDATE_SIMILARITY_THRESHOLD, candidateSimilarityThreshold);
        advisedUserParams.put(Qa.MIN_RETRIEVED_SCORE, minScore);
        advisedUserParams.put(Qa.MAX_RETRIEVED_SCORE, maxScore);
        advisedUserParams.put(Qa.RERANK_APPLIED, rerankResult.isApplied());
        advisedUserParams.put(Qa.RERANK_MODE, rerankResult.getMode());
        advisedUserParams.put(Qa.RERANK_CANDIDATE_COUNT, rerankResult.getCandidateCount());
        advisedUserParams.put(Qa.RERANK_FINAL_COUNT, rerankResult.getFinalCount());
        advisedUserParams.put(Qa.RERANK_FAILURE_REASON, StringUtils.defaultString(rerankResult.getFailureReason()));
        advisedUserParams.put(Qa.RERANK_MODEL_NAME, StringUtils.defaultString(rerankResult.getModelName()));
        advisedUserParams.put(Qa.RERANK_ENDPOINT, StringUtils.defaultString(rerankResult.getEndpoint()));


        //给人看 便于看到引用的文本
        advisedUserParams.put(Qa.PRE_RERANK_DOCUMENTS, documents);
        advisedUserParams.put(Qa.RETRIEVED_DOCUMENTS, rerankDocuments);
        advisedUserParams.put(Qa.RETRIEVED_DOCUMENT_COUNT, rerankDocuments.size());
        advisedUserParams.put(Qa.RETRIEVAL_EMPTY, false);

        PromptTemplate promptTemplate = new PromptTemplate(advisedUserText);
        String rendered = promptTemplate.render(Map.of(AdvisorContext.QUESTION_ANSWER_CONTEXT, documentContext));

        log.info("RAG检索结果: query={}, queryVariants={}, retrieved={}, selected={}, dropped={}, truncated={}, empty={}, similarityThreshold={}, minScore={}, maxScore={}",
                userText,
                queryVariants.size(),
                documents.size(),
                renderedContext.selectedCount(),
                renderedContext.droppedCount(),
                renderedContext.truncated(),
                false,
                request.getSimilarityThreshold(),
                minScore,
                maxScore);

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
                .rewriteMode(RewritePolicy.PASSTHROUGH.getPolicyName())
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

    private void addQueryVariant(Set<String> variants, String variant) {
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
        params.put(Qa.QUERY_VARIANT_COUNT, queryVariants.size());
        params.put(Qa.QUERY_VARIANT_TEXTS, queryVariants);
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

        int maxSize = 0;
        for (List<Document> documents : variantResults) {
            if (documents != null) {
                maxSize = Math.max(maxSize, documents.size());
            }
        }

        Map<String, Document> mergedDocuments = new LinkedHashMap<>();
        for (int rank = 0; rank < maxSize && mergedDocuments.size() < topK; rank++) {
            for (List<Document> documents : variantResults) {
                if (CollectionUtils.isEmpty(documents) || rank >= documents.size()) {
                    continue;
                }
                Document document = documents.get(rank);
                String key = mergedDocumentKey(document, rank);
                mergedDocuments.putIfAbsent(key, document);
                if (mergedDocuments.size() >= topK) {
                    break;
                }
            }
        }
        return new ArrayList<>(mergedDocuments.values());
    }

    private String mergedDocumentKey(Document document, int rank) {
        String key = documentKey(document);
        if (StringUtils.isNotBlank(key)) {
            return key;
        }
        return "rank:" + rank + ":text:" + StringUtils.defaultString(document.getText()).hashCode();
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

    private RerankResult rerankDocuments(String query, List<Document> candidates, int topK) {
        return documentReranker.rerank(query, candidates, topK);
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
        boolean truncated = false;
        int selectedCount = 0;
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
            String renderedDocument = "["
                    + (i + 1)
                    + "]"
                    + (sourceLine.isEmpty() ? "" : " " + sourceLine)
                    + System.lineSeparator()
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
                truncated
        );


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
            boolean truncated
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
