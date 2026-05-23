package com.tricoq.domain.agent.service.rag.profile.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Alias group grounded in one source chunk.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileAlias {

    @JsonPropertyDescription("原文中的核心概念或术语")
    private String value;

    @JsonPropertyDescription("可用于检索改写的同义表达或别名")
    private List<String> aliases;

    @JsonPropertyDescription("该别名组来自的原始文件名或 sourcePath")
    private String sourcePath;

    @JsonPropertyDescription("该别名组来自的 chunkIndex")
    private Integer chunkIndex;

    @JsonPropertyDescription("原文中能支撑该别名组的短证据，必须从对应 chunk 原文复制")
    private String evidenceText;
}
