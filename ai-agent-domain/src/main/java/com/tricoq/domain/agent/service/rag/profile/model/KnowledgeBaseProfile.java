package com.tricoq.domain.agent.service.rag.profile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lightweight knowledge-base profile for retrieval planning.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeBaseProfile {

    private String ragId;

    private String knowledgeTag;

    private String profileVersion;

    private String profileSource;

    private LocalDateTime generatedAt;

    private String summary;

    private List<GroundedProfileItem> concepts;

    private List<ProfileAlias> aliases;

    private List<GroundedProfileItem> questionsAnswered;

    private List<GroundedProfileItem> negativeScopes;

    private List<GroundedProfileItem> evidenceTypes;

    private List<ProfileHint> hints;
}
