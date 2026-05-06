# RAG Eval Result

- generated_at: `2026-05-06 04:53:45`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。
>
> `retrieved` / `score` / `empty` 三列的 `—` 表示**没拿到成功的 ChatResponse metadata**，不等价于"无检索"。当前实现把 retrieval SSE 帧放在 `.call().chatResponse()` 返回之后才发，所以 LLM 调用失败时（即使 RAG 检索本身成功）三列都会是 `—`。要区分"检索失败"和"生成失败"，对照 `error` 列 / details 区 / backend log。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-11 | control_plaintext_cn_answerable | true | 7515 | true | 4 | 0.7175 .. 0.7763 | false | 2/2 |  | 根据控制组文档，冷数据迁移阈值是**连续二十一天未访问**，用于判断明细是否进入候选池[1][2]。归档窗口编号是 **ARCHIVE-CN-7319**，这是文档中唯一用于识别迁移窗口的字符串[1][3]。 |  |
| RAG-12 | control_plaintext_cn_weak_related | true | 4698 | false | 4 | 0.6475 .. 0.7169 | false | manual | — | 根据文档内容，归档审批人信息并未被指定。文档中明确说明，没有提供审批人姓名、审批系统地址或相关组织架构信息，因此无法回答该问题。 |  |
| RAG-13 | control_plaintext_en_answerable | true | 5765 | true | 4 | 0.7632 .. 0.7906 | false | 2/2 |  | The fallback queue name is **silver-river-42**, and the retry budget for the fallback path is **seven attempts within ei... |  |
| RAG-14 | control_plaintext_en_weak_related | true | 7405 | false | 4 | 0.7432 .. 0.7799 | false | manual | — | The control document does not specify which cloud provider hosts the fallback queue. It explicitly states that this info... |  |

## Details

### RAG-11 - control_plaintext_cn_answerable

- question: 控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？
- should_answer: `true`
- expected_source_section: control-cn-low-newline.txt / 容量治理控制组
- completed: `true`
- duration_ms: `7515`
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
根据控制组文档，冷数据迁移阈值是**连续二十一天未访问**，用于判断明细是否进入候选池[1][2]。归档窗口编号是 **ARCHIVE-CN-7319**，这是文档中唯一用于识别迁移窗口的字符串[1][3]。
```

### RAG-12 - control_plaintext_cn_weak_related

- question: 控制组文档指定的归档审批人是谁？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `4698`
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
根据文档内容，归档审批人信息并未被指定。文档中明确说明，没有提供审批人姓名、审批系统地址或相关组织架构信息，因此无法回答该问题。
```

### RAG-13 - control_plaintext_en_answerable

- question: According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?
- should_answer: `true`
- expected_source_section: control-en-long-paragraph.txt / webhook fallback runbook
- completed: `true`
- duration_ms: `5765`
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
The fallback queue name is **silver-river-42**, and the retry budget for the fallback path is **seven attempts within eighteen minutes**[1][2].
```

### RAG-14 - control_plaintext_en_weak_related

- question: According to the control document, which cloud provider hosts the fallback queue?
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `7405`
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
The control document does not specify which cloud provider hosts the fallback queue. It explicitly states that this information is not included, so I cannot answer that question based on the provided context.
```
