package com.tricoq.domain.agent.model.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Flow 执行步骤的结构化定义。
 * 由 Step2（规划节点）通过 Spring AI Structured Output 直接输出，
 * 替代原来的 Markdown + 正则解析链路。
 *
 * @author trico qiang
 * @date 4/10/26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlowStepDTO {

    @JsonPropertyDescription("步骤序号，从1开始")
    private Integer stepNo;

    @JsonPropertyDescription("步骤标题，简短描述这一步做什么")
    private String title;

    @JsonPropertyDescription("步骤目标，说明这一步要达成什么结果")
    private String goal;

    @JsonPropertyDescription("建议使用的MCP工具名称，必须与可用工具列表中的名称完全匹配")
    private String toolHint;

    @JsonPropertyDescription("给执行器的精炼指令，包含具体的执行方法和参数说明")
    private String executionInstruction;

    @JsonPropertyDescription("预期产出描述，说明这一步应该输出什么")
    private String expectedOutput;

    //为并行执行埋好了结构基础如果要支持并行，dependsOn 字段已经描述了步骤间的 DAG 依赖关系，可以通过拓扑排序找出可以并行的步骤，用
    //CompletableFuture.allOf 并发执行同层步骤
    @JsonPropertyDescription("当前步骤依赖的前置步骤编号列表，为空或null表示不依赖任何步骤，" +
            "例如[1,2]表示需要第1步和第2步执行成功后才能执行"
    )
    private List<Integer> dependsOn;
}
