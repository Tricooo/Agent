package com.tricoq.application.service;

import com.tricoq.api.dto.AiAgentDrawConfigQueryRequestDTO;
import com.tricoq.api.dto.AiAgentDrawConfigResponseDTO;
import com.tricoq.application.model.dto.command.SaveAgentDrawCommand;
import com.tricoq.application.service.util.DrawConfigParser;
import com.tricoq.domain.agent.adapter.repository.IAgentDrawConfigRepository;
import com.tricoq.domain.agent.adapter.repository.IAgentRepository;
import com.tricoq.domain.agent.adapter.repository.IClientRepository;
import com.tricoq.domain.agent.model.aggregate.AiAgentAggregate;
import com.tricoq.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.tricoq.domain.agent.model.aggregate.AiAgentDrawConfigAggregate;
import com.tricoq.domain.agent.model.aggregate.AiClientAggregate;
import com.tricoq.domain.agent.model.entity.DrawConfigQueryCommandEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;

/**
 *
 *
 * @author trico qiang
 * @date 11/25/25
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiAgentDrawAdminService {

    private final DrawConfigParser drawConfigParser;

    private final IAgentRepository agentRepository;

    private final IAgentDrawConfigRepository agentDrawConfigRepository;

    private final IClientRepository clientRepository;

    @Transactional(rollbackFor = Exception.class)
    public void saveDrawConfig(SaveAgentDrawCommand command) throws Exception {

        log.info("保存流程图配置请求：{}", command);

        String agentId = command.getAgentId();
        if (StringUtils.isBlank(agentId)) {
            agentId = UUID.randomUUID().toString().replaceAll("-", "");
            command.setAgentId(agentId);
        }

        // 参数校验
        if (StringUtils.isBlank(command.getConfigName()) ||
                StringUtils.isBlank(command.getConfigData()) ||
                StringUtils.isBlank(command.getConfigExecData())) {
            throw new IllegalArgumentException("参数异常");
        }

        String configExecData = command.getConfigExecData();
        AiAgentAggregate aiAgent = drawConfigParser.buildAgent(agentId, configExecData);
        // 先装配聚合内的 flow 配置，后续保存使用聚合行为去重/校验
        List<AiAgentClientFlowConfigVO> aiAgentClientFlowConfigs = drawConfigParser.buildClientFlowConfig(agentId, configExecData);
        aiAgent.replaceFlowConfigs(aiAgentClientFlowConfigs);
        if (!agentRepository.saveOrUpdateByAggregateId(aiAgent)) {
            throw new Exception("agent保存失败");
        }

        // 生成配置ID（如果没有提供）
        String configId = command.getConfigId();
        if (StringUtils.isBlank(configId)) {
            configId = UUID.randomUUID().toString().replace("-", "");
        }
        AiAgentDrawConfigAggregate drawConfig = new AiAgentDrawConfigAggregate();
        BeanUtils.copyProperties(command, drawConfig);
        drawConfig.setConfigId(configId);
        drawConfig.setVersion(1);
        drawConfig.setStatus(1);
        if (!agentDrawConfigRepository.saveOrUpdateByAggregateId(drawConfig)) {
            throw new Exception("agent draw config保存失败");
        }

        //解析client config
        List<AiClientAggregate> aiClientAggregates = drawConfigParser.buildClientConfig(agentId, configId, configExecData);
        if (!clientRepository.saveOrUpdateClientConfigByAggregate(aiClientAggregates, null)) {
            throw new Exception("client config保存失败");
        }

        //解析agent flow
        if (!agentRepository.saveFlowConfig(aiAgent.getFlowConfigs())) {
            throw new Exception("client flow config保存失败");
        }
    }

    public List<AiAgentDrawConfigResponseDTO> queryDrawConfigList(AiAgentDrawConfigQueryRequestDTO request) {
        DrawConfigQueryCommandEntity query = DrawConfigQueryCommandEntity.builder()
                .configId(request.getConfigId())
                .configName(request.getConfigName())
                .agentId(request.getAgentId())
                .status(request.getStatus())
                .build();
        List<AiAgentDrawConfigResponseDTO> all = agentDrawConfigRepository.queryByCondition(query).stream()
                .map(this::toResponse)
                .toList();
//        Integer pageNum = request.getPageNum();
//        Integer pageSize = request.getPageSize();
//        if (pageNum != null && pageSize != null && pageNum > 0 && pageSize > 0) {
//            int start = Math.max(0, (pageNum - 1) * pageSize);
//            int end = Math.min(start + pageSize, all.size());
//            if (start >= all.size()) {
//                return List.of();
//            }
//            return all.subList(start, end);
//        }
        return all;
    }

    private AiAgentDrawConfigResponseDTO toResponse(AiAgentDrawConfigAggregate aggregate) {
        if (aggregate == null) {
            return null;
        }
        AiAgentDrawConfigResponseDTO dto = new AiAgentDrawConfigResponseDTO();
        BeanUtils.copyProperties(aggregate, dto);
        return dto;
    }

    public AiAgentDrawConfigResponseDTO getDrawConfig(String configId) {
        return toResponse(agentDrawConfigRepository.queryByConfigId(configId));
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByConfigId(String configId) {
        //todo 删除bean工厂中注册的智能体

        AiAgentDrawConfigAggregate configAggregate = agentDrawConfigRepository.queryByConfigId(configId);
        if(configAggregate == null) {
            return false;
        }
        String agentId = configAggregate.getAgentId();
        //todo 补全删除逻辑
        boolean result = agentRepository.removeByAggregateId(agentId);
        boolean delResult = agentDrawConfigRepository.removeByAggregateId(configId);
        return result && delResult;
    }
}
