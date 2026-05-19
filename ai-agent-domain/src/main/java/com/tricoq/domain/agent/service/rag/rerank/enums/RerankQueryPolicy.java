package com.tricoq.domain.agent.service.rag.rerank.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * rerank 阶段使用 query 的策略。
 *
 * @author：trico qiang
 * @date: 5/19/26
 */
@Getter
@AllArgsConstructor
@Slf4j
public enum RerankQueryPolicy {

    ORIGINAL("ORIGINAL"),
    ORIGINAL_PLUS_VARIANT("ORIGINAL_PLUS_VARIANT"),
    PER_VARIANT_RERANK_RRF("PER_VARIANT_RERANK_RRF");

    private final String policyName;

    public static RerankQueryPolicy getByPolicyOrDefault(String policyName) {
        if (StringUtils.isBlank(policyName)) {
            return RerankQueryPolicy.ORIGINAL;
        }
        for (RerankQueryPolicy policy : RerankQueryPolicy.values()) {
            if (policyName.equalsIgnoreCase(policy.getPolicyName())) {
                return policy;
            }
        }
        log.warn("rerank query policy {} not exist!", policyName);
        return RerankQueryPolicy.ORIGINAL;
    }
}
