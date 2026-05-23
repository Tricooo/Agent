# RAG Eval Result

- generated_at: `2026-05-23 22:33:51`
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
| EXT-ARTEMIS-01 | answerable | true | 6334 | true | 4 | 0.0156 .. 0.0164 | false | 0/3 | Nov. 16, 2022, Dec. 11, 2022, 25 days, 10 hours, 53 minutes | 根据提供的任务信息，Artemis I的发射日期是 **2022年11月16日**，溅落日期是 **2022年12月11日**，任务持续时间为 **25天10小时53分钟**[1]。 |  |
| EXT-ARTEMIS-02 | answerable_deep_section | true | 8570 | true | 4 | 0.0156 .. 0.0164 | false | 3/3 |  | 根据NASA提供的任务时间线，Artemis I 飞行第26天的关键事件顺序如下[1][2]： 1. 返回轨道修正燃烧-6（Return trajectory correction burn-6） 2. 乘员舱/服务舱分离（Crew mod... |  |
| EXT-RFC9110-03 | answerable_formula | true | 5049 | true | 2 | 0.0161 .. 0.0164 | false | 0/3 | HTTP/1.0 did not define 1xx, MUST NOT send a 1xx response, HTTP/1.0 client | 根据 RFC 9110 的规定，HTTP/1.0 没有定义 1xx 信息类状态码，因此服务器**必须不能**向 HTTP/1.0 客户端发送 1xx 响应。 |  |
| EXT-K8S-PV-03 | answerable_long_context | true | 6167 | true | 3 | 0.0159 .. 0.0164 | false | manual | — | 当 PVC 扩容失败时，文档给出的恢复流程如下（参见[1]）： 1. 将绑定到该 PVC 的 PersistentVolume 的回收策略（reclaim policy）标记为 **Retain**。 2. 删除该 PVC。由于设置了 Re... |  |
| EXT-OWASP-03 | answerable_deep_section | true | 19623 | true | 3 | 0.0159 .. 0.0164 | false | manual | — | 这三类风险的核心区别在于**故障的层面和原因**： - **Broken Access Control（访问控制失效）** 是**授权策略失败** [2][3]。它发生在系统已经验证了用户身份之后，但未能正确限制其能访问的资源或能执行的操作... |  |
| EXT-PG-04 | answerable_version_conflict | true | 19215 | true | 3 | 0.0159 .. 0.0164 | false | manual | — | 如果知识库中同时包含 PostgreSQL 16 和 18 的文档，回答 `generated column` 的 virtual/stored 差异时，必须**明确区分版本**，不能混用或套用 18 的新行为到 16 上。 根据文档对比：... |  |
| EXT-RFC9110-04 | weak_related | true | 10425 | false | 3 | 0.0159 .. 0.0164 | false | manual | — | 根据提供的上下文，我无法回答您的问题。 RFC 9110 定义了 HTTP 语义（如状态码、请求方法等），但它**不是 Spring Boot 开发指南**。上下文明确指出： “RFC 9110 is not a Spring Boot g... |  |
| EXT-K8S-PV-04 | unanswerable | true | 4781 | false | 1 | 0.0164 .. 0.0164 | false | manual | — | 根据提供的上下文信息，我无法回答关于 MySQL InnoDB undo log 调优的问题。 [1] 中明确说明：“此示例涵盖 Kubernetes 存储对象。它不描述 MySQL InnoDB MVCC、undo log 调优或数据库事... |  |

## Details

### EXT-ARTEMIS-01 - answerable

- question: Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？
- should_answer: `true`
- expected_source_section: Mission Overview
- completed: `true`
- duration_ms: `6334`
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
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `4326` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `4` / final `4`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `PASSTHROUGH` / mode `PASSTHROUGH` / variants `1` / elapsed_ms `0` / failure `—` / attempts `PASSTHROUGH:PASSTHROUGH` / texts `Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #2 score=`0.0161`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #3 score=`0.0159`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #4 score=`0.0156`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...
  - documents:
    - #1 score=`0.0164`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #2 score=`0.0161`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #3 score=`0.0159`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #4 score=`0.0156`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...

answer:

```text
根据提供的任务信息，Artemis I的发射日期是 **2022年11月16日**，溅落日期是 **2022年12月11日**，任务持续时间为 **25天10小时53分钟**[1]。
```

### EXT-ARTEMIS-02 - answerable_deep_section

- question: Artemis I Flight Day 26 的关键事件顺序是什么？
- should_answer: `true`
- expected_source_section: Flight Day 26
- completed: `true`
- duration_ms: `8570`
- score_mode: `literal`
- expected_points:
  - [x] Return trajectory correction burn-6
  - [x] Crew module/service module separation
  - [x] Splashdown

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0156 .. 0.0164`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `4326` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `4` / final `4`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `Artemis I Flight Day 26 的关键事件顺序是什么？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `PASSTHROUGH` / mode `PASSTHROUGH` / variants `1` / elapsed_ms `0` / failure `—` / attempts `PASSTHROUGH:PASSTHROUGH` / texts `Artemis I Flight Day 26 的关键事件顺序是什么？，`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #2 score=`0.0161`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #3 score=`0.0159`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #4 score=`0.0156`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...
  - documents:
    - #1 score=`0.0164`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #2 score=`0.0161`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #3 score=`0.0159`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #4 score=`0.0156`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...

answer:

```text
根据NASA提供的任务时间线，Artemis I 飞行第26天的关键事件顺序如下[1][2]：

