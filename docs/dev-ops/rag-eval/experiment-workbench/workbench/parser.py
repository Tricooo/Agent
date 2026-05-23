from __future__ import annotations

import re
from pathlib import Path
from typing import Any


CASE_HEADING_RE = re.compile(r"^###\s+([A-Z][A-Z0-9]*(?:-[A-Z0-9]+)*-\d+)(?:\s*-\s*(.+))?\s*$")


def parse_number(value: str | None) -> float | int | None:
    if value is None:
        return None
    raw = str(value).strip().strip("`")
    if raw in {"", "—", "-", "None", "null"}:
        return None
    try:
        num = float(raw)
    except ValueError:
        return None
    return int(num) if num.is_integer() else num


def parse_bool(value: str | None) -> bool | None:
    if value is None:
        return None
    raw = str(value).strip().strip("`").lower()
    if raw in {"true", "yes", "1"}:
        return True
    if raw in {"false", "no", "0"}:
        return False
    return None


def strip_md_inline(value: str | None) -> str:
    if value is None:
        return ""
    text = str(value).strip()
    if len(text) >= 2 and text[0] == "`" and text[-1] == "`":
        text = text[1:-1]
    return text.replace("\\|", "|").strip()


def split_markdown_row(line: str) -> list[str]:
    text = line.strip()
    if text.startswith("|"):
        text = text[1:]
    if text.endswith("|"):
        text = text[:-1]
    cells: list[str] = []
    cur: list[str] = []
    escaped = False
    for ch in text:
        if escaped:
            cur.append("\\" + ch if ch != "|" else "|")
            escaped = False
            continue
        if ch == "\\":
            escaped = True
            continue
        if ch == "|":
            cells.append("".join(cur).strip())
            cur = []
            continue
        cur.append(ch)
    cells.append("".join(cur).strip())
    return cells


def parse_score_range(value: str | None) -> tuple[float | None, float | None]:
    if not value:
        return None, None
    nums = re.findall(r"-?\d+(?:\.\d+)?", str(value))
    if not nums:
        return None, None
    if len(nums) == 1:
        n = float(nums[0])
        return n, n
    return float(nums[0]), float(nums[1])


def parse_literal_hit(value: str | None) -> tuple[str | None, float | None, int | None, int | None]:
    raw = strip_md_inline(value)
    if not raw or raw == "—":
        return None, None, None, None
    if raw.lower() == "manual":
        return "manual", None, None, None
    m = re.search(r"(\d+)\s*/\s*(\d+)", raw)
    if not m:
        return raw, None, None, None
    hit = int(m.group(1))
    total = int(m.group(2))
    ratio = hit / total if total else None
    return raw, ratio, hit, total


def parse_header(lines: list[str]) -> dict[str, str]:
    header: dict[str, str] = {}
    for line in lines[:40]:
        m = re.match(r"^\s*-\s*([\w_-]+):\s*`?(.*?)`?\s*$", line)
        if m:
            header[m.group(1)] = m.group(2).strip("`").strip()
    return header


def parse_summary_table(lines: list[str], warnings: list[str]) -> tuple[list[str], list[dict[str, str]]]:
    start = next((i for i, line in enumerate(lines) if line.strip() == "## Summary"), -1)
    if start < 0:
        warnings.append("summary_section_missing")
        return [], []
    header_idx = -1
    for i in range(start + 1, min(len(lines), start + 80)):
        if lines[i].strip().startswith("|") and " id " in f" {lines[i].lower()} ":
            header_idx = i
            break
    if header_idx < 0:
        warnings.append("summary_table_missing")
        return [], []
    headers = [strip_md_inline(c) for c in split_markdown_row(lines[header_idx])]
    rows: list[dict[str, str]] = []
    i = header_idx + 2
    while i < len(lines):
        line = lines[i]
        if line.startswith("## "):
            break
        if not line.strip().startswith("|"):
            i += 1
            continue
        cells = split_markdown_row(line)
        if len(cells) < len(headers):
            cells.extend([""] * (len(headers) - len(cells)))
        rows.append(dict(zip(headers, cells)))
        i += 1
    if not rows:
        warnings.append("summary_rows_empty")
    return headers, rows


def parse_parts(body: str) -> dict[str, str]:
    return {k: v for k, v in re.findall(r"(\w+)\s*`([^`]*)`", body)}


