package com.tricoq.domain.agent.model.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Auto Agent 任务分析阶段的结构化输出。
 * 由 Step1AnalyzeNode 通过 Spring AI Structured Output 直接输出，
 * 替代原来的字符串 contains("COMPLETED") 魔法字符串检测方式。
 *
 * @author trico qiang
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AutoAnalyzeResultDTO {

    public enum TaskStatus {
        /** 任务已完全完成，可跳转总结节点 */
        COMPLETED,
        /** 任务仍需继续执行 */
        CONTINUE
    }

    @JsonPropertyDescription("任务状态：COMPLETED 表示任务已完成，CONTINUE 表示需要继续执行")
    private TaskStatus taskStatus;

    @JsonPropertyDescription("完成度百分比，范围 0-100，100 表示完全完成")
    private int completionPercent;

    @JsonPropertyDescription("对当前任务状态的简要描述")
    private String statusDescription;

    @JsonPropertyDescription("对执行历史的评估，分析已完成的工作和存在的问题")
    private String historyEvaluation;

    @JsonPropertyDescription("下一步执行策略，为执行节点提供具体的行动方向")
    private String nextStrategy;
}