1. 返回轨道修正燃烧-6（Return trajectory correction burn-6）
2. 乘员舱/服务舱分离（Crew module/service module separation）
3. 乘员舱抬升燃烧（Crew module raise burn）
4. 预定进入界面（Targeted entry interface）
5. 乘员舱进入界面（Crew module entry interface）
6. 溅落（Splashdown）

这些事件依次完成了最后的轨道修正、舱段分离、再入引导和最终的伞降溅落，标志着整个无人的Artemis I飞行测试顺利结束。
```

### EXT-RFC9110-03 - answerable_formula

- question: RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？
- should_answer: `true`
- expected_source_section: Informational 1xx
- completed: `true`
- duration_ms: `5049`
- score_mode: `literal`
- expected_points:
  - [ ] HTTP/1.0 did not define 1xx
  - [ ] MUST NOT send a 1xx response
  - [ ] HTTP/1.0 client

retrieval:
- event #1
  - retrieved_document_count: `2`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0161 .. 0.0164`
  - context_selected: `2` / dropped: `0` / truncated: `false`
  - context_chars: actual `2520` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `2` / final `2`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `PASSTHROUGH` / mode `PASSTHROUGH` / variants `1` / elapsed_ms `0` / failure `—` / attempts `PASSTHROUGH:PASSTHROUGH` / texts `RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0161`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
  - documents:
    - #1 score=`0.0164`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0161`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...

answer:

```text
根据 RFC 9110 的规定，HTTP/1.0 没有定义 1xx 信息类状态码，因此服务器**必须不能**向 HTTP/1.0 客户端发送 1xx 响应。
```

### EXT-K8S-PV-03 - answerable_long_context

- question: PVC 扩容失败时，文档给出的恢复流程是什么？
- should_answer: `true`
- expected_source_section: Recovering From Failure When Expanding Volumes
- completed: `true`
- duration_ms: `6167`
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
  - score_range: `0.0159 .. 0.0164`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `3668` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `3` / final `3`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `PVC 扩容失败时，文档给出的恢复流程是什么？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `PASSTHROUGH` / mode `PASSTHROUGH` / variants `1` / elapsed_ms `0` / failure `—` / attempts `PASSTHROUGH:PASSTHROUGH` / texts `PVC 扩容失败时，文档给出的恢复流程是什么？，`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
    - #2 score=`0.0161`, source=`kubernetes-persistent-volumes.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"}]`
      - preview: ## Reclaiming When a user is done with a volume, the PVC can be deleted. The reclaim policy tells Kubernetes what to do with the volume after the claim is released. Current reclaim...
    - #3 score=`0.0159`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...
  - documents:
    - #1 score=`0.0164`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
    - #2 score=`0.0161`, source=`kubernetes-persistent-volumes.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"}]`
      - preview: ## Reclaiming When a user is done with a volume, the PVC can be deleted. The reclaim policy tells Kubernetes what to do with the volume after the claim is released. Current reclaim...
    - #3 score=`0.0159`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...

answer:

```text
当 PVC 扩容失败时，文档给出的恢复流程如下（参见[1]）：

1. 将绑定到该 PVC 的 PersistentVolume 的回收策略（reclaim policy）标记为 **Retain**。
2. 删除该 PVC。由于设置了 Retain 策略，删除 PVC 不会导致数据丢失。
3. 从 PV 的 spec 中删除 `claimRef` 条目，使 PV 变为 **Available** 状态。
4. 重新创建一个比当前 PV 容量更小的 PVC，并在其中设置 `volumeName` 字段指向该现有 PV 的名称。
5. 恢复 PV 原有的回收策略。

需要注意：Kubernetes 不支持将 PVC 缩小到低于其当前大小。
```

### EXT-OWASP-03 - answerable_deep_section

