package com.tricoq.domain.agent.service.rag.rewrite;

import com.tricoq.domain.agent.model.dto.RewriteResult;
import com.tricoq.domain.agent.service.rag.rewrite.enums.RewritePolicy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/15/26
 */
@Component
public class PassthroughQueryRewriter implements QueryRewriter {

    @Override
    public RewriteResult rewrite(String userText) {
        return RewriteResult.builder()
                .originUserText(userText)
                .queryVariantTexts(List.of(userText))
                .rewriteMode(RewritePolicy.PASSTHROUGH.getPolicyName())
                .build();
    }
}
