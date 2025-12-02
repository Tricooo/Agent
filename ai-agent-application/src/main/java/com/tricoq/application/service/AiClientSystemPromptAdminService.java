package com.tricoq.application.service;

import com.tricoq.api.dto.AiClientSystemPromptQueryRequestDTO;
import com.tricoq.api.dto.AiClientSystemPromptRequestDTO;
import com.tricoq.api.dto.AiClientSystemPromptResponseDTO;
import com.tricoq.domain.agent.adapter.repository.IAiClientSystemPromptRepository;
import com.tricoq.domain.agent.model.dto.AiClientSystemPromptDTO;
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
 * 系统提示词配置应用服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiClientSystemPromptAdminService {

    private final IAiClientSystemPromptRepository aiClientSystemPromptRepository;

    public boolean createAiClientSystemPrompt(AiClientSystemPromptRequestDTO request) {
        log.info("创建系统提示词配置请求：{}", request);
        AiClientSystemPromptDTO prompt = toDomain(request);
        prompt.setCreateTime(LocalDateTime.now());
        prompt.setUpdateTime(LocalDateTime.now());
        return aiClientSystemPromptRepository.insert(prompt);
    }

    public boolean updateAiClientSystemPromptById(AiClientSystemPromptRequestDTO request) {
        log.info("根据ID更新系统提示词配置请求：{}", request);
        if (request.getId() == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        AiClientSystemPromptDTO prompt = toDomain(request);
        prompt.setUpdateTime(LocalDateTime.now());
        return aiClientSystemPromptRepository.updateById(prompt);
    }

    public boolean updateAiClientSystemPromptByPromptId(AiClientSystemPromptRequestDTO request) {
        log.info("根据提示词ID更新系统提示词配置请求：{}", request);
        if (!StringUtils.hasText(request.getPromptId())) {
            throw new IllegalArgumentException("提示词ID不能为空");
        }
        AiClientSystemPromptDTO prompt = toDomain(request);
        prompt.setUpdateTime(LocalDateTime.now());
        return aiClientSystemPromptRepository.updateByPromptId(prompt);
    }

    public boolean deleteAiClientSystemPromptById(Long id) {
        log.info("根据ID删除系统提示词配置：{}", id);
        return aiClientSystemPromptRepository.deleteById(id);
    }

    public boolean deleteAiClientSystemPromptByPromptId(String promptId) {
        log.info("根据提示词ID删除系统提示词配置：{}", promptId);
        return aiClientSystemPromptRepository.deleteByPromptId(promptId);
    }

    public AiClientSystemPromptResponseDTO queryAiClientSystemPromptById(Long id) {
        log.info("根据ID查询系统提示词配置：{}", id);
        AiClientSystemPromptDTO prompt = aiClientSystemPromptRepository.queryById(id);
        return toResponse(prompt);
    }

    public AiClientSystemPromptResponseDTO queryAiClientSystemPromptByPromptId(String promptId) {
        log.info("根据提示词ID查询系统提示词配置：{}", promptId);
        AiClientSystemPromptDTO prompt = aiClientSystemPromptRepository.queryByPromptId(promptId);
        return toResponse(prompt);
    }

    public List<AiClientSystemPromptResponseDTO> queryAllAiClientSystemPrompts() {
        log.info("查询所有系统提示词配置");
        return aiClientSystemPromptRepository.queryAll().stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientSystemPromptResponseDTO> queryEnabledAiClientSystemPrompts() {
        log.info("查询启用的系统提示词配置");
        return aiClientSystemPromptRepository.queryEnabledPrompts().stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientSystemPromptResponseDTO> queryAiClientSystemPromptsByPromptName(String promptName) {
        log.info("根据提示词名称查询系统提示词配置：{}", promptName);
        return aiClientSystemPromptRepository.queryByPromptName(promptName).stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientSystemPromptResponseDTO> queryAiClientSystemPromptList(AiClientSystemPromptQueryRequestDTO request) {
        log.info("根据条件查询系统提示词配置列表：{}", request);
        List<AiClientSystemPromptDTO> prompts;
        if (StringUtils.hasText(request.getPromptId())) {
            AiClientSystemPromptDTO prompt = aiClientSystemPromptRepository.queryByPromptId(request.getPromptId());
            prompts = prompt != null ? List.of(prompt) : List.of();
        } else if (StringUtils.hasText(request.getPromptName())) {
            prompts = aiClientSystemPromptRepository.queryByPromptName(request.getPromptName());
        } else if (request.getStatus() != null && request.getStatus() == 1) {
            prompts = aiClientSystemPromptRepository.queryEnabledPrompts();
        } else {
            prompts = aiClientSystemPromptRepository.queryAll();
        }
        if (request.getStatus() != null && !StringUtils.hasText(request.getPromptId()) && !StringUtils.hasText(request.getPromptName())) {
            prompts = prompts.stream()
                    .filter(prompt -> request.getStatus().equals(prompt.getStatus()))
                    .collect(Collectors.toList());
        }
        return prompts.stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private AiClientSystemPromptDTO toDomain(AiClientSystemPromptRequestDTO requestDTO) {
        AiClientSystemPromptDTO dto = new AiClientSystemPromptDTO();
        BeanUtils.copyProperties(requestDTO, dto);
        return dto;
    }

    private AiClientSystemPromptResponseDTO toResponse(AiClientSystemPromptDTO dto) {
        if (dto == null) {
            return null;
        }
        AiClientSystemPromptResponseDTO responseDTO = new AiClientSystemPromptResponseDTO();
        BeanUtils.copyProperties(dto, responseDTO);
        return responseDTO;
    }
}
