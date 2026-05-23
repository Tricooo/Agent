package com.tricoq.domain.agent.adapter.repository;

import com.tricoq.domain.agent.service.rag.profile.model.KnowledgeBaseProfile;

import java.util.Collection;
import java.util.List;

public interface IKnowledgeBaseProfileRepository {

    boolean saveOrUpdate(KnowledgeBaseProfile profile);

    KnowledgeBaseProfile queryByKnowledgeTag(String knowledgeTag);

    List<KnowledgeBaseProfile> queryByKnowledgeTags(Collection<String> knowledgeTags);
}
