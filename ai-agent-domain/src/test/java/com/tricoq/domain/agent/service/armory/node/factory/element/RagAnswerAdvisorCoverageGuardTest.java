package com.tricoq.domain.agent.service.armory.node.factory.element;

import com.tricoq.domain.agent.model.dto.RerankResult;
import com.tricoq.domain.agent.model.dto.RewriteResult;
import com.tricoq.domain.agent.model.valobj.RagObservationKeys.DocumentMetadata;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RagAnswerAdvisorCoverageGuardTest {

    @Test
    public void shouldProtectEvidenceTypeCandidateWhenRerankDropsItFromFinalTopK() throws Exception {
        Document presentEvidenceChunk = document(3, 1, 1, 4, """
                ### 查询思路
                - 使用 PromQL 计算内存使用率
                - 状态评估需要结合判断标准
                """);
        Document toolChunk = document(1, 7, 1, 4, "grafana/query_prometheus 参数说明");
        Document statusChunk = document(7, 5, 1, 4, "- 当前使用率: {memory_usage}%\n- 状态评估: {memory_status}");
        Document tailChunk = document(2, 3, 1, 4, "node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes");
        Document droppedEvidenceChunk = document(5, 2, 1, 3, """
                ### 内存数据解释
                - 正常范围: 0-80%
                - 警告范围: 80-95%
                - 危险范围: 95-100%
                """);

        List<Document> candidates = List.of(
                presentEvidenceChunk,
                droppedEvidenceChunk,
                tailChunk,
                document(4, 4, 1, 4, "network query example"),
                statusChunk,
                document(9, 6, 1, 4, "best practice example"),
                toolChunk
        );
        List<Document> finalDocuments = List.of(presentEvidenceChunk, toolChunk, statusChunk, tailChunk);
        RerankResult rerankResult = RerankResult.builder()
                .documents(finalDocuments)
                .applied(true)
                .mode("PER_VARIANT_RERANK_RRF")
                .build();
        RewriteResult rewriteResult = RewriteResult.builder()
                .selectedProfileHints(List.of("memory_usage", "内存使用率查询"))
                .build();

        Object coverageResult = invokeCoverageGuard(
                finalDocuments,
                candidates,
                rerankResult,
                "如何判断内存压力，阈值范围是什么？",
                List.of("内存压力 查询思路 判断标准 阈值 步骤"),
                rewriteResult
        );

        @SuppressWarnings("unchecked")
        List<Document> guardedDocuments = (List<Document>) invokeRecordAccessor(coverageResult, "documents");
        Assert.assertEquals(4, guardedDocuments.size());
        Assert.assertTrue(guardedDocuments.stream().anyMatch(document -> chunkIndex(document) == 5));
        Assert.assertFalse(guardedDocuments.stream().anyMatch(document -> chunkIndex(document) == 2));

        Document guardedEvidence = guardedDocuments.stream()
                .filter(document -> chunkIndex(document) == 5)
                .findFirst()
                .orElseThrow();
        Assert.assertEquals(Boolean.TRUE, guardedEvidence.getMetadata().get(DocumentMetadata.COVERAGE_GUARD_ADDED));
        Assert.assertEquals("evidence_type", guardedEvidence.getMetadata().get(DocumentMetadata.COVERAGE_GUARD_REASON));
    }

    private Object invokeCoverageGuard(List<Document> finalDocuments,
                                       List<Document> candidates,
                                       RerankResult rerankResult,
                                       String userText,
                                       List<String> queryVariants,
                                       RewriteResult rewriteResult) throws Exception {
        RagAnswerAdvisor advisor = new RagAnswerAdvisor(null, SearchRequest.builder().topK(4).build());
        Method method = RagAnswerAdvisor.class.getDeclaredMethod(
                "applyRerankCoverageGuard",
                List.class,
                List.class,
                RerankResult.class,
                int.class,
                String.class,
                List.class,
                RewriteResult.class
        );
        method.setAccessible(true);
        return method.invoke(advisor, finalDocuments, candidates, rerankResult, 4, userText, queryVariants, rewriteResult);
    }

    private Object invokeRecordAccessor(Object record, String accessorName) throws Exception {
        Method method = record.getClass().getDeclaredMethod(accessorName);
        method.setAccessible(true);
        return method.invoke(record);
    }

    private Document document(int chunkIndex,
                              int beforeRerankRank,
                              int queryVariantIndex,
                              int queryVariantRank,
                              String text) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DocumentMetadata.SOURCE_PATH, "grafana-mcp-tools-guide.md");
        metadata.put(DocumentMetadata.CHUNK_INDEX, chunkIndex);
        metadata.put(DocumentMetadata.BEFORE_RERANK_RANK, beforeRerankRank);
        metadata.put(DocumentMetadata.QUERY_VARIANT_INDEX, queryVariantIndex);
        metadata.put(DocumentMetadata.QUERY_VARIANT_RANK, queryVariantRank);
        return Document.builder()
                .text(text)
                .metadata(metadata)
                .build();
    }

    private int chunkIndex(Document document) {
        return ((Number) document.getMetadata().get(DocumentMetadata.CHUNK_INDEX)).intValue();
    }
}
