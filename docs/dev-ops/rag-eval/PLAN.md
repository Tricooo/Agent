# RAG Eval 自动化 — 计划与状态

> 这份文档是 RAG 评测链路工作的 single source of truth。任何接手会话先读这里。
> 最后更新：2026-05-23（D26：外部样本消融验证计划）

## 目录组织

```
docs/dev-ops/rag-eval/
├── PLAN.md                      ← 本文（single source of truth）
├── rag_eval_runner.py           ← 评测脚本
├── cases.json                   ← 14 条评测用例（脚本与 cases 同目录是 Path(__file__).with_name 硬约定）
├── viewer/                      ← 单页评测看板；读取 results/** 后生成 viewer/data/dataset.{json,js}
└── results/
    ├── _archive/                ← F-fix 之前，旧 schema 无 retrieved/score/empty 三列，不可与 ffix 互比 score
    │   ├── rag-eval-result.md                ← Apr 28，0.65 误杀证据（answer 文本可证）
    │   ├── rag-eval-result-bsymmetric.md     ← auto chain 死路径反证
    │   └── rag-eval-result-t06{0,5}-triangle.md  ← 旧 schema threshold 三角
    ├── ffix/                    ← F-fix 之后，三列俱全，可互比
    │   ├── rag-eval-result-baseline-ffix.md      ← ★ 当前 baseline（10/10，threshold=0.60，May 2）
    │   ├── rag-eval-result-ffix.md               ← F-fix 首跑
    │   ├── rag-eval-result-t055-triangle-ffix.md ← threshold 三角 0.55
    │   └── rag-eval-result-rag05-ffix.md         ← A+B fix RAG-05 单跑验证
    ├── step3-chunker-v1/         ← chunkSize=800 A/A test
    ├── step3-chunker-v2/         ← chunkSize=400 实测，v2 gate 3/4
    ├── step3-chunker-v3/         ← chunkSize=250 实测，v3 gate 4/4 ✓ Phase A 收口（May 5 22:17）
    ├── step3-control-v3/         ← low-newline control-only，RAG-11/12/13/14 通过（May 6）
    ├── step3-full-v3-with-control/ ← full 14 regression，原 10 条不退化 + control 通过（May 6）
    ├── step4-hybrid-rrf/         ← Hybrid + RRF full 14 regression + top-K attribution（May 8）
    ├── step4-vector-ablation/    ← 同代码/同数据下 VECTOR 对照组（May 8）
    ├── step4.1-hybrid-calibrated/ ← Hybrid keyword gating 校准结果（May 9）
    ├── step5-rerank-observe/     ← rerank passthrough 观测字段 smoke（May 9）
    ├── step5.1-rerank-abstraction/ ← DocumentReranker 抽象接入 smoke（May 13）
    ├── step5.2-http-rerank-live/   ← 本地 bge reranker HTTP 接入 smoke（May 14）
    ├── step5.3-rerank-ab/          ← PASSTHROUGH vs LOCAL_BGE 全量 A/B（May 14）
    ├── step5.4-rerank-engineering/ ← 配置化 / fallback smoke / 生产化口径（May 14）
    ├── step6.0-rewrite-pipeline/   ← Query rewrite pipeline / fallback smoke（May 20）
    ├── step6.1-multi-query/        ← Query rewrite 首轮 RAG-10 smoke（May 18）
    ├── step6.2-coverage-guard/     ← Rerank coverage guard RAG-10 smoke + 窄回归（May 18）
    ├── step6.3-rag-fusion/         ← Query-variant RRF / RAG-fusion 窄回归（May 18）
    ├── step6.4-query-aware-rerank/ ← all-hit 观测 + ORIGINAL_PLUS_VARIANT smoke（May 19）
    ├── step6.5-per-variant-rerank-rrf/ ← PER_VARIANT_RERANK_RRF 全量验证（May 19）
    ├── step6.6-fusion-aware-rerank-rrf/ ← FUSION_AWARE_RERANK_RRF 实验收口（May 19）
    ├── step6.7-llm-query-rewrite/  ← LLM_MULTI_QUERY / domain hints 实验（May 21）
    ├── step6.8-knowledge-base-profile/ ← 自动 KnowledgeBaseProfile + query-time selection（May 23）
    ├── step6.9-llm-profile/        ← LLM profile hybrid-v2 验证（May 23）
    ├── step7-answer-context-salience/ ← Answer Generation / Context Salience 首轮验证（May 23）
    └── external-ablation/          ← 外部样本消融验证，计划中
```

> 归档原则：按"评测口径是否一致"分。F-fix 是 schema 硬边界——之前的产物没有 `retrieved/score/empty` 三列，永久不可与之后互比 score。
>
> Viewer 更新原则：新增 / 移动 results 下报告后，运行 `cd docs/dev-ops/rag-eval/viewer && python3 build_dataset.py` 重新生成 `data/dataset.json` 与 `data/dataset.js`。`viewer/data/` 是派生产物，报告真相源仍是 `results/` 下 markdown。

## 1. 总目标

让 `rag_demo` 这个 agent 的 RAG 检索观测信号（`retrieved_document_count`、`score_range`、`retrieval_empty`、`similarity_threshold`、`context_selected/dropped/truncated`、`context_chars` 等 11 个 `qa_*` 字段）稳定从 backend 通过 SSE `type=retrieval` 事件下发到 Python runner，落入 markdown 评测报告的 `retrieved/score/empty` 三列。

这是 RAG 评测自动化基础设施的第一步，后续叠 threshold triangle、paraphrase regression、RAGAS、rerank、hybrid。

## 2. 当前进展

### 2.1 已完成 / 已验证

- **F-fix 已上线并生效**：在 `FixedAgentExecuteStrategy.java` 加 `emitRetrievalIfPresent()` helper，把 `chatResponse.metadata` 的 `qa_*` 字段拍平成 `type=retrieval` SSE 事件下发。
  - 主流程：`ai-agent-domain/src/main/java/com/tricoq/domain/agent/service/fixed/FixedAgentExecuteStrategy.java:46-87`（改用 `.call().chatResponse()`）
  - Helper：同文件 `:105-126`
  - 共享 DTO：`ai-agent-domain/src/main/java/com/tricoq/domain/agent/model/entity/AutoAgentRetrievalSseEntity.java`
  - 验证证据：`docs/dev-ops/rag-eval/results/ffix/rag-eval-result-ffix.md`
    - RAG-04: retrieved=3, score 0.6305..0.6481, empty=false
    - RAG-06: retrieved=0, empty=true
    - RAG-07: retrieved=3, score 0.6185..0.6645
- 评测 runner score_mode 区分（literal vs manual）已落地。
- F-fix 链路下 threshold=0.60 baseline：`results/ffix/rag-eval-result-ffix.md`（May 1 03:21，三列俱全）
- F-fix 链路下 threshold=0.55 三角：`results/ffix/rag-eval-result-t055-triangle-ffix.md`（May 2 03:02，RAG-04 / 06 / 07 三列俱全，预热后跑通）
- threshold=0.65 沿用 `results/_archive/rag-eval-result.md`（Apr 28，旧 schema 无三列，但答案可证 RAG-04 误杀）
- 旧 schema 残留（不可比，不再补跑）：`results/_archive/rag-eval-result-t060-triangle.md` / `results/_archive/rag-eval-result-t065-triangle.md`（`rag-eval-result-test-triangle.md` 在 commit `a9fbf7e` 中已删除）
- **A+B fix（commit `a9fbf7e`）已上线**：DeepSeek `read-timeout 25s→120s`（A） + `AgentExecutionFacade.error()` 发 SSE `type=error` 帧而非 `emitter.completeWithError(t)`（B），runner 解析 error 帧到 markdown details。
  - A 验证：`results/ffix/rag-eval-result-rag05-ffix.md`（May 2，RAG-05 单跑 duration=26702ms completed=true，之前固定 30s timeout）
  - B 验证：`results/ffix/rag-eval-result-baseline-ffix.md`（May 2 04:14，10 条全跑）的 RAG-04 行触发偶发 `RestClientException`，markdown details 直接显示 `error: RestClientException: ...`，不再 silent connection reset
- **F-fix 链路完整 baseline**：`results/ffix/rag-eval-result-baseline-ffix.md`（May 2 04:14，threshold=0.60，10 条全跑，9/10 happy + 1 偶发）。这是后续 chunking / hybrid / rerank 的对照基线。
- **Step 3 Phase A — 参数化基础设施完成（May 5）**：
  - 3.1 `ai-agent-boot/src/main/java/com/tricoq/config/AiAgentConfig.java` TokenTextSplitter 5 参数化；`ai-agent-boot/src/main/resources/application-dev.yml` 加 chunker section（chunk-size=800 = Spring AI 默认值，基础设施就绪，调参不再需要改 Java）
  - 3.2 `ai-agent-domain/.../service/rag/RagService.java` chunk 级 metadata：chunkIndex / totalChunks / sourcePath（填充）/ parentSection / headingPath（Phase B 留空，schema 一次到位）
  - 3.3 `ai-agent-domain/.../element/RagAnswerAdvisor.java` 渲染端加来源行（buildSourceLine helper，graceful：sourcePath 空时返回 "" 兼容旧 chunk）
  - 3.4 vector store TRUNCATE + 重灌（用户操作，May 5）
  - 3.5 评测 10/10 completed（需预热后跑）：`results/step3-chunker-v1/rag-eval-result-step3-chunker-v1.md`
  - **3.6 Gate 判定：部分达标**（详见 §3 D5）
- **Step 3 Phase A — v2 / v3 全部完成，决策门通过（May 5 22:17）**：
  - v2 产物：`results/step3-chunker-v2/rag-eval-result-step3-chunker-v2.md`
  - v2 结果：RAG-04 max 0.6481→0.6715；RAG-07 max 0.6645→0.6634；gap -0.0164→+0.0081；gate 3/4
  - v3 产物：`results/step3-chunker-v3/rag-eval-result-step3-chunker-v3.md`（May 5 22:17）
  - v3 配置：`application-dev.yml` `chunk-size=250` / `min-chunk-size-chars=109`，用户已 TRUNCATE `vector_store_openai` 并重传 `rag_demo`（chunks v1=4 / v2=7 / v3=10）
  - v3 结果：RAG-04 max **0.7406**（≥0.70 决策门 ✓ 跨过）；RAG-07 max 0.6726；gap **+0.0680**（v2 +0.0081 → v3 扩大 8×）；10/10 completed；empty 仍 RAG-06/08；gate **4/4** → Phase A 收口
  - 详细决策见 §3 D7
- **Step 3 Phase A.5 — low-newline control + full regression 完成（May 6）**：
  - 新增控制文档：`docs/dev-ops/rag-file/control-cn-low-newline.txt` / `control-en-long-paragraph.txt`
  - 新增 case：RAG-11 / RAG-12 / RAG-13 / RAG-14
  - control-only 产物：`results/step3-control-v3/rag-eval-result-step3-control-v3.md`
  - full 14 regression 产物：`results/step3-full-v3-with-control/rag-eval-result-step3-full-v3-with-control.md`
  - 结果：RAG-11/13 正例命中，RAG-12/14 拒答；原 RAG-01..10 不退化
  - 详细决策见 §3 D9
- **Step 4 Hybrid + RRF — 功能闭环完成（May 8）**：
  - 实现：pgvector 向量召回 + PostgreSQL FTS 关键词召回 + 应用层 RRF 合并
  - 配置入口：`AiClientAdvisorDTO.RagAnswer` 新增 `retrievalMode/vectorTopK/keywordTopK/rrfK`
  - 可观测性：合并后 `Document.score` 为 `rrfScore`，metadata 记录 `retrievalSource/vectorRank/vectorScore/keywordRank/keywordScore`
  - full 14 regression 产物：`results/step4-hybrid-rrf/rag-eval-result-step4-hybrid-rrf.md`
  - attribution 产物：`results/step4-hybrid-rrf/rag-eval-result-step4-hybrid-rrf-attribution.md`
  - VECTOR 对照产物：`results/step4-vector-ablation/rag-eval-result-step4-vector-ablation.md`
  - 结果：14/14 completed；RAG-06/RAG-08 仍 empty 拒答；RAG-10 保持既有 literal 表达漂移；A/B 显示当前 Hybrid 没有带来答案指标提升，且 RAG-04 目标 chunk 被 RRF 从向量第 1 降到第 3
  - 详细决策见 §3 D10
- **Step 4.1 Hybrid calibration — 已收口（May 9）**：
  - 目标：规避 Step 4 中关键词全文检索引入的负面排序效果，而不是宣称 Hybrid 已经带来稳定指标提升。
  - 实现：收紧 `buildKeywordQuery` 的泛词过滤 / keyword gating，使 FTS 只在足够强的技术标识符场景下参与。
  - 验证产物：`results/step4.1-hybrid-calibrated/rag-eval-result-step4.1-hybrid-calibrated.md`
  - 关键观测：RAG-04 目标 chunk `grafana-mcp-tools-guide.md#5` 回到第 1；RAG-07 不再引入跨文档 keyword-only 噪声。
  - 详细决策见 §3 D11
- **Step 5 Rerank observe — 观测地基完成（May 9 / May 13）**：
  - 现状：已接入 rerank passthrough 占位，不改变最终排序，只写入 rerank 前后观测字段。
  - document 级 metadata：`beforeRerankRank/rerankRank/rerankApplied/rerankMode`
  - context / SSE 级 qa metadata：`qa_rerank_applied/qa_rerank_mode/qa_rerank_candidate_count/qa_rerank_final_count`
  - 候选池阈值：`candidateSimilarityThreshold` 已加入配置、fallback 和 `qa_candidate_similarity_threshold` 观测。
  - 字段 schema：`RagObservationKeys` 已集中维护 `qa_*` 与 document metadata 字段名。
  - 验证产物：`results/step5-rerank-observe/rag-eval-result-step5-rerank-observe.md`
  - 当前边界：还没有真实模型 reranker；下一步是 Step 5.1，先抽象 `DocumentReranker` 接口并接入默认 passthrough 实现。
  - 详细决策见 §3 D12
- **Step 5.1 Rerank abstraction — 抽象接入完成（May 13）**：
  - 实现：新增 `DocumentReranker` 接口、`RerankResult` 结果对象、`PassthroughDocumentReranker` 默认实现。
  - 接入：`RagAnswerAdvisor` 通过构造器依赖 `DocumentReranker`，保留 2 参数 / 3 参数兼容构造器，默认 fallback 到 passthrough。
  - 行为边界：当前仍不改变排序；`RerankResult` 只承载排序结果和观测元数据。
  - 验证产物：`results/step5.1-rerank-abstraction/rag-eval-result-step5.1-rerank-abstraction-smoke.md`
  - 关键观测：RAG-04 `rerank applied=false`、`mode=PASSTHROUGH`、`candidates=4`、`final=4`，4 个 document 的 `beforeRerankRank == rerankRank`。
  - 详细决策见 §3 D13
