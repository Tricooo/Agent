package com.tricoq.domain.agent.service.rag.rerank;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.tricoq.domain.agent.model.dto.RerankResult;
import com.tricoq.domain.agent.model.valobj.RagObservationKeys;
import com.tricoq.domain.agent.service.rag.rerank.enums.RerankPolicy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/**
 * @description:
 * @author：trico qiang
 * @date: 5/14/26
 */
@Slf4j
@Component
public class HttpDocumentReranker implements DocumentReranker {

    private static final String DEFAULT_ENDPOINT = "http://127.0.0.1:18080/rerank";
    private static final String DEFAULT_MODEL_NAME = "bge-reranker-v2-m3";
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final DocumentReranker fallbackDocumentReranker;
    private final boolean enabled;
    private final String endpoint;
    private final Duration readTimeout;
    private final String modelName;

    public HttpDocumentReranker(PassthroughDocumentReranker fallbackDocumentReranker,
                                @Value("${spring.ai.rag.rerank.local-bge.enabled:true}") boolean enabled,
                                @Value("${spring.ai.rag.rerank.local-bge.endpoint:" + DEFAULT_ENDPOINT + "}") String endpoint,
                                @Value("${spring.ai.rag.rerank.local-bge.connect-timeout:10s}") Duration connectTimeout,
                                @Value("${spring.ai.rag.rerank.local-bge.read-timeout:10s}") Duration readTimeout,
                                @Value("${spring.ai.rag.rerank.local-bge.model-name:" + DEFAULT_MODEL_NAME + "}") String modelName) {
        Duration safeConnectTimeout = connectTimeout == null ? DEFAULT_CONNECT_TIMEOUT : connectTimeout;
        this.enabled = enabled;
        this.endpoint = StringUtils.defaultIfBlank(endpoint, DEFAULT_ENDPOINT);
        this.readTimeout = readTimeout == null ? DEFAULT_READ_TIMEOUT : readTimeout;
        this.modelName = StringUtils.defaultIfBlank(modelName, DEFAULT_MODEL_NAME);
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(safeConnectTimeout)
                .build();
        this.fallbackDocumentReranker = fallbackDocumentReranker == null
                ? new PassthroughDocumentReranker()
                : fallbackDocumentReranker;
    }

