package com.tricoq.infrastructure.adapter.repository;

import com.tricoq.domain.agent.adapter.repository.IAiClientModelRepository;
import com.tricoq.domain.agent.model.dto.AiClientModelDTO;
import com.tricoq.infrastructure.dao.po.AiClientModel;
import com.tricoq.infrastructure.support.AiClientModelDaoSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 模型配置仓储实现
 */
@Repository
@RequiredArgsConstructor
public class AiClientModelRepositoryImpl implements IAiClientModelRepository {

    private final AiClientModelDaoSupport clientModelDaoSupport;

    @Override
    public boolean insert(AiClientModelDTO model) {
        if (model == null) {
            return false;
        }
        AiClientModel po = toPo(model);
        LocalDateTime now = LocalDateTime.now();
        po.setCreateTime(now);
        po.setUpdateTime(now);
        return clientModelDaoSupport.save(po);
    }

    @Override
    public boolean updateById(AiClientModelDTO model) {
        if (model == null || model.getId() == null) {
            return false;
        }
        AiClientModel po = toPo(model);
        po.setId(model.getId());
        po.setUpdateTime(LocalDateTime.now());
        return clientModelDaoSupport.updateById(po);
    }

    @Override
    public boolean updateByModelId(AiClientModelDTO model) {
        if (model == null || !StringUtils.hasText(model.getModelId())) {
            return false;
        }
        AiClientModel po = toPo(model);
        po.setUpdateTime(LocalDateTime.now());
        return clientModelDaoSupport.getBaseMapper().updateByModelId(po) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return id != null && clientModelDaoSupport.removeById(id);
    }

    @Override
    public boolean deleteByModelId(String modelId) {
        return StringUtils.hasText(modelId) && clientModelDaoSupport.getBaseMapper().deleteByModelId(modelId) > 0;
    }

    @Override
    public AiClientModelDTO queryById(Long id) {
        if (id == null) {
            return null;
        }
        return toDto(clientModelDaoSupport.getById(id));
    }

    @Override
    public AiClientModelDTO queryByModelId(String modelId) {
        if (!StringUtils.hasText(modelId)) {
            return null;
        }
        return toDto(clientModelDaoSupport.getBaseMapper().queryByModelId(modelId));
    }

    @Override
    public List<AiClientModelDTO> queryByApiId(String apiId) {
        if (!StringUtils.hasText(apiId)) {
            return List.of();
        }
        return clientModelDaoSupport.getBaseMapper().queryByApiId(apiId).stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AiClientModelDTO> queryByModelType(String modelType) {
        if (!StringUtils.hasText(modelType)) {
            return List.of();
        }
        return clientModelDaoSupport.getBaseMapper().queryByModelType(modelType).stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AiClientModelDTO> queryEnabledModels() {
        return clientModelDaoSupport.getBaseMapper().queryEnabledModels().stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AiClientModelDTO> queryAll() {
        return clientModelDaoSupport.getBaseMapper().queryAll().stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    private AiClientModelDTO toDto(AiClientModel po) {
        if (po == null) {
            return null;
        }
        return AiClientModelDTO.builder()
                .id(po.getId())
                .modelId(po.getModelId())
                .apiId(po.getApiId())
                .modelName(po.getModelName())
                .modelType(po.getModelType())
                .status(po.getStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiClientModel toPo(AiClientModelDTO dto) {
        if (dto == null) {
            return null;
        }
        return AiClientModel.builder()
                .id(dto.getId())
                .modelId(dto.getModelId())
                .apiId(dto.getApiId())
                .modelName(dto.getModelName())
                .modelType(dto.getModelType())
                .status(dto.getStatus())
                .createTime(dto.getCreateTime())
                .updateTime(dto.getUpdateTime())
                .build();
    }
}
