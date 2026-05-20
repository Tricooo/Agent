package com.tricoq.domain.agent.service.rag.rewrite.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/15/26
 */
@Getter
@AllArgsConstructor
@Slf4j
public enum RewritePolicy {

    LLM_MULTI_QUERY("LLM_MULTI_QUERY", List.of(
            "llmQueryRewriter",
            "heuristicMultiQueryRewriter",
            "passthroughQueryRewriter"
    )),
    HEURISTIC_MULTI_QUERY("HEURISTIC_MULTI_QUERY", List.of(
            "heuristicMultiQueryRewriter",
            "passthroughQueryRewriter"
    )),
    PASSTHROUGH("PASSTHROUGH", List.of(
            "passthroughQueryRewriter"
    ));

    private final String policyName;
    private final List<String> strategyBeanNames;

    public static RewritePolicy getByPolicyOrDefault(String policyName) {
        if (StringUtils.isBlank(policyName)) {
            return RewritePolicy.PASSTHROUGH;
        }
        for (RewritePolicy enumVO : RewritePolicy.values()) {
            if (policyName.equalsIgnoreCase(enumVO.getPolicyName())) {
                return enumVO;
            }
        }
        log.warn("policy name {} not exist!", policyName);
        return RewritePolicy.PASSTHROUGH;
    }

}
