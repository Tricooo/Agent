package com.tricoq.domain.agent.service.rag.rewrite.strategy.impl;

import com.tricoq.domain.agent.service.rag.rewrite.enums.RewritePolicy;
import com.tricoq.domain.agent.service.rag.rewrite.strategy.AbstractQueryRewriter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 基于启发式规则的多查询重写器
 * @author：trico qiang
 * @date: 5/15/26
 */
@Component
public class HeuristicMultiQueryRewriter extends AbstractQueryRewriter {

    private static final List<String> EXPLANATION_INTENT_WORDS = List.of(
            "压力", "异常", "健康", "是否正常", "怎么判断", "判断", "查询思路", "告警", "阈值", "范围"
    );

    private static final Map<String, String> METRIC_TOPIC_EXPANSIONS = new LinkedHashMap<>();

    static {
        METRIC_TOPIC_EXPANSIONS.put("内存", "内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围");
        METRIC_TOPIC_EXPANSIONS.put("memory", "memory usage data interpretation threshold normal warning critical range");
        METRIC_TOPIC_EXPANSIONS.put("CPU", "CPU 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围");
        METRIC_TOPIC_EXPANSIONS.put("cpu", "cpu usage data interpretation threshold normal warning critical range");
        METRIC_TOPIC_EXPANSIONS.put("磁盘", "磁盘 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围");
        METRIC_TOPIC_EXPANSIONS.put("disk", "disk usage data interpretation threshold normal warning critical range");
        METRIC_TOPIC_EXPANSIONS.put("延迟", "延迟 数据解释 判断标准 阈值 正常范围 警告范围 危险范围");
        METRIC_TOPIC_EXPANSIONS.put("错误率", "错误率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围");
    }

    @Override
    protected RewritePolicy rewritePolicy() {
        return RewritePolicy.HEURISTIC_MULTI_QUERY;
    }

    @Override
    protected List<String> doRewrite(String userText) {
        if (hasExplanationIntent(userText)) {
            for (Map.Entry<String, String> entry : METRIC_TOPIC_EXPANSIONS.entrySet()) {
                if (StringUtils.containsIgnoreCase(userText, entry.getKey())) {
                    return List.of(entry.getValue());
                }
            }
        }
        return List.of();
    }

    @Override
    protected String noRewriteVariantReason(String userText) {
        return StringUtils.isBlank(userText) ? "BLANK_QUERY" : "NO_HEURISTIC_MATCH";
    }

    private boolean hasExplanationIntent(String userText) {
        if (StringUtils.isBlank(userText)) {
            return false;
        }
        for (String word : EXPLANATION_INTENT_WORDS) {
            if (StringUtils.containsIgnoreCase(userText, word)) {
                return true;
            }
        }
        return false;
    }
}
