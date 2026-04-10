package com.tricoq.domain.agent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

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

    /**
     * 将工具目录列表格式化为 LLM 可读的 prompt 文本。
     */
    public static String toPromptText(List<McpToolCatalogDTO> catalogs) {
        if (catalogs == null || catalogs.isEmpty()) {
            return "当前无可用 MCP 工具。";
        }
        StringBuilder sb = new StringBuilder("# 当前可用 MCP 工具\n\n");
        int mcpIndex = 1;
        for (McpToolCatalogDTO catalog : catalogs) {
            sb.append("## ").append(mcpIndex++).append(". ")
                    .append(catalog.getMcpName())
                    .append(" (").append(catalog.getMcpId()).append(")\n\n");
            if (catalog.getTools() == null || catalog.getTools().isEmpty()) {
                sb.append("暂无工具\n\n");
                continue;
            }
            for (ToolSpecDTO tool : catalog.getTools()) {
                sb.append("### ").append(tool.getToolName()).append("\n");
                sb.append("- 功能: ").append(tool.getDescription()).append("\n");
                appendArgs(sb, "必填参数", tool.getRequiredArgs(), tool.getInputProperties());
                appendArgs(sb, "可选参数", tool.getOptionalArgs(), tool.getInputProperties());
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private static void appendArgs(StringBuilder sb, String label, List<String> argNames,
                                   Map<String, Object> properties) {
        if (argNames == null || argNames.isEmpty()) {
            return;
        }
        sb.append("- ").append(label).append(":\n");
        for (String arg : argNames) {
            Object schema = (properties != null) ? properties.get(arg) : null;
            String desc = extractDescription(schema);
            sb.append("  - `").append(arg).append("`");
            if (desc != null) {
                sb.append(": ").append(desc);
            }
            sb.append("\n");
        }
    }

    @SuppressWarnings("unchecked")
    private static String extractDescription(Object schema) {
        if (schema instanceof Map<?, ?> map) {
            Object desc = map.get("description");
            if (desc != null) {
                return desc.toString();
            }
            // 退而求其次，展示 type
            Object type = map.get("type");
            return type != null ? "(" + type + ")" : null;
        }
        return null;
    }
}

