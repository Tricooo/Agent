package com.tricoq.application.service;

import com.tricoq.api.dto.AiClientRagOrderQueryRequestDTO;
import com.tricoq.api.dto.AiClientRagOrderRequestDTO;
import com.tricoq.api.dto.AiClientRagOrderResponseDTO;
import com.tricoq.domain.agent.adapter.repository.IAiClientRagOrderRepository;
import com.tricoq.domain.agent.model.dto.AiRagOrderDTO;
import com.tricoq.domain.agent.service.IRagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 知识库配置应用服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiClientRagOrderAdminService {

    private final IAiClientRagOrderRepository aiClientRagOrderRepository;
    private final IRagService ragService;

    public boolean createAiClientRagOrder(AiClientRagOrderRequestDTO request) {
        log.info("创建知识库配置请求：{}", request);
        AiRagOrderDTO ragOrder = toDomain(request);
        ragOrder.setCreateTime(LocalDateTime.now());
        ragOrder.setUpdateTime(LocalDateTime.now());
        return aiClientRagOrderRepository.insert(ragOrder);
    }

    public boolean updateAiClientRagOrderById(AiClientRagOrderRequestDTO request) {
        log.info("根据ID更新知识库配置请求：{}", request);
        if (request.getId() == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        AiRagOrderDTO ragOrder = toDomain(request);
        ragOrder.setUpdateTime(LocalDateTime.now());
        return aiClientRagOrderRepository.updateById(ragOrder);
    }

    public boolean updateAiClientRagOrderByRagId(AiClientRagOrderRequestDTO request) {
        log.info("根据知识库ID更新知识库配置请求：{}", request);
        if (!StringUtils.hasText(request.getRagId())) {
            throw new IllegalArgumentException("知识库ID不能为空");
        }
        AiRagOrderDTO ragOrder = toDomain(request);
        ragOrder.setUpdateTime(LocalDateTime.now());
        return aiClientRagOrderRepository.updateByRagId(ragOrder);
    }

    public boolean deleteAiClientRagOrderById(Long id) {
        log.info("根据ID删除知识库配置：{}", id);
        return aiClientRagOrderRepository.deleteById(id);
    }

    public boolean deleteAiClientRagOrderByRagId(String ragId) {
        log.info("根据知识库ID删除知识库配置：{}", ragId);
        return aiClientRagOrderRepository.deleteByRagId(ragId);
    }

    public AiClientRagOrderResponseDTO queryAiClientRagOrderById(Long id) {
        log.info("根据ID查询知识库配置：{}", id);
        return toResponse(aiClientRagOrderRepository.queryById(id));
    }

    public AiClientRagOrderResponseDTO queryAiClientRagOrderByRagId(String ragId) {
        log.info("根据知识库ID查询知识库配置：{}", ragId);
        return toResponse(aiClientRagOrderRepository.queryByRagId(ragId));
    }

    public List<AiClientRagOrderResponseDTO> queryEnabledAiClientRagOrders() {
        log.info("查询启用的知识库配置");
        return aiClientRagOrderRepository.queryEnabledRagOrders().stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientRagOrderResponseDTO> queryAiClientRagOrdersByKnowledgeTag(String knowledgeTag) {
        log.info("根据知识标签查询知识库配置：{}", knowledgeTag);
        return aiClientRagOrderRepository.queryByKnowledgeTag(knowledgeTag).stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientRagOrderResponseDTO> queryAiClientRagOrdersByStatus(Integer status) {
        log.info("根据状态查询知识库配置：{}", status);
        return aiClientRagOrderRepository.queryAll().stream()
                .filter(order -> order.getStatus() != null && order.getStatus().equals(status))
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientRagOrderResponseDTO> queryAiClientRagOrderList(AiClientRagOrderQueryRequestDTO request) {
        log.info("分页查询知识库配置列表：{}", request);
        List<AiRagOrderDTO> orders = aiClientRagOrderRepository.queryAll();
        List<AiRagOrderDTO> filtered = orders.stream()
                .filter(order -> {
                    if (StringUtils.hasText(request.getRagId()) && (order.getRagId() == null || !order.getRagId().contains(request.getRagId()))) {
                        return false;
                    }
                    if (StringUtils.hasText(request.getRagName()) && (order.getRagName() == null || !order.getRagName().contains(request.getRagName()))) {
                        return false;
                    }
                    if (StringUtils.hasText(request.getKnowledgeTag()) && (order.getKnowledgeTag() == null || !order.getKnowledgeTag().contains(request.getKnowledgeTag()))) {
                        return false;
                    }
                    if (request.getStatus() != null && !request.getStatus().equals(order.getStatus())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        if (request.getPageNum() != null && request.getPageSize() != null) {
            int start = Math.max(0, (request.getPageNum() - 1) * request.getPageSize());
            int end = Math.min(start + request.getPageSize(), filtered.size());
            filtered = start < end ? filtered.subList(start, end) : List.of();
        }
        return filtered.stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public boolean uploadRagFile(String name, String tag, List<MultipartFile> files) {
        log.info("上传知识库，请求 {}", name);
        ragService.storeRagFile(name, tag, files);
        return true;
    }

    private AiRagOrderDTO toDomain(AiClientRagOrderRequestDTO requestDTO) {
        AiRagOrderDTO dto = new AiRagOrderDTO();
        BeanUtils.copyProperties(requestDTO, dto);
        return dto;
    }

    private AiClientRagOrderResponseDTO toResponse(AiRagOrderDTO dto) {
        if (dto == null) {
            return null;
        }
        AiClientRagOrderResponseDTO responseDTO = new AiClientRagOrderResponseDTO();
        BeanUtils.copyProperties(dto, responseDTO);
        return responseDTO;
    }
}
