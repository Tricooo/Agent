from __future__ import annotations

import sqlite3
from pathlib import Path
from typing import Any, Optional

from fastapi import FastAPI, HTTPException, Query
from fastapi.responses import FileResponse
from fastapi.staticfiles import StaticFiles

from workbench import db
from workbench.indexer import reindex as run_reindex
from workbench.paths import DB_PATH, STATIC_DIR


app = FastAPI(title="RAG Eval Experiment Workbench")
app.mount("/static", StaticFiles(directory=STATIC_DIR), name="static")


FAILURE_MODE_LABELS: dict[str, str] = {
    "pass": "answer passed",
    "manual_review_or_unscored": "manual review / unscored",
    "incomplete_or_error": "run incomplete or errored",
    "rerank_runtime_failure": "rerank runtime failure",
    "answer_generation_or_literal_mismatch": "evidence in final context, answer/literal mismatch",
    "literal_only_mismatch": "semantic/evidence ok; literal smoke mismatch",
    "answer_semantic_miss": "final context has evidence; answer semantic miss",
    "runtime_failure": "runtime failure",
    "retrieval_miss": "candidate evidence missing",
    "rerank_or_context_loss": "candidate evidence lost before final context",
    "false_refusal": "should answer but refused",
    "false_answer": "should refuse but answered",
    "manual_review_needed": "manual review needed",
    "none": "pass",
    "candidate_to_final_loss": "candidate evidence lost before final context",
    "final_context_missing_expected_points": "final context missing expected points",
    "section_not_in_final_context": "expected section not in final context",
    "source_not_in_final_context": "expected source not in final context",
    "retrieval_or_filter_missing": "retrieval/filter likely missing source",
    "answer_or_literal_failure": "answer failed literal/manual signal",
    "unknown": "unknown",
}


def open_db() -> sqlite3.Connection:
    conn = db.connect(DB_PATH)
    db.init_db(conn)
    return conn


def add_filter(filters: list[str], params: list[Any], column: str, value: str | None) -> None:
    if value:
        filters.append(f"{column} = ?")
        params.append(value)


def query_rows(conn: sqlite3.Connection, sql: str, params: list[Any] | tuple[Any, ...] = ()) -> list[dict[str, Any]]:
    return db.decode_rows(conn.execute(sql, params).fetchall())


def query_one(conn: sqlite3.Connection, sql: str, params: list[Any] | tuple[Any, ...] = ()) -> dict[str, Any] | None:
    return db.decode_row(conn.execute(sql, params).fetchone())


def hydrate_run_metrics(conn: sqlite3.Connection, run: dict[str, Any]) -> dict[str, Any]:
    stats = query_one(
        conn,
        """
        SELECT
          COUNT(*) AS result_count,
          SUM(CASE WHEN completed = 1 THEN 1 ELSE 0 END) AS completed_count,
          AVG(CASE WHEN duration_ms IS NOT NULL THEN duration_ms END) AS avg_duration_ms,
          SUM(CASE WHEN rerank_runtime_failure_reason IS NOT NULL AND rerank_runtime_failure_reason != '' THEN 1 ELSE 0 END) AS rerank_failure_count,
          SUM(CASE WHEN profile_source IS NOT NULL AND profile_source != '' AND profile_source != '—' THEN 1 ELSE 0 END) AS profile_usage_count,
          AVG(CASE WHEN literal_ratio IS NOT NULL THEN literal_ratio END) AS literal_pass_rate,
          AVG(CASE WHEN semantic_ratio IS NOT NULL THEN semantic_ratio END) AS semantic_pass_rate,
          AVG(CASE WHEN answer_completeness_ratio IS NOT NULL THEN answer_completeness_ratio END) AS answer_completeness_rate
        FROM case_results
        WHERE run_id = ?
        """,
        (run["run_id"],),
    ) or {}
    run.update(stats)
    run.update(derive_run_shape(conn, run))
    return run


