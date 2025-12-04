package com.tricoq.domain.agent.service;

import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 *
 *
 * @author trico qiang
 * @date 11/13/25
 */
public interface IAgentDispatchService {
    //这样不合理，领域层不应该和http强绑定，不应该知道这些技术细节
    void dispatch(ExecuteCommandEntity commandEntity, ResponseBodyEmitter emitter);
}
