# RAG Eval Result

- generated_at: `2026-05-08 04:43:24`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。
>
> `retrieved` / `score` / `empty` 三列的 `—` 表示**没拿到成功的 ChatResponse metadata**，不等价于"无检索"。当前实现把 retrieval SSE 帧放在 `.call().chatResponse()` 返回之后才发，所以 LLM 调用失败时（即使 RAG 检索本身成功）三列都会是 `—`。要区分"检索失败"和"生成失败"，对照 `error` 列 / details 区 / backend log。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-01 | answerable | true | 15689 | true | 4 | 0.0159 .. 0.0328 | false | manual | — | 根据 [1] 中的文档，**grafana/list_datasources** 工具的作用是： - **列出所有可用的数据源**：获取 Grafana 中已配置的所有数据源。 - **返回基本信息**：包括数据源的 ID、名称、类型、UR... |  |
| RAG-02 | answerable | true | 9283 | true | 4 | 0.0318 .. 0.0323 | false | 4/4 |  | 根据 Grafana MCP 工具文档，`grafana/query_prometheus` 支持以下参数： - **query** （必需） PromQL 查询语句，用于从 Prometheus 数据源获取数据。 - **start** ... |  |
| RAG-03 | answerable_formula | true | 15811 | true | 4 | 0.0310 .. 0.0323 | false | 3/3 |  | 根据知识库记录，CPU 使用率的 PromQL 示例有如下几种： **1. 整体 CPU 使用率（5分钟速率）** ```promql 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}... |  |
| RAG-04 | answerable_deep_section | true | 13581 | true | 4 | 0.0161 .. 0.0323 | false | 2/3 | 7*24*3600 | 根据你提供的上下文信息，磁盘 7 天增长趋势预测可以使用以下 PromQL： ```promql predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7 * 2... |  |
| RAG-05 | answerable_long_context | true | 24433 | true | 4 | 0.0156 .. 0.0164 | false | 5/5 |  | 系统健康报告模板包含以下几个主要部分，具体字段如下： - **监控数据概览**：监控时间范围、数据源、分析时间 [2] - **CPU 使用情况**：平均使用率、峰值使用率、状态评估、趋势分析 [2] - **内存使用情况**：当前使用率、... |  |
| RAG-06 | unanswerable | true | 5444 | false | 0 | — | true | manual | — | 根据当前提供的上下文信息，知识库中没有检索到与“MySQL InnoDB 的 MVCC undo log 版本链”相关的知识片段，因此我无法基于知识库为您回答这个问题。 |  |
| RAG-07 | weak_related | true | 8679 | false | 4 | 0.0159 .. 0.0328 | false | manual | — | 根据提供的知识库内容，其中主要介绍了 Grafana MCP 工具的使用方法（如列出数据源和查询 Prometheus 的示例），并没有包含关于 **Grafana dashboard 变量模板配置** 的相关文档。因此，我无法从现有知识库... |  |
| RAG-08 | weak_related | true | 6533 | false | 0 | — | true | manual | — | 根据当前知识库的检索结果，没有找到与 Alertmanager 的 silence 创建方法相关的可用内容，因此无法基于知识库为您提供解答。建议您查阅 Alertmanager 的官方文档或通过其他途径获取帮助。 |  |
| RAG-09 | paraphrase | true | 6007 | true | 4 | 0.0156 .. 0.0328 | false | manual | — | 要确认监控数据的来源，应该调用 **`grafana/list_datasources`** 工具。 这个工具会列出 Grafana 中已配置的所有数据源，并返回名称、类型、URL 等信息，方便你确认当前使用的是哪个 Prometheus ... |  |
| RAG-10 | paraphrase | true | 14403 | true | 4 | 0.0156 .. 0.0164 | false | 2/5 | 正常范围, 警告范围, 危险范围 | 根据知识库，查询内存压力的思路是使用Prometheus指标 `node_memory_MemAvailable_bytes` 和 `node_memory_MemTotal_bytes` 来计算内存使用率或已用内存量，从而评估当前内存压力... |  |
| RAG-11 | control_plaintext_cn_answerable | true | 10547 | true | 4 | 0.0156 .. 0.0164 | false | 2/2 |  | 根据控制组文档，冷数据迁移阈值是“连续二十一天未访问”，归档窗口编号为 ARCHIVE-CN-7319。这两个值分别在文档中明确写出（参见 [1] 和 [2]）。 |  |
| RAG-12 | control_plaintext_cn_weak_related | true | 5181 | false | 4 | 0.0156 .. 0.0164 | false | manual | — | 根据控制组文档的说明，**文档中没有指定归档审批人**，也没有提供审批人姓名、审批系统地址或相关组织架构信息。因此，我无法回答具体的审批人是谁。 |  |
| RAG-13 | control_plaintext_en_answerable | true | 9001 | true | 4 | 0.0310 .. 0.0323 | false | 2/2 |  | According to the control document, the fallback queue name is **silver-river-42**, and the retry budget for that fallbac... |  |
| RAG-14 | control_plaintext_en_weak_related | true | 7466 | false | 4 | 0.0308 .. 0.0325 | false | manual | — | According to the control document, no cloud provider is specified for the fallback queue. The document explicitly states... |  |

