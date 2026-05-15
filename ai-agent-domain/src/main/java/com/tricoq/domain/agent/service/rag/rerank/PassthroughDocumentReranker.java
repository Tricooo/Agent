package com.tricoq.domain.agent.service.rag.rerank;

import com.tricoq.domain.agent.model.dto.RerankResult;
import com.tricoq.domain.agent.model.valobj.RagObservationKeys;
import com.tricoq.domain.agent.service.rag.rerank.enums.RerankPolicy;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/13/26
 */
@Component
public class PassthroughDocumentReranker implements DocumentReranker {

    @Override
    public RerankResult rerank(String query, List<Document> candidates, int topK) {
        int candidateCount = candidates == null ? 0 : candidates.size();
        if (CollectionUtils.isEmpty(candidates) || topK <= 0) {
            return RerankResult.builder().documents(List.of())
                    .applied(false)
                    .mode(RerankPolicy.PASSTHROUGH.getPolicyName())
                    .candidateCount(candidateCount)
                    .finalCount(0)
                    .failureReason(null)
                    .modelName(null)
                    .endpoint(null)
                    .build();
        }

        int limit = Math.min(topK, candidates.size());
        List<Document> rerankedDocuments = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            Document document = candidates.get(i);
            Map<String, Object> metadata = new HashMap<>(document.getMetadata());
            int rank = i + 1;
            metadata.put(RagObservationKeys.DocumentMetadata.BEFORE_RERANK_RANK, rank);
            metadata.put(RagObservationKeys.DocumentMetadata.RERANK_RANK, rank);
            metadata.put(RagObservationKeys.DocumentMetadata.RERANK_APPLIED, false);
            metadata.put(RagObservationKeys.DocumentMetadata.RERANK_MODE, RerankPolicy.PASSTHROUGH.getPolicyName());
            rerankedDocuments.add(document.mutate()
                    .metadata(metadata)
                    .build());
        }
        return RerankResult.builder().documents(new ArrayList<>(rerankedDocuments))
                .applied(false)
                .mode(RerankPolicy.PASSTHROUGH.getPolicyName())
                .candidateCount(candidateCount)
                .finalCount(limit)
                .failureReason(null)
                .modelName(null)
                .endpoint(null)
                .build();
    }
}
