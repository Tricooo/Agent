package com.tricoq.domain.agent.service.rag.profile;

import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.model.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.model.request.StructuredInvocationRequest;
import com.tricoq.domain.agent.model.valobj.RagObservationKeys.DocumentMetadata;
import com.tricoq.domain.agent.service.armory.node.factory.DefaultArmoryStrategyFactory;
import com.tricoq.domain.agent.service.rag.profile.model.GroundedProfileItem;
import com.tricoq.domain.agent.service.rag.profile.model.KnowledgeBaseProfile;
import com.tricoq.domain.agent.service.rag.profile.model.LlmKnowledgeBaseProfileResponse;
import com.tricoq.domain.agent.service.rag.profile.model.ProfileAlias;
import com.tricoq.domain.agent.service.rag.profile.model.ProfileHint;
import com.tricoq.domain.agent.service.rag.profile.model.ProfileHintType;
import com.tricoq.domain.agent.spi.LlmInvocationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Offline LLM profile extractor. It only produces retrieval navigation metadata.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LlmKnowledgeBaseProfileExtractor {

    public static final String PROFILE_VERSION = "llm-v1";
    public static final String PROFILE_SOURCE = "LLM";

    private static final String ROLE_SUFFIX = "-rag-profile";
    private static final int MAX_HINT_VALUE_CHARS = 120;
    private static final int MAX_SUMMARY_CHARS = 240;

    private static final String PROFILE_PROMPT_TEMPLATE = """
            你是知识库离线画像抽取器，不要回答用户问题，也不要补充常识。

            目标：
            - 从输入片段中抽取检索导航信息，帮助后续 query rewrite 选择检索路线。
            - Profile 不是答案事实依据，最终回答仍必须基于后续召回 chunk。
            - 所有 concepts / aliases / questionsAnswered / negativeScopes / evidenceTypes 都必须带 sourcePath、chunkIndex 和 evidenceText。

            严格约束：
            - evidenceText 必须是对应片段里的连续原文短句或短语。
            - 如果某项不能被原文 evidenceText 支撑，不要输出。
            - negativeScopes 只记录文档明确说“没有说明 / 不包含 / cannot be answered / not specified”的范围。
            - aliases 只输出能帮助自然语言问题映射到文档术语的短别名。
            - 不要输出密码、密钥、URL 参数中的敏感值。

            输出字段：
            - summary：80 字以内，概括这批片段覆盖的主题。
            - concepts：核心概念或术语。
            - aliases：术语及其自然语言别名。
            - questionsAnswered：文档明确能回答的问题。
            - negativeScopes：文档明确不能回答或未覆盖的问题范围。
            - evidenceTypes：承载答案的证据类型。

            知识库标签：%s

            输入片段：
            %s
            """;

    private final LlmInvocationFacade facade;
    private final DefaultArmoryStrategyFactory armoryStrategyFactory;

    @Value("${spring.ai.rag.profile.llm.enabled:false}")
    private boolean enabled;

    @Value("${spring.ai.rag.profile.llm.client-id:}")
    private String clientId;

    @Value("${spring.ai.rag.profile.llm.max-snippets:12}")
    private int maxSnippets;

    @Value("${spring.ai.rag.profile.llm.max-snippet-chars:900}")
    private int maxSnippetChars;

    @Value("${spring.ai.rag.profile.llm.max-items-per-type:12}")
    private int maxItemsPerType;

    @Value("${spring.ai.rag.profile.llm.timeout-millis:45000}")
    private long timeoutMillis;

    public Optional<KnowledgeBaseProfile> extract(String ragId, String knowledgeTag, List<Document> documents) {
        if (!enabled || StringUtils.isBlank(clientId) || CollectionUtils.isEmpty(documents)) {
            return Optional.empty();
        }

        List<ProfileSnippet> snippets = snippets(documents);
        if (snippets.isEmpty()) {
            return Optional.empty();
        }

        Map<String, String> snippetsByKey = snippets.stream()
                .collect(Collectors.toMap(
                        ProfileSnippet::key,
                        ProfileSnippet::text,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        try {
            ensureProfileClientLoaded();
            LlmKnowledgeBaseProfileResponse response = facade.invokeStructured(StructuredInvocationRequest
                    .<LlmKnowledgeBaseProfileResponse>builder()
                    .operationName("rag.profile.extract")
                    .clientId(clientId)
                    .sessionId(ragId)
                    .roleSuffix(ROLE_SUFFIX)
                    .prompt(PROFILE_PROMPT_TEMPLATE.formatted(
                            StringUtils.defaultString(knowledgeTag),
                            formatSnippets(snippets)
                    ))
                    .responseType(LlmKnowledgeBaseProfileResponse.class)
                    .retrieveSize(0)
                    .validate(LlmKnowledgeBaseProfileResponse::validate)
                    .maxAttempts(1)
                    .timeoutMillis(timeoutMillis)
                    .build());

            return toProfile(ragId, knowledgeTag, response, snippetsByKey);
        } catch (Exception e) {
            log.warn("RAG离线LLM画像抽取失败，回退规则画像: ragId={}, tag={}, reason={}",
                    ragId, knowledgeTag, StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName()));
            return Optional.empty();
        }
    }

    private void ensureProfileClientLoaded() {
        armoryStrategyFactory.armoryStrategyHandler().apply(
                ArmoryCommandEntity.builder()
                        .commandType(AiAgentEnumVO.AI_CLIENT.getCode())
                        .commandIdList(List.of(clientId))
                        .build(),
                new DefaultArmoryStrategyFactory.DynamicContext()
        );
    }

    private Optional<KnowledgeBaseProfile> toProfile(String ragId, String knowledgeTag,
                                                     LlmKnowledgeBaseProfileResponse response,
                                                     Map<String, String> snippetsByKey) {
        List<GroundedProfileItem> concepts = groundedItems(response.getConcepts(), snippetsByKey);
        List<ProfileAlias> aliases = groundedAliases(response.getAliases(), snippetsByKey);
        List<GroundedProfileItem> questionsAnswered = groundedItems(response.getQuestionsAnswered(), snippetsByKey);
        List<GroundedProfileItem> negativeScopes = groundedItems(response.getNegativeScopes(), snippetsByKey);
        List<GroundedProfileItem> evidenceTypes = groundedItems(response.getEvidenceTypes(), snippetsByKey);

        List<ProfileHint> hints = new ArrayList<>();
        appendGroundedHints(hints, concepts, ProfileHintType.CONCEPT, "llm_concept", 60);
        appendAliasHints(hints, aliases);
        appendGroundedHints(hints, questionsAnswered, ProfileHintType.QUESTION_ANSWERED, "llm_question_answered", 46);
        appendGroundedHints(hints, negativeScopes, ProfileHintType.NEGATIVE_SCOPE, "llm_negative_scope", 42);
        appendGroundedHints(hints, evidenceTypes, ProfileHintType.LLM_EVIDENCE_TYPE, "llm_evidence_type", 40);

        if (hints.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(KnowledgeBaseProfile.builder()
                .ragId(ragId)
                .knowledgeTag(knowledgeTag)
                .profileVersion(PROFILE_VERSION)
                .profileSource(PROFILE_SOURCE)
                .generatedAt(LocalDateTime.now())
                .summary(StringUtils.abbreviate(StringUtils.trimToEmpty(response.getSummary()), MAX_SUMMARY_CHARS))
                .concepts(concepts)
                .aliases(aliases)
                .questionsAnswered(questionsAnswered)
                .negativeScopes(negativeScopes)
                .evidenceTypes(evidenceTypes)
                .hints(limitHints(hints))
                .build());
    }

    private void appendGroundedHints(List<ProfileHint> hints, List<GroundedProfileItem> items,
                                     ProfileHintType type, String source, int score) {
        if (CollectionUtils.isEmpty(items)) {
            return;
        }
        for (GroundedProfileItem item : items) {
            String value = normalizeHintValue(item.getValue());
            if (StringUtils.isBlank(value)) {
                continue;
            }
            hints.add(ProfileHint.builder()
                    .value(value)
                    .type(type)
                    .source(source)
                    .sourcePath(item.getSourcePath())
                    .chunkIndex(item.getChunkIndex())
                    .evidenceText(StringUtils.abbreviate(StringUtils.trimToEmpty(item.getEvidenceText()), MAX_HINT_VALUE_CHARS))
                    .frequency(1)
                    .score(score)
                    .build());
        }
    }

    private void appendAliasHints(List<ProfileHint> hints, List<ProfileAlias> aliases) {
        if (CollectionUtils.isEmpty(aliases)) {
            return;
        }
        for (ProfileAlias aliasGroup : aliases) {
            Set<String> values = new LinkedHashSet<>();
            values.add(aliasGroup.getValue());
            if (CollectionUtils.isNotEmpty(aliasGroup.getAliases())) {
                values.addAll(aliasGroup.getAliases());
            }
            for (String rawValue : values) {
                String value = normalizeHintValue(rawValue);
                if (StringUtils.isBlank(value)) {
                    continue;
                }
                hints.add(ProfileHint.builder()
                        .value(value)
                        .type(ProfileHintType.ALIAS)
                        .source("llm_alias")
                        .sourcePath(aliasGroup.getSourcePath())
                        .chunkIndex(aliasGroup.getChunkIndex())
                        .evidenceText(StringUtils.abbreviate(StringUtils.trimToEmpty(aliasGroup.getEvidenceText()), MAX_HINT_VALUE_CHARS))
                        .frequency(1)
                        .score(52)
                        .build());
            }
        }
    }

    private List<ProfileHint> limitHints(List<ProfileHint> hints) {
        Map<String, ProfileHint> deduplicated = new LinkedHashMap<>();
        for (ProfileHint hint : hints) {
            String key = StringUtils.lowerCase(StringUtils.trimToEmpty(hint.getValue()), Locale.ROOT);
            if (StringUtils.isBlank(key)) {
                continue;
            }
            deduplicated.merge(key, hint, this::higherScoreHint);
        }
        return deduplicated.values().stream()
                .sorted((left, right) -> Integer.compare(score(right), score(left)))
                .limit(Math.max(1, maxItemsPerType * 5L))
                .toList();
    }

    private ProfileHint higherScoreHint(ProfileHint left, ProfileHint right) {
        return score(right) > score(left) ? right : left;
    }

    private int score(ProfileHint hint) {
        return hint.getScore() == null ? 0 : hint.getScore();
    }

    private List<GroundedProfileItem> groundedItems(List<GroundedProfileItem> items, Map<String, String> snippetsByKey) {
        if (CollectionUtils.isEmpty(items)) {
            return List.of();
        }
        return items.stream()
                .filter(item -> grounded(item, snippetsByKey))
                .map(this::normalizeGroundedItem)
                .limit(effectiveMaxItemsPerType())
                .toList();
    }

    private List<ProfileAlias> groundedAliases(List<ProfileAlias> aliases, Map<String, String> snippetsByKey) {
        if (CollectionUtils.isEmpty(aliases)) {
            return List.of();
        }
        return aliases.stream()
                .filter(alias -> CollectionUtils.isNotEmpty(alias.getAliases()))
                .filter(alias -> grounded(alias, snippetsByKey))
                .map(this::normalizeAlias)
                .limit(effectiveMaxItemsPerType())
                .toList();
    }

    private boolean grounded(GroundedProfileItem item, Map<String, String> snippetsByKey) {
        if (item == null || StringUtils.isBlank(item.getValue())) {
            return false;
        }
        return evidenceSupported(item.getSourcePath(), item.getChunkIndex(), item.getEvidenceText(), snippetsByKey);
    }

    private boolean grounded(ProfileAlias alias, Map<String, String> snippetsByKey) {
        if (alias == null || StringUtils.isBlank(alias.getValue())) {
            return false;
        }
        return evidenceSupported(alias.getSourcePath(), alias.getChunkIndex(), alias.getEvidenceText(), snippetsByKey);
    }

    private boolean evidenceSupported(String sourcePath, Integer chunkIndex, String evidenceText,
                                      Map<String, String> snippetsByKey) {
        if (StringUtils.isBlank(sourcePath) || chunkIndex == null || StringUtils.isBlank(evidenceText)) {
            return false;
        }
        String sourceText = snippetsByKey.get(ProfileSnippet.key(sourcePath, chunkIndex));
        if (StringUtils.isBlank(sourceText)) {
            return false;
        }
        return compactForMatch(sourceText).contains(compactForMatch(evidenceText));
    }

    private GroundedProfileItem normalizeGroundedItem(GroundedProfileItem item) {
        return GroundedProfileItem.builder()
                .value(normalizeHintValue(item.getValue()))
                .sourcePath(StringUtils.trimToEmpty(item.getSourcePath()))
                .chunkIndex(item.getChunkIndex())
                .evidenceText(StringUtils.abbreviate(StringUtils.trimToEmpty(item.getEvidenceText()), MAX_HINT_VALUE_CHARS))
                .build();
    }

    private ProfileAlias normalizeAlias(ProfileAlias alias) {
        List<String> aliases = alias.getAliases().stream()
                .map(this::normalizeHintValue)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .limit(6)
                .toList();
        return ProfileAlias.builder()
                .value(normalizeHintValue(alias.getValue()))
                .aliases(aliases)
                .sourcePath(StringUtils.trimToEmpty(alias.getSourcePath()))
                .chunkIndex(alias.getChunkIndex())
                .evidenceText(StringUtils.abbreviate(StringUtils.trimToEmpty(alias.getEvidenceText()), MAX_HINT_VALUE_CHARS))
                .build();
    }

    private List<ProfileSnippet> snippets(List<Document> documents) {
        List<ProfileSnippet> snippets = new ArrayList<>();
        int limit = Math.max(1, maxSnippets);
        for (int i = 0; i < documents.size() && snippets.size() < limit; i++) {
            Document document = documents.get(i);
            String text = StringUtils.trimToEmpty(document.getText());
            if (StringUtils.isBlank(text)) {
                continue;
            }
            String sourcePath = metadataText(document, DocumentMetadata.SOURCE_PATH);
            if (StringUtils.isBlank(sourcePath)) {
                sourcePath = "document-" + i;
            }
            Integer chunkIndex = metadataInteger(document, DocumentMetadata.CHUNK_INDEX);
            if (chunkIndex == null) {
                chunkIndex = i;
            }
            snippets.add(new ProfileSnippet(sourcePath, chunkIndex,
                    StringUtils.abbreviate(text, Math.max(120, maxSnippetChars))));
        }
        return snippets;
    }

    private String formatSnippets(List<ProfileSnippet> snippets) {
        return snippets.stream()
                .map(snippet -> """
                        [%s#%s]
                        %s
                        """.formatted(snippet.sourcePath(), snippet.chunkIndex(), snippet.text()))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String metadataText(Document document, String key) {
        Object value = document.getMetadata().get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private Integer metadataInteger(Document document, String key) {
        Object value = document.getMetadata().get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String normalizeHintValue(String value) {
        return StringUtils.abbreviate(StringUtils.trimToEmpty(value)
                .replaceAll("[`*#>\\[\\](),;，。；：]+", " ")
                .replaceAll("\\s+", " ")
                .trim(), MAX_HINT_VALUE_CHARS);
    }

    private String compactForMatch(String value) {
        return StringUtils.lowerCase(StringUtils.trimToEmpty(value).replaceAll("\\s+", " "), Locale.ROOT);
    }

    private long effectiveMaxItemsPerType() {
        return Math.max(1, maxItemsPerType);
    }

    private record ProfileSnippet(String sourcePath, Integer chunkIndex, String text) {

        private String key() {
            return key(sourcePath, chunkIndex);
        }

        private static String key(String sourcePath, Integer chunkIndex) {
            return sourcePath + "#" + chunkIndex;
        }
    }
}
