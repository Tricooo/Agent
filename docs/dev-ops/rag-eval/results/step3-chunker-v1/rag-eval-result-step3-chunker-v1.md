# RAG Eval Result

- generated_at: `2026-05-05 05:34:07`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。
>
> `retrieved` / `score` / `empty` 三列的 `—` 表示**没拿到成功的 ChatResponse metadata**，不等价于"无检索"。当前实现把 retrieval SSE 帧放在 `.call().chatResponse()` 返回之后才发，所以 LLM 调用失败时（即使 RAG 检索本身成功）三列都会是 `—`。要区分"检索失败"和"生成失败"，对照 `error` 列 / details 区 / backend log。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-01 | answerable | true | 14354 | true | 3 | 0.6001 .. 0.7641 | false | manual | — | 根据提供的上下文信息，`grafana/list_datasources` 工具的主要作用如下： 该工具的核心功能是**列出所有可用的数据源**[1]。它会获取Grafana实例中配置的所有数据源，并返回这些数据源的基本信息，例如ID、名称... |  |
| RAG-02 | answerable | true | 7046 | true | 3 | 0.6656 .. 0.7146 | false | 4/4 |  | `grafana/query_prometheus` 支持以下参数： - **query** (必需)：要执行的 PromQL 查询语句。 - **start** (可选)：查询的开始时间。 - **end** (可选)：查询的结束时间。 ... |  |
| RAG-03 | answerable_formula | true | 12025 | true | 3 | 0.6479 .. 0.6908 | false | 3/3 |  | 知识库中 CPU 使用率相关的 PromQL 示例如下： 1. **整体 CPU 使用率**（计算所有节点平均非空闲时间百分比）： ```promql 100 - (avg(irate(node_cpu_seconds_total{mode... |  |
| RAG-04 | answerable_deep_section | true | 25247 | true | 3 | 0.6305 .. 0.6481 | false | 3/3 |  | 根据提供的上下文，可以使用 **`predict_linear`** 函数来预测磁盘 7 天后的增长趋势。推荐的 PromQL 是[1]： ```promql predict_linear(node_filesystem_avail_byt... |  |
| RAG-05 | answerable_long_context | true | 16793 | true | 2 | 0.6499 .. 0.6907 | false | 5/5 |  | 根据提供的参考资料，系统健康报告模板通常包含以下核心部分： ### 📊 监控数据概览 这部分是报告的头部信息，用于标明分析的基本参数： - **监控时间范围** - **数据源** - **分析时间** ### 🖥️ 资源使用详情 这部分是... |  |
| RAG-06 | unanswerable | true | 5220 | false | 0 | — | true | manual | — | 根据当前提供的知识库，没有检索到与 MySQL InnoDB MVCC undo log 版本链实现相关的可用内容，因此我无法基于知识库来回答这个问题。如果你需要，我可以基于通用的数据库知识为你解释，但请注意那将不是来源于你指定的知识库。 |  |
| RAG-07 | weak_related | true | 7432 | false | 3 | 0.6185 .. 0.6645 | false | manual | — | 根据提供的知识库内容，没有找到与“Grafana dashboard 变量模板怎么配置”相关的信息。知识库主要介绍的是 Grafana MCP 工具的使用，包括列出数据源、执行 Prometheus 查询、监控分析流程和报告模板等，不涉及仪... |  |
| RAG-08 | weak_related | true | 3839 | false | 0 | — | true | manual | — | 根据当前知识库的检索结果，没有找到关于 Alertmanager 的 silence 创建方式的可用知识片段，因此无法基于知识库内容为您提供回答。建议您查阅 Alertmanager 官方文档或相关运维手册获取操作指导。 |  |
| RAG-09 | paraphrase | true | 9622 | true | 3 | 0.6406 .. 0.8130 | false | manual | — | 要确认监控数据来源，应该调用 **`grafana/list_datasources`** 工具。 这个工具的功能和适用场景如下（根据引用 [1]）： - **功能**：列出 Grafana 中配置的所有数据源，返回基本信息如 ID、名称、... |  |
| RAG-10 | paraphrase | true | 21808 | true | 3 | 0.6379 .. 0.6703 | false | 2/5 | 正常范围, 警告范围, 危险范围 | 根据知识库的指导，查询内存压力的主要思路是**通过 PromQL 查询内存使用率，并按预定义阈值评估压力状况**。具体步骤如下： 1. **核心查询语句** 使用以下 PromQL 获取当前内存使用率百分比（[1]）： ```promql ... |  |

## Details

### RAG-01 - answerable

- question: 根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。
- should_answer: `true`
- expected_source_section: grafana/list_datasources
- completed: `true`
- duration_ms: `14354`
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
  - context_chars: actual `4824` / max `6000`

answer:

```text
根据提供的上下文信息，`grafana/list_datasources` 工具的主要作用如下：

该工具的核心功能是**列出所有可用的数据源**[1]。它会获取Grafana实例中配置的所有数据源，并返回这些数据源的基本信息，例如ID、名称、类型和URL等[1]。

从使用场景来看，它主要用于**系统分析前的数据源检查**，以确认Prometheus等特定数据源是否可用，并帮助确认监控数据的来源或多数据源环境的管理[1]。此外，在自动化监控分析流程中，该工具通常作为**第一步**来执行，以验证数据源状态[2][3]。
```

### RAG-02 - answerable

- question: `grafana/query_prometheus` 支持哪些参数？
- should_answer: `true`
- expected_source_section: grafana/query_prometheus 参数说明
- completed: `true`
- duration_ms: `7046`
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
  - context_chars: actual `4824` / max `6000`

answer:

```text
`grafana/query_prometheus` 支持以下参数：

- **query** (必需)：要执行的 PromQL 查询语句。
- **start** (可选)：查询的开始时间。
- **end** (可选)：查询的结束时间。
- **step** (可选)：查询的步长。

这些参数用于向 Prometheus 数据源发起范围查询或即时查询，从而获取结构化的监控数据。
```

### RAG-03 - answerable_formula

- question: 知识库里 CPU 使用率的 PromQL 示例是什么？
- should_answer: `true`
- expected_source_section: CPU使用率查询
- completed: `true`
- duration_ms: `12025`
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
  - context_chars: actual `4824` / max `6000`

answer:

```text
知识库中 CPU 使用率相关的 PromQL 示例如下：

1. **整体 CPU 使用率**（计算所有节点平均非空闲时间百分比）：
   ```promql
   100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
   ```

2. **按实例分组的 CPU 使用率**（查看每个节点的 CPU 使用率）：
   ```promql
   100 - (avg by (instance)(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
   ```

如果需要查询过去 1 小时的 CPU 使用率，可以将时间窗口调整为 `[1h]`：
```promql
100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100)
```

参考来源：[1]
```

### RAG-04 - answerable_deep_section

- question: 磁盘 7 天增长趋势预测用哪个 PromQL？
- should_answer: `true`
- expected_source_section: 趋势预测
- completed: `true`
- duration_ms: `25247`
- score_mode: `literal`
- expected_points:
  - [x] predict_linear
  - [x] node_filesystem_avail_bytes
  - [x] 7*24*3600

retrieval:
- event #1
  - retrieved_document_count: `3`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.6305 .. 0.6481`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `4824` / max `6000`

answer:

```text
根据提供的上下文，可以使用 **`predict_linear`** 函数来预测磁盘 7 天后的增长趋势。推荐的 PromQL 是[1]：

```promql
predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600)
```

**解释：**
- `node_filesystem_avail_bytes{fstype!="tmpfs"}`：筛选出非 tmpfs 文件系统的可用字节数。
- `[24h]`：基于过去 24 小时的数据建立线性回归模型。
- `7*24*3600`：预测 7 天（604800 秒）后的值。
- 该查询返回预测的 **可用空间**（字节），如果数值接近 0 或负值，说明磁盘可能在 7 天内被写满。

**补充用法：**
若想直接预测 **磁盘使用率** 的趋势，可以改写为：
```promql
predict_linear(
  (1 - node_filesystem_avail_bytes{fstype!="tmpfs"} / node_filesystem_size_bytes{fstype!="tmpfs"}) * 100[24h],
  7*24*3600
)
```
这样会返回预计 7 天后的磁盘使用百分比，便于与告警阈值（如 85%、95%）直接对比。

在使用时，建议结合具体挂载点标签（如 `mountpoint="/data"`）进行过滤，以避免混合不同磁盘的预测结果。
```

