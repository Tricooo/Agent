package com.tricoq.infrastructure.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tricoq.infrastructure.dao.po.AiAgentFlowConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 智能体-客户端关联表 DAO
 * @author trico qiang
 * @description 智能体-客户端关联表数据访问对象
 */
@Mapper
public interface IAiAgentFlowConfigDao extends BaseMapper<AiAgentFlowConfig> {

    /**
     * 根据智能体ID删除关联配置
     * @param agentId 智能体ID
     * @return 影响行数
     */
    int deleteByAgentId(String agentId);

    /**
     * 根据智能体ID查询关联配置列表
     * @param agentId 智能体ID
     * @return 智能体-客户端关联配置列表
     */
    List<AiAgentFlowConfig> queryByAgentId(String agentId);

    /**
     * 根据客户端ID查询关联配置列表
     * @param clientId 客户端ID
     * @return 智能体-客户端关联配置列表
     */
    List<AiAgentFlowConfig> queryByClientId(Long clientId);

    /**
     * 根据智能体ID和客户端ID查询关联配置
     * @param agentId 智能体ID
     * @param clientId 客户端ID
     * @return 智能体-客户端关联配置对象
     */
    AiAgentFlowConfig queryByAgentIdAndClientId(Long agentId, Long clientId);

    /**
     * 查询所有智能体-客户端关联配置
     * @return 智能体-客户端关联配置列表
     */
    List<AiAgentFlowConfig> queryAll();

}
