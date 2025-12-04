package com.tricoq.infrastructure.adapter.repository;

import com.tricoq.domain.agent.adapter.repository.IDataStatisticsRepository;
import com.tricoq.domain.agent.model.dto.DataStatisticsDTO;
import com.tricoq.infrastructure.support.AiAgentDaoSupport;
import com.tricoq.infrastructure.support.AiClientAdvisorDaoSupport;
import com.tricoq.infrastructure.support.AiClientDaoSupport;
import com.tricoq.infrastructure.support.AiClientModelDaoSupport;
import com.tricoq.infrastructure.support.AiClientRagOrderDaoSupport;
import com.tricoq.infrastructure.support.AiClientSystemPromptDaoSupport;
import com.tricoq.infrastructure.support.AiClientToolMcpDaoSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 数据统计仓储实现
 * @author trico qiang
 */
@Repository
@RequiredArgsConstructor
public class DataStatisticsRepositoryImpl implements IDataStatisticsRepository {

    private final AiAgentDaoSupport agentDaoSupport;
    private final AiClientDaoSupport clientDaoSupport;
    private final AiClientToolMcpDaoSupport toolMcpDaoSupport;
    private final AiClientSystemPromptDaoSupport systemPromptDaoSupport;
    private final AiClientRagOrderDaoSupport ragOrderDaoSupport;
    private final AiClientAdvisorDaoSupport advisorDaoSupport;
    private final AiClientModelDaoSupport modelDaoSupport;

    @Override
    public DataStatisticsDTO loadStatistics() {
        return DataStatisticsDTO.builder()
                .agentCount(agentDaoSupport.list().size())
                .clientCount(clientDaoSupport.list().size())
                .mcpToolCount(toolMcpDaoSupport.list().size())
                .systemPromptCount(systemPromptDaoSupport.list().size())
                .ragOrderCount(ragOrderDaoSupport.list().size())
                .advisorCount(advisorDaoSupport.list().size())
                .modelCount(modelDaoSupport.list().size())
                .build();
    }
}
