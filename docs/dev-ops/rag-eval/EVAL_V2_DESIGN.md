# RAG Eval v2 指标体系设计

> 状态：设计稿
> 适用范围：`docs/dev-ops/rag-eval/` 下 internal / external RAG 评测、runner report、experiment workbench
> 目标：把早期 `literal_hit` 快速 smoke 体系升级为可诊断、可归因、可扩展的分层评测体系。

## 1. 背景与问题

当前 RAG 项目已经完成 chunking、Hybrid + RRF、BGE rerank、Query Rewrite、KnowledgeBaseProfile、Context Salience 和 Step 7.3 final context evidence preservation。系统能力已经从“能不能跑通”进入“每一层能力是否真的改善 RAG 质量”的阶段。

但现有评测仍主要依赖两类口径：

- `score_mode=literal`：用 `expected_points` 对答案做纯子串匹配。
- `score_mode=manual`：拒答 / 概念题靠人工看 `manual_pass`。

这套口径在早期有价值，因为它稳定、便宜、可重复，适合 PromQL、参数名、状态码、日期、版本号等精确事实。但它现在已经成为优化判断的瓶颈：

1. **语义正确会被字面误杀**
   RAG-10 中答案写出 `正常：0-80% / 警告：80-95% / 危险：95-100%`，但因为没有逐字包含 `正常范围 / 警告范围 / 危险范围`，`literal_hit` 仍显示 `2/5`。

2. **模块改进被压成单一分数**
   rewrite、profile、rerank、context salience 的收益可能发生在不同层：candidate pool、final context、answer expression、refusal boundary。单个 `literal_hit` 无法说明到底哪层变好了。

3. **已有观测没有成为一等指标**
   runner 已经能输出 `pre_rerank_documents`、final `documents`、`source_coverage`、rerank/profile/rewrite metadata，但 summary 仍然容易让人第一眼看 `literal_hit`。

4. **拒答质量缺少结构化指标**
   `manual_pass` 无法区分 false refusal、false answer、true refusal 和 hallucination。

5. **样本和聚合不够分层**
   internal 14 条中 Grafana 样本占比高；external samples 已存在，但需要在指标层按 `case_type`、source domain、answerability 维度聚合。

因此 Step 8 的目标不是“删掉 literal”，而是把它降级为 `literal_smoke`，并新增更成熟的分层指标。

## 2. 设计目标

Eval v2 要回答五个问题：

1. **runtime 是否有效**
   这次结果能不能用于功能判断？是否 completed？是否 EOF？rerank 是否真实 applied？profile 是否真实进入 query-time selection？

2. **证据是否被召回**
   关键证据是否进入 pre-rerank candidate pool？

3. **证据是否进入 final context**
   rerank / coverage guard / context assembly 后，关键证据是否保住？

4. **答案语义是否正确**
   答案是否覆盖 expected points 的语义、数值、步骤、边界，而不是只看原词。

5. **答案是否 grounded / answerable**
   答案是否基于 final context；该拒答时是否拒答；有没有无依据编造。

## 3. 非目标

第一版不做这些：

- 不重写历史报告，不破坏旧 `literal_hit` 可比性。
- 不立即引入 LLM judge 作为主判分。
- 不把 runner 变成复杂实验编排器。
- 不要求所有历史 case 一次性补完 v2 rubric。
- 不把 `chunkIndex` 当唯一 golden truth，因为 chunker 参数变化会导致 chunk index 漂移。

## 4. 指标总览

### 4.1 Runtime Validity

用于判断报告是否能参与功能结论。

| 指标 | 含义 |
| --- | --- |
| `runtime_completed` | runner case 是否 completed |
| `runtime_error_type` | EOF、timeout、HTTP error、LLM error 等 |
| `rerank_runtime_status` | `APPLIED` / `PASSTHROUGH` / `FAILED` |
| `rerank_failure_reason` | BGE fallback / parse error / connect error 等 |
| `rewrite_status` | requested / actual mode / fallback reason |
| `profile_status` | `AUTO_PROFILE` / `NO_PROFILE_MATCH` / `NONE` |
| `result_valid_for_scoring` | 是否可用于功能打分 |

