# Step 8.5 Eval v2 Ablation Matrix Summary

Generated at: 2026-05-25

Scope:

- Cases: `RAG-10`, `RAG-14`, `EXT-ARTEMIS-01`, `EXT-RFC9110-03`, `EXT-K8S-PV-03`, `EXT-PG-04`
- Runner: `docs/dev-ops/rag-eval/rag_eval_runner.py`
- Cases file: `docs/dev-ops/rag-eval/results/step8-eval-v2/step8.4-v2-rubric-6-cases.json`
- Output reports: `docs/dev-ops/rag-eval/results/step8.5-v2-ablation-matrix/`

Important boundary:

- This matrix explicitly clears `queryRewriteDomainHints` during temporary advisor mutation, so `no profile` is not polluted by old manual hints.
- The script restores `rag_advisor_grafana_llm_v2` after the matrix. Restored filter is `knowledge == 'rag-profile-llm-v2-dedicated-20260523'`; restored manual hints count is `9`.
- BGE rerank service was live. No report has non-empty `rerank_runtime.failure_reason`.
- This is a 6-case single-run matrix. Treat module-level lift as directional, not statistically final.

## Run Aggregate

| run | completed | semantic | completeness | candidate | final | literal | failures | rewrite_modes | profile_sources | avg_ms |
|---|---:|---:|---:|---:|---:|---:|---|---|---|---:|
| 01-l0-vector-only | 6/6 | 12/22 (55%) | 12/22 (55%) | 18/22 (82%) | 18/22 (82%) | 2/11 (18%) | answer_semantic_miss:2, none:2, retrieval_miss:2 | PASSTHROUGH:6 | NONE:6 | 7896 |
| 02-l1-hybrid-only | 6/6 | 16/22 (73%) | 16/22 (73%) | 18/22 (82%) | 18/22 (82%) | 4/11 (36%) | answer_semantic_miss:1, literal_only_mismatch:1, none:2, retrieval_miss:2 | PASSTHROUGH:6 | NONE:6 | 8270 |
| 03-l2-hybrid-bge | 6/6 | 15/22 (68%) | 15/22 (68%) | 18/22 (82%) | 18/22 (82%) | 2/11 (18%) | answer_semantic_miss:2, none:2, retrieval_miss:2 | PASSTHROUGH:6 | NONE:6 | 9591 |
| 04-l3-rewrite-no-profile | 6/6 | 16/22 (73%) | 16/22 (73%) | 21/22 (95%) | 21/22 (95%) | 5/11 (45%) | answer_semantic_miss:2, none:3, retrieval_miss:1 | LLM_MULTI_QUERY:6 | NONE:6 | 14692 |
| 05-l4-profile-no-salience | 6/6 | 16/22 (73%) | 16/22 (73%) | 21/22 (95%) | 21/22 (95%) | 2/11 (18%) | answer_semantic_miss:2, literal_only_mismatch:1, none:2, retrieval_miss:1 | LLM_MULTI_QUERY:6 | AUTO_PROFILE:6 | 16258 |
| 06-l5-current-best | 6/6 | 15/22 (68%) | 15/22 (68%) | 21/22 (95%) | 21/22 (95%) | 5/11 (45%) | answer_semantic_miss:2, none:3, retrieval_miss:1 | LLM_MULTI_QUERY:6 | AUTO_PROFILE:6 | 15367 |
| 07-a1-current-no-rerank | 6/6 | 18/22 (82%) | 18/22 (82%) | 21/22 (95%) | 21/22 (95%) | 7/11 (64%) | answer_semantic_miss:2, none:3, retrieval_miss:1 | LLM_MULTI_QUERY:6 | AUTO_PROFILE:6 | 14823 |
| 08-a2-current-no-rewrite | 6/6 | 14/22 (64%) | 14/22 (64%) | 18/22 (82%) | 18/22 (82%) | 2/11 (18%) | answer_semantic_miss:2, none:2, retrieval_miss:2 | PASSTHROUGH:6 | NONE:6 | 8812 |
| 09-a3-current-no-profile | 6/6 | 13/22 (59%) | 13/22 (59%) | 21/22 (95%) | 21/22 (95%) | 5/11 (45%) | answer_semantic_miss:3, none:2, retrieval_miss:1 | LLM_MULTI_QUERY:6 | NONE:6 | 15228 |
| 10-a4-current-no-salience | 6/6 | 9/22 (41%) | 9/22 (41%) | 21/22 (95%) | 21/22 (95%) | 5/11 (45%) | answer_semantic_miss:3, none:2, retrieval_miss:1 | LLM_MULTI_QUERY:6 | AUTO_PROFILE:6 | 18840 |

