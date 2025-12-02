package com.tricoq.infrastructure.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tricoq.infrastructure.dao.IAiClientAdvisorDao;
import com.tricoq.infrastructure.dao.po.AiClientAdvisor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
* @author tricoqiang
* @description 针对表【ai_client_advisor(顾问配置表)】的数据库操作Service实现
* @createDate 2025-12-02 15:03:33
*/
@Service
public class AiClientAdvisorService extends ServiceImpl<IAiClientAdvisorDao, AiClientAdvisor>{

    public List<AiClientAdvisor> queryByAdvisorIdsEnabled(Set<String> advisorIds) {
        return baseMapper.queryByAdvisorIdsEnabled(advisorIds);
    }
}




