package com.tricoq.domain.agent.service.armory.business.data.impl;

import com.tricoq.domain.agent.adapter.repository.IAgentRepository;
import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.model.valobj.AiClientAdvisorVO;
import com.tricoq.domain.agent.model.valobj.AiClientApiVO;
import com.tricoq.domain.agent.model.valobj.AiClientModelVO;
import com.tricoq.domain.agent.model.valobj.AiClientSystemPromptVO;
import com.tricoq.domain.agent.model.valobj.AiClientToolMcpVO;
import com.tricoq.domain.agent.model.valobj.AiClientVO;
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

    private final IAgentRepository agentRepository;

    private final ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void loadData(ArmoryCommandEntity entity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        List<String> clientIds = entity.getCommandIdList();

        CompletableFuture<List<AiClientApiVO>> aiClientApiListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_api) {}", clientIds);
            return agentRepository.queryAiClientApiVOListByClientIds(clientIds);
        }, threadPoolExecutor);

        CompletableFuture<List<AiClientModelVO>> aiClientModelListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_model) {}", clientIds);
            return agentRepository.queryAiClientModelVOByClientIds(clientIds);
        }, threadPoolExecutor);

        CompletableFuture<List<AiClientToolMcpVO>> aiClientToolMcpListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_tool_mcp) {}", clientIds);
            return agentRepository.queryAiClientToolMcpVOByClientIds(clientIds);
        }, threadPoolExecutor);

        CompletableFuture<Map<String, AiClientSystemPromptVO>> aiClientSystemPromptListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_system_prompt) {}", clientIds);
            return agentRepository.queryAiClientSystemPromptVOByClientIds(clientIds);
        }, threadPoolExecutor);

        CompletableFuture<List<AiClientAdvisorVO>> aiClientAdvisorListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_advisor) {}", clientIds);
            return agentRepository.queryAiClientAdvisorVOByClientIds(clientIds);
        }, threadPoolExecutor);

        CompletableFuture<List<AiClientVO>> aiClientListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client) {}", clientIds);
            return agentRepository.queryAiClientVOByClientIds(clientIds);
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