- question: Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？
- should_answer: `true`
- expected_source_section: Category Differences
- completed: `true`
- duration_ms: `19623`
- score_mode: `manual`
- expected_points:
  - authorization policy failure
  - unsafe data flowing into interpreters
  - unsafe system, framework, server, or deployment settings

retrieval:
- event #1
  - retrieved_document_count: `3`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0159 .. 0.0164`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `3220` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `3` / final `3`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `PASSTHROUGH` / mode `PASSTHROUGH` / variants `1` / elapsed_ms `0` / failure `—` / attempts `PASSTHROUGH:PASSTHROUGH` / texts `Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`owasp-top10-a01-a03-a05.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"}]`
      - preview: A good answer should focus on permission boundaries, object ownership, and server-side enforcement. ## A03 Injection Injection occurs when untrusted input reaches an interpreter or...
    - #2 score=`0.0161`, source=`owasp-top10-a01-a03-a05.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"}]`
      - preview: # External RAG Sample: OWASP Top 10 A01 A03 A05 Source URLs: - https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control/ - https://owasp.org/Top10/2021/A03_2021-Injection/ - htt...
    - #3 score=`0.0159`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
  - documents:
    - #1 score=`0.0164`, source=`owasp-top10-a01-a03-a05.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"}]`
      - preview: A good answer should focus on permission boundaries, object ownership, and server-side enforcement. ## A03 Injection Injection occurs when untrusted input reaches an interpreter or...
    - #2 score=`0.0161`, source=`owasp-top10-a01-a03-a05.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"}]`
      - preview: # External RAG Sample: OWASP Top 10 A01 A03 A05 Source URLs: - https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control/ - https://owasp.org/Top10/2021/A03_2021-Injection/ - htt...
    - #3 score=`0.0159`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...

answer:

```text
这三类风险的核心区别在于**故障的层面和原因**：

- **Broken Access Control（访问控制失效）** 是**授权策略失败** [2][3]。它发生在系统已经验证了用户身份之后，但未能正确限制其能访问的资源或能执行的操作。问题出在**权限边界、对象所有权和服务端校验**缺失上 [1]。比如，用户 A 通过修改 URL 中的 ID 就能看到用户 B 的数据（IDOR）、普通用户能执行管理员操作、仅依赖前端隐藏按钮而没有后端检查等。简单说，**系统错误地让“不该做的人做了不该做的事”**。

- **Injection（注入）** 是**不受信的数据流入了解释器或命令上下文**，并被当成指令执行 [1][3]。问题在于输入处理：用户提交的数据没有被验证、过滤或转义，而且直接拼接到动态查询、命令或非参数化调用里 [1]。典型如 SQL 注入、跨站脚本（XSS）、系统命令注入。本质是 **“数据被当作代码执行了”**。

- **Security Misconfiguration（安全配置错误）** 是**系统、框架、服务器或部署环境自身搭建得不安全** [1][3]。它不是某一笔请求触发的逻辑缺陷，而是因默认账户未改、开启了不必要的服务或端口、堆栈未加固、安全头缺失、错误消息泄露堆栈信息等留下的隐患 [1]。可以理解为 **“房子建好了但门没锁、窗户开着”**。

三者可能同时出现在一次攻击中，但识别时要看**主要故障模式** [3]：
- 访问控制问题问的是“这个人有没有权限做这件事”。
- 注入问题问的是“用户输入有没有被安全地交给解析器”。
- 配置错误问的是“系统本身有没有因为不当设置而暴露弱点”。
```

### EXT-PG-04 - answerable_version_conflict

- question: 如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？
- should_answer: `true`
- expected_source_section: Version Conflict Handling
- completed: `true`
- duration_ms: `19215`
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
  - score_range: `0.0159 .. 0.0164`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `2932` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `3` / final `3`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `PASSTHROUGH` / mode `PASSTHROUGH` / variants `1` / elapsed_ms `0` / failure `—` / attempts `PASSTHROUGH:PASSTHROUGH` / texts `如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"}]`
      - preview: - The generation expression cannot reference another generated column. - A generated column cannot have a column default or identity definition. - A generated column cannot be part...
    - #2 score=`0.0161`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"}]`
      - preview: # External RAG Sample: PostgreSQL Generated Columns Version Contrast Source URLs: - https://www.postgresql.org/docs/18/ddl-generated-columns.html - https://www.postgresql.org/docs/...
    - #3 score=`0.0159`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.
  - documents:
    - #1 score=`0.0164`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"}]`
      - preview: - The generation expression cannot reference another generated column. - A generated column cannot have a column default or identity definition. - A generated column cannot be part...
    - #2 score=`0.0161`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"}]`
      - preview: # External RAG Sample: PostgreSQL Generated Columns Version Contrast Source URLs: - https://www.postgresql.org/docs/18/ddl-generated-columns.html - https://www.postgresql.org/docs/...
    - #3 score=`0.0159`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.

