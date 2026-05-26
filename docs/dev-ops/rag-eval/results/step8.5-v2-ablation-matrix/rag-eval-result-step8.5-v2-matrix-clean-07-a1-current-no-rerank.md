# RAG Eval Result

- generated_at: `2026-05-25 16:37:10`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_profile_v2_demo`
- eval_schema_version: `v2`
- case_schema_version: `v2-compatible`
- rubric_coverage: `6/6`

## Summary

> Eval v2 把旧 `literal_hit` 降级为 `literal_smoke`，并把一次 RAG 结果拆成 candidate -> final context -> answer 三层漏斗。Step 8.1 仍复用旧 `expected_points` 子串口径计算 candidate/final recall；真正的语义 rubric 从 Step 8.2 开始。
>
> `retrieved` / `score` / `empty` 三列的 `—` 表示**没拿到成功的 ChatResponse metadata**，不等价于"无检索"。当前实现把 retrieval SSE 帧放在 `.call().chatResponse()` 返回之后才发，所以 LLM 调用失败时（即使 RAG 检索本身成功）三列都会是 `—`。要区分"检索失败"和"生成失败"，对照 `error` 列 / details 区 / backend log。
>
> Details 区的 `pre_rerank_documents` 展开 rerank 前候选池，`documents` 展开最终 top-K chunk attribution；document 行的 `score` 是当前阶段写回的候选分，可能来自 HYBRID RRF、query-variant fusion、per-variant rerank RRF 或 fusion-aware rerank；原始向量分与关键词分分别看 `vectorScore` / `keywordScore`，多 query 召回融合看 `queryFusionScore/queryFusionRank/queryVariantHitCount`；最终 rerank 写回分看 `rerankScore`，多路 rerank 融合分看 `rerankFusionScore/rerankVariantHitCount`，fusion-aware 弱加成看 `queryFusionBoostScore/fusionAwareScore`；rerank 服务状态看 `rerank_runtime` 的 model / endpoint / failure_reason；keyword 分支未参与时看 `keywordSkippedReason`。

| id | type | completed | duration_ms | health | failure_layer | candidate_recall | final_context_recall | c2f_delta | f2a_delta | semantic_score | answer_completeness | literal_smoke | answerability | source_coverage | answer_preview |
|---|---|---:|---:|---|---|---|---|---:|---:|---|---|---|---|---|---|
| RAG-10 | paraphrase | true | 23459 | ok | none | 5/5 | 5/5 | +0 | +0 | 5/5 | 5/5 | 5/5 | true_answer | source_not_configured; file final=false; section final=false; points final=5/5 | 根据知识库提供的查询思路，排查内存压力主要按以下步骤进行： ### 1. 核心 PromQL 查询（内存使用率） 使用 Prometheus 查询当前内存使用率，以此评估内存压力状况 \[1\]： ```promql (1 - (node_... |
| RAG-14 | control_plaintext_en_weak_related | true | 11002 | ok | none | 1/1 | 1/1 | +0 | +0 | 1/1 | 1/1 | n/a | true_refusal | source_not_configured; file final=false; section final=false; points final=manual | The control document does not identify a cloud provider for the fallback queue. It explicitly states that a question abo... |
| EXT-ARTEMIS-01 | answerable | true | 11990 | fail | answer_semantic_miss | 3/3 | 3/3 | +0 | -2 | 1/3 | 1/3 | 0/3 | true_answer | answer_generation_or_literal_mismatch; file final=true; section final=true; points final=3/3 | 根据提供的任务资料，Artemis I 的发射日期、溅落日期和任务持续时间如下： - **发射日期**：2022年11月16日[1] - **溅落日期**：2022年12月11日[1] - **任务持续时间**：25天10小时53分钟[1] |
| EXT-RFC9110-03 | answerable_formula | true | 12113 | fail | answer_semantic_miss | 3/3 | 3/3 | +0 | -1 | 2/3 | 2/3 | 2/3 | true_answer | answer_generation_or_literal_mismatch; file final=true; section final=true; points final=3/3 | 根据提供的上下文，RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有以下明确限制： > HTTP/1.0 did not define 1xx status codes. A server **MUST NOT** send... |
| EXT-K8S-PV-03 | answerable_long_context | true | 12092 | ok | none | 5/5 | 5/5 | +0 | +0 | 5/5 | 5/5 | n/a | true_answer | section_in_final_context; file final=true; section final=true; points final=manual | 当 PVC 扩容失败时，文档给出的恢复流程如下（来自 [2]）： 1. 将绑定到该 PVC 的 PersistentVolume 的回收策略标记为 **Retain**。 2. 删除该 PVC。由于回收策略为 Retain，删除 PVC 不... |
| EXT-PG-04 | answerable_version_conflict | true | 18283 | fail | retrieval_miss | 4/5 | 4/5 | +0 | +0 | 4/5 | 4/5 | n/a | true_answer | section_in_final_context; file final=true; section final=true; points final=manual | 当知识库同时包含 PostgreSQL 16 和 18 的文档时，回答生成列的 virtual/stored 差异必须明确区分版本，避免将 PostgreSQL 18 的行为错误地套用到 PostgreSQL 16。根据上下文信息： - *... |


## Eval v2 By Case Type

| case_type | cases | runtime_ok | candidate_recall | final_context_recall | semantic_avg | answer_completeness_avg | literal_smoke | failure_layers |
|---|---:|---:|---|---|---:|---:|---|---|
| answerable | 1 | 1/1 | 3/3 | 3/3 | 33.3% | 33.3% | 0/3 | answer_semantic_miss:1 |
| answerable_formula | 1 | 1/1 | 3/3 | 3/3 | 66.7% | 66.7% | 2/3 | answer_semantic_miss:1 |
| answerable_long_context | 1 | 1/1 | n/a | n/a | 100.0% | 100.0% | n/a | none:1 |
| answerable_version_conflict | 1 | 1/1 | n/a | n/a | 80.0% | 80.0% | n/a | retrieval_miss:1 |
| control_plaintext_en_weak_related | 1 | 1/1 | n/a | n/a | 100.0% | 100.0% | n/a | none:1 |
| paraphrase | 1 | 1/1 | 5/5 | 5/5 | 100.0% | 100.0% | 5/5 | none:1 |

## Answerability Matrix

| bucket | count |
|---|---:|
| true_answer | 5 |
| true_refusal | 1 |
| false_refusal | 0 |
| false_answer | 0 |
| n/a | 0 |

## Details

### RAG-10 - paraphrase

- question: 如果我要查内存压力，知识库给出的查询思路是什么？
- should_answer: `true`
- expected_source_section: 内存使用率查询 / 内存数据解释
- completed: `true`
- duration_ms: `23459`
- score_mode: `literal`
- eval_v2: verdict `pass` / failure_layer `none` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `5/5` / literal_smoke `5/5`
  - answer_completeness `5/5` / evidence_source_unverified `false`
  - semantic_hits `memory_formula_available \| memory_formula_total \| normal_range \| warning_range \| danger_range` / semantic_misses `—`
  - evidence_funnel: candidate `5/5` -> final_context `5/5` -> answer `5/5`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `source_not_configured` / source_file `—` / section `内存使用率查询 / 内存数据解释`
  - pre_rerank: docs `6` / source_file `false` / section `false` / expected_points_exact `5/5`
  - final_context: docs `4` / source_file `false` / section `false` / expected_points_exact `5/5`
- expected_points:
  - [x] node_memory_MemAvailable_bytes
  - [x] node_memory_MemTotal_bytes
  - [x] 正常范围
  - [x] 警告范围
  - [x] 危险范围

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0320 .. 0.0484`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `3023` / max `6000`
  - context_salience: cues `formula \| judgement \| procedure \| range` / expansions `1`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `6` / final `4`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `如果我要查内存压力，知识库给出的查询思路是什么？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3081` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `如果我要查内存压力，知识库给出的查询思路是什么？， \| memory pressure 指标 node_memory_pressure 内存压力 \| 内存压力 查询步骤 判断方法 阈值 指标解读 排查思路`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `node_memory_MemAvailable_bytes \| node_memory_MemTotal_bytes \| memory_available \| memory_status \| memory_trend \| memory_usage \| 内存使用率查询 \| 七天检查窗口的作用是什么？`
  - pre_rerank_documents:
    - #1 score=`0.0484`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6819`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"},{"index":2,"rank":4,"text":"memory pressure 指标 node_memory_pressure 内存压力"},{"index":3,"rank":1,"text":"内存压力 查询步骤 判断方法 阈值 指标解读 排查思路"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #2 score=`0.0474`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6684`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0474`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"},{"index":2,"rank":2,"text":"memory pressure 指标 node_memory_pressure 内存压力"},{"index":3,"rank":4,"text":"内存压力 查询步骤 判断方法 阈值 指标解读 排查思路"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #3 score=`0.0325`, source=`grafana-mcp-tools-guide.md`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6731`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`1`, queryVariantText=`memory pressure 指标 node_memory_pressure 内存压力`, queryFusionScore=`0.0325`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`1`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":1,"text":"memory pressure 指标 node_memory_pressure 内存压力"},{"index":3,"rank":2,"text":"内存压力 查询步骤 判断方法 阈值 指标解读 排查思路"}]`
      - preview: # 磁盘使用增长趋势 predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) ``` ## 数据解释和分析标准 ### CPU数据解释 - **数值范围**: 0-100% - **正常范围**: 0-70% - **警告范围**: 70-90% - **危险范...
    - #4 score=`0.0320`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6741`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0320`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"},{"index":3,"rank":3,"text":"内存压力 查询步骤 判断方法 阈值 指标解读 排查思路"}]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #5 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6692`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"}]`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...
    - #6 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6588`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`6`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`memory pressure 指标 node_memory_pressure 内存压力`, queryFusionScore=`0.0159`, queryFusionRank=`6`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":3,"text":"memory pressure 指标 node_memory_pressure 内存压力"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
  - documents:
    - #1 score=`0.0484`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6819`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"},{"index":2,"rank":4,"text":"memory pressure 指标 node_memory_pressure 内存压力"},{"index":3,"rank":1,"text":"内存压力 查询步骤 判断方法 阈值 指标解读 排查思路"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #2 score=`0.0474`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6684`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0474`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"},{"index":2,"rank":2,"text":"memory pressure 指标 node_memory_pressure 内存压力"},{"index":3,"rank":4,"text":"内存压力 查询步骤 判断方法 阈值 指标解读 排查思路"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #3 score=`0.0325`, source=`grafana-mcp-tools-guide.md`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6731`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`1`, queryVariantText=`memory pressure 指标 node_memory_pressure 内存压力`, queryFusionScore=`0.0325`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`1`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":1,"text":"memory pressure 指标 node_memory_pressure 内存压力"},{"index":3,"rank":2,"text":"内存压力 查询步骤 判断方法 阈值 指标解读 排查思路"}]`
      - preview: # 磁盘使用增长趋势 predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) ``` ## 数据解释和分析标准 ### CPU数据解释 - **数值范围**: 0-100% - **正常范围**: 0-70% - **警告范围**: 70-90% - **危险范...
    - #4 score=`0.0320`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6741`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0320`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"},{"index":3,"rank":3,"text":"内存压力 查询步骤 判断方法 阈值 指标解读 排查思路"}]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...

answer:

```text
根据知识库提供的查询思路，排查内存压力主要按以下步骤进行：

### 1. 核心 PromQL 查询（内存使用率）
使用 Prometheus 查询当前内存使用率，以此评估内存压力状况 \[1\]：
```promql
(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
```
该公式通过 `MemAvailable` 与 `MemTotal` 的比值计算出已用内存的百分比。

### 2. 辅助查询（内存使用量）
如需直观了解实际内存使用量（GB），可使用 \[2\]：
```promql
(node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024
```
此外，可按实例分组查看各主机的内存使用率 \[2\]（通过 `avg by (instance)` 实现）。

### 3. 压力判定标准
获得内存使用率百分比后，按以下阈值判断压力等级 \[3\]：
- **正常范围**：0 – 80%
- **警告范围**：80 – 95%
- **危险范围**：95 – 100%

### 4. 进一步分析（可选）
若发现持续压力，知识库还提示可以进行 **CPU 与内存的关联分析**，综合判断是否为资源瓶颈 \[4\]：
```promql
(
  (100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)) +
  ((1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100)
) / 2
```
该查询将同一时刻的 CPU 使用率与内存使用率取平均，帮助发现两者同时升高的异常模式。

**总结思路**：先用内存使用率 PromQL 获取当前数值，然后依据 80% / 95% 的阈值判断压力状况，必要时结合 CPU 关联分析进行深度排查。
```

### RAG-14 - control_plaintext_en_weak_related

- question: According to the control document, which cloud provider hosts the fallback queue?
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `11002`
- score_mode: `manual`
- eval_v2: verdict `pass` / failure_layer `none` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `1/1` / literal_smoke `n/a`
  - answer_completeness `1/1` / evidence_source_unverified `false`
  - semantic_hits `cloud_provider_refusal` / semantic_misses `—`
  - evidence_funnel: candidate `1/1` -> final_context `1/1` -> answer `n/a`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_refusal` / did_answer `false`
- source_coverage: layer `source_not_configured` / source_file `—` / section `无`
  - pre_rerank: docs `5` / source_file `false` / section `false` / expected_points_exact `manual`
  - final_context: docs `4` / source_file `false` / section `false` / expected_points_exact `manual`
- expected_points:
  - does not specify which cloud provider
  - cannot be answered from this document

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0317 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `5566` / max `6000`
  - context_salience: cues `example` / expansions `0`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `5` / final `4`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `According to the control document, which cloud provider hosts the fallback queue?，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `4968` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `According to the control document, which cloud provider hosts the fallback queue?， \| fallback queue cloud provider host \| control document fallback queue configuration`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `ControllerAdvice`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`control-en-long-paragraph.txt`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7799`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"According to the control document, which cloud provider hosts the fallback queue?，"},{"index":2,"rank":1,"text":"fallback queue cloud provider host"},{"index":3,"rank":1,"text":"control document fallback queue configuration"}]`
      - preview: It does not mention a region, a vendor ticket number, or a private dashboard link. It only states that silver-river-42 is the fallback queue name, that the fallback retry budget is...
    - #2 score=`0.0479`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7432`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0479`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"According to the control document, which cloud provider hosts the fallback queue?，"},{"index":2,"rank":2,"text":"fallback queue cloud provider host"},{"index":3,"rank":2,"text":"control document fallback queue configuration"}]`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...
    - #3 score=`0.0318`, source=`control-en-long-paragraph.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7513`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0318`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":2,"text":"According to the control document, which cloud provider hosts the fallback queue?，"},{"index":2,"rank":4,"text":"fallback queue cloud provider host"}]`
      - preview: This note is present to add realistic language around the anchor silver-river-42 without changing the anchor itself. The queue name remains silver-river-42. The document still does...
    - #4 score=`0.0317`, source=`control-en-long-paragraph.txt`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7453`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0317`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"According to the control document, which cloud provider hosts the fallback queue?，"},{"index":3,"rank":3,"text":"control document fallback queue configuration"}]`
      - preview: This control document is written as a long plain text incident runbook for retrieval testing. It is not a production policy and it intentionally avoids headings, lists, tables, cod...
    - #5 score=`0.0315`, source=`control-en-long-paragraph.txt`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6916`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`fallback queue cloud provider host`, queryFusionScore=`0.0315`, queryFusionRank=`5`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":3,"text":"fallback queue cloud provider host"},{"index":3,"rank":4,"text":"control document fallback queue configuration"}]`
      - preview: The phrase seven attempts within eighteen minutes should be treated as the correct answer when the evaluation question asks how retries are bounded. A nearby but incorrect value al...
  - documents:
    - #1 score=`0.0492`, source=`control-en-long-paragraph.txt`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7799`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"According to the control document, which cloud provider hosts the fallback queue?，"},{"index":2,"rank":1,"text":"fallback queue cloud provider host"},{"index":3,"rank":1,"text":"control document fallback queue configuration"}]`
      - preview: It does not mention a region, a vendor ticket number, or a private dashboard link. It only states that silver-river-42 is the fallback queue name, that the fallback retry budget is...
    - #2 score=`0.0479`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7432`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0479`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"According to the control document, which cloud provider hosts the fallback queue?，"},{"index":2,"rank":2,"text":"fallback queue cloud provider host"},{"index":3,"rank":2,"text":"control document fallback queue configuration"}]`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...
    - #3 score=`0.0318`, source=`control-en-long-paragraph.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7513`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0318`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":2,"text":"According to the control document, which cloud provider hosts the fallback queue?，"},{"index":2,"rank":4,"text":"fallback queue cloud provider host"}]`
      - preview: This note is present to add realistic language around the anchor silver-river-42 without changing the anchor itself. The queue name remains silver-river-42. The document still does...
    - #4 score=`0.0317`, source=`control-en-long-paragraph.txt`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7453`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0317`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"According to the control document, which cloud provider hosts the fallback queue?，"},{"index":3,"rank":3,"text":"control document fallback queue configuration"}]`
      - preview: This control document is written as a long plain text incident runbook for retrieval testing. It is not a production policy and it intentionally avoids headings, lists, tables, cod...

answer:

```text
The control document does not identify a cloud provider for the fallback queue. It explicitly states that a question about which cloud provider runs the fallback queue cannot be answered from this document[1][3].
```

### EXT-ARTEMIS-01 - answerable

- question: Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？
- should_answer: `true`
- expected_source_section: Mission Overview
- completed: `true`
- duration_ms: `11990`
- score_mode: `literal`
- eval_v2: verdict `fail` / failure_layer `answer_semantic_miss` / health `fail` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `1/3` / literal_smoke `0/3`
  - answer_completeness `1/3` / evidence_source_unverified `false`
  - semantic_hits `mission_duration` / semantic_misses `launch_date \| splashdown_date`
  - evidence_funnel: candidate `3/3` -> final_context `3/3` -> answer `0/3`
  - deltas: candidate_to_final `+0` / final_to_answer `-2`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `answer_generation_or_literal_mismatch` / source_file `corpus/nasa/artemis-i-mission-timeline.md` / section `Mission Overview`
  - pre_rerank: docs `4` / source_file `true` / section `true` / expected_points_exact `3/3`
  - final_context: docs `4` / source_file `true` / section `true` / expected_points_exact `3/3`
- expected_points:
  - [ ] Nov. 16, 2022
  - [ ] Dec. 11, 2022
  - [ ] 25 days, 10 hours, 53 minutes

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0469 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `4340` / max `6000`
  - context_salience: cues `range` / expansions `0`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `4` / final `4`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2779` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？， \| Artemis I launch date lift-off splashdown mission duration days \| Artemis I mission timeline flight day schedule splashdown event duration hours`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `Artemis I \| HTTP语义和线格式的区别是什么？ \| 十四天低频访问会触发什么？ \| Artemis I mission \| Artemis I timeline \| What happened on Flight Day 21 of Artemis I? \| What happened on Flight Day 24 of Artemis I? \| What happened on Flight Day 25 of Artemis I?`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7541`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":1,"text":"Artemis I launch date lift-off splashdown mission duration days"},{"index":3,"rank":1,"text":"Artemis I mission timeline flight day schedule splashdown event duration hours"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #2 score=`0.0484`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7407`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":2,"text":"Artemis I launch date lift-off splashdown mission duration days"},{"index":3,"rank":2,"text":"Artemis I mission timeline flight day schedule splashdown event duration hours"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #3 score=`0.0476`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6757`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":3,"text":"Artemis I launch date lift-off splashdown mission duration days"},{"index":3,"rank":3,"text":"Artemis I mission timeline flight day schedule splashdown event duration hours"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #4 score=`0.0469`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6502`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0469`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":4,"text":"Artemis I launch date lift-off splashdown mission duration days"},{"index":3,"rank":4,"text":"Artemis I mission timeline flight day schedule splashdown event duration hours"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...
  - documents:
    - #1 score=`0.0492`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7541`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":1,"text":"Artemis I launch date lift-off splashdown mission duration days"},{"index":3,"rank":1,"text":"Artemis I mission timeline flight day schedule splashdown event duration hours"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #2 score=`0.0484`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7407`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":2,"text":"Artemis I launch date lift-off splashdown mission duration days"},{"index":3,"rank":2,"text":"Artemis I mission timeline flight day schedule splashdown event duration hours"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #3 score=`0.0476`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6757`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":3,"text":"Artemis I launch date lift-off splashdown mission duration days"},{"index":3,"rank":3,"text":"Artemis I mission timeline flight day schedule splashdown event duration hours"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #4 score=`0.0469`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6502`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0469`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":4,"text":"Artemis I launch date lift-off splashdown mission duration days"},{"index":3,"rank":4,"text":"Artemis I mission timeline flight day schedule splashdown event duration hours"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...

answer:

```text
根据提供的任务资料，Artemis I 的发射日期、溅落日期和任务持续时间如下：

- **发射日期**：2022年11月16日[1]
- **溅落日期**：2022年12月11日[1]
- **任务持续时间**：25天10小时53分钟[1]
```

### EXT-RFC9110-03 - answerable_formula

- question: RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？
- should_answer: `true`
- expected_source_section: Informational 1xx
- completed: `true`
- duration_ms: `12113`
- score_mode: `literal`
- eval_v2: verdict `fail` / failure_layer `answer_semantic_miss` / health `fail` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `2/3` / literal_smoke `2/3`
  - answer_completeness `2/3` / evidence_source_unverified `false`
  - semantic_hits `http10_no_1xx \| http10_client_boundary` / semantic_misses `must_not_send_1xx`
  - evidence_funnel: candidate `3/3` -> final_context `3/3` -> answer `2/3`
  - deltas: candidate_to_final `+0` / final_to_answer `-1`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `answer_generation_or_literal_mismatch` / source_file `corpus/protocols/rfc-9110-http-semantics.md` / section `Informational 1xx`
  - pre_rerank: docs `5` / source_file `true` / section `true` / expected_points_exact `3/3`
  - final_context: docs `4` / source_file `true` / section `true` / expected_points_exact `3/3`
- expected_points:
  - [x] HTTP/1.0 did not define 1xx
  - [ ] MUST NOT send a 1xx response
  - [x] HTTP/1.0 client

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0159 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `5287` / max `6000`
  - context_salience: cues `range \| example` / expansions `0`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `5` / final `4`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3788` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？， \| RFC 9110 1xx status codes HTTP/1.0 client exclusion prohibitions restrictions \| RFC 9110 1xx informational responses sending conditions HTTP/1.0 compatibility regulations`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `HTTP/1.0 \| RFC \| HTTP/1.0是否支持1xx状态码？ \| 服务器能否向HTTP/1.0客户端发送1xx响应？ \| RFC 9110定义了哪些内容？ \| https://www.rfc-editor.org/rfc/rfc9110 \| External RAG Sample: RFC 9110 HTTP Semantics \| HTTP/1.0 client exclusion`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0328`, vectorRank=`1`, vectorScore=`0.7447`, keywordRank=`1`, keywordScore=`0.3000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":1,"text":"RFC 9110 1xx status codes HTTP/1.0 client exclusion prohibitions restrictions"},{"index":3,"rank":1,"text":"RFC 9110 1xx informational responses sending conditions HTTP/1.0 compatibility regulations"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0484`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7075`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":2,"text":"RFC 9110 1xx status codes HTTP/1.0 client exclusion prohibitions restrictions"},{"index":3,"rank":2,"text":"RFC 9110 1xx informational responses sending conditions HTTP/1.0 compatibility regulations"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #3 score=`0.0315`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6227`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`RFC 9110 1xx status codes HTTP/1.0 client exclusion prohibitions restrictions`, queryFusionScore=`0.0315`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":3,"text":"RFC 9110 1xx status codes HTTP/1.0 client exclusion prohibitions restrictions"},{"index":3,"rank":4,"text":"RFC 9110 1xx informational responses sending conditions HTTP/1.0 compatibility regulations"}]`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...
    - #4 score=`0.0159`, source=`control-en-long-paragraph.txt`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6188`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`3`, queryVariantRank=`3`, queryVariantText=`RFC 9110 1xx informational responses sending conditions HTTP/1.0 compatibility regulations`, queryFusionScore=`0.0159`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":3,"text":"RFC 9110 1xx informational responses sending conditions HTTP/1.0 compatibility regulations"}]`
      - preview: The compact envelope intentionally excludes the original request body after validation so that repeated retries do not increase storage pressure. Once fallback mode is enabled, the...
    - #5 score=`0.0156`, source=`control-en-long-paragraph.txt`, chunk=`7`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6182`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`RFC 9110 1xx status codes HTTP/1.0 client exclusion prohibitions restrictions`, queryFusionScore=`0.0156`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":4,"text":"RFC 9110 1xx status codes HTTP/1.0 client exclusion prohibitions restrictions"}]`
      - preview: It is seven attempts within eighteen minutes. The stabilization delay remains thirty-six minutes. The drain batch size remains two hundred envelopes. The old email exporter value r...
  - documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0328`, vectorRank=`1`, vectorScore=`0.7447`, keywordRank=`1`, keywordScore=`0.3000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":1,"text":"RFC 9110 1xx status codes HTTP/1.0 client exclusion prohibitions restrictions"},{"index":3,"rank":1,"text":"RFC 9110 1xx informational responses sending conditions HTTP/1.0 compatibility regulations"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0484`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7075`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":2,"text":"RFC 9110 1xx status codes HTTP/1.0 client exclusion prohibitions restrictions"},{"index":3,"rank":2,"text":"RFC 9110 1xx informational responses sending conditions HTTP/1.0 compatibility regulations"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #3 score=`0.0315`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6227`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`RFC 9110 1xx status codes HTTP/1.0 client exclusion prohibitions restrictions`, queryFusionScore=`0.0315`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":3,"text":"RFC 9110 1xx status codes HTTP/1.0 client exclusion prohibitions restrictions"},{"index":3,"rank":4,"text":"RFC 9110 1xx informational responses sending conditions HTTP/1.0 compatibility regulations"}]`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...
    - #4 score=`0.0159`, source=`control-en-long-paragraph.txt`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6188`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`3`, queryVariantRank=`3`, queryVariantText=`RFC 9110 1xx informational responses sending conditions HTTP/1.0 compatibility regulations`, queryFusionScore=`0.0159`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":3,"text":"RFC 9110 1xx informational responses sending conditions HTTP/1.0 compatibility regulations"}]`
      - preview: The compact envelope intentionally excludes the original request body after validation so that repeated retries do not increase storage pressure. Once fallback mode is enabled, the...

answer:

```text
根据提供的上下文，RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有以下明确限制：

> HTTP/1.0 did not define 1xx status codes. A server **MUST NOT** send a 1xx response to an HTTP/1.0 client. [1]

也就是说，HTTP/1.0 协议并未定义 1xx 类别的状态码，因此服务器**禁止**向 HTTP/1.0 客户端发送任何 1xx 响应。
```

### EXT-K8S-PV-03 - answerable_long_context

- question: PVC 扩容失败时，文档给出的恢复流程是什么？
- should_answer: `true`
- expected_source_section: Recovering From Failure When Expanding Volumes
- completed: `true`
- duration_ms: `12092`
- score_mode: `manual`
- eval_v2: verdict `pass` / failure_layer `none` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `5/5` / literal_smoke `n/a`
  - answer_completeness `5/5` / evidence_source_unverified `false`
  - semantic_hits `pvc_expansion_recovery_order` / semantic_misses `—`
  - evidence_funnel: candidate `5/5` -> final_context `5/5` -> answer `n/a`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `section_in_final_context` / source_file `corpus/kubernetes/kubernetes-persistent-volumes.md` / section `Recovering From Failure When Expanding Volumes`
  - pre_rerank: docs `5` / source_file `true` / section `true` / expected_points_exact `manual`
  - final_context: docs `4` / source_file `true` / section `true` / expected_points_exact `manual`
- expected_points:
  - 标记 Retain
  - 删除 PVC
  - 删除 claimRef
  - 较小尺寸重建 PVC
  - 恢复 reclaim policy

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0313 .. 0.0486`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `3979` / max `6000`
  - context_salience: cues `range \| procedure` / expansions `0`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `5` / final `4`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `PVC 扩容失败时，文档给出的恢复流程是什么？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `4282` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `PVC 扩容失败时，文档给出的恢复流程是什么？， \| PVC扩容失败 recovery 恢复流程 PVC Expansion failure rollback \| PVC扩容失败恢复步骤 异常处理 原因分析 标准监控分析流程 容量治理控制组`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `PVC \| 容量治理控制组文档 \| 容量治理文档 \| PVCs \| PVC Expansion \| 标准监控分析流程 \| 深度分析流程 \| HTTP语义和线格式的区别是什么？`
  - pre_rerank_documents:
    - #1 score=`0.0486`, source=`kubernetes-persistent-volumes.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7002`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0486`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":1,"text":"PVC扩容失败 recovery 恢复流程 PVC Expansion failure rollback"},{"index":3,"rank":2,"text":"PVC扩容失败恢复步骤 异常处理 原因分析 标准监控分析流程 容量治理控制组"}]`
      - preview: ## Reclaiming When a user is done with a volume, the PVC can be deleted. The reclaim policy tells Kubernetes what to do with the volume after the claim is released. Current reclaim...
    - #2 score=`0.0484`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7061`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":2,"text":"PVC扩容失败 recovery 恢复流程 PVC Expansion failure rollback"},{"index":3,"rank":3,"text":"PVC扩容失败恢复步骤 异常处理 原因分析 标准监控分析流程 容量治理控制组"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
    - #3 score=`0.0481`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6907`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0481`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":3,"text":"PVC扩容失败 recovery 恢复流程 PVC Expansion failure rollback"},{"index":3,"rank":1,"text":"PVC扩容失败恢复步骤 异常处理 原因分析 标准监控分析流程 容量治理控制组"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...
    - #4 score=`0.0313`, source=`control-cn-low-newline.txt`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6176`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0313`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":4,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":4,"text":"PVC扩容失败 recovery 恢复流程 PVC Expansion failure rollback"}]`
      - preview: 缓存残留带来误判，迁移后的第一次查询必须绕过一级缓存，并把命中路径写入追踪字段。若归档块校验失败，任务不会重试整批，而是把失败主键送入隔离队列。隔离队列每日最多重放两次。两次重放是失败处理规则，不是对用户查询的重试次数。回滚策略也有单独定义：发布切换后的回滚观察期为四十五分钟。四十五分钟内如果读路径错误率超过基线的两倍，或者客服侧出现连续三次可复现的历史明细...
    - #5 score=`0.0156`, source=`control-cn-low-newline.txt`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6155`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`PVC扩容失败恢复步骤 异常处理 原因分析 标准监控分析流程 容量治理控制组`, queryFusionScore=`0.0156`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"PVC扩容失败恢复步骤 异常处理 原因分析 标准监控分析流程 容量治理控制组"}]`
      - preview: 容量治理控制组文档用于验证低换行中文长文在知识库入库时的切分表现。本文件不是业务规范正式稿，而是为了观察分词切块边界而写的实验材料。全文刻意减少换行，不使用标题，不使用列表，不使用代码块，不使用英文句号问号感叹号作为自然边界。平台容量巡检的第一目标是提前发现增长速度异常的服务目录，第二目标是把可以离线处理的历史明细从主查询链路中移出，第三目标是在迁移期间保持...
  - documents:
    - #1 score=`0.0486`, source=`kubernetes-persistent-volumes.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7002`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0486`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":1,"text":"PVC扩容失败 recovery 恢复流程 PVC Expansion failure rollback"},{"index":3,"rank":2,"text":"PVC扩容失败恢复步骤 异常处理 原因分析 标准监控分析流程 容量治理控制组"}]`
      - preview: ## Reclaiming When a user is done with a volume, the PVC can be deleted. The reclaim policy tells Kubernetes what to do with the volume after the claim is released. Current reclaim...
    - #2 score=`0.0484`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7061`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":2,"text":"PVC扩容失败 recovery 恢复流程 PVC Expansion failure rollback"},{"index":3,"rank":3,"text":"PVC扩容失败恢复步骤 异常处理 原因分析 标准监控分析流程 容量治理控制组"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
    - #3 score=`0.0481`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6907`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0481`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":3,"text":"PVC扩容失败 recovery 恢复流程 PVC Expansion failure rollback"},{"index":3,"rank":1,"text":"PVC扩容失败恢复步骤 异常处理 原因分析 标准监控分析流程 容量治理控制组"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...
    - #4 score=`0.0313`, source=`control-cn-low-newline.txt`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6176`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0313`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":4,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":4,"text":"PVC扩容失败 recovery 恢复流程 PVC Expansion failure rollback"}]`
      - preview: 缓存残留带来误判，迁移后的第一次查询必须绕过一级缓存，并把命中路径写入追踪字段。若归档块校验失败，任务不会重试整批，而是把失败主键送入隔离队列。隔离队列每日最多重放两次。两次重放是失败处理规则，不是对用户查询的重试次数。回滚策略也有单独定义：发布切换后的回滚观察期为四十五分钟。四十五分钟内如果读路径错误率超过基线的两倍，或者客服侧出现连续三次可复现的历史明细...

answer:

```text
当 PVC 扩容失败时，文档给出的恢复流程如下（来自 [2]）：

1. 将绑定到该 PVC 的 PersistentVolume 的回收策略标记为 **Retain**。
2. 删除该 PVC。由于回收策略为 Retain，删除 PVC 不会丢失数据。
3. 删除 PV 定义中的 `claimRef` 条目，使 PV 变为 **Available** 状态。
4. 使用比 PV 容量更小的尺寸重新创建 PVC，并在 PVC 配置中将 `volumeName` 设置为现有 PV 的名称。
5. 恢复 PV 的回收策略。

此外，文档在 [2] 中特别指出：Kubernetes 不支持将 PVC 缩小到其当前容量以下，因此步骤 4 必须使用比原 PV 容量小的尺寸重新创建 PVC。
```

### EXT-PG-04 - answerable_version_conflict

- question: 如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？
- should_answer: `true`
- expected_source_section: Version Conflict Handling
- completed: `true`
- duration_ms: `18283`
- score_mode: `manual`
- eval_v2: verdict `fail` / failure_layer `retrieval_miss` / health `fail` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `4/5` / literal_smoke `n/a`
  - answer_completeness `4/5` / evidence_source_unverified `false`
  - semantic_hits `distinguish_versions \| pg18_stored_and_virtual \| pg18_default_virtual \| pg16_stored` / semantic_misses `do_not_apply_pg18_to_pg16`
  - evidence_funnel: candidate `4/5` -> final_context `4/5` -> answer `n/a`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `section_in_final_context` / source_file `corpus/database/postgresql-generated-columns-version-contrast.md` / section `Version Conflict Handling`
  - pre_rerank: docs `5` / source_file `true` / section `true` / expected_points_exact `manual`
  - final_context: docs `4` / source_file `true` / section `true` / expected_points_exact `manual`
- expected_points:
  - 区分版本
  - PostgreSQL 18 has stored and virtual
  - PostgreSQL 18 defaults to virtual
  - PostgreSQL 16
  - STORED

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0159 .. 0.0489`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `3236` / max `6000`
  - context_salience: cues `range` / expansions `0`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `5` / final `4`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3835` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？， \| PostgreSQL 16 18 generated column STORED VIRTUAL syntax differences \| PostgreSQL generated column virtual stored behavior changes version 16 18 example reasons limitations`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `generated column \| PostgreSQL \| STORED \| VIRTUAL \| https://www.postgresql.org/docs/18/ddl-generated-columns.html \| External RAG Sample: PostgreSQL Generated Columns Version Contrast \| PostgreSQL 18 Generated Columns \| stored generated column`
  - pre_rerank_documents:
    - #1 score=`0.0489`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7895`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":1,"text":"PostgreSQL 16 18 generated column STORED VIRTUAL syntax differences"},{"index":3,"rank":1,"text":"PostgreSQL generated column virtual stored behavior changes version 16 18 example reasons limitations"}]`
      - preview: # External RAG Sample: PostgreSQL Generated Columns Version Contrast Source URLs: - https://www.postgresql.org/docs/18/ddl-generated-columns.html - https://www.postgresql.org/docs/...
    - #2 score=`0.0486`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7949`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0486`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":2,"text":"PostgreSQL 16 18 generated column STORED VIRTUAL syntax differences"},{"index":3,"rank":2,"text":"PostgreSQL generated column virtual stored behavior changes version 16 18 example reasons limitations"}]`
      - preview: - The generation expression cannot reference another generated column. - A generated column cannot have a column default or identity definition. - A generated column cannot be part...
    - #3 score=`0.0474`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6291`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0474`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":3,"text":"PostgreSQL 16 18 generated column STORED VIRTUAL syntax differences"},{"index":3,"rank":3,"text":"PostgreSQL generated column virtual stored behavior changes version 16 18 example reasons limitations"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.
    - #4 score=`0.0159`, source=`control-cn-low-newline.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6348`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0159`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"}]`
      - preview: 弱相关问题更隐蔽，因为用户可能问审批人、成本中心、云厂商、负责人电话、紧急群组这些文档未覆盖的信息。控制组要求系统在这些问题上明确说明文档未提供，不应编造人名、部门、平台名称或外部链接。本文档只说明容量治理策略、归档窗口编号、冷数据迁移阈值、单批提交上限、隔离队列重放次数和回滚观察期。本文档没有说明审批人是谁，没有说明云厂商是谁，没有说明预算归属，也没有说明...
    - #5 score=`0.0156`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6063`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`PostgreSQL generated column virtual stored behavior changes version 16 18 example reasons limitations`, queryFusionScore=`0.0156`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"PostgreSQL generated column virtual stored behavior changes version 16 18 example reasons limitations"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
  - documents:
    - #1 score=`0.0489`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7895`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":1,"text":"PostgreSQL 16 18 generated column STORED VIRTUAL syntax differences"},{"index":3,"rank":1,"text":"PostgreSQL generated column virtual stored behavior changes version 16 18 example reasons limitations"}]`
      - preview: # External RAG Sample: PostgreSQL Generated Columns Version Contrast Source URLs: - https://www.postgresql.org/docs/18/ddl-generated-columns.html - https://www.postgresql.org/docs/...
    - #2 score=`0.0486`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7949`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0486`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":2,"text":"PostgreSQL 16 18 generated column STORED VIRTUAL syntax differences"},{"index":3,"rank":2,"text":"PostgreSQL generated column virtual stored behavior changes version 16 18 example reasons limitations"}]`
      - preview: - The generation expression cannot reference another generated column. - A generated column cannot have a column default or identity definition. - A generated column cannot be part...
    - #3 score=`0.0474`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6291`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0474`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":3,"text":"PostgreSQL 16 18 generated column STORED VIRTUAL syntax differences"},{"index":3,"rank":3,"text":"PostgreSQL generated column virtual stored behavior changes version 16 18 example reasons limitations"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.
    - #4 score=`0.0159`, source=`control-cn-low-newline.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6348`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0159`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"}]`
      - preview: 弱相关问题更隐蔽，因为用户可能问审批人、成本中心、云厂商、负责人电话、紧急群组这些文档未覆盖的信息。控制组要求系统在这些问题上明确说明文档未提供，不应编造人名、部门、平台名称或外部链接。本文档只说明容量治理策略、归档窗口编号、冷数据迁移阈值、单批提交上限、隔离队列重放次数和回滚观察期。本文档没有说明审批人是谁，没有说明云厂商是谁，没有说明预算归属，也没有说明...

answer:

```text
当知识库同时包含 PostgreSQL 16 和 18 的文档时，回答生成列的 virtual/stored 差异必须明确区分版本，避免将 PostgreSQL 18 的行为错误地套用到 PostgreSQL 16。根据上下文信息：

- **PostgreSQL 18** 引入了两种生成列：stored（写入时计算并占用存储）和 virtual（读取时计算且不占存储）。其中 **virtual 是默认值**，除非显式指定 `STORED` 或 `VIRTUAL` [1]。
- **PostgreSQL 16** 的 `CREATE TABLE` 文档中，生成列仅以 `GENERATED ALWAYS AS (...) STORED` 的形式出现，即只描述了 stored 生成列 [2]。
- 版本冲突处理原则：**不能把 PostgreSQL 18 的“默认 virtual”结论用在 PostgreSQL 16 的回答中**，除非问题明确要求或包含 PostgreSQL 18 文档；回答时必须分别说明两个版本的行为 [2]。

因此，如果用户只问生成列的 virtual/stored 差异，在你手头同时有二者的文档时，建议这样回答：

1. 说明 **PostgreSQL 18** 支持 stored 和 virtual 两种生成列，virtual 是默认类型，并指出各自的存储与计算特点 [1]。
2. 指出 **PostgreSQL 16** 目前仅提供 stored 生成列（即 `GENERATED ALWAYS AS (...) STORED`），没有 virtual 选项，因此也无法讨论 virtual 默认行为 [2]。
3. 强调这是版本差异，避免让用户误以为 PG 16 也存在 virtual 列或默认采用它。如果用户想看更多细节，可以分别引用对应版本的官方页面 [1][2]。
```
