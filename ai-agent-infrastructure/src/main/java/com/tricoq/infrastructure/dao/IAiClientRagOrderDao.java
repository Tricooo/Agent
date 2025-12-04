package com.tricoq.infrastructure.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tricoq.infrastructure.dao.po.AiClientRagOrder;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 知识库配置表 DAO
 * @author trico qiang
 * @description 知识库配置表数据访问对象
 */
@Mapper
public interface IAiClientRagOrderDao extends BaseMapper<AiClientRagOrder> {

    /**
     * 根据知识库ID更新知识库配置
     * @param aiClientRagOrder 知识库配置对象
     * @return 影响行数
     */
    int updateByRagId(AiClientRagOrder aiClientRagOrder);

    /**
     * 根据知识库ID删除知识库配置
     * @param ragId 知识库ID
     * @return 影响行数
     */
    int deleteByRagId(String ragId);

    /**
     * 根据知识库ID查询知识库配置
     * @param ragId 知识库ID
     * @return 知识库配置对象
     */
    AiClientRagOrder queryByRagId(String ragId);

    /**
     * 查询启用的知识库配置
     * @return 知识库配置列表
     */
    List<AiClientRagOrder> queryEnabledRagOrders();

    /**
     * 根据知识标签查询知识库配置
     * @param knowledgeTag 知识标签
     * @return 知识库配置列表
     */
    List<AiClientRagOrder> queryByKnowledgeTag(String knowledgeTag);

    /**
     * 查询所有知识库配置
     * @return 知识库配置列表
     */
    List<AiClientRagOrder> queryAll();

}
