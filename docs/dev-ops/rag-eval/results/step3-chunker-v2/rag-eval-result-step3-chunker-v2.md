# RAG Eval Result

- generated_at: `2026-05-05 05:59:44`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。
>
> `retrieved` / `score` / `empty` 三列的 `—` 表示**没拿到成功的 ChatResponse metadata**，不等价于"无检索"。当前实现把 retrieval SSE 帧放在 `.call().chatResponse()` 返回之后才发，所以 LLM 调用失败时（即使 RAG 检索本身成功）三列都会是 `—`。要区分"检索失败"和"生成失败"，对照 `error` 列 / details 区 / backend log。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-01 | answerable | true | 12317 | true | 4 | 0.6157 .. 0.7609 | false | manual | — | 根据提供的上下文，**grafana/list_datasources** 工具的主要作用是**列出所有可用的数据源**[1]。 具体来说，它的功能主要包括以下几点： * **获取基本信息**：列出 Grafana 中配置的所有数据源，并返... |  |
| RAG-02 | answerable | true | 8410 | true | 4 | 0.6525 .. 0.7282 | false | 4/4 |  | 根据提供的文档，`grafana/query_prometheus` 工具支持以下参数： - **`query`** (必填)：PromQL 查询语句。 - **`start`** (可选)：查询开始时间，例如 `now-1h`。 - **... |  |
| RAG-03 | answerable_formula | true | 22618 | true | 4 | 0.6692 .. 0.7055 | false | 3/3 |  | 根据知识库，CPU 使用率相关的 PromQL 示例包括： - **整体 CPU 使用率**（最近 5 分钟） `100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 10... |  |
| RAG-04 | answerable_deep_section | true | 12602 | true | 4 | 0.6201 .. 0.6715 | false | 3/3 |  | 用于预测磁盘 7 天增长趋势的 PromQL 是： ```promql predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) ``` **说... |  |
| RAG-05 | answerable_long_context | true | 10367 | true | 4 | 0.6008 .. 0.7053 | false | 5/5 |  | 根据提供的资料，系统健康报告模板包含以下主要部分[1]： - **监控数据概览**：包括监控时间范围、数据源和分析时间。 - **CPU 使用情况**：涵盖平均使用率、峰值使用率、状态评估和趋势分析。 - **内存使用情况**：涵盖当前使用... |  |
| RAG-06 | unanswerable | true | 5337 | false | 0 | — | true | manual | — | 根据当前知识库的检索结果，没有找到与 MySQL InnoDB MVCC undo log 版本链实现相关的可用知识片段。因此，我无法基于知识库为您回答该问题，建议您提供更详细的上下文或查阅相关技术文档。 |  |
| RAG-07 | weak_related | true | 7276 | false | 4 | 0.6021 .. 0.6634 | false | manual | — | 根据提供的知识库内容，其中主要涉及 Grafana MCP 工具的使用方法，包括数据源列表、PromQL 查询、自动化分析流程以及报告生成等，但**并未包含 Grafana Dashboard 变量模板的配置说明**。因此，我无法基于当前知... |  |
| RAG-08 | weak_related | true | 5378 | false | 0 | — | true | manual | — | 抱歉，当前知识库中没有与 Alertmanager silence 创建相关的可用信息，因此我无法基于知识库为您提供回答。建议您查阅 Prometheus 官方文档或 Alertmanager 使用相关资源来了解如何创建 silence。 |  |
| RAG-09 | paraphrase | true | 7443 | true | 4 | 0.6430 .. 0.8130 | false | manual | — | 要确认监控数据来源，您应该使用 **`grafana/list_datasources`** 工具。该工具会列出 Grafana 中配置的所有数据源，并返回它们的 ID、名称、类型、URL 等信息，方便您验证 Prometheus 等特定数... |  |
| RAG-10 | paraphrase | true | 11282 | true | 4 | 0.6361 .. 0.6686 | false | 5/5 |  | 根据知识库的资料，查询内存压力的思路主要是通过计算**内存使用率**来判断，并参考相应的评估标准。 **1. 查询语句：** 使用以下 PromQL 查询来获取当前内存使用百分比： ```promql (1 - (node_memory_M... |  |

## Details

### RAG-01 - answerable

- question: 根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。
- should_answer: `true`
- expected_source_section: grafana/list_datasources
- completed: `true`
- duration_ms: `12317`
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
  - score_range: `0.6157 .. 0.7609`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `3356` / max `6000`

answer:

```text
根据提供的上下文，**grafana/list_datasources** 工具的主要作用是**列出所有可用的数据源**[1]。

具体来说，它的功能主要包括以下几点：
*   **获取基本信息**：列出 Grafana 中配置的所有数据源，并返回如 ID、名称、类型、URL 等关键信息[1]。
*   **确认数据源可用性**：常用于分析开始前的检查，特别是用来确认 Prometheus 数据源是否配置正确且可用[1]。
*   **故障排查的起点**：在标准监控分析流程或故障排查步骤中，它通常被作为第一步验证操作，以确认监控数据的来源状态[1][2][3]。
```

### RAG-02 - answerable

- question: `grafana/query_prometheus` 支持哪些参数？
- should_answer: `true`
- expected_source_section: grafana/query_prometheus 参数说明
- completed: `true`
- duration_ms: `8410`
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
  - score_range: `0.6525 .. 0.7282`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `3418` / max `6000`

answer:

```text
根据提供的文档，`grafana/query_prometheus` 工具支持以下参数：

- **`query`** (必填)：PromQL 查询语句。
- **`start`** (可选)：查询开始时间，例如 `now-1h`。
- **`end`** (可选)：查询结束时间，例如 `now`。
- **`step`** (可选)：查询步长，用于定义结果数据点的间隔。

这些参数在文档的 [2] 部分有明确说明。
```

### RAG-03 - answerable_formula

- question: 知识库里 CPU 使用率的 PromQL 示例是什么？
- should_answer: `true`
- expected_source_section: CPU使用率查询
- completed: `true`
- duration_ms: `22618`
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
  - score_range: `0.6692 .. 0.7055`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `3377` / max `6000`

answer:

```text
根据知识库，CPU 使用率相关的 PromQL 示例包括：

- **整体 CPU 使用率**（最近 5 分钟）  
  `100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)` [4]  
  这是最常用的整体 CPU 使用率查询。

- **按实例分组的 CPU 使用率**  
  `100 - (avg by (instance)(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)` [1]  
  可以按机器实例分别查看 CPU 使用率。

- **基于不同时间窗口的 CPU 使用率**  
  `100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100)` [1]  
  时间窗口可调整为其他时长（例如 15m、1h 等）。

- **查询 CPU 峰值时间**（过去 24 小时中每小时的最大值）  
  `max_over_time((100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100))[24h:1h])` [3]  
  可用于识别高负载时段。

**原理说明**：这些查询都基于 `node_cpu_seconds_total` 指标，`mode="idle"` 表示 CPU 空闲时间，`irate` 计算每秒的速率，用 100% 减去空闲百分比即得到使用率。
```

