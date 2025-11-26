package com.tricoq.domain.agent.model.aggregate;

import lombok.Getter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * AI Client 聚合根：封装模型、Prompt、工具、顾问等关联，并提供简单不变式约束。
 * @author trico qiang
 */
@Getter
public class AiClientAggregate {

    private final String clientId;
    private final String clientName;
    private final String description;
    private String modelId;
    private final Set<String> promptIds = new LinkedHashSet<>();
    private final Set<String> mcpIds = new LinkedHashSet<>();
    private final Set<String> advisorIds = new LinkedHashSet<>();

    private AiClientAggregate(String clientId, String clientName, String description) {
        this.clientId = Objects.requireNonNull(clientId, "clientId cannot be null");
        this.clientName = Objects.requireNonNull(clientName, "clientName cannot be null");
        this.description = description;
    }

    public static AiClientAggregate create(String clientId, String clientName, String description) {
        return new AiClientAggregate(clientId, clientName, description);
    }

    /**
     * 还原已有数据构建聚合根，供仓储从存储层装载。
     */
    public static AiClientAggregate restore(String clientId,
                                            String clientName,
                                            String description,
                                            String modelId,
                                            Collection<String> promptIds,
                                            Collection<String> mcpIds,
                                            Collection<String> advisorIds) {
        AiClientAggregate aggregate = new AiClientAggregate(clientId, clientName, description);
        aggregate.modelId = modelId;
        aggregate.attachPrompts(promptIds);
        aggregate.attachMcps(mcpIds);
        aggregate.attachAdvisors(advisorIds);
        return aggregate;
    }

    /**
     * 只允许绑定一个模型，重复绑定不同模型会抛出异常。
     */
    public void assignModel(String modelId) {
        if (modelId == null) {
            return;
        }
        if (this.modelId != null && !this.modelId.equals(modelId)) {
            throw new IllegalStateException("model already assigned: " + this.modelId);
        }
        this.modelId = modelId;
    }

    /**
     * 附加/去重 Prompt。
     */
    public void attachPrompts(Collection<String> promptIds) {
        if (promptIds == null || promptIds.isEmpty()) {
            return;
        }
        promptIds.stream().filter(Objects::nonNull).forEach(this.promptIds::add);
    }

    public void replacePrompts(Collection<String> promptIds) {
        this.promptIds.clear();
        attachPrompts(promptIds);
    }

    /**
     * 附加/去重 MCP 工具。
     */
    public void attachMcps(Collection<String> mcpIds) {
        if (mcpIds == null || mcpIds.isEmpty()) {
            return;
        }
        mcpIds.stream().filter(Objects::nonNull).forEach(this.mcpIds::add);
    }

    public void replaceMcps(Collection<String> mcpIds) {
        this.mcpIds.clear();
        attachMcps(mcpIds);
    }

    /**
     * 附加/去重 Advisor。
     */
    public void attachAdvisors(Collection<String> advisorIds) {
        if (advisorIds == null || advisorIds.isEmpty()) {
            return;
        }
        advisorIds.stream().filter(Objects::nonNull).forEach(this.advisorIds::add);
    }

    public void replaceAdvisors(Collection<String> advisorIds) {
        this.advisorIds.clear();
        attachAdvisors(advisorIds);
    }

    public List<String> getPromptIds() {
        return List.copyOf(promptIds);
    }

    public List<String> getMcpIds() {
        return List.copyOf(mcpIds);
    }

    public List<String> getAdvisorIds() {
        return List.copyOf(advisorIds);
    }
}
