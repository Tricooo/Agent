package com.tricoq.domain.agent.service.rag.rerank.factory;

import com.tricoq.domain.agent.service.rag.rerank.DocumentReranker;
import com.tricoq.domain.agent.service.rag.rerank.enums.RerankPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/14/26
 */
@Service
@RequiredArgsConstructor
public class DocumentRerankerFactory {

    private final Map<String, DocumentReranker> documentRerankers;

    public DocumentReranker getDocumentReranker(String policy) {
        RerankPolicy rerankPolicy = RerankPolicy.getByPolicyOrDefault(policy);
        return documentRerankers.get(rerankPolicy.getBeanName());
    }
}
