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

        private DocumentMetadata() {
        }
    }
}
