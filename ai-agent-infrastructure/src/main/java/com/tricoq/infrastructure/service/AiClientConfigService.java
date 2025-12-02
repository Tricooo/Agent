package com.tricoq.infrastructure.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tricoq.infrastructure.dao.IAiClientConfigDao;
import com.tricoq.infrastructure.dao.po.AiClientConfig;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
* @author tricoqiang
* @description 针对表【ai_client_config(AI客户端统一关联配置表)】的数据库操作Service实现
* @createDate 2025-12-02 15:03:33
*/
@Service
public class AiClientConfigService extends ServiceImpl<IAiClientConfigDao, AiClientConfig>{

    public List<AiClientConfig> queryBySourceTypeAndIdsEnabled(String code, Set<String> clientIdSet) {
        return baseMapper.queryBySourceTypeAndIdsEnabled(code, clientIdSet);
    }
}