## Details

### RAG-01 - answerable

- question: 根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。
- should_answer: `true`
- expected_source_section: grafana/list_datasources
- completed: `true`
- duration_ms: `15689`
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
  - score_range: `0.0159 .. 0.0328`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2310` / max `6000`

answer:

```text
根据 [1] 中的文档，**grafana/list_datasources** 工具的作用是：

- **列出所有可用的数据源**：获取 Grafana 中已配置的所有数据源。
- **返回基本信息**：包括数据源的 ID、名称、类型、URL 等。
- **用于系统分析前的检查**：例如确认 Prometheus 数据源是否可用，了解监控数据的来源。
- **管理多数据源环境**：方便识别和区分不同的数据源。

其返回信息类似：

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

因此，该工具主要用于在自动化监控分析流程的第一步——**数据源检查**。
```

### RAG-02 - answerable

- question: `grafana/query_prometheus` 支持哪些参数？
- should_answer: `true`
- expected_source_section: grafana/query_prometheus 参数说明
- completed: `true`
- duration_ms: `9283`
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
  - score_range: `0.0318 .. 0.0323`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2199` / max `6000`

answer:

```text
根据 Grafana MCP 工具文档，`grafana/query_prometheus` 支持以下参数：

- **query** （必需）
  PromQL 查询语句，用于从 Prometheus 数据源获取数据。

- **start** （可选）
  查询的开始时间。不指定时默认为当前时间附近的一个默认起始点。

- **end** （可选）
  查询的结束时间。不指定时默认为当前时间。

- **step** （可选）
  查询的步长，用于控制返回数据点的密度（例如 `15s`、`1m` 等）。不指定时由系统自动计算合适步长。
```

### RAG-03 - answerable_formula

- question: 知识库里 CPU 使用率的 PromQL 示例是什么？
- should_answer: `true`
- expected_source_section: CPU使用率查询
- completed: `true`
- duration_ms: `15811`
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
  - score_range: `0.0310 .. 0.0323`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2163` / max `6000`

answer:

```text
根据知识库记录，CPU 使用率的 PromQL 示例有如下几种：

**1. 整体 CPU 使用率（5分钟速率）**
```promql
100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
```

**2. 按实例分组的 CPU 使用率（5分钟速率）**
```promql
100 - (avg by (instance)(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
```

**3. 整体 CPU 使用率（1小时速率）**（在自动化分析流程中出现）
```promql
100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100)
```

以上查询均基于 `node_cpu_seconds_total` 指标，利用空闲模式（`mode="idle"`）的时间占比来计算 CPU 使用率。可以根据需要调整时间窗口（如 `[5m]`、`[1h]` 等）。
```

### RAG-04 - answerable_deep_section

- question: 磁盘 7 天增长趋势预测用哪个 PromQL？
- should_answer: `true`
- expected_source_section: 趋势预测
- completed: `true`
- duration_ms: `13581`
- score_mode: `literal`
- expected_points:
  - [x] predict_linear
  - [x] node_filesystem_avail_bytes
  - [ ] 7*24*3600

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.0161 .. 0.0323`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2217` / max `6000`

answer:

```text
根据你提供的上下文信息，磁盘 7 天增长趋势预测可以使用以下 PromQL：

```promql
predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7 * 24 * 3600)
```

**解释**：
- `node_filesystem_avail_bytes{fstype!="tmpfs"}` 选取非 tmpfs 文件系统的可用字节数。
- `[24h]` 表示使用过去 24 小时的数据作为拟合区间。
- `predict_linear(...)` 基于线性回归预测在 **7×24×3600 秒**（即 7 天）后的值。
- 预测结果为负数表示磁盘可用空间可能耗尽；数值越小（或负值越大）表明增长越快，风险越高。

如果需要进一步分析，可以对该预测值除以磁盘总容量来获得使用率预测，或结合 `max_over_time` 等函数评估趋势波动。
```

### RAG-05 - answerable_long_context

- question: 系统健康报告模板包含哪些部分？
- should_answer: `true`
- expected_source_section: 系统健康报告模板
- completed: `true`
- duration_ms: `24433`
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
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `1985` / max `6000`

answer:

```text
系统健康报告模板包含以下几个主要部分，具体字段如下：

- **监控数据概览**：监控时间范围、数据源、分析时间 [2]
- **CPU 使用情况**：平均使用率、峰值使用率、状态评估、趋势分析 [2]
- **内存使用情况**：当前使用率、可用内存、状态评估、使用趋势 [1][2]
- **磁盘使用情况**：使用率、可用空间、状态评估、空间预警 [1]
- **网络流量情况**：接收速率、发送速率、状态评估、流量模式 [1]
- **综合评估**：系统健康度、关键发现、优化建议、告警建议 [1]

