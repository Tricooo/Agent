package com.tricoq.application.service;

import com.tricoq.api.dto.AiClientToolMcpQueryRequestDTO;
import com.tricoq.api.dto.AiClientToolMcpRequestDTO;
import com.tricoq.api.dto.AiClientToolMcpResponseDTO;
import com.tricoq.domain.agent.adapter.repository.IAiClientToolMcpRepository;
import com.tricoq.domain.agent.model.dto.AiClientToolMcpDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * MCP 客户端配置应用服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiClientToolMcpAdminService {

    private final IAiClientToolMcpRepository aiClientToolMcpRepository;

    public boolean createAiClientToolMcp(AiClientToolMcpRequestDTO request) {
        log.info("创建MCP客户端配置请求：{}", request);
        AiClientToolMcpDTO mcp = toDomain(request);
        mcp.setCreateTime(LocalDateTime.now());
        mcp.setUpdateTime(LocalDateTime.now());
        return aiClientToolMcpRepository.insert(mcp);
    }

    public boolean updateAiClientToolMcpById(AiClientToolMcpRequestDTO request) {
        log.info("根据ID更新MCP客户端配置请求：{}", request);
        if (request.getId() == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        AiClientToolMcpDTO mcp = toDomain(request);
        mcp.setUpdateTime(LocalDateTime.now());
        return aiClientToolMcpRepository.updateById(mcp);
    }

    public boolean updateAiClientToolMcpByMcpId(AiClientToolMcpRequestDTO request) {
        log.info("根据MCP ID更新MCP客户端配置请求：{}", request);
        if (!StringUtils.hasText(request.getMcpId())) {
            throw new IllegalArgumentException("MCP ID不能为空");
        }
        AiClientToolMcpDTO mcp = toDomain(request);
        mcp.setUpdateTime(LocalDateTime.now());
        return aiClientToolMcpRepository.updateByMcpId(mcp);
    }

    public boolean deleteAiClientToolMcpById(Long id) {
        log.info("根据ID删除MCP客户端配置：{}", id);
        return aiClientToolMcpRepository.deleteById(id);
    }

    public boolean deleteAiClientToolMcpByMcpId(String mcpId) {
        log.info("根据MCP ID删除MCP客户端配置：{}", mcpId);
        return aiClientToolMcpRepository.deleteByMcpId(mcpId);
    }

    public AiClientToolMcpResponseDTO queryAiClientToolMcpById(Long id) {
        log.info("根据ID查询MCP客户端配置：{}", id);
        return toResponse(aiClientToolMcpRepository.queryById(id));
    }

    public AiClientToolMcpResponseDTO queryAiClientToolMcpByMcpId(String mcpId) {
        log.info("根据MCP ID查询MCP客户端配置：{}", mcpId);
        return toResponse(aiClientToolMcpRepository.queryByMcpId(mcpId));
    }

    public List<AiClientToolMcpResponseDTO> queryAllAiClientToolMcps() {
        log.info("查询所有MCP客户端配置");
        return aiClientToolMcpRepository.queryAll().stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientToolMcpResponseDTO> queryAiClientToolMcpsByStatus(Integer status) {
        log.info("根据状态查询MCP客户端配置：{}", status);
        return aiClientToolMcpRepository.queryByStatus(status).stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientToolMcpResponseDTO> queryAiClientToolMcpsByTransportType(String transportType) {
        log.info("根据传输类型查询MCP客户端配置：{}", transportType);
        return aiClientToolMcpRepository.queryByTransportType(transportType).stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientToolMcpResponseDTO> queryEnabledAiClientToolMcps() {
        log.info("查询启用的MCP客户端配置");
        return aiClientToolMcpRepository.queryEnabledMcps().stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientToolMcpResponseDTO> queryAiClientToolMcpList(AiClientToolMcpQueryRequestDTO request) {
        log.info("根据查询条件查询MCP客户端配置列表：{}", request);
        List<AiClientToolMcpDTO> mcps;
        if (StringUtils.hasText(request.getMcpId())) {
            AiClientToolMcpDTO mcp = aiClientToolMcpRepository.queryByMcpId(request.getMcpId());
            mcps = mcp != null ? List.of(mcp) : List.of();
        } else if (request.getStatus() != null) {
            mcps = aiClientToolMcpRepository.queryByStatus(request.getStatus());
        } else if (StringUtils.hasText(request.getTransportType())) {
            mcps = aiClientToolMcpRepository.queryByTransportType(request.getTransportType());
        } else {
            mcps = aiClientToolMcpRepository.queryAll();
        }
        if (StringUtils.hasText(request.getMcpName())) {
            mcps = mcps.stream()
                    .filter(mcp -> mcp.getMcpName() != null && mcp.getMcpName().contains(request.getMcpName()))
                    .collect(Collectors.toList());
        }
        return mcps.stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private AiClientToolMcpDTO toDomain(AiClientToolMcpRequestDTO requestDTO) {
        AiClientToolMcpDTO dto = new AiClientToolMcpDTO();
        BeanUtils.copyProperties(requestDTO, dto);
        return dto;
    }

    private AiClientToolMcpResponseDTO toResponse(AiClientToolMcpDTO dto) {
        if (dto == null) {
            return null;
        }
        AiClientToolMcpResponseDTO responseDTO = new AiClientToolMcpResponseDTO();
        BeanUtils.copyProperties(dto, responseDTO);
        return responseDTO;
    }
}
