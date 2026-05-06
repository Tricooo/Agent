# RAG Eval Result

- generated_at: `2026-05-06 05:03:20`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。
>
> `retrieved` / `score` / `empty` 三列的 `—` 表示**没拿到成功的 ChatResponse metadata**，不等价于"无检索"。当前实现把 retrieval SSE 帧放在 `.call().chatResponse()` 返回之后才发，所以 LLM 调用失败时（即使 RAG 检索本身成功）三列都会是 `—`。要区分"检索失败"和"生成失败"，对照 `error` 列 / details 区 / backend log。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-01 | answerable | true | 12831 | true | 4 | 0.6561 .. 0.8057 | false | manual | — | 根据知识库中的说明，**grafana/list_datasources** 工具的主要作用是**列出 Grafana 中所有已配置的数据源**。它返回每个数据源的基本信息（如 ID、名称、类型、URL、访问方式等），通常用于： - 在系统... |  |
| RAG-02 | answerable | true | 8326 | true | 4 | 0.6919 .. 0.8033 | false | 4/4 |  | `grafana/query_prometheus` 支持以下参数： \| 参数 \| 说明 \| 是否必填 \| \|------\|------\|----------\| \| `query` \| PromQL 查询语句 \| 必填 \| \| `start... |  |
| RAG-03 | answerable_formula | true | 6357 | true | 4 | 0.6745 .. 0.7252 | false | 3/3 |  | 根据知识库中的记录[1]，CPU 使用率的 PromQL 示例为： **整体 CPU 使用率：** ```promql 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) *... |  |
| RAG-04 | answerable_deep_section | true | 12398 | true | 4 | 0.6481 .. 0.7406 | false | 3/3 |  | 根据提供的上下文，磁盘 7 天增长趋势预测可以使用以下 PromQL： ```promql predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*360... |  |
| RAG-05 | answerable_long_context | true | 15269 | true | 4 | 0.6178 .. 0.7195 | false | 5/5 |  | 系统健康报告模板包含以下部分： 1. **📊 监控数据概览** - 监控时间范围 - 数据源信息 - 分析时间 2. **🖥️ CPU 使用情况** - 平均使用率 - 峰值使用率 - 状态评估 - 趋势分析 3. **💾 内存使用情况**... |  |
| RAG-06 | unanswerable | true | 4621 | false | 0 | — | true | manual | — | 根据当前知识库的检索结果，未能找到与您问题相关的知识片段。因此，我无法基于知识库为您解答 MySQL InnoDB 的 MVCC undo log 版本链实现细节。建议您查阅官方文档或相关技术资料获取详细说明。 |  |
| RAG-07 | weak_related | true | 6009 | false | 4 | 0.6464 .. 0.6726 | false | manual | — | 根据提供的知识库内容，目前仅包含 Grafana MCP 工具的使用说明，如 `list_datasources` 和 `query_prometheus`，未涉及 Grafana Dashboard 变量模板的配置方法。因此，我无法基于现... |  |
| RAG-08 | weak_related | true | 3935 | false | 0 | — | true | manual | — | 根据当前提供的知识库内容，未检索到与 Alertmanager 的 silence 创建相关的信息，因此我无法基于知识库回答该问题。建议查阅 Alertmanager 官方文档或相关配置参考以获取准确的操作步骤。 |  |
| RAG-09 | paraphrase | true | 5772 | true | 4 | 0.6810 .. 0.8186 | false | manual | — | 要确认监控数据来源，应使用 **`grafana/list_datasources`** 工具[1]。它可以列出 Grafana 中配置的所有数据源（如 Prometheus），并返回基本信息（ID、名称、类型、URL 等），帮助你核实数据... |  |
| RAG-10 | paraphrase | true | 12102 | true | 4 | 0.6684 .. 0.6819 | false | 2/5 | 正常范围, 警告范围, 危险范围 | 根据知识库，查询内存压力的思路主要是通过 **PromQL 查询内存使用率** 来评估当前的内存压力状况。具体包含以下两种常用查询： 1. **查询内存使用率（百分比）** ``` (1 - (node_memory_MemAvailabl... |  |
| RAG-11 | control_plaintext_cn_answerable | true | 3890 | true | 4 | 0.7175 .. 0.7763 | false | 2/2 |  | 根据控制组文档，冷数据迁移阈值是**连续二十一天未访问**。归档窗口编号是 **ARCHIVE-CN-7319**。 |  |
| RAG-12 | control_plaintext_cn_weak_related | true | 5501 | false | 4 | 0.6475 .. 0.7169 | false | manual | — | 根据控制组文档的内容，文档中并未指定归档审批人是谁，也未提供任何审批人姓名或审批系统地址。文档只说明了容量治理策略、归档窗口编号（ARCHIVE-CN-7319）、冷数据迁移阈值、单批提交上限等参数，无法回答审批人相关信息。 |  |
| RAG-13 | control_plaintext_en_answerable | true | 7376 | true | 4 | 0.7632 .. 0.7906 | false | 2/2 |  | According to the control document, the fallback queue name is **silver-river-42**, and the retry budget for that fallbac... |  |
| RAG-14 | control_plaintext_en_weak_related | true | 5451 | false | 4 | 0.7432 .. 0.7799 | false | manual | — | According to the control document, the cloud provider that hosts the fallback queue is not specified. The document expli... |  |

## Details

### RAG-01 - answerable

- question: 根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。
- should_answer: `true`
- expected_source_section: grafana/list_datasources
- completed: `true`
- duration_ms: `12831`
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
根据知识库中的说明，**grafana/list_datasources** 工具的主要作用是**列出 Grafana 中所有已配置的数据源**。它返回每个数据源的基本信息（如 ID、名称、类型、URL、访问方式等），通常用于：

- 在系统分析前检查可用的数据源；
- 确认监控数据的来源（例如验证 Prometheus 数据源是否可用）；
- 管理多数据源环境。

这些信息在上下文 [1] 中有明确描述。
```

### RAG-02 - answerable

- question: `grafana/query_prometheus` 支持哪些参数？
- should_answer: `true`
- expected_source_section: grafana/query_prometheus 参数说明
- completed: `true`
- duration_ms: `8326`
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
`grafana/query_prometheus` 支持以下参数：

| 参数 | 说明 | 是否必填 |
|------|------|----------|
| `query` | PromQL 查询语句 | 必填 |
| `start` | 查询开始时间 | 可选 |
| `end` | 查询结束时间 | 可选 |
| `step` | 查询步长（数据点间隔） | 可选 |

这些参数可用于执行 PromQL 查询，并支持指定时间范围和采样步长，从而获取结构化的监控数据。
```

### RAG-03 - answerable_formula

- question: 知识库里 CPU 使用率的 PromQL 示例是什么？
- should_answer: `true`
- expected_source_section: CPU使用率查询
- completed: `true`
- duration_ms: `6357`
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
根据知识库中的记录[1]，CPU 使用率的 PromQL 示例为：

**整体 CPU 使用率：**
```promql
100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
```

**按实例分组的 CPU 使用率：**
```promql
100 - (avg by (instance)(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
```
```

### RAG-04 - answerable_deep_section

- question: 磁盘 7 天增长趋势预测用哪个 PromQL？
- should_answer: `true`
- expected_source_section: 趋势预测
- completed: `true`
- duration_ms: `12398`
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
根据提供的上下文，磁盘 7 天增长趋势预测可以使用以下 PromQL：

```promql
predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600)
```

**解释：**
- `node_filesystem_avail_bytes{fstype!="tmpfs"}`：排除 `tmpfs` 类型的文件系统后，当前的可用磁盘空间（以字节为单位）。
- `[24h]`：取过去 24 小时的数据作为时间序列的样本范围。
- `predict_linear(... , 7*24*3600)`：基于过去 24 小时的变化趋势，预测未来 7 天（`7*24*3600` 秒）后的数值。该函数利用线性回归推算未来的值，结果为 `7` 天后预计的可用磁盘空间（单位：字节）。

**参考来源：** [1] grafana-mcp-tools-guide.md 中“磁盘使用增长趋势”一节。
```

### RAG-05 - answerable_long_context

- question: 系统健康报告模板包含哪些部分？
- should_answer: `true`
- expected_source_section: 系统健康报告模板
- completed: `true`
- duration_ms: `15269`
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
系统健康报告模板包含以下部分：

1. **📊 监控数据概览**  
   - 监控时间范围  
   - 数据源信息  
   - 分析时间  

2. **🖥️ CPU 使用情况**  
   - 平均使用率  
   - 峰值使用率  
   - 状态评估  
   - 趋势分析  

3. **💾 内存使用情况**  
   - 当前使用率  
   - 可用内存  
   - 状态评估  
   - 使用趋势  

4. **💿 磁盘使用情况**  
   - 使用率  
   - 可用空间  
   - 状态评估  
   - 空间预警  

5. **🌐 网络流量情况**  
   - 接收速率  
   - 发送速率  
   - 状态评估  
   - 流量模式  

6. **📈 综合评估**  
   - 系统健康度  
   - 关键发现  
   - 优化建议  
   - 告警建议
```

### RAG-06 - unanswerable

- question: 根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `4621`
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
根据当前知识库的检索结果，未能找到与您问题相关的知识片段。因此，我无法基于知识库为您解答 MySQL InnoDB 的 MVCC undo log 版本链实现细节。建议您查阅官方文档或相关技术资料获取详细说明。
```

### RAG-07 - weak_related

- question: 根据知识库，Grafana dashboard 变量模板怎么配置？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `6009`
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
根据提供的知识库内容，目前仅包含 Grafana MCP 工具的使用说明，如 `list_datasources` 和 `query_prometheus`，未涉及 Grafana Dashboard 变量模板的配置方法。因此，我无法基于现有知识回答您的问题。建议查阅 Grafana 官方文档或相关配置指南。
```

### RAG-08 - weak_related

- question: 根据知识库，Alertmanager 的 silence 应该怎么创建？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `3935`
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
根据当前提供的知识库内容，未检索到与 Alertmanager 的 silence 创建相关的信息，因此我无法基于知识库回答该问题。建议查阅 Alertmanager 官方文档或相关配置参考以获取准确的操作步骤。
```

### RAG-09 - paraphrase

- question: 我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？
- should_answer: `true`
- expected_source_section: 标准监控分析流程 / 数据源检查
- completed: `true`
- duration_ms: `5772`
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
要确认监控数据来源，应使用 **`grafana/list_datasources`** 工具[1]。它可以列出 Grafana 中配置的所有数据源（如 Prometheus），并返回基本信息（ID、名称、类型、URL 等），帮助你核实数据是否来自预期的端点和类型[1][3]。
```

### RAG-10 - paraphrase

- question: 如果我要查内存压力，知识库给出的查询思路是什么？
- should_answer: `true`
- expected_source_section: 内存使用率查询 / 内存数据解释
- completed: `true`
- duration_ms: `12102`
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
根据知识库，查询内存压力的思路主要是通过 **PromQL 查询内存使用率** 来评估当前的内存压力状况。具体包含以下两种常用查询：

1. **查询内存使用率（百分比）**  
   ```
   (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
   ```
   该表达式利用 `MemAvailable` 和 `MemTotal` 两个指标，计算出系统已用内存占总内存的百分比，直观反映内存压力程度 [1]。

2. **查询已用内存空间（GB）**  
   ```
   (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024
   ```
   该查询直接输出已消耗的内存量（单位 GB），便于结合业务场景进行容量评估 [4]。

实际分析时建议：
- 关注趋势而非瞬时值，可结合时间范围分析历史变化 [3]。
- 可与 CPU、磁盘等指标关联分析，定位综合性能瓶颈 [2]。
- 输出结果时结合具体数值和状态评估，给出可操作建议 [3]。
```

### RAG-11 - control_plaintext_cn_answerable

- question: 控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？
- should_answer: `true`
- expected_source_section: control-cn-low-newline.txt / 容量治理控制组
- completed: `true`
- duration_ms: `3890`
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

answer:

```text
根据控制组文档，冷数据迁移阈值是**连续二十一天未访问**。归档窗口编号是 **ARCHIVE-CN-7319**。
```

### RAG-12 - control_plaintext_cn_weak_related

- question: 控制组文档指定的归档审批人是谁？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `5501`
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

answer:

```text
根据控制组文档的内容，文档中并未指定归档审批人是谁，也未提供任何审批人姓名或审批系统地址。文档只说明了容量治理策略、归档窗口编号（ARCHIVE-CN-7319）、冷数据迁移阈值、单批提交上限等参数，无法回答审批人相关信息。
```

### RAG-13 - control_plaintext_en_answerable

- question: According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?
- should_answer: `true`
- expected_source_section: control-en-long-paragraph.txt / webhook fallback runbook
- completed: `true`
- duration_ms: `7376`
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

answer:

```text
According to the control document, the fallback queue name is **silver-river-42**, and the retry budget for that fallback path is **seven attempts within eighteen minutes** [1][3].
```

### RAG-14 - control_plaintext_en_weak_related

- question: According to the control document, which cloud provider hosts the fallback queue?
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `5451`
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

answer:

```text
According to the control document, the cloud provider that hosts the fallback queue is not specified. The document explicitly states that it does not identify a cloud provider, region, or any such infrastructure detail, and that a question asking which cloud provider runs the fallback queue cannot be answered from it [1][2].
```
