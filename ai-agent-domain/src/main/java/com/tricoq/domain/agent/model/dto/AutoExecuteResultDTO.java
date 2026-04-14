package com.tricoq.domain.agent.model.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * Auto Agent 任务执行阶段的结构化输出。
 * 由 Step2ExecuteNode 通过 Spring AI Structured Output 直接输出，
 * 替代原来的字符串 split + section 关键字匹配解析。
 *
 * @author trico qiang
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AutoExecuteResultDTO {

    @JsonPropertyDescription("本次执行的具体目标")
    private String executionTarget;

    @JsonPropertyDescription("详细的执行过程描述，包括使用的方法和步骤")
    private String executionProcess;

    @JsonPropertyDescription("执行结果，包含具体的输出内容和数据")
    private String executionResult;

    @JsonPropertyDescription("执行结果的初步质量自检，指出潜在的问题或不足")
    private String qualityCheck;

    public void validate() {
        if (!StringUtils.hasText(executionTarget)) {
            throw new IllegalStateException("AutoExecuteResultDTO.executionTarget 不能为空");
        }
        if (!StringUtils.hasText(executionProcess)) {
            throw new IllegalStateException("AutoExecuteResultDTO.executionProcess 不能为空");
        }
        if (!StringUtils.hasText(executionResult)) {
            throw new IllegalStateException("AutoExecuteResultDTO.executionResult 不能为空");
        }
    }
}
