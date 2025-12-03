package com.tricoq.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 智能体-客户端关联表
 * @author trico qiang
 * @description 智能体-客户端关联表 PO 对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("ai_agent_flow_config")
public class AiAgentFlowConfig {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 智能体ID
     */
    private String agentId;

    /**
     * 客户端ID
     */
    private String clientId;

    private String clientName;

    private String clientType;

    /**
     * 序列号(执行顺序)
     */
    private Integer sequence;

    private String stepPrompt;

    private int status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
