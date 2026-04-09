package com.tricoq.domain.agent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 单个可调用工具的语义描述，避免 domain 直接暴露 MCP SDK 类型。
 *
 * @author trico qiang
 * @date 4/9/26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ToolSpecDTO {

    /**
     * tool 的唯一名称，调用时使用
     */
    private String toolName;

    /**
     * tool 的描述信息
     */
    private String description;

    /**
     * 入参 schema 的顶层类型，通常为 object
     */
    private String inputSchemaType;

    /**
     * 工具可接收的参数定义
     */
    private Map<String, Object> inputProperties;

    /**
     * 必填参数名
     */
    private List<String> requiredArgs;

    /**
     * 非必填参数名
     */
    private List<String> optionalArgs;

    /**
     * 是否允许 additionalProperties
     */
    private Boolean additionalProperties;
}
