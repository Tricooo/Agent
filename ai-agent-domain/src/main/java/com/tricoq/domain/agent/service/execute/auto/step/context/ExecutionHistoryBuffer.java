package com.tricoq.domain.agent.service.execute.auto.step.context;

import com.tricoq.domain.agent.model.dto.AutoExecuteResultDTO;
import com.tricoq.domain.agent.model.dto.AutoSupervisionResultDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @description: Auto 链路跨轮共享的结构化执行历史缓冲。
 * 替代原无上限 StringBuilder，通过分层预算化渲染避免 O(N²) token 膨胀。
 * @author：trico qiang
 * @date: 4/20/26
 */
@Slf4j
public class ExecutionHistoryBuffer {

    // ---- 预算常量 (V1 硬编码, 不做配置化) ----
    private static final int RECENT_FULL_COUNT = 3;
    private static final int ANALYZER_MAX_CHARS = 5000;
    private static final int SUMMARY_MAX_CHARS = 20000;
    private static final String EMPTY_ANALYZER_PLACEHOLDER = "[首次执行，无历史记录]";
    private static final String EMPTY_SUMMARY_PLACEHOLDER = "[无执行历史]";
    private static final String EARLY_SKELETON_HEADER = "## 早期步骤轨迹（明细已压缩）";
    private static final String RECENT_DETAIL_HEADER = "## 最近若干轮执行明细";

    private final List<ExecutionHistoryEntry> entries = new ArrayList<>();

    //----写入----

    public void recordExecution(int step, String strategy, AutoExecuteResultDTO executeResult) {
        Objects.requireNonNull(executeResult, "executeResult 不能为空");
        ExecutionHistoryEntry entry = ExecutionHistoryEntry.builder().step(step)
                .strategy(strategy)
                .executionTarget(executeResult.getExecutionTarget())
                .executionResult(executeResult.getExecutionResult())
                .build();
        entries.add(entry);
    }

    public void recordSupervision(int step, AutoSupervisionResultDTO supervisionResult) {
        Objects.requireNonNull(supervisionResult, "supervisionResult 不能为空");
        for (int i = entries.size() - 1; i >= 0; i--) {
            ExecutionHistoryEntry entry = entries.get(i);
            if (step != entry.getStep()) {
                continue;
            }
            //偶发重复提交的幂等容忍,对真正状态冲突的强约束
            if (entry.hasSupervision() && (!entry.supervisionEqual(supervisionResult.getQualityScore(),
                    supervisionResult.getPass() == null ? null : supervisionResult.getPass().name(),
                    supervisionResult.getQualityAssessment()))) {
                log.error("执行历史监督结果发生覆盖冲突, step={}, 旧结论={}",
                        step, entry.supervisionPass);
                throw new RuntimeException("supervision overwrite");
            }
            entry.setSupervision(
                    supervisionResult.getQualityScore(),
                    supervisionResult.getPass() == null ? null : supervisionResult.getPass().name(),
                    supervisionResult.getQualityAssessment());
            return;
        }
        throw new IllegalStateException(
                "No execution entry found for step=" + step
                        + ", recordSupervision invariant violated: recordExecution must precede it.");
    }

    // ---- 消费 ----

    public String renderForAnalyzer() {
        if (entries.isEmpty()) {
            return EMPTY_ANALYZER_PLACEHOLDER;
        }
        return renderAnalyzerWithBudget(entries);
    }

    private String renderAnalyzerWithBudget(List<ExecutionHistoryEntry> source) {
        String handled = handleHistory(source);
        //size判断是为了避免只有一条记录但是超限被删除
        while (handled.length() > ANALYZER_MAX_CHARS && source.size() > 1) {
            source = source.subList(1, source.size());
            handled = handleHistory(source);
        }
        //当前是best-effort budget 而不是 hard budget
        if (handled.length() > ANALYZER_MAX_CHARS) {
            log.warn("executionHistory 在 analyzer 场景按 entry 边界裁剪后仍超预算, entries={}, length={}, limit={}",
                    source.size(), handled.length(), ANALYZER_MAX_CHARS);
        }
        return handled;
    }

