# RAG Eval Result

- generated_at: `2026-05-01 03:21:17`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-04 | answerable_deep_section | true | 17300 | true | 3 | 0.6305 .. 0.6481 | false | 3/3 |  | 要预测磁盘在未来 7 天的增长趋势，可以使用 Prometheus 的内置函数 `predict_linear`，它基于过去一段时间的线性回归来预测未来的值。 根据您提供的上下文 [1]，推荐的 PromQL 为： ```promql pr... |  |
| RAG-06 | unanswerable | true | 5959 | false | 0 | — | true | manual | — | 根据当前提供的知识库上下文，未检索到相关的知识片段，无法基于该知识库回答您关于 MySQL InnoDB MVCC undo log 版本链实现的问题。 |  |
| RAG-07 | weak_related | true | 13331 | false | 3 | 0.6185 .. 0.6645 | false | manual | — | 根据提供的知识库，其中并未包含 Grafana dashboard 变量模板的具体配置方法。 知识库内容主要涵盖以下两方面： 1. **Grafana MCP 工具使用**：详细描述了如何通过 `grafana/list_datasourc... |  |

## Details

### RAG-04 - answerable_deep_section

- question: 磁盘 7 天增长趋势预测用哪个 PromQL？
- should_answer: `true`
- expected_source_section: 趋势预测
- completed: `true`
- duration_ms: `17300`
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
  - context_chars: actual `4692` / max `6000`

answer:

```text
要预测磁盘在未来 7 天的增长趋势，可以使用 Prometheus 的内置函数 `predict_linear`，它基于过去一段时间的线性回归来预测未来的值。

根据您提供的上下文 [1]，推荐的 PromQL 为：
```promql
predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600)
```
- **说明**：该语句基于过去 24 小时 (`[24h]`) 的磁盘可用字节数变化，预测 7 天（`7*24*3600` 秒）后的可用字节数。  
- **结果解读**：如果预测值接近 0 或负数，说明磁盘空间可能在 7 天内耗尽，需要提前扩容或清理。

如果需要预测的是**使用率**的增长趋势，可以将其转换为使用率的预测，例如：
```promql
100 - (predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) / node_filesystem_size_bytes{fstype!="tmpfs"} * 100)
```
但通常直接监控可用字节的预测值更直观，能直接反映剩余空间。
```

### RAG-06 - unanswerable

- question: 根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `5959`
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
根据当前提供的知识库上下文，未检索到相关的知识片段，无法基于该知识库回答您关于 MySQL InnoDB MVCC undo log 版本链实现的问题。
```

### RAG-07 - weak_related

- question: 根据知识库，Grafana dashboard 变量模板怎么配置？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `13331`
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
根据提供的知识库，其中并未包含 Grafana dashboard 变量模板的具体配置方法。

知识库内容主要涵盖以下两方面：
1.  **Grafana MCP 工具使用**：详细描述了如何通过 `grafana/list_datasources` 和 `grafana/query_prometheus` 等工具进行自动化监控查询、生成健康报告以及排查故障。
2.  **PromQL 查询示例**：提供了具体的内存、磁盘、网络流量等查询语句及数据解释标准。

因此，我无法根据当前知识库回答关于“Dashboard 变量模板配置”的问题。
```
