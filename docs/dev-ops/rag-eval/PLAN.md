# RAG Eval 自动化 — 计划与状态

> 这份文档是 RAG 评测链路工作的 single source of truth。任何接手会话先读这里。
> 最后更新：2026-05-02

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
    └── ffix/                    ← F-fix 之后，三列俱全，可互比
        ├── rag-eval-result-baseline-ffix.md      ← ★ 当前 baseline（10/10，threshold=0.60，May 2）
        ├── rag-eval-result-ffix.md               ← F-fix 首跑
        ├── rag-eval-result-t055-triangle-ffix.md ← threshold 三角 0.55
        └── rag-eval-result-rag05-ffix.md         ← A+B fix RAG-05 单跑验证
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

### Commit 范围（已落地，未 push）

```
3093eb6 docs: 修正 baseline-ffix.md 为 a9fbf7e fix 后真实重跑数据
5a92e60 docs: RAG-05 单跑验证证据归档（A read-timeout 修复直接证据）
a9fbf7e fix: SSE 异常透传 + DeepSeek read-timeout 25s→120s
f78cde1 chore: gitignore 增量 - 本地日志目录 + Python 缓存
fc49eb4 docs: RAG eval 接力计划 + 历史评测产物归档
6da727e feature: RAG eval - F-fix 链路 SSE 下发 type=retrieval 事件
```

注：`a9fbf7e` 意外混入两个 docs 文件（baseline-ffix new + test-triangle delete，AM/D 状态 staging 残留所致）；`3093eb6` 修正了 a9fbf7e commit message 与 baseline-ffix 内容版本不匹配的问题（不 amend，按 CLAUDE.md "Always create NEW commits"）。等 Codex review 后再决定 push。

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

D1 / D2 / Hygiene 已收口（见 §3）。下一会话接手时：

1. Read 这份 `PLAN.md` 全文
2. 确认 §3 commit 范围是否已落地（`git log --oneline -5` 看是否有对应 commit）
3. 确认 backend 已预热：`POST http://localhost:8099/api/v1/agent/armory_agent` body `{"agentId":"rag_demo"}`（**未预热前直接打 auto_agent 会 HTTP 500，duration ~5ms，看似 endpoint 死了**）
4. 等用户点 §4 延期话题里的某一项再开工

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
