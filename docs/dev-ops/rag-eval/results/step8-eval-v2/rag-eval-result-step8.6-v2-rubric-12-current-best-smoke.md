# RAG Eval Result

- generated_at: `2026-05-26 12:03:08`
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
| EXT-ARTEMIS-01 | answerable | true | 10989 | ok | literal_only_mismatch | 3/3 | 3/3 | +0 | +0 | 3/3 | 3/3 | 0/3 | true_answer | answer_generation_or_literal_mismatch; file final=true; section final=true; points final=3/3 | 根据提供的任务资料，Artemis I 的发射日期、溅落日期和任务持续时间如下[1]： - **发射日期**：2022年11月16日 - **溅落日期**：2022年12月11日 - **任务持续时间**：25天10小时53分钟 |
| EXT-ARTEMIS-02 | answerable_deep_section | true | 29055 | ok | none | 3/3 | 3/3 | +0 | +0 | 3/3 | 3/3 | 3/3 | true_answer | section_in_final_context; file final=true; section final=true; points final=3/3 | 根据提供的任务时间线信息，Artemis I 任务飞行第 26 天（Flight Day 26）包含了最终的再入与回收序列，其关键事件按顺序如下： 1. **返回轨道修正燃烧-6**（Return trajectory correction... |
| EXT-ARTEMIS-04 | weak_related | true | 10276 | ok | none | 1/1 | 1/1 | +0 | +0 | 1/1 | 1/1 | n/a | true_refusal | section_in_final_context; file final=true; section final=true; points final=manual | 根据提供的上下文，**我无法回答这个问题**。 这份材料是 **Artemis I 任务时间线**的文档，其中明确说明： - “This sample can answer Artemis I mission-fact and timeli... |
| EXT-RFC9110-01 | answerable | true | 13311 | ok | none | 3/3 | 3/3 | +0 | +0 | 3/3 | 3/3 | 3/3 | true_answer | section_in_final_context; file final=true; section final=true; points final=3/3 | 根据RFC 9110，状态码（status code）的基本定义是： - **一个三位整数代码**（a three-digit integer code）[1]。 - **用来描述请求的结果以及响应的语义**（describes the r... |
| EXT-RFC9110-03 | answerable_formula | true | 12882 | ok | none | 3/3 | 3/3 | +0 | +0 | 3/3 | 3/3 | 3/3 | true_answer | section_in_final_context; file final=true; section final=true; points final=3/3 | 根据 RFC 9110 的规定，**HTTP/1.0 并未定义 1xx 信息性状态码**，因此服务器**绝不能**向 HTTP/1.0 客户端发送 1xx 响应。 这一限制在上下文中有明确表述：“HTTP/1.0 did not defin... |
| EXT-RFC9110-04 | weak_related | true | 13915 | ok | none | 1/1 | 1/1 | +0 | +0 | 1/1 | 1/1 | n/a | true_refusal | section_in_final_context; file final=true; section final=true; points final=manual | 根据提供的上下文，我无法回答如何编写 Spring Boot 的 `@ControllerAdvice`。 [1] 中明确指出：“RFC 9110 is not a Spring Boot guide. It does not specif... |
| EXT-K8S-PV-02 | answerable_deep_section | true | 12884 | ok | literal_only_mismatch | 3/3 | 3/3 | +0 | +0 | 3/3 | 3/3 | 0/3 | true_answer | answer_generation_or_literal_mismatch; file final=true; section final=true; points final=3/3 | 在 `Retain` 回收策略下，管理员需要手动执行以下步骤来完成存储资源的回收[1]： 1. **删除 PersistentVolume 对象**。 2. **清理关联存储资产（如云盘、NFS 目录等）上的残留数据**。 3. **删除该... |
| EXT-K8S-PV-03 | answerable_long_context | true | 14315 | ok | none | 5/5 | 5/5 | +0 | +0 | 5/5 | 5/5 | n/a | true_answer | section_in_final_context; file final=true; section final=true; points final=manual | 根据文档，当 PVC 扩容失败时（例如底层存储无法满足请求的大小），集群管理员可以按以下流程手动恢复： 1. 将与该 PVC 绑定的 PersistentVolume（PV）的回收策略标记为 **Retain**。 2. 删除该 PVC。由... |
| EXT-K8S-PV-04 | unanswerable | true | 9263 | ok | none | 1/1 | 1/1 | +0 | +0 | 1/1 | 1/1 | n/a | true_refusal | section_in_final_context; file final=true; section final=true; points final=manual | 根据您提供的 PV 文档内容，它明确说明：“本示例涵盖 Kubernetes 存储对象，不描述 MySQL InnoDB MVCC、undo log 调优或数据库事务内部原理。”因此，我无法基于该文档回答关于 MySQL InnoDB un... |
| EXT-OWASP-03 | answerable_deep_section | true | 22293 | ok | none | 3/3 | 3/3 | +0 | +0 | 3/3 | 3/3 | n/a | true_answer | section_in_final_context; file final=true; section final=true; points final=manual | 根据提供的 OWASP Top 10 风险说明，这三类风险的核心区别在于**失效的根本原因和攻击面**不同： * **A01 Broken Access Control（失效的访问控制）** * **核心问题是授权策略失效** [1]。 *... |
| EXT-PG-02 | answerable_deep_section | true | 19823 | fail | answer_semantic_miss | 4/4 | 4/4 | +0 | -1 | 3/4 | 3/4 | 0/4 | true_answer | answer_generation_or_literal_mismatch; file final=true; section final=true; points final=4/4 | 根据提供的 PostgreSQL 18 文档上下文，二者的主要区别如下： - **存储与计算时机** - **Stored generated column（存储生成列）**：在行被插入或更新时 **计算**，并像普通列一样 **占用存储空... |
| EXT-PG-04 | answerable_version_conflict | true | 22542 | fail | retrieval_miss | 4/5 | 4/5 | +0 | -1 | 3/5 | 3/5 | n/a | true_answer | section_in_final_context; file final=true; section final=true; points final=manual | 当知识库同时包含 PostgreSQL 16 和 18 的文档时，回答 generated column 的 virtual / stored 差异需要**首先区分版本**，避免将某个版本的默认行为不加限定地应用到另一个版本。 根据上下文中... |


## Eval v2 By Case Type

| case_type | cases | runtime_ok | candidate_recall | final_context_recall | semantic_avg | answer_completeness_avg | literal_smoke | failure_layers |
|---|---:|---:|---|---|---:|---:|---|---|
| answerable | 2 | 2/2 | 6/6 | 6/6 | 100.0% | 100.0% | 3/6 | literal_only_mismatch:1, none:1 |
| answerable_deep_section | 4 | 4/4 | 10/10 | 10/10 | 93.8% | 93.8% | 3/10 | answer_semantic_miss:1, literal_only_mismatch:1, none:2 |
| answerable_formula | 1 | 1/1 | 3/3 | 3/3 | 100.0% | 100.0% | 3/3 | none:1 |
| answerable_long_context | 1 | 1/1 | n/a | n/a | 100.0% | 100.0% | n/a | none:1 |
| answerable_version_conflict | 1 | 1/1 | n/a | n/a | 60.0% | 60.0% | n/a | retrieval_miss:1 |
| unanswerable | 1 | 1/1 | n/a | n/a | 100.0% | 100.0% | n/a | none:1 |
| weak_related | 2 | 2/2 | n/a | n/a | 100.0% | 100.0% | n/a | none:2 |

## Answerability Matrix

| bucket | count |
|---|---:|
| true_answer | 9 |
| true_refusal | 3 |
| false_refusal | 0 |
| false_answer | 0 |
| n/a | 0 |

## Details

### EXT-ARTEMIS-01 - answerable

