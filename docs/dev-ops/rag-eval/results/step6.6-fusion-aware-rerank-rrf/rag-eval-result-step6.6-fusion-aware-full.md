# RAG Eval Result

- generated_at: `2026-05-19 16:35:59`
- api_url: `http://127.0.0.1:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。
>
> `retrieved` / `score` / `empty` 三列的 `—` 表示**没拿到成功的 ChatResponse metadata**，不等价于"无检索"。当前实现把 retrieval SSE 帧放在 `.call().chatResponse()` 返回之后才发，所以 LLM 调用失败时（即使 RAG 检索本身成功）三列都会是 `—`。要区分"检索失败"和"生成失败"，对照 `error` 列 / details 区 / backend log。
>
> Details 区的 `pre_rerank_documents` 展开 rerank 前候选池，`documents` 展开最终 top-K chunk attribution；document 行的 `score` 是当前阶段写回的候选分，可能来自 HYBRID RRF、query-variant fusion、per-variant rerank RRF 或 fusion-aware rerank；原始向量分与关键词分分别看 `vectorScore` / `keywordScore`，多 query 召回融合看 `queryFusionScore/queryFusionRank/queryVariantHitCount`；最终 rerank 写回分看 `rerankScore`，多路 rerank 融合分看 `rerankFusionScore/rerankVariantHitCount`，fusion-aware 弱加成看 `queryFusionBoostScore/fusionAwareScore`；rerank 服务状态看 `rerank_runtime` 的 model / endpoint / failure_reason；keyword 分支未参与时看 `keywordSkippedReason`。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-01 | answerable | true | 12405 | true | 4 | 0.0202 .. 0.0213 | false | manual | — | 根据知识库中的内容，`grafana/list_datasources` 工具的主要作用如下： - **列出所有可用的数据源**：获取 Grafana 中已配置的全部数据源。 - **返回数据源基本信息**：包括数据源的 ID、名称、类型（... |  |
| RAG-02 | answerable | true | 7089 | true | 4 | 0.0203 .. 0.0212 | false | 4/4 |  | 根据文档，`grafana/query_prometheus` 支持以下参数： - **query**（必需）：PromQL 查询语句 - **start**（可选）：查询开始时间 - **end**（可选）：查询结束时间 - **step... |  |
| RAG-03 | answerable_formula | true | 19555 | true | 4 | 0.0205 .. 0.0213 | false | 3/3 |  | 根据知识库中的内容，查询 CPU 使用率的 PromQL 示例如下： - **整体 CPU 使用率** `100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)` ... |  |
| RAG-04 | answerable_deep_section | true | 23765 | true | 4 | 0.0205 .. 0.0213 | false | 3/3 |  | 您需要的 PromQL 是从 [1] 中直接给出的磁盘使用增长趋势预测查询： ```promql predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*... |  |
| RAG-05 | answerable_long_context | true | 12371 | true | 4 | 0.0203 .. 0.0212 | false | 5/5 |  | 根据提供的文档，系统健康报告模板包含以下部分： 1. **监控数据概览** - 监控时间范围 - 数据源 - 分析时间 2. **CPU 使用情况** - 平均使用率 - 峰值使用率 - 状态评估 - 趋势分析 3. **内存使用情况** ... |  |
| RAG-06 | unanswerable | true | 4085 | false | 0 | — | true | manual | — | 当前知识库没有可用上下文，不能基于知识库回答该问题。如果你有更具体的技术细节需要探讨，可以补充更多信息，我将尽力协助。 |  |
| RAG-07 | weak_related | true | 7927 | false | 4 | 0.0205 .. 0.0212 | false | manual | — | 根据当前知识库的内容，并未包含关于 Grafana dashboard 变量模板配置的说明。现有知识主要围绕 Grafana MCP 工具的使用（如数据源列表、PromQL 查询等），暂未涉及 dashboard 模板变量的配置方法。 如果... |  |
| RAG-08 | weak_related | true | 4706 | false | 0 | — | true | manual | — | 根据当前知识库的检索结果，没有找到与 Alertmanager 的 silence 创建相关的可用信息，因此我无法基于知识库为您提供具体的创建步骤。如需进一步协助，建议查阅官方文档或提供更多背景信息。 |  |
| RAG-09 | paraphrase | true | 8327 | true | 4 | 0.0203 .. 0.0213 | false | manual | — | 要确认监控数据来源，应该调用 **`grafana/list_datasources`** 工具。 这个工具会列出 Grafana 中配置的所有数据源（如 Prometheus、InfluxDB 等），并返回它们的名称、类型、URL 等信息... |  |
| RAG-10 | paraphrase | true | 14741 | true | 4 | 0.0359 .. 0.0366 | false | 4/5 | 警告范围 | 根据知识库，查询内存压力的思路是通过 Prometheus 查询计算当前内存使用率，具体查询语句为： ``` (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes... |  |
| RAG-11 | control_plaintext_cn_answerable | true | 6278 | true | 4 | 0.0205 .. 0.0213 | false | 2/2 |  | 根据控制组文档，冷数据迁移阈值是**连续二十一天未访问**（[4][1]），归档窗口编号固定为 **ARCHIVE-CN-7319**（[2][1]）。 |  |
| RAG-12 | control_plaintext_cn_weak_related | true | 5372 | false | 4 | 0.0203 .. 0.0213 | false | manual | — | 根据控制组文档的内容，文档中没有指定归档审批人的姓名。文档明确说明未提供审批人信息，也不应编造相关组织架构信息。 |  |
| RAG-13 | control_plaintext_en_answerable | true | 6478 | true | 4 | 0.0205 .. 0.0212 | false | 2/2 |  | According to the control document, the fallback queue name is **silver-river-42**, and the retry budget for the fallback... |  |
| RAG-14 | control_plaintext_en_weak_related | true | 7500 | false | 4 | 0.0203 .. 0.0212 | false | manual | — | The control document explicitly states that it does not identify a cloud provider for the fallback queue, so this questi... |  |

## Details

### RAG-01 - answerable

- question: 根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。
- should_answer: `true`
- expected_source_section: grafana/list_datasources
- completed: `true`
- duration_ms: `12405`
- score_mode: `manual`
- expected_points:
  - 列出所有可用的数据源
  - 返回数据源的基本信息
  - 确认 Prometheus 数据源的可用性

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0202 .. 0.0213`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2252` / max `6000`
  - rerank: applied `true` / mode `FUSION_AWARE_RERANK_RRF` / candidates `5` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `FUSION_AWARE_RERANK_RRF` / text `根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: mode `PASSTHROUGH` / variants `1` / texts `根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0328`, vectorRank=`1`, vectorScore=`0.8057`, keywordRank=`1`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，"}]`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #2 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0318`, vectorRank=`4`, vectorScore=`0.6561`, keywordRank=`2`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #3 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6647`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #4 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6601`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #5 score=`0.0154`, source=`grafana-mcp-tools-guide.md`, chunk=`8`, retrievalSource=`KEYWORD`, rrfScore=`0.0159`, vectorRank=`—`, vectorScore=`—`, keywordRank=`3`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`5`, queryVariantText=`根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`, queryFusionScore=`0.0154`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`5`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":5,"text":"根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，"}]`
      - preview: ### 常见错误 1. **数据源连接失败** - 检查Prometheus服务状态 - 验证网络连接 - 确认认证配置 2. **查询语法错误** - 验证PromQL语法 - 检查指标名称拼写 - 确认标签选择器 3. **数据缺失** - 检查时间范围设置 - 验证指标采集状态 - 确认数据保留策略 ### 故障排查步骤 1. **验证数据源** ``...
  - documents:
    - #1 score=`0.0213`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0328`, vectorRank=`1`, vectorScore=`0.8057`, keywordRank=`1`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0213`, rerankFusionScore=`0.0164`, queryFusionBoostScore=`0.0049`, fusionAwareScore=`0.0213`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，"}]`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #2 score=`0.0208`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6601`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`2`, rerankScore=`0.0208`, rerankFusionScore=`0.0161`, queryFusionBoostScore=`0.0047`, fusionAwareScore=`0.0208`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #3 score=`0.0207`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0318`, vectorRank=`4`, vectorScore=`0.6561`, keywordRank=`2`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`3`, rerankScore=`0.0207`, rerankFusionScore=`0.0159`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0207`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #4 score=`0.0202`, source=`grafana-mcp-tools-guide.md`, chunk=`8`, retrievalSource=`KEYWORD`, rrfScore=`0.0159`, vectorRank=`—`, vectorScore=`—`, keywordRank=`3`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`5`, rerankRank=`4`, rerankScore=`0.0202`, rerankFusionScore=`0.0156`, queryFusionBoostScore=`0.0046`, fusionAwareScore=`0.0202`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`5`, queryVariantText=`根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`, queryFusionScore=`0.0154`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`5`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":5,"text":"根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，"}]`
      - preview: ### 常见错误 1. **数据源连接失败** - 检查Prometheus服务状态 - 验证网络连接 - 确认认证配置 2. **查询语法错误** - 验证PromQL语法 - 检查指标名称拼写 - 确认标签选择器 3. **数据缺失** - 检查时间范围设置 - 验证指标采集状态 - 确认数据保留策略 ### 故障排查步骤 1. **验证数据源** ``...