### RAG-05 - answerable_long_context

- question: 系统健康报告模板包含哪些部分？
- should_answer: `true`
- expected_source_section: 系统健康报告模板
- completed: `true`
- duration_ms: `16793`
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
  - context_chars: actual `3067` / max `6000`

answer:

```text
根据提供的参考资料，系统健康报告模板通常包含以下核心部分：

### 📊 监控数据概览
这部分是报告的头部信息，用于标明分析的基本参数：
- **监控时间范围**
- **数据源**
- **分析时间**

### 🖥️ 资源使用详情
这部分是报告的核心，通常会对各关键组件逐项分析，每项都应包含具体数值、状态评估和趋势分析：

1.  **CPU 使用情况**
    -   平均使用率、峰值使用率
    -   状态评估（如正常、警告、危险，依据[1]所示标准：0-70% 正常，70-90% 警告，90-100% 危险）及趋势分析。

2.  **内存使用情况**
    -   当前使用率、可用内存
    -   状态评估（如正常、警告、危险，依据[1]所示标准：0-80% 正常，80-95% 警告，95-100% 危险）及使用趋势。

3.  **磁盘使用情况**
    -   使用率、可用空间
    -   状态评估（如正常、警告、危险，依据[1]所示标准：0-85% 正常，85-95% 警告，95-100% 危险）及空间预警。

4.  **网络流量情况**
    -   接收速率、发送速率
    -   状态评估及流量模式。

### 📈 综合评估
这部分用于给出总结和可执行的建议：
-   **系统健康度**：给出一个整体评分或结论。
-   **关键发现**：列出本次分析中最值得关注的问题点。
-   **优化建议**：针对发现的问题提出改进方案。
-   **告警建议**：说明是否需要设置或调整告警阈值。

对于模板无法覆盖的内容，我目前无法提供相关信息。
```