原则：

- `completed=false` 或无 retrieval metadata 的 case，不进入功能 pass/fail 结论。
- runtime failure 要单独统计，不能被误判为 RAG 逻辑失败。

### 4.2 Evidence Recall

用于判断证据是否被找到和保住。

| 指标 | 含义 |
| --- | --- |
| `candidate_recall` | expected evidence 在 `pre_rerank_documents` 中命中比例 |
| `final_context_recall` | expected evidence 在 final `documents` 中命中比例 |
| `candidate_to_final_delta` | `final_context_recall - candidate_recall` |
| `lost_after_rerank` | candidate 有证据但 final context 丢证据 |
| `source_hit_pre` | expected source file 是否在 pre-rerank 出现 |
| `source_hit_final` | expected source file 是否在 final context 出现 |
| `section_hit_pre` | expected section 是否在 pre-rerank 出现 |
| `section_hit_final` | expected section 是否在 final context 出现 |

典型诊断：

```text
candidate 0/5 -> final 0/5 -> answer 0/5
= retrieval / query understanding 问题

candidate 5/5 -> final 2/5 -> answer 2/5
= rerank / final context preservation 问题

candidate 5/5 -> final 5/5 -> answer 2/5
= answer generation / semantic scoring / literal mismatch 问题
```

### 4.3 Answer Correctness

用于判断答案是否覆盖预期要点。

| 指标 | 含义 |
| --- | --- |
| `literal_smoke` | 旧 `literal_hit`，纯子串匹配，仅做 smoke |
| `semantic_score` | rubric matcher 命中的加权得分 |
| `semantic_hits` | 命中的 v2 expected point id |
| `semantic_misses` | 漏掉的 v2 expected point id |
| `answer_completeness` | 必答要点覆盖率 |
| `answer_relevance` | 是否回答了用户问题，而不是只复述上下文 |

v2 rubric 的加权规则：

```text
semantic_score = sum(matched point.weight) / sum(all point.weight)
answer_completeness = sum(matched required point.weight) / sum(required point.weight)
```

其中 `required` 默认为 `true`。如果某个 point 是加分项，可显式设置 `"required": false`，它参与 `semantic_score`，但不参与 `answer_completeness`。

`literal_smoke` 适用：

- PromQL 函数名
- 指标名 / 字段名
- 参数名
- 状态码
- 版本号
- 日期 / ID / 队列名

`semantic_score` 适用：

- 自然语言同义表达
- 范围 / 阈值 / 判断标准
- 流程步骤
- 概念边界
- 版本差异说明

`answer_relevance` v1 不做自动判定，默认输出 `n/a`。只有人工复核或 LLM judge second opinion 明确给出结果时，才展示 `pass/fail`；它不能作为 Step 8.1 / 8.2 的自动 verdict 条件。

### 4.4 Groundedness

用于判断答案是否被 final context 支持。

第一版先做轻量 deterministic groundedness：

- answer 命中的 semantic point 必须有对应 `evidence` 在 final context 中命中。
- 只自动检查数字、百分比、日期、状态码、代码 span、PromQL / 参数名等高置信 token。
- 如果 answer 中出现未在 context 中支持的高置信 token，可标记 `unsupported_claim_suspected`。
- 第一版不做通用 NER / claim decomposition，不尝试自动判断所有自然语言 claim 是否 grounded。
- LLM judge 后续作为 P2 second opinion，不作为第一版主判分。

高置信 token v1 先用保守正则抽取：

- 百分比：`\d+(?:\.\d+)?%`
- 多位数字：`\b\d{2,}\b`
- HTTP 状态码：`\b[1-5]\d\d\b`
- Markdown / answer code span：反引号包裹内容

v1 实现范围限定：

| 能自动判 | 不在 v1 自动判 |
| --- | --- |
| code span / 参数名 / 指标名 | 普通中文实体关系 |
| 数字 / 百分比 / 日期 / 状态码 | 复杂因果链 |
| semantic point 对应 evidence 是否进 final context | 段落级隐含推理 |

