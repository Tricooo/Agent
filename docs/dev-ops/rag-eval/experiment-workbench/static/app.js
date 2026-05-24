/* RAG Eval Experiment Workbench — frontend logic.
   Pure browser JS, no build step. Views are rendered into existing DOM nodes
   from index.html; render functions are per-view to keep coupling local. */

const state = {
  view: "dashboard",
  health: null,
  runs: [],
  groups: [],
  agents: [],
  cases: [],
  families: [],
  caseTypes: [],
  sourceFiles: [],
  compareA: null,
  compareB: null,
  /* compare UX */
  compareDiffOnly: true,
  compareLcsDiff: true,
  compareOpenCases: new Set(),
  comparePairKey: null,
  /* matrix UX */
  matrixCursor: null,          // {rowIdx, colIdx} in current sort
  matrixOrderedRuns: [],
  matrixOrderedRows: [],
  matrixHideEmpty: false,      // when true: drop rows/cols with no cells
  /* drilldown UX */
  modalOpen: false,
  lastFocus: null,
  drillContext: null,          // {runId, caseId, caseOrder: [caseId,...]}
  /* hash sync */
  suppressHashRead: false
};

/* ----------------------------- helpers ----------------------------- */
const $ = (sel, root = document) => root.querySelector(sel);
const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function qs(params) {
  const search = new URLSearchParams();
  Object.entries(params || {}).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== "") search.set(k, v);
  });
  const t = search.toString();
  return t ? `?${t}` : "";
}

async function api(path, options) {
  const response = await fetch(path, options);
  if (!response.ok) {
    let msg = `${response.status} ${response.statusText}`;
    try {
      const payload = await response.json();
      msg = payload.detail || msg;
    } catch (_) { /* response had no JSON body */ }
    throw new Error(msg);
  }
  return response.json();
}

function fmtPct(value, opts) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return "—";
  const pct = Number(value) * 100;
  if (opts && opts.signed) {
    const sign = pct > 0 ? "+" : "";
    return `${sign}${pct.toFixed(1)}%`;
  }
  return `${Math.round(pct)}%`;
}

function fmtMs(value, opts) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return "—";
  const n = Number(value);
  const sign = opts && opts.signed ? (n > 0 ? "+" : (n < 0 ? "−" : "")) : "";
  const abs = Math.abs(n);
  const body = abs >= 1000 ? `${(abs / 1000).toFixed(1)}s` : `${Math.round(abs)}ms`;
  return `${sign}${body}`;
}

function fmtTime(value) {
  if (!value) return "—";
  // accept ISO or already-formatted strings; trim seconds + tz for compactness
  const text = String(value);
  const m = text.match(/^(\d{4}-\d{2}-\d{2})[T ](\d{2}:\d{2})/);
  return m ? `${m[1]} ${m[2]}` : text;
}

function shortRun(run) {
  if (!run) return "—";
  const id = typeof run === "string" ? run : (run.run_id || "");
  return id.replace(/^.*__/, "").replace(/^rag-eval-result-/, "") || id || "—";
}

/* Insert zero-width spaces after `_` in ALL_CAPS_WITH_UNDERSCORES so long
   enum tokens (PER_VARIANT_RERANK_RRF, AUTO_PROFILE) wrap at underscore
   boundaries instead of mid-token. No-op for natural text. */
const ENUM_RE = /^[A-Z][A-Z0-9_]*$/;
function softBreakEnum(value) {
  if (value === null || value === undefined) return value;
  const s = String(value);
  if (!ENUM_RE.test(s) || !s.includes("_")) return s;
  return s.replace(/_/g, "_\u200B");
}

/* Compact rendering of an enum token for tight cells: drop the noisiest
   prefix segments above a threshold so PER_VARIANT_RERANK_RRF → RERANK_RRF.
   Always pair with the full value in a title/tooltip. */
function shortEnum(value, maxLen) {
  if (value === null || value === undefined) return "";
  const s = String(value);
  if (!ENUM_RE.test(s) || !s.includes("_") || s.length <= (maxLen || 14)) return s;
  const parts = s.split("_");
  // keep the last two segments (usually the most specific)
  return parts.slice(Math.max(0, parts.length - 2)).join("_");
}

/* Classification — colours follow the spec's priority:
     1. source_coverage_label contains "mismatch" → manual stripe
     2. literal_ratio present → pass / partial / fail
     3. completed === false → fail
     4. otherwise → manual (waiting human / coverage case) */
function classifyCell(cell) {
  if (!cell) return "empty";
  const label = String(cell.source_coverage_label || "").toLowerCase();
  if (label.includes("mismatch")) return "manual";
  const ratio = cell.literal_ratio;
  if (ratio !== null && ratio !== undefined && !Number.isNaN(Number(ratio))) {
    const r = Number(ratio);
    if (r >= 1) return "pass";
    if (r > 0) return "partial";
    return "fail";
  }
  if (cell.completed === false) return "fail";
  return "manual";
}

function cellMainLabel(cell, cls) {
  if (!cell) return "—";
  if (cls === "pass") return "PASS";
  if (cls === "fail") return cell.completed === false ? "FAIL" : "FAIL";
  if (cls === "partial") {
    const r = Number(cell.literal_ratio || 0);
    return `${Math.round(r * 100)}%`;
  }
  if (cls === "manual") {
    const label = String(cell.source_coverage_label || "");
    if (label.toLowerCase().includes("mismatch")) return "COV";
    if (label) return "COV";
    return "MANUAL";
  }
  return "—";
}

function badge(text, cls = "muted") {
  return `<span class="badge ${cls}">${escapeHtml(text ?? "—")}</span>`;
}

function metric(label, value, sub) {
  return `<div class="metric">
    <div class="k">${escapeHtml(label)}</div>
    <div class="v">${escapeHtml(value)}</div>
    ${sub ? `<div class="s">${escapeHtml(sub)}</div>` : ""}
  </div>`;
}

/* Variant for drilldown header — technical enum tokens (AUTO_PROFILE,
   PER_VARIANT_RERANK_RRF) render in mono and stay on title for hover.
   Soft-break inserts ZWSPs at underscores so wraps land at token boundaries. */
function drillMetric(label, value, sub) {
  const v = value == null ? "—" : String(value);
  const s = sub == null ? "" : String(sub);
  return `<div class="metric drill-metric">
    <div class="k">${escapeHtml(label)}</div>
    <div class="v" title="${escapeHtml(v)}">${escapeHtml(softBreakEnum(v))}</div>
    ${s ? `<div class="s" title="${escapeHtml(s)}">${escapeHtml(softBreakEnum(s))}</div>` : ""}
  </div>`;
}

function emptyState(title, sub) {
  return `<div class="empty-state"><h3>${escapeHtml(title)}</h3>${sub ? `<div>${escapeHtml(sub)}</div>` : ""}</div>`;
}

/* ----------------------------- tooltip ----------------------------- */
let _tt = null;
function ensureTooltip() {
  if (_tt) return _tt;
  _tt = document.createElement("div");
  _tt.className = "tt";
  _tt.setAttribute("role", "tooltip");
  document.body.appendChild(_tt);
  return _tt;
}
function bindTooltip(el, text) {
  if (!el || !text) return;
  el.dataset.tt = text;
  el.addEventListener("mouseenter", _showTooltip);
  el.addEventListener("mousemove", _moveTooltip);
  el.addEventListener("mouseleave", _hideTooltip);
  el.addEventListener("focus", _showTooltip);
  el.addEventListener("blur", _hideTooltip);
}
function _showTooltip(e) {
  const txt = e.currentTarget.dataset.tt;
  if (!txt) return;
  const tt = ensureTooltip();
  tt.textContent = txt;
  tt.classList.add("show");
  _moveTooltip(e);
}
function _moveTooltip(e) {
  const tt = ensureTooltip();
  const pad = 14;
  const rect = tt.getBoundingClientRect();
  // focus events have no mouse coords — fall back to element bounding box
  const x0 = e.clientX ?? e.currentTarget.getBoundingClientRect().left + 16;
  const y0 = e.clientY ?? e.currentTarget.getBoundingClientRect().bottom + 8;
  let x = x0 + pad, y = y0 + pad;
  if (x + rect.width > innerWidth - 8) x = x0 - rect.width - pad;
  if (y + rect.height > innerHeight - 8) y = y0 - rect.height - pad;
  tt.style.left = `${Math.max(8, x)}px`;
  tt.style.top = `${Math.max(8, y)}px`;
}
function _hideTooltip() {
  if (_tt) _tt.classList.remove("show");
}

/* ----------------------------- LCS line diff ----------------------------- */
/* Returns {a:[{line,mark}], b:[{line,mark}]} where mark is 'same' | 'diff'.
   O(m*n) — capped to avoid blowing the page on multi-KB answers. */
