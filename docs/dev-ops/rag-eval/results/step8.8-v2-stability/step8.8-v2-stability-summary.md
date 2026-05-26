# Step 8.8 v2 Stability Summary

- Date: 2026-05-26
- Agent: `rag_profile_v2_demo`
- Cases: 12 external v2 rubric cases
- Runs: N=3 current-best repeated-run

## Reports

- `rag-eval-result-step8.8-v2-stability-current-best-run1.md`
- `rag-eval-result-step8.8-v2-stability-current-best-run2.md`
- `rag-eval-result-step8.8-v2-stability-current-best-run3.md`

## Runtime Validity

- 36/36 case rows completed.
- 36/36 rows used `PER_VARIANT_RERANK_RRF`.
- 36/36 rows used `LLM_MULTI_QUERY`.
- 36/36 rows used `AUTO_PROFILE`.
- 36/36 rows had blank `rerank_runtime.failure_reason`.

This run is valid for current-best evaluation. It is not a stale-jar, BGE fallback, PASSTHROUGH, or SSE EOF artifact.

## Raw Report Scores

| run | semantic total | health | notable failure layers |
| --- | ---: | --- | --- |
| run1 | `34/35 = 97.1%` | ok=10 / refusal_issue=1 / fail=1 | false_answer=1, retrieval_miss=1 |
| run2 | `26/35 = 74.3%` | ok=8 / fail=4 | answer_semantic_miss=3, retrieval_miss=1 |
| run3 | `30/35 = 85.7%` | ok=8 / fail=4 | answer_semantic_miss=3, retrieval_miss=1 |

Raw average: `90/105 = 85.7%`.

## Stable Signals

- `EXT-ARTEMIS-01`, `EXT-ARTEMIS-02`, and `EXT-ARTEMIS-04` are stable across all three runs.
- `EXT-RFC9110-01` is stable semantically even when literal wording differs.
- `EXT-K8S-PV-04` stays a true refusal.
- `EXT-PG-02` stays semantically complete across all three runs.
- `EXT-PG-04` answers are semantically complete in all three runs, while evidence recall still flags `retrieval_miss`; this proves answer score and evidence recall must remain separate signals.

## Calibration Notes

The N=3 run exposed evaluation-layer false negatives:

- `EXT-K8S-PV-02`: answers included the Retain reclaim steps, but old `ordered_steps` could be thrown off by generic aliases and storage-asset wording.
- `EXT-K8S-PV-03`: answers included Retain -> delete PVC -> remove claimRef -> recreate smaller PVC -> restore reclaim policy, but old `ordered_steps` missed some Chinese variants.
- `EXT-RFC9110-04`: run1 was a correct refusal using "无法直接回答", but the global refusal template did not include that phrase.
- `EXT-OWASP-03`: answers expressed Broken Access Control as authorization-policy failure, but Chinese formatting around "授权策略" caused alias mismatch.

Applied calibration:

- `ordered_steps` now chooses the earliest match after the previous step, instead of independently taking each step's earliest global match.
- `ordered_steps` avoids ASCII token prefix/suffix false matches, for example `删除 PV` no longer matches `删除 PVC`.
- Refusal templates include `无法直接回答`.
- External v2 rubric aliases now include general Chinese variants for Retain reclaim, PVC expansion recovery, and authorization-policy wording.

## Interview Conclusion

This is enough to present the RAG optimization as an engineering closed loop:

- We have ingestion, chunking, hybrid retrieval, BGE rerank, LLM query rewrite, KnowledgeBaseProfile, final-context preservation, and v2 eval observability.
- The important maturity point is layer attribution: runtime, candidate recall, final context, answer semantic, literal smoke, and refusal are separated.
- Current-best is clearly more capable than raw vector search, but it is not a production-grade deterministic answer generator yet.
- The next hardening step is not more rewrite/profile tuning. It is repeated-run stability, calibrated deterministic rubric, and optionally LLM-judge/manual review for cases where deterministic matching is too brittle.
