package com.tricoq.domain.agent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI客户端配置，值对象
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/6/27 18:51
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientDTO {

    /**
     * 数据库主键
     */
    private Long id;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 全局唯一模型ID
     */
    private String modelId;

    /**
     * Prompt ID List
     */
    private List<String> promptIdList;

    /**
     * MCP ID List
     */
    private List<String> mcpIdList;

    /**
     * 顾问ID List
     */
    private List<String> advisorIdList;

}
