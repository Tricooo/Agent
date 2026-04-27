package com.tricoq.domain.agent.service.armory.node.factory.element;

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
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG 顾问
 * 提供上传文档/个人知识库检索的能力
 *
 * rag基本链路
 * Retrieval：找到了哪些 chunk
 * Context Assembly：怎么把 chunk 放进 prompt
 * Generation：模型怎么基于 chunk 回答
 * Attribution：回答引用了哪些 chunk
 *
 * @author trico qiang
 * @date 10/28/25
 */
public class RagAnswerAdvisor implements BaseAdvisor {

    private final VectorStore vectorStore;
    private final SearchRequest searchRequest;
    private final String userTextAdvisor;

    public RagAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest) {
        this.vectorStore = vectorStore;
        this.searchRequest = searchRequest;
        this.userTextAdvisor = """
                
                Context information is below,Each context chunk is prefixed with a citation number like [1], [2].
                When using context information, prefer mentioning the citation number.
                surrounded by ---------------------
                
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
            //空召回不可静默，是RAG链路重要状态
            HashMap<String, Object> emptyRetrievalContext = new HashMap<>(unmodifiedContext);
            emptyRetrievalContext.put("qa_retrieved_documents", List.of());
            emptyRetrievalContext.put("qa_retrieval_empty", true);
            emptyRetrievalContext.put("question_answer_context", "");

            return ChatClientRequest.builder()
                    .prompt(chatClientRequest.prompt())
                    .context(emptyRetrievalContext)
                    .build();
        }

        //documentContext 很长时要做裁剪/摘要（Top-K、去重、截断），否则可能超长或稀释关键信息
        //编号是建立模型引用的基础，模型可以确定编号，系统也能找到对应的引用---用于解决可溯源
        StringBuilder documentContextBuilder = new StringBuilder();
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            documentContextBuilder
                    .append("[")
                    .append(i + 1)
                    .append("]")
                    .append(System.lineSeparator())
                    .append(document.getText())
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }
        String documentContext = documentContextBuilder.toString();
        Map<String, Object> advisedUserParams = new HashMap<>(unmodifiedContext);
        //给LLM看
        advisedUserParams.put("question_answer_context", documentContext);
        //给人看 便于看到引用的文本
        advisedUserParams.put("qa_retrieved_documents", documents);
        advisedUserParams.put("qa_retrieved_document_count", documents.size());
        advisedUserParams.put("qa_retrieval_empty", false);

        PromptTemplate promptTemplate = new PromptTemplate(advisedUserText);
        String rendered = promptTemplate.render(Map.of("question_answer_context", documentContext));

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
        ChatResponse chatResponse = chatClientResponse.chatResponse();
        if (null == chatResponse) {
            return chatClientResponse;
        }
        ChatResponse response = ChatResponse.builder().from(chatResponse)
                .metadata("qa_retrieved_documents", chatClientResponse.context().get("qa_retrieved_documents")).build();
        return ChatClientResponse.builder()
                .chatResponse(response)
                .context(chatClientResponse.context())
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
                StringUtils.hasText(context.get("qa_filter_expression").toString()))
                ? (new FilterExpressionTextParser().parse(context.get("qa_filter_expression").toString()))
                : searchRequest.getFilterExpression();

    }
}
