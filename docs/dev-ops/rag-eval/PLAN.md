# RAG Eval 自动化 — 计划与状态

> 这份文档是 RAG 评测链路工作的 single source of truth。任何接手会话先读这里。
> 最后更新：2026-05-05 22:20 EDT（D7：Step 3 Phase A 收口）

## 目录组织

```
docs/dev-ops/rag-eval/
├── PLAN.md                      ← 本文（single source of truth）
├── rag_eval_runner.py           ← 评测脚本
├── cases.json                   ← 10 条评测用例（脚本与 cases 同目录是 Path(__file__).with_name 硬约定）
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
    └── step3-chunker-v3/         ← chunkSize=250 实测，v3 gate 4/4 ✓ Phase A 收口（May 5 22:17）
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

### Commit 范围（已落地，未 push）

```
05a3f49 (HEAD -> main) feat(rag): Step 3.1-3.5 chunker参数化+chunk metadata+渲染来源+Phase A评测
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

未提交的当前状态（D7 收口后即将一次性提交）：

- `.gitignore`：planning-with-files 三件套本地工作记忆忽略规则
- `ai-agent-boot/src/main/resources/application-dev.yml`：v3 参数 250/109 已落盘
- `docs/dev-ops/rag-eval/PLAN.md`：D7 收口决议
- `docs/dev-ops/rag-eval/results/step3-chunker-v2/rag-eval-result-step3-chunker-v2.md`：v2 评测产物
- `docs/dev-ops/rag-eval/results/step3-chunker-v3/rag-eval-result-step3-chunker-v3.md`：v3 评测产物（May 5 22:17）

## 4. 延期话题（用户上一会话明确说"后续讨论"，不要主动开工）

按下一步时机排序：

1. RAG-09 / RAG-10 paraphrase regression
2. RAGAS 集成（faithfulness / context precision / answer relevance）
3. rerank 模型选型
4. hybrid search RRF（pgvector + PG FTS）
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

1. Read 这份 `PLAN.md` 全文（特别是 §3 D7 = Step 3 Phase A 收口决议）
2. 确认 §3 commit 范围是否已落地（`git log --oneline -5` 看是否有 D7 收口 commit）
3. 当前游标：**Step 3 Phase A 已收口**（v3 决策门 4/4，gate 通过，不触发 Phase B）；下一步等用户在 §4 延期话题或 Step 4 Hybrid Retrieval 之间选
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

## 9. Step 3 — Chunker + Chunk Metadata（in progress，May 4 启动）

> Phase A 实现由 AI 协作辅助完成（候选人逐行 review + 验证 + 跑 verify）；Phase B（条件触发，复杂度更高）保留候选人主笔。授权变更日期：2026-05-05。
> 拍板基线见 §3 D4，本节是执行手册。
> 当前游标（2026-05-05 22:20 EDT）：**Phase A 已收口（D7）**——3.1-3.6 全部完成（v1/v2/v3 三轮调参），决策门 4/4 通过，不触发 3.7 Phase B。Phase A 锁定参数：chunk-size=250 / min-chunk-size-chars=109。

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
