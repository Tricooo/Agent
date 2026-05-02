# RAG Eval Result

- generated_at: `2026-05-02 03:45:59`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-01 | answerable | true | 16334 | true | 3 | 0.6001 .. 0.7641 | false | manual | — | 根据提供的上下文，`grafana/list_datasources` 工具的作用是： **核心功能** 列出并获取 Grafana 实例中已配置的所有数据源的信息。如 [1] 中所述，其功能描述为“获取Grafana中配置的所有数据源”和... |  |
| RAG-02 | answerable | true | 21741 | true | 3 | 0.6656 .. 0.7146 | false | 4/4 |  | 根据提供的文档信息，`grafana/query_prometheus` 支持以下参数： - **`query`**（必需）：要执行的 PromQL 查询语句。 - **`start`**（可选）：查询的开始时间。 - **`end`**（... |  |
| RAG-03 | answerable_formula | true | 11343 | true | 3 | 0.6479 .. 0.6908 | false | 3/3 |  | 根据知识库中的信息，CPU 使用率的 PromQL 查询示例如下： - **整体 CPU 使用率**： ```promql 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m]))... |  |
| RAG-04 | answerable_deep_section | true | 14370 | true | 3 | 0.6305 .. 0.6481 | false | 2/3 | 7*24*3600 | 根据上下文 [1] 中“趋势预测”部分，预测磁盘 7 天增长趋势可使用内置的 `predict_linear` 函数。示例 PromQL 如下： ```promql predict_linear(node_filesystem_avail_... |  |
| RAG-05 | answerable_long_context | false | 30323 | true | — | — | — | 0/5 | CPU, 内存, 磁盘, 网络, 综合评估 |  |  |
| RAG-06 | unanswerable | true | 6605 | false | 0 | — | true | manual | — | 根据知识库的检索结果，当前没有找到与您问题相关的上下文信息，因此我无法基于知识库为您解答 MySQL InnoDB MVCC undo log 版本链的实现细节。如果您有其他非知识库类问题，我可以尝试提供通用帮助。 |  |
| RAG-07 | weak_related | true | 7639 | false | 3 | 0.6185 .. 0.6645 | false | manual | — | 根据当前的知识库内容，主要涵盖了Grafana MCP工具的使用方法、Prometheus查询示例和监控分析流程，并未包含关于Grafana dashboard变量模板配置的具体说明。因此，我无法针对“Grafana dashboard 变... |  |
| RAG-08 | weak_related | true | 4504 | false | 0 | — | true | manual | — | 根据当前知识库的检索结果，未找到与 Alertmanager silence 创建相关的任何可用知识片段。因此，我无法基于知识库为您提供答案。建议您查阅 Alertmanager 官方文档或相关资源以获取帮助。 |  |
| RAG-09 | paraphrase | true | 9349 | true | 3 | 0.6406 .. 0.8130 | false | manual | — | 要确认监控数据来源，您应该调用 **`grafana/list_datasources`** 工具。 该工具会列出 Grafana 中所有已配置的数据源，返回信息包括数据源 ID、名称、类型（如 `prometheus`）、URL 以及是否... |  |
| RAG-10 | paraphrase | true | 19406 | true | 3 | 0.6379 .. 0.6703 | false | 5/5 |  | 根据知识库的内容，查询内存压力的核心思路是通过计算当前内存使用率来评估内存压力状况。 具体的分析查询思路包含在基础查询和深度分析两个维度中： 1. **内存使用率查询**（用于评估当前压力状况） 参考引用 [1] 和 [2]，使用以下 Pr... |  |

## Details

### RAG-01 - answerable

- question: 根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。
- should_answer: `true`
- expected_source_section: grafana/list_datasources
- completed: `true`
- duration_ms: `16334`
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
根据提供的上下文，`grafana/list_datasources` 工具的作用是：

**核心功能**
列出并获取 Grafana 实例中已配置的所有数据源的信息。如 [1] 中所述，其功能描述为“获取Grafana中配置的所有数据源”和“返回数据源的基本信息（ID、名称、类型、URL等）”。

**主要使用场景**
如 [1] 中所述，它主要用于：
1.  **系统分析前的数据源检查**：在进行监控分析之前，确认环境中有哪些数据源可用。
2.  **确认监控数据的来源**：特别是用于确认 Prometheus 数据源是否存在且配置正确。
3.  **多数据源环境的管理**：帮助管理和了解复杂环境中的多个数据源。

同时，根据 [2] 和 [3] 中的故障排查流程，它也是验证数据源可用性的第一步操作。
```

### RAG-02 - answerable

- question: `grafana/query_prometheus` 支持哪些参数？
- should_answer: `true`
- expected_source_section: grafana/query_prometheus 参数说明
- completed: `true`
- duration_ms: `21741`
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
根据提供的文档信息，`grafana/query_prometheus` 支持以下参数：

- **`query`**（必需）：要执行的 PromQL 查询语句。
- **`start`**（可选）：查询的开始时间。
- **`end`**（可选）：查询的结束时间。
- **`step`**（可选）：查询的步长。

其中 `query` 是必填参数，其余三个用于限定时间范围和采样粒度。
```

### RAG-03 - answerable_formula

- question: 知识库里 CPU 使用率的 PromQL 示例是什么？
- should_answer: `true`
- expected_source_section: CPU使用率查询
- completed: `true`
- duration_ms: `11343`
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
根据知识库中的信息，CPU 使用率的 PromQL 查询示例如下：

-   **整体 CPU 使用率**：
    ```promql
    100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
    ```
    这个查询计算的是所有实例的平均 CPU 使用率【1】。

-   **按实例分组的 CPU 使用率**：
    ```promql
    100 - (avg by (instance)(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
    ```
    这个查询会分别展示每个实例的 CPU 使用率【1】。

此外，在自动化分析流程中，也有一个查询过去 1 小时 CPU 使用率的变体【2】：
```promql
100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100)
```
```

### RAG-04 - answerable_deep_section

- question: 磁盘 7 天增长趋势预测用哪个 PromQL？
- should_answer: `true`
- expected_source_section: 趋势预测
- completed: `true`
- duration_ms: `14370`
- score_mode: `literal`
- expected_points:
  - [x] predict_linear
  - [x] node_filesystem_avail_bytes
  - [ ] 7*24*3600

retrieval:
- event #1
  - retrieved_document_count: `3`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.6305 .. 0.6481`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `4692` / max `6000`

answer:

```text
根据上下文 [1] 中“趋势预测”部分，预测磁盘 7 天增长趋势可使用内置的 `predict_linear` 函数。示例 PromQL 如下：

```promql
predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7 * 24 * 3600)
```

**说明：**  
- `node_filesystem_avail_bytes{fstype!="tmpfs"}` 选取所有非 tmpfs 文件系统的可用字节数（随时间减少表明磁盘使用在增长）。  
- `[24h]` 表示基于过去 24 小时的历史数据计算线性回归。  
- `7 * 24 * 3600` 即 7 天的秒数（604800 秒），表示预测从现在起 7 天后的可用空间值。  

如果结果为负数或远小于当前值，则预示磁盘可能在 7 天内耗尽。结合阈值，你还可以转换为使用率百分比来进行告警（例如 `(1 - predict(...) / node_filesystem_size_bytes) * 100`）。
```

