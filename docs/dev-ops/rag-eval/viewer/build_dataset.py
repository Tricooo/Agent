#!/usr/bin/env python3
"""Build dataset.json from all RAG eval markdown reports.

Reads:  ../cases.json + ../results/**/rag-eval-result*.md + ../results/**/rag-eval-retrievalContext*.md + ../results/**/probe-*.md
Writes: data/dataset.json (consumed by index.html)

Schema-tolerant: handles 3 schema eras (legacy / triangle / new).
Does NOT modify any source file.
"""

from __future__ import annotations

import json
import re
from dataclasses import dataclass, field, asdict
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parent
EVAL_ROOT = ROOT.parent
RESULTS_DIR = EVAL_ROOT / "results"
CASES_PATH = EVAL_ROOT / "cases.json"
OUT_PATH = ROOT / "data" / "dataset.json"


# ---------- helpers ----------

NUM_RE = re.compile(r"-?\d+(?:\.\d+)?")


def parse_number(s: str) -> float | int | None:
    if s is None:
        return None
    s = s.strip().strip("`")
    if s in ("", "—", "-", "None", "null"):
        return None
    try:
        if "." in s:
            return float(s)
        return int(s)
    except ValueError:
        return None


def parse_bool(s: str) -> bool | None:
    if s is None:
        return None
    s = s.strip().strip("`").lower()
    if s in ("true", "yes", "1"):
        return True
    if s in ("false", "no", "0"):
        return False
    return None


def parse_score_range(s: str) -> tuple[float | None, float | None]:
    if not s or s.strip() in ("—", "-"):
        return None, None
    m = re.findall(r"-?\d+(?:\.\d+)?", s)
    if len(m) >= 2:
        return float(m[0]), float(m[-1])
    if len(m) == 1:
        v = float(m[0])
        return v, v
    return None, None


def parse_literal_hit(s: str) -> tuple[str, float | None]:
    """Returns (raw, ratio). '3/3' -> ('3/3', 1.0), 'manual' -> ('manual', None)."""
    if s is None:
        return "", None
    s = s.strip()
    if not s or s == "—":
        return s, None
    m = re.match(r"^(\d+)\s*/\s*(\d+)$", s)
    if m:
        num, den = int(m.group(1)), int(m.group(2))
        return s, (num / den if den else None)
    return s, None


def strip_md_inline(s: str) -> str:
    return s.strip().strip("`").strip()


def split_markdown_row(line: str) -> list[str]:
    """Split a markdown table row by '|', honoring '\\|' escapes and inline-code spans.

    Markdown allows '\\|' to embed a literal pipe inside a cell. Also, '|'
    inside paired backtick inline code does not split a cell. Unpaired
    backticks (e.g. when a cell value was truncated mid-code) are treated
    as literal characters. We unescape '\\|' to a literal '|' in output.
    """
    s = line.strip()
    if s.startswith("|"):
        s = s[1:]
    if s.endswith("|"):
        s = s[:-1]
    # Pre-pair backticks so an unmatched trailing backtick (e.g. truncated
    # content "`start..." ) does not swallow the following pipe.
    bt_positions = [k for k, ch in enumerate(s) if ch == "`"]
    matched: set[int] = set()
    for k in range(0, len(bt_positions) - 1, 2):
        matched.add(bt_positions[k])
        matched.add(bt_positions[k + 1])
    cells: list[str] = []
    cur: list[str] = []
    in_code = False
    k = 0
    n = len(s)
    while k < n:
        ch = s[k]
        if ch == "\\" and k + 1 < n and s[k + 1] == "|":
            cur.append("|")
            k += 2
            continue
        if ch == "`" and k in matched:
            in_code = not in_code
            cur.append(ch)
            k += 1
            continue
        if ch == "|" and not in_code:
            cells.append("".join(cur).strip())
            cur = []
            k += 1
            continue
        cur.append(ch)
        k += 1
    cells.append("".join(cur).strip())
    return cells


# ---------- group / run inference ----------

