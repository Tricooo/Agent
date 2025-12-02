package com.tricoq.application.service;

import com.tricoq.application.model.dto.command.SaveAgentDrawCommand;
import com.tricoq.domain.agent.adapter.repository.IAgentDrawConfigRepository;
import com.tricoq.domain.agent.adapter.repository.IAgentRepository;
import com.tricoq.domain.agent.adapter.repository.IClientRepository;
import com.tricoq.domain.agent.model.aggregate.AiAgentAggregate;
import com.tricoq.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.tricoq.domain.agent.model.aggregate.AiAgentDrawConfigAggregate;
import com.tricoq.domain.agent.model.aggregate.AiClientAggregate;
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
        if (!StringUtils.isBlank(command.getConfigName()) ||
                !StringUtils.isBlank(command.getConfigData()) ||
                !StringUtils.isBlank(command.getConfigExecData())) {
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
        if (!StringUtils.isBlank(configId)) {
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
}
