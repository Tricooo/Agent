# RAG Eval Result

- generated_at: `2026-05-26 12:46:19`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_profile_v2_demo`
- eval_schema_version: `v2`
- case_schema_version: `v2-compatible`
- rubric_coverage: `12/12`

## Summary

> Eval v2 把旧 `literal_hit` 降级为 `literal_smoke`，并把一次 RAG 结果拆成 candidate -> final context -> answer 三层漏斗。Step 8.1 仍复用旧 `expected_points` 子串口径计算 candidate/final recall；真正的语义 rubric 从 Step 8.2 开始。
>
> `retrieved` / `score` / `empty` 三列的 `—` 表示**没拿到成功的 ChatResponse metadata**，不等价于"无检索"。当前实现把 retrieval SSE 帧放在 `.call().chatResponse()` 返回之后才发，所以 LLM 调用失败时（即使 RAG 检索本身成功）三列都会是 `—`。要区分"检索失败"和"生成失败"，对照 `error` 列 / details 区 / backend log。
>
> Details 区的 `pre_rerank_documents` 展开 rerank 前候选池，`documents` 展开最终 top-K chunk attribution；document 行的 `score` 是当前阶段写回的候选分，可能来自 HYBRID RRF、query-variant fusion、per-variant rerank RRF 或 fusion-aware rerank；原始向量分与关键词分分别看 `vectorScore` / `keywordScore`，多 query 召回融合看 `queryFusionScore/queryFusionRank/queryVariantHitCount`；最终 rerank 写回分看 `rerankScore`，多路 rerank 融合分看 `rerankFusionScore/rerankVariantHitCount`，fusion-aware 弱加成看 `queryFusionBoostScore/fusionAwareScore`；rerank 服务状态看 `rerank_runtime` 的 model / endpoint / failure_reason；keyword 分支未参与时看 `keywordSkippedReason`。

| id | type | completed | duration_ms | health | failure_layer | candidate_recall | final_context_recall | c2f_delta | f2a_delta | semantic_score | answer_completeness | literal_smoke | answerability | source_coverage | answer_preview |
|---|---|---:|---:|---|---|---|---|---:|---:|---|---|---|---|---|---|
| EXT-ARTEMIS-01 | answerable | true | 13171 | ok | literal_only_mismatch | 3/3 | 3/3 | +0 | +0 | 3/3 | 3/3 | 0/3 | true_answer | answer_generation_or_literal_mismatch; file final=true; section final=true; points final=3/3 | 根据 NASA 的阿尔忒弥斯一号任务信息，发射日期是 **2022年11月16日**，溅落日期是 **2022年12月11日**，任务持续时间为 **25天10小时53分钟**[1]。 |
| EXT-ARTEMIS-02 | answerable_deep_section | true | 14541 | ok | none | 3/3 | 3/3 | +0 | +0 | 3/3 | 3/3 | 3/3 | true_answer | section_in_final_context; file final=true; section final=true; points final=3/3 | 根据NASA Artemis I任务时间线，Flight Day 26（最终返回与回收阶段）的关键事件按顺序如下[1][3]： 1. **Return trajectory correction burn-6**（返回轨迹修正燃烧-6） 2... |
| EXT-ARTEMIS-04 | weak_related | true | 19101 | ok | none | 1/1 | 1/1 | +0 | +0 | 1/1 | 1/1 | n/a | true_refusal | section_in_final_context; file final=true; section final=true; points final=manual | 根据提供的上下文，我无法回答 Artemis II 的四名宇航员分别是谁。这份资料仅涵盖 Artemis I 任务的时间线，并且明确指出“该样本不包含 Artemis II 的机组人员名单”（[2] 中的 “This sample does... |
| EXT-RFC9110-01 | answerable | true | 15275 | ok | literal_only_mismatch | 3/3 | 3/3 | +0 | +0 | 3/3 | 3/3 | 0/3 | true_answer | answer_generation_or_literal_mismatch; file final=true; section final=true; points final=3/3 | 根据 RFC 9110 的定义，状态码（status code）是一个**三位整数代码**，它描述了**请求的结果**以及**响应的语义**[1][2]。 |
| EXT-RFC9110-03 | answerable_formula | true | 10792 | ok | literal_only_mismatch | 3/3 | 3/3 | +0 | +0 | 3/3 | 3/3 | 0/3 | true_answer | answer_generation_or_literal_mismatch; file final=true; section final=true; points final=3/3 | 根据 RFC 9110，对 1xx 状态码和 HTTP/1.0 客户端的限制是： **HTTP/1.0 没有定义 1xx 状态码，服务器不得向 HTTP/1.0 客户端发送 1xx 响应**[1]。 |
| EXT-RFC9110-04 | weak_related | true | 17888 | refusal_issue | false_answer | 1/1 | 1/1 | +0 | -1 | 0/1 | 0/1 | n/a | false_answer | section_in_final_context; file final=true; section final=true; points final=manual | 根据现有上下文，我无法直接回答“Spring Boot 的 `@ControllerAdvice` 应该怎么写”这个问题。 [1] 明确指出，RFC 9110 并不是 Spring Boot 的指南，它不规定如何实现 `@Controlle... |
| EXT-K8S-PV-02 | answerable_deep_section | true | 12475 | ok | literal_only_mismatch | 3/3 | 3/3 | +0 | +0 | 3/3 | 3/3 | 0/3 | true_answer | answer_generation_or_literal_mismatch; file final=true; section final=true; points final=3/3 | 在 **Retain** 回收策略下，管理员需要执行以下手动回收步骤（引自[1]）： 1. **删除 PersistentVolume 对象**。 2. **清理关联存储资产上的数据**。 3. **删除关联的存储资产**，或者如果希望重用... |
| EXT-K8S-PV-03 | answerable_long_context | true | 16963 | ok | none | 5/5 | 5/5 | +0 | +0 | 5/5 | 5/5 | n/a | true_answer | section_in_final_context; file final=true; section final=true; points final=manual | 当 PVC 扩容失败时，文档给出的恢复流程是手动步骤，适用于底层存储无法满足请求大小的场景。具体步骤如下（根据 `[1]`）： 1. 将绑定到该 PVC 的 PersistentVolume（PV）的回收策略标记为 **Retain**。 ... |
| EXT-K8S-PV-04 | unanswerable | true | 11893 | ok | none | 1/1 | 1/1 | +0 | +0 | 1/1 | 1/1 | n/a | true_refusal | section_in_final_context; file final=true; section final=true; points final=manual | 根据提供的上下文，无法回答关于 MySQL InnoDB undo log 调优的问题。上下文明确指出，它所涵盖的内容是 Kubernetes 存储对象，不包含 MySQL InnoDB MVCC、undo log 调优或数据库事务内部机制... |
| EXT-OWASP-03 | answerable_deep_section | true | 24931 | ok | none | 3/3 | 3/3 | +0 | +0 | 3/3 | 3/3 | n/a | true_answer | section_in_final_context; file final=true; section final=true; points final=manual | Broken Access Control（失效的访问控制）、Injection（注入）和 Security Misconfiguration（安全配置错误）这三类风险的核心区别在于它们对应的失效层面不同： • **A01 Broken A... |
| EXT-PG-02 | answerable_deep_section | true | 22169 | ok | none | 4/4 | 4/4 | +0 | +0 | 4/4 | 4/4 | 4/4 | true_answer | section_in_final_context; file final=true; section final=true; points final=4/4 | 在 PostgreSQL 18 中，生成列分为 **stored**（存储式）和 **virtual**（虚拟式）两种，它们的主要区别如下： * **计算时机与存储** - **stored** 生成列在行被写入（INSERT 或 UPDA... |
| EXT-PG-04 | answerable_version_conflict | true | 18485 | fail | retrieval_miss | 4/5 | 4/5 | +0 | +1 | 5/5 | 5/5 | n/a | true_answer | section_in_final_context; file final=true; section final=true; points final=manual | 当知识库中同时存在 PostgreSQL 16 和 PostgreSQL 18 的文档时，回答 generated column 的 virtual / stored 差异必须显式区分版本，不能将 PostgreSQL 18 的默认行为套用... |


## Eval v2 By Case Type

