package com.tricoq.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tricoq.domain.agent.adapter.repository.IAgentDrawConfigRepository;
import com.tricoq.domain.agent.model.aggregate.AiAgentDrawConfigAggregate;
import com.tricoq.domain.agent.model.entity.DrawConfigQueryCommandEntity;
import com.tricoq.infrastructure.dao.IAiAgentDrawConfigDao;
import com.tricoq.infrastructure.dao.po.AiAgentDrawConfig;
import com.tricoq.infrastructure.service.AiAgentDrawConfigService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 *
 * @author trico qiang
 * @date 11/26/25
 */
@Repository
@RequiredArgsConstructor
public class AgentDrawConfigRepositoryImpl
        extends MpAggregateRepository<AiAgentDrawConfigAggregate, AiAgentDrawConfig, String, Long, IAiAgentDrawConfigDao>
        implements IAgentDrawConfigRepository {

    private final AiAgentDrawConfigService drawConfigService;

    /**
     * 领域对象 -> 数据库实体
     *
     * @param aggregate
     */
    @Override
    protected AiAgentDrawConfig toPo(AiAgentDrawConfigAggregate aggregate) {
        if (aggregate == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        return AiAgentDrawConfig.builder().agentId(aggregate.getAgentId())
                .configId(aggregate.getConfigId())
                .configName(aggregate.getConfigName())
                .description(aggregate.getDescription())
                .configData(aggregate.getConfigData())
                .version(aggregate.getVersion())
                .status(aggregate.getStatus())
                .createTime(now)
                .updateTime(now)
                .build();
    }

    /**
     * 数据库实体 -> 领域对象
     *
     * @param data
     */
    @Override
    protected AiAgentDrawConfigAggregate toAggregate(AiAgentDrawConfig data) {
        if (data == null) {
            return null;
        }
        return AiAgentDrawConfigAggregate.builder()
                .agentId(data.getAgentId())
                .configId(data.getConfigId())
                .configName(data.getConfigName())
                .description(data.getDescription())
                .configData(data.getConfigData())
                .version(data.getVersion())
                .status(data.getStatus())
                .build();
    }

    /**
     * 从聚合根中提取业务 ID
     *
     * @param aggregate
     */
    @Override
    protected String toId(AiAgentDrawConfigAggregate aggregate) {
        return aggregate == null ? null : aggregate.getConfigId();
    }

    @Override
    protected Long toDbId(AiAgentDrawConfig data) {
        return data.getId();
    }

    @Override
    protected void fillDbId(AiAgentDrawConfig target, Long dbId) {
        target.setId(dbId);
    }

    @Override
    protected AiAgentDrawConfig getByAggregateId(String s) {
        return drawConfigService.queryByConfigId(s);
    }

    /**
     * 业务 ID -> 可供 MyBatis-Plus 使用的可序列化 ID
     *
     * @param id
     */
    @Override
    protected Serializable toSerializableId(String id) {
        return id;
    }

    @Override
    public List<AiAgentDrawConfigAggregate> queryByCondition(DrawConfigQueryCommandEntity query) {
        return this.list(Wrappers.<AiAgentDrawConfig>lambdaQuery()
                .eq(AiAgentDrawConfig::getStatus, 1)
                .eq(StringUtils.isNotBlank(query.getAgentId()), AiAgentDrawConfig::getAgentId, query.getAgentId())
                .like(StringUtils.isNotBlank(query.getConfigName()), AiAgentDrawConfig::getConfigName, query.getConfigName())
        ).stream().map(this::toAggregate).toList();
    }

    @Override
    public AiAgentDrawConfigAggregate queryByConfigId(String configId) {
        return toAggregate(drawConfigService.queryByConfigId(configId));
    }

    @Override
    public boolean removeByAggregateId(String configId) {
        return false;
    }
}
