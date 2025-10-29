package com.tricoq.domain.agent.service.armory.factory.element;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
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
                
                Context information is below, surrounded by ---------------------
                
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
        Map<String, Object> unmodifiedContext = Map.copyOf(chatClientRequest.context());
        Map<String, Object> context = new HashMap<>(unmodifiedContext);

        String userText = chatClientRequest.prompt().getUserMessage().getText();
        String advisedUserText = userText + System.lineSeparator() + userTextAdvisor;

        //这里填充userText的占位符，但是目前还没用到
        String query = new PromptTemplate(userText).render();

        SearchRequest request = SearchRequest.from(searchRequest).query(query)
                .filterExpression(doGetFilterExpression(context)).build();
        List<Document> documents = vectorStore.similaritySearch(request);
        if (CollectionUtils.isEmpty(documents)) {
            //未找到引用的文本
            return chatClientRequest;
        }

        //documentContext 很长时要做裁剪/摘要（Top-K、去重、截断），否则可能超长或稀释关键信息
        String documentContext = documents.stream().map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));
        Map<String, Object> advisedUserParams = new HashMap<>(unmodifiedContext);
        //给LLM看
        advisedUserParams.put("question_answer_context", documentContext);
        //给人看 便于看到引用的文本
        advisedUserParams.put("qa_retrieved_documents", documents);

        return ChatClientRequest.builder()
                .prompt(Prompt.builder()
                        .messages(
                                new UserMessage(advisedUserText)
                                //这条可以省去以节省token，以为context已经将占位符替换，模型可以拿到上下文
                                //AssistantMessage 用于在 Prompt 中表示助手（AI 模型）先前“说过”的内容
                                //多加一条助手消息可能会让部分模型“困惑”或影响消息权重
                                //, new AssistantMessage(JSON.toJSONString(advisedUserParams))
                        )
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
