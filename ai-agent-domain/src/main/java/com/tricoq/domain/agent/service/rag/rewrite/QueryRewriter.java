package com.tricoq.domain.agent.service.rag.rewrite;

import com.tricoq.domain.agent.model.dto.RewriteResult;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/15/26
 */
public interface QueryRewriter {

    RewriteResult rewrite(String userText);
}
