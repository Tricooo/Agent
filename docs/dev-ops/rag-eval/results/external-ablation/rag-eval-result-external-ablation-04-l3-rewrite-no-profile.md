# RAG Eval Result

- generated_at: `2026-05-23 22:38:49`
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
| EXT-ARTEMIS-01 | answerable | true | 11072 | true | 4 | 0.0469 .. 0.0492 | false | 0/3 | Nov. 16, 2022, Dec. 11, 2022, 25 days, 10 hours, 53 minutes | 根据任务资料，Artemis I 的发射日期是 2022 年 11 月 16 日，溅落日期是 2022 年 12 月 11 日，任务持续时间为 25 天 10 小时 53 分钟[1]。 |  |
| EXT-ARTEMIS-02 | answerable_deep_section | true | 15863 | true | 4 | 0.0474 .. 0.0492 | false | 2/3 | Splashdown | 根据提供的任务时间线信息，Artemis I 在飞行第26天的关键事件顺序如下[1]： 1. **执行返回轨迹修正燃烧-6** (Return trajectory correction burn-6) 2. **乘员舱与服务舱分离** (... |  |
| EXT-RFC9110-03 | answerable_formula | true | 9866 | true | 2 | 0.0484 .. 0.0492 | false | 0/3 | HTTP/1.0 did not define 1xx, MUST NOT send a 1xx response, HTTP/1.0 client | 根据 RFC 9110 的规定，HTTP/1.0 并未定义 1xx（信息性）状态码，服务器**不得**向 HTTP/1.0 客户端发送 1xx 响应（[1]）。 |  |
| EXT-K8S-PV-03 | answerable_long_context | true | 17399 | true | 3 | 0.0479 .. 0.0492 | false | manual | — | 根据文档说明，当 PVC 扩容失败（底层存储无法满足请求的大小）时，集群管理员可以按以下恢复流程手动处理[1]： 1. 将绑定到该 PVC 的 PersistentVolume（PV）的回收策略标记为 **Retain**。 2. 删除 P... |  |
| EXT-OWASP-03 | answerable_deep_section | true | 24163 | true | 4 | 0.0469 .. 0.0489 | false | manual | — | 根据您提供的上下文，**Broken Access Control（失效的访问控制）**、**Injection（注入）** 与 **Security Misconfiguration（安全配置错误）** 的核心区别在于它们对应的**根本失... |  |
| EXT-PG-04 | answerable_version_conflict | true | 21422 | true | 4 | 0.0469 .. 0.0489 | false | manual | — | 当知识库同时包含 PostgreSQL 16 和 18 文档时，关于 generated column 的 virtual/stored 差异，必须按**版本区分**进行回答，核心要点如下： - **PostgreSQL 18** 支持 `... |  |
| EXT-RFC9110-04 | weak_related | true | 14721 | false | 3 | 0.0476 .. 0.0492 | false | manual | — | 根据提供的 RFC 9110 上下文，我无法回答如何编写 Spring Boot `@ControllerAdvice` 的问题。 上下文明确指出： > RFC 9110 is not a Spring Boot guide. It doe... |  |
| EXT-K8S-PV-04 | unanswerable | true | 12087 | false | 1 | 0.0492 .. 0.0492 | false | manual | — | 根据提供的上下文信息，**无法回答您的问题**。 给出的文档片段（[1]）是关于 Kubernetes 持久卷（PV）和持久卷声明（PVC）的存储对象说明，其边界注释明确提到： > This sample covers Kubernetes... |  |

## Details

### EXT-ARTEMIS-01 - answerable

- question: Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？
- should_answer: `true`
- expected_source_section: Mission Overview
- completed: `true`
- duration_ms: `11072`
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
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，<br>Artemis I mission launch splashdown duration date time Artemis 1 Orion SLS lunar<br>Artemis I 任务时间线 发射 溅落 持续天数 具体日期 里程碑 飞行日志 NASA`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `4684` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？， \| Artemis I mission launch splashdown duration date time Artemis 1 Orion SLS lunar \| Artemis I 任务时间线 发射 溅落 持续天数 具体日期 里程碑 飞行日志 NASA`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7541`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":1,"text":"Artemis I mission launch splashdown duration date time Artemis 1 Orion SLS lunar"},{"index":3,"rank":1,"text":"Artemis I 任务时间线 发射 溅落 持续天数 具体日期 里程碑 飞行日志 NASA"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #2 score=`0.0484`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7407`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":2,"text":"Artemis I mission launch splashdown duration date time Artemis 1 Orion SLS lunar"},{"index":3,"rank":2,"text":"Artemis I 任务时间线 发射 溅落 持续天数 具体日期 里程碑 飞行日志 NASA"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #3 score=`0.0476`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6757`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":3,"text":"Artemis I mission launch splashdown duration date time Artemis 1 Orion SLS lunar"},{"index":3,"rank":3,"text":"Artemis I 任务时间线 发射 溅落 持续天数 具体日期 里程碑 飞行日志 NASA"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #4 score=`0.0469`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6502`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0469`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":4,"text":"Artemis I mission launch splashdown duration date time Artemis 1 Orion SLS lunar"},{"index":3,"rank":4,"text":"Artemis I 任务时间线 发射 溅落 持续天数 具体日期 里程碑 飞行日志 NASA"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...
  - documents:
    - #1 score=`0.0492`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7541`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":1,"text":"Artemis I mission launch splashdown duration date time Artemis 1 Orion SLS lunar"},{"index":3,"rank":1,"text":"Artemis I 任务时间线 发射 溅落 持续天数 具体日期 里程碑 飞行日志 NASA"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #2 score=`0.0484`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6757`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":3,"text":"Artemis I mission launch splashdown duration date time Artemis 1 Orion SLS lunar"},{"index":3,"rank":3,"text":"Artemis I 任务时间线 发射 溅落 持续天数 具体日期 里程碑 飞行日志 NASA"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #3 score=`0.0476`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7407`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":2,"text":"Artemis I mission launch splashdown duration date time Artemis 1 Orion SLS lunar"},{"index":3,"rank":2,"text":"Artemis I 任务时间线 发射 溅落 持续天数 具体日期 里程碑 飞行日志 NASA"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #4 score=`0.0469`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6502`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0469`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":4,"text":"Artemis I mission launch splashdown duration date time Artemis 1 Orion SLS lunar"},{"index":3,"rank":4,"text":"Artemis I 任务时间线 发射 溅落 持续天数 具体日期 里程碑 飞行日志 NASA"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...

answer:

```text
根据任务资料，Artemis I 的发射日期是 2022 年 11 月 16 日，溅落日期是 2022 年 12 月 11 日，任务持续时间为 25 天 10 小时 53 分钟[1]。
```

### EXT-ARTEMIS-02 - answerable_deep_section

- question: Artemis I Flight Day 26 的关键事件顺序是什么？
- should_answer: `true`
- expected_source_section: Flight Day 26
- completed: `true`
- duration_ms: `15863`
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
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `Artemis I Flight Day 26 的关键事件顺序是什么？，<br>Artemis I Flight Day 26 timeline key events milestones activities sequence<br>Artemis I FD26 critical events order burn maneuvers trajectory communication passes science operations`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `4346` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `Artemis I Flight Day 26 的关键事件顺序是什么？， \| Artemis I Flight Day 26 timeline key events milestones activities sequence \| Artemis I FD26 critical events order burn maneuvers trajectory communication passes science operations`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0489`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7962`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":2,"text":"Artemis I Flight Day 26 timeline key events milestones activities sequence"},{"index":3,"rank":1,"text":"Artemis I FD26 critical events order burn maneuvers trajectory communication passes science operations"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #2 score=`0.0484`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7517`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":1,"text":"Artemis I Flight Day 26 timeline key events milestones activities sequence"},{"index":3,"rank":2,"text":"Artemis I FD26 critical events order burn maneuvers trajectory communication passes science operations"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #3 score=`0.0474`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7551`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0474`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":4,"text":"Artemis I Flight Day 26 timeline key events milestones activities sequence"},{"index":3,"rank":4,"text":"Artemis I FD26 critical events order burn maneuvers trajectory communication passes science operations"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #4 score=`0.0474`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7427`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0474`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":3,"text":"Artemis I Flight Day 26 timeline key events milestones activities sequence"},{"index":3,"rank":3,"text":"Artemis I FD26 critical events order burn maneuvers trajectory communication passes science operations"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...
  - documents:
    - #1 score=`0.0492`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7962`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":2,"text":"Artemis I Flight Day 26 timeline key events milestones activities sequence"},{"index":3,"rank":1,"text":"Artemis I FD26 critical events order burn maneuvers trajectory communication passes science operations"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #2 score=`0.0479`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7517`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0479`, rerankFusionScore=`0.0479`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":1,"text":"Artemis I Flight Day 26 timeline key events milestones activities sequence"},{"index":3,"rank":2,"text":"Artemis I FD26 critical events order burn maneuvers trajectory communication passes science operations"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #3 score=`0.0476`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7551`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0474`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":4,"text":"Artemis I Flight Day 26 timeline key events milestones activities sequence"},{"index":3,"rank":4,"text":"Artemis I FD26 critical events order burn maneuvers trajectory communication passes science operations"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #4 score=`0.0474`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7427`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0474`, rerankFusionScore=`0.0474`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0474`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":3,"text":"Artemis I Flight Day 26 timeline key events milestones activities sequence"},{"index":3,"rank":3,"text":"Artemis I FD26 critical events order burn maneuvers trajectory communication passes science operations"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...

answer:

```text
根据提供的任务时间线信息，Artemis I 在飞行第26天的关键事件顺序如下[1]：

1.  **执行返回轨迹修正燃烧-6** (Return trajectory correction burn-6)
2.  **乘员舱与服务舱分离** (Crew module/service module separation)
3.  **乘员舱抬升燃烧** (Crew module raise burn)
4.  **进入目标再入接口** (Targeted entry interface)

随后发生的事件包括乘员舱进入接口和最终的溅落[3]。
```

### EXT-RFC9110-03 - answerable_formula

- question: RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？
- should_answer: `true`
- expected_source_section: Informational 1xx
- completed: `true`
- duration_ms: `9866`
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
  - score_range: `0.0484 .. 0.0492`
  - context_selected: `2` / dropped: `0` / truncated: `false`
  - context_chars: actual `2520` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `2` / final `2`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，<br>RFC 9110 1xx 状态码 HTTP/1.0 客户端 限制 100 Continue 101 Switching Protocols 102 Processing Hop-by-hop End-to-end<br>RFC 9110 1xx 状态码 HTTP/1.0 兼容性 限制条件 规范要求 不允许的行为 错误处理 边界情况`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3501` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？， \| RFC 9110 1xx 状态码 HTTP/1.0 客户端 限制 100 Continue 101 Switching Protocols 102 Processing Hop-by-hop End-to-end \| RFC 9110 1xx 状态码 HTTP/1.0 兼容性 限制条件 规范要求 不允许的行为 错误处理 边界情况`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0328`, vectorRank=`1`, vectorScore=`0.7447`, keywordRank=`1`, keywordScore=`0.3000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":1,"text":"RFC 9110 1xx 状态码 HTTP/1.0 客户端 限制 100 Continue 101 Switching Protocols 102 Processing Hop-by-hop End-to-end"},{"index":3,"rank":1,"text":"RFC 9110 1xx 状态码 HTTP/1.0 兼容性 限制条件 规范要求 不允许的行为 错误处理 边界情况"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0484`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7075`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":2,"text":"RFC 9110 1xx 状态码 HTTP/1.0 客户端 限制 100 Continue 101 Switching Protocols 102 Processing Hop-by-hop End-to-end"},{"index":3,"rank":2,"text":"RFC 9110 1xx 状态码 HTTP/1.0 兼容性 限制条件 规范要求 不允许的行为 错误处理 边界情况"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
  - documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0328`, vectorRank=`1`, vectorScore=`0.7447`, keywordRank=`1`, keywordScore=`0.3000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":1,"text":"RFC 9110 1xx 状态码 HTTP/1.0 客户端 限制 100 Continue 101 Switching Protocols 102 Processing Hop-by-hop End-to-end"},{"index":3,"rank":1,"text":"RFC 9110 1xx 状态码 HTTP/1.0 兼容性 限制条件 规范要求 不允许的行为 错误处理 边界情况"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0484`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7075`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":2,"text":"RFC 9110 1xx 状态码 HTTP/1.0 客户端 限制 100 Continue 101 Switching Protocols 102 Processing Hop-by-hop End-to-end"},{"index":3,"rank":2,"text":"RFC 9110 1xx 状态码 HTTP/1.0 兼容性 限制条件 规范要求 不允许的行为 错误处理 边界情况"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...

answer:

```text
根据 RFC 9110 的规定，HTTP/1.0 并未定义 1xx（信息性）状态码，服务器**不得**向 HTTP/1.0 客户端发送 1xx 响应（[1]）。
```

### EXT-K8S-PV-03 - answerable_long_context

- question: PVC 扩容失败时，文档给出的恢复流程是什么？
- should_answer: `true`
- expected_source_section: Recovering From Failure When Expanding Volumes
- completed: `true`
- duration_ms: `17399`
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
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `PVC 扩容失败时，文档给出的恢复流程是什么？，<br>PVC扩容失败 恢复流程 PVC resize failure recovery steps PersistentVolumeClaim expansion error troubleshooting<br>PVC扩容失败原因 恢复步骤 限制条件 错误 扩容恢复 判断标准`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `5165` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `PVC 扩容失败时，文档给出的恢复流程是什么？， \| PVC扩容失败 恢复流程 PVC resize failure recovery steps PersistentVolumeClaim expansion error troubleshooting \| PVC扩容失败原因 恢复步骤 限制条件 错误 扩容恢复 判断标准`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0486`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7061`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0486`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":3,"text":"PVC扩容失败 恢复流程 PVC resize failure recovery steps PersistentVolumeClaim expansion error troubleshooting"},{"index":3,"rank":1,"text":"PVC扩容失败原因 恢复步骤 限制条件 错误 扩容恢复 判断标准"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
    - #2 score=`0.0484`, source=`kubernetes-persistent-volumes.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7002`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":2,"text":"PVC扩容失败 恢复流程 PVC resize failure recovery steps PersistentVolumeClaim expansion error troubleshooting"},{"index":3,"rank":2,"text":"PVC扩容失败原因 恢复步骤 限制条件 错误 扩容恢复 判断标准"}]`
      - preview: ## Reclaiming When a user is done with a volume, the PVC can be deleted. The reclaim policy tells Kubernetes what to do with the volume after the claim is released. Current reclaim...
    - #3 score=`0.0481`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6907`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0481`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":1,"text":"PVC扩容失败 恢复流程 PVC resize failure recovery steps PersistentVolumeClaim expansion error troubleshooting"},{"index":3,"rank":3,"text":"PVC扩容失败原因 恢复步骤 限制条件 错误 扩容恢复 判断标准"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...
  - documents:
    - #1 score=`0.0492`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7061`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0486`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":3,"text":"PVC扩容失败 恢复流程 PVC resize failure recovery steps PersistentVolumeClaim expansion error troubleshooting"},{"index":3,"rank":1,"text":"PVC扩容失败原因 恢复步骤 限制条件 错误 扩容恢复 判断标准"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
    - #2 score=`0.0481`, source=`kubernetes-persistent-volumes.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7002`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0481`, rerankFusionScore=`0.0481`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":2,"text":"PVC扩容失败 恢复流程 PVC resize failure recovery steps PersistentVolumeClaim expansion error troubleshooting"},{"index":3,"rank":2,"text":"PVC扩容失败原因 恢复步骤 限制条件 错误 扩容恢复 判断标准"}]`
      - preview: ## Reclaiming When a user is done with a volume, the PVC can be deleted. The reclaim policy tells Kubernetes what to do with the volume after the claim is released. Current reclaim...
    - #3 score=`0.0479`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6907`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0479`, rerankFusionScore=`0.0479`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0481`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":1,"text":"PVC扩容失败 恢复流程 PVC resize failure recovery steps PersistentVolumeClaim expansion error troubleshooting"},{"index":3,"rank":3,"text":"PVC扩容失败原因 恢复步骤 限制条件 错误 扩容恢复 判断标准"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...

answer:

```text
根据文档说明，当 PVC 扩容失败（底层存储无法满足请求的大小）时，集群管理员可以按以下恢复流程手动处理[1]：

1. 将绑定到该 PVC 的 PersistentVolume（PV）的回收策略标记为 **Retain**。
2. 删除 PVC。由于回收策略为 Retain，删除 PVC 不会丢失数据。
3. 删除 PV 规格中的 `claimRef` 条目，使 PV 变为 **Available** 状态。
4. 以**小于** PV 当前容量的尺寸重新创建 PVC，并在 PVC 中设置 `volumeName` 为已有 PV 的名称。
5. 恢复 PV 原先的回收策略。

此外，文档特别说明：**Kubernetes 不支持将 PVC 缩小到当前尺寸以下**（即不能直接缩容，上述流程实质是通过解绑后创建更小尺寸的 PVC 来间接恢复）。
```

### EXT-OWASP-03 - answerable_deep_section

- question: Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？
- should_answer: `true`
- expected_source_section: Category Differences
- completed: `true`
- duration_ms: `24163`
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
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，<br>Broken Access Control Injection Security Misconfiguration OWASP Top 10 CWE-284 CWE-77 CWE-16 differences<br>OWASP Top 10 risk comparison broken access control vs injection vs security misconfiguration examples impacts root causes prevention`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `4207` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？， \| Broken Access Control Injection Security Misconfiguration OWASP Top 10 CWE-284 CWE-77 CWE-16 differences \| OWASP Top 10 risk comparison broken access control vs injection vs security misconfiguration examples impacts root causes prevention`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0489`, source=`owasp-top10-a01-a03-a05.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7612`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":1,"text":"Broken Access Control Injection Security Misconfiguration OWASP Top 10 CWE-284 CWE-77 CWE-16 differences"},{"index":3,"rank":1,"text":"OWASP Top 10 risk comparison broken access control vs injection vs security misconfiguration examples impacts root causes prevention"}]`
      - preview: # External RAG Sample: OWASP Top 10 A01 A03 A05 Source URLs: - https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control/ - https://owasp.org/Top10/2021/A03_2021-Injection/ - htt...
    - #2 score=`0.0486`, source=`owasp-top10-a01-a03-a05.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7852`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0486`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":2,"text":"Broken Access Control Injection Security Misconfiguration OWASP Top 10 CWE-284 CWE-77 CWE-16 differences"},{"index":3,"rank":2,"text":"OWASP Top 10 risk comparison broken access control vs injection vs security misconfiguration examples impacts root causes prevention"}]`
      - preview: A good answer should focus on permission boundaries, object ownership, and server-side enforcement. ## A03 Injection Injection occurs when untrusted input reaches an interpreter or...
    - #3 score=`0.0476`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7313`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":3,"text":"Broken Access Control Injection Security Misconfiguration OWASP Top 10 CWE-284 CWE-77 CWE-16 differences"},{"index":3,"rank":3,"text":"OWASP Top 10 risk comparison broken access control vs injection vs security misconfiguration examples impacts root causes prevention"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
    - #4 score=`0.0156`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6315`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`Broken Access Control Injection Security Misconfiguration OWASP Top 10 CWE-284 CWE-77 CWE-16 differences`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":4,"text":"Broken Access Control Injection Security Misconfiguration OWASP Top 10 CWE-284 CWE-77 CWE-16 differences"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
  - documents:
    - #1 score=`0.0489`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7313`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`1`, rerankScore=`0.0489`, rerankFusionScore=`0.0489`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":2},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":3,"text":"Broken Access Control Injection Security Misconfiguration OWASP Top 10 CWE-284 CWE-77 CWE-16 differences"},{"index":3,"rank":3,"text":"OWASP Top 10 risk comparison broken access control vs injection vs security misconfiguration examples impacts root causes prevention"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
    - #2 score=`0.0486`, source=`owasp-top10-a01-a03-a05.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7852`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0486`, rerankFusionScore=`0.0486`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":1},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0486`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":2,"text":"Broken Access Control Injection Security Misconfiguration OWASP Top 10 CWE-284 CWE-77 CWE-16 differences"},{"index":3,"rank":2,"text":"OWASP Top 10 risk comparison broken access control vs injection vs security misconfiguration examples impacts root causes prevention"}]`
      - preview: A good answer should focus on permission boundaries, object ownership, and server-side enforcement. ## A03 Injection Injection occurs when untrusted input reaches an interpreter or...
    - #3 score=`0.0476`, source=`owasp-top10-a01-a03-a05.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7612`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":1,"text":"Broken Access Control Injection Security Misconfiguration OWASP Top 10 CWE-284 CWE-77 CWE-16 differences"},{"index":3,"rank":1,"text":"OWASP Top 10 risk comparison broken access control vs injection vs security misconfiguration examples impacts root causes prevention"}]`
      - preview: # External RAG Sample: OWASP Top 10 A01 A03 A05 Source URLs: - https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control/ - https://owasp.org/Top10/2021/A03_2021-Injection/ - htt...
    - #4 score=`0.0469`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6315`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`Broken Access Control Injection Security Misconfiguration OWASP Top 10 CWE-284 CWE-77 CWE-16 differences`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":4,"text":"Broken Access Control Injection Security Misconfiguration OWASP Top 10 CWE-284 CWE-77 CWE-16 differences"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...

answer:

```text
根据您提供的上下文，**Broken Access Control（失效的访问控制）**、**Injection（注入）** 与 **Security Misconfiguration（安全配置错误）** 的核心区别在于它们对应的**根本失效模式**不同[1]：

- **Broken Access Control** 是**授权策略失败**：用户能够执行超出其预期权限的操作。
  典型表现包括：违反最小权限原则、通过篡改 URL/参数绕过访问检查、不安全的直接对象引用（IDOR）、以及从普通用户越权为管理员等[3]。

- **Injection** 是**不可信数据流入解释器或命令上下文时缺乏安全处理**：攻击者提交的恶意数据被当作代码或命令执行。
  典型表现包括：用户输入未经验证、过滤或净化；在解释器中直接拼接动态查询或使用非参数化调用（如 SQL 注入、跨站脚本等）[2]。

- **Security Misconfiguration** 是**系统、框架、服务器或部署中的不安全配置**。
  典型表现包括：缺少安全加固、启用不必要的端口/服务/默认账户、错误消息泄露堆栈跟踪、缺少安全头、使用过期组件，以及新系统未开启新的安全特性等[2]。

**简单总结区别**：
- 访问控制：**有没有权限做这件事？**（授权与策略）
- 注入：**用户输入的内容会不会被当成命令执行？**（代码/数据边界）
- 配置错误：**系统环境本身是不是以不安全的方式搭建或部署？**（默认配置、补丁、头信息等）

虽然同一次安全事件可能涉及多个类别，但精确的回答应当识别其**主要失效模式**[1]。
```

### EXT-PG-04 - answerable_version_conflict

- question: 如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？
- should_answer: `true`
- expected_source_section: Version Conflict Handling
- completed: `true`
- duration_ms: `21422`
- score_mode: `manual`
- expected_points:
  - 区分版本
  - PostgreSQL 18 has stored and virtual
  - PostgreSQL 18 defaults to virtual
  - PostgreSQL 16
  - STORED

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0469 .. 0.0489`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `4266` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，<br>PostgreSQL 16 18 generated column virtual stored difference syntax example<br>PostgreSQL generated column virtual vs stored limitations indexing performance storage requirements version differences`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `4486` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？， \| PostgreSQL 16 18 generated column virtual stored difference syntax example \| PostgreSQL generated column virtual vs stored limitations indexing performance storage requirements version differences`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0489`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7895`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":1,"text":"PostgreSQL 16 18 generated column virtual stored difference syntax example"},{"index":3,"rank":1,"text":"PostgreSQL generated column virtual vs stored limitations indexing performance storage requirements version differences"}]`
      - preview: # External RAG Sample: PostgreSQL Generated Columns Version Contrast Source URLs: - https://www.postgresql.org/docs/18/ddl-generated-columns.html - https://www.postgresql.org/docs/...
    - #2 score=`0.0486`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7949`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0486`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":2,"text":"PostgreSQL 16 18 generated column virtual stored difference syntax example"},{"index":3,"rank":2,"text":"PostgreSQL generated column virtual vs stored limitations indexing performance storage requirements version differences"}]`
      - preview: - The generation expression cannot reference another generated column. - A generated column cannot have a column default or identity definition. - A generated column cannot be part...
    - #3 score=`0.0476`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6291`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":3,"text":"PostgreSQL 16 18 generated column virtual stored difference syntax example"},{"index":3,"rank":3,"text":"PostgreSQL generated column virtual vs stored limitations indexing performance storage requirements version differences"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.
    - #4 score=`0.0156`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6086`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`PostgreSQL generated column virtual vs stored limitations indexing performance storage requirements version differences`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"PostgreSQL generated column virtual vs stored limitations indexing performance storage requirements version differences"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...
  - documents:
    - #1 score=`0.0489`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7949`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`2`, rerankRank=`1`, rerankScore=`0.0489`, rerankFusionScore=`0.0489`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0486`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":2,"text":"PostgreSQL 16 18 generated column virtual stored difference syntax example"},{"index":3,"rank":2,"text":"PostgreSQL generated column virtual vs stored limitations indexing performance storage requirements version differences"}]`
      - preview: - The generation expression cannot reference another generated column. - A generated column cannot have a column default or identity definition. - A generated column cannot be part...
    - #2 score=`0.0486`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7895`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`0.0486`, rerankFusionScore=`0.0486`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":1,"text":"PostgreSQL 16 18 generated column virtual stored difference syntax example"},{"index":3,"rank":1,"text":"PostgreSQL generated column virtual vs stored limitations indexing performance storage requirements version differences"}]`
      - preview: # External RAG Sample: PostgreSQL Generated Columns Version Contrast Source URLs: - https://www.postgresql.org/docs/18/ddl-generated-columns.html - https://www.postgresql.org/docs/...
    - #3 score=`0.0476`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6291`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":3,"text":"PostgreSQL 16 18 generated column virtual stored difference syntax example"},{"index":3,"rank":3,"text":"PostgreSQL generated column virtual vs stored limitations indexing performance storage requirements version differences"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.
    - #4 score=`0.0469`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6086`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`PostgreSQL generated column virtual vs stored limitations indexing performance storage requirements version differences`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"PostgreSQL generated column virtual vs stored limitations indexing performance storage requirements version differences"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...

answer:

```text
当知识库同时包含 PostgreSQL 16 和 18 文档时，关于 generated column 的 virtual/stored 差异，必须按**版本区分**进行回答，核心要点如下：

- **PostgreSQL 18** 支持 `STORED` 与 `VIRTUAL` 两种生成列，**不指定时默认为 `VIRTUAL`**[1][2]。同时，`VIRTUAL` 列对用户定义类型与函数有额外限制，而 `STORED` 列没有此类限制[1]。
- **PostgreSQL 16** 只支持 `STORED` 生成列，语法必须显式写出 `GENERATED ALWAYS AS (…) STORED`，不存在 `VIRTUAL` 类型[1]。
- **回答时必须做出版本声明**，不能将 PostgreSQL 18 的“默认 virtual”结论直接套用到 PostgreSQL 16 上[1]。若问题未指定版本，应同时说明两个版本的行为差异，并标注各自的适用版本。

简而言之，答案结构应像这样组织：
1. 指出 PostgreSQL 18 有 `VIRTUAL`/`STORED`，默认 `VIRTUAL`；
2. 指出 PostgreSQL 16 仅有 `STORED`；
3. 明确提示不要混用默认行为，给出对应的版本限定。

其他无关主题（如 Kubernetes PV 等）不应混入回答。如果问题仅与其中一个版本相关且文档齐全，直接按该版本回答；若问题模糊或跨版本，则主动对比并澄清版本差异。
```

### EXT-RFC9110-04 - weak_related

- question: 根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？
- should_answer: `false`
- expected_source_section: Framework Boundary
- completed: `true`
- duration_ms: `14721`
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
  - score_range: `0.0476 .. 0.0492`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `3177` / max `6000`
  - context_salience: cues `—` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `3` / final `3`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，<br>Spring Boot @ControllerAdvice 根据 RFC 9110 错误处理 HTTP 状态码 ResponseEntity<br>Spring Boot @ControllerAdvice 异常处理实现步骤 配置示例 状态码映射`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `4071` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？， \| Spring Boot @ControllerAdvice 根据 RFC 9110 错误处理 HTTP 状态码 ResponseEntity \| Spring Boot @ControllerAdvice 异常处理实现步骤 配置示例 状态码映射`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0328`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7402`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0328`, queryFusionRank=`1`, queryVariantHitCount=`2`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":1,"text":"Spring Boot @ControllerAdvice 根据 RFC 9110 错误处理 HTTP 状态码 ResponseEntity"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0323`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6720`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0323`, queryFusionRank=`2`, queryVariantHitCount=`2`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":2,"text":"Spring Boot @ControllerAdvice 根据 RFC 9110 错误处理 HTTP 状态码 ResponseEntity"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #3 score=`0.0159`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6076`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
  - documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7402`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0328`, queryFusionRank=`1`, queryVariantHitCount=`2`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":1,"text":"Spring Boot @ControllerAdvice 根据 RFC 9110 错误处理 HTTP 状态码 ResponseEntity"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0484`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6720`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0323`, queryFusionRank=`2`, queryVariantHitCount=`2`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":2,"text":"Spring Boot @ControllerAdvice 根据 RFC 9110 错误处理 HTTP 状态码 ResponseEntity"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #3 score=`0.0476`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6076`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...

answer:

```text
根据提供的 RFC 9110 上下文，我无法回答如何编写 Spring Boot `@ControllerAdvice` 的问题。

上下文明确指出：

> RFC 9110 is not a Spring Boot guide. It does not specify how to implement `@ControllerAdvice`, how to map Java exceptions, or how to structure a JSON error body in a Spring application. It can support general HTTP status-code reasoning, but not framework-specific code generation.[1]

RFC 9110 只定义 HTTP 语义（如状态码、方法、头部字段等），并不涉及 Spring Boot 框架的具体用法。因此，这个问题超出了 RFC 9110 的范围，我无法基于给定上下文提供答案。
```

### EXT-K8S-PV-04 - unanswerable

- question: 根据 PV 文档，MySQL InnoDB undo log 应该如何调优？
- should_answer: `false`
- expected_source_section: Boundary Notes
- completed: `true`
- duration_ms: `12087`
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
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，<br>MySQL InnoDB undo log 调优 innodb_undo_log_truncate innodb_max_undo_log_size undo tablespace 参数<br>undo log 调优 配置步骤 回收阈值 性能影响 限制`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3800` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `根据 PV 文档，MySQL InnoDB undo log 应该如何调优？， \| MySQL InnoDB undo log 调优 innodb_undo_log_truncate innodb_max_undo_log_size undo tablespace 参数 \| undo log 调优 配置步骤 回收阈值 性能影响 限制`
  - profile_hints: version `—` / source `NONE` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6261`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
  - documents:
    - #1 score=`0.0492`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6261`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...

answer:

```text
根据提供的上下文信息，**无法回答您的问题**。

给出的文档片段（[1]）是关于 Kubernetes 持久卷（PV）和持久卷声明（PVC）的存储对象说明，其边界注释明确提到：

> This sample covers Kubernetes storage objects. It does not describe MySQL InnoDB MVCC, undo log tuning, or database transaction internals.

因此，该 PV 文档不包含 MySQL InnoDB undo log 调优的相关内容，我无法基于它提供该问题的解答。
```