GROUP_META: dict[str, dict[str, str]] = {
    "_archive": {
        "label": "_archive (pre F-fix)",
        "phase": "F-fix 前",
        "color": "#9aa0a6",
        "note": "旧 schema，不可与 ffix 后 score 互比",
    },
    "ffix": {
        "label": "F-fix baseline",
        "phase": "F-fix 收口",
        "color": "#5b8def",
        "note": "三列俱全的首个 baseline 族",
    },
    "step3-chunker-v1": {"label": "Step 3 chunker v1", "phase": "Step 3 chunker", "color": "#b39ddb", "note": "chunk-size=800 A/A"},
    "step3-chunker-v2": {"label": "Step 3 chunker v2", "phase": "Step 3 chunker", "color": "#9575cd", "note": "chunk-size=400"},
    "step3-chunker-v3": {"label": "Step 3 chunker v3", "phase": "Step 3 chunker", "color": "#7e57c2", "note": "chunk-size=250 → Phase A 收口"},
    "step3-control-v3": {"label": "Step 3 control v3", "phase": "Step 3 A.5", "color": "#26a69a", "note": "RAG-11/12/13/14 control only"},
    "step3-full-v3-with-control": {"label": "Step 3 full v3 + control", "phase": "Step 3 A.5", "color": "#00897b", "note": "首次 14 case 全量"},
    "step4-hybrid-rrf": {"label": "Step 4 Hybrid + RRF", "phase": "Step 4 Hybrid", "color": "#ef6c00", "note": "向量+关键词 + RRF 融合"},
    "step4-vector-ablation": {"label": "Step 4 VECTOR ablation", "phase": "Step 4 Hybrid", "color": "#f9a825", "note": "纯 VECTOR 对照"},
    "step4.1-hybrid-calibrated": {"label": "Step 4.1 Hybrid calibrated", "phase": "Step 4 Hybrid", "color": "#d84315", "note": "keyword gating 校准"},
    "step5-rerank-observe": {"label": "Step 5 rerank observe", "phase": "Step 5 Rerank", "color": "#c2185b", "note": "passthrough 观测占位"},
    "step5.1-rerank-abstraction": {"label": "Step 5.1 rerank abstraction", "phase": "Step 5 Rerank", "color": "#ad1457", "note": "DocumentReranker 抽象"},
    "step5.2-http-rerank-live": {"label": "Step 5.2 HTTP rerank live", "phase": "Step 5 Rerank", "color": "#8e24aa", "note": "本地 bge HTTP 接入"},
    "step5.3-rerank-ab": {"label": "Step 5.3 rerank A/B", "phase": "Step 5 Rerank", "color": "#6a1b9a", "note": "LOCAL_BGE vs PASSTHROUGH 全量"},
    "step5.4-rerank-engineering": {"label": "Step 5.4 rerank engineering", "phase": "Step 5 Rerank", "color": "#4527a0", "note": "enabled/fallback/disabled"},
    "phase-a5-control": {"label": "Phase A.5 control (empty)", "phase": "Step 3 A.5", "color": "#bdbdbd", "note": "空目录"},
}


def run_label_from_file(group: str, filename: str) -> str:
    stem = filename.rsplit(".", 1)[0]
    base = stem.replace("rag-eval-retrievalContext-", "").replace("rag-eval-retrievalContext", "retrievalContext")
    return base


# ---------- markdown parsing ----------

@dataclass
class RetrievalEvent:
    retrieved_document_count: int | None = None
    retrieval_empty: bool | None = None
    similarity_threshold: float | None = None
    candidate_similarity_threshold: float | None = None
    score_range: str | None = None
    score_min: float | None = None
    score_max: float | None = None
    context_selected: int | None = None
    context_dropped: int | None = None
    context_truncated: bool | None = None
    context_chars_actual: int | None = None
    context_chars_max: int | None = None
    rerank_applied: bool | None = None
    rerank_mode: str | None = None
    rerank_candidates: int | None = None
    rerank_final: int | None = None
    rerank_runtime_model: str | None = None
    rerank_runtime_endpoint: str | None = None
    rerank_runtime_failure_reason: str | None = None
    documents: list[dict[str, Any]] = field(default_factory=list)