def classify_failure_mode(row: dict[str, Any] | None) -> str | None:
    if row is None:
        return None
    if row.get("failure_layer"):
        return row.get("failure_layer")
    if row.get("completed") is False:
        return "incomplete_or_error"
    if row.get("rerank_runtime_failure_reason"):
        return "rerank_runtime_failure"

    label = str(row.get("source_coverage_label") or "").lower()
    if "answer_generation_or_literal_mismatch" in label or "literal_mismatch" in label:
        return "answer_generation_or_literal_mismatch"
    if "lost_before_final" in label or "pre_only" in label:
        return "candidate_to_final_loss"
    if "retrieval" in label and ("missing" in label or "not" in label):
        return "retrieval_or_filter_missing"

    final_hit = row.get("expected_points_final_hit_count")
    final_total = row.get("expected_points_final_total")
    literal_ratio = row.get("literal_ratio")
    if final_hit is not None and final_total:
        if final_hit >= final_total:
            if literal_ratio is not None and literal_ratio < 1:
                return "answer_generation_or_literal_mismatch"
        else:
            return "final_context_missing_expected_points"

    if row.get("expected_section_in_final_context") is False:
        return "section_not_in_final_context"
    if row.get("source_file_in_final_context") is False:
        return "source_not_in_final_context"
    if literal_ratio is not None:
        if literal_ratio >= 1:
            return "pass"
        return "answer_or_literal_failure"
    if row.get("completed") is True:
        return "manual_review_or_unscored"
    return "unknown"


def enrich_result(row: dict[str, Any] | None) -> dict[str, Any] | None:
    if row is None:
        return None
    row["failure_mode"] = classify_failure_mode(row)
    row["failure_mode_label"] = FAILURE_MODE_LABELS.get(row["failure_mode"] or "unknown", row["failure_mode"])
    return row


def result_case_ids(conn: sqlite3.Connection, run_id: str) -> set[str]:
    return {
        str(row["case_id"])
        for row in conn.execute("SELECT case_id FROM case_results WHERE run_id = ?", (run_id,)).fetchall()
    }