- **Step 5.2 HTTP reranker — 本地 bge reranker 接入完成（May 14）**：
  - 实现：新增 `HttpDocumentReranker`，通过本地 Python FastAPI 服务 `127.0.0.1:18080/rerank` 调用 `bge-reranker-v2-m3`。
  - 策略入口：新增 `RerankPolicy` 与 `DocumentRerankerFactory`，`AiClientAdvisorDTO.RagAnswer.rerankPolicy` 从数据库 `ext_param` 进入 advisor 装配，空值 / 未知值默认 fallback 到 `PASSTHROUGH`。
  - 失败兜底：HTTP 异常、非 2xx、空 body、响应解析失败、空 results、非法 index 均回退 passthrough，并通过 `RerankResult.failureReason` 保留原因。
  - 观测字段：runner 展示 `rerankScore`，document attribution 可同时看到 `beforeRerankRank/rerankRank/rerankScore/rerankMode`。
  - 验证产物：`results/step5.2-http-rerank-live/rag-eval-result-step5.2-factory-rag04.md` / `rag-eval-result-step5.2-factory-rag07.md`
  - 关键观测：RAG-04 `literal_hit=3/3`，`rerank applied=true`、`mode=LOCAL_BGE`，目标 chunk #5 保持第 1；RAG-07 仍保持拒答方向。
  - 当前边界：Python rerank 服务需独立启动；HTTP URL 仍硬编码为本地 POC 地址。
  - 详细决策见 §3 D14
- **Step 5.3 Rerank A/B — 全量归因完成（May 14）**：
  - 对照组：DB `rerankPolicy=PASSTHROUGH`，armory 重装配后跑 14 条 case。
  - 实验组：DB `rerankPolicy=LOCAL_BGE`，本地 Python rerank 服务健康后跑同一批 14 条 case。
  - 验证产物：`results/step5.3-rerank-ab/rag-eval-result-step5.3-passthrough.md` / `rag-eval-result-step5.3-local-bge.md`
  - 关键结论：答案指标未进一步提升也未退化；`LOCAL_BGE` 改变 12/14 条的 chunk 顺序，说明二阶段排序链路真实生效。
  - 正向价值：RAG-05、RAG-07、RAG-13、RAG-14 等 case 中，rerank 能把更贴近 query 的 chunk 前移，给面试讲解提供了“向量/RRF 初排 + query-chunk 交互重排”的可观测证据。
  - 边界：RAG-10 仍是 2/5，说明 rerank 只能重排已召回候选，不能弥补候选池本身没有覆盖完整答案、或生成阶段没有展开细节的问题。
  - 详细决策见 §3 D15
- **Step 5.4 Rerank 工程化收口 — 已完成（May 14）**：
  - 实现：`HttpDocumentReranker` 的 enabled / endpoint / connect-timeout / read-timeout / model-name 配置化；fallback 依赖 `PassthroughDocumentReranker` bean；`qa_rerank_failure_reason/qa_rerank_model_name/qa_rerank_endpoint` 透出到 retrieval SSE 与 runner 报告。
  - 验证产物：`results/step5.4-rerank-engineering/rag-eval-result-step5.4-live-rag04.md` / `rag-eval-result-step5.4-fallback-rag04.md` / `rag-eval-result-step5.4-disabled-rag04.md`
  - 关键结论：正常服务时 `LOCAL_BGE` applied=true；服务不可用或配置禁用时自动降级 `PASSTHROUGH`，主链路仍 completed=true，报告能看到 failure reason。
  - 详细决策见 §3 D16
- **Step 6.1 Query rewrite 首轮 smoke — 已完成（May 18）**：
  - 实现方向：`QueryRewriter / RewriteResult / RewritePolicy / QueryRewriterFactory` 接入 `RagAnswerAdvisor`，原 query 永远保留为 variant 1，多 variant 同步检索后按 rank 轮询去重合并，再进入既有 rerank/context assembly。
  - 配置入口：`ai_client_advisor.rag_advisor_grafana.ext_param.rewritePolicy=HEURISTIC_MULTI_QUERY`；重启/改 DB 后仍需 `POST /api/v1/agent/armory_agent {"agentId":"rag_demo"}` 重装配。
  - 验证产物：`results/step6.1-multi-query/rag-eval-result-step6.1-rag10.md`
  - 关键观测：报告显示 `query_rewrite mode=HEURISTIC_MULTI_QUERY / variants=2`，document attribution 出现 `queryVariantIndex/queryVariantText/queryVariantRank`，证明 rewrite 机制进入运行链路。
  - 当前边界：RAG-10 literal 仍为 `2/5`，缺 `正常范围/警告范围/危险范围`；完整“内存数据解释”段在 source lines 160-164 / vector chunkIndex=5，但本轮最终上下文没有稳定把该 chunk 送入答案。
  - 追加观测：已新增 `qa_pre_rerank_documents` / runner `pre_rerank_documents`，用于对比 rerank 前候选池和最终 `documents`。下一步应先诊断 candidate coverage、rerank final topK 和 answer prompt，不直接跳 LLM rewrite。
  - pre-rerank 复测结论：用户重启 8099 后，RAG-10 报告显示 chunkIndex=5 已进入 `pre_rerank_documents` 第 4 位，但最终 `documents` 不包含 chunk 5；因此当前断点收窄为 `LOCAL_BGE` rerank/final topK 淘汰目标 chunk。下一步建议先跑 `rerankPolicy=PASSTHROUGH` 对照。
  - PASSTHROUGH 对照结论：严格顺序切 `rerankPolicy=PASSTHROUGH` 并 armory 后，RAG-10 报告显示 chunkIndex=5 进入最终 `documents` 第 4 位，literal_hit 从 `2/5` 变为 `5/5`。测试后已恢复 `rerankPolicy=LOCAL_BGE`。下一步方向转为 rerank 保护策略 / final context coverage guard，而不是继续强化 query rewrite。
- **Step 6.2 Rerank coverage guard 最小验证版 — RAG-10 live smoke 通过（May 18）**：
  - 实现方向：在真实 rerank 后保护非原始 query variant 的前 2 个候选；如果保护候选缺席 final documents，则补回并优先替换尾部非保护 chunk，不扩大 finalTopK。
  - 观测字段：新增 `qa_rerank_coverage_guard_applied` / `qa_rerank_coverage_guard_added_count` / `coverageGuardAdded`，runner 报告展示 `coverage_guard` 与 document 级标记。
  - 当前验证：`mvn -q -pl ai-agent-domain -am compile -DskipTests`、`mvn -q -pl ai-agent-boot -am compile -DskipTests`、`python3 -m py_compile docs/dev-ops/rag-eval/rag_eval_runner.py`、`git diff --check` 已通过。
  - live smoke：用户重启 8099 后，armory 返回装配成功，reranker health `loaded=true`；RAG-10 completed=true，`coverage_guard applied=true / added=1`，chunkIndex=5 以 `coverageGuardAdded=True` 进入最终 `documents` 第 3 位，literal_hit=`5/5`。
  - 验证产物：`results/step6.2-coverage-guard/rag-eval-result-step6.2-rag10-guard.md`
  - 窄回归：RAG-04 / RAG-07 / RAG-10 / RAG-14 completed=true；RAG-04 仍 `3/3` 且 guard=false；RAG-07 保持拒答且 guard=false；RAG-10 `5/5` 且 guard=true added=1；RAG-14 保持英文负证据拒答且 guard=false。
  - 回归产物：`results/step6.2-coverage-guard/rag-eval-result-step6.2-narrow-regression.md`
  - 提交状态：已提交到 `main`，commit `1555cfb feat(rag): 增加 rerank coverage guard`。
  - 当前边界：`HeuristicMultiQueryRewriter` 是针对 RAG-10 失败模式的启发式验证器，证明的是 query rewrite / multi-query / pre-rerank 观测 / coverage guard 链路可用，不等于已经具备强通用 rewrite 能力。
