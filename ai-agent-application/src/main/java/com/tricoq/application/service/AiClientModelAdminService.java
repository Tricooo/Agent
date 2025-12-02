package com.tricoq.application.service;

import com.tricoq.api.dto.AiClientModelQueryRequestDTO;
import com.tricoq.api.dto.AiClientModelRequestDTO;
import com.tricoq.api.dto.AiClientModelResponseDTO;
import com.tricoq.domain.agent.adapter.repository.IAiClientModelRepository;
import com.tricoq.domain.agent.model.dto.AiClientModelDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * AI 客户端模型配置应用服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiClientModelAdminService {

    private final IAiClientModelRepository aiClientModelRepository;

    public boolean createAiClientModel(AiClientModelRequestDTO request) {
        log.info("创建AI客户端模型配置请求：{}", request);
        AiClientModelDTO model = toDomain(request);
        model.setCreateTime(LocalDateTime.now());
        model.setUpdateTime(LocalDateTime.now());
        return aiClientModelRepository.insert(model);
    }

    public boolean updateAiClientModelById(AiClientModelRequestDTO request) {
        log.info("根据ID更新AI客户端模型配置请求：{}", request);
        if (request.getId() == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        AiClientModelDTO model = toDomain(request);
        model.setUpdateTime(LocalDateTime.now());
        return aiClientModelRepository.updateById(model);
    }

    public boolean updateAiClientModelByModelId(AiClientModelRequestDTO request) {
        log.info("根据模型ID更新AI客户端模型配置请求：{}", request);
        if (!StringUtils.hasText(request.getModelId())) {
            throw new IllegalArgumentException("模型ID不能为空");
        }
        AiClientModelDTO model = toDomain(request);
        model.setUpdateTime(LocalDateTime.now());
        return aiClientModelRepository.updateByModelId(model);
    }

    public boolean deleteAiClientModelById(Long id) {
        log.info("根据ID删除AI客户端模型配置请求：{}", id);
        return aiClientModelRepository.deleteById(id);
    }

    public boolean deleteAiClientModelByModelId(String modelId) {
        log.info("根据模型ID删除AI客户端模型配置请求：{}", modelId);
        return aiClientModelRepository.deleteByModelId(modelId);
    }

    public AiClientModelResponseDTO queryAiClientModelById(Long id) {
        log.info("根据ID查询AI客户端模型配置请求：{}", id);
        AiClientModelDTO model = aiClientModelRepository.queryById(id);
        return toResponse(model);
    }

    public AiClientModelResponseDTO queryAiClientModelByModelId(String modelId) {
        log.info("根据模型ID查询AI客户端模型配置请求：{}", modelId);
        AiClientModelDTO model = aiClientModelRepository.queryByModelId(modelId);
        return toResponse(model);
    }

    public List<AiClientModelResponseDTO> queryAiClientModelsByApiId(String apiId) {
        log.info("根据API配置ID查询AI客户端模型配置列表请求：{}", apiId);
        return aiClientModelRepository.queryByApiId(apiId).stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientModelResponseDTO> queryAiClientModelsByModelType(String modelType) {
        log.info("根据模型类型查询AI客户端模型配置列表请求：{}", modelType);
        return aiClientModelRepository.queryByModelType(modelType).stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientModelResponseDTO> queryEnabledAiClientModels() {
        log.info("查询所有启用的AI客户端模型配置请求");
        return aiClientModelRepository.queryEnabledModels().stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientModelResponseDTO> queryAiClientModelList(AiClientModelQueryRequestDTO request) {
        log.info("根据条件查询AI客户端模型配置列表请求：{}", request);
        List<AiClientModelDTO> models;
        if (StringUtils.hasText(request.getModelId())) {
            AiClientModelDTO model = aiClientModelRepository.queryByModelId(request.getModelId());
            models = model != null ? List.of(model) : List.of();
        } else if (StringUtils.hasText(request.getApiId())) {
            models = aiClientModelRepository.queryByApiId(request.getApiId());
        } else if (StringUtils.hasText(request.getModelType())) {
            models = aiClientModelRepository.queryByModelType(request.getModelType());
        } else if (request.getStatus() != null && request.getStatus() == 1) {
            models = aiClientModelRepository.queryEnabledModels();
        } else {
            models = aiClientModelRepository.queryAll();
        }
        return models.stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientModelResponseDTO> queryAllAiClientModels() {
        log.info("查询所有AI客户端模型配置请求");
        return aiClientModelRepository.queryAll().stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private AiClientModelDTO toDomain(AiClientModelRequestDTO requestDTO) {
        AiClientModelDTO dto = new AiClientModelDTO();
        BeanUtils.copyProperties(requestDTO, dto);
        return dto;
    }

    private AiClientModelResponseDTO toResponse(AiClientModelDTO model) {
        if (model == null) {
            return null;
        }
        AiClientModelResponseDTO responseDTO = new AiClientModelResponseDTO();
        BeanUtils.copyProperties(model, responseDTO);
        return responseDTO;
    }
}
