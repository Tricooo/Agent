# RAG Eval Result

- generated_at: `2026-05-14 19:16:42`
- api_url: `http://127.0.0.1:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。
>
> `retrieved` / `score` / `empty` 三列的 `—` 表示**没拿到成功的 ChatResponse metadata**，不等价于"无检索"。当前实现把 retrieval SSE 帧放在 `.call().chatResponse()` 返回之后才发，所以 LLM 调用失败时（即使 RAG 检索本身成功）三列都会是 `—`。要区分"检索失败"和"生成失败"，对照 `error` 列 / details 区 / backend log。
>
> Details 区的 `documents` 会展开 top-K chunk attribution；HYBRID 模式下 `score` 是 RRF score，原始向量分与关键词分分别看 `vectorScore` / `keywordScore`；真实 rerank 分数看 `rerankScore`；keyword 分支未参与时看 `keywordSkippedReason`。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-01 | answerable | true | 10273 | true | 4 | 0.0159 .. 0.0328 | false | manual | — | 根据知识库中的说明，`grafana/list_datasources` 工具的主要作用是： - **列出所有可用的数据源**：获取 Grafana 中配置的全部数据源，并返回它们的基本信息，如 ID、名称、类型、URL 等 [1]。 - ... |  |
| RAG-02 | answerable | true | 8747 | true | 4 | 0.0156 .. 0.0323 | false | 4/4 |  | 根据提供的文档，`grafana/query_prometheus` 工具支持以下参数： - **`query`** (必需)：PromQL 查询语句。 - **`start`** (可选)：查询的开始时间。 - **`end`** (可选... |  |
| RAG-03 | answerable_formula | true | 19023 | true | 4 | 0.0156 .. 0.0164 | false | 3/3 |  | 根据知识库内容，CPU 使用率的 PromQL 查询示例如下（见[1]）： - **整体 CPU 使用率** `100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100... |  |
| RAG-04 | answerable_deep_section | true | 9332 | true | 4 | 0.0156 .. 0.0164 | false | 3/3 |  | 根据您提供的上下文信息 [1]，磁盘 7 天增长趋势预测应使用以下 PromQL： ```promql predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*... |  |
| RAG-05 | answerable_long_context | true | 13323 | true | 4 | 0.0156 .. 0.0164 | false | 5/5 |  | 根据提供的 Grafana 监控工具指南，系统健康报告模板包含以下主要部分（见[1]和[2]）： 1. **📊 监控数据概览** - 监控时间范围、数据源信息、分析时间。 2. **🖥️ CPU 使用情况** - 平均使用率、峰值使用率、状... |  |
| RAG-06 | unanswerable | true | 4667 | false | 0 | — | true | manual | — | 根据当前提供的上下文，知识库中没有检索到与 MySQL InnoDB MVCC undo log 版本链相关的可用内容，因此无法基于知识库回答您的问题。 |  |
| RAG-07 | weak_related | true | 7130 | false | 4 | 0.0156 .. 0.0164 | false | manual | — | 根据目前提供的知识库内容，主要涵盖的是 Grafana MCP 工具的使用（如列出数据源、执行 Prometheus 查询等），并没有包含关于 **Grafana dashboard 变量模板配置** 的相关信息，所以我无法从现有资料中回答... |  |
| RAG-08 | weak_related | true | 4544 | false | 0 | — | true | manual | — | 抱歉，当前的知识库中没有收录关于 Alertmanager 的 silence 创建方法的内容，我无法基于现有资料为您提供解答。建议您查阅 Alertmanager 的官方文档或相关社区资源。 |  |
| RAG-09 | paraphrase | true | 8037 | true | 4 | 0.0156 .. 0.0164 | false | manual | — | 要确认监控数据的来源，应调用 **`grafana/list_datasources`** 工具[1]。 这个工具会返回 Grafana 中已配置的所有数据源列表，包括它们的名称、类型和 URL 等信息，帮助你验证 Prometheus（或... |  |
| RAG-10 | paraphrase | true | 9522 | true | 4 | 0.0156 .. 0.0164 | false | 2/5 | 正常范围, 警告范围, 危险范围 | 根据知识库，查询内存压力的思路主要基于**内存使用率**指标。具体查询语句为： ```promql (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * ... |  |
| RAG-11 | control_plaintext_cn_answerable | true | 7977 | true | 4 | 0.0156 .. 0.0164 | false | 2/2 |  | 根据控制组文档，冷数据迁移阈值是 **连续二十一天未访问**，归档窗口编号是 **ARCHIVE-CN-7319**[1][2][4]。 |  |
| RAG-12 | control_plaintext_cn_weak_related | true | 5380 | false | 4 | 0.0156 .. 0.0164 | false | manual | — | 根据文档内容，**未指定归档审批人**。文档明确说明，该控制组文档不包含审批人姓名、审批系统地址或组织架构信息，因此无法提供具体审批人。 |  |
| RAG-13 | control_plaintext_en_answerable | true | 8917 | true | 4 | 0.0156 .. 0.0164 | false | 2/2 |  | Based on the control document, the fallback queue name is **silver-river-42** and the retry budget for that fallback pat... |  |
| RAG-14 | control_plaintext_en_weak_related | true | 7368 | false | 4 | 0.0156 .. 0.0164 | false | manual | — | Based on the provided control document, the cloud provider hosting the fallback queue is not identified. The document ex... |  |

## Details

### RAG-01 - answerable

- question: 根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。
- should_answer: `true`
- expected_source_section: grafana/list_datasources
- completed: `true`
- duration_ms: `10273`
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
  - score_range: `0.0159 .. 0.0328`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2252` / max `6000`
  - rerank: applied `true` / mode `LOCAL_BGE` / candidates `5` / final `4`
  - documents:
    - #1 score=`0.0328`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0328`, vectorRank=`1`, vectorScore=`0.8057`, keywordRank=`1`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`2.3371`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #2 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6601`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`2`, rerankScore=`0.9759`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #3 score=`0.0318`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0318`, vectorRank=`4`, vectorScore=`0.6561`, keywordRank=`2`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`3`, rerankScore=`0.1608`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #4 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`8`, retrievalSource=`KEYWORD`, rrfScore=`0.0159`, vectorRank=`—`, vectorScore=`—`, keywordRank=`3`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`5`, rerankRank=`4`, rerankScore=`0.1596`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: ### 常见错误 1. **数据源连接失败** - 检查Prometheus服务状态 - 验证网络连接 - 确认认证配置 2. **查询语法错误** - 验证PromQL语法 - 检查指标名称拼写 - 确认标签选择器 3. **数据缺失** - 检查时间范围设置 - 验证指标采集状态 - 确认数据保留策略 ### 故障排查步骤 1. **验证数据源** ``...

answer:

```text
根据知识库中的说明，`grafana/list_datasources` 工具的主要作用是：

- **列出所有可用的数据源**：获取 Grafana 中配置的全部数据源，并返回它们的基本信息，如 ID、名称、类型、URL 等 [1]。
- **用于确认数据源可用性**：在系统分析前检查数据源状态，特别是确认 Prometheus 数据源是否可用 [1]。
- **支持多数据源环境管理**：帮助了解监控数据的来源，便于后续的多数据源管理和分析 [1]。

该工具返回的数据示例包含数据源的 ID、名称（如 “Prometheus”）、类型（如 “prometheus”）、URL 和访问方式等字段 [1]。
```

### RAG-02 - answerable

- question: `grafana/query_prometheus` 支持哪些参数？
- should_answer: `true`
- expected_source_section: grafana/query_prometheus 参数说明
- completed: `true`
- duration_ms: `8747`
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
  - score_range: `0.0156 .. 0.0323`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2305` / max `6000`
  - rerank: applied `true` / mode `LOCAL_BGE` / candidates `5` / final `4`
  - documents:
    - #1 score=`0.0323`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0323`, vectorRank=`1`, vectorScore=`0.8033`, keywordRank=`3`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`1`, rerankScore=`3.6264`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #2 score=`0.0323`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0323`, vectorRank=`3`, vectorScore=`0.6989`, keywordRank=`1`, keywordScore=`0.3000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`2.5366`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #3 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`KEYWORD`, rrfScore=`0.0156`, vectorRank=`—`, vectorScore=`—`, keywordRank=`4`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`5`, rerankRank=`3`, rerankScore=`0.3075`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #4 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7107`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`-0.3230`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...

answer:

```text
根据提供的文档，`grafana/query_prometheus` 工具支持以下参数：

- **`query`** (必需)：PromQL 查询语句。
- **`start`** (可选)：查询的开始时间。
- **`end`** (可选)：查询的结束时间。
- **`step`** (可选)：查询的步长。

[1]
```

### RAG-03 - answerable_formula

- question: 知识库里 CPU 使用率的 PromQL 示例是什么？
- should_answer: `true`
- expected_source_section: CPU使用率查询
- completed: `true`
- duration_ms: `19023`
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
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2163` / max `6000`
  - rerank: applied `true` / mode `LOCAL_BGE` / candidates `4` / final `4`
  - documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7252`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`2.9097`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #2 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6863`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`-0.9454`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #3 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6745`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`3`, rerankScore=`-2.1477`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #4 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6976`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`4`, rerankScore=`-6.6411`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...

answer:

```text
根据知识库内容，CPU 使用率的 PromQL 查询示例如下（见[1]）：

- **整体 CPU 使用率**
  `100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)`

- **按实例分组的 CPU 使用率**
  `100 - (avg by (instance)(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)`

这两个查询都基于 `node_cpu_seconds_total` 指标，通过减去空闲(idle)比例计算 CPU 占用率，默认使用过去 5 分钟的数据进行速率计算。

> 另外，在自动化分析流程中（见[3]）也给出了调整时间窗口为 1 小时的示例：
> `100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100)`。
```

