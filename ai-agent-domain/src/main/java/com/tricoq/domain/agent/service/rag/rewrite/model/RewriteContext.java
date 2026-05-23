package com.tricoq.domain.agent.service.rag.rewrite.model;

import com.tricoq.domain.agent.service.rag.profile.model.ProfileHint;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/20/26
 */
@Data
@Builder
public class RewriteContext {

    private String extraClientId;

    private List<String> queryRewriteDomainHints;

    private List<ProfileHint> knowledgeBaseProfileHints;

    private List<String> selectedProfileHints;

    private String profileSource;

    private String profileVersion;

    private Integer profileHintTopN;

}
