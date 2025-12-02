package com.tricoq.infrastructure.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tricoq.infrastructure.dao.IAiClientModelDao;
import com.tricoq.infrastructure.dao.po.AiClientModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
* @author tricoqiang
* @description 针对表【ai_client_model(聊天模型配置表)】的数据库操作Service实现
* @createDate 2025-12-02 15:03:33
*/
@Service
public class AiClientModelService extends ServiceImpl<IAiClientModelDao, AiClientModel>{

    public List<AiClientModel> queryByIds(Set<String> modelIds) {
        return baseMapper.queryByIds(modelIds);
    }
}