def derive_run_shape(conn: sqlite3.Connection, run: dict[str, Any]) -> dict[str, Any]:
    rows = query_rows(
        conn,
        """
        SELECT c.origin, c.family, c.score_mode, cr.rerank_mode, cr.profile_source,
               cr.query_rewrite_mode, cr.context_salience_expansions
        FROM case_results cr
        JOIN cases c ON c.case_id = cr.case_id
        WHERE cr.run_id = ?
        """,
        (run["run_id"],),
    )
    result_count = len(rows)
    origins = sorted({row.get("origin") or "unknown" for row in rows})
    families = sorted({row.get("family") or "unknown" for row in rows})
    score_modes = sorted({row.get("score_mode") or "unknown" for row in rows})
    run_text = " ".join(
        str(run.get(key) or "").lower() for key in ("run_id", "group_name", "file_path", "config_hint")
    )

    if result_count <= 0:
        scope = "empty"
    elif result_count == 1 or "probe" in run_text or "retry" in run_text:
        scope = "single-case"
    elif origins == ["internal"] and result_count >= 14:
        scope = "internal-full"
    elif origins == ["external"] and result_count >= 31:
        scope = "external-full"
    elif origins == ["external"] and result_count >= 8:
        scope = "external-narrow"
    elif result_count <= 4:
        scope = "narrow"
    else:
        scope = "partial"

    rerank_modes = sorted({row.get("rerank_mode") for row in rows if row.get("rerank_mode")})
    profile_sources = sorted({
        row.get("profile_source")
        for row in rows
        if row.get("profile_source") and row.get("profile_source") not in {"—", "NONE", "NO_PROFILE_MATCH"}
    })
    rewrite_modes = sorted({row.get("query_rewrite_mode") for row in rows if row.get("query_rewrite_mode")})

    if "no-rerank" in run_text or "passthrough" in run_text or "disabled" in run_text:
        rerank_flag = "off"
    elif rerank_modes and all(mode == "PASSTHROUGH" for mode in rerank_modes):
        rerank_flag = "off"
    elif any("BGE" in mode or "RERANK" in mode or "LOCAL" in mode for mode in rerank_modes):
        rerank_flag = "on"
    elif rerank_modes:
        rerank_flag = "configured"
    else:
        rerank_flag = "unknown"

    if "no-profile" in run_text:
        profile_flag = "off"
    elif "step6.9" in run_text or "hybrid-v2" in run_text:
        profile_flag = "llm"
    elif "step6.8" in run_text or profile_sources:
        profile_flag = "auto"
    else:
        profile_flag = "unknown"

    if "no-salience" in run_text:
        salience_flag = "off"
    elif "salience" in run_text or any((row.get("context_salience_expansions") or 0) > 0 for row in rows):
        salience_flag = "on"
    else:
        salience_flag = "unknown"

    if "rewrite-no-profile" in run_text or "llm" in run_text or "multi-query" in run_text:
        rewrite_flag = "on"
    elif "l0-vector-only" in run_text or "l1-hybrid-only" in run_text or "l2-hybrid-bge" in run_text:
        rewrite_flag = "off"
    elif rewrite_modes:
        rewrite_flag = "on"
    else:
        rewrite_flag = "unknown"

    feature_flags = {
        "rerank": rerank_flag,
        "rewrite": rewrite_flag,
        "profile": profile_flag,
        "salience": salience_flag,
    }
    feature_label = " · ".join(f"{key}:{value}" for key, value in feature_flags.items() if value != "unknown")

    return {
        "case_scope": scope,
        "origin_mix": origins,
        "family_mix": families,
        "score_mode_mix": score_modes,
        "feature_flags": feature_flags,
        "feature_label": feature_label,
    }


def list_runs_data(conn: sqlite3.Connection, group: str | None = None, agent_id: str | None = None) -> list[dict[str, Any]]:
    filters: list[str] = []
    params: list[Any] = []
    add_filter(filters, params, "group_name", group)
    add_filter(filters, params, "agent_id", agent_id)
    where = f"WHERE {' AND '.join(filters)}" if filters else ""
    runs = query_rows(
        conn,
        f"""
        SELECT * FROM runs
        {where}
        ORDER BY COALESCE(generated_at, '') DESC, run_id DESC
        """,
        params,
    )
    return [hydrate_run_metrics(conn, run) for run in runs]


def list_cases_data(
    conn: sqlite3.Connection,
    origin: str | None = None,
    source_file: str | None = None,
    case_type: str | None = None,
    score_mode: str | None = None,
    family: str | None = None,
) -> list[dict[str, Any]]:
    filters: list[str] = []
    params: list[Any] = []
    add_filter(filters, params, "origin", origin)
    add_filter(filters, params, "source_file", source_file)
    add_filter(filters, params, "case_type", case_type)
    add_filter(filters, params, "score_mode", score_mode)
    add_filter(filters, params, "family", family)
    where = f"WHERE {' AND '.join(filters)}" if filters else ""
    return query_rows(
        conn,
        f"""
        SELECT * FROM cases
        {where}
        ORDER BY
          CASE origin WHEN 'internal' THEN 0 WHEN 'external' THEN 1 ELSE 2 END,
          family,
          case_id
        """,
        params,
    )


