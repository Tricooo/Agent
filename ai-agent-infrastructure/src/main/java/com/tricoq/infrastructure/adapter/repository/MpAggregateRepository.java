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
 * MyBatis-Plus 与领域仓储的适配基类，保持领域层与 ORM 解耦。这里相当于主表的MP增强层
 *
 * @param <A>  聚合根/领域对象
 * @param <PO> 数据库实体 主表
 * @param <ID> 聚合根ID
 * @param <M>  对应的 Mapper
 * @author trico qiang
 * @date 11/25/25
 */
public abstract class MpAggregateRepository<A, PO, ID, DB, M extends BaseMapper<PO>>
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
     * 从数据库实体中提取数据库 ID
     */
    protected abstract DB toDbId(PO data);

    /**
     * 将数据库 ID 回填到待持久化的 PO 中（用于更新场景）。
     */
    protected abstract void fillDbId(PO target, DB dbId);

    /**
     * 通过聚合根id查询数据库
     */
    protected abstract PO getByAggregateId(ID id);

    /**
     * 业务 ID -> 可供 MyBatis-Plus 使用的可序列化 ID
     */
    protected abstract Serializable toSerializableId(ID id);

    @Override
    public A findByAggregateId(ID id) {
        if (id == null) {
            return null;
        }
        // 按业务 ID 查询，而不是按数据库自增主键
        PO data = getByAggregateId(id);
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

    /**
     * 根据聚合根ID更新/新增
     *
     * @param aggregate 聚合根
     * @return 聚合根
     */
    @Override
    public boolean saveOrUpdateByAggregateId(A aggregate) {
        if (aggregate == null) {
            return false;
        }
        ID aggId = toId(aggregate);
        PO existing = getByAggregateId(aggId);

        PO po = toPo(aggregate);
        if (existing == null) {
            return super.save(po);
        }
        // 回填数据库主键，走更新
        fillDbId(po, toDbId(existing));
        return super.updateById(po);
    }
}
