# RAG Eval Result

- generated_at: `2026-05-25 16:29:15`
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
| RAG-10 | paraphrase | true | 9501 | fail | retrieval_miss | 2/5 | 2/5 | +0 | +0 | 2/5 | 2/5 | 2/5 | true_answer | source_not_configured; file final=false; section final=false; points final=2/5 | 根据知识库中的信息，查询内存压力的核心思路是监控**内存使用率**，具体查询如下： - 查询内存使用率（百分比）： `(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_b... |
| RAG-14 | control_plaintext_en_weak_related | true | 5477 | ok | none | 1/1 | 1/1 | +0 | +0 | 1/1 | 1/1 | n/a | true_refusal | source_not_configured; file final=false; section final=false; points final=manual | The control document does not mention which cloud provider hosts the fallback queue. It states explicitly that this fact... |
| EXT-ARTEMIS-01 | answerable | true | 5007 | fail | answer_semantic_miss | 3/3 | 3/3 | +0 | -3 | 0/3 | 0/3 | 0/3 | true_answer | answer_generation_or_literal_mismatch; file final=true; section final=true; points final=3/3 | 根据 NASA 提供的任务信息 [1]： - **发射日期**：2022 年 11 月 16 日 - **溅落日期**：2022 年 12 月 11 日 - **任务持续时间**：25 天 10 小时 53 分钟 |
| EXT-RFC9110-03 | answerable_formula | true | 6752 | fail | answer_semantic_miss | 3/3 | 3/3 | +0 | -2 | 1/3 | 1/3 | 0/3 | true_answer | answer_generation_or_literal_mismatch; file final=true; section final=true; points final=3/3 | 根据 RFC 9110 的规定，由于 HTTP/1.0 最初并未定义 1xx 信息状态码，**服务器不得（MUST NOT）向 HTTP/1.0 客户端发送 1xx 响应**[1]。 |
| EXT-K8S-PV-03 | answerable_long_context | true | 7290 | ok | none | 5/5 | 5/5 | +0 | +0 | 5/5 | 5/5 | n/a | true_answer | section_in_final_context; file final=true; section final=true; points final=manual | 根据文档，当 PVC 扩容失败时，集群管理员可以按照以下流程手动恢复[1]： 1. 将绑定到 PVC 的 PersistentVolume（PV）的回收策略（reclaim policy）标记为 `Retain`。 2. 删除 PVC。由于... |
| EXT-PG-04 | answerable_version_conflict | true | 13352 | fail | retrieval_miss | 4/5 | 4/5 | +0 | -1 | 3/5 | 3/5 | n/a | true_answer | section_in_final_context; file final=true; section final=true; points final=manual | 当知识库中同时包含 PostgreSQL 16 和 18 的文档时，回答 generated column 中 virtual/stored 的差异必须**显式区分版本**，不能将 18 的默认行为（virtual）错误套用到 16 上。 ... |


## Eval v2 By Case Type

| case_type | cases | runtime_ok | candidate_recall | final_context_recall | semantic_avg | answer_completeness_avg | literal_smoke | failure_layers |
|---|---:|---:|---|---|---:|---:|---|---|
| answerable | 1 | 1/1 | 3/3 | 3/3 | 0.0% | 0.0% | 0/3 | answer_semantic_miss:1 |
| answerable_formula | 1 | 1/1 | 3/3 | 3/3 | 33.3% | 33.3% | 0/3 | answer_semantic_miss:1 |
| answerable_long_context | 1 | 1/1 | n/a | n/a | 100.0% | 100.0% | n/a | none:1 |
| answerable_version_conflict | 1 | 1/1 | n/a | n/a | 60.0% | 60.0% | n/a | retrieval_miss:1 |
| control_plaintext_en_weak_related | 1 | 1/1 | n/a | n/a | 100.0% | 100.0% | n/a | none:1 |
| paraphrase | 1 | 1/1 | 2/5 | 2/5 | 40.0% | 40.0% | 2/5 | retrieval_miss:1 |

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
- duration_ms: `9501`
- score_mode: `literal`
- eval_v2: verdict `fail` / failure_layer `retrieval_miss` / health `fail` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `2/5` / literal_smoke `2/5`
  - answer_completeness `2/5` / evidence_source_unverified `false`
  - semantic_hits `memory_formula_available \| memory_formula_total` / semantic_misses `normal_range \| warning_range \| danger_range`
  - evidence_funnel: candidate `2/5` -> final_context `2/5` -> answer `2/5`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `source_not_configured` / source_file `—` / section `内存使用率查询 / 内存数据解释`
  - pre_rerank: docs `4` / source_file `false` / section `false` / expected_points_exact `2/5`
  - final_context: docs `4` / source_file `false` / section `false` / expected_points_exact `2/5`
- expected_points:
  - [x] node_memory_MemAvailable_bytes
  - [x] node_memory_MemTotal_bytes
  - [ ] 正常范围
  - [ ] 警告范围
  - [ ] 危险范围

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2211` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `4` / final `4`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `如果我要查内存压力，知识库给出的查询思路是什么？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `PASSTHROUGH` / mode `PASSTHROUGH` / variants `1` / elapsed_ms `0` / failure `—` / attempts `PASSTHROUGH:PASSTHROUGH` / texts `如果我要查内存压力，知识库给出的查询思路是什么？，`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #2 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"}]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #3 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"}]`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...
    - #4 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
  - documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #2 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"}]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #3 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"}]`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...
    - #4 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...

