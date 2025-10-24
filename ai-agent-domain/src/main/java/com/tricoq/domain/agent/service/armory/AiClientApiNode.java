package com.tricoq.domain.agent.service.armory;

import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.model.valobj.AiAgentEnumVO;
import com.tricoq.domain.agent.model.valobj.AiClientApiVO;
import com.tricoq.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import com.tricoq.domain.framework.chain.StrategyHandler;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author trico qiang
 * @date 10/24/25
 */
@Component
public class AiClientApiNode extends AbstractArmorySupport {

    /**
     * 节点自身处理逻辑
     */
    @Override
    protected String doApply(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        List<AiClientApiVO> apiVOList = dynamicContext.getValue(AiAgentEnumVO.AI_CLIENT_API.getDataName());

        for (AiClientApiVO apiVO : apiVOList) {
            OpenAiApi openAiApi = OpenAiApi.builder()
                    .apiKey(apiVO.getApiKey())
                    .baseUrl(apiVO.getBaseUrl())
                    .completionsPath(apiVO.getCompletionsPath())
                    .embeddingsPath(apiVO.getEmbeddingsPath())
                    .build();
            //注册bean
        }

        return router(requestParam, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        return getDefaultHandler();
    }
}