def get_result_data(conn: sqlite3.Connection, run_id: str, case_id: str) -> dict[str, Any]:
    result = query_one(
        conn,
        """
        SELECT cr.*, c.question, c.case_type, c.score_mode, c.source_file,
               c.expected_source_section, c.expected_points_json, c.expected_points_v2_json,
               c.origin, c.family,
               r.file_path, r.group_name, r.generated_at, r.agent_id, r.config_hint
        FROM case_results cr
        JOIN cases c ON c.case_id = cr.case_id
        JOIN runs r ON r.run_id = cr.run_id
        WHERE cr.run_id = ? AND cr.case_id = ?
        """,
        (run_id, case_id),
    )
    if not result:
        raise HTTPException(status_code=404, detail="result not found")
    enrich_result(result)
    result["documents"] = {
        "pre_rerank": query_rows(
            conn,
            "SELECT * FROM document_hits WHERE run_id = ? AND case_id = ? AND stage = 'pre_rerank' ORDER BY rank",
            (run_id, case_id),
        ),
        "final": query_rows(
            conn,
            "SELECT * FROM document_hits WHERE run_id = ? AND case_id = ? AND stage = 'final' ORDER BY rank",
            (run_id, case_id),
        ),
    }
    return result


def compare_warnings(conn: sqlite3.Connection, run_a: dict[str, Any], run_b: dict[str, Any]) -> list[dict[str, Any]]:
    ids_a = result_case_ids(conn, run_a["run_id"])
    ids_b = result_case_ids(conn, run_b["run_id"])
    shared = ids_a & ids_b
    warnings: list[dict[str, Any]] = []
    if ids_a != ids_b:
        warnings.append(
            {
                "level": "warn",
                "code": "case_set_mismatch",
                "message": "Run A and Run B do not cover the same case set; one-sided cases are shown as shifts, not clean ablation wins.",
                "a_only_count": len(ids_a - ids_b),
                "b_only_count": len(ids_b - ids_a),
                "shared_count": len(shared),
            }
        )
    if run_a.get("case_scope") != run_b.get("case_scope"):
        warnings.append(
            {
                "level": "warn",
                "code": "scope_mismatch",
                "message": "Run scopes differ; avoid reading this as a clean ablation.",
                "run_a_scope": run_a.get("case_scope"),
                "run_b_scope": run_b.get("case_scope"),
            }
        )
    if run_a.get("origin_mix") != run_b.get("origin_mix"):
        warnings.append(
            {
                "level": "info",
                "code": "origin_mismatch",
                "message": "Runs contain different case origins.",
                "run_a_origin": run_a.get("origin_mix"),
                "run_b_origin": run_b.get("origin_mix"),
            }
        )
    if len(shared) == 0:
        warnings.append(
            {
                "level": "bad",
                "code": "no_overlap",
                "message": "Runs have no overlapping cases.",
            }
        )
    return warnings


@app.on_event("startup")
def startup_index_if_empty() -> None:
    with open_db() as conn:
        if db.count_rows(conn, "runs") == 0:
            run_reindex(conn)


@app.get("/")
def index() -> FileResponse:
    return FileResponse(STATIC_DIR / "index.html")


@app.get("/api/health")
def health() -> dict[str, Any]:
    with open_db() as conn:
        return {"ok": True, "db_path": str(DB_PATH), "counts": db.counts(conn)}


@app.post("/api/reindex")
def reindex() -> dict[str, Any]:
    with open_db() as conn:
        return {"ok": True, "counts": run_reindex(conn)}


@app.get("/api/runs")
def runs(group: Optional[str] = None, agent_id: Optional[str] = None) -> dict[str, Any]:
    with open_db() as conn:
        return {
            "runs": list_runs_data(conn, group, agent_id),
            "groups": query_rows(conn, "SELECT group_name, COUNT(*) AS run_count FROM runs GROUP BY group_name ORDER BY group_name"),
            "agents": query_rows(conn, "SELECT agent_id, COUNT(*) AS run_count FROM runs GROUP BY agent_id ORDER BY agent_id"),
        }