answer:

```text
根据知识库中的内容，`grafana/list_datasources` 工具的主要作用如下：

- **列出所有可用的数据源**：获取 Grafana 中已配置的全部数据源。
- **返回数据源基本信息**：包括数据源的 ID、名称、类型（如 Prometheus）、URL、访问方式等。
- **用于系统分析前的检查**：例如在自动化监控流程中，第一步通常是调用该工具确认 Prometheus 等数据源的可用性，确保后续查询可以正常进行。
- **多数据源环境管理**：帮助使用者了解当前有哪些监控数据来源，便于管理和验证[1]。
```

### RAG-02 - answerable

- question: `grafana/query_prometheus` 支持哪些参数？
- should_answer: `true`
- expected_source_section: grafana/query_prometheus 参数说明
- completed: `true`
- duration_ms: `7089`
- score_mode: `literal`
- expected_points:
  - [x] query
  - [x] start
  - [x] end
  - [x] step

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0203 .. 0.0212`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2305` / max `6000`
  - rerank: applied `true` / mode `FUSION_AWARE_RERANK_RRF` / candidates `5` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `FUSION_AWARE_RERANK_RRF` / text ``grafana/query_prometheus` 支持哪些参数？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: mode `PASSTHROUGH` / variants `1` / texts ``grafana/query_prometheus` 支持哪些参数？，`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0323`, vectorRank=`3`, vectorScore=`0.6989`, keywordRank=`1`, keywordScore=`0.3000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=``grafana/query_prometheus` 支持哪些参数？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"`grafana/query_prometheus` 支持哪些参数？，"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #2 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0323`, vectorRank=`1`, vectorScore=`0.8033`, keywordRank=`3`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=``grafana/query_prometheus` 支持哪些参数？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"`grafana/query_prometheus` 支持哪些参数？，"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #3 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`8`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0318`, vectorRank=`4`, vectorScore=`0.6919`, keywordRank=`2`, keywordScore=`0.3000`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=``grafana/query_prometheus` 支持哪些参数？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"`grafana/query_prometheus` 支持哪些参数？，"}]`
      - preview: ### 常见错误 1. **数据源连接失败** - 检查Prometheus服务状态 - 验证网络连接 - 确认认证配置 2. **查询语法错误** - 验证PromQL语法 - 检查指标名称拼写 - 确认标签选择器 3. **数据缺失** - 检查时间范围设置 - 验证指标采集状态 - 确认数据保留策略 ### 故障排查步骤 1. **验证数据源** ``...
    - #4 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7107`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=``grafana/query_prometheus` 支持哪些参数？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"`grafana/query_prometheus` 支持哪些参数？，"}]`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #5 score=`0.0154`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`KEYWORD`, rrfScore=`0.0156`, vectorRank=`—`, vectorScore=`—`, keywordRank=`4`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`5`, queryVariantText=``grafana/query_prometheus` 支持哪些参数？，`, queryFusionScore=`0.0154`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`5`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":5,"text":"`grafana/query_prometheus` 支持哪些参数？，"}]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
  - documents:
    - #1 score=`0.0212`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0323`, vectorRank=`1`, vectorScore=`0.8033`, keywordRank=`3`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`1`, rerankScore=`0.0212`, rerankFusionScore=`0.0164`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0212`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=``grafana/query_prometheus` 支持哪些参数？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"`grafana/query_prometheus` 支持哪些参数？，"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #2 score=`0.0210`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0323`, vectorRank=`3`, vectorScore=`0.6989`, keywordRank=`1`, keywordScore=`0.3000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`0.0210`, rerankFusionScore=`0.0161`, queryFusionBoostScore=`0.0049`, fusionAwareScore=`0.0210`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=``grafana/query_prometheus` 支持哪些参数？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"`grafana/query_prometheus` 支持哪些参数？，"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #3 score=`0.0205`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`KEYWORD`, rrfScore=`0.0156`, vectorRank=`—`, vectorScore=`—`, keywordRank=`4`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`5`, rerankRank=`3`, rerankScore=`0.0205`, rerankFusionScore=`0.0159`, queryFusionBoostScore=`0.0046`, fusionAwareScore=`0.0205`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`5`, queryVariantText=``grafana/query_prometheus` 支持哪些参数？，`, queryFusionScore=`0.0154`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`5`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":5,"text":"`grafana/query_prometheus` 支持哪些参数？，"}]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #4 score=`0.0203`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7107`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0203`, rerankFusionScore=`0.0156`, queryFusionBoostScore=`0.0047`, fusionAwareScore=`0.0203`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=``grafana/query_prometheus` 支持哪些参数？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"`grafana/query_prometheus` 支持哪些参数？，"}]`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...

