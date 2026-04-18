package com.tricoq.domain.agent.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * @description: 客户端运行时能力画像
 * @author：trico qiang
 * @date: 4/18/26
 */
@Data
@Builder
public class AiClientRuntimeProfile {

    private String clientId;

    // 是否挂了 ChatMemory advisor
    private boolean chatMemoryEnabled;

    // 如果有 ChatMemory，窗口上限是多少
    private Integer chatMemoryMaxMessages;

    // 是否挂了 RAG advisor
    private boolean ragEnabled;

    // 是否挂了 MCP tools 与retry 策略的副作用有关
    private boolean mcpEnabled;

    // 是否配置了系统提示词
    private boolean systemPromptEnabled;

    // 已挂载的 advisor 类型集合（可选）
    private Set<String> advisorTypes;
}
