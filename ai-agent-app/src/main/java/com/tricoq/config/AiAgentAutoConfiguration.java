package com.tricoq.config;

import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

/**
 * agent 自动装配
 *
 * @author trico qiang
 * @date 11/4/25
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AiAgentAutoConfigProperties.class)
@ConditionalOnProperty(prefix = "spring.ai.agent.auto-config", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class AiAgentAutoConfiguration implements ApplicationListener<ApplicationReadyEvent> {

    private final AiAgentAutoConfigProperties aiAgentAutoConfigProperties;

    private final DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        try {
            log.info("AI Agent 自动装配开始，配置: {}", aiAgentAutoConfigProperties);

            // 检查配置是否有效
            if (!aiAgentAutoConfigProperties.isEnabled()) {
                log.info("AI Agent 自动装配未启用");
                return;
            }

            List<String> clientIds = aiAgentAutoConfigProperties.getClientIds();
            if (CollectionUtils.isEmpty(clientIds)) {
                log.warn("AI Agent 自动装配配置的客户端ID列表为空");
                return;
            }

            log.info("开始自动装配AI客户端，客户端ID列表: {}", clientIds);

            var strategyHandler = Optional.ofNullable(defaultArmoryStrategyFactory.armoryStrategyHandler())
                    .orElseThrow(() -> new RuntimeException("装配根节点不存在"));

            String result = strategyHandler.apply(ArmoryCommandEntity.builder()
                            .commandIdList(clientIds)
                            .commandType(AiAgentEnumVO.AI_CLIENT.getCode())
                            .build()
                    , new DefaultArmoryStrategyFactory.DynamicContext());

            log.info("AI Agent 自动装配完成，结果: {}", result);
        } catch (Exception e) {
            log.error("AI Agent 自动装配失败", e);
        }
    }
}
