package com.tricoq.domain.agent.service.fixed;


import com.alibaba.fastjson2.JSON;
import com.tricoq.domain.agent.adapter.repository.IAgentRepository;
import com.tricoq.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.tricoq.domain.agent.model.entity.AutoAgentRetrievalSseEntity;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.model.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.service.IExecuteStrategy;
import com.tricoq.domain.agent.shared.ExecuteOutputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 固定执行策略
 *
 * @author trico qiang
 * 2025/9/13 15:14
 */
@Slf4j
@Service("fixedAgentExecuteStrategy")
@RequiredArgsConstructor
public class FixedAgentExecuteStrategy implements IExecuteStrategy {

    private final IAgentRepository repository;

    protected final ApplicationContext applicationContext;

    public static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";
    public static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_response_size";

    @Override
    public void execute(ExecuteCommandEntity requestParameter, ExecuteOutputPort port) {
        // 1. 获取配置客户端
        List<AiAgentClientFlowConfigDTO> aiAgentClientList = repository
                .queryAiAgentFlowConfigListByAgentId(requestParameter.getAgentId());

        // 2. 循环执行客户端
        String content = "";

        for (AiAgentClientFlowConfigDTO config : aiAgentClientList) {
            ChatClient chatClient = getChatClientByClientId(config.getClientId());
            //content 显式传递是为了agent之间的交互
            //使用chat memory存储content是为了维持人和系统的连续性，用户一般只关系最终的执行结果，所以content可以只存最终结果
            //用户请求是需要都存储的
            ChatResponse chatResponse = chatClient.prompt(requestParameter.getUserInput() + "，" + content)
                    //spec-配置器 请求级配置-当前调用生效
                    .system(s -> s.param("current_date", LocalDate.now().toString()))
                    .advisors(a -> a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, requestParameter.getSessionId())
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                    .call().chatResponse();
            if (chatResponse == null || chatResponse.getResult() == null
                    || chatResponse.getResult().getOutput() == null) {
                throw new RuntimeException("文本调用结果为空: clientId=" + config.getClientId());
            }
            content = chatResponse.getResult().getOutput().getText();

            // 透出 RAG 检索观测信号：advisor.after() 写入 metadata 的 qa_* 字段，包成 type=retrieval SSE 事件
            emitRetrievalIfPresent(chatResponse, port, requestParameter.getSessionId());

            log.info("智能体对话进行，客户端ID {}", requestParameter.getAgentId());
        }

        AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createSummaryResult(
                content, requestParameter.getSessionId());
        port.send(JSON.toJSONString(result));

        AutoAgentExecuteResultEntity completeResult = AutoAgentExecuteResultEntity.createCompleteResult(
                requestParameter.getSessionId());
        port.send(JSON.toJSONString(completeResult));

        log.info("智能体对话请求，结果 {} {}", requestParameter.getAgentId(), content);
    }

    private ChatClient getChatClientByClientId(String clientId) {
        return getBean(AiAgentEnumVO.AI_CLIENT.getBeanName(clientId));
    }

    @SuppressWarnings("unchecked")
    private <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

    /**
     * 把 ChatResponse.metadata 中的 qa_* 字段拍平成 type=retrieval SSE 事件下发，
     * 供评测 runner 落档 retrieved/score/empty 等列。
     *
     * - metadata 为空、port 为 null、qa_* 过滤后为空时静默跳过；
     * - 异常只 log 不抛，不影响主链路文本输出。
     */
    private void emitRetrievalIfPresent(ChatResponse chatResponse, ExecuteOutputPort port, String sessionId) {
        if (chatResponse == null || port == null) {
            return;
        }
        ChatResponseMetadata metadata = chatResponse.getMetadata();
        if (metadata == null || metadata.isEmpty()) {
            return;
        }
        Map<String, Object> snapshot = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            snapshot.put(entry.getKey(), entry.getValue());
        }
        AutoAgentRetrievalSseEntity entity = AutoAgentRetrievalSseEntity.from(snapshot, sessionId, null);
        if (entity.getData() == null || entity.getData().isEmpty()) {
            return;
        }
        try {
            port.send(JSON.toJSONString(entity));
        } catch (Exception e) {
            log.error("发送 retrieval SSE 事件失败：{}", e.getMessage(), e);
        }
    }

}
