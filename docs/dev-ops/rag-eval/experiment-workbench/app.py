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
          AVG(CASE WHEN literal_ratio IS NOT NULL THEN literal_ratio END) AS literal_pass_rate
        FROM case_results
        WHERE run_id = ?
        """,
        (run["run_id"],),
    ) or {}
    run.update(stats)
    return run


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
               c.expected_source_section, c.expected_points_json, c.origin, c.family,
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
                   cr.literal_hit, cr.literal_ratio, cr.retrieved, cr.score_max, cr.empty,
                   cr.duration_ms, cr.source_coverage_label
            FROM case_results cr
            JOIN cases c ON c.case_id = cr.case_id
            WHERE cr.run_id = ?
            ORDER BY c.origin, c.family, cr.case_id
            """,
            (run_id,),
        )
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
                SELECT run_id, case_id, completed, literal_hit, literal_ratio, retrieved, score_max, empty,
                       source_coverage_label, duration_ms, rerank_mode, profile_source
                FROM case_results
                WHERE case_id IN ({case_marks}) AND run_id IN ({run_marks})
                """,
                case_ids + run_ids,
            )
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
        case_rows = query_rows(conn, "SELECT * FROM cases ORDER BY origin, family, case_id")
        rows = []
        for case in case_rows:
            ra = query_one(conn, "SELECT * FROM case_results WHERE run_id = ? AND case_id = ?", (run_a, case["case_id"]))
            rb = query_one(conn, "SELECT * FROM case_results WHERE run_id = ? AND case_id = ?", (run_b, case["case_id"]))
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
                        "duration_ms": None
                        if not (ra and rb and ra.get("duration_ms") is not None and rb.get("duration_ms") is not None)
                        else rb["duration_ms"] - ra["duration_ms"],
                        "coverage_changed": None
                        if not (ra and rb)
                        else ra.get("source_coverage_label") != rb.get("source_coverage_label"),
                    },
                }
            )
        return {"run_a": hydrate_run_metrics(conn, a), "run_b": hydrate_run_metrics(conn, b), "rows": rows}


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
            run_summaries.append(run)

        case_summaries = query_rows(
            conn,
            """
            SELECT c.case_id, c.origin, c.family, c.case_type, c.score_mode,
                   COUNT(cr.run_id) AS run_count,
                   SUM(CASE WHEN cr.completed = 0 THEN 1 ELSE 0 END) AS incomplete_count,
                   SUM(CASE WHEN cr.literal_ratio IS NOT NULL AND cr.literal_ratio < 1 THEN 1 ELSE 0 END) AS literal_failure_count,
                   SUM(CASE WHEN cr.source_coverage_label = 'answer_generation_or_literal_mismatch' THEN 1 ELSE 0 END) AS literal_mismatch_count,
                   COUNT(DISTINCT COALESCE(cr.source_coverage_label, 'unknown')) AS coverage_volatility
            FROM cases c
            LEFT JOIN case_results cr ON cr.case_id = c.case_id
            GROUP BY c.case_id
            ORDER BY literal_failure_count DESC, coverage_volatility DESC, c.case_id
            """
        )
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
            ("L0 vector only vs L5 current best", ["l0-vector-only"], ["l5-current-best"]),
            ("no rerank vs BGE", ["no-rerank"], ["l5-current-best"]),
            ("no profile vs profile", ["no-profile"], ["l4-rewrite-profile"]),
            ("rule profile vs LLM profile", ["step6.8"], ["step6.9"]),
            ("no salience vs salience", ["no-salience"], ["l5-current-best"]),
            ("Step 6.8 vs Step 6.9", ["step6.8"], ["step6.9"]),
        ]
        output = []
        for name, left, right in definitions:
            run_a = find_run_for_preset(runs_data, left)
            run_b = find_run_for_preset(runs_data, right)
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
