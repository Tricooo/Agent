package com.tricoq.domain.agent.model.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.ai.document.Document;

import java.util.List;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/13/26
 */
@Data
@Builder
public class RerankResult {

    private List<Document> documents;

    private boolean applied;

    private String mode;

    private int candidateCount;

    private int finalCount;

    private String failureReason;

}
