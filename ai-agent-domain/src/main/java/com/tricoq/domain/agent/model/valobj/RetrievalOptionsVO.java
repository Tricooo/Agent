package com.tricoq.domain.agent.model.valobj;

import com.tricoq.domain.agent.model.dto.AiClientAdvisorDTO;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/7/26
 */
@Data
public class RetrievalOptionsVO {

    public static final String RETRIEVAL_MODE_VECTOR = "VECTOR";
    public static final String RETRIEVAL_MODE_HYBRID = "HYBRID";
    private static final int DEFAULT_RRF_K = 60;

    /**
     * 检索模式：VECTOR / HYBRID。默认 VECTOR，避免旧 extParam 在升级后行为漂移。
     */
    private String retrievalMode = RETRIEVAL_MODE_VECTOR;
    /**
     * HYBRID 模式下向量召回候选数；<=0 时回退到 topK。
     */
    private int vectorTopK = 0;
    /**
     * HYBRID 模式下 PG FTS 词面召回候选数；<=0 时回退到 topK。
     */
    private int keywordTopK = 0;
    /**
     * Reciprocal Rank Fusion 的平滑常量，常用默认值 60。
     */
    private int rrfK = DEFAULT_RRF_K;

    public static RetrievalOptionsVO from(AiClientAdvisorDTO.RagAnswer ragAnswer, int topK) {
        RetrievalOptionsVO options = new RetrievalOptionsVO();
        if (ragAnswer == null) {
            return options;
        }

        int effectiveTopK = positiveOrDefault(topK, 1);
        options.setRetrievalMode(normalizeRetrievalMode(ragAnswer.getRetrievalMode()));
        options.setVectorTopK(positiveOrDefault(ragAnswer.getVectorTopK(), effectiveTopK));
        options.setKeywordTopK(positiveOrDefault(ragAnswer.getKeywordTopK(), effectiveTopK));
        options.setRrfK(positiveOrDefault(ragAnswer.getRrfK(), DEFAULT_RRF_K));
        return options;
    }

    public boolean isHybridMode() {
        return RETRIEVAL_MODE_HYBRID.equalsIgnoreCase(retrievalMode);
    }

    public int effectiveVectorTopK(int topK) {
        return positiveOrDefault(vectorTopK, topK);
    }

    public int effectiveKeywordTopK(int topK) {
        return positiveOrDefault(keywordTopK, topK);
    }

    public int effectiveRrfK() {
        return positiveOrDefault(rrfK, DEFAULT_RRF_K);
    }

    private static String normalizeRetrievalMode(String retrievalMode) {
        if (StringUtils.isBlank(retrievalMode)) {
            return RETRIEVAL_MODE_VECTOR;
        }
        if (RETRIEVAL_MODE_HYBRID.equalsIgnoreCase(retrievalMode)) {
            return RETRIEVAL_MODE_HYBRID;
        }
        return RETRIEVAL_MODE_VECTOR;
    }

    private static int positiveOrDefault(int value, int defaultValue) {
        return value > 0 ? value : defaultValue;
    }
}