- question: Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？
- should_answer: `true`
- expected_source_section: Mission Overview
- completed: `true`
- duration_ms: `10989`
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
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，<br>Artemis I launch date, splashdown date, mission duration<br>Artemis I timeline launch splashdown days`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `4058` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？， \| Artemis I launch date, splashdown date, mission duration \| Artemis I timeline launch splashdown days`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `Artemis I \| HTTP语义和线格式的区别是什么？ \| Artemis I mission \| Artemis I timeline \| What happened on Flight Day 21 of Artemis I? \| What happened on Flight Day 24 of Artemis I? \| What happened on Flight Day 25 of Artemis I? \| What was the launch date of Artemis I?`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7541`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":1,"text":"Artemis I launch date, splashdown date, mission duration"},{"index":3,"rank":1,"text":"Artemis I timeline launch splashdown days"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #2 score=`0.0484`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7407`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":2,"text":"Artemis I launch date, splashdown date, mission duration"},{"index":3,"rank":2,"text":"Artemis I timeline launch splashdown days"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #3 score=`0.0476`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6757`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":3,"text":"Artemis I launch date, splashdown date, mission duration"},{"index":3,"rank":3,"text":"Artemis I timeline launch splashdown days"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #4 score=`0.0469`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6502`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0469`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":4,"text":"Artemis I launch date, splashdown date, mission duration"},{"index":3,"rank":4,"text":"Artemis I timeline launch splashdown days"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...
  - documents:
    - #1 score=`0.0492`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7541`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":1,"text":"Artemis I launch date, splashdown date, mission duration"},{"index":3,"rank":1,"text":"Artemis I timeline launch splashdown days"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #2 score=`0.0484`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6757`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":3,"text":"Artemis I launch date, splashdown date, mission duration"},{"index":3,"rank":3,"text":"Artemis I timeline launch splashdown days"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #3 score=`0.0476`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7407`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":2,"text":"Artemis I launch date, splashdown date, mission duration"},{"index":3,"rank":2,"text":"Artemis I timeline launch splashdown days"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #4 score=`0.0469`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6502`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，`, queryFusionScore=`0.0469`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？，"},{"index":2,"rank":4,"text":"Artemis I launch date, splashdown date, mission duration"},{"index":3,"rank":4,"text":"Artemis I timeline launch splashdown days"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...

answer:

```text
根据提供的任务资料，Artemis I 的发射日期、溅落日期和任务持续时间如下[1]：

- **发射日期**：2022年11月16日  
- **溅落日期**：2022年12月11日  
- **任务持续时间**：25天10小时53分钟
```

### EXT-ARTEMIS-02 - answerable_deep_section

- question: Artemis I Flight Day 26 的关键事件顺序是什么？
- should_answer: `true`
- expected_source_section: Flight Day 26
- completed: `true`
- duration_ms: `29055`
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
  - score_range: `0.0474 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `4340` / max `6000`
  - context_salience: cues `range` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `Artemis I Flight Day 26 的关键事件顺序是什么？，<br>Artemis I Flight Day 26 timeline sequence of events milestones<br>Artemis I Flight Day 26 events list phase burn trajectory maneuver order`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3501` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `Artemis I Flight Day 26 的关键事件顺序是什么？， \| Artemis I Flight Day 26 timeline sequence of events milestones \| Artemis I Flight Day 26 events list phase burn trajectory maneuver order`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `Flight Day 26 \| Artemis I \| What happened on Flight Day 21 of Artemis I? \| What happened on Flight Day 24 of Artemis I? \| What happened on Flight Day 25 of Artemis I? \| Flight Day 1 \| Flight Day 20 \| Flight Day 21`
  - pre_rerank_documents:
    - #1 score=`0.0489`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7962`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":2,"text":"Artemis I Flight Day 26 timeline sequence of events milestones"},{"index":3,"rank":1,"text":"Artemis I Flight Day 26 events list phase burn trajectory maneuver order"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #2 score=`0.0484`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7517`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":1,"text":"Artemis I Flight Day 26 timeline sequence of events milestones"},{"index":3,"rank":2,"text":"Artemis I Flight Day 26 events list phase burn trajectory maneuver order"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #3 score=`0.0476`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7551`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":3,"text":"Artemis I Flight Day 26 timeline sequence of events milestones"},{"index":3,"rank":4,"text":"Artemis I Flight Day 26 events list phase burn trajectory maneuver order"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #4 score=`0.0471`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7427`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0471`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":4,"text":"Artemis I Flight Day 26 timeline sequence of events milestones"},{"index":3,"rank":3,"text":"Artemis I Flight Day 26 events list phase burn trajectory maneuver order"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...
  - documents:
    - #1 score=`0.0492`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7962`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":2,"text":"Artemis I Flight Day 26 timeline sequence of events milestones"},{"index":3,"rank":1,"text":"Artemis I Flight Day 26 events list phase burn trajectory maneuver order"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #2 score=`0.0481`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7517`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0481`, rerankFusionScore=`0.0481`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":1,"text":"Artemis I Flight Day 26 timeline sequence of events milestones"},{"index":3,"rank":2,"text":"Artemis I Flight Day 26 events list phase burn trajectory maneuver order"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #3 score=`0.0474`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7427`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`3`, rerankScore=`0.0474`, rerankFusionScore=`0.0474`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0471`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":4,"text":"Artemis I Flight Day 26 timeline sequence of events milestones"},{"index":3,"rank":3,"text":"Artemis I Flight Day 26 events list phase burn trajectory maneuver order"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...
    - #4 score=`0.0474`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7551`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`4`, rerankScore=`0.0474`, rerankFusionScore=`0.0474`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Artemis I Flight Day 26 的关键事件顺序是什么？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Artemis I Flight Day 26 的关键事件顺序是什么？，"},{"index":2,"rank":3,"text":"Artemis I Flight Day 26 timeline sequence of events milestones"},{"index":3,"rank":4,"text":"Artemis I Flight Day 26 events list phase burn trajectory maneuver order"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...

answer:

```text
根据提供的任务时间线信息，Artemis I 任务飞行第 26 天（Flight Day 26）包含了最终的再入与回收序列，其关键事件按顺序如下：

1. **返回轨道修正燃烧-6**（Return trajectory correction burn-6）  
2. **乘员舱/服务舱分离**（Crew module/service module separation）  
3. **乘员舱抬升燃烧**（Crew module raise burn）  
4. **目标进入接口**（Targeted entry interface）  
5. **乘员舱进入接口**（Crew module entry interface）  
6. **溅落**（Splashdown）

这些步骤从最后的轨迹修正开始，经历了服务舱分离、抬升机动，然后进入大气层，最终以降落伞辅助的方式在太平洋溅落，标志着阿尔忒弥斯 I 任务的主要里程碑完成 [1][4]。
```

### EXT-ARTEMIS-04 - weak_related

- question: 根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？
- should_answer: `false`
- expected_source_section: Boundary Notes
- completed: `true`
- duration_ms: `10276`
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
  - score_range: `0.0469 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `4340` / max `6000`
  - context_salience: cues `range` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，<br>Artemis II crew members names astronauts Commander Pilot Mission Specialist<br>Artemis II crew announcement NASA selection list`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2618` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？， \| Artemis II crew members names astronauts Commander Pilot Mission Specialist \| Artemis II crew announcement NASA selection list`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `Artemis I timeline \| Artemis I \| https://www.nasa.gov/reference/artemis-i-mission-timeline \| External RAG Sample: NASA Artemis I Mission Timeline \| Artemis I mission \| What happened on Flight Day 21 of Artemis I? \| What happened on Flight Day 24 of Artemis I? \| What happened on Flight Day 25 of Artemis I?`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6892`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，"},{"index":2,"rank":1,"text":"Artemis II crew members names astronauts Commander Pilot Mission Specialist"},{"index":3,"rank":1,"text":"Artemis II crew announcement NASA selection list"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #2 score=`0.0484`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6704`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，"},{"index":2,"rank":2,"text":"Artemis II crew members names astronauts Commander Pilot Mission Specialist"},{"index":3,"rank":2,"text":"Artemis II crew announcement NASA selection list"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #3 score=`0.0476`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6087`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，"},{"index":2,"rank":3,"text":"Artemis II crew members names astronauts Commander Pilot Mission Specialist"},{"index":3,"rank":3,"text":"Artemis II crew announcement NASA selection list"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #4 score=`0.0313`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6218`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`Artemis II crew members names astronauts Commander Pilot Mission Specialist`, queryFusionScore=`0.0313`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":4,"text":"Artemis II crew members names astronauts Commander Pilot Mission Specialist"},{"index":3,"rank":4,"text":"Artemis II crew announcement NASA selection list"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...
  - documents:
    - #1 score=`0.0492`, source=`artemis-i-mission-timeline.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6704`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，"},{"index":2,"rank":2,"text":"Artemis II crew members names astronauts Commander Pilot Mission Specialist"},{"index":3,"rank":2,"text":"Artemis II crew announcement NASA selection list"}]`
      - preview: - Crew module entry interface. - Splashdown. NASA reported the Orion spacecraft completed a parachute-assisted splashdown in the Pacific Ocean as the final major milestone of Artem...
    - #2 score=`0.0484`, source=`artemis-i-mission-timeline.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6892`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，"},{"index":2,"rank":1,"text":"Artemis II crew members names astronauts Commander Pilot Mission Specialist"},{"index":3,"rank":1,"text":"Artemis II crew announcement NASA selection list"}]`
      - preview: # External RAG Sample: NASA Artemis I Mission Timeline Source URL: https://www.nasa.gov/reference/artemis-i-mission-timeline Prepared for: RAG timeline, exact facts, deep section r...
    - #3 score=`0.0476`, source=`artemis-i-mission-timeline.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6087`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？，"},{"index":2,"rank":3,"text":"Artemis II crew members names astronauts Commander Pilot Mission Specialist"},{"index":3,"rank":3,"text":"Artemis II crew announcement NASA selection list"}]`
      - preview: - Returned powered flyby burn. - Second and final close approach to the Moon. - Recovery assets prepared for Dec. 11 splashdown. ## Flight Day 21 On Flight Day 21, Orion performed ...
    - #4 score=`0.0469`, source=`artemis-i-mission-timeline.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6218`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`Artemis II crew members names astronauts Commander Pilot Mission Specialist`, queryFusionScore=`0.0313`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":4,"text":"Artemis II crew members names astronauts Commander Pilot Mission Specialist"},{"index":3,"rank":4,"text":"Artemis II crew announcement NASA selection list"}]`
      - preview: Early mission events included launch, max dynamic pressure, solid rocket booster separation, service module fairing jettison, launch abort system jettison, core stage main engine c...

answer:

```text
根据提供的上下文，**我无法回答这个问题**。

这份材料是 **Artemis I 任务时间线**的文档，其中明确说明：
- “This sample can answer Artemis I mission-fact and timeline questions. It cannot answer who flew Artemis II...”（[1]）
- “This sample does not contain the crew roster for Artemis II.”（[2]）

因此，上下文中没有 Artemis II 宇航员的任何信息，建议您查阅专门的 Artemis II 任务资料获取机组名单。
```

### EXT-RFC9110-01 - answerable

- question: RFC 9110 中 status code 的基本定义是什么？
- should_answer: `true`
- expected_source_section: Status Codes
- completed: `true`
- duration_ms: `13311`
- score_mode: `literal`
- eval_v2: verdict `pass` / failure_layer `none` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `3/3` / literal_smoke `3/3`
  - answer_completeness `3/3` / evidence_source_unverified `false`
  - semantic_hits `status_code_three_digit \| status_code_request_result \| status_code_response_semantics` / semantic_misses `—`
  - evidence_funnel: candidate `3/3` -> final_context `3/3` -> answer `3/3`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `section_in_final_context` / source_file `corpus/protocols/rfc-9110-http-semantics.md` / section `Status Codes`
  - pre_rerank: docs `3` / source_file `true` / section `true` / expected_points_exact `3/3`
  - final_context: docs `3` / source_file `true` / section `true` / expected_points_exact `3/3`
- expected_points:
  - [x] three-digit integer code
  - [x] result of the request
  - [x] semantics of the response

retrieval:
- event #1
  - retrieved_document_count: `3`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0476 .. 0.0492`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `4717` / max `6000`
  - context_salience: cues `range \| example` / expansions `1`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `3` / final `3`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `RFC 9110 中 status code 的基本定义是什么？，<br>RFC 9110 status code 定义 HTTP响应状态码<br>RFC 9110 HTTP status codes 分类 语义 示例`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3078` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `RFC 9110 中 status code 的基本定义是什么？， \| RFC 9110 status code 定义 HTTP响应状态码 \| RFC 9110 HTTP status codes 分类 语义 示例`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `RFC \| RFC 9110定义了哪些内容？ \| https://www.rfc-editor.org/rfc/rfc9110 \| status-code \| External RAG Sample: RFC 9110 HTTP Semantics \| Status Codes \| HTTP语义和线格式的区别是什么？ \| OAuth 2.1 authorization code flow message formats`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7738`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`RFC 9110 中 status code 的基本定义是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"RFC 9110 中 status code 的基本定义是什么？，"},{"index":2,"rank":1,"text":"RFC 9110 status code 定义 HTTP响应状态码"},{"index":3,"rank":1,"text":"RFC 9110 HTTP status codes 分类 语义 示例"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #2 score=`0.0484`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7567`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`RFC 9110 中 status code 的基本定义是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"RFC 9110 中 status code 的基本定义是什么？，"},{"index":2,"rank":2,"text":"RFC 9110 status code 定义 HTTP响应状态码"},{"index":3,"rank":2,"text":"RFC 9110 HTTP status codes 分类 语义 示例"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #3 score=`0.0317`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6010`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`RFC 9110 status code 定义 HTTP响应状态码`, queryFusionScore=`0.0317`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":3,"text":"RFC 9110 status code 定义 HTTP响应状态码"},{"index":3,"rank":3,"text":"RFC 9110 HTTP status codes 分类 语义 示例"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
  - documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7738`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`RFC 9110 中 status code 的基本定义是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"RFC 9110 中 status code 的基本定义是什么？，"},{"index":2,"rank":1,"text":"RFC 9110 status code 定义 HTTP响应状态码"},{"index":3,"rank":1,"text":"RFC 9110 HTTP status codes 分类 语义 示例"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #2 score=`0.0484`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7567`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`RFC 9110 中 status code 的基本定义是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"RFC 9110 中 status code 的基本定义是什么？，"},{"index":2,"rank":2,"text":"RFC 9110 status code 定义 HTTP响应状态码"},{"index":3,"rank":2,"text":"RFC 9110 HTTP status codes 分类 语义 示例"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #3 score=`0.0476`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6010`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`RFC 9110 status code 定义 HTTP响应状态码`, queryFusionScore=`0.0317`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":3,"text":"RFC 9110 status code 定义 HTTP响应状态码"},{"index":3,"rank":3,"text":"RFC 9110 HTTP status codes 分类 语义 示例"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...

answer:

```text
根据RFC 9110，状态码（status code）的基本定义是：

- **一个三位整数代码**（a three-digit integer code）[1]。
- **用来描述请求的结果以及响应的语义**（describes the result of the request and the semantics of the response）[1][2]。

除此之外，状态码还按首位数字划分为不同类别（如信息、成功、重定向、客户端错误、服务器错误），但这是分类方式，核心定义即上述两点。
```

### EXT-RFC9110-03 - answerable_formula

- question: RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？
- should_answer: `true`
- expected_source_section: Informational 1xx
- completed: `true`
- duration_ms: `12882`
- score_mode: `literal`
- eval_v2: verdict `pass` / failure_layer `none` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `3/3` / literal_smoke `3/3`
  - answer_completeness `3/3` / evidence_source_unverified `false`
  - semantic_hits `http10_no_1xx \| must_not_send_1xx \| http10_client_boundary` / semantic_misses `—`
  - evidence_funnel: candidate `3/3` -> final_context `3/3` -> answer `3/3`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `section_in_final_context` / source_file `corpus/protocols/rfc-9110-http-semantics.md` / section `Informational 1xx`
  - pre_rerank: docs `4` / source_file `true` / section `true` / expected_points_exact `3/3`
  - final_context: docs `4` / source_file `true` / section `true` / expected_points_exact `3/3`
- expected_points:
  - [x] HTTP/1.0 did not define 1xx
  - [x] MUST NOT send a 1xx response
  - [x] HTTP/1.0 client

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
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，<br>RFC 9110 1xx informational status codes HTTP/1.0 client exclusion restrictions<br>HTTP/1.0 client cannot receive 1xx responses reason limitation RFC 9110`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3408` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？， \| RFC 9110 1xx informational status codes HTTP/1.0 client exclusion restrictions \| HTTP/1.0 client cannot receive 1xx responses reason limitation RFC 9110`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `HTTP/1.0 \| RFC \| HTTP/1.0是否支持1xx状态码？ \| 服务器能否向HTTP/1.0客户端发送1xx响应？ \| RFC 9110定义了哪些内容？ \| https://www.rfc-editor.org/rfc/rfc9110 \| External RAG Sample: RFC 9110 HTTP Semantics \| HTTP/1.0 client exclusion`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0328`, vectorRank=`1`, vectorScore=`0.7447`, keywordRank=`1`, keywordScore=`0.3000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":1,"text":"RFC 9110 1xx informational status codes HTTP/1.0 client exclusion restrictions"},{"index":3,"rank":1,"text":"HTTP/1.0 client cannot receive 1xx responses reason limitation RFC 9110"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0484`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7075`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":2,"text":"RFC 9110 1xx informational status codes HTTP/1.0 client exclusion restrictions"},{"index":3,"rank":2,"text":"HTTP/1.0 client cannot receive 1xx responses reason limitation RFC 9110"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #3 score=`0.0159`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6391`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`RFC 9110 1xx informational status codes HTTP/1.0 client exclusion restrictions`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":3,"text":"RFC 9110 1xx informational status codes HTTP/1.0 client exclusion restrictions"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
    - #4 score=`0.0156`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6128`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`RFC 9110 1xx informational status codes HTTP/1.0 client exclusion restrictions`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":4,"text":"RFC 9110 1xx informational status codes HTTP/1.0 client exclusion restrictions"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.
  - documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0328`, vectorRank=`1`, vectorScore=`0.7447`, keywordRank=`1`, keywordScore=`0.3000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":1,"text":"RFC 9110 1xx informational status codes HTTP/1.0 client exclusion restrictions"},{"index":3,"rank":1,"text":"HTTP/1.0 client cannot receive 1xx responses reason limitation RFC 9110"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0484`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7075`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？，"},{"index":2,"rank":2,"text":"RFC 9110 1xx informational status codes HTTP/1.0 client exclusion restrictions"},{"index":3,"rank":2,"text":"HTTP/1.0 client cannot receive 1xx responses reason limitation RFC 9110"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #3 score=`0.0476`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6391`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`RFC 9110 1xx informational status codes HTTP/1.0 client exclusion restrictions`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":3,"text":"RFC 9110 1xx informational status codes HTTP/1.0 client exclusion restrictions"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
    - #4 score=`0.0469`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6128`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`RFC 9110 1xx informational status codes HTTP/1.0 client exclusion restrictions`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":4,"text":"RFC 9110 1xx informational status codes HTTP/1.0 client exclusion restrictions"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.

answer:

```text
根据 RFC 9110 的规定，**HTTP/1.0 并未定义 1xx 信息性状态码**，因此服务器**绝不能**向 HTTP/1.0 客户端发送 1xx 响应。

这一限制在上下文中有明确表述：“HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to an HTTP/1.0 client.”【1】
```

### EXT-RFC9110-04 - weak_related

- question: 根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？
- should_answer: `false`
- expected_source_section: Framework Boundary
- completed: `true`
- duration_ms: `13915`
- score_mode: `manual`
- eval_v2: verdict `pass` / failure_layer `none` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `1/1` / literal_smoke `n/a`
  - answer_completeness `1/1` / evidence_source_unverified `false`
  - semantic_hits `controller_advice_refusal` / semantic_misses `—`
  - evidence_funnel: candidate `1/1` -> final_context `1/1` -> answer `n/a`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_refusal` / did_answer `false`
- source_coverage: layer `section_in_final_context` / source_file `corpus/protocols/rfc-9110-http-semantics.md` / section `Framework Boundary`
  - pre_rerank: docs `4` / source_file `true` / section `true` / expected_points_exact `manual`
  - final_context: docs `4` / source_file `true` / section `true` / expected_points_exact `manual`
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
  - context_chars: actual `5810` / max `6000`
  - context_salience: cues `range \| example` / expansions `1`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，<br>RFC 9110 HTTP semantics @ControllerAdvice @ExceptionHandler ResponseEntity status code error response<br>Spring Boot @ControllerAdvice best practices RFC 9110 compliant error response format example step configuration`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3717` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？， \| RFC 9110 HTTP semantics @ControllerAdvice @ExceptionHandler ResponseEntity status code error response \| Spring Boot @ControllerAdvice best practices RFC 9110 compliant error response format example step configuration`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `ControllerAdvice \| RFC \| RFC 9110定义了哪些内容？ \| https://www.rfc-editor.org/rfc/rfc9110 \| External RAG Sample: RFC 9110 HTTP Semantics`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7402`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":1,"text":"RFC 9110 HTTP semantics @ControllerAdvice @ExceptionHandler ResponseEntity status code error response"},{"index":3,"rank":1,"text":"Spring Boot @ControllerAdvice best practices RFC 9110 compliant error response format example step configuration"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0484`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6720`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":2,"text":"RFC 9110 HTTP semantics @ControllerAdvice @ExceptionHandler ResponseEntity status code error response"},{"index":3,"rank":2,"text":"Spring Boot @ControllerAdvice best practices RFC 9110 compliant error response format example step configuration"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #3 score=`0.0476`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6076`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":3,"text":"RFC 9110 HTTP semantics @ControllerAdvice @ExceptionHandler ResponseEntity status code error response"},{"index":3,"rank":3,"text":"Spring Boot @ControllerAdvice best practices RFC 9110 compliant error response format example step configuration"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
    - #4 score=`0.0156`, source=`owasp-top10-a01-a03-a05.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6039`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`Spring Boot @ControllerAdvice best practices RFC 9110 compliant error response format example step configuration`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"Spring Boot @ControllerAdvice best practices RFC 9110 compliant error response format example step configuration"}]`
      - preview: # External RAG Sample: OWASP Top 10 A01 A03 A05 Source URLs: - https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control/ - https://owasp.org/Top10/2021/A03_2021-Injection/ - htt...
  - documents:
    - #1 score=`0.0492`, source=`rfc-9110-http-semantics.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7402`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":1,"text":"RFC 9110 HTTP semantics @ControllerAdvice @ExceptionHandler ResponseEntity status code error response"},{"index":3,"rank":1,"text":"Spring Boot @ControllerAdvice best practices RFC 9110 compliant error response format example step configuration"}]`
      - preview: - semantics of the response. ## Informational 1xx Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to a...
    - #2 score=`0.0484`, source=`rfc-9110-http-semantics.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6720`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":2,"text":"RFC 9110 HTTP semantics @ControllerAdvice @ExceptionHandler ResponseEntity status code error response"},{"index":3,"rank":2,"text":"Spring Boot @ControllerAdvice best practices RFC 9110 compliant error response format example step configuration"}]`
      - preview: # External RAG Sample: RFC 9110 HTTP Semantics Source URL: https://www.rfc-editor.org/rfc/rfc9110 Prepared for: standards semantics, exact term retrieval, framework-boundary refusa...
    - #3 score=`0.0474`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6076`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0474`, rerankFusionScore=`0.0474`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？，"},{"index":2,"rank":3,"text":"RFC 9110 HTTP semantics @ControllerAdvice @ExceptionHandler ResponseEntity status code error response"},{"index":3,"rank":3,"text":"Spring Boot @ControllerAdvice best practices RFC 9110 compliant error response format example step configuration"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
    - #4 score=`0.0471`, source=`owasp-top10-a01-a03-a05.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6039`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0471`, rerankFusionScore=`0.0471`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`Spring Boot @ControllerAdvice best practices RFC 9110 compliant error response format example step configuration`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"Spring Boot @ControllerAdvice best practices RFC 9110 compliant error response format example step configuration"}]`
      - preview: # External RAG Sample: OWASP Top 10 A01 A03 A05 Source URLs: - https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control/ - https://owasp.org/Top10/2021/A03_2021-Injection/ - htt...

answer:

```text
根据提供的上下文，我无法回答如何编写 Spring Boot 的 `@ControllerAdvice`。

[1] 中明确指出：“RFC 9110 is not a Spring Boot guide. It does not specify how to implement `@ControllerAdvice`, how to map Java exceptions, or how to structure a JSON error body in a Spring application.” 这说明 RFC 9110 并不涉及框架层面的具体实现，自然也不包含 `@ControllerAdvice` 的写法规范或示例。因此，基于当前可用的信息，我无法给出符合要求的 Spring Boot 代码。
```

### EXT-K8S-PV-02 - answerable_deep_section

- question: Retain reclaim policy 下管理员需要做哪些手动回收步骤？
- should_answer: `true`
- expected_source_section: Retain Reclaim Policy
- completed: `true`
- duration_ms: `12884`
- score_mode: `literal`
- eval_v2: verdict `pass` / failure_layer `literal_only_mismatch` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `3/3` / literal_smoke `0/3`
  - answer_completeness `3/3` / evidence_source_unverified `false`
  - semantic_hits `retain_manual_reclaim_order` / semantic_misses `—`
  - evidence_funnel: candidate `3/3` -> final_context `3/3` -> answer `0/3`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `answer_generation_or_literal_mismatch` / source_file `corpus/kubernetes/kubernetes-persistent-volumes.md` / section `Retain Reclaim Policy`
  - pre_rerank: docs `3` / source_file `true` / section `true` / expected_points_exact `3/3`
  - final_context: docs `3` / source_file `true` / section `true` / expected_points_exact `3/3`
- expected_points:
  - [ ] Delete the PersistentVolume
  - [ ] Clean up data
  - [ ] Delete the associated storage asset

retrieval:
- event #1
  - retrieved_document_count: `3`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0476 .. 0.0492`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `3682` / max `6000`
  - context_salience: cues `range` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `3` / final `3`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `Retain reclaim policy 下管理员需要做哪些手动回收步骤？，<br>Retain reclaim policy manual reclaim steps PVC PV cleanup<br>Retain reclaim policy 手动回收 操作步骤 清理流程 配置参数`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2858` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `Retain reclaim policy 下管理员需要做哪些手动回收步骤？， \| Retain reclaim policy manual reclaim steps PVC PV cleanup \| Retain reclaim policy 手动回收 操作步骤 清理流程 配置参数`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `Retain Reclaim Policy \| Delete Reclaim Policy \| Authorization policy failure \| Reclaiming \| RFC 9110定义了哪些内容？`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`kubernetes-persistent-volumes.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7032`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Retain reclaim policy 下管理员需要做哪些手动回收步骤？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Retain reclaim policy 下管理员需要做哪些手动回收步骤？，"},{"index":2,"rank":1,"text":"Retain reclaim policy manual reclaim steps PVC PV cleanup"},{"index":3,"rank":1,"text":"Retain reclaim policy 手动回收 操作步骤 清理流程 配置参数"}]`
      - preview: ## Reclaiming When a user is done with a volume, the PVC can be deleted. The reclaim policy tells Kubernetes what to do with the volume after the claim is released. Current reclaim...
    - #2 score=`0.0484`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6443`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Retain reclaim policy 下管理员需要做哪些手动回收步骤？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Retain reclaim policy 下管理员需要做哪些手动回收步骤？，"},{"index":2,"rank":2,"text":"Retain reclaim policy manual reclaim steps PVC PV cleanup"},{"index":3,"rank":2,"text":"Retain reclaim policy 手动回收 操作步骤 清理流程 配置参数"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...
    - #3 score=`0.0159`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6375`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`Retain reclaim policy manual reclaim steps PVC PV cleanup`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":3,"text":"Retain reclaim policy manual reclaim steps PVC PV cleanup"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
  - documents:
    - #1 score=`0.0492`, source=`kubernetes-persistent-volumes.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7032`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Retain reclaim policy 下管理员需要做哪些手动回收步骤？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Retain reclaim policy 下管理员需要做哪些手动回收步骤？，"},{"index":2,"rank":1,"text":"Retain reclaim policy manual reclaim steps PVC PV cleanup"},{"index":3,"rank":1,"text":"Retain reclaim policy 手动回收 操作步骤 清理流程 配置参数"}]`
      - preview: ## Reclaiming When a user is done with a volume, the PVC can be deleted. The reclaim policy tells Kubernetes what to do with the volume after the claim is released. Current reclaim...
    - #2 score=`0.0484`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6375`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`Retain reclaim policy manual reclaim steps PVC PV cleanup`, queryFusionScore=`0.0159`, queryFusionRank=`3`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":3,"text":"Retain reclaim policy manual reclaim steps PVC PV cleanup"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
    - #3 score=`0.0476`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6443`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Retain reclaim policy 下管理员需要做哪些手动回收步骤？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Retain reclaim policy 下管理员需要做哪些手动回收步骤？，"},{"index":2,"rank":2,"text":"Retain reclaim policy manual reclaim steps PVC PV cleanup"},{"index":3,"rank":2,"text":"Retain reclaim policy 手动回收 操作步骤 清理流程 配置参数"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...

answer:

```text
在 `Retain` 回收策略下，管理员需要手动执行以下步骤来完成存储资源的回收[1]：

1. **删除 PersistentVolume 对象**。
2. **清理关联存储资产（如云盘、NFS 目录等）上的残留数据**。
3. **删除该存储资产**，或者如果希望重用，可以使用相同的存储资产定义创建一个新的 PersistentVolume。

这些步骤确保了旧数据被清除，释放的存储可以被安全地重新分配或重用。
```

### EXT-K8S-PV-03 - answerable_long_context

- question: PVC 扩容失败时，文档给出的恢复流程是什么？
- should_answer: `true`
- expected_source_section: Recovering From Failure When Expanding Volumes
- completed: `true`
- duration_ms: `14315`
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
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `PVC 扩容失败时，文档给出的恢复流程是什么？，<br>PVC 扩容失败 恢复流程 PersistentVolumeClaim resize failure recovery steps<br>PVC 扩容失败 原因 错误 kubectl 命令 存储类 容量限制 恢复步骤`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2775` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `PVC 扩容失败时，文档给出的恢复流程是什么？， \| PVC 扩容失败 恢复流程 PersistentVolumeClaim resize failure recovery steps \| PVC 扩容失败 原因 错误 kubectl 命令 存储类 容量限制 恢复步骤`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `PVC \| PVCs \| PVC Expansion \| HTTP语义和线格式的区别是什么？`
  - pre_rerank_documents:
    - #1 score=`0.0489`, source=`kubernetes-persistent-volumes.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7002`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":1,"text":"PVC 扩容失败 恢复流程 PersistentVolumeClaim resize failure recovery steps"},{"index":3,"rank":1,"text":"PVC 扩容失败 原因 错误 kubectl 命令 存储类 容量限制 恢复步骤"}]`
      - preview: ## Reclaiming When a user is done with a volume, the PVC can be deleted. The reclaim policy tells Kubernetes what to do with the volume after the claim is released. Current reclaim...
    - #2 score=`0.0484`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7061`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":3,"text":"PVC 扩容失败 恢复流程 PersistentVolumeClaim resize failure recovery steps"},{"index":3,"rank":2,"text":"PVC 扩容失败 原因 错误 kubectl 命令 存储类 容量限制 恢复步骤"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
    - #3 score=`0.0479`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6907`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0479`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":2,"text":"PVC 扩容失败 恢复流程 PersistentVolumeClaim resize failure recovery steps"},{"index":3,"rank":3,"text":"PVC 扩容失败 原因 错误 kubectl 命令 存储类 容量限制 恢复步骤"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...
  - documents:
    - #1 score=`0.0492`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7061`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":3,"text":"PVC 扩容失败 恢复流程 PersistentVolumeClaim resize failure recovery steps"},{"index":3,"rank":2,"text":"PVC 扩容失败 原因 错误 kubectl 命令 存储类 容量限制 恢复步骤"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
    - #2 score=`0.0481`, source=`kubernetes-persistent-volumes.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7002`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`0.0481`, rerankFusionScore=`0.0481`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":1,"text":"PVC 扩容失败 恢复流程 PersistentVolumeClaim resize failure recovery steps"},{"index":3,"rank":1,"text":"PVC 扩容失败 原因 错误 kubectl 命令 存储类 容量限制 恢复步骤"}]`
      - preview: ## Reclaiming When a user is done with a volume, the PVC can be deleted. The reclaim policy tells Kubernetes what to do with the volume after the claim is released. Current reclaim...
    - #3 score=`0.0479`, source=`kubernetes-persistent-volumes.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6907`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0479`, rerankFusionScore=`0.0479`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`PVC 扩容失败时，文档给出的恢复流程是什么？，`, queryFusionScore=`0.0479`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"PVC 扩容失败时，文档给出的恢复流程是什么？，"},{"index":2,"rank":2,"text":"PVC 扩容失败 恢复流程 PersistentVolumeClaim resize failure recovery steps"},{"index":3,"rank":3,"text":"PVC 扩容失败 原因 错误 kubectl 命令 存储类 容量限制 恢复步骤"}]`
      - preview: # External RAG Sample: Kubernetes Persistent Volumes Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/ Prepared for: long technical docs, lifecycle terms,...

answer:

```text
根据文档，当 PVC 扩容失败时（例如底层存储无法满足请求的大小），集群管理员可以按以下流程手动恢复：

1. 将与该 PVC 绑定的 PersistentVolume（PV）的回收策略标记为 **Retain**。
2. 删除该 PVC。由于已经设置为 Retain 策略，删除 PVC 不会丢失数据。
3. 从 PV 的规格中删除 `claimRef` 条目，使 PV 变为 **Available** 状态。
4. 重新创建一个 PVC，指定一个 **小于** PV 容量的较小尺寸，并通过设置 `volumeName` 字段将其直接绑定到已有的 PV 名称上。
5. 恢复 PV 的回收策略。

文档还特别指出：Kubernetes 不支持将 PVC 缩小到其当前大小以下。
```

### EXT-K8S-PV-04 - unanswerable

- question: 根据 PV 文档，MySQL InnoDB undo log 应该如何调优？
- should_answer: `false`
- expected_source_section: Boundary Notes
- completed: `true`
- duration_ms: `9263`
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
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，<br>MySQL InnoDB undo log 调优参数 innodb_undo_tablespaces innodb_undo_log_truncate innodb_max_undo_log_size 配置<br>undo log 调优步骤 配置建议 大小设置 清理策略 阈值 性能影响`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2983` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `根据 PV 文档，MySQL InnoDB undo log 应该如何调优？， \| MySQL InnoDB undo log 调优参数 innodb_undo_tablespaces innodb_undo_log_truncate innodb_max_undo_log_size 配置 \| undo log 调优步骤 配置建议 大小设置 清理策略 阈值 性能影响`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `InnoDB \| MySQL \| SQL \| MySQL invisible columns`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6261`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
  - documents:
    - #1 score=`0.0492`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6261`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据 PV 文档，MySQL InnoDB undo log 应该如何调优？，"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...

answer:

```text
根据您提供的 PV 文档内容，它明确说明：“本示例涵盖 Kubernetes 存储对象，不描述 MySQL InnoDB MVCC、undo log 调优或数据库事务内部原理。”因此，我无法基于该文档回答关于 MySQL InnoDB undo log 调优的问题，因为文档中不包含任何相关信息。
```

### EXT-OWASP-03 - answerable_deep_section

- question: Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？
- should_answer: `true`
- expected_source_section: Category Differences
- completed: `true`
- duration_ms: `22293`
- score_mode: `manual`
- eval_v2: verdict `pass` / failure_layer `none` / health `ok` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `3/3` / literal_smoke `n/a`
  - answer_completeness `3/3` / evidence_source_unverified `false`
  - semantic_hits `broken_access_control_boundary \| injection_boundary \| security_misconfiguration_boundary` / semantic_misses `—`
  - evidence_funnel: candidate `3/3` -> final_context `3/3` -> answer `n/a`
  - deltas: candidate_to_final `+0` / final_to_answer `+0`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `section_in_final_context` / source_file `corpus/security/owasp-top10-a01-a03-a05.md` / section `Category Differences`
  - pre_rerank: docs `3` / source_file `true` / section `true` / expected_points_exact `manual`
  - final_context: docs `3` / source_file `true` / section `true` / expected_points_exact `manual`
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
  - score_range: `0.0476 .. 0.0492`
  - context_selected: `3` / dropped: `0` / truncated: `false`
  - context_chars: actual `3250` / max `6000`
  - context_salience: cues `range \| example` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `3` / final `3`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，<br>Broken Access Control Injection Security Misconfiguration OWASP Top 10 对比 差异<br>Broken Access Control 与 Injection 与 Security Misconfiguration 示例 攻击方式 预防 原理`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `4524` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？， \| Broken Access Control Injection Security Misconfiguration OWASP Top 10 对比 差异 \| Broken Access Control 与 Injection 与 Security Misconfiguration 示例 攻击方式 预防 原理`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `Broken Access Control \| Security Misconfiguration \| Injection \| What is Broken Access Control? \| https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control \| A01 Broken Access Control \| What is Security Misconfiguration? \| access-control`
  - pre_rerank_documents:
    - #1 score=`0.0489`, source=`owasp-top10-a01-a03-a05.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7612`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":1,"text":"Broken Access Control Injection Security Misconfiguration OWASP Top 10 对比 差异"},{"index":3,"rank":1,"text":"Broken Access Control 与 Injection 与 Security Misconfiguration 示例 攻击方式 预防 原理"}]`
      - preview: # External RAG Sample: OWASP Top 10 A01 A03 A05 Source URLs: - https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control/ - https://owasp.org/Top10/2021/A03_2021-Injection/ - htt...
    - #2 score=`0.0486`, source=`owasp-top10-a01-a03-a05.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7852`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0486`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":2,"text":"Broken Access Control Injection Security Misconfiguration OWASP Top 10 对比 差异"},{"index":3,"rank":2,"text":"Broken Access Control 与 Injection 与 Security Misconfiguration 示例 攻击方式 预防 原理"}]`
      - preview: A good answer should focus on permission boundaries, object ownership, and server-side enforcement. ## A03 Injection Injection occurs when untrusted input reaches an interpreter or...
    - #3 score=`0.0476`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7313`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":3,"text":"Broken Access Control Injection Security Misconfiguration OWASP Top 10 对比 差异"},{"index":3,"rank":3,"text":"Broken Access Control 与 Injection 与 Security Misconfiguration 示例 攻击方式 预防 原理"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
  - documents:
    - #1 score=`0.0492`, source=`owasp-top10-a01-a03-a05.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7313`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":3,"text":"Broken Access Control Injection Security Misconfiguration OWASP Top 10 对比 差异"},{"index":3,"rank":3,"text":"Broken Access Control 与 Injection 与 Security Misconfiguration 示例 攻击方式 预防 原理"}]`
      - preview: - Broken Access Control is about authorization policy failure. - Injection is about unsafe data flowing into interpreters or command contexts. - Security Misconfiguration is about ...
    - #2 score=`0.0484`, source=`owasp-top10-a01-a03-a05.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7852`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0486`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":2,"text":"Broken Access Control Injection Security Misconfiguration OWASP Top 10 对比 差异"},{"index":3,"rank":2,"text":"Broken Access Control 与 Injection 与 Security Misconfiguration 示例 攻击方式 预防 原理"}]`
      - preview: A good answer should focus on permission boundaries, object ownership, and server-side enforcement. ## A03 Injection Injection occurs when untrusted input reaches an interpreter or...
    - #3 score=`0.0476`, source=`owasp-top10-a01-a03-a05.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7612`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？，"},{"index":2,"rank":1,"text":"Broken Access Control Injection Security Misconfiguration OWASP Top 10 对比 差异"},{"index":3,"rank":1,"text":"Broken Access Control 与 Injection 与 Security Misconfiguration 示例 攻击方式 预防 原理"}]`
      - preview: # External RAG Sample: OWASP Top 10 A01 A03 A05 Source URLs: - https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control/ - https://owasp.org/Top10/2021/A03_2021-Injection/ - htt...

answer:

```text
根据提供的 OWASP Top 10 风险说明，这三类风险的核心区别在于**失效的根本原因和攻击面**不同：

*   **A01 Broken Access Control（失效的访问控制）**  
    *   **核心问题是授权策略失效** [1]。  
    *   攻击者利用缺失、绕过或错误实施的权限检查，以未获得授权的身份（或提权后）访问、修改或删除资源。典型表现形式包括：违反最小权限原则、URL/参数篡改绕过检查、不安全直接对象引用（IDOR）、对 API 的 POST/PUT/DELETE 缺少访问控制等 [3]。  
    *   **本质：权限边界被打破，允许越权操作。**

*   **A03 Injection（注入）**  
    *   **核心问题是不可信数据流入解释器或命令上下文** [1][2]。  
    *   当用户输入未经验证、过滤或清洗，直接被嵌入动态查询、非参数化调用或命令中，攻击者就能注入恶意代码，使解释器执行非预期指令。典型示例包括 SQL 注入、跨站脚本、命令注入、不安全文件路径控制等 [2]。  
    *   **本质：数据与代码/指令混淆，导致执行异常。**

*   **A05 Security Misconfiguration（安全配置错误）**  
    *   **核心问题是系统、框架、服务器或部署层面的不安全设置** [1][3]。  
    *   表现为默认账户密码未更改、缺少安全头部、开启不必要的端口/服务/功能、错误信息泄露堆栈跟踪、组件未升级导致新版安全功能被禁用等 [2]。攻击者通过利用这些配置缺陷逐步渗透，通常无需绕过认证或编写复杂注入。  
    *   **本质：基础设施或应用自身的“锁”没装对或没锁好。**

**总结差异**：  
- **访问控制失效** → “该不该让你进/做这件事”的规则错了。  
- **注入** → “你送进来的脏数据”被当成指令执行了。  
- **安全配置错误** → “房子的门禁系统、门窗、监控”本身没按安全标准装好。

同一安全事件可能同时涉及多个类别，但精准判定应识别主要的失效模式 [1]。
```

### EXT-PG-02 - answerable_deep_section

- question: PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？
- should_answer: `true`
- expected_source_section: PostgreSQL 18 Generated Columns
- completed: `true`
- duration_ms: `19823`
- score_mode: `literal`
- eval_v2: verdict `fail` / failure_layer `answer_semantic_miss` / health `fail` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `3/4` / literal_smoke `0/4`
  - answer_completeness `3/4` / evidence_source_unverified `false`
  - semantic_hits `stored_generated_column_occupies_storage \| virtual_generated_column_read_time \| virtual_generated_column_no_storage` / semantic_misses `stored_generated_column_write_time`
  - evidence_funnel: candidate `4/4` -> final_context `4/4` -> answer `0/4`
  - deltas: candidate_to_final `+0` / final_to_answer `-1`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `answer_generation_or_literal_mismatch` / source_file `corpus/database/postgresql-generated-columns-version-contrast.md` / section `PostgreSQL 18 Generated Columns`
  - pre_rerank: docs `4` / source_file `true` / section `true` / expected_points_exact `4/4`
  - final_context: docs `4` / source_file `true` / section `true` / expected_points_exact `4/4`
- expected_points:
  - [ ] computed when the row is written
  - [ ] occupies storage
  - [ ] computed when it is read
  - [ ] occupies no storage

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0469 .. 0.0489`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `3943` / max `6000`
  - context_salience: cues `range` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？，<br>PostgreSQL 18 stored generated column virtual generated column difference comparison STORED VIRTUAL<br>PostgreSQL 18 generated column STORED VIRTUAL behavior syntax example limitation performance storage`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2243` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？， \| PostgreSQL 18 stored generated column virtual generated column difference comparison STORED VIRTUAL \| PostgreSQL 18 generated column STORED VIRTUAL behavior syntax example limitation performance storage`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `stored generated column \| virtual generated column \| generated column \| PostgreSQL \| STORED \| VIRTUAL \| POST \| SQL`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.8043`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？，"},{"index":2,"rank":1,"text":"PostgreSQL 18 stored generated column virtual generated column difference comparison STORED VIRTUAL"},{"index":3,"rank":1,"text":"PostgreSQL 18 generated column STORED VIRTUAL behavior syntax example limitation performance storage"}]`
      - preview: # External RAG Sample: PostgreSQL Generated Columns Version Contrast Source URLs: - https://www.postgresql.org/docs/18/ddl-generated-columns.html - https://www.postgresql.org/docs/...
    - #2 score=`0.0484`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7590`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？，"},{"index":2,"rank":2,"text":"PostgreSQL 18 stored generated column virtual generated column difference comparison STORED VIRTUAL"},{"index":3,"rank":2,"text":"PostgreSQL 18 generated column STORED VIRTUAL behavior syntax example limitation performance storage"}]`
      - preview: - The generation expression cannot reference another generated column. - A generated column cannot have a column default or identity definition. - A generated column cannot be part...
    - #3 score=`0.0317`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6488`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`PostgreSQL 18 stored generated column virtual generated column difference comparison STORED VIRTUAL`, queryFusionScore=`0.0317`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":3,"text":"PostgreSQL 18 stored generated column virtual generated column difference comparison STORED VIRTUAL"},{"index":3,"rank":3,"text":"PostgreSQL 18 generated column STORED VIRTUAL behavior syntax example limitation performance storage"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.
    - #4 score=`0.0156`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6063`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`PostgreSQL 18 generated column STORED VIRTUAL behavior syntax example limitation performance storage`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"PostgreSQL 18 generated column STORED VIRTUAL behavior syntax example limitation performance storage"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
  - documents:
    - #1 score=`0.0489`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.8043`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0489`, rerankFusionScore=`0.0489`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？，"},{"index":2,"rank":1,"text":"PostgreSQL 18 stored generated column virtual generated column difference comparison STORED VIRTUAL"},{"index":3,"rank":1,"text":"PostgreSQL 18 generated column STORED VIRTUAL behavior syntax example limitation performance storage"}]`
      - preview: # External RAG Sample: PostgreSQL Generated Columns Version Contrast Source URLs: - https://www.postgresql.org/docs/18/ddl-generated-columns.html - https://www.postgresql.org/docs/...
    - #2 score=`0.0486`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7590`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0486`, rerankFusionScore=`0.0486`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？，"},{"index":2,"rank":2,"text":"PostgreSQL 18 stored generated column virtual generated column difference comparison STORED VIRTUAL"},{"index":3,"rank":2,"text":"PostgreSQL 18 generated column STORED VIRTUAL behavior syntax example limitation performance storage"}]`
      - preview: - The generation expression cannot reference another generated column. - A generated column cannot have a column default or identity definition. - A generated column cannot be part...
    - #3 score=`0.0476`, source=`kubernetes-persistent-volumes.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6063`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`PostgreSQL 18 generated column STORED VIRTUAL behavior syntax example limitation performance storage`, queryFusionScore=`0.0156`, queryFusionRank=`4`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"PostgreSQL 18 generated column STORED VIRTUAL behavior syntax example limitation performance storage"}]`
      - preview: The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands. ## Recovering From Failure When Expanding Volumes If a user requests a s...
    - #4 score=`0.0469`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6488`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`PostgreSQL 18 stored generated column virtual generated column difference comparison STORED VIRTUAL`, queryFusionScore=`0.0317`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":3,"text":"PostgreSQL 18 stored generated column virtual generated column difference comparison STORED VIRTUAL"},{"index":3,"rank":3,"text":"PostgreSQL 18 generated column STORED VIRTUAL behavior syntax example limitation performance storage"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.

answer:

```text
根据提供的 PostgreSQL 18 文档上下文，二者的主要区别如下：

- **存储与计算时机**  
  - **Stored generated column（存储生成列）**：在行被插入或更新时 **计算**，并像普通列一样 **占用存储空间**。  
  - **Virtual generated column（虚拟生成列）**：**不占用存储空间**，仅在每次 **读取** 该列时实时计算。  

- **默认行为**  
  在 PostgreSQL 18 中，如果未显式指定 `VIRTUAL` 或 `STORED`，生成列 **默认为虚拟（virtual）** 类型 [1]。

- **额外限制**  
  - 所有生成列都会受到通用限制（如表达式只能使用不可变函数、不能引用其他生成列、不能作为分区键等）[2]。  
  - **虚拟生成列** 对 **用户自定义类型和函数** 还存在额外限制，而存储生成列没有这些限制 [2]。

简言之，stored 列是“写时计算、存结果”；virtual 列是“读时计算、不存结果”，且 virtual 列在自定义类型/函数上有更严格的约束。
```

### EXT-PG-04 - answerable_version_conflict

- question: 如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？
- should_answer: `true`
- expected_source_section: Version Conflict Handling
- completed: `true`
- duration_ms: `22542`
- score_mode: `manual`
- eval_v2: verdict `fail` / failure_layer `retrieval_miss` / health `fail` / valid_for_scoring `true`
  - case_schema_version `v2-compatible` / semantic_score `3/5` / literal_smoke `n/a`
  - answer_completeness `3/5` / evidence_source_unverified `false`
  - semantic_hits `distinguish_versions \| pg18_default_virtual \| pg16_stored` / semantic_misses `pg18_stored_and_virtual \| do_not_apply_pg18_to_pg16`
  - evidence_funnel: candidate `4/5` -> final_context `4/5` -> answer `n/a`
  - deltas: candidate_to_final `+0` / final_to_answer `-1`
  - answerability: bucket `true_answer` / did_answer `true`
- source_coverage: layer `section_in_final_context` / source_file `corpus/database/postgresql-generated-columns-version-contrast.md` / section `Version Conflict Handling`
  - pre_rerank: docs `3` / source_file `true` / section `true` / expected_points_exact `manual`
  - final_context: docs `3` / source_file `true` / section `true` / expected_points_exact `manual`
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
  - context_chars: actual `2946` / max `6000`
  - context_salience: cues `range` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `3` / final `3`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，<br>PostgreSQL generated column VIRTUAL STORED syntax difference version 16 18<br>DDL generated columns example PostgreSQL 16 vs 18 behavior change`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `1882` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？， \| PostgreSQL generated column VIRTUAL STORED syntax difference version 16 18 \| DDL generated columns example PostgreSQL 16 vs 18 behavior change`
  - profile_hints: version `hybrid-v2,rule-v1` / source `AUTO_PROFILE` / selected `generated column \| PostgreSQL \| STORED \| VIRTUAL \| POST \| SQL \| https://www.postgresql.org/docs/18/ddl-generated-columns.html \| External RAG Sample: PostgreSQL Generated Columns Version Contrast`
  - pre_rerank_documents:
    - #1 score=`0.0489`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7895`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":1,"text":"PostgreSQL generated column VIRTUAL STORED syntax difference version 16 18"},{"index":3,"rank":1,"text":"DDL generated columns example PostgreSQL 16 vs 18 behavior change"}]`
      - preview: # External RAG Sample: PostgreSQL Generated Columns Version Contrast Source URLs: - https://www.postgresql.org/docs/18/ddl-generated-columns.html - https://www.postgresql.org/docs/...
    - #2 score=`0.0486`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7949`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0486`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":2,"text":"PostgreSQL generated column VIRTUAL STORED syntax difference version 16 18"},{"index":3,"rank":2,"text":"DDL generated columns example PostgreSQL 16 vs 18 behavior change"}]`
      - preview: - The generation expression cannot reference another generated column. - A generated column cannot have a column default or identity definition. - A generated column cannot be part...
    - #3 score=`0.0476`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6291`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":3,"text":"PostgreSQL generated column VIRTUAL STORED syntax difference version 16 18"},{"index":3,"rank":3,"text":"DDL generated columns example PostgreSQL 16 vs 18 behavior change"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.
  - documents:
    - #1 score=`0.0489`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7949`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`2`, rerankRank=`1`, rerankScore=`0.0489`, rerankFusionScore=`0.0489`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0486`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":2,"text":"PostgreSQL generated column VIRTUAL STORED syntax difference version 16 18"},{"index":3,"rank":2,"text":"DDL generated columns example PostgreSQL 16 vs 18 behavior change"}]`
      - preview: - The generation expression cannot reference another generated column. - A generated column cannot have a column default or identity definition. - A generated column cannot be part...
    - #2 score=`0.0486`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7895`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`0.0486`, rerankFusionScore=`0.0486`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":1,"text":"PostgreSQL generated column VIRTUAL STORED syntax difference version 16 18"},{"index":3,"rank":1,"text":"DDL generated columns example PostgreSQL 16 vs 18 behavior change"}]`
      - preview: # External RAG Sample: PostgreSQL Generated Columns Version Contrast Source URLs: - https://www.postgresql.org/docs/18/ddl-generated-columns.html - https://www.postgresql.org/docs/...
    - #3 score=`0.0476`, source=`postgresql-generated-columns-version-contrast.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6291`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, coverageGuardReason=`—`, coverageGuardCues=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？，"},{"index":2,"rank":3,"text":"PostgreSQL generated column VIRTUAL STORED syntax difference version 16 18"},{"index":3,"rank":3,"text":"DDL generated columns example PostgreSQL 16 vs 18 behavior change"}]`
      - preview: It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.

answer:

```text
当知识库同时包含 PostgreSQL 16 和 18 的文档时，回答 generated column 的 virtual / stored 差异需要**首先区分版本**，避免将某个版本的默认行为不加限定地应用到另一个版本。

根据上下文中的版本冲突处理指导 [1]：

- **PostgreSQL 16** 的 `CREATE TABLE` 文档中，generated column 使用 `GENERATED ALWAYS AS (...) STORED` 语法，**只存在 stored 这一种生成列** [1]。
- **PostgreSQL 18** 则引入了两种生成列：**stored**（写入时计算、占用存储）和 **virtual**（读取时计算、不占存储），并且**在未显式指明类型时，默认为 virtual** [2]。

因此，回答时应遵循以下原则：

1. **先确认问题的版本指向**  
   如果用户明确提到了 PostgreSQL 18 或问题中包含了 PostgreSQL 18 文档引用，应说明 18 的两个种类及默认行为 [2]。  
   如果用户只提到 PostgreSQL 16 或引用了 16 的文档，不要宣称 PostgreSQL 18 的 virtual 默认行为，应指出 16 中 generated column 即为 stored [1]。

2. **同时展示两个版本时明确对照**  
   例如可以这样回答：
   > PostgreSQL 16 中的 generated column 只有 stored 类型，写入时计算并存储 [1]。  
   > PostgreSQL 18 新增了 virtual generated column，读取时计算、不占存储，并且在不指定 `VIRTUAL` 或 `STORED` 时默认为 virtual [2]。

3. **注意限制的差异**  
   上下文提到，virtual generated column 对用户定义类型和函数有额外限制，而 stored generated column 没有这些限制 [1]。回答时如果涉及限制条件，也应当区分版本说明。

总之，**版本是回答的核心前提**：不跨版本套用结论，而是根据提问中涉及的版本来选择对应的论述，或主动分版本对比呈现。如果问题没有指明版本，可以同时给出两者并请用户确认环境。
```
