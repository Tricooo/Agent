package com.tricoq.domain.agent.service.rag.rerank.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/14/26
 */
@AllArgsConstructor
@Getter
@Slf4j
public enum RerankPolicy {
    LOCAL_BGE("httpDocumentReranker", "LOCAL_BGE"),
    PASSTHROUGH("passthroughDocumentReranker", "PASSTHROUGH");

    private final String beanName;
    private final String policyName;

    public static RerankPolicy getByPolicyOrDefault(String policyName) {
        if (policyName == null) {
            return RerankPolicy.PASSTHROUGH;
        }
        for (RerankPolicy enumVO : RerankPolicy.values()) {
            if (policyName.equals(enumVO.getPolicyName())) {
                return enumVO;
            }
        }
        log.warn("policy name {} not exist!", policyName);
        return RerankPolicy.PASSTHROUGH;
    }

}
