package com.tricoq.domain.agent.service.rag.rewrite.enums;

/**
 * @description: query rewrite 单策略尝试状态。
 * @author：trico qiang
 * @date: 5/19/26
 */
public enum RewriteStatus {

    REWRITTEN,
    NO_MATCH,
    FAILED,
    PASSTHROUGH
}
