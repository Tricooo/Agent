# RAG Eval Result

- generated_at: `2026-04-30 04:04:59`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。

| id | type | completed | duration_ms | should_answer | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---|---|
| RAG-04 | answerable_deep_section | true | 16616 | true | 3/3 |  | 根据您提供的资料，用于预测磁盘 7 天增长趋势的 PromQL 查询为： ```promql predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*36... |  |
| RAG-06 | unanswerable | true | 4824 | false | manual | — | 抱歉，根据当前提供的知识库，没有检索到与 MySQL InnoDB MVCC undo log 版本链相关的可用上下文，因此我无法基于知识库来回答这个问题。 |  |
| RAG-07 | weak_related | true | 5251 | false | manual | — | 根据提供的知识库内容，没有关于 Grafana dashboard 变量模板配置的相关信息。我无法就这个问题给出准确的解答。建议查阅 Grafana 官方文档中关于“Dashboard variables”（仪表盘变量）的配置指南。 |  |

## Details

### RAG-04 - answerable_deep_section

- question: 磁盘 7 天增长趋势预测用哪个 PromQL？
- should_answer: `true`
- expected_source_section: 趋势预测
- completed: `true`
- duration_ms: `16616`
- score_mode: `literal`
- expected_points:
  - [x] predict_linear
  - [x] node_filesystem_avail_bytes
  - [x] 7*24*3600

answer:

```text
根据您提供的资料，用于预测磁盘 7 天增长趋势的 PromQL 查询为：

```promql
predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600)
```

该查询基于过去 **24 小时**（`[24h]`）的磁盘可用空间变化趋势，预测 **7 天**（`7*24*3600` 秒）后的可用空间值，从而评估存储空间的未来消耗趋势[1]。
```

### RAG-06 - unanswerable

- question: 根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `4824`
- score_mode: `manual`
- expected_points:
  - 无法基于当前知识库回答
  - 当前知识库没有相关上下文

answer:

```text
抱歉，根据当前提供的知识库，没有检索到与 MySQL InnoDB MVCC undo log 版本链相关的可用上下文，因此我无法基于知识库来回答这个问题。
```

### RAG-07 - weak_related

- question: 根据知识库，Grafana dashboard 变量模板怎么配置？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `5251`
- score_mode: `manual`
- expected_points:
  - 知识库未涉及 dashboard 变量模板
  - 无法基于当前知识库回答

answer:

```text
根据提供的知识库内容，没有关于 Grafana dashboard 变量模板配置的相关信息。我无法就这个问题给出准确的解答。建议查阅 Grafana 官方文档中关于“Dashboard variables”（仪表盘变量）的配置指南。
```