answer:

```text
根据文档，`grafana/query_prometheus` 支持以下参数：

- **query**（必需）：PromQL 查询语句
- **start**（可选）：查询开始时间
- **end**（可选）：查询结束时间
- **step**（可选）：查询步长

这些信息在 [1] 的参数说明部分有明确列出。
```

### RAG-03 - answerable_formula

- question: 知识库里 CPU 使用率的 PromQL 示例是什么？
- should_answer: `true`
- expected_source_section: CPU使用率查询
- completed: `true`
- duration_ms: `19555`
- score_mode: `literal`
- expected_points:
  - [x] node_cpu_seconds_total
  - [x] mode="idle"
  - [x] irate

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0205 .. 0.0213`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2163` / max `6000`
  - rerank: applied `true` / mode `FUSION_AWARE_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `FUSION_AWARE_RERANK_RRF` / text `知识库里 CPU 使用率的 PromQL 示例是什么？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: mode `PASSTHROUGH` / variants `1` / texts `知识库里 CPU 使用率的 PromQL 示例是什么？，`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7252`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`知识库里 CPU 使用率的 PromQL 示例是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"知识库里 CPU 使用率的 PromQL 示例是什么？，"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #2 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6976`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`知识库里 CPU 使用率的 PromQL 示例是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"知识库里 CPU 使用率的 PromQL 示例是什么？，"}]`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...
    - #3 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6863`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`知识库里 CPU 使用率的 PromQL 示例是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"知识库里 CPU 使用率的 PromQL 示例是什么？，"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #4 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6745`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`知识库里 CPU 使用率的 PromQL 示例是什么？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"知识库里 CPU 使用率的 PromQL 示例是什么？，"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
  - documents:
    - #1 score=`0.0213`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7252`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0213`, rerankFusionScore=`0.0164`, queryFusionBoostScore=`0.0049`, fusionAwareScore=`0.0213`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`知识库里 CPU 使用率的 PromQL 示例是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"知识库里 CPU 使用率的 PromQL 示例是什么？，"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #2 score=`0.0209`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6863`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`0.0209`, rerankFusionScore=`0.0161`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0209`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`知识库里 CPU 使用率的 PromQL 示例是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"知识库里 CPU 使用率的 PromQL 示例是什么？，"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #3 score=`0.0206`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6745`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`3`, rerankScore=`0.0206`, rerankFusionScore=`0.0159`, queryFusionBoostScore=`0.0047`, fusionAwareScore=`0.0206`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`知识库里 CPU 使用率的 PromQL 示例是什么？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"知识库里 CPU 使用率的 PromQL 示例是什么？，"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #4 score=`0.0205`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6976`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`4`, rerankScore=`0.0205`, rerankFusionScore=`0.0156`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0205`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`知识库里 CPU 使用率的 PromQL 示例是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"知识库里 CPU 使用率的 PromQL 示例是什么？，"}]`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...

answer:

```text
根据知识库中的内容，查询 CPU 使用率的 PromQL 示例如下：

- **整体 CPU 使用率**
  `100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)` [1]

- **按实例分组的 CPU 使用率**
  `100 - (avg by (instance)(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)` [1]

- **用于自动化分析的 CPU 查询（查询过去 1 小时）**
  `100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100)` [3]

这些查询均基于 `node_cpu_seconds_total` 指标，通过计算非空闲时间占比来反映 CPU 使用率。
```

### RAG-04 - answerable_deep_section

- question: 磁盘 7 天增长趋势预测用哪个 PromQL？
- should_answer: `true`
- expected_source_section: 趋势预测
- completed: `true`
- duration_ms: `23765`
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
  - score_range: `0.0205 .. 0.0213`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2217` / max `6000`
  - rerank: applied `true` / mode `FUSION_AWARE_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `FUSION_AWARE_RERANK_RRF` / text `磁盘 7 天增长趋势预测用哪个 PromQL？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: mode `PASSTHROUGH` / variants `1` / texts `磁盘 7 天增长趋势预测用哪个 PromQL？，`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7406`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"磁盘 7 天增长趋势预测用哪个 PromQL？，"}]`
      - preview: # 磁盘使用增长趋势 predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) ``` ## 数据解释和分析标准 ### CPU数据解释 - **数值范围**: 0-100% - **正常范围**: 0-70% - **警告范围**: 70-90% - **危险范...
    - #2 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6639`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"磁盘 7 天增长趋势预测用哪个 PromQL？，"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #3 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6563`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"磁盘 7 天增长趋势预测用哪个 PromQL？，"}]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #4 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6481`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"磁盘 7 天增长趋势预测用哪个 PromQL？，"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
  - documents:
    - #1 score=`0.0213`, source=`grafana-mcp-tools-guide.md`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7406`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0213`, rerankFusionScore=`0.0164`, queryFusionBoostScore=`0.0049`, fusionAwareScore=`0.0213`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"磁盘 7 天增长趋势预测用哪个 PromQL？，"}]`
      - preview: # 磁盘使用增长趋势 predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) ``` ## 数据解释和分析标准 ### CPU数据解释 - **数值范围**: 0-100% - **正常范围**: 0-70% - **警告范围**: 70-90% - **危险范...
    - #2 score=`0.0209`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6563`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`0.0209`, rerankFusionScore=`0.0161`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0209`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"磁盘 7 天增长趋势预测用哪个 PromQL？，"}]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #3 score=`0.0206`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6481`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`3`, rerankScore=`0.0206`, rerankFusionScore=`0.0159`, queryFusionBoostScore=`0.0047`, fusionAwareScore=`0.0206`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"磁盘 7 天增长趋势预测用哪个 PromQL？，"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #4 score=`0.0205`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6639`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`4`, rerankScore=`0.0205`, rerankFusionScore=`0.0156`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0205`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"磁盘 7 天增长趋势预测用哪个 PromQL？，"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...

answer:

```text
您需要的 PromQL 是从 [1] 中直接给出的磁盘使用增长趋势预测查询：

```promql
predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600)
```

**解释**：
- 基于过去 24 小时（`[24h]`）的磁盘可用空间数据（`node_filesystem_avail_bytes`，排除 tmpfs 文件系统）
- 使用线性回归预测 7 天（即 `7*24*3600` 秒）后的可用空间
- 如果预测值变为负数，说明当前剩余空间预计在 7 天内就会耗尽
```

### RAG-05 - answerable_long_context

