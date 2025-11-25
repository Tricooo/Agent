package com.tricoq.domain.agent.adapter.repository;

import com.tricoq.domain.agent.model.valobj.AiRagOrderVO;

/**
 * RAG 仓储
 */
public interface IRagRepository {

    void createTagOrder(AiRagOrderVO aiRagOrderVO);
}
