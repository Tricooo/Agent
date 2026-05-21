package com.tricoq.domain.agent.service.rag.rewrite.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/20/26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class LlmRewriteResponse {

    @JsonPropertyDescription("重写后的检索问题列表，生成 1 到 2 个，不要包含原始问题")
    private List<String> rewrittenQueries;

    public void validate() {
        if (CollectionUtils.isEmpty(rewrittenQueries) || rewrittenQueries.stream().allMatch(StringUtils::isBlank)) {
            throw new IllegalStateException("RAG用户问题重写后不能为空");
        }
    }
}
