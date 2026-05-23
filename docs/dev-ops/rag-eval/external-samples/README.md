# RAG External Sample Pack

Purpose: provide local, categorized RAG test materials and matching case definitions. This pack is not uploaded automatically and does not modify the active `../cases.json`.

Created: 2026-05-23

## Layout

```text
external-samples/
├── README.md
├── corpus/
│   ├── ai-governance/
│   │   └── nist-ai-rmf-1-0.md
│   ├── database/
│   │   └── postgresql-generated-columns-version-contrast.md
│   ├── kubernetes/
│   │   ├── kubernetes-persistent-volumes.md
│   │   └── kubernetes-rolling-update.md
│   ├── nasa/
│   │   ├── apollo-13-mission-details.md
│   │   └── artemis-i-mission-timeline.md
│   ├── protocols/
│   │   └── rfc-9110-http-semantics.md
│   └── security/
│       └── owasp-top10-a01-a03-a05.md
└── cases/
    └── external-sample-cases.json
```

## Scope

The corpus files are compact offline samples prepared for RAG testing from official public sources. They are not full-page mirrors. They preserve the facts needed by the matching cases and use stable section headings so retrieval failures can be diagnosed by `expected_source_section`.

## How To Use

1. Upload selected files under `corpus/` to the target knowledge base.
2. Copy selected cases from `cases/external-sample-cases.json` into a temporary eval file or into `../cases.json` after manual review.
3. Run a narrow subset first. Suggested first pass:
   - `EXT-ARTEMIS-01`
   - `EXT-RFC9110-03`
   - `EXT-K8S-PV-03`
   - `EXT-OWASP-03`
   - `EXT-PG-04`
4. Inspect retrieved chunks before judging answer quality. A failed answer can come from candidate coverage, rerank filtering, context salience, or literal mismatch.

## Source Groups

- NASA: timeline, long narrative, exact date and mission-event retrieval.
- Protocols: standards-style exact semantics and negative framework-boundary questions.
- Kubernetes: operational docs with procedures, feature states, and unrelated technical distractors.
- Security: close concept classification across OWASP categories.
- AI governance: policy framework and companion-resource retrieval.
- Database: version-aware conflict handling across PostgreSQL 16 and 18.

## Boundary

This pack intentionally does not include CDC, NOAA, Chinese legal texts, SEC filings, HotpotQA, MultiHop-RAG, QASPER, or Natural Questions yet. Those remain candidates in `../source-corpus-candidates.md` and can be added as a second sample pack after this first batch is verified.
