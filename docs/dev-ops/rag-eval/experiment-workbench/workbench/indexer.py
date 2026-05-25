from __future__ import annotations

import json
import re
import sqlite3
from collections import Counter
from pathlib import Path
from typing import Any

from . import db
from .parser import parse_report
from .paths import EXTERNAL_CASES_DIR, INTERNAL_CASES_PATH, RAG_EVAL_DIR, RESULTS_DIR


REPORT_PREFIXES = ("rag-eval-result", "rag-eval-retrievalContext", "probe-")
EMPTY_JSON_ARRAY = "[]"


def load_json_cases(path: Path) -> list[dict[str, Any]]:
    data = json.loads(path.read_text(encoding="utf-8"))
    if isinstance(data, list):
        return data
    if isinstance(data, dict):
        for key in ("cases", "items", "data"):
            if isinstance(data.get(key), list):
                return data[key]
    return []


def derive_family(origin: str, source_file: str | None, case_id: str) -> str:
    if origin == "internal":
        return "internal"
    if source_file:
        parts = Path(source_file).parts
        if len(parts) >= 2 and parts[0] == "corpus":
            return parts[1]
        if parts:
            return parts[0]
    if case_id.startswith("EXT-"):
        return case_id.split("-")[1].lower()
    return origin


def normalize_case(case: dict[str, Any], origin: str, source_case_file: Path) -> dict[str, Any] | None:
    case_id = case.get("case_id") or case.get("id")
    if not case_id:
        return None
    source_file = case.get("source_file")
    return {
        "case_id": case_id,
        "question": case.get("question", ""),
        "case_type": case.get("case_type") or case.get("type"),
        "score_mode": case.get("score_mode"),
        "source_file": source_file,
        "expected_source_section": case.get("expected_source_section"),
        "expected_points_json": db.to_json(case.get("expected_points") or []),
        "expected_points_v2_json": db.to_json(case.get("expected_points_v2") or []),
        "origin": origin,
        "family": case.get("family") or case.get("corpus_group") or derive_family(origin, source_file, case_id),
        "source_case_file": str(source_case_file.relative_to(RAG_EVAL_DIR)),
    }


def merge_case_record(existing: dict[str, Any] | None, incoming: dict[str, Any]) -> dict[str, Any]:
    if existing is None:
        return incoming
    merged = dict(existing)
    merged.update(incoming)
    for key in ("expected_points_json", "expected_points_v2_json"):
        if incoming.get(key) == EMPTY_JSON_ARRAY and existing.get(key) != EMPTY_JSON_ARRAY:
            merged[key] = existing[key]
    return merged


def load_case_registry() -> list[dict[str, Any]]:
    cases: dict[str, dict[str, Any]] = {}
    for case in load_json_cases(INTERNAL_CASES_PATH):
        normalized = normalize_case(case, "internal", INTERNAL_CASES_PATH)
        if normalized:
            cases[normalized["case_id"]] = merge_case_record(cases.get(normalized["case_id"]), normalized)
    for path in sorted(EXTERNAL_CASES_DIR.glob("*.json")):
        for case in load_json_cases(path):
            normalized = normalize_case(case, "external", path)
            if normalized:
                cases[normalized["case_id"]] = merge_case_record(cases.get(normalized["case_id"]), normalized)
    return sorted(cases.values(), key=lambda item: (item["origin"], item["family"] or "", item["case_id"]))


def run_id_for(path: Path) -> str:
    rel = path.relative_to(RESULTS_DIR).with_suffix("")
    return "__".join(rel.parts)


def group_for(path: Path) -> str:
    parent = path.relative_to(RESULTS_DIR).parent
    return parent.as_posix() if str(parent) != "." else "root"


def scan_reports() -> list[Path]:
    reports: list[Path] = []
    for path in sorted(RESULTS_DIR.rglob("*.md")):
        if not path.is_file():
            continue
        if path.name.startswith(REPORT_PREFIXES):
            reports.append(path)
    return reports


def derive_config_hint(run_id: str, group_name: str, parsed: dict[str, Any]) -> str:
    stem = Path(run_id.replace("__", "/")).name.lower()
    checks = [
        ("l0-vector-only", "L0 vector only"),
        ("l1-hybrid-only", "L1 hybrid only"),
        ("l2-hybrid-bge", "L2 hybrid + BGE"),
        ("l3-rewrite-no-profile", "LLM rewrite, no profile"),
        ("l4-rewrite-profile-no-salience", "rewrite + profile, no salience"),
        ("l5-current-best", "L5 current best"),
        ("no-rerank", "current best without rerank"),
        ("no-salience", "current best without salience"),
        ("step6.8", "Step 6.8 KnowledgeBaseProfile"),
        ("step6.9", "Step 6.9 LLM profile"),
        ("source-coverage", "Step 7.1 source coverage"),
    ]
    for token, label in checks:
        if token in stem or token in group_name.lower():
            return label
    modes = Counter()
    for result in parsed.get("details", {}).values():
        for event in result.get("retrieval_events", []):
            if event.get("rerank_mode"):
                modes[event["rerank_mode"]] += 1
            if event.get("query_rewrite_mode"):
                modes[event["query_rewrite_mode"]] += 1
            if event.get("profile_source"):
                modes[event["profile_source"]] += 1
    if modes:
        return ", ".join(item for item, _ in modes.most_common(3))
    return group_name


