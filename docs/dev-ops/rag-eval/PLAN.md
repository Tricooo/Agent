# RAG Eval 自动化 — 计划与状态

> 这份文档是 RAG 评测链路工作的 single source of truth。任何接手会话先读这里。
> 最后更新：2026-05-08 EDT（D10：Step 4 Hybrid + RRF A/B 归因）

## 目录组织

```
docs/dev-ops/rag-eval/
├── PLAN.md                      ← 本文（single source of truth）
├── rag_eval_runner.py           ← 评测脚本
├── cases.json                   ← 14 条评测用例（脚本与 cases 同目录是 Path(__file__).with_name 硬约定）
└── results/
    ├── _archive/                ← F-fix 之前，旧 schema 无 retrieved/score/empty 三列，不可与 ffix 互比 score
    │   ├── rag-eval-result.md                ← Apr 28，0.65 误杀证据（answer 文本可证）
    │   ├── rag-eval-result-bsymmetric.md     ← auto chain 死路径反证
    │   └── rag-eval-result-t06{0,5}-triangle.md  ← 旧 schema threshold 三角
    ├── ffix/                    ← F-fix 之后，三列俱全，可互比
    │   ├── rag-eval-result-baseline-ffix.md      ← ★ 当前 baseline（10/10，threshold=0.60，May 2）
    │   ├── rag-eval-result-ffix.md               ← F-fix 首跑
    │   ├── rag-eval-result-t055-triangle-ffix.md ← threshold 三角 0.55
    │   └── rag-eval-result-rag05-ffix.md         ← A+B fix RAG-05 单跑验证
    ├── step3-chunker-v1/         ← chunkSize=800 A/A test
    ├── step3-chunker-v2/         ← chunkSize=400 实测，v2 gate 3/4
    ├── step3-chunker-v3/         ← chunkSize=250 实测，v3 gate 4/4 ✓ Phase A 收口（May 5 22:17）
    ├── step3-control-v3/         ← low-newline control-only，RAG-11/12/13/14 通过（May 6）
    ├── step3-full-v3-with-control/ ← full 14 regression，原 10 条不退化 + control 通过（May 6）
    ├── step4-hybrid-rrf/         ← Hybrid + RRF full 14 regression + top-K attribution（May 8）
    └── step4-vector-ablation/    ← 同代码/同数据下 VECTOR 对照组（May 8）
```

> 归档原则：按"评测口径是否一致"分。F-fix 是 schema 硬边界——之前的产物没有 `retrieved/score/empty` 三列，永久不可与之后互比 score。

## 1. 总目标

让 `rag_demo` 这个 agent 的 RAG 检索观测信号（`retrieved_document_count`、`score_range`、`retrieval_empty`、`similarity_threshold`、`context_selected/dropped/truncated`、`context_chars` 等 11 个 `qa_*` 字段）稳定从 backend 通过 SSE `type=retrieval` 事件下发到 Python runner，落入 markdown 评测报告的 `retrieved/score/empty` 三列。

这是 RAG 评测自动化基础设施的第一步，后续叠 threshold triangle、paraphrase regression、RAGAS、rerank、hybrid。

## 2. 当前进展

### 2.1 已完成 / 已验证

- **F-fix 已上线并生效**：在 `FixedAgentExecuteStrategy.java` 加 `emitRetrievalIfPresent()` helper，把 `chatResponse.metadata` 的 `qa_*` 字段拍平成 `type=retrieval` SSE 事件下发。
  - 主流程：`ai-agent-domain/src/main/java/com/tricoq/domain/agent/service/fixed/FixedAgentExecuteStrategy.java:46-87`（改用 `.call().chatResponse()`）
  - Helper：同文件 `:105-126`
  - 共享 DTO：`ai-agent-domain/src/main/java/com/tricoq/domain/agent/model/entity/AutoAgentRetrievalSseEntity.java`
  - 验证证据：`docs/dev-ops/rag-eval/results/ffix/rag-eval-result-ffix.md`
    - RAG-04: retrieved=3, score 0.6305..0.6481, empty=false
    - RAG-06: retrieved=0, empty=true
    - RAG-07: retrieved=3, score 0.6185..0.6645
- 评测 runner score_mode 区分（literal vs manual）已落地。
- F-fix 链路下 threshold=0.60 baseline：`results/ffix/rag-eval-result-ffix.md`（May 1 03:21，三列俱全）
- F-fix 链路下 threshold=0.55 三角：`results/ffix/rag-eval-result-t055-triangle-ffix.md`（May 2 03:02，RAG-04 / 06 / 07 三列俱全，预热后跑通）
- threshold=0.65 沿用 `results/_archive/rag-eval-result.md`（Apr 28，旧 schema 无三列，但答案可证 RAG-04 误杀）
- 旧 schema 残留（不可比，不再补跑）：`results/_archive/rag-eval-result-t060-triangle.md` / `results/_archive/rag-eval-result-t065-triangle.md`（`rag-eval-result-test-triangle.md` 在 commit `a9fbf7e` 中已删除）
- **A+B fix（commit `a9fbf7e`）已上线**：DeepSeek `read-timeout 25s→120s`（A） + `AgentExecutionFacade.error()` 发 SSE `type=error` 帧而非 `emitter.completeWithError(t)`（B），runner 解析 error 帧到 markdown details。
  - A 验证：`results/ffix/rag-eval-result-rag05-ffix.md`（May 2，RAG-05 单跑 duration=26702ms completed=true，之前固定 30s timeout）
  - B 验证：`results/ffix/rag-eval-result-baseline-ffix.md`（May 2 04:14，10 条全跑）的 RAG-04 行触发偶发 `RestClientException`，markdown details 直接显示 `error: RestClientException: ...`，不再 silent connection reset
- **F-fix 链路完整 baseline**：`results/ffix/rag-eval-result-baseline-ffix.md`（May 2 04:14，threshold=0.60，10 条全跑，9/10 happy + 1 偶发）。这是后续 chunking / hybrid / rerank 的对照基线。
- **Step 3 Phase A — 参数化基础设施完成（May 5）**：
  - 3.1 `ai-agent-boot/src/main/java/com/tricoq/config/AiAgentConfig.java` TokenTextSplitter 5 参数化；`ai-agent-boot/src/main/resources/application-dev.yml` 加 chunker section（chunk-size=800 = Spring AI 默认值，基础设施就绪，调参不再需要改 Java）
  - 3.2 `ai-agent-domain/.../service/rag/RagService.java` chunk 级 metadata：chunkIndex / totalChunks / sourcePath（填充）/ parentSection / headingPath（Phase B 留空，schema 一次到位）
  - 3.3 `ai-agent-domain/.../element/RagAnswerAdvisor.java` 渲染端加来源行（buildSourceLine helper，graceful：sourcePath 空时返回 "" 兼容旧 chunk）
  - 3.4 vector store TRUNCATE + 重灌（用户操作，May 5）
  - 3.5 评测 10/10 completed（需预热后跑）：`results/step3-chunker-v1/rag-eval-result-step3-chunker-v1.md`
  - **3.6 Gate 判定：部分达标**（详见 §3 D5）
