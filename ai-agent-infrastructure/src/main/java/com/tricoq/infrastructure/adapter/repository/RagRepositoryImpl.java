package com.tricoq.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tricoq.domain.agent.model.dto.AiRagOrderDTO;
import com.tricoq.domain.agent.adapter.repository.IRagRepository;
import com.tricoq.infrastructure.dao.IAiClientRagOrderDao;
import com.tricoq.infrastructure.dao.po.AiClientRagOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * @author trico qiang
 * @date 11/25/25
 */
@Repository
@RequiredArgsConstructor
public class RagRepositoryImpl extends ServiceImpl<IAiClientRagOrderDao, AiClientRagOrder> implements IRagRepository {

    private final IAiClientRagOrderDao aiClientRagOrderDao;

    @Override
    public void createTagOrder(AiRagOrderDTO aiRagOrderVO) {
        AiClientRagOrder aiRagOrder = new AiClientRagOrder();
        aiRagOrder.setRagName(aiRagOrderVO.getRagName());
        aiRagOrder.setKnowledgeTag(aiRagOrderVO.getKnowledgeTag());
        aiRagOrder.setStatus(1);
        aiClientRagOrderDao.insert(aiRagOrder);
    }
}
