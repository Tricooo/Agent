package com.tricoq.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author trico qiang
 * @date 10/24/25
 */
@Getter
@AllArgsConstructor
public enum AiAgentEnumVO {

    AI_CLIENT_API("对话API", "api",  "ai_client_api_data_list", "aiClientApiLoadDataStrategy"),
    AI_CLIENT_MODEL("对话模型", "model", "ai_client_model_data_list", "aiClientModelLoadDataStrategy"),
    AI_CLIENT_SYSTEM_PROMPT("提示词", "prompt",  "ai_client_system_prompt_data_list", "aiClientSystemPromptLoadDataStrategy"),
    AI_CLIENT_TOOL_MCP("mcp工具", "mcp", "ai_client_tool_mcp_data_list", "aiClientToolMCPLoadDataStrategy"),
    AI_CLIENT_ADVISOR("顾问角色", "advisor",  "ai_client_advisor_data_list", "aiClientAdvisorLoadDataStrategy"),
    AI_CLIENT("客户端", "client", "ai_client_data_list", "aiClientLoadDataStrategy"),
    ;

    private final String name;

    private final String code;

    private final String dataName;

    private final String loadDataStrategy;

    public static AiAgentEnumVO getByCode(String code) {
        if(StringUtils.isBlank(code)) {
            return null;
        }
        for (AiAgentEnumVO value : AiAgentEnumVO.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        throw new RuntimeException("code value " + code + " not exist!");
    }
}
