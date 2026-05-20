package com.tricoq.domain.agent.service.rag.rewrite.model;

import com.tricoq.domain.agent.service.rag.rewrite.enums.RewritePolicy;
import com.tricoq.domain.agent.service.rag.rewrite.enums.RewriteStatus;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @description: 单个 rewrite 策略的一次尝试结果。
 * @author：trico qiang
 * @date: 5/19/26
 */
@Data
@Builder
public class RewriteAttempt {

    private RewritePolicy policy;

    private RewriteStatus status;

    private List<String> queryVariantTexts;

    private String failureReason;

    private Long elapsedMs;

    public boolean effectiveRewrite() {
        return status == RewriteStatus.REWRITTEN
                && CollectionUtils.size(queryVariantTexts) > 1
                && StringUtils.isBlank(failureReason);
    }

    public boolean passthrough() {
        return status == RewriteStatus.PASSTHROUGH;
    }

    public String traceText() {
        String policyName = policy == null ? "UNKNOWN" : policy.getPolicyName();
        String statusName = status == null ? "UNKNOWN" : status.name();
        if (StringUtils.isBlank(failureReason)) {
            return policyName + ":" + statusName;
        }
        return policyName + ":" + statusName + "(" + failureReason + ")";
    }
}
