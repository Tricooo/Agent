package com.tricoq.domain.agent.service.rag.profile;

import com.tricoq.domain.agent.model.valobj.RagObservationKeys.DocumentMetadata;
import com.tricoq.domain.agent.service.rag.profile.model.KnowledgeBaseProfile;
import com.tricoq.domain.agent.service.rag.profile.model.ProfileHint;
import com.tricoq.domain.agent.service.rag.profile.model.ProfileHintType;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HybridKnowledgeBaseProfileExtractorTest {

    @Test
    public void shouldKeepRuleProfileWhenLlmProfileUnavailable() {
        HybridKnowledgeBaseProfileExtractor extractor = new HybridKnowledgeBaseProfileExtractor(
                new RuleBasedKnowledgeBaseProfileExtractor(),
                new StubLlmProfileExtractor(Optional.empty())
        );

        KnowledgeBaseProfile profile = extractor.extract("rag-1", "grafana", List.of(document("""
                ## 内存使用率查询
                `node_memory_MemAvailable_bytes`
                """)));

        Assert.assertEquals(RuleBasedKnowledgeBaseProfileExtractor.PROFILE_VERSION, profile.getProfileVersion());
        Assert.assertEquals(RuleBasedKnowledgeBaseProfileExtractor.PROFILE_SOURCE, profile.getProfileSource());
        Assert.assertTrue(profile.getHints().stream()
                .anyMatch(hint -> "node_memory_MemAvailable_bytes".equals(hint.getValue())));
    }

    @Test
    public void shouldMergeRuleAndLlmProfileHints() {
        KnowledgeBaseProfile llmProfile = KnowledgeBaseProfile.builder()
                .profileVersion(LlmKnowledgeBaseProfileExtractor.PROFILE_VERSION)
                .profileSource(LlmKnowledgeBaseProfileExtractor.PROFILE_SOURCE)
                .hints(List.of(ProfileHint.builder()
                        .value("冷数据迁移阈值")
                        .type(ProfileHintType.CONCEPT)
                        .source("llm_concept")
                        .sourcePath("control-cn-low-newline.txt")
                        .chunkIndex(0)
                        .evidenceText("冷数据迁移阈值被明确写成：连续二十一天未访问")
                        .frequency(1)
                        .score(60)
                        .build()))
                .build();
        HybridKnowledgeBaseProfileExtractor extractor = new HybridKnowledgeBaseProfileExtractor(
                new RuleBasedKnowledgeBaseProfileExtractor(),
                new StubLlmProfileExtractor(Optional.of(llmProfile))
        );

        KnowledgeBaseProfile profile = extractor.extract("rag-1", "control", List.of(document("""
                ## 内存使用率查询
                `node_memory_MemAvailable_bytes`
                """)));

        Assert.assertEquals(HybridKnowledgeBaseProfileExtractor.PROFILE_VERSION, profile.getProfileVersion());
        Assert.assertEquals(HybridKnowledgeBaseProfileExtractor.PROFILE_SOURCE, profile.getProfileSource());
        Assert.assertTrue(profile.getHints().stream()
                .anyMatch(hint -> "node_memory_MemAvailable_bytes".equals(hint.getValue())));
        Assert.assertTrue(profile.getHints().stream()
                .anyMatch(hint -> "冷数据迁移阈值".equals(hint.getValue())));
    }

    private Document document(String text) {
        return new Document(text, Map.of(
                DocumentMetadata.SOURCE_PATH, "test.md",
                DocumentMetadata.CHUNK_INDEX, 0
        ));
    }

    private static class StubLlmProfileExtractor extends LlmKnowledgeBaseProfileExtractor {

        private final Optional<KnowledgeBaseProfile> profile;

        private StubLlmProfileExtractor(Optional<KnowledgeBaseProfile> profile) {
            super(null, null);
            this.profile = profile;
        }

        @Override
        public Optional<KnowledgeBaseProfile> extract(String ragId, String knowledgeTag, List<Document> documents) {
            return profile;
        }
    }
}
