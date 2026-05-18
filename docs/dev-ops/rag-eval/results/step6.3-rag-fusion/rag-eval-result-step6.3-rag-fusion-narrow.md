# RAG Eval Result

- generated_at: `2026-05-18 19:57:59`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。
>
> `retrieved` / `score` / `empty` 三列的 `—` 表示**没拿到成功的 ChatResponse metadata**，不等价于"无检索"。当前实现把 retrieval SSE 帧放在 `.call().chatResponse()` 返回之后才发，所以 LLM 调用失败时（即使 RAG 检索本身成功）三列都会是 `—`。要区分"检索失败"和"生成失败"，对照 `error` 列 / details 区 / backend log。
>
> Details 区的 `pre_rerank_documents` 展开 rerank 前候选池，`documents` 展开最终 top-K chunk attribution；document 行的 `score` 是当前阶段写回的候选分，可能来自 HYBRID RRF 或 query-variant fusion；原始向量分与关键词分分别看 `vectorScore` / `keywordScore`，多 query 融合看 `queryFusionScore/queryFusionRank/queryVariantHitCount`；真实 rerank 分数看 `rerankScore`；rerank 服务状态看 `rerank_runtime` 的 model / endpoint / failure_reason；keyword 分支未参与时看 `keywordSkippedReason`。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-04 | answerable_deep_section | true | 16777 | true | 4 | 0.0156 .. 0.0164 | false | 3/3 |  | 根据提供的 Grafana 监控指南[1]，用于预测磁盘 7 天增长趋势的 PromQL 是： ```promql predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24... |  |
| RAG-07 | weak_related | true | 5877 | false | 4 | 0.0156 .. 0.0164 | false | manual | — | 根据当前提供的知识库内容，主要介绍了 Grafana MCP 工具的使用，例如数据源查询和 PromQL 示例，并未涉及 Dashboard 变量模板的配置方法。因此我无法回答您关于 Grafana dashboard 变量模板配置的问题。... |  |
| RAG-10 | paraphrase | true | 15512 | true | 4 | 0.0159 .. 0.0164 | false | 5/5 |  | 根据知识库，查询内存压力的思路如下： - **使用 PromQL 查询**： `(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100` 该表达式计... |  |
| RAG-14 | control_plaintext_en_weak_related | true | 7315 | false | 4 | 0.0156 .. 0.0164 | false | manual | — | The control document does not state which cloud provider hosts the fallback queue. It explicitly notes that the document... |  |

## Details

### RAG-04 - answerable_deep_section

- question: 磁盘 7 天增长趋势预测用哪个 PromQL？
- should_answer: `true`
- expected_source_section: 趋势预测
- completed: `true`
- duration_ms: `16777`
- score_mode: `literal`
- expected_points:
  - [x] predict_linear
  - [x] node_filesystem_avail_bytes
  - [x] 7*24*3600

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2217` / max `6000`
  - rerank: applied `true` / mode `LOCAL_BGE` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: mode `PASSTHROUGH` / variants `1` / texts `磁盘 7 天增长趋势预测用哪个 PromQL？，`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7406`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`
      - preview: # 磁盘使用增长趋势 predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) ``` ## 数据解释和分析标准 ### CPU数据解释 - **数值范围**: 0-100% - **正常范围**: 0-70% - **警告范围**: 70-90% - **危险范...
    - #2 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6639`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #3 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6563`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #4 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6481`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
  - documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7406`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.1935`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`
      - preview: # 磁盘使用增长趋势 predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) ``` ## 数据解释和分析标准 ### CPU数据解释 - **数值范围**: 0-100% - **正常范围**: 0-70% - **警告范围**: 70-90% - **危险范...
    - #2 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6563`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`-0.4167`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #3 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6481`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`3`, rerankScore=`-2.8748`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #4 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6639`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`4`, rerankScore=`-3.1441`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...

answer:

```text
根据提供的 Grafana 监控指南[1]，用于预测磁盘 7 天增长趋势的 PromQL 是：

```promql
predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600)
```

该查询基于过去 24 小时内 `node_filesystem_avail_bytes` 指标（排除 tmpfs 文件系统）的变化趋势，预测未来 7 天（`7*24*3600` 秒）的剩余磁盘空间。如果预测值趋近于零或负数，则表明磁盘空间存在耗尽风险。
```

### RAG-07 - weak_related

