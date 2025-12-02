package com.tricoq.domain.agent.model.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 数据统计快照
 */
@Data
@Builder
public class DataStatisticsDTO {

    private long agentCount;

    private long clientCount;

    private long mcpToolCount;

    private long systemPromptCount;

    private long ragOrderCount;

    private long advisorCount;

    private long modelCount;
}
