package com.tricoq.domain.agent.service.armory.business.data.impl;

import com.tricoq.domain.agent.adapter.repository.IClientRepository;
import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.model.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.model.dto.AiClientAdvisorDTO;
import com.tricoq.domain.agent.model.dto.AiClientApiDTO;
import com.tricoq.domain.agent.model.dto.AiClientModelDTO;
import com.tricoq.domain.agent.model.dto.AiClientSystemPromptDTO;
import com.tricoq.domain.agent.model.dto.AiClientToolMcpDTO;
import com.tricoq.domain.agent.model.dto.AiClientDTO;
import com.tricoq.domain.agent.service.armory.business.data.ILoadDataStrategy;
import com.tricoq.domain.agent.service.armory.node.factory.DefaultArmoryStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 客户端的数据加载策略
 *
 * @author trico qiang
 * @date 10/23/25
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiClientLoadDataStrategy implements ILoadDataStrategy {

    private final IClientRepository clientRepository;

    private final ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void loadData(ArmoryCommandEntity entity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        List<String> clientIds = entity.getCommandIdList();

        CompletableFuture<List<AiClientApiDTO>> aiClientApiListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_api) {}", clientIds);
            return clientRepository.queryAiClientApisByClientIds(clientIds);
        }, threadPoolExecutor);

        CompletableFuture<List<AiClientModelDTO>> aiClientModelListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_model) {}", clientIds);
            return clientRepository.queryAiClientModelsByClientIds(clientIds);
        }, threadPoolExecutor);

        CompletableFuture<List<AiClientToolMcpDTO>> aiClientToolMcpListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_tool_mcp) {}", clientIds);
            return clientRepository.queryAiClientToolMcpsByClientIds(clientIds);
        }, threadPoolExecutor);

        CompletableFuture<Map<String, AiClientSystemPromptDTO>> aiClientSystemPromptListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_system_prompt) {}", clientIds);
            return clientRepository.queryAiClientSystemPromptsByClientIds(clientIds);
        }, threadPoolExecutor);

        CompletableFuture<List<AiClientAdvisorDTO>> aiClientAdvisorListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_advisor) {}", clientIds);
            return clientRepository.queryAiClientAdvisorsByClientIds(clientIds);
        }, threadPoolExecutor);

        CompletableFuture<List<AiClientDTO>> aiClientListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client) {}", clientIds);
            return clientRepository.queryAiClientsByClientIds(clientIds);
        }, threadPoolExecutor);

        CompletableFuture<Void> all = CompletableFuture.allOf(aiClientApiListFuture,
                aiClientModelListFuture,
                aiClientToolMcpListFuture,
                aiClientSystemPromptListFuture,
                aiClientAdvisorListFuture,
                aiClientListFuture).orTimeout(5, TimeUnit.SECONDS);
        //这里还是使用之前的线程
        all.thenRun(() -> {
            dynamicContext.setValue(AiAgentEnumVO.AI_CLIENT_API.getDataName(), aiClientApiListFuture.join());
            dynamicContext.setValue(AiAgentEnumVO.AI_CLIENT_MODEL.getDataName(), aiClientModelListFuture.join());
            dynamicContext.setValue(AiAgentEnumVO.AI_CLIENT_SYSTEM_PROMPT.getDataName(), aiClientSystemPromptListFuture.join());
            dynamicContext.setValue(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getDataName(), aiClientToolMcpListFuture.join());
            dynamicContext.setValue(AiAgentEnumVO.AI_CLIENT_ADVISOR.getDataName(), aiClientAdvisorListFuture.join());
            dynamicContext.setValue(AiAgentEnumVO.AI_CLIENT.getDataName(), aiClientListFuture.join());
        }).join();
    }

    @Override
    public AiAgentEnumVO support() {
        return AiAgentEnumVO.AI_CLIENT;
    }
}
