package com.tricoq.domain.agent.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/15/26
 */
@Data
@Builder
public class RewriteResult {

    private String originUserText;

    private List<String> queryVariantTexts;

    private String rewriteMode;

}
