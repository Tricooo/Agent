package com.tricoq.domain.agent.model.aggregate;

import com.tricoq.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Agent 聚合根：承载基础信息以及关联的客户端流程配置。
 *
 * @author trico qiang
 * @date 11/26/25
 */
@Getter
public class AiAgentAggregate {

    /**
     * 智能体ID（业务主键）
     */
    private final String agentId;

    /**
     * 智能体名称
     */
    private String agentName;

    /**
     * 描述
     */
    private String description;

    /**
     * 渠道类型(agent，chat_stream)
     */
    private String channel;

    /**
     * 执行策略(auto、flow)
     */
    private String strategy;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 客户端流程配置（按 sequence 去重/保序）
     */
    private final List<AiAgentClientFlowConfigVO> flowConfigs = new ArrayList<>();

    private AiAgentAggregate(String agentId, String agentName, String description,
                             String channel, String strategy, Integer status) {
        this.agentId = Objects.requireNonNull(agentId, "agentId cannot be null");
        this.agentName = agentName;
        this.description = description;
        this.channel = channel;
        this.strategy = strategy;
        this.status = status;
    }

    /**
     * 创建新的聚合根（用于新建场景）。
     */
    public static AiAgentAggregate create(String agentId, String agentName, String description,
                                          String channel, String strategy, Integer status) {
        return new AiAgentAggregate(agentId, agentName, description, channel, strategy, status);
    }

    /**
     * 从存储数据还原聚合根，并附带流程配置。
     */
    public static AiAgentAggregate restore(String agentId, String agentName, String description,
                                           String channel, String strategy, Integer status,
                                           Collection<AiAgentClientFlowConfigVO> flowConfigs) {
        AiAgentAggregate aggregate = new AiAgentAggregate(agentId, agentName, description, channel, strategy, status);
        aggregate.replaceFlowConfigs(flowConfigs);
        return aggregate;
    }

    /**
     * 更新基础信息，保持对外封装。
     */
    public void updateBasicInfo(String agentName, String description, String channel,
                                String strategy, Integer status) {
        this.agentName = agentName;
        this.description = description;
        this.channel = channel;
        this.strategy = strategy;
        this.status = status;
    }

    /**
     * 批量附加流程配置，按 sequence+agentId 去重。
     */
    public void attachFlowConfigs(Collection<AiAgentClientFlowConfigVO> flowConfigs) {
        if (flowConfigs == null || flowConfigs.isEmpty()) {
            return;
        }
        this.flowConfigs.addAll(new HashSet<>(flowConfigs));
    }

    /**
     * 用新流程覆盖旧流程。
     */
    public void replaceFlowConfigs(Collection<AiAgentClientFlowConfigVO> flowConfigs) {
        this.flowConfigs.clear();
        attachFlowConfigs(flowConfigs);
    }
}
