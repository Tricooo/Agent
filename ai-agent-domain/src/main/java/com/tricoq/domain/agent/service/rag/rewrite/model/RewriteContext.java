package com.tricoq.domain.agent.service.rag.rewrite.model;

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

}
