package com.tricoq.domain.agent.service.armory.node;

import com.alibaba.fastjson.JSON;
import com.tricoq.domain.agent.adapter.port.IMcpClientProvider;
import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.model.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.model.dto.AiClientToolMcpDTO;
import com.tricoq.domain.agent.service.armory.node.factory.DefaultArmoryStrategyFactory;
import com.tricoq.types.framework.chain.StrategyHandler;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.List;

/**
 * @author trico qiang
 * @date 10/27/25
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AiClientToolMcpNode extends AbstractArmorySupport {

    private final AiClientModelNode aiClientModelNode;

    private final IMcpClientProvider mcpClientProvider;

    /**
     * 节点自身处理逻辑
     *
     * @param requestParam   请求参数
     * @param dynamicContext 链路上下文
     * @return 结果
     */
    @Override
    protected String doApply(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        log.info("Ai Agent 构建节点，Tool MCP 工具配置{}", JSON.toJSONString(requestParam));
        List<AiClientToolMcpDTO> mcpNodes = dynamicContext.getToolMcps();
        if (CollectionUtils.isEmpty(mcpNodes)) {
            log.warn("没有需要被初始化的 ai client tool mcp");
            return router(requestParam, dynamicContext);
        }

        for (AiClientToolMcpDTO vo : mcpNodes) {
            mcpClientProvider.getOrCreate(vo);
        }
        return router(requestParam, dynamicContext);
    }

    @Override
    protected String beanName(String id) {
        return AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanName(id);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        return aiClientModelNode;
    }
}
