package com.tricoq.domain.agent.service.rag.rewrite;

import com.tricoq.domain.agent.model.dto.RewriteResult;
import com.tricoq.domain.agent.service.rag.rewrite.enums.RewritePolicy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description: 基于启发式规则的多查询重写器
 * @author：trico qiang
 * @date: 5/15/26
 */
@Component
public class HeuristicMultiQueryRewriter implements QueryRewriter {

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
    public RewriteResult rewrite(String userText) {
        Set<String> variants = new LinkedHashSet<>();
        addVariant(variants, userText);

        if (hasExplanationIntent(userText)) {
            for (Map.Entry<String, String> entry : METRIC_TOPIC_EXPANSIONS.entrySet()) {
                if (StringUtils.containsIgnoreCase(userText, entry.getKey())) {
                    addVariant(variants, entry.getValue());
                    break;
                }
            }
        }

        String rewriteMode = variants.size() > 1
                ? RewritePolicy.HEURISTIC_MULTI_QUERY.getPolicyName()
                : RewritePolicy.PASSTHROUGH.getPolicyName();
        return RewriteResult.builder()
                .originUserText(userText)
                .queryVariantTexts(List.copyOf(variants))
                .rewriteMode(rewriteMode)
                .build();
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

    private void addVariant(Set<String> variants, String text) {
        if (StringUtils.isBlank(text)) {
            return;
        }
        variants.add(text.trim());
    }
}
