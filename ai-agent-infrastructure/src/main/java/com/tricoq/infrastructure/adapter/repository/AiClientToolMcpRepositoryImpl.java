package com.tricoq.infrastructure.adapter.repository;

import com.tricoq.domain.agent.adapter.repository.IAiClientToolMcpRepository;
import com.tricoq.domain.agent.model.dto.AiClientToolMcpDTO;
import com.tricoq.infrastructure.dao.po.AiClientToolMcp;
import com.tricoq.infrastructure.support.AiClientToolMcpDaoSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * MCP 客户端配置仓储实现
 * @author trico qiang
 */
@Repository
@RequiredArgsConstructor
public class AiClientToolMcpRepositoryImpl implements IAiClientToolMcpRepository {

    private final AiClientToolMcpDaoSupport toolMcpDaoSupport;

    @Override
    public boolean insert(AiClientToolMcpDTO mcp) {
        if (mcp == null) {
            return false;
        }
        AiClientToolMcp po = toPo(mcp);
        LocalDateTime now = LocalDateTime.now();
        po.setCreateTime(now);
        po.setUpdateTime(now);
        return toolMcpDaoSupport.save(po);
    }

    @Override
    public boolean updateById(AiClientToolMcpDTO mcp) {
        if (mcp == null || mcp.getId() == null) {
            return false;
        }
        AiClientToolMcp po = toPo(mcp);
        po.setId(mcp.getId());
        po.setUpdateTime(LocalDateTime.now());
        return toolMcpDaoSupport.updateById(po);
    }

    @Override
    public boolean updateByMcpId(AiClientToolMcpDTO mcp) {
        if (mcp == null || !StringUtils.hasText(mcp.getMcpId())) {
            return false;
        }
        AiClientToolMcp po = toPo(mcp);
        po.setUpdateTime(LocalDateTime.now());
        return toolMcpDaoSupport.getBaseMapper().updateByMcpId(po) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return id != null && toolMcpDaoSupport.removeById(id);
    }

    @Override
    public boolean deleteByMcpId(String mcpId) {
        return StringUtils.hasText(mcpId) && toolMcpDaoSupport.getBaseMapper().deleteByMcpId(mcpId) > 0;
    }

    @Override
    public AiClientToolMcpDTO queryById(Long id) {
        if (id == null) {
            return null;
        }
        return toDto(toolMcpDaoSupport.getById(id));
    }

    @Override
    public AiClientToolMcpDTO queryByMcpId(String mcpId) {
        if (!StringUtils.hasText(mcpId)) {
            return null;
        }
        return toDto(toolMcpDaoSupport.getBaseMapper().queryByMcpId(mcpId));
    }

    @Override
    public List<AiClientToolMcpDTO> queryAll() {
        return toolMcpDaoSupport.getBaseMapper().queryAll().stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AiClientToolMcpDTO> queryByStatus(Integer status) {
        if (status == null) {
            return List.of();
        }
        return toolMcpDaoSupport.getBaseMapper().queryByStatus(status).stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AiClientToolMcpDTO> queryByTransportType(String transportType) {
        if (!StringUtils.hasText(transportType)) {
            return List.of();
        }
        return toolMcpDaoSupport.getBaseMapper().queryByTransportType(transportType).stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AiClientToolMcpDTO> queryEnabledMcps() {
        return toolMcpDaoSupport.getBaseMapper().queryEnabledMcps().stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    private AiClientToolMcpDTO toDto(AiClientToolMcp po) {
        if (po == null) {
            return null;
        }
        return AiClientToolMcpDTO.builder()
                .id(po.getId())
                .mcpId(po.getMcpId())
                .mcpName(po.getMcpName())
                .transportType(po.getTransportType())
                .transportConfig(po.getTransportConfig())
                .requestTimeout(po.getRequestTimeout())
                .status(po.getStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiClientToolMcp toPo(AiClientToolMcpDTO dto) {
        if (dto == null) {
            return null;
        }
        return AiClientToolMcp.builder()
                .id(dto.getId())
                .mcpId(dto.getMcpId())
                .mcpName(dto.getMcpName())
                .transportType(dto.getTransportType())
                .transportConfig(dto.getTransportConfig())
                .requestTimeout(dto.getRequestTimeout())
                .status(dto.getStatus())
                .createTime(dto.getCreateTime())
                .updateTime(dto.getUpdateTime())
                .build();
    }
}