- **Step 3 Phase A — v2 / v3 全部完成，决策门通过（May 5 22:17）**：
  - v2 产物：`results/step3-chunker-v2/rag-eval-result-step3-chunker-v2.md`
  - v2 结果：RAG-04 max 0.6481→0.6715；RAG-07 max 0.6645→0.6634；gap -0.0164→+0.0081；gate 3/4
  - v3 产物：`results/step3-chunker-v3/rag-eval-result-step3-chunker-v3.md`（May 5 22:17）
  - v3 配置：`application-dev.yml` `chunk-size=250` / `min-chunk-size-chars=109`，用户已 TRUNCATE `vector_store_openai` 并重传 `rag_demo`（chunks v1=4 / v2=7 / v3=10）
  - v3 结果：RAG-04 max **0.7406**（≥0.70 决策门 ✓ 跨过）；RAG-07 max 0.6726；gap **+0.0680**（v2 +0.0081 → v3 扩大 8×）；10/10 completed；empty 仍 RAG-06/08；gate **4/4** → Phase A 收口
  - 详细决策见 §3 D7
- **Step 3 Phase A.5 — low-newline control + full regression 完成（May 6）**：
  - 新增控制文档：`docs/dev-ops/rag-file/control-cn-low-newline.txt` / `control-en-long-paragraph.txt`
  - 新增 case：RAG-11 / RAG-12 / RAG-13 / RAG-14
  - control-only 产物：`results/step3-control-v3/rag-eval-result-step3-control-v3.md`
  - full 14 regression 产物：`results/step3-full-v3-with-control/rag-eval-result-step3-full-v3-with-control.md`
  - 结果：RAG-11/13 正例命中，RAG-12/14 拒答；原 RAG-01..10 不退化
  - 详细决策见 §3 D9
- **Step 4 Hybrid + RRF — 功能闭环完成（May 8）**：
  - 实现：pgvector 向量召回 + PostgreSQL FTS 关键词召回 + 应用层 RRF 合并
  - 配置入口：`AiClientAdvisorDTO.RagAnswer` 新增 `retrievalMode/vectorTopK/keywordTopK/rrfK`
  - 可观测性：合并后 `Document.score` 为 `rrfScore`，metadata 记录 `retrievalSource/vectorRank/vectorScore/keywordRank/keywordScore`
  - full 14 regression 产物：`results/step4-hybrid-rrf/rag-eval-result-step4-hybrid-rrf.md`
  - attribution 产物：`results/step4-hybrid-rrf/rag-eval-result-step4-hybrid-rrf-attribution.md`
  - VECTOR 对照产物：`results/step4-vector-ablation/rag-eval-result-step4-vector-ablation.md`
  - 结果：14/14 completed；RAG-06/RAG-08 仍 empty 拒答；RAG-10 保持既有 literal 表达漂移；A/B 显示当前 Hybrid 没有带来答案指标提升，且 RAG-04 目标 chunk 被 RRF 从向量第 1 降到第 3
  - 详细决策见 §3 D10

### 2.2 关键认知（必读，否则会重复踩坑）

- `rag_demo` 的 `ai_agent.strategy` 是 **`fixedAgentExecuteStrategy`**，**不是** auto chain。
- 不能因为 endpoint 名叫 `/api/v1/agent/auto_agent` 就推断走 `AutoAgentExecuteStrategy`——这是这个项目最大的锚点陷阱。
- C-full + B-symmetric 两轮在 auto chain 上加的 observer 改动（Step1AnalyzeNode / Step4LogExecutionSummaryNode / AbstractExecuteSupport / TextInvocationRequest / SpringAiLlmInvocationGateway）对 `rag_demo` **完全无效**，是死路径。死路径反证见 `results/_archive/rag-eval-result-bsymmetric.md:13-15`（三列全是 `—`）。
- 修 RAG/SSE 相关前必须按 `.claude/rules/agent-runtime-path-proof.md` 的 6 步证据链证明 live path。
- **runner 报告里 `retrieved`/`score`/`empty` 三列 `—` 不等于"无检索"**：F-fix retrieval SSE 在 `FixedAgentExecuteStrategy.execute()` 调 `.call().chatResponse()` 成功返回后才从 `ChatResponseMetadata` 取出来发，所以 LLM 调用失败时（例如 DeepSeek 偶发 `RestClientException`），即使 RAG 检索本身成功（backend log 里有 `RAG检索结果: retrieved=...`），runner 也只能看到三列 `—`。要区分"检索失败"和"生成失败"，对照报告 `error:` 行 / backend `data/log` 里的 `RagAnswerAdvisor` 日志。后续在 LLM 失败时也保住 retrieval 观测需要 advisor before 阶段独立 side-channel（Codex P2 建议）。
- **runner 的 `completed: true` 只表示 SSE 流收到了终端 `type=complete` 帧**，不等于 task 语义完成。AutoAgentExecuteResultEntity 的 summary 事件本身也带 `completed=true`，但那是 task 完成不是流收尾。runner 已绑定到 `event_type == "complete"`（Codex P2 修复后），不再被 summary 误导。

## 3. 已决议（接力会话 May 2 收口）

### D1（已决议）：B 回滚 6 个 auto chain 死路径文件

- 已回滚：`StructuredInvocationRequest` / `TextInvocationRequest` / `AbstractExecuteSupport` / `Step1AnalyzeNode` / `Step4LogExecutionSummaryNode` / `SpringAiLlmInvocationGateway`
- 保留 live path：`FixedAgentExecuteStrategy.java`（F-fix）+ `AutoAgentRetrievalSseEntity.java`（共享 DTO）
- 编译验证：`mvn -T 1C -pl ai-agent-domain,ai-agent-infrastructure -am compile -DskipTests` exit 0
- 理由摘要：6 个文件是 b-symmetric/c-full 误把 RAG observer 挂到 auto chain 时的死代码，留着只会让后续读代码的人误判 RAG metadata 流向；`AutoAgentRetrievalSseEntity` 因为也被 F-fix 用到所以保留。

### D2（已决议）：0.55 已补跑；0.60 / 0.65 不再补跑

