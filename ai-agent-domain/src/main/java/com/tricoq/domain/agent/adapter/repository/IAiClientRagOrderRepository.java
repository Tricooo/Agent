package com.tricoq.domain.agent.adapter.repository;

import com.tricoq.domain.agent.model.dto.AiRagOrderDTO;

import java.util.List;

/**
 * 知识库配置仓储接口
 */
public interface IAiClientRagOrderRepository {

    boolean insert(AiRagOrderDTO ragOrder);

    boolean updateById(AiRagOrderDTO ragOrder);

    boolean updateByRagId(AiRagOrderDTO ragOrder);

    boolean deleteById(Long id);

    boolean deleteByRagId(String ragId);

    AiRagOrderDTO queryById(Long id);

    AiRagOrderDTO queryByRagId(String ragId);

    List<AiRagOrderDTO> queryEnabledRagOrders();

    List<AiRagOrderDTO> queryByKnowledgeTag(String knowledgeTag);

    List<AiRagOrderDTO> queryAll();
}
