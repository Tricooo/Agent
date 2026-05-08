package com.tricoq.domain.agent.model.entity;

import lombok.Data;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/7/26
 */
@Data
public class VectorKeywordEntity {

    private String id;

    private String content;

    private String metaData;

    private double keywordScore;
}