- **Step 6.3 Query-variant RRF / RAG-fusion — 窄回归通过（May 18）**：
  - 总目标：把 Step 6 从“启发式验证 query rewrite 猜想”升级为“可配置、可观测、可回退、可评测的 query rewrite + multi-query fusion 闭环”。
  - 已实现 `RAG-fusion / query-variant RRF`：
    - 触发场景：多路 query 已能召回候选，但结果重复、排序不稳定，或补充 query 找回的关键 chunk 容易在 candidate pool / rerank 阶段被挤掉。
    - 原缺口：`mergeQueryVariantResults(...)` 只是 round-robin 去重，能验证 multi-query 链路，但不能表达“多路 query 共识越强，chunk 越应该靠前”。
    - 解决问题：按 query variant 维度做 RRF，同一 chunk 被多路召回时累加 `1 / (rrfK + rank)`；输出融合后的 candidate pool，再进入现有 rerank / context assembly。
    - 验证标准：RAG-10 关键 chunk 仍进入 pre-rerank candidate pool；RAG-04 不退；RAG-07 / RAG-14 拒答边界不破；报告能解释 chunk 是多路共识前移还是补充 query 单路带入。
    - 窄回归产物：`results/step6.3-rag-fusion/rag-eval-result-step6.3-rag-fusion-narrow.md`。
    - 窄回归结果：RAG-04 completed=true 且 literal_hit=`3/3`；RAG-10 completed=true 且 literal_hit=`5/5`；RAG-07 / RAG-14 仍保持拒答方向。
    - 关键证据：RAG-10 中 `grafana-mcp-tools-guide.md#2` 从 Step 6.2 pre-rerank 第 7 位前移到 Step 6.3 pre-rerank 第 1 位，metadata 显示 `queryFusionScore=0.0313`、`queryVariantHitCount=2`、`queryVariantIndexes=[1, 2]`。
    - 当前边界：RAG-fusion 改善的是 rerank 前 candidate pool；最终 `documents` 仍由 LOCAL_BGE rerank 与 coverage guard 决定。RAG-10 中 `chunk=2` 虽被 fusion 提到 pre-rerank 第 1，但未进入最终 topK，说明下一步应看 query-aware rerank / guard 配置化，而不是继续单纯堆 query。
    - 2026-05-19 归因纠偏：RAG-10 `chunk=2` 的 report 行里 `queryVariantText` 显示原始问题，不代表它只由原始 query 命中；当前实现保留 first document 作为代表，只额外聚合 `queryVariantIndexes/queryVariantHitCount/bestRank`。因此 `chunk=2` 更准确地说是“原始 query 与 rewrite query 同时命中的高融合候选”，不能把它没进 final topK 简化为“rerank 没用 rewrite query”。
    - 进一步拆分后的问题模型：1. 只被补充 query 找回的 chunk，可能被原始 query rerank 低估；2. 多路 query 共同命中的 chunk，即使 `queryFusionScore/queryVariantHitCount` 很高，也可能因为 reranker 不消费 fusion 共识信号而被重新打下去。
    - Step 6.4 设计前置要求：先补全 all-hit 观测，例如 `queryVariantHits=[{index, rank, text}]` 或至少 `queryVariantTexts`，避免只显示代表 query 文本导致误判；再做 `ORIGINAL_PLUS_VARIANT` 最小验证。如果 RAG-10 仍依赖 coverage guard，再进入 fusion-aware rerank / final score blend，而不是继续堆 query。
  - 已实现 `fusion metadata`：
    - 触发场景：功能跑通但无法解释哪个 query variant 起作用、为什么某个 chunk 被前移或保留。
    - 解决问题：在 document metadata 和 runner 报告中展示 `queryFusionScore`、`queryFusionRank`、`queryVariantHitCount`、`bestQueryVariantRank`、`queryVariantIndexes`。
    - 验证标准：`pre_rerank_documents` / `documents` 都能展示 fusion 字段，且不破坏既有 `queryVariantIndex/queryVariantRank/rerankScore/coverageGuardAdded`。
  - 后续 P0 `LLMQueryRewriter` 最小版：
    - 触发场景：启发式规则覆盖不足，例如问题不包含固定意图词，或业务主题不在内存 / CPU / 磁盘 / 延迟 / 错误率列表里。
    - 解决问题：由 LLM 只生成检索 query，不直接生成答案；输出 JSON `{"queries":["原问题","补充检索 query 1","补充检索 query 2"]}`，限制最多 3 条，做去重、长度限制、空 query 过滤。
    - 降级链路：JSON 解析失败、超时、空结果或输出质量不合格时，按 `LLM_MULTI_QUERY -> HEURISTIC_MULTI_QUERY -> PASSTHROUGH` 回退，并写入 rewrite failure reason。
    - 验证标准：RAG-10 不低于现有结果；RAG-07 / RAG-14 不因 query 扩展误答；报告展示 rewrite mode、query 数量、query 文本、failure reason 和耗时。
  - 后续 P0 `rewrite A/B`：
    - 触发场景：新 rewrite 策略接上后，需要证明不是只为 RAG-10 单样本调参。
    - 对照策略：`PASSTHROUGH` / `HEURISTIC_MULTI_QUERY` / `LLM_MULTI_QUERY`。
    - 验证顺序：先跑 RAG-04 / RAG-07 / RAG-10 / RAG-14 窄回归，通过后再跑 14 条全量。
    - 观察指标：literal_hit / manual_pass、retrieved count、pre-rerank candidates、final documents、coverage guard 是否触发、延迟、rewrite failure reason。
  - 后续 P1 `coverage guard 配置化`：
    - 触发场景：coverage guard 已证明有用，但保护前 2 个非原始 query variant 候选仍是实验参数，不适合长期硬编码。
    - 配置建议：`coverageGuardEnabled`、`coverageGuardProtectedVariantRank`、`coverageGuardMaxAdded`。
    - 验证标准：默认兼容；关闭 guard 后回到纯 rerank；开启 guard 后能复现 RAG-10 on/off 差异并展示配置与触发结果。
  - 后续 P1 `query-aware rerank`：
    - 触发场景：candidate pool 已携带 query variant / fusion 信号，但 reranker 只接收一个 query 文本，并且不直接消费 `queryFusionScore/queryVariantHitCount`。
    - 问题边界：`ORIGINAL_PLUS_VARIANT` 主要验证“reranker 看到更完整检索意图”是否有收益；它不等价于 fusion-aware rerank，不能保证解决所有多路共识 chunk 被打下去的问题。
    - 可选策略：`ORIGINAL`、`ORIGINAL_PLUS_VARIANT`、`PER_VARIANT_RERANK_RRF`，后续再单独做 `FUSION_AWARE_RERANK_RRF`（rank-level 融合 per-variant rerank rank 与 queryFusionRank）。
    - 验证标准：RAG-10 不再依赖 coverage guard 或 guard 触发减少；RAG-07 / RAG-14 不误答；rerank latency 可接受；报告展示 `rerankQueryPolicy/rerankQueryText` 与 all-hit query variant 观测。
  - HyDE 定位：HyDE 是让 LLM 先写一段“假想答案 / 假想文档”，再用这段文本 embedding 检索真实 chunk。它可能让检索输入更接近知识库正文，但会引入编造方向、延迟、成本和归因复杂度；当前排在 P0/P1 之后，不作为下一步最小闭环。
  - 面试主线：Step 6.1 证明 multi-query 能把缺失 chunk 拉进候选池；Step 6.2 证明 final context 缺关键 chunk 才导致 RAG-10 2/5；Step 6.3 证明 multi-query 不是“多搜几次”，而是可以通过 RAG-fusion 形成可解释 candidate pool；下一步要证明 rerank 如何消费这些 fusion 信号，再补 LLMQueryRewriter + A/B 证明通用 rewrite 雏形。
  - **Step 6.4 思维过程（2026-05-19）**：
    - 起点不是“再加一个功能”，而是纠正归因：`queryVariantText` 只展示代表 query，不能用它判断一个 chunk 是否只由原 query 命中；真正判断命中来源要看 `queryVariantHitCount/queryVariantIndexes`，下一步必须补 all-hit 明细。
    - 工程分层要拆开：第一层是 candidate coverage，Step 6.1/6.3 已证明补充 query 能把目标 chunk 拉进候选池；第二层是 candidate fusion，RRF 让多路共识 chunk 前移；第三层是 rerank query awareness，当前 LOCAL_BGE 只看到一个 query 文本；第四层才是 fusion awareness，reranker 仍不消费 `queryFusionScore/hitCount/bestRank`。
    - 因此 Step 6.4 的最小闭环顺序是：先把 `queryVariantHits=[{index, rank, text}]` 打到 report，保证证据可读；再加 `rerankQueryPolicy=ORIGINAL_PLUS_VARIANT`，验证“让 reranker 看到原问题 + 改写意图”是否减少 coverage guard 依赖；如果仍靠 guard 才过，再讨论 fusion-aware rerank / score blend。
    - 面试表达边界：可以说我们发现了“多路召回已经解决覆盖，但二阶段排序没有充分利用多路信号”的工程断点；不能说 `ORIGINAL_PLUS_VARIANT` 一定等价解决 RAG-fusion，因为它只改变 reranker 输入文本，不改变 reranker 对 fusion 分数的消费方式。
  - **Step 6.4 live smoke 结论（2026-05-19）**：
    - all-hit 观测已验证：RAG-10 `pre_rerank_documents` 中 `chunk=2` 展示 `queryVariantHits=[{"index":1,...},{"index":2,...}]`，证明确实是原 query + rewrite query 双命中，不再只靠代表 `queryVariantText` 推断。
    - `ORIGINAL` 基线产物：`results/step6.4-query-aware-rerank/rag-eval-result-step6.4-original-rag10.md`。RAG-10 completed=true、literal_hit=`5/5`、`rerank_query policy=ORIGINAL`、`coverage_guard applied=true / added=1`。
    - `ORIGINAL_PLUS_VARIANT` 单 case 产物：`results/step6.4-query-aware-rerank/rag-eval-result-step6.4-original-plus-variant-rag10.md`。RAG-10 completed=true、literal_hit=`5/5`，但 `coverage_guard applied=true / added=2`，说明拼接 query 文本没有减少 guard 依赖。
    - `ORIGINAL_PLUS_VARIANT` 窄回归产物：`results/step6.4-query-aware-rerank/rag-eval-result-step6.4-original-plus-variant-narrow.md`。RAG-04 3/3；RAG-07 仍拒答；RAG-10 5/5；RAG-14 仍拒答。未观察到正例/拒答边界退化。
    - 当前决策：保留 all-hit observability；`ORIGINAL_PLUS_VARIANT` 可作为可配置实验策略，但不把它作为 RAG-10 的主修复。下一步优先做 coverage guard 配置化，或直接进入 fusion-aware rerank / score blend 小实验。
  - **Step 6.5 Per-Variant Rerank RRF 全量验证（2026-05-19）**：
    - 策略：新增 `PER_VARIANT_RERANK_RRF`，每个 query variant 对同一批 `pre_rerank_documents` 单独调用 BGE rerank，再用 RRF 融合各路 rerank rank，最后截断 final topK。
    - 产物：`results/step6.5-per-variant-rerank-rrf/rag-eval-result-step6.5-per-variant-rrf-narrow.md` 与 `results/step6.5-per-variant-rerank-rrf/rag-eval-result-step6.5-per-variant-rrf-full.md`。
    - 全量结论：14/14 completed；literal case 均命中；RAG-07 / RAG-12 / RAG-14 拒答边界未破；coverage guard 全量 `applied=false / added=0`。
    - RAG-10 关键结论：`PER_VARIANT_RERANK_RRF` 达到 literal_hit=`5/5` 且不再依赖 coverage guard；相比 `ORIGINAL` 与 `ORIGINAL_PLUS_VARIANT`，它证明收益来自“多路 query 分别 rerank 后再融合 rank”，不是简单拼接 query 文本。
    - 当前边界：该策略仍未显式保护 retrieval/query-fusion 第一名。RAG-10 中 `grafana-mcp-tools-guide.md#2` 是 `pre_rerank_documents #1`、`queryFusionRank=1`、`queryVariantHitCount=2`，但未进入 final topK；后续如继续优化，应单独做 `FUSION_AWARE_RERANK_RRF`，而不是把 raw score blend 混进当前策略。
  - **Step 6.6 Fusion-aware Rerank RRF 实验收口（2026-05-19）**：
    - 策略：新增 `FUSION_AWARE_RERANK_RRF`，在 `PER_VARIANT_RERANK_RRF` 的 rerank rank 融合结果上，弱融合 query-fusion 候选排名信号；当前公式为 `finalScore = rerankFusionScore + 0.3 * RRF(queryFusionRank)`。
    - 产物：`results/step6.6-fusion-aware-rerank-rrf/rag-eval-result-step6.6-fusion-aware-rag10.md`、`results/step6.6-fusion-aware-rerank-rrf/rag-eval-result-step6.6-fusion-aware-narrow.md`、`results/step6.6-fusion-aware-rerank-rrf/rag-eval-result-step6.6-fusion-aware-full.md`。
    - 验证结论：RAG-10 单跑 `5/5` 且 guard=0；窄回归 4/4 completed、guard=0；全量 14/14 completed、guard=0，但 RAG-10 为 `4/5`，漏了“警告范围”。
    - 归因：RAG-10 全量漏项更像生成表达波动，不是 ranking/context 断裂；同一策略下单跑与窄回归 RAG-10 均为 `5/5`，且最终上下文仍包含公式与阈值证据。
    - chunk2 结论：`grafana-mcp-tools-guide.md#2` 很重要但不是唯一必要证据。它承载内存公式证据；但 chunk3 也包含同类公式，chunk5/6 包含阈值证据。RAG-10 的真正验收对象应是“答案证据集是否完整”，而不是单个期待 chunk 必须进 final topK。
    - 当前决策：保留 `FUSION_AWARE_RERANK_RRF` 作为可配置实验策略与面试讲解素材，但不宣称它优于 `PER_VARIANT_RERANK_RRF`；默认策略恢复到 Step 6.5 的 `PER_VARIANT_RERANK_RRF`，下一步进入 `LLMQueryRewriter` 最小版。
- **Step 6.7 LLM Query Rewrite 配置化实验（2026-05-21）**：
  - 实现方向：新增 `LLM_MULTI_QUERY` rewrite 策略，使用专用 rewrite client 调 `invokeStructured`，返回 `LlmRewriteResponse.rewrittenQueries`；失败后按 `LLM_MULTI_QUERY -> HEURISTIC_MULTI_QUERY -> PASSTHROUGH` 降级。
  - 配置入口：`RagAnswer.ext_param.queryRewriterClientId` 指定专用 rewrite client，`queryRewriteDomainHints` 作为当前知识库的可选领域提示。
  - 工程修正：Armory 先加载 advisor，再从 RAG advisor 中解析 rewrite client id，把专用 client 一起加载进 Spring 运行时，避免 `LlmQueryRewriter` 单例 Bean 初始化早于 DB client 装配的问题。
  - 验证产物：`results/step6.7-llm-query-rewrite/rag-eval-result-step6.7-llm-list-rag10.md`、`rag-eval-result-step6.7-llm-list-narrow.md`、`rag-eval-result-step6.7-llm-prompt-v2-narrow.md`、`rag-eval-result-step6.7-config-hints-narrow.md`。
  - 当前结论：LLM rewrite 工程骨架可用；`config-hints-narrow` 中 RAG-04 保持 `3/3`，RAG-07 保持拒答，RAG-10 仍为 `2/5` 但 rewrite query 已显式覆盖指标、判断标准和阈值范围，关键证据进入 final documents。
  - 重要边界：手工 `queryRewriteDomainHints` 只是验证链路，不是长期方案；如果 hints 过细，LLM 容易退化成 hints 拼接器。后续 rewrite 泛化应做自动 `KnowledgeBaseProfile` 与 query-time hints selection；短期若只追 RAG-10 答案完整性，应转向 answer generation / context salience，不要和 profile 自动化混成一个实验。
- **Viewer / results 整理（2026-05-21）**：
  - `results/step6-rewrite-pipeline*.md` 已移动到 `results/step6.0-rewrite-pipeline/` 并改为 `rag-eval-result-*` 命名，使 viewer 能自动收录。
  - `viewer/build_dataset.py` 已补 Step 6.0-6.7 的 group metadata、短展示名和 config hint；`viewer/index.html` 优先使用 `display_label`，避免矩阵列头只显示冗长路径。
  - `viewer/data/dataset.json` / `dataset.js` 已重新生成；当前 viewer 数据覆盖 48 个 runs、14 个 cases、245 条 summary rows。
- **2026-05-22 决策：如果继续补 LLM Rewrite 能力闭环，优先做 KnowledgeBaseProfile 面试版**：
  - 背景：Step 6.7 证明 LLM rewrite 工程骨架可用，但只看用户问题时 rewrite 泛化，手工 `queryRewriteDomainHints` 又容易让模型退化成 hints 拼接器。
  - 目标：不再手写 hints，而是在入库阶段自动生成知识库画像，并在查询时动态选择少量相关 hints 给 LLM rewrite。
  - 范围：做面试版闭环，不做完整产品化 profile 平台；预计 1.5-2 天。
  - 验收：清空手工 hints 后，auto profile 仍能让 RAG-10 的 rewrite query 带出关键领域词；RAG-04 不退，RAG-07/RAG-14 不误答；报告展示 selectedProfileHints。
  - 边界：如果关键证据已进入 final documents 但答案仍漏点，归因到 Answer Generation / Context Salience，不继续硬调 rewrite prompt。
- **Step 6.8 KnowledgeBaseProfile 面试版实测（2026-05-23）**：
  - 实现结果：新增轻量 `ai_client_rag_profile` 表，`RagService.storeRagFile()` 在 Tika + `TokenTextSplitter` 后抽取 profile；`AiClientAdvisorNode` 按 `filterExpression` 加载 profile hints；`RewritePipeline` 在 query-time 选择 `selectedProfileHints` 后接入现有 `LLM_MULTI_QUERY`。
  - 关键修正：selector 不能把 profile 原始分当相关性；必须先有 query overlap / alias overlap / 有效 CJK phrase overlap，再用 profile score 排序。英文 stopword（如 `to` / `the`）和中文低信号单字（如 `的`）会导致无关技术 token 误入 prompt，已过滤。若 profile 已加载但 query 无匹配，则返回 `NO_PROFILE_MATCH`，不再回退手工 `queryRewriteDomainHints`。
  - 验证产物：`results/step6.8-knowledge-base-profile/rag-eval-result-step6.8-auto-profile-narrow.md` 与 `rag-eval-result-step6.8-auto-profile-full.md`。最终 full 14/14 completed；RAG-04 `3/3`；RAG-07/RAG-08/RAG-14 拒答保持；RAG-10 report 展示 `AUTO_PROFILE` 和内存指标 hints，但最终 full 仍为 `2/5`，缺三档范围，归因转向 Answer Generation / Context Salience。
  - 当前结论：Step 6.8 证明了“手工 hints -> 自动 profile + query-time selection + report 可观测”的工程闭环；不要再继续通过扩大 rewrite prompt 或手工 hints 追 RAG-10 答案完整性。
