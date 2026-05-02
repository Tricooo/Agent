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
        if event_type == "complete" or event.get("completed") is True:
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

    return {
        "id": case_id,
        "case_type": case.get("case_type", ""),
        "score_mode": case.get("score_mode", "literal"),
        "question": case.get("question", ""),
        "should_answer": case.get("should_answer"),
        "expected_source_section": case.get("expected_source_section", ""),
        "expected_points": expected_points,
        "status": status,
        "completed": completed,
        "duration_ms": duration_ms,
        "matched_expected_points": matched_count,
        "missed_expected_points": missed_points,
        "answer": answer,
        "error": error,
        "retrievals": retrievals,
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
    retrieved = data.get("qa_retrieved_document_count")
    min_score = data.get("qa_min_retrieved_score")
    max_score = data.get("qa_max_retrieved_score")
    empty = data.get("qa_retrieval_empty")
    retrieved_cell = "—" if retrieved is None else str(retrieved)
    if min_score is None and max_score is None:
        score_cell = "—"
    else:
        score_cell = f"{_fmt_score(min_score)} .. {_fmt_score(max_score)}"
    empty_cell = "—" if empty is None else str(empty).lower()
    return retrieved_cell, score_cell, empty_cell


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
    lines.append("")
    lines.append("| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |")
    lines.append("|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|")
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
            "| {id} | {case_type} | {completed} | {duration_ms} | {should_answer} | {retrieved} | {score} | {empty} | {literal} | {missed} | {preview} |  |".format(
                id=md_escape(r["id"]),
                case_type=md_escape(r["case_type"]),
                completed=str(r["completed"]).lower(),
                duration_ms=r["duration_ms"],
                should_answer=str(r["should_answer"]).lower(),
                retrieved=retrieved_cell,
                score=score_cell,
                empty=empty_cell,
                literal=literal_cell,
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
                lines.append(f"  - retrieved_document_count: `{data.get('qa_retrieved_document_count')}`")
                lines.append(f"  - retrieval_empty: `{str(data.get('qa_retrieval_empty')).lower()}`")
                lines.append(f"  - similarity_threshold: `{_fmt_score(data.get('qa_similarity_threshold'))}`")
                lines.append(
                    "  - score_range: `{lo} .. {hi}`".format(
                        lo=_fmt_score(data.get("qa_min_retrieved_score")),
                        hi=_fmt_score(data.get("qa_max_retrieved_score")),
                    )
                )
                lines.append(
                    "  - context_selected: `{sel}` / dropped: `{drop}` / truncated: `{trunc}`".format(
                        sel=data.get("qa_context_selected_count"),
                        drop=data.get("qa_context_dropped_count"),
                        trunc=str(data.get("qa_context_truncated")).lower(),
                    )
                )
                lines.append(
                    "  - context_chars: actual `{act}` / max `{mx}`".format(
                        act=data.get("qa_context_actual_chars"),
                        mx=data.get("qa_context_max_chars"),
                    )
                )
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
