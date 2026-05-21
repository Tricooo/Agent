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
import com.tricoq.domain.agent.service.rag.rewrite.enums.RewritePolicy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
@Slf4j
public class AiClientLoadDataStrategy implements ILoadDataStrategy {

    private final IClientRepository clientRepository;

    private final ThreadPoolExecutor threadPoolExecutor;

    public AiClientLoadDataStrategy(IClientRepository clientRepository,
                                    @Qualifier("threadPoolExecutor") ThreadPoolExecutor threadPoolExecutor) {
        this.clientRepository = clientRepository;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Override
    public void loadData(ArmoryCommandEntity entity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        List<String> requestedClientIds = distinctClientIds(entity.getCommandIdList());

        CompletableFuture<List<AiClientAdvisorDTO>> aiClientAdvisorListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_advisor) {}", requestedClientIds);
            return clientRepository.queryAiClientAdvisorsByClientIds(requestedClientIds);
        }, threadPoolExecutor);

        List<AiClientAdvisorDTO> advisors = aiClientAdvisorListFuture.orTimeout(5, TimeUnit.SECONDS).join();
        if (CollectionUtils.isNotEmpty(advisors)) {
            dynamicContext.setAdvisorConfigs(advisors);
        }

        List<String> clientIds = resolveLoadClientIds(requestedClientIds, advisors);

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

        CompletableFuture<List<AiClientDTO>> aiClientListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client) {}", clientIds);
            return clientRepository.queryAiClientsByClientIds(clientIds);
        }, threadPoolExecutor);

        CompletableFuture<Void> configFutures = CompletableFuture.allOf(aiClientApiListFuture,
                aiClientModelListFuture,
                aiClientToolMcpListFuture,
                aiClientSystemPromptListFuture,
                aiClientListFuture).orTimeout(5, TimeUnit.SECONDS);

        configFutures.thenRun(() -> {
            dynamicContext.setClientApis(aiClientApiListFuture.join());
            dynamicContext.setClientModels(aiClientModelListFuture.join());
            dynamicContext.setSystemPromptMap(aiClientSystemPromptListFuture.join());
            dynamicContext.setToolMcps(aiClientToolMcpListFuture.join());
            dynamicContext.setClients(aiClientListFuture.join());
        }).join();
    }

    private List<String> distinctClientIds(Collection<String> clientIds) {
        Set<String> distinctIds = new LinkedHashSet<>();
        addClientIds(distinctIds, clientIds);
        return new ArrayList<>(distinctIds);
    }

    private List<String> resolveLoadClientIds(List<String> requestedClientIds, List<AiClientAdvisorDTO> advisors) {
        Set<String> clientIds = new LinkedHashSet<>();
        addClientIds(clientIds, requestedClientIds);
        addClientIds(clientIds, extractRagRewriteClientIds(advisors));
        return new ArrayList<>(clientIds);
    }

    private void addClientIds(Set<String> target, Collection<String> clientIds) {
        if (CollectionUtils.isEmpty(clientIds)) {
            return;
        }
        for (String clientId : clientIds) {
            if (StringUtils.isNotBlank(clientId)) {
                target.add(clientId);
            }
        }
    }

    private List<String> extractRagRewriteClientIds(List<AiClientAdvisorDTO> advisors) {
        if (CollectionUtils.isEmpty(advisors)) {
            return List.of();
        }
        List<String> addClients = new ArrayList<>();
        for (AiClientAdvisorDTO advisor : advisors) {
            AiClientAdvisorDTO.RagAnswer ragAnswer = advisor.getRagAnswer();
            if (ragAnswer == null) {
                continue;
            }
            String rewritePolicy = ragAnswer.getRewritePolicy();
            if (RewritePolicy.getByPolicyOrDefault(rewritePolicy).equals(RewritePolicy.LLM_MULTI_QUERY) &&
                    StringUtils.isNotBlank(ragAnswer.getQueryRewriterClientId())) {
                String clientId = ragAnswer.getQueryRewriterClientId();
                addClients.add(clientId);
            }
        }
        return addClients;
    }

    @Override
    public AiAgentEnumVO support() {
        return AiAgentEnumVO.AI_CLIENT;
    }
}
