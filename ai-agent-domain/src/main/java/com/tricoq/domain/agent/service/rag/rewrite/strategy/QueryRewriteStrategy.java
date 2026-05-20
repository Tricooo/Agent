package com.tricoq.domain.agent.service.rag.rewrite.strategy;

import com.tricoq.domain.agent.service.rag.rewrite.enums.RewritePolicy;
import com.tricoq.domain.agent.service.rag.rewrite.model.RewriteAttempt;

/**
 * @description: 单个 query rewrite 策略，只负责一次尝试，不负责降级编排。
 * @author：trico qiang
 * @date: 5/19/26
 */
public interface QueryRewriteStrategy {

    RewritePolicy policy();

    RewriteAttempt attempt(String userText);
}
