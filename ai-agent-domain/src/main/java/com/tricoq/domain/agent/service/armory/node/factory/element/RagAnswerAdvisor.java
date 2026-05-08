package com.tricoq.domain.agent.service.armory.node.factory.element;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.tricoq.domain.agent.model.entity.VectorKeywordEntity;
import com.tricoq.domain.agent.model.valobj.RetrievalOptionsVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.util.CollectionUtils;
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

    private static final int DEFAULT_MAX_CONTEXT_CHARS = 6000;
    private static final String CHUNK_TRUNCATED_NOTICE = "\n...[chunk truncated]...\n";
    private static final int MIN_CHUNK_HEAD_CHARS = 1000;
    private static final String EMPTY_RETRIEVAL_CONTEXT =
            "未检索到满足当前知识库过滤条件和相似度阈值的知识片段。请明确告知用户：当前知识库没有可用上下文，不能基于知识库回答该问题。";

    private static final Pattern KEYWORD_TOKEN_PATTERN = Pattern.compile("[A-Za-z0-9_./]+");
    private static final int MAX_KEYWORD_TERMS = 12;
    private static final int MIN_KEYWORD_LENGTH = 2;
    private static final FilterExpressionConverter PG_FILTER_EXPRESSION_CONVERTER = new PgVectorFilterExpressionConverter();


    public RagAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest) {
        this(vectorStore, searchRequest, new RetrievalOptionsVO());
    }

    public RagAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest, RetrievalOptionsVO retrievalOptions) {
        this.vectorStore = vectorStore;
        this.searchRequest = searchRequest;
        this.retrievalOptions = retrievalOptions == null ? new RetrievalOptionsVO() : retrievalOptions;
        this.userTextAdvisor = """
                
                Context information is below, surrounded by ---------------------
                Each context chunk is prefixed with a citation number like [1], [2].
                When using context information, prefer mentioning the citation number.
                
                ---------------------
                {question_answer_context}
                ---------------------
                
                Given the context and provided history information and not prior knowledge,
                reply to the user comment. If the answer is not in the context, inform
                the user that you can't answer the question.
                """;

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
        //  放 "qa_retrieved_documents" 和 "question_answer_context"，后续的 after() 方法或其他 Advisor 可以从 context 中读取这些数据。
        Map<String, Object> unmodifiedContext = Map.copyOf(chatClientRequest.context());
        Map<String, Object> context = new HashMap<>(unmodifiedContext);

        String userText = chatClientRequest.prompt().getUserMessage().getText();
        String advisedUserText = userText + System.lineSeparator() + userTextAdvisor;

        SearchRequest request = SearchRequest.from(searchRequest).query(userText)
                .filterExpression(doGetFilterExpression(context)).build();
        List<Document> documents = retrieveDocuments(userText, request);
        if (CollectionUtils.isEmpty(documents)) {
            // 空召回不可静默，是 RAG 链路重要状态；同时不能退化成普通聊天，仍要把“无可用上下文”的边界写进 prompt。
            String emptyContext = EMPTY_RETRIEVAL_CONTEXT;
            HashMap<String, Object> emptyRetrievalContext = new HashMap<>(unmodifiedContext);
            emptyRetrievalContext.put("qa_retrieved_documents", List.of());
            emptyRetrievalContext.put("qa_retrieval_empty", true);
            emptyRetrievalContext.put("question_answer_context", emptyContext);
            emptyRetrievalContext.put("qa_retrieved_document_count", 0);
            emptyRetrievalContext.put("qa_context_max_chars", DEFAULT_MAX_CONTEXT_CHARS);
            emptyRetrievalContext.put("qa_context_actual_chars", emptyContext.length());
            emptyRetrievalContext.put("qa_context_selected_count", 0);
            emptyRetrievalContext.put("qa_context_dropped_count", 0);
            emptyRetrievalContext.put("qa_context_truncated", false);
            emptyRetrievalContext.put("qa_similarity_threshold", request.getSimilarityThreshold());

            PromptTemplate promptTemplate = new PromptTemplate(advisedUserText);
            String rendered = promptTemplate.render(Map.of("question_answer_context", emptyContext));

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

        //documentContext 很长时要做裁剪/摘要（Top-K、去重、截断），否则可能超长或稀释关键信息
        //编号是建立模型引用的基础，模型可以确定编号，系统也能找到对应的引用---用于解决可溯源
        RenderedDocumentContext renderedContext = renderDocumentContext(documents, DEFAULT_MAX_CONTEXT_CHARS);
        String documentContext = renderedContext.context();
        Double minScore = minScore(documents);
        Double maxScore = maxScore(documents);
        Map<String, Object> advisedUserParams = new HashMap<>(unmodifiedContext);
        //给LLM看
        advisedUserParams.put("question_answer_context", documentContext);
        advisedUserParams.put("qa_context_max_chars", DEFAULT_MAX_CONTEXT_CHARS);
        advisedUserParams.put("qa_context_actual_chars", documentContext.length());
        advisedUserParams.put("qa_context_selected_count", renderedContext.selectedCount());
        advisedUserParams.put("qa_context_dropped_count", renderedContext.droppedCount());
        advisedUserParams.put("qa_context_truncated", renderedContext.truncated());
        advisedUserParams.put("qa_similarity_threshold", request.getSimilarityThreshold());
        advisedUserParams.put("qa_min_retrieved_score", minScore);
        advisedUserParams.put("qa_max_retrieved_score", maxScore);


        //给人看 便于看到引用的文本
        advisedUserParams.put("qa_retrieved_documents", documents);
        advisedUserParams.put("qa_retrieved_document_count", documents.size());
        advisedUserParams.put("qa_retrieval_empty", false);

        PromptTemplate promptTemplate = new PromptTemplate(advisedUserText);
        String rendered = promptTemplate.render(Map.of("question_answer_context", documentContext));

        log.info("RAG检索结果: query={}, retrieved={}, selected={}, dropped={}, truncated={}, empty={}, similarityThreshold={}, minScore={}, maxScore={}",
                userText,
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

    private List<Document> retrieveDocuments(String userText, SearchRequest request) {
        if (!retrievalOptions.isHybridMode()) {
            return vectorStore.similaritySearch(request);
        }

        SearchRequest vectorRequest = SearchRequest.from(request)
                .topK(retrievalOptions.effectiveVectorTopK(request.getTopK()))
                .build();

        List<Document> vectorDocuments = vectorStore.similaritySearch(vectorRequest);
        List<Document> keywordDocuments = keywordSearch(userText, request);

        return rrMerge(vectorDocuments, keywordDocuments, request.getTopK());
    }

    private List<Document> rrMerge(List<Document> vectorDocuments, List<Document> keywordDocuments, int topK) {
        //score = 1 / (rrfK + rank)
        if (topK <= 0) {
            return List.of();
        }

        Map<String, RrfDocumentCandidate> candidates = new HashMap<>();

        addRrfScores(candidates, vectorDocuments);
        addRrfScores(candidates, keywordDocuments);

        if (CollectionUtils.isEmpty(candidates)) {
            return List.of();
        }

        List<RrfDocumentCandidate> candidateList = new ArrayList<>(candidates.values());
        candidateList.sort(Comparator.comparing(RrfDocumentCandidate::getScore).reversed());

        return candidateList.stream().map(RrfDocumentCandidate::getDocument).limit(topK).toList();
    }

    private void addRrfScores(Map<String, RrfDocumentCandidate> candidates, List<Document> documents) {
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
                candidates.put(key, new RrfDocumentCandidate(document, calculateScore(i + 1)));
                continue;
            }
            candidate.setScore(candidate.getScore() + calculateScore(i + 1));
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
        if (CollectionUtils.isEmpty(metadata)) {
            return StringUtils.EMPTY;
        }

        Object sourcePath = metadata.get("sourcePath");
        Object chunkIndex = metadata.get("chunkIndex");
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

    @Data
    @AllArgsConstructor
    private static class RrfDocumentCandidate {
        private final Document document;
        private double score;
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
        addMetadataIfPresent(responseBuilder, responseContext, "qa_retrieved_documents");
        addMetadataIfPresent(responseBuilder, responseContext, "qa_retrieved_document_count");
        addMetadataIfPresent(responseBuilder, responseContext, "qa_retrieval_empty");
        addMetadataIfPresent(responseBuilder, responseContext, "qa_context_max_chars");
        addMetadataIfPresent(responseBuilder, responseContext, "qa_context_actual_chars");
        addMetadataIfPresent(responseBuilder, responseContext, "qa_context_selected_count");
        addMetadataIfPresent(responseBuilder, responseContext, "qa_context_dropped_count");
        addMetadataIfPresent(responseBuilder, responseContext, "qa_context_truncated");
        addMetadataIfPresent(responseBuilder, responseContext, "qa_similarity_threshold");
        addMetadataIfPresent(responseBuilder, responseContext, "qa_min_retrieved_score");
        addMetadataIfPresent(responseBuilder, responseContext, "qa_max_retrieved_score");

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
        return (context.containsKey("qa_filter_expression") &&
                StringUtils.isNotBlank(context.get("qa_filter_expression").toString()))
                ? (new FilterExpressionTextParser().parse(context.get("qa_filter_expression").toString()))
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
        Object sourcePath = metadata.get("sourcePath");
        if (sourcePath == null || StringUtils.isBlank(sourcePath.toString())) {
            return "";
        }

        StringBuilder sb = new StringBuilder("(来自: ");
        sb.append(sourcePath);

        Object chunkIndex = metadata.get("chunkIndex");
        Object totalChunks = metadata.get("totalChunks");
        if (chunkIndex != null && totalChunks != null) {
            sb.append(", chunk ").append(chunkIndex).append("/").append(totalChunks);
        }

        Object parentSection = metadata.get("parentSection");
        if (parentSection != null && StringUtils.isNotBlank(parentSection.toString())) {
            sb.append(", 章节: ").append(parentSection);
        }

        sb.append(")");
        return sb.toString();
    }

    private void addMetadataIfPresent(ChatResponse.Builder responseBuilder, Map<String, Object> context, String key) {
        Object value = context.get(key);
        if (value != null) {
            responseBuilder.metadata(key, value);
        }
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


    private List<Document> keywordSearch(String userText, SearchRequest request) {
        //从用户问题构建需要查询的keyword字符串
        String keywordQuery = buildKeywordQuery(userText);
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
            addKeyword(keywords, matcher.group());
        }
        return String.join(" OR ", keywords);
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

        for (String part : token.split("[_./]+")) {
            if (isUsefulKeyword(part)) {
                keywords.add(part);
            }
        }
    }


    private boolean isUsefulKeyword(String token) {
        return StringUtils.isNotBlank(token)
                && token.length() >= MIN_KEYWORD_LENGTH;
    }

    private Map<String, Object> parseMetadata(String metadataJson) {
        if (StringUtils.isBlank(metadataJson)) {
            return Map.of();
        }
        return JSON.parseObject(metadataJson, new TypeReference<>() {
        });
    }


}