    @Override
    public RerankResult rerank(String query, List<Document> candidates, int topK) {
        if (!enabled) {
            return fallback("RERANK_DISABLED", query, candidates, topK);
        }

        if (StringUtils.isBlank(query) || CollectionUtils.isEmpty(candidates) || topK <= 0) {
            return fallback("RERANK_INVALID_ARGUMENT", query, candidates, topK);
        }

        //构建请求体
        List<RerankHttpDocument> rerankHttpDocuments = candidates.stream().map(candidate ->
                new RerankHttpDocument(candidate.getId(), candidate.getText(), candidate.getMetadata())
        ).toList();

        RerankHttpRequest rerankHttpRequest = new RerankHttpRequest(query, rerankHttpDocuments, topK);

        HttpRequest httpRequest;
        try {
            httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(readTimeout)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(rerankHttpRequest), StandardCharsets.UTF_8))
                    .build();
        } catch (IllegalArgumentException e) {
            log.warn("Rerank服务地址配置非法,endpoint={},message={}", endpoint, e.getMessage());
            return fallback(failureReason("RERANK_INVALID_ENDPOINT", e), query, candidates, topK);
        }

        HttpResponse<String> response = null;

        try {
            response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.warn("Rerank服务调用失败,message={}", e.getMessage());
            return fallback(failureReason("RERANK_HTTP_IO_ERROR", e), query, candidates, topK);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Rerank服务调用被中断,message={}", e.getMessage());
            return fallback(failureReason("RERANK_HTTP_INTERRUPTED", e), query, candidates, topK);
        }

        //处理响应
        if (response == null) {
            return fallback("RERANK_NULL_HTTP_RESPONSE", query, candidates, topK);
        }

        int statusCode = response.statusCode();
        if (statusCode / 100 != 2) {
            log.warn("Rerank服务返回非成功状态码,statusCode={},body={}", statusCode, response.body());
            return fallback("RERANK_NON_2XX_STATUS,statusCode=" + statusCode, query, candidates, topK);
        }

        String body = response.body();
        if (StringUtils.isBlank(body)) {
            return fallback("RERANK_EMPTY_RESPONSE_BODY,statusCode=" + statusCode, query, candidates, topK);
        }

        RerankHttpResponse rerankHttpResponse;
        try {
            rerankHttpResponse = JSON.parseObject(body, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.warn("Rerank服务响应解析失败,statusCode={},message={}", statusCode, e.getMessage());
            return fallback("RERANK_RESPONSE_PARSE_ERROR,statusCode=" + statusCode + "," + failureReason("message", e), query, candidates, topK);
        }

        if (rerankHttpResponse == null) {
            return fallback("RERANK_NULL_RESPONSE_OBJECT,statusCode=" + statusCode, query, candidates, topK);
        }

        List<RerankHttpResult> results = rerankHttpResponse.results();
        if (CollectionUtils.isEmpty(results)) {
            return fallback("RERANK_EMPTY_RESULTS,statusCode=" + statusCode, query, candidates, topK);
        }

        //重排序
        int candidateCount = candidates.size();
        int finalCount = Math.min(topK, candidateCount);

        List<Document> documents = results.stream().sorted(Comparator.comparing(RerankHttpResult::rank))
                .map(result -> {
                    if (result.originalIndex() > candidates.size() - 1 || result.originalIndex() < 0) {
                        log.warn("Rerank index越界:{},candidates:{}", result.originalIndex(), candidates.size());
                        return null;
                    }
                    Document document = candidates.get(result.originalIndex());
                    Map<String, Object> metadata = new HashMap<>(document.getMetadata());
                    metadata.put(RagObservationKeys.DocumentMetadata.RERANK_RANK, result.rank());
                    metadata.put(RagObservationKeys.DocumentMetadata.RERANK_APPLIED, true);
                    metadata.put(RagObservationKeys.DocumentMetadata.RERANK_MODE, RerankPolicy.LOCAL_BGE.getPolicyName());
                    metadata.put(RagObservationKeys.DocumentMetadata.BEFORE_RERANK_RANK, result.originalIndex() + 1);
                    metadata.put(RagObservationKeys.DocumentMetadata.RERANK_SCORE, result.score());
                    return document.mutate().metadata(metadata).build();
                })
                .filter(Objects::nonNull)
                .limit(finalCount)
                .toList();

        if (CollectionUtils.isEmpty(documents)) {
            return fallback("RERANK_NO_VALID_DOCUMENTS,statusCode=" + statusCode, query, candidates, topK);
        }


        String actualModelName = StringUtils.defaultIfBlank(rerankHttpResponse.model(), modelName);

        return RerankResult.builder().documents(documents)
                .applied(true)
                .mode(RerankPolicy.LOCAL_BGE.getPolicyName())
                .candidateCount(candidateCount)
                .finalCount(Math.min(finalCount, documents.size()))
                .failureReason(null)
                .modelName(actualModelName)
                .endpoint(endpoint)
                .build();
    }

    private RerankResult fallback(String reason, String query, List<Document> candidates, int topK) {
        RerankResult result = fallbackDocumentReranker.rerank(query, candidates, topK);
        if (StringUtils.isNotBlank(reason)) {
            result.setFailureReason(reason);
        }
        result.setModelName(modelName);
        result.setEndpoint(endpoint);
        return result;
    }

    private String failureReason(String code, Exception e) {
        if (e == null) {
            return code;
        }
        String message = StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName());
        return code + ":" + message;
    }


    private record RerankHttpRequest(
            String query,
            List<RerankHttpDocument> documents,
            int topK
    ) {
    }

    private record RerankHttpDocument(
            String id,
            String text,
            Map<String, Object> metadata
    ) {
    }


    private record RerankHttpResponse(
            String model,
            long elapsedMs,
            List<RerankHttpResult> results
    ) {
    }

    private record RerankHttpResult(
            String id,
            double score,
            int rank,
            int originalIndex,
            Map<String, Object> metadata
    ) {
    }
}
