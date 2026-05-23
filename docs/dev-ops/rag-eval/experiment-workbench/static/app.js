const state = {
  view: "dashboard",
  runs: [],
  cases: [],
  groups: [],
  agents: [],
  families: [],
  health: null,
  compareA: null,
  compareB: null
};

const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

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
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") search.set(key, value);
  });
  const text = search.toString();
  return text ? `?${text}` : "";
}

async function api(path, options) {
  const response = await fetch(path, options);
  if (!response.ok) {
    let message = `${response.status} ${response.statusText}`;
    try {
      const payload = await response.json();
      message = payload.detail || message;
    } catch (_) {
      message = await response.text();
    }
    throw new Error(message);
  }
  return response.json();
}

function fmtPct(value) {
  if (value === null || value === undefined) return "—";
  return `${Math.round(Number(value) * 100)}%`;
}

function fmtMs(value) {
  if (value === null || value === undefined) return "—";
  const n = Number(value);
  return n >= 1000 ? `${(n / 1000).toFixed(1)}s` : `${n}ms`;
}

function shortRun(run) {
  return (run.run_id || "").replace(/^.*__/, "").replace(/^rag-eval-result-/, "");
}

function resultClass(result) {
  if (!result) return "empty";
  if (result.literal_ratio !== null && result.literal_ratio !== undefined) {
    if (Number(result.literal_ratio) >= 1) return "ok";
    if (Number(result.literal_ratio) > 0) return "warn";
    return "bad";
  }
  if (result.source_coverage_label) {
    return result.source_coverage_label.includes("mismatch") ? "warn" : "manual";
  }
  if (result.completed === false) return "bad";
  return "manual";
}

function badge(text, cls = "info") {
  return `<span class="badge ${cls}">${escapeHtml(text ?? "—")}</span>`;
}

function metric(label, value, sub) {
  return `<div class="metric"><div class="k">${escapeHtml(label)}</div><div class="v">${escapeHtml(value)}</div>${sub ? `<div class="subtle">${escapeHtml(sub)}</div>` : ""}</div>`;
}

function setView(view) {
  state.view = view;
  $$(".tab").forEach(btn => btn.classList.toggle("active", btn.dataset.view === view));
  $$(".view").forEach(section => section.classList.toggle("active", section.id === `view-${view}`));
  if (view === "dashboard") renderDashboard();
  if (view === "runs") renderRuns();
  if (view === "cases") renderCases();
  if (view === "matrix") renderMatrix();
  if (view === "compare") renderCompare();
  if (view === "insights") renderInsights();
}

function populateSelect(select, label, rows, valueKey, countKey) {
  const current = select.value;
  select.innerHTML = `<option value="">${escapeHtml(label)}</option>` + rows.map(row => {
    const value = row[valueKey] ?? "";
    const count = row[countKey] ?? "";
    return `<option value="${escapeHtml(value)}">${escapeHtml(value || "—")} ${count !== "" ? `(${count})` : ""}</option>`;
  }).join("");
  if ([...select.options].some(option => option.value === current)) select.value = current;
}

async function loadBase() {
  const [health, runsPayload, casesPayload] = await Promise.all([
    api("/api/health"),
    api("/api/runs"),
    api("/api/cases")
  ]);
  state.health = health;
  state.runs = runsPayload.runs;
  state.groups = runsPayload.groups;
  state.agents = runsPayload.agents;
  state.cases = casesPayload.cases;
  state.families = casesPayload.families;
  $("#health-line").textContent =
    `${health.counts.runs} runs · ${health.counts.internal_cases} internal · ${health.counts.external_cases} external`;
  populateSelect($("#run-group-filter"), "all groups", state.groups, "group_name", "run_count");
  populateSelect($("#run-agent-filter"), "all agents", state.agents, "agent_id", "run_count");
  populateSelect($("#case-family-filter"), "all families", state.families, "family", "case_count");
  populateSelect($("#matrix-family-filter"), "all families", state.families, "family", "case_count");
  populateSelect($("#matrix-group-filter"), "all groups", state.groups, "group_name", "run_count");
  populateSelect($("#matrix-agent-filter"), "all agents", state.agents, "agent_id", "run_count");
  const runOptions = state.runs.map(run => `<option value="${escapeHtml(run.run_id)}">${escapeHtml(shortRun(run))} · ${escapeHtml(run.group_name)}</option>`).join("");
  $("#compare-a").innerHTML = runOptions;
  $("#compare-b").innerHTML = runOptions;
  if (!state.compareA && state.runs[0]) state.compareA = state.runs[0].run_id;
  if (!state.compareB && state.runs[1]) state.compareB = state.runs[1].run_id;
  $("#compare-a").value = state.compareA || "";
  $("#compare-b").value = state.compareB || "";
}