def parse_doc_inline_kv(line: str) -> dict[str, Any]:
    out: dict[str, Any] = {}
    m = re.match(r"^\s*-?\s*#(\d+)\s+", line)
    if m:
        out["rank"] = int(m.group(1))
    for key, value in re.findall(r"(\w+)=`([^`]*)`", line):
        out[key] = value
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
    ):
        if key in out and out[key] not in {"", "—", "-"}:
            try:
                out[key] = float(out[key])
            except ValueError:
                pass
    for key in (
        "chunk",
        "vectorRank",
        "keywordRank",
        "beforeRerankRank",
        "rerankRank",
        "queryVariantIndex",
        "queryVariantRank",
        "queryFusionRank",
        "queryVariantHitCount",
        "rerankVariantHitCount",
        "bestQueryVariantRank",
        "bestRerankVariantRank",
    ):
        if key in out and out[key] not in {"", "—", "-"}:
            try:
                out[key] = int(float(out[key]))
            except ValueError:
                pass
    for key in ("rerankApplied", "coverageGuardAdded"):
        if key in out:
            out[key] = parse_bool(out[key])
    return out


def parse_docs_block(lines: list[str], start_idx: int) -> tuple[list[dict[str, Any]], int]:
    docs: list[dict[str, Any]] = []
    cur: dict[str, Any] | None = None
    i = start_idx
    while i < len(lines):
        line = lines[i]
        stripped = line.strip()
        if line.startswith("### ") or line.startswith("## "):
            break
        if stripped.startswith("answer:"):
            break
        if re.match(r"^\s*-\s*#\d+", line):
            if cur is not None:
                docs.append(cur)
            cur = parse_doc_inline_kv(line)
        elif stripped.startswith("- preview:") and cur is not None:
            cur["preview"] = stripped[len("- preview:") :].strip()
        elif stripped.startswith("preview:") and cur is not None:
            cur["preview"] = stripped[len("preview:") :].strip()
        elif re.match(r"^\s*-\s*[A-Za-z_]+:", line):
            break
        elif stripped == "":
            pass
        elif not line.startswith(" "):
            break
        i += 1
    if cur is not None:
        docs.append(cur)
    return docs, i


def parse_source_coverage(body: str) -> dict[str, Any]:
    parts = parse_parts(body)
    return {
        "label": parts.get("layer"),
        "source_file": parts.get("source_file"),
        "section": parts.get("section"),
    }


def parse_coverage_detail(body: str) -> dict[str, Any]:
    parts = parse_parts(body)
    result: dict[str, Any] = {}
    if "docs" in parts:
        result["docs"] = parse_number(parts["docs"])
    if "source_file" in parts:
        result["source_file"] = parse_bool(parts["source_file"])
    if "section" in parts:
        result["section"] = parse_bool(parts["section"])
    if "expected_points_exact" in parts:
        result["expected_points_exact"] = parts["expected_points_exact"]
    return result


