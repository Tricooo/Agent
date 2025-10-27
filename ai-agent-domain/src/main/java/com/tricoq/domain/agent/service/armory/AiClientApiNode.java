package com.tricoq.domain.agent.service.armory;

import com.alibaba.fastjson.JSON;
import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.model.valobj.AiAgentEnumVO;
import com.tricoq.domain.agent.model.valobj.AiClientApiVO;
import com.tricoq.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import com.tricoq.domain.framework.chain.StrategyHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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

    /**
     * 节点自身处理逻辑
     */
    @Override
    protected String doApply(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        log.info("Ai Agent 构建节点，API 接口请求{}", JSON.toJSONString(requestParam));
        List<AiClientApiVO> apiVOList = dynamicContext.getValue(AiAgentEnumVO.AI_CLIENT_API.getDataName());
        if(CollectionUtils.isEmpty(apiVOList)){
            log.warn("没有需要被初始化的 ai client api");
            return router(requestParam, dynamicContext);
        }

        for (AiClientApiVO apiVO : apiVOList) {
            OpenAiApi openAiApi = OpenAiApi.builder()
                    .apiKey(apiVO.getApiKey())
                    .baseUrl(apiVO.getBaseUrl())
                    .completionsPath(apiVO.getCompletionsPath())
                    .embeddingsPath(apiVO.getEmbeddingsPath())
                    .build();
            //注册bean
            registerBean(AiAgentEnumVO.AI_CLIENT_API.getBeanName(apiVO.getApiId()), OpenAiApi.class, openAiApi);
        }

        return router(requestParam, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        return aiClientToolMcpNode;
    }
}