    private String handleHistory(List<ExecutionHistoryEntry> source) {
        int overEntry = Math.max(0, source.size() - RECENT_FULL_COUNT);
        StringBuilder sb = new StringBuilder();
        if (overEntry > 0) {
            sb.append(EARLY_SKELETON_HEADER).append('\n');
            for (int i = 0; i < overEntry; i++) {
                ExecutionHistoryEntry entry = source.get(i);
                sb.append(formatSkeletonLine(entry)).append('\n');
            }
            sb.append('\n');
        }
        sb.append(RECENT_DETAIL_HEADER).append('\n');
        for (int i = overEntry; i < source.size(); i++) {
            ExecutionHistoryEntry entry = source.get(i);
            sb.append(formatFullEntry(entry)).append('\n');
        }
        return sb.toString();
    }

    private String formatFullEntry(ExecutionHistoryEntry e) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("""
                        === 第 %d 步执行记录 ===
                        【分析策略】%s
                        【执行目标】%s
                        【执行结果】%s
                        """,
                e.step,
                nullToEmpty(e.strategy),
                nullToEmpty(e.executionTarget),
                nullToEmpty(e.executionResult)));
        if (e.hasSupervision()) {
            sb.append(String.format("【监督阶段】评分: %d | 结论: %s | 评估: %s%n",
                    e.supervisionScore,
                    e.supervisionPass,
                    nullToEmpty(e.supervisionAssessment)));
        }
        return sb.toString();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private String formatSkeletonLine(ExecutionHistoryEntry e) {
        if (e.hasSupervision()) {
            return String.format("- 第 %d 步 → 结论: %s | 评分: %d",
                    e.step, e.supervisionPass, e.supervisionScore);
        }
        return String.format("- 第 %d 步 → 结论: N/A", e.step);
    }

    public String renderForSummary() {
        if (entries.isEmpty()) {
            return EMPTY_SUMMARY_PLACEHOLDER;
        }
        return renderSummaryWithBudget(entries);
    }

    private String renderSummaryWithBudget(List<ExecutionHistoryEntry> source) {
        int dropped = 0;
        String rendered = composeSummary(source);
        while (rendered.length() > SUMMARY_MAX_CHARS && source.size() > 1) {
            source = source.subList(1, source.size());
            dropped++;
            rendered = composeSummary(source);
        }
        //summary 场景同样优先保证 entry 语义完整性，当前仍是 best-effort budget
        if (rendered.length() > SUMMARY_MAX_CHARS) {
            log.warn("executionHistory 在 summary 场景按 entry 边界裁剪后仍超预算, entries={}, length={}, limit={}",
                    source.size(), rendered.length(), SUMMARY_MAX_CHARS);
        }
        if (dropped > 0) {
            return "[早期 " + dropped + " 条已裁剪]\n" + rendered;
        }
        return rendered;
    }

    public int size() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    private String composeSummary(List<ExecutionHistoryEntry> source) {
        StringBuilder sb = new StringBuilder();
        for (ExecutionHistoryEntry entry : source) {
            sb.append(formatFullEntry(entry));
        }
        return sb.toString();
    }


    @Data
    @AllArgsConstructor
    @Builder
    public static final class ExecutionHistoryEntry {
        private final int step;
        private final String strategy;
        private final String executionTarget;
        private final String executionResult;

        private Integer supervisionScore;
        private String supervisionPass;
        private String supervisionAssessment;

        public boolean hasSupervision() {
            return StringUtils.isNoneBlank(supervisionPass);
        }

        void setSupervision(Integer score, String pass, String assessment) {
            this.supervisionScore = score;
            this.supervisionPass = pass;
            this.supervisionAssessment = assessment;
        }

        boolean supervisionEqual(Integer score, String pass, String assessment) {
            return Objects.equals(score, supervisionScore)
                    && Objects.equals(pass, supervisionPass)
                    && Objects.equals(assessment, supervisionAssessment);
        }
    }
}
