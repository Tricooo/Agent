package com.tricoq.domain.agent.adapter.repository;

import com.tricoq.domain.agent.model.dto.AiClientModelDTO;

import java.util.List;

/**
 * 模型配置仓储接口
 */
public interface IAiClientModelRepository {

    boolean insert(AiClientModelDTO model);

    boolean updateById(AiClientModelDTO model);

    boolean updateByModelId(AiClientModelDTO model);

    boolean deleteById(Long id);

    boolean deleteByModelId(String modelId);

    AiClientModelDTO queryById(Long id);

    AiClientModelDTO queryByModelId(String modelId);

    List<AiClientModelDTO> queryByApiId(String apiId);

    List<AiClientModelDTO> queryByModelType(String modelType);

    List<AiClientModelDTO> queryEnabledModels();

    List<AiClientModelDTO> queryAll();
}