@app.get("/api/runs/{run_id}")
def run_detail(run_id: str) -> dict[str, Any]:
    with open_db() as conn:
        run = query_one(conn, "SELECT * FROM runs WHERE run_id = ?", (run_id,))
        if not run:
            raise HTTPException(status_code=404, detail="run not found")
        run = hydrate_run_metrics(conn, run)
        run["results"] = query_rows(
            conn,
            """
            SELECT cr.case_id, c.origin, c.family, c.case_type, c.score_mode, cr.completed,
                   cr.health, cr.failure_layer, cr.literal_hit, cr.literal_ratio,
                   cr.semantic_score, cr.semantic_ratio, cr.answer_completeness,
                   cr.answer_completeness_ratio, cr.answerability,
                   cr.candidate_recall, cr.final_context_recall,
                   cr.retrieved, cr.score_max, cr.empty,
                   cr.duration_ms, cr.source_coverage_label, cr.rerank_runtime_failure_reason,
                   cr.source_file_in_final_context, cr.expected_section_in_final_context,
                   cr.expected_points_final_hit_count, cr.expected_points_final_total
            FROM case_results cr
            JOIN cases c ON c.case_id = cr.case_id
            WHERE cr.run_id = ?
            ORDER BY c.origin, c.family, cr.case_id
            """,
            (run_id,),
        )
        run["results"] = [enrich_result(row) for row in run["results"]]
        return run


@app.get("/api/cases")
def cases(
    origin: Optional[str] = None,
    source_file: Optional[str] = None,
    case_type: Optional[str] = None,
    score_mode: Optional[str] = None,
    family: Optional[str] = None,
) -> dict[str, Any]:
    with open_db() as conn:
        return {
            "cases": list_cases_data(conn, origin, source_file, case_type, score_mode, family),
            "origins": query_rows(conn, "SELECT origin, COUNT(*) AS case_count FROM cases GROUP BY origin ORDER BY origin"),
            "families": query_rows(conn, "SELECT family, COUNT(*) AS case_count FROM cases GROUP BY family ORDER BY family"),
            "source_files": query_rows(
                conn,
                "SELECT source_file, COUNT(*) AS case_count FROM cases WHERE source_file IS NOT NULL GROUP BY source_file ORDER BY source_file",
            ),
        }


@app.get("/api/matrix")
def matrix(
    origin: Optional[str] = None,
    source_file: Optional[str] = None,
    case_type: Optional[str] = None,
    score_mode: Optional[str] = None,
    family: Optional[str] = None,
    group: Optional[str] = None,
    agent_id: Optional[str] = None,
) -> dict[str, Any]:
    with open_db() as conn:
        selected_cases = list_cases_data(conn, origin, source_file, case_type, score_mode, family)
        selected_runs = list_runs_data(conn, group, agent_id)
        case_ids = [item["case_id"] for item in selected_cases]
        run_ids = [item["run_id"] for item in selected_runs]
        cells: dict[tuple[str, str], dict[str, Any]] = {}
        if case_ids and run_ids:
            case_marks = ",".join(["?"] * len(case_ids))
            run_marks = ",".join(["?"] * len(run_ids))
            rows = query_rows(
                conn,
                f"""
                SELECT run_id, case_id, completed, health, failure_layer,
                       literal_hit, literal_ratio, semantic_score, semantic_ratio,
                       answer_completeness, answer_completeness_ratio, answerability,
                       candidate_recall, final_context_recall, retrieved, score_max, empty,
                       source_coverage_label, duration_ms, rerank_mode, profile_source,
                       rerank_runtime_failure_reason, source_file_in_final_context,
                       expected_section_in_final_context, expected_points_final_hit_count,
                       expected_points_final_total
                FROM case_results
                WHERE case_id IN ({case_marks}) AND run_id IN ({run_marks})
                """,
                case_ids + run_ids,
            )
            rows = [enrich_result(row) for row in rows]
            cells = {(row["case_id"], row["run_id"]): row for row in rows}
        matrix_rows = [
            {
                "case": case,
                "cells": [cells.get((case["case_id"], run["run_id"])) for run in selected_runs],
            }
            for case in selected_cases
        ]
        return {"runs": selected_runs, "cases": selected_cases, "rows": matrix_rows}