answer:

```text
根据知识库中的信息，查询内存压力的核心思路是监控**内存使用率**，具体查询如下：

- 查询内存使用率（百分比）：  
  `(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100` [1]  
  该指标通过可用内存与总内存的比例计算已用内存占比，直观反映内存压力状况。

- 查询已用内存量（GB）：  
  `(node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024` [4]  
  这个查询可以帮助评估实际被占用的内存大小，辅助判断内存压力程度。

在实际使用中，可以结合这些查询设置合理的时间范围，关注趋势变化而非单一瞬时值，从而更准确地评估内存压力。
```

### RAG-14 - control_plaintext_en_weak_related

- question: According to the control document, which cloud provider hosts the fallback queue?
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `5477`
- score_mode: `manual`
- eval_v2: verdict `pass` / failure_layer `none` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `1/1` / literal_smoke `n/a`
  - answer_completeness `1/1` / evidence_source_unverified `false`
  - semantic_hits `cloud_provider_refusal` / semantic_misses `—`
  - evidence_funnel: candidate `1/1` -> final_context `1/1` -> answer `n/a`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_refusal` / did_answer `false`
- source_coverage: layer `source_not_configured` / source_file `—` / section `无`
  - pre_rerank: docs `4` / source_file `false` / section `false` / expected_points_exact `manual`
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
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `5550` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `4` / final `4`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `According to the control document, which cloud provider hosts the fallback queue?，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `PASSTHROUGH` / mode `PASSTHROUGH` / variants `1` / elapsed_ms `0` / failure `—` / attempts `PASSTHROUGH:PASSTHROUGH` / texts `According to the control document, which cloud provider hosts the fallback queue?，`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`control-en-long-paragraph.txt`, chunk=`4`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"According to the control document, which cloud provider hosts the fallback queue?，"}]`
      - preview: It does not mention a region, a vendor ticket number, or a private dashboard link. It only states that silver-river-42 is the fallback queue name, that the fallback retry budget is...
    - #2 score=`0.0161`, source=`control-en-long-paragraph.txt`, chunk=`6`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"According to the control document, which cloud provider hosts the fallback queue?，"}]`
      - preview: This note is present to add realistic language around the anchor silver-river-42 without changing the anchor itself. The queue name remains silver-river-42. The document still does...
    - #3 score=`0.0159`, source=`control-en-long-paragraph.txt`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"According to the control document, which cloud provider hosts the fallback queue?，"}]`
      - preview: This control document is written as a long plain text incident runbook for retrieval testing. It is not a production policy and it intentionally avoids headings, lists, tables, cod...
    - #4 score=`0.0156`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"According to the control document, which cloud provider hosts the fallback queue?，"}]`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...
  - documents:
    - #1 score=`0.0164`, source=`control-en-long-paragraph.txt`, chunk=`4`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"According to the control document, which cloud provider hosts the fallback queue?，"}]`
      - preview: It does not mention a region, a vendor ticket number, or a private dashboard link. It only states that silver-river-42 is the fallback queue name, that the fallback retry budget is...
    - #2 score=`0.0161`, source=`control-en-long-paragraph.txt`, chunk=`6`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"According to the control document, which cloud provider hosts the fallback queue?，"}]`
      - preview: This note is present to add realistic language around the anchor silver-river-42 without changing the anchor itself. The queue name remains silver-river-42. The document still does...
    - #3 score=`0.0159`, source=`control-en-long-paragraph.txt`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"According to the control document, which cloud provider hosts the fallback queue?，"}]`
      - preview: This control document is written as a long plain text incident runbook for retrieval testing. It is not a production policy and it intentionally avoids headings, lists, tables, cod...
    - #4 score=`0.0156`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"According to the control document, which cloud provider hosts the fallback queue?，"}]`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...

