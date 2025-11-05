package com.tricoq.domain.agent.model.valobj.enums;

import com.tricoq.domain.agent.model.valobj.AiClientAdvisorVO;
import com.tricoq.domain.agent.service.armory.factory.element.RagAnswerAdvisor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.HashMap;
import java.util.Map;

/**
 * @author trico qiang
 * @date 10/28/25
 */
@Getter
@AllArgsConstructor
public enum AiClientAdvisorTypeEnumVO {

    CHAT_MEMORY("ChatMemory", "上下文记忆（内存模式）") {
        @Override
        public Advisor createAdvisor(AiClientAdvisorVO aiClientAdvisorVO, VectorStore vectorStore) {
            return PromptChatMemoryAdvisor.builder(MessageWindowChatMemory.builder()
                            .maxMessages(aiClientAdvisorVO.getChatMemory().getMaxMessages())
                            .build())
                    .build();
        }
    },

    RAG_ANSWER("RagAnswer", "知识库") {
        @Override
        public Advisor createAdvisor(AiClientAdvisorVO aiClientAdvisorVO, VectorStore vectorStore) {
            AiClientAdvisorVO.RagAnswer ragAnswer = aiClientAdvisorVO.getRagAnswer();
            if (ragAnswer == null) {
                return null;
            }
            SearchRequest searchRequest = SearchRequest.builder()
                    .filterExpression(StringUtils.defaultIfEmpty(ragAnswer.getFilterExpression(), StringUtils.EMPTY))
                    .topK(ragAnswer.getTopK())
                    .build();
            return new RagAnswerAdvisor(vectorStore,searchRequest);
        }
    };

    private final String code;
    private final String info;

    private static final Map<String, AiClientAdvisorTypeEnumVO> CACHE = new HashMap<>(2);

    /**
     * 策略方法：创建顾问对象
     *
     * @param aiClientAdvisorVO 顾问配置对象
     * @param vectorStore       向量存储
     * @return 顾问对象
     */
    public abstract Advisor createAdvisor(AiClientAdvisorVO aiClientAdvisorVO, VectorStore vectorStore);

    static {
        for (AiClientAdvisorTypeEnumVO vo : values()) {
            CACHE.put(vo.getCode(), vo);
        }
    }

    /**
     * 根据code获取枚举
     *
     * @param code 编码
     * @return 枚举对象
     */
    public static AiClientAdvisorTypeEnumVO getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            throw new IllegalArgumentException("blank code");
        }
        AiClientAdvisorTypeEnumVO vo = CACHE.get(code);
        if (vo == null) {
            throw new RuntimeException("err! advisorType " + code + " not exist!");
        }
        return vo;
    }
}
