package com.tricoq.infrastructure.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tricoq.infrastructure.dao.IAiClientSystemPromptDao;
import com.tricoq.infrastructure.dao.po.AiClientSystemPrompt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
* @author tricoqiang
* @description 针对表【ai_client_system_prompt(系统提示词配置表)】的数据库操作Service实现
* @createDate 2025-12-02 15:03:33
*/
@Service
public class AiClientSystemPromptService extends ServiceImpl<IAiClientSystemPromptDao, AiClientSystemPrompt>{

    public List<AiClientSystemPrompt> queryByIdsPromptsEnabled(Set<String> systemPromptIds) {
        return baseMapper.queryByIdsPromptsEnabled(systemPromptIds);
    }
}




