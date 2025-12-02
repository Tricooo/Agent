package com.tricoq.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

/**
 *
 *
 * @author trico qiang
 * @date 11/27/25
 */
@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class AiClientModelVO {

    /**
     * 全局唯一模型ID
     */
    private final String modelId;

    /**
     * 工具 mcp ids
     */
    private final List<String> toolMcpIds;
}