- question: 根据知识库，Grafana dashboard 变量模板怎么配置？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `5877`
- score_mode: `manual`
- expected_points:
  - 知识库未涉及 dashboard 变量模板
  - 无法基于当前知识库回答

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2199` / max `6000`
  - rerank: applied `true` / mode `LOCAL_BGE` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: mode `PASSTHROUGH` / variants `1` / texts `根据知识库，Grafana dashboard 变量模板怎么配置？，`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6726`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #2 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6648`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #3 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6647`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #4 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`8`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6464`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`
      - preview: ### 常见错误 1. **数据源连接失败** - 检查Prometheus服务状态 - 验证网络连接 - 确认认证配置 2. **查询语法错误** - 验证PromQL语法 - 检查指标名称拼写 - 确认标签选择器 3. **数据缺失** - 检查时间范围设置 - 验证指标采集状态 - 确认数据保留策略 ### 故障排查步骤 1. **验证数据源** ``...
  - documents:
    - #1 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6647`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`1`, rerankScore=`-1.9974`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #2 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6726`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`-2.3690`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #3 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`8`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6464`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`3`, rerankScore=`-3.9344`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`
      - preview: ### 常见错误 1. **数据源连接失败** - 检查Prometheus服务状态 - 验证网络连接 - 确认认证配置 2. **查询语法错误** - 验证PromQL语法 - 检查指标名称拼写 - 确认标签选择器 3. **数据缺失** - 检查时间范围设置 - 验证指标采集状态 - 确认数据保留策略 ### 故障排查步骤 1. **验证数据源** ``...
    - #4 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6648`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`4`, rerankScore=`-4.1469`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...

answer:

```text
根据当前提供的知识库内容，主要介绍了 Grafana MCP 工具的使用，例如数据源查询和 PromQL 示例，并未涉及 Dashboard 变量模板的配置方法。因此我无法回答您关于 Grafana dashboard 变量模板配置的问题。建议您查阅 Grafana 官方文档中关于“变量（Variables）”的章节获取详细配置步骤。
```

### RAG-10 - paraphrase

- question: 如果我要查内存压力，知识库给出的查询思路是什么？
- should_answer: `true`
- expected_source_section: 内存使用率查询 / 内存数据解释
- completed: `true`
- duration_ms: `15512`
- score_mode: `literal`
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
  - score_range: `0.0159 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2044` / max `6000`
  - rerank: applied `true` / mode `LOCAL_BGE` / candidates `7` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - coverage_guard: applied `true` / added `1`
  - query_rewrite: mode `HEURISTIC_MULTI_QUERY` / variants `2` / texts `如果我要查内存压力，知识库给出的查询思路是什么？， \| 内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`
  - pre_rerank_documents:
    - #1 score=`0.0313`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6684`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0313`, queryFusionRank=`1`, queryVariantHitCount=`2`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 2]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #2 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6819`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #3 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7815`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, queryVariantIndex=`2`, queryVariantRank=`1`, queryVariantText=`内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`, queryFusionScore=`0.0164`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[2]`
      - preview: - **危险范围**: 95-100% ### 网络数据解释 - **单位**: bps (bits per second) - **正常范围**: 根据网络带宽确定 - **异常指标**: 突然的流量峰值或持续的高流量 ## 报告生成模板 ### 系统健康报告模板 ```markdown ## 系统运行状态分析报告 ### 📊 监控数据概览 - **监控时...
    - #4 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6741`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #5 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7290`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, queryVariantIndex=`2`, queryVariantRank=`2`, queryVariantText=`内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`, queryFusionScore=`0.0161`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[2]`
      - preview: # 磁盘使用增长趋势 predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) ``` ## 数据解释和分析标准 ### CPU数据解释 - **数值范围**: 0-100% - **正常范围**: 0-70% - **警告范围**: 70-90% - **危险范...
    - #6 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6692`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`6`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`6`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...
    - #7 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`7`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6813`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`7`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`, queryFusionScore=`0.0159`, queryFusionRank=`7`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2]`
      - preview: - **当前使用率**: {memory_usage}% - **可用内存**: {memory_available}GB - **状态评估**: {memory_status} - **使用趋势**: {memory_trend} ### 💿 磁盘使用情况 - **使用率**: {disk_usage}% - **可用空间**: {disk_availab...
  - documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6819`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`1`, rerankScore=`-1.0146`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #2 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`7`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6813`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`7`, rerankRank=`2`, rerankScore=`-4.2563`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, coverageGuardAdded=`—`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`, queryFusionScore=`0.0159`, queryFusionRank=`7`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2]`
      - preview: - **当前使用率**: {memory_usage}% - **可用内存**: {memory_available}GB - **状态评估**: {memory_status} - **使用趋势**: {memory_trend} ### 💿 磁盘使用情况 - **使用率**: {disk_usage}% - **可用空间**: {disk_availab...
    - #3 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7290`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`5`, rerankRank=`3`, rerankScore=`—`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, coverageGuardAdded=`True`, queryVariantIndex=`2`, queryVariantRank=`2`, queryVariantText=`内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`, queryFusionScore=`0.0161`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[2]`
      - preview: # 磁盘使用增长趋势 predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) ``` ## 数据解释和分析标准 ### CPU数据解释 - **数值范围**: 0-100% - **正常范围**: 0-70% - **警告范围**: 70-90% - **危险范...
    - #4 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7815`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`4`, rerankScore=`-5.1201`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, coverageGuardAdded=`—`, queryVariantIndex=`2`, queryVariantRank=`1`, queryVariantText=`内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`, queryFusionScore=`0.0164`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[2]`
      - preview: - **危险范围**: 95-100% ### 网络数据解释 - **单位**: bps (bits per second) - **正常范围**: 根据网络带宽确定 - **异常指标**: 突然的流量峰值或持续的高流量 ## 报告生成模板 ### 系统健康报告模板 ```markdown ## 系统运行状态分析报告 ### 📊 监控数据概览 - **监控时...

