package com.tricoq.domain.agent.service.execute;

import com.tricoq.domain.agent.adapter.port.IMcpClientProvider;
import com.tricoq.domain.agent.adapter.repository.IClientRepository;
import com.tricoq.domain.agent.model.dto.AiClientToolMcpDTO;
import com.tricoq.domain.agent.model.dto.McpToolCatalogDTO;
import com.tricoq.domain.agent.model.dto.ToolSpecDTO;
import com.tricoq.domain.agent.service.IToolCatalogService;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/9/26
 */
@Service
@RequiredArgsConstructor
public class ToolCatalogService implements IToolCatalogService {

    private final IClientRepository clientRepository;

    private final IMcpClientProvider mcpClientProvider;

    /**
     * 通过客户端id解析可用工具列表
     *
     * @param clientId 客户端id
     * @return 工具列表
     */
    @Override
    public List<McpToolCatalogDTO> resolveToolsByClient(String clientId) {
        List<AiClientToolMcpDTO> mcps = clientRepository.queryAiClientToolMcpsByClientIds(List.of(clientId));
        return mcps.stream().map(mcp -> {
            McpSyncClient mcpClient = mcpClientProvider.getOrCreate(mcp);
            return McpToolCatalogDTO.builder().mcpId(mcp.getMcpId())
                    .mcpName(mcp.getMcpName())
                    .tools(listToolSpecs(mcpClient))
                    .status(mcp.getStatus())
                    .build();
        }).toList();
    }

    private List<ToolSpecDTO> listToolSpecs(McpSyncClient mcpClient) {
        McpSchema.ListToolsResult firstPage = mcpClient.listTools();
        List<McpSchema.Tool> allTools = new ArrayList<>(safeTools(firstPage));
        String nextCursor = firstPage.nextCursor();
        while (StringUtils.isNotBlank(nextCursor)) {
            McpSchema.ListToolsResult nextPage = mcpClient.listTools(nextCursor);
            allTools.addAll(safeTools(nextPage));
            nextCursor = nextPage.nextCursor();
        }
        return allTools.stream().map(this::toToolSpec).toList();
    }

    private List<McpSchema.Tool> safeTools(McpSchema.ListToolsResult result) {
        return result == null || result.tools() == null ? List.of() : result.tools();
    }

    private ToolSpecDTO toToolSpec(McpSchema.Tool tool) {
        McpSchema.JsonSchema inputSchema = tool.inputSchema();
        List<String> requiredArgs = inputSchema == null || inputSchema.required() == null
                ? List.of()
                : List.copyOf(inputSchema.required());
        LinkedHashSet<String> requiredArgSet = new LinkedHashSet<>(requiredArgs);
        Map<String, Object> inputProperties = inputSchema == null || inputSchema.properties() == null
                ? Map.of()
                : new LinkedHashMap<>(inputSchema.properties());
        List<String> optionalArgs = inputProperties.isEmpty()
                ? List.of()
                : inputProperties.keySet().stream()
                .filter(arg -> !requiredArgSet.contains(arg))
                .toList();

        return ToolSpecDTO.builder()
                .toolName(tool.name())
                .description(tool.description())
                .inputSchemaType(inputSchema == null ? null : inputSchema.type())
                .inputProperties(inputProperties)
                .requiredArgs(requiredArgs)
                .optionalArgs(optionalArgs)
                .additionalProperties(inputSchema == null ? null : inputSchema.additionalProperties())
                .build();
    }
}