超出 v1 能力的 case 标记为 `groundedness=n/a` 或 `manual_review_needed`，不能用不稳定 heuristic 直接判 fail。

### 4.5 Answerability / Refusal

拒答 case 单独评估，不再塞进 `literal_hit`。

四象限：

| should_answer | did_answer | 结果 |
| --- | --- | --- |
| true | true | `true_answer` |
| true | false | `false_refusal` |
| false | false | `true_refusal` |
| false | true | `false_answer` / `hallucination` |

`did_answer` v1 判定规则：

```text
did_answer =
  normalized_answer length >= 8
  and not matches_refusal_template(answer)
```

拒答模板匹配在 normalize 后做 substring 判断，但 v1 实现必须保持 prefix-biased：强拒答措辞只看答案开头窗口，弱拒答措辞只看第一句。长答案后半段的局部 caveat，例如“具体限制细节上下文未提供”，不能把一个已经回答主体问题的答案误判成 `false_refusal`。

如果 case 有 `type=refusal` 的 v2 rubric point，且该 point 的 `answer_match` / `aliases` 命中答案，则 answerability 层优先把它视为 `did_answer=false`。这样拒答判断不完全依赖全局拒答模板，避免因为模型用了“无法回答”“无法依据所给资料回答”“文档不包含”等不同措辞而误判为 `false_answer`。

拒答模板第一版包含：

- `无法基于`
- `无法直接回答`
- `当前知识库没有`
- `知识库未涉及`
- `文档没有指定`
- `不能从文档回答`
- `cannot be answered`
- `does not specify`
- `does not identify`
- `not provided in the document`

如果答案同时包含拒答模板和具体无依据答案，以 `false_answer` 优先。例如“文档没有说明，但可能是 AWS”应判为 `did_answer=true` 并进入 `false_answer / hallucination` 风险。

注意：全局拒答模板只能兜底高频表达；每个拒答 case 仍应在 v2 rubric 中写清 case-specific 的拒答证据和 forbidden specific answers。

拒答指标：

| 指标 | 含义 |
| --- | --- |
| `refusal_pass` | 应拒答时是否拒答 |
| `false_answer` | 不该回答却给出具体答案 |
| `false_refusal` | 应回答却拒答 |
| `negative_evidence_used` | 是否利用文档中的 negative evidence |
| `unsupported_specific_answer` | 是否编造具体 provider、审批人、配置等 |

## 5. Case Schema v2

保留旧字段：

```json
{
  "score_mode": "literal",
  "expected_points": ["node_memory_MemAvailable_bytes"]
}
```

新增可选字段：

```json
{
  "expected_points_v2": [
    {
      "id": "memory_formula_available",
      "type": "exact",
      "weight": 1,
      "required": true,
      "answer_match": ["node_memory_MemAvailable_bytes"],
      "context_match": ["node_memory_MemAvailable_bytes"],
      "evidence": [
        {
          "source_file": "grafana-mcp-tools-guide.md",
          "anchor_text": "node_memory_MemAvailable_bytes",
          "optional_chunk_index": 2
        }
      ]
    },
    {
      "id": "normal_range",
      "type": "numeric_alias",
      "weight": 1,
      "required": true,
      "aliases": ["正常范围", "正常", "健康", "normal"],
      "values": ["0-80%"],
      "evidence": [
        {
          "source_file": "grafana-mcp-tools-guide.md",
          "anchor_text": "正常范围: 0-80%",
          "optional_chunk_index": 5
        }
      ]
    }
  ]
}
```

### 5.1 Matcher 类型

| type | 用途 | 示例 |
| --- | --- | --- |
| `exact` | 精确 token | `node_memory_MemAvailable_bytes` |
| `alias` | 同义词 | `正常范围` / `正常` |
| `regex` | 格式可变文本 | 日期、命令、HTTP 状态描述 |
| `numeric_alias` | 标签 + 数值 | `正常` + `0-80%` |
| `ordered_steps` | 流程顺序 | Kubernetes PVC 扩容失败恢复步骤 |
| `refusal` | 拒答语义 | `文档未指定` / `cannot be answered` |

