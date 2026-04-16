package com.tricoq.domain.agent.model.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

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
@Slf4j
public class AutoAnalyzeResultDTO {

    public enum TaskStatus {
        /** 任务已完全完成，可跳转总结节点 */
        COMPLETED,
        /** 任务仍需继续执行 */
        CONTINUE
    }

    //Step1 分析节点的结构化结论，表示“分析器认为任务是否已完成” 只由step1产出 离散状态更适合做控制流
    @JsonPropertyDescription("任务状态：COMPLETED 表示任务已完成，CONTINUE 表示需要继续执行")
    private TaskStatus taskStatus;

    //Analyzer 对当前任务完成度的估计值，用于展示、观察、诊断，不作为主控制字段
    @JsonPropertyDescription("完成度百分比，范围 0-100，100 表示完全完成")
    private Integer completionPercent;

    @JsonPropertyDescription("对当前任务状态的简要描述")
    private String statusDescription;

    @JsonPropertyDescription("对执行历史的评估，分析已完成的工作和存在的问题")
    private String historyEvaluation;

    @JsonPropertyDescription("下一步执行策略，为执行节点提供具体的行动方向")
    private String nextStrategy;

    public void validate() {
        if (taskStatus == null) {
            throw new IllegalStateException("AutoAnalyzeResultDTO.taskStatus 不能为空");
        }
        if (completionPercent == null) {
            throw new IllegalStateException("AutoAnalyzeResultDTO.completionPercent 不能为空");
        }
        if (completionPercent < 0 || completionPercent > 100) {
            throw new IllegalStateException("AutoAnalyzeResultDTO.completionPercent 超出范围: " + completionPercent);
        }
        if (taskStatus == TaskStatus.CONTINUE && !StringUtils.hasText(nextStrategy)) {
            throw new IllegalStateException("AutoAnalyzeResultDTO.nextStrategy 不能为空，当 taskStatus=CONTINUE 时必须提供下一步策略");
        }
        if (taskStatus == TaskStatus.COMPLETED && completionPercent < 100) {
            log.warn("taskStatus=COMPLETED 但 completionPercent<100，结构化结果存在语义冲突");
        }

        if (taskStatus == TaskStatus.CONTINUE && completionPercent >= 100) {
            log.warn("taskStatus=CONTINUE 但 completionPercent>=100，结构化结果存在语义冲突");
        }
    }
}