function renderDashboard() {
  const counts = state.health?.counts || {};
  $("#dashboard-metrics").innerHTML = [
    metric("runs", counts.runs ?? "—", "indexed markdown reports"),
    metric("internal cases", counts.internal_cases ?? "—", "formal cases.json"),
    metric("external cases", counts.external_cases ?? "—", "external sample registry"),
    metric("case results", counts.case_results ?? "—", "run × case rows"),
    metric("document hits", counts.document_hits ?? "—", "pre-rerank + final docs")
  ].join("");

  $("#recent-runs").innerHTML = state.runs.slice(0, 10).map(run => `
    <div class="list-item">
      <strong>${escapeHtml(shortRun(run))}</strong>
      <span class="subtle mono">${escapeHtml(run.run_id)}</span>
      <span class="subtle">${escapeHtml(run.generated_at || "no time")} · ${escapeHtml(run.case_count)} cases · ${escapeHtml(run.config_hint || "")}</span>
    </div>
  `).join("");

  const warningRuns = state.runs.filter(run => (run.parse_warnings || []).length);
  $("#warning-list").innerHTML = warningRuns.length ? warningRuns.slice(0, 12).map(run => `
    <div class="list-item">
      <strong>${escapeHtml(shortRun(run))}</strong>
      <span class="subtle mono">${escapeHtml((run.parse_warnings || []).join(", "))}</span>
    </div>
  `).join("") : `<div class="list-item"><span class="subtle">no parse warnings</span></div>`;
}

async function renderRuns() {
  const payload = await api(`/api/runs${qs({
    group: $("#run-group-filter").value,
    agent_id: $("#run-agent-filter").value
  })}`);
  state.runs = payload.runs;
  $("#runs-table").innerHTML = `
    <thead><tr>
      <th>run</th><th>group</th><th>time</th><th>agent</th><th>cases</th><th>literal</th><th>duration</th><th>warnings</th>
    </tr></thead>
    <tbody>
      ${payload.runs.map(run => `
        <tr>
          <td><strong>${escapeHtml(shortRun(run))}</strong><div class="subtle mono">${escapeHtml(run.file_path)}</div></td>
          <td>${escapeHtml(run.group_name)}</td>
          <td>${escapeHtml(run.generated_at || "—")}</td>
          <td>${escapeHtml(run.agent_id || "—")}</td>
          <td>${escapeHtml(run.case_count)}</td>
          <td>${fmtPct(run.literal_pass_rate)}</td>
          <td>${fmtMs(run.avg_duration_ms)}</td>
          <td>${(run.parse_warnings || []).length ? badge((run.parse_warnings || []).length, "warn") : badge("0", "ok")}</td>
        </tr>
      `).join("")}
    </tbody>`;
}

async function renderCases() {
  const payload = await api(`/api/cases${qs({
    origin: $("#case-origin-filter").value,
    family: $("#case-family-filter").value,
    score_mode: $("#case-score-filter").value
  })}`);
  $("#cases-table").innerHTML = `
    <thead><tr>
      <th>case</th><th>origin</th><th>family</th><th>type</th><th>score</th><th>source</th><th>question</th>
    </tr></thead>
    <tbody>
      ${payload.cases.map(c => `
        <tr>
          <td class="mono"><strong>${escapeHtml(c.case_id)}</strong></td>
          <td>${badge(c.origin, c.origin === "external" ? "info" : "ok")}</td>
          <td>${escapeHtml(c.family || "—")}</td>
          <td>${escapeHtml(c.case_type || "—")}</td>
          <td>${escapeHtml(c.score_mode || "—")}</td>
          <td>${escapeHtml(c.source_file || c.expected_source_section || "—")}</td>
          <td>${escapeHtml(c.question || "—")}</td>
        </tr>
      `).join("")}
    </tbody>`;
}

