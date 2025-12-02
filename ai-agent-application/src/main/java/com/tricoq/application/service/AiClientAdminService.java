package com.tricoq.application.service;

import com.tricoq.api.dto.AiClientQueryRequestDTO;
import com.tricoq.api.dto.AiClientRequestDTO;
import com.tricoq.api.dto.AiClientResponseDTO;
import com.tricoq.domain.agent.adapter.repository.IClientRepository;
import com.tricoq.domain.agent.model.dto.AiClientDTO;
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
 * AI 客户端配置应用服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiClientAdminService {

    private final IClientRepository clientRepository;

    public boolean createAiClient(AiClientRequestDTO request) {
        log.info("创建AI客户端配置请求：{}", request);
        AiClientDTO clientDTO = toDomain(request);
        clientDTO.setCreateTime(LocalDateTime.now());
        clientDTO.setUpdateTime(LocalDateTime.now());
        return clientRepository.insertClient(clientDTO);
    }

    public boolean updateAiClientById(AiClientRequestDTO request) {
        log.info("根据ID更新AI客户端配置请求：{}", request);
        if (request.getId() == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        AiClientDTO clientDTO = toDomain(request);
        clientDTO.setUpdateTime(LocalDateTime.now());
        return clientRepository.updateClientById(clientDTO);
    }

    public boolean updateAiClientByClientId(AiClientRequestDTO request) {
        log.info("根据客户端ID更新AI客户端配置请求：{}", request);
        if (!StringUtils.hasText(request.getClientId())) {
            throw new IllegalArgumentException("客户端ID不能为空");
        }
        AiClientDTO clientDTO = toDomain(request);
        clientDTO.setUpdateTime(LocalDateTime.now());
        return clientRepository.updateClientByClientId(clientDTO);
    }

    public boolean deleteAiClientById(Long id) {
        log.info("根据ID删除AI客户端配置请求：{}", id);
        return clientRepository.deleteClientById(id);
    }

    public boolean deleteAiClientByClientId(String clientId) {
        log.info("根据客户端ID删除AI客户端配置请求：{}", clientId);
        return clientRepository.deleteClientByClientId(clientId);
    }

    public AiClientResponseDTO queryAiClientById(Long id) {
        log.info("根据ID查询AI客户端配置请求：{}", id);
        AiClientDTO clientDTO = clientRepository.queryClientById(id);
        if (clientDTO == null) {
            return null;
        }
        return toResponse(clientDTO);
    }

    public AiClientResponseDTO queryAiClientByClientId(String clientId) {
        log.info("根据客户端ID查询AI客户端配置请求：{}", clientId);
        AiClientDTO clientDTO = clientRepository.queryClientByClientId(clientId);
        return toResponse(clientDTO);
    }

    public List<AiClientResponseDTO> queryEnabledAiClients() {
        log.info("查询所有启用的AI客户端配置");
        return clientRepository.queryEnabledClients().stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientResponseDTO> queryAiClientList(AiClientQueryRequestDTO request) {
        log.info("根据条件查询AI客户端配置列表请求：{}", request);
        List<AiClientDTO> clients;
        if (StringUtils.hasText(request.getClientId())) {
            AiClientDTO client = clientRepository.queryClientByClientId(request.getClientId());
            clients = client != null ? List.of(client) : List.of();
        } else if (StringUtils.hasText(request.getClientName())) {
            clients = clientRepository.queryClientsByName(request.getClientName());
        } else {
            clients = clientRepository.queryAllClients();
        }
        if (request.getStatus() != null) {
            clients = clients.stream()
                    .filter(item -> request.getStatus().equals(item.getStatus()))
                    .toList();
        }
        if (request.getPageNum() != null && request.getPageSize() != null) {
            int start = Math.max(0, (request.getPageNum() - 1) * request.getPageSize());
            int end = Math.min(start + request.getPageSize(), clients.size());
            clients = start < end ? clients.subList(start, end) : List.of();
        }
        return clients.stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AiClientResponseDTO> queryAllAiClients() {
        log.info("查询所有AI客户端配置");
        return clientRepository.queryAllClients().stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private AiClientDTO toDomain(AiClientRequestDTO requestDTO) {
        AiClientDTO clientDTO = new AiClientDTO();
        BeanUtils.copyProperties(requestDTO, clientDTO);
        return clientDTO;
    }

    private AiClientResponseDTO toResponse(AiClientDTO aiClient) {
        if (aiClient == null) {
            return null;
        }
        AiClientResponseDTO responseDTO = new AiClientResponseDTO();
        BeanUtils.copyProperties(aiClient, responseDTO);
        return responseDTO;
    }
}
