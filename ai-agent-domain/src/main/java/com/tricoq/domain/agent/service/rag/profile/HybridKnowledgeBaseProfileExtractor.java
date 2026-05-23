package com.tricoq.domain.agent.service.rag.profile;

import com.tricoq.domain.agent.service.rag.profile.model.KnowledgeBaseProfile;
import com.tricoq.domain.agent.service.rag.profile.model.ProfileHint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Primary profile extractor that keeps rule-v1 as the safe baseline and optionally merges offline LLM signals.
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class HybridKnowledgeBaseProfileExtractor implements KnowledgeBaseProfileExtractor {

    public static final String PROFILE_VERSION = "hybrid-v2";
    public static final String PROFILE_SOURCE = "HYBRID";

    private static final int MAX_HINTS = 120;

    private final RuleBasedKnowledgeBaseProfileExtractor ruleBasedExtractor;
    private final LlmKnowledgeBaseProfileExtractor llmExtractor;

    @Override
    public KnowledgeBaseProfile extract(String ragId, String knowledgeTag, List<Document> documents) {
        KnowledgeBaseProfile ruleProfile = ruleBasedExtractor.extract(ragId, knowledgeTag, documents);
        Optional<KnowledgeBaseProfile> llmProfile = llmExtractor.extract(ragId, knowledgeTag, documents);
        if (llmProfile.isEmpty()) {
            return ruleProfile;
        }

        KnowledgeBaseProfile llm = llmProfile.get();
        List<ProfileHint> mergedHints = mergeHints(ruleProfile.getHints(), llm.getHints());
        log.info("RAG知识库画像hybrid合并完成: ragId={}, tag={}, ruleHints={}, llmHints={}, mergedHints={}",
                ragId,
                knowledgeTag,
                ruleProfile.getHints() == null ? 0 : ruleProfile.getHints().size(),
                llm.getHints() == null ? 0 : llm.getHints().size(),
                mergedHints.size());

        return KnowledgeBaseProfile.builder()
                .ragId(ragId)
                .knowledgeTag(knowledgeTag)
                .profileVersion(PROFILE_VERSION)
                .profileSource(PROFILE_SOURCE)
                .generatedAt(LocalDateTime.now())
                .summary(llm.getSummary())
                .concepts(llm.getConcepts())
                .aliases(llm.getAliases())
                .questionsAnswered(llm.getQuestionsAnswered())
                .negativeScopes(llm.getNegativeScopes())
                .evidenceTypes(llm.getEvidenceTypes())
                .hints(mergedHints)
                .build();
    }

    private List<ProfileHint> mergeHints(List<ProfileHint> ruleHints, List<ProfileHint> llmHints) {
        Map<String, ProfileHint> merged = new LinkedHashMap<>();
        appendHints(merged, ruleHints);
        appendHints(merged, llmHints);
        return merged.values().stream()
                .sorted(Comparator.comparingInt(this::score).reversed()
                        .thenComparing(ProfileHint::getValue))
                .limit(MAX_HINTS)
                .toList();
    }

    private void appendHints(Map<String, ProfileHint> merged, List<ProfileHint> hints) {
        if (CollectionUtils.isEmpty(hints)) {
            return;
        }
        for (ProfileHint hint : hints) {
            if (hint == null || StringUtils.isBlank(hint.getValue())) {
                continue;
            }
            String key = hint.getValue().trim().toLowerCase(Locale.ROOT);
            merged.merge(key, hint, this::higherScoreHint);
        }
    }

    private ProfileHint higherScoreHint(ProfileHint left, ProfileHint right) {
        return score(right) > score(left) ? right : left;
    }

    private int score(ProfileHint hint) {
        return hint.getScore() == null ? 0 : hint.getScore();
    }
}
