package com.tricoq.infrastructure.adapter.repository;

import com.tricoq.domain.agent.adapter.repository.IAiClientSystemPromptRepository;
import com.tricoq.domain.agent.model.dto.AiClientSystemPromptDTO;
import com.tricoq.infrastructure.dao.IAiClientSystemPromptDao;
import com.tricoq.infrastructure.dao.po.AiClientSystemPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 系统提示词仓储实现
 */
@Repository
@RequiredArgsConstructor
public class AiClientSystemPromptRepositoryImpl implements IAiClientSystemPromptRepository {

    private final IAiClientSystemPromptDao aiClientSystemPromptDao;

    @Override
    public boolean insert(AiClientSystemPromptDTO prompt) {
        if (prompt == null) {
            return false;
        }
        AiClientSystemPrompt po = toPo(prompt);
        LocalDateTime now = LocalDateTime.now();
        po.setCreateTime(now);
        po.setUpdateTime(now);
        return aiClientSystemPromptDao.insert(po) > 0;
    }

    @Override
    public boolean updateById(AiClientSystemPromptDTO prompt) {
        if (prompt == null || prompt.getId() == null) {
            return false;
        }
        AiClientSystemPrompt po = toPo(prompt);
        po.setId(prompt.getId());
        po.setUpdateTime(LocalDateTime.now());
        return aiClientSystemPromptDao.updateById(po) > 0;
    }

    @Override
    public boolean updateByPromptId(AiClientSystemPromptDTO prompt) {
        if (prompt == null || !StringUtils.hasText(prompt.getPromptId())) {
            return false;
        }
        AiClientSystemPrompt po = toPo(prompt);
        po.setUpdateTime(LocalDateTime.now());
        return aiClientSystemPromptDao.updateByPromptId(po) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return id != null && aiClientSystemPromptDao.deleteById(id) > 0;
    }

    @Override
    public boolean deleteByPromptId(String promptId) {
        return StringUtils.hasText(promptId) && aiClientSystemPromptDao.deleteByPromptId(promptId) > 0;
    }

    @Override
    public AiClientSystemPromptDTO queryById(Long id) {
        if (id == null) {
            return null;
        }
        return toDto(aiClientSystemPromptDao.queryById(id));
    }

    @Override
    public AiClientSystemPromptDTO queryByPromptId(String promptId) {
        if (!StringUtils.hasText(promptId)) {
            return null;
        }
        return toDto(aiClientSystemPromptDao.queryByPromptId(promptId));
    }

    @Override
    public List<AiClientSystemPromptDTO> queryAll() {
        return aiClientSystemPromptDao.queryAll().stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AiClientSystemPromptDTO> queryEnabledPrompts() {
        return aiClientSystemPromptDao.queryEnabledPrompts().stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AiClientSystemPromptDTO> queryByPromptName(String promptName) {
        if (!StringUtils.hasText(promptName)) {
            return List.of();
        }
        return aiClientSystemPromptDao.queryByPromptName(promptName).stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    private AiClientSystemPromptDTO toDto(AiClientSystemPrompt po) {
        if (po == null) {
            return null;
        }
        return AiClientSystemPromptDTO.builder()
                .id(po.getId())
                .promptId(po.getPromptId())
                .promptName(po.getPromptName())
                .promptContent(po.getPromptContent())
                .description(po.getDescription())
                .status(po.getStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiClientSystemPrompt toPo(AiClientSystemPromptDTO dto) {
        if (dto == null) {
            return null;
        }
        return AiClientSystemPrompt.builder()
                .id(dto.getId())
                .promptId(dto.getPromptId())
                .promptName(dto.getPromptName())
                .promptContent(dto.getPromptContent())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .createTime(dto.getCreateTime())
                .updateTime(dto.getUpdateTime())
                .build();
    }
}