- question: 系统健康报告模板包含哪些部分？
- should_answer: `true`
- expected_source_section: 系统健康报告模板
- completed: `true`
- duration_ms: `12371`
- score_mode: `literal`
- expected_points:
  - [x] CPU
  - [x] 内存
  - [x] 磁盘
  - [x] 网络
  - [x] 综合评估

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0203 .. 0.0212`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `1985` / max `6000`
  - rerank: applied `true` / mode `FUSION_AWARE_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `FUSION_AWARE_RERANK_RRF` / text `系统健康报告模板包含哪些部分？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: mode `PASSTHROUGH` / variants `1` / texts `系统健康报告模板包含哪些部分？，`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`7`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7195`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`系统健康报告模板包含哪些部分？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"系统健康报告模板包含哪些部分？，"}]`
      - preview: - **当前使用率**: {memory_usage}% - **可用内存**: {memory_available}GB - **状态评估**: {memory_status} - **使用趋势**: {memory_trend} ### 💿 磁盘使用情况 - **使用率**: {disk_usage}% - **可用空间**: {disk_availab...
    - #2 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7172`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`系统健康报告模板包含哪些部分？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"系统健康报告模板包含哪些部分？，"}]`
      - preview: - **危险范围**: 95-100% ### 网络数据解释 - **单位**: bps (bits per second) - **正常范围**: 根据网络带宽确定 - **异常指标**: 突然的流量峰值或持续的高流量 ## 报告生成模板 ### 系统健康报告模板 ```markdown ## 系统运行状态分析报告 ### 📊 监控数据概览 - **监控时...
    - #3 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6314`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`系统健康报告模板包含哪些部分？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"系统健康报告模板包含哪些部分？，"}]`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...
    - #4 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6178`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`系统健康报告模板包含哪些部分？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"系统健康报告模板包含哪些部分？，"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
  - documents:
    - #1 score=`0.0212`, source=`grafana-mcp-tools-guide.md`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7172`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`1`, rerankScore=`0.0212`, rerankFusionScore=`0.0164`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0212`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`系统健康报告模板包含哪些部分？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"系统健康报告模板包含哪些部分？，"}]`
      - preview: - **危险范围**: 95-100% ### 网络数据解释 - **单位**: bps (bits per second) - **正常范围**: 根据网络带宽确定 - **异常指标**: 突然的流量峰值或持续的高流量 ## 报告生成模板 ### 系统健康报告模板 ```markdown ## 系统运行状态分析报告 ### 📊 监控数据概览 - **监控时...
    - #2 score=`0.0210`, source=`grafana-mcp-tools-guide.md`, chunk=`7`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7195`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`0.0210`, rerankFusionScore=`0.0161`, queryFusionBoostScore=`0.0049`, fusionAwareScore=`0.0210`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`系统健康报告模板包含哪些部分？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"系统健康报告模板包含哪些部分？，"}]`
      - preview: - **当前使用率**: {memory_usage}% - **可用内存**: {memory_available}GB - **状态评估**: {memory_status} - **使用趋势**: {memory_trend} ### 💿 磁盘使用情况 - **使用率**: {disk_usage}% - **可用空间**: {disk_availab...
    - #3 score=`0.0206`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6314`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0206`, rerankFusionScore=`0.0159`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0206`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`系统健康报告模板包含哪些部分？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"系统健康报告模板包含哪些部分？，"}]`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...
    - #4 score=`0.0203`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6178`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0203`, rerankFusionScore=`0.0156`, queryFusionBoostScore=`0.0047`, fusionAwareScore=`0.0203`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`系统健康报告模板包含哪些部分？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"系统健康报告模板包含哪些部分？，"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...

answer:

```text
根据提供的文档，系统健康报告模板包含以下部分：

1. **监控数据概览**
   - 监控时间范围
   - 数据源
   - 分析时间

2. **CPU 使用情况**
   - 平均使用率
   - 峰值使用率
   - 状态评估
   - 趋势分析

3. **内存使用情况**
   - 当前使用率
   - 可用内存
   - 状态评估
   - 使用趋势

4. **磁盘使用情况**
   - 使用率
   - 可用空间
   - 状态评估
   - 空间预警

5. **网络流量情况**
   - 接收速率
   - 发送速率
   - 状态评估
   - 流量模式

6. **综合评估**
   - 系统健康度
   - 关键发现
   - 优化建议
   - 告警建议

这些部分共同构成一份完整的系统健康报告，覆盖计算、存储、网络等核心资源的状态与趋势分析【1】【2】。
```

### RAG-06 - unanswerable

- question: 根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `4085`
- score_mode: `manual`
- expected_points:
  - 无法基于当前知识库回答
  - 当前知识库没有相关上下文

retrieval:
- event #1
  - retrieved_document_count: `0`
  - retrieval_empty: `true`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `— .. —`
  - context_selected: `0` / dropped: `0` / truncated: `false`
  - context_chars: actual `61` / max `6000`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `0` / final `0`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `FUSION_AWARE_RERANK_RRF` / text `根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: mode `PASSTHROUGH` / variants `1` / texts `根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？，`

answer:

```text
当前知识库没有可用上下文，不能基于知识库回答该问题。如果你有更具体的技术细节需要探讨，可以补充更多信息，我将尽力协助。
```

### RAG-07 - weak_related

- question: 根据知识库，Grafana dashboard 变量模板怎么配置？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `7927`
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
  - score_range: `0.0205 .. 0.0212`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2199` / max `6000`
  - rerank: applied `true` / mode `FUSION_AWARE_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `FUSION_AWARE_RERANK_RRF` / text `根据知识库，Grafana dashboard 变量模板怎么配置？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: mode `PASSTHROUGH` / variants `1` / texts `根据知识库，Grafana dashboard 变量模板怎么配置？，`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6726`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据知识库，Grafana dashboard 变量模板怎么配置？，"}]`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #2 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6648`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据知识库，Grafana dashboard 变量模板怎么配置？，"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #3 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6647`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据知识库，Grafana dashboard 变量模板怎么配置？，"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #4 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`8`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6464`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"根据知识库，Grafana dashboard 变量模板怎么配置？，"}]`
      - preview: ### 常见错误 1. **数据源连接失败** - 检查Prometheus服务状态 - 验证网络连接 - 确认认证配置 2. **查询语法错误** - 验证PromQL语法 - 检查指标名称拼写 - 确认标签选择器 3. **数据缺失** - 检查时间范围设置 - 验证指标采集状态 - 确认数据保留策略 ### 故障排查步骤 1. **验证数据源** ``...
  - documents:
    - #1 score=`0.0212`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6647`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`1`, rerankScore=`0.0212`, rerankFusionScore=`0.0164`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0212`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据知识库，Grafana dashboard 变量模板怎么配置？，"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #2 score=`0.0210`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6726`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`0.0210`, rerankFusionScore=`0.0161`, queryFusionBoostScore=`0.0049`, fusionAwareScore=`0.0210`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据知识库，Grafana dashboard 变量模板怎么配置？，"}]`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #3 score=`0.0206`, source=`grafana-mcp-tools-guide.md`, chunk=`8`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6464`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`3`, rerankScore=`0.0206`, rerankFusionScore=`0.0159`, queryFusionBoostScore=`0.0047`, fusionAwareScore=`0.0206`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"根据知识库，Grafana dashboard 变量模板怎么配置？，"}]`
      - preview: ### 常见错误 1. **数据源连接失败** - 检查Prometheus服务状态 - 验证网络连接 - 确认认证配置 2. **查询语法错误** - 验证PromQL语法 - 检查指标名称拼写 - 确认标签选择器 3. **数据缺失** - 检查时间范围设置 - 验证指标采集状态 - 确认数据保留策略 ### 故障排查步骤 1. **验证数据源** ``...
    - #4 score=`0.0205`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6648`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`4`, rerankScore=`0.0205`, rerankFusionScore=`0.0156`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0205`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据知识库，Grafana dashboard 变量模板怎么配置？，"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...

