package com.tricoq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 *
 *
 * @author trico qiang
 * @date 11/4/25
 */
@Data
@ConfigurationProperties(prefix = "spring.ai.agent.auto-config")
public class AiAgentAutoConfigProperties {

    private boolean enabled = false;

    private List<String> clientIds;
}
