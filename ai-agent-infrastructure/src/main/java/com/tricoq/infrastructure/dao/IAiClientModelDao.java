package com.tricoq.infrastructure.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tricoq.infrastructure.dao.po.AiClientModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 聊天模型配置表 DAO
 * @author trico qiang
 * @description 聊天模型配置表数据访问对象
 */
@Mapper
public interface IAiClientModelDao extends BaseMapper<AiClientModel> {

    /**
     * 根据模型ID更新聊天模型配置
     * @param aiClientModel 聊天模型配置对象
     * @return 影响行数
     */
    int updateByModelId(AiClientModel aiClientModel);

    /**
     * 根据模型ID删除聊天模型配置
     * @param modelId 模型ID
     * @return 影响行数
     */
    int deleteByModelId(String modelId);

    /**
     * 根据模型ID查询聊天模型配置
     * @param modelId 模型ID
     * @return 聊天模型配置对象
     */
    AiClientModel queryByModelId(String modelId);

    /**
     * 根据API配置ID查询聊天模型配置
     * @param apiId API配置ID
     * @return 聊天模型配置列表
     */
    List<AiClientModel> queryByApiId(String apiId);

    /**
     * 根据模型类型查询聊天模型配置
     * @param modelType 模型类型
     * @return 聊天模型配置列表
     */
    List<AiClientModel> queryByModelType(String modelType);

    /**
     * 查询所有启用的聊天模型配置
     * @return 聊天模型配置列表
     */
    List<AiClientModel> queryEnabledModels();

    /**
     * 查询所有聊天模型配置
     * @return 聊天模型配置列表
     */
    List<AiClientModel> queryAll();

    /**
     * 批量查询模型
     *
     * @param modelIds 模型id集合
     * @return 聊天模型配置列表
     */
    List<AiClientModel> queryByIds(@Param("modelIds") Collection<String> modelIds);
}