| case_type | cases | runtime_ok | candidate_recall | final_context_recall | semantic_avg | answer_completeness_avg | literal_smoke | failure_layers |
|---|---:|---:|---|---|---:|---:|---|---|
| answerable | 2 | 2/2 | 6/6 | 6/6 | 100.0% | 100.0% | 0/6 | literal_only_mismatch:2 |
| answerable_deep_section | 4 | 4/4 | 10/10 | 10/10 | 100.0% | 100.0% | 7/10 | literal_only_mismatch:1, none:3 |
| answerable_formula | 1 | 1/1 | 3/3 | 3/3 | 100.0% | 100.0% | 0/3 | literal_only_mismatch:1 |
| answerable_long_context | 1 | 1/1 | n/a | n/a | 100.0% | 100.0% | n/a | none:1 |
| answerable_version_conflict | 1 | 1/1 | n/a | n/a | 100.0% | 100.0% | n/a | retrieval_miss:1 |
| unanswerable | 1 | 1/1 | n/a | n/a | 100.0% | 100.0% | n/a | none:1 |
| weak_related | 2 | 2/2 | n/a | n/a | 50.0% | 50.0% | n/a | false_answer:1, none:1 |

## Answerability Matrix

| bucket | count |
|---|---:|
| true_answer | 9 |
| true_refusal | 2 |
| false_refusal | 0 |
| false_answer | 1 |
| n/a | 0 |

## Details

### EXT-ARTEMIS-01 - answerable