### 5.2 Golden Evidence 规则

优先使用：

```text
source_file + anchor_text
```

辅助使用：

```text
optional_chunk_index
```

原因：

- `source_file + anchor_text` 更稳定，适合 chunker 参数变化。
- `chunkIndex` 方便当前报告定位，但不能当唯一真相。

### 5.3 Matcher 判定规则

为避免 v2 rubric 变成另一个不透明的字面匹配，第一版 matcher 必须固定规则。

#### 5.3.1 文本归一化

所有 matcher 默认先做 normalize：

- 英文转小写。
- 全角数字 / 字母 / 百分号 / 常见标点转半角。
- 中文冒号、短横线、波浪线统一为 `:` / `-` / `~`。
- 连续空白折叠为单空格。
- 去掉 Markdown emphasis 标记，如 `**`、反引号外壳。
- 百分比区间允许等价写法：`0-80%`、`0 – 80%`、`0 到 80%`、`0 至 80%`。

#### 5.3.2 Evidence recall 命中

一个 v2 point 的 candidate recall 命中规则：

```text
candidate_hit(point) =
  exists doc in pre_rerank_documents
  and exists evidence in point.evidence
  and source_file_matches(evidence.source_file, doc.source/sourcePath/file_name)
  and normalize(evidence.anchor_text) is contained in normalize(doc.text)
```

final context recall 同理，只是 document 集合换成 final `documents`。

`source_file_matches` 沿用 runner 现有 source 匹配语义：优先比较 `sourcePath` / `source` / `file_name` 等 metadata，允许 basename 相等、路径 suffix 相等和 source file 末尾匹配。具体实现应复用 runner 中已有 helper，避免 v2 与现有 `source_coverage` 口径分叉。

如果 `source_file` 缺失，则允许只用 `anchor_text` 命中，但 report 必须标记 `evidence_source_unverified=true`。

#### 5.3.3 Answer matcher 命中

| type | 命中规则 |
| --- | --- |
| `exact` | `answer_match` 中任一 normalized token 出现在 normalized answer |
| `alias` | `aliases` 中任一 normalized alias 出现在 normalized answer |
| `regex` | 任一 regex 匹配 raw answer；regex 必须在 case 中显式声明 |
| `numeric_alias` | alias 命中，且 value 在 alias 附近窗口命中 |
| `ordered_steps` | 所有 required steps 必须 100% 命中；按步骤顺序依次选择“上一步之后的最早命中位置”，要求严格单调递增；v1 不给 partial credit |
| `refusal` | 命中拒答语义，且没有 unsupported specific answer |

`ordered_steps` 的 alias 匹配允许两种形式：

- normalized alias 作为连续子串出现在 answer。
- alias 由多个空格分隔 token 组成时，允许这些 token 在同一句 / 同一列表项内按顺序出现，整体跨度不超过 80 字符；token 之间不能跨 `。`、`;`、`!`、`?` 等句子边界。
- 连续子串命中需要避开 ASCII token 前后缀误伤，例如 `删除 PV` 不能命中 `删除 PVC`。

这样 `重新创建 PVC` 可以命中 `重新创建一个比 PV 当前容量更小的 PVC`，但不会把上一条步骤里的 `删除 PVC` 和下一条步骤里的 `claimRef` 错拼成同一步。

#### 5.3.4 numeric_alias 窗口

`numeric_alias` 必须同时满足：

```text
exists alias in aliases
and exists value in values
and distance(normalize(alias), normalize(value)) <= 80 chars
```

`distance` 在同一个 normalized text window 内计算：

```text
distance(alias, value) =
  0, if ranges overlap
  start(value) - end(alias), if alias appears before value
  start(alias) - end(value), if value appears before alias
```

取所有 alias/value 命中组合中的最小非负距离。

如果答案按列表拆分，如：

```text
- 正常：0-80%
- 警告：80-95%
```

每个 list item 单独作为窗口；如果没有 list item，再按句号、分号、换行分句；最后才退回 80 字符窗口。