### RAG-04 - answerable_deep_section

- question: 磁盘 7 天增长趋势预测用哪个 PromQL？
- should_answer: `true`
- expected_source_section: 趋势预测
- completed: `true`
- duration_ms: `9332`
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
  - documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7406`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.1935`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: # 磁盘使用增长趋势 predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) ``` ## 数据解释和分析标准 ### CPU数据解释 - **数值范围**: 0-100% - **正常范围**: 0-70% - **警告范围**: 70-90% - **危险范...
    - #2 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6563`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`-0.4167`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #3 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6481`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`3`, rerankScore=`-2.8748`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #4 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6639`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`4`, rerankScore=`-3.1441`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...

answer:

```text
根据您提供的上下文信息 [1]，磁盘 7 天增长趋势预测应使用以下 PromQL：

```promql
predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600)
```

该查询会基于过去 24 小时的磁盘可用空间线性变化趋势，预测 7 天（7\*24\*3600 秒）后的可用字节数。
若预测值接近 0，则表明磁盘可能在未来 7 天内耗尽，需提前扩容或清理。
```

### RAG-05 - answerable_long_context

- question: 系统健康报告模板包含哪些部分？
- should_answer: `true`
- expected_source_section: 系统健康报告模板
- completed: `true`
- duration_ms: `13323`
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
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `1985` / max `6000`
  - rerank: applied `true` / mode `LOCAL_BGE` / candidates `4` / final `4`
  - documents:
    - #1 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7172`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`1`, rerankScore=`1.1491`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: - **危险范围**: 95-100% ### 网络数据解释 - **单位**: bps (bits per second) - **正常范围**: 根据网络带宽确定 - **异常指标**: 突然的流量峰值或持续的高流量 ## 报告生成模板 ### 系统健康报告模板 ```markdown ## 系统运行状态分析报告 ### 📊 监控数据概览 - **监控时...
    - #2 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`7`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7195`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`-2.5481`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: - **当前使用率**: {memory_usage}% - **可用内存**: {memory_available}GB - **状态评估**: {memory_status} - **使用趋势**: {memory_trend} ### 💿 磁盘使用情况 - **使用率**: {disk_usage}% - **可用空间**: {disk_availab...
    - #3 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6314`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`-6.3833`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...
    - #4 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6178`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`-7.7330`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...

