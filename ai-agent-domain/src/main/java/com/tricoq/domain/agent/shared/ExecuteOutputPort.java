package com.tricoq.domain.agent.shared;

/**
 *
 *
 * @author trico qiang
 * @date 12/5/25
 */
@FunctionalInterface
public interface ExecuteOutputPort {

    void send(String json);

    default void complete(){}

    default void error(Throwable t){}
}
