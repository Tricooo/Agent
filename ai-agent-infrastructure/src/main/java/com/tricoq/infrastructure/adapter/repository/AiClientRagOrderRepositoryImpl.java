package com.tricoq.infrastructure.adapter.repository;

import com.tricoq.domain.agent.adapter.repository.IAiClientRagOrderRepository;
import com.tricoq.domain.agent.model.dto.AiRagOrderDTO;
import com.tricoq.infrastructure.dao.IAiClientRagOrderDao;
import com.tricoq.infrastructure.dao.po.AiClientRagOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 知识库配置仓储实现
 */
@Repository
@RequiredArgsConstructor
public class AiClientRagOrderRepositoryImpl implements IAiClientRagOrderRepository {

    private final IAiClientRagOrderDao aiClientRagOrderDao;

    @Override
    public boolean insert(AiRagOrderDTO ragOrder) {
        if (ragOrder == null) {
            return false;
        }
        AiClientRagOrder po = toPo(ragOrder);
        LocalDateTime now = LocalDateTime.now();
        po.setCreateTime(now);
        po.setUpdateTime(now);
        return aiClientRagOrderDao.insert(po) > 0;
    }

    @Override
    public boolean updateById(AiRagOrderDTO ragOrder) {
        if (ragOrder == null || ragOrder.getId() == null) {
            return false;
        }
        AiClientRagOrder po = toPo(ragOrder);
        po.setId(ragOrder.getId());
        po.setUpdateTime(LocalDateTime.now());
        return aiClientRagOrderDao.updateById(po) > 0;
    }

    @Override
    public boolean updateByRagId(AiRagOrderDTO ragOrder) {
        if (ragOrder == null || !StringUtils.hasText(ragOrder.getRagId())) {
            return false;
        }
        AiClientRagOrder po = toPo(ragOrder);
        po.setUpdateTime(LocalDateTime.now());
        return aiClientRagOrderDao.updateByRagId(po) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return id != null && aiClientRagOrderDao.deleteById(id) > 0;
    }

    @Override
    public boolean deleteByRagId(String ragId) {
        return StringUtils.hasText(ragId) && aiClientRagOrderDao.deleteByRagId(ragId) > 0;
    }

    @Override
    public AiRagOrderDTO queryById(Long id) {
        if (id == null) {
            return null;
        }
        return toDto(aiClientRagOrderDao.queryById(id));
    }

    @Override
    public AiRagOrderDTO queryByRagId(String ragId) {
        if (!StringUtils.hasText(ragId)) {
            return null;
        }
        return toDto(aiClientRagOrderDao.queryByRagId(ragId));
    }

    @Override
    public List<AiRagOrderDTO> queryEnabledRagOrders() {
        return aiClientRagOrderDao.queryEnabledRagOrders().stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AiRagOrderDTO> queryByKnowledgeTag(String knowledgeTag) {
        if (!StringUtils.hasText(knowledgeTag)) {
            return List.of();
        }
        return aiClientRagOrderDao.queryByKnowledgeTag(knowledgeTag).stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AiRagOrderDTO> queryAll() {
        return aiClientRagOrderDao.queryAll().stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    private AiRagOrderDTO toDto(AiClientRagOrder po) {
        if (po == null) {
            return null;
        }
        return AiRagOrderDTO.builder()
                .id(po.getId())
                .ragId(po.getRagId())
                .ragName(po.getRagName())
                .knowledgeTag(po.getKnowledgeTag())
                .status(po.getStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiClientRagOrder toPo(AiRagOrderDTO dto) {
        if (dto == null) {
            return null;
        }
        return AiClientRagOrder.builder()
                .id(dto.getId())
                .ragId(dto.getRagId())
                .ragName(dto.getRagName())
                .knowledgeTag(dto.getKnowledgeTag())
                .status(dto.getStatus())
                .createTime(dto.getCreateTime())
                .updateTime(dto.getUpdateTime())
                .build();
    }
}
