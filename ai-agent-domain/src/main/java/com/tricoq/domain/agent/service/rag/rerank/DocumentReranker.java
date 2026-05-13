package com.tricoq.domain.agent.service.rag.rerank;

import com.tricoq.domain.agent.model.dto.RerankResult;
import org.springframework.ai.document.Document;

import java.util.List;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/13/26
 */
public interface DocumentReranker {

    RerankResult rerank(String query, List<Document> candidates, int topK);

}