| threshold | 文件 | 链路 | 三列状态 | 关键观测 |
|---|---|---|---|---|
| 0.55 | `results/ffix/rag-eval-result-t055-triangle-ffix.md`（May 2） | F-fix | 全 | RAG-04 score 0.6305..0.6481 答 ✓；RAG-06 empty 拒答 ✓；RAG-07 score 0.6185..0.6645 manual 拒答 ✓ |
| 0.60 | `results/ffix/rag-eval-result-ffix.md`（May 1） | F-fix | 全 | 与 0.55 完全一致（同 score 区间，因为 score 全部 > 0.60）|
| 0.65 | `results/_archive/rag-eval-result.md`（Apr 28） | F-fix 之前旧 schema | 无三列 | RAG-04 score≈0.648 < 0.65 → 误杀拒答（answer 文本可证）|

**核心结论**：0.55 vs 0.60 在 RAG-04 / RAG-07 上 score 完全一致，**没有 diagnostic delta**；真正分水岭是 0.65（RAG-04 死亡线）。这印证了 PLAN §1 / 笔记 §4.1.3 的认知 —— 单一静态 threshold 难以同时挡住弱相关并保住深层公式（RAG-04 = 0.648 < RAG-07 = 0.6645，弱相关分数高于正例）。后续优化方向应转向 query rewrite / rerank / answerability，不再继续在 threshold 上拍。

### Hygiene（已决议）

- `.gitignore` 已加：`/data/` / `/ai-agent-boot/data/` / `__pycache__/` / `*.pyc`
- `.env.example` 早已有 `!.env.example` 例外规则（`.gitignore:51`），保持入库
- `application-local.yml` / `application-*.local.yml` 早已忽略

### D3（已决议 May 2）：A+B 修复 RAG-05 30s 失败 + SSE 异常透传

`a9fbf7e` 同时修同一个 RAG-05 失败的两条路径：

- **A**: `application-dev.yml` `spring.ai.openai.http` —— `read-timeout 25s→120s` / `response-timeout 25s→120s` / `connect-timeout 5s→10s`。RAG eval long_context case 在 DeepSeek 25s read-timeout + RetryTemplate 重试 5s = 30s 触发 `SocketTimeoutException`。120s 给慢链路余量。`AiClientApiNode.buildRestClientBuilder()` 已支持 `@Value` 注入，纯 yml 调整不动 Java。
- **B**: `AgentExecutionFacade.ssePort.error()` 不再调 `emitter.completeWithError(t)`（Spring MVC 默认 ExceptionHandler 把 `LinkedHashMap` 写 JSON 错误响应，但 Content-Type 已锁成 `text/event-stream`，触发 `HttpMessageNotWritableException`，客户端只看到 connection reset）。改为往 SSE 流发 `data: {"type":"error","errorClass":"...","message":"..."}\n\n` 帧，`emitter.complete()` 由 `AgentDispatchService.finally` 统一收尾避免重复 complete。
- **配套**: `rag_eval_runner.py.summarize_events` 解析 `type=error` 帧，HTTP 没异常但 SSE 流里有 error 事件时把 `errorClass: message` 写进 markdown 报告 details `error:` 行。

直接验证：

- A: RAG-05 单跑 duration=26702ms completed=true（fix 前固定 30s timeout）
- B: 全量 baseline 重跑时 RAG-04 偶发 `RestClientException: Error while extracting response for type [...] and content type [application/json]`（duration=28.2s，< 120s 不是 read-timeout，是 OpenAI 响应解析失败），markdown details 直接显示 errorClass/message。

### D4（已决议 May 4）：Step 3 chunker + chunk metadata 启动

**拍板基线**（用户 May 4 确认）：

- `ai_client_advisor` 表 `rag_demo` 当前 `topK=4` / `similarityThreshold=0.6`，**Step 3 期间保持不动**（单变量隔离，让 chunker 改动 attribution 干净）
- chunker：Phase A 调 `TokenTextSplitter` 参数 → 决策门不达标再触发 Phase B 自写 `MarkdownTextSplitter`
- metadata 完整集：`chunkIndex` / `totalChunks` / `sourcePath` / `parentSection` / `headingPath`
  - Phase A 阶段 `parentSection` / `headingPath` 字段建好但留空（schema 一次到位，避免 Phase B 再重灌）
- 重灌策略：`TRUNCATE vector_store_openai` + admin 接口重新上传（`rag_demo` 原始文档可复用，已确认）
- baseline 对照：`results/ffix/rag-eval-result-baseline-ffix.md`

**决策门口径（margin-based eval）**：

- pass 率 ≥ 10/10（不退化）
- RAG-04 `max_score` ≥ 0.70（理想 ≥ 0.75）
- 正例 - 弱相关 max score gap 扩大
- empty 率不增加

**口径警示（与 baseline 不可比的列）**：

- ❌ 不可比：chunk count（边界变了）、max_score 绝对值（embedding 输入变了）
- ✅ 可比：completed / hit-rate / score gap / empty 率

**理由摘要**：

`results/ffix/rag-eval-result-ffix.md` / `results/ffix/rag-eval-result-t055-triangle-ffix.md` 显示 RAG-04 max_score=0.6481（正例）< RAG-07 max_score=0.6645（弱相关），是 score inversion 现象，单一 threshold 数学上无解。RAG-07 已在 `before()` empty/manual 拒答路径处理；RAG-04 偏低的根因怀疑是 chunk 边界稀释正例（默认 chunkSize=800 token ≈ 1500 中文字，正例段落可能只占 30% chunk 容量）。Step 3 = 改写入端 chunker + metadata + 渲染端三件套，验证 chunk 边界是否是当前 max_score 偏低的瓶颈。

详细执行步骤见 §9。

### D5（已决议 May 5）：Step 3 Phase A Gate — 部分达标，调参第二轮

**评测产物**：`results/step3-chunker-v1/rag-eval-result-step3-chunker-v1.md`

| 验收口径 | 结果 | 说明 |
|---|---|---|
| pass rate ≥ 10/10 | ✅ 10/10 | +1 vs baseline（RAG-04 baseline=RestClientException flaky → step3 completed=true 3/3 lit） |
| RAG-04 max_score ≥ 0.70 | ❌ 0.6481 | chunkSize=800=默认值未改，scores 与 t055-triangle 完全一致，无改善 |
| 正例-弱相关 gap 扩大 | ❌ 仍倒挂 | RAG-07 max=0.6645 > RAG-04 max=0.6481，delta=-0.0164，与 baseline 同 |
| empty 率不增加 | ✅ 稳定 | RAG-06/RAG-08 empty=true 同 baseline |
| RAG-10 regression | ⚠️ 2/5 vs 5/5 | 同 chunk 内容，LLM variability（missed: 正常范围/警告范围/危险范围） |

