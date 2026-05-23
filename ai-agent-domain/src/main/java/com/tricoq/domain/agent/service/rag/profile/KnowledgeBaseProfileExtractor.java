package com.tricoq.domain.agent.service.rag.profile;

import com.tricoq.domain.agent.service.rag.profile.model.KnowledgeBaseProfile;
import org.springframework.ai.document.Document;

import java.util.List;

public interface KnowledgeBaseProfileExtractor {

    KnowledgeBaseProfile extract(String ragId, String knowledgeTag, List<Document> documents);
}
