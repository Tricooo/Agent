package com.tricoq.infrastructure.support;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tricoq.infrastructure.dao.IAiAgentDao;
import com.tricoq.infrastructure.dao.po.AiAgent;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* @author tricoqiang
* @description 针对表【ai_agent(AI智能体配置表)】的数据库操作Service实现
* @createDate 2025-12-02 11:21:34
*/
@Repository
public class AiAgentDaoSupport extends ServiceImpl<IAiAgentDao, AiAgent> {

    public AiAgent queryByAgentId(String agentId) {
        return this.baseMapper.queryByAgentId(agentId);
    }

    public List<AiAgent> queryEnabledAgents() {
        return this.baseMapper.queryEnabledAgents();
    }
}




