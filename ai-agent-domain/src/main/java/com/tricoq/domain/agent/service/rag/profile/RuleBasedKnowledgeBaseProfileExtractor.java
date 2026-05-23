package com.tricoq.domain.agent.service.rag.profile;

import com.tricoq.domain.agent.service.rag.profile.model.KnowledgeBaseProfile;
import com.tricoq.domain.agent.service.rag.profile.model.ProfileHint;
import com.tricoq.domain.agent.service.rag.profile.model.ProfileHintType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RuleBasedKnowledgeBaseProfileExtractor implements KnowledgeBaseProfileExtractor {

    public static final String PROFILE_VERSION = "rule-v1";

    private static final int MAX_HINTS = 80;
    private static final int MAX_HINT_CHARS = 80;
    private static final Pattern HEADING_PATTERN = Pattern.compile("(?m)^#{1,6}\\s+(.{2,120})\\s*$");
    private static final Pattern INLINE_CODE_PATTERN = Pattern.compile("`([^`\\n]{2,120})`");
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("(?s)```(?:[A-Za-z0-9_-]+)?\\s*(.*?)```");
    private static final Pattern TECHNICAL_TOKEN_PATTERN = Pattern.compile(
            "(?<![A-Za-z0-9_./:-])([A-Za-z][A-Za-z0-9_./:-]{2,}|[A-Za-z0-9]+(?:[_./:-][A-Za-z0-9]+)+)(?![A-Za-z0-9_./:-])"
    );
    private static final Pattern ENGLISH_DIGIT_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z0-9_./:-]{3,}$");
    private static final List<String> EVIDENCE_PHRASES = List.of(
            "参数", "示例", "判断标准", "阈值", "范围", "排查步骤", "查询思路", "数据解释",
            "正常范围", "警告范围", "危险范围", "公式", "配置示例", "异常边界",
            "threshold", "range", "troubleshooting", "example", "parameter", "formula"
    );
    private static final Set<String> LOW_SIGNAL_TOKENS = Set.of(
            "the", "and", "for", "with", "from", "this", "that", "true", "false", "null",
            "http", "https", "www", "com", "org", "user", "query", "answer", "context",
            "document", "knowledge", "guide", "markdown", "yaml", "json", "text"
    );

    @Override
    public KnowledgeBaseProfile extract(String ragId, String knowledgeTag, List<Document> documents) {
        Map<String, HintAccumulator> hints = new LinkedHashMap<>();
        if (!CollectionUtils.isEmpty(documents)) {
            for (Document document : documents) {
                collectFromText(StringUtils.defaultString(document.getText()), hints);
            }
        }

        List<ProfileHint> profileHints = hints.values().stream()
                .sorted(Comparator.comparingInt(HintAccumulator::score).reversed()
                        .thenComparing(HintAccumulator::value))
                .limit(MAX_HINTS)
                .map(HintAccumulator::toProfileHint)
                .toList();

        return KnowledgeBaseProfile.builder()
                .ragId(ragId)
                .knowledgeTag(knowledgeTag)
                .profileVersion(PROFILE_VERSION)
                .generatedAt(LocalDateTime.now())
                .hints(profileHints)
                .build();
    }

    private void collectFromText(String text, Map<String, HintAccumulator> hints) {
        if (StringUtils.isBlank(text)) {
            return;
        }
        collectPattern(text, HEADING_PATTERN, ProfileHintType.HEADING, "heading", hints);
        collectPattern(text, INLINE_CODE_PATTERN, ProfileHintType.INLINE_CODE, "inline_code", hints);
        collectCodeBlockTokens(text, hints);
        collectTechnicalTokens(text, hints);
        collectEvidencePhrases(text, hints);
    }

    private void collectPattern(String text, Pattern pattern, ProfileHintType type,
                                String source, Map<String, HintAccumulator> hints) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            addHint(matcher.group(1), type, source, hints);
        }
    }

    private void collectCodeBlockTokens(String text, Map<String, HintAccumulator> hints) {
        Matcher blockMatcher = CODE_BLOCK_PATTERN.matcher(text);
        while (blockMatcher.find()) {
            Matcher tokenMatcher = TECHNICAL_TOKEN_PATTERN.matcher(blockMatcher.group(1));
            while (tokenMatcher.find()) {
                String token = tokenMatcher.group(1);
                if (isStrongTechnicalToken(token)) {
                    addHint(token, ProfileHintType.CODE_TOKEN, "code_block", hints);
                }
            }
        }
    }

    private void collectTechnicalTokens(String text, Map<String, HintAccumulator> hints) {
        Matcher matcher = TECHNICAL_TOKEN_PATTERN.matcher(text);
        while (matcher.find()) {
            String token = matcher.group(1);
            if (isStrongTechnicalToken(token)) {
                addHint(token, ProfileHintType.TECHNICAL_TOKEN, "technical_token", hints);
            }
        }
    }

    private void collectEvidencePhrases(String text, Map<String, HintAccumulator> hints) {
        for (String phrase : EVIDENCE_PHRASES) {
            if (StringUtils.containsIgnoreCase(text, phrase)) {
                addHint(phrase, ProfileHintType.EVIDENCE_TYPE, "evidence_type", hints);
            }
        }
    }

    private boolean isStrongTechnicalToken(String token) {
        String normalized = normalizeHint(token);
        if (StringUtils.isBlank(normalized)) {
            return false;
        }
        if (normalized.length() < 3 || normalized.length() > MAX_HINT_CHARS) {
            return false;
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        if (LOW_SIGNAL_TOKENS.contains(lower)) {
            return false;
        }
        return StringUtils.containsAny(normalized, '_', '-', '.', '/', ':')
                || ENGLISH_DIGIT_PATTERN.matcher(normalized).matches()
                || isUppercaseAcronym(normalized)
                || hasMixedCase(normalized);
    }

    private boolean isUppercaseAcronym(String token) {
        return token.length() >= 2
                && token.length() <= 8
                && token.chars().allMatch(ch -> !Character.isLetter(ch) || Character.isUpperCase(ch));
    }

    private boolean hasMixedCase(String token) {
        if (!token.chars().anyMatch(Character::isUpperCase)
                || !token.chars().anyMatch(Character::isLowerCase)) {
            return false;
        }
        return !Character.isUpperCase(token.charAt(0))
                || token.substring(1).chars().anyMatch(Character::isUpperCase);
    }

    private void addHint(String rawValue, ProfileHintType type, String source,
                         Map<String, HintAccumulator> hints) {
        String value = normalizeHint(rawValue);
        if (StringUtils.isBlank(value)) {
            return;
        }
        String key = value.toLowerCase(Locale.ROOT);
        HintAccumulator accumulator = hints.computeIfAbsent(key, ignored -> new HintAccumulator(value, type, source));
        accumulator.add(type, source);
    }

    private String normalizeHint(String value) {
        return StringUtils.abbreviate(StringUtils.trimToEmpty(value)
                .replaceAll("[`*#>\\[\\](),;，。；：]+", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .replaceAll("^[\\p{Punct}\\s]+|[\\p{Punct}\\s]+$", ""), MAX_HINT_CHARS);
    }

    private int typeWeight(ProfileHintType type) {
        return switch (type) {
            case INLINE_CODE -> 45;
            case CODE_TOKEN -> 40;
            case TECHNICAL_TOKEN -> 35;
            case EVIDENCE_TYPE -> 30;
            case HEADING -> 20;
        };
    }

    private final class HintAccumulator {

        private final String value;
        private ProfileHintType type;
        private String source;
        private int frequency;
        private int score;

        private HintAccumulator(String value, ProfileHintType type, String source) {
            this.value = value;
            this.type = type;
            this.source = source;
        }

        private void add(ProfileHintType nextType, String nextSource) {
            frequency++;
            int nextWeight = typeWeight(nextType);
            if (nextWeight > typeWeight(type)) {
                type = nextType;
                source = nextSource;
            }
            score += nextWeight;
        }

        private String value() {
            return value;
        }

        private int score() {
            return score;
        }

        private ProfileHint toProfileHint() {
            return ProfileHint.builder()
                    .value(value)
                    .type(type)
                    .source(source)
                    .frequency(frequency)
                    .score(score)
                    .build();
        }
    }
}