async function renderMatrix() {
  const payload = await api(`/api/matrix${qs({
    origin: $("#matrix-origin-filter").value,
    family: $("#matrix-family-filter").value,
    group: $("#matrix-group-filter").value,
    agent_id: $("#matrix-agent-filter").value
  })}`);
  const table = $("#matrix-table");
  table.innerHTML = `
    <thead>
      <tr>
        <th class="matrix-case">case</th>
        ${payload.runs.map(run => `<th class="run-head" title="${escapeHtml(run.run_id)}">${escapeHtml(shortRun(run))}<div class="subtle">${escapeHtml(run.case_count)} cases</div></th>`).join("")}
      </tr>
    </thead>
    <tbody>
      ${payload.rows.map(row => `
        <tr>
          <td class="matrix-case">
            <strong>${escapeHtml(row.case.case_id)}</strong>
            <div class="subtle">${escapeHtml(row.case.family || row.case.origin)} · ${escapeHtml(row.case.score_mode || "—")}</div>
          </td>
          ${row.cells.map((cell, index) => renderMatrixCell(cell, payload.runs[index], row.case)).join("")}
        </tr>
      `).join("")}
    </tbody>`;
  $$(".cell[data-run]", table).forEach(cell => {
    cell.addEventListener("click", () => openDrilldown(cell.dataset.run, cell.dataset.case));
  });
}

function renderMatrixCell(cell, run, rowCase) {
  if (!cell) return `<td class="cell empty">—</td>`;
  const cls = resultClass(cell);
  const main = cell.literal_hit || cell.source_coverage_label || (cell.completed ? "completed" : "failed");
  const sub = [
    cell.retrieved !== null && cell.retrieved !== undefined ? `docs ${cell.retrieved}` : null,
    cell.score_max !== null && cell.score_max !== undefined ? `score ${Number(cell.score_max).toFixed(3)}` : null,
    cell.rerank_mode || null
  ].filter(Boolean).join(" · ");
  return `
    <td class="cell ${cls}" data-run="${escapeHtml(run.run_id)}" data-case="${escapeHtml(rowCase.case_id)}">
      <div class="cell-main">${escapeHtml(main)}</div>
      <div class="cell-sub">${escapeHtml(sub || "—")}</div>
    </td>`;
}

async function renderCompare() {
  if (!state.compareA && state.runs[0]) state.compareA = state.runs[0].run_id;
  if (!state.compareB && state.runs[1]) state.compareB = state.runs[1].run_id;
  $("#compare-a").value = state.compareA || "";
  $("#compare-b").value = state.compareB || "";
  await renderPresets();
  if (!state.compareA || !state.compareB) return;
  const payload = await api(`/api/compare${qs({run_a: state.compareA, run_b: state.compareB})}`);
  const changed = payload.rows.filter(row => row.delta.coverage_changed || row.delta.literal_ratio !== null).length;
  $("#compare-summary").innerHTML = [
    metric("Run A", shortRun(payload.run_a), `${payload.run_a.case_count} cases · ${fmtPct(payload.run_a.literal_pass_rate)}`),
    metric("Run B", shortRun(payload.run_b), `${payload.run_b.case_count} cases · ${fmtPct(payload.run_b.literal_pass_rate)}`),
    metric("Compared Cases", payload.rows.length, "present in A or B"),
    metric("Changed Rows", changed, "literal ratio or coverage changed")
  ].join("");
  $("#compare-table").innerHTML = `
    <thead><tr>
      <th>case</th><th>A literal</th><th>B literal</th><th>A coverage</th><th>B coverage</th><th>duration delta</th>
    </tr></thead>
    <tbody>
      ${payload.rows.map(row => `
        <tr>
          <td><strong>${escapeHtml(row.case.case_id)}</strong><div class="subtle">${escapeHtml(row.case.family || row.case.origin)}</div></td>
          <td>${escapeHtml(row.a?.literal_hit || "—")}</td>
          <td>${escapeHtml(row.b?.literal_hit || "—")}</td>
          <td>${escapeHtml(row.a?.source_coverage_label || "—")}</td>
          <td>${escapeHtml(row.b?.source_coverage_label || "—")}</td>
          <td>${row.delta.duration_ms === null ? "—" : escapeHtml(`${row.delta.duration_ms > 0 ? "+" : ""}${fmtMs(row.delta.duration_ms)}`)}</td>
        </tr>
      `).join("")}
    </tbody>`;
}

