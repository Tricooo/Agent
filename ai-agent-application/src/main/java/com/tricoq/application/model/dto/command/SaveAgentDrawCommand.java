package com.tricoq.application.model.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author trico qiang
 * @date 11/25/25
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveAgentDrawCommand {

    /**
     * 配置ID（唯一标识）
     */
    private String configId;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 关联的智能体ID
     */
    private String agentId;

    /**
     * 完整的拖拉拽配置JSON数据（包含nodes和edges）
     */
    private String configData;

    /**
     * 前端将 configData 扁平后易于后端处理的版本
     */
    private String configExecData;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;
}
