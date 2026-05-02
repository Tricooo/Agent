# RAG Eval Result

- generated_at: `2026-04-30 02:45:42`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。

| id | type | completed | duration_ms | should_answer | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---|---|
| RAG-04 | answerable_deep_section | true | 11123 | true | 3/3 |  | 根据当前提供的上下文，知识库中没有与您的问题相关的信息，因此无法基于知识库回答“磁盘 7 天增长趋势预测用哪个 PromQL”。如果需要通用的 PromQL 思路，建议使用 `predict_linear` 函数结合磁盘使用率指标，例如 `... |  |
| RAG-06 | unanswerable | true | 4452 | false | manual | — | 根据当前知识库的检索结果，没有找到与 MySQL InnoDB MVCC undo log 版本链实现相关的可用上下文，因此无法基于知识库回答该问题。如果您有其他非知识库相关的问题，可以进一步说明。 |  |
| RAG-07 | weak_related | true | 6039 | false | manual | — | 根据当前的知识库内容，主要是关于 Grafana MCP 工具的使用，包括列出数据源和执行 Prometheus 查询等操作，并没有涉及 Grafana Dashboard 变量模板的配置方法。因此，我无法根据现有知识库回答这个问题。建议参... |  |

## Details

### RAG-04 - answerable_deep_section

- question: 磁盘 7 天增长趋势预测用哪个 PromQL？
- should_answer: `true`
- expected_source_section: 趋势预测
- completed: `true`
- duration_ms: `11123`
- score_mode: `literal`
- expected_points:
  - [x] predict_linear
  - [x] node_filesystem_avail_bytes
  - [x] 7*24*3600

answer:

```text
根据当前提供的上下文，知识库中没有与您的问题相关的信息，因此无法基于知识库回答“磁盘 7 天增长趋势预测用哪个 PromQL”。如果需要通用的 PromQL 思路，建议使用 `predict_linear` 函数结合磁盘使用率指标，例如 `predict_linear(node_filesystem_avail_bytes[7d], 7*24*3600)`，但具体实现需依据您的监控环境调整。
```

### RAG-06 - unanswerable

- question: 根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `4452`
- score_mode: `manual`
- expected_points:
  - 无法基于当前知识库回答
  - 当前知识库没有相关上下文

answer:

```text
根据当前知识库的检索结果，没有找到与 MySQL InnoDB MVCC undo log 版本链实现相关的可用上下文，因此无法基于知识库回答该问题。如果您有其他非知识库相关的问题，可以进一步说明。
```

### RAG-07 - weak_related

- question: 根据知识库，Grafana dashboard 变量模板怎么配置？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `6039`
- score_mode: `manual`
- expected_points:
  - 知识库未涉及 dashboard 变量模板
  - 无法基于当前知识库回答

answer:

```text
根据当前的知识库内容，主要是关于 Grafana MCP 工具的使用，包括列出数据源和执行 Prometheus 查询等操作，并没有涉及 Grafana Dashboard 变量模板的配置方法。因此，我无法根据现有知识库回答这个问题。建议参考 Grafana 官方文档中关于变量（Variables）模板配置的部分。
```
