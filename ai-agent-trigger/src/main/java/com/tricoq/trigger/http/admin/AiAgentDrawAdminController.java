package com.tricoq.trigger.http.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tricoq.api.IAiAgentDrawAdminService;
import com.tricoq.api.dto.AiAgentDrawConfigRequestDTO;
import com.tricoq.api.dto.AiAgentDrawConfigResponseDTO;
import com.tricoq.api.response.Response;
import com.tricoq.application.model.dto.command.SaveAgentDrawCommand;
import com.tricoq.application.service.AiAgentDrawAdminService;
import com.tricoq.types.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 拖拉拽
 *
 * @author trico qiang
 * 2025/9/28 07:35
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/ai-agent-draw")
@CrossOrigin(origins = "*", allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequiredArgsConstructor
public class AiAgentDrawAdminController implements IAiAgentDrawAdminService {

    private final AiAgentDrawAdminService aiAgentDrawAdminService;

    /**
     * 保存draw config
     *
     * @param request 配置请求参数
     * @return 响应
     */
    @Override
    @PostMapping("/save-config")
    @Transactional(rollbackFor = Exception.class)
    public Response<String> saveDrawConfig(@RequestBody AiAgentDrawConfigRequestDTO request) {

        SaveAgentDrawCommand command = SaveAgentDrawCommand.builder().build();

//        aiAgentDrawAdminService.saveDrawConfig(command);

        return null;
    }

//    /**
//     * 解析JSON配置数据中的agent信息
//     *
//     * @param configData JSON配置数据
//     * @return agent信息数组 [agentName, channel]
//     */
//    private String[] parseAgentInfoFromJson(String configData) {
//        // 默认值
//        String[] agentInfo = new String[]{"", "", "", ""};
//
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode rootNode = objectMapper.readTree(configData);
//            JsonNode nodesArray = rootNode.get("nodes");
//
//            if (nodesArray != null && nodesArray.isArray()) {
//                for (JsonNode node : nodesArray) {
//                    String nodeType = node.get("type").asText();
//
//                    // 只处理type为"agent"的节点
//                    if ("agent".equals(nodeType)) {
//                        JsonNode dataNode = node.get("data");
//                        if (dataNode != null) {
//                            JsonNode inputsValuesNode = dataNode.get("inputsValues");
//                            if (inputsValuesNode != null) {
//                                log.debug("开始解析agent节点的inputsValues: {}", inputsValuesNode.toString());
//
//                                // 提取agent信息
//                                String agentName = extractValueFromInputs(inputsValuesNode, "agentName");
//                                String description = extractValueFromInputs(inputsValuesNode, "description");
//                                String channel = extractValueFromInputs(inputsValuesNode, "channel");
//                                String strategy = extractValueFromInputs(inputsValuesNode, "strategy");
//
//                                agentInfo[0] = agentName != null ? agentName : "";
//                                agentInfo[1] = description != null ? description : "";
//                                agentInfo[2] = channel != null ? channel : "";
//                                agentInfo[3] = strategy != null ? strategy : "";
//
//                                log.info("解析到agent信息: agentName={}, description={}, channel={}, strategy={}",
//                                        agentName, description, channel, strategy);
//                                break; // 找到第一个agent节点就退出
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("解析JSON配置数据中的agent信息失败", e);
//            // 返回默认值，不抛出异常，避免影响整个保存流程
//        }
//
//        return agentInfo;
//    }
//
//    /**
//     * 解析JSON配置数据中的client信息
//     *
//     * @param configData JSON配置数据
//     * @param agentId    智能体ID
//     * @return agent-client关系配置列表
//     */
//    private List<AiAgentFlowConfig> parseClientInfoFromJson(String configData, String agentId) {
//        List<AiAgentFlowConfig> agentFlowConfigs = new ArrayList<>();
//
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode rootNode = objectMapper.readTree(configData);
//            JsonNode nodesArray = rootNode.get("nodes");
//
//            if (nodesArray != null && nodesArray.isArray()) {
//                for (JsonNode node : nodesArray) {
//                    String nodeType = node.get("type").asText();
//
//                    // 只处理type为"client"的节点
//                    if ("client".equals(nodeType)) {
//                        JsonNode dataNode = node.get("data");
//                        if (dataNode != null) {
//                            JsonNode inputsValuesNode = dataNode.get("inputsValues");
//                            if (inputsValuesNode != null) {
//                                // 提取client信息
//                                String clientType = extractValueFromInputs(inputsValuesNode, "clientType");
//                                String clientId = extractValueFromInputs(inputsValuesNode, "clientId");
//                                String clientName = extractValueFromInputs(inputsValuesNode, "clientName");
//                                Integer sequence = extractIntegerValueFromInputs(inputsValuesNode, "sequence");
//                                String stepPrompt = extractValueFromInputs(inputsValuesNode, "stepPrompt");
//
//                                // 创建AiAgentFlowConfig对象
//                                AiAgentFlowConfig flowConfig = AiAgentFlowConfig.builder()
//                                        .agentId(agentId)
//                                        .clientId(clientId)
//                                        .clientName(clientName)
//                                        .clientType(clientType)
//                                        .sequence(sequence)
//                                        .stepPrompt(stepPrompt)
//                                        .createTime(LocalDateTime.now())
//                                        .build();
//
//                                agentFlowConfigs.add(flowConfig);
//                                log.info("解析到client信息: clientType={}, clientName={}, sequence={}",
//                                        clientType, clientName, sequence);
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("解析JSON配置数据失败", e);
//            throw new RuntimeException("解析JSON配置数据失败", e);
//        }
//
//        return agentFlowConfigs;
//    }
//
//    /**
//     * 从inputsValues中提取字符串值
//     *
//     * @param inputsValuesNode inputsValues节点
//     * @param fieldName        字段名
//     * @return 字段值
//     */
//    private String extractValueFromInputs(JsonNode inputsValuesNode, String fieldName) {
//        JsonNode fieldNode = inputsValuesNode.get(fieldName);
//        log.debug("提取字段 '{}': fieldNode={}", fieldName, fieldNode != null ? fieldNode.toString() : "null");
//
//        if (fieldNode != null) {
//            // 处理数组格式：[{"key": "xxx", "value": "yyy"}] 或 [{"key": "xxx", "value": {"content": "yyy"}}]
//            if (fieldNode.isArray() && !fieldNode.isEmpty()) {
//                JsonNode firstItem = fieldNode.get(0);
//                if (firstItem != null) {
//                    JsonNode valueNode = firstItem.get("value");
//                    log.debug("字段 '{}' 数组格式，valueNode={}", fieldName, valueNode != null ? valueNode.toString() : "null");
//
//                    if (valueNode != null) {
//                        // 如果value是对象，尝试获取content字段
//                        if (valueNode.isObject()) {
//                            JsonNode contentNode = valueNode.get("content");
//                            if (contentNode != null) {
//                                String result = contentNode.asText();
//                                log.debug("字段 '{}' 从content获取值: {}", fieldName, result);
//                                return result;
//                            }
//                        }
//                        // 如果value是字符串，直接返回
//                        else if (valueNode.isTextual()) {
//                            String result = valueNode.asText();
//                            log.debug("字段 '{}' 直接获取字符串值: {}", fieldName, result);
//                            return result;
//                        }
//                        // 如果value是数字，转换为字符串
//                        else if (valueNode.isNumber()) {
//                            String result = valueNode.asText();
//                            log.debug("字段 '{}' 数字转字符串值: {}", fieldName, result);
//                            return result;
//                        }
//                    }
//                }
//            }
//            // 处理直接字符串格式："fieldName": "value"
//            else if (fieldNode.isTextual()) {
//                String result = fieldNode.asText();
//                log.debug("字段 '{}' 直接字符串格式值: {}", fieldName, result);
//                return result;
//            }
//        }
//
//        log.debug("字段 '{}' 未找到有效值", fieldName);
//        return null;
//    }
//
//    /**
//     * 从inputsValues中提取整数值
//     *
//     * @param inputsValuesNode inputsValues节点
//     * @param fieldName        字段名
//     * @return 字段值
//     */
//    private Integer extractIntegerValueFromInputs(JsonNode inputsValuesNode, String fieldName) {
//        JsonNode fieldNode = inputsValuesNode.get(fieldName);
//        if (fieldNode != null) {
//            // 处理数组格式：[{"key": "xxx", "value": 123}] 或 [{"key": "xxx", "value": {"content": 123}}]
//            if (fieldNode.isArray() && fieldNode.size() > 0) {
//                JsonNode firstItem = fieldNode.get(0);
//                if (firstItem != null) {
//                    JsonNode valueNode = firstItem.get("value");
//                    if (valueNode != null) {
//                        // 如果value是对象，尝试获取content字段
//                        if (valueNode.isObject()) {
//                            JsonNode contentNode = valueNode.get("content");
//                            if (contentNode != null && contentNode.isNumber()) {
//                                return contentNode.asInt();
//                            }
//                        }
//                        // 如果value是数字，直接返回
//                        else if (valueNode.isNumber()) {
//                            return valueNode.asInt();
//                        }
//                        // 如果value是字符串，尝试转换为数字
//                        else if (valueNode.isTextual()) {
//                            try {
//                                return Integer.parseInt(valueNode.asText());
//                            } catch (NumberFormatException e) {
//                                log.warn("无法将字符串 '{}' 转换为整数", valueNode.asText());
//                            }
//                        }
//                    }
//                }
//            }
//            // 处理直接数值格式："fieldName": 123
//            else if (fieldNode.isNumber()) {
//                return fieldNode.asInt();
//            }
//        }
//        return null;
//    }
//
    @Override
    @GetMapping("/get-config/{configId}")
    public Response<AiAgentDrawConfigResponseDTO> getDrawConfig(@PathVariable String configId) {
//        try {
//            log.info("获取流程图配置请求，configId: {}", configId);
//
//            if (!StringUtils.hasText(configId)) {
//                return Response.<AiAgentDrawConfigResponseDTO>builder()
//                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
//                        .info("配置ID不能为空")
//                        .build();
//            }
//
//            AiAgentDrawConfig drawConfig = aiAgentDrawConfigDao.queryByConfigId(configId);
//
//            if (drawConfig == null) {
//                return Response.<AiAgentDrawConfigResponseDTO>builder()
//                        .code(ResponseCode.UN_ERROR.getCode())
//                        .info("配置不存在")
//                        .build();
//            }
//
//            AiAgentDrawConfigResponseDTO responseDTO = new AiAgentDrawConfigResponseDTO();
//            BeanUtils.copyProperties(drawConfig, responseDTO);
//
//            return Response.<AiAgentDrawConfigResponseDTO>builder()
//                    .code(ResponseCode.SUCCESS.getCode())
//                    .info(ResponseCode.SUCCESS.getInfo())
//                    .data(responseDTO)
//                    .build();
//
//        } catch (Exception e) {
//            log.error("获取流程图配置失败", e);
//            return Response.<AiAgentDrawConfigResponseDTO>builder()
//                    .code(ResponseCode.UN_ERROR.getCode())
//                    .info("获取失败：" + e.getMessage())
//                    .build();
//        }
        return null;
    }

    @Override
    @DeleteMapping("/delete-config/{configId}")
    public Response<String> deleteDrawConfig(@PathVariable String configId) {
//        try {
//            log.info("删除流程图配置请求，configId: {}", configId);
//
//            if (!StringUtils.hasText(configId)) {
//                return Response.<String>builder()
//                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
//                        .info("配置ID不能为空")
//                        .build();
//            }
//
//            int result = aiAgentDrawConfigDao.deleteByConfigId(configId);
//
//            if (result > 0) {
//                return Response.<String>builder()
//                        .code(ResponseCode.SUCCESS.getCode())
//                        .info(ResponseCode.SUCCESS.getInfo())
//                        .data("删除成功")
//                        .build();
//            } else {
//                return Response.<String>builder()
//                        .code(ResponseCode.UN_ERROR.getCode())
//                        .info("删除失败，配置不存在")
//                        .build();
//            }
//
//        } catch (Exception e) {
//            log.error("删除流程图配置失败", e);
//            return Response.<String>builder()
//                    .code(ResponseCode.UN_ERROR.getCode())
//                    .info("删除失败：" + e.getMessage())
//                    .build();
//        }
        return null;
    }
}