answer:

```text
The control document does not mention which cloud provider hosts the fallback queue. It states explicitly that this fact is not included, and a question about it cannot be answered from the document [1][2][3].
```

### EXT-ARTEMIS-01 - answerable

- question: Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？
- should_answer: `true`
- expected_source_section: Mission Overview
- completed: `true`
- duration_ms: `5007`
- score_mode: `literal`
- eval_v2: verdict `fail` / failure_layer `answer_semantic_miss` / health `fail` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `0/3` / literal_smoke `0/3`
  - answer_completeness `0/3` / evidence_source_unverified `false`
  - semantic_hits `—` / semantic_misses `launch_date \| splashdown_date \| mission_duration`
  - evidence_funnel: candidate `3/3` -> final_context `3/3` -> answer `0/3`
  - deltas: candidate_to_final `+0` / final_to_answer `-3`
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
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `4326` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `4` / final `4`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `PASSTHROUGH` / mode `PASSTHROUGH` / variants `1` / elapsed_ms `0` / failure `—` / attempts `PASSTHROUGH:PASSTHROUGH` / texts `Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #2 score=`0.0161`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #3 score=`0.0159`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #4 score=`0.0156`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...
  - documents:
    - #1 score=`0.0164`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #2 score=`0.0161`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #3 score=`0.0159`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #4 score=`0.0156`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...

answer:

```text
根据 NASA 提供的任务信息 [1]：

- **发射日期**：2022 年 11 月 16 日  
- **溅落日期**：2022 年 12 月 11 日  
- **任务持续时间**：25 天 10 小时 53 分钟
```

### EXT-RFC9110-03 - answerable_formula

- question: RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？
- should_answer: `true`
- expected_source_section: Informational 1xx
- completed: `true`
- duration_ms: `6752`
- score_mode: `literal`
- eval_v2: verdict `fail` / failure_layer `answer_semantic_miss` / health `fail` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `1/3` / literal_smoke `0/3`
  - answer_completeness `1/3` / evidence_source_unverified `false`
  - semantic_hits `must_not_send_1xx` / semantic_misses `http10_no_1xx \| http10_client_boundary`
  - evidence_funnel: candidate `3/3` -> final_context `3/3` -> answer `0/3`
  - deltas: candidate_to_final `+0` / final_to_answer `-2`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `answer_generation_or_literal_mismatch` / source_file `corpus/protocols/rfc-9110-http-semantics.md` / section `Informational 1xx`
  - pre_rerank: docs `2` / source_file `true` / section `true` / expected_points_exact `3/3`
  - final_context: docs `2` / source_file `true` / section `true` / expected_points_exact `3/3`
- expected_points:
  - [ ] HTTP/1.0 did not define 1xx
  - [ ] MUST NOT send a 1xx response
  - [ ] HTTP/1.0 client

retrieval:
- event #1
  - retrieved_document_count: `2`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0161 .. 0.0164`
  - context_selected: `2` / dropped: `0` / truncated: `false`
  - context_chars: actual `2520` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `2` / final `2`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `PASSTHROUGH` / mode `PASSTHROUGH` / variants `1` / elapsed_ms `0` / failure `—` / attempts `PASSTHROUGH:PASSTHROUGH` / texts `RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0161`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
  - documents:
    - #1 score=`0.0164`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0161`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...

answer:

```text
根据 RFC 9110 的规定，由于 HTTP/1.0 最初并未定义 1xx 信息状态码，**服务器不得（MUST NOT）向 HTTP/1.0 客户端发送 1xx 响应**[1]。
```

### EXT-K8S-PV-03 - answerable_long_context