**根因**：Step 3.1 做了 yml 参数化基础设施，但 chunk-size=800 是 Spring AI 1.0.3 默认值，重灌后 chunk 结构与 baseline 完全相同，scoring 无改善（scores 与 t055-triangle 完全一致印证此点）。

**历史下一步（Phase A 调参第二轮，D6 已完成并推进到 v3）**：
1. 把 `application-dev.yml` `chunker.chunk-size: 800 → 400`
2. TRUNCATE `vector_store_openai` + 重传 rag_demo 原始文档
3. 预热后跑 `rag_eval_runner.py --session-prefix step3-chunker-v2`
4. 对比 RAG-04 max_score 是否提升 + RAG-10 regression 是否 LLM variability
- 预期：同内容切成更小 chunk，predict_linear 段落占比提升 → embedding score 应提升
- 如 RAG-04 仍 ≤ 0.65 → 触发 Phase B（MarkdownTextSplitter 自写）

### D6（已确认 May 5）：v2 完成，v3 已准备完成，待跑评测

**v2 评测产物**：`results/step3-chunker-v2/rag-eval-result-step3-chunker-v2.md`（May 5 05:59）

| 验收口径 | v1 | v2 | 结论 |
|---|---:|---:|---|
| pass rate | 10/10 | 10/10 | ✅ 不退化 |
| RAG-04 max_score | 0.6481 | **0.6715** | ❌ 仍低于 0.70，差 0.0285 |
| RAG-07 max_score（弱相关） | 0.6645 | **0.6634** | ✅ 未上升 |
| score gap (RAG-04 − RAG-07) | -0.0164 | **+0.0081** | ✅ 倒挂已翻正 |
| empty 率 | 稳定 | 稳定 | ✅ 不退化 |

**v3 准备状态（已 supersede by D7）**：

1. 已选择 D5 候选 A：`chunk-size 400 → 250`，`min-chunk-size-chars 175 → 109`
2. `ai-agent-boot/src/main/resources/application-dev.yml` 已落盘为 250/109
3. 用户确认已完成 `vector_store_openai` TRUNCATE + admin 重传 `rag_demo`（v3 chunks=10，v1=4 / v2=7）

→ v3 评测已于 May 5 22:17 跑完，决策与收口见 D7。

### D7（已决议 May 5 22:20）：Step 3 Phase A 收口 — v3 决策门 4/4 通过，不触发 Phase B

**评测产物**：`results/step3-chunker-v3/rag-eval-result-step3-chunker-v3.md`（May 5 22:17）

**chunks 数量演化**（v1/v2/v3 同源原始文档，仅 chunker 参数变化）：v1 chunk-size=800 → 4 / v2 400 → 7 / v3 250 → 10

| 验收口径 | v1 | v2 | v3 | 决策门 | v3 |
|---|---:|---:|---:|---|:---:|
| pass rate | 10/10 | 10/10 | **10/10** | ≥10/10 | ✅ |
| RAG-04 max_score | 0.6481 | 0.6715 | **0.7406** | ≥0.70 | ✅（跨过 0.0406 安全余量） |
| RAG-04 min_score | 0.6305 | 0.6201 | **0.6481** | — | ✅（整段 chunk 质量提升）|
| RAG-07 max_score（弱相关） | 0.6645 | 0.6634 | **0.6726** | 不抬升 | 🟡（+0.0092 vs v2，仍 < RAG-04 max）|
| score gap (RAG-04 − RAG-07) | -0.0164 | +0.0081 | **+0.0680** | 翻正 | ✅（magnitude 8×）|
| empty 率（RAG-06/08） | 2 | 2 | **2** | 不退化 | ✅ |
| RAG-10 literal_hit | 2/5 | 5/5 | **2/5** | 不退化 | 🟡（LLM variability，非 chunker 归因）|

**核心结论**：

1. **决策门 4 个核心 KPI 全部通过**，触发 Phase B 阈值（RAG-04 max ≤ 0.65）反向不命中——**不需要自写 `MarkdownTextSplitter`**
2. **chunker 边界稀释假设三轮证实**：chunk-size 800/400/250 对应 chunks 4/7/10，正例（RAG-04）max +0.0925，弱相关（RAG-07）max +0.0081，**非对称响应**
3. **score gap 从 -0.0164 翻正到 +0.0680**：8 倍 magnitude 是结构性证据，不是 noise
4. **🟡 RAG-10 literal_hit 退化**（缺"正常范围 / 警告范围 / 危险范围"）：v1 也 2/5、v2 是 5/5，同 chunker 不同结果证明是 LLM 输出多样性，非 chunker 归因；后续可在延期话题 1（paraphrase regression）跟进
5. **🟡 RAG-07 max 略升**：弱相关 chunk 上限维持在 ~0.67，gap 翻正且扩大本身已说明分隔度提升

**Phase A 锁定参数**（生产配置）：

```yaml
spring.ai.rag.chunker:
  chunk-size: 250
  min-chunk-size-chars: 109
  min-chunk-length-to-embed: 5
  max-num-chunks: 10000
  keep-separator: true
```

**下一步选项**（等用户触发）：

- 进入 Step 4 Hybrid Retrieval（PG FTS + pgvector + 应用层 RRF）
- 或先休 Phase A，跑延期话题（RAGAS / paraphrase regression / rerank 等）

**不再做**：

- Phase B 自写 `MarkdownTextSplitter`（决策门通过反向不命中）
- 在 Step 4 之前继续调 chunker 参数（边际收益已显著下降，超出 Phase A 范围）

### D8（已决议 2026-05-06）：Phase A.5 Generalizability Probe — Step 4 启动前补 control case

**触发原因**：D7 决策门 4/4 通过经 Codex review 发现 generalizability 缺口——v3 边界回退几乎全靠 `\n` 命中，而 `\n` 高密度来自 rag_demo 当前 2 份 markdown 文档的结构副作用（list / code fence / heading 强制每行换行）。`TokenTextSplitter.java:105-107` 仅识别 `.?!\n` 4 个 ASCII 字符的 lastIndexOf 链，对纯中文长文（全角 `。？！` + 少换行）会退化为 token 硬切。Phase A 决策门的有效声明范围严格地说是"在 markdown 类文档上达到决策门"，不是"通用 chunker 改进"。

**Codex review 暴露的关键校正**（2026-05-06，详见会话记录）：

