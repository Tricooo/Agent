# RAG Eval Result

- generated_at: `2026-04-30 06:18:29`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-04 | answerable_deep_section | true | 19664 | true | — | — | — | 3/3 |  | 根据提供的上下文，磁盘 7 天增长趋势预测的 PromQL 查询如下： ```promql predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7 * 24 *... |  |
| RAG-06 | unanswerable | true | 8619 | false | — | — | — | manual | — | 根据当前提供的知识库内容，未能检索到与“MySQL InnoDB 的 MVCC undo log 版本链实现”相关的信息，因此我无法基于该知识库回答您的问题。 |  |
| RAG-07 | weak_related | true | 10859 | false | — | — | — | manual | — | 根据提供的知识库内容，我无法回答关于 Grafana dashboard 变量模板配置的问题。 现有的知识库主要侧重于 **Grafana MCP（Model Context Protocol）工具** 的使用，介绍了如何通过 API 形式... |  |

## Details

### RAG-04 - answerable_deep_section

- question: 磁盘 7 天增长趋势预测用哪个 PromQL？
- should_answer: `true`
- expected_source_section: 趋势预测
- completed: `true`
- duration_ms: `19664`
- score_mode: `literal`
- expected_points:
  - [x] predict_linear
  - [x] node_filesystem_avail_bytes
  - [x] 7*24*3600

answer:

```text
根据提供的上下文，磁盘 7 天增长趋势预测的 PromQL 查询如下：

```promql
predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7 * 24 * 3600)
```

该查询基于过去 24 小时的可用空间变化，使用线性回归预测 7 天后（即 `7*24*3600` 秒后）的磁盘可用字节数。如果结果为负数或接近零，则表明存储空间有耗尽风险。你也可以将其改写为磁盘使用率的趋势预测，例如：

```promql
(1 - predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) / node_filesystem_size_bytes{fstype!="tmpfs"}) * 100
```

但上下文直接给出的原始示例即是第一个。
```

### RAG-06 - unanswerable

- question: 根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `8619`
- score_mode: `manual`
- expected_points:
  - 无法基于当前知识库回答
  - 当前知识库没有相关上下文

answer:

```text
根据当前提供的知识库内容，未能检索到与“MySQL InnoDB 的 MVCC undo log 版本链实现”相关的信息，因此我无法基于该知识库回答您的问题。
```

### RAG-07 - weak_related

- question: 根据知识库，Grafana dashboard 变量模板怎么配置？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `10859`
- score_mode: `manual`
- expected_points:
  - 知识库未涉及 dashboard 变量模板
  - 无法基于当前知识库回答

answer:

```text
根据提供的知识库内容，我无法回答关于 Grafana dashboard 变量模板配置的问题。

现有的知识库主要侧重于 **Grafana MCP（Model Context Protocol）工具** 的使用，介绍了如何通过 API 形式调用数据源列表和进行 Prometheus 查询，并提供了报告生成模板。文档中并未包含在 Grafana 界面中配置 Dashboard 变量（如 `$instance`、`$job` 等）的具体步骤或语法。

建议您查阅 Grafana 官方文档中的“Variables”章节以获取配置指南，或者提供相关的知识库更新给我，我会很乐意协助您。
```
