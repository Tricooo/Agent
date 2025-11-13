package com.tricoq.infrastructure.dao;


import com.tricoq.infrastructure.dao.po.AiClientSystemPrompt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 系统提示词配置表 DAO
 *
 * @author trico qiang
 * @description 系统提示词配置表数据访问对象
 */
@Mapper
public interface IAiClientSystemPromptDao {

    /**
     * 插入系统提示词配置
     */
    void insert(AiClientSystemPrompt aiClientSystemPrompt);

    /**
     * 根据ID更新系统提示词配置
     */
    int updateById(AiClientSystemPrompt aiClientSystemPrompt);

    /**
     * 根据提示词ID更新系统提示词配置
     */
    int updateByPromptId(AiClientSystemPrompt aiClientSystemPrompt);

    /**
     * 根据ID删除系统提示词配置
     */
    int deleteById(Long id);

    /**
     * 根据提示词ID删除系统提示词配置
     */
    int deleteByPromptId(String promptId);

    /**
     * 根据ID查询系统提示词配置
     */
    AiClientSystemPrompt queryById(Long id);

    /**
     * 根据提示词ID查询系统提示词配置
     */
    AiClientSystemPrompt queryByPromptId(String promptId);

    /**
     * 查询启用的系统提示词配置
     */
    List<AiClientSystemPrompt> queryEnabledPrompts();

    /**
     * 根据提示词名称查询系统提示词配置
     */
    List<AiClientSystemPrompt> queryByPromptName(String promptName);

    /**
     * 查询所有系统提示词配置
     */
    List<AiClientSystemPrompt> queryAll();

    /**
     * 批量查询系统提示词
     *
     * @param systemPromptIds 提示词id集合
     * @return 提示词配置集合
     */
    List<AiClientSystemPrompt> queryByIdsPromptsEnabled(@Param("systemPromptIds") Collection<String> systemPromptIds);
}