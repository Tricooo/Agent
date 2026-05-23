package com.tricoq.domain.agent.service.rag.profile.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A profile item that must be traceable to one source chunk.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroundedProfileItem {

    @JsonPropertyDescription("用于检索导航的短文本，例如概念、可回答问题或缺失范围")
    private String value;

    @JsonPropertyDescription("该字段来自的原始文件名或 sourcePath")
    private String sourcePath;

    @JsonPropertyDescription("该字段来自的 chunkIndex")
    private Integer chunkIndex;

    @JsonPropertyDescription("原文中能支撑该字段的短证据，必须从对应 chunk 原文复制")
    private String evidenceText;
}
