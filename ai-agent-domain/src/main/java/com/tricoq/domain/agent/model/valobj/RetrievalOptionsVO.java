package com.tricoq.domain.agent.model.valobj;

import com.tricoq.domain.agent.model.dto.AiClientAdvisorDTO;
import com.tricoq.domain.agent.model.enums.KeyWordPolicy;
import com.tricoq.domain.agent.service.rag.rerank.enums.RerankQueryPolicy;
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

    /**
     * rerank 前候选池召回阈值；<=0 时沿用 SearchRequest 的最终检索阈值。
     */
    private double candidateSimilarityThreshold = 0.0;

    private KeyWordPolicy keyWordPolicy = KeyWordPolicy.STRONG;

    /**
     * rerank 使用的 query 构造策略。默认只用原始用户问题。
     */
    private RerankQueryPolicy rerankQueryPolicy = RerankQueryPolicy.ORIGINAL;

    /**
     * 查询阶段是否渲染 context salience 提示与相邻证据片段。默认开启，保持现有行为。
     */
    private boolean contextSalienceEnabled = true;

    public static RetrievalOptionsVO from(AiClientAdvisorDTO.RagAnswer ragAnswer, int topK) {
        return from(ragAnswer, topK, null);
    }

    public static RetrievalOptionsVO from(AiClientAdvisorDTO.RagAnswer ragAnswer, int topK, KeyWordPolicy keyWordPolicy) {
        RetrievalOptionsVO options = new RetrievalOptionsVO();
        if (ragAnswer == null) {
            return options;
        }

        int effectiveTopK = positiveOrDefault(topK, 1);
        options.setRetrievalMode(normalizeRetrievalMode(ragAnswer.getRetrievalMode()));
        options.setVectorTopK(positiveOrDefault(ragAnswer.getVectorTopK(), effectiveTopK));
        options.setKeywordTopK(positiveOrDefault(ragAnswer.getKeywordTopK(), effectiveTopK));
        options.setRrfK(positiveOrDefault(ragAnswer.getRrfK(), DEFAULT_RRF_K));
        options.setCandidateSimilarityThreshold(ragAnswer.getCandidateSimilarityThreshold());
        options.setRerankQueryPolicy(RerankQueryPolicy.getByPolicyOrDefault(ragAnswer.getRerankQueryPolicy()));
        options.setContextSalienceEnabled(ragAnswer.isContextSalienceEnabled());
        if (null != keyWordPolicy) {
            options.setKeyWordPolicy(keyWordPolicy);
        }
        return options;
    }

    public boolean isHybridMode() {
        return RETRIEVAL_MODE_HYBRID.equalsIgnoreCase(retrievalMode);
    }

    public int effectiveVectorTopK(int topK) {
        return positiveOrDefault(vectorTopK, topK);
    }

    public double effectiveCandidateSimilarityThreshold(double defaultThreshold) {
        return candidateSimilarityThreshold > 0 ? candidateSimilarityThreshold : defaultThreshold;
    }

    public int effectiveKeywordTopK(int topK) {
        return positiveOrDefault(keywordTopK, topK);
    }

    public int effectiveRrfK() {
        return positiveOrDefault(rrfK, DEFAULT_RRF_K);
    }

    public RerankQueryPolicy effectiveRerankQueryPolicy() {
        return rerankQueryPolicy == null ? RerankQueryPolicy.ORIGINAL : rerankQueryPolicy;
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
