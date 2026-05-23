package com.tricoq.domain.agent.service.rag.profile.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Structured output for offline LLM profile extraction.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LlmKnowledgeBaseProfileResponse {

    @JsonPropertyDescription("80 字以内的知识库主题摘要，只能概括输入片段中明确出现的内容")
    private String summary;

    @JsonPropertyDescription("知识库中的核心业务或技术概念，每项必须带原文证据")
    private List<GroundedProfileItem> concepts;

    @JsonPropertyDescription("用户自然语言可能使用的同义表达或别名，每组必须带原文证据")
    private List<ProfileAlias> aliases;

    @JsonPropertyDescription("这份知识库明确可以回答的问题，每项必须带原文证据")
    private List<GroundedProfileItem> questionsAnswered;

    @JsonPropertyDescription("这份知识库明确没有覆盖、应拒答或限定回答的范围，每项必须带原文证据")
    private List<GroundedProfileItem> negativeScopes;

    @JsonPropertyDescription("文档中承载答案的证据类型，例如阈值、窗口编号、重试预算、缺失事实说明")
    private List<GroundedProfileItem> evidenceTypes;

    public void validate() {
        if (StringUtils.isBlank(summary)
                && CollectionUtils.isEmpty(concepts)
                && CollectionUtils.isEmpty(aliases)
                && CollectionUtils.isEmpty(questionsAnswered)
                && CollectionUtils.isEmpty(negativeScopes)
                && CollectionUtils.isEmpty(evidenceTypes)) {
            throw new IllegalStateException("LLM知识库画像抽取结果不能为空");
        }
    }
}
