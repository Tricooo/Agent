package com.tricoq.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tricoq.application.model.dto.DrawGraphDTO;
import com.tricoq.domain.agent.model.aggregate.AiAgentAggregate;
import com.tricoq.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.tricoq.domain.agent.model.aggregate.AiClientAggregate;
import com.tricoq.domain.agent.model.valobj.AiClientModelVO;
import com.tricoq.types.common.DrawConstants;
import com.tricoq.types.enums.ResponseCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 拖拽配置解析工具类
 * 用于解析前端传来的JSON配置数据，生成ai_client_config表的关系映射
 *
 * @author trico qiang
 * 2025/1/20 10:00
 */
@Slf4j
@Component
public class DrawConfigParser {

    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Map<String, DrawGraphDTO> DRAW_GRAPH_MAP = new ConcurrentHashMap<>();

    private final Map<String, Map<String, List<DrawGraphDTO.NodeDTO>>> DRAW_NODES_MAP = new ConcurrentHashMap<>();

    public DrawGraphDTO transformDrawGraph(String agentId, String drawConfig) {
        try {
            DrawGraphDTO drawGraphDTO = OBJECT_MAPPER.readValue(drawConfig, DrawGraphDTO.class);
            if (null == drawGraphDTO) {
                throw new IllegalArgumentException();
            }
            DRAW_GRAPH_MAP.put(agentId, drawGraphDTO);
            List<DrawGraphDTO.NodeDTO> nodes = drawGraphDTO.getNodes();
            if (CollectionUtils.isEmpty(nodes)) {
                throw new IllegalArgumentException(ResponseCode.ILLEGAL_PARAMETER.getCode());
            }
            Map<String, List<DrawGraphDTO.NodeDTO>> type2NodeMap = nodes.stream()
                    .collect(Collectors.groupingBy(DrawGraphDTO.NodeDTO::type));
            DRAW_NODES_MAP.put(agentId, type2NodeMap);
            return drawGraphDTO;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<AiClientAggregate> buildClientConfig(String agentId, String configId, String configData) {
        Map<String, List<DrawGraphDTO.NodeDTO>> type2NodeMap = getNodeMap(agentId, configData);
        //分类node集合
        List<DrawGraphDTO.NodeDTO> clientNodes = type2NodeMap.getOrDefault(DrawConstants.NodeTypeConstants.CLIENT, Collections.emptyList());
        List<DrawGraphDTO.NodeDTO> mcpNodes = type2NodeMap.getOrDefault(DrawConstants.NodeTypeConstants.TOOL_MCP, Collections.emptyList());
        List<DrawGraphDTO.NodeDTO> advisorNodes = type2NodeMap.getOrDefault(DrawConstants.NodeTypeConstants.ADVISOR, Collections.emptyList());
        List<DrawGraphDTO.NodeDTO> modelNodes = type2NodeMap.getOrDefault(DrawConstants.NodeTypeConstants.MODEL, Collections.emptyList());
        List<DrawGraphDTO.NodeDTO> promptNodes = type2NodeMap.getOrDefault(DrawConstants.NodeTypeConstants.PROMPT, Collections.emptyList());
        Map<String, DrawGraphDTO.NodeDTO> exceptClientNodeMap = Stream.of(mcpNodes, advisorNodes, modelNodes, promptNodes)
                .flatMap(List::stream)
                .collect(Collectors.toMap(DrawGraphDTO.NodeDTO::id, Function.identity()));
        DrawGraphDTO dto = DRAW_GRAPH_MAP.get(agentId);
        List<DrawGraphDTO.EdgeDTO> edges = dto.getEdges();
        Map<String, List<DrawGraphDTO.EdgeDTO>> fromMap = edges.stream()
                .filter(edge -> Objects.nonNull(edge.fromPort()))
                .collect(Collectors.groupingBy(DrawGraphDTO.EdgeDTO::from));

        return clientNodes.stream().map(clientNode -> {
            String clientNodeId = clientNode.id();
            Map<String, List<String>> type2NodeIds = extractType2IdsMap(fromMap, clientNodeId, exceptClientNodeMap);

            List<String> modelIds = type2NodeIds.get(DrawConstants.NodeTypeConstants.MODEL);
            if (modelIds == null || modelIds.size() != 1) {
                throw new IllegalArgumentException(ResponseCode.ILLEGAL_PARAMETER.getCode());
            }
            String modelId = modelIds.get(0);
            Map<String, List<String>> model2NodeIds = extractType2IdsMap(fromMap, modelId, exceptClientNodeMap);

            Map<String, String> inputsValues = clientNode.inputsValues();
            String clientId = Optional.ofNullable(inputsValues.get("clientId"))
                    .orElseThrow(() -> new IllegalArgumentException(ResponseCode.ILLEGAL_PARAMETER.getCode()));
            return AiClientAggregate.restore(clientId, null, null,
                    AiClientModelVO.builder()
                            .modelId(modelId)
                            .toolMcpIds(model2NodeIds.get(DrawConstants.NodeTypeConstants.TOOL_MCP))
                            .build(),
                    configId,
                    type2NodeIds.get(DrawConstants.NodeTypeConstants.PROMPT),
                    type2NodeIds.get(DrawConstants.NodeTypeConstants.TOOL_MCP),
                    type2NodeIds.get(DrawConstants.NodeTypeConstants.ADVISOR)
            );
        }).toList();
    }

    private Map<String, List<String>> extractType2IdsMap(Map<String, List<DrawGraphDTO.EdgeDTO>> fromMap,
                                                         String clientNodeId,
                                                         Map<String, DrawGraphDTO.NodeDTO> exceptClientNodeMap) {
        List<DrawGraphDTO.EdgeDTO> clientLines = fromMap.get(clientNodeId);
        return clientLines.stream()
                .map(line -> exceptClientNodeMap.get(line.to()))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(DrawGraphDTO.NodeDTO::type,
                        Collectors.mapping(DrawGraphDTO.NodeDTO::extractNodeIds, Collectors.toList())));
    }

    public AiAgentAggregate buildAgent(String agentId, String configData) {
        Map<String, List<DrawGraphDTO.NodeDTO>> type2NodeMap = getNodeMap(agentId, configData);
        List<DrawGraphDTO.NodeDTO> agentNode = type2NodeMap.get(DrawConstants.NodeTypeConstants.AGENT);
        return parseAgentConfig(agentNode.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("不存在agent节点")), agentId);
    }