## Case Matrix

| case | L0 vector | L1 hybrid | L2 hybrid+BGE | L3 rewrite/no profile | L4 profile/no salience | L5 current | A1 no rerank | A2 no rewrite | A3 no profile | A4 no salience |
|---|---|---|---|---|---|---|---|---|---|---|
| RAG-10 | 2/5 retrieval_miss | 2/5 retrieval_miss | 2/5 retrieval_miss | 5/5 none | 5/5 literal_only_mismatch | 5/5 none | 5/5 none | 2/5 retrieval_miss | 5/5 none | 5/5 none |
| RAG-14 | 1/1 none | 1/1 none | 1/1 none | 1/1 none | 1/1 none | 1/1 none | 1/1 none | 1/1 none | 1/1 none | 1/1 none |
| EXT-ARTEMIS-01 | 0/3 answer_semantic_miss | 1/3 answer_semantic_miss | 0/3 answer_semantic_miss | 1/3 answer_semantic_miss | 0/3 answer_semantic_miss | 0/3 answer_semantic_miss | 1/3 answer_semantic_miss | 1/3 answer_semantic_miss | 1/3 answer_semantic_miss | 0/3 answer_semantic_miss |
| EXT-RFC9110-03 | 1/3 answer_semantic_miss | 3/3 literal_only_mismatch | 2/3 answer_semantic_miss | 1/3 answer_semantic_miss | 0/3 answer_semantic_miss | 1/3 answer_semantic_miss | 2/3 answer_semantic_miss | 2/3 answer_semantic_miss | 2/3 answer_semantic_miss | 0/3 answer_semantic_miss |
| EXT-K8S-PV-03 | 5/5 none | 5/5 none | 5/5 none | 5/5 none | 5/5 none | 5/5 none | 5/5 none | 5/5 none | 0/5 answer_semantic_miss | 0/5 answer_semantic_miss |
| EXT-PG-04 | 3/5 retrieval_miss | 4/5 retrieval_miss | 5/5 retrieval_miss | 3/5 retrieval_miss | 5/5 retrieval_miss | 3/5 retrieval_miss | 4/5 retrieval_miss | 3/5 retrieval_miss | 4/5 retrieval_miss | 3/5 retrieval_miss |

## Interpretation

1. Eval v2 is doing its job: `literal_smoke` and `semantic` now diverge visibly. Example: `EXT-RFC9110-03` under `L1 hybrid-only` has `semantic=3/3` but `literal=0/3`, so old literal scoring would have hidden a correct answer.

2. Query rewrite has clear evidence on `RAG-10`: `L2` stays `2/5`, while `L3` reaches `5/5`. It also raises candidate/final evidence coverage from `18/22` to `21/22` at aggregate level. But it does not universally improve answer wording on Artemis/RFC/PG in a single run.

3. Auto profile is confirmed in the runtime path (`AUTO_PROFILE:6` where enabled), but its net score lift is not stable on this 6-case set. `L3` and `L4` both score `16/22`; `L5` vs `A3 no profile` is `15/22` vs `13/22`, with benefits and regressions mixed by case.

4. Context salience has the strongest directional signal in this matrix: `L5 current` is `15/22`, while `A4 no salience` drops to `9/22`. The biggest difference is `EXT-K8S-PV-03`, which goes from `0/5` to `5/5`. This supports keeping salience as an answer-side evidence usage layer.

5. BGE rerank is not proven as a net scorer on this small set. `A1 no rerank` is `18/22`, higher than `L5` at `15/22`, while both have `candidate=21/22` and `final=21/22`. This should not be read as "remove rerank"; it means rerank value needs larger samples, repeated runs, and per-case final-context inspection.

6. PostgreSQL version-conflict remains stability-sensitive. All runs keep `failure_layer=retrieval_miss` because v2 evidence still sees only `3/5` to `5/5` required version-conflict evidence. This case should use N=3 / majority before module-level conclusions.

## Follow-up

- Refresh workbench so this matrix appears in Dashboard / Runs / Matrix.
- Update `PLAN.md` and Obsidian notes with the corrected Step 8.5 result and the matcher correction.
- Next large test should expand v2 rubric coverage beyond 6 cases before publishing module lift as a headline metric.
