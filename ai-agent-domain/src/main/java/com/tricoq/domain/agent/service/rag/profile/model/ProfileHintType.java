package com.tricoq.domain.agent.service.rag.profile.model;

/**
 * Knowledge-base profile hint categories used for query-time selection.
 */
public enum ProfileHintType {

    HEADING,
    INLINE_CODE,
    CODE_TOKEN,
    TECHNICAL_TOKEN,
    EVIDENCE_TYPE
}