1. **Hybrid 不独立于 chunk 边界**：PG FTS 和 pgvector 检索同一批 chunk，hybrid 在坏 chunk 上做 ablation 无法分离收益来源——A 必须先于 Step 4，不是策略选择，是必要前置
2. **score 提升 ≠ 目标 chunk 命中**：v3 RAG-04 max=0.7406 是 top-K max similarity，LLM 可能从邻近/干扰 chunk 拼答案；evaluator 需要补 chunk-level attribution metric
3. **C 优先于 B**：当前 root cause 是 CJK 标点 + 低换行密度，C（自写 splitter 加中文标点回退）直接对应；B（`MarkdownDocumentReader`）解决的是 markdown 结构维度，是另一支
4. **决策门分两层**：Step 3 attribution gate（当前 Phase A.5 收口）+ Promotion gate（远期声称"通用 chunker"前要求覆盖 markdown + low-newline plain text）

**A0 control case 设计**（接受 Codex 4 case + 2 文档方案）：

| 文档 | 规格 | 用途 |
|---|---|---|
| `control-cn-low-newline.txt` | 2500-4000 中文字 / 2-3 段超长（每段 800-1200 字） / 全角 `。？！；：` / 少换行 / 埋深层正例 + 干扰项 | CJK 句界回退场景验证 |
| `control-en-long-paragraph.txt` | 1500-2500 词 / 少换行 / 英文 `.` | 验证英文 `.` 回退路径未被中文修复破坏 |

| Case | 类型 | 期望 |
|---|---|---|
| RAG-11 | 中文 literal 深层正例 | 命中 2-3 个独特词 |
| RAG-12 | 中文 manual 弱相关 / 干扰项 | 拒答或明确"未覆盖" |
| RAG-13 | 英文 literal 深层正例 | 命中独特词 |
| RAG-14 | 英文 manual 弱相关 | 拒答 |

**PDF 不加**：引入 Tika/PdfBox 提取质量 + 分页 + 页眉页脚噪声，不再是 chunker control，应在后续 reader matrix 阶段做。

**A1 ingest 诊断脚本**（离线工具）：

- 每个 control 文档的 chunk 数 / 目标锚点在哪个 chunk / 目标句是否被切断 / 相邻 chunk preview
- SQL：`SELECT id, content, metadata FROM vector_store_openai WHERE metadata->>'sourcePath' = ?`
- 若后续实现，输出建议放：`docs/dev-ops/rag-eval/results/phase-a5-control/ingest-diagnostic-<timestamp>.md`

**新增 attribution metric `target_chunk_intactness`**：目标句是否完整在单个 chunk 内（基于 control 文档埋点 + ingest 诊断输出），让 Codex 主张"score 提升 ≠ 目标 chunk 命中"的盲点可量化进决策门。

**Step 3 attribution gate（Phase A.5 收口口径）**：

- 原 10 case 不退化（pass 10/10 + 决策门 4/4 维持）
- control-cn 正例可召回（RAG-11 retrieved=true + score ≥ baseline 范围）
- control-cn 弱相关不误答（RAG-12 不输出捏造内容）
- control-en 不退化（RAG-13/14 行为对照英文 `.` 回退路径）
- `target_chunk_intactness` ≥ 阈值（具体阈值 A0 跑完后定）

**Promotion gate（远期，不在当前 sprint）**：

- 要声称"通用 chunker 改进"前覆盖 markdown + low-newline plain text 至少两类形态
- 要进 production 再加 PDF / HTML 控制组

**A → C → Step 4 决策路径**：

```
A0 (半天)        : 补 control 文档 + RAG-11/12/13/14 case + ingest 诊断脚本
A1 (1-2h)        : 跑 v3 chunker 评测 + ingest 诊断 + 算 target_chunk_intactness
gate decision    : Step 3 attribution gate 是否通过
  ├─ ✅ 通过     → 进 Step 4 Hybrid + RRF（3-5 day）
  └─ ❌ 失败     → C: CjkAwareTokenTextSplitter (半天) → 重跑 A1 → 再 Step 4
```

**C 实现路径**（仅在 A 失败时触发）：

- `CjkAwareTokenTextSplitter extends TextSplitter`（**不是 extends TokenTextSplitter**——1.0.3 sources 显示 `doSplit` protected 但 encode/decode helpers + 5 字段全 private，继承会复制状态）
- `TextSplitter` 本身 implements `DocumentTransformer`，是 idiomatic Spring AI ETL pattern：Reader → Transformer → VectorStore
- `@ConfigurationProperties` 替代当前 5 个 `@Value`（Spring 风格 + 类型安全）
- 强边界：`\n\n`, `\n`, `.?!。？！`
- 弱边界：`;；:` 可选
- **`，`（中文逗号）不进强边界**——长中文句逗号多，加了会过碎

**待 verify（不影响当前决策）**：

Spring AI current API（1.1.x / 2.0.0-m3）是否引入带 `punctuationMarks` 参数的 `TokenTextSplitter` 构造器（Codex 提到 doc URL，1.0.3 [源码验证] 确认无）。验证后若属实，未来 BOM 升级可能不需自写 splitter——影响 C 的 ROI 评估。

**不再做（D8 范围）**：

- 推翻 D7 Phase A 锁定参数（`chunk-size=250 / min-chunk-size-chars=109` 在 markdown 形态上仍是有效结论）
- 把 Step 4 Hybrid 排到 A 前面（hybrid 在坏 chunk 上做 ablation 归因混乱，A 必须先做）
- 在 A 阶段加 PDF / HTML 控制组（属 reader matrix 维度，不是 chunker control）

**证据锚点**：

- `TokenTextSplitter.java:105-107`（spring-ai-commons-1.0.3-sources.jar 解压）：仅 `.?!\n` 4 字符 lastIndexOf
- 项目 Reader 现状：`RagService.java:51` 用 `TikaDocumentReader`，未引入 `spring-ai-markdown-document-reader`
- v3 评测产物：`docs/dev-ops/rag-eval/results/step3-chunker-v3/rag-eval-result-step3-chunker-v3.md`
- rag_demo 文档清单：`docs/dev-ops/rag-file/{ai-agent-prompt-optimization,grafana-mcp-tools-guide}.md`
- 文档形态实测：2 份 markdown 中文，每 ~31 / ~16 字符 1 个 `\n`（UTF-8 字符口径，非 byte），中文标点不识别 + 英文 `.` 稀疏

### D9（已验证 2026-05-06）：Phase A.5 通过，Step 3 不继续调 chunker，下一候选 Step 4 Hybrid

**新增产物**（commit `2877cbc`）：

- `docs/dev-ops/rag-file/control-cn-low-newline.txt`
- `docs/dev-ops/rag-file/control-en-long-paragraph.txt`
- `docs/dev-ops/rag-eval/cases.json` 新增 RAG-11/12/13/14
- `docs/dev-ops/rag-eval/results/step3-control-v3/rag-eval-result-step3-control-v3.md`
- `docs/dev-ops/rag-eval/results/step3-full-v3-with-control/rag-eval-result-step3-full-v3-with-control.md`