@app.get("/api/results/{run_id}/{case_id}")
def result(run_id: str, case_id: str) -> dict[str, Any]:
    with open_db() as conn:
        return get_result_data(conn, run_id, case_id)


@app.get("/api/compare")
def compare(run_a: str = Query(...), run_b: str = Query(...)) -> dict[str, Any]:
    with open_db() as conn:
        a = query_one(conn, "SELECT * FROM runs WHERE run_id = ?", (run_a,))
        b = query_one(conn, "SELECT * FROM runs WHERE run_id = ?", (run_b,))
        if not a or not b:
            raise HTTPException(status_code=404, detail="run_a or run_b not found")
        a = hydrate_run_metrics(conn, a)
        b = hydrate_run_metrics(conn, b)
        case_rows = query_rows(conn, "SELECT * FROM cases ORDER BY origin, family, case_id")
        rows = []
        for case in case_rows:
            ra = query_one(conn, "SELECT * FROM case_results WHERE run_id = ? AND case_id = ?", (run_a, case["case_id"]))
            rb = query_one(conn, "SELECT * FROM case_results WHERE run_id = ? AND case_id = ?", (run_b, case["case_id"]))
            ra = enrich_result(ra)
            rb = enrich_result(rb)
            if not ra and not rb:
                continue
            rows.append(
                {
                    "case": case,
                    "a": ra,
                    "b": rb,
                    "delta": {
                        "literal_ratio": None
                        if not (ra and rb and ra.get("literal_ratio") is not None and rb.get("literal_ratio") is not None)
                        else rb["literal_ratio"] - ra["literal_ratio"],
                        "semantic_ratio": None
                        if not (ra and rb and ra.get("semantic_ratio") is not None and rb.get("semantic_ratio") is not None)
                        else rb["semantic_ratio"] - ra["semantic_ratio"],
                        "duration_ms": None
                        if not (ra and rb and ra.get("duration_ms") is not None and rb.get("duration_ms") is not None)
                        else rb["duration_ms"] - ra["duration_ms"],
                        "coverage_changed": None
                        if not (ra and rb)
                        else ra.get("source_coverage_label") != rb.get("source_coverage_label"),
                    },
                }
            )
        return {"run_a": a, "run_b": b, "warnings": compare_warnings(conn, a, b), "rows": rows}