@dataclass
class CaseDetail:
    case_id: str
    case_type: str | None = None
    question: str | None = None
    should_answer: bool | None = None
    expected_source_section: str | None = None
    completed: bool | None = None
    duration_ms: int | None = None
    score_mode: str | None = None
    expected_points: list[dict[str, Any]] = field(default_factory=list)
    retrieval_events: list[RetrievalEvent] = field(default_factory=list)
    answer: str | None = None
    error: str | None = None


def parse_header(lines: list[str]) -> dict[str, str]:
    meta: dict[str, str] = {}
    for line in lines[:15]:
        m = re.match(r"^\-\s*(\w+):\s*`([^`]+)`", line)
        if m:
            meta[m.group(1)] = m.group(2)
    return meta


def parse_summary_table(lines: list[str]) -> tuple[list[str], list[dict[str, str]]]:
    """Return (header_columns, rows[dict[col->value]])."""
    header_idx = None
    for i, line in enumerate(lines):
        if line.startswith("| id ") and "type" in line and "completed" in line:
            header_idx = i
            break
    if header_idx is None:
        return [], []
    header_cells = split_markdown_row(lines[header_idx])
    # Skip separator row at header_idx+1
    rows: list[dict[str, str]] = []
    i = header_idx + 2
    while i < len(lines):
        line = lines[i]
        if not line.startswith("|"):
            break
        cells = split_markdown_row(line)
        if len(cells) < len(header_cells):
            cells = cells + [""] * (len(header_cells) - len(cells))
        elif len(cells) > len(header_cells):
            # Defensive: should not happen now that '\|' and code spans are
            # respected; merge tail just in case.
            cells = cells[: len(header_cells) - 1] + ["|".join(cells[len(header_cells) - 1 :])]
        rows.append({h: cells[j] if j < len(cells) else "" for j, h in enumerate(header_cells)})
        i += 1
    return header_cells, rows


def detect_schema_era(header_cells: list[str]) -> str:
    cols = set(header_cells)
    if "retrieved" in cols and "score" in cols:
        return "new"
    if "literal_hit" in cols and "retrieved" not in cols:
        return "triangle"
    if "matched_points" in cols:
        return "legacy"
    return "unknown"


def parse_doc_inline_kv(s: str) -> dict[str, Any]:
    """Parse a documents line like:
       #1 score=`0.0164`, source=`grafana...`, chunk=`5`, retrievalSource=`VECTOR`, ...
    """
    out: dict[str, Any] = {}
    # rank
    m = re.match(r"^\s*-?\s*#(\d+)\s+", s)
    if m:
        out["rank"] = int(m.group(1))
    # key=`value` or key=value
    for k, v in re.findall(r"(\w+)=`([^`]*)`", s):
        out[k] = v
    # numeric coercion
    for k in (
        "score",
        "rrfScore",
        "vectorScore",
        "keywordScore",
        "rerankScore",
    ):
        if k in out and out[k] not in ("", "—", "-"):
            try:
                out[k] = float(out[k])
            except ValueError:
                pass
    for k in (
        "chunk",
        "vectorRank",
        "keywordRank",
        "beforeRerankRank",
        "rerankRank",
    ):
        if k in out and out[k] not in ("", "—", "-"):
            try:
                out[k] = int(out[k])
            except ValueError:
                pass
    for k in ("rerankApplied",):
        if k in out:
            out[k] = parse_bool(out[k])
    return out


