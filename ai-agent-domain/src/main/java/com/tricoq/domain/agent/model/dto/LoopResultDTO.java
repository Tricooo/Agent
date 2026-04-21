package com.tricoq.domain.agent.model.dto;

import com.tricoq.domain.agent.service.execute.auto.context.AutoTerminationReason;
import lombok.Builder;
import lombok.Data;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/21/26
 */
@Data
@Builder
public class LoopResultDTO {

    private boolean completed;

    private AutoTerminationReason terminationReason;

    private int actualSteps;
}
