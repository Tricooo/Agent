package com.tricoq.types.common;

import java.util.List;

/**
 * 拖拽画布常量
 *
 * @author trico qiang
 * @date 11/24/25
 */
public interface DrawConstants {

    interface AgentConstants {
        String AGENT_NAME = "agentName";
        String DESCRIPTION = "description";
        String CHANNEL = "channel";
        String STRATEGY = "strategy";
    }

    interface NodeTypeConstants {
        String AGENT = "agent";
        String CLIENT = "client";
        String ADVISOR = "advisor";
        String TOOL_MCP = "tool_mcp";
        String PROMPT = "prompt";
        String MODEL = "model";
        List<String> TYPES = List.of(AGENT, CLIENT, ADVISOR, TOOL_MCP, MODEL, PROMPT);
    }
}
