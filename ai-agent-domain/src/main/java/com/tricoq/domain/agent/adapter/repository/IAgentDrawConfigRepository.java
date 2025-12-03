package com.tricoq.domain.agent.adapter.repository;

import com.tricoq.domain.agent.model.aggregate.AiAgentDrawConfigAggregate;
import com.tricoq.domain.agent.model.entity.DrawConfigQueryCommandEntity;

import java.util.List;

/**
 *
 *
 * @author trico qiang
 * @date 11/26/25
 */
public interface IAgentDrawConfigRepository extends IAggregateRepository<AiAgentDrawConfigAggregate, String> {

    /**
     * 按条件查询配置列表
     */
    List<AiAgentDrawConfigAggregate> queryByCondition(DrawConfigQueryCommandEntity query);

    AiAgentDrawConfigAggregate queryByConfigId(String configId);

    boolean removeByAggregateId(String configId);
}
