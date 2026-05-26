#!/usr/bin/env python3
"""
Minimal RAG eval runner for local Agent API.

Route A: do not require backend metadata in API response. It records SSE summary,
completion status, duration, and a lightweight keyword check for manual review.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
import time
import unicodedata
from datetime import datetime
from pathlib import Path
from typing import Any
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen


DEFAULT_API_URL = "http://localhost:8099/api/v1/agent/auto_agent"

QA_RETRIEVED_DOCUMENTS = "qa_retrieved_documents"
QA_PRE_RERANK_DOCUMENTS = "qa_pre_rerank_documents"
QA_RETRIEVED_DOCUMENT_COUNT = "qa_retrieved_document_count"
QA_RETRIEVAL_EMPTY = "qa_retrieval_empty"
QA_SIMILARITY_THRESHOLD = "qa_similarity_threshold"
QA_CANDIDATE_SIMILARITY_THRESHOLD = "qa_candidate_similarity_threshold"
QA_MIN_RETRIEVED_SCORE = "qa_min_retrieved_score"
QA_MAX_RETRIEVED_SCORE = "qa_max_retrieved_score"
QA_CONTEXT_MAX_CHARS = "qa_context_max_chars"
QA_CONTEXT_ACTUAL_CHARS = "qa_context_actual_chars"
QA_CONTEXT_SELECTED_COUNT = "qa_context_selected_count"
QA_CONTEXT_DROPPED_COUNT = "qa_context_dropped_count"
QA_CONTEXT_TRUNCATED = "qa_context_truncated"
QA_CONTEXT_SALIENCE_CUES = "qa_context_salience_cues"
QA_CONTEXT_SALIENCE_EXPANSION_COUNT = "qa_context_salience_expansion_count"
QA_RERANK_APPLIED = "qa_rerank_applied"
QA_RERANK_MODE = "qa_rerank_mode"
QA_RERANK_CANDIDATE_COUNT = "qa_rerank_candidate_count"
QA_RERANK_FINAL_COUNT = "qa_rerank_final_count"
QA_RERANK_FAILURE_REASON = "qa_rerank_failure_reason"
QA_RERANK_MODEL_NAME = "qa_rerank_model_name"
QA_RERANK_ENDPOINT = "qa_rerank_endpoint"
QA_RERANK_QUERY_POLICY = "qa_rerank_query_policy"
QA_RERANK_QUERY_TEXT = "qa_rerank_query_text"
QA_RERANK_COVERAGE_GUARD_APPLIED = "qa_rerank_coverage_guard_applied"
QA_RERANK_COVERAGE_GUARD_ADDED_COUNT = "qa_rerank_coverage_guard_added_count"
QA_QUERY_REWRITE_MODE = "qa_query_rewrite_mode"
QA_QUERY_REWRITE_REQUESTED_POLICY = "qa_query_rewrite_requested_policy"
QA_QUERY_REWRITE_FAILURE_REASON = "qa_query_rewrite_failure_reason"
QA_QUERY_REWRITE_ELAPSED_MS = "qa_query_rewrite_elapsed_ms"
QA_QUERY_REWRITE_ATTEMPT_TRACE = "qa_query_rewrite_attempt_trace"
QA_QUERY_REWRITE_PROFILE_SOURCE = "qa_query_rewrite_profile_source"
QA_QUERY_REWRITE_PROFILE_VERSION = "qa_query_rewrite_profile_version"
QA_QUERY_REWRITE_SELECTED_PROFILE_HINTS = "qa_query_rewrite_selected_profile_hints"
QA_QUERY_VARIANT_COUNT = "qa_query_variant_count"
QA_QUERY_VARIANT_TEXTS = "qa_query_variant_texts"

DOC_SOURCE_PATH = "sourcePath"
DOC_CHUNK_INDEX = "chunkIndex"
DOC_RETRIEVAL_SOURCE = "retrievalSource"
DOC_RRF_SCORE = "rrfScore"
DOC_VECTOR_RANK = "vectorRank"
DOC_VECTOR_SCORE = "vectorScore"
DOC_KEYWORD_RANK = "keywordRank"
DOC_KEYWORD_SCORE = "keywordScore"
DOC_KEYWORD_SKIPPED_REASON = "keywordSkippedReason"
DOC_BEFORE_RERANK_RANK = "beforeRerankRank"
DOC_RERANK_RANK = "rerankRank"
DOC_RERANK_SCORE = "rerankScore"
DOC_RERANK_FUSION_SCORE = "rerankFusionScore"
DOC_QUERY_FUSION_BOOST_SCORE = "queryFusionBoostScore"
DOC_FUSION_AWARE_SCORE = "fusionAwareScore"
DOC_FUSION_AWARE_WEIGHT = "fusionAwareWeight"
DOC_RERANK_APPLIED = "rerankApplied"
DOC_RERANK_MODE = "rerankMode"
DOC_COVERAGE_GUARD_ADDED = "coverageGuardAdded"
DOC_COVERAGE_GUARD_REASON = "coverageGuardReason"
DOC_COVERAGE_GUARD_CUES = "coverageGuardCues"
DOC_RERANK_VARIANT_HIT_COUNT = "rerankVariantHitCount"
DOC_BEST_RERANK_VARIANT_RANK = "bestRerankVariantRank"
DOC_RERANK_VARIANT_INDEXES = "rerankVariantIndexes"
DOC_RERANK_VARIANT_HITS = "rerankVariantHits"
DOC_QUERY_VARIANT_INDEX = "queryVariantIndex"
DOC_QUERY_VARIANT_TEXT = "queryVariantText"
DOC_QUERY_VARIANT_RANK = "queryVariantRank"
DOC_QUERY_FUSION_SCORE = "queryFusionScore"
DOC_QUERY_FUSION_RANK = "queryFusionRank"
DOC_QUERY_VARIANT_HIT_COUNT = "queryVariantHitCount"
DOC_BEST_QUERY_VARIANT_RANK = "bestQueryVariantRank"
DOC_QUERY_VARIANT_INDEXES = "queryVariantIndexes"
DOC_QUERY_VARIANT_HITS = "queryVariantHits"

EVAL_SCHEMA_VERSION = "v2"
CASE_SCHEMA_VERSION_V1_COMPATIBLE = "v1-compatible"
CASE_SCHEMA_VERSION_V2_COMPATIBLE = "v2-compatible"

REFUSAL_TEMPLATES = [
    "无法基于",
    "无法回答",
    "无法直接回答",
    "无法从",
    "无法根据",
    "不能回答",
    "没有提供",
    "未提供",
    "当前知识库没有",
    "知识库未涉及",
    "文档没有指定",
    "不能从文档回答",
    "cannot answer",
    "cannot be answered",
    "does not contain",
    "does not specify",
    "does not identify",
    "not provided in the document",
]

WEAK_REFUSAL_TEMPLATES = {
    "没有提供",
    "未提供",
    "does not contain",
    "does not specify",
    "does not identify",
    "not provided in the document",
}


def normalize_text(value: Any) -> str:
    return " ".join(str(value or "").lower().split())


def normalize_match_text(value: Any) -> str:
    text = unicodedata.normalize("NFKC", str(value or "")).lower()
    text = re.sub(r"[*_`]+", "", text)
    text = text.replace("：", ":")
    text = re.sub(r"[‐‑‒–—−]", "-", text)
    text = text.replace("～", "~").replace("〜", "~")
    text = re.sub(r"(\d+(?:\.\d+)?)\s*(?:-|~|到|至)\s*(\d+(?:\.\d+)?)\s*%", r"\1-\2%", text)
    text = re.sub(r"\s+", " ", text)
    return text.strip()


def load_cases(path: Path) -> list[dict[str, Any]]:
    with path.open("r", encoding="utf-8") as f:
        data = json.load(f)
    if not isinstance(data, list):
        raise ValueError("cases file must be a JSON array")
    return data


def post_sse(api_url: str, payload: dict[str, Any], timeout: int) -> tuple[list[dict[str, Any]], int, str | None]:
    body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
    req = Request(
        api_url,
        data=body,
        headers={"Content-Type": "application/json"},
        method="POST",
    )

    events: list[dict[str, Any]] = []
    status = 0
    error: str | None = None
    try:
        with urlopen(req, timeout=timeout) as resp:
            status = resp.status
            for raw_line in resp:
                line = raw_line.decode("utf-8", errors="replace").strip()
                if not line.startswith("data:"):
                    continue
                data = line.removeprefix("data:").strip()
                if not data:
                    continue
                try:
                    events.append(json.loads(data))
                except json.JSONDecodeError:
                    events.append({"type": "raw", "content": data})
    except HTTPError as e:
        status = e.code
        error = e.read().decode("utf-8", errors="replace")
    except URLError as e:
        error = str(e.reason)
    except TimeoutError:
        error = "timeout"
    return events, status, error


def summarize_events(events: list[dict[str, Any]]) -> tuple[str, bool, list[dict[str, Any]], str | None]:
    summary_parts: list[str] = []
    completed = False
    retrievals: list[dict[str, Any]] = []
    sse_error: str | None = None
    for event in events:
        event_type = event.get("type")
        if event_type == "summary":
            summary_parts.append(str(event.get("content", "")))
        # 只认终端 type=complete 帧。AutoAgentExecuteResultEntity.createSummaryResult
        # 出于 task 语义也会把 completed=true 写进 summary 事件，但那不等于 SSE
        # 流收尾。如果只看 completed=true 旁路，port.complete() 失败 / 连接中断
        # 这种纯收尾问题会被掩盖。Codex P2 review 指出。
        if event_type == "complete":
            completed = True
        if event_type == "retrieval":
            retrievals.append({
                "step": event.get("step"),
                "timestamp": event.get("timestamp"),
                "data": event.get("data") or {},
            })
        if event_type == "error":
            err_cls = event.get("errorClass") or "Error"
            msg = event.get("message") or ""
            sse_error = f"{err_cls}: {msg}"
    return "\n".join(part for part in summary_parts if part).strip(), completed, retrievals, sse_error


def keyword_check(answer: str, expected_points: list[str]) -> tuple[int, list[str]]:
    missed = [point for point in expected_points if point and point not in answer]
    return len(expected_points) - len(missed), missed


def _has_expected_points_v2(case: dict[str, Any]) -> bool:
    points = case.get("expected_points_v2")
    return isinstance(points, list) and bool(points)


def _case_schema_version(case: dict[str, Any]) -> str:
    return CASE_SCHEMA_VERSION_V2_COMPATIBLE if _has_expected_points_v2(case) else CASE_SCHEMA_VERSION_V1_COMPATIBLE


def _matches_refusal_template(answer: str) -> bool:
    normalized = normalize_text(answer)
    if not normalized:
        return False
    # Global refusal templates are only a coarse did_answer signal. Keep them
    # prefix-biased so a substantive answer with a later caveat such as
    # "specific details are not provided" is not counted as a full refusal.
    prefix = normalized[:240]
    strong_templates = [template for template in REFUSAL_TEMPLATES if template not in WEAK_REFUSAL_TEMPLATES]
    if any(template in prefix for template in strong_templates):
        return True
    first_sentence = re.split(r"[。.!?；;\n]", normalized, maxsplit=1)[0]
    return any(template in first_sentence for template in WEAK_REFUSAL_TEMPLATES)


def _did_answer(answer: str) -> bool:
    normalized = normalize_text(answer)
    return len(normalized) >= 8 and not _matches_refusal_template(answer)


def _answerability_bucket(should_answer: Any, did_answer: bool) -> str:
    if should_answer is None:
        return "n/a"
    if bool(should_answer) and did_answer:
        return "true_answer"
    if bool(should_answer) and not did_answer:
        return "false_refusal"
    if not bool(should_answer) and did_answer:
        return "false_answer"
    return "true_refusal"


def _ratio_cell(matched: int, total: int, enabled: bool = True) -> str:
    if not enabled or total <= 0:
        return "n/a"
    return f"{matched}/{total}"


def _delta_cell(after: int, before: int, enabled: bool = True) -> str:
    if not enabled:
        return "n/a"
    return f"{after - before:+d}"


def _evidence_scored(score_mode: str, total: int) -> bool:
    return score_mode != "manual" and total > 0


def _point_id(point: dict[str, Any], index: int) -> str:
    return str(point.get("id") or f"point_{index}")


def _point_weight(point: dict[str, Any]) -> float:
    value = point.get("weight", 1)
    try:
        return float(value)
    except (TypeError, ValueError):
        return 1.0


def _is_required(point: dict[str, Any]) -> bool:
    return bool(point.get("required", True))


def _weighted_score_cell(matched: float, total: float) -> str:
    if total <= 0:
        return "n/a"
    if matched.is_integer() and total.is_integer():
        return f"{int(matched)}/{int(total)}"
    return f"{matched:.2f}/{total:.2f}"


def _weighted_ratio(matched: float, total: float) -> float | None:
    if total <= 0:
        return None
    return matched / total


def _match_any(text: str, tokens: list[Any]) -> bool:
    normalized = normalize_match_text(text)
    return any(normalize_match_text(token) and normalize_match_text(token) in normalized for token in tokens)


def _is_ascii_word_char(value: str) -> bool:
    return bool(value) and bool(re.match(r"[a-z0-9_]", value))


def _valid_alias_span(text: str, start: int, end: int, alias: str) -> bool:
    if _is_ascii_word_char(alias[0]) and start > 0 and _is_ascii_word_char(text[start - 1]):
        return False
    if _is_ascii_word_char(alias[-1]) and end < len(text) and _is_ascii_word_char(text[end]):
        return False
    return True


def _ordered_alias_position(normalized_text: str, alias: Any, max_span: int = 80) -> int:
    normalized_alias = normalize_match_text(alias)
    if not normalized_alias:
        return -1
    start_from = 0
    while True:
        exact_position = normalized_text.find(normalized_alias, start_from)
        if exact_position < 0:
            break
        exact_end = exact_position + len(normalized_alias)
        if _valid_alias_span(normalized_text, exact_position, exact_end, normalized_alias):
            return exact_position
        start_from = exact_position + 1

    parts = [part for part in normalized_alias.split(" ") if part]
    if len(parts) <= 1:
        return -1
    start_from = 0
    while True:
        first_position = normalized_text.find(parts[0], start_from)
        if first_position < 0:
            return -1
        search_from = first_position + len(parts[0])
        last_end = search_from
        matched = True
        for part in parts[1:]:
            position = normalized_text.find(part, search_from)
            if position < 0:
                matched = False
                break
            between = normalized_text[last_end:position]
            if re.search(r"[。；;.!?]", between):
                matched = False
                break
            last_end = position + len(part)
            search_from = last_end
        if matched and last_end - first_position <= max_span:
            return first_position
        start_from = first_position + len(parts[0])


def _regex_any(text: str, patterns: list[Any]) -> bool:
    for pattern in patterns:
        try:
            if re.search(str(pattern), text, flags=re.IGNORECASE | re.MULTILINE):
                return True
        except re.error:
            continue
    return False


def _value_alias_distance(alias_span: tuple[int, int], value_span: tuple[int, int]) -> int:
    alias_start, alias_end = alias_span
    value_start, value_end = value_span
    if alias_start <= value_end and value_start <= alias_end:
        return 0
    if alias_end <= value_start:
        return value_start - alias_end
    return alias_start - value_end


def _segments_for_numeric_alias(answer: str) -> list[str]:
    normalized = normalize_match_text(answer)
    list_items = [item.strip() for item in re.split(r"(?:^|\n)\s*(?:[-*]|\d+[.)、])\s+", normalized) if item.strip()]
    if len(list_items) > 1:
        return list_items
    sentence_items = [item.strip() for item in re.split(r"[。；;\n]+", normalized) if item.strip()]
    return sentence_items or [normalized]


def _numeric_alias_match(answer: str, aliases: list[Any], values: list[Any]) -> bool:
    normalized_aliases = [normalize_match_text(alias) for alias in aliases if normalize_match_text(alias)]
    normalized_values = [normalize_match_text(value) for value in values if normalize_match_text(value)]
    for segment in _segments_for_numeric_alias(answer):
        for alias in normalized_aliases:
            for alias_match in re.finditer(re.escape(alias), segment):
                alias_span = alias_match.span()
                for value in normalized_values:
                    for value_match in re.finditer(re.escape(value), segment):
                        if _value_alias_distance(alias_span, value_match.span()) <= 80:
                            return True
    return False


def _ordered_steps_match(answer: str, steps: list[Any]) -> bool:
    normalized_answer = normalize_match_text(answer)
    positions: list[int] = []
    previous_position = -1
    for step in steps:
        aliases: list[Any]
        if isinstance(step, dict):
            if step.get("required", True) is False:
                continue
            aliases = list(step.get("aliases") or step.get("answer_match") or step.get("values") or [])
            if step.get("text"):
                aliases.append(step.get("text"))
        else:
            aliases = [step]
        step_positions = [_ordered_alias_position(normalized_answer, alias) for alias in aliases]
        step_positions = sorted(pos for pos in step_positions if pos > previous_position)
        if not step_positions:
            return False
        previous_position = step_positions[0]
        positions.append(previous_position)
    return True


def _refusal_match(answer: str, point: dict[str, Any]) -> bool:
    positive = point.get("answer_match") or point.get("aliases") or REFUSAL_TEMPLATES
    if not (_matches_refusal_template(answer) or _match_any(answer, list(positive))):
        return False
    unsupported = point.get("unsupported_specific_answers") or []
    return not _match_any(answer, list(unsupported))


def _answer_point_matches(answer: str, point: dict[str, Any]) -> bool:
    point_type = str(point.get("type") or "exact")
    if point_type == "exact":
        return _match_any(answer, list(point.get("answer_match") or point.get("values") or []))
    if point_type == "alias":
        return _match_any(answer, list(point.get("aliases") or point.get("answer_match") or []))
    if point_type == "regex":
        return _regex_any(answer, list(point.get("regex") or point.get("patterns") or []))
    if point_type == "numeric_alias":
        return _numeric_alias_match(answer, list(point.get("aliases") or []), list(point.get("values") or []))
    if point_type == "ordered_steps":
        return _ordered_steps_match(answer, list(point.get("steps") or []))
    if point_type == "refusal":
        return _refusal_match(answer, point)
    return False


def _document_matches_evidence(document: dict[str, Any], evidence: dict[str, Any]) -> bool:
    anchor = str(evidence.get("anchor_text") or "")
    if not anchor:
        return False
    source_file = str(evidence.get("source_file") or "")
    if source_file and not _source_file_matches(document, source_file):
        return False
    return normalize_match_text(anchor) in normalize_match_text(_doc_search_text(document))


def _point_evidence_hit(point: dict[str, Any], documents: list[dict[str, Any]]) -> bool:
    evidence_items = point.get("evidence") or []
    if not isinstance(evidence_items, list) or not evidence_items:
        fallbacks = point.get("context_match") or point.get("answer_match") or point.get("aliases") or []
        return any(_match_any(_doc_text(document), list(fallbacks)) for document in documents if isinstance(document, dict))
    for evidence in evidence_items:
        if not isinstance(evidence, dict):
            continue
        for document in documents:
            if isinstance(document, dict) and _document_matches_evidence(document, evidence):
                return True
    return False


def evaluate_expected_points_v2(case: dict[str, Any],
                                answer: str,
                                pre_documents: list[dict[str, Any]],
                                final_documents: list[dict[str, Any]]) -> dict[str, Any]:
    points = case.get("expected_points_v2")
    if not isinstance(points, list) or not points:
        return {
            "enabled": False,
            "candidate_matched_weight": 0.0,
            "final_matched_weight": 0.0,
            "answer_matched_weight": 0.0,
            "answer_required_matched_weight": 0.0,
            "total_weight": 0.0,
            "required_weight": 0.0,
            "semantic_hits": [],
            "semantic_misses": [],
            "evidence_source_unverified": False,
            "refusal_answer_matched": False,
        }

    total_weight = 0.0
    required_weight = 0.0
    candidate_weight = 0.0
    final_weight = 0.0
    answer_weight = 0.0
    required_answer_weight = 0.0
    hits: list[str] = []
    misses: list[str] = []
    evidence_source_unverified = False
    refusal_answer_matched = False

    for index, raw_point in enumerate(points, start=1):
        if not isinstance(raw_point, dict):
            continue
        point = raw_point
        point_id = _point_id(point, index)
        weight = _point_weight(point)
        required = _is_required(point)
        total_weight += weight
        if required:
            required_weight += weight
        if any(not (item.get("source_file") if isinstance(item, dict) else None) for item in point.get("evidence") or []):
            evidence_source_unverified = True
        if _point_evidence_hit(point, pre_documents):
            candidate_weight += weight
        if _point_evidence_hit(point, final_documents):
            final_weight += weight
        answer_hit = _answer_point_matches(answer, point)
        if answer_hit:
            answer_weight += weight
            if required:
                required_answer_weight += weight
            hits.append(point_id)
            if str(point.get("type") or "") == "refusal":
                refusal_answer_matched = True
        else:
            misses.append(point_id)

    return {
        "enabled": True,
        "candidate_matched_weight": candidate_weight,
        "final_matched_weight": final_weight,
        "answer_matched_weight": answer_weight,
        "answer_required_matched_weight": required_answer_weight,
        "total_weight": total_weight,
        "required_weight": required_weight,
        "semantic_hits": hits,
        "semantic_misses": misses,
        "evidence_source_unverified": evidence_source_unverified,
        "refusal_answer_matched": refusal_answer_matched,
    }


def _failure_layer(result_valid_for_scoring: bool,
                   answerability: str,
                   score_mode: str,
                   coverage: dict[str, Any],
                   answer_matched_count: int,
                   semantic: dict[str, Any] | None = None) -> str:
    if not result_valid_for_scoring:
        return "runtime_failure"
    if answerability == "false_answer":
        return "false_answer"
    if answerability == "false_refusal":
        return "false_refusal"

    coverage_layer = str(coverage.get("coverage_layer") or "")
    if coverage_layer == "no_retrieval_metadata":
        return "manual_review_needed"

    if semantic and semantic.get("enabled"):
        total = float(semantic.get("total_weight") or 0)
        required_total = float(semantic.get("required_weight") or 0)
        pre_count = float(semantic.get("candidate_matched_weight") or 0)
        final_count = float(semantic.get("final_matched_weight") or 0)
        answer_count = float(semantic.get("answer_matched_weight") or 0)
        required_answer_count = float(semantic.get("answer_required_matched_weight") or 0)
        if pre_count < total:
            return "retrieval_miss"
        if final_count < pre_count or final_count < total:
            return "rerank_or_context_loss"
        if required_answer_count < required_total:
            return "answer_semantic_miss"
        literal_total = int(coverage.get("expected_points_total") or 0)
        if _evidence_scored(score_mode, literal_total) and answer_matched_count < literal_total:
            return "literal_only_mismatch"
        return "none"

    total = int(coverage.get("expected_points_total") or 0)
    pre_count = int(coverage.get("expected_points_in_pre_rerank") or 0)
    final_count = int(coverage.get("expected_points_in_final_context") or 0)
    evidence_scored = _evidence_scored(score_mode, total)

    if not evidence_scored:
        return "none" if answerability == "true_refusal" else "manual_review_needed"
    if pre_count < total:
        return "retrieval_miss"
    if final_count < pre_count or final_count < total:
        return "rerank_or_context_loss"
    if coverage_layer == "answer_generation_or_literal_mismatch" or answer_matched_count < total:
        return "literal_only_mismatch"
    return "none"


def _verdict_for_failure_layer(failure_layer: str) -> str:
    if failure_layer == "runtime_failure":
        return "run_fail"
    if failure_layer in {"false_answer", "false_refusal"}:
        return "refusal_fail"
    if failure_layer in {"none", "literal_only_mismatch"}:
        return "pass"
    if failure_layer == "manual_review_needed":
        return "manual_review_needed"
    return "fail"


def _health_for_failure_layer(failure_layer: str) -> str:
    if failure_layer == "runtime_failure":
        return "run_fail"
    if failure_layer in {"false_answer", "false_refusal"}:
        return "refusal_issue"
    if failure_layer == "groundedness_risk":
        return "ungrounded"
    if failure_layer == "manual_review_needed":
        return "review"
    if failure_layer in {"none", "literal_only_mismatch"}:
        return "ok"
    return "fail"


def build_eval_v2(case: dict[str, Any],
                  completed: bool,
                  error: str | None,
                  answer: str,
                  score_mode: str,
                  answer_matched_count: int,
                  coverage: dict[str, Any],
                  semantic: dict[str, Any] | None = None) -> dict[str, Any]:
    total = int(coverage.get("expected_points_total") or 0)
    pre_count = int(coverage.get("expected_points_in_pre_rerank") or 0)
    final_count = int(coverage.get("expected_points_in_final_context") or 0)
    evidence_scored = _evidence_scored(score_mode, total)
    semantic = semantic or {"enabled": False}
    if semantic.get("enabled"):
        semantic_total = float(semantic.get("total_weight") or 0)
        semantic_required_total = float(semantic.get("required_weight") or 0)
        semantic_candidate = float(semantic.get("candidate_matched_weight") or 0)
        semantic_final = float(semantic.get("final_matched_weight") or 0)
        semantic_answer = float(semantic.get("answer_matched_weight") or 0)
        semantic_required_answer = float(semantic.get("answer_required_matched_weight") or 0)
        candidate_recall = _weighted_score_cell(semantic_candidate, semantic_total)
        final_context_recall = _weighted_score_cell(semantic_final, semantic_total)
        candidate_to_final_delta = _delta_cell(int(semantic_final), int(semantic_candidate), True)
        final_to_answer_delta = _delta_cell(int(semantic_answer), int(semantic_final), True)
        semantic_score = _weighted_score_cell(semantic_answer, semantic_total)
        semantic_ratio = _weighted_ratio(semantic_answer, semantic_total)
        answer_completeness = _weighted_score_cell(semantic_required_answer, semantic_required_total)
        answer_completeness_ratio = _weighted_ratio(semantic_required_answer, semantic_required_total)
    else:
        candidate_recall = _ratio_cell(pre_count, total, evidence_scored)
        final_context_recall = _ratio_cell(final_count, total, evidence_scored)
        candidate_to_final_delta = _delta_cell(final_count, pre_count, evidence_scored)
        final_to_answer_delta = _delta_cell(answer_matched_count, final_count, evidence_scored)
        semantic_score = "n/a"
        semantic_ratio = None
        answer_completeness = "n/a"
        answer_completeness_ratio = None
    did_answer = _did_answer(answer)
    if case.get("should_answer") is False and semantic.get("refusal_answer_matched"):
        did_answer = False
    answerability = _answerability_bucket(case.get("should_answer"), did_answer)
    result_valid_for_scoring = bool(completed) and not error
    failure_layer = _failure_layer(
        result_valid_for_scoring,
        answerability,
        score_mode,
        coverage,
        answer_matched_count,
        semantic,
    )
    return {
        "eval_schema_version": EVAL_SCHEMA_VERSION,
        "case_schema_version": _case_schema_version(case),
        "result_valid_for_scoring": result_valid_for_scoring,
        "verdict": _verdict_for_failure_layer(failure_layer),
        "health": _health_for_failure_layer(failure_layer),
        "failure_layer": failure_layer,
        "candidate_recall": candidate_recall,
        "final_context_recall": final_context_recall,
        "candidate_to_final_delta": candidate_to_final_delta,
        "final_to_answer_delta": final_to_answer_delta,
        "literal_smoke": _ratio_cell(answer_matched_count, total, evidence_scored),
        "semantic_score": semantic_score,
        "semantic_ratio": semantic_ratio,
        "answer_completeness": answer_completeness,
        "answer_completeness_ratio": answer_completeness_ratio,
        "semantic_hits": list(semantic.get("semantic_hits") or []),
        "semantic_misses": list(semantic.get("semantic_misses") or []),
        "evidence_source_unverified": bool(semantic.get("evidence_source_unverified")),
        "did_answer": did_answer,
        "answerability": answerability,
    }


def _doc_metadata(document: dict[str, Any]) -> dict[str, Any]:
    metadata = document.get("metadata") or {}
    return metadata if isinstance(metadata, dict) else {}


def _doc_text(document: dict[str, Any]) -> str:
    return str(document.get("text") or document.get("content") or document.get("preview") or "")


def _doc_search_text(document: dict[str, Any]) -> str:
    metadata = _doc_metadata(document)
    haystacks = [
        _doc_text(document),
        metadata.get("headingPath"),
        metadata.get("parentSection"),
        metadata.get("section"),
        metadata.get("sourcePath"),
        metadata.get("source"),
        metadata.get("ragName"),
    ]
    return "\n".join(str(item) for item in haystacks if item)


def _doc_source_candidates(document: dict[str, Any]) -> list[str]:
    metadata = _doc_metadata(document)
    candidates = [
        metadata.get("sourcePath"),
        metadata.get("source"),
        metadata.get("file_name"),
        metadata.get("filename"),
        metadata.get("ragName"),
    ]
    return [str(item) for item in candidates if item]


def _documents_from_retrievals(retrievals: list[dict[str, Any]]) -> tuple[list[dict[str, Any]], list[dict[str, Any]]]:
    first_retrieval = retrievals[0].get("data") if retrievals else {}
    if not isinstance(first_retrieval, dict):
        first_retrieval = {}
    pre_documents = first_retrieval.get(QA_PRE_RERANK_DOCUMENTS) or []
    final_documents = first_retrieval.get(QA_RETRIEVED_DOCUMENTS) or []
    if not isinstance(pre_documents, list):
        pre_documents = []
    if not isinstance(final_documents, list):
        final_documents = []
    return pre_documents, final_documents


def _source_file_matches(document: dict[str, Any], expected_source_file: str) -> bool:
    if not expected_source_file:
        return False
    expected = expected_source_file.replace("\\", "/")
    expected_name = Path(expected).name
    for candidate in _doc_source_candidates(document):
        normalized = candidate.replace("\\", "/")
        if normalized == expected or normalized.endswith("/" + expected) or Path(normalized).name == expected_name:
            return True
    return False


def _section_matches(document: dict[str, Any], expected_section: str) -> bool:
    if not expected_section:
        return False
    section = normalize_text(expected_section)
    metadata = _doc_metadata(document)
    haystacks = [
        _doc_text(document),
        metadata.get("headingPath"),
        metadata.get("parentSection"),
        metadata.get("section"),
    ]
    return any(section and section in normalize_text(item) for item in haystacks)


def _count_points_in_documents(documents: list[dict[str, Any]], expected_points: list[str]) -> tuple[int, list[str]]:
    if not expected_points:
        return 0, []
    context_text = "\n".join(_doc_text(document) for document in documents)
    missed = [point for point in expected_points if point and point not in context_text]
    return len(expected_points) - len(missed), missed


def _coverage_layer(source_file: str,
                    source_file_pre: bool,
                    source_file_final: bool,
                    section: str,
                    section_pre: bool,
                    section_final: bool) -> str:
    if section:
        if section_final:
            return "section_in_final_context"
        if section_pre:
            return "section_lost_before_final"
    if source_file:
        if source_file_final:
            return "source_file_in_final_context"
        if source_file_pre:
            return "source_file_lost_before_final"
        return "source_file_not_in_candidates"
    return "source_not_configured"


def build_coverage(case: dict[str, Any],
                   retrievals: list[dict[str, Any]],
                   expected_points: list[str],
                   score_mode: str,
                   answer_matched_count: int) -> dict[str, Any]:
    expected_source_file = str(case.get("source_file") or "")
    expected_section = str(case.get("expected_source_section") or "")
    pre_documents, final_documents = _documents_from_retrievals(retrievals)

    source_file_pre = any(isinstance(doc, dict) and _source_file_matches(doc, expected_source_file) for doc in pre_documents)
    source_file_final = any(isinstance(doc, dict) and _source_file_matches(doc, expected_source_file) for doc in final_documents)
    section_pre = any(isinstance(doc, dict) and _section_matches(doc, expected_section) for doc in pre_documents)
    section_final = any(isinstance(doc, dict) and _section_matches(doc, expected_section) for doc in final_documents)
    pre_point_count, pre_missed_points = _count_points_in_documents(pre_documents, expected_points)
    final_point_count, final_missed_points = _count_points_in_documents(final_documents, expected_points)

    layer = _coverage_layer(
        expected_source_file,
        source_file_pre,
        source_file_final,
        expected_section,
        section_pre,
        section_final,
    )
    if not retrievals:
        layer = "no_retrieval_metadata"
    if score_mode == "literal" and expected_points and final_point_count == len(expected_points) and answer_matched_count < len(expected_points):
        layer = "answer_generation_or_literal_mismatch"

    return {
        "expected_source_file": expected_source_file,
        "expected_source_section": expected_section,
        "pre_rerank_document_count": len(pre_documents),
        "final_document_count": len(final_documents),
        "source_file_in_pre_rerank": source_file_pre,
        "source_file_in_final_context": source_file_final,
        "section_in_pre_rerank": section_pre,
        "section_in_final_context": section_final,
        "expected_points_in_pre_rerank": pre_point_count,
        "expected_points_in_final_context": final_point_count,
        "expected_points_total": len(expected_points),
        "expected_points_missing_pre_rerank": pre_missed_points,
        "expected_points_missing_final_context": final_missed_points,
        "coverage_layer": layer,
    }


def run_case(api_url: str, agent_id: str, session_prefix: str, max_step: int, timeout: int, case: dict[str, Any]) -> dict[str, Any]:
    case_id = str(case["id"])
    payload = {
        "aiAgentId": agent_id,
        "message": case["question"],
        "sessionId": f"{session_prefix}-{case_id}",
        "maxStep": max_step,
    }

    started = time.monotonic()
    events, status, error = post_sse(api_url, payload, timeout)
    duration_ms = int((time.monotonic() - started) * 1000)
    answer, completed, retrievals, sse_error = summarize_events(events)
    # HTTP 异常优先；HTTP 正常但 SSE 流里有 type=error 帧时（backend 主动发出的错误事件），
    # 用 SSE 错误作为 error，便于 markdown 报告 details 直接定位 LLM/链路失败原因。
    if not error and sse_error:
        error = sse_error
    expected_points = list(case.get("expected_points", []))
    matched_count, missed_points = keyword_check(answer, expected_points)
    score_mode = case.get("score_mode", "literal")
    coverage = build_coverage(case, retrievals, expected_points, score_mode, matched_count)
    pre_documents, final_documents = _documents_from_retrievals(retrievals)
    semantic = evaluate_expected_points_v2(case, answer, pre_documents, final_documents)
    eval_v2 = build_eval_v2(case, completed, error, answer, score_mode, matched_count, coverage, semantic)

    return {
        "id": case_id,
        "case_type": case.get("case_type", ""),
        "score_mode": score_mode,
        "question": case.get("question", ""),
        "should_answer": case.get("should_answer"),
        "expected_source_section": case.get("expected_source_section", ""),
        "expected_source_file": case.get("source_file", ""),
        "expected_points": expected_points,
        "expected_points_v2": case.get("expected_points_v2") or [],
        "status": status,
        "completed": completed,
        "duration_ms": duration_ms,
        "matched_expected_points": matched_count,
        "missed_expected_points": missed_points,
        "answer": answer,
        "error": error,
        "retrievals": retrievals,
        "coverage": coverage,
        "eval_v2": eval_v2,
        "raw_events": events,
    }


def md_escape(value: Any) -> str:
    text = str(value).replace("\n", "<br>")
    return text.replace("|", "\\|")


def answer_preview(answer: str, limit: int = 120) -> str:
    compact = " ".join(answer.split())
    if len(compact) <= limit:
        return compact
    return compact[:limit] + "..."


def _fmt_score(value: Any) -> str:
    if value is None:
        return "—"
    if isinstance(value, bool):
        return str(value).lower()
    if isinstance(value, (int, float)):
        return f"{value:.4f}"
    return str(value)


def _retrieval_summary_cells(retrievals: list[dict[str, Any]]) -> tuple[str, str, str]:
    """Return (retrieved, score_range, empty) cells for the markdown summary table.

    Step1 通常只产生 1 个 retrieval 事件；如果存在多个就汇总展示首条。
    无 retrieval 事件（旧后端 / 无 RAG 调用）一律返回 "—"。
    """
    if not retrievals:
        return "—", "—", "—"
    data = retrievals[0].get("data") or {}
    retrieved = data.get(QA_RETRIEVED_DOCUMENT_COUNT)
    min_score = data.get(QA_MIN_RETRIEVED_SCORE)
    max_score = data.get(QA_MAX_RETRIEVED_SCORE)
    empty = data.get(QA_RETRIEVAL_EMPTY)
    retrieved_cell = "—" if retrieved is None else str(retrieved)
    if min_score is None and max_score is None:
        score_cell = "—"
    else:
        score_cell = f"{_fmt_score(min_score)} .. {_fmt_score(max_score)}"
    empty_cell = "—" if empty is None else str(empty).lower()
    return retrieved_cell, score_cell, empty_cell


def _coverage_summary_cell(result: dict[str, Any]) -> str:
    coverage = result.get("coverage") or {}
    if not coverage:
        return "—"
    total = coverage.get("expected_points_total") or 0
    points_cell = "manual"
    if result.get("score_mode") != "manual" and total:
        points_cell = f"{coverage.get('expected_points_in_final_context', 0)}/{total}"
    return (
        f"{coverage.get('coverage_layer', '—')}; "
        f"file final={str(coverage.get('source_file_in_final_context')).lower()}; "
        f"section final={str(coverage.get('section_in_final_context')).lower()}; "
        f"points final={points_cell}"
    )


def _metadata_value(metadata: dict[str, Any], key: str) -> Any:
    value = metadata.get(key)
    return "—" if value is None else value


def _metadata_json_value(metadata: dict[str, Any], key: str) -> str:
    value = metadata.get(key)
    if value is None:
        return "—"
    return json.dumps(value, ensure_ascii=False, separators=(",", ":"))


def _document_preview(document: dict[str, Any], limit: int = 180) -> str:
    text = document.get("text") or document.get("content") or ""
    return answer_preview(str(text), limit)


def _append_document_list(lines: list[str], data: dict[str, Any], documents_key: str, title: str) -> None:
    documents = data.get(documents_key) or []
    if not documents:
        return

    lines.append(f"  - {title}:")
    for doc_index, document in enumerate(documents, start=1):
        if not isinstance(document, dict):
            lines.append(f"    - #{doc_index}: `{md_escape(document)}`")
            continue

        metadata = document.get("metadata") or {}
        if not isinstance(metadata, dict):
            metadata = {}

        lines.append(
            (
                "    - #{idx} score=`{score}`, source=`{source}`, chunk=`{chunk}`, "
                "retrievalSource=`{retrieval_source}`, rrfScore=`{rrf_score}`, "
                "vectorRank=`{vector_rank}`, vectorScore=`{vector_score}`, "
                "keywordRank=`{keyword_rank}`, keywordScore=`{keyword_score}`, "
                "keywordSkippedReason=`{keyword_skipped_reason}`, "
                "beforeRerankRank=`{before_rerank_rank}`, rerankRank=`{rerank_rank}`, "
                "rerankScore=`{rerank_score}`, rerankFusionScore=`{rerank_fusion_score}`, "
                "queryFusionBoostScore=`{query_fusion_boost_score}`, "
                "fusionAwareScore=`{fusion_aware_score}`, fusionAwareWeight=`{fusion_aware_weight}`, "
                "rerankApplied=`{rerank_applied}`, "
                "rerankMode=`{rerank_mode}`, coverageGuardAdded=`{coverage_guard_added}`, "
                "coverageGuardReason=`{coverage_guard_reason}`, coverageGuardCues=`{coverage_guard_cues}`, "
                "rerankVariantHitCount=`{rerank_variant_hit_count}`, "
                "bestRerankVariantRank=`{best_rerank_variant_rank}`, "
                "rerankVariantIndexes=`{rerank_variant_indexes}`, "
                "rerankVariantHits=`{rerank_variant_hits}`, "
                "queryVariantIndex=`{query_variant_index}`, "
                "queryVariantRank=`{query_variant_rank}`, queryVariantText=`{query_variant_text}`, "
                "queryFusionScore=`{query_fusion_score}`, queryFusionRank=`{query_fusion_rank}`, "
                "queryVariantHitCount=`{query_variant_hit_count}`, "
                "bestQueryVariantRank=`{best_query_variant_rank}`, "
                "queryVariantIndexes=`{query_variant_indexes}`, "
                "queryVariantHits=`{query_variant_hits}`"
            ).format(
                idx=doc_index,
                score=_fmt_score(document.get("score")),
                source=md_escape(_metadata_value(metadata, DOC_SOURCE_PATH)),
                chunk=md_escape(_metadata_value(metadata, DOC_CHUNK_INDEX)),
                retrieval_source=md_escape(_metadata_value(metadata, DOC_RETRIEVAL_SOURCE)),
                rrf_score=_fmt_score(metadata.get(DOC_RRF_SCORE)),
                vector_rank=md_escape(_metadata_value(metadata, DOC_VECTOR_RANK)),
                vector_score=_fmt_score(metadata.get(DOC_VECTOR_SCORE)),
                keyword_rank=md_escape(_metadata_value(metadata, DOC_KEYWORD_RANK)),
                keyword_score=_fmt_score(metadata.get(DOC_KEYWORD_SCORE)),
                keyword_skipped_reason=md_escape(_metadata_value(metadata, DOC_KEYWORD_SKIPPED_REASON)),
                before_rerank_rank=md_escape(_metadata_value(metadata, DOC_BEFORE_RERANK_RANK)),
                rerank_rank=md_escape(_metadata_value(metadata, DOC_RERANK_RANK)),
                rerank_score=_fmt_score(metadata.get(DOC_RERANK_SCORE)),
                rerank_fusion_score=_fmt_score(metadata.get(DOC_RERANK_FUSION_SCORE)),
                query_fusion_boost_score=_fmt_score(metadata.get(DOC_QUERY_FUSION_BOOST_SCORE)),
                fusion_aware_score=_fmt_score(metadata.get(DOC_FUSION_AWARE_SCORE)),
                fusion_aware_weight=_fmt_score(metadata.get(DOC_FUSION_AWARE_WEIGHT)),
                rerank_applied=md_escape(_metadata_value(metadata, DOC_RERANK_APPLIED)),
                rerank_mode=md_escape(_metadata_value(metadata, DOC_RERANK_MODE)),
                coverage_guard_added=md_escape(_metadata_value(metadata, DOC_COVERAGE_GUARD_ADDED)),
                coverage_guard_reason=md_escape(_metadata_value(metadata, DOC_COVERAGE_GUARD_REASON)),
                coverage_guard_cues=md_escape(_metadata_value(metadata, DOC_COVERAGE_GUARD_CUES)),
                rerank_variant_hit_count=md_escape(_metadata_value(metadata, DOC_RERANK_VARIANT_HIT_COUNT)),
                best_rerank_variant_rank=md_escape(_metadata_value(metadata, DOC_BEST_RERANK_VARIANT_RANK)),
                rerank_variant_indexes=md_escape(_metadata_value(metadata, DOC_RERANK_VARIANT_INDEXES)),
                rerank_variant_hits=md_escape(_metadata_json_value(metadata, DOC_RERANK_VARIANT_HITS)),
                query_variant_index=md_escape(_metadata_value(metadata, DOC_QUERY_VARIANT_INDEX)),
                query_variant_rank=md_escape(_metadata_value(metadata, DOC_QUERY_VARIANT_RANK)),
                query_variant_text=md_escape(_metadata_value(metadata, DOC_QUERY_VARIANT_TEXT)),
                query_fusion_score=_fmt_score(metadata.get(DOC_QUERY_FUSION_SCORE)),
                query_fusion_rank=md_escape(_metadata_value(metadata, DOC_QUERY_FUSION_RANK)),
                query_variant_hit_count=md_escape(_metadata_value(metadata, DOC_QUERY_VARIANT_HIT_COUNT)),
                best_query_variant_rank=md_escape(_metadata_value(metadata, DOC_BEST_QUERY_VARIANT_RANK)),
                query_variant_indexes=md_escape(_metadata_value(metadata, DOC_QUERY_VARIANT_INDEXES)),
                query_variant_hits=md_escape(_metadata_json_value(metadata, DOC_QUERY_VARIANT_HITS)),
            )
        )
        preview = _document_preview(document)
        if preview:
            lines.append(f"      - preview: {md_escape(preview)}")


def _append_pre_rerank_documents(lines: list[str], data: dict[str, Any]) -> None:
    _append_document_list(lines, data, QA_PRE_RERANK_DOCUMENTS, "pre_rerank_documents")


def _append_retrieved_documents(lines: list[str], data: dict[str, Any]) -> None:
    _append_document_list(lines, data, QA_RETRIEVED_DOCUMENTS, "documents")


def _rubric_coverage_cell(results: list[dict[str, Any]]) -> str:
    covered = sum(1 for result in results if (result.get("eval_v2") or {}).get("case_schema_version") == CASE_SCHEMA_VERSION_V2_COMPATIBLE)
    return f"{covered}/{len(results)}"


def _report_case_schema_version(results: list[dict[str, Any]]) -> str:
    if any((result.get("eval_v2") or {}).get("case_schema_version") == CASE_SCHEMA_VERSION_V2_COMPATIBLE for result in results):
        return CASE_SCHEMA_VERSION_V2_COMPATIBLE
    return CASE_SCHEMA_VERSION_V1_COMPATIBLE


def _aggregate_recall(results: list[dict[str, Any]], key: str) -> str:
    matched = 0
    total = 0
    coverage_key = "expected_points_in_pre_rerank" if key == "candidate" else "expected_points_in_final_context"
    for result in results:
        coverage = result.get("coverage") or {}
        point_total = int(coverage.get("expected_points_total") or 0)
        if not _evidence_scored(result.get("score_mode") or "literal", point_total):
            continue
        matched += int(coverage.get(coverage_key) or 0)
        total += point_total
    return _ratio_cell(matched, total, total > 0)


def _aggregate_literal_smoke(results: list[dict[str, Any]]) -> str:
    matched = 0
    total = 0
    for result in results:
        coverage = result.get("coverage") or {}
        point_total = int(coverage.get("expected_points_total") or 0)
        if not _evidence_scored(result.get("score_mode") or "literal", point_total):
            continue
        matched += int(result.get("matched_expected_points") or 0)
        total += point_total
    return _ratio_cell(matched, total, total > 0)


def _aggregate_eval_v2_ratio(results: list[dict[str, Any]], ratio_key: str) -> str:
    values = [
        float((result.get("eval_v2") or {}).get(ratio_key))
        for result in results
        if (result.get("eval_v2") or {}).get(ratio_key) is not None
    ]
    if not values:
        return "n/a"
    return f"{sum(values) / len(values):.1%}"


def _append_case_type_summary(lines: list[str], results: list[dict[str, Any]]) -> None:
    grouped: dict[str, list[dict[str, Any]]] = {}
    for result in results:
        grouped.setdefault(str(result.get("case_type") or "unknown"), []).append(result)

    lines.append("")
    lines.append("## Eval v2 By Case Type")
    lines.append("")
    lines.append("| case_type | cases | runtime_ok | candidate_recall | final_context_recall | semantic_avg | answer_completeness_avg | literal_smoke | failure_layers |")
    lines.append("|---|---:|---:|---|---|---:|---:|---|---|")
    for case_type in sorted(grouped):
        group = grouped[case_type]
        runtime_ok = sum(1 for item in group if (item.get("eval_v2") or {}).get("result_valid_for_scoring"))
        failure_counts: dict[str, int] = {}
        for item in group:
            layer = str((item.get("eval_v2") or {}).get("failure_layer") or "unknown")
            failure_counts[layer] = failure_counts.get(layer, 0) + 1
        failure_cell = ", ".join(f"{name}:{count}" for name, count in sorted(failure_counts.items()))
        lines.append(
            "| {case_type} | {cases} | {runtime_ok}/{cases} | {candidate} | {final} | {semantic} | {answer_completeness} | {literal} | {failures} |".format(
                case_type=md_escape(case_type),
                cases=len(group),
                runtime_ok=runtime_ok,
                candidate=_aggregate_recall(group, "candidate"),
                final=_aggregate_recall(group, "final"),
                semantic=_aggregate_eval_v2_ratio(group, "semantic_ratio"),
                answer_completeness=_aggregate_eval_v2_ratio(group, "answer_completeness_ratio"),
                literal=_aggregate_literal_smoke(group),
                failures=md_escape(failure_cell),
            )
        )


def _append_answerability_matrix(lines: list[str], results: list[dict[str, Any]]) -> None:
    counts: dict[str, int] = {}
    for result in results:
        bucket = str((result.get("eval_v2") or {}).get("answerability") or "n/a")
        counts[bucket] = counts.get(bucket, 0) + 1

    lines.append("")
    lines.append("## Answerability Matrix")
    lines.append("")
    lines.append("| bucket | count |")
    lines.append("|---|---:|")
    for bucket in ["true_answer", "true_refusal", "false_refusal", "false_answer", "n/a"]:
        lines.append(f"| {bucket} | {counts.get(bucket, 0)} |")


def write_markdown(path: Path, results: list[dict[str, Any]], api_url: str, agent_id: str) -> None:
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    lines: list[str] = []
    lines.append("# RAG Eval Result")
    lines.append("")
    lines.append(f"- generated_at: `{now}`")
    lines.append(f"- api_url: `{api_url}`")
    lines.append(f"- agent_id: `{agent_id}`")
    lines.append(f"- eval_schema_version: `{EVAL_SCHEMA_VERSION}`")
    lines.append(f"- case_schema_version: `{_report_case_schema_version(results)}`")
    lines.append(f"- rubric_coverage: `{_rubric_coverage_cell(results)}`")
    lines.append("")
    lines.append("## Summary")
    lines.append("")
    lines.append("> Eval v2 把旧 `literal_hit` 降级为 `literal_smoke`，并把一次 RAG 结果拆成 candidate -> final context -> answer 三层漏斗。Step 8.1 仍复用旧 `expected_points` 子串口径计算 candidate/final recall；真正的语义 rubric 从 Step 8.2 开始。")
    lines.append(">")
    lines.append("> `retrieved` / `score` / `empty` 三列的 `—` 表示**没拿到成功的 ChatResponse metadata**，不等价于\"无检索\"。当前实现把 retrieval SSE 帧放在 `.call().chatResponse()` 返回之后才发，所以 LLM 调用失败时（即使 RAG 检索本身成功）三列都会是 `—`。要区分\"检索失败\"和\"生成失败\"，对照 `error` 列 / details 区 / backend log。")
    lines.append(">")
    lines.append("> Details 区的 `pre_rerank_documents` 展开 rerank 前候选池，`documents` 展开最终 top-K chunk attribution；document 行的 `score` 是当前阶段写回的候选分，可能来自 HYBRID RRF、query-variant fusion、per-variant rerank RRF 或 fusion-aware rerank；原始向量分与关键词分分别看 `vectorScore` / `keywordScore`，多 query 召回融合看 `queryFusionScore/queryFusionRank/queryVariantHitCount`；最终 rerank 写回分看 `rerankScore`，多路 rerank 融合分看 `rerankFusionScore/rerankVariantHitCount`，fusion-aware 弱加成看 `queryFusionBoostScore/fusionAwareScore`；rerank 服务状态看 `rerank_runtime` 的 model / endpoint / failure_reason；keyword 分支未参与时看 `keywordSkippedReason`。")
    lines.append("")
    lines.append("| id | type | completed | duration_ms | health | failure_layer | candidate_recall | final_context_recall | c2f_delta | f2a_delta | semantic_score | answer_completeness | literal_smoke | answerability | source_coverage | answer_preview |")
    lines.append("|---|---|---:|---:|---|---|---|---|---:|---:|---|---|---|---|---|---|")
    for r in results:
        eval_v2 = r.get("eval_v2") or {}
        lines.append(
            "| {id} | {case_type} | {completed} | {duration_ms} | {health} | {failure_layer} | {candidate} | {final} | {c2f} | {f2a} | {semantic} | {answer_completeness} | {literal} | {answerability} | {coverage} | {preview} |".format(
                id=md_escape(r["id"]),
                case_type=md_escape(r["case_type"]),
                completed=str(r["completed"]).lower(),
                duration_ms=r["duration_ms"],
                health=md_escape(eval_v2.get("health") or "—"),
                failure_layer=md_escape(eval_v2.get("failure_layer") or "—"),
                candidate=eval_v2.get("candidate_recall") or "n/a",
                final=eval_v2.get("final_context_recall") or "n/a",
                c2f=eval_v2.get("candidate_to_final_delta") or "n/a",
                f2a=eval_v2.get("final_to_answer_delta") or "n/a",
                semantic=eval_v2.get("semantic_score") or "n/a",
                answer_completeness=eval_v2.get("answer_completeness") or "n/a",
                literal=eval_v2.get("literal_smoke") or "n/a",
                answerability=eval_v2.get("answerability") or "n/a",
                coverage=md_escape(_coverage_summary_cell(r)),
                preview=md_escape(answer_preview(r["answer"])),
            )
        )
    lines.append("")
    _append_case_type_summary(lines, results)
    _append_answerability_matrix(lines, results)
    lines.append("")
    lines.append("## Details")
    for r in results:
        lines.append("")
        lines.append(f"### {r['id']} - {r['case_type']}")
        lines.append("")
        lines.append(f"- question: {r['question']}")
        lines.append(f"- should_answer: `{str(r['should_answer']).lower()}`")
        lines.append(f"- expected_source_section: {r['expected_source_section']}")
        lines.append(f"- completed: `{str(r['completed']).lower()}`")
        lines.append(f"- duration_ms: `{r['duration_ms']}`")
        score_mode = r.get("score_mode") or "literal"
        lines.append(f"- score_mode: `{score_mode}`")
        eval_v2 = r.get("eval_v2") or {}
        if eval_v2:
            lines.append(
                "- eval_v2: verdict `{verdict}` / failure_layer `{failure}` / health `{health}` / valid_for_scoring `{valid}`".format(
                    verdict=eval_v2.get("verdict"),
                    failure=eval_v2.get("failure_layer"),
                    health=eval_v2.get("health"),
                    valid=str(eval_v2.get("result_valid_for_scoring")).lower(),
                )
            )
            lines.append(
                "  - case_schema_version `{case_schema}` / semantic_score `{semantic}` / literal_smoke `{literal}`".format(
                    case_schema=eval_v2.get("case_schema_version"),
                    semantic=eval_v2.get("semantic_score"),
                    literal=eval_v2.get("literal_smoke"),
                )
            )
            lines.append(
                "  - answer_completeness `{answer_completeness}` / evidence_source_unverified `{unverified}`".format(
                    answer_completeness=eval_v2.get("answer_completeness"),
                    unverified=str(eval_v2.get("evidence_source_unverified")).lower(),
                )
            )
            lines.append(
                "  - semantic_hits `{hits}` / semantic_misses `{misses}`".format(
                    hits=md_escape(" | ".join(str(item) for item in eval_v2.get("semantic_hits") or []) or "—"),
                    misses=md_escape(" | ".join(str(item) for item in eval_v2.get("semantic_misses") or []) or "—"),
                )
            )
            lines.append(
                "  - evidence_funnel: candidate `{candidate}` -> final_context `{final}` -> answer `{literal}`".format(
                    candidate=eval_v2.get("candidate_recall"),
                    final=eval_v2.get("final_context_recall"),
                    literal=eval_v2.get("literal_smoke"),
                )
            )
            lines.append(
                "  - deltas: candidate_to_final `{c2f}` / final_to_answer `{f2a}`".format(
                    c2f=eval_v2.get("candidate_to_final_delta"),
                    f2a=eval_v2.get("final_to_answer_delta"),
                )
            )
            lines.append(
                "  - answerability: bucket `{bucket}` / did_answer `{did_answer}`".format(
                    bucket=eval_v2.get("answerability"),
                    did_answer=str(eval_v2.get("did_answer")).lower(),
                )
            )
        if r["error"]:
            lines.append(f"- error: `{r['error']}`")
        coverage = r.get("coverage") or {}
        if coverage:
            pre_points_cell = "manual"
            final_points_cell = "manual"
            if r.get("score_mode") != "manual":
                pre_points_cell = f"{coverage.get('expected_points_in_pre_rerank')}/{coverage.get('expected_points_total')}"
                final_points_cell = f"{coverage.get('expected_points_in_final_context')}/{coverage.get('expected_points_total')}"
            lines.append(
                "- source_coverage: layer `{layer}` / source_file `{source_file}` / section `{section}`".format(
                    layer=coverage.get("coverage_layer"),
                    source_file=coverage.get("expected_source_file") or "—",
                    section=coverage.get("expected_source_section") or "—",
                )
            )
            lines.append(
                "  - pre_rerank: docs `{docs}` / source_file `{source_file}` / section `{section}` / expected_points_exact `{points}`".format(
                    docs=coverage.get("pre_rerank_document_count"),
                    source_file=str(coverage.get("source_file_in_pre_rerank")).lower(),
                    section=str(coverage.get("section_in_pre_rerank")).lower(),
                    points=pre_points_cell,
                )
            )
            lines.append(
                "  - final_context: docs `{docs}` / source_file `{source_file}` / section `{section}` / expected_points_exact `{points}`".format(
                    docs=coverage.get("final_document_count"),
                    source_file=str(coverage.get("source_file_in_final_context")).lower(),
                    section=str(coverage.get("section_in_final_context")).lower(),
                    points=final_points_cell,
                )
            )
        lines.append("- expected_points:")
        for point in r["expected_points"]:
            if score_mode == "manual":
                lines.append(f"  - {point}")
            else:
                mark = " " if point in r["missed_expected_points"] else "x"
                lines.append(f"  - [{mark}] {point}")
        retrievals = r.get("retrievals") or []
        if retrievals:
            lines.append("")
            lines.append("retrieval:")
            for idx, retrieval in enumerate(retrievals, start=1):
                step = retrieval.get("step")
                data = retrieval.get("data") or {}
                header = f"- event #{idx}" + (f" (step={step})" if step is not None else "")
                lines.append(header)
                lines.append(f"  - retrieved_document_count: `{data.get(QA_RETRIEVED_DOCUMENT_COUNT)}`")
                lines.append(f"  - retrieval_empty: `{str(data.get(QA_RETRIEVAL_EMPTY)).lower()}`")
                lines.append(f"  - similarity_threshold: `{_fmt_score(data.get(QA_SIMILARITY_THRESHOLD))}`")
                lines.append(
                    f"  - candidate_similarity_threshold: "
                    f"`{_fmt_score(data.get(QA_CANDIDATE_SIMILARITY_THRESHOLD))}`"
                )
                lines.append(
                    "  - score_range: `{lo} .. {hi}`".format(
                        lo=_fmt_score(data.get(QA_MIN_RETRIEVED_SCORE)),
                        hi=_fmt_score(data.get(QA_MAX_RETRIEVED_SCORE)),
                    )
                )
                lines.append(
                    "  - context_selected: `{sel}` / dropped: `{drop}` / truncated: `{trunc}`".format(
                        sel=data.get(QA_CONTEXT_SELECTED_COUNT),
                        drop=data.get(QA_CONTEXT_DROPPED_COUNT),
                        trunc=str(data.get(QA_CONTEXT_TRUNCATED)).lower(),
                    )
                )
                lines.append(
                    "  - context_chars: actual `{act}` / max `{mx}`".format(
                        act=data.get(QA_CONTEXT_ACTUAL_CHARS),
                        mx=data.get(QA_CONTEXT_MAX_CHARS),
                    )
                )
                salience_cues = data.get(QA_CONTEXT_SALIENCE_CUES) or []
                if not isinstance(salience_cues, list):
                    salience_cues = [salience_cues]
                lines.append(
                    "  - context_salience: cues `{cues}` / expansions `{expansions}`".format(
                        cues=md_escape(" | ".join(str(item) for item in salience_cues) or "—"),
                        expansions=data.get(QA_CONTEXT_SALIENCE_EXPANSION_COUNT) or 0,
                    )
                )
                lines.append(
                    "  - rerank: applied `{applied}` / mode `{mode}` / candidates `{cand}` / final `{final}`".format(
                        applied=str(data.get(QA_RERANK_APPLIED)).lower(),
                        mode=data.get(QA_RERANK_MODE),
                        cand=data.get(QA_RERANK_CANDIDATE_COUNT),
                        final=data.get(QA_RERANK_FINAL_COUNT),
                    )
                )
                lines.append(
                    "  - rerank_runtime: model `{model}` / endpoint `{endpoint}` / failure_reason `{reason}`".format(
                        model=data.get(QA_RERANK_MODEL_NAME),
                        endpoint=data.get(QA_RERANK_ENDPOINT),
                        reason=data.get(QA_RERANK_FAILURE_REASON),
                    )
                )
                lines.append(
                    "  - rerank_query: policy `{policy}` / text `{text}`".format(
                        policy=data.get(QA_RERANK_QUERY_POLICY) or "—",
                        text=md_escape(data.get(QA_RERANK_QUERY_TEXT) or "—"),
                    )
                )
                lines.append(
                    "  - coverage_guard: applied `{applied}` / added `{added}`".format(
                        applied=str(data.get(QA_RERANK_COVERAGE_GUARD_APPLIED)).lower(),
                        added=data.get(QA_RERANK_COVERAGE_GUARD_ADDED_COUNT),
                    )
                )
                query_variants = data.get(QA_QUERY_VARIANT_TEXTS) or []
                if not isinstance(query_variants, list):
                    query_variants = [query_variants]
                lines.append(
                    "  - query_rewrite: requested `{requested}` / mode `{mode}` / variants `{count}` / elapsed_ms `{elapsed}` / failure `{failure}` / attempts `{attempts}` / texts `{texts}`".format(
                        requested=data.get(QA_QUERY_REWRITE_REQUESTED_POLICY) or "—",
                        mode=data.get(QA_QUERY_REWRITE_MODE),
                        count=data.get(QA_QUERY_VARIANT_COUNT),
                        elapsed=data.get(QA_QUERY_REWRITE_ELAPSED_MS),
                        failure=md_escape(data.get(QA_QUERY_REWRITE_FAILURE_REASON) or "—"),
                        attempts=md_escape(data.get(QA_QUERY_REWRITE_ATTEMPT_TRACE) or "—"),
                        texts=md_escape(" | ".join(str(item) for item in query_variants)),
                    )
                )
                selected_profile_hints = data.get(QA_QUERY_REWRITE_SELECTED_PROFILE_HINTS) or []
                if not isinstance(selected_profile_hints, list):
                    selected_profile_hints = [selected_profile_hints]
                lines.append(
                    "  - profile_hints: version `{version}` / source `{source}` / selected `{selected}`".format(
                        version=data.get(QA_QUERY_REWRITE_PROFILE_VERSION) or "—",
                        source=data.get(QA_QUERY_REWRITE_PROFILE_SOURCE) or "—",
                        selected=md_escape(" | ".join(str(item) for item in selected_profile_hints) or "—"),
                    )
                )
                _append_pre_rerank_documents(lines, data)
                _append_retrieved_documents(lines, data)
        lines.append("")
        lines.append("answer:")
        lines.append("")
        lines.append("```text")
        lines.append(r["answer"] or "")
        lines.append("```")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser(description="Run minimal RAG eval cases against local Agent SSE API.")
    parser.add_argument("--cases", type=Path, default=Path(__file__).with_name("cases.json"))
    parser.add_argument("--api-url", default=DEFAULT_API_URL)
    parser.add_argument("--agent-id", default="rag_demo")
    parser.add_argument("--session-prefix", default=f"rag-eval-{datetime.now().strftime('%Y%m%d%H%M%S')}")
    parser.add_argument("--max-step", type=int, default=1)
    parser.add_argument("--timeout", type=int, default=120)
    parser.add_argument("--limit", type=int, default=0, help="Run only the first N cases. 0 means all cases. Ignored when --case-id is given.")
    parser.add_argument(
        "--case-id",
        dest="case_ids",
        action="append",
        default=None,
        help="Run only the cases with the given id. Repeatable, e.g. --case-id RAG-04 --case-id RAG-07.",
    )
    parser.add_argument("--output", type=Path, default=Path(__file__).with_name("rag-eval-result.md"))
    args = parser.parse_args()

    cases = load_cases(args.cases)
    if args.case_ids:
        wanted = {cid.strip() for cid in args.case_ids if cid and cid.strip()}
        cases = [c for c in cases if str(c.get("id")) in wanted]
        if not cases:
            print(f"[RAG eval] no cases matched ids: {sorted(wanted)}", file=sys.stderr)
            return 2
    elif args.limit > 0:
        cases = cases[: args.limit]

    results: list[dict[str, Any]] = []
    for case in cases:
        print(f"[RAG eval] running {case['id']}: {case['question']}", file=sys.stderr)
        result = run_case(args.api_url, args.agent_id, args.session_prefix, args.max_step, args.timeout, case)
        results.append(result)
        status = "ok" if result["completed"] and not result["error"] else "check"
        print(f"[RAG eval] {case['id']} {status}, duration={result['duration_ms']}ms", file=sys.stderr)

    write_markdown(args.output, results, args.api_url, args.agent_id)
    print(str(args.output))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
