package com.tricoq.domain.agent.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author trico qiang
 * @date 11/3/25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteCommandEntity {

    private String userInput;

    private String agentId;

    private Integer maxSteps;

    private String sessionId;
}