- question: Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？
- should_answer: `true`
- expected_source_section: Mission Overview
- completed: `true`
- duration_ms: `13171`
- score_mode: `literal`
- eval_v2: verdict `pass` / failure_layer `literal_only_mismatch` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `3/3` / literal_smoke `0/3`
  - answer_completeness `3/3` / evidence_source_unverified `false`
  - semantic_hits `launch_date \| splashdown_date \| mission_duration` / semantic_misses `—`
  - evidence_funnel: candidate `3/3` -> final_context `3/3` -> answer `0/3`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `answer_generation_or_literal_mismatch` / source_file `corpus/nasa/artemis-i-mission-timeline.md` / section `Mission Overview`
  - pre_rerank: docs `4` / source_file `true` / section `true` / expected_points_exact `3/3`
  - final_context: docs `4` / source_file `true` / section `true` / expected_points_exact `3/3`
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
  - context_chars: actual `4340` / max `6000`
  - context_salience: cues `range` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，<br>Artemis I launch date splashdown date mission duration<br>Artemis I mission timeline launch splashdown duration days hours`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3075` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？， \| Artemis I launch date splashdown date mission duration \| Artemis I mission timeline launch splashdown duration days hours`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `Artemis I \| HTTP语义和线格式的区别是什么？ \| Artemis I mission \| Artemis I timeline \| What happened on Flight Day 21 of Artemis I? \| What happened on Flight Day 24 of Artemis I? \| What happened on Flight Day 25 of Artemis I? \| What was the launch date of Artemis I?`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7541`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":1,"text":"Artemis I launch date splashdown date mission duration"},{"index":3,"rank":1,"text":"Artemis I mission timeline launch splashdown duration days hours"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #2 score=`0.0484`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7407`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":2,"text":"Artemis I launch date splashdown date mission duration"},{"index":3,"rank":2,"text":"Artemis I mission timeline launch splashdown duration days hours"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #3 score=`0.0476`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6757`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":3,"text":"Artemis I launch date splashdown date mission duration"},{"index":3,"rank":3,"text":"Artemis I mission timeline launch splashdown duration days hours"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #4 score=`0.0469`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6502`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0469`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":4,"text":"Artemis I launch date splashdown date mission duration"},{"index":3,"rank":4,"text":"Artemis I mission timeline launch splashdown duration days hours"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...
  - documents:
    - #1 score=`0.0492`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7541`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":1,"text":"Artemis I launch date splashdown date mission duration"},{"index":3,"rank":1,"text":"Artemis I mission timeline launch splashdown duration days hours"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #2 score=`0.0484`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6757`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":3,"text":"Artemis I launch date splashdown date mission duration"},{"index":3,"rank":3,"text":"Artemis I mission timeline launch splashdown duration days hours"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #3 score=`0.0476`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7407`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":2,"text":"Artemis I launch date splashdown date mission duration"},{"index":3,"rank":2,"text":"Artemis I mission timeline launch splashdown duration days hours"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #4 score=`0.0469`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6502`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0469`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":4,"text":"Artemis I launch date splashdown date mission duration"},{"index":3,"rank":4,"text":"Artemis I mission timeline launch splashdown duration days hours"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...

answer:

```text
根据 NASA 的阿尔忒弥斯一号任务信息，发射日期是 **2022年11月16日**，溅落日期是 **2022年12月11日**，任务持续时间为 **25天10小时53分钟**[1]。
```

### EXT-ARTEMIS-02 - answerable_deep_section

- question: Artemis I Flight Day 26 的关键事件顺序是什么？
- should_answer: `true`
- expected_source_section: Flight Day 26
- completed: `true`
- duration_ms: `14541`
- score_mode: `literal`
- eval_v2: verdict `pass` / failure_layer `none` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `3/3` / literal_smoke `3/3`
  - answer_completeness `3/3` / evidence_source_unverified `false`
  - semantic_hits `flight_day_26_key_order` / semantic_misses `—`
  - evidence_funnel: candidate `3/3` -> final_context `3/3` -> answer `3/3`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `section_in_final_context` / source_file `corpus/nasa/artemis-i-mission-timeline.md` / section `Flight Day 26`
  - pre_rerank: docs `4` / source_file `true` / section `true` / expected_points_exact `3/3`
  - final_context: docs `4` / source_file `true` / section `true` / expected_points_exact `3/3`
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
  - score_range: `0.0469 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `4340` / max `6000`
  - context_salience: cues `range` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `Artemis I Flight Day 26 的关键事件顺序是什么？，<br>Artemis I Flight Day 26 timeline sequence milestones events<br>Artemis I Flight Day 26 propulsion maneuver command communication step configuration`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2976` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `Artemis I Flight Day 26 的关键事件顺序是什么？， \| Artemis I Flight Day 26 timeline sequence milestones events \| Artemis I Flight Day 26 propulsion maneuver command communication step configuration`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `Flight Day 26 \| Artemis I \| What happened on Flight Day 21 of Artemis I? \| What happened on Flight Day 24 of Artemis I? \| What happened on Flight Day 25 of Artemis I? \| Flight Day 1 \| Flight Day 20 \| Flight Day 21`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7962`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":1,"text":"Artemis I Flight Day 26 timeline sequence milestones events"},{"index":3,"rank":1,"text":"Artemis I Flight Day 26 propulsion maneuver command communication step configuration"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #2 score=`0.0481`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7517`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0481`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":2,"text":"Artemis I Flight Day 26 timeline sequence milestones events"},{"index":3,"rank":2,"text":"Artemis I Flight Day 26 propulsion maneuver command communication step configuration"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #3 score=`0.0476`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7551`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":3,"text":"Artemis I Flight Day 26 timeline sequence milestones events"},{"index":3,"rank":4,"text":"Artemis I Flight Day 26 propulsion maneuver command communication step configuration"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #4 score=`0.0471`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7427`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0471`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":4,"text":"Artemis I Flight Day 26 timeline sequence milestones events"},{"index":3,"rank":3,"text":"Artemis I Flight Day 26 propulsion maneuver command communication step configuration"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...
  - documents:
    - #1 score=`0.0492`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7962`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":1,"text":"Artemis I Flight Day 26 timeline sequence milestones events"},{"index":3,"rank":1,"text":"Artemis I Flight Day 26 propulsion maneuver command communication step configuration"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #2 score=`0.0484`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7517`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0481`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":2,"text":"Artemis I Flight Day 26 timeline sequence milestones events"},{"index":3,"rank":2,"text":"Artemis I Flight Day 26 propulsion maneuver command communication step configuration"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #3 score=`0.0476`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7551`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":3,"text":"Artemis I Flight Day 26 timeline sequence milestones events"},{"index":3,"rank":4,"text":"Artemis I Flight Day 26 propulsion maneuver command communication step configuration"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #4 score=`0.0469`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7427`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0471`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":4,"text":"Artemis I Flight Day 26 timeline sequence milestones events"},{"index":3,"rank":3,"text":"Artemis I Flight Day 26 propulsion maneuver command communication step configuration"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...

answer:

```text
根据NASA Artemis I任务时间线，Flight Day 26（最终返回与回收阶段）的关键事件按顺序如下[1][3]：

1. **Return trajectory correction burn-6**（返回轨迹修正燃烧-6）
2. **Crew module/service module separation**（乘员舱与服务舱分离）
3. **Crew module raise burn**（乘员舱抬升燃烧）
4. **Targeted entry interface**（目标再入接口）
5. **Crew module entry interface**（乘员舱再入接口）
6. **Splashdown**（溅落）

最终，猎户座飞船在太平洋上通过降落伞辅助完成溅落，由美国海军“波特兰”号在加利福尼亚州巴哈海岸附近回收。
```

### EXT-ARTEMIS-04 - weak_related

- question: 根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？
- should_answer: `false`
- expected_source_section: Boundary Notes
- completed: `true`
- duration_ms: `19101`
- score_mode: `manual`
- eval_v2: verdict `pass` / failure_layer `none` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `1/1` / literal_smoke `n/a`
  - answer_completeness `1/1` / evidence_source_unverified `false`
  - semantic_hits `artemis_ii_roster_refusal` / semantic_misses `—`
  - evidence_funnel: candidate `1/1` -> final_context `1/1` -> answer `n/a`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_refusal` / did_answer `false`
- source_coverage: layer `section_in_final_context` / source_file `corpus/nasa/artemis-i-mission-timeline.md` / section `Boundary Notes`
  - pre_rerank: docs `4` / source_file `true` / section `true` / expected_points_exact `manual`
  - final_context: docs `4` / source_file `true` / section `true` / expected_points_exact `manual`
- expected_points:
  - 当前 timeline 不足以回答
  - 需要其他 Artemis II 资料

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0469 .. 0.0489`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `4340` / max `6000`
  - context_salience: cues `range` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，<br>Artemis II crew members astronauts names<br>Artemis II mission crew announcement Reid Wiseman Victor Glover Christina Koch Jeremy Hansen`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `7007` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？， \| Artemis II crew members astronauts names \| Artemis II mission crew announcement Reid Wiseman Victor Glover Christina Koch Jeremy Hansen`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `Artemis I timeline \| Artemis I \| https://www.nasa.gov/reference/artemis-i-mission-timeline \| External RAG Sample: NASA Artemis I Mission Timeline \| Artemis I mission \| What happened on Flight Day 21 of Artemis I? \| What happened on Flight Day 24 of Artemis I? \| What happened on Flight Day 25 of Artemis I?`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6892`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，"},{"index":2,"rank":1,"text":"Artemis II crew members astronauts names"},{"index":3,"rank":1,"text":"Artemis II mission crew announcement Reid Wiseman Victor Glover Christina Koch Jeremy Hansen"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #2 score=`0.0323`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6704`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，`, queryFusionScore=`0.0323`, queryFusionRank=`2`, queryVariantHitCount=`2`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，"},{"index":2,"rank":2,"text":"Artemis II crew members astronauts names"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #3 score=`0.0317`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6087`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，`, queryFusionScore=`0.0317`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，"},{"index":2,"rank":3,"text":"Artemis II crew members astronauts names"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #4 score=`0.0156`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6253`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`Artemis II crew members astronauts names`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":4,"text":"Artemis II crew members astronauts names"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...
  - documents:
    - #1 score=`0.0489`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6704`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`1`, rerankScore=`0.0489`, rerankFusionScore=`0.0489`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，`, queryFusionScore=`0.0323`, queryFusionRank=`2`, queryVariantHitCount=`2`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，"},{"index":2,"rank":2,"text":"Artemis II crew members astronauts names"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #2 score=`0.0486`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6892`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`0.0486`, rerankFusionScore=`0.0486`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，"},{"index":2,"rank":1,"text":"Artemis II crew members astronauts names"},{"index":3,"rank":1,"text":"Artemis II mission crew announcement Reid Wiseman Victor Glover Christina Koch Jeremy Hansen"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #3 score=`0.0476`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6087`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，`, queryFusionScore=`0.0317`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，"},{"index":2,"rank":3,"text":"Artemis II crew members astronauts names"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #4 score=`0.0469`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6253`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`Artemis II crew members astronauts names`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":4,"text":"Artemis II crew members astronauts names"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...

answer:

```text
根据提供的上下文，我无法回答 Artemis II 的四名宇航员分别是谁。这份资料仅涵盖 Artemis I 任务的时间线，并且明确指出“该样本不包含 Artemis II 的机组人员名单”（[2] 中的 “This sample does not contain the crew roster for Artemis II.”）。如果您需要了解 Artemis II 的宇航员信息，建议查阅其他来源。
```

### EXT-RFC9110-01 - answerable

- question: RFC 9110 中 status code 的基本定义是什么？
- should_answer: `true`
- expected_source_section: Status Codes
- completed: `true`
- duration_ms: `15275`
- score_mode: `literal`
- eval_v2: verdict `pass` / failure_layer `literal_only_mismatch` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `3/3` / literal_smoke `0/3`
  - answer_completeness `3/3` / evidence_source_unverified `false`
  - semantic_hits `status_code_three_digit \| status_code_request_result \| status_code_response_semantics` / semantic_misses `—`
  - evidence_funnel: candidate `3/3` -> final_context `3/3` -> answer `0/3`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `answer_generation_or_literal_mismatch` / source_file `corpus/protocols/rfc-9110-http-semantics.md` / section `Status Codes`
  - pre_rerank: docs `4` / source_file `true` / section `true` / expected_points_exact `3/3`
  - final_context: docs `4` / source_file `true` / section `true` / expected_points_exact `3/3`
- expected_points:
  - [ ] three-digit integer code
  - [ ] result of the request
  - [ ] semantics of the response

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0471 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `5810` / max `6000`
  - context_salience: cues `range \| example` / expansions `1`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `RFC 9110 中 status code 的基本定义是什么？，<br>RFC 9110 HTTP status code definition<br>RFC 9110 response status code categories and syntax`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3414` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `RFC 9110 中 status code 的基本定义是什么？， \| RFC 9110 HTTP status code definition \| RFC 9110 response status code categories and syntax`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `RFC \| RFC 9110定义了哪些内容？ \| https://www.rfc-editor.org/rfc/rfc9110 \| status-code \| External RAG Sample: RFC 9110 HTTP Semantics \| Status Codes \| HTTP语义和线格式的区别是什么？ \| OAuth 2.1 authorization code flow message formats`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7738`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`RFC 9110 中 status code 的基本定义是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"RFC 9110 中 status code 的基本定义是什么？，"},{"index":2,"rank":1,"text":"RFC 9110 HTTP status code definition"},{"index":3,"rank":1,"text":"RFC 9110 response status code categories and syntax"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #2 score=`0.0484`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7567`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`RFC 9110 中 status code 的基本定义是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"RFC 9110 中 status code 的基本定义是什么？，"},{"index":2,"rank":2,"text":"RFC 9110 HTTP status code definition"},{"index":3,"rank":2,"text":"RFC 9110 response status code categories and syntax"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #3 score=`0.0317`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6682`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`RFC 9110 HTTP status code definition`, queryFusionScore=`0.0317`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":3,"text":"RFC 9110 HTTP status code definition"},{"index":3,"rank":3,"text":"RFC 9110 response status code categories and syntax"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
    - #4 score=`0.0156`, source=`owasp-top10-a01-a03-a05.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6197`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`RFC 9110 HTTP status code definition`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":4,"text":"RFC 9110 HTTP status code definition"}]`
      - preview: # External RAG Sample: OWASP Top 10 A01 A03 A05 Source URLs: - https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control/ - https://owasp.org/Top10/2021/A03_2021-Injection/ - htt...
  - documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7738`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`RFC 9110 中 status code 的基本定义是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"RFC 9110 中 status code 的基本定义是什么？，"},{"index":2,"rank":1,"text":"RFC 9110 HTTP status code definition"},{"index":3,"rank":1,"text":"RFC 9110 response status code categories and syntax"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #2 score=`0.0484`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7567`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`RFC 9110 中 status code 的基本定义是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"RFC 9110 中 status code 的基本定义是什么？，"},{"index":2,"rank":2,"text":"RFC 9110 HTTP status code definition"},{"index":3,"rank":2,"text":"RFC 9110 response status code categories and syntax"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #3 score=`0.0474`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6682`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0474`, rerankFusionScore=`0.0474`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`RFC 9110 HTTP status code definition`, queryFusionScore=`0.0317`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":3,"text":"RFC 9110 HTTP status code definition"},{"index":3,"rank":3,"text":"RFC 9110 response status code categories and syntax"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
    - #4 score=`0.0471`, source=`owasp-top10-a01-a03-a05.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6197`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0471`, rerankFusionScore=`0.0471`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`RFC 9110 HTTP status code definition`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":4,"text":"RFC 9110 HTTP status code definition"}]`
      - preview: # External RAG Sample: OWASP Top 10 A01 A03 A05 Source URLs: - https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control/ - https://owasp.org/Top10/2021/A03_2021-Injection/ - htt...

answer:

```text
根据 RFC 9110 的定义，状态码（status code）是一个**三位整数代码**，它描述了**请求的结果**以及**响应的语义**[1][2]。
```

### EXT-RFC9110-03 - answerable_formula

- question: RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？
- should_answer: `true`
- expected_source_section: Informational 1xx
- completed: `true`
- duration_ms: `10792`
- score_mode: `literal`
- eval_v2: verdict `pass` / failure_layer `literal_only_mismatch` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `3/3` / literal_smoke `0/3`
  - answer_completeness `3/3` / evidence_source_unverified `false`
  - semantic_hits `http10_no_1xx \| must_not_send_1xx \| http10_client_boundary` / semantic_misses `—`
  - evidence_funnel: candidate `3/3` -> final_context `3/3` -> answer `0/3`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `answer_generation_or_literal_mismatch` / source_file `corpus/protocols/rfc-9110-http-semantics.md` / section `Informational 1xx`
  - pre_rerank: docs `4` / source_file `true` / section `true` / expected_points_exact `3/3`
  - final_context: docs `4` / source_file `true` / section `true` / expected_points_exact `3/3`
- expected_points:
  - [ ] HTTP/1.0 did not define 1xx
  - [ ] MUST NOT send a 1xx response
  - [ ] HTTP/1.0 client

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0469 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `4918` / max `6000`
  - context_salience: cues `range \| example` / expansions `1`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，<br>RFC 9110 1xx informational responses HTTP/1.0 client exclusion constraints<br>HTTP/1.0 client must not send 1xx status code restrictions RFC 9110`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2397` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？， \| RFC 9110 1xx informational responses HTTP/1.0 client exclusion constraints \| HTTP/1.0 client must not send 1xx status code restrictions RFC 9110`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `HTTP/1.0 \| RFC \| HTTP/1.0是否支持1xx状态码？ \| 服务器能否向HTTP/1.0客户端发送1xx响应？ \| RFC 9110定义了哪些内容？ \| https://www.rfc-editor.org/rfc/rfc9110 \| External RAG Sample: RFC 9110 HTTP Semantics \| HTTP/1.0 client exclusion`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0328`, vectorRank=`1`, vectorScore=`0.7447`, keywordRank=`1`, keywordScore=`0.3000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":1,"text":"RFC 9110 1xx informational responses HTTP/1.0 client exclusion constraints"},{"index":3,"rank":1,"text":"HTTP/1.0 client must not send 1xx status code restrictions RFC 9110"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0484`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7075`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":2,"text":"RFC 9110 1xx informational responses HTTP/1.0 client exclusion constraints"},{"index":3,"rank":2,"text":"HTTP/1.0 client must not send 1xx status code restrictions RFC 9110"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #3 score=`0.0317`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6327`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`RFC 9110 1xx informational responses HTTP/1.0 client exclusion constraints`, queryFusionScore=`0.0317`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":3,"text":"RFC 9110 1xx informational responses HTTP/1.0 client exclusion constraints"},{"index":3,"rank":3,"text":"HTTP/1.0 client must not send 1xx status code restrictions RFC 9110"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
    - #4 score=`0.0156`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6106`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`RFC 9110 1xx informational responses HTTP/1.0 client exclusion constraints`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":4,"text":"RFC 9110 1xx informational responses HTTP/1.0 client exclusion constraints"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.
  - documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0328`, vectorRank=`1`, vectorScore=`0.7447`, keywordRank=`1`, keywordScore=`0.3000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":1,"text":"RFC 9110 1xx informational responses HTTP/1.0 client exclusion constraints"},{"index":3,"rank":1,"text":"HTTP/1.0 client must not send 1xx status code restrictions RFC 9110"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0484`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7075`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":2,"text":"RFC 9110 1xx informational responses HTTP/1.0 client exclusion constraints"},{"index":3,"rank":2,"text":"HTTP/1.0 client must not send 1xx status code restrictions RFC 9110"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #3 score=`0.0476`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6327`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`RFC 9110 1xx informational responses HTTP/1.0 client exclusion constraints`, queryFusionScore=`0.0317`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":3,"text":"RFC 9110 1xx informational responses HTTP/1.0 client exclusion constraints"},{"index":3,"rank":3,"text":"HTTP/1.0 client must not send 1xx status code restrictions RFC 9110"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
    - #4 score=`0.0469`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6106`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`RFC 9110 1xx informational responses HTTP/1.0 client exclusion constraints`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":4,"text":"RFC 9110 1xx informational responses HTTP/1.0 client exclusion constraints"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.

answer:

```text
根据 RFC 9110，对 1xx 状态码和 HTTP/1.0 客户端的限制是：

**HTTP/1.0 没有定义 1xx 状态码，服务器不得向 HTTP/1.0 客户端发送 1xx 响应**[1]。
```

### EXT-RFC9110-04 - weak_related

- question: 根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？
- should_answer: `false`
- expected_source_section: Framework Boundary
- completed: `true`
- duration_ms: `17888`
- score_mode: `manual`
- eval_v2: verdict `refusal_fail` / failure_layer `false_answer` / health `refusal_issue` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `0/1` / literal_smoke `n/a`
  - answer_completeness `0/1` / evidence_source_unverified `false`
  - semantic_hits `—` / semantic_misses `controller_advice_refusal`
  - evidence_funnel: candidate `1/1` -> final_context `1/1` -> answer `n/a`
  - deltas: candidate_to_final `+0` / final_to_answer `-1`
  - answerability: bucket `false_answer` / did_answer `true`
- source_coverage: layer `section_in_final_context` / source_file `corpus/protocols/rfc-9110-http-semantics.md` / section `Framework Boundary`
  - pre_rerank: docs `3` / source_file `true` / section `true` / expected_points_exact `manual`
  - final_context: docs `3` / source_file `true` / section `true` / expected_points_exact `manual`
- expected_points:
  - RFC 9110 不是 Spring Boot guide
  - 不包含 @ControllerAdvice 实现细节

retrieval:
- event #1
  - retrieved_document_count: `3`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0479 .. 0.0492`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `4717` / max `6000`
  - context_salience: cues `range \| example` / expansions `1`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `3` / final `3`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，<br>RFC 9110 HTTP Semantics @ControllerAdvice @ExceptionHandler ResponseEntity HTTP status codes<br>@ControllerAdvice 异常处理实现步骤 配置示例 注解用法 响应体格式 状态码枚举 错误处理模式`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2505` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？， \| RFC 9110 HTTP Semantics @ControllerAdvice @ExceptionHandler ResponseEntity HTTP status codes \| @ControllerAdvice 异常处理实现步骤 配置示例 注解用法 响应体格式 状态码枚举 错误处理模式`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `ControllerAdvice \| RFC \| RFC 9110定义了哪些内容？ \| https://www.rfc-editor.org/rfc/rfc9110 \| External RAG Sample: RFC 9110 HTTP Semantics`
  - pre_rerank_documents:
    - #1 score=`0.0325`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7402`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0325`, queryFusionRank=`1`, queryVariantHitCount=`2`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":2,"text":"RFC 9110 HTTP Semantics @ControllerAdvice @ExceptionHandler ResponseEntity HTTP status codes"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0325`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6720`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0325`, queryFusionRank=`2`, queryVariantHitCount=`2`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":1,"text":"RFC 9110 HTTP Semantics @ControllerAdvice @ExceptionHandler ResponseEntity HTTP status codes"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #3 score=`0.0317`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6076`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0317`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":3,"text":"RFC 9110 HTTP Semantics @ControllerAdvice @ExceptionHandler ResponseEntity HTTP status codes"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
  - documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7402`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0325`, queryFusionRank=`1`, queryVariantHitCount=`2`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":2,"text":"RFC 9110 HTTP Semantics @ControllerAdvice @ExceptionHandler ResponseEntity HTTP status codes"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0481`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6720`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0481`, rerankFusionScore=`0.0481`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0325`, queryFusionRank=`2`, queryVariantHitCount=`2`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":1,"text":"RFC 9110 HTTP Semantics @ControllerAdvice @ExceptionHandler ResponseEntity HTTP status codes"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #3 score=`0.0479`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6076`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0479`, rerankFusionScore=`0.0479`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0317`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":3,"text":"RFC 9110 HTTP Semantics @ControllerAdvice @ExceptionHandler ResponseEntity HTTP status codes"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...

answer:

```text
根据现有上下文，我无法直接回答“Spring Boot 的 `@ControllerAdvice` 应该怎么写”这个问题。

[1] 明确指出，RFC 9110 并不是 Spring Boot 的指南，它不规定如何实现 `@ControllerAdvice`，也不涉及 Java 异常映射或 JSON 错误体结构。RFC 9110 只定义了 HTTP 语义（如状态码、请求含义等），而 Spring 框架的实现细节超出了该文档的范围。[2] 也再次确认 RFC 9110 的范畴是 HTTP 语义，而非特定框架的代码生成。

因此，虽然 RFC 9110 可以为应用返回的 HTTP 状态码提供依据（例如 4xx 客户端错误、5xx 服务端错误），但具体的 Spring Boot `@ControllerAdvice` 写法（如 `@ExceptionHandler` 映射、响应体格式）需要参考 Spring 官方文档或其他 Spring 相关资源，而非 RFC 9110。
```

### EXT-K8S-PV-02 - answerable_deep_section

- question: Retain reclaim policy 下管理员需要做哪些手动回收步骤？
- should_answer: `true`
- expected_source_section: Retain Reclaim Policy
- completed: `true`
- duration_ms: `12475`
- score_mode: `literal`
- eval_v2: verdict `pass` / failure_layer `literal_only_mismatch` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `3/3` / literal_smoke `0/3`
  - answer_completeness `3/3` / evidence_source_unverified `false`
  - semantic_hits `retain_manual_reclaim_order` / semantic_misses `—`
  - evidence_funnel: candidate `3/3` -> final_context `3/3` -> answer `0/3`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `answer_generation_or_literal_mismatch` / source_file `corpus/kubernetes/kubernetes-persistent-volumes.md` / section `Retain Reclaim Policy`
  - pre_rerank: docs `4` / source_file `true` / section `true` / expected_points_exact `3/3`
  - final_context: docs `4` / source_file `true` / section `true` / expected_points_exact `3/3`
- expected_points:
  - [ ] Delete the PersistentVolume
  - [ ] Clean up data
  - [ ] Delete the associated storage asset

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0469 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `4763` / max `6000`
  - context_salience: cues `range` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `Retain reclaim policy 下管理员需要做哪些手动回收步骤？，<br>Retain reclaim policy manual steps administrator<br>Retain reclaim policy reclaim process configuration steps example`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3129` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `Retain reclaim policy 下管理员需要做哪些手动回收步骤？， \| Retain reclaim policy manual steps administrator \| Retain reclaim policy reclaim process configuration steps example`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `Retain Reclaim Policy \| Delete Reclaim Policy \| Authorization policy failure \| Reclaiming \| RFC 9110定义了哪些内容？`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`kubernetes-persistent-volumes.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7032`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Retain reclaim policy 下管理员需要做哪些手动回收步骤？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Retain reclaim policy 下管理员需要做哪些手动回收步骤？，"},{"index":2,"rank":1,"text":"Retain reclaim policy manual steps administrator"},{"index":3,"rank":1,"text":"Retain reclaim policy reclaim process configuration steps example"}]`
      - preview: ## Reclaiming When a user is done with a volume, the PVC can be deleted. The reclaim policy tells Kubernetes what to do with the volume after the claim is released. Current reclaim...
    - #2 score=`0.0484`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6443`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Retain reclaim policy 下管理员需要做哪些手动回收步骤？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Retain reclaim policy 下管理员需要做哪些手动回收步骤？，"},{"index":2,"rank":2,"text":"Retain reclaim policy manual steps administrator"},{"index":3,"rank":2,"text":"Retain reclaim policy reclaim process configuration steps example"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...
    - #3 score=`0.0159`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6070`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`3`, queryVariantRank=`3`, queryVariantText=`Retain reclaim policy reclaim process configuration steps example`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":3,"text":"Retain reclaim policy reclaim process configuration steps example"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
    - #4 score=`0.0156`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6030`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`Retain reclaim policy reclaim process configuration steps example`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"Retain reclaim policy reclaim process configuration steps example"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
  - documents:
    - #1 score=`0.0492`, source=`kubernetes-persistent-volumes.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7032`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Retain reclaim policy 下管理员需要做哪些手动回收步骤？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Retain reclaim policy 下管理员需要做哪些手动回收步骤？，"},{"index":2,"rank":1,"text":"Retain reclaim policy manual steps administrator"},{"index":3,"rank":1,"text":"Retain reclaim policy reclaim process configuration steps example"}]`
      - preview: ## Reclaiming When a user is done with a volume, the PVC can be deleted. The reclaim policy tells Kubernetes what to do with the volume after the claim is released. Current reclaim...
    - #2 score=`0.0484`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6070`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`3`, queryVariantRank=`3`, queryVariantText=`Retain reclaim policy reclaim process configuration steps example`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":3,"text":"Retain reclaim policy reclaim process configuration steps example"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
    - #3 score=`0.0476`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6443`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Retain reclaim policy 下管理员需要做哪些手动回收步骤？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Retain reclaim policy 下管理员需要做哪些手动回收步骤？，"},{"index":2,"rank":2,"text":"Retain reclaim policy manual steps administrator"},{"index":3,"rank":2,"text":"Retain reclaim policy reclaim process configuration steps example"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...
    - #4 score=`0.0469`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6030`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`Retain reclaim policy reclaim process configuration steps example`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"Retain reclaim policy reclaim process configuration steps example"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...

answer:

```text
在 **Retain** 回收策略下，管理员需要执行以下手动回收步骤（引自[1]）：

1. **删除 PersistentVolume 对象**。  
2. **清理关联存储资产上的数据**。  
3. **删除关联的存储资产**，或者如果希望重用该存储资产，则使用相同的存储资产定义**创建新的 PersistentVolume**。
```

### EXT-K8S-PV-03 - answerable_long_context

- question: PVC 扩容失败时，文档给出的恢复流程是什么？
- should_answer: `true`
- expected_source_section: Recovering From Failure When Expanding Volumes
- completed: `true`
- duration_ms: `16963`
- score_mode: `manual`
- eval_v2: verdict `pass` / failure_layer `none` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `5/5` / literal_smoke `n/a`
  - answer_completeness `5/5` / evidence_source_unverified `false`
  - semantic_hits `pvc_expansion_recovery_order` / semantic_misses `—`
  - evidence_funnel: candidate `5/5` -> final_context `5/5` -> answer `n/a`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `section_in_final_context` / source_file `corpus/kubernetes/kubernetes-persistent-volumes.md` / section `Recovering From Failure When Expanding Volumes`
  - pre_rerank: docs `3` / source_file `true` / section `true` / expected_points_exact `manual`
  - final_context: docs `3` / source_file `true` / section `true` / expected_points_exact `manual`
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
  - context_chars: actual `3682` / max `6000`
  - context_salience: cues `range` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `3` / final `3`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `PVC 扩容失败时，文档给出的恢复流程是什么？，<br>PVC扩容失败恢复流程<br>PVC Expansion failure recovery steps`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2291` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `PVC 扩容失败时，文档给出的恢复流程是什么？， \| PVC扩容失败恢复流程 \| PVC Expansion failure recovery steps`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `PVC \| PVCs \| PVC Expansion \| HTTP语义和线格式的区别是什么？`
  - pre_rerank_documents:
    - #1 score=`0.0489`, source=`kubernetes-persistent-volumes.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7002`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":1,"text":"PVC扩容失败恢复流程"},{"index":3,"rank":1,"text":"PVC Expansion failure recovery steps"}]`
      - preview: ## Reclaiming When a user is done with a volume, the PVC can be deleted. The reclaim policy tells Kubernetes what to do with the volume after the claim is released. Current reclaim...
    - #2 score=`0.0481`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6907`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0481`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":2,"text":"PVC扩容失败恢复流程"},{"index":3,"rank":2,"text":"PVC Expansion failure recovery steps"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...
    - #3 score=`0.0481`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7061`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0481`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":3,"text":"PVC扩容失败恢复流程"},{"index":3,"rank":3,"text":"PVC Expansion failure recovery steps"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
  - documents:
    - #1 score=`0.0492`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7061`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0481`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":3,"text":"PVC扩容失败恢复流程"},{"index":3,"rank":3,"text":"PVC Expansion failure recovery steps"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
    - #2 score=`0.0481`, source=`kubernetes-persistent-volumes.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7002`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`0.0481`, rerankFusionScore=`0.0481`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":1,"text":"PVC扩容失败恢复流程"},{"index":3,"rank":1,"text":"PVC Expansion failure recovery steps"}]`
      - preview: ## Reclaiming When a user is done with a volume, the PVC can be deleted. The reclaim policy tells Kubernetes what to do with the volume after the claim is released. Current reclaim...
    - #3 score=`0.0479`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6907`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`3`, rerankScore=`0.0479`, rerankFusionScore=`0.0479`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0481`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":2,"text":"PVC扩容失败恢复流程"},{"index":3,"rank":2,"text":"PVC Expansion failure recovery steps"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...

answer:

```text
当 PVC 扩容失败时，文档给出的恢复流程是手动步骤，适用于底层存储无法满足请求大小的场景。具体步骤如下（根据 `[1]`）：

1. 将绑定到该 PVC 的 PersistentVolume（PV）的回收策略标记为 **Retain**。
2. 删除 PVC。由于回收策略为 Retain，删除 PVC 不会丢失数据。
3. 从 PV 的 spec 中删除 `claimRef` 条目，使 PV 变为 **Available**（可用）状态。
4. 重新创建一个 PVC，并指定一个**小于** PV 现有大小的容量，同时将 `volumeName` 字段设置为该现有 PV 的名称，以直接绑定到原 PV。
5. 恢复 PV 原来的回收策略。

> 提醒：Kubernetes 不支持将 PVC 缩小到比当前大小更小的值。上述流程本质上是通过重建 PVC 并绑定到原 PV 来绕过大小限制，而非直接收缩卷。
```

### EXT-K8S-PV-04 - unanswerable

- question: 根据 PV 文档，MySQL InnoDB undo log 应该如何调优？
- should_answer: `false`
- expected_source_section: Boundary Notes
- completed: `true`
- duration_ms: `11893`
- score_mode: `manual`
- eval_v2: verdict `pass` / failure_layer `none` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `1/1` / literal_smoke `n/a`
  - answer_completeness `1/1` / evidence_source_unverified `false`
  - semantic_hits `innodb_undo_log_refusal` / semantic_misses `—`
  - evidence_funnel: candidate `1/1` -> final_context `1/1` -> answer `n/a`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_refusal` / did_answer `false`
- source_coverage: layer `section_in_final_context` / source_file `corpus/kubernetes/kubernetes-persistent-volumes.md` / section `Boundary Notes`
  - pre_rerank: docs `1` / source_file `true` / section `true` / expected_points_exact `manual`
  - final_context: docs `1` / source_file `true` / section `true` / expected_points_exact `manual`
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
  - context_chars: actual `997` / max `6000`
  - context_salience: cues `range` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `1` / final `1`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，<br>MySQL InnoDB undo log 调优参数 innodb_undo_log_truncate innodb_undo_tablespaces undo segment undo tablespace MVCC 回滚段<br>undo log 调优步骤 配置示例 性能影响 清理策略 大小限制 截断阈值 异常边界`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3686` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `根据 PV 文档，MySQL InnoDB undo log 应该如何调优？， \| MySQL InnoDB undo log 调优参数 innodb_undo_log_truncate innodb_undo_tablespaces undo segment undo tablespace MVCC 回滚段 \| undo log 调优步骤 配置示例 性能影响 清理策略 大小限制 截断阈值 异常边界`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `InnoDB \| MySQL \| SQL \| MySQL invisible columns`
  - pre_rerank_documents:
    - #1 score=`0.0328`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6261`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，`, queryFusionScore=`0.0328`, queryFusionRank=`1`, queryVariantHitCount=`2`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，"},{"index":2,"rank":1,"text":"MySQL InnoDB undo log 调优参数 innodb_undo_log_truncate innodb_undo_tablespaces undo segment undo tablespace MVCC 回滚段"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
  - documents:
    - #1 score=`0.0492`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6261`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，`, queryFusionScore=`0.0328`, queryFusionRank=`1`, queryVariantHitCount=`2`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，"},{"index":2,"rank":1,"text":"MySQL InnoDB undo log 调优参数 innodb_undo_log_truncate innodb_undo_tablespaces undo segment undo tablespace MVCC 回滚段"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...

answer:

```text
根据提供的上下文，无法回答关于 MySQL InnoDB undo log 调优的问题。上下文明确指出，它所涵盖的内容是 Kubernetes 存储对象，不包含 MySQL InnoDB MVCC、undo log 调优或数据库事务内部机制的描述（[1]）。
```

### EXT-OWASP-03 - answerable_deep_section

- question: Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？
- should_answer: `true`
- expected_source_section: Category Differences
- completed: `true`
- duration_ms: `24931`
- score_mode: `manual`
- eval_v2: verdict `pass` / failure_layer `none` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `3/3` / literal_smoke `n/a`
  - answer_completeness `3/3` / evidence_source_unverified `false`
  - semantic_hits `broken_access_control_boundary \| injection_boundary \| security_misconfiguration_boundary` / semantic_misses `—`
  - evidence_funnel: candidate `3/3` -> final_context `3/3` -> answer `n/a`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `section_in_final_context` / source_file `corpus/security/owasp-top10-a01-a03-a05.md` / section `Category Differences`
  - pre_rerank: docs `4` / source_file `true` / section `true` / expected_points_exact `manual`
  - final_context: docs `4` / source_file `true` / section `true` / expected_points_exact `manual`
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
  - context_chars: actual `4552` / max `6000`
  - context_salience: cues `example \| range` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，<br>Broken Access Control A01 Injection A03 Security Misconfiguration A05 OWASP Top 10 vulnerabilities difference comparison<br>Broken Access Control Injection Security Misconfiguration definition examples causes impact mitigation`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3578` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？， \| Broken Access Control A01 Injection A03 Security Misconfiguration A05 OWASP Top 10 vulnerabilities difference comparison \| Broken Access Control Injection Security Misconfiguration definition examples causes impact mitigation`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `Broken Access Control \| Security Misconfiguration \| Injection \| What is Broken Access Control? \| https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control \| A01 Broken Access Control \| What is Security Misconfiguration? \| access-control`
  - pre_rerank_documents:
    - #1 score=`0.0489`, source=`owasp-top10-a01-a03-a05.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7612`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":1,"text":"Broken Access Control A01 Injection A03 Security Misconfiguration A05 OWASP Top 10 vulnerabilities difference comparison"},{"index":3,"rank":1,"text":"Broken Access Control Injection Security Misconfiguration definition examples causes impact mitigation"}]`
      - preview: # External RAG Sample: OWASP Top 10 A01 A03 A05 Source URLs: - https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control/ - https://owasp.org/Top10/2021/A03_2021-Injection/ - htt...
    - #2 score=`0.0484`, source=`owasp-top10-a01-a03-a05.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7852`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":2,"text":"Broken Access Control A01 Injection A03 Security Misconfiguration A05 OWASP Top 10 vulnerabilities difference comparison"},{"index":3,"rank":3,"text":"Broken Access Control Injection Security Misconfiguration definition examples causes impact mitigation"}]`
      - preview: A good answer should focus on permission boundaries, object ownership, and server-side enforcement. ## A03 Injection Injection occurs when untrusted input reaches an interpreter or...
    - #3 score=`0.0479`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7313`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0479`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":3,"text":"Broken Access Control A01 Injection A03 Security Misconfiguration A05 OWASP Top 10 vulnerabilities difference comparison"},{"index":3,"rank":2,"text":"Broken Access Control Injection Security Misconfiguration definition examples causes impact mitigation"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
    - #4 score=`0.0313`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6443`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`Broken Access Control A01 Injection A03 Security Misconfiguration A05 OWASP Top 10 vulnerabilities difference comparison`, queryFusionScore=`0.0313`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":4,"text":"Broken Access Control A01 Injection A03 Security Misconfiguration A05 OWASP Top 10 vulnerabilities difference comparison"},{"index":3,"rank":4,"text":"Broken Access Control Injection Security Misconfiguration definition examples causes impact mitigation"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
  - documents:
    - #1 score=`0.0489`, source=`owasp-top10-a01-a03-a05.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7852`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`1`, rerankScore=`0.0489`, rerankFusionScore=`0.0489`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":2,"text":"Broken Access Control A01 Injection A03 Security Misconfiguration A05 OWASP Top 10 vulnerabilities difference comparison"},{"index":3,"rank":3,"text":"Broken Access Control Injection Security Misconfiguration definition examples causes impact mitigation"}]`
      - preview: A good answer should focus on permission boundaries, object ownership, and server-side enforcement. ## A03 Injection Injection occurs when untrusted input reaches an interpreter or...
    - #2 score=`0.0486`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7313`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`0.0486`, rerankFusionScore=`0.0486`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0479`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":3,"text":"Broken Access Control A01 Injection A03 Security Misconfiguration A05 OWASP Top 10 vulnerabilities difference comparison"},{"index":3,"rank":2,"text":"Broken Access Control Injection Security Misconfiguration definition examples causes impact mitigation"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
    - #3 score=`0.0476`, source=`owasp-top10-a01-a03-a05.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7612`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":1,"text":"Broken Access Control A01 Injection A03 Security Misconfiguration A05 OWASP Top 10 vulnerabilities difference comparison"},{"index":3,"rank":1,"text":"Broken Access Control Injection Security Misconfiguration definition examples causes impact mitigation"}]`
      - preview: # External RAG Sample: OWASP Top 10 A01 A03 A05 Source URLs: - https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control/ - https://owasp.org/Top10/2021/A03_2021-Injection/ - htt...
    - #4 score=`0.0469`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6443`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`Broken Access Control A01 Injection A03 Security Misconfiguration A05 OWASP Top 10 vulnerabilities difference comparison`, queryFusionScore=`0.0313`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":4,"text":"Broken Access Control A01 Injection A03 Security Misconfiguration A05 OWASP Top 10 vulnerabilities difference comparison"},{"index":3,"rank":4,"text":"Broken Access Control Injection Security Misconfiguration definition examples causes impact mitigation"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...

answer:

```text
Broken Access Control（失效的访问控制）、Injection（注入）和 Security Misconfiguration（安全配置错误）这三类风险的核心区别在于它们对应的失效层面不同：

• **A01 Broken Access Control** 本质上是**授权策略失效** [2][3]。它意味着应用未能正确执行“用户只能在其权限范围内操作”的策略，例如通过篡改 URL、参数绕过检查，或未对 API 操作（POST/PUT/DELETE）实施访问控制等 [3]。简单说，是“有权限的人做了没权力做的事”。

• **A03 Injection** 本质上是**不可信数据未经适当处理就进入了解释器或命令上下文** [1][2]。当用户提供的数据没有被验证、过滤或净化，直接用于动态查询或非参数化调用（如 SQL 注入、跨站脚本、命令注入）时，就会发生注入 [1]。这是“数据被当成代码执行了”。

• **A05 Security Misconfiguration** 本质上是**系统、框架、服务器或部署配置本身不安全** [2]。这包括缺少安全加固、启用了不必要的端口/服务/账户（如默认账号密码未改）、错误信息泄露堆栈跟踪、安全头缺失、组件过时等 [1]（来源中虽未列出该条，但可从 A05 的特征归纳）。这是“环境或设置本身留下了漏洞”。

同一个事件可能涉及多个类别，但准确判断应识别出**主要的失效模式** [2]。所以，简单区分就是：  
- **Access Control** 看权限策略栏没拦住人；  
- **Injection** 看外界数据是不是混进了执行命令；  
- **Misconfiguration** 看系统部署是不是没关严门。
```

### EXT-PG-02 - answerable_deep_section

- question: PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？
- should_answer: `true`
- expected_source_section: PostgreSQL 18 Generated Columns
- completed: `true`
- duration_ms: `22169`
- score_mode: `literal`
- eval_v2: verdict `pass` / failure_layer `none` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `4/4` / literal_smoke `4/4`
  - answer_completeness `4/4` / evidence_source_unverified `false`
  - semantic_hits `stored_generated_column_write_time \| stored_generated_column_occupies_storage \| virtual_generated_column_read_time \| virtual_generated_column_no_storage` / semantic_misses `—`
  - evidence_funnel: candidate `4/4` -> final_context `4/4` -> answer `4/4`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `section_in_final_context` / source_file `corpus/database/postgresql-generated-columns-version-contrast.md` / section `PostgreSQL 18 Generated Columns`
  - pre_rerank: docs `2` / source_file `true` / section `true` / expected_points_exact `4/4`
  - final_context: docs `2` / source_file `true` / section `true` / expected_points_exact `4/4`
- expected_points:
  - [x] computed when the row is written
  - [x] occupies storage
  - [x] computed when it is read
  - [x] occupies no storage

retrieval:
- event #1
  - retrieved_document_count: `2`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0484 .. 0.0492`
  - context_selected: `2` / dropped: `0` / truncated: `false`
  - context_chars: actual `2745` / max `6000`
  - context_salience: cues `range` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `2` / final `2`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？，<br>PostgreSQL 18 generated column STORED VIRTUAL 区别 语法 定义<br>PostgreSQL 18 stored generated column virtual generated column 实现 差异 示例 存储方式 性能`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2263` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？， \| PostgreSQL 18 generated column STORED VIRTUAL 区别 语法 定义 \| PostgreSQL 18 stored generated column virtual generated column 实现 差异 示例 存储方式 性能`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `stored generated column \| virtual generated column \| generated column \| PostgreSQL \| STORED \| VIRTUAL \| POST \| SQL`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.8043`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？，"},{"index":2,"rank":1,"text":"PostgreSQL 18 generated column STORED VIRTUAL 区别 语法 定义"},{"index":3,"rank":1,"text":"PostgreSQL 18 stored generated column virtual generated column 实现 差异 示例 存储方式 性能"}]`
      - preview: # External RAG Sample: PostgreSQL Generated Columns Version Contrast Source URLs: - https://www.postgresql.org/docs/18/ddl-generated-columns.html - https://www.postgresql.org/docs/...
    - #2 score=`0.0484`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7590`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？，"},{"index":2,"rank":2,"text":"PostgreSQL 18 generated column STORED VIRTUAL 区别 语法 定义"},{"index":3,"rank":2,"text":"PostgreSQL 18 stored generated column virtual generated column 实现 差异 示例 存储方式 性能"}]`
      - preview: - The generation expression cannot reference another generated column. - A generated column cannot have a column default or identity definition. - A generated column cannot be part...
  - documents:
    - #1 score=`0.0492`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.8043`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？，"},{"index":2,"rank":1,"text":"PostgreSQL 18 generated column STORED VIRTUAL 区别 语法 定义"},{"index":3,"rank":1,"text":"PostgreSQL 18 stored generated column virtual generated column 实现 差异 示例 存储方式 性能"}]`
      - preview: # External RAG Sample: PostgreSQL Generated Columns Version Contrast Source URLs: - https://www.postgresql.org/docs/18/ddl-generated-columns.html - https://www.postgresql.org/docs/...
    - #2 score=`0.0484`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7590`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？，"},{"index":2,"rank":2,"text":"PostgreSQL 18 generated column STORED VIRTUAL 区别 语法 定义"},{"index":3,"rank":2,"text":"PostgreSQL 18 stored generated column virtual generated column 实现 差异 示例 存储方式 性能"}]`
      - preview: - The generation expression cannot reference another generated column. - A generated column cannot have a column default or identity definition. - A generated column cannot be part...

answer:

```text
在 PostgreSQL 18 中，生成列分为 **stored**（存储式）和 **virtual**（虚拟式）两种，它们的主要区别如下：

*   **计算时机与存储**  
    - **stored** 生成列在行被写入（INSERT 或 UPDATE）时计算，之后像普通列一样占用存储空间。  
    - **virtual** 生成列不占用存储空间，只在被读取时实时计算。  
    引用原文：*“A stored generated column is computed when the row is written, during insert or update, and it occupies storage like a normal column. A virtual generated column occupies no storage and is computed when it is read.”*【1】

*   **默认行为**  
    在 PostgreSQL 18 中，如果不显式指定 `VIRTUAL` 或 `STORED`，生成列**默认为 virtual**。  
    引用原文：*“In PostgreSQL 18, a generated column is virtual by default unless VIRTUAL or STORED is specified explicitly.”*【1】

*   **额外的限制差异**  
    虚拟生成列对用户自定义类型和函数有额外的限制，而存储生成列则没有这类虚拟列特有的限制。  
    引用原文：*“For virtual generated columns, additional restrictions apply to user-defined types and functions. Stored generated columns do not have the same virtual-column restriction.”*【2】

此外，两者共同遵守生成列的一般约束，例如：生成表达式只能使用不可变函数、不能包含子查询、不能引用其他生成列、不能有列默认值或标识定义、不能作为分区键等【2】。

总结来说，最关键的区别在于：**stored 列以空间换时间（写入时计算、占用存储），而 virtual 列以计算开销换空间（读取时计算、不占用存储）；且未指定种类时默认创建 virtual 列，同时 virtual 列对用户自定义类型/函数有更严格的限制。**
```

### EXT-PG-04 - answerable_version_conflict

- question: 如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？
- should_answer: `true`
- expected_source_section: Version Conflict Handling
- completed: `true`
- duration_ms: `18485`
- score_mode: `manual`
- eval_v2: verdict `fail` / failure_layer `retrieval_miss` / health `fail` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `5/5` / literal_smoke `n/a`
  - answer_completeness `5/5` / evidence_source_unverified `false`
  - semantic_hits `distinguish_versions \| pg18_stored_and_virtual \| pg18_default_virtual \| pg16_stored \| do_not_apply_pg18_to_pg16` / semantic_misses `—`
  - evidence_funnel: candidate `4/5` -> final_context `4/5` -> answer `n/a`
  - deltas: candidate_to_final `+0` / final_to_answer `+1`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `section_in_final_context` / source_file `corpus/database/postgresql-generated-columns-version-contrast.md` / section `Version Conflict Handling`
  - pre_rerank: docs `4` / source_file `true` / section `true` / expected_points_exact `manual`
  - final_context: docs `4` / source_file `true` / section `true` / expected_points_exact `manual`
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
  - score_range: `0.0471 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `3943` / max `6000`
  - context_salience: cues `range` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，<br>PostgreSQL generated columns VIRTUAL STORED difference between versions 16 and 18<br>PostgreSQL 16 18 generated column VIRTUAL STORED changes behavior limitations examples`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2737` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？， \| PostgreSQL generated columns VIRTUAL STORED difference between versions 16 and 18 \| PostgreSQL 16 18 generated column VIRTUAL STORED changes behavior limitations examples`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `generated column \| PostgreSQL \| STORED \| VIRTUAL \| POST \| SQL \| https://www.postgresql.org/docs/18/ddl-generated-columns.html \| External RAG Sample: PostgreSQL Generated Columns Version Contrast`
  - pre_rerank_documents:
    - #1 score=`0.0489`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7895`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":1,"text":"PostgreSQL generated columns VIRTUAL STORED difference between versions 16 and 18"},{"index":3,"rank":1,"text":"PostgreSQL 16 18 generated column VIRTUAL STORED changes behavior limitations examples"}]`
      - preview: # External RAG Sample: PostgreSQL Generated Columns Version Contrast Source URLs: - https://www.postgresql.org/docs/18/ddl-generated-columns.html - https://www.postgresql.org/docs/...
    - #2 score=`0.0486`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7949`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0486`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":2,"text":"PostgreSQL generated columns VIRTUAL STORED difference between versions 16 and 18"},{"index":3,"rank":2,"text":"PostgreSQL 16 18 generated column VIRTUAL STORED changes behavior limitations examples"}]`
      - preview: - The generation expression cannot reference another generated column. - A generated column cannot have a column default or identity definition. - A generated column cannot be part...
    - #3 score=`0.0476`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6291`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":3,"text":"PostgreSQL generated columns VIRTUAL STORED difference between versions 16 and 18"},{"index":3,"rank":3,"text":"PostgreSQL 16 18 generated column VIRTUAL STORED changes behavior limitations examples"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.
    - #4 score=`0.0156`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6200`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`PostgreSQL 16 18 generated column VIRTUAL STORED changes behavior limitations examples`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"PostgreSQL 16 18 generated column VIRTUAL STORED changes behavior limitations examples"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
  - documents:
    - #1 score=`0.0492`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7949`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`2`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0486`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":2,"text":"PostgreSQL generated columns VIRTUAL STORED difference between versions 16 and 18"},{"index":3,"rank":2,"text":"PostgreSQL 16 18 generated column VIRTUAL STORED changes behavior limitations examples"}]`
      - preview: - The generation expression cannot reference another generated column. - A generated column cannot have a column default or identity definition. - A generated column cannot be part...
    - #2 score=`0.0484`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7895`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":1,"text":"PostgreSQL generated columns VIRTUAL STORED difference between versions 16 and 18"},{"index":3,"rank":1,"text":"PostgreSQL 16 18 generated column VIRTUAL STORED changes behavior limitations examples"}]`
      - preview: # External RAG Sample: PostgreSQL Generated Columns Version Contrast Source URLs: - https://www.postgresql.org/docs/18/ddl-generated-columns.html - https://www.postgresql.org/docs/...
    - #3 score=`0.0474`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6291`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0474`, rerankFusionScore=`0.0474`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":3,"text":"PostgreSQL generated columns VIRTUAL STORED difference between versions 16 and 18"},{"index":3,"rank":3,"text":"PostgreSQL 16 18 generated column VIRTUAL STORED changes behavior limitations examples"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.
    - #4 score=`0.0471`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6200`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0471`, rerankFusionScore=`0.0471`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":3}]`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`PostgreSQL 16 18 generated column VIRTUAL STORED changes behavior limitations examples`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"PostgreSQL 16 18 generated column VIRTUAL STORED changes behavior limitations examples"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...

answer:

```text
当知识库中同时存在 PostgreSQL 16 和 PostgreSQL 18 的文档时，回答 generated column 的 virtual / stored 差异必须显式区分版本，不能将 PostgreSQL 18 的默认行为套用到 PostgreSQL 16 上。

根据现有文档（[1][2]）：

- **PostgreSQL 16**  
  在 `CREATE TABLE` 语法中，生成的列由 `GENERATED ALWAYS AS (...) STORED` 表示。文档仅描述 **stored** 类型的生成列，该列在插入或更新时计算并占用存储空间。**PostgreSQL 16 中没有 virtual 类型的生成列，也不存在“默认 virtual”的概念。**

- **PostgreSQL 18**  
  引入两种生成列：**stored**（写入时计算，占用存储）和 **virtual**（读取时计算，不占存储）。  
  *关键区别*：在 PostgreSQL 18 中，如果未指定 `VIRTUAL` 或 `STORED`，生成的列 **默认是 virtual**。  
  此外，virtual generated column 在用户自定义类型和函数方面有额外限制，而 stored 没有这些附加限制（[1]）。

**正确的回答方式**（摘自上下文 [1] 的指导）：

> - PostgreSQL 18 同时支持 stored 和 virtual 生成列。
> - PostgreSQL 18 在未指定类型时默认为 virtual。
> - PostgreSQL 16 的 `CREATE TABLE` 文档仅展示 `STORED` 形式的生成列。
> - 在没有版本限定的情况下，不应将 PostgreSQL 18 “默认 virtual” 的结论应用于 PostgreSQL 16。

**需要避开的陷阱**：  
如果用户的问题没有明确提及 PostgreSQL 版本，应当反问或分别说明两个版本的行为；如果用户的问题只涉及 PostgreSQL 16，绝不能因为知识库中有 PostgreSQL 18 的文档就声称 “默认是 virtual”。
```
