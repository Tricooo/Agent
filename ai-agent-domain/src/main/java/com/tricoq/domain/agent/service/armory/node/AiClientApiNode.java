package com.tricoq.domain.agent.service.armory.node;

import com.alibaba.fastjson.JSON;
import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.model.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.model.dto.AiClientApiDTO;
import com.tricoq.domain.agent.service.armory.node.factory.DefaultArmoryStrategyFactory;
import com.tricoq.types.framework.chain.StrategyHandler;
import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;

/**
 * @author trico qiang
 * @date 10/24/25
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiClientApiNode extends AbstractArmorySupport {

    private final AiClientToolMcpNode aiClientToolMcpNode;
    private final RestClient.Builder restClientBuilder;
    private final WebClient.Builder webClientBuilder;

    @Value("${spring.ai.openai.http.connect-timeout:5s}")
    private Duration connectTimeout;

    @Value("${spring.ai.openai.http.read-timeout:30s}")
    private Duration readTimeout;

    @Value("${spring.ai.openai.http.response-timeout:30s}")
    private Duration responseTimeout;

    /**
     * 节点自身处理逻辑
     */
    @Override
    protected String doApply(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        log.info("Ai Agent 构建节点，API 接口请求{}", JSON.toJSONString(requestParam));
        List<AiClientApiDTO> apiVOList = dynamicContext.getValue(AiAgentEnumVO.AI_CLIENT_API.getDataName());
        if(CollectionUtils.isEmpty(apiVOList)){
            log.warn("没有需要被初始化的 ai client api");
            return router(requestParam, dynamicContext);
        }

        for (AiClientApiDTO apiVO : apiVOList) {
            log.info("初始化 AI API client apiId={} connectTimeout={} readTimeout={} responseTimeout={}",
                    apiVO.getApiId(), connectTimeout, readTimeout, responseTimeout);

            OpenAiApi openAiApi = OpenAiApi.builder()
                    .apiKey(apiVO.getApiKey())
                    .baseUrl(apiVO.getBaseUrl())
                    .completionsPath(apiVO.getCompletionsPath())
                    .embeddingsPath(apiVO.getEmbeddingsPath())
                    .restClientBuilder(buildRestClientBuilder())
                    .webClientBuilder(buildWebClientBuilder())
                    .build();
            //注册 bean
            registerBean(AiAgentEnumVO.AI_CLIENT_API.getBeanName(apiVO.getApiId()), OpenAiApi.class, openAiApi);
        }

        return router(requestParam, dynamicContext);
    }

    private RestClient.Builder buildRestClientBuilder() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        return restClientBuilder.clone().requestFactory(requestFactory);
    }

    private WebClient.Builder buildWebClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(connectTimeout.toMillis()))
                .responseTimeout(responseTimeout);

        return webClientBuilder.clone()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        return aiClientToolMcpNode;
    }
}
