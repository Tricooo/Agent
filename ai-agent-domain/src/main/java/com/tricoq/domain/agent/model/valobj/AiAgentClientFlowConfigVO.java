package com.tricoq.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 *
 *
 * @author trico qiang
 * @date 11/27/25
 */
@Getter
@AllArgsConstructor
@Builder
public class AiAgentClientFlowConfigVO {

    private String agentId;
    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * 客户端枚举
     */
    private String clientType;

    /**
     * 序列号(执行顺序)
     */
    private Integer sequence;

    /**
     * 执行步骤提示词
     */
    private String stepPrompt;

    /**
     * 业务等值：基于 agentId + sequence 判定唯一性。
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AiAgentClientFlowConfigVO that = (AiAgentClientFlowConfigVO) o;
        // 只有当 agentId 与 sequence 都具备时才参与业务等值
        if (agentId != null && sequence != null
                && that.agentId != null && that.sequence != null) {
            return agentId.equals(that.agentId) && sequence.equals(that.sequence);
        }
        // 业务主键不完整时，不认为等值
        return false;
    }

    @Override
    public int hashCode() {
        if (agentId != null && sequence != null) {
            return Objects.hash(agentId, sequence);
        }
        // 主键不完整时，用对象身份保证 hashCode 与 equals 合约
        return System.identityHashCode(this);
    }
}
