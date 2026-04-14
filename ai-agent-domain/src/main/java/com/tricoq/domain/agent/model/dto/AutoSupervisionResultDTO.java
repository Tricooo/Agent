package com.tricoq.domain.agent.model.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Auto Agent 质量监督阶段的结构化输出。
 * 由 Step3QualitySupervisorNode 通过 Spring AI Structured Output 直接输出，
 * 替代原来的字符串 contains("是否通过: FAIL") 魔法字符串检测方式。
 *
 * @author trico qiang
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AutoSupervisionResultDTO {

    public enum QualityStatus {
        /** 质量达标，任务完成 */
        PASS,
        /** 质量不达标，需要重新执行 */
        FAIL,
        /** 基本完成但可优化 */
        OPTIMIZE
    }

    @JsonPropertyDescription("对执行结果的综合质量评估描述")
    private String qualityAssessment;

    @JsonPropertyDescription("识别出的问题列表，每项描述一个具体问题")
    private List<String> issues;

    @JsonPropertyDescription("改进建议列表，每项给出一条具体的优化建议")
    private List<String> suggestions;

    @JsonPropertyDescription("质量评分，1-10 分，10 分为满分")
    private Integer qualityScore;

    @JsonPropertyDescription("质量检查结论：PASS 表示通过，FAIL 表示需要重新执行，OPTIMIZE 表示建议优化")
    private QualityStatus pass;

    public void validate() {
        if (!StringUtils.hasText(qualityAssessment)) {
            throw new IllegalStateException("AutoSupervisionResultDTO.qualityAssessment 不能为空");
        }
        if (qualityScore == null) {
            throw new IllegalStateException("AutoSupervisionResultDTO.qualityScore 不能为空");
        }
        if (qualityScore < 1 || qualityScore > 10) {
            throw new IllegalStateException("AutoSupervisionResultDTO.qualityScore 超出范围: " + qualityScore);
        }
        if (pass == null) {
            throw new IllegalStateException("AutoSupervisionResultDTO.pass 不能为空");
        }
        if (pass != QualityStatus.PASS && CollectionUtils.isEmpty(suggestions)) {
            throw new IllegalStateException("AutoSupervisionResultDTO.suggestions 不能为空，当 pass=FAIL/OPTIMIZE 时必须提供改进建议");
        }
    }
}