**control-only 结果**（filterExpression = `knowledge in ['control-cn-low-newline', 'control-en-long-paragraph']`）：

| case | 类型 | retrieved | score range | 判分 |
|---|---|---:|---|---|
| RAG-11 | 中文低换行正例 | 4 | 0.7175..0.7763 | literal 2/2 |
| RAG-12 | 中文弱相关 / 未覆盖问题 | 4 | 0.6475..0.7169 | manual 拒答 |
| RAG-13 | 英文长段落正例 | 4 | 0.7632..0.7906 | literal 2/2 |
| RAG-14 | 英文弱相关 / 未覆盖问题 | 4 | 0.7432..0.7799 | manual 拒答 |

**full 14 regression 结果**（filterExpression = `knowledge in ['grafana-mcp-tools-guide', 'control-cn-low-newline', 'control-en-long-paragraph']`）：

- 原 RAG-01..10 行为不退化；RAG-06 / RAG-08 仍 empty 拒答
- RAG-10 仍是 literal 表达漂移 2/5（缺正常/警告/危险范围），与 v1/v3 既有现象一致，不归因于 control 文档
- 新 RAG-11 / RAG-13 正例命中，RAG-12 / RAG-14 拒答

**RAG-14 定性**：

RAG-14 是 negative-evidence answerability case（负证据型拒答）。英文控制组文档明确写了没有指定 cloud provider、该问题不能从文档回答；高分表示召回到“没有答案”的相关证据，不是弱相关误召回后靠模型硬压住。它能验证 prompt + 模型在显式负证据下的拒答能力，但不能替代“纯缺失答案、无负证据”的拒答评测。

**决策**：

1. 保留 v3 参数：`chunk-size=250 / min-chunk-size-chars=109`
2. 不继续调 chunker，不触发自写 CJK/Markdown splitter
3. 当前结论边界收窄为：Phase A 参数在 markdown + 低换行中英文纯文本控制组上未发现阻断性退化，足够进入下一工程维度；不宣称通用 chunker
4. 下一候选进入 Step 4 Hybrid + RRF。理由是补单路向量召回的词面通道（PG FTS），不是为了修 RAG-14

**遗留观测缺口**：

当前 runner report 不展开 top-K chunk 的 `sourcePath/chunkIndex/score/preview`，只能看到 retrieved / score range / empty / context chars。后续若要把 attribution gate 做严，应把 top-K chunk attribution 写入 markdown 报告。

### D10（已验证 2026-05-08）：Step 4 Hybrid + RRF 功能完成，但 A/B 未证明收益，下一步先做 Hybrid calibration

**新增提交**：

- `aee5a75 feat: add hybrid RAG retrieval`
- `f65c365 feat: expose hybrid retrieval metadata`

**实现范围**：

- `AiClientAdvisorDTO.RagAnswer` 增加 `retrievalMode/vectorTopK/keywordTopK/rrfK`
- `RetrievalOptionsVO` 集中封装 fallback：blank/unknown mode → VECTOR，topK/rrfK <= 0 走默认
- `RagAnswerAdvisor` HYBRID 分支：
  - vector side：pgvector `similaritySearch`，topK 使用 `effectiveVectorTopK`
  - keyword side：PostgreSQL FTS，`websearch_to_tsquery('simple', ?)` + `ts_rank_cd(...)`
  - filter：Spring AI `Filter.Expression` 经 `PgVectorFilterExpressionConverter` 转 JSONPath，并用 `metadata::jsonb @@ CAST(? AS jsonpath)` 参数绑定
  - merge：应用层 RRF，`1 / (rrfK + rank)`，默认 `rrfK=60`
- 可观测性：
  - 合并后 `Document.score` 改为 `rrfScore`
  - metadata 写入 `retrievalMode/retrievalSource/rrfScore/rrfK/vectorRank/vectorScore/keywordRank/keywordScore`

**当前运行配置**（`rag_advisor_grafana`）：

```json
{
  "topK": 4,
  "similarityThreshold": 0.6,
  "filterExpression": "knowledge in ['grafana-mcp-tools-guide', 'control-cn-low-newline', 'control-en-long-paragraph']",
  "retrievalMode": "HYBRID",
  "vectorTopK": 4,
  "keywordTopK": 6,
  "rrfK": 60
}
```

**验证产物**：

- `docs/dev-ops/rag-eval/results/step4-hybrid-rrf/rag-eval-result-step4-hybrid-rrf.md`
- `docs/dev-ops/rag-eval/results/step4-hybrid-rrf/rag-eval-result-step4-hybrid-rrf-attribution.md`
- `docs/dev-ops/rag-eval/results/step4-vector-ablation/rag-eval-result-step4-vector-ablation.md`

| 口径 | 结果 |
|---|---|
| full 14 completed | 14/14 |
| RAG-06 / RAG-08 empty 拒答 | 维持 |
| RAG-02 / RAG-03 / RAG-05 | literal 全命中 |
| RAG-11 / RAG-13 control 正例 | literal 全命中 |
| RAG-12 / RAG-14 control 弱相关 | 拒答 / 负证据回答维持 |
| RAG-04 | 语义正确；attribution 显示目标 chunk `grafana-mcp-tools-guide.md#5` 在 VECTOR 是第 1，HYBRID/RRF 后降到第 3 |
| RAG-10 | 仍为既有 literal 表达漂移：缺 `正常范围 / 警告范围 / 危险范围`，不归因于 Hybrid |

**A/B attribution 关键发现**：

| case | VECTOR | HYBRID + RRF | 判断 |
|---|---|---|---|
| RAG-02 | `chunk=1`（参数说明）排第 1 | `chunk=3` 因 keywordRank=1 排第 1，`chunk=1` 降第 2 | keyword 通道参与排序，但未证明比纯向量更好 |
| RAG-04 | 目标 `chunk=5` 排第 1，vectorScore=0.7406 | 目标 `chunk=5` 只来自 VECTOR，排第 3；`chunk=4/1` 因命中 `PromQL` 词面被 RRF 提到前 2 | 当前 keyword query 对“PromQL”这类泛词过敏，产生负向排序干扰 |
| RAG-07 | top4 全是 grafana 文档 | 引入 1 条 `KEYWORD` only 的 `control-en-long-paragraph.txt#4` 噪声 | keyword 分支在弱相关问题上有跨文档噪声风险 |
| RAG-13 | 英文控制组 top4 都正确 | top4 都是 `VECTOR_KEYWORD`，顺序小幅调整 | 英文词面问题上 Hybrid 确实参与排序，但答案指标未新增收益 |

**解释边界**：

