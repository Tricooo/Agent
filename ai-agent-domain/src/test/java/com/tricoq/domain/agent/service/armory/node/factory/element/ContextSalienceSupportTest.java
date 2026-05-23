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
}
