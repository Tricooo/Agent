package com.tricoq.domain.agent.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RAG 检索观测 SSE 事件实体。
 *
 * 由 RagAnswerAdvisor.after() 写入 ChatResponse.metadata 的 qa_* 字段，
 * 经 SpringAiLlmInvocationGateway 的 retrievalObserver 回调，
 * 由 Step 节点透传成 type="retrieval" 的 SSE 事件，
 * 供评测 runner 解析为评测产物的「retrieval」子节。
 *
 * 设计取舍：data 字段以原始 Map 形式承载所有 qa_* key，前端 / runner 直接 JSON 解析；
 * 这样 advisor 以后增减 qa_* 字段，本实体零修改即可跟上。
 *
 * @author trico qiang
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AutoAgentRetrievalSseEntity {

    /**
     * 事件类型，固定 "retrieval"。与 AutoAgentExecuteResultEntity 的
     * type 字段共享前端 SSE 路由约定，但属于独立事件流不会混淆。
     */
    private String type;

    /**
     * 会话 ID，对齐 AutoAgentExecuteResultEntity.sessionId。
     */
    private String sessionId;

    /**
     * 当前 step（Step1=1 / Step2=2 ...），便于 runner 把 retrieval 事件
     * 关联到具体的 Auto 流程阶段。
     */
    private Integer step;

    /**
     * 事件时间戳（ms）。
     */
    private Long timestamp;

    /**
     * 检索观测原始数据：仅保留 ChatResponse.metadata 中以 "qa_" 开头的 key，
     * 过滤掉 Spring AI 默认 metadata（usage / id / model 等）。
     *
     * 当前 11 个 key（来自 RagAnswerAdvisor.after()）：
     * qa_retrieved_documents / qa_retrieved_document_count / qa_retrieval_empty
     * qa_context_max_chars / qa_context_actual_chars / qa_context_selected_count
     * qa_context_dropped_count / qa_context_truncated / qa_similarity_threshold
     * qa_min_retrieved_score / qa_max_retrieved_score
     */
    private Map<String, Object> data;

    /**
     * 从 ChatResponse.metadata 抽取所有 qa_* 字段，构造 retrieval 事件实体。
     *
     * @param metadata  ChatResponse.metadata 转换出的 Map（容器层负责把
     *                  Spring AI 的 ChatResponseMetadata 转成 Map 后再调本方法）
     * @param sessionId 会话 ID
     * @param step      当前 step
     */
    public static AutoAgentRetrievalSseEntity from(Map<String, Object> metadata, String sessionId, Integer step) {
        return AutoAgentRetrievalSseEntity.builder()
                .type("retrieval")
                .sessionId(sessionId)
                .step(step)
                .timestamp(System.currentTimeMillis())
                .data(filterQaFields(metadata))
                .build();
    }

    private static Map<String, Object> filterQaFields(Map<String, Object> metadata) {
        Map<String, Object> filtered = new LinkedHashMap<>();
        if (metadata == null) {
            return filtered;
        }
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            String key = entry.getKey();
            if (key != null && key.startsWith("qa_")) {
                filtered.put(key, entry.getValue());
            }
        }
        return filtered;
    }
}
