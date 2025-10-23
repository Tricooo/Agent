package com.tricoq.domain.agent.service.armory.factory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author trico qiang
 * @date 10/23/25
 */
@Service
public class DefaultArmoryStrategyFactory {


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