### RAG-06 - unanswerable

- question: 根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `5220`
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
根据当前提供的知识库，没有检索到与 MySQL InnoDB MVCC undo log 版本链实现相关的可用内容，因此我无法基于知识库来回答这个问题。如果你需要，我可以基于通用的数据库知识为你解释，但请注意那将不是来源于你指定的知识库。
```

### RAG-07 - weak_related

- question: 根据知识库，Grafana dashboard 变量模板怎么配置？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `7432`
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
  - context_chars: actual `4824` / max `6000`

answer:

```text
根据提供的知识库内容，没有找到与“Grafana dashboard 变量模板怎么配置”相关的信息。知识库主要介绍的是 Grafana MCP 工具的使用，包括列出数据源、执行 Prometheus 查询、监控分析流程和报告模板等，不涉及仪表板变量模板的配置方法。因此无法回答您这个问题。建议查阅 Grafana 官方文档中关于“Variables”（变量）或者“Dashboard templates”（仪表板模板）的部分。
```

### RAG-08 - weak_related

- question: 根据知识库，Alertmanager 的 silence 应该怎么创建？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `3839`
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
根据当前知识库的检索结果，没有找到关于 Alertmanager 的 silence 创建方式的可用知识片段，因此无法基于知识库内容为您提供回答。建议您查阅 Alertmanager 官方文档或相关运维手册获取操作指导。
```

### RAG-09 - paraphrase

- question: 我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？
- should_answer: `true`
- expected_source_section: 标准监控分析流程 / 数据源检查
- completed: `true`
- duration_ms: `9622`
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
  - context_chars: actual `4824` / max `6000`

answer:

```text
要确认监控数据来源，应该调用 **`grafana/list_datasources`** 工具。

这个工具的功能和适用场景如下（根据引用 [1]）：

-   **功能**：列出 Grafana 中配置的所有数据源，返回基本信息如 ID、名称、类型等。
-   **适用场景**：
    -   系统分析前的数据源检查
    -   **确认监控数据的来源**
    -   确认 Prometheus 数据源的可用性

这通常也是标准监控自动化分析流程的第一步 [1]。
```

### RAG-10 - paraphrase

- question: 如果我要查内存压力，知识库给出的查询思路是什么？
- should_answer: `true`
- expected_source_section: 内存使用率查询 / 内存数据解释
- completed: `true`
- duration_ms: `21808`
- score_mode: `literal`
- expected_points:
  - [x] node_memory_MemAvailable_bytes
  - [x] node_memory_MemTotal_bytes
  - [ ] 正常范围
  - [ ] 警告范围
  - [ ] 危险范围

retrieval:
- event #1
  - retrieved_document_count: `3`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - score_range: `0.6379 .. 0.6703`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `4824` / max `6000`

answer:

```text
根据知识库的指导，查询内存压力的主要思路是**通过 PromQL 查询内存使用率，并按预定义阈值评估压力状况**。具体步骤如下：

1. **核心查询语句**  
   使用以下 PromQL 获取当前内存使用率百分比（[1]）：
   ```promql
   (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
   ```
   该查询利用 `node_memory_MemAvailable_bytes`（可用内存）和 `node_memory_MemTotal_bytes`（总内存）计算出已用内存占比。

2. **关联分析（可选）**  
   在深度分析中，可以将内存与 CPU 结合，评估资源关联性（[1]）：
   ```promql
   (
     (100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)) +
     ((1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100)
   ) / 2
   ```

3. **数据解释标准**  
   将查询到的内存使用率按以下范围评估压力等级（[1]）：  
   - **正常**：0–80%  
   - **警告**：80–95%  
   - **危险**：95–100%  

4. **辅助查询**  
   如需查看具体内存使用量（GB），可使用（[2]）：
   ```promql
   (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024
   ```

**总结**  
知识库给出的查询思路就是先通过 PromQL 获取内存使用百分比，再套用阈值判断是否出现内存压力，并可根据实际需要结合其他资源指标进行关联分析或生成告警。
```