answer:

```text
根据知识库，查询内存压力的思路如下：

- **使用 PromQL 查询**：  
  `(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100`  
  该表达式计算当前内存使用率（百分比），其中 `node_memory_MemAvailable_bytes` 表示可用内存，`node_memory_MemTotal_bytes` 表示总内存。

- **评估内存压力状况**：  
  根据使用率判断压力级别：
  - **正常范围**：0% – 80%
  - **警告范围**：80% – 95%
  - **危险范围**：95% – 100%

- **在 Grafana 中查询时**，可通过 `grafana/query_prometheus` 工具执行上述 PromQL，并得到当前内存使用率，进而分析是否存在内存压力。
```

### RAG-14 - control_plaintext_en_weak_related

- question: According to the control document, which cloud provider hosts the fallback queue?
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `7315`
- score_mode: `manual`
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
  - rerank: applied `true` / mode `LOCAL_BGE` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: mode `PASSTHROUGH` / variants `1` / texts `According to the control document, which cloud provider hosts the fallback queue?，`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`control-en-long-paragraph.txt`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7799`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`
      - preview: It does not mention a region, a vendor ticket number, or a private dashboard link. It only states that silver-river-42 is the fallback queue name, that the fallback retry budget is...
    - #2 score=`0.0161`, source=`control-en-long-paragraph.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7513`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`
      - preview: This note is present to add realistic language around the anchor silver-river-42 without changing the anchor itself. The queue name remains silver-river-42. The document still does...
    - #3 score=`0.0159`, source=`control-en-long-paragraph.txt`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7453`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`
      - preview: This control document is written as a long plain text incident runbook for retrieval testing. It is not a production policy and it intentionally avoids headings, lists, tables, cod...
    - #4 score=`0.0156`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7432`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...
  - documents:
    - #1 score=`0.0161`, source=`control-en-long-paragraph.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7513`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`1`, rerankScore=`1.7528`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`
      - preview: This note is present to add realistic language around the anchor silver-river-42 without changing the anchor itself. The queue name remains silver-river-42. The document still does...
    - #2 score=`0.0164`, source=`control-en-long-paragraph.txt`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7799`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`1.2228`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`
      - preview: It does not mention a region, a vendor ticket number, or a private dashboard link. It only states that silver-river-42 is the fallback queue name, that the fallback retry budget is...
    - #3 score=`0.0159`, source=`control-en-long-paragraph.txt`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7453`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.6725`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`
      - preview: This control document is written as a long plain text incident runbook for retrieval testing. It is not a production policy and it intentionally avoids headings, lists, tables, cod...
    - #4 score=`0.0156`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7432`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`-5.2668`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`, coverageGuardAdded=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...

answer:

```text
The control document does not state which cloud provider hosts the fallback queue. It explicitly notes that the document does not identify a cloud provider, so that question cannot be answered from this document [1][2].
```