def parse_details_section(lines: list[str], start_idx: int) -> tuple[CaseDetail, int]:
    """Parse one `### RAG-XX - <type>` section. Returns (detail, next_idx)."""
    header_line = lines[start_idx]
    m = re.match(r"^###\s+(RAG-\d+)(?:\s*-\s*(.+))?\s*$", header_line.strip())
    if not m:
        return CaseDetail(case_id="UNKNOWN"), start_idx + 1
    case_id = m.group(1)
    case_type = (m.group(2) or "").strip() or None
    d = CaseDetail(case_id=case_id, case_type=case_type)

    i = start_idx + 1
    while i < len(lines):
        line = lines[i]
        if line.startswith("### ") or line.startswith("## "):
            break

        s = line.rstrip()
        # top-level meta lines
        mm = re.match(r"^\s*-\s*(question|should_answer|expected_source_section|completed|duration_ms|score_mode|error):\s*`?(.*?)`?\s*$", s)
        if mm:
            key, val = mm.group(1), mm.group(2).strip("`").strip()
            if key == "should_answer":
                d.should_answer = parse_bool(val)
            elif key == "completed":
                d.completed = parse_bool(val)
            elif key == "duration_ms":
                d.duration_ms = parse_number(val)
            elif key == "question":
                d.question = val
            elif key == "expected_source_section":
                d.expected_source_section = val
            elif key == "score_mode":
                d.score_mode = val
            elif key == "error":
                d.error = val
            i += 1
            continue

        # expected_points checklist
        if re.match(r"^\s*-\s*expected_points\s*:", s):
            i += 1
            while i < len(lines):
                cl = lines[i]
                cm = re.match(r"^\s*-\s*\[( |x|X)\]\s+(.*)$", cl)
                if not cm:
                    break
                d.expected_points.append({"point": cm.group(2).strip(), "matched": cm.group(1).lower() == "x"})
                i += 1
            continue

        # retrieval block
        if s.strip() == "retrieval:":
            i += 1
            ev: RetrievalEvent | None = None
            while i < len(lines):
                rl = lines[i]
                if rl.startswith("### ") or rl.startswith("## "):
                    break
                ev_match = re.match(r"^\s*-\s*event\s*#(\d+)", rl)
                if ev_match:
                    if ev is not None:
                        d.retrieval_events.append(ev)
                    ev = RetrievalEvent()
                    i += 1
                    continue
                if ev is None:
                    # accept un-numbered events (legacy)
                    if rl.strip().startswith("- ") or rl.strip().startswith("retrieved_document_count"):
                        ev = RetrievalEvent()
                    else:
                        i += 1
                        continue
                if rl.strip().startswith("answer:") or rl.startswith("```"):
                    break

                t = rl.strip()
                if not t.startswith("-"):
                    i += 1
                    continue
                body = t.lstrip("-").strip()

                # Match simple `key: `value`` (single backtick-wrapped value, no extra slash-separated parts).
                # Use anchored \` ... \` requirement so compound lines like "rerank: applied `true` / mode `X`" do NOT match here.
                simple_kv = re.match(r"^([\w_]+):\s*`([^`]+)`\s*$", body)
                if simple_kv:
                    key, val_stripped = simple_kv.group(1), simple_kv.group(2).strip()
                    if key == "retrieved_document_count":
                        ev.retrieved_document_count = parse_number(val_stripped)
                    elif key == "retrieval_empty":
                        ev.retrieval_empty = parse_bool(val_stripped)
                    elif key == "similarity_threshold":
                        ev.similarity_threshold = parse_number(val_stripped)
                    elif key == "candidate_similarity_threshold":
                        ev.candidate_similarity_threshold = parse_number(val_stripped)
                    elif key == "score_range":
                        ev.score_range = val_stripped
                        ev.score_min, ev.score_max = parse_score_range(val_stripped)
                    i += 1
                    continue

                # context_selected line: "context_selected: `4` / dropped: `0` / truncated: `false`"
                if body.startswith("context_selected"):
                    nums = re.findall(r"`([^`]+)`", body)
                    if len(nums) >= 3:
                        ev.context_selected = parse_number(nums[0])
                        ev.context_dropped = parse_number(nums[1])
                        ev.context_truncated = parse_bool(nums[2])
                    i += 1
                    continue

                if body.startswith("context_chars"):
                    nums = re.findall(r"`([^`]+)`", body)
                    if len(nums) >= 2:
                        ev.context_chars_actual = parse_number(nums[0])
                        ev.context_chars_max = parse_number(nums[1])
                    i += 1
                    continue

                if body.startswith("rerank:"):
                    parts = re.findall(r"(\w+)\s*`([^`]+)`", body)
                    for k, v in parts:
                        if k == "applied":
                            ev.rerank_applied = parse_bool(v)
                        elif k == "mode":
                            ev.rerank_mode = v
                        elif k == "candidates":
                            ev.rerank_candidates = parse_number(v)
                        elif k == "final":
                            ev.rerank_final = parse_number(v)
                    i += 1
                    continue

                if body.startswith("rerank_runtime"):
                    parts = re.findall(r"(\w+)\s*`([^`]*)`", body)
                    for k, v in parts:
                        if k == "model":
                            ev.rerank_runtime_model = v
                        elif k == "endpoint":
                            ev.rerank_runtime_endpoint = v
                        elif k == "failure_reason":
                            ev.rerank_runtime_failure_reason = v
                    i += 1
                    continue

                if body.startswith("documents:"):
                    i += 1
                    cur_doc: dict[str, Any] | None = None
                    while i < len(lines):
                        dl = lines[i]
                        if dl.startswith("### ") or dl.startswith("## "):
                            break
                        if dl.strip().startswith("answer:") or dl.startswith("```"):
                            break
                        dstrip = dl.strip()
                        if dstrip.startswith("- #") or re.match(r"^\s*-\s*#\d+", dl):
                            if cur_doc is not None:
                                ev.documents.append(cur_doc)
                            cur_doc = parse_doc_inline_kv(dl)
                        elif dstrip.startswith("- preview:") and cur_doc is not None:
                            cur_doc["preview"] = dstrip[len("- preview:") :].strip()
                        elif dstrip.startswith("preview:") and cur_doc is not None:
                            cur_doc["preview"] = dstrip[len("preview:") :].strip()
                        else:
                            # break out if we hit a non-doc line that isn't an indented continuation
                            if dl.strip() == "":
                                i += 1
                                continue
                            if not dl.startswith(" "):
                                break
                        i += 1
                    if cur_doc is not None:
                        ev.documents.append(cur_doc)
                    continue

                # unknown line — skip
                i += 1
            if ev is not None:
                d.retrieval_events.append(ev)
            continue

        # answer block — scan to next "### RAG-\d+" case header, any "## " H2,
        # or EOF. Plain "### 子标题" inside the answer body must NOT break,
        # otherwise nested markdown headings (e.g. "### 监控数据概览")
        # would prematurely cut the answer.
        if s.strip() == "answer:":
            j = i + 1
            while j < len(lines) and lines[j].strip() == "":
                j += 1
            if j < len(lines) and lines[j].strip().startswith("```"):
                j += 1  # skip opening fence
                buf: list[str] = []
                while j < len(lines):
                    nxt = lines[j]
                    if nxt.startswith("## ") or re.match(r"^### RAG-\d+", nxt):
                        break
                    buf.append(nxt.rstrip("\n"))
                    j += 1
                while buf and buf[-1].strip() == "":
                    buf.pop()
                if buf and buf[-1].strip() == "```":
                    buf.pop()
                while buf and buf[-1].strip() == "":
                    buf.pop()
                d.answer = "\n".join(buf)
                i = j
                continue

        i += 1

    return d, i


