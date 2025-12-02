package com.tricoq.infrastructure.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tricoq.infrastructure.dao.IAiAgentDrawConfigDao;
import com.tricoq.infrastructure.dao.po.AiAgentDrawConfig;
import org.springframework.stereotype.Service;

/**
* @author tricoqiang
* @description 针对表【ai_agent_draw_config(AI智能体拖拉拽配置主表)】的数据库操作Service实现
* @createDate 2025-12-02 15:03:03
*/
@Service
public class AiAgentDrawConfigService extends ServiceImpl<IAiAgentDrawConfigDao, AiAgentDrawConfig> {

    public AiAgentDrawConfig queryByConfigId(String s) {
        return baseMapper.queryByConfigId(s);
    }
}




