package com.tricoq.domain.agent.service.armory.node.factory.element;

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
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final int DEFAULT_MAX_CONTEXT_CHARS = 6000;
    private static final String CHUNK_TRUNCATED_NOTICE = "\n...[chunk truncated]...\n";
    private static final int MIN_CHUNK_HEAD_CHARS = 1000;
    private static final String EMPTY_RETRIEVAL_CONTEXT = "未检索到满足当前知识库过滤条件和相似度阈值的知识片段。请明确告知用户：当前知识库没有可用上下文，不能基于知识库回答该问题。";


    public RagAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest) {
        this.vectorStore = vectorStore;
        this.searchRequest = searchRequest;
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
        List<Document> documents = vectorStore.similaritySearch(request);
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
            String renderedDocument = "["
                    + (i + 1)
                    + "]"
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

}
