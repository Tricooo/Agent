package com.tricoq.domain.agent.model.valobj;

/**
 * RAG 检索观测字段名。
 * <p>
 * qa_* 字段用于从 Advisor context 透传到 ChatResponse.metadata，再进入 retrieval SSE 事件。
 * DocumentMetadata 字段用于描述每个 chunk 自身的召回、融合、rerank 归因信息。
 */
public final class RagObservationKeys {

    /**
     * RAG 问答观测字段前缀；只有该前缀的字段会从 ChatResponse.metadata 进入 retrieval SSE 事件。
     */
    public static final String QA_PREFIX = "qa_";

    private RagObservationKeys() {
    }

    public static final class AdvisorContext {

        /**
         * 渲染进 Prompt 的知识库上下文，不直接作为 qa_* 观测字段透出。
         */
        public static final String QUESTION_ANSWER_CONTEXT = "question_answer_context";

        private AdvisorContext() {
        }
    }

    public static final class Qa {

        /**
         * 外部传入的知识库过滤表达式文本，解析后会参与向量库过滤。
         */
        public static final String FILTER_EXPRESSION = "qa_filter_expression";

        /**
         * 最终进入上下文的文档列表，用于评测报告展开 chunk 归因。
         */
        public static final String RETRIEVED_DOCUMENTS = "qa_retrieved_documents";

        /**
         * rerank 前的候选文档列表，用于诊断候选池覆盖与 rerank 淘汰原因。
         */
        public static final String PRE_RERANK_DOCUMENTS = "qa_pre_rerank_documents";

        /**
         * 最终进入上下文的文档数量。
         */
        public static final String RETRIEVED_DOCUMENT_COUNT = "qa_retrieved_document_count";

        /**
         * 本次 RAG 是否为空召回。
         */
        public static final String RETRIEVAL_EMPTY = "qa_retrieval_empty";

        /**
         * 最终检索安全阈值，通常来自 SearchRequest 装配后的相似度阈值。
         */
        public static final String SIMILARITY_THRESHOLD = "qa_similarity_threshold";

        /**
         * 候选池召回阶段实际使用的相似度阈值，用于观察 rerank 前召回是否被放宽。
         */
        public static final String CANDIDATE_SIMILARITY_THRESHOLD = "qa_candidate_similarity_threshold";

        /**
         * 最终文档中的最低分数。
         */
        public static final String MIN_RETRIEVED_SCORE = "qa_min_retrieved_score";

        /**
         * 最终文档中的最高分数。
         */
        public static final String MAX_RETRIEVED_SCORE = "qa_max_retrieved_score";

        /**
         * 上下文装配允许的最大字符预算。
         */
        public static final String CONTEXT_MAX_CHARS = "qa_context_max_chars";

        /**
         * 实际装配进 Prompt 的上下文字符数。
         */
        public static final String CONTEXT_ACTUAL_CHARS = "qa_context_actual_chars";

        /**
         * 上下文装配后被保留的文档数量。
         */
        public static final String CONTEXT_SELECTED_COUNT = "qa_context_selected_count";

        /**
         * 因上下文预算不足被丢弃的文档数量。
         */
        public static final String CONTEXT_DROPPED_COUNT = "qa_context_dropped_count";

        /**
         * 上下文内容是否发生截断。
         */
        public static final String CONTEXT_TRUNCATED = "qa_context_truncated";

        /**
         * 本次是否执行真实 rerank；false 表示只做 passthrough 占位排序。
         */
        public static final String RERANK_APPLIED = "qa_rerank_applied";

        /**
         * rerank 模式，例如 PASSTHROUGH。
         */
        public static final String RERANK_MODE = "qa_rerank_mode";

        /**
         * rerank 前候选文档数量。
         */
        public static final String RERANK_CANDIDATE_COUNT = "qa_rerank_candidate_count";

        /**
         * rerank 后最终文档数量。
         */
        public static final String RERANK_FINAL_COUNT = "qa_rerank_final_count";

        /**
         * rerank 模型或服务降级时的失败原因；为空表示未触发异常降级。
         */
        public static final String RERANK_FAILURE_REASON = "qa_rerank_failure_reason";

        /**
         * 本次 rerank 期望或实际使用的模型名称，用于区分不同重排模型。
         */
        public static final String RERANK_MODEL_NAME = "qa_rerank_model_name";

