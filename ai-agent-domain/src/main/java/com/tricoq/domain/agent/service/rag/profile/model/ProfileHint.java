package com.tricoq.domain.agent.service.rag.profile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A compact navigation hint extracted from a knowledge base.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileHint {

    private String value;

    private ProfileHintType type;

    private String source;

    private Integer frequency;

    private Integer score;
}
