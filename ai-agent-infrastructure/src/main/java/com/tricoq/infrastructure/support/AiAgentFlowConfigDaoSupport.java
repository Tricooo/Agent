package com.tricoq.infrastructure.support;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tricoq.infrastructure.dao.IAiAgentFlowConfigDao;
import com.tricoq.infrastructure.dao.po.AiAgentFlowConfig;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* @author tricoqiang
* @description 针对表【ai_agent_flow_config(智能体-客户端关联表)】的数据库操作Service实现
* @createDate 2025-12-02 11:31:03
*/
@Repository
public class AiAgentFlowConfigDaoSupport extends ServiceImpl<IAiAgentFlowConfigDao, AiAgentFlowConfig> {

    public List<AiAgentFlowConfig> queryByAgentId(String agentId) {
        return this.baseMapper.queryByAgentId(agentId);
    }
}




