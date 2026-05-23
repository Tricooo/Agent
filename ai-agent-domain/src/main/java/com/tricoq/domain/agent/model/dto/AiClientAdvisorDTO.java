package com.tricoq.domain.agent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 顾问配置，值对象
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/6/27 18:42
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientAdvisorDTO {

    /**
     * 数据库主键
     */
    private Long id;

    /**
     * 顾问ID
     */
    private String advisorId;

    /**
     * 顾问名称
     */
    private String advisorName;

    /**
     * 顾问类型(PromptChatMemory/RagAnswer/SimpleLoggerAdvisor等)
     */
    private String advisorType;

    /**
     * 扩展参数
     */
    private String extParam;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 顺序号
     */
    private Integer orderNum;

    /**
     * 扩展；记忆
     */
    private ChatMemory chatMemory;

    /**
     * 扩展；rag 问答
     */
    private RagAnswer ragAnswer;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatMemory {
        private int maxMessages;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RagAnswer {
        private int topK = 4;
        //score判断的阈值
        private double similarityThreshold = 0.0;

        /**
         * rerank 候选池召回阈值；<=0 时沿用 similarityThreshold。
         */
        private double candidateSimilarityThreshold = 0.0;

        private String filterExpression;
        /**
         * 检索模式：VECTOR / HYBRID。默认 VECTOR，避免旧 extParam 在升级后行为漂移。
         */
        private String retrievalMode = "VECTOR";
        /**
         * HYBRID 模式下向量召回候选数；<=0 时回退到 topK。
         */
        private int vectorTopK = 0;
        /**
         * HYBRID 模式下 PG FTS 词面召回候选数；<=0 时回退到 topK。
         */
        private int keywordTopK = 0;
        /**
         * Reciprocal Rank Fusion 的平滑常量，常用默认值 60。
         */
        private int rrfK = 60;

        private String rerankPolicy;

        /**
         * rerank query 构造策略：ORIGINAL / ORIGINAL_PLUS_VARIANT / PER_VARIANT_RERANK_RRF /
         * FUSION_AWARE_RERANK_RRF。
         * 默认 ORIGINAL，保证旧行为不漂移。
         */
        private String rerankQueryPolicy;

        private String rewritePolicy;

        private String queryRewriterClientId;

        /**
         * LLM query rewrite 的知识库领域提示词；例如指标名、术语、函数名、证据类型。
         */
        private List<String> queryRewriteDomainHints;

        /**
         * 自动 KnowledgeBaseProfile 在 query-time selection 阶段最多选择的 hint 数量。
         */
        private int queryRewriteProfileHintTopN = 8;

        /**
         * 是否启用自动 KnowledgeBaseProfile hints；默认开启，便于消融实验单独关闭 profile。
         */
        private boolean queryRewriteProfileEnabled = true;

        /**
         * 是否启用 Context Salience 渲染增强；默认开启，便于消融实验按 advisor 配置关闭。
         */
        private boolean contextSalienceEnabled = true;
    }

}
