# RAG Eval Result

- generated_at: `2026-05-23 22:41:05`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_profile_v2_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。
>
> `retrieved` / `score` / `empty` 三列的 `—` 表示**没拿到成功的 ChatResponse metadata**，不等价于"无检索"。当前实现把 retrieval SSE 帧放在 `.call().chatResponse()` 返回之后才发，所以 LLM 调用失败时（即使 RAG 检索本身成功）三列都会是 `—`。要区分"检索失败"和"生成失败"，对照 `error` 列 / details 区 / backend log。
>
> Details 区的 `pre_rerank_documents` 展开 rerank 前候选池，`documents` 展开最终 top-K chunk attribution；document 行的 `score` 是当前阶段写回的候选分，可能来自 HYBRID RRF、query-variant fusion、per-variant rerank RRF 或 fusion-aware rerank；原始向量分与关键词分分别看 `vectorScore` / `keywordScore`，多 query 召回融合看 `queryFusionScore/queryFusionRank/queryVariantHitCount`；最终 rerank 写回分看 `rerankScore`，多路 rerank 融合分看 `rerankFusionScore/rerankVariantHitCount`，fusion-aware 弱加成看 `queryFusionBoostScore/fusionAwareScore`；rerank 服务状态看 `rerank_runtime` 的 model / endpoint / failure_reason；keyword 分支未参与时看 `keywordSkippedReason`。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| EXT-ARTEMIS-01 | answerable | true | 8952 | true | 4 | 0.0469 .. 0.0492 | false | 0/3 | Nov. 16, 2022, Dec. 11, 2022, 25 days, 10 hours, 53 minutes | 根据 NASA 的 Artemis I 任务信息[1]： - 发射日期：2022 年 11 月 16 日 - 溅落日期：2022 年 12 月 11 日 - 任务持续时间：25 天 10 小时 53 分钟 |  |
| EXT-ARTEMIS-02 | answerable_deep_section | true | 17962 | true | 4 | 0.0474 .. 0.0492 | false | 2/3 | Splashdown | 根据提供的资料，Artemis I 任务在飞行第26天（Flight Day 26）的关键事件按顺序如下： 1. **返回轨迹修正燃烧-6**（Return trajectory correction burn-6） 2. **乘员舱/服务... |  |
| EXT-RFC9110-03 | answerable_formula | true | 16121 | true | 3 | 0.0476 .. 0.0492 | false | 0/3 | HTTP/1.0 did not define 1xx, MUST NOT send a 1xx response, HTTP/1.0 client | 根据 RFC 9110 的规定，HTTP/1.0 并没有定义 1xx 信息类状态码，因此服务器 **绝不能（MUST NOT）** 向 HTTP/1.0 客户端发送 1xx 响应。 [1] 简单来说，如果你在服务端实现 HTTP，一旦识别出... |  |
| EXT-K8S-PV-03 | answerable_long_context | true | 13299 | true | 3 | 0.0479 .. 0.0492 | false | manual | — | 当 PVC 扩容失败时，文档给出的恢复流程如下[1]： 1. 将绑定到此 PVC 的 PersistentVolume（PV）的回收策略（reclaim policy）标记为 `Retain`。 2. 删除该 PVC。由于 PV 的策略为 ... |  |
| EXT-OWASP-03 | answerable_deep_section | true | 21431 | true | 4 | 0.0469 .. 0.0489 | false | manual | — | 根据提供的上下文，这三类风险的核心区别在于它们对应的**根本失败模式**不同[1]： - **Broken Access Control（访问控制失效）**：本质上是**授权策略的失败**。即用户设法执行了超出其应有权限的操作，例如通过篡改... |  |
| EXT-PG-04 | answerable_version_conflict | true | 30469 | true | 3 | 0.0476 .. 0.0489 | false | manual | — | 根据文档中的版本冲突处理指南，当知识库同时包含 PostgreSQL 16 和 18 文档时，回答 **generated column 的 virtual/stored 差异** 需要遵循以下规则 [1]： **必须区分版本作答**，不能... |  |
| EXT-RFC9110-04 | weak_related | true | 13096 | false | 4 | 0.0471 .. 0.0492 | false | manual | — | 根据提供的上下文，我无法回答您的问题。上下文中的 RFC 9110 片段明确指出： > RFC 9110 is not a Spring Boot guide. It does not specify how to implement `@... |  |
| EXT-K8S-PV-04 | unanswerable | true | 13679 | false | 1 | 0.0492 .. 0.0492 | false | manual | — | 根据您提供的 PV（Persistent Volume）文档，其中明确说明： > “This sample covers Kubernetes storage objects. It does not describe MySQL Inno... |  |

## Details

### EXT-ARTEMIS-01 - answerable

- question: Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？
- should_answer: `true`
- expected_source_section: Mission Overview
- completed: `true`
- duration_ms: `8952`
- score_mode: `literal`
- expected_points:
  - [ ] Nov. 16, 2022
  - [ ] Dec. 11, 2022
  - [ ] 25 days, 10 hours, 53 minutes

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0469 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `4326` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，<br>Artemis I launch date, splashdown date, mission duration<br>Artemis I timeline, flight day summary, mission duration calculation`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2443` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？， \| Artemis I launch date, splashdown date, mission duration \| Artemis I timeline, flight day summary, mission duration calculation`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `Artemis I \| HTTP语义和线格式的区别是什么？ \| Artemis I mission \| Artemis I timeline \| What happened on Flight Day 21 of Artemis I? \| What happened on Flight Day 24 of Artemis I? \| What happened on Flight Day 25 of Artemis I? \| What was the launch date of Artemis I?`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7541`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":1,"text":"Artemis I launch date, splashdown date, mission duration"},{"index":3,"rank":1,"text":"Artemis I timeline, flight day summary, mission duration calculation"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #2 score=`0.0479`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7407`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0479`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":2,"text":"Artemis I launch date, splashdown date, mission duration"},{"index":3,"rank":4,"text":"Artemis I timeline, flight day summary, mission duration calculation"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #3 score=`0.0479`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6757`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0479`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":3,"text":"Artemis I launch date, splashdown date, mission duration"},{"index":3,"rank":2,"text":"Artemis I timeline, flight day summary, mission duration calculation"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #4 score=`0.0471`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6502`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0471`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":4,"text":"Artemis I launch date, splashdown date, mission duration"},{"index":3,"rank":3,"text":"Artemis I timeline, flight day summary, mission duration calculation"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...
  - documents:
    - #1 score=`0.0492`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7541`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":1,"text":"Artemis I launch date, splashdown date, mission duration"},{"index":3,"rank":1,"text":"Artemis I timeline, flight day summary, mission duration calculation"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #2 score=`0.0484`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6757`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0479`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":3,"text":"Artemis I launch date, splashdown date, mission duration"},{"index":3,"rank":2,"text":"Artemis I timeline, flight day summary, mission duration calculation"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #3 score=`0.0476`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7407`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0479`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":2,"text":"Artemis I launch date, splashdown date, mission duration"},{"index":3,"rank":4,"text":"Artemis I timeline, flight day summary, mission duration calculation"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #4 score=`0.0469`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6502`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0471`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":4,"text":"Artemis I launch date, splashdown date, mission duration"},{"index":3,"rank":3,"text":"Artemis I timeline, flight day summary, mission duration calculation"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...

answer:

```text
根据 NASA 的 Artemis I 任务信息[1]：

- 发射日期：2022 年 11 月 16 日
- 溅落日期：2022 年 12 月 11 日
- 任务持续时间：25 天 10 小时 53 分钟
```

### EXT-ARTEMIS-02 - answerable_deep_section

- question: Artemis I Flight Day 26 的关键事件顺序是什么？
- should_answer: `true`
- expected_source_section: Flight Day 26
- completed: `true`
- duration_ms: `17962`
- score_mode: `literal`
- expected_points:
  - [x] Return trajectory correction burn-6
  - [x] Crew module/service module separation
  - [ ] Splashdown

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0474 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `4326` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `Artemis I Flight Day 26 的关键事件顺序是什么？，<br>Artemis I Flight Day 26 sequence of events timeline milestones<br>Artemis I Flight Day 26 steps splashdown preparation return trajectory events`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2955` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `Artemis I Flight Day 26 的关键事件顺序是什么？， \| Artemis I Flight Day 26 sequence of events timeline milestones \| Artemis I Flight Day 26 steps splashdown preparation return trajectory events`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `Flight Day 26 \| Artemis I \| What happened on Flight Day 21 of Artemis I? \| What happened on Flight Day 24 of Artemis I? \| What happened on Flight Day 25 of Artemis I? \| Flight Day 1 \| Flight Day 20 \| Flight Day 21`
  - pre_rerank_documents:
    - #1 score=`0.0489`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7962`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":2,"text":"Artemis I Flight Day 26 sequence of events timeline milestones"},{"index":3,"rank":1,"text":"Artemis I Flight Day 26 steps splashdown preparation return trajectory events"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #2 score=`0.0484`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7517`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":1,"text":"Artemis I Flight Day 26 sequence of events timeline milestones"},{"index":3,"rank":2,"text":"Artemis I Flight Day 26 steps splashdown preparation return trajectory events"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #3 score=`0.0479`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7551`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0479`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":3,"text":"Artemis I Flight Day 26 sequence of events timeline milestones"},{"index":3,"rank":3,"text":"Artemis I Flight Day 26 steps splashdown preparation return trajectory events"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #4 score=`0.0469`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7427`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0469`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":4,"text":"Artemis I Flight Day 26 sequence of events timeline milestones"},{"index":3,"rank":4,"text":"Artemis I Flight Day 26 steps splashdown preparation return trajectory events"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...
  - documents:
    - #1 score=`0.0492`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7962`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":2,"text":"Artemis I Flight Day 26 sequence of events timeline milestones"},{"index":3,"rank":1,"text":"Artemis I Flight Day 26 steps splashdown preparation return trajectory events"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #2 score=`0.0481`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7517`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0481`, rerankFusionScore=`0.0481`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":1,"text":"Artemis I Flight Day 26 sequence of events timeline milestones"},{"index":3,"rank":2,"text":"Artemis I Flight Day 26 steps splashdown preparation return trajectory events"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #3 score=`0.0474`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7427`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`3`, rerankScore=`0.0474`, rerankFusionScore=`0.0474`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0469`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":4,"text":"Artemis I Flight Day 26 sequence of events timeline milestones"},{"index":3,"rank":4,"text":"Artemis I Flight Day 26 steps splashdown preparation return trajectory events"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...
    - #4 score=`0.0474`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7551`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`4`, rerankScore=`0.0474`, rerankFusionScore=`0.0474`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0479`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":3,"text":"Artemis I Flight Day 26 sequence of events timeline milestones"},{"index":3,"rank":3,"text":"Artemis I Flight Day 26 steps splashdown preparation return trajectory events"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...

answer:

```text
根据提供的资料，Artemis I 任务在飞行第26天（Flight Day 26）的关键事件按顺序如下：

1. **返回轨迹修正燃烧-6**（Return trajectory correction burn-6）
2. **乘员舱/服务舱分离**（Crew module/service module separation）
3. **乘员舱提升燃烧**（Crew module raise burn）
4. **目标进入界面**（Targeted entry interface）

随后，航天器按计划进入地球大气层，并通过降落伞辅助在太平洋溅落，完成回收[1][4]。
```

### EXT-RFC9110-03 - answerable_formula

- question: RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？
- should_answer: `true`
- expected_source_section: Informational 1xx
- completed: `true`
- duration_ms: `16121`
- score_mode: `literal`
- expected_points:
  - [ ] HTTP/1.0 did not define 1xx
  - [ ] MUST NOT send a 1xx response
  - [ ] HTTP/1.0 client

retrieval:
- event #1
  - retrieved_document_count: `3`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0476 .. 0.0492`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `3177` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `3` / final `3`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，<br>RFC 9110 1xx status code restrictions HTTP/1.0 client limitations 100 Continue 101 Switching Protocols<br>RFC 9110 Section 4.1 server behavior provisional responses HTTP/1.0 client exclusion hop-by-hop headers`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `4374` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？， \| RFC 9110 1xx status code restrictions HTTP/1.0 client limitations 100 Continue 101 Switching Protocols \| RFC 9110 Section 4.1 server behavior provisional responses HTTP/1.0 client exclusion hop-by-hop headers`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `HTTP/1.0 \| RFC \| HTTP/1.0是否支持1xx状态码？ \| 服务器能否向HTTP/1.0客户端发送1xx响应？ \| RFC 9110定义了哪些内容？ \| https://www.rfc-editor.org/rfc/rfc9110 \| External RAG Sample: RFC 9110 HTTP Semantics \| HTTP/1.0 client exclusion`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0328`, vectorRank=`1`, vectorScore=`0.7447`, keywordRank=`1`, keywordScore=`0.3000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":1,"text":"RFC 9110 1xx status code restrictions HTTP/1.0 client limitations 100 Continue 101 Switching Protocols"},{"index":3,"rank":1,"text":"RFC 9110 Section 4.1 server behavior provisional responses HTTP/1.0 client exclusion hop-by-hop headers"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0484`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7075`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":2,"text":"RFC 9110 1xx status code restrictions HTTP/1.0 client limitations 100 Continue 101 Switching Protocols"},{"index":3,"rank":2,"text":"RFC 9110 Section 4.1 server behavior provisional responses HTTP/1.0 client exclusion hop-by-hop headers"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #3 score=`0.0317`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6079`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`RFC 9110 1xx status code restrictions HTTP/1.0 client limitations 100 Continue 101 Switching Protocols`, queryFusionScore=`0.0317`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":3,"text":"RFC 9110 1xx status code restrictions HTTP/1.0 client limitations 100 Continue 101 Switching Protocols"},{"index":3,"rank":3,"text":"RFC 9110 Section 4.1 server behavior provisional responses HTTP/1.0 client exclusion hop-by-hop headers"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
  - documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0328`, vectorRank=`1`, vectorScore=`0.7447`, keywordRank=`1`, keywordScore=`0.3000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":1,"text":"RFC 9110 1xx status code restrictions HTTP/1.0 client limitations 100 Continue 101 Switching Protocols"},{"index":3,"rank":1,"text":"RFC 9110 Section 4.1 server behavior provisional responses HTTP/1.0 client exclusion hop-by-hop headers"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0484`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7075`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":2,"text":"RFC 9110 1xx status code restrictions HTTP/1.0 client limitations 100 Continue 101 Switching Protocols"},{"index":3,"rank":2,"text":"RFC 9110 Section 4.1 server behavior provisional responses HTTP/1.0 client exclusion hop-by-hop headers"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #3 score=`0.0476`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6079`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`RFC 9110 1xx status code restrictions HTTP/1.0 client limitations 100 Continue 101 Switching Protocols`, queryFusionScore=`0.0317`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":3,"text":"RFC 9110 1xx status code restrictions HTTP/1.0 client limitations 100 Continue 101 Switching Protocols"},{"index":3,"rank":3,"text":"RFC 9110 Section 4.1 server behavior provisional responses HTTP/1.0 client exclusion hop-by-hop headers"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...

answer:

```text
根据 RFC 9110 的规定，HTTP/1.0 并没有定义 1xx 信息类状态码，因此服务器 **绝不能（MUST NOT）** 向 HTTP/1.0 客户端发送 1xx 响应。 [1]

简单来说，如果你在服务端实现 HTTP，一旦识别出客户端版本为 HTTP/1.0，就应当避免返回任何 1xx（如 100 Continue、101 Switching Protocols 等）状态码。
```

### EXT-K8S-PV-03 - answerable_long_context

- question: PVC 扩容失败时，文档给出的恢复流程是什么？
- should_answer: `true`
- expected_source_section: Recovering From Failure When Expanding Volumes
- completed: `true`
- duration_ms: `13299`
- score_mode: `manual`
- expected_points:
  - 标记 Retain
  - 删除 PVC
  - 删除 claimRef
  - 较小尺寸重建 PVC
  - 恢复 reclaim policy

retrieval:
- event #1
  - retrieved_document_count: `3`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0479 .. 0.0492`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `3668` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `3` / final `3`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `PVC 扩容失败时，文档给出的恢复流程是什么？，<br>PVC Expansion failure recovery procedure<br>PVC resize failed recovery steps troubleshooting conditions`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3166` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `PVC 扩容失败时，文档给出的恢复流程是什么？， \| PVC Expansion failure recovery procedure \| PVC resize failed recovery steps troubleshooting conditions`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `PVC \| PVCs \| PVC Expansion \| HTTP语义和线格式的区别是什么？`
  - pre_rerank_documents:
    - #1 score=`0.0486`, source=`kubernetes-persistent-volumes.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7002`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0486`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":1,"text":"PVC Expansion failure recovery procedure"},{"index":3,"rank":2,"text":"PVC resize failed recovery steps troubleshooting conditions"}]`
      - preview: ## Reclaiming When a user is done with a volume, the PVC can be deleted. The reclaim policy tells Kubernetes what to do with the volume after the claim is released. Current reclaim...
    - #2 score=`0.0484`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6907`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":2,"text":"PVC Expansion failure recovery procedure"},{"index":3,"rank":1,"text":"PVC resize failed recovery steps troubleshooting conditions"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...
    - #3 score=`0.0481`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7061`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0481`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":3,"text":"PVC Expansion failure recovery procedure"},{"index":3,"rank":3,"text":"PVC resize failed recovery steps troubleshooting conditions"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
  - documents:
    - #1 score=`0.0492`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7061`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0481`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":3,"text":"PVC Expansion failure recovery procedure"},{"index":3,"rank":3,"text":"PVC resize failed recovery steps troubleshooting conditions"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
    - #2 score=`0.0481`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6907`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0481`, rerankFusionScore=`0.0481`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":2,"text":"PVC Expansion failure recovery procedure"},{"index":3,"rank":1,"text":"PVC resize failed recovery steps troubleshooting conditions"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...
    - #3 score=`0.0479`, source=`kubernetes-persistent-volumes.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7002`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`3`, rerankScore=`0.0479`, rerankFusionScore=`0.0479`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0486`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":1,"text":"PVC Expansion failure recovery procedure"},{"index":3,"rank":2,"text":"PVC resize failed recovery steps troubleshooting conditions"}]`
      - preview: ## Reclaiming When a user is done with a volume, the PVC can be deleted. The reclaim policy tells Kubernetes what to do with the volume after the claim is released. Current reclaim...

answer:

```text
当 PVC 扩容失败时，文档给出的恢复流程如下[1]：

1. 将绑定到此 PVC 的 PersistentVolume（PV）的回收策略（reclaim policy）标记为 `Retain`。
2. 删除该 PVC。由于 PV 的策略为 `Retain`，删除 PVC 不会丢失数据。
3. 从 PV 的规格（spec）中删除 `claimRef` 条目，使 PV 变为 `Available` 状态。
4. 重新创建一个 PVC，其请求的大小应小于现有 PV 的大小，并在 PVC 规格中设置 `volumeName` 字段，指定为该现有 PV 的名称。
5. 恢复 PV 的回收策略为之前的值（可选，但建议还原）。

这样操作后，PV 能重新被绑定，避免了因底层存储不支持请求的扩展大小而导致的问题。注意，Kubernetes 不支持将 PVC 缩小到低于其当前大小，因此重新创建 PVC 时必须确保请求的大小小于或等于 PV 的实际容量。
```

### EXT-OWASP-03 - answerable_deep_section

- question: Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？
- should_answer: `true`
- expected_source_section: Category Differences
- completed: `true`
- duration_ms: `21431`
- score_mode: `manual`
- expected_points:
  - authorization policy failure
  - unsafe data flowing into interpreters
  - unsafe system, framework, server, or deployment settings

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0469 .. 0.0489`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `4508` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，<br>Broken Access Control A01 Injection Security Misconfiguration OWASP Top 10 differences<br>Broken Access Control vs Injection vs Security Misconfiguration comparison examples causes impact`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2706` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？， \| Broken Access Control A01 Injection Security Misconfiguration OWASP Top 10 differences \| Broken Access Control vs Injection vs Security Misconfiguration comparison examples causes impact`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `Broken Access Control \| Security Misconfiguration \| Injection \| What is Broken Access Control? \| https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control \| A01 Broken Access Control \| What is Security Misconfiguration? \| access-control`
  - pre_rerank_documents:
    - #1 score=`0.0489`, source=`owasp-top10-a01-a03-a05.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7612`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":1,"text":"Broken Access Control A01 Injection Security Misconfiguration OWASP Top 10 differences"},{"index":3,"rank":1,"text":"Broken Access Control vs Injection vs Security Misconfiguration comparison examples causes impact"}]`
      - preview: # External RAG Sample: OWASP Top 10 A01 A03 A05 Source URLs: - https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control/ - https://owasp.org/Top10/2021/A03_2021-Injection/ - htt...
    - #2 score=`0.0486`, source=`owasp-top10-a01-a03-a05.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7852`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0486`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":2,"text":"Broken Access Control A01 Injection Security Misconfiguration OWASP Top 10 differences"},{"index":3,"rank":2,"text":"Broken Access Control vs Injection vs Security Misconfiguration comparison examples causes impact"}]`
      - preview: A good answer should focus on permission boundaries, object ownership, and server-side enforcement. ## A03 Injection Injection occurs when untrusted input reaches an interpreter or...
    - #3 score=`0.0476`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7313`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":3,"text":"Broken Access Control A01 Injection Security Misconfiguration OWASP Top 10 differences"},{"index":3,"rank":3,"text":"Broken Access Control vs Injection vs Security Misconfiguration comparison examples causes impact"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
    - #4 score=`0.0313`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6390`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`Broken Access Control A01 Injection Security Misconfiguration OWASP Top 10 differences`, queryFusionScore=`0.0313`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":4,"text":"Broken Access Control A01 Injection Security Misconfiguration OWASP Top 10 differences"},{"index":3,"rank":4,"text":"Broken Access Control vs Injection vs Security Misconfiguration comparison examples causes impact"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
  - documents:
    - #1 score=`0.0489`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7313`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`1`, rerankScore=`0.0489`, rerankFusionScore=`0.0489`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":3,"text":"Broken Access Control A01 Injection Security Misconfiguration OWASP Top 10 differences"},{"index":3,"rank":3,"text":"Broken Access Control vs Injection vs Security Misconfiguration comparison examples causes impact"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
    - #2 score=`0.0484`, source=`owasp-top10-a01-a03-a05.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7852`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":3},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0486`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":2,"text":"Broken Access Control A01 Injection Security Misconfiguration OWASP Top 10 differences"},{"index":3,"rank":2,"text":"Broken Access Control vs Injection vs Security Misconfiguration comparison examples causes impact"}]`
      - preview: A good answer should focus on permission boundaries, object ownership, and server-side enforcement. ## A03 Injection Injection occurs when untrusted input reaches an interpreter or...
    - #3 score=`0.0479`, source=`owasp-top10-a01-a03-a05.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7612`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`3`, rerankScore=`0.0479`, rerankFusionScore=`0.0479`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":2},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":1,"text":"Broken Access Control A01 Injection Security Misconfiguration OWASP Top 10 differences"},{"index":3,"rank":1,"text":"Broken Access Control vs Injection vs Security Misconfiguration comparison examples causes impact"}]`
      - preview: # External RAG Sample: OWASP Top 10 A01 A03 A05 Source URLs: - https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control/ - https://owasp.org/Top10/2021/A03_2021-Injection/ - htt...
    - #4 score=`0.0469`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6390`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`Broken Access Control A01 Injection Security Misconfiguration OWASP Top 10 differences`, queryFusionScore=`0.0313`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":4,"text":"Broken Access Control A01 Injection Security Misconfiguration OWASP Top 10 differences"},{"index":3,"rank":4,"text":"Broken Access Control vs Injection vs Security Misconfiguration comparison examples causes impact"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...

answer:

```text
根据提供的上下文，这三类风险的核心区别在于它们对应的**根本失败模式**不同[1]：

- **Broken Access Control（访问控制失效）**：本质上是**授权策略的失败**。即用户设法执行了超出其应有权限的操作，例如通过篡改URL或参数绕过权限检查、越权访问他人资源（IDOR）、或从未登录状态直接升级为管理员等[3]。它关注的是“谁能访问什么”的问题。

- **Injection（注入）**：本质上是**不安全的数据流入解释器或命令上下文**。当来自用户的数据没有被验证、过滤或净化，就被直接拼接到动态查询、非参数化调用或命令中，导致攻击者可以注入恶意代码并被执行（如SQL注入、跨站脚本、命令注入等）[2]。它关注的是“数据如何被当作指令执行”的问题。

- **Security Misconfiguration（安全配置错误）**：本质上是**系统、框架、服务器或部署设置的不安全状态**。例如：使用默认账号密码、启用了不必要的端口和服务、错误消息泄露堆栈跟踪、缺少安全头、或升级后未开启新的安全功能等[2][1]。它关注的是“环境是否被正确加固和配置”的问题。

简单来说：**访问控制失效是“权限没管好”，注入是“输入没查好”，安全配置错误是“设置没弄好”**。虽然同一事件可能涉及多个类别，但精准区分应着眼于主要的失效环节[1]。
```

### EXT-PG-04 - answerable_version_conflict

- question: 如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？
- should_answer: `true`
- expected_source_section: Version Conflict Handling
- completed: `true`
- duration_ms: `30469`
- score_mode: `manual`
- expected_points:
  - 区分版本
  - PostgreSQL 18 has stored and virtual
  - PostgreSQL 18 defaults to virtual
  - PostgreSQL 16
  - STORED

retrieval:
- event #1
  - retrieved_document_count: `3`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0476 .. 0.0489`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `2932` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `3` / final `3`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，<br>PostgreSQL 16 18 generated column virtual stored difference<br>generated column virtual stored limitations examples PostgreSQL version comparison`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2789` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？， \| PostgreSQL 16 18 generated column virtual stored difference \| generated column virtual stored limitations examples PostgreSQL version comparison`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `generated column \| PostgreSQL \| STORED \| VIRTUAL \| POST \| SQL \| https://www.postgresql.org/docs/18/ddl-generated-columns.html \| External RAG Sample: PostgreSQL Generated Columns Version Contrast`
  - pre_rerank_documents:
    - #1 score=`0.0489`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7895`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":1,"text":"PostgreSQL 16 18 generated column virtual stored difference"},{"index":3,"rank":1,"text":"generated column virtual stored limitations examples PostgreSQL version comparison"}]`
      - preview: # External RAG Sample: PostgreSQL Generated Columns Version Contrast Source URLs: - https://www.postgresql.org/docs/18/ddl-generated-columns.html - https://www.postgresql.org/docs/...
    - #2 score=`0.0486`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7949`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0486`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":2,"text":"PostgreSQL 16 18 generated column virtual stored difference"},{"index":3,"rank":2,"text":"generated column virtual stored limitations examples PostgreSQL version comparison"}]`
      - preview: - The generation expression cannot reference another generated column. - A generated column cannot have a column default or identity definition. - A generated column cannot be part...
    - #3 score=`0.0476`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6291`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":3,"text":"PostgreSQL 16 18 generated column virtual stored difference"},{"index":3,"rank":3,"text":"generated column virtual stored limitations examples PostgreSQL version comparison"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.
  - documents:
    - #1 score=`0.0489`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7949`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`2`, rerankRank=`1`, rerankScore=`0.0489`, rerankFusionScore=`0.0489`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0486`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":2,"text":"PostgreSQL 16 18 generated column virtual stored difference"},{"index":3,"rank":2,"text":"generated column virtual stored limitations examples PostgreSQL version comparison"}]`
      - preview: - The generation expression cannot reference another generated column. - A generated column cannot have a column default or identity definition. - A generated column cannot be part...
    - #2 score=`0.0486`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7895`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`0.0486`, rerankFusionScore=`0.0486`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":1,"text":"PostgreSQL 16 18 generated column virtual stored difference"},{"index":3,"rank":1,"text":"generated column virtual stored limitations examples PostgreSQL version comparison"}]`
      - preview: # External RAG Sample: PostgreSQL Generated Columns Version Contrast Source URLs: - https://www.postgresql.org/docs/18/ddl-generated-columns.html - https://www.postgresql.org/docs/...
    - #3 score=`0.0476`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6291`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":3,"text":"PostgreSQL 16 18 generated column virtual stored difference"},{"index":3,"rank":3,"text":"generated column virtual stored limitations examples PostgreSQL version comparison"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.

answer:

```text
根据文档中的版本冲突处理指南，当知识库同时包含 PostgreSQL 16 和 18 文档时，回答 **generated column 的 virtual/stored 差异** 需要遵循以下规则 [1]：

**必须区分版本作答**，不能将 PostgreSQL 18 的默认行为直接套用到 PostgreSQL 16 上。正确的版本感知回答应包含：

- PostgreSQL **18** 支持两种生成列：**stored**（存储型）与 **virtual**（虚拟型）[2]。
  - 存储生成列在写入（INSERT/UPDATE）时计算并占用存储空间。
  - 虚拟生成列不占存储，在读取时计算 [2]。
  - 在 PostgreSQL 18 中，如果没有显式指定 `VIRTUAL` 或 `STORED`，**默认为 virtual** [2]。
- PostgreSQL **16** 的 `CREATE TABLE` 文档仅展示 `GENERATED ALWAYS AS (...) STORED` 语法，也就是说在 PG16 中，生成列就是 **stored** 类型，不存在 virtual 列，也没有 virtual 默认行为 [1]。

**回答时需明确指出版本适用范围**：
- 如果问题直接包含 PostgreSQL 18 文档或明确要求回答 PG18 特性，可以说明 virtual/stored 的差异及 virtual 默认行为。
- 如果问题只涉及 PostgreSQL 16 上下文，则应仅说明 stored 生成列，不应声称存在 virtual 选项或 virtual 默认值，除非问题本身已引入 PG18 文档 [1]。

另外，无论哪个版本，生成列都有共同的限制（摘自 [1][2]）：
- 生成表达式不能引用其他生成列。
- 生成列不能有列默认值或标识定义。
- 生成列不能作为分区键。
- 生成表达式只能使用不可变函数，不能包含子查询。

对于 **virtual** 生成列，还有额外的限制（如用户自定义类型和函数的特殊约束），而 stored 生成列不受这些限制 [1]。

因此，回答的要点是：**先区分版本，再说明 PG16 只有 stored，PG18 新增 virtual 且默认 virtual，同时给出各自限制**。如果不清楚用户所指版本，可主动询问或同时列出两版差异并标明适用版本。
```

### EXT-RFC9110-04 - weak_related

- question: 根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？
- should_answer: `false`
- expected_source_section: Framework Boundary
- completed: `true`
- duration_ms: `13096`
- score_mode: `manual`
- expected_points:
  - RFC 9110 不是 Spring Boot guide
  - 不包含 @ControllerAdvice 实现细节

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0471 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `4270` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，<br>@ControllerAdvice RFC 9110 HTTP semantics exception handler status code response entity<br>Spring Boot @ControllerAdvice implementation example according to RFC 9110 error handling problem details`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3379` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？， \| @ControllerAdvice RFC 9110 HTTP semantics exception handler status code response entity \| Spring Boot @ControllerAdvice implementation example according to RFC 9110 error handling problem details`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `ControllerAdvice \| RFC \| RFC 9110定义了哪些内容？ \| https://www.rfc-editor.org/rfc/rfc9110 \| External RAG Sample: RFC 9110 HTTP Semantics`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7402`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":1,"text":"@ControllerAdvice RFC 9110 HTTP semantics exception handler status code response entity"},{"index":3,"rank":1,"text":"Spring Boot @ControllerAdvice implementation example according to RFC 9110 error handling problem details"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0484`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6720`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":2,"text":"@ControllerAdvice RFC 9110 HTTP semantics exception handler status code response entity"},{"index":3,"rank":2,"text":"Spring Boot @ControllerAdvice implementation example according to RFC 9110 error handling problem details"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #3 score=`0.0476`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6076`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":3,"text":"@ControllerAdvice RFC 9110 HTTP semantics exception handler status code response entity"},{"index":3,"rank":3,"text":"Spring Boot @ControllerAdvice implementation example according to RFC 9110 error handling problem details"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
    - #4 score=`0.0156`, source=`owasp-top10-a01-a03-a05.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6108`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`Spring Boot @ControllerAdvice implementation example according to RFC 9110 error handling problem details`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"Spring Boot @ControllerAdvice implementation example according to RFC 9110 error handling problem details"}]`
      - preview: # External RAG Sample: OWASP Top 10 A01 A03 A05 Source URLs: - https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control/ - https://owasp.org/Top10/2021/A03_2021-Injection/ - htt...
  - documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7402`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":1,"text":"@ControllerAdvice RFC 9110 HTTP semantics exception handler status code response entity"},{"index":3,"rank":1,"text":"Spring Boot @ControllerAdvice implementation example according to RFC 9110 error handling problem details"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0484`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6720`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":2,"text":"@ControllerAdvice RFC 9110 HTTP semantics exception handler status code response entity"},{"index":3,"rank":2,"text":"Spring Boot @ControllerAdvice implementation example according to RFC 9110 error handling problem details"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #3 score=`0.0474`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6076`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0474`, rerankFusionScore=`0.0474`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":3,"text":"@ControllerAdvice RFC 9110 HTTP semantics exception handler status code response entity"},{"index":3,"rank":3,"text":"Spring Boot @ControllerAdvice implementation example according to RFC 9110 error handling problem details"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
    - #4 score=`0.0471`, source=`owasp-top10-a01-a03-a05.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6108`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0471`, rerankFusionScore=`0.0471`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`Spring Boot @ControllerAdvice implementation example according to RFC 9110 error handling problem details`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"Spring Boot @ControllerAdvice implementation example according to RFC 9110 error handling problem details"}]`
      - preview: # External RAG Sample: OWASP Top 10 A01 A03 A05 Source URLs: - https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control/ - https://owasp.org/Top10/2021/A03_2021-Injection/ - htt...

answer:

```text
根据提供的上下文，我无法回答您的问题。上下文中的 RFC 9110 片段明确指出：