- **D26 外部样本消融验证计划（2026-05-23）**：
  - 触发原因：现有 Grafana/RAG-10 样本过窄，容易把单 case 失败误判为通用问题。新增 `external-samples/` 后，下一步先做消融验证，而不是继续直接实现 evidence-type coverage。
  - 测试边界：先不改正式 `cases.json`，使用 runner 的 `--cases` 指向临时窄集文件；不上传 `.DS_Store` / `README.md` / cases JSON；正式上传外部 corpus、改 DB agent/advisor 配置、跑 eval 前单独确认目标 knowledge tag。
  - 首轮窄集：`docs/dev-ops/rag-eval/external-samples/cases/external-sample-narrow-8.json`，包含 `EXT-ARTEMIS-01`、`EXT-ARTEMIS-02`、`EXT-RFC9110-03`、`EXT-K8S-PV-03`、`EXT-OWASP-03`、`EXT-PG-04`、`EXT-RFC9110-04`、`EXT-K8S-PV-04`。
  - 首轮 corpus：`artemis-i-mission-timeline.md`、`rfc-9110-http-semantics.md`、`kubernetes-persistent-volumes.md`、`owasp-top10-a01-a03-a05.md`、`postgresql-generated-columns-version-contrast.md`。
  - 消融矩阵优先级：先跑 `L0 baseline`（VECTOR / no rerank / no rewrite / no profile / no salience）、`L2 hybrid+bge`、`L4 +LLM rewrite+auto profile`、`L5 current best`、`A1 current best without rerank`、`A4 current best without salience`。若差异不清，再补 `L1 hybrid only`、`L3 rewrite without profile`、`A2 no rewrite/profile`、`A3 no profile`。
  - 观察指标：不只看 final score，还要看 candidate coverage、rerank 是否丢关键 chunk、final context coverage、rewrite variants、selectedProfileHints、context salience cues/expansions、拒答正确性，并把失败归到 ingestion / retrieval / rerank / final context / generation / literal mismatch / refusal violation。
  - 当前代码支持度：`retrievalMode`、`rerankPolicy`、`rewritePolicy`、`rerankQueryPolicy`、`queryRewriteProfileEnabled`、`queryRewriteProfileHintTopN`、`contextSalienceEnabled` 均来自 RAG advisor `ext_param`，可通过测试 agent/advisor 配置切换；`queryRewriteProfileEnabled=false` 会保留 LLM rewrite 但不加载自动 profile hints，`contextSalienceEnabled=false` 会关闭 salience 生成提示、cue 渲染和相邻证据片段扩展，二者默认 `true` 保持现有行为。
  - 决策门：若外部样本中 `L5` 只改善 Grafana/RAG-10 而不能改善流程、规范、概念区分、版本冲突类 case，则判定当前优化存在样本局限；只有当 candidate 有证据但 final context 丢证据时，才进入 evidence-type coverage 设计。
  - **首轮实测结果（2026-05-23）**：
    - 上传策略：按文章一 tag 上传 5 份首轮 corpus，tag 为 `ext-artemis-i-20260523`、`ext-rfc9110-20260523`、`ext-k8s-pv-20260523`、`ext-owasp-top10-20260523`、`ext-postgresql-generated-columns-version-contrast.md` 对应的 `ext-postgresql-generated-columns-20260523`。未改正式 `cases.json`。
    - Profile 状态：Artemis / RFC9110 / OWASP / PostgreSQL 生成 `hybrid-v2`；K8S PV 因离线 LLM structured output 解析失败回退 `rule-v1`，这是 hybrid profile 的真实降级样本，不重传覆盖。
    - 运行矩阵：`results/external-ablation/` 下生成 L0 VECTOR only、L1 Hybrid only、L2 Hybrid+BGE、L3 Rewrite no profile、L4 Rewrite+profile no salience、L5 current best、A1 no rerank、A4 no salience 共 8 个主报告；另有 2 个 retry 报告用于补齐外部 embedding API transient timeout。
    - 工程状态：BGE `loaded=true`；所有 rerank 报告 `rerank_runtime.failure_reason` 为空；矩阵结束后 `rag_advisor_grafana_llm_v2` 已从备份恢复到 Grafana profile v2 配置。
    - 主要结论 1：外部窄集下 baseline 已能回答大部分 manual case，Hybrid / Rewrite / Profile 没有显示稳定碾压式增益。`L3 rewrite no profile` 与 `L4 rewrite+profile` 的差异很小，说明在短小、单主题、query 与原文术语接近的文档上，Profile 的可见收益有限。
    - 主要结论 2：Context Salience 有非 Grafana 的正向证据。`L5 current best` 相比 `A4 no salience`，`EXT-ARTEMIS-02` 从 retry 后 `2/3` 提升到 `3/3`，`EXT-RFC9110-03` 从 `0/3` 提升到 `2/3`；这说明 salience 不只是 RAG-10 量身定做，但收益仍受 literal 判分与生成措辞影响。
    - 主要结论 3：BGE rerank 在该 8-case 窄集没有体现稳定净收益。`A1 current no rerank` 与 `L5 current best` 表现接近，且早期 `L2/L3/L4` 对 `EXT-ARTEMIS-02` 反而从 `3/3` 降为 `2/3`。这不是 BGE 失败，而是说明 rerank 需要按“候选覆盖 / final context / generation”分层看，不能假设 rerank 一定提升答案。
    - 判分边界：`EXT-ARTEMIS-01` 多组答案语义正确（中文日期与持续时间），但 literal 仍为 `0/3`，属于字面匹配低估；后续外部集需要补 source coverage / semantic manual scoring，否则容易把答案格式差异误判为检索失败。
    - 决策：暂不直接上更复杂的 evidence-type coverage。下一步应先把外部样本评测口径和报告聚合做稳，再决定是否做更通用的 context salience / final context coverage 机制。

### 2.2 关键认知（必读，否则会重复踩坑）

- `rag_demo` 的 `ai_agent.strategy` 是 **`fixedAgentExecuteStrategy`**，**不是** auto chain。
- 不能因为 endpoint 名叫 `/api/v1/agent/auto_agent` 就推断走 `AutoAgentExecuteStrategy`——这是这个项目最大的锚点陷阱。
- C-full + B-symmetric 两轮在 auto chain 上加的 observer 改动（Step1AnalyzeNode / Step4LogExecutionSummaryNode / AbstractExecuteSupport / TextInvocationRequest / SpringAiLlmInvocationGateway）对 `rag_demo` **完全无效**，是死路径。死路径反证见 `results/_archive/rag-eval-result-bsymmetric.md:13-15`（三列全是 `—`）。
- 修 RAG/SSE 相关前必须按 `.claude/rules/agent-runtime-path-proof.md` 的 6 步证据链证明 live path。
- **runner 报告里 `retrieved`/`score`/`empty` 三列 `—` 不等于"无检索"**：F-fix retrieval SSE 在 `FixedAgentExecuteStrategy.execute()` 调 `.call().chatResponse()` 成功返回后才从 `ChatResponseMetadata` 取出来发，所以 LLM 调用失败时（例如 DeepSeek 偶发 `RestClientException`），即使 RAG 检索本身成功（backend log 里有 `RAG检索结果: retrieved=...`），runner 也只能看到三列 `—`。要区分"检索失败"和"生成失败"，对照报告 `error:` 行 / backend `data/log` 里的 `RagAnswerAdvisor` 日志。后续在 LLM 失败时也保住 retrieval 观测需要 advisor before 阶段独立 side-channel（Codex P2 建议）。
- **runner 的 `completed: true` 只表示 SSE 流收到了终端 `type=complete` 帧**，不等于 task 语义完成。AutoAgentExecuteResultEntity 的 summary 事件本身也带 `completed=true`，但那是 task 完成不是流收尾。runner 已绑定到 `event_type == "complete"`（Codex P2 修复后），不再被 summary 误导。

## 3. 已决议（接力会话 May 2 收口）

### D1（已决议）：B 回滚 6 个 auto chain 死路径文件

- 已回滚：`StructuredInvocationRequest` / `TextInvocationRequest` / `AbstractExecuteSupport` / `Step1AnalyzeNode` / `Step4LogExecutionSummaryNode` / `SpringAiLlmInvocationGateway`
- 保留 live path：`FixedAgentExecuteStrategy.java`（F-fix）+ `AutoAgentRetrievalSseEntity.java`（共享 DTO）
- 编译验证：`mvn -T 1C -pl ai-agent-domain,ai-agent-infrastructure -am compile -DskipTests` exit 0
- 理由摘要：6 个文件是 b-symmetric/c-full 误把 RAG observer 挂到 auto chain 时的死代码，留着只会让后续读代码的人误判 RAG metadata 流向；`AutoAgentRetrievalSseEntity` 因为也被 F-fix 用到所以保留。

### D2（已决议）：0.55 已补跑；0.60 / 0.65 不再补跑

| threshold | 文件 | 链路 | 三列状态 | 关键观测 |
|---|---|---|---|---|
| 0.55 | `results/ffix/rag-eval-result-t055-triangle-ffix.md`（May 2） | F-fix | 全 | RAG-04 score 0.6305..0.6481 答 ✓；RAG-06 empty 拒答 ✓；RAG-07 score 0.6185..0.6645 manual 拒答 ✓ |
| 0.60 | `results/ffix/rag-eval-result-ffix.md`（May 1） | F-fix | 全 | 与 0.55 完全一致（同 score 区间，因为 score 全部 > 0.60）|
| 0.65 | `results/_archive/rag-eval-result.md`（Apr 28） | F-fix 之前旧 schema | 无三列 | RAG-04 score≈0.648 < 0.65 → 误杀拒答（answer 文本可证）|

**核心结论**：0.55 vs 0.60 在 RAG-04 / RAG-07 上 score 完全一致，**没有 diagnostic delta**；真正分水岭是 0.65（RAG-04 死亡线）。这印证了 PLAN §1 / 笔记 §4.1.3 的认知 —— 单一静态 threshold 难以同时挡住弱相关并保住深层公式（RAG-04 = 0.648 < RAG-07 = 0.6645，弱相关分数高于正例）。后续优化方向应转向 query rewrite / rerank / answerability，不再继续在 threshold 上拍。

### Hygiene（已决议）

- `.gitignore` 已加：`/data/` / `/ai-agent-boot/data/` / `__pycache__/` / `*.pyc`
- `.env.example` 早已有 `!.env.example` 例外规则（`.gitignore:51`），保持入库
- `application-local.yml` / `application-*.local.yml` 早已忽略

### D3（已决议 May 2）：A+B 修复 RAG-05 30s 失败 + SSE 异常透传

`a9fbf7e` 同时修同一个 RAG-05 失败的两条路径：

- **A**: `application-dev.yml` `spring.ai.openai.http` —— `read-timeout 25s→120s` / `response-timeout 25s→120s` / `connect-timeout 5s→10s`。RAG eval long_context case 在 DeepSeek 25s read-timeout + RetryTemplate 重试 5s = 30s 触发 `SocketTimeoutException`。120s 给慢链路余量。`AiClientApiNode.buildRestClientBuilder()` 已支持 `@Value` 注入，纯 yml 调整不动 Java。
- **B**: `AgentExecutionFacade.ssePort.error()` 不再调 `emitter.completeWithError(t)`（Spring MVC 默认 ExceptionHandler 把 `LinkedHashMap` 写 JSON 错误响应，但 Content-Type 已锁成 `text/event-stream`，触发 `HttpMessageNotWritableException`，客户端只看到 connection reset）。改为往 SSE 流发 `data: {"type":"error","errorClass":"...","message":"..."}\n\n` 帧，`emitter.complete()` 由 `AgentDispatchService.finally` 统一收尾避免重复 complete。
- **配套**: `rag_eval_runner.py.summarize_events` 解析 `type=error` 帧，HTTP 没异常但 SSE 流里有 error 事件时把 `errorClass: message` 写进 markdown 报告 details `error:` 行。

直接验证：

- A: RAG-05 单跑 duration=26702ms completed=true（fix 前固定 30s timeout）
- B: 全量 baseline 重跑时 RAG-04 偶发 `RestClientException: Error while extracting response for type [...] and content type [application/json]`（duration=28.2s，< 120s 不是 read-timeout，是 OpenAI 响应解析失败），markdown details 直接显示 errorClass/message。

### D4（已决议 May 4）：Step 3 chunker + chunk metadata 启动

**拍板基线**（用户 May 4 确认）：

- `ai_client_advisor` 表 `rag_demo` 当前 `topK=4` / `similarityThreshold=0.6`，**Step 3 期间保持不动**（单变量隔离，让 chunker 改动 attribution 干净）
- chunker：Phase A 调 `TokenTextSplitter` 参数 → 决策门不达标再触发 Phase B 自写 `MarkdownTextSplitter`
- metadata 完整集：`chunkIndex` / `totalChunks` / `sourcePath` / `parentSection` / `headingPath`
  - Phase A 阶段 `parentSection` / `headingPath` 字段建好但留空（schema 一次到位，避免 Phase B 再重灌）
- 重灌策略：`TRUNCATE vector_store_openai` + admin 接口重新上传（`rag_demo` 原始文档可复用，已确认）
- baseline 对照：`results/ffix/rag-eval-result-baseline-ffix.md`

**决策门口径（margin-based eval）**：

- pass 率 ≥ 10/10（不退化）
- RAG-04 `max_score` ≥ 0.70（理想 ≥ 0.75）
- 正例 - 弱相关 max score gap 扩大
- empty 率不增加

**口径警示（与 baseline 不可比的列）**：

- ❌ 不可比：chunk count（边界变了）、max_score 绝对值（embedding 输入变了）
- ✅ 可比：completed / hit-rate / score gap / empty 率

**理由摘要**：

`results/ffix/rag-eval-result-ffix.md` / `results/ffix/rag-eval-result-t055-triangle-ffix.md` 显示 RAG-04 max_score=0.6481（正例）< RAG-07 max_score=0.6645（弱相关），是 score inversion 现象，单一 threshold 数学上无解。RAG-07 已在 `before()` empty/manual 拒答路径处理；RAG-04 偏低的根因怀疑是 chunk 边界稀释正例（默认 chunkSize=800 token ≈ 1500 中文字，正例段落可能只占 30% chunk 容量）。Step 3 = 改写入端 chunker + metadata + 渲染端三件套，验证 chunk 边界是否是当前 max_score 偏低的瓶颈。

详细执行步骤见 §9。

### D5（已决议 May 5）：Step 3 Phase A Gate — 部分达标，调参第二轮

**评测产物**：`results/step3-chunker-v1/rag-eval-result-step3-chunker-v1.md`

| 验收口径 | 结果 | 说明 |
|---|---|---|
| pass rate ≥ 10/10 | ✅ 10/10 | +1 vs baseline（RAG-04 baseline=RestClientException flaky → step3 completed=true 3/3 lit） |
| RAG-04 max_score ≥ 0.70 | ❌ 0.6481 | chunkSize=800=默认值未改，scores 与 t055-triangle 完全一致，无改善 |
| 正例-弱相关 gap 扩大 | ❌ 仍倒挂 | RAG-07 max=0.6645 > RAG-04 max=0.6481，delta=-0.0164，与 baseline 同 |
| empty 率不增加 | ✅ 稳定 | RAG-06/RAG-08 empty=true 同 baseline |
| RAG-10 regression | ⚠️ 2/5 vs 5/5 | 同 chunk 内容，LLM variability（missed: 正常范围/警告范围/危险范围） |

**根因**：Step 3.1 做了 yml 参数化基础设施，但 chunk-size=800 是 Spring AI 1.0.3 默认值，重灌后 chunk 结构与 baseline 完全相同，scoring 无改善（scores 与 t055-triangle 完全一致印证此点）。

**历史下一步（Phase A 调参第二轮，D6 已完成并推进到 v3）**：
1. 把 `application-dev.yml` `chunker.chunk-size: 800 → 400`
2. TRUNCATE `vector_store_openai` + 重传 rag_demo 原始文档
3. 预热后跑 `rag_eval_runner.py --session-prefix step3-chunker-v2`
4. 对比 RAG-04 max_score 是否提升 + RAG-10 regression 是否 LLM variability
- 预期：同内容切成更小 chunk，predict_linear 段落占比提升 → embedding score 应提升
- 如 RAG-04 仍 ≤ 0.65 → 触发 Phase B（MarkdownTextSplitter 自写）

### D6（已确认 May 5）：v2 完成，v3 已准备完成，待跑评测

**v2 评测产物**：`results/step3-chunker-v2/rag-eval-result-step3-chunker-v2.md`（May 5 05:59）