function diffLines(aText, bText) {
  const ai = String(aText ?? "").split("\n");
  const bi = String(bText ?? "").split("\n");
  const m = ai.length, n = bi.length;
  if (m * n > 60000) {
    return {
      a: ai.map(line => ({ line, mark: "diff" })),
      b: bi.map(line => ({ line, mark: "diff" }))
    };
  }
  const dp = Array.from({ length: m + 1 }, () => new Int32Array(n + 1));
  for (let i = m - 1; i >= 0; i--) {
    for (let j = n - 1; j >= 0; j--) {
      dp[i][j] = ai[i] === bi[j] ? dp[i + 1][j + 1] + 1 : Math.max(dp[i + 1][j], dp[i][j + 1]);
    }
  }
  const aMark = new Array(m).fill("diff");
  const bMark = new Array(n).fill("diff");
  let i = 0, j = 0;
  while (i < m && j < n) {
    if (ai[i] === bi[j]) { aMark[i] = "same"; bMark[j] = "same"; i++; j++; }
    else if (dp[i + 1][j] >= dp[i][j + 1]) i++;
    else j++;
  }
  return {
    a: ai.map((line, k) => ({ line, mark: aMark[k] })),
    b: bi.map((line, k) => ({ line, mark: bMark[k] }))
  };
}
function renderAnswerLines(lines) {
  return lines.map(({ line, mark }) =>
    `<span class="ln ln-${mark}">${escapeHtml(line) || "&nbsp;"}</span>`
  ).join("");
}

/* "in expected source" — used in Final Documents.
   The case's source_file is typically a basename or relative path; doc.source_path
   is whatever the retriever recorded. We match generously (substring either way). */
function docInExpectedSource(doc, caseObj) {
  if (!doc || !caseObj) return false;
  const dp = String(doc.source_path || "").toLowerCase().trim();
  const cf = String(caseObj.source_file || "").toLowerCase().trim();
  if (!dp || !cf) return false;
  if (dp === cf) return true;
  if (dp.includes(cf) || cf.includes(dp)) return true;
  const dpBase = dp.split("/").pop() || dp;
  const cfBase = cf.split("/").pop() || cf;
  return dpBase === cfBase;
}

/* ----------------------------- view router ----------------------------- */
function setView(view) {
  state.view = view;
  $$(".tab").forEach(btn => btn.classList.toggle("active", btn.dataset.view === view));
  $$(".view").forEach(s => s.classList.toggle("active", s.id === `view-${view}`));
  const fn = {
    dashboard: renderDashboard,
    runs: renderRuns,
    cases: renderCases,
    matrix: renderMatrix,
    compare: renderCompare,
    insights: renderInsights
  }[view];
  if (fn) fn();
  writeHash();
}

/* ----------------------------- URL hash ----------------------------- */
/* Hash schema:
     #view=<view>                                  (default: dashboard)
     #view=compare&a=<run>&b=<run>                 (compare picker state)
     #view=matrix                                  (matrix doesn't need extra keys)
     &drill=<runId>|<caseId>                       (opens drilldown across views)
   Only keys with non-default values are serialised — keeps URLs short. */