async function renderPresets() {
  const payload = await api("/api/presets");
  $("#preset-row").innerHTML = payload.presets.map(preset => `
    <button class="preset" ${preset.available ? "" : "disabled"} data-a="${escapeHtml(preset.run_a || "")}" data-b="${escapeHtml(preset.run_b || "")}" title="${escapeHtml(preset.reason || "")}">
      ${escapeHtml(preset.name)}
    </button>
  `).join("");
  $$(".preset").forEach(btn => {
    btn.addEventListener("click", () => {
      if (!btn.dataset.a || !btn.dataset.b) return;
      state.compareA = btn.dataset.a;
      state.compareB = btn.dataset.b;
      renderCompare();
    });
  });
}

async function renderInsights() {
  const payload = await api("/api/insights");
  $("#insight-runs-table").innerHTML = `
    <thead><tr><th>run</th><th>completed</th><th>literal</th><th>avg duration</th><th>rerank failures</th><th>profile usage</th></tr></thead>
    <tbody>
      ${payload.runs.slice(0, 30).map(run => `
        <tr>
          <td><strong>${escapeHtml(shortRun(run))}</strong><div class="subtle mono">${escapeHtml(run.group_name)}</div></td>
          <td>${escapeHtml(run.completed_count || 0)}/${escapeHtml(run.result_count || 0)}</td>
          <td>${fmtPct(run.literal_pass_rate)}</td>
          <td>${fmtMs(run.avg_duration_ms)}</td>
          <td>${escapeHtml(run.rerank_failure_count || 0)}</td>
          <td>${escapeHtml(run.profile_usage_count || 0)}</td>
        </tr>
      `).join("")}
    </tbody>`;
  $("#insight-cases-table").innerHTML = `
    <thead><tr><th>case</th><th>origin</th><th>runs</th><th>literal failures</th><th>mismatch</th><th>coverage volatility</th></tr></thead>
    <tbody>
      ${payload.cases.map(c => `
        <tr>
          <td><strong>${escapeHtml(c.case_id)}</strong><div class="subtle">${escapeHtml(c.family || "")}</div></td>
          <td>${escapeHtml(c.origin)}</td>
          <td>${escapeHtml(c.run_count || 0)}</td>
          <td>${escapeHtml(c.literal_failure_count || 0)}</td>
          <td>${escapeHtml(c.literal_mismatch_count || 0)}</td>
          <td>${escapeHtml(c.coverage_volatility || 0)}</td>
        </tr>
      `).join("")}
    </tbody>`;
}

async function openDrilldown(runId, caseId) {
  const payload = await api(`/api/results/${encodeURIComponent(runId)}/${encodeURIComponent(caseId)}`);
  $("#modal-title").textContent = `${caseId} · ${shortRun({run_id: runId})}`;
  $("#modal-subtitle").textContent = `${payload.question || ""}`;
  $("#modal-body").innerHTML = renderDrilldown(payload);
  $("#modal-backdrop").hidden = false;
}