模板整体标题为 **“系统运行状态分析报告”**，每个模块都用相应图标标识，最终给出可操作的评估与建议。
```

### RAG-06 - unanswerable

- question: 根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `5444`
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
根据当前提供的上下文信息，知识库中没有检索到与“MySQL InnoDB 的 MVCC undo log 版本链”相关的知识片段，因此我无法基于知识库为您回答这个问题。
```

### RAG-07 - weak_related

- question: 根据知识库，Grafana dashboard 变量模板怎么配置？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `8679`
- score_mode: `manual`
- expected_points:
  - 知识库未涉及 dashboard 变量模板
  - 无法基于当前知识库回答

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.0159 .. 0.0328`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `3103` / max `6000`

answer:

```text
根据提供的知识库内容，其中主要介绍了 Grafana MCP 工具的使用方法（如列出数据源和查询 Prometheus 的示例），并没有包含关于 **Grafana dashboard 变量模板配置** 的相关文档。因此，我无法从现有知识库中提取答案来解答您的问题。

如果您需要了解这方面的内容，建议查阅 Grafana 官方文档或提供更具体的资料，我可以再帮您分析。
```

### RAG-08 - weak_related

- question: 根据知识库，Alertmanager 的 silence 应该怎么创建？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `6533`
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
根据当前知识库的检索结果，没有找到与 Alertmanager 的 silence 创建方法相关的可用内容，因此无法基于知识库为您提供解答。建议您查阅 Alertmanager 的官方文档或通过其他途径获取帮助。
```

### RAG-09 - paraphrase

- question: 我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？
- should_answer: `true`
- expected_source_section: 标准监控分析流程 / 数据源检查
- completed: `true`
- duration_ms: `6007`
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
  - score_range: `0.0156 .. 0.0328`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2310` / max `6000`

answer:

```text
要确认监控数据的来源，应该调用 **`grafana/list_datasources`** 工具。

这个工具会列出 Grafana 中已配置的所有数据源，并返回名称、类型、URL 等信息，方便你确认当前使用的是哪个 Prometheus 数据源（或其他类型的数据源）[1]。
```

### RAG-10 - paraphrase

- question: 如果我要查内存压力，知识库给出的查询思路是什么？
- should_answer: `true`
- expected_source_section: 内存使用率查询 / 内存数据解释
- completed: `true`
- duration_ms: `14403`
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
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2211` / max `6000`

answer:

```text
根据知识库，查询内存压力的思路是使用Prometheus指标 `node_memory_MemAvailable_bytes` 和 `node_memory_MemTotal_bytes` 来计算内存使用率或已用内存量，从而评估当前内存压力状况。

- 查询当前内存使用率（%）[1]：
  ```promql
  (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
  ```
  该查询结果越高，代表内存压力越大。

- 查询已用内存量（GB）[4]：
  ```promql
  (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024
  ```
  通过可用内存与总内存的差值，直观反映实际被占用的内存大小。

你可以通过Grafana的 `query_prometheus` 工具执行上述查询，并结合时间范围和具体节点标签来获取详细的内存压力数据。
```

### RAG-11 - control_plaintext_cn_answerable

- question: 控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？
- should_answer: `true`
- expected_source_section: control-cn-low-newline.txt / 容量治理控制组
- completed: `true`
- duration_ms: `10547`
- score_mode: `literal`
- expected_points:
  - [x] 连续二十一天未访问
  - [x] ARCHIVE-CN-7319

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `1061` / max `6000`

answer:

```text
根据控制组文档，冷数据迁移阈值是“连续二十一天未访问”，归档窗口编号为 ARCHIVE-CN-7319。这两个值分别在文档中明确写出（参见 [1] 和 [2]）。
```

### RAG-12 - control_plaintext_cn_weak_related

- question: 控制组文档指定的归档审批人是谁？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `5181`
- score_mode: `manual`
- expected_points:
  - 文档没有指定审批人
  - 无法基于当前知识库回答

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `1045` / max `6000`

answer:

```text
根据控制组文档的说明，**文档中没有指定归档审批人**，也没有提供审批人姓名、审批系统地址或相关组织架构信息。因此，我无法回答具体的审批人是谁。
```

### RAG-13 - control_plaintext_en_answerable

- question: According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?
- should_answer: `true`
- expected_source_section: control-en-long-paragraph.txt / webhook fallback runbook
- completed: `true`
- duration_ms: `9001`
- score_mode: `literal`
- expected_points:
  - [x] silver-river-42
  - [x] seven attempts within eighteen minutes

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.0310 .. 0.0323`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `5545` / max `6000`

answer:

```text
According to the control document, the fallback queue name is **silver-river-42**, and the retry budget for that fallback path is **seven attempts within eighteen minutes**[1][2].
```

### RAG-14 - control_plaintext_en_weak_related

- question: According to the control document, which cloud provider hosts the fallback queue?
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `7466`
- score_mode: `manual`
- expected_points:
  - does not specify which cloud provider
  - cannot be answered from this document

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.0308 .. 0.0325`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `5550` / max `6000`

answer:

```text
According to the control document, no cloud provider is specified for the fallback queue. The document explicitly states that it does not identify a cloud provider, region, or related details [1][2]. Therefore, I cannot answer which cloud provider hosts it.
```
