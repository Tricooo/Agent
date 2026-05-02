# RAG Eval Result

- generated_at: `2026-05-02 03:02:59`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-04 | answerable_deep_section | true | 10909 | true | 3 | 0.6305 .. 0.6481 | false | 3/3 |  | 根据您提供的上下文，用于磁盘 7 天增长趋势预测的 PromQL 查询是： ```promql predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3... |  |
| RAG-06 | unanswerable | true | 4155 | false | 0 | — | true | manual | — | 根据当前知识库的检索结果，未能找到关于 MySQL InnoDB MVCC undo log 版本链实现的相关内容，因此无法基于知识库为您解答该问题。如果您有其它明确的知识点需要查询，我很乐意继续提供帮助。 |  |
| RAG-07 | weak_related | true | 8333 | false | 3 | 0.6185 .. 0.6645 | false | manual | — | 根据当前提供的知识库内容，其中主要涉及 Grafana MCP 工具（如数据源列表和 Prometheus 查询）以及监控分析流程，并没有包含 Grafana dashboard 变量模板配置的相关信息。因此，我无法基于现有上下文回答您的问... |  |

## Details

### RAG-04 - answerable_deep_section

- question: 磁盘 7 天增长趋势预测用哪个 PromQL？
- should_answer: `true`
- expected_source_section: 趋势预测
- completed: `true`
- duration_ms: `10909`
- score_mode: `literal`
- expected_points:
  - [x] predict_linear
  - [x] node_filesystem_avail_bytes
  - [x] 7*24*3600

retrieval:
- event #1
  - retrieved_document_count: `3`
  - retrieval_empty: `false`
  - similarity_threshold: `0.5500`
  - score_range: `0.6305 .. 0.6481`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `4692` / max `6000`

answer:

```text
根据您提供的上下文，用于磁盘 7 天增长趋势预测的 PromQL 查询是：

```promql
predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600)
```

这个查询会基于过去 24 小时的磁盘可用空间变化，预测 7 天（即 7×24×3600 秒）后的可用空间值，从而判断磁盘是否会在未来一周内耗尽。
```

### RAG-06 - unanswerable

- question: 根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `4155`
- score_mode: `manual`
- expected_points:
  - 无法基于当前知识库回答
  - 当前知识库没有相关上下文

retrieval:
- event #1
  - retrieved_document_count: `0`
  - retrieval_empty: `true`
  - similarity_threshold: `0.5500`
  - score_range: `— .. —`
  - context_selected: `0` / dropped: `0` / truncated: `false`
  - context_chars: actual `61` / max `6000`

answer:

```text
根据当前知识库的检索结果，未能找到关于 MySQL InnoDB MVCC undo log 版本链实现的相关内容，因此无法基于知识库为您解答该问题。如果您有其它明确的知识点需要查询，我很乐意继续提供帮助。
```

### RAG-07 - weak_related

- question: 根据知识库，Grafana dashboard 变量模板怎么配置？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `8333`
- score_mode: `manual`
- expected_points:
  - 知识库未涉及 dashboard 变量模板
  - 无法基于当前知识库回答

retrieval:
- event #1
  - retrieved_document_count: `3`
  - retrieval_empty: `false`
  - similarity_threshold: `0.5500`
  - score_range: `0.6185 .. 0.6645`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `4692` / max `6000`

answer:

```text
根据当前提供的知识库内容，其中主要涉及 Grafana MCP 工具（如数据源列表和 Prometheus 查询）以及监控分析流程，并没有包含 Grafana dashboard 变量模板配置的相关信息。因此，我无法基于现有上下文回答您的问题。
```