| 验收口径 | v1 | v2 | 结论 |
|---|---:|---:|---|
| pass rate | 10/10 | 10/10 | ✅ 不退化 |
| RAG-04 max_score | 0.6481 | **0.6715** | ❌ 仍低于 0.70，差 0.0285 |
| RAG-07 max_score（弱相关） | 0.6645 | **0.6634** | ✅ 未上升 |
| score gap (RAG-04 − RAG-07) | -0.0164 | **+0.0081** | ✅ 倒挂已翻正 |
| empty 率 | 稳定 | 稳定 | ✅ 不退化 |

**v3 准备状态（已 supersede by D7）**：

1. 已选择 D5 候选 A：`chunk-size 400 → 250`，`min-chunk-size-chars 175 → 109`
2. `ai-agent-boot/src/main/resources/application-dev.yml` 已落盘为 250/109
3. 用户确认已完成 `vector_store_openai` TRUNCATE + admin 重传 `rag_demo`（v3 chunks=10，v1=4 / v2=7）

→ v3 评测已于 May 5 22:17 跑完，决策与收口见 D7。

### D7（已决议 May 5 22:20）：Step 3 Phase A 收口 — v3 决策门 4/4 通过，不触发 Phase B

**评测产物**：`results/step3-chunker-v3/rag-eval-result-step3-chunker-v3.md`（May 5 22:17）

**chunks 数量演化**（v1/v2/v3 同源原始文档，仅 chunker 参数变化）：v1 chunk-size=800 → 4 / v2 400 → 7 / v3 250 → 10

| 验收口径 | v1 | v2 | v3 | 决策门 | v3 |
|---|---:|---:|---:|---|:---:|
| pass rate | 10/10 | 10/10 | **10/10** | ≥10/10 | ✅ |
| RAG-04 max_score | 0.6481 | 0.6715 | **0.7406** | ≥0.70 | ✅（跨过 0.0406 安全余量） |
| RAG-04 min_score | 0.6305 | 0.6201 | **0.6481** | — | ✅（整段 chunk 质量提升）|
| RAG-07 max_score（弱相关） | 0.6645 | 0.6634 | **0.6726** | 不抬升 | 🟡（+0.0092 vs v2，仍 < RAG-04 max）|
| score gap (RAG-04 − RAG-07) | -0.0164 | +0.0081 | **+0.0680** | 翻正 | ✅（magnitude 8×）|
| empty 率（RAG-06/08） | 2 | 2 | **2** | 不退化 | ✅ |
| RAG-10 literal_hit | 2/5 | 5/5 | **2/5** | 不退化 | 🟡（LLM variability，非 chunker 归因）|

**核心结论**：

1. **决策门 4 个核心 KPI 全部通过**，触发 Phase B 阈值（RAG-04 max ≤ 0.65）反向不命中——**不需要自写 `MarkdownTextSplitter`**
2. **chunker 边界稀释假设三轮证实**：chunk-size 800/400/250 对应 chunks 4/7/10，正例（RAG-04）max +0.0925，弱相关（RAG-07）max +0.0081，**非对称响应**
3. **score gap 从 -0.0164 翻正到 +0.0680**：8 倍 magnitude 是结构性证据，不是 noise
4. **🟡 RAG-10 literal_hit 退化**（缺"正常范围 / 警告范围 / 危险范围"）：v1 也 2/5、v2 是 5/5，同 chunker 不同结果证明是 LLM 输出多样性，非 chunker 归因；后续可在延期话题 1（paraphrase regression）跟进
5. **🟡 RAG-07 max 略升**：弱相关 chunk 上限维持在 ~0.67，gap 翻正且扩大本身已说明分隔度提升

**Phase A 锁定参数**（生产配置）：

```yaml
spring.ai.rag.chunker:
  chunk-size: 250
  min-chunk-size-chars: 109
  min-chunk-length-to-embed: 5
  max-num-chunks: 10000
  keep-separator: true
```

**下一步选项**（等用户触发）：

- 进入 Step 4 Hybrid Retrieval（PG FTS + pgvector + 应用层 RRF）
- 或先休 Phase A，跑延期话题（RAGAS / paraphrase regression / rerank 等）

**不再做**：

- Phase B 自写 `MarkdownTextSplitter`（决策门通过反向不命中）
- 在 Step 4 之前继续调 chunker 参数（边际收益已显著下降，超出 Phase A 范围）

### D8（已决议 2026-05-06）：Phase A.5 Generalizability Probe — Step 4 启动前补 control case

**触发原因**：D7 决策门 4/4 通过经 Codex review 发现 generalizability 缺口——v3 边界回退几乎全靠 `\n` 命中，而 `\n` 高密度来自 rag_demo 当前 2 份 markdown 文档的结构副作用（list / code fence / heading 强制每行换行）。`TokenTextSplitter.java:105-107` 仅识别 `.?!\n` 4 个 ASCII 字符的 lastIndexOf 链，对纯中文长文（全角 `。？！` + 少换行）会退化为 token 硬切。Phase A 决策门的有效声明范围严格地说是"在 markdown 类文档上达到决策门"，不是"通用 chunker 改进"。

**Codex review 暴露的关键校正**（2026-05-06，详见会话记录）：

1. **Hybrid 不独立于 chunk 边界**：PG FTS 和 pgvector 检索同一批 chunk，hybrid 在坏 chunk 上做 ablation 无法分离收益来源——A 必须先于 Step 4，不是策略选择，是必要前置
2. **score 提升 ≠ 目标 chunk 命中**：v3 RAG-04 max=0.7406 是 top-K max similarity，LLM 可能从邻近/干扰 chunk 拼答案；evaluator 需要补 chunk-level attribution metric
3. **C 优先于 B**：当前 root cause 是 CJK 标点 + 低换行密度，C（自写 splitter 加中文标点回退）直接对应；B（`MarkdownDocumentReader`）解决的是 markdown 结构维度，是另一支
4. **决策门分两层**：Step 3 attribution gate（当前 Phase A.5 收口）+ Promotion gate（远期声称"通用 chunker"前要求覆盖 markdown + low-newline plain text）

**A0 control case 设计**（接受 Codex 4 case + 2 文档方案）：

| 文档 | 规格 | 用途 |
|---|---|---|
| `control-cn-low-newline.txt` | 2500-4000 中文字 / 2-3 段超长（每段 800-1200 字） / 全角 `。？！；：` / 少换行 / 埋深层正例 + 干扰项 | CJK 句界回退场景验证 |
| `control-en-long-paragraph.txt` | 1500-2500 词 / 少换行 / 英文 `.` | 验证英文 `.` 回退路径未被中文修复破坏 |

| Case | 类型 | 期望 |
|---|---|---|
| RAG-11 | 中文 literal 深层正例 | 命中 2-3 个独特词 |
| RAG-12 | 中文 manual 弱相关 / 干扰项 | 拒答或明确"未覆盖" |
| RAG-13 | 英文 literal 深层正例 | 命中独特词 |
| RAG-14 | 英文 manual 弱相关 | 拒答 |

**PDF 不加**：引入 Tika/PdfBox 提取质量 + 分页 + 页眉页脚噪声，不再是 chunker control，应在后续 reader matrix 阶段做。

**A1 ingest 诊断脚本**（离线工具）：

- 每个 control 文档的 chunk 数 / 目标锚点在哪个 chunk / 目标句是否被切断 / 相邻 chunk preview
- SQL：`SELECT id, content, metadata FROM vector_store_openai WHERE metadata->>'sourcePath' = ?`
- 若后续实现，输出建议放：`docs/dev-ops/rag-eval/results/phase-a5-control/ingest-diagnostic-<timestamp>.md`

**新增 attribution metric `target_chunk_intactness`**：目标句是否完整在单个 chunk 内（基于 control 文档埋点 + ingest 诊断输出），让 Codex 主张"score 提升 ≠ 目标 chunk 命中"的盲点可量化进决策门。

**Step 3 attribution gate（Phase A.5 收口口径）**：

- 原 10 case 不退化（pass 10/10 + 决策门 4/4 维持）
- control-cn 正例可召回（RAG-11 retrieved=true + score ≥ baseline 范围）
- control-cn 弱相关不误答（RAG-12 不输出捏造内容）
- control-en 不退化（RAG-13/14 行为对照英文 `.` 回退路径）
- `target_chunk_intactness` ≥ 阈值（具体阈值 A0 跑完后定）

**Promotion gate（远期，不在当前 sprint）**：

- 要声称"通用 chunker 改进"前覆盖 markdown + low-newline plain text 至少两类形态
- 要进 production 再加 PDF / HTML 控制组

**A → C → Step 4 决策路径**：

```
A0 (半天)        : 补 control 文档 + RAG-11/12/13/14 case + ingest 诊断脚本
A1 (1-2h)        : 跑 v3 chunker 评测 + ingest 诊断 + 算 target_chunk_intactness
gate decision    : Step 3 attribution gate 是否通过
  ├─ ✅ 通过     → 进 Step 4 Hybrid + RRF（3-5 day）
  └─ ❌ 失败     → C: CjkAwareTokenTextSplitter (半天) → 重跑 A1 → 再 Step 4
```

**C 实现路径**（仅在 A 失败时触发）：

- `CjkAwareTokenTextSplitter extends TextSplitter`（**不是 extends TokenTextSplitter**——1.0.3 sources 显示 `doSplit` protected 但 encode/decode helpers + 5 字段全 private，继承会复制状态）
- `TextSplitter` 本身 implements `DocumentTransformer`，是 idiomatic Spring AI ETL pattern：Reader → Transformer → VectorStore
- `@ConfigurationProperties` 替代当前 5 个 `@Value`（Spring 风格 + 类型安全）
- 强边界：`\n\n`, `\n`, `.?!。？！`
- 弱边界：`;；:` 可选
- **`，`（中文逗号）不进强边界**——长中文句逗号多，加了会过碎

**待 verify（不影响当前决策）**：

Spring AI current API（1.1.x / 2.0.0-m3）是否引入带 `punctuationMarks` 参数的 `TokenTextSplitter` 构造器（Codex 提到 doc URL，1.0.3 [源码验证] 确认无）。验证后若属实，未来 BOM 升级可能不需自写 splitter——影响 C 的 ROI 评估。

**不再做（D8 范围）**：

- 推翻 D7 Phase A 锁定参数（`chunk-size=250 / min-chunk-size-chars=109` 在 markdown 形态上仍是有效结论）
- 把 Step 4 Hybrid 排到 A 前面（hybrid 在坏 chunk 上做 ablation 归因混乱，A 必须先做）
- 在 A 阶段加 PDF / HTML 控制组（属 reader matrix 维度，不是 chunker control）

**证据锚点**：

- `TokenTextSplitter.java:105-107`（spring-ai-commons-1.0.3-sources.jar 解压）：仅 `.?!\n` 4 字符 lastIndexOf
- 项目 Reader 现状：`RagService.java:51` 用 `TikaDocumentReader`，未引入 `spring-ai-markdown-document-reader`
- v3 评测产物：`docs/dev-ops/rag-eval/results/step3-chunker-v3/rag-eval-result-step3-chunker-v3.md`
- rag_demo 文档清单：`docs/dev-ops/rag-file/{ai-agent-prompt-optimization,grafana-mcp-tools-guide}.md`
- 文档形态实测：2 份 markdown 中文，每 ~31 / ~16 字符 1 个 `\n`（UTF-8 字符口径，非 byte），中文标点不识别 + 英文 `.` 稀疏

### D9（已验证 2026-05-06）：Phase A.5 通过，Step 3 不继续调 chunker，下一候选 Step 4 Hybrid

**新增产物**（commit `2877cbc`）：

- `docs/dev-ops/rag-file/control-cn-low-newline.txt`
- `docs/dev-ops/rag-file/control-en-long-paragraph.txt`
- `docs/dev-ops/rag-eval/cases.json` 新增 RAG-11/12/13/14
- `docs/dev-ops/rag-eval/results/step3-control-v3/rag-eval-result-step3-control-v3.md`
- `docs/dev-ops/rag-eval/results/step3-full-v3-with-control/rag-eval-result-step3-full-v3-with-control.md`

**control-only 结果**（filterExpression = `knowledge in ['control-cn-low-newline', 'control-en-long-paragraph']`）：

| case | 类型 | retrieved | score range | 判分 |
|---|---|---:|---|---|
| RAG-11 | 中文低换行正例 | 4 | 0.7175..0.7763 | literal 2/2 |
| RAG-12 | 中文弱相关 / 未覆盖问题 | 4 | 0.6475..0.7169 | manual 拒答 |
| RAG-13 | 英文长段落正例 | 4 | 0.7632..0.7906 | literal 2/2 |
| RAG-14 | 英文弱相关 / 未覆盖问题 | 4 | 0.7432..0.7799 | manual 拒答 |

**full 14 regression 结果**（filterExpression = `knowledge in ['grafana-mcp-tools-guide', 'control-cn-low-newline', 'control-en-long-paragraph']`）：

- 原 RAG-01..10 行为不退化；RAG-06 / RAG-08 仍 empty 拒答
- RAG-10 仍是 literal 表达漂移 2/5（缺正常/警告/危险范围），与 v1/v3 既有现象一致，不归因于 control 文档
- 新 RAG-11 / RAG-13 正例命中，RAG-12 / RAG-14 拒答

**RAG-14 定性**：

RAG-14 是 negative-evidence answerability case（负证据型拒答）。英文控制组文档明确写了没有指定 cloud provider、该问题不能从文档回答；高分表示召回到“没有答案”的相关证据，不是弱相关误召回后靠模型硬压住。它能验证 prompt + 模型在显式负证据下的拒答能力，但不能替代“纯缺失答案、无负证据”的拒答评测。

**决策**：

1. 保留 v3 参数：`chunk-size=250 / min-chunk-size-chars=109`
2. 不继续调 chunker，不触发自写 CJK/Markdown splitter
3. 当前结论边界收窄为：Phase A 参数在 markdown + 低换行中英文纯文本控制组上未发现阻断性退化，足够进入下一工程维度；不宣称通用 chunker
4. 下一候选进入 Step 4 Hybrid + RRF。理由是补单路向量召回的词面通道（PG FTS），不是为了修 RAG-14

**遗留观测缺口**：

当前 runner report 不展开 top-K chunk 的 `sourcePath/chunkIndex/score/preview`，只能看到 retrieved / score range / empty / context chars。后续若要把 attribution gate 做严，应把 top-K chunk attribution 写入 markdown 报告。

### D10（已验证 2026-05-08）：Step 4 Hybrid + RRF 功能完成，但 A/B 未证明收益，下一步先做 Hybrid calibration

**新增提交**：

