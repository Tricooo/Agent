from __future__ import annotations

import json
import sqlite3
from pathlib import Path
from typing import Any, Iterable


SCHEMA = """
PRAGMA journal_mode=WAL;

CREATE TABLE IF NOT EXISTS cases (
  case_id TEXT PRIMARY KEY,
  question TEXT,
  case_type TEXT,
  score_mode TEXT,
  source_file TEXT,
  expected_source_section TEXT,
  expected_points_json TEXT NOT NULL DEFAULT '[]',
  expected_points_v2_json TEXT NOT NULL DEFAULT '[]',
  origin TEXT NOT NULL,
  family TEXT,
  source_case_file TEXT
);

CREATE TABLE IF NOT EXISTS runs (
  run_id TEXT PRIMARY KEY,
  group_name TEXT,
  file_path TEXT NOT NULL,
  generated_at TEXT,
  agent_id TEXT,
  api_url TEXT,
  config_hint TEXT,
  schema_era TEXT,
  eval_schema_version TEXT,
  case_schema_version TEXT,
  rubric_coverage TEXT,
  case_count INTEGER NOT NULL DEFAULT 0,
  parse_warnings_json TEXT NOT NULL DEFAULT '[]'
);

CREATE TABLE IF NOT EXISTS case_results (
  run_id TEXT NOT NULL,
  case_id TEXT NOT NULL,
  completed INTEGER,
  health TEXT,
  failure_layer TEXT,
  verdict TEXT,
  result_valid_for_scoring INTEGER,
  literal_hit TEXT,
  literal_ratio REAL,
  semantic_score TEXT,
  semantic_ratio REAL,
  semantic_hit_count INTEGER,
  semantic_total INTEGER,
  answer_completeness TEXT,
  answer_completeness_ratio REAL,
  answerability TEXT,
  did_answer INTEGER,
  candidate_recall TEXT,
  final_context_recall TEXT,
  candidate_to_final_delta TEXT,
  final_to_answer_delta TEXT,
  hit_count INTEGER,
  expected_point_count INTEGER,
  missing_points_json TEXT NOT NULL DEFAULT '[]',
  answer TEXT,
  answer_preview TEXT,
  retrieved INTEGER,
  score_min REAL,
  score_max REAL,
  empty INTEGER,
  duration_ms INTEGER,
  rerank_applied INTEGER,
  rerank_mode TEXT,
  rerank_runtime_model TEXT,
  rerank_runtime_failure_reason TEXT,
  profile_version TEXT,
  profile_source TEXT,
  selected_profile_hints_json TEXT NOT NULL DEFAULT '[]',
  query_rewrite_mode TEXT,
  query_rewrite_failure_reason TEXT,
  rewrite_variants_json TEXT NOT NULL DEFAULT '[]',
  source_coverage_label TEXT,
  source_file_in_final_context INTEGER,
  expected_section_in_final_context INTEGER,
  expected_points_final_hit_count INTEGER,
  expected_points_final_total INTEGER,
  context_salience_cues_json TEXT NOT NULL DEFAULT '[]',
  context_salience_expansions INTEGER,
  semantic_hits_json TEXT NOT NULL DEFAULT '[]',
  semantic_misses_json TEXT NOT NULL DEFAULT '[]',
  evidence_source_unverified INTEGER,
  raw_json TEXT NOT NULL DEFAULT '{}',
  PRIMARY KEY (run_id, case_id),
  FOREIGN KEY (run_id) REFERENCES runs(run_id) ON DELETE CASCADE,
  FOREIGN KEY (case_id) REFERENCES cases(case_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS document_hits (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  run_id TEXT NOT NULL,
  case_id TEXT NOT NULL,
  stage TEXT NOT NULL,
  rank INTEGER,
  source_path TEXT,
  chunk_index INTEGER,
  text_excerpt TEXT,
  scores_json TEXT NOT NULL DEFAULT '{}',
  metadata_json TEXT NOT NULL DEFAULT '{}',
  FOREIGN KEY (run_id, case_id) REFERENCES case_results(run_id, case_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_runs_group ON runs(group_name);
CREATE INDEX IF NOT EXISTS idx_results_case ON case_results(case_id);
CREATE INDEX IF NOT EXISTS idx_results_coverage ON case_results(source_coverage_label);
CREATE INDEX IF NOT EXISTS idx_docs_result ON document_hits(run_id, case_id, stage);
"""


