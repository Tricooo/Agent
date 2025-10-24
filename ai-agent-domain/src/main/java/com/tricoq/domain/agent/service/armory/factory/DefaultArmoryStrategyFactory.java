package com.tricoq.domain.agent.service.armory.factory;

import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.service.armory.RootNode;
import com.tricoq.domain.framework.chain.StrategyHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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
    @AllArgsConstructor
    @Data
    public static class DynamicContext {

        private final Map<String, Object> dataObjects = new HashMap<>();

        public <T> void setValue(String key,T value){
            dataObjects.put(key, value);
        }

        public <T> T getValue(String key){
            return (T) dataObjects.get(key);
        }
    }
}