        /**
         * 本次 rerank 调用的服务端点，用于排查本地服务和远端服务配置。
         */
        public static final String RERANK_ENDPOINT = "qa_rerank_endpoint";

        /**
         * 本次 rerank query 构造策略，例如 ORIGINAL / ORIGINAL_PLUS_VARIANT / PER_VARIANT_RERANK_RRF /
         * FUSION_AWARE_RERANK_RRF。
         */
        public static final String RERANK_QUERY_POLICY = "qa_rerank_query_policy";

        /**
         * 本次实际传给 reranker 的 query 文本。
         */
        public static final String RERANK_QUERY_TEXT = "qa_rerank_query_text";

        /**
         * rerank 后是否触发 final context coverage guard。
         */
        public static final String RERANK_COVERAGE_GUARD_APPLIED = "qa_rerank_coverage_guard_applied";

        /**
         * coverage guard 补回 final context 的文档数量。
         */
        public static final String RERANK_COVERAGE_GUARD_ADDED_COUNT = "qa_rerank_coverage_guard_added_count";

        /**
         * query rewrite 模式，例如 PASSTHROUGH / HEURISTIC_MULTI_QUERY。
         */
        public static final String QUERY_REWRITE_MODE = "qa_query_rewrite_mode";

        /**
         * 配置请求的 query rewrite 策略，例如 LLM_MULTI_QUERY。
         */
        public static final String QUERY_REWRITE_REQUESTED_POLICY = "qa_query_rewrite_requested_policy";

        /**
         * 本次实际参与检索的 query variant 数量。
         */
        public static final String QUERY_VARIANT_COUNT = "qa_query_variant_count";

        /**
         * 本次实际参与检索的 query variant 文本列表。
         */
        public static final String QUERY_VARIANT_TEXTS = "qa_query_variant_texts";

        /**
         * query rewrite 降级或失败原因。
         */
        public static final String QUERY_REWRITE_FAILURE_REASON = "qa_query_rewrite_failure_reason";

        /**
         * query rewrite 总耗时，单位毫秒。
         */
        public static final String QUERY_REWRITE_ELAPSED_MS = "qa_query_rewrite_elapsed_ms";

        /**
         * query rewrite 策略尝试轨迹。
         */
        public static final String QUERY_REWRITE_ATTEMPT_TRACE = "qa_query_rewrite_attempt_trace";

        private Qa() {
        }
    }

    public static final class DocumentMetadata {

        /**
         * 文档所属知识库标签，用于 PgVector 过滤。
         */
        public static final String KNOWLEDGE = "knowledge";

        /**
         * 一次上传生成的知识库批次 ID。
         */
        public static final String RAG_ID = "rag_id";

        /**
         * 原始文件名，兼容既有知识库 schema。
         */
        public static final String FILE_NAME = "file_name";

        /**
         * 原始来源路径或文件名，用于报告和 Prompt attribution 展示。
         */
        public static final String SOURCE_PATH = "sourcePath";

        /**
         * 当前 chunk 在原始文档切分结果中的序号。
         */
        public static final String CHUNK_INDEX = "chunkIndex";

        /**
         * 当前文档总 chunk 数。
         */
        public static final String TOTAL_CHUNKS = "totalChunks";

        /**
         * chunk 所属父级章节，当前 Phase A 可为空。
         */
        public static final String PARENT_SECTION = "parentSection";

        /**
         * chunk 所属标题路径，当前 Phase A 可为空。
         */
        public static final String HEADING_PATH = "headingPath";

        /**
         * 文档来自哪种检索模式，例如 HYBRID。
         */
        public static final String RETRIEVAL_MODE = "retrievalMode";

        /**
         * 文档被哪条召回分支命中，例如 VECTOR、KEYWORD、VECTOR_KEYWORD。
         */
        public static final String RETRIEVAL_SOURCE = "retrievalSource";

        /**
         * HYBRID 融合后的 RRF 分数。
         */
        public static final String RRF_SCORE = "rrfScore";

        /**
         * RRF 融合公式中的平滑参数 k。
         */
        public static final String RRF_K = "rrfK";

        /**
         * keyword 分支未参与或无结果时的原因。
         */
        public static final String KEYWORD_SKIPPED_REASON = "keywordSkippedReason";

