package com.tricoq.domain.agent.adapter.repository;

import com.tricoq.domain.agent.model.dto.AiRagOrderDTO;

/**
 * RAG 仓储
 *
 * @author trico qiang
 * @date 11/25/25
 */
public interface IRagRepository {

    void createTagOrder(AiRagOrderDTO aiRagOrderVO);
}
