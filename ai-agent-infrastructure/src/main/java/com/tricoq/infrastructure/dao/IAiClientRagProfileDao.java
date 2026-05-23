package com.tricoq.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tricoq.infrastructure.dao.po.AiClientRagProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface IAiClientRagProfileDao extends BaseMapper<AiClientRagProfile> {

    int updateByKnowledgeTag(AiClientRagProfile profile);

    AiClientRagProfile queryByKnowledgeTag(String knowledgeTag);

    List<AiClientRagProfile> queryByKnowledgeTags(@Param("knowledgeTags") Collection<String> knowledgeTags);
}