这样可以接受 `正常：0-80%`，但不会因为同一 chunk 或同一答案里远处出现 `正常` 和 `0-80%` 就误判命中。

#### 5.3.5 v2 rubric 缺失时的 fallback

如果 case 没有 `expected_points_v2`：

- `semantic_score = n/a`
- `semantic_hits / semantic_misses = n/a`
- `failure_layer` 不允许标记为 `answer_semantic_miss` 或 `literal_only_mismatch`
- 正例 case 的 `verdict` 降级为 `manual_review_needed`，或沿用旧 `literal_smoke` 仅作辅助
- 拒答 case 仍可按 answerability 四象限判定

这条规则确保未补 rubric 的历史 case 不会被 v2 指标伪装成完整语义评测。

#### 5.3.6 Rubric 校准原则

v2 rubric 不是天然真相，live smoke 后允许校准，但只能补通用表达，不为单次答案硬编码：

- 可以补：中英文同义词、中文日期格式、`HTTP/1.0 client` / `HTTP/1.0 客户端` 这类等价表达、流程步骤的自然语言改写。
- 可以补：更短但仍指向同一证据的 `anchor_text`，用于降低 chunk preview / metadata 截断带来的 evidence false negative。
- 不可以补：只在某一次答案中出现、但不代表预期语义的冗余句子。
- 不可以因为单次 run 没全绿就持续追 alias；同一 case 多次波动时，应转入 N=3 repeated-run / majority / consistency。

## 6. Report v2 结构

### 6.1 Summary 表

新增列：

```text
| id | type | verdict | failure_layer | candidate_recall | final_context_recall | semantic_score | literal_smoke | health |
```

示例：

```text
| RAG-10 | paraphrase | pass | literal_only_mismatch | 5/5 | 5/5 | 5/5 | 2/5 | ok |
```

`health` 是 summary 压缩列，避免 Markdown 表过宽。取值建议：

| health | 含义 |
| --- | --- |
| `ok` | runtime / groundedness / answerability 均无阻塞 |
| `run_fail` | runtime 失败，不参与功能结论 |
| `ungrounded` | 存在 groundedness 风险 |
| `refusal_issue` | 拒答四象限异常 |
| `review` | 需要人工或 LLM judge second opinion |

`runtime`、`groundedness`、`answerability` 的详细值放到 Details 区。

### 6.1.1 Verdict 取值

`verdict` 只允许以下取值：

| verdict | 含义 |
| --- | --- |
| `pass` | v2 判定通过 |
| `fail` | v2 判定失败 |
| `run_fail` | runtime 失败，本次不进入功能结论 |
| `refusal_fail` | answerability 四象限异常 |
| `manual_review_needed` | deterministic 指标不足，需要人工或 LLM judge second opinion |

判定规则：

| 条件 | verdict |
| --- | --- |
| `failure_layer=runtime_failure` | `run_fail` |
| `failure_layer=false_answer` 或 `false_refusal` | `refusal_fail` |
| `failure_layer=none` 或 `literal_only_mismatch` | `pass` |
| `failure_layer=manual_review_needed` | `manual_review_needed` |
| `failure_layer=groundedness_risk` | `fail` |
| 其他 failure layer | `fail` |

### 6.2 Details 区

每个 case 增加：

```text
eval_v2:
- verdict: pass
- failure_layer: literal_only_mismatch
- candidate_recall: 5/5
- final_context_recall: 5/5
- semantic_score: 5/5
- literal_smoke: 2/5
- groundedness: pass
- answerability: n/a
- semantic_hits: memory_formula_available, memory_formula_total, normal_range, warning_range, danger_range
- semantic_misses: —
```

### 6.3 分层汇总

报告底部新增 `case_type` 汇总：

```text
## Eval v2 By Case Type

| case_type | cases | runtime_ok | candidate_recall | final_context_recall | semantic_score | refusal_pass |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| answerable_formula | 2 | 100% | 100% | 100% | 100% | n/a |
| paraphrase | 2 | 100% | 100% | 80% | 90% | n/a |
| weak_related | 4 | 100% | n/a | n/a | n/a | 100% |
```

