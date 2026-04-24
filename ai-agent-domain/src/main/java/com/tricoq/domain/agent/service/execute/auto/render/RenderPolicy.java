package com.tricoq.domain.agent.service.execute.auto.render;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description: 渲染策略
 *               策略和运行中状态应该分清，不能混用
 * @author：trico qiang
 * @date: 4/21/26
 */
@Getter
@AllArgsConstructor
public enum RenderPolicy {

    ANALYZER_POLICY(3300, 6, 3, 1200,
            600, true, true),
    SUMMARY_POLICY(6800, 6, 3, 3000,
            1500, false, false);

    private final int maxChars;
    /*
     * 策略配置，表示“最多保留多少 skeleton”或者“skeleton 上限”
     */
    private final int skeletonLimit;
    private final int recentDetailCount;
    private final int executionResultBudget;
    private final int executionProcessBudget;
    private final boolean omitQualityCheckFirst;
    private final boolean omitSupervisionAssessmentFirst;
}
