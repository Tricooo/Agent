# RAG Eval Result

- generated_at: `2026-05-18 16:48:21`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。
>
> `retrieved` / `score` / `empty` 三列的 `—` 表示**没拿到成功的 ChatResponse metadata**，不等价于"无检索"。当前实现把 retrieval SSE 帧放在 `.call().chatResponse()` 返回之后才发，所以 LLM 调用失败时（即使 RAG 检索本身成功）三列都会是 `—`。要区分"检索失败"和"生成失败"，对照 `error` 列 / details 区 / backend log。
>
> Details 区的 `pre_rerank_documents` 展开 rerank 前候选池，`documents` 展开最终 top-K chunk attribution；HYBRID 模式下 `score` 是 RRF score，原始向量分与关键词分分别看 `vectorScore` / `keywordScore`；真实 rerank 分数看 `rerankScore`；rerank 服务状态看 `rerank_runtime` 的 model / endpoint / failure_reason；keyword 分支未参与时看 `keywordSkippedReason`。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-10 | paraphrase | true | 20028 | true | 4 | 0.0159 .. 0.0164 | false | 2/5 | 正常范围, 警告范围, 危险范围 | 根据知识库的指引，查询内存压力的思路是使用以下 PromQL 语句（通过 `grafana/query_prometheus` 执行）[1]： ``` (1 - (node_memory_MemAvailable_bytes / node_... |  |

## Details

### RAG-10 - paraphrase

- question: 如果我要查内存压力，知识库给出的查询思路是什么？
- should_answer: `true`
- expected_source_section: 内存使用率查询 / 内存数据解释
- completed: `true`
- duration_ms: `20028`
- score_mode: `literal`
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
  - score_range: `0.0159 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2255` / max `6000`
  - rerank: applied `true` / mode `LOCAL_BGE` / candidates `7` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - query_rewrite: mode `HEURISTIC_MULTI_QUERY` / variants `2` / texts `如果我要查内存压力，知识库给出的查询思路是什么？， \| 内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6819`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #2 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7815`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, queryVariantIndex=`2`, queryVariantRank=`1`, queryVariantText=`内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`
      - preview: - **危险范围**: 95-100% ### 网络数据解释 - **单位**: bps (bits per second) - **正常范围**: 根据网络带宽确定 - **异常指标**: 突然的流量峰值或持续的高流量 ## 报告生成模板 ### 系统健康报告模板 ```markdown ## 系统运行状态分析报告 ### 📊 监控数据概览 - **监控时...
    - #3 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6741`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #4 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7290`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, queryVariantIndex=`2`, queryVariantRank=`2`, queryVariantText=`内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`
      - preview: # 磁盘使用增长趋势 predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) ``` ## 数据解释和分析标准 ### CPU数据解释 - **数值范围**: 0-100% - **正常范围**: 0-70% - **警告范围**: 70-90% - **危险范...
    - #5 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6692`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...
    - #6 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`7`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6813`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`6`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`
      - preview: - **当前使用率**: {memory_usage}% - **可用内存**: {memory_available}GB - **状态评估**: {memory_status} - **使用趋势**: {memory_trend} ### 💿 磁盘使用情况 - **使用率**: {disk_usage}% - **可用空间**: {disk_availab...
    - #7 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6684`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`7`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
  - documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6819`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`-1.0146`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #2 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`7`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6813`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`6`, rerankRank=`2`, rerankScore=`-4.2563`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`
      - preview: - **当前使用率**: {memory_usage}% - **可用内存**: {memory_available}GB - **状态评估**: {memory_status} - **使用趋势**: {memory_trend} ### 💿 磁盘使用情况 - **使用率**: {disk_usage}% - **可用空间**: {disk_availab...
    - #3 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6741`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`-4.7510`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #4 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7815`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`4`, rerankScore=`-5.1201`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, queryVariantIndex=`2`, queryVariantRank=`1`, queryVariantText=`内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`
      - preview: - **危险范围**: 95-100% ### 网络数据解释 - **单位**: bps (bits per second) - **正常范围**: 根据网络带宽确定 - **异常指标**: 突然的流量峰值或持续的高流量 ## 报告生成模板 ### 系统健康报告模板 ```markdown ## 系统运行状态分析报告 ### 📊 监控数据概览 - **监控时...

answer:

```text
根据知识库的指引，查询内存压力的思路是使用以下 PromQL 语句（通过 `grafana/query_prometheus` 执行）[1]：

```
(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
```

这个查询会计算当前内存使用率（已用内存占总内存的百分比），从而评估内存压力状况。如果使用率持续偏高，则说明内存压力较大。
```
