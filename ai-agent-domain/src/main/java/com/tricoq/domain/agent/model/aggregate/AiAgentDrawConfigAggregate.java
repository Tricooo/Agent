package com.tricoq.domain.agent.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author trico qiang
 * @date 11/26/25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAgentDrawConfigAggregate {

    /**
     * 配置ID（唯一标识）
     */
    private String configId;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 关联的智能体ID（来自ai_agent表）
     */
    private String agentId;

    /**
     * 完整的拖拉拽配置JSON数据（包含nodes和edges）
     */
    private String configData;

    /**
     * 配置版本号
     */
    private Integer version;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;
}