- `aee5a75 feat: add hybrid RAG retrieval`
- `f65c365 feat: expose hybrid retrieval metadata`

**实现范围**：

- `AiClientAdvisorDTO.RagAnswer` 增加 `retrievalMode/vectorTopK/keywordTopK/rrfK`
- `RetrievalOptionsVO` 集中封装 fallback：blank/unknown mode → VECTOR，topK/rrfK <= 0 走默认
- `RagAnswerAdvisor` HYBRID 分支：
  - vector side：pgvector `similaritySearch`，topK 使用 `effectiveVectorTopK`
  - keyword side：PostgreSQL FTS，`websearch_to_tsquery('simple', ?)` + `ts_rank_cd(...)`
  - filter：Spring AI `Filter.Expression` 经 `PgVectorFilterExpressionConverter` 转 JSONPath，并用 `metadata::jsonb @@ CAST(? AS jsonpath)` 参数绑定
  - merge：应用层 RRF，`1 / (rrfK + rank)`，默认 `rrfK=60`
- 可观测性：
  - 合并后 `Document.score` 改为 `rrfScore`
  - metadata 写入 `retrievalMode/retrievalSource/rrfScore/rrfK/vectorRank/vectorScore/keywordRank/keywordScore`

**当前运行配置**（`rag_advisor_grafana`）：

```json
{
  "topK": 4,
  "similarityThreshold": 0.6,
  "filterExpression": "knowledge in ['grafana-mcp-tools-guide', 'control-cn-low-newline', 'control-en-long-paragraph']",
  "retrievalMode": "HYBRID",
  "vectorTopK": 4,
  "keywordTopK": 6,
  "rrfK": 60
}
```

**验证产物**：

- `docs/dev-ops/rag-eval/results/step4-hybrid-rrf/rag-eval-result-step4-hybrid-rrf.md`
- `docs/dev-ops/rag-eval/results/step4-hybrid-rrf/rag-eval-result-step4-hybrid-rrf-attribution.md`
- `docs/dev-ops/rag-eval/results/step4-vector-ablation/rag-eval-result-step4-vector-ablation.md`

| 口径 | 结果 |
|---|---|
| full 14 completed | 14/14 |
| RAG-06 / RAG-08 empty 拒答 | 维持 |
| RAG-02 / RAG-03 / RAG-05 | literal 全命中 |
| RAG-11 / RAG-13 control 正例 | literal 全命中 |
| RAG-12 / RAG-14 control 弱相关 | 拒答 / 负证据回答维持 |
| RAG-04 | 语义正确；attribution 显示目标 chunk `grafana-mcp-tools-guide.md#5` 在 VECTOR 是第 1，HYBRID/RRF 后降到第 3 |
| RAG-10 | 仍为既有 literal 表达漂移：缺 `正常范围 / 警告范围 / 危险范围`，不归因于 Hybrid |

**A/B attribution 关键发现**：

| case | VECTOR | HYBRID + RRF | 判断 |
|---|---|---|---|
| RAG-02 | `chunk=1`（参数说明）排第 1 | `chunk=3` 因 keywordRank=1 排第 1，`chunk=1` 降第 2 | keyword 通道参与排序，但未证明比纯向量更好 |
| RAG-04 | 目标 `chunk=5` 排第 1，vectorScore=0.7406 | 目标 `chunk=5` 只来自 VECTOR，排第 3；`chunk=4/1` 因命中 `PromQL` 词面被 RRF 提到前 2 | 当前 keyword query 对“PromQL”这类泛词过敏，产生负向排序干扰 |
| RAG-07 | top4 全是 grafana 文档 | 引入 1 条 `KEYWORD` only 的 `control-en-long-paragraph.txt#4` 噪声 | keyword 分支在弱相关问题上有跨文档噪声风险 |
| RAG-13 | 英文控制组 top4 都正确 | top4 都是 `VECTOR_KEYWORD`，顺序小幅调整 | 英文词面问题上 Hybrid 确实参与排序，但答案指标未新增收益 |

**解释边界**：

1. Step 4 后 markdown 报告里的 `score` 已经是 RRF score，不再是 Step 3 的向量相似度 score；不能直接拿 `0.03` 和 Step 3 的 `0.74` 比。
2. `rag_eval_runner.py` 已展开 top-K doc attribution；判断 Hybrid 贡献要看每条 document metadata 的 `retrievalSource/vectorRank/vectorScore/keywordRank/keywordScore`。
3. 当前 14 条用例已经证明 Hybrid **可用且大体不退化**，但没有证明它比纯向量更优。
4. 当前主要问题不是 RRF 公式本身，而是 keyword query 构造过宽：`PromQL`、`Grafana`、`MCP`、`query` 这类泛词容易把词面相关但语义不够聚焦的 chunk 提前。
5. Hybrid + RRF 解决的是“单路向量召回缺少词面通道”的问题；要证明它有效，需要更聚焦的 lexical probe 或更严格的 keyword gating。

**决策**：

1. Step 4 功能闭环通过，HYBRID 配置已恢复并保留，但不能在面试里宣称“明显提升了效果”。
2. 下一步不直接进入 Step 5 Reranker；先做 Step 4.1 Hybrid calibration。
3. Step 4.1 最小目标：
   - 给 `buildKeywordQuery` 加 stopword / generic token 过滤，优先保留技术标识符（如 `query_prometheus`、`node_filesystem_avail_bytes`、`ARCHIVE-CN-7319`、`silver-river-42`）
   - 或增加 keyword 分支启用条件：只有 query 中出现足够强的技术 token 时才走 PG FTS
   - 重跑 VECTOR vs HYBRID attribution，要求 RAG-04 目标 chunk 不再被降序，RAG-07 不再引入跨文档 keyword-only 噪声
4. Step 4.1 通过后再进入 Step 5 Reranker：目标是对已召回 top-K 做二阶段排序，提升最终上下文顺序稳定性。

### D11（已验证 2026-05-09）：Step 4.1 Hybrid calibration 收口，Hybrid 作为受控增强保留

**新增提交**：

- `0314954 test: 补充 Hybrid RAG 归因评测`
- `a7826bc feat: 收口 Hybrid RAG 校准`

**实现范围**：

- `buildKeywordQuery` 加强强标识符优先和泛词过滤，降低 `PromQL`、`Grafana`、`MCP`、`query` 这类泛词触发 PG FTS 的概率。
- keyword 分支未参与时，document metadata 写入 `keywordSkippedReason`，runner 报告能直接看到 `NO_KEYWORD_QUERY` / `NO_KEYWORD_RESULTS`。

**验证产物**：

- `docs/dev-ops/rag-eval/results/step4.1-hybrid-calibrated/rag-eval-result-step4.1-hybrid-calibrated.md`

**关键结论**：

| case | Step 4 问题 | Step 4.1 后结果 |
|---|---|---|
| RAG-04 | 目标 chunk `grafana-mcp-tools-guide.md#5` 被 RRF 从 VECTOR 第 1 降到第 3 | 目标 chunk 回到第 1，keyword 分支未参与，避免负向排序干扰 |
| RAG-07 | 引入跨文档 keyword-only 噪声 | 不再引入跨文档 keyword-only 噪声 |

**面试口径**：

Step 4.1 不是证明 Hybrid 已经稳定提升答案质量，而是证明我们识别并控制了 Hybrid 的负面效果：关键词通道只在强词面信号足够明确时参与，避免泛词把弱相关 chunk 提前。

### D12（已验证 2026-05-13）：Step 5 Rerank 观测地基完成，下一步进入抽象接入

**新增提交**：

- `fca43f2 feat: 增加 RAG rerank 占位与评测观测`
- `555df14 fix: 透传 RAG rerank 观测字段`
- `a820144 feat: 增加 RAG 候选召回阈值观测`
- `631ffd2 refactor: 抽取 RAG 观测字段常量`

**实现范围**：

- `RagAnswerAdvisor` 中已有 passthrough rerank 占位，当前不改变排序，只给最终文档补充 rerank 观测字段。
- context / SSE 级别已经透出 `qa_rerank_applied/qa_rerank_mode/qa_rerank_candidate_count/qa_rerank_final_count`。
- document metadata 级别已经透出 `beforeRerankRank/rerankRank/rerankApplied/rerankMode`。
- `candidateSimilarityThreshold` 已进入配置、fallback、向量候选召回和 `qa_candidate_similarity_threshold` 观测。
- `RagObservationKeys` 集中维护 RAG 观测字段名，避免 `qa_*` 与 document metadata 字符串散落。

**验证产物**：

- `docs/dev-ops/rag-eval/results/step5-rerank-observe/rag-eval-result-step5-rerank-observe.md`

**当前边界**：

- `rerankApplied=false`
- `rerankMode=PASSTHROUGH`
- `beforeRerankRank == rerankRank`
- 还没有接入真实模型 reranker，也没有 rerank 分数。

**下一步 Step 5.1**：

先抽象 `DocumentReranker` / `RerankResult` 并接入默认 `PassthroughDocumentReranker`，让 `RagAnswerAdvisor` 依赖接口而不是私有方法。该步骤应保持行为不变，只为后续真实 reranker 留扩展点。

### D13（已验证 2026-05-13）：Step 5.1 Rerank 抽象接入完成，行为保持 passthrough

**实现范围**：

- 新增 `DocumentReranker`：定义 `rerank(String query, List<Document> candidates, int topK)` 抽象。
- 新增 `RerankResult`：承载 `documents/applied/mode/candidateCount/finalCount/failureReason`，避免 rerank 行为元数据继续散落在 `RagAnswerAdvisor`。
- 新增 `PassthroughDocumentReranker`：当前默认实现，按原顺序截断到 `topK`，写入 document 级 rerank metadata。
- `RagAnswerAdvisor` 改为依赖 `DocumentReranker`，保留旧构造器兼容调用点；外部未传 reranker 时 fallback 到 passthrough。

**验证产物**：

- `docs/dev-ops/rag-eval/results/step5.1-rerank-abstraction/rag-eval-result-step5.1-rerank-abstraction-smoke.md`

**关键观测**：

- 预热 `rag_demo` 成功：`armory_agent` 返回 `装配成功`。
- RAG-04 live eval：`completed=true`、`retrieved_document_count=4`、`retrieval_empty=false`。
- rerank event：`applied=false`、`mode=PASSTHROUGH`、`candidates=4`、`final=4`。
- document attribution：4 个 document 均满足 `beforeRerankRank == rerankRank`，目标 chunk `grafana-mcp-tools-guide.md#5` 仍为第 1。

**当前边界**：

- 还没有真实 reranker 模型，也没有 rerank score。
- `RerankResult.failureReason` 只是为后续真实模型失败 fallback 预留，当前 passthrough 正常为 `null`。

**下一步 Step 5.2**：

先设计真实 reranker 接入方式：模型/API 选型、配置入口、失败 fallback、耗时和模型名观测字段，以及是否先用 mock reranker 验证排序变化。

### D14（已验证 2026-05-14）：Step 5.2 本地 bge reranker HTTP 接入完成

**实现范围**：

- 新增 `HttpDocumentReranker`：把 query 和候选 chunk 通过 HTTP POST 传给本地 Python rerank 服务。
- 新增 `RerankPolicy`：当前支持 `LOCAL_BGE` 与 `PASSTHROUGH`，空值或未知值默认 `PASSTHROUGH`，避免 rerank 配置错误阻断主链路。
- 新增 `DocumentRerankerFactory`：用 Spring Bean map 按策略名选择 `DocumentReranker` 实现。
- `AiClientAdvisorNode` 在创建 RAG advisor 时读取 `AiClientAdvisorDTO.RagAnswer.rerankPolicy`，把 factory 返回的 reranker 传入 `RagAnswerAdvisor`。
- `HttpDocumentReranker` 所有异常/无效响应路径 fallback 到 passthrough，并写入 `RerankResult.failureReason`。
- `rag_eval_runner.py` 在 document attribution 中展示 `rerankScore`。

**运行配置**：

- Python 服务：`/Users/qianghaixin/rerank/rerank_service.py`
- 模型路径：`/Users/qianghaixin/models/huggingface/bge-reranker-v2-m3`
- 服务地址：`http://127.0.0.1:18080/rerank`
- 数据库配置：`ai_client_advisor.rag_advisor_grafana.ext_param` 中 `rerankPolicy=LOCAL_BGE`

**验证产物**：

- `docs/dev-ops/rag-eval/results/step5.2-http-rerank-live/rag-eval-result-step5.2-factory-rag04.md`
- `docs/dev-ops/rag-eval/results/step5.2-http-rerank-live/rag-eval-result-step5.2-factory-rag07.md`

**关键观测**：

- RAG-04：`completed=true`、`literal_hit=3/3`、`rerank applied=true`、`mode=LOCAL_BGE`，目标 chunk `grafana-mcp-tools-guide.md#5` 保持第 1，`rerankScore=0.1935`。
- RAG-07：`completed=true`、`should_answer=false`，仍输出“知识库没有 dashboard 变量模板配置相关信息”的拒答方向，rerank 运行态同样为 `LOCAL_BGE`。

**当前边界**：

- 本地 Python rerank 服务需要独立启动，当前不是 Java 进程生命周期的一部分。
- `HttpDocumentReranker` 的 URL/timeout 仍是本地 POC 常量，后续若要生产化，应迁入配置。
- 当前只跑 RAG-04/RAG-07 最小回归；完整 14 条回归可在 Step 5.2 收口后补跑。

### D15（已验证 2026-05-14）：Step 5.3 Rerank A/B 完成，rerank 有排序收益但没有整体答案指标提升

**前置修复**：

- 复现：将 DB `rerankPolicy` 从 `LOCAL_BGE` 切到 `PASSTHROUGH` 并调用 armory 后，RAG-04 报告仍显示 `LOCAL_BGE`。
- 根因：动态注册同名 Bean 时只移除 `BeanDefinition`，没有销毁已经实例化的 singleton，旧 advisor / client 实例仍可能被执行链路拿到。
- 修复：`AbstractArmorySupport.registerBean` 在重新注册前先 `destroySingleton(beanName)`，再 `removeBeanDefinition(beanName)`。
- 验证：后端重启后，MySQL 与 admin 查询接口均为 `PASSTHROUGH`，armory 装配后 RAG-04 报告显示 `rerank applied=false / mode=PASSTHROUGH`。

**验证产物**：

- `docs/dev-ops/rag-eval/results/step5.3-rerank-ab/rag-eval-result-step5.3-passthrough.md`
- `docs/dev-ops/rag-eval/results/step5.3-rerank-ab/rag-eval-result-step5.3-local-bge.md`

**A/B 关键数据**：