1. Step 4 后 markdown 报告里的 `score` 已经是 RRF score，不再是 Step 3 的向量相似度 score；不能直接拿 `0.03` 和 Step 3 的 `0.74` 比。
2. `rag_eval_runner.py` 已展开 top-K doc attribution；判断 Hybrid 贡献要看每条 document metadata 的 `retrievalSource/vectorRank/vectorScore/keywordRank/keywordScore`。
3. 当前 14 条用例已经证明 Hybrid **可用且大体不退化**，但没有证明它比纯向量更优。
4. 当前主要问题不是 RRF 公式本身，而是 keyword query 构造过宽：`PromQL`、`Grafana`、`MCP`、`query` 这类泛词容易把词面相关但语义不够聚焦的 chunk 提前。
5. Hybrid + RRF 解决的是“单路向量召回缺少词面通道”的问题；要证明它有效，需要更聚焦的 lexical probe 或更严格的 keyword gating。

**决策**：

1. Step 4 功能闭环通过，HYBRID 配置已恢复并保留，但不能在面试里宣称“明显提升了效果”。
2. 下一步不直接进入 Step 5 Reranker；先做 Step 4.1 Hybrid calibration。
3. Step 4.1 最小目标：
   - 给 `buildKeywordQuery` 加 stopword / generic token 过滤，优先保留技术标识符（如 `query_prometheus`、`node_filesystem_avail_bytes`、`ARCHIVE-CN-7319`、`silver-river-42`）
   - 或增加 keyword 分支启用条件：只有 query 中出现足够强的技术 token 时才走 PG FTS
   - 重跑 VECTOR vs HYBRID attribution，要求 RAG-04 目标 chunk 不再被降序，RAG-07 不再引入跨文档 keyword-only 噪声
4. Step 4.1 通过后再进入 Step 5 Reranker：目标是对已召回 top-K 做二阶段排序，提升最终上下文顺序稳定性。

### Commit 范围（已落地）

```
f65c365 feat: expose hybrid retrieval metadata
aee5a75 feat: add hybrid RAG retrieval
2877cbc test(rag): add low-newline control eval cases
ec0e4fa feat(rag): Step 3 Phase A 收口 - chunker v3 (250/109) 决策门 4/4 通过
05a3f49 feat(rag): Step 3.1-3.5 chunker参数化+chunk metadata+渲染来源+Phase A评测
e4dea8b docs(rag-eval): 补齐 944c006 漏 stage 的 PLAN.md 目录索引
944c006 docs(rag-eval): reorganize results into _archive/ and ffix/ subdirs
f5b33a8 fix: Codex P2 - runner completed 语义 + 三列 — 含义文档化
5ace3bd docs: PLAN.md 回写 D3 决议（A+B 修复 RAG-05）+ commit 范围归档
3093eb6 docs: 修正 baseline-ffix.md 为 a9fbf7e fix 后真实重跑数据
5a92e60 docs: RAG-05 单跑验证证据归档（A read-timeout 修复直接证据）
a9fbf7e fix: SSE 异常透传 + DeepSeek read-timeout 25s→120s
f78cde1 chore: gitignore 增量 - 本地日志目录 + Python 缓存
fc49eb4 docs: RAG eval 接力计划 + 历史评测产物归档
6da727e feature: RAG eval - F-fix 链路 SSE 下发 type=retrieval 事件
```

## 4. 延期话题（用户上一会话明确说"后续讨论"，不要主动开工）

按下一步时机排序：

1. RAG-09 / RAG-10 paraphrase regression
2. RAGAS 集成（faithfulness / context precision / answer relevance）
3. Step 4.1 Hybrid calibration（keyword query gating / stopword / lexical probe）
4. rerank 模型选型（Step 4.1 通过后下一候选）
5. embedding 模型迁移
6. Codex 对全部 8 个文件改动的 final review

**触发原则**：等用户点哪个就做哪个，不批量推进。

## 5. 关键约束 / 隐藏陷阱

- 业务代码 / 对外文档不使用 emoji（CLI 提示文案例外）
- Java 符号检索必须 Serena 优先（`mcp__serena__find_symbol` / `find_referencing_symbols` / `get_symbols_overview`），不要用 rg 替代——PreToolUse hook 会硬拦 Grep 纯 Java 符号
- rg / grep 只用于 yml / xml / sql / md / log / SSE 字面量
- `LlmInvocationExecutor` 的 timeout 异常是不可重试（commit `32aec6d` 约定），改要先判定 memory 污染 / 重复请求风险
- Maven 走阿里云 Nexus（`pom.xml:20-32`），断网 / VPN 注意
- 项目级 `.claude/` 与根 `CLAUDE.md` 在 `.gitignore`（`.gitignore:40,43`），无法 commit 到主仓——本地或外部 dotfiles repo
- Application 默认端口 `8099`，启动：`mvn -pl ai-agent-boot spring-boot:run`
- MySQL 容器名 `mysql`，root/123456，库 `ai-agent-station-study`
- 业务 Controller：`AiAgentController`，POST `/api/v1/agent/auto_agent`

## 6. 证据锚点（验证用，不要凭记忆）

- F-fix 代码：`ai-agent-domain/src/main/java/com/tricoq/domain/agent/service/fixed/FixedAgentExecuteStrategy.java:46-87, 105-126`
- F-fix 验证：`docs/dev-ops/rag-eval/results/ffix/rag-eval-result-ffix.md`
- 死路径反证：`docs/dev-ops/rag-eval/results/_archive/rag-eval-result-bsymmetric.md:13-15`
- 排障纪律：`.claude/rules/agent-runtime-path-proof.md`
- 项目记忆：`CLAUDE.md`（项目根，gitignored）
- 全局红线：`~/.claude/CLAUDE.md`

## 7. 接手后第一步清单

下一会话接手时：

1. Read 这份 `PLAN.md` 全文（特别是 §3 D9/D10：Phase A.5 实测收口、Hybrid + RRF A/B 归因）
2. 确认 §3 commit 范围是否已落地（`git log --oneline -5` 看 `aee5a75` 和 `f65c365`）
3. 当前游标：**Step 4 Hybrid + RRF 功能完成，但 A/B 未证明收益**。HYBRID 配置保留；下一候选 Step 4.1 Hybrid calibration。注意 Step 4 后报告里的 `score` 是 RRF score，不再是向量相似度 score。
4. 如果改 yml / 重启 backend / 重灌向量库后再跑 eval：必须先预热 `POST http://localhost:8099/api/v1/agent/armory_agent` body `{"agentId":"rag_demo"}`（未预热直接打 auto_agent 会 HTTP 500，duration ~5ms，看似 endpoint 死了）
5. **不要**自行重跑 v1/v2/v3 中任何一轮——Phase A 已锁定参数 250/109，重跑只会消耗 LLM 配额且 score 必然飘动（embedding 不变 score 应稳定，LLM 输出会因 sampling 飘）

