package com.tricoq.domain.agent.adapter.repository;

import com.tricoq.domain.agent.model.dto.AiClientSystemPromptDTO;

import java.util.List;

/**
 * 系统提示词仓储接口
 */
public interface IAiClientSystemPromptRepository {

    boolean insert(AiClientSystemPromptDTO prompt);

    boolean updateById(AiClientSystemPromptDTO prompt);

    boolean updateByPromptId(AiClientSystemPromptDTO prompt);

    boolean deleteById(Long id);

    boolean deleteByPromptId(String promptId);

    AiClientSystemPromptDTO queryById(Long id);

    AiClientSystemPromptDTO queryByPromptId(String promptId);

    List<AiClientSystemPromptDTO> queryAll();

    List<AiClientSystemPromptDTO> queryEnabledPrompts();

    List<AiClientSystemPromptDTO> queryByPromptName(String promptName);
}
