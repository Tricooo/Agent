package com.tricoq.infrastructure.adapter.repository;

import com.tricoq.domain.agent.adapter.repository.IAiClientAdvisorRepository;
import com.tricoq.domain.agent.model.dto.AiClientAdvisorDTO;
import com.tricoq.infrastructure.dao.po.AiClientAdvisor;
import com.tricoq.infrastructure.support.AiClientAdvisorDaoSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 顾问配置仓储实现
 * @author trico qiang
 */
@Repository
@RequiredArgsConstructor
public class AiClientAdvisorRepositoryImpl implements IAiClientAdvisorRepository {

    private final AiClientAdvisorDaoSupport advisorDaoSupport;

    @Override
    public boolean insert(AiClientAdvisorDTO advisor) {
        if (advisor == null) {
            return false;
        }
        AiClientAdvisor po = toPo(advisor);
        LocalDateTime now = LocalDateTime.now();
        po.setCreateTime(now);
        po.setUpdateTime(now);
        return advisorDaoSupport.save(po);
    }

    @Override
    public boolean updateById(AiClientAdvisorDTO advisor) {
        if (advisor == null || advisor.getId() == null) {
            return false;
        }
        AiClientAdvisor po = toPo(advisor);
        po.setId(advisor.getId());
        po.setUpdateTime(LocalDateTime.now());
        return advisorDaoSupport.updateById(po);
    }

    @Override
    public boolean updateByAdvisorId(AiClientAdvisorDTO advisor) {
        if (advisor == null || !StringUtils.hasText(advisor.getAdvisorId())) {
            return false;
        }
        AiClientAdvisor po = toPo(advisor);
        po.setUpdateTime(LocalDateTime.now());
        return advisorDaoSupport.getBaseMapper().updateByAdvisorId(po) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return id != null && advisorDaoSupport.removeById(id);
    }

    @Override
    public boolean deleteByAdvisorId(String advisorId) {
        return StringUtils.hasText(advisorId) && advisorDaoSupport.getBaseMapper().deleteByAdvisorId(advisorId) > 0;
    }

    @Override
    public AiClientAdvisorDTO queryById(Long id) {
        if (id == null) {
            return null;
        }
        return toDto(advisorDaoSupport.getById(id));
    }

    @Override
    public AiClientAdvisorDTO queryByAdvisorId(String advisorId) {
        if (!StringUtils.hasText(advisorId)) {
            return null;
        }
        return toDto(advisorDaoSupport.getBaseMapper().queryByAdvisorId(advisorId));
    }

    @Override
    public List<AiClientAdvisorDTO> queryByStatus(Integer status) {
        if (status == null) {
            return List.of();
        }
        return advisorDaoSupport.getBaseMapper().queryByStatus(status).stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AiClientAdvisorDTO> queryByAdvisorType(String advisorType) {
        if (!StringUtils.hasText(advisorType)) {
            return List.of();
        }
        return advisorDaoSupport.getBaseMapper().queryByAdvisorType(advisorType).stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AiClientAdvisorDTO> queryAll() {
        return advisorDaoSupport.getBaseMapper().queryAll().stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    private AiClientAdvisorDTO toDto(AiClientAdvisor po) {
        if (po == null) {
            return null;
        }
        return AiClientAdvisorDTO.builder()
                .id(po.getId())
                .advisorId(po.getAdvisorId())
                .advisorName(po.getAdvisorName())
                .advisorType(po.getAdvisorType())
                .orderNum(po.getOrderNum())
                .extParam(po.getExtParam())
                .status(po.getStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiClientAdvisor toPo(AiClientAdvisorDTO dto) {
        if (dto == null) {
            return null;
        }
        return AiClientAdvisor.builder()
                .id(dto.getId())
                .advisorId(dto.getAdvisorId())
                .advisorName(dto.getAdvisorName())
                .advisorType(dto.getAdvisorType())
                .orderNum(dto.getOrderNum())
                .extParam(dto.getExtParam())
                .status(dto.getStatus())
                .createTime(dto.getCreateTime())
                .updateTime(dto.getUpdateTime())
                .build();
    }
}
