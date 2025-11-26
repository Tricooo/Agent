package com.tricoq.application.service;

import com.tricoq.application.model.dto.command.SaveAgentDrawCommand;
import com.tricoq.domain.agent.adapter.repository.IAgentDrawConfigRepository;
import com.tricoq.domain.agent.adapter.repository.IAgentRepository;
import com.tricoq.domain.agent.model.aggregate.AiAgentAggregate;
import com.tricoq.domain.agent.model.aggregate.AiAgentDrawConfigAggregate;
import com.tricoq.domain.agent.model.dto.AiAgentDTO;
import com.tricoq.types.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 *
 *
 * @author trico qiang
 * @date 11/25/25
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiAgentDrawAdminService {

    private final DrawConfigParser drawConfigParser;

    private final IAgentRepository agentRepository;

    private final IAgentDrawConfigRepository agentDrawConfigRepository;

    @Transactional(rollbackFor = Exception.class)
    public void saveDrawConfig(SaveAgentDrawCommand command) {
        try {
            log.info("保存流程图配置请求：{}", command);

            String agentId = UUID.randomUUID().toString().replaceAll("-", "");
            command.setAgentId(agentId);

            // 参数校验
            if (!StringUtils.hasText(command.getConfigName())) {

            }

            if (!StringUtils.hasText(command.getConfigData())) {

            }

            AiAgentAggregate aiAgent = drawConfigParser.buildAgent(agentId, command.getConfigData());
            if (!agentRepository.saveAggregate(aiAgent)) {
                throw new Exception("agent保存失败");
            }

            // 生成配置ID（如果没有提供）
            String configId = command.getConfigId();
            if (!StringUtils.hasText(configId)) {
                configId = UUID.randomUUID().toString().replace("-", "");
            }

            // 检查配置是否已存在
            AiAgentDrawConfigAggregate existingConfig = agentDrawConfigRepository.findById(configId);
            AiAgentDrawConfig drawConfig = new AiAgentDrawConfig();
            BeanUtils.copyProperties(command, drawConfig);
            drawConfig.setConfigId(configId);
            drawConfig.setVersion(1);
            drawConfig.setStatus(1);

            int result;
            if (existingConfig != null) {
                // 更新现有配置
                drawConfig.setId(existingConfig.getId());
                drawConfig.setVersion(existingConfig.getVersion() + 1);
                drawConfig.setUpdateTime(LocalDateTime.now());
                result = aiAgentDrawConfigDao.updateByConfigId(drawConfig);
                log.info("更新流程图配置，configId: {}, result: {}", configId, result);
            } else {
                // 创建新配置
                drawConfig.setCreateTime(LocalDateTime.now());
                drawConfig.setUpdateTime(LocalDateTime.now());
                result = aiAgentDrawConfigDao.insert(drawConfig);
                log.info("创建流程图配置，configId: {}, result: {}", configId, result);
            }

            List<AiAgentFlowConfig> flowConfigs = drawConfigParser.buildDrawConfig(agentId, command.getConfigData());

            if (result > 0) {
                // 解析JSON配置数据，生成关系映射并存储到ai_client_config表
                try {
                    List<AiClientConfig> configRelations = drawConfigParser.parseConfigData(command.getConfigData());
                    if (!configRelations.isEmpty()) {
                        // 先删除该配置相关的旧关系数据（如果是更新操作）
                        if (existingConfig != null) {
                            aiClientConfigDao.deleteBySourceId(configId);
                            log.info("删除配置{}的旧关系数据", configId);
                        }

                        // 批量插入新的关系数据
                        for (AiClientConfig config : configRelations) {
                            // 检查是否已经存在相同的记录
                            List<AiClientConfig> existingConfigs = aiClientConfigDao.queryByConditions(
                                    config.getSourceType(),
                                    config.getSourceId(),
                                    config.getTargetType(),
                                    config.getTargetId()
                            );

                            if (existingConfigs.isEmpty()) {
                                // 设置扩展参数，记录来源配置ID
                                config.setExtParam("{\"configId\":\"" + configId + "\"}");
                                aiClientConfigDao.insert(config);
                                log.debug("插入新的配置关系: sourceType={}, sourceId={}, targetType={}, targetId={}",
                                        config.getSourceType(), config.getSourceId(), config.getTargetType(), config.getTargetId());
                            } else {
                                log.debug("配置关系已存在，跳过插入: sourceType={}, sourceId={}, targetType={}, targetId={}",
                                        config.getSourceType(), config.getSourceId(), config.getTargetType(), config.getTargetId());
                            }
                        }
                        log.info("成功保存{}条配置关系数据", configRelations.size());
                    }
                } catch (Exception e) {
                    log.error("解析和保存配置关系数据失败，configId: {}", configId, e);
                    // 这里不影响主流程，只记录错误日志
                }

                // 解析JSON配置数据，提取client信息并保存agent-client关系
                try {
                    List<AiAgentFlowConfig> agentFlowConfigs = parseClientInfoFromJson(command.getConfigData(), agentId);
                    if (!agentFlowConfigs.isEmpty()) {
                        // 先删除该agentId相关的旧关系数据（如果是更新操作）
                        if (existingConfig != null) {
                            aiAgentFlowConfigDao.deleteByAgentId(agentId);
                            log.info("删除agentId{}的旧流程配置数据", agentId);
                        }

                        // 批量插入新的agent-client关系数据
                        for (AiAgentFlowConfig flowConfig : agentFlowConfigs) {
                            aiAgentFlowConfigDao.insert(flowConfig);
                        }
                        log.info("成功保存{}条agent-client关系数据", agentFlowConfigs.size());
                    }
                } catch (Exception e) {
                    log.error("解析和保存agent-client关系数据失败，agentId: {}", agentId, e);
                    // 这里不影响主流程，只记录错误日志
                }

                return Response.<String>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info(ResponseCode.SUCCESS.getInfo())
                        .data(configId)
                        .build();
            } else {
                return Response.<String>builder()
                        .code(ResponseCode.UN_ERROR.getCode())
                        .info("保存失败")
                        .build();
            }

        } catch (Exception e) {
            log.error("保存流程图配置失败", e);
            return Response.<String>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("保存失败：" + e.getMessage())
                    .build();
        }
    }
}
