package com.tricoq.domain.agent.service.armory.node;

import com.alibaba.fastjson.JSON;
import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.model.valobj.AiClientToolMcpVO;
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
        List<AiClientToolMcpVO> mcpNodes = dynamicContext.getValue(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getDataName());
        if (CollectionUtils.isEmpty(mcpNodes)) {
            log.warn("没有需要被初始化的 ai client tool mcp");
            return router(requestParam, dynamicContext);
        }

        for (AiClientToolMcpVO vo : mcpNodes) {
            McpSyncClient mcpSyncClient = createMcpSyncClient(vo);
            registerBean(beanName(vo.getMcpId()), McpSyncClient.class, mcpSyncClient);
        }
        return router(requestParam, dynamicContext);
    }

    @Override
    protected String beanName(String id) {
        return AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanName(id);
    }

    private McpSyncClient createMcpSyncClient(AiClientToolMcpVO vo) {

        String transportType = vo.getTransportType();
        switch (transportType) {
            case "sse" -> {
                AiClientToolMcpVO.TransportConfigSse configSse = vo.getTransportConfigSse();
                String originalBaseUri = configSse.getBaseUri();
                String baseUri;
                String sseEndpoint;
                int queryParamStartIndex = originalBaseUri.indexOf("sse");
                if (queryParamStartIndex != -1) {
                    baseUri = originalBaseUri.substring(0, queryParamStartIndex - 1);
                    sseEndpoint = originalBaseUri.substring(queryParamStartIndex - 1);
                } else {
                    baseUri = originalBaseUri;
                    sseEndpoint = configSse.getSseEndpoint();
                }

                sseEndpoint = StringUtils.isBlank(sseEndpoint) ? "/sse" : sseEndpoint;
                var transport = HttpClientSseClientTransport.builder(baseUri)
                        .sseEndpoint(sseEndpoint)
                        .build();
                McpSyncClient client = McpClient.sync(transport)
                        .requestTimeout(Duration.ofSeconds(vo.getRequestTimeout()))
                        .build();
                //与mcp server通讯，获取工具的能力以及出入参
                McpSchema.InitializeResult initializeResult = client.initialize();
                log.info("Tool SSE MCP Initialized {}", initializeResult);
                return client;
            }

            case "stdio" -> {
                AiClientToolMcpVO.TransportConfigStdio configStdio = vo.getTransportConfigStdio();
                var stdioMap = configStdio.getStdio();
                AiClientToolMcpVO.TransportConfigStdio.Stdio stdio = stdioMap.get(vo.getMcpId());
                ServerParameters parameters = ServerParameters.builder(stdio.getCommand())
                        .env(stdio.getEnv())
                        .args(stdio.getArgs())
                        .build();

                McpSyncClient client = McpClient.sync(new StdioClientTransport(parameters))
                        .requestTimeout(Duration.ofSeconds(vo.getRequestTimeout()))
                        .build();
                McpSchema.InitializeResult initializeResult = client.initialize();

                log.info("Tool Stdio MCP Initialized {}", initializeResult);
                return client;
            }
        }
        throw new RuntimeException("err! transportType " + transportType + " not exist!");
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        return aiClientModelNode;
    }
}
