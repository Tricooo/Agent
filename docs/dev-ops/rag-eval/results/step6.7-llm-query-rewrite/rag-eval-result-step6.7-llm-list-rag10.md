# RAG Eval Result

- generated_at: `2026-05-21 17:15:10`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。
>
> `retrieved` / `score` / `empty` 三列的 `—` 表示**没拿到成功的 ChatResponse metadata**，不等价于"无检索"。当前实现把 retrieval SSE 帧放在 `.call().chatResponse()` 返回之后才发，所以 LLM 调用失败时（即使 RAG 检索本身成功）三列都会是 `—`。要区分"检索失败"和"生成失败"，对照 `error` 列 / details 区 / backend log。
>
> Details 区的 `pre_rerank_documents` 展开 rerank 前候选池，`documents` 展开最终 top-K chunk attribution；document 行的 `score` 是当前阶段写回的候选分，可能来自 HYBRID RRF、query-variant fusion、per-variant rerank RRF 或 fusion-aware rerank；原始向量分与关键词分分别看 `vectorScore` / `keywordScore`，多 query 召回融合看 `queryFusionScore/queryFusionRank/queryVariantHitCount`；最终 rerank 写回分看 `rerankScore`，多路 rerank 融合分看 `rerankFusionScore/rerankVariantHitCount`，fusion-aware 弱加成看 `queryFusionBoostScore/fusionAwareScore`；rerank 服务状态看 `rerank_runtime` 的 model / endpoint / failure_reason；keyword 分支未参与时看 `keywordSkippedReason`。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-10 | paraphrase | true | 21192 | true | 4 | 0.0476 .. 0.0489 | false | 3/5 | 正常范围, 警告范围 | 根据知识库，查询内存压力的思路是使用以下 PromQL 查询来计算内存使用率： ```promql (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * ... |  |

## Details

### RAG-10 - paraphrase

- question: 如果我要查内存压力，知识库给出的查询思路是什么？
- should_answer: `true`
- expected_source_section: 内存使用率查询 / 内存数据解释
- completed: `true`
- duration_ms: `21192`
- score_mode: `literal`
- expected_points:
  - [x] node_memory_MemAvailable_bytes
  - [x] node_memory_MemTotal_bytes
  - [ ] 正常范围
  - [ ] 警告范围
  - [x] 危险范围

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0476 .. 0.0489`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2255` / max `6000`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `6` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `如果我要查内存压力，知识库给出的查询思路是什么？，<br>内存压力高时的排查方法有哪些<br>知识库中关于内存压力指标和解决思路的文档`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2567` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `如果我要查内存压力，知识库给出的查询思路是什么？， \| 内存压力高时的排查方法有哪些 \| 知识库中关于内存压力指标和解决思路的文档`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6819`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"},{"index":2,"rank":1,"text":"内存压力高时的排查方法有哪些"},{"index":3,"rank":1,"text":"知识库中关于内存压力指标和解决思路的文档"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #2 score=`0.0476`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6684`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0476`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"},{"index":2,"rank":3,"text":"内存压力高时的排查方法有哪些"},{"index":3,"rank":2,"text":"知识库中关于内存压力指标和解决思路的文档"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #3 score=`0.0323`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6741`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0323`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"},{"index":2,"rank":2,"text":"内存压力高时的排查方法有哪些"}]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #4 score=`0.0315`, source=`grafana-mcp-tools-guide.md`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6170`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`内存压力高时的排查方法有哪些`, queryFusionScore=`0.0315`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":4,"text":"内存压力高时的排查方法有哪些"},{"index":3,"rank":3,"text":"知识库中关于内存压力指标和解决思路的文档"}]`
      - preview: - **危险范围**: 95-100% ### 网络数据解释 - **单位**: bps (bits per second) - **正常范围**: 根据网络带宽确定 - **异常指标**: 突然的流量峰值或持续的高流量 ## 报告生成模板 ### 系统健康报告模板 ```markdown ## 系统运行状态分析报告 ### 📊 监控数据概览 - **监控时...
    - #5 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6692`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"}]`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...
    - #6 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`7`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6198`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`6`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`知识库中关于内存压力指标和解决思路的文档`, queryFusionScore=`0.0156`, queryFusionRank=`6`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"知识库中关于内存压力指标和解决思路的文档"}]`
      - preview: - **当前使用率**: {memory_usage}% - **可用内存**: {memory_available}GB - **状态评估**: {memory_status} - **使用趋势**: {memory_trend} ### 💿 磁盘使用情况 - **使用率**: {disk_usage}% - **可用空间**: {disk_availab...
  - documents:
    - #1 score=`0.0489`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6819`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0489`, rerankFusionScore=`0.0489`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"},{"index":2,"rank":1,"text":"内存压力高时的排查方法有哪些"},{"index":3,"rank":1,"text":"知识库中关于内存压力指标和解决思路的文档"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #2 score=`0.0479`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6741`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`0.0479`, rerankFusionScore=`0.0479`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":2},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0323`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"},{"index":2,"rank":2,"text":"内存压力高时的排查方法有哪些"}]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #3 score=`0.0476`, source=`grafana-mcp-tools-guide.md`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6170`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":1}]`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`内存压力高时的排查方法有哪些`, queryFusionScore=`0.0315`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":4,"text":"内存压力高时的排查方法有哪些"},{"index":3,"rank":3,"text":"知识库中关于内存压力指标和解决思路的文档"}]`
      - preview: - **危险范围**: 95-100% ### 网络数据解释 - **单位**: bps (bits per second) - **正常范围**: 根据网络带宽确定 - **异常指标**: 突然的流量峰值或持续的高流量 ## 报告生成模板 ### 系统健康报告模板 ```markdown ## 系统运行状态分析报告 ### 📊 监控数据概览 - **监控时...
    - #4 score=`0.0476`, source=`grafana-mcp-tools-guide.md`, chunk=`7`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6198`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`6`, rerankRank=`4`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":3},{"index":3,"rank":4}]`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`知识库中关于内存压力指标和解决思路的文档`, queryFusionScore=`0.0156`, queryFusionRank=`6`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"知识库中关于内存压力指标和解决思路的文档"}]`
      - preview: - **当前使用率**: {memory_usage}% - **可用内存**: {memory_available}GB - **状态评估**: {memory_status} - **使用趋势**: {memory_trend} ### 💿 磁盘使用情况 - **使用率**: {disk_usage}% - **可用空间**: {disk_availab...

answer:

```text
根据知识库，查询内存压力的思路是使用以下 PromQL 查询来计算内存使用率：

```promql
(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
```

**查询思路解释：**  
- 通过 `node_memory_MemAvailable_bytes` 获取当前可用内存的字节数，`node_memory_MemTotal_bytes` 获取总内存字节数。  
- 计算可用内存占总内存的比例，再用 `1` 减去该比例，得到已用内存的占比。  
- 最后乘以 `100` 转换为百分比形式的“内存使用率”。  
- 这个值越高，说明内存压力越大。结合上下文中的阈值，用于评估内存压力状况（如正常、警告或危险范围）。  

如果需要进一步分析内存压力，还可以结合时间范围观察趋势，或与 CPU 等指标进行关联分析（知识库中也提供了资源关联性查询的示例）。
```
