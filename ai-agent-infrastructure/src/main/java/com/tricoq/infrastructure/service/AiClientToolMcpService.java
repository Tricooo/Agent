package com.tricoq.infrastructure.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tricoq.infrastructure.dao.IAiClientToolMcpDao;
import com.tricoq.infrastructure.dao.po.AiClientToolMcp;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
* @author tricoqiang
* @description 针对表【ai_client_tool_mcp(MCP客户端配置表)】的数据库操作Service实现
* @createDate 2025-12-02 15:03:33
*/
@Service
public class AiClientToolMcpService extends ServiceImpl<IAiClientToolMcpDao, AiClientToolMcp>{

    public List<AiClientToolMcp> queryByMcpIdsEnabled(Set<String> mcpIdSet) {
        return baseMapper.queryByMcpIdsEnabled(mcpIdSet);
    }
}




