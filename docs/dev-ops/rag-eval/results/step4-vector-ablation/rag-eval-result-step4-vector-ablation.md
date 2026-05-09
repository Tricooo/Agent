# RAG Eval Result

- generated_at: `2026-05-08 04:57:31`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。
>
> `retrieved` / `score` / `empty` 三列的 `—` 表示**没拿到成功的 ChatResponse metadata**，不等价于"无检索"。当前实现把 retrieval SSE 帧放在 `.call().chatResponse()` 返回之后才发，所以 LLM 调用失败时（即使 RAG 检索本身成功）三列都会是 `—`。要区分"检索失败"和"生成失败"，对照 `error` 列 / details 区 / backend log。
>
> Details 区的 `documents` 会展开 top-K chunk attribution；HYBRID 模式下 `score` 是 RRF score，原始向量分与关键词分分别看 `vectorScore` / `keywordScore`。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-01 | answerable | true | 7333 | true | 4 | 0.6561 .. 0.8057 | false | manual | — | 根据知识库，`grafana/list_datasources` 工具的作用是**列出 Grafana 中所有可用的数据源**，并返回它们的基本信息，如 ID、名称、类型、URL、访问方式以及是否为默认数据源等。该工具常用于系统分析前的数据... |  |
| RAG-02 | answerable | true | 9314 | true | 4 | 0.6919 .. 0.8033 | false | 4/4 |  | 根据提供的文档，`grafana/query_prometheus` 支持以下参数： - **`query`**：PromQL 查询语句（必填） - **`start`**：查询开始时间（可选） - **`end`**：查询结束时间（可选）... |  |
| RAG-03 | answerable_formula | true | 8441 | true | 4 | 0.6745 .. 0.7252 | false | 3/3 |  | 根据知识库中的信息，CPU 使用率的 PromQL 示例包含： - **整体 CPU 使用率**： ```promql 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) *... |  |
| RAG-04 | answerable_deep_section | true | 10474 | true | 4 | 0.6481 .. 0.7406 | false | 3/3 |  | 根据提供的参考信息，磁盘 7 天增长趋势预测可以使用以下 PromQL： ```promql predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*36... |  |
| RAG-05 | answerable_long_context | true | 16736 | true | 4 | 0.6178 .. 0.7195 | false | 5/5 |  | 根据提供的资料，系统健康报告模板通常包含以下几个核心部分[1][2]： 1. **系统运行状态分析报告**（报告标题） 2. **📊 监控数据概览**（时间范围、数据源、分析时间） 3. **🖥️ CPU 使用情况**（平均/峰值使用率、状... |  |
| RAG-06 | unanswerable | true | 4191 | false | 0 | — | true | manual | — | 对不起，当前知识库没有可用的相关信息，无法根据知识库回答您关于 MySQL InnoDB MVCC undo log 版本链实现的问题。 |  |
| RAG-07 | weak_related | true | 6813 | false | 4 | 0.6464 .. 0.6726 | false | manual | — | 根据提供的知识库内容，主要介绍了Grafana MCP工具的使用，包括数据源列表和Prometheus查询等操作，但没有涉及Grafana dashboard变量模板的配置方法。因此，我无法根据当前知识库回答关于变量模板配置的问题。 |  |
| RAG-08 | weak_related | true | 4536 | false | 0 | — | true | manual | — | 根据当前知识库的内容，没有检索到与“Alertmanager 的 silence 创建方法”相关的信息，因此我无法基于知识库为您提供解答。如果您有其他相关问题或需要通用性帮助，可以告诉我，我会尽力协助。 |  |
| RAG-09 | paraphrase | true | 7139 | true | 4 | 0.6810 .. 0.8186 | false | manual | — | 要确认监控数据来源，应该调用 **`grafana/list_datasources`** 工具。 这个工具会列出 Grafana 中配置的所有数据源（包括名称、类型、URL 等），可以帮你确认 Prometheus 等数据源是否可用，以及... |  |
| RAG-10 | paraphrase | true | 14541 | true | 4 | 0.6684 .. 0.6819 | false | 2/5 | 正常范围, 警告范围, 危险范围 | 根据知识库给出的查询思路，要查询内存压力，核心是使用以下 PromQL 查询来计算内存使用率并评估压力： ```promql (1 - (node_memory_MemAvailable_bytes / node_memory_MemTot... |  |
| RAG-11 | control_plaintext_cn_answerable | true | 5765 | true | 4 | 0.7175 .. 0.7763 | false | 2/2 |  | 根据控制组文档，冷数据迁移阈值是**连续二十一天未访问**，用于判断明细是否进入候选池；归档窗口编号固定为 **ARCHIVE-CN-7319**，是本次控制组用于识别迁移窗口的字符串[1][2]。 |  |
| RAG-12 | control_plaintext_cn_weak_related | true | 3668 | false | 4 | 0.6475 .. 0.7169 | false | manual | — | 根据控制组文档的说明，文档中并未指定归档审批人的姓名或相关信息，因此无法提供审批人是谁的答案。 |  |
| RAG-13 | control_plaintext_en_answerable | true | 8434 | true | 4 | 0.7632 .. 0.7906 | false | 2/2 |  | According to the control document, the fallback queue name is **silver-river-42**, and the retry budget for that fallbac... |  |
| RAG-14 | control_plaintext_en_weak_related | true | 7354 | false | 4 | 0.7432 .. 0.7799 | false | manual | — | The control document does not identify which cloud provider hosts the fallback queue. It explicitly states that such inf... |  |

## Details

### RAG-01 - answerable

- question: 根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。
- should_answer: `true`
- expected_source_section: grafana/list_datasources
- completed: `true`
- duration_ms: `7333`
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
  - score_range: `0.6561 .. 0.8057`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2310` / max `6000`
  - documents:
    - #1 score=`0.8057`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #2 score=`0.6647`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #3 score=`0.6601`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #4 score=`0.6561`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...

