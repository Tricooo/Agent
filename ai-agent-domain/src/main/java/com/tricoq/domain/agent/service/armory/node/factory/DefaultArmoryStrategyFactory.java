package com.tricoq.domain.agent.service.armory.node.factory;

import com.tricoq.domain.agent.model.dto.AiClientAdvisorDTO;
import com.tricoq.domain.agent.model.dto.AiClientApiDTO;
import com.tricoq.domain.agent.model.dto.AiClientDTO;
import com.tricoq.domain.agent.model.dto.AiClientModelDTO;
import com.tricoq.domain.agent.model.dto.AiClientSystemPromptDTO;
import com.tricoq.domain.agent.model.dto.AiClientToolMcpDTO;
import com.tricoq.domain.agent.model.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.service.armory.node.RootNode;
import com.tricoq.types.framework.chain.StrategyHandler;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author trico qiang
 * @date 10/23/25
 */
@Service
@RequiredArgsConstructor
public class DefaultArmoryStrategyFactory {

    private final RootNode rootNode;

    //调用的起点，只需要节点的自身数据处理能力
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext,String> armoryStrategyHandler(){
        return rootNode;
    }

    @Builder
    @NoArgsConstructor
    @Data
    public static class DynamicContext {

        private final Map<String, Object> dataObjects = new HashMap<>();

        @Deprecated
        public <T> void setValue(String key,T value){
            dataObjects.put(key, value);
        }

        @Deprecated
        @SuppressWarnings("unchecked")
        public <T> T getValue(String key){
            //如果想要没有warning的转换集合，必须每个元素调用cast进行转换，但是这样性能耗费较大
            //如果能够通过编码规避这种可能的报错，直接忽略就好
            return (T) dataObjects.get(key);
        }

        public void setClientApis(List<AiClientApiDTO> value) {
            put(AiAgentEnumVO.AI_CLIENT_API.getDataName(), value);
        }

        public List<AiClientApiDTO> getClientApis() {
            return getOrDefault(AiAgentEnumVO.AI_CLIENT_API.getDataName(), Collections.emptyList());
        }

        public void setClientModels(List<AiClientModelDTO> value) {
            put(AiAgentEnumVO.AI_CLIENT_MODEL.getDataName(), value);
        }

        public List<AiClientModelDTO> getClientModels() {
            return getOrDefault(AiAgentEnumVO.AI_CLIENT_MODEL.getDataName(), Collections.emptyList());
        }

        public void setSystemPromptMap(Map<String, AiClientSystemPromptDTO> value) {
            put(AiAgentEnumVO.AI_CLIENT_SYSTEM_PROMPT.getDataName(), value);
        }

        public Map<String, AiClientSystemPromptDTO> getSystemPromptMap() {
            return getOrDefault(AiAgentEnumVO.AI_CLIENT_SYSTEM_PROMPT.getDataName(), Collections.emptyMap());
        }

        public void setToolMcps(List<AiClientToolMcpDTO> value) {
            put(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getDataName(), value);
        }

        public List<AiClientToolMcpDTO> getToolMcps() {
            return getOrDefault(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getDataName(), Collections.emptyList());
        }

        public void setAdvisorConfigs(List<AiClientAdvisorDTO> value) {
            put(AiAgentEnumVO.AI_CLIENT_ADVISOR.getDataName(), value);
        }

        public List<AiClientAdvisorDTO> getAdvisorConfigs() {
            return getOrDefault(AiAgentEnumVO.AI_CLIENT_ADVISOR.getDataName(), Collections.emptyList());
        }

        public Map<String, AiClientAdvisorDTO> getAdvisorConfigMap() {
            return getAdvisorConfigs().stream()
                    .filter(advisor -> advisor.getAdvisorId() != null)
                    .collect(Collectors.toMap(AiClientAdvisorDTO::getAdvisorId,
                            Function.identity(),
                            (existing, ignored) -> existing));
        }

        public void setClients(List<AiClientDTO> value) {
            put(AiAgentEnumVO.AI_CLIENT.getDataName(), value);
        }

        public List<AiClientDTO> getClients() {
            return getOrDefault(AiAgentEnumVO.AI_CLIENT.getDataName(), Collections.emptyList());
        }

        public Map<String, AiClientDTO> getClientMap() {
            return getClients().stream()
                    .filter(client -> client.getClientId() != null)
                    .collect(Collectors.toMap(AiClientDTO::getClientId,
                            Function.identity(),
                            (existing, ignored) -> existing));
        }

        private <T> void put(String key, T value) {
            dataObjects.put(key, value);
        }

        @SuppressWarnings("unchecked")
        private <T> T getOrDefault(String key, T defaultValue) {
            Object value = dataObjects.get(key);
            return value == null ? defaultValue : (T) value;
        }
    }
}