def parse_report(path: Path) -> dict[str, Any]:
    text = path.read_text(encoding="utf-8")
    lines = text.splitlines()
    header = parse_header(lines)
    header_cells, summary_rows = parse_summary_table(lines)
    schema_era = detect_schema_era(header_cells)

    summary: dict[str, dict[str, Any]] = {}
    for row in summary_rows:
        cid = strip_md_inline(row.get("id", ""))
        if not cid.startswith("RAG-"):
            continue
        retrieved = parse_number(row.get("retrieved", ""))
        score_min, score_max = parse_score_range(row.get("score", ""))
        literal_raw, literal_ratio = parse_literal_hit(row.get("literal_hit", "") or row.get("matched_points", ""))
        summary[cid] = {
            "type": row.get("type", "").strip(),
            "completed": parse_bool(row.get("completed", "")),
            "duration_ms": parse_number(row.get("duration_ms", "")),
            "should_answer": parse_bool(row.get("should_answer", "")),
            "retrieved": retrieved,
            "score_range": row.get("score", "").strip() if "score" in row else None,
            "score_min": score_min,
            "score_max": score_max,
            "empty": parse_bool(row.get("empty", "")),
            "literal_hit": literal_raw,
            "literal_hit_ratio": literal_ratio,
            "missed_points": row.get("missed_points", "").strip(),
            "answer_preview": row.get("answer_preview", "").strip(),
            "manual_pass": row.get("manual_pass", "").strip(),
            "matched_points": row.get("matched_points", "").strip() if "matched_points" in row else None,
        }

    # parse details
    details: dict[str, dict[str, Any]] = {}
    i = 0
    while i < len(lines):
        if lines[i].startswith("### RAG-"):
            d, next_i = parse_details_section(lines, i)
            details[d.case_id] = asdict(d)
            i = next_i
        else:
            i += 1

    return {
        "header": header,
        "schema_era": schema_era,
        "schema_columns": header_cells,
        "summary": summary,
        "details": details,
    }


