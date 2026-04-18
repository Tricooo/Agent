package com.tricoq.infrastructure.gateway.llm;

import com.tricoq.domain.agent.model.dto.AiClientRuntimeProfile;
import com.tricoq.domain.agent.spi.AiClientRuntimeRegistry;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/18/26
 */
public class SpringAiClientRuntimeRegistry implements AiClientRuntimeRegistry {



    @Override
    public AiClientRuntimeProfile getRequired(String clientId) {
        return null;
    }
}
