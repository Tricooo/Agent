package com.tricoq.test.spring.ai.advisors;

import com.alibaba.fastjson.JSON;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * rag顾问
 */
public class RagAnswerAdvisor implements BaseAdvisor {

    private final VectorStore vectorStore;
    private final SearchRequest searchRequest;
    private final String userTextAdvise;

    public RagAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest) {
        this.vectorStore = vectorStore;
        this.searchRequest = searchRequest;
        //TODO 约束性很强的prompt最好放在SystemMessage合适
        this.userTextAdvise = """
                
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
     * 模型调用前修改请求，加入文档上下文
     *
     * @param chatClientRequest
     * @param advisorChain
     * @return
     */
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        //复制上下文 防止修改原始上下文
        Map<String, Object> context = new HashMap(chatClientRequest.context());
        //提取用户问题
        String userText = chatClientRequest.prompt().getUserMessage().getText();
        String advisedUserText = userText + System.lineSeparator() + this.userTextAdvise;

        String query = (new PromptTemplate(userText)).render();

        //使用用户问题构造一个带有过滤条件的请求
        SearchRequest searchRequestToUse = SearchRequest.from(this.searchRequest).query(query)
                .filterExpression(this.doGetFilterExpression(context)).build();
        List<Document> documents = this.vectorStore.similaritySearch(searchRequestToUse);
        context.put("qa_retrieved_documents", documents);

        //documentContext 很长时要做裁剪/摘要（Top-K、去重、截断），否则可能超长或稀释关键信息
        String documentContext = documents.stream().map(Document::getText).collect(Collectors.joining(System.lineSeparator()));
        Map<String, Object> advisedUserParams = new HashMap(chatClientRequest.context());
        advisedUserParams.put("question_answer_context", documentContext);

        return ChatClientRequest.builder()
                .prompt(Prompt.builder().messages(new UserMessage(advisedUserText),
                        //这条可以省去以节省token，以为context已经将占位符替换，模型可以拿到上下文
                        //AssistantMessage 用于在 Prompt 中表示助手（AI 模型）先前“说过”的内容
                        new AssistantMessage(JSON.toJSONString(advisedUserParams)))
                        .build())
                //这里会替换掉占位符为真实文本
                .context(advisedUserParams)
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        ChatResponse.Builder chatResponseBuilder = ChatResponse.builder().from(chatClientResponse.chatResponse());
        chatResponseBuilder.metadata("qa_retrieved_documents", chatClientResponse.context().get("qa_retrieved_documents"));
        ChatResponse chatResponse = chatResponseBuilder.build();

        return ChatClientResponse.builder()
                .chatResponse(chatResponse)
                .context(chatClientResponse.context())
                .build();
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(this.before(chatClientRequest, callAdvisorChain));
        return this.after(chatClientResponse, callAdvisorChain);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        return BaseAdvisor.super.adviseStream(chatClientRequest, streamAdvisorChain);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    protected Filter.Expression doGetFilterExpression(Map<String, Object> context) {
        return context.containsKey("qa_filter_expression") &&
                StringUtils.hasText(context.get("qa_filter_expression").toString()) ?
                (new FilterExpressionTextParser()).parse(context.get("qa_filter_expression").toString()) :
                this.searchRequest.getFilterExpression();
    }

}
