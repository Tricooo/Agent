package com.tricoq.infrastructure.adapter.repository;

import com.alibaba.fastjson2.JSON;
import com.tricoq.domain.agent.adapter.repository.IKnowledgeBaseProfileRepository;
import com.tricoq.domain.agent.service.rag.profile.model.KnowledgeBaseProfile;
import com.tricoq.infrastructure.dao.po.AiClientRagProfile;
import com.tricoq.infrastructure.support.AiClientRagProfileDaoSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class KnowledgeBaseProfileRepositoryImpl implements IKnowledgeBaseProfileRepository {

    private static final int STATUS_ENABLED = 1;

    private final AiClientRagProfileDaoSupport ragProfileDaoSupport;

    @Override
    public boolean saveOrUpdate(KnowledgeBaseProfile profile) {
        if (profile == null || !StringUtils.hasText(profile.getKnowledgeTag())) {
            return false;
        }
        AiClientRagProfile po = toPo(profile);
        LocalDateTime now = LocalDateTime.now();
        po.setUpdateTime(now);
        AiClientRagProfile existing = ragProfileDaoSupport.getBaseMapper()
                .queryByKnowledgeTag(profile.getKnowledgeTag());
        if (existing == null) {
            po.setCreateTime(now);
            return ragProfileDaoSupport.save(po);
        }
        return ragProfileDaoSupport.getBaseMapper().updateByKnowledgeTag(po) > 0;
    }

    @Override
    public KnowledgeBaseProfile queryByKnowledgeTag(String knowledgeTag) {
        if (!StringUtils.hasText(knowledgeTag)) {
            return null;
        }
        return toProfile(ragProfileDaoSupport.getBaseMapper().queryByKnowledgeTag(knowledgeTag));
    }

    @Override
    public List<KnowledgeBaseProfile> queryByKnowledgeTags(Collection<String> knowledgeTags) {
        if (CollectionUtils.isEmpty(knowledgeTags)) {
            return List.of();
        }
        return ragProfileDaoSupport.getBaseMapper().queryByKnowledgeTags(knowledgeTags).stream()
                .map(this::toProfile)
                .filter(Objects::nonNull)
                .toList();
    }

    private AiClientRagProfile toPo(KnowledgeBaseProfile profile) {
        return AiClientRagProfile.builder()
                .ragId(profile.getRagId())
                .knowledgeTag(profile.getKnowledgeTag())
                .profileVersion(profile.getProfileVersion())
                .profileJson(JSON.toJSONString(profile))
                .status(STATUS_ENABLED)
                .build();
    }

    private KnowledgeBaseProfile toProfile(AiClientRagProfile po) {
        if (po == null || !StringUtils.hasText(po.getProfileJson())) {
            return null;
        }
        KnowledgeBaseProfile profile = JSON.parseObject(po.getProfileJson(), KnowledgeBaseProfile.class);
        profile.setRagId(po.getRagId());
        profile.setKnowledgeTag(po.getKnowledgeTag());
        profile.setProfileVersion(po.getProfileVersion());
        return profile;
    }
}
