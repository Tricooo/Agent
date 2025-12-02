package com.tricoq.domain.agent.adapter.repository;

import java.util.Collection;

/**
 * 聚合根通用仓储接口，屏蔽基础设施细节（ MyBatis-Plus IService），保持领域层纯净。
 *
 * @author trico qiang
 * @date 11/26/25
 */
public interface IAggregateRepository<A, ID> {

    /**
     * 根据聚合根ID查询
     *
     * @param id 聚合根ID
     * @return 聚合根
     */
    A findByAggregateId(ID id);

    /**
     * 根据聚合根ID更新/新增
     *
     * @param aggregate 聚合根
     * @return 聚合根
     */
    boolean saveOrUpdateByAggregateId(A aggregate);

    /**
     * 保存单个聚合根
     *
     * @param aggregate 聚合根
     */
    boolean saveAggregate(A aggregate);

    /**
     * 批量保存聚合根
     *
     * @param aggregates 聚合根集合
     */
    void saveAllAggregates(Collection<A> aggregates);
}
