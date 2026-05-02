# RAG Eval Result

- generated_at: `2026-05-02 04:14:56`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-01 | answerable | true | 11320 | true | 3 | 0.6001 .. 0.7641 | false | manual | — | 根据知识库，`grafana/list_datasources` 工具的主要作用是**列出所有可用的数据源** [1]。 具体来说，它的功能包括： * 获取Grafana中配置的所有数据源 [1] * 返回数据源的基本信息，如ID、名称、类... |  |
| RAG-02 | answerable | true | 13343 | true | 3 | 0.6656 .. 0.7146 | false | 4/4 |  | 根据 Grafana MCP 工具文档，`grafana/query_prometheus` 支持以下四个参数： - **`query`** *(必填)*：PromQL 查询语句，例如 `up` 或 CPU 使用率表达式。 - **`sta... |  |
| RAG-03 | answerable_formula | true | 15791 | true | 3 | 0.6479 .. 0.6908 | false | 3/3 |  | 根据知识库 [1] 和 [2] 的内容，CPU 使用率的 PromQL 示例如下： **1. 整体 CPU 使用率** 计算 `[5m]`（5分钟）范围内的整体 CPU 使用率： ```promql 100 - (avg(irate(nod... |  |
| RAG-04 | answerable_deep_section | false | 28240 | true | — | — | — | 0/3 | predict_linear, node_filesystem_avail_bytes, 7*24*3600 |  |  |
| RAG-05 | answerable_long_context | true | 8192 | true | 2 | 0.6499 .. 0.6907 | false | 5/5 |  | 根据提供的上下文，系统健康报告模板主要包含以下几个部分（详见[1]）： - **监控数据概览**：涵盖监控时间范围、数据源、分析时间等基本信息。 - **CPU 使用情况**：包括平均使用率、峰值使用率、状态评估和趋势分析。 - **内存使... |  |
| RAG-06 | unanswerable | true | 4910 | false | 0 | — | true | manual | — | 根据当前提供的知识库内容，未检索到满足过滤条件和相似度阈值的相关片段，因此无法基于知识库回答关于 MySQL InnoDB MVCC undo log 版本链实现的问题。建议您查阅官方文档或相关技术资料获取详细信息。 |  |
| RAG-07 | weak_related | true | 10213 | false | 3 | 0.6185 .. 0.6645 | false | manual | — | 根据提供的知识库内容，主要介绍了通过 Grafana MCP 工具进行自动化监控分析（如列出数据源、执行 Prometheus 查询、报告生成等），**并未包含关于 Grafana Dashboard 变量模板配置的具体方法**。知识库中的... |  |
| RAG-08 | weak_related | true | 10488 | false | 0 | — | true | manual | — | 根据当前提供的知识库上下文，未检索到与“Alertmanager 的 silence 创建方法”相关的可用片段。因此，我无法基于现有知识库回答您的问题。 建议您查阅 Alertmanager 官方文档，或重新提供更具体的知识库内容以便我能为... |  |
| RAG-09 | paraphrase | true | 12330 | true | 3 | 0.6406 .. 0.8130 | false | manual | — | 要确认监控数据的来源，应该调用 **`grafana/list_datasources`** 工具。 根据指南中的说明，这个工具的功能就是获取 Grafana 中所有配置的数据源信息（如 Prometheus 的 ID、名称、URL 等），... |  |
| RAG-10 | paraphrase | true | 14274 | true | 3 | 0.6379 .. 0.6703 | false | 5/5 |  | 根据知识库内容，查询内存压力的思路主要分为以下几步： 1. **执行查询语句**：使用 PromQL 查询 `(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes... |  |

## Details

### RAG-01 - answerable

- question: 根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。
- should_answer: `true`
- expected_source_section: grafana/list_datasources
- completed: `true`
- duration_ms: `11320`
- score_mode: `manual`
- expected_points:
  - 列出所有可用的数据源
  - 返回数据源的基本信息
  - 确认 Prometheus 数据源的可用性

retrieval:
- event #1
  - retrieved_document_count: `3`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.6001 .. 0.7641`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `4692` / max `6000`

answer:

```text
根据知识库，`grafana/list_datasources` 工具的主要作用是**列出所有可用的数据源** [1]。

具体来说，它的功能包括：
*   获取Grafana中配置的所有数据源 [1]
*   返回数据源的基本信息，如ID、名称、类型、URL等 [1]
*   用于确认特定数据源（如Prometheus）的可用性 [1]

常见使用场景是系统分析前的数据源检查以及多数据源环境的管理 [1]。在自动化监控分析流程中，它通常也是第一步，用于验证数据源是否可用 [2]。
```

### RAG-02 - answerable

- question: `grafana/query_prometheus` 支持哪些参数？
- should_answer: `true`
- expected_source_section: grafana/query_prometheus 参数说明
- completed: `true`
- duration_ms: `13343`
- score_mode: `literal`
- expected_points:
  - [x] query
  - [x] start
  - [x] end
  - [x] step

retrieval:
- event #1
  - retrieved_document_count: `3`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.6656 .. 0.7146`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `4692` / max `6000`

answer:

```text
根据 Grafana MCP 工具文档，`grafana/query_prometheus` 支持以下四个参数：

- **`query`** *(必填)*：PromQL 查询语句，例如 `up` 或 CPU 使用率表达式。
- **`start`** *(可选)*：查询开始时间，支持相对时间（如 `now-1h`）或绝对时间，不指定时使用默认时间范围。
- **`end`** *(可选)*：查询结束时间，格式同 `start`，不指定时使用默认时间范围。
- **`step`** *(可选)*：查询步长（采样分辨率），用于控制返回数据点的间隔，适用于范围查询，不指定时由系统自动计算。

典型调用示例：
```json
{
  "query": "100 - (avg(irate(node_cpu_seconds_total{mode=\"idle\"}[5m])) * 100)",
  "start": "now-1h",
  "end": "now",
  "step": "15s"
}
```
```

### RAG-03 - answerable_formula

- question: 知识库里 CPU 使用率的 PromQL 示例是什么？
- should_answer: `true`
- expected_source_section: CPU使用率查询
- completed: `true`
- duration_ms: `15791`
- score_mode: `literal`
- expected_points:
  - [x] node_cpu_seconds_total
  - [x] mode="idle"
  - [x] irate

retrieval:
- event #1
  - retrieved_document_count: `3`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.6479 .. 0.6908`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `4692` / max `6000`

answer:

```text
根据知识库 [1] 和 [2] 的内容，CPU 使用率的 PromQL 示例如下：

**1. 整体 CPU 使用率**
计算 `[5m]`（5分钟）范围内的整体 CPU 使用率：
```promql
100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
```

**2. 按实例分组的 CPU 使用率**
按 `instance` 标签分组显示各实例的 CPU 使用率：
```promql
100 - (avg by (instance)(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
```

**3. 长时间范围的 CPU 分析**
在自动化分析流程中，查询过去 1 小时（`[1h]`）的 CPU 使用率 [2]：
```promql
100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100)
```

你可以根据需要调整时间范围（如 `[5m]`、`[1h]` 等）来监控不同窗口期的 CPU 负载。
```

### RAG-04 - answerable_deep_section

- question: 磁盘 7 天增长趋势预测用哪个 PromQL？
- should_answer: `true`
- expected_source_section: 趋势预测
- completed: `false`
- duration_ms: `28240`
- score_mode: `literal`
- error: `RestClientException: Error while extracting response for type [org.springframework.ai.openai.api.OpenAiApi$ChatCompletion] and content type [application/json]`
- expected_points:
  - [ ] predict_linear
  - [ ] node_filesystem_avail_bytes
  - [ ] 7*24*3600

answer:

```text

```

### RAG-05 - answerable_long_context

- question: 系统健康报告模板包含哪些部分？
- should_answer: `true`
- expected_source_section: 系统健康报告模板
- completed: `true`
- duration_ms: `8192`
- score_mode: `literal`
- expected_points:
  - [x] CPU
  - [x] 内存
  - [x] 磁盘
  - [x] 网络
  - [x] 综合评估

retrieval:
- event #1
  - retrieved_document_count: `2`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.6499 .. 0.6907`
  - context_selected: `2` / dropped: `0` / truncated: `false`
  - context_chars: actual `2979` / max `6000`

answer:

```text
根据提供的上下文，系统健康报告模板主要包含以下几个部分（详见[1]）：

- **监控数据概览**：涵盖监控时间范围、数据源、分析时间等基本信息。
- **CPU 使用情况**：包括平均使用率、峰值使用率、状态评估和趋势分析。
- **内存使用情况**：包括当前使用率、可用内存、状态评估和使用趋势。
- **磁盘使用情况**：包括使用率、可用空间、状态评估和空间预警。
- **网络流量情况**：包括接收/发送速率、状态评估和流量模式。
- **综合评估**：汇总系统健康度、关键发现、优化建议及告警建议。

这些模块共同构成一份完整的系统健康分析报告，便于快速掌握资源状态并采取相应措施。
```

### RAG-06 - unanswerable

- question: 根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `4910`
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
根据当前提供的知识库内容，未检索到满足过滤条件和相似度阈值的相关片段，因此无法基于知识库回答关于 MySQL InnoDB MVCC undo log 版本链实现的问题。建议您查阅官方文档或相关技术资料获取详细信息。
```

### RAG-07 - weak_related

- question: 根据知识库，Grafana dashboard 变量模板怎么配置？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `10213`
- score_mode: `manual`
- expected_points:
  - 知识库未涉及 dashboard 变量模板
  - 无法基于当前知识库回答

retrieval:
- event #1
  - retrieved_document_count: `3`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.6185 .. 0.6645`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `4692` / max `6000`

answer:

```text
根据提供的知识库内容，主要介绍了通过 Grafana MCP 工具进行自动化监控分析（如列出数据源、执行 Prometheus 查询、报告生成等），**并未包含关于 Grafana Dashboard 变量模板配置的具体方法**。知识库中的信息无法回答您关于 Dashboard 变量模板如何配置的问题。

如果您需要了解 Dashboard 变量的配置，可以参考 Grafana 官方文档，通常步骤包括：进入 Dashboard 设置 → Variables → 新建变量，然后根据数据源选择查询类型并定义变量的取值逻辑。
```

### RAG-08 - weak_related

- question: 根据知识库，Alertmanager 的 silence 应该怎么创建？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `10488`
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
根据当前提供的知识库上下文，未检索到与“Alertmanager 的 silence 创建方法”相关的可用片段。因此，我无法基于现有知识库回答您的问题。

建议您查阅 Alertmanager 官方文档，或重新提供更具体的知识库内容以便我能为您解答。
```

### RAG-09 - paraphrase

- question: 我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？
- should_answer: `true`
- expected_source_section: 标准监控分析流程 / 数据源检查
- completed: `true`
- duration_ms: `12330`
- score_mode: `manual`
- expected_points:
  - grafana/list_datasources
  - 确认监控数据来源
  - 数据源检查

retrieval:
- event #1
  - retrieved_document_count: `3`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.6406 .. 0.8130`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `4692` / max `6000`

answer:

```text
要确认监控数据的来源，应该调用 **`grafana/list_datasources`** 工具。

根据指南中的说明，这个工具的功能就是获取 Grafana 中所有配置的数据源信息（如 Prometheus 的 ID、名称、URL 等），专门用于系统分析前的数据源检查，以便确认监控数据是否来自预期的 Prometheus 实例[1]。
```

### RAG-10 - paraphrase

- question: 如果我要查内存压力，知识库给出的查询思路是什么？
- should_answer: `true`
- expected_source_section: 内存使用率查询 / 内存数据解释
- completed: `true`
- duration_ms: `14274`
- score_mode: `literal`
- expected_points:
  - [x] node_memory_MemAvailable_bytes
  - [x] node_memory_MemTotal_bytes
  - [x] 正常范围
  - [x] 警告范围
  - [x] 危险范围

retrieval:
- event #1
  - retrieved_document_count: `3`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.6379 .. 0.6703`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `4692` / max `6000`

answer:

```text
根据知识库内容，查询内存压力的思路主要分为以下几步：

1.  **执行查询语句**：使用 PromQL 查询 `(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100` [1]。
    这个公式是通过计算可用内存占总内存的比例，来得出当前的内存使用率[2]。

2.  **解读数值范围**：查询返回的结果是一个 0-100% 的百分比数值[1]。根据这个数值，可以评估当前内存压力所处的等级：
    *   **正常范围**：0-80%
    *   **警告范围**：80-95%
    *   **危险范围**：95-100% [1]

3.  **评估压力状况**：根据查询到的内存使用率，最终目的是“评估内存压力状况”[1]。如果使用率过高（例如进入警告或危险范围），就意味着系统可能存在内存压力。
```
