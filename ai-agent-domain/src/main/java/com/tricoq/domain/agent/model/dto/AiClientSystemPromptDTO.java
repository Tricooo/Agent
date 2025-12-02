package com.tricoq.domain.agent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 提示词&动态规划，值对象
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/6/27 18:45
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientSystemPromptDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 提示词ID
     */
    private String promptId;

    /**
     * 提示词名称
     */
    private String promptName;

    /**
     * 提示词内容
     */
    private String promptContent;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
