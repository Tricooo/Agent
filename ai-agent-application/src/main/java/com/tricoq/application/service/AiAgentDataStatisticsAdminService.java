package com.tricoq.application.service;

import com.tricoq.api.dto.DataStatisticsResponseDTO;
import com.tricoq.domain.agent.adapter.repository.IDataStatisticsRepository;
import com.tricoq.domain.agent.model.dto.DataStatisticsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 数据统计应用服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiAgentDataStatisticsAdminService {

    private final IDataStatisticsRepository dataStatisticsRepository;

    public DataStatisticsResponseDTO getDataStatistics() {
        log.info("开始获取系统数据统计");
        DataStatisticsDTO snapshot = dataStatisticsRepository.loadStatistics();
        return DataStatisticsResponseDTO.builder()
                .activeAgentCount(snapshot.getAgentCount())
                .clientCount(snapshot.getClientCount())
                .mcpToolCount(snapshot.getMcpToolCount())
                .systemPromptCount(snapshot.getSystemPromptCount())
                .ragOrderCount(snapshot.getRagOrderCount())
                .advisorCount(snapshot.getAdvisorCount())
                .modelCount(snapshot.getModelCount())
                .todayRequestCount(0L)
                .successRate(95.5)
                .runningTaskCount(0L)
                .build();
    }
}