JSON_FIELDS = {
    "expected_points_json",
    "expected_points_v2_json",
    "parse_warnings_json",
    "missing_points_json",
    "selected_profile_hints_json",
    "rewrite_variants_json",
    "context_salience_cues_json",
    "semantic_hits_json",
    "semantic_misses_json",
    "raw_json",
    "scores_json",
    "metadata_json",
}

MIGRATION_COLUMNS = {
    "cases": {
        "expected_points_v2_json": "TEXT NOT NULL DEFAULT '[]'",
    },
    "runs": {
        "eval_schema_version": "TEXT",
        "case_schema_version": "TEXT",
        "rubric_coverage": "TEXT",
    },
    "case_results": {
        "health": "TEXT",
        "failure_layer": "TEXT",
        "verdict": "TEXT",
        "result_valid_for_scoring": "INTEGER",
        "semantic_score": "TEXT",
        "semantic_ratio": "REAL",
        "semantic_hit_count": "INTEGER",
        "semantic_total": "INTEGER",
        "answer_completeness": "TEXT",
        "answer_completeness_ratio": "REAL",
        "answerability": "TEXT",
        "did_answer": "INTEGER",
        "candidate_recall": "TEXT",
        "final_context_recall": "TEXT",
        "candidate_to_final_delta": "TEXT",
        "final_to_answer_delta": "TEXT",
        "semantic_hits_json": "TEXT NOT NULL DEFAULT '[]'",
        "semantic_misses_json": "TEXT NOT NULL DEFAULT '[]'",
        "evidence_source_unverified": "INTEGER",
    },
}


def ensure_columns(conn: sqlite3.Connection) -> None:
    for table, columns in MIGRATION_COLUMNS.items():
        existing = {row["name"] for row in conn.execute(f"PRAGMA table_info({table})").fetchall()}
        for name, definition in columns.items():
            if name not in existing:
                conn.execute(f"ALTER TABLE {table} ADD COLUMN {name} {definition}")


def connect(db_path: Path) -> sqlite3.Connection:
    db_path.parent.mkdir(parents=True, exist_ok=True)
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys=ON")
    return conn


def init_db(conn: sqlite3.Connection) -> None:
    conn.executescript(SCHEMA)
    ensure_columns(conn)
    conn.commit()


def reset_db(conn: sqlite3.Connection) -> None:
    conn.executescript(
        """
        DELETE FROM document_hits;
        DELETE FROM case_results;
        DELETE FROM runs;
        DELETE FROM cases;
        DELETE FROM sqlite_sequence WHERE name='document_hits';
        """
    )
    conn.commit()


def to_json(value: Any) -> str:
    return json.dumps(value if value is not None else [], ensure_ascii=False)


def decode_row(row: sqlite3.Row | None) -> dict[str, Any] | None:
    if row is None:
        return None
    out = dict(row)
    for key in list(out):
        if key in JSON_FIELDS:
            target_key = key[:-5] if key.endswith("_json") else key
            try:
                out[target_key] = json.loads(out[key] or "null")
            except json.JSONDecodeError:
                out[target_key] = out[key]
            if target_key != key:
                del out[key]
    for key, value in list(out.items()):
        if key in {
            "completed",
            "empty",
            "rerank_applied",
            "source_file_in_final_context",
            "expected_section_in_final_context",
            "result_valid_for_scoring",
            "did_answer",
            "evidence_source_unverified",
        } and value is not None:
            out[key] = bool(value)
    return out


def decode_rows(rows: Iterable[sqlite3.Row]) -> list[dict[str, Any]]:
    return [decode_row(row) or {} for row in rows]


def count_rows(conn: sqlite3.Connection, table: str) -> int:
    return int(conn.execute(f"SELECT COUNT(*) FROM {table}").fetchone()[0])


def counts(conn: sqlite3.Connection) -> dict[str, int]:
    return {
        "runs": count_rows(conn, "runs"),
        "cases": count_rows(conn, "cases"),
        "case_results": count_rows(conn, "case_results"),
        "document_hits": count_rows(conn, "document_hits"),
        "internal_cases": int(conn.execute("SELECT COUNT(*) FROM cases WHERE origin='internal'").fetchone()[0]),
        "external_cases": int(conn.execute("SELECT COUNT(*) FROM cases WHERE origin='external'").fetchone()[0]),
    }
