package com.tricoq.domain.agent.service.armory.node.factory.element;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

final class ContextSalienceSupport {

    private static final Pattern FORMULA_PATTERN = Pattern.compile(
            "(?i)(```|promql|query:|node_[a-z0-9_:]+|[a-z_]+\\([^\\n)]*\\))"
    );
    private static final Pattern RANGE_PATTERN = Pattern.compile(
            "(?i)(range|threshold|limit|boundary|scope|区间|范围|阈值|上限|下限|边界|等级|级别|标准)"
                    + "|\\d+(?:\\.\\d+)?\\s*%?\\s*[-~–—至到]\\s*\\d+(?:\\.\\d+)?\\s*%"
    );
    private static final Pattern JUDGMENT_PATTERN = Pattern.compile("判断标准|分析标准|状态评估|风险|压力|异常|解释|评估");
    private static final Pattern PROCEDURE_PATTERN = Pattern.compile("步骤|流程|排查|确认|执行|查询|分析");
    private static final Pattern PARAMETER_PATTERN = Pattern.compile("参数|必填|可选|返回值|返回结构");
    private static final Pattern EXAMPLE_PATTERN = Pattern.compile("示例|example|Example");
    private static final Pattern HEADING_LINE_PATTERN = Pattern.compile("^\\s*#{1,6}\\s+\\S+");
    private static final Pattern LIST_LINE_PATTERN = Pattern.compile("^\\s*(?:[-*+]\\s+|\\d+[.)]\\s+)\\S+");
    private static final Pattern TABLE_ROW_PATTERN = Pattern.compile("^\\s*\\|.+\\|\\s*$");
    private static final Pattern CODE_FENCE_PATTERN = Pattern.compile("```");
    private static final Pattern CODE_CONTINUATION_PATTERN = Pattern.compile(
            "(?i)^\\s*(?:[A-Za-z0-9_./:-]+\\s*[:=]|[({\\[]|\\w+\\([^)]*)"
    );
    private static final Set<String> EVIDENCE_COVERAGE_CUES = Set.of(
            "formula", "range", "judgement", "procedure", "parameter", "example"
    );

    private ContextSalienceSupport() {
    }

    static Analysis analyze(String text) {
        String safeText = StringUtils.defaultString(text);
        LinkedHashSet<String> cues = new LinkedHashSet<>();
        addCueIfMatches(cues, "formula", FORMULA_PATTERN, safeText);
        addCueIfMatches(cues, "range", RANGE_PATTERN, safeText);
        addCueIfMatches(cues, "judgement", JUDGMENT_PATTERN, safeText);
        addCueIfMatches(cues, "procedure", PROCEDURE_PATTERN, safeText);
        addCueIfMatches(cues, "parameter", PARAMETER_PATTERN, safeText);
        addCueIfMatches(cues, "example", EXAMPLE_PATTERN, safeText);
        return new Analysis(List.copyOf(cues), needsPreviousChunkForBoundary(safeText, cues));
    }

    static Analysis analyzeIntent(String userText, Collection<String> queryVariants, Collection<String> profileHints) {
        StringBuilder builder = new StringBuilder();
        appendIntentText(builder, userText);
        if (!CollectionUtils.isEmpty(queryVariants)) {
            for (String queryVariant : queryVariants) {
                appendIntentText(builder, queryVariant);
            }
        }
        if (!CollectionUtils.isEmpty(profileHints)) {
            for (String profileHint : profileHints) {
                appendIntentText(builder, profileHint);
            }
        }
        return analyze(builder.toString());
    }

    static List<String> coverageCues(Analysis analysis) {
        if (analysis == null || CollectionUtils.isEmpty(analysis.cues())) {
            return List.of();
        }
        LinkedHashSet<String> cues = new LinkedHashSet<>();
        for (String cue : analysis.cues()) {
            if (EVIDENCE_COVERAGE_CUES.contains(cue)) {
                cues.add(cue);
            }
        }
        return List.copyOf(cues);
    }

    static boolean matchesCoverageIntent(Analysis intentAnalysis, Analysis evidenceAnalysis) {
        List<String> intentCues = coverageCues(intentAnalysis);
        List<String> evidenceCues = coverageCues(evidenceAnalysis);
        if (CollectionUtils.isEmpty(intentCues) || CollectionUtils.isEmpty(evidenceCues)) {
            return false;
        }
        for (String intentCue : intentCues) {
            if (evidenceCues.contains(intentCue)) {
                return true;
            }
        }
        return (intentCues.contains("judgement") && evidenceCues.contains("range"))
                || (intentCues.contains("range") && evidenceCues.contains("judgement"));
    }

    static String renderCueLine(Analysis analysis) {
        if (analysis == null || CollectionUtils.isEmpty(analysis.cues())) {
            return "";
        }
        return "(证据提示: " + String.join(", ", analysis.cues()) + ")";
    }

    private static void addCueIfMatches(Set<String> cues, String cue, Pattern pattern, String text) {
        if (pattern.matcher(text).find()) {
            cues.add(cue);
        }
    }

    private static void appendIntentText(StringBuilder builder, String text) {
        if (StringUtils.isBlank(text)) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(System.lineSeparator());
        }
        builder.append(text);
    }

    private static boolean needsPreviousChunkForBoundary(String text, Set<String> cues) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        String firstLine = firstNonBlankLine(text);
        if (StringUtils.isBlank(firstLine) || HEADING_LINE_PATTERN.matcher(firstLine).find()) {
            return false;
        }
        boolean hasStructuredEvidence = cues.contains("range") || cues.contains("parameter");
        boolean startsWithStructuredList = LIST_LINE_PATTERN.matcher(firstLine).find() && hasStructuredEvidence;
        boolean startsWithTableRow = TABLE_ROW_PATTERN.matcher(firstLine).find();
        boolean startsInsideCodeBlock = startsInsideCodeBoundary(text, firstLine);
        return startsWithStructuredList || startsWithTableRow || startsInsideCodeBlock;
    }

    private static boolean startsInsideCodeBoundary(String text, String firstLine) {
        if (StringUtils.startsWith(StringUtils.trimToEmpty(firstLine), "```")) {
            return false;
        }
        int fenceCount = 0;
        var matcher = CODE_FENCE_PATTERN.matcher(text);
        while (matcher.find()) {
            fenceCount++;
        }
        return fenceCount % 2 != 0 && CODE_CONTINUATION_PATTERN.matcher(firstLine).find();
    }

    private static String firstNonBlankLine(String text) {
        String[] lines = StringUtils.defaultString(text).split("\\R");
        for (String line : lines) {
            if (StringUtils.isNotBlank(line)) {
                return line;
            }
        }
        return "";
    }

    record Analysis(List<String> cues, boolean needsPreviousChunkContext) {
    }
}