    private Map<String, List<DrawGraphDTO.NodeDTO>> getNodeMap(String agentId, String configData) {
        Map<String, List<DrawGraphDTO.NodeDTO>> type2NodeMap = DRAW_NODES_MAP.get(agentId);
        if (MapUtils.isEmpty(type2NodeMap)) {
            DrawGraphDTO drawGraph = DRAW_GRAPH_MAP.get(agentId);
            if (null == drawGraph) {
                drawGraph = transformDrawGraph(agentId, configData);
            }
            List<DrawGraphDTO.NodeDTO> nodes = drawGraph.getNodes();
            type2NodeMap = nodes.stream()
                    .collect(Collectors.groupingBy(DrawGraphDTO.NodeDTO::type));
        }
        return type2NodeMap;
    }

    private AiAgentAggregate parseAgentConfig(DrawGraphDTO.NodeDTO agentNode, String agentId) {
        Map<String, String> inputsValues = agentNode.inputsValues();
        if (MapUtils.isEmpty(inputsValues)) {
            throw new IllegalArgumentException();
        }
        String agentName = inputsValues.get(DrawConstants.AgentConstants.AGENT_NAME);
        String description = inputsValues.get(DrawConstants.AgentConstants.DESCRIPTION);
        String channel = inputsValues.get(DrawConstants.AgentConstants.CHANNEL);
        String strategy = inputsValues.get(DrawConstants.AgentConstants.STRATEGY);
        return AiAgentAggregate.create(agentId, agentName, description, channel, strategy, null);
    }

    public List<AiAgentClientFlowConfigVO> buildClientFlowConfig(String agentId, String configExecData) {
        Map<String, List<DrawGraphDTO.NodeDTO>> type2NodeMap = getNodeMap(agentId, configExecData);
        //分类node集合
        List<DrawGraphDTO.NodeDTO> clientNodes = type2NodeMap.get(DrawConstants.NodeTypeConstants.CLIENT);
        return clientNodes.stream()
                .map(node -> toFlowConfig(agentId, node.inputsValues()))
                .toList();
    }

    private AiAgentClientFlowConfigVO toFlowConfig(String agentId, Map<String, String> inputsValues) {
        if (MapUtils.isEmpty(inputsValues)) {
            throw new IllegalArgumentException("客户端节点参数为空");
        }
        Map<String, String> data = new HashMap<>(inputsValues);
        data.put("agentId", agentId);
        requireNonBlank(data, "clientId", "sequence");

        AiAgentClientFlowConfigVO vo = OBJECT_MAPPER.convertValue(data, AiAgentClientFlowConfigVO.class);
        if (vo.getSequence() == null) {
            throw new IllegalArgumentException("sequence 非法: " + data.get("sequence"));
        }
        return vo;
    }

    private void requireNonBlank(Map<String, String> data, String... keys) {
        for (String key : keys) {
            if (!StringUtils.isNotBlank(data.get(key))) {
                throw new IllegalArgumentException(key + " 不能为空");
            }
        }
    }

    /**
     * 节点信息内部类
     */
    @Data
    private static class NodeInfo {
        private String nodeId;
        private String nodeType;
        private String title;
        private String refId;
    }
}
