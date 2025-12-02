package com.tricoq.infrastructure.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tricoq.infrastructure.dao.IAiClientApiDao;
import com.tricoq.infrastructure.dao.po.AiClientApi;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
* @author tricoqiang
* @description 针对表【ai_client_api(OpenAI API配置表)】的数据库操作Service实现
* @createDate 2025-12-02 15:03:33
*/
@Service
public class AiClientApiService extends ServiceImpl<IAiClientApiDao, AiClientApi>{

    public List<AiClientApi> queryByApiIdsEnabled(Set<String> apiIdSet) {
        return baseMapper.queryByApiIdsEnabled(apiIdSet);
    }
}




