package com.tricoq.domain.agent.service.rag.profile;

import com.tricoq.domain.agent.service.rag.profile.model.ProfileHint;
import com.tricoq.domain.agent.service.rag.profile.model.ProfileHintSelection;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RuleBasedProfileHintSelector implements ProfileHintSelector {

    public static final int DEFAULT_TOP_N = 8;
    private static final int MIN_CJK_OVERLAP = 2;

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\p{IsHan}]+|[A-Za-z0-9_./:-]+");
    private static final Map<String, List<String>> QUERY_ALIASES = Map.of(
            "内存", List.of("memory", "mem", "MemAvailable", "MemTotal", "node_memory"),
            "磁盘", List.of("disk", "filesystem", "node_filesystem"),
            "cpu", List.of("cpu", "node_cpu"),
            "CPU", List.of("cpu", "node_cpu"),
            "压力", List.of("threshold", "range", "判断标准", "阈值", "范围"),
            "趋势", List.of("predict", "predict_linear", "forecast"),
            "预测", List.of("predict", "predict_linear", "forecast")
    );
    private static final Set<String> LOW_SIGNAL_QUERY_TERMS = Set.of(
            "a", "an", "the", "to", "of", "in", "on", "for", "with", "from", "by",
            "is", "are", "was", "were", "be", "been", "being", "which", "what", "how",
            "according", "document", "knowledge", "base"
    );
    private static final Set<Integer> LOW_SIGNAL_CJK_CHARS = Set.of(
            (int) '的', (int) '是', (int) '了', (int) '在', (int) '和', (int) '与',
            (int) '及', (int) '或', (int) '为', (int) '个', (int) '这', (int) '那',
            (int) '请', (int) '问'
    );

    @Override
    public ProfileHintSelection select(String userText, List<ProfileHint> profileHints, int topN) {
        if (StringUtils.isBlank(userText) || CollectionUtils.isEmpty(profileHints)) {
            return ProfileHintSelection.builder()
                    .profileSource(SOURCE_NO_PROFILE_MATCH)
                    .selectedHints(List.of())
                    .build();
        }

        int limit = topN > 0 ? topN : DEFAULT_TOP_N;
        Set<String> queryTerms = queryTerms(userText);
        List<String> selectedHints = profileHints.stream()
                .filter(hint -> StringUtils.isNotBlank(hint.getValue()))
                .map(hint -> new ScoredHint(hint.getValue(), score(userText, queryTerms, hint)))
                .filter(scoredHint -> scoredHint.score() > 0)
                .sorted(Comparator.comparingInt(ScoredHint::score).reversed()
                        .thenComparing(ScoredHint::value))
                .map(ScoredHint::value)
                .distinct()
                .limit(limit)
                .toList();

        return ProfileHintSelection.builder()
                .profileSource(selectedHints.isEmpty() ? SOURCE_NO_PROFILE_MATCH : SOURCE_AUTO_PROFILE)
                .selectedHints(selectedHints)
                .build();
    }

    private Set<String> queryTerms(String userText) {
        Set<String> terms = new LinkedHashSet<>();
        Matcher matcher = TOKEN_PATTERN.matcher(userText);
        while (matcher.find()) {
            String term = matcher.group();
            if (StringUtils.isBlank(term)) {
                continue;
            }
            String normalizedTerm = term.toLowerCase(Locale.ROOT);
            if (isLowSignalQueryTerm(normalizedTerm)) {
                continue;
            }
            terms.add(normalizedTerm);
            List<String> aliases = QUERY_ALIASES.get(term);
            if (aliases != null) {
                aliases.stream()
                        .filter(StringUtils::isNotBlank)
                        .map(item -> item.toLowerCase(Locale.ROOT))
                        .forEach(terms::add);
            }
        }
        addMatchedAliases(userText, terms);
        return terms;
    }

    private void addMatchedAliases(String userText, Set<String> terms) {
        for (Map.Entry<String, List<String>> entry : QUERY_ALIASES.entrySet()) {
            if (!StringUtils.containsIgnoreCase(userText, entry.getKey())) {
                continue;
            }
            terms.add(entry.getKey().toLowerCase(Locale.ROOT));
            entry.getValue().stream()
                    .filter(StringUtils::isNotBlank)
                    .map(item -> item.toLowerCase(Locale.ROOT))
                    .forEach(terms::add);
        }
    }

    private int score(String userText, Set<String> queryTerms, ProfileHint hint) {
        String value = StringUtils.trimToEmpty(hint.getValue());
        String lowerValue = value.toLowerCase(Locale.ROOT);
        int relevanceScore = 0;
        if (StringUtils.containsIgnoreCase(userText, value)) {
            relevanceScore += 100;
        }
        for (String term : queryTerms) {
            if (isLowSignalQueryTerm(term)) {
                continue;
            }
            if (lowerValue.contains(term) || term.contains(lowerValue)) {
                relevanceScore += 35;
            }
        }
        relevanceScore += cjkOverlap(userText, value) * 12;
        if (relevanceScore <= 0) {
            return 0;
        }
        int profileScore = hint.getScore() == null ? 0 : Math.min(hint.getScore(), 80);
        return relevanceScore * 100 + profileScore;
    }

    private boolean isLowSignalQueryTerm(String term) {
        return StringUtils.length(term) < 3 || LOW_SIGNAL_QUERY_TERMS.contains(term);
    }

    private int cjkOverlap(String userText, String value) {
        Set<Integer> queryChars = new LinkedHashSet<>(cjkChars(userText));
        queryChars.removeAll(LOW_SIGNAL_CJK_CHARS);
        if (queryChars.isEmpty()) {
            return 0;
        }
        Set<Integer> valueChars = new LinkedHashSet<>(cjkChars(value));
        valueChars.removeAll(LOW_SIGNAL_CJK_CHARS);
        int overlap = 0;
        for (Integer queryChar : queryChars) {
            if (valueChars.contains(queryChar)) {
                overlap++;
            }
        }
        return overlap < MIN_CJK_OVERLAP ? 0 : overlap;
    }

    private List<Integer> cjkChars(String value) {
        List<Integer> chars = new ArrayList<>();
        value.codePoints()
                .filter(this::isCjk)
                .forEach(chars::add);
        return chars;
    }

    private boolean isCjk(int codePoint) {
        Character.UnicodeScript script = Character.UnicodeScript.of(codePoint);
        return script == Character.UnicodeScript.HAN;
    }

    private record ScoredHint(String value, int score) {
    }
}