### RAG-05 - answerable_long_context

- question: 系统健康报告模板包含哪些部分？
- should_answer: `true`
- expected_source_section: 系统健康报告模板
- completed: `false`
- duration_ms: `30323`
- score_mode: `literal`
- expected_points:
  - [ ] CPU
  - [ ] 内存
  - [ ] 磁盘
  - [ ] 网络
  - [ ] 综合评估

answer:

```text

```

### RAG-06 - unanswerable

- question: 根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `6605`
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
根据知识库的检索结果，当前没有找到与您问题相关的上下文信息，因此我无法基于知识库为您解答 MySQL InnoDB MVCC undo log 版本链的实现细节。如果您有其他非知识库类问题，我可以尝试提供通用帮助。
```

### RAG-07 - weak_related

- question: 根据知识库，Grafana dashboard 变量模板怎么配置？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `7639`
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
根据当前的知识库内容，主要涵盖了Grafana MCP工具的使用方法、Prometheus查询示例和监控分析流程，并未包含关于Grafana dashboard变量模板配置的具体说明。因此，我无法针对“Grafana dashboard 变量模板怎么配置？”这个问题提供答案。

如果您需要配置Grafana仪表板变量，一般步骤包括：进入仪表板设置，添加变量，选择变量类型（如查询、自定义、数据源等），并配置对应的查询语句或值列表。建议参考Grafana官方文档或现有仪表板的变量配置示例。
```

### RAG-08 - weak_related

- question: 根据知识库，Alertmanager 的 silence 应该怎么创建？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `4504`
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
根据当前知识库的检索结果，未找到与 Alertmanager silence 创建相关的任何可用知识片段。因此，我无法基于知识库为您提供答案。建议您查阅 Alertmanager 官方文档或相关资源以获取帮助。
```

### RAG-09 - paraphrase

- question: 我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？
- should_answer: `true`
- expected_source_section: 标准监控分析流程 / 数据源检查
- completed: `true`
- duration_ms: `9349`
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
要确认监控数据来源，您应该调用 **`grafana/list_datasources`** 工具。  

该工具会列出 Grafana 中所有已配置的数据源，返回信息包括数据源 ID、名称、类型（如 `prometheus`）、URL 以及是否为默认数据源等【1】。通过查看这些信息，您可以快速验证监控数据的确切来源和连接状态。
```

### RAG-10 - paraphrase

- question: 如果我要查内存压力，知识库给出的查询思路是什么？
- should_answer: `true`
- expected_source_section: 内存使用率查询 / 内存数据解释
- completed: `true`
- duration_ms: `19406`
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
根据知识库的内容，查询内存压力的核心思路是通过计算当前内存使用率来评估内存压力状况。

具体的分析查询思路包含在基础查询和深度分析两个维度中：

1.  **内存使用率查询**（用于评估当前压力状况）
    参考引用 [1] 和 [2]，使用以下 PromQL 语句查询内存使用率：
    ```promql
    (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
    ```
    这个公式通过计算“可用内存”占“总内存”比例的补数，得出当前的内存使用百分比。

2.  **多维度资源关联分析**（用于分析压力是否与其它资源有关）
    参考引用 [1]，在深度分析流程中，提供了 **CPU 和内存关联分析** 的查询思路，将两者放在一起评估：
    ```promql
    (
      (100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)) +
      ((1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100)
    ) / 2
    ```

**数据解释标准**（参考 [1]）：
在获得查询结果后，你可以按以下标准界定内存压力等级：
*   **正常范围**: 0-80%
*   **警告范围**: 80-95%
*   **危险范围**: 95-100%
```
