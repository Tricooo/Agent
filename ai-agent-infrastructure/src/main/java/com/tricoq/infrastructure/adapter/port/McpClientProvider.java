package com.tricoq.infrastructure.adapter.port;

import com.tricoq.domain.agent.adapter.port.IMcpClientProvider;
import com.tricoq.domain.agent.model.dto.AiClientToolMcpDTO;
import com.tricoq.domain.agent.model.enums.AiAgentEnumVO;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/9/26
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class McpClientProvider implements IMcpClientProvider {

    private final ApplicationContext applicationContext;

    @Override
    public McpSyncClient getOrCreate(AiClientToolMcpDTO toolMcp) {
        String mcpId = toolMcp.getMcpId();
        String beanName = beanName(mcpId);
        McpSyncClient mcpSyncClient = getBean(beanName, McpSyncClient.class);
        if (null != mcpSyncClient) {
            return mcpSyncClient;
        }
        synchronized (mcpId) {
            mcpSyncClient = getBean(beanName, McpSyncClient.class);
            if (null != mcpSyncClient) {
                return mcpSyncClient;
            }
            mcpSyncClient = createMcpSyncClient(toolMcp);
            registerBean(beanName, McpSyncClient.class, mcpSyncClient);
            return mcpSyncClient;
        }
    }

    private McpSyncClient createMcpSyncClient(AiClientToolMcpDTO toolMcp) {

        String transportType = toolMcp.getTransportType();
        switch (transportType) {
            case "sse" -> {
                AiClientToolMcpDTO.TransportConfigSse configSse = toolMcp.getTransportConfigSse();
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
                        .requestTimeout(Duration.ofSeconds(toolMcp.getRequestTimeout()))
                        .build();
                //与mcp server通讯(初始握手)，获取工具的能力以及出入参
                McpSchema.InitializeResult initializeResult = client.initialize();
                log.info("Tool SSE MCP Initialized {}", initializeResult);
                return client;
            }

            case "stdio" -> {
                AiClientToolMcpDTO.TransportConfigStdio configStdio = toolMcp.getTransportConfigStdio();
                var stdioMap = configStdio.getStdio();
                AiClientToolMcpDTO.TransportConfigStdio.Stdio stdio = stdioMap.get(toolMcp.getMcpId());
                ServerParameters parameters = ServerParameters.builder(stdio.getCommand())
                        .env(stdio.getEnv())
                        .args(stdio.getArgs())
                        .build();

                McpSyncClient client = McpClient.sync(new StdioClientTransport(parameters))
                        .requestTimeout(Duration.ofSeconds(toolMcp.getRequestTimeout()))
                        .build();
                McpSchema.InitializeResult initializeResult = client.initialize();

                log.info("Tool Stdio MCP Initialized {}", initializeResult);
                return client;
            }
        }
        throw new RuntimeException("err! transportType " + transportType + " not exist!");
    }

    protected synchronized <T> void registerBean(String beanName, Class<T> clazz, T instance) {
        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz, () -> instance);
        AbstractBeanDefinition rawBeanDefinition = builder.getRawBeanDefinition();
        rawBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

        if (factory.containsBeanDefinition(beanName)) {
            factory.removeBeanDefinition(beanName);
        }

//        if (factory.containsSingleton(beanName)) {
//            factory.destroySingleton(beanName);
//        }

        factory.registerBeanDefinition(beanName, rawBeanDefinition);

        log.info("成功注册Bean: {}", beanName);
    }


    @SuppressWarnings("unchecked")
    protected <T> T getBean(String beanName, Class<T> clazz) {
        try {
            return applicationContext.getBean(beanName, clazz);
        } catch (NoSuchBeanDefinitionException e) {
            return null;
        } catch (BeansException e) {
            log.error("bean获取/转换异常:{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    protected String beanName(String id) {
        return AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanName(id);
    }
}