- question: PVC 扩容失败时，文档给出的恢复流程是什么？
- should_answer: `true`
- expected_source_section: Recovering From Failure When Expanding Volumes
- completed: `true`
- duration_ms: `7290`
- score_mode: `manual`
- eval_v2: verdict `pass` / failure_layer `none` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `5/5` / literal_smoke `n/a`
  - answer_completeness `5/5` / evidence_source_unverified `false`
  - semantic_hits `pvc_expansion_recovery_order` / semantic_misses `—`
  - evidence_funnel: candidate `5/5` -> final_context `5/5` -> answer `n/a`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `section_in_final_context` / source_file `corpus/kubernetes/kubernetes-persistent-volumes.md` / section `Recovering From Failure When Expanding Volumes`
  - pre_rerank: docs `4` / source_file `true` / section `true` / expected_points_exact `manual`
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
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `3947` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `4` / final `4`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `PVC 扩容失败时，文档给出的恢复流程是什么？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `PASSTHROUGH` / mode `PASSTHROUGH` / variants `1` / elapsed_ms `0` / failure `—` / attempts `PASSTHROUGH:PASSTHROUGH` / texts `PVC 扩容失败时，文档给出的恢复流程是什么？，`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
    - #2 score=`0.0161`, source=`kubernetes-persistent-volumes.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"}]`
      - preview: ## Reclaiming When a user is done with a volume, the PVC can be deleted. The reclaim policy tells Kubernetes what to do with the volume after the claim is released. Current reclaim...
    - #3 score=`0.0159`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...
    - #4 score=`0.0156`, source=`control-cn-low-newline.txt`, chunk=`4`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"}]`
      - preview: 缓存残留带来误判，迁移后的第一次查询必须绕过一级缓存，并把命中路径写入追踪字段。若归档块校验失败，任务不会重试整批，而是把失败主键送入隔离队列。隔离队列每日最多重放两次。两次重放是失败处理规则，不是对用户查询的重试次数。回滚策略也有单独定义：发布切换后的回滚观察期为四十五分钟。四十五分钟内如果读路径错误率超过基线的两倍，或者客服侧出现连续三次可复现的历史明细...
  - documents:
    - #1 score=`0.0164`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
    - #2 score=`0.0161`, source=`kubernetes-persistent-volumes.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"}]`
      - preview: ## Reclaiming When a user is done with a volume, the PVC can be deleted. The reclaim policy tells Kubernetes what to do with the volume after the claim is released. Current reclaim...
    - #3 score=`0.0159`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...
    - #4 score=`0.0156`, source=`control-cn-low-newline.txt`, chunk=`4`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"}]`
      - preview: 缓存残留带来误判，迁移后的第一次查询必须绕过一级缓存，并把命中路径写入追踪字段。若归档块校验失败，任务不会重试整批，而是把失败主键送入隔离队列。隔离队列每日最多重放两次。两次重放是失败处理规则，不是对用户查询的重试次数。回滚策略也有单独定义：发布切换后的回滚观察期为四十五分钟。四十五分钟内如果读路径错误率超过基线的两倍，或者客服侧出现连续三次可复现的历史明细...

answer:

```text
根据文档，当 PVC 扩容失败时，集群管理员可以按照以下流程手动恢复[1]：

1.  将绑定到 PVC 的 PersistentVolume（PV）的回收策略（reclaim policy）标记为 `Retain`。
2.  删除 PVC。由于回收策略为 `Retain`，删除 PVC 不会导致数据丢失。
3.  删除 PV 规格（spec）中的 `claimRef` 条目，使 PV 变为 `Available` 状态。
4.  重新创建一个大小比 PV 小的 PVC，并在 PVC 中通过 `volumeName` 字段指定到该现有 PV 的名称。
5.  恢复 PV 的回收策略。

请注意，Kubernetes 不支持将 PVC 缩小到比当前大小更小的尺寸。
```

### EXT-PG-04 - answerable_version_conflict