function parseHash() {
  const h = location.hash.replace(/^#/, "");
  const out = {};
  h.split("&").filter(Boolean).forEach(part => {
    const idx = part.indexOf("=");
    if (idx < 0) { out[part] = ""; return; }
    out[part.slice(0, idx)] = decodeURIComponent(part.slice(idx + 1));
  });
  return out;
}
function writeHash() {
  const parts = [];
  if (state.view && state.view !== "dashboard") parts.push(`view=${state.view}`);
  if (state.view === "compare") {
    if (state.compareA) parts.push(`a=${encodeURIComponent(state.compareA)}`);
    if (state.compareB) parts.push(`b=${encodeURIComponent(state.compareB)}`);
  }
  if (state.drillContext) {
    parts.push(`drill=${encodeURIComponent(state.drillContext.runId)}|${encodeURIComponent(state.drillContext.caseId)}`);
  }
  const newHash = parts.length ? "#" + parts.join("&") : "";
  if (newHash === location.hash) return;
  state.suppressHashRead = true;
  history.replaceState(null, "", newHash || location.pathname + location.search);
  setTimeout(() => { state.suppressHashRead = false; }, 0);
}
function applyHash() {
  if (state.suppressHashRead) return;
  const h = parseHash();
  const view = ["dashboard", "runs", "cases", "matrix", "compare", "insights"].includes(h.view)
    ? h.view : (h.view ? "dashboard" : state.view);
  if (h.view === "compare") {
    if (h.a) state.compareA = h.a;
    if (h.b) state.compareB = h.b;
    state.comparePairKey = null;     // force auto-open recompute on hash-driven nav
  }
  state.suppressHashRead = true;
  setView(view);
  state.suppressHashRead = false;
  if (h.drill) {
    const [runId, caseId] = h.drill.split("|");
    if (runId && caseId && (!state.drillContext ||
        state.drillContext.runId !== runId || state.drillContext.caseId !== caseId)) {
      openDrilldown(runId, caseId);
    }
  } else if (state.modalOpen && !h.drill) {
    closeModal();
  }
}

/* ----------------------------- selects ----------------------------- */
function populateSelect(select, label, rows, valueKey, countKey) {
  if (!select) return;
  const prev = select.value;
  const opts = [`<option value="">${escapeHtml(label)}</option>`];
  rows.forEach(row => {
    const value = row[valueKey] ?? "";
    const count = row[countKey];
    const text = value || "—";
    const suffix = count !== undefined && count !== null && count !== "" ? ` (${count})` : "";
    opts.push(`<option value="${escapeHtml(value)}">${escapeHtml(text)}${suffix}</option>`);
  });
  select.innerHTML = opts.join("");
  if ([...select.options].some(o => o.value === prev)) select.value = prev;
}

function populateRunSelects() {
  const html = state.runs.map(run =>
    `<option value="${escapeHtml(run.run_id)}">${escapeHtml(shortRun(run))} · ${escapeHtml(run.group_name || "no group")}</option>`
  ).join("");
  ["compare-a", "compare-b"].forEach(id => {
    const el = $(`#${id}`);
    if (!el) return;
    el.innerHTML = `<option value="">— select run —</option>${html}`;
  });
}

/* ----------------------------- bootstrap ----------------------------- */
async function loadBase() {
  const [health, runsPayload, casesPayload] = await Promise.all([
    api("/api/health"),
    api("/api/runs"),
    api("/api/cases")
  ]);
  state.health = health;
  state.runs = runsPayload.runs || [];
  state.groups = runsPayload.groups || [];
  state.agents = runsPayload.agents || [];
  state.cases = casesPayload.cases || [];
  state.families = casesPayload.families || [];
  state.sourceFiles = casesPayload.source_files || [];
  state.caseTypes = Array.from(new Set(state.cases.map(c => c.case_type).filter(Boolean)))
    .map(t => ({ case_type: t }));

  const counts = health.counts || {};
  $("#health-line").textContent =
    `${counts.runs ?? 0} runs · ${counts.internal_cases ?? 0} internal · ${counts.external_cases ?? 0} external · ${counts.case_results ?? 0} results`;

  populateSelect($("#run-group-filter"), "all groups", state.groups, "group_name", "run_count");
  populateSelect($("#run-agent-filter"), "all agents", state.agents, "agent_id", "run_count");
  populateSelect($("#case-family-filter"), "all families", state.families, "family", "case_count");
  populateSelect($("#matrix-family-filter"), "all families", state.families, "family", "case_count");
  populateSelect($("#matrix-group-filter"), "all groups", state.groups, "group_name", "run_count");
  populateSelect($("#matrix-agent-filter"), "all agents", state.agents, "agent_id", "run_count");
  populateSelect($("#matrix-case-type-filter"), "all case types", state.caseTypes, "case_type");
  populateRunSelects();

  if (!state.compareA && state.runs[0]) state.compareA = state.runs[0].run_id;
  if (!state.compareB && state.runs[1]) state.compareB = state.runs[1].run_id;
  const sa = $("#compare-a"), sb = $("#compare-b");
  if (sa) sa.value = state.compareA || "";
  if (sb) sb.value = state.compareB || "";
}

/* ============================================================
   DASHBOARD
   ============================================================ */
async function renderDashboard() {
  const counts = state.health?.counts || {};
  const warningRuns = state.runs.filter(r => (r.parse_warnings || []).length);
  $("#dashboard-metrics").innerHTML = [
    metric("runs", String(counts.runs ?? 0), "indexed markdown reports"),
    metric("cases", String((counts.internal_cases ?? 0) + (counts.external_cases ?? 0)),
      `${counts.internal_cases ?? 0} internal · ${counts.external_cases ?? 0} external`),
    metric("case results", String(counts.case_results ?? 0), "run × case rows"),
    metric("parse warnings", String(warningRuns.length),
      `${counts.document_hits ?? 0} document hits indexed`)
  ].join("");

  // Recent runs — five
  const recent = state.runs.slice(0, 5);
  $("#recent-runs-meta").textContent = state.runs.length ? `top ${recent.length} of ${state.runs.length}` : "";
  $("#recent-runs").innerHTML = recent.length
    ? recent.map(run => `
        <div class="list-item" data-run="${escapeHtml(run.run_id)}">
          <div class="li-title">${escapeHtml(shortRun(run))}</div>
          <div class="li-meta">${escapeHtml(fmtTime(run.generated_at))} · ${fmtPct(run.literal_pass_rate)}</div>
          <div class="li-sub">${escapeHtml(run.group_name || "no group")} · ${escapeHtml(run.case_count ?? 0)} cases · ${escapeHtml(run.run_id)}</div>
        </div>`).join("")
    : emptyState("No runs indexed yet", "Drop reports into the workbench folder and Reindex.");

  // Parse warnings
  $("#warning-list-meta").textContent = warningRuns.length ? `${warningRuns.length} run(s) with warnings` : "";
  $("#warning-list").innerHTML = warningRuns.length
    ? warningRuns.slice(0, 12).map(run => `
        <div class="list-item" data-run="${escapeHtml(run.run_id)}">
          <div class="li-title">${escapeHtml(shortRun(run))}</div>
          <div class="li-meta">${badge((run.parse_warnings || []).length + " warn", "warn")}</div>
          <div class="li-sub">${escapeHtml((run.parse_warnings || []).join(" · "))}</div>
        </div>`).join("")
    : emptyState("No parse warnings", "All indexed reports parsed cleanly.");

  await renderQuickCompare();
}

async function renderQuickCompare() {
  const container = $("#quick-compare");
  try {
    const payload = await api("/api/presets");
    const presets = payload.presets || [];
    $("#quick-compare-meta").textContent = `${presets.filter(p => p.available).length}/${presets.length} available`;
    if (!presets.length) {
      container.innerHTML = emptyState("No presets configured", "Define ablation presets in the workbench config.");
      return;
    }
    container.innerHTML = presets.map(preset => {
      const disabled = !preset.available;
      const pair = preset.run_a && preset.run_b
        ? `${shortRun(preset.run_a)}  →  ${shortRun(preset.run_b)}`
        : (preset.reason || "—");
      return `<button class="qc-card" type="button"
        ${disabled ? "disabled" : ""}
        data-a="${escapeHtml(preset.run_a || "")}"
        data-b="${escapeHtml(preset.run_b || "")}"
        title="${escapeHtml(preset.reason || preset.name)}">
        <div class="qc-name">${escapeHtml(preset.name)}</div>
        <div class="qc-pair">${escapeHtml(pair)}</div>
        ${disabled ? `<div class="qc-reason">${escapeHtml(preset.reason || "unavailable")}</div>` : ""}
      </button>`;
    }).join("");
    $$(".qc-card", container).forEach(btn => {
      btn.addEventListener("click", () => {
        if (btn.disabled) return;
        const a = btn.dataset.a, b = btn.dataset.b;
        if (!a || !b) return;
        state.compareA = a;
        state.compareB = b;
        setView("compare");
      });
    });
  } catch (err) {
    container.innerHTML = emptyState("Presets unavailable", err.message);
  }
}

/* ============================================================
   RUNS
   ============================================================ */
async function renderRuns() {
  const params = {
    group: $("#run-group-filter").value,
    agent_id: $("#run-agent-filter").value
  };
  let runs;
  try {
    const payload = await api(`/api/runs${qs(params)}`);
    runs = payload.runs || [];
    state.runs = runs;
  } catch (err) {
    $("#runs-table").innerHTML = `<tbody><tr><td>${emptyState("Failed to load runs", err.message)}</td></tr></tbody>`;
    return;
  }
  if (!runs.length) {
    $("#runs-table").innerHTML = `<tbody><tr><td>${emptyState("No runs match the current filters")}</td></tr></tbody>`;
    return;
  }
  $("#runs-table").innerHTML = `
    <thead><tr>
      <th>run</th>
      <th>group</th>
      <th>agent</th>
      <th>generated</th>
      <th class="num">cases</th>
      <th class="num">literal</th>
      <th class="num">avg dur</th>
      <th>warnings</th>
    </tr></thead>
    <tbody>
      ${runs.map(run => `
        <tr>
          <td>
            <div class="row-title">${escapeHtml(shortRun(run))}</div>
            <div class="row-sub">${escapeHtml(run.file_path || run.run_id)}</div>
          </td>
          <td>${escapeHtml(run.group_name || "—")}</td>
          <td>${escapeHtml(run.agent_id || "—")}</td>
          <td class="mono">${escapeHtml(fmtTime(run.generated_at))}</td>
          <td class="num">${escapeHtml(run.case_count ?? 0)}</td>
          <td class="num">${fmtPct(run.literal_pass_rate)}</td>
          <td class="num">${fmtMs(run.avg_duration_ms)}</td>
          <td>${(run.parse_warnings || []).length
              ? badge(`${(run.parse_warnings || []).length} warn`, "warn")
              : badge("clean", "ok")}</td>
        </tr>`).join("")}
    </tbody>`;
}

/* ============================================================
   CASES
   ============================================================ */
async function renderCases() {
  const params = {
    origin: $("#case-origin-filter").value,
    family: $("#case-family-filter").value,
    score_mode: $("#case-score-filter").value
  };
  let cases;
  try {
    const payload = await api(`/api/cases${qs(params)}`);
    cases = payload.cases || [];
  } catch (err) {
    $("#cases-table").innerHTML = `<tbody><tr><td>${emptyState("Failed to load cases", err.message)}</td></tr></tbody>`;
    return;
  }
  if (!cases.length) {
    $("#cases-table").innerHTML = `<tbody><tr><td>${emptyState("No cases match the current filters")}</td></tr></tbody>`;
    return;
  }
  $("#cases-table").innerHTML = `
    <thead><tr>
      <th>case</th>
      <th>origin</th>
      <th>family</th>
      <th>type</th>
      <th>score mode</th>
      <th>source</th>
      <th>question</th>
    </tr></thead>
    <tbody>
      ${cases.map(c => `
        <tr>
          <td><div class="row-title mono">${escapeHtml(c.case_id)}</div></td>
          <td>${badge(c.origin || "—", c.origin === "external" ? "info" : "ok")}</td>
          <td>${escapeHtml(c.family || "—")}</td>
          <td>${escapeHtml(c.case_type || "—")}</td>
          <td>${badge(c.score_mode || "—", c.score_mode === "literal" ? "muted" : "warn")}</td>
          <td class="mono">${escapeHtml(c.source_file || c.expected_source_section || "—")}</td>
          <td>${escapeHtml(c.question || "—")}</td>
        </tr>`).join("")}
    </tbody>`;
}

/* ============================================================
   MATRIX
   ============================================================ */
async function renderMatrix() {
  const params = {
    origin: $("#matrix-origin-filter").value,
    family: $("#matrix-family-filter").value,
    group: $("#matrix-group-filter").value,
    agent_id: $("#matrix-agent-filter").value
  };
  let payload;
  try {
    payload = await api(`/api/matrix${qs(params)}`);
  } catch (err) {
    $("#matrix-table").innerHTML = `<tbody><tr><td>${emptyState("Failed to load matrix", err.message)}</td></tr></tbody>`;
    return;
  }

  // Client-side filters (score_mode, case_type) — backend may not yet support them.
  const scoreMode = $("#matrix-score-filter").value;
  const caseType = $("#matrix-case-type-filter").value;
  let rows = payload.rows || [];
  if (scoreMode) rows = rows.filter(r => (r.case?.score_mode || "") === scoreMode);
  if (caseType) rows = rows.filter(r => (r.case?.case_type || "") === caseType);

  // Sort: origin → family → case_id. Stable so equal keys keep API order.
  rows.sort((x, y) => {
    const a = x.case || {}, b = y.case || {};
    const o = (a.origin || "").localeCompare(b.origin || "");
    if (o) return o;
    const f = (a.family || "").localeCompare(b.family || "");
    if (f) return f;
    return (a.case_id || "").localeCompare(b.case_id || "");
  });

  let runs = payload.runs || [];

  // "hide empty" toggle — drop columns where every row has no cell, then drop
  // rows where every surviving column has no cell. Cells are positional so we
  // splice both runs and row.cells in parallel.
  if (state.matrixHideEmpty && rows.length && runs.length) {
    const colHas = runs.map((_, ci) => rows.some(r => r.cells && r.cells[ci]));
    runs = runs.filter((_, ci) => colHas[ci]);
    rows = rows.map(r => ({
      ...r,
      cells: (r.cells || []).filter((_, ci) => colHas[ci])
    })).filter(r => r.cells.some(Boolean));
  }

  if (!runs.length || !rows.length) {
    const msg = state.matrixHideEmpty
      ? "Hide empty filtered everything out — uncheck or widen filters."
      : "Adjust filters or reindex.";
    $("#matrix-table").innerHTML = `<thead></thead><tbody><tr><td>${emptyState("No runs or cases match", msg)}</td></tr></tbody>`;
    return;
  }

  // expose ordered runs/rows for keyboard navigation + drill case-order
  state.matrixOrderedRuns = runs;
  state.matrixOrderedRows = rows;

  let lastKey = null;
  const bodyHtml = rows.map((row, rowIdx) => {
    const c = row.case || {};
    const key = `${c.origin || ""}|${c.family || ""}`;
    const isBreak = lastKey !== null && key !== lastKey;
    lastKey = key;
    const tail = c.family || c.origin || "";
    return `
      <tr${isBreak ? ' class="family-break"' : ""}>
        <td class="matrix-case">
          <div class="row-title">${escapeHtml(c.case_id)}</div>
          <div class="row-sub">${escapeHtml(tail)}${c.score_mode ? ` · ${escapeHtml(c.score_mode)}` : ""}</div>
        </td>
        ${row.cells.map((cell, colIdx) => renderMatrixCell(cell, runs[colIdx], c, rowIdx, colIdx)).join("")}
      </tr>`;
  }).join("");

  $("#matrix-table").innerHTML = `
    <thead>
      <tr>
        <th class="matrix-case">case</th>
        ${runs.map(run => `
          <th class="run-head" data-run="${escapeHtml(run.run_id)}">
            <div class="rh-name">${escapeHtml(shortRun(run))}</div>
            <div class="rh-group">${escapeHtml(run.group_name || "—")}</div>
            <div class="rh-stat">${fmtPct(run.literal_pass_rate)} · ${escapeHtml(run.case_count ?? 0)}</div>
          </th>`).join("")}
      </tr>
    </thead>
    <tbody>${bodyHtml}</tbody>`;

  // tooltips on run headers
  $$(".run-head[data-run]", $("#matrix-table")).forEach(th => {
    const run = runs.find(r => r.run_id === th.dataset.run);
    if (!run) return;
    bindTooltip(th, [
      run.run_id,
      `${run.group_name || "—"} · ${run.agent_id || "—"}`,
      `literal ${fmtPct(run.literal_pass_rate)} · avg ${fmtMs(run.avg_duration_ms)}`,
      `${run.case_count ?? 0} cases · ${fmtTime(run.generated_at)}`
    ].join("\n"));
  });

  // click + tooltip + kbd-focus on cells
  $$(".cell[data-run]", $("#matrix-table")).forEach(cell => {
    cell.addEventListener("click", () => openDrilldown(cell.dataset.run, cell.dataset.case));
    cell.addEventListener("focus", () => {
      $$(".matrix-wrap .cell.kbd-focus").forEach(x => x.classList.remove("kbd-focus"));
      cell.classList.add("kbd-focus");
      state.matrixCursor = {
        rowIdx: Number(cell.dataset.row),
        colIdx: Number(cell.dataset.col)
      };
    });
  });

  // restore keyboard cursor across re-renders (e.g. filter change)
  if (state.matrixCursor) focusMatrixCell(state.matrixCursor.rowIdx, state.matrixCursor.colIdx);
}

function renderMatrixCell(cell, run, rowCase, rowIdx, colIdx) {
  if (!cell) {
    return `<td class="cell empty" data-row="${rowIdx}" data-col="${colIdx}">
      <div class="cell-main">—</div>
    </td>`;
  }
  const cls = classifyCell(cell);
  const main = cellMainLabel(cell, cls);
  const sub = [
    cell.retrieved !== null && cell.retrieved !== undefined ? `docs ${cell.retrieved}` : null,
    cell.score_max !== null && cell.score_max !== undefined ? `s ${Number(cell.score_max).toFixed(3)}` : null,
    cell.rerank_mode ? shortEnum(cell.rerank_mode, 14) : null
  ].filter(Boolean).join(" · ");
  const tt = [
    `${rowCase.case_id} × ${shortRun(run)}`,
    `literal_hit = ${cell.literal_hit ?? "—"}${cell.literal_ratio != null ? ` (${Math.round(cell.literal_ratio * 100)}%)` : ""}`,
    `coverage = ${cell.source_coverage_label || "—"}`,
    `retrieved = ${cell.retrieved ?? "—"} · score_max = ${cell.score_max != null ? Number(cell.score_max).toFixed(3) : "—"}`,
    cell.rerank_mode ? `rerank = ${cell.rerank_mode}` : null,
    cell.duration_ms != null ? `duration ${fmtMs(cell.duration_ms)}` : null
  ].filter(Boolean).join("\n");
  const td = `
    <td class="cell ${cls}"
        data-run="${escapeHtml(run.run_id)}" data-case="${escapeHtml(rowCase.case_id)}"
        data-row="${rowIdx}" data-col="${colIdx}"
        tabindex="0" role="button"
        aria-label="${escapeHtml(`${rowCase.case_id} × ${shortRun(run)} — ${main}`)}"
        data-tt="${escapeHtml(tt)}">
      <div class="cell-main">${escapeHtml(main)}</div>
      <div class="cell-sub">${escapeHtml(sub || "—")}</div>
    </td>`;
  return td;
}

/* Move kbd focus to a specific cell and scroll into view within the matrix
   container (avoids the page jumping). */
function focusMatrixCell(rowIdx, colIdx) {
  const runs = state.matrixOrderedRuns || [];
  const rows = state.matrixOrderedRows || [];
  if (!runs.length || !rows.length) return;
  rowIdx = Math.max(0, Math.min(rows.length - 1, rowIdx));
  colIdx = Math.max(0, Math.min(runs.length - 1, colIdx));
  const cell = $(`.matrix-wrap td.cell[data-row="${rowIdx}"][data-col="${colIdx}"]`);
  if (!cell) return;
  cell.focus({ preventScroll: true });
  const scroll = $(".matrix-wrap");
  if (scroll) {
    const cr = cell.getBoundingClientRect();
    const sr = scroll.getBoundingClientRect();
    if (cr.top < sr.top + 60) scroll.scrollTop -= (sr.top + 60 - cr.top);
    if (cr.bottom > sr.bottom - 20) scroll.scrollTop += (cr.bottom - sr.bottom + 20);
    if (cr.left < sr.left + 220) scroll.scrollLeft -= (sr.left + 220 - cr.left);
    if (cr.right > sr.right - 20) scroll.scrollLeft += (cr.right - sr.right + 20);
  }
}

function handleMatrixKeydown(e) {
  if (state.view !== "matrix" || state.modalOpen) return;
  if (e.target.tagName === "SELECT" || e.target.tagName === "INPUT" || e.target.tagName === "TEXTAREA") return;
  const runs = state.matrixOrderedRuns || [];
  const rows = state.matrixOrderedRows || [];
  if (!runs.length || !rows.length) return;
  const cur = state.matrixCursor || { rowIdx: 0, colIdx: 0 };
  let { rowIdx, colIdx } = cur;
  let handled = true;
  switch (e.key) {
    case "ArrowUp": rowIdx--; break;
    case "ArrowDown": rowIdx++; break;
    case "ArrowLeft": colIdx--; break;
    case "ArrowRight": colIdx++; break;
    case "Home": colIdx = 0; break;
    case "End": colIdx = runs.length - 1; break;
    case "Enter":
    case " ": {
      const row = rows[cur.rowIdx];
      const run = runs[cur.colIdx];
      const cell = row?.cells?.[cur.colIdx];
      if (row && run && cell) openDrilldown(run.run_id, row.case.case_id);
      e.preventDefault();
      return;
    }
    default: handled = false;
  }
  if (handled) {
    e.preventDefault();
    state.matrixCursor = { rowIdx, colIdx };
    focusMatrixCell(rowIdx, colIdx);
  }
}

/* ============================================================
   COMPARE
   ============================================================ */
/* Metric definitions for the per-case A vs B diff. `kind` controls how the
   delta is computed; `good` says which direction is better for that metric;
   `crossRerankNeutral=true` means up/down is meaningless when rerank backends
   differ (e.g. RRF vs LOCAL_BGE — score_max has different scale). */
const COMPARE_METRICS = [
  { key: "literal_hit", label: "literal hit", kind: "str" },
  { key: "literal_ratio", label: "literal ratio", kind: "num", good: "high",
    fmt: v => v == null ? "—" : `${Math.round(Number(v) * 100)}%` },
  { key: "source_coverage_label", label: "coverage label", kind: "str" },
  { key: "expected_points_final_hit_count", label: "expected hit", kind: "num", good: "high",
    fmt: v => v == null ? "—" : String(v) },
  { key: "retrieved", label: "retrieved", kind: "num", good: "neutral" },
  { key: "score_max", label: "score_max", kind: "num", good: "high",
    crossRerankNeutral: true,
    fmt: v => v == null ? "—" : Number(v).toFixed(3) },
  { key: "empty", label: "empty", kind: "bool", good: "false" },
  { key: "duration_ms", label: "duration", kind: "num", good: "low", fmt: fmtMs },
  { key: "rerank_mode", label: "rerank mode", kind: "str" },
  { key: "profile_source", label: "profile source", kind: "str" },
  { key: "query_rewrite_mode", label: "rewrite mode", kind: "str" }
];

function compareDelta(va, vb, metric, ctx) {
  // both missing → flat; one missing → shift (something changed but no order)
  if ((va === null || va === undefined) && (vb === null || vb === undefined)) {
    return { cls: "flat", sym: "·" };
  }
  if (va === null || va === undefined || vb === null || vb === undefined) {
    return { cls: "shift", sym: "≠" };
  }
  // cross-rerank score scales are not comparable — degrade to neutral shift
  if (metric.crossRerankNeutral && ctx && ctx.rerankA && ctx.rerankB && ctx.rerankA !== ctx.rerankB) {
    if (va === vb) return { cls: "flat", sym: "=" };
    return { cls: "shift", sym: "≠", neutral: true };
  }
  if (metric.kind === "bool") {
    if (va === vb) return { cls: "flat", sym: "=" };
    const aGood = (metric.good === "true" && va === true) || (metric.good === "false" && va === false);
    return { cls: aGood ? "up" : "down", sym: aGood ? "↑" : "↓" };
  }
  if (metric.kind === "num") {
    const na = Number(va), nb = Number(vb);
    if (na === nb) return { cls: "flat", sym: "=" };
    if (metric.good === "neutral") return { cls: "shift", sym: "≠", neutral: true };
    const aBetter = metric.good === "high" ? na > nb : na < nb;
    return { cls: aBetter ? "up" : "down", sym: aBetter ? "↑" : "↓" };
  }
  // str
  if (va === vb) return { cls: "flat", sym: "=" };
  return { cls: "shift", sym: "≠" };
}
function compareValDisplay(v, metric) {
  if (v === null || v === undefined) return "—";
  if (typeof v === "boolean") return v ? "true" : "false";
  if (metric.fmt) return metric.fmt(v);
  if (typeof v === "number") return Number.isInteger(v) ? String(v) : v.toFixed(3);
  return softBreakEnum(String(v));
}

async function renderCompare() {
  populateRunSelects();
  if (!state.compareA && state.runs[0]) state.compareA = state.runs[0].run_id;
  if (!state.compareB && state.runs[1]) state.compareB = state.runs[1].run_id;
  const sa = $("#compare-a"), sb = $("#compare-b");
  if (sa) sa.value = state.compareA || "";
  if (sb) sb.value = state.compareB || "";

  // Keep URL hash in sync with the picked pair so the compare view is
  // shareable — select.change, preset click and hash apply all route here.
  writeHash();

  await renderPresetRow();
  renderCompareCardMeta("compare-a-meta", state.runs.find(r => r.run_id === state.compareA));
  renderCompareCardMeta("compare-b-meta", state.runs.find(r => r.run_id === state.compareB));

  const toolbar = $("#compare-toolbar");
  const list = $("#compare-list");
  const summary = $("#compare-summary");

  if (!state.compareA || !state.compareB) {
    summary.innerHTML = "";
    if (toolbar) toolbar.hidden = true;
    list.innerHTML = emptyState("Pick run A and run B to compare");
    return;
  }
  if (state.compareA === state.compareB) {
    summary.innerHTML = "";
    if (toolbar) toolbar.hidden = true;
    list.innerHTML = emptyState("Run A and Run B are identical", "Pick a different run to see deltas.");
    return;
  }

  let payload;
  try {
    payload = await api(`/api/compare${qs({ run_a: state.compareA, run_b: state.compareB })}`);
  } catch (err) {
    summary.innerHTML = "";
    if (toolbar) toolbar.hidden = true;
    list.innerHTML = emptyState("Failed to load compare", err.message);
    return;
  }

  renderCompareCardMeta("compare-a-meta", payload.run_a);
  renderCompareCardMeta("compare-b-meta", payload.run_b);

  const rows = payload.rows || [];
  if (toolbar) toolbar.hidden = !rows.length;

  // Compute per-case metric diff + verdict (matches viewer's logic)
  const perCase = rows.map(row => {
    const c = row.case || {};
    const a = row.a, b = row.b;
    const ctx = { rerankA: a?.rerank_mode ?? null, rerankB: b?.rerank_mode ?? null };
    const metricRows = COMPARE_METRICS.map(m => {
      const va = a ? a[m.key] : null;
      const vb = b ? b[m.key] : null;
      const d = compareDelta(va, vb, m, ctx);
      return { metric: m, va, vb, delta: d, isDiff: d.cls !== "flat" };
    });
    // verdict: clear A win, clear B regression, mixed (both directions),
    // shift (only neutral changes), or flat (everything equal).
    const hasUp = metricRows.some(x => x.delta.cls === "up");
    const hasDown = metricRows.some(x => x.delta.cls === "down");
    const hasShift = metricRows.some(x => x.delta.cls === "shift");
    let verdict = "flat";
    if (hasUp && hasDown) verdict = "mixed";
    else if (hasUp) verdict = "up";
    else if (hasDown) verdict = "down";
    else if (hasShift) verdict = "shift";
    const diffCount = metricRows.filter(x => x.isDiff).length;
    return { c, a, b, metricRows, verdict, diffCount, hasUp, hasDown, hasShift };
  });

  // Track ordered case IDs for drilldown ←/→ navigation within compare view
  state._compareCaseIds = perCase.map(p => p.c.case_id).filter(Boolean);

  // Re-seed openCases when pair changes — auto-open top 3 most-different cases
  const pairKey = `${state.compareA}|${state.compareB}`;
  if (state.comparePairKey !== pairKey) {
    state.compareOpenCases = new Set(
      [...perCase]
        .filter(p => p.diffCount > 0)
        .sort((a, b) => b.diffCount - a.diffCount)
        .slice(0, 3)
        .map(p => p.c.case_id)
    );
    state.comparePairKey = pairKey;
  }

  // Summary bar (verdict-based buckets)
  renderCompareSummary(perCase, summary);

  if (!rows.length) {
    list.innerHTML = emptyState("No overlapping cases between these runs");
    return;
  }

  // Sync toolbar to state, attach listeners (idempotent — re-set value but only bind once)
  if (toolbar && !toolbar.dataset.bound) {
    toolbar.dataset.bound = "1";
    $("#cmp-diff-only").addEventListener("change", e => {
      state.compareDiffOnly = e.target.checked;
      renderCompare();
    });
    $("#cmp-lcs-diff").addEventListener("change", e => {
      state.compareLcsDiff = e.target.checked;
      renderCompare();
    });
  }
  if ($("#cmp-diff-only")) $("#cmp-diff-only").checked = state.compareDiffOnly;
  if ($("#cmp-lcs-diff")) $("#cmp-lcs-diff").checked = state.compareLcsDiff;

  // Filter by diff-only — hide cases with no diff at all
  const visibleCases = state.compareDiffOnly
    ? perCase.filter(p => p.diffCount > 0)
    : perCase;

  if (!visibleCases.length) {
    list.innerHTML = emptyState("No differences between A and B",
      "Toggle off ‘only show diff metrics’ to see all cases.");
    return;
  }

  list.innerHTML = visibleCases.map(p => renderCompareCase(p, payload)).join("");

  // Hook up: collapse/expand + open drilldown via dedicated button (avoid swallowing click)
  $$(".cmp-case", list).forEach(block => {
    const cid = block.dataset.case;
    block.querySelector(".cmp-case-head").addEventListener("click", () => {
      if (state.compareOpenCases.has(cid)) {
        state.compareOpenCases.delete(cid);
        block.classList.remove("open");
      } else {
        state.compareOpenCases.add(cid);
        block.classList.add("open");
      }
    });
    block.querySelectorAll("[data-drill-run]").forEach(btn => {
      btn.addEventListener("click", e => {
        e.stopPropagation();
        openDrilldown(btn.dataset.drillRun, cid);
      });
    });
  });
}

function renderCompareCase(p, payload) {
  const c = p.c;
  const isOpen = state.compareOpenCases.has(c.case_id);
  const visibleMetrics = state.compareDiffOnly
    ? p.metricRows.filter(r => r.isDiff)
    : p.metricRows;
  const verdictLabel = {
    up: "A wins", down: "B wins", mixed: "mixed", shift: "shifted", flat: "same"
  }[p.verdict];

  const aAns = p.a?.answer || "—";
  const bAns = p.b?.answer || "—";
  const useLcs = state.compareLcsDiff && aAns !== "—" && bAns !== "—" && aAns !== bAns;
  let aAnsHtml, bAnsHtml;
  if (useLcs) {
    const diff = diffLines(aAns, bAns);
    aAnsHtml = renderAnswerLines(diff.a);
    bAnsHtml = renderAnswerLines(diff.b);
  } else {
    aAnsHtml = escapeHtml(aAns);
    bAnsHtml = escapeHtml(bAns);
  }

  return `
    <div class="cmp-case ${isOpen ? "open" : ""}" id="cmp-case-${escapeHtml(c.case_id)}" data-case="${escapeHtml(c.case_id)}">
      <div class="cmp-case-head">
        <svg class="chev" viewBox="0 0 16 16" aria-hidden="true"><path d="M5.5 3.5l5 4.5-5 4.5" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
        <span class="cid">${escapeHtml(c.case_id)}</span>
        <span class="cverdict ${p.verdict}">${escapeHtml(verdictLabel)}</span>
        <span class="cq">${escapeHtml(c.question || c.family || "")}</span>
        <span class="cdiff ${p.diffCount > 0 ? "has-diff" : ""}">${p.diffCount}/${p.metricRows.length} diff</span>
      </div>
      <div class="cmp-body">
        ${visibleMetrics.length ? visibleMetrics.map(r => `
          <div class="cmp-metric">
            <div class="mname">${escapeHtml(r.metric.label)}</div>
            <div class="mval ${r.va == null ? "muted" : ""}" title="${escapeHtml(compareValDisplay(r.va, r.metric))}">${escapeHtml(compareValDisplay(r.va, r.metric))}</div>
            <div class="cdelta ${r.delta.cls}"${r.delta.neutral ? ' title="neutralised — values not comparable across rerank backends or neutral direction"' : ""}>${r.delta.sym}</div>
            <div class="mval ${r.vb == null ? "muted" : ""}" title="${escapeHtml(compareValDisplay(r.vb, r.metric))}">${escapeHtml(compareValDisplay(r.vb, r.metric))}</div>
          </div>
        `).join("") : `<div class="subtle" style="padding:10px 0">All metrics identical between A and B for this case.</div>`}
        <div class="cmp-answer">
          <div class="panel">
            <div class="ptitle">Run A · answer · <a href="#" data-drill-run="${escapeHtml(payload.run_a?.run_id || state.compareA)}">open drilldown ↗</a></div>
            ${aAnsHtml}
          </div>
          <div class="panel">
            <div class="ptitle">Run B · answer · <a href="#" data-drill-run="${escapeHtml(payload.run_b?.run_id || state.compareB)}">open drilldown ↗</a></div>
            ${bAnsHtml}
          </div>
        </div>
      </div>
    </div>`;
}

function renderCompareSummary(perCase, host) {
  if (!perCase.length) {
    host.innerHTML = `<div class="compare-summary"><div class="cs-head"><h3>Compare Summary</h3></div><div class="subtle" style="padding:10px 0">No overlapping cases.</div></div>`;
    return;
  }
  const buckets = { up: 0, down: 0, mixed: 0, shift: 0, flat: 0 };
  perCase.forEach(p => { buckets[p.verdict] = (buckets[p.verdict] || 0) + 1; });
  const total = Math.max(1, perCase.length);

  // Jump targets — first occurrence of each non-flat verdict
  const firstUp = perCase.find(p => p.verdict === "up");
  const firstDown = perCase.find(p => p.verdict === "down");
  const firstMixed = perCase.find(p => p.verdict === "mixed");
  const firstShift = perCase.find(p => p.verdict === "shift");

  // Empty buckets render nothing — otherwise the row shows a dead "→ first
  // shifted case" link that looks clickable but goes nowhere.
  const jumpLink = (label, target) => {
    if (!target) return "";
    return `<a href="#cmp-case-${escapeHtml(target.c.case_id)}" data-jump="${escapeHtml(target.c.case_id)}">${escapeHtml(label)} · ${escapeHtml(target.c.case_id)}</a>`;
  };

  host.innerHTML = `
    <div class="compare-summary">
      <div class="cs-head">
        <h3>Compare Summary</h3>
        <div class="cs-count">${perCase.length} cases compared</div>
      </div>
      <div class="cs-bar" role="img" aria-label="A wins ${buckets.up}, B wins ${buckets.down}, mixed ${buckets.mixed}, shifted ${buckets.shift}, flat ${buckets.flat}">
        ${buckets.up ? `<div class="cs-seg up" style="width:${(buckets.up / total * 100).toFixed(2)}%"></div>` : ""}
        ${buckets.down ? `<div class="cs-seg down" style="width:${(buckets.down / total * 100).toFixed(2)}%"></div>` : ""}
        ${buckets.mixed ? `<div class="cs-seg mixed" style="width:${(buckets.mixed / total * 100).toFixed(2)}%"></div>` : ""}
        ${buckets.shift ? `<div class="cs-seg shift" style="width:${(buckets.shift / total * 100).toFixed(2)}%"></div>` : ""}
        ${buckets.flat ? `<div class="cs-seg flat" style="width:${(buckets.flat / total * 100).toFixed(2)}%"></div>` : ""}
      </div>
      <div class="cs-legend">
        <div class="cs-leg-item"><div class="top"><i class="sw pass"></i>↑ A wins</div><div class="num">${buckets.up}</div></div>
        <div class="cs-leg-item"><div class="top"><i class="sw fail"></i>↓ B wins</div><div class="num">${buckets.down}</div></div>
        <div class="cs-leg-item"><div class="top"><i class="sw mixed"></i>↑↓ mixed</div><div class="num">${buckets.mixed}</div></div>
        <div class="cs-leg-item"><div class="top"><i class="sw partial"></i>≠ shifted</div><div class="num">${buckets.shift}</div></div>
        <div class="cs-leg-item"><div class="top"><i class="sw empty"></i>= unchanged</div><div class="num">${buckets.flat}</div></div>
      </div>
      ${(buckets.up + buckets.down + buckets.mixed + buckets.shift) > 0 ? `
        <div class="cmp-jump">
          ${jumpLink("→ first A-win", firstUp)}
          ${jumpLink("→ first B-win", firstDown)}
          ${jumpLink("→ first mixed", firstMixed)}
          ${jumpLink("→ first shifted", firstShift)}
        </div>` : ""}
    </div>`;

  host.querySelectorAll("a[data-jump]").forEach(a => {
    a.addEventListener("click", e => {
      e.preventDefault();
      const cid = a.dataset.jump;
      state.compareOpenCases.add(cid);
      const block = document.getElementById(`cmp-case-${cid}`);
      if (block) block.classList.add("open");
      setTimeout(() => {
        const target = document.getElementById(`cmp-case-${cid}`);
        if (target) {
          const top = target.getBoundingClientRect().top + window.scrollY - 80;
          window.scrollTo({ top, behavior: "smooth" });
        }
      }, 20);
    });
  });
}

function renderCompareCardMeta(elId, run) {
  const el = $(`#${elId}`);
  if (!el) return;
  if (!run) {
    el.innerHTML = `<div class="k">—</div><div class="v">—</div>`;
    return;
  }
  el.innerHTML = `
    <div><div class="k">group</div><div class="v">${escapeHtml(run.group_name || "—")}</div></div>
    <div><div class="k">literal</div><div class="v">${fmtPct(run.literal_pass_rate)}</div></div>
    <div><div class="k">cases</div><div class="v">${escapeHtml(run.case_count ?? "—")}</div></div>
    <div><div class="k">generated</div><div class="v mono" style="font-size:11px">${escapeHtml(fmtTime(run.generated_at))}</div></div>
    <div><div class="k">agent</div><div class="v mono" style="font-size:11px">${escapeHtml(run.agent_id || "—")}</div></div>
    <div><div class="k">duration</div><div class="v">${fmtMs(run.avg_duration_ms)}</div></div>`;
}

async function renderPresetRow() {
  const el = $("#preset-row");
  try {
    const payload = await api("/api/presets");
    const presets = payload.presets || [];
    if (!presets.length) { el.innerHTML = ""; return; }
    el.innerHTML = presets.map(p => `
      <button class="chip-btn" type="button" ${p.available ? "" : "disabled"}
        data-a="${escapeHtml(p.run_a || "")}" data-b="${escapeHtml(p.run_b || "")}"
        title="${escapeHtml(p.reason || p.name)}">
        ${escapeHtml(p.name)}
      </button>`).join("");
    $$(".chip-btn", el).forEach(btn => {
      btn.addEventListener("click", () => {
        if (btn.disabled) return;
        const a = btn.dataset.a, b = btn.dataset.b;
        if (!a || !b) return;
        state.compareA = a; state.compareB = b;
        renderCompare();
      });
    });
  } catch (_) { el.innerHTML = ""; }
}

/* ============================================================
   INSIGHTS
   ============================================================ */
async function renderInsights() {
  let payload;
  try {
    payload = await api("/api/insights");
  } catch (err) {
    $("#insight-runs-table").innerHTML = `<tbody><tr><td>${emptyState("Failed to load insights", err.message)}</td></tr></tbody>`;
    $("#insight-cases-table").innerHTML = "";
    return;
  }
  const runs = payload.runs || [];
  $("#insight-runs-table").innerHTML = runs.length ? `
    <thead><tr>
      <th>run</th>
      <th class="num">completed</th>
      <th class="num">literal</th>
      <th class="num">avg dur</th>
      <th class="num">rerank fail</th>
      <th class="num">profile use</th>
    </tr></thead>
    <tbody>
      ${runs.map(run => `
        <tr>
          <td>
            <div class="row-title">${escapeHtml(shortRun(run))}</div>
            <div class="row-sub">${escapeHtml(run.group_name || "—")}</div>
          </td>
          <td class="num">${escapeHtml(run.completed_count ?? 0)}/${escapeHtml(run.result_count ?? 0)}</td>
          <td class="num">${fmtPct(run.literal_pass_rate)}</td>
          <td class="num">${fmtMs(run.avg_duration_ms)}</td>
          <td class="num">${escapeHtml(run.rerank_failure_count ?? 0)}</td>
          <td class="num">${escapeHtml(run.profile_usage_count ?? 0)}</td>
        </tr>`).join("")}
    </tbody>` : `<tbody><tr><td>${emptyState("No insights yet")}</td></tr></tbody>`;

  const cases = (payload.cases || []).slice().sort((a, b) =>
    (b.literal_failure_count || 0) - (a.literal_failure_count || 0));
  $("#insight-cases-table").innerHTML = cases.length ? `
    <thead><tr>
      <th>case</th>
      <th>origin</th>
      <th class="num">runs</th>
      <th class="num">literal fail</th>
      <th class="num">mismatch</th>
      <th class="num">coverage vol.</th>
    </tr></thead>
    <tbody>
      ${cases.map(c => `
        <tr>
          <td>
            <div class="row-title mono">${escapeHtml(c.case_id)}</div>
            <div class="row-sub">${escapeHtml(c.family || "—")}</div>
          </td>
          <td>${badge(c.origin || "—", c.origin === "external" ? "info" : "ok")}</td>
          <td class="num">${escapeHtml(c.run_count ?? 0)}</td>
          <td class="num">${escapeHtml(c.literal_failure_count ?? 0)}</td>
          <td class="num">${escapeHtml(c.literal_mismatch_count ?? 0)}</td>
          <td class="num">${escapeHtml(c.coverage_volatility ?? 0)}</td>
        </tr>`).join("")}
    </tbody>` : `<tbody><tr><td>${emptyState("No case insights")}</td></tr></tbody>`;
}

/* ============================================================
   DRILLDOWN MODAL
   ============================================================ */
/* Compute the ordered list of case_ids the user can step through with ←/→.
   Source of truth depends on which view opened the drilldown:
     - matrix view: the current sorted rows
     - compare view: the rows shared by both runs
     - elsewhere: cases.json order, filtered to ones present in the run
   Always falls back gracefully to a single-entry array. */
function buildDrillCaseOrder(runId) {
  if (state.view === "matrix" && state.matrixOrderedRows?.length) {
    return state.matrixOrderedRows.map(r => r.case?.case_id).filter(Boolean);
  }
  if (state.view === "compare" && state._compareCaseIds?.length) {
    return state._compareCaseIds.slice();
  }
  // fallback: all cases, in original index order
  return state.cases.map(c => c.case_id);
}

async function openDrilldown(runId, caseId) {
  // remember focus to restore on close; only re-capture if not already in modal
  if (!state.modalOpen) state.lastFocus = document.activeElement;

  const caseOrder = buildDrillCaseOrder(runId);
  const order = caseOrder.includes(caseId) ? caseOrder : [caseId, ...caseOrder];
  state.drillContext = { runId, caseId, caseOrder: order };

  $("#modal-eyebrow").textContent = "loading drilldown";
  $("#modal-title").textContent = `${caseId}`;
  $("#modal-subtitle").textContent = "";
  $("#modal-body").innerHTML = emptyState("Loading…");
  $("#modal-backdrop").hidden = false;
  state.modalOpen = true;
  document.body.style.overflow = "hidden";

  renderModalNav();   // prev/next buttons reflect new context
  setTimeout(() => $("#modal-close")?.focus(), 0);
  writeHash();

  try {
    const payload = await api(`/api/results/${encodeURIComponent(runId)}/${encodeURIComponent(caseId)}`);
    const caseObj = state.cases.find(c => c.case_id === caseId) || {};
    const runObj = state.runs.find(r => r.run_id === runId) || { run_id: runId };
    const tags = [
      payload.score_mode || caseObj.score_mode,
      shortRun(runObj),
      runObj.group_name
    ].filter(Boolean).map(escapeHtml).join("  ·  ");
    $("#modal-eyebrow").innerHTML = tags;
    $("#modal-title").textContent = caseId;
    $("#modal-subtitle").textContent = payload.question || caseObj.question || "";
    $("#modal-body").innerHTML = renderDrilldown(payload, caseObj);
    $("#modal-body").scrollTop = 0;
  } catch (err) {
    $("#modal-eyebrow").textContent = "error";
    $("#modal-body").innerHTML = emptyState("Failed to load drilldown", err.message);
  }
}

/* Render the prev / next / position label in the modal header. */
function renderModalNav() {
  const host = $("#modal-nav");
  if (!host) return;
  const ctx = state.drillContext;
  if (!ctx) { host.innerHTML = ""; return; }
  const idx = ctx.caseOrder.indexOf(ctx.caseId);
  const total = ctx.caseOrder.length;
  const hasPrev = idx > 0;
  const hasNext = idx >= 0 && idx < total - 1;
  host.innerHTML = `
    <button class="mnav-btn" id="modal-prev" aria-label="prev case (←)" title="prev case (←)" ${hasPrev ? "" : "disabled"}>
      <svg width="14" height="14" viewBox="0 0 16 16" aria-hidden="true"><path d="M10 3.5l-5 4.5 5 4.5" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
    </button>
    <span class="mnav-pos">${idx >= 0 ? String(idx + 1).padStart(2, "0") : "—"} / ${String(total).padStart(2, "0")}</span>
    <button class="mnav-btn" id="modal-next" aria-label="next case (→)" title="next case (→)" ${hasNext ? "" : "disabled"}>
      <svg width="14" height="14" viewBox="0 0 16 16" aria-hidden="true"><path d="M6 3.5l5 4.5-5 4.5" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
    </button>`;
  if (hasPrev) $("#modal-prev").addEventListener("click", () => navDrill(-1));
  if (hasNext) $("#modal-next").addEventListener("click", () => navDrill(1));
}

/* Step drilldown to the prev/next case in caseOrder. Skips no-op when at boundary. */
function navDrill(direction) {
  const ctx = state.drillContext;
  if (!ctx) return;
  const idx = ctx.caseOrder.indexOf(ctx.caseId);
  if (idx < 0) return;
  const target = idx + direction;
  if (target < 0 || target >= ctx.caseOrder.length) return;
  const nextCase = ctx.caseOrder[target];
  if (!nextCase) return;
  openDrilldown(ctx.runId, nextCase);
}

function closeModal() {
  $("#modal-backdrop").hidden = true;
  state.modalOpen = false;
  state.drillContext = null;
  document.body.style.overflow = "";
  if (state.lastFocus && typeof state.lastFocus.focus === "function") {
    state.lastFocus.focus();
  }
  writeHash();
}

function renderDrilldown(result, caseObj) {
  const finalDocs = result.documents?.final || [];
  const preDocs = result.documents?.pre_rerank || [];
  const expected = result.expected_points || caseObj.expected_points || [];
  const missing = result.missing_points || [];
  const missingSet = new Set(missing.map(String));

  // Coverage hit ratio
  const hitN = Number(result.expected_points_final_hit_count ?? NaN);
  const hitT = Number(result.expected_points_final_total ?? NaN);
  const hasHit = !Number.isNaN(hitN) && !Number.isNaN(hitT) && hitT > 0;
  const hitRatio = hasHit ? hitN / hitT : 0;
  const hitCls = hitRatio >= 1 ? "" : (hitRatio > 0 ? "partial" : "fail");

  return `
    <div class="drill-grid">
      ${drillMetric("literal", result.literal_hit || "—",
        `${result.score_mode || "—"}${result.literal_ratio !== null && result.literal_ratio !== undefined ? " · " + fmtPct(result.literal_ratio) : ""}`)}
      ${drillMetric("coverage", result.source_coverage_label || "—", result.source_file || caseObj.source_file || "—")}
      ${drillMetric("rerank", result.rerank_mode || "—", result.rerank_runtime_model || "—")}
      ${drillMetric("profile", result.profile_source || "—",
        `${(result.selected_profile_hints || []).length} hint(s)`)}
    </div>

    <section class="drill-section">
      <h3>Expected points <span class="h3-tag">${expected.length} total · ${missing.length} missing</span></h3>
      ${expected.length ? `<ul class="expected-list">
        ${expected.map(p => {
          const text = typeof p === "string" ? p : (p?.text || p?.point || JSON.stringify(p));
          const missed = missingSet.has(String(text));
          return `<li class="${missed ? "missed" : ""}"><span class="dot"></span><span>${escapeHtml(text)}</span></li>`;
        }).join("")}
      </ul>` : `<div class="subtle">No expected points recorded for this case.</div>`}
    </section>

    <section class="drill-section">
      <h3>Answer <span class="h3-tag">${result.answer ? `${String(result.answer).length} chars` : "no answer"}</span></h3>
      <div class="answer-pre">${escapeHtml(result.answer || result.answer_preview || "—")}</div>
    </section>

    <section class="drill-section">
      <h3>Source coverage <span class="h3-tag">expected: ${escapeHtml(caseObj.source_file || result.source_file || "—")}</span></h3>
      <table class="kv-table">
        <tr>
          <th>source file in final ctx</th>
          <td>${renderBoolPill(result.source_file_in_final_context)}</td>
        </tr>
        <tr>
          <th>expected section in final ctx</th>
          <td>${renderBoolPill(result.expected_section_in_final_context)}</td>
        </tr>
        <tr>
          <th>expected points hit</th>
          <td>
            ${escapeHtml(hasHit ? `${hitN} / ${hitT}` : "—")}
            ${hasHit ? `<div class="ratio-bar ${hitCls}" aria-label="${Math.round(hitRatio*100)}%"><span style="width:${(hitRatio*100).toFixed(1)}%"></span></div>` : ""}
          </td>
        </tr>
        <tr>
          <th>coverage label</th>
          <td><span class="kv-pill">${escapeHtml(result.source_coverage_label || "—")}</span></td>
        </tr>
      </table>
    </section>

    <section class="drill-section">
      <h3>Rewrite, profile & salience <span class="h3-tag">query / context shaping</span></h3>
      <table class="kv-table">
        <tr><th>rewrite mode</th><td>${escapeHtml(result.query_rewrite_mode || "—")}</td></tr>
        <tr><th>rewrite variants</th><td>${renderPillList(result.rewrite_variants)}</td></tr>
        <tr><th>profile source</th><td>${escapeHtml(result.profile_source || "—")}</td></tr>
        <tr><th>selected profile hints</th><td>${renderPillList(result.selected_profile_hints)}</td></tr>
        <tr><th>context salience cues</th><td>${renderPillList(result.context_salience_cues)}</td></tr>
        <tr><th>salience expansions</th><td>${escapeHtml(result.context_salience_expansions ?? "—")}</td></tr>
      </table>
    </section>

    <section class="drill-section">
      <h3>Pre-rerank documents <span class="h3-tag">${preDocs.length} doc(s)</span></h3>
      ${renderDocsTable(preDocs, caseObj, false)}
    </section>

    <section class="drill-section">
      <h3>Final documents <span class="h3-tag">${finalDocs.length} doc(s) · highlight = in expected source</span></h3>
      ${renderDocsTable(finalDocs, caseObj, true)}
    </section>
  `;
}

function renderBoolPill(value) {
  if (value === true) return badge("yes", "ok");
  if (value === false) return badge("no", "bad");
  return badge("—", "muted");
}

function renderPillList(arr) {
  if (!arr || !arr.length) return `<span class="subtle">—</span>`;
  return arr.map(v => `<span class="kv-pill">${escapeHtml(typeof v === "string" ? v : JSON.stringify(v))}</span>`).join("");
}

function renderDocsTable(docs, caseObj, markExpected) {
  if (!docs || !docs.length) return `<div class="subtle">No documents recorded.</div>`;
  // Hide columns where every row is empty so the table stays compact across
  // pipelines that don't surface chunk indexes or score breakdowns.
  const hasChunk = docs.some(d => d.chunk_index !== null && d.chunk_index !== undefined && d.chunk_index !== "");
  const hasScores = docs.some(d => d.scores && typeof d.scores === "object" && Object.keys(d.scores).length);
  const hasPreview = docs.some(d => d.text_excerpt != null && d.text_excerpt !== "");
  return `<table class="docs-table">
    <thead><tr>
      <th class="num">rank</th>
      <th>source</th>
      ${hasChunk ? `<th class="num">chunk</th>` : ""}
      ${hasScores ? `<th>scores</th>` : ""}
      ${hasPreview ? `<th>preview</th>` : ""}
    </tr></thead>
    <tbody>
      ${docs.map((doc, i) => {
        const inExpected = markExpected && docInExpectedSource(doc, caseObj);
        const scores = doc.scores && typeof doc.scores === "object"
          ? Object.entries(doc.scores).map(([k, v]) => `<div>${escapeHtml(k)} = ${escapeHtml(typeof v === "number" ? v.toFixed(4) : v)}</div>`).join("")
          : "<div>—</div>";
        return `<tr class="${inExpected ? "in-expected" : ""}">
          <td class="num">${escapeHtml(doc.rank ?? (i + 1))}</td>
          <td class="src">${escapeHtml(doc.source_path || "—")}${inExpected ? ` ${badge("in expected", "ok")}` : ""}</td>
          ${hasChunk ? `<td class="num">${escapeHtml(doc.chunk_index ?? "—")}</td>` : ""}
          ${hasScores ? `<td><div class="doc-scores">${scores}</div></td>` : ""}
          ${hasPreview ? `<td class="preview">${escapeHtml(doc.text_excerpt || "—")}</td>` : ""}
        </tr>`;
      }).join("")}
    </tbody>
  </table>`;
}

/* ============================================================
   EVENTS / BOOT
   ============================================================ */
function bindEvents() {
  $$(".tab").forEach(btn => btn.addEventListener("click", () => setView(btn.dataset.view)));

  $("#reindex-btn").addEventListener("click", async () => {
    const btn = $("#reindex-btn");
    const originalLabel = btn.textContent;
    btn.disabled = true;
    btn.textContent = "Reindexing…";
    try {
      await api("/api/reindex", { method: "POST" });
      await loadBase();
      const fn = {
        dashboard: renderDashboard,
        runs: renderRuns,
        cases: renderCases,
        matrix: renderMatrix,
        compare: renderCompare,
        insights: renderInsights
      }[state.view];
      if (fn) await fn();
    } catch (err) {
      $("#health-line").textContent = `reindex failed — ${err.message}`;
    } finally {
      btn.disabled = false;
      btn.textContent = originalLabel;
    }
  });

  ["run-group-filter", "run-agent-filter"].forEach(id =>
    $(`#${id}`).addEventListener("change", renderRuns));
  ["case-origin-filter", "case-family-filter", "case-score-filter"].forEach(id =>
    $(`#${id}`).addEventListener("change", renderCases));
  ["matrix-origin-filter", "matrix-family-filter", "matrix-score-filter",
   "matrix-case-type-filter", "matrix-group-filter", "matrix-agent-filter"].forEach(id =>
    $(`#${id}`).addEventListener("change", renderMatrix));
  const hideEmptyBox = $("#matrix-hide-empty");
  if (hideEmptyBox) {
    hideEmptyBox.checked = !!state.matrixHideEmpty;
    hideEmptyBox.addEventListener("change", e => {
      state.matrixHideEmpty = e.target.checked;
      renderMatrix();
    });
  }

  $("#compare-a").addEventListener("change", e => { state.compareA = e.target.value; renderCompare(); });
  $("#compare-b").addEventListener("change", e => { state.compareB = e.target.value; renderCompare(); });

  $("#modal-close").addEventListener("click", closeModal);
  $("#modal-backdrop").addEventListener("click", e => {
    if (e.target.id === "modal-backdrop") closeModal();
  });
  document.addEventListener("keydown", e => {
    if (e.target.tagName === "INPUT" || e.target.tagName === "TEXTAREA" || e.target.tagName === "SELECT") return;
    if (state.modalOpen) {
      if (e.key === "Escape") { closeModal(); return; }
      if (e.key === "ArrowLeft" || e.key === "j") { navDrill(-1); e.preventDefault(); return; }
      if (e.key === "ArrowRight" || e.key === "k") { navDrill(1); e.preventDefault(); return; }
      return;
    }
    handleMatrixKeydown(e);
  });
  window.addEventListener("hashchange", applyHash);
}

async function boot() {
  bindEvents();
  try {
    await loadBase();
    // Honor URL hash on first paint: deep-link to a view, compare pair, or
    // drilldown opens immediately instead of always landing on dashboard.
    if (location.hash && location.hash.length > 1) {
      applyHash();
    } else {
      renderDashboard();
    }
  } catch (err) {
    $("#health-line").textContent = `index unavailable — ${err.message}`;
    $("#dashboard-metrics").innerHTML = metric("offline", "—", err.message);
    $("#recent-runs").innerHTML = emptyState("Backend unreachable", "Start the FastAPI server, then Reindex.");
    $("#warning-list").innerHTML = "";
    $("#quick-compare").innerHTML = "";
  }
}

boot();
