package com.tricoq.domain.agent.service.armory.node.factory;

import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.service.armory.node.RootNode;
import com.tricoq.types.framework.chain.StrategyHandler;
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
    @Data
    public static class DynamicContext {

        private final Map<String, Object> dataObjects = new HashMap<>();

        public <T> void setValue(String key,T value){
            dataObjects.put(key, value);
        }

        @SuppressWarnings("unchecked")
        public <T> T getValue(String key){
            //如果想要没有warning的转换集合，必须每个元素调用cast进行转换，但是这样性能耗费较大
            //如果能够通过编码规避这种可能的报错，直接忽略就好
            return (T) dataObjects.get(key);
        }
    }
}