- question: 如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？
- should_answer: `true`
- expected_source_section: Version Conflict Handling
- completed: `true`
- duration_ms: `13352`
- score_mode: `manual`
- eval_v2: verdict `fail` / failure_layer `retrieval_miss` / health `fail` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `3/5` / literal_smoke `n/a`
  - answer_completeness `3/5` / evidence_source_unverified `false`
  - semantic_hits `distinguish_versions \| pg18_default_virtual \| pg16_stored` / semantic_misses `pg18_stored_and_virtual \| do_not_apply_pg18_to_pg16`
  - evidence_funnel: candidate `4/5` -> final_context `4/5` -> answer `n/a`
  - deltas: candidate_to_final `+0` / final_to_answer `-1`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `section_in_final_context` / source_file `corpus/database/postgresql-generated-columns-version-contrast.md` / section `Version Conflict Handling`
  - pre_rerank: docs `4` / source_file `true` / section `true` / expected_points_exact `manual`
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
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `3208` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `4` / final `4`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `PASSTHROUGH` / mode `PASSTHROUGH` / variants `1` / elapsed_ms `0` / failure `—` / attempts `PASSTHROUGH:PASSTHROUGH` / texts `如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"}]`
      - preview: - The generation expression cannot reference another generated column. - A generated column cannot have a column default or identity definition. - A generated column cannot be part...
    - #2 score=`0.0161`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"}]`
      - preview: # External RAG Sample: PostgreSQL Generated Columns Version Contrast Source URLs: - https://www.postgresql.org/docs/18/ddl-generated-columns.html - https://www.postgresql.org/docs/...
    - #3 score=`0.0159`, source=`control-cn-low-newline.txt`, chunk=`6`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"}]`
      - preview: 弱相关问题更隐蔽，因为用户可能问审批人、成本中心、云厂商、负责人电话、紧急群组这些文档未覆盖的信息。控制组要求系统在这些问题上明确说明文档未提供，不应编造人名、部门、平台名称或外部链接。本文档只说明容量治理策略、归档窗口编号、冷数据迁移阈值、单批提交上限、隔离队列重放次数和回滚观察期。本文档没有说明审批人是谁，没有说明云厂商是谁，没有说明预算归属，也没有说明...
    - #4 score=`0.0156`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.
  - documents:
    - #1 score=`0.0164`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"}]`
      - preview: - The generation expression cannot reference another generated column. - A generated column cannot have a column default or identity definition. - A generated column cannot be part...
    - #2 score=`0.0161`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"}]`
      - preview: # External RAG Sample: PostgreSQL Generated Columns Version Contrast Source URLs: - https://www.postgresql.org/docs/18/ddl-generated-columns.html - https://www.postgresql.org/docs/...
    - #3 score=`0.0159`, source=`control-cn-low-newline.txt`, chunk=`6`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"}]`
      - preview: 弱相关问题更隐蔽，因为用户可能问审批人、成本中心、云厂商、负责人电话、紧急群组这些文档未覆盖的信息。控制组要求系统在这些问题上明确说明文档未提供，不应编造人名、部门、平台名称或外部链接。本文档只说明容量治理策略、归档窗口编号、冷数据迁移阈值、单批提交上限、隔离队列重放次数和回滚观察期。本文档没有说明审批人是谁，没有说明云厂商是谁，没有说明预算归属，也没有说明...
    - #4 score=`0.0156`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.

answer:

```text
当知识库中同时包含 PostgreSQL 16 和 18 的文档时，回答 generated column 中 virtual/stored 的差异必须**显式区分版本**，不能将 18 的默认行为（virtual）错误套用到 16 上。  

具体做法如下：

1. **先说明 PostgreSQL 18 的行为**  
   - 18 支持两种生成的列：`stored`（写入时计算并占用存储）和 `virtual`（读取时计算，不占存储）[2]  
   - 18 中若不指定 `VIRTUAL` 或 `STORED`，默认是 `virtual`[2]  
   - 需要提及虚拟列对用户定义类型和函数的额外限制[1]

2. **再明确 PostgreSQL 16 的不同**  
   - 16 的 `CREATE TABLE` 文档中生成的列语法固定为 `GENERATED ALWAYS AS (...) STORED`，只支持 stored 类型[1]  
   - 16 中不存在 virtual 生成的列，因此不存在默认 virtual 的概念  
   - 回答时绝不能将 18 的“默认 virtual”结论应用到 16 上[1]

3. **给出版本对比结论**  
   例如：“如果你的环境是 PostgreSQL 16，生成列只能是 stored 类型；而 PostgreSQL 18 新增了 virtual 类型，并且在不指定类型时默认为 virtual。”

这种回答方式遵循了 [1] 中的 **Version Conflict Handling** 原则，确保用户能根据所用版本正确理解差异，不会受到跨版本默认行为的误导。
```
