package com.tricoq.domain.agent.service;

import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 *
 *
 * @author trico qiang
 * @date 11/4/25
 */
public interface IExecuteStrategy {

    void execute(ExecuteCommandEntity commandEntity, ResponseBodyEmitter emitter);
}