| 指标 | PASSTHROUGH | LOCAL_BGE | 结论 |
|---|---:|---:|---|
| completed | 14/14 | 14/14 | 主链路稳定 |
| empty 拒答 | RAG-06 / RAG-08 | RAG-06 / RAG-08 | 无退化 |
| literal cases | RAG-02 4/4、RAG-03 3/3、RAG-04 3/3、RAG-05 5/5、RAG-10 2/5、RAG-11 2/2、RAG-13 2/2 | 同左 | 答案指标未提升也未退化 |
| top-1 改变 | — | RAG-02 / RAG-05 / RAG-07 / RAG-13 / RAG-14 等 | rerank 确实改变排序 |
| 平均 duration | 8880.4ms | 8874.3ms | 单次样本下无可证明额外延迟，LLM 波动大于 rerank 差异 |

**典型排序归因**：

- RAG-04：目标 chunk `grafana-mcp-tools-guide.md#5` 在两组都保持第 1，`LOCAL_BGE` 给它 `rerankScore=0.1935`，同时把原第 2 的 query_prometheus 参数 chunk 降到第 4。
- RAG-05：`LOCAL_BGE` 把报告模板开头 chunk `#6` 从第 2 提到第 1，比原第 1 的模板后半段 `#7` 更适合作为回答入口。
- RAG-07：弱相关 case 仍拒答；`LOCAL_BGE` 只是把“CPU 分析示例”chunk 提到第 1，没有把系统推向幻觉回答。
- RAG-13：答案仍 2/2；`LOCAL_BGE` 把长英文控制组开头 chunk `#0` 提到第 1，但直接包含 fallback queue / retry budget 的 `#4` 仍在第 2，所以最终答案不退化。
- RAG-14：仍拒答 cloud provider；rerank 排序变化没有破坏“文档未指定”的边界。

**可讲结论**：

- rerank 的价值不是“增加新文档”或“提高召回数量”，而是在候选池已经召回后，用 query + chunk 的交互相关性重新排序。
- 这批样本中，Hybrid + gating 后的候选池已经足够覆盖核心答案，所以 LOCAL_BGE 没有把 pass rate 从 14/14 再往上拉。
- 它的正向作用体现在 attribution：能解释为什么某些 chunk 应该前移 / 后移，并为后续更大候选池、更复杂 query、长文档场景提供安全扩展点。
- 它的边界也很清楚：RAG-10 仍缺 3 个 literal points，说明 rerank 不解决“候选池没有完整覆盖”或“生成阶段没有展开所有细节”的问题。

**下一步候选**：

1. Step 5.3 已由 commit `cc20ba4` 提交：singleton 刷新修复与 A/B 评测证据已归档。
2. Step 5.4 Rerank 工程化收口已完成：本地 POC 能力已收敛成可配置、可观测、可降级的工程能力，并补齐面试解释口径。
3. Step 5.4 代码待用户 review 后自行提交；功能主线可进入 Step 6 Query rewrite，用 multi-query / RAG-fusion / HyDE 解决“原 query 候选池覆盖不足”的问题。

### D16（已完成并验证 2026-05-14）：Step 5.4 Rerank 工程化收口

**为什么还需要 Step 5.4**：

Step 5.3 已经证明真实 rerank 链路生效，但当前仍有明显 POC 边界：`HttpDocumentReranker` 的本地 HTTP 地址 / timeout 仍偏硬编码，本地 Python 服务生命周期不属于 Java 应用，rerank 服务不可用时的端到端 fallback 还没有专门 smoke，报告里也还需要更直接地解释 model / endpoint / failureReason。

因此 Step 5.4 的目标不是再证明“准确率提升”，而是把 rerank 做成可运维、可解释、可面试复述的工程闭环。

**计划范围**：

1. 配置化：把 `HttpDocumentReranker` 的 enabled、baseUrl 或 endpoint、connect/read timeout、modelName 等从代码常量迁入 Spring 配置。
2. 可观测：补齐 rerank endpoint / model / failureReason / fallback mode 等观测字段，让报告能解释“为什么这次用了模型”或“为什么降级”。
3. 降级验证：做服务健康与服务不可用两类 smoke test。服务不可用时应 fallback 到 `PASSTHROUGH`，Java 主链路不崩，报告能看到失败原因。
4. 面试口径：把 rerank 的“为什么做、解决什么、没有解决什么、实测证据是什么”沉淀到计划或笔记。

**实现范围**：

- `HttpDocumentReranker`：去掉硬编码 URL / timeout 作为唯一来源，改为读取 `spring.ai.rag.rerank.local-bge.enabled/endpoint/connect-timeout/read-timeout/model-name`；fallback 改为依赖 `PassthroughDocumentReranker` bean。
- `RerankResult`：新增 `modelName` / `endpoint`，与既有 `failureReason` 一起承载运行时观测信息。
- `RagObservationKeys.Qa` + `RagAnswerAdvisor`：新增并透传 `qa_rerank_failure_reason` / `qa_rerank_model_name` / `qa_rerank_endpoint`。注意 advisor context 不能放 null，否则 Spring AI 后续 `Map.copyOf` 会抛 NPE，因此空值用空字符串。
- `rag_eval_runner.py`：在 retrieval details 中新增 `rerank_runtime: model / endpoint / failure_reason`。
- `application-dev.yml`：新增本地 bge reranker 配置入口，支持环境变量覆盖。

**非目标**：

- 不换 rerank 模型。
- 不把 Python 服务强行塞进 Java 进程生命周期。
- 不新增复杂评测集。
- 不进入 Step 6 Query rewrite。
- 不把 Step 5.3 的 A/B 结果包装成“答案指标显著提升”。

**验收标准**：

| 验收项 | 通过标准 |
|---|---|
| 配置化 | URL / timeout / enabled / modelName 不再散落在 `HttpDocumentReranker` 常量中 |
| 正常链路 | 本地 rerank 服务健康时，RAG-04 smoke 仍显示 `rerank applied=true / mode=LOCAL_BGE` |
| 降级链路 | rerank 服务不可用或配置禁用时，RAG 主链路仍完成，且报告能看到 fallback reason |
| 可解释性 | 评测报告或 metadata 能解释 rerank 模型、endpoint、是否 applied、失败原因 |
| 面试材料 | 能用 1-2 分钟讲清 Step 5 为什么做、做了什么、效果与边界 |

**验证产物**：

| 场景 | 端口 / 配置 | 产物 | 关键观测 |
|---|---|---|---|
| 正常 LOCAL_BGE | `8100`，endpoint=`127.0.0.1:18080/rerank` | `results/step5.4-rerank-engineering/rag-eval-result-step5.4-live-rag04.md` | RAG-04 completed=true，literal 3/3，`applied=true / mode=LOCAL_BGE`，runtime model/endpoint 正常，failure_reason 空 |
| 服务不可用 fallback | `8101`，endpoint=`127.0.0.1:18081/rerank` | `results/step5.4-rerank-engineering/rag-eval-result-step5.4-fallback-rag04.md` | RAG-04 completed=true，literal 3/3，`applied=false / mode=PASSTHROUGH`，failure_reason=`RERANK_HTTP_IO_ERROR:ConnectException` |
| 配置禁用 fallback | `8102`，`RAG_RERANK_LOCAL_BGE_ENABLED=false` | `results/step5.4-rerank-engineering/rag-eval-result-step5.4-disabled-rag04.md` | RAG-04 completed=true，literal 3/3，`applied=false / mode=PASSTHROUGH`，failure_reason=`RERANK_DISABLED` |

**收口结论**：

Step 5.4 不改变 Step 5.3 的效果结论：rerank 的答案指标仍不能被包装成显著提升。它完成的是工程化闭环：真实 reranker 可配置、可观测、可降级，报告能直接解释用了哪个模型、打到哪个 endpoint、为什么 fallback。

**辅导节奏**：

1. 先讲清“配置类 / 策略工厂 / fallback / 观测字段”的职责边界。
2. 用户主笔改 Java 代码，Codex 负责拆步骤、review、小范围修补和验证。
3. 每个小改动都先跑 compile 或 smoke，再进入下一步。
4. Step 5.4 已完成；代码待用户 review 后自行提交，功能主线可进入 Step 6。

### Commit 范围（已落地）

```
cc20ba4 fix: 修复 Armory 重装配并归档 RAG rerank A/B
176abc5 feat: 接入本地 RAG rerank 模型服务
c95aabd feat: 完成 RAG rerank 抽象接入
631ffd2 refactor: 抽取 RAG 观测字段常量
a820144 feat: 增加 RAG 候选召回阈值观测
555df14 fix: 透传 RAG rerank 观测字段
fca43f2 feat: 增加 RAG rerank 占位与评测观测
a7826bc feat: 收口 Hybrid RAG 校准
0314954 test: 补充 Hybrid RAG 归因评测
f65c365 feat: expose hybrid retrieval metadata
aee5a75 feat: add hybrid RAG retrieval
2877cbc test(rag): add low-newline control eval cases
ec0e4fa feat(rag): Step 3 Phase A 收口 - chunker v3 (250/109) 决策门 4/4 通过
05a3f49 feat(rag): Step 3.1-3.5 chunker参数化+chunk metadata+渲染来源+Phase A评测
e4dea8b docs(rag-eval): 补齐 944c006 漏 stage 的 PLAN.md 目录索引
944c006 docs(rag-eval): reorganize results into _archive/ and ffix/ subdirs
f5b33a8 fix: Codex P2 - runner completed 语义 + 三列 — 含义文档化
5ace3bd docs: PLAN.md 回写 D3 决议（A+B 修复 RAG-05）+ commit 范围归档
3093eb6 docs: 修正 baseline-ffix.md 为 a9fbf7e fix 后真实重跑数据
5a92e60 docs: RAG-05 单跑验证证据归档（A read-timeout 修复直接证据）
a9fbf7e fix: SSE 异常透传 + DeepSeek read-timeout 25s→120s
f78cde1 chore: gitignore 增量 - 本地日志目录 + Python 缓存
fc49eb4 docs: RAG eval 接力计划 + 历史评测产物归档
6da727e feature: RAG eval - F-fix 链路 SSE 下发 type=retrieval 事件
```

## 4. 后续计划（Step 6.8 KnowledgeBaseProfile 面试版）

按下一步时机排序：

1. Step 6.3-6.7 已阶段性收口：RAG-fusion、per-variant rerank RRF、fusion-aware rerank、LLM rewrite 工程骨架和 Step 6.7 eval 产物均已落地。当前不继续硬调 `LlmQueryRewriter` prompt。
2. Step 6.8 KnowledgeBaseProfile 面试版已完成（2026-05-23）：
   - 入库阶段：`RagService.storeRagFile()` 在 Tika 解析 / TokenTextSplitter 切分之后，使用规则抽取 profile hints；第一版不接离线 LLM。
   - 存储阶段：新增轻量 `ai_client_rag_profile` 表保存知识库级 profile JSON；profile 不进入 answer prompt，也不作为事实依据。
   - 查询阶段：`AiClientAdvisorNode -> RewriteContext -> RewritePipeline` 加载 profile hints，并按 user query 选择 topN `selectedProfileHints`；如果 profile 已加载但没有匹配，source 为 `NO_PROFILE_MATCH`，不回退手工 hints。
   - 观测阶段：报告新增 `profile_hints: source ... / selected ...`，viewer 新增 Step 6.8 group metadata。
   - 验证阶段：窄回归与 full 14 均已跑通；最终 full 14/14 completed，RAG-04 `3/3`、RAG-07/RAG-08/RAG-14 拒答保持，RAG-10 自动选中内存指标 hints 但答案仍可能漏三档范围。
3. Step 6.9 v2 离线 LLM KnowledgeBaseProfile 已完成 fresh BGE 验证：`hybrid-v2` profile 能进入 query-time selection，BGE rerank 真实生效，RAG-14 拒答边界不破；RAG-10 仍漏三档范围，归因不继续放在 profile / rewrite。
4. Step 7 Answer Generation / Context Salience：仅当 Step 6.8/6.9 证明关键证据已经进入 final documents 但最终答案仍漏点时启动。它解决“找到了会不会用”，不要和 profile 自动化混成一个实验。
5. 延期话题：RAG-09 / RAG-10 paraphrase regression、RAGAS 集成、embedding 模型迁移、profile 管理端、profile 版本管理、多知识库 profile merge。

**执行原则**：Step 6.8 只证明一件事：`queryRewriteDomainHints` 能从手工配置升级为自动 profile + query-time selection。不要同时改 rerank policy、answer prompt、embedding 或评测口径。

### Step 6.9 v2 离线 LLM KnowledgeBaseProfile（已验证）

2026-05-23 已完成 v2 profile 小闭环与 fresh BGE 验证。早期 Step 6.9 报告中有 BGE 未启动和 stale fat jar 两类无效样本，已从结果目录中删除；保留的有效报告只包含真实 `rerank applied=true` 的 fresh 结果：

- `results/step6.9-llm-profile/rag-eval-result-step6.9-hybrid-v2-dedicated-local-bge-narrow-fresh.md`
- `results/step6.9-llm-profile/rag-eval-result-step6.9-hybrid-v2-dedicated-local-bge-full-fresh.md`

工程落点：

- `HybridKnowledgeBaseProfileExtractor` 成为主入口：先跑 `RuleBasedKnowledgeBaseProfileExtractor`，再可选合并 `LlmKnowledgeBaseProfileExtractor` 输出；LLM 关闭或失败时保留 `rule-v1` 结果。
- `LlmKnowledgeBaseProfileExtractor` 只在入库阶段执行，配置项为 `spring.ai.rag.profile.llm.*`；输出 fields 包括 `summary / concepts / aliases / questionsAnswered / negativeScopes / evidenceTypes`。
- v2 防幻觉边界：LLM 输出的结构化项必须带 `sourcePath / chunkIndex / evidenceText`，本地校验 evidenceText 能在对应 chunk 中找到，否则丢弃，不进入 `selectedProfileHints`。
- 存储仍复用 `ai_client_rag_profile.profile_json`；`KnowledgeBaseProfile` 扩展 JSON 字段，但 query-time 第一版仍向下兼容 `ProfileHint`。
- report 观测补充 `profileVersion`，继续展示 `profileSource / selectedProfileHints`，用于区分 `rule-v1` 与 `hybrid-v2`。

fresh 验证结论：

- Full eval 14/14 completed；14 条均显示 `profile_hints: version hybrid-v2`。
- 14 条均显示 `rerank_runtime: model bge-reranker-v2-m3`，非空 `failure_reason=0`，`PASSTHROUGH=0`。
- RAG-10 选中 `node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes / memory_available / memory_usage / 内存使用率查询` 等 hints，说明 LLM profile 已进入 query-time selection。
- RAG-14 显示 `source NO_PROFILE_MATCH / selected —`，拒答边界未被 profile 强行拉偏。
- RAG-10 最终仍为 `2/5`，漏 `正常范围 / 警告范围 / 危险范围`；由于 profile / rewrite / rerank 已有证据，下一步归因转向 Answer Generation / Context Salience。

