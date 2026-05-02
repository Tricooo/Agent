# RAG Eval Result

- generated_at: `2026-05-01 03:00:11`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-04 | answerable_deep_section | true | 19907 | true | — | — | — | 2/3 | 7*24*3600 | 要预测磁盘在未来 7 天的增长趋势（即剩余空间是否会耗尽），可以使用 `predict_linear` 函数，基于过去一段时间的可用空间变化进行线性回归预测。对应的 PromQL 如下： ```promql # 预测 7 天后的磁盘可用字节... |  |
| RAG-06 | unanswerable | true | 5238 | false | — | — | — | manual | — | 根据提供的上下文信息，当前知识库中没有相关的内容可以回答您关于 MySQL InnoDB MVCC undo log 版本链实现的问题。因此，我无法基于知识库为您提供解答。建议您查阅官方文档或相关技术资料获取详细信息。 |  |
| RAG-07 | weak_related | true | 10244 | false | — | — | — | manual | — | 根据当前提供的知识库内容，主要涵盖的是 Grafana MCP 工具的使用、Prometheus 查询示例、自动化分析流程以及系统监控报告的生成模板，**并未包含 Grafana Dashboard 变量模板的配置方法**。 因此，我无法基... |  |

## Details

### RAG-04 - answerable_deep_section

- question: 磁盘 7 天增长趋势预测用哪个 PromQL？
- should_answer: `true`
- expected_source_section: 趋势预测
- completed: `true`
- duration_ms: `19907`
- score_mode: `literal`
- expected_points:
  - [x] predict_linear
  - [x] node_filesystem_avail_bytes
  - [ ] 7*24*3600

answer:

```text
要预测磁盘在未来 7 天的增长趋势（即剩余空间是否会耗尽），可以使用 `predict_linear` 函数，基于过去一段时间的可用空间变化进行线性回归预测。对应的 PromQL 如下：

```promql
# 预测 7 天后的磁盘可用字节数
predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7 * 24 * 3600)
```
- `[24h]` 表示基于过去 24 小时的数据进行拟合。
- `7 * 24 * 3600` 表示预测未来 7 天的秒数（7 天后的值）。
- 如果结果 ≤ 0，说明磁盘可能在 7 天内被写满。

如果想直接查看未来 7 天可能达到的 **使用率**，可以进一步计算：
```promql
# 预测 7 天后的磁盘使用率（%）
(1 - predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7 * 24 * 3600) / node_filesystem_size_bytes{fstype!="tmpfs"}) * 100
```

两个查询都可以放在 Grafana 的面板中用来展示磁盘空间趋势预警。
```

### RAG-06 - unanswerable

- question: 根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `5238`
- score_mode: `manual`
- expected_points:
  - 无法基于当前知识库回答
  - 当前知识库没有相关上下文

answer:

```text
根据提供的上下文信息，当前知识库中没有相关的内容可以回答您关于 MySQL InnoDB MVCC undo log 版本链实现的问题。因此，我无法基于知识库为您提供解答。建议您查阅官方文档或相关技术资料获取详细信息。
```

### RAG-07 - weak_related

- question: 根据知识库，Grafana dashboard 变量模板怎么配置？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `10244`
- score_mode: `manual`
- expected_points:
  - 知识库未涉及 dashboard 变量模板
  - 无法基于当前知识库回答

answer:

```text
根据当前提供的知识库内容，主要涵盖的是 Grafana MCP 工具的使用、Prometheus 查询示例、自动化分析流程以及系统监控报告的生成模板，**并未包含 Grafana Dashboard 变量模板的配置方法**。

因此，我无法基于现有知识库给你提供相关答案。如果你需要这方面的帮助，可以补充更具体的需求或提供其他相关文档，我会尽力协助。
```
