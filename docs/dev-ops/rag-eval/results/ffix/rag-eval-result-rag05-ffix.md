# RAG Eval Result

- generated_at: `2026-05-02 04:12:46`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-05 | answerable_long_context | true | 26702 | true | 2 | 0.6499 .. 0.6907 | false | 5/5 |  | 系统健康报告模板包含以下主要部分： 1. **监控数据概览** - 监控时间范围 - 数据源信息 - 分析时间 2. **CPU 使用情况** - 平均使用率 - 峰值使用率 - 状态评估 - 趋势分析 3. **内存使用情况** - 当前... |  |

## Details

### RAG-05 - answerable_long_context

- question: 系统健康报告模板包含哪些部分？
- should_answer: `true`
- expected_source_section: 系统健康报告模板
- completed: `true`
- duration_ms: `26702`
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
  - context_chars: actual `2979` / max `6000`

answer:

```text
系统健康报告模板包含以下主要部分：

1. **监控数据概览**  
   - 监控时间范围  
   - 数据源信息  
   - 分析时间  

2. **CPU 使用情况**  
   - 平均使用率  
   - 峰值使用率  
   - 状态评估  
   - 趋势分析  

3. **内存使用情况**  
   - 当前使用率  
   - 可用内存  
   - 状态评估  
   - 使用趋势  

4. **磁盘使用情况**  
   - 使用率  
   - 可用空间  
   - 状态评估  
   - 空间预警  

5. **网络流量情况**  
   - 接收速率  
   - 发送速率  
   - 状态评估  
   - 流量模式  

6. **综合评估**  
   - 系统健康度  
   - 关键发现  
   - 优化建议  
   - 告警建议  

这些部分共同构成一份完整的系统运行状态分析报告，覆盖基础资源监控、状态判断及优化指引（参见[1]中的模板）。
```