answer:

```text
根据提供的 Grafana 监控工具指南，系统健康报告模板包含以下主要部分（见[1]和[2]）：

1. **📊 监控数据概览**
   - 监控时间范围、数据源信息、分析时间。

2. **🖥️ CPU 使用情况**
   - 平均使用率、峰值使用率、状态评估、趋势分析。

3. **💾 内存使用情况**
   - 当前使用率、可用内存、状态评估、使用趋势。

4. **💿 磁盘使用情况**
   - 使用率、可用空间、状态评估、空间预警。

5. **🌐 网络流量情况**
   - 接收/发送速率、状态评估、流量模式。

6. **📈 综合评估**
   - 系统健康度、关键发现、优化建议、告警建议。

模板中的 `{xxx}` 均为占位符，在生成报告时需要用实际的监控数据填充。
```

### RAG-06 - unanswerable

- question: 根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `4667`
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

answer:

```text
根据当前提供的上下文，知识库中没有检索到与 MySQL InnoDB MVCC undo log 版本链相关的可用内容，因此无法基于知识库回答您的问题。
```

### RAG-07 - weak_related

- question: 根据知识库，Grafana dashboard 变量模板怎么配置？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `7130`
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
  - documents:
    - #1 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6647`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`1`, rerankScore=`-1.9974`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #2 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6726`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`-2.3690`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #3 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`8`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6464`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`3`, rerankScore=`-3.9344`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: ### 常见错误 1. **数据源连接失败** - 检查Prometheus服务状态 - 验证网络连接 - 确认认证配置 2. **查询语法错误** - 验证PromQL语法 - 检查指标名称拼写 - 确认标签选择器 3. **数据缺失** - 检查时间范围设置 - 验证指标采集状态 - 确认数据保留策略 ### 故障排查步骤 1. **验证数据源** ``...
    - #4 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6648`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`4`, rerankScore=`-4.1469`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...