def parse_details_section(lines: list[str], start_idx: int, warnings: list[str]) -> tuple[dict[str, Any], int]:
    match = CASE_HEADING_RE.match(lines[start_idx].strip())
    if not match:
        warnings.append(f"detail_heading_unparsed:{lines[start_idx].strip()[:80]}")
        return {"case_id": "UNKNOWN"}, start_idx + 1

    detail: dict[str, Any] = {
        "case_id": match.group(1),
        "case_type": (match.group(2) or "").strip() or None,
        "expected_points": [],
        "retrieval_events": [],
        "pre_rerank_documents": [],
        "documents": [],
        "source_coverage": {},
    }
    i = start_idx + 1
    while i < len(lines):
        line = lines[i]
        stripped = line.rstrip()
        if CASE_HEADING_RE.match(stripped.strip()) or stripped.startswith("## "):
            break

        meta = re.match(
            r"^\s*-\s*(question|should_answer|expected_source_section|completed|duration_ms|score_mode|error):\s*`?(.*?)`?\s*$",
            stripped,
        )
        if meta:
            key, value = meta.group(1), meta.group(2).strip("`").strip()
            if key in {"should_answer", "completed"}:
                detail[key] = parse_bool(value)
            elif key == "duration_ms":
                detail[key] = parse_number(value)
            else:
                detail[key] = value
            i += 1
            continue

        if re.match(r"^\s*-\s*expected_points\s*:", stripped):
            i += 1
            while i < len(lines):
                point = re.match(r"^\s*-\s*\[( |x|X)\]\s+(.*)$", lines[i])
                if not point:
                    break
                detail["expected_points"].append(
                    {"point": point.group(2).strip(), "matched": point.group(1).lower() == "x"}
                )
                i += 1
            continue

        source_line = re.match(r"^\s*-\s*source_coverage:\s*(.+)$", stripped)
        if source_line:
            detail["source_coverage"].update(parse_source_coverage(source_line.group(1)))
            i += 1
            continue

        coverage_detail = re.match(r"^\s*-\s*(pre_rerank|final_context):\s*(.+)$", stripped)
        if coverage_detail:
            detail["source_coverage"][coverage_detail.group(1)] = parse_coverage_detail(coverage_detail.group(2))
            i += 1
            continue

        if stripped.strip() == "retrieval:":
            event: dict[str, Any] | None = None
            i += 1
            while i < len(lines):
                rl = lines[i]
                if CASE_HEADING_RE.match(rl.strip()) or rl.startswith("## "):
                    break
                text = rl.strip()
                if text.startswith("answer:") or rl.startswith("```"):
                    break
                ev_match = re.match(r"^\s*-\s*event\s*#(\d+)", rl)
                if ev_match:
                    if event is not None:
                        detail["retrieval_events"].append(event)
                    event = {"event_index": int(ev_match.group(1))}
                    i += 1
                    continue
                if event is None:
                    if text.startswith("-"):
                        event = {"event_index": 1}
                    else:
                        i += 1
                        continue
                if not text.startswith("-"):
                    i += 1
                    continue
                body = text.lstrip("-").strip()
                simple = re.match(r"^([\w_]+):\s*`([^`]+)`\s*$", body)
                if simple:
                    key, value = simple.group(1), simple.group(2).strip()
                    if key in {"retrieved_document_count", "similarity_threshold", "candidate_similarity_threshold"}:
                        event[key] = parse_number(value)
                    elif key == "retrieval_empty":
                        event[key] = parse_bool(value)
                    elif key == "score_range":
                        event[key] = value
                        event["score_min"], event["score_max"] = parse_score_range(value)
                    else:
                        event[key] = value
                    i += 1
                    continue
                if body.startswith("context_selected"):
                    nums = re.findall(r"`([^`]+)`", body)
                    if len(nums) >= 3:
                        event["context_selected"] = parse_number(nums[0])
                        event["context_dropped"] = parse_number(nums[1])
                        event["context_truncated"] = parse_bool(nums[2])
                    i += 1
                    continue
                if body.startswith("context_chars"):
                    nums = re.findall(r"`([^`]+)`", body)
                    if len(nums) >= 2:
                        event["context_chars_actual"] = parse_number(nums[0])
                        event["context_chars_max"] = parse_number(nums[1])
                    i += 1
                    continue
                if body.startswith("context_salience"):
                    parts = parse_parts(body)
                    cues = [x.strip() for x in (parts.get("cues") or "").split("|") if x.strip()]
                    event["context_salience_cues"] = cues
                    event["context_salience_expansions"] = parse_number(parts.get("expansions"))
                    i += 1
                    continue
                if body.startswith("rerank:"):
                    parts = parse_parts(body)
                    event["rerank_applied"] = parse_bool(parts.get("applied"))
                    event["rerank_mode"] = parts.get("mode")
                    event["rerank_candidates"] = parse_number(parts.get("candidates"))
                    event["rerank_final"] = parse_number(parts.get("final"))
                    i += 1
                    continue
                if body.startswith("rerank_runtime"):
                    parts = parse_parts(body)
                    event["rerank_runtime_model"] = parts.get("model")
                    event["rerank_runtime_endpoint"] = parts.get("endpoint")
                    event["rerank_runtime_failure_reason"] = parts.get("failure_reason")
                    i += 1
                    continue
                if body.startswith("query_rewrite"):
                    parts = parse_parts(body)
                    event["query_rewrite_requested"] = parts.get("requested")
                    event["query_rewrite_mode"] = parts.get("mode")
                    event["query_rewrite_variants_count"] = parse_number(parts.get("variants"))
                    event["query_rewrite_elapsed_ms"] = parse_number(parts.get("elapsed_ms"))
                    event["query_rewrite_failure"] = parts.get("failure")
                    event["query_rewrite_attempts"] = parts.get("attempts")
                    event["query_rewrite_texts"] = [
                        item.strip() for item in (parts.get("texts") or "").split("|") if item.strip()
                    ]
                    i += 1
                    continue
                if body.startswith("profile_hints"):
                    parts = parse_parts(body)
                    event["profile_version"] = parts.get("version")
                    event["profile_source"] = parts.get("source")
                    event["selected_profile_hints"] = [
                        item.strip() for item in (parts.get("selected") or "").split("|") if item.strip()
                    ]
                    i += 1
                    continue
                if body.startswith("rerank_query"):
                    parts = parse_parts(body)
                    event["rerank_query_policy"] = parts.get("policy")
                    event["rerank_query_text"] = parts.get("text")
                    i += 1
                    continue
                if body.startswith("coverage_guard"):
                    parts = parse_parts(body)
                    event["coverage_guard_applied"] = parse_bool(parts.get("applied"))
                    event["coverage_guard_added"] = parse_number(parts.get("added"))
                    i += 1
                    continue
                if body.startswith("pre_rerank_documents:") or body.startswith("documents:"):
                    stage = "pre_rerank" if body.startswith("pre_rerank_documents:") else "final"
                    docs, next_i = parse_docs_block(lines, i + 1)
                    event[f"{stage}_documents"] = docs
                    detail["pre_rerank_documents" if stage == "pre_rerank" else "documents"].extend(docs)
                    i = next_i
                    continue
                i += 1
            if event is not None:
                detail["retrieval_events"].append(event)
            continue

        if stripped.strip() == "answer:":
            j = i + 1
            while j < len(lines) and lines[j].strip() == "":
                j += 1
            if j < len(lines) and lines[j].strip().startswith("```"):
                j += 1
                buf: list[str] = []
                while j < len(lines):
                    if lines[j].strip().startswith("```"):
                        j += 1
                        break
                    buf.append(lines[j])
                    j += 1
                detail["answer"] = "\n".join(buf).strip()
                i = j
                continue
        i += 1
    return detail, i