def bool_to_db(value: bool | None) -> int | None:
    if value is None:
        return None
    return 1 if value else 0


def first_event(detail: dict[str, Any]) -> dict[str, Any]:
    events = detail.get("retrieval_events") or []
    return events[0] if events else {}


def missing_points_from_detail(detail: dict[str, Any], fallback_text: str | None) -> list[str]:
    points = [
        item.get("point")
        for item in detail.get("expected_points") or []
        if item and item.get("matched") is False and item.get("point")
    ]
    if points:
        return points
    if fallback_text and fallback_text not in {"—", "manual"}:
        return [item.strip() for item in re.split(r"[,，;；]\s*", fallback_text) if item.strip()]
    return []


def point_counts_from_coverage(coverage: dict[str, Any], stage: str) -> tuple[int | None, int | None]:
    stage_data = coverage.get(stage) or {}
    raw = stage_data.get("expected_points_exact")
    if not raw:
        return None, None
    match = re.search(r"(\d+)\s*/\s*(\d+)", str(raw))
    if not match:
        return None, None
    return int(match.group(1)), int(match.group(2))


def ensure_case(conn: sqlite3.Connection, case_id: str) -> None:
    conn.execute(
        """
        INSERT OR IGNORE INTO cases
          (case_id, question, case_type, score_mode, expected_points_json, origin, family, source_case_file)
        VALUES (?, '', NULL, NULL, '[]', 'report-only', 'report-only', NULL)
        """,
        (case_id,),
    )


