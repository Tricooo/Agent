package com.tricoq.domain.agent.service.execute.auto.render;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/21/26
 */
public enum DetailRenderLevel {
    //todo 限制单向变化
    FULL,
    OMIT_OPTIONAL,
    COMPRESS_LONG_FIELDS,
    COMPACT_DETAIL,
    DROP
}
