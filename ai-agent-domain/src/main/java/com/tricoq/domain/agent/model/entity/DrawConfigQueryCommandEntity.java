package com.tricoq.domain.agent.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 绘图配置查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrawConfigQueryCommandEntity {

    private String configId;

    private String configName;

    private String agentId;

    private Integer status;
}
