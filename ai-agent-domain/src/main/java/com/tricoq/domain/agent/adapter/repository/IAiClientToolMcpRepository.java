package com.tricoq.domain.agent.adapter.repository;

import com.tricoq.domain.agent.model.dto.AiClientToolMcpDTO;

import java.util.List;

/**
 * MCP客户端配置仓储接口
 */
public interface IAiClientToolMcpRepository {

    boolean insert(AiClientToolMcpDTO mcp);

    boolean updateById(AiClientToolMcpDTO mcp);

    boolean updateByMcpId(AiClientToolMcpDTO mcp);

    boolean deleteById(Long id);

    boolean deleteByMcpId(String mcpId);

    AiClientToolMcpDTO queryById(Long id);

    AiClientToolMcpDTO queryByMcpId(String mcpId);

    List<AiClientToolMcpDTO> queryAll();

    List<AiClientToolMcpDTO> queryByStatus(Integer status);

    List<AiClientToolMcpDTO> queryByTransportType(String transportType);

    List<AiClientToolMcpDTO> queryEnabledMcps();
}