**禁止**：
- 直接重做 F-fix（已验证生效）
- 在 auto chain 上加 RAG observer（死路径已证）
- 凭 endpoint 名字推断 strategy
- 批量推进 §4 延期话题
- push 任何 commit
- 改 `~/.claude/` 任何东西（用户已搁置配置优化分支）
- 在没预热的情况下跑评测 runner（结果会全是 completed=false / answer 空 / 三列 `—`，看起来像 backend bug）

## 8. 这份 PLAN 自身的维护

- 任何会话推进了进展，**必须**回写到 §2 进展 / §3 决策项 / §4 延期话题
- 不要重写 §1 总目标和 §5 约束，除非确实变化
- 同时更新顶部"最后更新"日期

## 9. Step 3 — Chunker + Chunk Metadata（completed，May 4 启动，May 6 Phase A.5 收口）

> Phase A 实现由 AI 协作辅助完成（候选人逐行 review + 验证 + 跑 verify）；Phase B（条件触发，复杂度更高）保留候选人主笔。授权变更日期：2026-05-05。
> 拍板基线见 §3 D4，本节是执行手册。
> 当前游标（2026-05-06 EDT）：**Phase A.5 已收口（D9）**——3.1-3.6 完成（v1/v2/v3 三轮调参），3.7 Phase B 不触发，D8 generalizability 缺口已由 D9 低换行控制组验证。Phase A 锁定参数：chunk-size=250 / min-chunk-size-chars=109。

### 9.1 改动落点表

| 落点 | 文件:行 | 当前状态 | Step 3 改动 |
|---|---|---|---|
| A. chunker @Bean | `ai-agent-boot/src/main/java/com/tricoq/config/AiAgentConfig.java:62-65` | `new TokenTextSplitter()` 全部默认参数 | 改为带参构造，5 个参数从 `application.yml` 注入 |
| B. 写入端 metadata | `ai-agent-domain/src/main/java/com/tricoq/domain/agent/service/rag/RagService.java:50-58` | metadata 仅 3 字段（knowledge / rag_id / file_name），全部文件级 | 增 chunk 级字段（chunkIndex / totalChunks / sourcePath；parentSection / headingPath 字段建好留空） |
| C. 渲染端 | `ai-agent-domain/src/main/java/com/tricoq/domain/agent/service/armory/node/factory/element/RagAnswerAdvisor.java:233-275`（`renderDocumentContext`）| 仅渲染 `[i+1] + text`，metadata 完全不进 prompt | 在 `[i+1]` 后追加来源行（如 `(来自: foo.md / 章节: Sec 1.2)`），让 LLM 可引用溯源 |
| D. 配置入口（备用） | `ai-agent-domain/src/main/java/com/tricoq/domain/agent/model/enums/AiClientAdvisorTypeEnumVO.java:51-55` | 配置驱动（topK / threshold 从 DB `ai_client_advisor` 表 → DTO 流入）| **本阶段不动**，保持 topK=4 / threshold=0.6 |

### 9.2 执行步骤（每步独立可回滚）

1. **3.1 TokenTextSplitter 参数化** — 改 A 落点；`application-dev.yml` 加 chunker section（chunkSize / minChunkSizeChars / minChunkLengthToEmbed / maxNumChunks / keepSeparator）
2. **3.2 写入端 chunk 级 metadata 扩展** — 改 B 落点；jsonb 自由扩展无 schema cost
3. **3.3 渲染端展示 metadata** — 改 C 落点；让 LLM 看到 chunk 来源
4. **3.4 vector store 重灌** — `TRUNCATE vector_store_openai` + admin 接口重新上传 `rag_demo` 原始文档
5. **3.5 跑 baseline 评测** — `python rag_eval_runner.py`，10 条 case
6. **3.6 决策门** — 看 §3 D4 验收口径
   - **结果（D7）**：v1 部分达标（gate 1/4） → v2 调参 chunkSize 800→400（gate 3/4，gap 翻正） → v3 再调 400→250（gate 4/4，RAG-04 max=0.7406 跨过 0.70 决策门，gap +0.0680 扩大 8×）
   - **Phase A 收口（D7）**：锁定参数 chunk-size=250 / min-chunk-size-chars=109，不触发 3.7 Phase B
7. **3.7 [Phase B 条件触发] MarkdownTextSplitter 自写**（**不触发**：D7 决议反向不命中 RAG-04 max ≤ 0.65 阈值）— `implements org.springframework.ai.transformer.splitter.TextSplitter`，按 file extension 路由（`.md` → MarkdownTextSplitter / 其它 → TokenTextSplitter 兜底）

### 9.3 Spring AI 1.0.3 splitter 现状（context7 已验证）

- 标准库**仅 `TokenTextSplitter`**，无 markdown-aware / recursive char / html splitter
- `TokenTextSplitter` 5 参数默认值：`(chunkSize=800, minChunkSizeChars=350, minChunkLengthToEmbed=5, maxNumChunks=10000, keepSeparator=true)`
- encoding 固定 CL100K_BASE（OpenAI tiktoken 系，1 token ≈ 1.5~2 中文字符）
- 自写需 implements `org.springframework.ai.transformer.splitter.TextSplitter`（SPI 简单，单方法 `splitText(String): List<String>`）

### 9.4 上下文恢复 hook（防 /compact 后丢失）

/compact 后回到此处，按以下顺序读：

1. **§3 D4** — 拍板基线（topK / threshold / chunker / metadata / 重灌 / baseline）
2. **§9.1** — 落点表（精确到 file:line）
3. **§9.2** — 步骤表（当前进展看哪一步是 in_progress）
4. **§9.3** — Spring AI 1.0.3 splitter 现状
5. **baseline / threshold 文件** — `results/ffix/rag-eval-result-baseline-ffix.md`（10 条全跑）+ `results/ffix/rag-eval-result-t055-triangle-ffix.md`（RAG-04 max=0.6481）
6. **生产 live path proof** — `RagAnswerAdvisor` 在 `ai-agent-domain/.../element/`（不是 boot/test 下的同名 spike），`fixedAgentExecuteStrategy` 是 rag_demo 的 strategy

### 9.5 面试故事链（写笔记时参考）

- "默认 TokenTextSplitter 800 token 切，发现 RAG-04 max_score 偏低（0.6481）"
- "假设：chunk 太大稀释正例 token 比例"
- "Phase A：调 chunkSize 验证假设 + 加 chunk 级 metadata（溯源能力）"
- "Phase A 不达标？→ Phase B：自写 MarkdownTextSplitter，按 heading 切，加 headingPath"
- "evaluation 驱动迭代：margin-based 看 score gap，不看绝对值"