answer:

```text
根据知识库，`grafana/list_datasources` 工具的作用是**列出 Grafana 中所有可用的数据源**，并返回它们的基本信息，如 ID、名称、类型、URL、访问方式以及是否为默认数据源等。该工具常用于系统分析前的数据源检查、确认监控数据的来源，以及多数据源环境的管理。例如，在自动化分析流程的第一步就是调用此工具来确认 Prometheus 数据源是否可用[1][3]。
```

### RAG-02 - answerable

- question: `grafana/query_prometheus` 支持哪些参数？
- should_answer: `true`
- expected_source_section: grafana/query_prometheus 参数说明
- completed: `true`
- duration_ms: `9314`
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
  - score_range: `0.6919 .. 0.8033`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2199` / max `6000`
  - documents:
    - #1 score=`0.8033`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #2 score=`0.7107`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #3 score=`0.6989`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #4 score=`0.6919`, source=`grafana-mcp-tools-guide.md`, chunk=`8`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: ### 常见错误 1. **数据源连接失败** - 检查Prometheus服务状态 - 验证网络连接 - 确认认证配置 2. **查询语法错误** - 验证PromQL语法 - 检查指标名称拼写 - 确认标签选择器 3. **数据缺失** - 检查时间范围设置 - 验证指标采集状态 - 确认数据保留策略 ### 故障排查步骤 1. **验证数据源** ``...

answer:

```text
根据提供的文档，`grafana/query_prometheus` 支持以下参数：

- **`query`**：PromQL 查询语句（必填）
- **`start`**：查询开始时间（可选）
- **`end`**：查询结束时间（可选）
- **`step`**：查询步长（可选）

这些信息来源于 [1] 中的参数说明部分。
```

### RAG-03 - answerable_formula

- question: 知识库里 CPU 使用率的 PromQL 示例是什么？
- should_answer: `true`
- expected_source_section: CPU使用率查询
- completed: `true`
- duration_ms: `8441`
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
  - score_range: `0.6745 .. 0.7252`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2163` / max `6000`
  - documents:
    - #1 score=`0.7252`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #2 score=`0.6976`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...
    - #3 score=`0.6863`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #4 score=`0.6745`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...

answer:

