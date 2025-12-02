package com.tricoq.infrastructure.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tricoq.infrastructure.dao.IAiClientDao;
import com.tricoq.infrastructure.dao.po.AiClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
* @author tricoqiang
* @description 针对表【ai_client(AI客户端配置表)】的数据库操作Service实现
* @createDate 2025-12-02 15:03:17
*/
@Service
public class AiClientService extends ServiceImpl<IAiClientDao, AiClient>{

    public List<AiClient> queryByClientIdEnabled(Set<String> clientIdSet) {
        return baseMapper.queryByClientIdEnabled(clientIdSet);
    }

    public AiClient queryByClientId(String s) {
        return baseMapper.queryByClientId(s);
    }
}