def parse_report(path: Path) -> dict[str, Any]:
    warnings: list[str] = []
    try:
        text = path.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        text = path.read_text(encoding="utf-8", errors="replace")
        warnings.append("utf8_replacement_used")
    lines = text.splitlines()
    header = parse_header(lines)
    headers, summary_rows = parse_summary_table(lines, warnings)
    results: dict[str, dict[str, Any]] = {}
    for row in summary_rows:
        case_id = strip_md_inline(row.get("id"))
        if not case_id:
            warnings.append("summary_row_without_id")
            continue
        score_min, score_max = parse_score_range(row.get("score"))
        literal_raw, literal_ratio, hit_count, point_count = parse_literal_hit(
            row.get("literal_hit") or row.get("matched_points")
        )
        results[case_id] = {
            "case_id": case_id,
            "case_type": strip_md_inline(row.get("type")),
            "completed": parse_bool(row.get("completed")),
            "duration_ms": parse_number(row.get("duration_ms")),
            "should_answer": parse_bool(row.get("should_answer")),
            "retrieved": parse_number(row.get("retrieved")),
            "score_range": strip_md_inline(row.get("score")),
            "score_min": score_min,
            "score_max": score_max,
            "empty": parse_bool(row.get("empty")),
            "literal_hit": literal_raw,
            "literal_ratio": literal_ratio,
            "hit_count": hit_count,
            "expected_point_count": point_count,
            "missing_points_text": strip_md_inline(row.get("missed_points")),
            "answer_preview": strip_md_inline(row.get("answer_preview")),
            "manual_pass": strip_md_inline(row.get("manual_pass")),
            "source_coverage_label": strip_md_inline(row.get("source_coverage")),
        }

    details: dict[str, dict[str, Any]] = {}
    i = 0
    while i < len(lines):
        if CASE_HEADING_RE.match(lines[i].strip()):
            detail, next_i = parse_details_section(lines, i, warnings)
            case_id = detail.get("case_id")
            if case_id and case_id != "UNKNOWN":
                details[case_id] = detail
            i = next_i
        else:
            i += 1

    for case_id, detail in details.items():
        if case_id not in results:
            results[case_id] = {
                "case_id": case_id,
                "case_type": detail.get("case_type"),
                "completed": detail.get("completed"),
                "duration_ms": detail.get("duration_ms"),
                "should_answer": detail.get("should_answer"),
                "literal_hit": None,
                "literal_ratio": None,
                "hit_count": None,
                "expected_point_count": len(detail.get("expected_points") or []) or None,
                "source_coverage_label": (detail.get("source_coverage") or {}).get("label"),
            }

    if not details:
        warnings.append("detail_sections_empty")

    return {
        "header": header,
        "schema_columns": headers,
        "schema_era": detect_schema_era(headers),
        "case_results": results,
        "details": details,
        "warnings": sorted(set(warnings)),
    }


def detect_schema_era(headers: list[str]) -> str:
    header_set = set(headers)
    if {"retrieved", "score", "empty"} <= header_set:
        return "new"
    if "literal_hit" in header_set:
        return "triangle"
    return "legacy"
