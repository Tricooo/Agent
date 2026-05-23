#!/usr/bin/env python3
"""
Minimal RAG eval runner for local Agent API.

Route A: do not require backend metadata in API response. It records SSE summary,
completion status, duration, and a lightweight keyword check for manual review.
"""

from __future__ import annotations

import argparse
import json
import sys
import time
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


def normalize_text(value: Any) -> str:
    return " ".join(str(value or "").lower().split())


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


def _doc_metadata(document: dict[str, Any]) -> dict[str, Any]:
    metadata = document.get("metadata") or {}
    return metadata if isinstance(metadata, dict) else {}


def _doc_text(document: dict[str, Any]) -> str:
    return str(document.get("text") or document.get("content") or document.get("preview") or "")


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
    first_retrieval = retrievals[0].get("data") if retrievals else {}
    if not isinstance(first_retrieval, dict):
        first_retrieval = {}
    pre_documents = first_retrieval.get(QA_PRE_RERANK_DOCUMENTS) or []
    final_documents = first_retrieval.get(QA_RETRIEVED_DOCUMENTS) or []
    if not isinstance(pre_documents, list):
        pre_documents = []
    if not isinstance(final_documents, list):
        final_documents = []

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

    return {
        "id": case_id,
        "case_type": case.get("case_type", ""),
        "score_mode": score_mode,
        "question": case.get("question", ""),
        "should_answer": case.get("should_answer"),
        "expected_source_section": case.get("expected_source_section", ""),
        "expected_source_file": case.get("source_file", ""),
        "expected_points": expected_points,
        "status": status,
        "completed": completed,
        "duration_ms": duration_ms,
        "matched_expected_points": matched_count,
        "missed_expected_points": missed_points,
        "answer": answer,
        "error": error,
        "retrievals": retrievals,
        "coverage": coverage,
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


def write_markdown(path: Path, results: list[dict[str, Any]], api_url: str, agent_id: str) -> None:
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    lines: list[str] = []
    lines.append("# RAG Eval Result")
    lines.append("")
    lines.append(f"- generated_at: `{now}`")
    lines.append(f"- api_url: `{api_url}`")
    lines.append(f"- agent_id: `{agent_id}`")
    lines.append("")
    lines.append("## Summary")
    lines.append("")
    lines.append("> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。")
    lines.append(">")
    lines.append("> `retrieved` / `score` / `empty` 三列的 `—` 表示**没拿到成功的 ChatResponse metadata**，不等价于\"无检索\"。当前实现把 retrieval SSE 帧放在 `.call().chatResponse()` 返回之后才发，所以 LLM 调用失败时（即使 RAG 检索本身成功）三列都会是 `—`。要区分\"检索失败\"和\"生成失败\"，对照 `error` 列 / details 区 / backend log。")
    lines.append(">")
    lines.append("> Details 区的 `pre_rerank_documents` 展开 rerank 前候选池，`documents` 展开最终 top-K chunk attribution；document 行的 `score` 是当前阶段写回的候选分，可能来自 HYBRID RRF、query-variant fusion、per-variant rerank RRF 或 fusion-aware rerank；原始向量分与关键词分分别看 `vectorScore` / `keywordScore`，多 query 召回融合看 `queryFusionScore/queryFusionRank/queryVariantHitCount`；最终 rerank 写回分看 `rerankScore`，多路 rerank 融合分看 `rerankFusionScore/rerankVariantHitCount`，fusion-aware 弱加成看 `queryFusionBoostScore/fusionAwareScore`；rerank 服务状态看 `rerank_runtime` 的 model / endpoint / failure_reason；keyword 分支未参与时看 `keywordSkippedReason`。")
    lines.append("")
    lines.append("| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | source_coverage | missed_points | answer_preview | manual_pass |")
    lines.append("|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|---|")
    for r in results:
        score_mode = r.get("score_mode") or "literal"
        if score_mode == "manual":
            literal_cell = "manual"
            missed_cell = "—"
        else:
            literal_cell = f"{r['matched_expected_points']}/{len(r['expected_points'])}"
            missed_cell = md_escape(", ".join(r["missed_expected_points"]))
        retrieved_cell, score_cell, empty_cell = _retrieval_summary_cells(r.get("retrievals") or [])
        lines.append(
            "| {id} | {case_type} | {completed} | {duration_ms} | {should_answer} | {retrieved} | {score} | {empty} | {literal} | {coverage} | {missed} | {preview} |  |".format(
                id=md_escape(r["id"]),
                case_type=md_escape(r["case_type"]),
                completed=str(r["completed"]).lower(),
                duration_ms=r["duration_ms"],
                should_answer=str(r["should_answer"]).lower(),
                retrieved=retrieved_cell,
                score=score_cell,
                empty=empty_cell,
                literal=literal_cell,
                coverage=md_escape(_coverage_summary_cell(r)),
                missed=missed_cell,
                preview=md_escape(answer_preview(r["answer"])),
            )
        )
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
