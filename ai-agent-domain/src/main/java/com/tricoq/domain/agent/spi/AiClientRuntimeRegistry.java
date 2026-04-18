package com.tricoq.domain.agent.spi;

import com.tricoq.domain.agent.model.dto.AiClientRuntimeProfile;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/18/26
 */
public interface AiClientRuntimeRegistry {

    String AI_CLIENT_RUNTIME_PROFILE_BEAN_PREFIX = "ai_client_runtime_profile_";

    AiClientRuntimeProfile getRequired(String clientId);
}
