package com.tricoq.infrastructure.adapter.repository;

import com.tricoq.domain.agent.adapter.repository.IDataStatisticsRepository;
import com.tricoq.domain.agent.model.dto.DataStatisticsDTO;
import com.tricoq.infrastructure.dao.IAiAgentDao;
import com.tricoq.infrastructure.dao.IAiClientAdvisorDao;
import com.tricoq.infrastructure.dao.IAiClientDao;
import com.tricoq.infrastructure.dao.IAiClientModelDao;
import com.tricoq.infrastructure.dao.IAiClientRagOrderDao;
import com.tricoq.infrastructure.dao.IAiClientSystemPromptDao;
import com.tricoq.infrastructure.dao.IAiClientToolMcpDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 数据统计仓储实现
 */
@Repository
@RequiredArgsConstructor
public class DataStatisticsRepositoryImpl implements IDataStatisticsRepository {

    private final IAiAgentDao aiAgentDao;
    private final IAiClientDao aiClientDao;
    private final IAiClientToolMcpDao aiClientToolMcpDao;
    private final IAiClientSystemPromptDao aiClientSystemPromptDao;
    private final IAiClientRagOrderDao aiClientRagOrderDao;
    private final IAiClientAdvisorDao aiClientAdvisorDao;
    private final IAiClientModelDao aiClientModelDao;

    @Override
    public DataStatisticsDTO loadStatistics() {
        return DataStatisticsDTO.builder()
                .agentCount(aiAgentDao.queryAll().size())
                .clientCount(aiClientDao.queryAll().size())
                .mcpToolCount(aiClientToolMcpDao.queryAll().size())
                .systemPromptCount(aiClientSystemPromptDao.queryAll().size())
                .ragOrderCount(aiClientRagOrderDao.queryAll().size())
                .advisorCount(aiClientAdvisorDao.queryAll().size())
                .modelCount(aiClientModelDao.queryAll().size())
                .build();
    }
}