### 6.4 拒答四象限

```text
## Answerability Matrix

| bucket | count |
| --- | ---: |
| true_answer | 8 |
| true_refusal | 4 |
| false_refusal | 1 |
| false_answer | 0 |
```

## 7. Failure Layer 枚举

`failure_layer` 用于让报告直接说明失败发生在哪层。

| failure_layer | 含义 |
| --- | --- |
| `none` | 通过 |
| `runtime_failure` | LLM / HTTP / SSE / rerank runtime 失败 |
| `retrieval_miss` | pre-rerank 没有关键证据 |
| `rerank_or_context_loss` | pre-rerank 有证据，final context 丢证据 |
| `answer_semantic_miss` | final context 有证据，答案语义漏点 |
| `literal_only_mismatch` | 语义正确但旧 literal 未命中 |
| `groundedness_risk` | 答案含未被 context 支持的 claim |
| `false_refusal` | 应回答但拒答 |
| `false_answer` | 不该回答但给出具体答案 |
| `manual_review_needed` | deterministic matcher 不足，需要人工或 LLM judge |

### 7.1 优先级

`failure_layer` 必须单值输出，多个问题同时出现时按以下优先级选择最高优先级：

```text
runtime_failure
> false_answer
> false_refusal
> retrieval_miss
> rerank_or_context_loss
> answer_semantic_miss
> groundedness_risk
> literal_only_mismatch
> manual_review_needed
> none
```

理由：

- runtime 失败时报告不具备功能判定资格。
- `false_answer` 是最高风险的 RAG 幻觉，比 false refusal 更严重。
- retrieval / final context 断点优先于 answer 层断点，因为答案层无法弥补证据缺失。
- `literal_only_mismatch` 只在 semantic 已通过但旧 literal 未通过时出现，本质是旧指标误杀。

### 7.2 与现有 source_coverage 的映射

现有 runner 已有 `source_coverage.layer`。Step 8.1 先复用它推导 `failure_layer`：

| source_coverage / runtime 条件 | failure_layer |
| --- | --- |
| completed=false 或 error 非空 | `runtime_failure` |
| should_answer=false 且 did_answer=true | `false_answer` |
| should_answer=true 且 did_answer=false | `false_refusal` |
| `no_retrieval_metadata` | `runtime_failure` 或 `manual_review_needed`，需看 error / backend log |
| pre-rerank expected evidence = 0 | `retrieval_miss` |
| pre-rerank expected evidence > final context expected evidence | `rerank_or_context_loss` |
| final context expected evidence 充足，但 semantic_score 未达阈值 | `answer_semantic_miss` |
| groundedness 检查发现 unsupported high-confidence token | `groundedness_risk` |
| semantic_score 达标，但 literal_smoke 未达标 | `literal_only_mismatch` |
| 没有 v2 rubric 且无法 deterministic 判定 | `manual_review_needed` |
| 以上都不满足 | `none` |

注意：`source_coverage=answer_generation_or_literal_mismatch` 在 v1 中会被拆成 `answer_semantic_miss` 或 `literal_only_mismatch`。拆分依据是 `semantic_score` 是否达标。

`final context expected evidence 充足` 的默认阈值为 `final_context_recall >= 0.8`；如果 case 明确要求全量证据（如公式 + 三档范围），可在 v2 rubric 中设置更高阈值或默认按 required points 全命中处理。

拒答层失败锁定最终 verdict，但 retrieval / final context 指标仍然照常记录到 Details，用于 drilldown。例如 `false_refusal` 的 case 仍可显示 candidate/final 是否找到了证据，帮助区分“证据没找到导致拒答”和“证据已找到但模型拒答”。

## 8. Workbench v2 设计

Runner 负责单次报告，workbench 负责跨 run 归因。

Workbench 后续新增：

### 8.1 Module Lift

从已有 run pair 计算，不让 runner 负责编排 ablation。