```text
根据知识库中的信息，CPU 使用率的 PromQL 示例包含：

- **整体 CPU 使用率**：
  ```promql
  100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
  ```
  参考 [1]

- **按实例分组的 CPU 使用率**：
  ```promql
  100 - (avg by (instance)(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
  ```
  参考 [1]

- **监控分析流程中的示例（查询过去1小时）**：
  ```promql
  100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100)
  ```
  参考 [4]
```

### RAG-04 - answerable_deep_section

- question: 磁盘 7 天增长趋势预测用哪个 PromQL？
- should_answer: `true`
- expected_source_section: 趋势预测
- completed: `true`
- duration_ms: `10474`
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
  - score_range: `0.6481 .. 0.7406`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2217` / max `6000`
  - documents:
    - #1 score=`0.7406`, source=`grafana-mcp-tools-guide.md`, chunk=`5`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: # 磁盘使用增长趋势 predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) ``` ## 数据解释和分析标准 ### CPU数据解释 - **数值范围**: 0-100% - **正常范围**: 0-70% - **警告范围**: 70-90% - **危险范...
    - #2 score=`0.6639`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #3 score=`0.6563`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #4 score=`0.6481`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...

answer:

```text
根据提供的参考信息，磁盘 7 天增长趋势预测可以使用以下 PromQL：

```promql
predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600)
```

**解释**：
- `predict_linear` 函数基于过去一段时间的数据进行线性回归，预测未来某个时间点的值。
- `node_filesystem_avail_bytes{fstype!="tmpfs"}` 获取非 tmpfs 文件系统的可用空间（单位：字节）。
- `[24h]` 表示使用过去 24 小时的数据作为预测基础。
- `7*24*3600` 是预测从现在起 7 天（604800 秒）后的可用空间。

如果预测值接近 0 或负值，说明磁盘空间可能在 7 天内耗尽，需要及时扩容或清理。该查询常用于 Grafana 告警规则中。
```

### RAG-05 - answerable_long_context

- question: 系统健康报告模板包含哪些部分？
- should_answer: `true`
- expected_source_section: 系统健康报告模板
- completed: `true`
- duration_ms: `16736`
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
  - score_range: `0.6178 .. 0.7195`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `1985` / max `6000`
  - documents:
    - #1 score=`0.7195`, source=`grafana-mcp-tools-guide.md`, chunk=`7`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: - **当前使用率**: {memory_usage}% - **可用内存**: {memory_available}GB - **状态评估**: {memory_status} - **使用趋势**: {memory_trend} ### 💿 磁盘使用情况 - **使用率**: {disk_usage}% - **可用空间**: {disk_availab...
    - #2 score=`0.7172`, source=`grafana-mcp-tools-guide.md`, chunk=`6`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: - **危险范围**: 95-100% ### 网络数据解释 - **单位**: bps (bits per second) - **正常范围**: 根据网络带宽确定 - **异常指标**: 突然的流量峰值或持续的高流量 ## 报告生成模板 ### 系统健康报告模板 ```markdown ## 系统运行状态分析报告 ### 📊 监控数据概览 - **监控时...
    - #3 score=`0.6314`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...
    - #4 score=`0.6178`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...

answer:

```text
根据提供的资料，系统健康报告模板通常包含以下几个核心部分[1][2]：

1. **系统运行状态分析报告**（报告标题）
2. **📊 监控数据概览**（时间范围、数据源、分析时间）
3. **🖥️ CPU 使用情况**（平均/峰值使用率、状态评估、趋势）
4. **💾 内存使用情况**（当前使用率、可用内存、状态评估、趋势）
5. **💿 磁盘使用情况**（使用率、可用空间、状态评估、空间预警）
6. **🌐 网络流量情况**（接收/发送速率、状态评估、流量模式）
7. **📈 综合评估**（系统健康度、关键发现、优化建议、告警建议）

这些部分共同构成一份标准的系统健康分析报告，能够全面反映服务器或服务的运行状态。
```

### RAG-06 - unanswerable

- question: 根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `4191`
- score_mode: `manual`
- expected_points:
  - 无法基于当前知识库回答
  - 当前知识库没有相关上下文

