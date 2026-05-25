package com.tricoq.domain.agent.service.armory.node.factory.element;

import org.junit.Assert;
import org.junit.Test;

public class ContextSalienceSupportTest {

    @Test
    public void shouldDetectFormulaAndSelfContainedRangeEvidence() {
        ContextSalienceSupport.Analysis analysis = ContextSalienceSupport.analyze("""
                ### Resource pressure levels
                ```promql
                (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
                ```
                - Low risk: 0-80%
                - Medium risk: 80-95%
                - High risk: 95-100%
                """);

        Assert.assertTrue(analysis.cues().contains("formula"));
        Assert.assertTrue(analysis.cues().contains("range"));
        Assert.assertFalse(analysis.needsPreviousChunkContext());
    }

    @Test
    public void shouldRequestPreviousChunkWhenStructuredRangeListStartsMidChunk() {
        ContextSalienceSupport.Analysis analysis = ContextSalienceSupport.analyze("""
                - High risk: 95-100%

                ### Network evidence
                - Unit: bps
                - Expected range: depends on bandwidth
                """);

        Assert.assertTrue(analysis.cues().contains("range"));
        Assert.assertTrue(analysis.needsPreviousChunkContext());
    }

    @Test
    public void shouldRequestPreviousChunkWhenTableStartsAtChunkBoundary() {
        ContextSalienceSupport.Analysis analysis = ContextSalienceSupport.analyze("""
                | level | threshold |
                | high | 95-100% |
                """);

        Assert.assertTrue(analysis.needsPreviousChunkContext());
    }

    @Test
    public void shouldRequestPreviousChunkWhenCodeBlockStartsBeforeCurrentChunk() {
        ContextSalienceSupport.Analysis analysis = ContextSalienceSupport.analyze("""
                (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
                ```
                """);

        Assert.assertTrue(analysis.cues().contains("formula"));
        Assert.assertTrue(analysis.needsPreviousChunkContext());
    }

    @Test
    public void shouldMatchEvidenceWhenQueryIntentAsksForRangeAndJudgement() {
        ContextSalienceSupport.Analysis intent = ContextSalienceSupport.analyzeIntent(
                "如何判断内存压力，阈值范围是什么？",
                java.util.List.of("内存压力 查询思路 判断标准 阈值 步骤"),
                java.util.List.of("memory_usage", "内存使用率查询")
        );
        ContextSalienceSupport.Analysis evidence = ContextSalienceSupport.analyze("""
                ### 内存数据解释
                - 正常范围: 0-80%
                - 警告范围: 80-95%
                - 危险范围: 95-100%
                """);

        Assert.assertTrue(ContextSalienceSupport.coverageCues(intent).contains("range"));
        Assert.assertTrue(ContextSalienceSupport.matchesCoverageIntent(intent, evidence));
    }

    @Test
    public void shouldNotMatchEvidenceWhenQueryHasNoEvidenceIntent() {
        ContextSalienceSupport.Analysis intent = ContextSalienceSupport.analyzeIntent(
                "文档指定的 cloud provider 是什么？",
                java.util.List.of("cloud provider hosting deployment"),
                java.util.List.of()
        );
        ContextSalienceSupport.Analysis evidence = ContextSalienceSupport.analyze("""
                - Retry budget: 3 attempts
                - Timeout range: 10-30s
                """);

        Assert.assertTrue(ContextSalienceSupport.coverageCues(intent).isEmpty());
        Assert.assertFalse(ContextSalienceSupport.matchesCoverageIntent(intent, evidence));
    }
}