### Step 7 Answer Generation / Context Salience（验证中）

2026-05-23 在 Step 6.9 fresh BGE 基础上进入 Step 7。只读诊断先确认：

- 原文 `docs/dev-ops/rag-file/grafana-mcp-tools-guide.md:160-164` 包含完整“内存数据解释”：`正常范围 0-80% / 警告范围 80-95% / 危险范围 95-100%`。
- pgvector 真实 chunk 中，完整内存范围在 `chunkIndex=5`；Step 6.9 full 报告最终 documents 命中的是 `chunkIndex=6`，该 chunk 只从 `危险范围: 95-100%` 这一截开始。
- 因此断点不是 profile 或 rerank 未生效，而是 final context 组装和 answer generation 合同没有把跨 chunk 的关键标准稳定呈现给模型。

本轮实现边界：

- 不修改 rewrite/profile/rerank/embedding/eval 口径。
- `RagAnswerAdvisor` 的 answer prompt 增加通用回答合同：如果上下文含公式、阈值、范围、判断标准、状态等级、参数、示例或排查步骤，相关时必须显式展开；上下文没有的范围不能编造。
- 新增 `ContextSalienceSupport`：在 final context 渲染时识别 `formula / range / judgement / procedure / parameter / example` 证据提示。
- 对疑似被 chunk 边界切开的结构化证据块，渲染层按同一 `knowledge + sourcePath + previous chunkIndex` 读取相邻前一 chunk，作为 `[相邻证据片段]` 放进当前引用块下；触发条件不再依赖 RAG-10 的 `正常/警告/危险` 词表，而是看当前 chunk 是否从列表项、表格行或代码块中间开始，并且包含范围/阈值/参数/代码等结构化证据。这不改变检索命中和 rerank 排序，只增强最终上下文显著性。
- 观测字段新增 `qa_context_salience_cues` / `qa_context_salience_expansion_count`，runner 报告展示 `context_salience`。

验证产物：

- 窄集：`results/step7-answer-context-salience/rag-eval-result-step7-salience-narrow.md`
  - RAG-04 `3/3`，RAG-07/RAG-14 拒答保持。
  - RAG-10 `5/5`；`context_salience expansions=1`；`profile_hints version hybrid-v2`；`rerank_runtime model bge-reranker-v2-m3`。
- Full：`results/step7-answer-context-salience/rag-eval-result-step7-salience-full.md`
  - 14/14 completed。
  - literal cases 全命中：RAG-02 `4/4`、RAG-03 `3/3`、RAG-04 `3/3`、RAG-05 `5/5`、RAG-10 `5/5`、RAG-11 `2/2`、RAG-13 `2/2`。
  - 14 条均显示 `rerank_runtime: model bge-reranker-v2-m3`，`PASSTHROUGH=0`，非空 `failure_reason=0`。
  - `context_salience expansions=1` 仅出现在 RAG-05 和 RAG-10；RAG-10 从 Step 6.9 full 的 `2/5` 回到 `5/5`。
- 泛化复盘：`results/step7-answer-context-salience/rag-eval-result-step7-boundary-verbatim-full.md`
  - 14/14 completed，BGE rerank 正常，拒答 case 未被 profile/salience 明显拉偏。
  - RAG-04 在 full 中 `3/3`；窄集曾出现 `7*24*3600` 被模型格式化为 `7 * 24 * 3600`，属于 literal smoke 的格式保真风险，不改 eval 口径。
  - RAG-10 full 又回到 `2/5`，但这次断点不同：候选池有 `chunkIndex=5/6`，final topK 没有保住阈值标准 chunk，`context_salience expansions=0`。这说明“相邻 chunk 补全”只能解决已进入 final context 的 chunk 边界切断，不能解决关键证据未进入 final topK。
  - 曾尝试让 coverage guard 使用 all-hit `bestQueryVariantRank/queryVariantIndexes`，窄集验证未解决 RAG-10，且可能扩大无关 case 的 guard 行为，已撤回。下一步若继续 Step 7，应做更明确的 evidence-type coverage：当 query/rewrite/profile 表达了 `threshold / judgement / range / parameter / procedure` 意图时，从候选池中保护少量同类证据 chunk，而不是扩大普通 variant coverage。

面试口径：

> 前面的 Query Rewrite / Profile / Hybrid / BGE rerank 解决的是 find evidence；Step 7 解决 use evidence。第一版 salience 证明了：当关键证据已经进入 final context 但被 chunk 边界切断时，可以通过 answer contract + 相邻证据补全提高生成稳定性。泛化复盘进一步暴露第二类问题：关键证据在候选池里但没进入 final topK 时，单纯的相邻补全无效，后续要做 evidence-type coverage，而不是继续硬调 rewrite prompt 或把 profile 当答案事实。

## 5. 关键约束 / 隐藏陷阱

- 业务代码 / 对外文档不使用 emoji（CLI 提示文案例外）
- Java 符号检索必须 Serena 优先（`mcp__serena__find_symbol` / `find_referencing_symbols` / `get_symbols_overview`），不要用 rg 替代——PreToolUse hook 会硬拦 Grep 纯 Java 符号
- rg / grep 只用于 yml / xml / sql / md / log / SSE 字面量
- `LlmInvocationExecutor` 的 timeout 异常是不可重试（commit `32aec6d` 约定），改要先判定 memory 污染 / 重复请求风险
- Maven 走阿里云 Nexus（`pom.xml:20-32`），断网 / VPN 注意
- 项目级 `.claude/` 与根 `CLAUDE.md` 在 `.gitignore`（`.gitignore:40,43`），无法 commit 到主仓——本地或外部 dotfiles repo
- Application 默认端口 `8099`，启动：`mvn -pl ai-agent-boot spring-boot:run`
- MySQL 容器名 `mysql`，root/123456，库 `ai-agent-station-study`
- 业务 Controller：`AiAgentController`，POST `/api/v1/agent/auto_agent`

## 6. 证据锚点（验证用，不要凭记忆）

- F-fix 代码：`ai-agent-domain/src/main/java/com/tricoq/domain/agent/service/fixed/FixedAgentExecuteStrategy.java:46-87, 105-126`
- F-fix 验证：`docs/dev-ops/rag-eval/results/ffix/rag-eval-result-ffix.md`
- 死路径反证：`docs/dev-ops/rag-eval/results/_archive/rag-eval-result-bsymmetric.md:13-15`
- 排障纪律：`.claude/rules/agent-runtime-path-proof.md`
- 项目记忆：`CLAUDE.md`（项目根，gitignored）
- 全局红线：`~/.claude/CLAUDE.md`

## 7. 接手后第一步清单

下一会话接手时：

1. Read 这份 `PLAN.md` 全文（特别是 §3 D10-D16：Hybrid、Rerank、A/B 归因与 Step 5.4 工程化收口）
2. 确认 §3 commit 范围是否已落地（`git log --oneline -10` 看 `cc20ba4`、`176abc5`、`c95aabd`、`631ffd2` 等 Step 5 提交）
3. 当前游标：**Step 6.8 KnowledgeBaseProfile 面试版已完成实现与验证**。LLM Query Rewrite 主线已经从手工 `queryRewriteDomainHints` 升级为自动 profile + query-time selection + report 可观测；后续若继续追 RAG-10 漏范围，应切到 Answer Generation / Context Salience，不要继续把 Grafana/PromQL 词硬写进 prompt，也不要把 profile 当作回答事实依据。
4. 如果改 yml / 重启 backend / 重灌向量库后再跑 eval：必须先预热 `POST http://localhost:8099/api/v1/agent/armory_agent` body `{"agentId":"rag_demo"}`（未预热直接打 auto_agent 会 HTTP 500，duration ~5ms，看似 endpoint 死了）
5. **不要**自行重跑 v1/v2/v3 中任何一轮——Phase A 已锁定参数 250/109，重跑只会消耗 LLM 配额且 score 必然飘动（embedding 不变 score 应稳定，LLM 输出会因 sampling 飘）

**禁止**：
- 直接重做 F-fix（已验证生效）
- 在 auto chain 上加 RAG observer（死路径已证）
- 凭 endpoint 名字推断 strategy
- 批量推进 §4 延期话题
- push 任何 commit
- 改 `~/.claude/` 任何东西（用户已搁置配置优化分支）
- 在没预热的情况下跑评测 runner（结果会全是 completed=false / answer 空 / 三列 `—`，看起来像 backend bug）

## 8. 这份 PLAN 自身的维护

- 任何会话推进了进展，**必须**回写到 §2 进展 / §3 决策项 / §4 延期话题
- 不要重写 §1 总目标和 §5 约束，除非确实变化
- 同时更新顶部"最后更新"日期

## 9. Step 3 — Chunker + Chunk Metadata（completed，May 4 启动，May 6 Phase A.5 收口）

> Phase A 实现由 AI 协作辅助完成（候选人逐行 review + 验证 + 跑 verify）；Phase B（条件触发，复杂度更高）保留候选人主笔。授权变更日期：2026-05-05。
> 拍板基线见 §3 D4，本节是执行手册。
> 当前游标（2026-05-06 EDT）：**Phase A.5 已收口（D9）**——3.1-3.6 完成（v1/v2/v3 三轮调参），3.7 Phase B 不触发，D8 generalizability 缺口已由 D9 低换行控制组验证。Phase A 锁定参数：chunk-size=250 / min-chunk-size-chars=109。

### 9.1 改动落点表

| 落点 | 文件:行 | 当前状态 | Step 3 改动 |
|---|---|---|---|
| A. chunker @Bean | `ai-agent-boot/src/main/java/com/tricoq/config/AiAgentConfig.java:62-65` | `new TokenTextSplitter()` 全部默认参数 | 改为带参构造，5 个参数从 `application.yml` 注入 |
| B. 写入端 metadata | `ai-agent-domain/src/main/java/com/tricoq/domain/agent/service/rag/RagService.java:50-58` | metadata 仅 3 字段（knowledge / rag_id / file_name），全部文件级 | 增 chunk 级字段（chunkIndex / totalChunks / sourcePath；parentSection / headingPath 字段建好留空） |
| C. 渲染端 | `ai-agent-domain/src/main/java/com/tricoq/domain/agent/service/armory/node/factory/element/RagAnswerAdvisor.java:233-275`（`renderDocumentContext`）| 仅渲染 `[i+1] + text`，metadata 完全不进 prompt | 在 `[i+1]` 后追加来源行（如 `(来自: foo.md / 章节: Sec 1.2)`），让 LLM 可引用溯源 |
| D. 配置入口（备用） | `ai-agent-domain/src/main/java/com/tricoq/domain/agent/model/enums/AiClientAdvisorTypeEnumVO.java:51-55` | 配置驱动（topK / threshold 从 DB `ai_client_advisor` 表 → DTO 流入）| **本阶段不动**，保持 topK=4 / threshold=0.6 |

### 9.2 执行步骤（每步独立可回滚）

1. **3.1 TokenTextSplitter 参数化** — 改 A 落点；`application-dev.yml` 加 chunker section（chunkSize / minChunkSizeChars / minChunkLengthToEmbed / maxNumChunks / keepSeparator）
2. **3.2 写入端 chunk 级 metadata 扩展** — 改 B 落点；jsonb 自由扩展无 schema cost
3. **3.3 渲染端展示 metadata** — 改 C 落点；让 LLM 看到 chunk 来源
4. **3.4 vector store 重灌** — `TRUNCATE vector_store_openai` + admin 接口重新上传 `rag_demo` 原始文档
5. **3.5 跑 baseline 评测** — `python rag_eval_runner.py`，10 条 case
6. **3.6 决策门** — 看 §3 D4 验收口径
   - **结果（D7）**：v1 部分达标（gate 1/4） → v2 调参 chunkSize 800→400（gate 3/4，gap 翻正） → v3 再调 400→250（gate 4/4，RAG-04 max=0.7406 跨过 0.70 决策门，gap +0.0680 扩大 8×）
   - **Phase A 收口（D7）**：锁定参数 chunk-size=250 / min-chunk-size-chars=109，不触发 3.7 Phase B
7. **3.7 [Phase B 条件触发] MarkdownTextSplitter 自写**（**不触发**：D7 决议反向不命中 RAG-04 max ≤ 0.65 阈值）— `implements org.springframework.ai.transformer.splitter.TextSplitter`，按 file extension 路由（`.md` → MarkdownTextSplitter / 其它 → TokenTextSplitter 兜底）

### 9.3 Spring AI 1.0.3 splitter 现状（context7 已验证）

- 标准库**仅 `TokenTextSplitter`**，无 markdown-aware / recursive char / html splitter
- `TokenTextSplitter` 5 参数默认值：`(chunkSize=800, minChunkSizeChars=350, minChunkLengthToEmbed=5, maxNumChunks=10000, keepSeparator=true)`
- encoding 固定 CL100K_BASE（OpenAI tiktoken 系，1 token ≈ 1.5~2 中文字符）
- 自写需 implements `org.springframework.ai.transformer.splitter.TextSplitter`（SPI 简单，单方法 `splitText(String): List<String>`）

### 9.4 上下文恢复 hook（防 /compact 后丢失）

/compact 后回到此处，按以下顺序读：

1. **§3 D4** — 拍板基线（topK / threshold / chunker / metadata / 重灌 / baseline）
2. **§9.1** — 落点表（精确到 file:line）
3. **§9.2** — 步骤表（当前进展看哪一步是 in_progress）
4. **§9.3** — Spring AI 1.0.3 splitter 现状
5. **baseline / threshold 文件** — `results/ffix/rag-eval-result-baseline-ffix.md`（10 条全跑）+ `results/ffix/rag-eval-result-t055-triangle-ffix.md`（RAG-04 max=0.6481）
6. **生产 live path proof** — `RagAnswerAdvisor` 在 `ai-agent-domain/.../element/`（不是 boot/test 下的同名 spike），`fixedAgentExecuteStrategy` 是 rag_demo 的 strategy

### 9.5 面试故事链（写笔记时参考）

- "默认 TokenTextSplitter 800 token 切，发现 RAG-04 max_score 偏低（0.6481）"
- "假设：chunk 太大稀释正例 token 比例"
- "Phase A：调 chunkSize 验证假设 + 加 chunk 级 metadata（溯源能力）"
- "Phase A 不达标？→ Phase B：自写 MarkdownTextSplitter，按 heading 切，加 headingPath"
- "evaluation 驱动迭代：margin-based 看 score gap，不看绝对值"