retrieval:
- event #1
  - retrieved_document_count: `0`
  - retrieval_empty: `true`
  - similarity_threshold: `0.6000`
  - score_range: `— .. —`
  - context_selected: `0` / dropped: `0` / truncated: `false`
  - context_chars: actual `61` / max `6000`

answer:

```text
对不起，当前知识库没有可用的相关信息，无法根据知识库回答您关于 MySQL InnoDB MVCC undo log 版本链实现的问题。
```

### RAG-07 - weak_related

- question: 根据知识库，Grafana dashboard 变量模板怎么配置？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `6813`
- score_mode: `manual`
- expected_points:
  - 知识库未涉及 dashboard 变量模板
  - 无法基于当前知识库回答

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.6464 .. 0.6726`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2199` / max `6000`
  - documents:
    - #1 score=`0.6726`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #2 score=`0.6648`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #3 score=`0.6647`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #4 score=`0.6464`, source=`grafana-mcp-tools-guide.md`, chunk=`8`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: ### 常见错误 1. **数据源连接失败** - 检查Prometheus服务状态 - 验证网络连接 - 确认认证配置 2. **查询语法错误** - 验证PromQL语法 - 检查指标名称拼写 - 确认标签选择器 3. **数据缺失** - 检查时间范围设置 - 验证指标采集状态 - 确认数据保留策略 ### 故障排查步骤 1. **验证数据源** ``...

answer:

```text
根据提供的知识库内容，主要介绍了Grafana MCP工具的使用，包括数据源列表和Prometheus查询等操作，但没有涉及Grafana dashboard变量模板的配置方法。因此，我无法根据当前知识库回答关于变量模板配置的问题。
```

### RAG-08 - weak_related

- question: 根据知识库，Alertmanager 的 silence 应该怎么创建？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `4536`
- score_mode: `manual`
- expected_points:
  - 知识库未涉及 Alertmanager silence
  - 无法基于当前知识库回答

retrieval:
- event #1
  - retrieved_document_count: `0`
  - retrieval_empty: `true`
  - similarity_threshold: `0.6000`
  - score_range: `— .. —`
  - context_selected: `0` / dropped: `0` / truncated: `false`
  - context_chars: actual `61` / max `6000`

answer:

```text
根据当前知识库的内容，没有检索到与“Alertmanager 的 silence 创建方法”相关的信息，因此我无法基于知识库为您提供解答。如果您有其他相关问题或需要通用性帮助，可以告诉我，我会尽力协助。
```

### RAG-09 - paraphrase

- question: 我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？
- should_answer: `true`
- expected_source_section: 标准监控分析流程 / 数据源检查
- completed: `true`
- duration_ms: `7139`
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
  - score_range: `0.6810 .. 0.8186`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2310` / max `6000`
  - documents:
    - #1 score=`0.8186`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #2 score=`0.6867`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #3 score=`0.6820`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #4 score=`0.6810`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...

answer:

```text
要确认监控数据来源，应该调用 **`grafana/list_datasources`** 工具。

这个工具会列出 Grafana 中配置的所有数据源（包括名称、类型、URL 等），可以帮你确认 Prometheus 等数据源是否可用，以及它们的连接信息[1]。
```

### RAG-10 - paraphrase

- question: 如果我要查内存压力，知识库给出的查询思路是什么？
- should_answer: `true`
- expected_source_section: 内存使用率查询 / 内存数据解释
- completed: `true`
- duration_ms: `14541`
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
  - score_range: `0.6684 .. 0.6819`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2211` / max `6000`
  - documents:
    - #1 score=`0.6819`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #2 score=`0.6741`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #3 score=`0.6692`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...
    - #4 score=`0.6684`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...

answer:

