package com.tricoq.domain.agent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天模型配置，值对象
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/6/27 17:43
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientModelDTO {

    /**
     * 自增主键ID
     */
    private Long id;

    /**
     * 全局唯一模型ID
     */
    private String modelId;

    /**
     * 关联的API配置ID
     */
    private String apiId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型类型：openai、deepseek、claude
     */
    private String modelType;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 工具 mcp ids
     */
    private List<String> toolMcpIds;
}
