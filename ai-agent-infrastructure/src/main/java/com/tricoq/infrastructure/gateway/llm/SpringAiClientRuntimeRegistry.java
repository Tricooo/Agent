package com.tricoq.infrastructure.gateway.llm;

import com.tricoq.domain.agent.model.dto.AiClientRuntimeProfile;
import com.tricoq.domain.agent.spi.AiClientRuntimeRegistry;
import org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/18/26
 */
@Service
@Slf4j
public class SpringAiClientRuntimeRegistry extends SpringAiSupport implements AiClientRuntimeRegistry {

    private static final String AI_CLIENT_RUNTIME_PROFILE_BEAN_PREFIX = "ai_client_runtime_profile_";

    @Override
    public void register(String clientId, AiClientRuntimeProfile profile) {
        if (StringUtils.isBlank(clientId)) {
            throw new IllegalArgumentException("clientId 不能为空");
        }
        if (profile == null) {
            throw new IllegalArgumentException("runtimeProfile 不能为空");
        }

        registerBean(buildBeanName(clientId), AiClientRuntimeProfile.class, profile);
        log.info("注册 client runtime profile 成功, clientId={}", clientId);
    }

    @Override
    public AiClientRuntimeProfile getRequired(String clientId) {
        String beanName = buildBeanName(clientId);
        try {
            return getBean(beanName);
        } catch (Exception e) {
            throw new IllegalStateException("未找到 client 对应的 runtime profile, clientId=" + clientId, e);
        }
    }

    private String buildBeanName(String clientId) {
        if (StringUtils.isBlank(clientId)) {
            throw new IllegalArgumentException("clientId 不能为空");
        }
        return AI_CLIENT_RUNTIME_PROFILE_BEAN_PREFIX + clientId;
    }
}