answer:

```text
根据目前提供的知识库内容，主要涵盖的是 Grafana MCP 工具的使用（如列出数据源、执行 Prometheus 查询等），并没有包含关于 **Grafana dashboard 变量模板配置** 的相关信息，所以我无法从现有资料中回答这个问题。

如果你需要了解变量模板的配置方法，建议查阅 Grafana 官方文档中关于“变量（Variables）”的部分，或提供包含相关内容的其他知识库资料。
```

### RAG-08 - weak_related

- question: 根据知识库，Alertmanager 的 silence 应该怎么创建？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `4544`
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

answer:

```text
抱歉，当前的知识库中没有收录关于 Alertmanager 的 silence 创建方法的内容，我无法基于现有资料为您提供解答。建议您查阅 Alertmanager 的官方文档或相关社区资源。
```

### RAG-09 - paraphrase

- question: 我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？
- should_answer: `true`
- expected_source_section: 标准监控分析流程 / 数据源检查
- completed: `true`
- duration_ms: `8037`
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
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2310` / max `6000`
  - rerank: applied `true` / mode `LOCAL_BGE` / candidates `4` / final `4`
  - documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.8186`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`2.9390`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #2 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6820`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`0.3062`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #3 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6867`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`3`, rerankScore=`-0.0034`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #4 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6810`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`-0.1035`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...

answer:

```text
要确认监控数据的来源，应调用 **`grafana/list_datasources`** 工具[1]。

这个工具会返回 Grafana 中已配置的所有数据源列表，包括它们的名称、类型和 URL 等信息，帮助你验证 Prometheus（或其他监控数据源）是否可用，以及其具体的连接配置[1]。
```

### RAG-10 - paraphrase

- question: 如果我要查内存压力，知识库给出的查询思路是什么？
- should_answer: `true`
- expected_source_section: 内存使用率查询 / 内存数据解释
- completed: `true`
- duration_ms: `9522`
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
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2211` / max `6000`
  - rerank: applied `true` / mode `LOCAL_BGE` / candidates `4` / final `4`
  - documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6819`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`-1.0146`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #2 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6741`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`-4.7510`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #3 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6684`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`3`, rerankScore=`-5.2179`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #4 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6692`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`4`, rerankScore=`-5.9093`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...

answer:

```text
根据知识库，查询内存压力的思路主要基于**内存使用率**指标。具体查询语句为：

```promql
(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
```

该查询通过计算 `(1 - 可用内存/总内存) * 100` 得出当前内存使用百分比，并据此评估内存压力状况[1]。

此外，在深度分析流程中，还可以将内存使用率与 CPU 使用率结合，进行资源关联性分析（计算两者的平均值），以综合判断系统负载压力[2]。
```

### RAG-11 - control_plaintext_cn_answerable

- question: 控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？
- should_answer: `true`
- expected_source_section: control-cn-low-newline.txt / 容量治理控制组
- completed: `true`
- duration_ms: `7977`
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
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `1061` / max `6000`
  - rerank: applied `true` / mode `LOCAL_BGE` / candidates `4` / final `4`
  - documents:
    - #1 score=`0.0164`, source=`control-cn-low-newline.txt`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7763`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`5.8551`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: �过移动。若某条明细已经进入候选池但还没有搬迁，用户查询仍然走主库。若某条明细已经搬迁且摘要索引校验通过，用户查询可以走归档读路径。若摘要索引存在但正文块缺失，系统必须返回内部错误并触发隔离，而不是把问题解释成知识不存在。控制组还刻意加入一个容易误读的术语：容量观察期。容量观察期用于评估增长趋势，长度为十天，但它不参与冷数据迁移判断。冷数据迁移阈值仍然是连续...
    - #2 score=`0.0159`, source=`control-cn-low-newline.txt`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7277`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`4.8933`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: �队列之前，调度器还会读取业务线的保留策略，确认租户等级、合规标签、冻结状态和最近一次补偿任务结果。为了便于回放，本次控制组把归档窗口编号固定为 ARCHIVE-CN-7319。这个编号应该出现在检索答案中，因为它是文档里唯一一个用于识别迁移窗口的字符串。窗口编号不是审批单号，也不是工单编号。文档没有指定审批人姓名，没有给出审批系统地址，也没有要求用户在回答...
    - #3 score=`0.0156`, source=`control-cn-low-newline.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7175`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`3`, rerankScore=`4.1353`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: 弱相关问题更隐蔽，因为用户可能问审批人、成本中心、云厂商、负责人电话、紧急群组这些文档未覆盖的信息。控制组要求系统在这些问题上明确说明文档未提供，不应编造人名、部门、平台名称或外部链接。本文档只说明容量治理策略、归档窗口编号、冷数据迁移阈值、单批提交上限、隔离队列重放次数和回滚观察期。本文档没有说明审批人是谁，没有说明云厂商是谁，没有说明预算归属，也没有说明...
    - #4 score=`0.0161`, source=`control-cn-low-newline.txt`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7298`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`4`, rerankScore=`3.4392`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: 过异步索引回填，冷数据只保留查询摘要和审计索引。这里的冷数据迁移阈值被明确写成：连续二十一天未访问。这个阈值只用于判断明细是否进入候选池，不等于删除条件，也不等于归档任务立即执行。为了避免误判，系统还会检查最近七天是否存在人工置顶、账单核对、客服补单、风控复查等保护标记。七天检查窗口只是安全保护项，不是冷数据迁移阈值。另一个常见误解是把十四天低频访问当成归档...

answer:

```text
根据控制组文档，冷数据迁移阈值是 **连续二十一天未访问**，归档窗口编号是 **ARCHIVE-CN-7319**[1][2][4]。
```

### RAG-12 - control_plaintext_cn_weak_related

- question: 控制组文档指定的归档审批人是谁？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `5380`
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
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `1045` / max `6000`
  - rerank: applied `true` / mode `LOCAL_BGE` / candidates `4` / final `4`
  - documents:
    - #1 score=`0.0164`, source=`control-cn-low-newline.txt`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7169`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`2.8353`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: �队列之前，调度器还会读取业务线的保留策略，确认租户等级、合规标签、冻结状态和最近一次补偿任务结果。为了便于回放，本次控制组把归档窗口编号固定为 ARCHIVE-CN-7319。这个编号应该出现在检索答案中，因为它是文档里唯一一个用于识别迁移窗口的字符串。窗口编号不是审批单号，也不是工单编号。文档没有指定审批人姓名，没有给出审批系统地址，也没有要求用户在回答...
    - #2 score=`0.0161`, source=`control-cn-low-newline.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6741`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`2.0946`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: 弱相关问题更隐蔽，因为用户可能问审批人、成本中心、云厂商、负责人电话、紧急群组这些文档未覆盖的信息。控制组要求系统在这些问题上明确说明文档未提供，不应编造人名、部门、平台名称或外部链接。本文档只说明容量治理策略、归档窗口编号、冷数据迁移阈值、单批提交上限、隔离队列重放次数和回滚观察期。本文档没有说明审批人是谁，没有说明云厂商是谁，没有说明预算归属，也没有说明...
    - #3 score=`0.0159`, source=`control-cn-low-newline.txt`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6687`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`-3.2406`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: �过移动。若某条明细已经进入候选池但还没有搬迁，用户查询仍然走主库。若某条明细已经搬迁且摘要索引校验通过，用户查询可以走归档读路径。若摘要索引存在但正文块缺失，系统必须返回内部错误并触发隔离，而不是把问题解释成知识不存在。控制组还刻意加入一个容易误读的术语：容量观察期。容量观察期用于评估增长趋势，长度为十天，但它不参与冷数据迁移判断。冷数据迁移阈值仍然是连续...
    - #4 score=`0.0156`, source=`control-cn-low-newline.txt`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6475`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`-5.9137`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: 归档执行过程分为预扫描、冻结快照、分批搬迁、校验回填和灰度释放五段。预扫描阶段只读取元数据，不移动正文。冻结快照阶段会记录候选明细的主键范围、最近更新时间、摘要哈希和原始分区。分批搬迁阶段按租户和业务类型做小批量提交，每批上限为三千条。三千条是单次提交上限，不是迁移阈值。校验回填阶段会对摘要索引、审计索引和归档位置做三方比对。灰度释放阶段会在查询网关里开启双...

answer:

```text
根据文档内容，**未指定归档审批人**。文档明确说明，该控制组文档不包含审批人姓名、审批系统地址或组织架构信息，因此无法提供具体审批人。
```

### RAG-13 - control_plaintext_en_answerable

- question: According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?
- should_answer: `true`
- expected_source_section: control-en-long-paragraph.txt / webhook fallback runbook
- completed: `true`
- duration_ms: `8917`
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
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `5545` / max `6000`
  - rerank: applied `true` / mode `LOCAL_BGE` / candidates `4` / final `4`
  - documents:
    - #1 score=`0.0159`, source=`control-en-long-paragraph.txt`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7743`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`1`, rerankScore=`5.5213`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: This control document is written as a long plain text incident runbook for retrieval testing. It is not a production policy and it intentionally avoids headings, lists, tables, cod...
    - #2 score=`0.0164`, source=`control-en-long-paragraph.txt`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7906`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`4.0941`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: It does not mention a region, a vendor ticket number, or a private dashboard link. It only states that silver-river-42 is the fallback queue name, that the fallback retry budget is...
    - #3 score=`0.0156`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7632`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`3`, rerankScore=`2.2964`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...
    - #4 score=`0.0161`, source=`control-en-long-paragraph.txt`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7826`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`4`, rerankScore=`0.6092`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: The compact envelope intentionally excludes the original request body after validation so that repeated retries do not increase storage pressure. Once fallback mode is enabled, the...

answer:

```text
Based on the control document, the fallback queue name is **silver-river-42** and the retry budget for that fallback path is **seven attempts within eighteen minutes**[1][2][3].
```

### RAG-14 - control_plaintext_en_weak_related

- question: According to the control document, which cloud provider hosts the fallback queue?
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `7368`
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
  - documents:
    - #1 score=`0.0161`, source=`control-en-long-paragraph.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7513`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`1`, rerankScore=`1.7528`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: This note is present to add realistic language around the anchor silver-river-42 without changing the anchor itself. The queue name remains silver-river-42. The document still does...
    - #2 score=`0.0164`, source=`control-en-long-paragraph.txt`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7799`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`1.2228`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: It does not mention a region, a vendor ticket number, or a private dashboard link. It only states that silver-river-42 is the fallback queue name, that the fallback retry budget is...
    - #3 score=`0.0159`, source=`control-en-long-paragraph.txt`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7453`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.6725`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: This control document is written as a long plain text incident runbook for retrieval testing. It is not a production policy and it intentionally avoids headings, lists, tables, cod...
    - #4 score=`0.0156`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7432`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`-5.2668`, rerankApplied=`True`, rerankMode=`LOCAL_BGE`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...

answer:

```text
Based on the provided control document, the cloud provider hosting the fallback queue is not identified. The document explicitly states that it "does not identify a cloud provider" and that such a question "cannot be answered from this document" [1][2].
```
