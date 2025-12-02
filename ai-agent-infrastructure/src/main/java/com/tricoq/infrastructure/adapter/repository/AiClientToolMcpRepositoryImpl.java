package com.tricoq.infrastructure.adapter.repository;

import com.tricoq.domain.agent.adapter.repository.IAiClientToolMcpRepository;
import com.tricoq.domain.agent.model.dto.AiClientToolMcpDTO;
import com.tricoq.infrastructure.dao.IAiClientToolMcpDao;
import com.tricoq.infrastructure.dao.po.AiClientToolMcp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * MCP 客户端配置仓储实现
 */
@Repository
@RequiredArgsConstructor
public class AiClientToolMcpRepositoryImpl implements IAiClientToolMcpRepository {

    private final IAiClientToolMcpDao aiClientToolMcpDao;

    @Override
    public boolean insert(AiClientToolMcpDTO mcp) {
        if (mcp == null) {
            return false;
        }
        AiClientToolMcp po = toPo(mcp);
        LocalDateTime now = LocalDateTime.now();
        po.setCreateTime(now);
        po.setUpdateTime(now);
        return aiClientToolMcpDao.insert(po) > 0;
    }

    @Override
    public boolean updateById(AiClientToolMcpDTO mcp) {
        if (mcp == null || mcp.getId() == null) {
            return false;
        }
        AiClientToolMcp po = toPo(mcp);
        po.setId(mcp.getId());
        po.setUpdateTime(LocalDateTime.now());
        return aiClientToolMcpDao.updateById(po) > 0;
    }

    @Override
    public boolean updateByMcpId(AiClientToolMcpDTO mcp) {
        if (mcp == null || !StringUtils.hasText(mcp.getMcpId())) {
            return false;
        }
        AiClientToolMcp po = toPo(mcp);
        po.setUpdateTime(LocalDateTime.now());
        return aiClientToolMcpDao.updateByMcpId(po) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return id != null && aiClientToolMcpDao.deleteById(id) > 0;
    }

    @Override
    public boolean deleteByMcpId(String mcpId) {
        return StringUtils.hasText(mcpId) && aiClientToolMcpDao.deleteByMcpId(mcpId) > 0;
    }

    @Override
    public AiClientToolMcpDTO queryById(Long id) {
        if (id == null) {
            return null;
        }
        return toDto(aiClientToolMcpDao.queryById(id));
    }

    @Override
    public AiClientToolMcpDTO queryByMcpId(String mcpId) {
        if (!StringUtils.hasText(mcpId)) {
            return null;
        }
        return toDto(aiClientToolMcpDao.queryByMcpId(mcpId));
    }

    @Override
    public List<AiClientToolMcpDTO> queryAll() {
        return aiClientToolMcpDao.queryAll().stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AiClientToolMcpDTO> queryByStatus(Integer status) {
        if (status == null) {
            return List.of();
        }
        return aiClientToolMcpDao.queryByStatus(status).stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AiClientToolMcpDTO> queryByTransportType(String transportType) {
        if (!StringUtils.hasText(transportType)) {
            return List.of();
        }
        return aiClientToolMcpDao.queryByTransportType(transportType).stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AiClientToolMcpDTO> queryEnabledMcps() {
        return aiClientToolMcpDao.queryEnabledMcps().stream()
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
