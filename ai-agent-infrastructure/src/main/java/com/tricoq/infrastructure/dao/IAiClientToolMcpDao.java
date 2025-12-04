package com.tricoq.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tricoq.infrastructure.dao.po.AiClientToolMcp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * MCP客户端配置表 DAO
 * @author trico qiang
 * @description MCP客户端配置表数据访问对象
 */
@Mapper
public interface IAiClientToolMcpDao extends BaseMapper<AiClientToolMcp> {

    /**
     * 根据MCP ID更新MCP客户端配置
     * @param aiClientToolMcp MCP客户端配置对象
     * @return 影响行数
     */
    int updateByMcpId(AiClientToolMcp aiClientToolMcp);

    /**
     * 根据MCP ID删除MCP客户端配置
     * @param mcpId MCP ID
     * @return 影响行数
     */
    int deleteByMcpId(String mcpId);

    /**
     * 根据MCP ID查询MCP客户端配置
     * @param mcpId MCP ID
     * @return MCP客户端配置对象
     */
    AiClientToolMcp queryByMcpId(String mcpId);

    /**
     * 查询所有MCP客户端配置
     * @return MCP客户端配置列表
     */
    List<AiClientToolMcp> queryAll();

    /**
     * 根据状态查询MCP客户端配置
     * @param status 状态
     * @return MCP客户端配置列表
     */
    List<AiClientToolMcp> queryByStatus(Integer status);

    /**
     * 根据传输类型查询MCP客户端配置
     * @param transportType 传输类型
     * @return MCP客户端配置列表
     */
    List<AiClientToolMcp> queryByTransportType(String transportType);

    /**
     * 查询启用的MCP客户端配置
     * @return MCP客户端配置列表
     */
    List<AiClientToolMcp> queryEnabledMcps();

    /**
     * 查询mcp客户端配置集合
     *
     * @param mcpToolIds mcp id集合
     * @return MCP客户端配置列表
     */
    List<AiClientToolMcp> queryByMcpIdsEnabled(@Param("mcpToolIds") Set<String> mcpToolIds);
}