@app.get("/api/insights")
def insights() -> dict[str, Any]:
    with open_db() as conn:
        run_summaries = []
        for run in list_runs_data(conn):
            coverage = query_rows(
                conn,
                """
                SELECT COALESCE(source_coverage_label, 'unknown') AS label, COUNT(*) AS count
                FROM case_results
                WHERE run_id = ?
                GROUP BY COALESCE(source_coverage_label, 'unknown')
                ORDER BY count DESC
                """,
                (run["run_id"],),
            )
            run["source_coverage_distribution"] = coverage
            failure_counts: dict[str, int] = {}
            result_rows = query_rows(conn, "SELECT * FROM case_results WHERE run_id = ?", (run["run_id"],))
            for row in result_rows:
                mode = classify_failure_mode(row) or "unknown"
                failure_counts[mode] = failure_counts.get(mode, 0) + 1
            run["failure_mode_distribution"] = [
                {
                    "mode": mode,
                    "label": FAILURE_MODE_LABELS.get(mode, mode),
                    "count": count,
                }
                for mode, count in sorted(failure_counts.items(), key=lambda item: item[1], reverse=True)
            ]
            run["dominant_failure_mode"] = (
                run["failure_mode_distribution"][0]["mode"] if run["failure_mode_distribution"] else None
            )
            run["dominant_failure_label"] = (
                run["failure_mode_distribution"][0]["label"] if run["failure_mode_distribution"] else None
            )
            run_summaries.append(run)

        case_summaries = query_rows(
            conn,
            """
            SELECT c.case_id, c.origin, c.family, c.case_type, c.score_mode,
                   CASE WHEN c.expected_points_v2_json IS NOT NULL AND c.expected_points_v2_json != '[]' THEN 1 ELSE 0 END AS has_v2_rubric,
                   COUNT(cr.run_id) AS run_count,
                   SUM(CASE WHEN cr.completed = 0 THEN 1 ELSE 0 END) AS incomplete_count,
                   AVG(CASE WHEN cr.semantic_ratio IS NOT NULL THEN cr.semantic_ratio END) AS semantic_pass_rate,
                   AVG(CASE WHEN cr.answer_completeness_ratio IS NOT NULL THEN cr.answer_completeness_ratio END) AS answer_completeness_rate,
                   SUM(CASE WHEN cr.literal_ratio IS NOT NULL AND cr.literal_ratio < 1 THEN 1 ELSE 0 END) AS literal_failure_count,
                   SUM(CASE WHEN cr.source_coverage_label = 'answer_generation_or_literal_mismatch' THEN 1 ELSE 0 END) AS literal_mismatch_count,
                   COUNT(DISTINCT COALESCE(cr.source_coverage_label, 'unknown')) AS coverage_volatility
            FROM cases c
            LEFT JOIN case_results cr ON cr.case_id = c.case_id
            GROUP BY c.case_id
            ORDER BY literal_failure_count DESC, coverage_volatility DESC, c.case_id
            """
        )
        for case in case_summaries:
            rows = query_rows(conn, "SELECT * FROM case_results WHERE case_id = ?", (case["case_id"],))
            counts: dict[str, int] = {}
            for row in rows:
                mode = classify_failure_mode(row) or "unknown"
                counts[mode] = counts.get(mode, 0) + 1
            if counts:
                dominant = max(counts.items(), key=lambda item: item[1])[0]
                case["dominant_failure_mode"] = dominant
                case["dominant_failure_label"] = FAILURE_MODE_LABELS.get(dominant, dominant)
            else:
                case["dominant_failure_mode"] = None
                case["dominant_failure_label"] = None
        return {"runs": run_summaries, "cases": case_summaries}


def find_run_for_preset(runs_data: list[dict[str, Any]], include: list[str], exclude: list[str] | None = None) -> dict[str, Any] | None:
    exclude = exclude or []
    for run in runs_data:
        haystack = " ".join(
            str(run.get(key) or "").lower() for key in ("run_id", "group_name", "file_path", "config_hint")
        )
        if all(token in haystack for token in include) and not any(token in haystack for token in exclude):
            return run
    return None


@app.get("/api/presets")
def presets() -> dict[str, Any]:
    with open_db() as conn:
        runs_data = list_runs_data(conn)
        definitions = [
            ("L0 vector only vs L5 current best", ["l0-vector-only"], ["l5-current-best"], [], []),
            ("no rerank vs BGE", ["no-rerank"], ["l5-current-best"], ["retry"], []),
            ("no profile vs profile", ["no-profile"], ["l4-rewrite-profile"], [], []),
            ("rule profile vs LLM profile", ["step6.8"], ["step6.9"], [], []),
            ("no salience vs salience", ["no-salience"], ["l5-current-best"], ["retry", "source-coverage"], []),
            ("Step 6.8 vs Step 6.9", ["step6.8"], ["step6.9"], [], []),
        ]
        output = []
        for name, left, right, left_exclude, right_exclude in definitions:
            run_a = find_run_for_preset(runs_data, left, left_exclude)
            run_b = find_run_for_preset(runs_data, right, right_exclude)
            output.append(
                {
                    "name": name,
                    "run_a": run_a["run_id"] if run_a else None,
                    "run_b": run_b["run_id"] if run_b else None,
                    "available": bool(run_a and run_b),
                    "reason": None if run_a and run_b else f"missing {'/'.join(left if not run_a else right)}",
                }
            )
        return {"presets": output}