```text
根据知识库给出的查询思路，要查询内存压力，核心是使用以下 PromQL 查询来计算内存使用率并评估压力：

```promql
(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
```

这个表达式会返回当前内存使用百分比，数值越高表示内存压力越大[1]。如果需要查看已用内存的绝对值（单位GB），可以配合以下查询：

```promql
(node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024
```

这些查询属于标准监控分析流程中的“内存分析”步骤，用于评估内存压力状况[1][4]。
```

### RAG-11 - control_plaintext_cn_answerable

- question: 控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？
- should_answer: `true`
- expected_source_section: control-cn-low-newline.txt / 容量治理控制组
- completed: `true`
- duration_ms: `5765`
- score_mode: `literal`
- expected_points:
  - [x] 连续二十一天未访问
  - [x] ARCHIVE-CN-7319

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.7175 .. 0.7763`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `1061` / max `6000`
  - documents:
    - #1 score=`0.7763`, source=`control-cn-low-newline.txt`, chunk=`9`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: �过移动。若某条明细已经进入候选池但还没有搬迁，用户查询仍然走主库。若某条明细已经搬迁且摘要索引校验通过，用户查询可以走归档读路径。若摘要索引存在但正文块缺失，系统必须返回内部错误并触发隔离，而不是把问题解释成知识不存在。控制组还刻意加入一个容易误读的术语：容量观察期。容量观察期用于评估增长趋势，长度为十天，但它不参与冷数据迁移判断。冷数据迁移阈值仍然是连续...
    - #2 score=`0.7298`, source=`control-cn-low-newline.txt`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: 过异步索引回填，冷数据只保留查询摘要和审计索引。这里的冷数据迁移阈值被明确写成：连续二十一天未访问。这个阈值只用于判断明细是否进入候选池，不等于删除条件，也不等于归档任务立即执行。为了避免误判，系统还会检查最近七天是否存在人工置顶、账单核对、客服补单、风控复查等保护标记。七天检查窗口只是安全保护项，不是冷数据迁移阈值。另一个常见误解是把十四天低频访问当成归档...
    - #3 score=`0.7277`, source=`control-cn-low-newline.txt`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: �队列之前，调度器还会读取业务线的保留策略，确认租户等级、合规标签、冻结状态和最近一次补偿任务结果。为了便于回放，本次控制组把归档窗口编号固定为 ARCHIVE-CN-7319。这个编号应该出现在检索答案中，因为它是文档里唯一一个用于识别迁移窗口的字符串。窗口编号不是审批单号，也不是工单编号。文档没有指定审批人姓名，没有给出审批系统地址，也没有要求用户在回答...
    - #4 score=`0.7175`, source=`control-cn-low-newline.txt`, chunk=`6`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: 弱相关问题更隐蔽，因为用户可能问审批人、成本中心、云厂商、负责人电话、紧急群组这些文档未覆盖的信息。控制组要求系统在这些问题上明确说明文档未提供，不应编造人名、部门、平台名称或外部链接。本文档只说明容量治理策略、归档窗口编号、冷数据迁移阈值、单批提交上限、隔离队列重放次数和回滚观察期。本文档没有说明审批人是谁，没有说明云厂商是谁，没有说明预算归属，也没有说明...

answer:

```text
根据控制组文档，冷数据迁移阈值是**连续二十一天未访问**，用于判断明细是否进入候选池；归档窗口编号固定为 **ARCHIVE-CN-7319**，是本次控制组用于识别迁移窗口的字符串[1][2]。
```

### RAG-12 - control_plaintext_cn_weak_related

- question: 控制组文档指定的归档审批人是谁？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `3668`
- score_mode: `manual`
- expected_points:
  - 文档没有指定审批人
  - 无法基于当前知识库回答

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.6475 .. 0.7169`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `1045` / max `6000`
  - documents:
    - #1 score=`0.7169`, source=`control-cn-low-newline.txt`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: �队列之前，调度器还会读取业务线的保留策略，确认租户等级、合规标签、冻结状态和最近一次补偿任务结果。为了便于回放，本次控制组把归档窗口编号固定为 ARCHIVE-CN-7319。这个编号应该出现在检索答案中，因为它是文档里唯一一个用于识别迁移窗口的字符串。窗口编号不是审批单号，也不是工单编号。文档没有指定审批人姓名，没有给出审批系统地址，也没有要求用户在回答...
    - #2 score=`0.6741`, source=`control-cn-low-newline.txt`, chunk=`6`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: 弱相关问题更隐蔽，因为用户可能问审批人、成本中心、云厂商、负责人电话、紧急群组这些文档未覆盖的信息。控制组要求系统在这些问题上明确说明文档未提供，不应编造人名、部门、平台名称或外部链接。本文档只说明容量治理策略、归档窗口编号、冷数据迁移阈值、单批提交上限、隔离队列重放次数和回滚观察期。本文档没有说明审批人是谁，没有说明云厂商是谁，没有说明预算归属，也没有说明...
    - #3 score=`0.6687`, source=`control-cn-low-newline.txt`, chunk=`9`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: �过移动。若某条明细已经进入候选池但还没有搬迁，用户查询仍然走主库。若某条明细已经搬迁且摘要索引校验通过，用户查询可以走归档读路径。若摘要索引存在但正文块缺失，系统必须返回内部错误并触发隔离，而不是把问题解释成知识不存在。控制组还刻意加入一个容易误读的术语：容量观察期。容量观察期用于评估增长趋势，长度为十天，但它不参与冷数据迁移判断。冷数据迁移阈值仍然是连续...
    - #4 score=`0.6475`, source=`control-cn-low-newline.txt`, chunk=`3`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: 归档执行过程分为预扫描、冻结快照、分批搬迁、校验回填和灰度释放五段。预扫描阶段只读取元数据，不移动正文。冻结快照阶段会记录候选明细的主键范围、最近更新时间、摘要哈希和原始分区。分批搬迁阶段按租户和业务类型做小批量提交，每批上限为三千条。三千条是单次提交上限，不是迁移阈值。校验回填阶段会对摘要索引、审计索引和归档位置做三方比对。灰度释放阶段会在查询网关里开启双...

answer:

```text
根据控制组文档的说明，文档中并未指定归档审批人的姓名或相关信息，因此无法提供审批人是谁的答案。
```

### RAG-13 - control_plaintext_en_answerable

- question: According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?
- should_answer: `true`
- expected_source_section: control-en-long-paragraph.txt / webhook fallback runbook
- completed: `true`
- duration_ms: `8434`
- score_mode: `literal`
- expected_points:
  - [x] silver-river-42
  - [x] seven attempts within eighteen minutes

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.7632 .. 0.7906`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `5545` / max `6000`
  - documents:
    - #1 score=`0.7906`, source=`control-en-long-paragraph.txt`, chunk=`4`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: It does not mention a region, a vendor ticket number, or a private dashboard link. It only states that silver-river-42 is the fallback queue name, that the fallback retry budget is...
    - #2 score=`0.7826`, source=`control-en-long-paragraph.txt`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: The compact envelope intentionally excludes the original request body after validation so that repeated retries do not increase storage pressure. Once fallback mode is enabled, the...
    - #3 score=`0.7743`, source=`control-en-long-paragraph.txt`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: This control document is written as a long plain text incident runbook for retrieval testing. It is not a production policy and it intentionally avoids headings, lists, tables, cod...
    - #4 score=`0.7632`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...

answer:

```text
According to the control document, the fallback queue name is **silver-river-42**, and the retry budget for that fallback path is **seven attempts within eighteen minutes**[1][2][3].
```

### RAG-14 - control_plaintext_en_weak_related

- question: According to the control document, which cloud provider hosts the fallback queue?
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `7354`
- score_mode: `manual`
- expected_points:
  - does not specify which cloud provider
  - cannot be answered from this document

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.7432 .. 0.7799`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `5550` / max `6000`
  - documents:
    - #1 score=`0.7799`, source=`control-en-long-paragraph.txt`, chunk=`4`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: It does not mention a region, a vendor ticket number, or a private dashboard link. It only states that silver-river-42 is the fallback queue name, that the fallback retry budget is...
    - #2 score=`0.7513`, source=`control-en-long-paragraph.txt`, chunk=`6`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: This note is present to add realistic language around the anchor silver-river-42 without changing the anchor itself. The queue name remains silver-river-42. The document still does...
    - #3 score=`0.7453`, source=`control-en-long-paragraph.txt`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: This control document is written as a long plain text incident runbook for retrieval testing. It is not a production policy and it intentionally avoids headings, lists, tables, cod...
    - #4 score=`0.7432`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...

answer:

```text
The control document does not identify which cloud provider hosts the fallback queue. It explicitly states that such information is not included in the document, so I'm unable to answer your question.
```
