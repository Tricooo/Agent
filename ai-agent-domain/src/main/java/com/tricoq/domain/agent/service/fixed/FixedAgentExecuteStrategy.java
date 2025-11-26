package com.tricoq.domain.agent.service.fixed;


import com.tricoq.domain.agent.adapter.repository.IAgentRepository;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.model.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.service.IExecuteStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;


import java.time.LocalDate;
import java.util.List;

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
    public void execute(ExecuteCommandEntity requestParameter, ResponseBodyEmitter emitter) {
        // 1. 获取配置客户端
        List<AiAgentClientFlowConfigDTO> aiAgentClientList = (List<AiAgentClientFlowConfigDTO>) repository
                .queryAiAgentFlowConfigByAgentId(requestParameter.getAgentId()).values();

        // 2. 循环执行客户端
        String content = "";

        for (AiAgentClientFlowConfigDTO config : aiAgentClientList) {
            ChatClient chatClient = getChatClientByClientId(config.getClientId());
            //content 显式传递是为了agent之间的交互
            //使用chat memory存储content是为了维持人和系统的连续性，用户一般只关系最终的执行结果，所以content可以只存最终结果
            //用户请求是需要都存储的
            content = chatClient.prompt(requestParameter.getUserInput() + "，" + content)
                    .system(s -> s.param("current_date", LocalDate.now().toString()))
                    .advisors(a -> a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, requestParameter.getSessionId())
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                    .call().content();

            log.info("智能体对话进行，客户端ID {}", requestParameter.getAgentId());
        }

        log.info("智能体对话请求，结果 {} {}", requestParameter.getAgentId(), content);
    }

    private ChatClient getChatClientByClientId(String clientId) {
        return getBean(AiAgentEnumVO.AI_CLIENT.getBeanName(clientId));
    }

    @SuppressWarnings("unchecked")
    private <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

}