        /**
         * 文档在向量召回结果中的排名。
         */
        public static final String VECTOR_RANK = "vectorRank";

        /**
         * 文档在向量召回结果中的原始相似度分数。
         */
        public static final String VECTOR_SCORE = "vectorScore";

        /**
         * 文档在关键词全文检索结果中的排名。
         */
        public static final String KEYWORD_RANK = "keywordRank";

        /**
         * 文档在关键词全文检索结果中的原始分数。
         */
        public static final String KEYWORD_SCORE = "keywordScore";

        /**
         * rerank 前的候选排序位置。
         */
        public static final String BEFORE_RERANK_RANK = "beforeRerankRank";

        /**
         * rerank 后的最终排序位置。
         */
        public static final String RERANK_RANK = "rerankRank";

        /**
         * 当前文档是否经过真实 rerank。
         */
        public static final String RERANK_APPLIED = "rerankApplied";

        /**
         * 当前文档使用的 rerank 模式。
         */
        public static final String RERANK_MODE = "rerankMode";

        /**
         * rerank 模型对当前文档和查询相关性的打分。
         */
        public static final String RERANK_SCORE = "rerankScore";

        /**
         * 多 query variant 独立 rerank 后的 RRF 融合分数。
         */
        public static final String RERANK_FUSION_SCORE = "rerankFusionScore";

        /**
         * fusion-aware rerank 中，query-fusion 排名贡献的弱加成分数。
         */
        public static final String QUERY_FUSION_BOOST_SCORE = "queryFusionBoostScore";

        /**
         * fusion-aware rerank 最终排序分数。
         */
        public static final String FUSION_AWARE_SCORE = "fusionAwareScore";

        /**
         * fusion-aware rerank 使用的 query-fusion 加成权重。
         */
        public static final String FUSION_AWARE_WEIGHT = "fusionAwareWeight";

        /**
         * 当前 chunk 被多少个 rerank query variant 命中。
         */
        public static final String RERANK_VARIANT_HIT_COUNT = "rerankVariantHitCount";

        /**
         * 当前 chunk 在所有 rerank query variant 结果中的最佳排名。
         */
        public static final String BEST_RERANK_VARIANT_RANK = "bestRerankVariantRank";

        /**
         * 当前 chunk 命中的 rerank query variant 编号列表。
         */
        public static final String RERANK_VARIANT_INDEXES = "rerankVariantIndexes";

        /**
         * 当前 chunk 命中的 rerank query variant 详情列表，元素包含 index / rank。
         */
        public static final String RERANK_VARIANT_HITS = "rerankVariantHits";

        /**
         * 当前文档是否由 rerank coverage guard 补回最终上下文。
         */
        public static final String COVERAGE_GUARD_ADDED = "coverageGuardAdded";

        /**
         * 当前 chunk 最先由第几个 query variant 召回，1 表示原始 query。
         */
        public static final String QUERY_VARIANT_INDEX = "queryVariantIndex";

        /**
         * 当前 chunk 最先由哪个 query variant 文本召回。
         */
        public static final String QUERY_VARIANT_TEXT = "queryVariantText";

        /**
         * 当前 chunk 在对应 query variant 召回结果内的排序。
         */
        public static final String QUERY_VARIANT_RANK = "queryVariantRank";

        /**
         * 多 query variant 融合后的 RRF 分数。
         */
        public static final String QUERY_FUSION_SCORE = "queryFusionScore";

        /**
         * 多 query variant 融合后的候选池排序。
         */
        public static final String QUERY_FUSION_RANK = "queryFusionRank";

        /**
         * 当前 chunk 被多少个 query variant 命中。
         */
        public static final String QUERY_VARIANT_HIT_COUNT = "queryVariantHitCount";

        /**
         * 当前 chunk 在所有命中 query variant 中的最佳排名。
         */
        public static final String BEST_QUERY_VARIANT_RANK = "bestQueryVariantRank";

        /**
         * 当前 chunk 命中的 query variant 编号列表。
         */
        public static final String QUERY_VARIANT_INDEXES = "queryVariantIndexes";

        /**
         * 当前 chunk 命中的 query variant 详情列表，元素包含 index / rank / text。
         */
        public static final String QUERY_VARIANT_HITS = "queryVariantHits";

        private DocumentMetadata() {
        }
    }
}
