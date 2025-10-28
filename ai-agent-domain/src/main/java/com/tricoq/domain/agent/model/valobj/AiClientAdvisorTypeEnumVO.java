package com.tricoq.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;

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
            return null;
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
