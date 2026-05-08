package com.tricoq.domain.agent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    }

}