### RAG-04 - answerable_deep_section

- question: 磁盘 7 天增长趋势预测用哪个 PromQL？
- should_answer: `true`
- expected_source_section: 趋势预测
- completed: `true`
- duration_ms: `12602`
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
  - score_range: `0.6201 .. 0.6715`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `3300` / max `6000`

answer:

```text
用于预测磁盘 7 天增长趋势的 PromQL 是：

```promql
predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600)
```

**说明**：  
- `predict_linear` 函数基于过去一段时间的线性回归来预测未来某个时间点的值。  
- 这里使用 `[24h]` 作为时间窗口，即基于最近 24 小时的数据。  
- `7*24*3600` 表示预测未来 7 天（以秒为单位）的值。  
- 该查询返回的是**磁盘可用字节数**的预测值，如果数值接近 0 或快速下降，说明磁盘可能会在 7 天内被写满。  

如果需要直接查看磁盘使用率的趋势，可以用下面这个变体：
```promql
predict_linear((1 - node_filesystem_avail_bytes{fstype!="tmpfs"} / node_filesystem_size_bytes{fstype!="tmpfs"}) * 100[24h], 7*24*3600)
```
它会预测 7 天后的使用率百分比。
```

### RAG-05 - answerable_long_context

- question: 系统健康报告模板包含哪些部分？
- should_answer: `true`
- expected_source_section: 系统健康报告模板
- completed: `true`
- duration_ms: `10367`
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
  - score_range: `0.6008 .. 0.7053`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2347` / max `6000`

answer:

```text
根据提供的资料，系统健康报告模板包含以下主要部分[1]：

