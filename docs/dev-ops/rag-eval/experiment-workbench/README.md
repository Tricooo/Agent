# RAG Eval Experiment Workbench

独立的 RAG 实验分析台，用于把 `docs/dev-ops/rag-eval/results/**/*.md` 报告和 internal / external case registry 重新索引到 SQLite，并通过 FastAPI + 原生前端做筛选、矩阵、对比、下钻和汇总。

## Install

```bash
cd docs/dev-ops/rag-eval/experiment-workbench
python3 -m pip install -r requirements.txt
```

## Start

```bash
cd docs/dev-ops/rag-eval/experiment-workbench
python3 -m uvicorn app:app --host 127.0.0.1 --port 8777
```

Open `http://127.0.0.1:8777`.

## Reindex

From the UI, click `Reindex`.

From the terminal:

```bash
curl -X POST http://127.0.0.1:8777/api/reindex
```

The SQLite database is regenerated at `.workbench/rag_eval_workbench.sqlite`. It is ignored by git and can be rebuilt from markdown reports and case JSON files.

## Data Sources

- Reports: `docs/dev-ops/rag-eval/results/**/*.md`
- Internal cases: `docs/dev-ops/rag-eval/cases.json`
- External cases: `docs/dev-ops/rag-eval/external-samples/cases/*.json`

The workbench does not write back to `results/`, `cases.json`, or external case JSON files.

## API

- `GET /api/health`
- `POST /api/reindex`
- `GET /api/runs`
- `GET /api/cases`
- `GET /api/matrix`
- `GET /api/runs/{run_id}`
- `GET /api/results/{run_id}/{case_id}`
- `GET /api/compare?run_a=...&run_b=...`
- `GET /api/insights`
- `GET /api/presets`

`run_id` is the report path under `results/` without `.md`, with path separators replaced by `__`.

## Supported Fields

The parser is schema-tolerant and accepts both `RAG-*` and `EXT-*` case ids. Missing fields are stored as null/empty values rather than failing the whole run.

Currently indexed:

- Run header: generated time, API URL, agent id, group, file path, schema era, parse warnings.
- Case registry: question, case type, score mode, source file, expected section, expected points, origin, family.
- Case result: completed, literal hit/ratio, missing points, answer, retrieved count, score range, empty, duration.
- Attribution: rerank applied/mode/runtime, profile source/selected hints, rewrite variants, source coverage, context salience.
- Documents: pre-rerank and final document hits with rank, source, chunk, preview, scores, and metadata.

## Known Limits

- It does not replace the old static viewer; `docs/dev-ops/rag-eval/viewer/` remains independent.
- It does not run new RAG eval jobs or call the Java backend; it only indexes existing markdown reports.
- Semantic correctness is not judged automatically. `literal_hit` remains a smoke signal, and source coverage helps decide whether failures belong to retrieval, rerank, final context, answer generation, or literal mismatch.
- Very old reports with fields outside the known markdown shape may index partially with parse warnings.
