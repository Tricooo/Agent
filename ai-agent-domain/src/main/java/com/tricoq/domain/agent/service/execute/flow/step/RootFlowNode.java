package com.tricoq.domain.agent.service.execute.flow.step;

import com.tricoq.domain.agent.adapter.repository.IAgentRepository;
import com.tricoq.domain.agent.model.dto.McpToolCatalogDTO;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.model.enums.AiClientTypeEnumVO;
import com.tricoq.domain.agent.service.IToolCatalogService;
import com.tricoq.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import com.tricoq.types.framework.chain.StrategyHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author trico qiang
 * @date 11/12/25
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RootFlowNode extends AbstractExecuteSupport {

    private final IAgentRepository agentRepository;

    private final Step1McpToolsAnalysisNode step1McpToolsAnalysisNode;

    private final IToolCatalogService toolCatalogService;

    /**
     * 节点自身处理逻辑
     *
     * @param requestParam   请求参数
     * @param dynamicContext 链路上下文
     * @return 结果
     */
    @Override
    protected String doApply(ExecuteCommandEntity requestParam,
                             DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        log.info("=== 流程执行开始 ====");
        log.info("用户输入: {}", requestParam.getUserInput());
        log.info("最大执行步数: {}", requestParam.getMaxSteps());
        log.info("会话ID: {}", requestParam.getSessionId());

        Map<String, AiAgentClientFlowConfigDTO> configMap = agentRepository
                .queryAiAgentFlowConfigMapByAgentId(requestParam.getAgentId());
        if (configMap.isEmpty()) {
            throw new IllegalArgumentException("该Agent未配置Flow执行链路");
        }
        dynamicContext.getFlowConfigMap().putAll(configMap);

        //提取执行器可用的工具列表
        AiAgentClientFlowConfigDTO executeClientConfig = configMap.get(AiClientTypeEnumVO.EXECUTOR_CLIENT.getCode());
        if (null == executeClientConfig) {
            throw new IllegalArgumentException("执行client未配置");
        }
        String clientId = executeClientConfig.getClientId();
        List<McpToolCatalogDTO> tools = toolCatalogService.resolveToolsByClient(clientId);
        if (CollectionUtils.isEmpty(tools)) {
            log.warn("此agent链路执行client无可用mcp tool,agent id: {},client id: {}", requestParam.getAgentId(),
                    clientId);
        }
        dynamicContext.getState().setToolListPrompt(McpToolCatalogDTO.toPromptText(tools));
        return router(requestParam, dynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String> get(
            ExecuteCommandEntity requestParam,
            DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        return step1McpToolsAnalysisNode;
    }
}
