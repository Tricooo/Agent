# RAG Eval 自动化 — 计划与状态

> 这份文档是 RAG 评测链路工作的 single source of truth。任何接手会话先读这里。
> 最后更新：2026-05-02

## 1. 总目标

让 `rag_demo` 这个 agent 的 RAG 检索观测信号（`retrieved_document_count`、`score_range`、`retrieval_empty`、`similarity_threshold`、`context_selected/dropped/truncated`、`context_chars` 等 11 个 `qa_*` 字段）稳定从 backend 通过 SSE `type=retrieval` 事件下发到 Python runner，落入 markdown 评测报告的 `retrieved/score/empty` 三列。

这是 RAG 评测自动化基础设施的第一步，后续叠 threshold triangle、paraphrase regression、RAGAS、rerank、hybrid。

## 2. 当前进展

### 2.1 已完成 / 已验证

- **F-fix 已上线并生效**：在 `FixedAgentExecuteStrategy.java` 加 `emitRetrievalIfPresent()` helper，把 `chatResponse.metadata` 的 `qa_*` 字段拍平成 `type=retrieval` SSE 事件下发。
  - 主流程：`ai-agent-domain/src/main/java/com/tricoq/domain/agent/service/fixed/FixedAgentExecuteStrategy.java:46-87`（改用 `.call().chatResponse()`）
  - Helper：同文件 `:105-126`
  - 共享 DTO：`ai-agent-domain/src/main/java/com/tricoq/domain/agent/model/entity/AutoAgentRetrievalSseEntity.java`
  - 验证证据：`docs/dev-ops/rag-eval/rag-eval-result-ffix.md`
    - RAG-04: retrieved=3, score 0.6305..0.6481, empty=false
    - RAG-06: retrieved=0, empty=true
    - RAG-07: retrieved=3, score 0.6185..0.6645
- 评测 runner score_mode 区分（literal vs manual）已落地。
- F-fix 链路下 threshold=0.60 baseline：`rag-eval-result-ffix.md`（May 1 03:21，三列俱全）
- F-fix 链路下 threshold=0.55 三角：`rag-eval-result-t055-triangle-ffix.md`（May 2 03:02，RAG-04 / 06 / 07 三列俱全，预热后跑通）
- threshold=0.65 沿用 `rag-eval-result.md`（Apr 28，旧 schema 无三列，但答案可证 RAG-04 误杀）
- 旧 schema 残留（不可比，不再补跑）：`rag-eval-result-t060-triangle.md` / `rag-eval-result-t065-triangle.md` / `rag-eval-result-test-triangle.md`

### 2.2 关键认知（必读，否则会重复踩坑）

- `rag_demo` 的 `ai_agent.strategy` 是 **`fixedAgentExecuteStrategy`**，**不是** auto chain。
- 不能因为 endpoint 名叫 `/api/v1/agent/auto_agent` 就推断走 `AutoAgentExecuteStrategy`——这是这个项目最大的锚点陷阱。
- C-full + B-symmetric 两轮在 auto chain 上加的 observer 改动（Step1AnalyzeNode / Step4LogExecutionSummaryNode / AbstractExecuteSupport / TextInvocationRequest / SpringAiLlmInvocationGateway）对 `rag_demo` **完全无效**，是死路径。死路径反证见 `rag-eval-result-bsymmetric.md:13-15`（三列全是 `—`）。
- 修 RAG/SSE 相关前必须按 `.claude/rules/agent-runtime-path-proof.md` 的 6 步证据链证明 live path。

## 3. 已决议（接力会话 May 2 收口）

### D1（已决议）：B 回滚 6 个 auto chain 死路径文件

- 已回滚：`StructuredInvocationRequest` / `TextInvocationRequest` / `AbstractExecuteSupport` / `Step1AnalyzeNode` / `Step4LogExecutionSummaryNode` / `SpringAiLlmInvocationGateway`
- 保留 live path：`FixedAgentExecuteStrategy.java`（F-fix）+ `AutoAgentRetrievalSseEntity.java`（共享 DTO）
- 编译验证：`mvn -T 1C -pl ai-agent-domain,ai-agent-infrastructure -am compile -DskipTests` exit 0
- 理由摘要：6 个文件是 b-symmetric/c-full 误把 RAG observer 挂到 auto chain 时的死代码，留着只会让后续读代码的人误判 RAG metadata 流向；`AutoAgentRetrievalSseEntity` 因为也被 F-fix 用到所以保留。

### D2（已决议）：0.55 已补跑；0.60 / 0.65 不再补跑

| threshold | 文件 | 链路 | 三列状态 | 关键观测 |
|---|---|---|---|---|
| 0.55 | `rag-eval-result-t055-triangle-ffix.md`（May 2） | F-fix | 全 | RAG-04 score 0.6305..0.6481 答 ✓；RAG-06 empty 拒答 ✓；RAG-07 score 0.6185..0.6645 manual 拒答 ✓ |
| 0.60 | `rag-eval-result-ffix.md`（May 1） | F-fix | 全 | 与 0.55 完全一致（同 score 区间，因为 score 全部 > 0.60）|
| 0.65 | `rag-eval-result.md`（Apr 28） | F-fix 之前旧 schema | 无三列 | RAG-04 score≈0.648 < 0.65 → 误杀拒答（answer 文本可证）|

**核心结论**：0.55 vs 0.60 在 RAG-04 / RAG-07 上 score 完全一致，**没有 diagnostic delta**；真正分水岭是 0.65（RAG-04 死亡线）。这印证了 PLAN §1 / 笔记 §4.1.3 的认知 —— 单一静态 threshold 难以同时挡住弱相关并保住深层公式（RAG-04 = 0.648 < RAG-07 = 0.6645，弱相关分数高于正例）。后续优化方向应转向 query rewrite / rerank / answerability，不再继续在 threshold 上拍。

### Hygiene（已决议）

- `.gitignore` 已加：`/data/` / `/ai-agent-boot/data/` / `__pycache__/` / `*.pyc`
- `.env.example` 早已有 `!.env.example` 例外规则（`.gitignore:51`），保持入库
- `application-local.yml` / `application-*.local.yml` 早已忽略

### 仍待用户拍板：commit 范围

建议拆三段：

1. **业务 commit**：`FixedAgentExecuteStrategy.java`（M）+ `AutoAgentRetrievalSseEntity.java`（A）+ `cases.json`（A）+ `rag_eval_runner.py`（A）+ `.env.example`（A）
2. **文档 commit**：`PLAN.md` + `rag-eval-result-{ffix,bsymmetric,t055-triangle-ffix,t060-triangle,t065-triangle,test-triangle,}.md`
3. **gitignore commit**：`.gitignore` 增量

不 push。等 Codex review 业务 commit 后再决定。

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
- F-fix 验证：`docs/dev-ops/rag-eval/rag-eval-result-ffix.md`
- 死路径反证：`docs/dev-ops/rag-eval/rag-eval-result-bsymmetric.md:13-15`
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
