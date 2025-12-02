package com.tricoq.domain.agent.adapter.repository;

import com.tricoq.domain.agent.model.dto.DataStatisticsDTO;

/**
 * 数据统计仓储接口
 */
public interface IDataStatisticsRepository {

    DataStatisticsDTO loadStatistics();
}