answer:

```text
如果知识库中同时包含 PostgreSQL 16 和 18 的文档，回答 `generated column` 的 virtual/stored 差异时，必须**明确区分版本**，不能混用或套用 18 的新行为到 16 上。

根据文档对比：

- **PostgreSQL 18** 拥有 `stored` 和 `virtual` 两种生成列，并且**当未指定关键字时，默认为 `virtual`**（即虚拟生成列）[2]。
- **PostgreSQL 16** 的 `CREATE TABLE` 语法中，生成列仅表现为 **`GENERATED ALWAYS AS (...) STORED`**，即只能使用存储生成列，没有 `virtual` 选项，也没有“默认虚拟”的概念[1]。

因此，正确做法如下：

1. 如果用户问题**明确针对 PostgreSQL 18**，可以说明有两种类型（stored/virtual），并指出默认为 `virtual`。
2. 如果问题**未指明版本，或同时涉及两个版本**，应分别说明：
   - “PostgreSQL 18 支持 stored 和 virtual 生成列，且不写关键字时默认是 virtual。”
   - “PostgreSQL 16 中的生成列必须指定为 `STORED`，不存在 virtual 类型，也不应套用 18 的默认规则。”
3. **绝对不能**在只参考 PostgreSQL 16 文档的情况下，给出“默认 virtual”这类属于 18 的结论[1]。

若知识库检索到的素材混合了 16 与 18 的内容，必须以版本为线索进行解释，避免误导。
```

### EXT-RFC9110-04 - weak_related

- question: 根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？
- should_answer: `false`
- expected_source_section: Framework Boundary
- completed: `true`
- duration_ms: `10425`
- score_mode: `manual`
- expected_points:
  - RFC 9110 不是 Spring Boot guide
  - 不包含 @ControllerAdvice 实现细节

retrieval:
- event #1
  - retrieved_document_count: `3`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0159 .. 0.0164`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `3177` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `3` / final `3`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `PASSTHROUGH` / mode `PASSTHROUGH` / variants `1` / elapsed_ms `0` / failure `—` / attempts `PASSTHROUGH:PASSTHROUGH` / texts `根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0161`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #3 score=`0.0159`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
  - documents:
    - #1 score=`0.0164`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0161`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0161`, queryFusionRank=`2`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #3 score=`0.0159`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...

answer:

```text
根据提供的上下文，我无法回答您的问题。

RFC 9110 定义了 HTTP 语义（如状态码、请求方法等），但它**不是 Spring Boot 开发指南**。上下文明确指出：

“RFC 9110 is not a Spring Boot guide. It does not specify how to implement `@ControllerAdvice`, how to map Java exceptions, or how to structure a JSON error body in a Spring application.”（[1]，Framework Boundary 部分）

因此，RFC 9110 中不包含任何关于 Spring Boot `@ControllerAdvice` 实现方式的规范，基于目前提供的资料无法给出具体写法。
```

### EXT-K8S-PV-04 - unanswerable

- question: 根据 PV 文档，MySQL InnoDB undo log 应该如何调优？
- should_answer: `false`
- expected_source_section: Boundary Notes
- completed: `true`
- duration_ms: `4781`
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
  - score_range: `0.0164 .. 0.0164`
  - context_selected: `1` / dropped: `0` / truncated: `false`
  - context_chars: actual `983` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `false` / mode `PASSTHROUGH` / candidates `1` / final `1`
  - rerank_runtime: model `` / endpoint `` / failure_reason ``
  - rerank_query: policy `ORIGINAL` / text `根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `PASSTHROUGH` / mode `PASSTHROUGH` / variants `1` / elapsed_ms `0` / failure `—` / attempts `PASSTHROUGH:PASSTHROUGH` / texts `根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
  - documents:
    - #1 score=`0.0164`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`—`, rrfScore=`—`, vectorRank=`—`, vectorScore=`—`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`False`, rerankMode=`PASSTHROUGH`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...

answer:

```text
根据提供的上下文信息，我无法回答关于 MySQL InnoDB undo log 调优的问题。

[1] 中明确说明：“此示例涵盖 Kubernetes 存储对象。它不描述 MySQL InnoDB MVCC、undo log 调优或数据库事务内部信息。”因此，相关内容不在所提供的文档范围内，我无法给出相关建议。
```