function renderDrilldown(result) {
  const finalDocs = result.documents?.final || [];
  const preDocs = result.documents?.pre_rerank || [];
  return `
    <div class="drill-grid">
      ${metric("literal", result.literal_hit || "—", result.score_mode || "")}
      ${metric("coverage", result.source_coverage_label || "—", result.source_file || "")}
      ${metric("rerank", result.rerank_mode || "—", result.rerank_runtime_model || "")}
      ${metric("profile", result.profile_source || "—", `${(result.selected_profile_hints || []).length} selected hints`)}
    </div>
    <div class="drill-section">
      <h2>Expected Points</h2>
      <ul>${(result.expected_points || []).map(point => `<li>${escapeHtml(point)}</li>`).join("") || "<li>—</li>"}</ul>
      <div class="subtle">missing: ${escapeHtml((result.missing_points || []).join(" · ") || "—")}</div>
    </div>
    <div class="drill-section">
      <h2>Answer</h2>
      <pre>${escapeHtml(result.answer || result.answer_preview || "—")}</pre>
    </div>
    <div class="drill-section">
      <h2>Rewrite / Profile / Salience</h2>
      <div class="table-wrap"><table>
        <tbody>
          <tr><th>rewrite mode</th><td>${escapeHtml(result.query_rewrite_mode || "—")}</td></tr>
          <tr><th>rewrite variants</th><td>${escapeHtml((result.rewrite_variants || []).join(" | ") || "—")}</td></tr>
          <tr><th>profile hints</th><td>${escapeHtml((result.selected_profile_hints || []).join(" | ") || "—")}</td></tr>
          <tr><th>context salience</th><td>${escapeHtml((result.context_salience_cues || []).join(" | ") || "—")} · expansions ${escapeHtml(result.context_salience_expansions ?? "—")}</td></tr>
          <tr><th>source coverage</th><td>source ${escapeHtml(result.source_file_in_final_context ?? "—")} · section ${escapeHtml(result.expected_section_in_final_context ?? "—")} · points ${escapeHtml(result.expected_points_final_hit_count ?? "—")}/${escapeHtml(result.expected_points_final_total ?? "—")}</td></tr>
        </tbody>
      </table></div>
    </div>
    <div class="drill-section">
      <h2>Pre-rerank Documents</h2>
      ${renderDocs(preDocs)}
    </div>
    <div class="drill-section">
      <h2>Final Documents</h2>
      ${renderDocs(finalDocs)}
    </div>`;
}

function renderDocs(docs) {
  if (!docs.length) return `<div class="subtle">no documents parsed</div>`;
  return `<div class="table-wrap"><table>
    <thead><tr><th>rank</th><th>source</th><th>chunk</th><th>scores</th><th>preview</th></tr></thead>
    <tbody>
      ${docs.map(doc => `
        <tr>
          <td>${escapeHtml(doc.rank ?? "—")}</td>
          <td>${escapeHtml(doc.source_path || "—")}</td>
          <td>${escapeHtml(doc.chunk_index ?? "—")}</td>
          <td class="mono">${escapeHtml(Object.entries(doc.scores || {}).map(([k, v]) => `${k}=${v}`).join(" · ") || "—")}</td>
          <td>${escapeHtml(doc.text_excerpt || "—")}</td>
        </tr>
      `).join("")}
    </tbody>
  </table></div>`;
}

function bindEvents() {
  $$(".tab").forEach(btn => btn.addEventListener("click", () => setView(btn.dataset.view)));
  $("#reindex-btn").addEventListener("click", async () => {
    const btn = $("#reindex-btn");
    btn.disabled = true;
    btn.textContent = "Indexing";
    try {
      await api("/api/reindex", {method: "POST"});
      await loadBase();
      renderDashboard();
    } catch (error) {
      $("#warning-list").innerHTML = `<div class="list-item">${escapeHtml(error.message)}</div>`;
    } finally {
      btn.disabled = false;
      btn.textContent = "Reindex";
    }
  });
  ["run-group-filter", "run-agent-filter"].forEach(id => $(`#${id}`).addEventListener("change", renderRuns));
  ["case-origin-filter", "case-family-filter", "case-score-filter"].forEach(id => $(`#${id}`).addEventListener("change", renderCases));
  ["matrix-origin-filter", "matrix-family-filter", "matrix-group-filter", "matrix-agent-filter"].forEach(id => $(`#${id}`).addEventListener("change", renderMatrix));
  $("#compare-a").addEventListener("change", event => { state.compareA = event.target.value; renderCompare(); });
  $("#compare-b").addEventListener("change", event => { state.compareB = event.target.value; renderCompare(); });
  $("#modal-close").addEventListener("click", closeModal);
  $("#modal-backdrop").addEventListener("click", event => {
    if (event.target.id === "modal-backdrop") closeModal();
  });
  document.addEventListener("keydown", event => {
    if (event.key === "Escape") closeModal();
  });
}

function closeModal() {
  $("#modal-backdrop").hidden = true;
}

async function boot() {
  bindEvents();
  try {
    await loadBase();
    renderDashboard();
  } catch (error) {
    $("#health-line").textContent = error.message;
    $("#dashboard-metrics").innerHTML = metric("error", error.message, "API unavailable");
  }
}

boot();