def derive_config_hint(group: str, run_id: str, parsed: dict | None = None) -> str:
    g = group
    r = run_id.lower()

    # Content-derived rerank signal (preferred over naming when present)
    if parsed:
        modes: set[str] = set()
        applied: set[bool] = set()
        models: set[str] = set()
        failures: set[str] = set()
        for det in parsed.get("details", {}).values():
            for ev in det.get("retrieval_events", []):
                if ev.get("rerank_mode"):
                    modes.add(ev["rerank_mode"])
                if ev.get("rerank_applied") is not None:
                    applied.add(bool(ev["rerank_applied"]))
                if ev.get("rerank_runtime_model"):
                    models.add(ev["rerank_runtime_model"])
                if ev.get("rerank_runtime_failure_reason"):
                    failures.add(ev["rerank_runtime_failure_reason"])
        if failures:
            if failures <= {"RERANK_DISABLED"}:
                return "rerank=DISABLED (config off)"
            return "rerank=LOCAL_BGE (服务不可用 → fallback PASSTHROUGH)"
        if "LOCAL_BGE" in modes and models:
            return f"rerank=LOCAL_BGE (live, model={sorted(models)[0]})"
        if modes and modes <= {"PASSTHROUGH"} and applied <= {False}:
            return "rerank=PASSTHROUGH (observe)"

    if "passthrough" in r:
        return "rerank=PASSTHROUGH"
    if "local-bge" in r:
        return "rerank=LOCAL_BGE"
    if "live" in r and g == "step5.4-rerank-engineering":
        return "rerank=LOCAL_BGE (live, enabled)"
    if "fallback" in r and g == "step5.4-rerank-engineering":
        return "rerank=LOCAL_BGE (服务不可用 → fallback PASSTHROUGH)"
    if "disabled" in r and g == "step5.4-rerank-engineering":
        return "rerank=DISABLED (config off)"
    if "factory-rag04" in r or "factory-rag07" in r:
        return "rerank=LOCAL_BGE (factory probe)"
    if "smoke" in r and g == "step5.1-rerank-abstraction":
        return "rerank=PASSTHROUGH (abstraction smoke)"
    if g == "step5-rerank-observe":
        return "rerank=PASSTHROUGH (observe only)"
    if g == "step4-vector-ablation":
        return "retrievalMode=VECTOR"
    if g == "step4-hybrid-rrf":
        return "retrievalMode=HYBRID + RRF"
    if g == "step4.1-hybrid-calibrated":
        return "HYBRID + keyword gating 校准"
    if g == "step3-chunker-v1":
        return "chunk-size=800"
    if g == "step3-chunker-v2":
        return "chunk-size=400"
    if g == "step3-chunker-v3":
        return "chunk-size=250"
    if g == "step3-control-v3":
        return "chunk v3 + control-only"
    if g == "step3-full-v3-with-control":
        return "chunk v3 + 14 case 全量"
    if "t055" in r:
        return "threshold=0.55"
    if "t060" in r:
        return "threshold=0.60"
    if "t065" in r:
        return "threshold=0.65"
    if g == "ffix" and "baseline" in r:
        return "F-fix baseline, threshold=0.60"
    if g == "_archive":
        return "F-fix 前历史"
    return ""