```text
rerank_lift = final_context_recall(with_bge) - final_context_recall(no_rerank)
rewrite_lift = candidate_recall(with_rewrite) - candidate_recall(no_rewrite)
profile_lift = candidate_recall(with_profile) - candidate_recall(no_profile)
salience_lift = semantic_score(with_salience) - semantic_score(no_salience)
guard_lift = final_context_recall(with_guard) - final_context_recall(no_guard)
```

### 8.2 聚合维度

- by `case_type`
- by `source_file`
- by `should_answer`
- by `failure_layer`
- by `feature flags`
- by `profile_source`
- by `rerank_mode`

### 8.3 主要视图

- `Overview`: verdict / semantic score / runtime health
- `Failure Layers`: 各失败层分布
- `Evidence Flow`: candidate -> final -> answer 的漏斗
- `Module Lift`: 功能开关的边际贡献
- `Refusal Matrix`: answerability 四象限
- `Literal Mismatch`: 语义过但 literal 失败的 case 列表

## 9. 实施路线

### Step 8.1 Metric Reframe

不改 Java，不改业务链路，只改 runner/report。

任务：

1. 把 `literal_hit` 展示名改成 `literal_smoke`。
2. Summary 新增 `candidate_recall`、`final_context_recall`、`candidate_to_final_delta`、`final_to_answer_delta`。
3. Details 中明确展示三层漏斗：candidate -> final -> answer。
4. 新增 `failure_layer` 初版，基于现有 `source_coverage` 和 runtime 字段推导。
5. 新增 `case_type` 汇总表。
6. 新增 answerability 四象限汇总。
7. 对没有 `expected_points_v2` 的 case，不输出伪 semantic pass/fail，只显示 `semantic_score=n/a`。

Step 8.1 的 recall 口径：

- 不引入 v2 rubric 时，`candidate_recall` / `final_context_recall` 复用旧 `expected_points` 子串口径。
- Step 8.2 起，存在 `expected_points_v2` 的 case 切到 v2 `evidence.anchor_text` 口径。
- workbench 跨版本聚合时必须按 `case_schema_version` 区隔，不能把旧 `expected_points` recall 和 v2 evidence recall 当同一指标直接平均。

验收：

- RAG-10 Step 7.3 报告应显示：
  - `candidate_recall=5/5`
  - `final_context_recall=5/5`
  - `literal_smoke=2/5`
  - `failure_layer=literal_only_mismatch`
- runtime failure 不进入功能结论。
- 未补 v2 rubric 的 literal case 不被自动标记为 `answer_semantic_miss`。

### Step 8.2 Rubric Schema v2

任务：

1. runner 支持 `expected_points_v2`。
2. 实现 deterministic matchers：
   - `exact`
   - `alias`
   - `regex`
   - `numeric_alias`
   - `ordered_steps`
   - `refusal`
3. 先给 6-8 条代表 case 补 v2 rubric：
   - RAG-04
   - RAG-10
   - RAG-14
   - EXT-ARTEMIS-01
   - EXT-RFC9110-03
   - EXT-K8S-PV-03
   - EXT-PG-04
4. Report 输出 `semantic_score`、`semantic_hits`、`semantic_misses`。
5. external samples 至少 12 条进入 v2 rubric 后，才在 workbench 展示 case_type 维度的 module lift；否则只展示 per-case drilldown，避免小样本误导。

验收：

- RAG-10 semantic score 应为 `5/5`，literal smoke 仍保留 `2/5`。
- EXT-ARTEMIS-01 这类日期 / 持续时间格式差异不再被简单误杀。
- 未补 v2 rubric 的 case 显示为 `semantic_score=n/a`，verdict 不因旧 literal 自动通过或失败。

### Step 8.3 Workbench v2

任务：

1. 索引 v2 report 字段。
2. 增加 failure layer dashboard。
3. 增加 module lift pair comparison。
4. 增加 case_type 维度统计。
5. 增加 literal-only mismatch 列表。
6. 增加 dataset version / case schema version 过滤，避免 v1/v2 report 混算。

验收：

- 能回答“某个功能对哪类 case 有贡献”。
- 能区分“模块没用”和“指标分辨率不够”。