answer:

```text
根据当前知识库的内容，并未包含关于 Grafana dashboard 变量模板配置的说明。现有知识主要围绕 Grafana MCP 工具的使用（如数据源列表、PromQL 查询等），暂未涉及 dashboard 模板变量的配置方法。

如果需要，我可以为您提供 Grafana 变量模板的通用配置思路，或建议查阅 Grafana 官方文档。
```

### RAG-08 - weak_related

- question: 根据知识库，Alertmanager 的 silence 应该怎么创建？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `4706`
- score_mode: `manual`
- expected_points:
  - 知识库未涉及 Alertmanager silence
  - 无法基于当前知识库回答

retrieval:
- event #1
  - retrieved_document_count: `0`
  - retrieval_empty: `true`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `— .. —`
  - context_selected: `0` / dropped: `0` / truncated: `false`
  - context_chars: actual `61` / max `6000`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `0` / final `0`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `FUSION_AWARE_RERANK_RRF` / text `根据知识库，Alertmanager 的 silence 应该怎么创建？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: mode `PASSTHROUGH` / variants `1` / texts `根据知识库，Alertmanager 的 silence 应该怎么创建？，`

answer:

```text
根据当前知识库的检索结果，没有找到与 Alertmanager 的 silence 创建相关的可用信息，因此我无法基于知识库为您提供具体的创建步骤。如需进一步协助，建议查阅官方文档或提供更多背景信息。
```

### RAG-09 - paraphrase

- question: 我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？
- should_answer: `true`
- expected_source_section: 标准监控分析流程 / 数据源检查
- completed: `true`
- duration_ms: `8327`
- score_mode: `manual`
- expected_points:
  - grafana/list_datasources
  - 确认监控数据来源
  - 数据源检查

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0203 .. 0.0213`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2310` / max `6000`
  - rerank: applied `true` / mode `FUSION_AWARE_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `FUSION_AWARE_RERANK_RRF` / text `我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: mode `PASSTHROUGH` / variants `1` / texts `我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.8186`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，"}]`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #2 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6867`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #3 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6820`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #4 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6810`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
  - documents:
    - #1 score=`0.0213`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.8186`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0213`, rerankFusionScore=`0.0164`, queryFusionBoostScore=`0.0049`, fusionAwareScore=`0.0213`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，"}]`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #2 score=`0.0209`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6820`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`0.0209`, rerankFusionScore=`0.0161`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0209`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #3 score=`0.0207`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6867`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`3`, rerankScore=`0.0207`, rerankFusionScore=`0.0159`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0207`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #4 score=`0.0203`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6810`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0203`, rerankFusionScore=`0.0156`, queryFusionBoostScore=`0.0047`, fusionAwareScore=`0.0203`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...

answer:

```text
要确认监控数据来源，应该调用 **`grafana/list_datasources`** 工具。

这个工具会列出 Grafana 中配置的所有数据源（如 Prometheus、InfluxDB 等），并返回它们的名称、类型、URL 等信息，让你明确当前可以从哪些来源获取监控数据。
```

### RAG-10 - paraphrase

- question: 如果我要查内存压力，知识库给出的查询思路是什么？
- should_answer: `true`
- expected_source_section: 内存使用率查询 / 内存数据解释
- completed: `true`
- duration_ms: `14741`
- score_mode: `literal`
- expected_points:
  - [x] node_memory_MemAvailable_bytes
  - [x] node_memory_MemTotal_bytes
  - [x] 正常范围
  - [ ] 警告范围
  - [x] 危险范围

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0359 .. 0.0366`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2044` / max `6000`
  - rerank: applied `true` / mode `FUSION_AWARE_RERANK_RRF` / candidates `7` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `FUSION_AWARE_RERANK_RRF` / text `如果我要查内存压力，知识库给出的查询思路是什么？，<br>内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: mode `HEURISTIC_MULTI_QUERY` / variants `2` / texts `如果我要查内存压力，知识库给出的查询思路是什么？， \| 内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`
  - pre_rerank_documents:
    - #1 score=`0.0313`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6684`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0313`, queryFusionRank=`1`, queryVariantHitCount=`2`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":4,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"},{"index":2,"rank":4,"text":"内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #2 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6819`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #3 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7815`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`1`, queryVariantText=`内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`, queryFusionScore=`0.0164`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":1,"text":"内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围"}]`
      - preview: - **危险范围**: 95-100% ### 网络数据解释 - **单位**: bps (bits per second) - **正常范围**: 根据网络带宽确定 - **异常指标**: 突然的流量峰值或持续的高流量 ## 报告生成模板 ### 系统健康报告模板 ```markdown ## 系统运行状态分析报告 ### 📊 监控数据概览 - **监控时...
    - #4 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6741`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"}]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #5 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7290`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`2`, queryVariantText=`内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`, queryFusionScore=`0.0161`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":2,"text":"内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围"}]`
      - preview: # 磁盘使用增长趋势 predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) ``` ## 数据解释和分析标准 ### CPU数据解释 - **数值范围**: 0-100% - **正常范围**: 0-70% - **警告范围**: 70-90% - **危险范...
    - #6 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6692`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`6`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`6`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"}]`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...
    - #7 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`7`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6813`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`7`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`, queryFusionScore=`0.0159`, queryFusionRank=`7`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":3,"text":"内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围"}]`
      - preview: - **当前使用率**: {memory_usage}% - **可用内存**: {memory_available}GB - **状态评估**: {memory_status} - **使用趋势**: {memory_trend} ### 💿 磁盘使用情况 - **使用率**: {disk_usage}% - **可用空间**: {disk_availab...
  - documents:
    - #1 score=`0.0366`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6819`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`1`, rerankScore=`0.0366`, rerankFusionScore=`0.0318`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0366`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`2`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":5}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #2 score=`0.0365`, source=`grafana-mcp-tools-guide.md`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7815`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`0.0365`, rerankFusionScore=`0.0318`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0365`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`2`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":2}]`, queryVariantIndex=`2`, queryVariantRank=`1`, queryVariantText=`内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`, queryFusionScore=`0.0164`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":1,"text":"内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围"}]`
      - preview: - **危险范围**: 95-100% ### 网络数据解释 - **单位**: bps (bits per second) - **正常范围**: 根据网络带宽确定 - **异常指标**: 突然的流量峰值或持续的高流量 ## 报告生成模板 ### 系统健康报告模板 ```markdown ## 系统运行状态分析报告 ### 📊 监控数据概览 - **监控时...
    - #3 score=`0.0365`, source=`grafana-mcp-tools-guide.md`, chunk=`7`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6813`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`7`, rerankRank=`3`, rerankScore=`0.0365`, rerankFusionScore=`0.0320`, queryFusionBoostScore=`0.0045`, fusionAwareScore=`0.0365`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`2`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":3}]`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`, queryFusionScore=`0.0159`, queryFusionRank=`7`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":3,"text":"内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围"}]`
      - preview: - **当前使用率**: {memory_usage}% - **可用内存**: {memory_available}GB - **状态评估**: {memory_status} - **使用趋势**: {memory_trend} ### 💿 磁盘使用情况 - **使用率**: {disk_usage}% - **可用空间**: {disk_availab...
    - #4 score=`0.0359`, source=`grafana-mcp-tools-guide.md`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7290`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`5`, rerankRank=`4`, rerankScore=`0.0359`, rerankFusionScore=`0.0313`, queryFusionBoostScore=`0.0046`, fusionAwareScore=`0.0359`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`2`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2]`, rerankVariantHits=`[{"index":1,"rank":7},{"index":2,"rank":1}]`, queryVariantIndex=`2`, queryVariantRank=`2`, queryVariantText=`内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围`, queryFusionScore=`0.0161`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":2,"text":"内存 使用率 数据解释 判断标准 阈值 正常范围 警告范围 危险范围"}]`
      - preview: # 磁盘使用增长趋势 predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) ``` ## 数据解释和分析标准 ### CPU数据解释 - **数值范围**: 0-100% - **正常范围**: 0-70% - **警告范围**: 70-90% - **危险范...

