package com.tricoq.application.service;

import com.tricoq.api.dto.AiClientAdvisorQueryRequestDTO;
import com.tricoq.api.dto.AiClientAdvisorRequestDTO;
import com.tricoq.api.dto.AiClientAdvisorResponseDTO;
import com.tricoq.domain.agent.adapter.repository.IAiClientAdvisorRepository;
import com.tricoq.domain.agent.model.dto.AiClientAdvisorDTO;
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
 * 顾问配置应用服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiClientAdvisorAdminService {

    private final IAiClientAdvisorRepository advisorRepository;

    public boolean createAiClientAdvisor(AiClientAdvisorRequestDTO request) {
        log.info("创建顾问配置请求：{}", request);
        AiClientAdvisorDTO advisor = toDomain(request);
        advisor.setCreateTime(LocalDateTime.now());
        advisor.setUpdateTime(LocalDateTime.now());
        return advisorRepository.insert(advisor);
    }

    public boolean updateAiClientAdvisorById(AiClientAdvisorRequestDTO request) {
        log.info("根据ID更新顾问配置请求：{}", request);
        if (request.getId() == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        AiClientAdvisorDTO advisor = toDomain(request);
        advisor.setUpdateTime(LocalDateTime.now());
        return advisorRepository.updateById(advisor);
    }

    public boolean updateAiClientAdvisorByAdvisorId(AiClientAdvisorRequestDTO request) {
        log.info("根据顾问ID更新顾问配置请求：{}", request);
        if (!StringUtils.hasText(request.getAdvisorId())) {
            throw new IllegalArgumentException("顾问ID不能为空");
        }
        AiClientAdvisorDTO advisor = toDomain(request);
        advisor.setUpdateTime(LocalDateTime.now());
        return advisorRepository.updateByAdvisorId(advisor);
    }

    public boolean deleteAiClientAdvisorById(Long id) {
        log.info("根据ID删除顾问配置请求：{}", id);
        return advisorRepository.deleteById(id);
    }

    public boolean deleteAiClientAdvisorByAdvisorId(String advisorId) {
        log.info("根据顾问ID删除顾问配置请求：{}", advisorId);
        return advisorRepository.deleteByAdvisorId(advisorId);
    }

    public AiClientAdvisorResponseDTO queryAiClientAdvisorById(Long id) {
        log.info("根据ID查询顾问配置请求：{}", id);
        AiClientAdvisorDTO advisor = advisorRepository.queryById(id);
        return toResponse(advisor);
    }

    public AiClientAdvisorResponseDTO queryAiClientAdvisorByAdvisorId(String advisorId) {
        log.info("根据顾问ID查询顾问配置请求：{}", advisorId);
        AiClientAdvisorDTO advisor = advisorRepository.queryByAdvisorId(advisorId);
        return toResponse(advisor);
    }

    public List<AiClientAdvisorResponseDTO> queryEnabledAiClientAdvisors() {
        log.info("查询所有启用的顾问配置");
        return advisorRepository.queryByStatus(1).stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientAdvisorResponseDTO> queryAiClientAdvisorsByStatus(Integer status) {
        log.info("根据状态查询顾问配置请求：{}", status);
        return advisorRepository.queryByStatus(status).stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientAdvisorResponseDTO> queryAiClientAdvisorsByType(String advisorType) {
        log.info("根据顾问类型查询顾问配置请求：{}", advisorType);
        return advisorRepository.queryByAdvisorType(advisorType).stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientAdvisorResponseDTO> queryAiClientAdvisorList(AiClientAdvisorQueryRequestDTO request) {
        log.info("根据条件查询顾问配置列表请求：{}", request);
        List<AiClientAdvisorDTO> advisors;
        if (StringUtils.hasText(request.getAdvisorId())) {
            AiClientAdvisorDTO advisor = advisorRepository.queryByAdvisorId(request.getAdvisorId());
            advisors = advisor != null ? List.of(advisor) : List.of();
        } else if (StringUtils.hasText(request.getAdvisorType())) {
            advisors = advisorRepository.queryByAdvisorType(request.getAdvisorType());
        } else if (request.getStatus() != null) {
            advisors = advisorRepository.queryByStatus(request.getStatus());
        } else {
            advisors = advisorRepository.queryAll();
        }
        List<AiClientAdvisorDTO> filtered = advisors.stream()
                .filter(item -> {
                    if (StringUtils.hasText(request.getAdvisorName()) && (item.getAdvisorName() == null || !item.getAdvisorName().contains(request.getAdvisorName()))) {
                        return false;
                    }
                    if (request.getStatus() != null && !request.getStatus().equals(item.getStatus())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        if (request.getPageNum() != null && request.getPageSize() != null) {
            int pageNum = Math.max(1, request.getPageNum());
            int pageSize = Math.max(1, request.getPageSize());
            int startIndex = (pageNum - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, filtered.size());
            filtered = startIndex < filtered.size() ? filtered.subList(startIndex, endIndex) : List.of();
        }
        return filtered.stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientAdvisorResponseDTO> queryAllAiClientAdvisors() {
        log.info("查询所有顾问配置");
        return advisorRepository.queryAll().stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private AiClientAdvisorDTO toDomain(AiClientAdvisorRequestDTO requestDTO) {
        AiClientAdvisorDTO advisor = new AiClientAdvisorDTO();
        BeanUtils.copyProperties(requestDTO, advisor);
        return advisor;
    }

    private AiClientAdvisorResponseDTO toResponse(AiClientAdvisorDTO advisor) {
        if (advisor == null) {
            return null;
        }
        AiClientAdvisorResponseDTO responseDTO = new AiClientAdvisorResponseDTO();
        BeanUtils.copyProperties(advisor, responseDTO);
        return responseDTO;
    }
}
