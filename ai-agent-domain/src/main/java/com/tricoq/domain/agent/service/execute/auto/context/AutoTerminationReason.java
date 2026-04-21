package com.tricoq.domain.agent.service.execute.auto.context;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/21/26
 */
public enum AutoTerminationReason {
    //业务填充
    COMPLETED_BY_ANALYZE,
    COMPLETED_BY_SUPERVISION,

    //流程填充
    STOPPED_BY_MAX_STEP
}
