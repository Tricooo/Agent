package com.tricoq.domain.agent.adapter.port;

import com.tricoq.domain.agent.model.dto.AiClientToolMcpDTO;
import io.modelcontextprotocol.client.McpSyncClient;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/9/26
 */
public interface IMcpClientProvider {

    McpSyncClient getOrCreate(AiClientToolMcpDTO toolMcp);
}
