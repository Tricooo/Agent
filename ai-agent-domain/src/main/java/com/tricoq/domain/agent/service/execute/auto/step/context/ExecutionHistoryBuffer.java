package com.tricoq.domain.agent.service.execute.auto.step.context;

import com.tricoq.domain.agent.model.dto.AutoExecuteResultDTO;
import com.tricoq.domain.agent.model.dto.AutoSupervisionResultDTO;
import com.tricoq.domain.agent.service.execute.auto.render.DetailRenderLevel;
import com.tricoq.domain.agent.service.execute.auto.render.RenderPolicy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
    //不变量，作为raw fact
    private final List<ExecutionHistoryEntry> entries = new ArrayList<>();

    //----写入----

    public void recordExecution(int step, String strategy, AutoExecuteResultDTO executeResult) {
        Objects.requireNonNull(executeResult, "executeResult 不能为空");
        ExecutionHistoryEntry entry = ExecutionHistoryEntry.builder().step(step)
                .strategy(strategy)
                .executionTarget(executeResult.getExecutionTarget())
                .executionProcess(executeResult.getExecutionProcess())
                .executionResult(executeResult.getExecutionResult())
                .qualityCheck(executeResult.getQualityCheck())
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

    //职责：负责循环
    private String renderAnalyzerWithBudget(List<ExecutionHistoryEntry> source) {
        RenderPolicy analyzerPolicy = RenderPolicy.ANALYZER_POLICY;
        AnalyzerRenderDraft draft = buildAnalyzerRenderDraft(source, analyzerPolicy);
        int totalSkeleton = draft.getSkeletonEntries().size();
        String rendered = composeAnalyzer(draft, analyzerPolicy, totalSkeleton);
        while (rendered.length() > analyzerPolicy.getMaxChars()) {
            if (!degradeOneStep(draft, analyzerPolicy)) {
                log.warn("无法继续降级");
                break;
            }
            rendered = composeAnalyzer(draft, analyzerPolicy, totalSkeleton);
        }

        //当前是best-effort budget 而不是 hard budget
        if (rendered.length() > analyzerPolicy.getMaxChars()) {
            log.warn("仍然超预算,analyze draft 已经降到当前允许的最小程度,长度：{},预算长度：{}", rendered.length(),
                    analyzerPolicy.getMaxChars());
        }

        return rendered;
    }

    //职责：改变draft (detail状态流转)
    private boolean degradeOneStep(AnalyzerRenderDraft draft, RenderPolicy policy) {
        int skeletonLimit = policy.getSkeletonLimit();
        List<ExecutionHistoryEntry> skeletonEntries = draft.getSkeletonEntries();
        if (skeletonLimit < skeletonEntries.size()) {
            skeletonEntries.removeFirst();
            return true;
        }
        //历史纪录裁剪到限制，开始处理detail的可省略字段
        List<DetailRenderState> detailStates = draft.getDetailStates();
        //第一轮次 先依次对detail的可省略字段进行省略
        for (DetailRenderState detailState : detailStates) {
            if (detailState.level == DetailRenderLevel.FULL) {
                detailState.setLevel(DetailRenderLevel.OMIT_OPTIONAL);
                return true;
            }
        }
        //然后下面的轮次，对最老的detail纪录进行依次降级
        for (DetailRenderState detailState : detailStates) {
            if (detailState.level == DetailRenderLevel.OMIT_OPTIONAL) {
                detailState.setLevel(DetailRenderLevel.COMPRESS_LONG_FIELDS);
                return true;
            }
            if (detailState.level == DetailRenderLevel.COMPRESS_LONG_FIELDS) {
                detailState.setLevel(DetailRenderLevel.COMPACT_DETAIL);
                return true;
            }
            if (detailState.level == DetailRenderLevel.COMPACT_DETAIL) {
                detailState.setLevel(DetailRenderLevel.DROP);
                return true;
            }
        }
        return false;
    }

    //职责：构建draft
    private AnalyzerRenderDraft buildAnalyzerRenderDraft(List<ExecutionHistoryEntry> source, RenderPolicy policy) {
        int recentDetailCount = policy.getRecentDetailCount();
        int overLimited = Math.max(0, source.size() - recentDetailCount);

        List<ExecutionHistoryEntry> detailEntries = source.subList(overLimited, source.size());
        List<DetailRenderState> states = detailEntries.stream().map(DetailRenderState::new).toList();
        AnalyzerRenderDraft draft = new AnalyzerRenderDraft();
        draft.addDetailStates(states);

        if (overLimited != 0) {
            List<ExecutionHistoryEntry> historyEntries = source.subList(0, overLimited);
            draft.addSkeletonEntries(historyEntries);
        }
        return draft;
    }

    //职责：负责渲染draft，只读draft
    private String composeAnalyzer(AnalyzerRenderDraft draft, RenderPolicy policy, int totalSkeleton) {
        List<ExecutionHistoryEntry> skeletonEntries = draft.getSkeletonEntries();
        StringBuilder sb = new StringBuilder();

        int omitSkeleton = totalSkeleton - skeletonEntries.size();
        if (omitSkeleton > 0) {
            sb.append("[更早 ").append(omitSkeleton).append(" 步已省略]").append('\n');
        }

        if (!skeletonEntries.isEmpty()) {
            //老历史保留基本骨架
            sb.append(EARLY_SKELETON_HEADER).append('\n');
            for (ExecutionHistoryEntry entry : skeletonEntries) {
                sb.append(formatSkeletonLine(entry)).append('\n');
            }
            sb.append('\n');
        }

        List<DetailRenderState> detailStates = draft.getDetailStates();
        //近N轮保持详情（如果还超限按照字段分层截断）
        if (CollectionUtils.isEmpty(detailStates)) {
            return sb.toString();
        }
        sb.append(RECENT_DETAIL_HEADER).append('\n');
        for (DetailRenderState state : detailStates) {
            sb.append(formatDetailEntry(state, policy)).append('\n');
        }
        return sb.toString();
    }

    private String formatDetailEntry(DetailRenderState state, RenderPolicy policy) {
        DetailRenderLevel level = state.getLevel();
        return switch (level) {
            case DROP -> "";
            case COMPACT_DETAIL -> formatCompactDetailEntry(state.getSourceEntry());
            case COMPRESS_LONG_FIELDS -> formatCompressEntry(state.getSourceEntry(), policy);
            case OMIT_OPTIONAL -> formatOmitEntry(state.getSourceEntry());
            case FULL -> formatFullEntry(state.getSourceEntry());
        };
    }

    /**
     * 对可安全省略字段做安全压缩 head+tail 不截断
     */
    private String formatCompressEntry(ExecutionHistoryEntry e, RenderPolicy policy) {
        return formatCompactEntry(
                e,
                safetyCompress(e.getExecutionProcess(), policy.getExecutionProcessBudget()),
                safetyCompress(e.getExecutionResult(), policy.getExecutionResultBudget()),
                !policy.isOmitQualityCheckFirst(),
                !policy.isOmitSupervisionAssessmentFirst()
        );
    }

    private String safetyCompress(String origin, int limit) {
        if (StringUtils.isBlank(origin) || limit <= 0) {
            return "";
        }

        // 先判断整体预算，没超就不要压
        if (origin.length() <= limit) {
            return origin;
        }

        String[] phases = origin.split("\\R");
        if (phases.length <= 2) {
            return "【该字段内容过长，已省略；请以上下文中的策略、目标、监督结论为准】";
        }

        String compressed = phases[0] + "\n【中间部分内容已省略】\n" + phases[phases.length - 1];
        if (compressed.length() > limit) {
            return "【该字段首尾段仍超出预算，已省略；请以上下文中的策略、目标、监督结论为准】";
        }
        return compressed;
    }

    private String formatCompactDetailEntry(ExecutionHistoryEntry e) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("""
                        === 第 %d 步执行记录（详情已极简化） ===
                        【分析策略】%s
                        【执行目标】%s
                        """,
                e.getStep(),
                nullToEmpty(e.getStrategy()),
                nullToEmpty(e.getExecutionTarget())));

        if (e.hasSupervision()) {
            sb.append(String.format("【监督阶段】评分: %d | 结论: %s",
                    e.getSupervisionScore(),
                    e.getSupervisionPass()));
        }
        return sb.toString();
    }


    private String formatOmitEntry(ExecutionHistoryEntry e) {
        return formatCompactEntry(
                e,
                e.getExecutionProcess(),
                e.getExecutionResult(),
                false,
                false
        );
    }

    private String formatFullEntry(ExecutionHistoryEntry e) {
        return formatCompactEntry(
                e,
                e.getExecutionProcess(),
                e.getExecutionResult(),
                true,
                true
        );
    }

    private String formatCompactEntry(
            ExecutionHistoryEntry e,
            String executionProcess,
            String executionResult,
            boolean includeQualityCheck,
            boolean includeSupervisionAssessment
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("""
                        === 第 %d 步执行记录 ===
                        【分析策略】%s
                        【执行目标】%s
                        【执行过程】%s
                        【执行结果】%s
                        """,
                e.getStep(),
                nullToEmpty(e.getStrategy()),
                nullToEmpty(e.getExecutionTarget()),
                nullToEmpty(executionProcess),
                nullToEmpty(executionResult)));

        if (includeQualityCheck) {
            sb.append(String.format("【初步自检】%s%n", nullToEmpty(e.getQualityCheck())));
        }

        if (e.hasSupervision()) {
            if (includeSupervisionAssessment) {
                sb.append(String.format("【监督阶段】评分: %d | 结论: %s | 评估: %s%n",
                        e.getSupervisionScore(),
                        e.getSupervisionPass(),
                        nullToEmpty(e.getSupervisionAssessment())));
            } else {
                sb.append(String.format("【监督阶段】评分: %d | 结论: %s",
                        e.getSupervisionScore(),
                        e.getSupervisionPass()));
            }
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


    @Getter
    private static final class AnalyzerRenderDraft {
        private final List<ExecutionHistoryEntry> skeletonEntries = new ArrayList<>();
        private final List<DetailRenderState> detailStates = new ArrayList<>();

        public void addSkeletonEntries(List<ExecutionHistoryEntry> histories) {
            this.skeletonEntries.addAll(histories);
        }

        public void addDetailStates(List<DetailRenderState> details) {
            this.detailStates.addAll(details);
        }
    }


    @Data
    private static final class DetailRenderState {
        //指向raw fact-不可更改
        private final ExecutionHistoryEntry sourceEntry;
        private DetailRenderLevel level = DetailRenderLevel.FULL;

        public DetailRenderState(ExecutionHistoryEntry raw) {
            this.sourceEntry = Objects.requireNonNull(raw, "raw execution history entry 不能为空");
        }
    }


    @Getter
    @AllArgsConstructor
    @Builder
    public static final class ExecutionHistoryEntry {
        //🔴不可压缩 🟢优先省略 🟡安全压缩（head+tail）


        private final int step;
        //🔴执行策略，为执行节点提供具体的行动方向
        private final String strategy;
        //🔴本次执行的具体目标
        private final String executionTarget;
        //🟡详细的执行过程描述，包括使用的方法和步骤
        private final String executionProcess;
        //🟡执行结果，包含具体的输出内容和数据
        private final String executionResult;
        //🟢执行结果的初步质量自检，指出潜在的问题或不足
        private final String qualityCheck;

        //🔴质量评分，1-10 分，10 分为满分
        private Integer supervisionScore;
        //🔴质量检查结论：PASS 表示通过，FAIL 表示需要重新执行，OPTIMIZE 表示建议优化
        private String supervisionPass;
        //🟢对执行结果的综合质量评估描述
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