- **监控数据概览**：包括监控时间范围、数据源和分析时间。
- **CPU 使用情况**：涵盖平均使用率、峰值使用率、状态评估和趋势分析。
- **内存使用情况**：涵盖当前使用率、可用内存、状态评估和使用趋势。
- **磁盘使用情况**：涵盖使用率、可用空间、状态评估和空间预警。
- **网络流量情况**：涵盖接收速率、发送速率、状态评估和流量模式。
- **综合评估**：包括系统健康度、关键发现、优化建议和告警建议。
```

### RAG-06 - unanswerable

- question: 根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `5337`
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
根据当前知识库的检索结果，没有找到与 MySQL InnoDB MVCC undo log 版本链实现相关的可用知识片段。因此，我无法基于知识库为您回答该问题，建议您提供更详细的上下文或查阅相关技术文档。
```

### RAG-07 - weak_related

- question: 根据知识库，Grafana dashboard 变量模板怎么配置？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `7276`
- score_mode: `manual`
- expected_points:
  - 知识库未涉及 dashboard 变量模板
  - 无法基于当前知识库回答

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.6021 .. 0.6634`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `3356` / max `6000`

answer:

```text
根据提供的知识库内容，其中主要涉及 Grafana MCP 工具的使用方法，包括数据源列表、PromQL 查询、自动化分析流程以及报告生成等，但**并未包含 Grafana Dashboard 变量模板的配置说明**。因此，我无法基于当前知识库为您解答该问题，建议查阅 Grafana 官方文档或相关配置指南。
```

### RAG-08 - weak_related

- question: 根据知识库，Alertmanager 的 silence 应该怎么创建？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `5378`
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
抱歉，当前知识库中没有与 Alertmanager silence 创建相关的可用信息，因此我无法基于知识库为您提供回答。建议您查阅 Prometheus 官方文档或 Alertmanager 使用相关资源来了解如何创建 silence。
```

### RAG-09 - paraphrase

- question: 我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？
- should_answer: `true`
- expected_source_section: 标准监控分析流程 / 数据源检查
- completed: `true`
- duration_ms: `7443`
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
  - score_range: `0.6430 .. 0.8130`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `3356` / max `6000`

answer:

```text
要确认监控数据来源，您应该使用 **`grafana/list_datasources`** 工具。该工具会列出 Grafana 中配置的所有数据源，并返回它们的 ID、名称、类型、URL 等信息，方便您验证 Prometheus 等特定数据源是否可用以及其基本配置​[1]。在使用一般分析流程之前，通常会先调用这个工具进行数据源检查​[2]。
```

### RAG-10 - paraphrase

- question: 如果我要查内存压力，知识库给出的查询思路是什么？
- should_answer: `true`
- expected_source_section: 内存使用率查询 / 内存数据解释
- completed: `true`
- duration_ms: `11282`
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
  - score_range: `0.6361 .. 0.6686`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2409` / max `6000`

answer:

```text
根据知识库的资料，查询内存压力的思路主要是通过计算**内存使用率**来判断，并参考相应的评估标准。

**1. 查询语句：**
使用以下 PromQL 查询来获取当前内存使用百分比：
```promql
(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
```
这个查询通过 `MemAvailable` 与 `MemTotal` 的比值计算出已用内存比例，从而衡量内存压力[1]。

**2. 评估标准：**
根据知识库提供的数据解释，内存压力的判断区间如下[3]：
- **正常范围**：0-80%
- **警告范围**：80-95%
- **危险范围**：95-100%

因此，你可以将该查询结果与上述阈值对比，如果使用率超过 80% 就应开始关注内存压力，超过 95% 则属于危险状态。如果需要深度分析，还可以结合趋势预测或关联 CPU 使用率进行综合判断[2][3]。
```