### Step 8.4 Stability / Repeatability

P2，先作为独立实验，不进入 Step 8.1 的主线。

任务：

1. 对高波动 case 支持 N=3 重复跑。
2. 输出 `answer_consistency_rate`。
3. runtime 统计增加 P50 / P95 duration。
4. rewrite / rerank elapsed 分别聚合。

验收：

- 能区分“功能逻辑不稳定”和“LLM 采样表达波动”。
- 不把单次 transient timeout 当作功能退化。

### Step 8.5 LLM Judge Second Opinion

P2，暂不进入第一版。

适用：

- 概念解释题
- 长流程题
- 策略比较题
- 复杂拒答题

要求：

- 输出 judge score 和 reason。
- 保留人工抽检。
- 不替代 deterministic rubric，只作为 second opinion。

## 10. Dataset Versioning 与样本门槛

Eval v2 report 必须带版本字段：

```text
eval_schema_version: v2
case_schema_version: v2-compatible
rubric_coverage: 7/14
```

`rubric_coverage` 定义为：

```text
has_non_empty_expected_points_v2_case_count / total_case_count
```

它不是 semantic point 命中数，也不是 expected points 总数。

workbench 聚合时必须遵守：

- v1 report 和 v2 report 默认不混算 `semantic_score`。
- `literal_smoke` 可以跨版本展示，但不能作为主 verdict。
- `case_type` 维度下样本数少于 3 时，显示 `insufficient_samples`。
- external v2 rubric 少于 12 条时，不展示 external 的 module lift 总结，只允许 case drilldown。
- 单一 source domain 占比过高时，summary 标记 `domain_skew_warning`。

## 11. 后续阶段入场条件

Step 8.4 / Step 8.5 不应过早启动。

| 阶段 | 入场条件 |
| --- | --- |
| Step 8.4 Stability / Repeatability | Step 8.1-8.3 已落地，并且至少跑过 2 轮 internal + external full / smoke 后，仍观察到明显 LLM 采样波动或 transient runtime 干扰 |
| Step 8.5 LLM Judge Second Opinion | deterministic rubric 对具体 case_type 明确不够用，例如概念解释、长流程、复杂拒答；必须先列出需要 judge 的 case 清单 |

## 12. 与现有 Step 7.3 的关系

Step 7.3 当前窄集验证显示：

```text
RAG-10:
pre_rerank expected_points_exact = 5/5
final_context expected_points_exact = 5/5
answer literal_hit = 2/5
```

旧口径容易让人误判为“RAG-10 仍失败”。Eval v2 会把它标为：

```text
candidate_recall = 5/5
final_context_recall = 5/5
semantic_score = 5/5
literal_smoke = 2/5
failure_layer = literal_only_mismatch
verdict = pass
```

这说明 Step 7.3 解决的是 `candidate-to-final context preservation`，剩余问题不是 retrieval / rerank / profile，而是旧 literal smoke 指标过窄。

因此建议在继续扩大 Step 7.3 full / external 验证之前，先完成 Step 8.1。否则 full 报告仍会继续被 `literal_hit` 误导。

## 13. 面试表达

可以这样讲：

> 早期我用 literal match 作为低成本 smoke test，因为它对 PromQL、参数名、状态码这类精确信息很稳定。但随着 RAG 链路变复杂，单个 literal 分数会把 retrieval、rerank、context assembly、answer generation 和评测口径混在一起。后来我把 eval 拆成 runtime、candidate evidence recall、final context recall、semantic answer correctness、groundedness 和 answerability 六层。这样一个 case 失败时，不只是看到“分低”，而是能定位到底是没召回、rerank 丢证据、final context 没保住、答案没用好，还是旧字面指标误杀。

## 14. 决策结论

Step 8 的优先级应高于继续堆 RAG 功能。

推荐立即开始：

1. Step 8.1 Metric Reframe：最快获得分层诊断能力。
2. Step 8.2 Rubric Schema v2：解决自然语言语义正确但 literal 失败的问题。
3. Step 8.3 Workbench v2：把模块边际贡献做成长期可视化能力。
