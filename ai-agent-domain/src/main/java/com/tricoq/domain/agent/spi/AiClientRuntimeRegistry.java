package com.tricoq.domain.agent.spi;

import com.tricoq.domain.agent.model.dto.AiClientRuntimeProfile;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/18/26
 */
public interface AiClientRuntimeRegistry {

    void register(String clientId, AiClientRuntimeProfile profile);

    AiClientRuntimeProfile getRequired(String clientId);
}