> RFC 9110 is not a Spring Boot guide. It does not specify how to implement `@ControllerAdvice`, how to map Java exceptions, or how to structure a JSON error body in a Spring application. It can support general HTTP status-code reasoning, but not framework-specific code generation. [1]

因此，基于这些材料，我无法给出关于 Spring Boot 的 `@ControllerAdvice` 应该如何编写的指导。
```

### EXT-K8S-PV-04 - unanswerable

- question: 根据 PV 文档，MySQL InnoDB undo log 应该如何调优？
- should_answer: `false`
- expected_source_section: Boundary Notes
- completed: `true`
- duration_ms: `13679`
- score_mode: `manual`
- expected_points:
  - Kubernetes 存储对象说明
  - 不涉及 InnoDB undo log 调优

retrieval:
- event #1
  - retrieved_document_count: `1`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0492 .. 0.0492`
  - context_selected: `1` / dropped: `0` / truncated: `false`
  - context_chars: actual `983` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `1` / final `1`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，<br>InnoDB undo log调优参数 innodb_undo_log_truncate innodb_max_undo_log_size innodb_undo_tablespaces<br>InnoDB undo log调优配置步骤 性能影响 最佳实践 监控`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `4369` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `根据 PV 文档，MySQL InnoDB undo log 应该如何调优？， \| InnoDB undo log调优参数 innodb_undo_log_truncate innodb_max_undo_log_size innodb_undo_tablespaces \| InnoDB undo log调优配置步骤 性能影响 最佳实践 监控`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `InnoDB \| MySQL \| SQL \| MySQL invisible columns`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6261`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
  - documents:
    - #1 score=`0.0492`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6261`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...

answer:

```text
根据您提供的 PV（Persistent Volume）文档，其中明确说明：

> “This sample covers Kubernetes storage objects. It does not describe MySQL InnoDB MVCC, undo log tuning, or database transaction internals.”

因此，该文档不包含关于 MySQL InnoDB undo log 调优的任何内容，我无法基于当前上下文为您提供相关解答。如果您需要了解 InnoDB undo log 的调优方法，建议查阅 MySQL 官方文档或专门的数据库性能优化资料。
```
