package com.tricoq.domain.agent.service;

import com.tricoq.domain.agent.model.dto.McpToolCatalogDTO;

import java.util.List;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/9/26
 */
public interface IToolCatalogService {

    /**
     * 通过客户端id解析可用工具列表
     * @param clientId 客户端id
     * @return 工具列表
     */
    List<McpToolCatalogDTO> resolveToolsByClient(String clientId);
}
