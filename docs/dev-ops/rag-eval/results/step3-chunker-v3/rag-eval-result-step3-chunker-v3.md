# RAG Eval Result

- generated_at: `2026-05-05 22:17:49`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。
>
> `retrieved` / `score` / `empty` 三列的 `—` 表示**没拿到成功的 ChatResponse metadata**，不等价于"无检索"。当前实现把 retrieval SSE 帧放在 `.call().chatResponse()` 返回之后才发，所以 LLM 调用失败时（即使 RAG 检索本身成功）三列都会是 `—`。要区分"检索失败"和"生成失败"，对照 `error` 列 / details 区 / backend log。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-01 | answerable | true | 14322 | true | 4 | 0.6561 .. 0.8057 | false | manual | — | 根据知识库中的描述，`grafana/list_datasources` 工具的主要作用是： - **列出所有可用的数据源**：获取 Grafana 中配置的全部数据源信息，包括 ID、名称、类型、URL、访问方式以及是否为默认数据源等。 ... |  |
| RAG-02 | answerable | true | 6807 | true | 4 | 0.6919 .. 0.8033 | false | 4/4 |  | 根据文档 [1] 中的说明，`grafana/query_prometheus` 支持以下参数： - **`query`**（必需）：PromQL 查询语句。 - **`start`**（可选）：查询开始时间。 - **`end`**（可选... |  |
| RAG-03 | answerable_formula | true | 8505 | true | 4 | 0.6745 .. 0.7252 | false | 3/3 |  | 根据知识库中的记录，CPU 使用率的 PromQL 示例如下： **整体 CPU 使用率** ```promql 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 10... |  |
| RAG-04 | answerable_deep_section | true | 12975 | true | 4 | 0.6481 .. 0.7406 | false | 3/3 |  | 根据提供的文档，磁盘 7 天增长趋势预测的 PromQL 为： ```promql predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) `... |  |
| RAG-05 | answerable_long_context | true | 22247 | true | 4 | 0.6178 .. 0.7195 | false | 5/5 |  | 系统健康报告模板由以下几个主要部分组成，每个部分都包含具体的监控指标和评估信息[2][1]： - **监控数据概览**：包含监控时间范围、数据源信息和分析时间[2]。 - **CPU 使用情况**：包含平均使用率、峰值使用率、状态评估和趋势... |  |
| RAG-06 | unanswerable | true | 5117 | false | 0 | — | true | manual | — | 当前知识库中没有相关的可用上下文，无法基于知识库回答你关于 MySQL InnoDB MVCC undo log 版本链实现的问题。如果你有更具体的文档或想从通用知识角度探讨，可以告诉我，我可以基于一般技术理解为你解释。 |  |
| RAG-07 | weak_related | true | 7853 | false | 4 | 0.6464 .. 0.6726 | false | manual | — | 根据提供的知识库内容，主要介绍了 Grafana MCP 工具（如 `list_datasources`、`query_prometheus`）的使用方法，但并没有涉及如何在 Grafana 仪表板（dashboard）中配置变量模板的相关... |  |
| RAG-08 | weak_related | true | 3864 | false | 0 | — | true | manual | — | 当前知识库中没有与 Alertmanager 创建 silence 相关的操作步骤或上下文信息，因此我无法基于知识库来回答这个问题。如果需要，可以参考 Alertmanager 的官方文档或通过命令 `amtool silence add`... |  |
| RAG-09 | paraphrase | true | 6727 | true | 4 | 0.6810 .. 0.8186 | false | manual | — | 要确认监控数据来源，应该使用 **`grafana/list_datasources`** 工具。 这个工具能列出 Grafana 中所有已配置的数据源，返回数据源 ID、名称、类型和 URL 等信息，帮助你确认 Prometheus 等监... |  |
| RAG-10 | paraphrase | true | 13435 | true | 4 | 0.6684 .. 0.6819 | false | 2/5 | 正常范围, 警告范围, 危险范围 | 根据知识库的指引，查询内存压力的思路是使用以下 PromQL 查询来计算**内存使用率**，从而评估内存压力状况： ```promql (1 - (node_memory_MemAvailable_bytes / node_memory_M... |  |

## Details

### RAG-01 - answerable

- question: 根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。
- should_answer: `true`
- expected_source_section: grafana/list_datasources
- completed: `true`
- duration_ms: `14322`
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

answer:

```text
根据知识库中的描述，`grafana/list_datasources` 工具的主要作用是：

- **列出所有可用的数据源**：获取 Grafana 中配置的全部数据源信息，包括 ID、名称、类型、URL、访问方式以及是否为默认数据源等。  
- **支持数据源检查与确认**：常用于进行系统分析前，快速验证目标数据源（例如 Prometheus）是否已正确配置并可用。  
- **辅助多数据源环境管理**：帮助用户了解当前监控数据的来源，便于后续查询和分析。

返回的数据示例格式如下（来自上下文 [1]）：
```json
{
  "id": 1,
  "name": "Prometheus",
  "type": "prometheus",
  "url": "http://prometheus:9090",
  "access": "proxy",
  "isDefault": true
}
```
```

### RAG-02 - answerable

- question: `grafana/query_prometheus` 支持哪些参数？
- should_answer: `true`
- expected_source_section: grafana/query_prometheus 参数说明
- completed: `true`
- duration_ms: `6807`
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

answer:

```text
根据文档 [1] 中的说明，`grafana/query_prometheus` 支持以下参数：

- **`query`**（必需）：PromQL 查询语句。
- **`start`**（可选）：查询开始时间。
- **`end`**（可选）：查询结束时间。
- **`step`**（可选）：查询步长。
```

### RAG-03 - answerable_formula

- question: 知识库里 CPU 使用率的 PromQL 示例是什么？
- should_answer: `true`
- expected_source_section: CPU使用率查询
- completed: `true`
- duration_ms: `8505`
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

answer:

```text
根据知识库中的记录，CPU 使用率的 PromQL 示例如下：

**整体 CPU 使用率**  
```promql
100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
```

**按实例分组的 CPU 使用率**  
```promql
100 - (avg by (instance)(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
```

这些查询利用 `node_cpu_seconds_total` 指标，通过计算空闲 CPU 时间的比例反推使用率。如果需要调整时间范围（例如 `[5m]` 可改为 `[1h]` 等），可以按实际监控需求修改。参考来源：[1]。
```

### RAG-04 - answerable_deep_section

- question: 磁盘 7 天增长趋势预测用哪个 PromQL？
- should_answer: `true`
- expected_source_section: 趋势预测
- completed: `true`
- duration_ms: `12975`
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

answer:

```text
根据提供的文档，磁盘 7 天增长趋势预测的 PromQL 为：

```promql
predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600)
```

这个查询会基于过去 24 小时的文件系统可用字节数变化趋势，预测未来 7 天（`7*24*3600` 秒）后的可用字节数。若希望预测磁盘使用率的增长，可结合当前容量计算，例如：

```promql
(1 - predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) / node_filesystem_size_bytes{fstype!="tmpfs"}) * 100
```

推荐直接使用第一个查询来评估存储空间是否会在未来一周被耗尽。
```

### RAG-05 - answerable_long_context

- question: 系统健康报告模板包含哪些部分？
- should_answer: `true`
- expected_source_section: 系统健康报告模板
- completed: `true`
- duration_ms: `22247`
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

answer:

```text
系统健康报告模板由以下几个主要部分组成，每个部分都包含具体的监控指标和评估信息[2][1]：

- **监控数据概览**：包含监控时间范围、数据源信息和分析时间[2]。
- **CPU 使用情况**：包含平均使用率、峰值使用率、状态评估和趋势分析[2]。
- **内存使用情况**：包含当前使用率、可用内存、状态评估和使用趋势[1][2]。
- **磁盘使用情况**：包含使用率、可用空间、状态评估和空间预警[1]。
- **网络流量情况**：包含接收速率、发送速率、状态评估和流量模式[1]。
- **综合评估**：包含系统健康度、关键发现、优化建议和告警建议[1]。

这些部分共同构成一份结构化的系统运行状态分析报告。
```

### RAG-06 - unanswerable

- question: 根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `5117`
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
当前知识库中没有相关的可用上下文，无法基于知识库回答你关于 MySQL InnoDB MVCC undo log 版本链实现的问题。如果你有更具体的文档或想从通用知识角度探讨，可以告诉我，我可以基于一般技术理解为你解释。
```

### RAG-07 - weak_related

- question: 根据知识库，Grafana dashboard 变量模板怎么配置？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `7853`
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

answer:

```text
根据提供的知识库内容，主要介绍了 Grafana MCP 工具（如 `list_datasources`、`query_prometheus`）的使用方法，但并没有涉及如何在 Grafana 仪表板（dashboard）中配置变量模板的相关信息，因此我无法依据知识库回答您的问题。建议查阅 Grafana 官方文档中关于“变量”（Variables）的配置指南。
```

### RAG-08 - weak_related

- question: 根据知识库，Alertmanager 的 silence 应该怎么创建？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `3864`
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
当前知识库中没有与 Alertmanager 创建 silence 相关的操作步骤或上下文信息，因此我无法基于知识库来回答这个问题。如果需要，可以参考 Alertmanager 的官方文档或通过命令 `amtool silence add` 等方式了解具体创建方法。
```

### RAG-09 - paraphrase

- question: 我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？
- should_answer: `true`
- expected_source_section: 标准监控分析流程 / 数据源检查
- completed: `true`
- duration_ms: `6727`
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

answer:

```text
要确认监控数据来源，应该使用 **`grafana/list_datasources`** 工具。

这个工具能列出 Grafana 中所有已配置的数据源，返回数据源 ID、名称、类型和 URL 等信息，帮助你确认 Prometheus 等监控数据来源是否可用以及配置是否正确 [1]。
```

### RAG-10 - paraphrase

- question: 如果我要查内存压力，知识库给出的查询思路是什么？
- should_answer: `true`
- expected_source_section: 内存使用率查询 / 内存数据解释
- completed: `true`
- duration_ms: `13435`
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

answer:

```text
根据知识库的指引，查询内存压力的思路是使用以下 PromQL 查询来计算**内存使用率**，从而评估内存压力状况：

```promql
(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
```

该查询的含义是：用 `1` 减去 `可用内存` 与 `总内存` 的比值，再转换为百分比，即可得到当前内存已使用的百分比。内存使用率越高，代表内存压力越大。

另外，知识库也提供了**查看已用内存量**的辅助查询（单位：GB）：

```promql
(node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024
```

这可以帮你从绝对值角度了解内存消耗情况。在实际分析时，建议结合业务场景、历史基线对比，并关注趋势变化而非瞬时值 [3]。
```
