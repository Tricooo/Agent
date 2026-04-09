package com.tricoq.domain.agent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description: MCP可用工具源(工具能力描述)
 * @author：trico qiang
 * @date: 4/9/26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class McpToolCatalogDTO {

    /**
     * 所归属MCP id
     */
    private String mcpId;

    private String mcpName;

    /**
     * 当前 MCP 服务暴露出的具体工具列表
     */
    private List<ToolSpecDTO> tools;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;
}

