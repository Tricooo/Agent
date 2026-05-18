package com.tricoq.domain.agent.service.rag.rewrite.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/15/26
 */
@Getter
@AllArgsConstructor
@Slf4j
public enum RewritePolicy {

    HEURISTIC_MULTI_QUERY("heuristicMultiQueryRewriter", "HEURISTIC_MULTI_QUERY"),
    PASSTHROUGH("passthroughQueryRewriter", "PASSTHROUGH");

    private final String beanName;
    private final String policyName;

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
