package com.tricoq.domain.agent.service;

import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.shared.ExecuteOutputPort;

/**
 *
 *
 * @author trico qiang
 * @date 11/4/25
 */
public interface IExecuteStrategy {

    void execute(ExecuteCommandEntity commandEntity, ExecuteOutputPort port);
}