def main() -> None:
    cases = json.loads(CASES_PATH.read_text(encoding="utf-8"))
    cases_by_id = {c["id"]: c for c in cases}

    runs: list[dict[str, Any]] = []
    md_files: list[tuple[str, Path]] = []
    for group_dir in sorted(RESULTS_DIR.iterdir()):
        if not group_dir.is_dir():
            continue
        group = group_dir.name
        for f in sorted(group_dir.iterdir()):
            if not (f.is_file() and f.suffix == ".md"):
                continue
            name = f.name
            if not (
                name.startswith("rag-eval-result")
                or name.startswith("rag-eval-retrievalContext")
                or name.startswith("probe-")
            ):
                continue
            md_files.append((group, f))

    for group, f in md_files:
        parsed = parse_report(f)
        run_id = f"{group}/{f.stem}"
        run_label = run_label_from_file(group, f.name)
        meta = GROUP_META.get(group, {"label": group, "phase": group, "color": "#888", "note": ""})
        case_count = len(parsed["summary"])
        run = {
            "run_id": run_id,
            "group": group,
            "group_label": meta["label"],
            "group_phase": meta["phase"],
            "group_color": meta["color"],
            "group_note": meta["note"],
            "file_label": run_label,
            "file_path": str(f.relative_to(EVAL_ROOT)),
            "generated_at": parsed["header"].get("generated_at", ""),
            "api_url": parsed["header"].get("api_url", ""),
            "agent_id": parsed["header"].get("agent_id", ""),
            "schema_era": parsed["schema_era"],
            "schema_columns": parsed["schema_columns"],
            "config_hint": derive_config_hint(group, f.stem, parsed),
            "case_count": case_count,
            "summary": parsed["summary"],
            "details": parsed["details"],
        }
        runs.append(run)

    # sort: by generated_at if available, otherwise by group then file
    def sort_key(r: dict[str, Any]) -> tuple:
        return (r.get("generated_at", "") or "", r["group"], r["file_label"])

    runs.sort(key=sort_key)

    dataset = {
        "schema_version": "1.0",
        "source_root": str(EVAL_ROOT.relative_to(EVAL_ROOT.parent.parent)),
        "cases": cases,
        "cases_by_id": cases_by_id,
        "groups": GROUP_META,
        "runs": runs,
        "stats": {
            "run_count": len(runs),
            "case_count": len(cases),
            "total_summary_rows": sum(len(r["summary"]) for r in runs),
        },
    }

    OUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    payload = json.dumps(dataset, ensure_ascii=False, indent=2)
    OUT_PATH.write_text(payload, encoding="utf-8")
    # Also emit a JS variant so the HTML works under file:// without a local server.
    js_path = OUT_PATH.with_suffix(".js")
    js_path.write_text(
        "window.RAG_EVAL_DATASET = " + json.dumps(dataset, ensure_ascii=False) + ";\n",
        encoding="utf-8",
    )
    print(
        f"wrote {OUT_PATH}  runs={len(runs)}  cases={len(cases)}  rows={dataset['stats']['total_summary_rows']}\n"
        f"wrote {js_path} ({js_path.stat().st_size // 1024} KB)"
    )


if __name__ == "__main__":
    main()