def insert_document_hits(
    conn: sqlite3.Connection,
    run_id: str,
    case_id: str,
    stage: str,
    docs: list[dict[str, Any]],
) -> None:
    for doc in docs:
        scores = {
            key: doc.get(key)
            for key in (
                "score",
                "rrfScore",
                "vectorScore",
                "keywordScore",
                "rerankScore",
                "queryFusionScore",
                "rerankFusionScore",
                "queryFusionBoostScore",
                "fusionAwareScore",
            )
            if key in doc
        }
        conn.execute(
            """
            INSERT INTO document_hits
              (run_id, case_id, stage, rank, source_path, chunk_index, text_excerpt, scores_json, metadata_json)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                run_id,
                case_id,
                stage,
                doc.get("rank"),
                doc.get("source"),
                doc.get("chunk"),
                doc.get("preview"),
                db.to_json(scores if scores else {}),
                db.to_json(doc),
            ),
        )


def reindex(conn: sqlite3.Connection) -> dict[str, Any]:
    db.init_db(conn)
    db.reset_db(conn)

    for case in load_case_registry():
        conn.execute(
            """
            INSERT INTO cases
              (case_id, question, case_type, score_mode, source_file, expected_source_section,
               expected_points_json, expected_points_v2_json, origin, family, source_case_file)
            VALUES (:case_id, :question, :case_type, :score_mode, :source_file, :expected_source_section,
                    :expected_points_json, :expected_points_v2_json, :origin, :family, :source_case_file)
            ON CONFLICT(case_id) DO UPDATE SET
              question=excluded.question,
              case_type=excluded.case_type,
              score_mode=excluded.score_mode,
              source_file=excluded.source_file,
              expected_source_section=excluded.expected_source_section,
              expected_points_json=excluded.expected_points_json,
              expected_points_v2_json=excluded.expected_points_v2_json,
              origin=excluded.origin,
              family=excluded.family,
              source_case_file=excluded.source_case_file
            """,
            case,
        )

    report_count = 0
    warning_count = 0
    for report_path in scan_reports():
        parsed = parse_report(report_path)
        report_count += 1
        run_id = run_id_for(report_path)
        group_name = group_for(report_path)
        warnings = list(parsed.get("warnings") or [])
        warning_count += len(warnings)
        case_results = parsed.get("case_results") or {}
        conn.execute(
            """
            INSERT INTO runs
              (run_id, group_name, file_path, generated_at, agent_id, api_url, config_hint,
               schema_era, eval_schema_version, case_schema_version, rubric_coverage,
               case_count, parse_warnings_json)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                run_id,
                group_name,
                str(report_path.relative_to(RAG_EVAL_DIR)),
                parsed.get("header", {}).get("generated_at"),
                parsed.get("header", {}).get("agent_id"),
                parsed.get("header", {}).get("api_url"),
                derive_config_hint(run_id, group_name, parsed),
                parsed.get("schema_era"),
                parsed.get("header", {}).get("eval_schema_version"),
                parsed.get("header", {}).get("case_schema_version"),
                parsed.get("header", {}).get("rubric_coverage"),
                len(case_results),
                db.to_json(warnings),
            ),
        )

        for case_id, summary in case_results.items():
            ensure_case(conn, case_id)
            detail = (parsed.get("details") or {}).get(case_id, {})
            event = first_event(detail)
            coverage = detail.get("source_coverage") or {}
            eval_v2 = detail.get("eval_v2") or {}
            final_hit_count, final_total = point_counts_from_coverage(coverage, "final_context")
            missing_points = missing_points_from_detail(detail, summary.get("missing_points_text"))
            source_file_final = (coverage.get("final_context") or {}).get("source_file")
            section_final = (coverage.get("final_context") or {}).get("section")
            conn.execute(
                """
                INSERT INTO case_results
                  (run_id, case_id, completed, health, failure_layer, verdict, result_valid_for_scoring,
                   literal_hit, literal_ratio, semantic_score, semantic_ratio, semantic_hit_count,
                   semantic_total, answer_completeness, answer_completeness_ratio, answerability,
                   did_answer, candidate_recall, final_context_recall, candidate_to_final_delta,
                   final_to_answer_delta, hit_count, expected_point_count,
                   missing_points_json, answer, answer_preview, retrieved, score_min, score_max, empty,
                   duration_ms, rerank_applied, rerank_mode, rerank_runtime_model,
                   rerank_runtime_failure_reason, profile_version, profile_source,
                   selected_profile_hints_json, query_rewrite_mode, query_rewrite_failure_reason,
                   rewrite_variants_json, source_coverage_label, source_file_in_final_context,
                   expected_section_in_final_context, expected_points_final_hit_count,
                   expected_points_final_total, context_salience_cues_json,
                   context_salience_expansions, semantic_hits_json, semantic_misses_json,
                   evidence_source_unverified, raw_json)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    run_id,
                    case_id,
                    bool_to_db(summary.get("completed", detail.get("completed"))),
                    summary.get("health") or eval_v2.get("health"),
                    summary.get("failure_layer") or eval_v2.get("failure_layer"),
                    eval_v2.get("verdict"),
                    bool_to_db(eval_v2.get("valid_for_scoring")),
                    summary.get("literal_hit"),
                    summary.get("literal_ratio"),
                    summary.get("semantic_score") or eval_v2.get("semantic_score"),
                    summary.get("semantic_ratio"),
                    summary.get("semantic_hit_count"),
                    summary.get("semantic_total"),
                    summary.get("answer_completeness") or eval_v2.get("answer_completeness"),
                    summary.get("answer_completeness_ratio"),
                    summary.get("answerability") or eval_v2.get("bucket"),
                    bool_to_db(eval_v2.get("did_answer")),
                    summary.get("candidate_recall") or eval_v2.get("candidate"),
                    summary.get("final_context_recall") or eval_v2.get("final_context"),
                    summary.get("candidate_to_final_delta") or eval_v2.get("candidate_to_final"),
                    summary.get("final_to_answer_delta") or eval_v2.get("final_to_answer"),
                    summary.get("hit_count"),
                    summary.get("expected_point_count") or len(detail.get("expected_points") or []) or None,
                    db.to_json(missing_points),
                    detail.get("answer"),
                    summary.get("answer_preview"),
                    summary.get("retrieved", event.get("retrieved_document_count")),
                    summary.get("score_min", event.get("score_min")),
                    summary.get("score_max", event.get("score_max")),
                    bool_to_db(summary.get("empty", event.get("retrieval_empty"))),
                    summary.get("duration_ms", detail.get("duration_ms")),
                    bool_to_db(event.get("rerank_applied")),
                    event.get("rerank_mode"),
                    event.get("rerank_runtime_model"),
                    event.get("rerank_runtime_failure_reason"),
                    event.get("profile_version"),
                    event.get("profile_source"),
                    db.to_json(event.get("selected_profile_hints") or []),
                    event.get("query_rewrite_mode"),
                    event.get("query_rewrite_failure"),
                    db.to_json(event.get("query_rewrite_texts") or []),
                    coverage.get("label") or summary.get("source_coverage_label"),
                    bool_to_db(source_file_final),
                    bool_to_db(section_final),
                    final_hit_count,
                    final_total,
                    db.to_json(event.get("context_salience_cues") or []),
                    event.get("context_salience_expansions"),
                    db.to_json(eval_v2.get("semantic_hits") or []),
                    db.to_json(eval_v2.get("semantic_misses") or []),
                    bool_to_db(eval_v2.get("evidence_source_unverified")),
                    db.to_json({"summary": summary, "detail": detail, "event": event}),
                ),
            )
            insert_document_hits(conn, run_id, case_id, "pre_rerank", detail.get("pre_rerank_documents") or [])
            insert_document_hits(conn, run_id, case_id, "final", detail.get("documents") or [])

    conn.commit()
    result = db.counts(conn)
    result.update({"reports_scanned": report_count, "parse_warning_count": warning_count})
    return result
