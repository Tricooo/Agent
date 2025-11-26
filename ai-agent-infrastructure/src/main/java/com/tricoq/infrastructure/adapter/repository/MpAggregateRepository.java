package com.tricoq.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tricoq.domain.agent.adapter.repository.IAggregateRepository;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * MyBatis-Plus 与领域仓储的适配基类，保持领域层与 ORM 解耦。
 *
 * @param <A>  聚合根/领域对象
 * @param <PO> 数据库实体
 * @param <ID> 聚合根ID
 * @param <M>  对应的 Mapper
 * @author trico qiang
 * @date 11/25/25
 */
public abstract class MpAggregateRepository<A, PO, ID, M extends BaseMapper<PO>>
        extends ServiceImpl<M, PO> implements IAggregateRepository<A, ID> {

    /**
     * 领域对象 -> 数据库实体
     */
    protected abstract PO toPo(A aggregate);

    /**
     * 数据库实体 -> 领域对象
     */
    protected abstract A toAggregate(PO data);

    /**
     * 从聚合根中提取业务 ID
     */
    protected abstract ID toId(A aggregate);

    /**
     * 业务 ID -> 可供 MyBatis-Plus 使用的可序列化 ID
     */
    protected abstract Serializable toSerializableId(ID id);

    @Override
    public A findById(ID id) {
        if (id == null) {
            return null;
        }
        PO data = getById(toSerializableId(id));
        return data == null ? null : toAggregate(data);
    }

    @Override
    public boolean saveAggregate(A aggregate) {
        if (aggregate == null) {
            return false;
        }
        return super.save(toPo(aggregate));
    }

    @Override
    public void saveAllAggregates(Collection<A> aggregates) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }
        List<PO> list = aggregates.stream()
                .map(this::toPo)
                .filter(Objects::nonNull)
                .toList();
        if (list.isEmpty()) {
            return;
        }
        super.saveBatch(list);
    }
}