answer:

```text
根据知识库，查询内存压力的思路是通过 Prometheus 查询计算当前内存使用率，具体查询语句为：

```
(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
```

这个查询会返回内存使用百分比，用于评估内存压力状况[1]。查询结果可以依据以下标准解读：0-80% 为正常范围，80-95% 为警告，95-100% 为危险范围[4]。
```

### RAG-11 - control_plaintext_cn_answerable

- question: 控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？
- should_answer: `true`
- expected_source_section: control-cn-low-newline.txt / 容量治理控制组
- completed: `true`
- duration_ms: `6278`
- score_mode: `literal`
- expected_points:
  - [x] 连续二十一天未访问
  - [x] ARCHIVE-CN-7319

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0205 .. 0.0213`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `1061` / max `6000`
  - rerank: applied `true` / mode `FUSION_AWARE_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `FUSION_AWARE_RERANK_RRF` / text `控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: mode `PASSTHROUGH` / variants `1` / texts `控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`control-cn-low-newline.txt`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7763`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，"}]`
      - preview: �过移动。若某条明细已经进入候选池但还没有搬迁，用户查询仍然走主库。若某条明细已经搬迁且摘要索引校验通过，用户查询可以走归档读路径。若摘要索引存在但正文块缺失，系统必须返回内部错误并触发隔离，而不是把问题解释成知识不存在。控制组还刻意加入一个容易误读的术语：容量观察期。容量观察期用于评估增长趋势，长度为十天，但它不参与冷数据迁移判断。冷数据迁移阈值仍然是连续...
    - #2 score=`0.0161`, source=`control-cn-low-newline.txt`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7298`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，"}]`
      - preview: 过异步索引回填，冷数据只保留查询摘要和审计索引。这里的冷数据迁移阈值被明确写成：连续二十一天未访问。这个阈值只用于判断明细是否进入候选池，不等于删除条件，也不等于归档任务立即执行。为了避免误判，系统还会检查最近七天是否存在人工置顶、账单核对、客服补单、风控复查等保护标记。七天检查窗口只是安全保护项，不是冷数据迁移阈值。另一个常见误解是把十四天低频访问当成归档...
    - #3 score=`0.0159`, source=`control-cn-low-newline.txt`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7277`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，"}]`
      - preview: �队列之前，调度器还会读取业务线的保留策略，确认租户等级、合规标签、冻结状态和最近一次补偿任务结果。为了便于回放，本次控制组把归档窗口编号固定为 ARCHIVE-CN-7319。这个编号应该出现在检索答案中，因为它是文档里唯一一个用于识别迁移窗口的字符串。窗口编号不是审批单号，也不是工单编号。文档没有指定审批人姓名，没有给出审批系统地址，也没有要求用户在回答...
    - #4 score=`0.0156`, source=`control-cn-low-newline.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7175`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，"}]`
      - preview: 弱相关问题更隐蔽，因为用户可能问审批人、成本中心、云厂商、负责人电话、紧急群组这些文档未覆盖的信息。控制组要求系统在这些问题上明确说明文档未提供，不应编造人名、部门、平台名称或外部链接。本文档只说明容量治理策略、归档窗口编号、冷数据迁移阈值、单批提交上限、隔离队列重放次数和回滚观察期。本文档没有说明审批人是谁，没有说明云厂商是谁，没有说明预算归属，也没有说明...
  - documents:
    - #1 score=`0.0213`, source=`control-cn-low-newline.txt`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7763`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0213`, rerankFusionScore=`0.0164`, queryFusionBoostScore=`0.0049`, fusionAwareScore=`0.0213`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，"}]`
      - preview: �过移动。若某条明细已经进入候选池但还没有搬迁，用户查询仍然走主库。若某条明细已经搬迁且摘要索引校验通过，用户查询可以走归档读路径。若摘要索引存在但正文块缺失，系统必须返回内部错误并触发隔离，而不是把问题解释成知识不存在。控制组还刻意加入一个容易误读的术语：容量观察期。容量观察期用于评估增长趋势，长度为十天，但它不参与冷数据迁移判断。冷数据迁移阈值仍然是连续...
    - #2 score=`0.0209`, source=`control-cn-low-newline.txt`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7277`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`0.0209`, rerankFusionScore=`0.0161`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0209`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，"}]`
      - preview: �队列之前，调度器还会读取业务线的保留策略，确认租户等级、合规标签、冻结状态和最近一次补偿任务结果。为了便于回放，本次控制组把归档窗口编号固定为 ARCHIVE-CN-7319。这个编号应该出现在检索答案中，因为它是文档里唯一一个用于识别迁移窗口的字符串。窗口编号不是审批单号，也不是工单编号。文档没有指定审批人姓名，没有给出审批系统地址，也没有要求用户在回答...
    - #3 score=`0.0206`, source=`control-cn-low-newline.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7175`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`3`, rerankScore=`0.0206`, rerankFusionScore=`0.0159`, queryFusionBoostScore=`0.0047`, fusionAwareScore=`0.0206`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，"}]`
      - preview: 弱相关问题更隐蔽，因为用户可能问审批人、成本中心、云厂商、负责人电话、紧急群组这些文档未覆盖的信息。控制组要求系统在这些问题上明确说明文档未提供，不应编造人名、部门、平台名称或外部链接。本文档只说明容量治理策略、归档窗口编号、冷数据迁移阈值、单批提交上限、隔离队列重放次数和回滚观察期。本文档没有说明审批人是谁，没有说明云厂商是谁，没有说明预算归属，也没有说明...
    - #4 score=`0.0205`, source=`control-cn-low-newline.txt`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7298`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`4`, rerankScore=`0.0205`, rerankFusionScore=`0.0156`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0205`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，"}]`
      - preview: 过异步索引回填，冷数据只保留查询摘要和审计索引。这里的冷数据迁移阈值被明确写成：连续二十一天未访问。这个阈值只用于判断明细是否进入候选池，不等于删除条件，也不等于归档任务立即执行。为了避免误判，系统还会检查最近七天是否存在人工置顶、账单核对、客服补单、风控复查等保护标记。七天检查窗口只是安全保护项，不是冷数据迁移阈值。另一个常见误解是把十四天低频访问当成归档...

answer:

```text
根据控制组文档，冷数据迁移阈值是**连续二十一天未访问**（[4][1]），归档窗口编号固定为 **ARCHIVE-CN-7319**（[2][1]）。
```

### RAG-12 - control_plaintext_cn_weak_related

- question: 控制组文档指定的归档审批人是谁？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `5372`
- score_mode: `manual`
- expected_points:
  - 文档没有指定审批人
  - 无法基于当前知识库回答

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0203 .. 0.0213`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `1045` / max `6000`
  - rerank: applied `true` / mode `FUSION_AWARE_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `FUSION_AWARE_RERANK_RRF` / text `控制组文档指定的归档审批人是谁？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: mode `PASSTHROUGH` / variants `1` / texts `控制组文档指定的归档审批人是谁？，`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`control-cn-low-newline.txt`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7169`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`控制组文档指定的归档审批人是谁？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"控制组文档指定的归档审批人是谁？，"}]`
      - preview: �队列之前，调度器还会读取业务线的保留策略，确认租户等级、合规标签、冻结状态和最近一次补偿任务结果。为了便于回放，本次控制组把归档窗口编号固定为 ARCHIVE-CN-7319。这个编号应该出现在检索答案中，因为它是文档里唯一一个用于识别迁移窗口的字符串。窗口编号不是审批单号，也不是工单编号。文档没有指定审批人姓名，没有给出审批系统地址，也没有要求用户在回答...
    - #2 score=`0.0161`, source=`control-cn-low-newline.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6741`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`控制组文档指定的归档审批人是谁？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"控制组文档指定的归档审批人是谁？，"}]`
      - preview: 弱相关问题更隐蔽，因为用户可能问审批人、成本中心、云厂商、负责人电话、紧急群组这些文档未覆盖的信息。控制组要求系统在这些问题上明确说明文档未提供，不应编造人名、部门、平台名称或外部链接。本文档只说明容量治理策略、归档窗口编号、冷数据迁移阈值、单批提交上限、隔离队列重放次数和回滚观察期。本文档没有说明审批人是谁，没有说明云厂商是谁，没有说明预算归属，也没有说明...
    - #3 score=`0.0159`, source=`control-cn-low-newline.txt`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6687`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`控制组文档指定的归档审批人是谁？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"控制组文档指定的归档审批人是谁？，"}]`
      - preview: �过移动。若某条明细已经进入候选池但还没有搬迁，用户查询仍然走主库。若某条明细已经搬迁且摘要索引校验通过，用户查询可以走归档读路径。若摘要索引存在但正文块缺失，系统必须返回内部错误并触发隔离，而不是把问题解释成知识不存在。控制组还刻意加入一个容易误读的术语：容量观察期。容量观察期用于评估增长趋势，长度为十天，但它不参与冷数据迁移判断。冷数据迁移阈值仍然是连续...
    - #4 score=`0.0156`, source=`control-cn-low-newline.txt`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6475`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`控制组文档指定的归档审批人是谁？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"控制组文档指定的归档审批人是谁？，"}]`
      - preview: 归档执行过程分为预扫描、冻结快照、分批搬迁、校验回填和灰度释放五段。预扫描阶段只读取元数据，不移动正文。冻结快照阶段会记录候选明细的主键范围、最近更新时间、摘要哈希和原始分区。分批搬迁阶段按租户和业务类型做小批量提交，每批上限为三千条。三千条是单次提交上限，不是迁移阈值。校验回填阶段会对摘要索引、审计索引和归档位置做三方比对。灰度释放阶段会在查询网关里开启双...
  - documents:
    - #1 score=`0.0213`, source=`control-cn-low-newline.txt`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7169`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0213`, rerankFusionScore=`0.0164`, queryFusionBoostScore=`0.0049`, fusionAwareScore=`0.0213`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`控制组文档指定的归档审批人是谁？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"控制组文档指定的归档审批人是谁？，"}]`
      - preview: �队列之前，调度器还会读取业务线的保留策略，确认租户等级、合规标签、冻结状态和最近一次补偿任务结果。为了便于回放，本次控制组把归档窗口编号固定为 ARCHIVE-CN-7319。这个编号应该出现在检索答案中，因为它是文档里唯一一个用于识别迁移窗口的字符串。窗口编号不是审批单号，也不是工单编号。文档没有指定审批人姓名，没有给出审批系统地址，也没有要求用户在回答...
    - #2 score=`0.0210`, source=`control-cn-low-newline.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6741`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0210`, rerankFusionScore=`0.0161`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0210`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`控制组文档指定的归档审批人是谁？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"控制组文档指定的归档审批人是谁？，"}]`
      - preview: 弱相关问题更隐蔽，因为用户可能问审批人、成本中心、云厂商、负责人电话、紧急群组这些文档未覆盖的信息。控制组要求系统在这些问题上明确说明文档未提供，不应编造人名、部门、平台名称或外部链接。本文档只说明容量治理策略、归档窗口编号、冷数据迁移阈值、单批提交上限、隔离队列重放次数和回滚观察期。本文档没有说明审批人是谁，没有说明云厂商是谁，没有说明预算归属，也没有说明...
    - #3 score=`0.0206`, source=`control-cn-low-newline.txt`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6687`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0206`, rerankFusionScore=`0.0159`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0206`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`控制组文档指定的归档审批人是谁？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"控制组文档指定的归档审批人是谁？，"}]`
      - preview: �过移动。若某条明细已经进入候选池但还没有搬迁，用户查询仍然走主库。若某条明细已经搬迁且摘要索引校验通过，用户查询可以走归档读路径。若摘要索引存在但正文块缺失，系统必须返回内部错误并触发隔离，而不是把问题解释成知识不存在。控制组还刻意加入一个容易误读的术语：容量观察期。容量观察期用于评估增长趋势，长度为十天，但它不参与冷数据迁移判断。冷数据迁移阈值仍然是连续...
    - #4 score=`0.0203`, source=`control-cn-low-newline.txt`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6475`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0203`, rerankFusionScore=`0.0156`, queryFusionBoostScore=`0.0047`, fusionAwareScore=`0.0203`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`控制组文档指定的归档审批人是谁？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"控制组文档指定的归档审批人是谁？，"}]`
      - preview: 归档执行过程分为预扫描、冻结快照、分批搬迁、校验回填和灰度释放五段。预扫描阶段只读取元数据，不移动正文。冻结快照阶段会记录候选明细的主键范围、最近更新时间、摘要哈希和原始分区。分批搬迁阶段按租户和业务类型做小批量提交，每批上限为三千条。三千条是单次提交上限，不是迁移阈值。校验回填阶段会对摘要索引、审计索引和归档位置做三方比对。灰度释放阶段会在查询网关里开启双...

answer:

```text
根据控制组文档的内容，文档中没有指定归档审批人的姓名。文档明确说明未提供审批人信息，也不应编造相关组织架构信息。
```

### RAG-13 - control_plaintext_en_answerable

- question: According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?
- should_answer: `true`
- expected_source_section: control-en-long-paragraph.txt / webhook fallback runbook
- completed: `true`
- duration_ms: `6478`
- score_mode: `literal`
- expected_points:
  - [x] silver-river-42
  - [x] seven attempts within eighteen minutes

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0205 .. 0.0212`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `5545` / max `6000`
  - rerank: applied `true` / mode `FUSION_AWARE_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `FUSION_AWARE_RERANK_RRF` / text `According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: mode `PASSTHROUGH` / variants `1` / texts `According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`control-en-long-paragraph.txt`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7906`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，"}]`
      - preview: It does not mention a region, a vendor ticket number, or a private dashboard link. It only states that silver-river-42 is the fallback queue name, that the fallback retry budget is...
    - #2 score=`0.0161`, source=`control-en-long-paragraph.txt`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7826`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，"}]`
      - preview: The compact envelope intentionally excludes the original request body after validation so that repeated retries do not increase storage pressure. Once fallback mode is enabled, the...
    - #3 score=`0.0159`, source=`control-en-long-paragraph.txt`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7743`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，"}]`
      - preview: This control document is written as a long plain text incident runbook for retrieval testing. It is not a production policy and it intentionally avoids headings, lists, tables, cod...
    - #4 score=`0.0156`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7632`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，"}]`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...
  - documents:
    - #1 score=`0.0212`, source=`control-en-long-paragraph.txt`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7743`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`1`, rerankScore=`0.0212`, rerankFusionScore=`0.0164`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0212`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，"}]`
      - preview: This control document is written as a long plain text incident runbook for retrieval testing. It is not a production policy and it intentionally avoids headings, lists, tables, cod...
    - #2 score=`0.0210`, source=`control-en-long-paragraph.txt`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7906`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`0.0210`, rerankFusionScore=`0.0161`, queryFusionBoostScore=`0.0049`, fusionAwareScore=`0.0210`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，"}]`
      - preview: It does not mention a region, a vendor ticket number, or a private dashboard link. It only states that silver-river-42 is the fallback queue name, that the fallback retry budget is...
    - #3 score=`0.0206`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7632`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`3`, rerankScore=`0.0206`, rerankFusionScore=`0.0159`, queryFusionBoostScore=`0.0047`, fusionAwareScore=`0.0206`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，"}]`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...
    - #4 score=`0.0205`, source=`control-en-long-paragraph.txt`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7826`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`4`, rerankScore=`0.0205`, rerankFusionScore=`0.0156`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0205`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，"}]`
      - preview: The compact envelope intentionally excludes the original request body after validation so that repeated retries do not increase storage pressure. Once fallback mode is enabled, the...

answer:

```text
According to the control document, the fallback queue name is **silver-river-42**, and the retry budget for the fallback path is **seven attempts within eighteen minutes** [1][2][3].
```

### RAG-14 - control_plaintext_en_weak_related

- question: According to the control document, which cloud provider hosts the fallback queue?
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `7500`
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
  - score_range: `0.0203 .. 0.0212`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `5550` / max `6000`
  - rerank: applied `true` / mode `FUSION_AWARE_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `FUSION_AWARE_RERANK_RRF` / text `According to the control document, which cloud provider hosts the fallback queue?，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: mode `PASSTHROUGH` / variants `1` / texts `According to the control document, which cloud provider hosts the fallback queue?，`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`control-en-long-paragraph.txt`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7799`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"According to the control document, which cloud provider hosts the fallback queue?，"}]`
      - preview: It does not mention a region, a vendor ticket number, or a private dashboard link. It only states that silver-river-42 is the fallback queue name, that the fallback retry budget is...
    - #2 score=`0.0161`, source=`control-en-long-paragraph.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7513`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"According to the control document, which cloud provider hosts the fallback queue?，"}]`
      - preview: This note is present to add realistic language around the anchor silver-river-42 without changing the anchor itself. The queue name remains silver-river-42. The document still does...
    - #3 score=`0.0159`, source=`control-en-long-paragraph.txt`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7453`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"According to the control document, which cloud provider hosts the fallback queue?，"}]`
      - preview: This control document is written as a long plain text incident runbook for retrieval testing. It is not a production policy and it intentionally avoids headings, lists, tables, cod...
    - #4 score=`0.0156`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7432`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"According to the control document, which cloud provider hosts the fallback queue?，"}]`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...
  - documents:
    - #1 score=`0.0212`, source=`control-en-long-paragraph.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7513`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`1`, rerankScore=`0.0212`, rerankFusionScore=`0.0164`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0212`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"According to the control document, which cloud provider hosts the fallback queue?，"}]`
      - preview: This note is present to add realistic language around the anchor silver-river-42 without changing the anchor itself. The queue name remains silver-river-42. The document still does...
    - #2 score=`0.0210`, source=`control-en-long-paragraph.txt`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7799`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`0.0210`, rerankFusionScore=`0.0161`, queryFusionBoostScore=`0.0049`, fusionAwareScore=`0.0210`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"According to the control document, which cloud provider hosts the fallback queue?，"}]`
      - preview: It does not mention a region, a vendor ticket number, or a private dashboard link. It only states that silver-river-42 is the fallback queue name, that the fallback retry budget is...
    - #3 score=`0.0206`, source=`control-en-long-paragraph.txt`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7453`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0206`, rerankFusionScore=`0.0159`, queryFusionBoostScore=`0.0048`, fusionAwareScore=`0.0206`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"According to the control document, which cloud provider hosts the fallback queue?，"}]`
      - preview: This control document is written as a long plain text incident runbook for retrieval testing. It is not a production policy and it intentionally avoids headings, lists, tables, cod...
    - #4 score=`0.0203`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7432`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0203`, rerankFusionScore=`0.0156`, queryFusionBoostScore=`0.0047`, fusionAwareScore=`0.0203`, fusionAwareWeight=`0.3000`, rerankApplied=`True`, rerankMode=`FUSION_AWARE_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`1`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1]`, rerankVariantHits=`[{"index":1,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"According to the control document, which cloud provider hosts the fallback queue?，"}]`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...

answer:

```text
The control document explicitly states that it does not identify a cloud provider for the fallback queue, so this question cannot be answered from the provided material [1][2].
```
