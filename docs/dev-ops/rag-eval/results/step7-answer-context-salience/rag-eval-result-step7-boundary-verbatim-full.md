# RAG Eval Result

- generated_at: `2026-05-23 21:10:16`
- api_url: `http://localhost:8099/api/v1/agent/auto_agent`
- agent_id: `rag_profile_v2_demo`

## Summary

> `literal_hit` 仅做字面子串匹配，是 smoke signal。`score_mode=manual` 的 case（拒答 / 改写 / 概念题）不做字面匹配，统一看 `manual_pass`；`score_mode=literal` 的 case（参数 / 公式 / 清单）才参考 `literal_hit`。
>
> `retrieved` / `score` / `empty` 三列的 `—` 表示**没拿到成功的 ChatResponse metadata**，不等价于"无检索"。当前实现把 retrieval SSE 帧放在 `.call().chatResponse()` 返回之后才发，所以 LLM 调用失败时（即使 RAG 检索本身成功）三列都会是 `—`。要区分"检索失败"和"生成失败"，对照 `error` 列 / details 区 / backend log。
>
> Details 区的 `pre_rerank_documents` 展开 rerank 前候选池，`documents` 展开最终 top-K chunk attribution；document 行的 `score` 是当前阶段写回的候选分，可能来自 HYBRID RRF、query-variant fusion、per-variant rerank RRF 或 fusion-aware rerank；原始向量分与关键词分分别看 `vectorScore` / `keywordScore`，多 query 召回融合看 `queryFusionScore/queryFusionRank/queryVariantHitCount`；最终 rerank 写回分看 `rerankScore`，多路 rerank 融合分看 `rerankFusionScore/rerankVariantHitCount`，fusion-aware 弱加成看 `queryFusionBoostScore/fusionAwareScore`；rerank 服务状态看 `rerank_runtime` 的 model / endpoint / failure_reason；keyword 分支未参与时看 `keywordSkippedReason`。

| id | type | completed | duration_ms | should_answer | retrieved | score | empty | literal_hit | missed_points | answer_preview | manual_pass |
|---|---|---:|---:|---:|---:|---|---:|---:|---|---|---|
| RAG-01 | answerable | true | 14458 | true | 4 | 0.0471 .. 0.0492 | false | manual | — | 根据知识库中的文档，`grafana/list_datasources` 工具的作用是**列出 Grafana 中配置的所有数据源**。 它主要用于： - **确认数据源可用性**（尤其是 Prometheus 的可用性）。 - 获取每个数... |  |
| RAG-02 | answerable | true | 17221 | true | 4 | 0.0471 .. 0.0492 | false | 4/4 |  | 根据提供的文档，`grafana/query_prometheus` 支持以下参数： - **`query`**（必选） PromQL 查询语句，例如 `up` 或计算 CPU 使用率的表达式。 - **`start`**（可选） 查询的开... |  |
| RAG-03 | answerable_formula | true | 22482 | true | 4 | 0.0469 .. 0.0492 | false | 3/3 |  | 知识库里提供了多个 CPU 使用率的 PromQL 查询示例，适用于不同分析场景： 1. **整体 CPU 使用率（5 分钟平均）**[1] ```promql 100 - (avg(irate(node_cpu_seconds_total... |  |
| RAG-04 | answerable_deep_section | true | 16079 | true | 4 | 0.0469 .. 0.0492 | false | 3/3 |  | 根据提供的上下文，磁盘 7 天增长趋势预测使用的 PromQL 是： ```promql predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600... |  |
| RAG-05 | answerable_long_context | true | 14668 | true | 4 | 0.0471 .. 0.0492 | false | 5/5 |  | 根据上下文提供的系统健康报告模板**（来源 [1]）**，一份完整的系统健康报告应包含以下部分： - **📊 监控数据概览** 包括：监控时间范围、数据源信息、分析时间。 - **🖥️ CPU 使用情况** 包括：平均使用率、峰值使用率、状... |  |
| RAG-06 | unanswerable | true | 19152 | false | 1 | 0.0492 .. 0.0492 | false | manual | — | 根据当前的知识库内容，无法找到关于 MySQL InnoDB 的 MVCC undo log 版本链实现的信息。知识库中提供的资料仅涉及“归档执行过程”的步骤描述（预扫描、冻结快照等），与您询问的数据库内部机制无关。如果需要解答这个问题，建... |  |
| RAG-07 | weak_related | true | 13185 | false | 4 | 0.0471 .. 0.0489 | false | manual | — | 根据现有的知识库内容，没有提供关于 Grafana dashboard 变量模板配置的信息。知识库主要介绍的是 Grafana MCP 工具（如 `list_datasources`、`query_prometheus`）的用法、PromQ... |  |
| RAG-08 | weak_related | true | 11603 | false | 1 | 0.0492 .. 0.0492 | false | manual | — | 根据提供的知识库内容，没有与 Alertmanager 的 silence 创建方式相关的信息。知识库主要介绍了 Grafana 中 Prometheus 查询的用法和示例，并未涉及 Alertmanager 配置或 silence 的管理... |  |
| RAG-09 | paraphrase | true | 16534 | true | 4 | 0.0469 .. 0.0492 | false | manual | — | 要确认监控数据的来源，您应该调用 **`grafana/list_datasources`** 工具[1]。 该工具会列出 Grafana 中配置的所有数据源，返回每个数据源的 ID、名称、类型和 URL 等信息，帮助您确认 Prometh... |  |
| RAG-10 | paraphrase | true | 30266 | true | 4 | 0.0469 .. 0.0489 | false | 2/5 | 正常范围, 警告范围, 危险范围 | 根据知识库，查询内存压力的思路主要分为以下几步： 1. **使用 PromQL 查询内存使用率**[1][2] 通过 `grafana/query_prometheus` 工具执行查询，PromQL 为： ```promql (1 - (n... |  |
| RAG-11 | control_plaintext_cn_answerable | true | 14146 | true | 4 | 0.0469 .. 0.0492 | false | 2/2 |  | 根据控制组文档： * **冷数据迁移阈值**：是**连续二十一天未访问**[1][4]。这个阈值用于判断明细是否进入候选池，不等于删除条件或归档任务立即执行[4]。文档中还特别指出，七天检查窗口是安全保护项，而十四天低频访问只会触发容量预警... |  |
| RAG-12 | control_plaintext_cn_weak_related | true | 14003 | false | 4 | 0.0161 .. 0.0486 | false | manual | — | 根据控制组文档的说明，文档中**并未指定归档审批人**。 相关上下文明确指出：“文档没有指定审批人姓名，没有给出审批系统地址，也没有要求用户在回答中补充组织架构信息。”（[1]）同时强调，系统在遇到此类问题时“应明确说明文档未提供，不应编造... |  |
| RAG-13 | control_plaintext_en_answerable | true | 18379 | true | 4 | 0.0469 .. 0.0489 | false | 2/2 |  | According to the control document, the fallback queue name is **silver-river-42**, and the retry budget for that fallbac... |  |
| RAG-14 | control_plaintext_en_weak_related | true | 17970 | false | 4 | 0.0469 .. 0.0489 | false | manual | — | According to the control document, the fallback queue is not hosted by any cloud provider that is identified in the text... |  |

## Details

### RAG-01 - answerable

- question: 根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。
- should_answer: `true`
- expected_source_section: grafana/list_datasources
- completed: `true`
- duration_ms: `14458`
- score_mode: `manual`
- expected_points:
  - 列出所有可用的数据源
  - 返回数据源的基本信息
  - 确认 Prometheus 数据源的可用性

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0471 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `3005` / max `6000`
  - context_salience: cues `formula \| procedure \| parameter \| judgement \| range` / expansions `1`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `5` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，<br>grafana/list_datasources 工具的作用 使用方法 参数<br>list_datasources 返回字段 示例 用途 Grafana MCP`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2631` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。， \| grafana/list_datasources 工具的作用 使用方法 参数 \| list_datasources 返回字段 示例 用途 Grafana MCP`
  - profile_hints: version `hybrid-v2` / source `AUTO_PROFILE` / selected `grafana/list_datasources \| 七天检查窗口的作用是什么？ \| 该文档不是正式业务规范 不能作为生产依据 \| Grafana MCP 工具使用指南 \| 1. grafana/list_datasources \| Grafana MCP工具 \| 列出所有可用的数据源 \| 容量治理控制组文档`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0325`, vectorRank=`1`, vectorScore=`0.8057`, keywordRank=`2`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，"},{"index":2,"rank":1,"text":"grafana/list_datasources 工具的作用 使用方法 参数"},{"index":3,"rank":1,"text":"list_datasources 返回字段 示例 用途 Grafana MCP"}]`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #2 score=`0.0479`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6647`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`, queryFusionScore=`0.0479`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，"},{"index":2,"rank":3,"text":"grafana/list_datasources 工具的作用 使用方法 参数"},{"index":3,"rank":2,"text":"list_datasources 返回字段 示例 用途 Grafana MCP"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #3 score=`0.0469`, source=`grafana-mcp-tools-guide.md`, chunk=`8`, retrievalSource=`KEYWORD`, rrfScore=`0.0159`, vectorRank=`—`, vectorScore=`—`, keywordRank=`3`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`, queryFusionScore=`0.0469`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，"},{"index":2,"rank":4,"text":"grafana/list_datasources 工具的作用 使用方法 参数"},{"index":3,"rank":4,"text":"list_datasources 返回字段 示例 用途 Grafana MCP"}]`
      - preview: ### 常见错误 1. **数据源连接失败** - 检查Prometheus服务状态 - 验证网络连接 - 确认认证配置 2. **查询语法错误** - 验证PromQL语法 - 检查指标名称拼写 - 确认标签选择器 3. **数据缺失** - 检查时间范围设置 - 验证指标采集状态 - 确认数据保留策略 ### 故障排查步骤 1. **验证数据源** ``...
    - #4 score=`0.0466`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6601`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`5`, queryVariantText=`根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`, queryFusionScore=`0.0466`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":5,"text":"根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，"},{"index":2,"rank":5,"text":"grafana/list_datasources 工具的作用 使用方法 参数"},{"index":3,"rank":3,"text":"list_datasources 返回字段 示例 用途 Grafana MCP"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #5 score=`0.0323`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0320`, vectorRank=`4`, vectorScore=`0.6561`, keywordRank=`1`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`, queryFusionScore=`0.0323`, queryFusionRank=`5`, queryVariantHitCount=`2`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，"},{"index":2,"rank":2,"text":"grafana/list_datasources 工具的作用 使用方法 参数"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
  - documents:
    - #1 score=`0.0492`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0325`, vectorRank=`1`, vectorScore=`0.8057`, keywordRank=`2`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，"},{"index":2,"rank":1,"text":"grafana/list_datasources 工具的作用 使用方法 参数"},{"index":3,"rank":1,"text":"list_datasources 返回字段 示例 用途 Grafana MCP"}]`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #2 score=`0.0479`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6601`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`2`, rerankScore=`0.0479`, rerankFusionScore=`0.0479`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`5`, queryVariantText=`根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`, queryFusionScore=`0.0466`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":5,"text":"根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，"},{"index":2,"rank":5,"text":"grafana/list_datasources 工具的作用 使用方法 参数"},{"index":3,"rank":3,"text":"list_datasources 返回字段 示例 用途 Grafana MCP"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #3 score=`0.0479`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0320`, vectorRank=`4`, vectorScore=`0.6561`, keywordRank=`1`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`5`, rerankRank=`3`, rerankScore=`0.0479`, rerankFusionScore=`0.0479`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`, queryFusionScore=`0.0323`, queryFusionRank=`5`, queryVariantHitCount=`2`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，"},{"index":2,"rank":2,"text":"grafana/list_datasources 工具的作用 使用方法 参数"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #4 score=`0.0471`, source=`grafana-mcp-tools-guide.md`, chunk=`8`, retrievalSource=`KEYWORD`, rrfScore=`0.0159`, vectorRank=`—`, vectorScore=`—`, keywordRank=`3`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`4`, rerankScore=`0.0471`, rerankFusionScore=`0.0471`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，`, queryFusionScore=`0.0469`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"根据知识库，grafana/list_datasources 工具的作用是什么？请尽量基于上下文回答。，"},{"index":2,"rank":4,"text":"grafana/list_datasources 工具的作用 使用方法 参数"},{"index":3,"rank":4,"text":"list_datasources 返回字段 示例 用途 Grafana MCP"}]`
      - preview: ### 常见错误 1. **数据源连接失败** - 检查Prometheus服务状态 - 验证网络连接 - 确认认证配置 2. **查询语法错误** - 验证PromQL语法 - 检查指标名称拼写 - 确认标签选择器 3. **数据缺失** - 检查时间范围设置 - 验证指标采集状态 - 确认数据保留策略 ### 故障排查步骤 1. **验证数据源** ``...

answer:

```text
根据知识库中的文档，`grafana/list_datasources` 工具的作用是**列出 Grafana 中配置的所有数据源**。

它主要用于：
- **确认数据源可用性**（尤其是 Prometheus 的可用性）。
- 获取每个数据源的基本信息，包括 ID、名称、类型（如`prometheus`）、URL、访问方式（如`proxy`）以及是否为默认数据源。
- 支持多数据源环境的管理和系统分析前的基础检查。

返回的示例格式如下（来自上下文）：
```json
{
  "id": 1,
  "name": "Prometheus",
  "type": "prometheus",
  "url": "http://prometheus:9090",
  "access": "proxy",
  "isDefault": true
}
```

上下文还指出，该工具常在**自动化分析流程的第一步**中使用，用来验证数据源配置，确保后续查询能正常执行。
```

### RAG-02 - answerable

- question: `grafana/query_prometheus` 支持哪些参数？
- should_answer: `true`
- expected_source_section: grafana/query_prometheus 参数说明
- completed: `true`
- duration_ms: `17221`
- score_mode: `literal`
- expected_points:
  - [x] query
  - [x] start
  - [x] end
  - [x] step

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0471 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2475` / max `6000`
  - context_salience: cues `formula \| range \| procedure \| parameter \| example \| judgement` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `6` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text ``grafana/query_prometheus` 支持哪些参数？，<br>grafana query_prometheus datasource query parameters PromQL step instant range resolution<br>grafana query_prometheus parameter configuration examples time range interval limit`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2750` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts ``grafana/query_prometheus` 支持哪些参数？， \| grafana query_prometheus datasource query parameters PromQL step instant range resolution \| grafana query_prometheus parameter configuration examples time range interval limit`
  - profile_hints: version `hybrid-v2` / source `AUTO_PROFILE` / selected `参数 \| grafana/query_prometheus \| query`
  - pre_rerank_documents:
    - #1 score=`0.0489`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0323`, vectorRank=`1`, vectorScore=`0.8033`, keywordRank=`3`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=``grafana/query_prometheus` 支持哪些参数？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"`grafana/query_prometheus` 支持哪些参数？，"},{"index":2,"rank":1,"text":"grafana query_prometheus datasource query parameters PromQL step instant range resolution"},{"index":3,"rank":1,"text":"grafana query_prometheus parameter configuration examples time range interval limit"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #2 score=`0.0479`, source=`grafana-mcp-tools-guide.md`, chunk=`8`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0318`, vectorRank=`4`, vectorScore=`0.6919`, keywordRank=`2`, keywordScore=`0.3000`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=``grafana/query_prometheus` 支持哪些参数？，`, queryFusionScore=`0.0479`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"`grafana/query_prometheus` 支持哪些参数？，"},{"index":2,"rank":2,"text":"grafana query_prometheus datasource query parameters PromQL step instant range resolution"},{"index":3,"rank":3,"text":"grafana query_prometheus parameter configuration examples time range interval limit"}]`
      - preview: ### 常见错误 1. **数据源连接失败** - 检查Prometheus服务状态 - 验证网络连接 - 确认认证配置 2. **查询语法错误** - 验证PromQL语法 - 检查指标名称拼写 - 确认标签选择器 3. **数据缺失** - 检查时间范围设置 - 验证指标采集状态 - 确认数据保留策略 ### 故障排查步骤 1. **验证数据源** ``...
    - #3 score=`0.0476`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7107`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=``grafana/query_prometheus` 支持哪些参数？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"`grafana/query_prometheus` 支持哪些参数？，"},{"index":2,"rank":3,"text":"grafana query_prometheus datasource query parameters PromQL step instant range resolution"},{"index":3,"rank":2,"text":"grafana query_prometheus parameter configuration examples time range interval limit"}]`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #4 score=`0.0320`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0323`, vectorRank=`3`, vectorScore=`0.6989`, keywordRank=`1`, keywordScore=`0.3000`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=``grafana/query_prometheus` 支持哪些参数？，`, queryFusionScore=`0.0320`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":1,"text":"`grafana/query_prometheus` 支持哪些参数？，"},{"index":2,"rank":4,"text":"grafana query_prometheus datasource query parameters PromQL step instant range resolution"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #5 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6909`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`grafana query_prometheus parameter configuration examples time range interval limit`, queryFusionScore=`0.0156`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"grafana query_prometheus parameter configuration examples time range interval limit"}]`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...
    - #6 score=`0.0154`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`KEYWORD`, rrfScore=`0.0156`, vectorRank=`—`, vectorScore=`—`, keywordRank=`4`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`6`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`5`, queryVariantText=``grafana/query_prometheus` 支持哪些参数？，`, queryFusionScore=`0.0154`, queryFusionRank=`6`, queryVariantHitCount=`1`, bestQueryVariantRank=`5`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":5,"text":"`grafana/query_prometheus` 支持哪些参数？，"}]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
  - documents:
    - #1 score=`0.0492`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0323`, vectorRank=`1`, vectorScore=`0.8033`, keywordRank=`3`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=``grafana/query_prometheus` 支持哪些参数？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"`grafana/query_prometheus` 支持哪些参数？，"},{"index":2,"rank":1,"text":"grafana query_prometheus datasource query parameters PromQL step instant range resolution"},{"index":3,"rank":1,"text":"grafana query_prometheus parameter configuration examples time range interval limit"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #2 score=`0.0479`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0323`, vectorRank=`3`, vectorScore=`0.6989`, keywordRank=`1`, keywordScore=`0.3000`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`2`, rerankScore=`0.0479`, rerankFusionScore=`0.0479`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=``grafana/query_prometheus` 支持哪些参数？，`, queryFusionScore=`0.0320`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":1,"text":"`grafana/query_prometheus` 支持哪些参数？，"},{"index":2,"rank":4,"text":"grafana query_prometheus datasource query parameters PromQL step instant range resolution"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #3 score=`0.0476`, source=`grafana-mcp-tools-guide.md`, chunk=`8`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0318`, vectorRank=`4`, vectorScore=`0.6919`, keywordRank=`2`, keywordScore=`0.3000`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":5},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=``grafana/query_prometheus` 支持哪些参数？，`, queryFusionScore=`0.0479`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"`grafana/query_prometheus` 支持哪些参数？，"},{"index":2,"rank":2,"text":"grafana query_prometheus datasource query parameters PromQL step instant range resolution"},{"index":3,"rank":3,"text":"grafana query_prometheus parameter configuration examples time range interval limit"}]`
      - preview: ### 常见错误 1. **数据源连接失败** - 检查Prometheus服务状态 - 验证网络连接 - 确认认证配置 2. **查询语法错误** - 验证PromQL语法 - 检查指标名称拼写 - 确认标签选择器 3. **数据缺失** - 检查时间范围设置 - 验证指标采集状态 - 确认数据保留策略 ### 故障排查步骤 1. **验证数据源** ``...
    - #4 score=`0.0471`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`KEYWORD`, rrfScore=`0.0156`, vectorRank=`—`, vectorScore=`—`, keywordRank=`4`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`6`, rerankRank=`4`, rerankScore=`0.0471`, rerankFusionScore=`0.0471`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`5`, queryVariantText=``grafana/query_prometheus` 支持哪些参数？，`, queryFusionScore=`0.0154`, queryFusionRank=`6`, queryVariantHitCount=`1`, bestQueryVariantRank=`5`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":5,"text":"`grafana/query_prometheus` 支持哪些参数？，"}]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...

answer:

```text
根据提供的文档，`grafana/query_prometheus` 支持以下参数：

- **`query`**（必选）
  PromQL 查询语句，例如 `up` 或计算 CPU 使用率的表达式。

- **`start`**（可选）
  查询的开始时间。如果不指定，则由 Grafana 或工具自身决定默认范围（文档未说明默认值）。

- **`end`**（可选）
  查询的结束时间。同样为可选，未说明默认行为。

- **`step`**（可选）
  查询步长（分辨率），通常用于范围查询。可选参数。

以上信息来自上下文 [1] 的参数说明部分，其中明确列出了这四个参数，并标注后三个为可选。
```

### RAG-03 - answerable_formula

- question: 知识库里 CPU 使用率的 PromQL 示例是什么？
- should_answer: `true`
- expected_source_section: CPU使用率查询
- completed: `true`
- duration_ms: `22482`
- score_mode: `literal`
- expected_points:
  - [x] node_cpu_seconds_total
  - [x] mode="idle"
  - [x] irate

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0469 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2873` / max `6000`
  - context_salience: cues `formula \| range \| procedure \| parameter \| example \| judgement` / expansions `1`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `5` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `知识库里 CPU 使用率的 PromQL 示例是什么？，<br>CPU使用率 PromQL 示例 node_cpu_seconds_total rate<br>PromQL查询 计算CPU使用率 公式 步骤 参数`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3234` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `知识库里 CPU 使用率的 PromQL 示例是什么？， \| CPU使用率 PromQL 示例 node_cpu_seconds_total rate \| PromQL查询 计算CPU使用率 公式 步骤 参数`
  - profile_hints: version `hybrid-v2` / source `AUTO_PROFILE` / selected `CPU \| 示例 \| PromQL \| CPU使用率查询 \| node_cpu_seconds_total \| 内存使用率查询 \| 磁盘使用率查询 \| 七天检查窗口的作用是什么？`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7252`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`知识库里 CPU 使用率的 PromQL 示例是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"知识库里 CPU 使用率的 PromQL 示例是什么？，"},{"index":2,"rank":1,"text":"CPU使用率 PromQL 示例 node_cpu_seconds_total rate"},{"index":3,"rank":1,"text":"PromQL查询 计算CPU使用率 公式 步骤 参数"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #2 score=`0.0481`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6976`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`知识库里 CPU 使用率的 PromQL 示例是什么？，`, queryFusionScore=`0.0481`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"知识库里 CPU 使用率的 PromQL 示例是什么？，"},{"index":2,"rank":3,"text":"CPU使用率 PromQL 示例 node_cpu_seconds_total rate"},{"index":3,"rank":2,"text":"PromQL查询 计算CPU使用率 公式 步骤 参数"}]`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...
    - #3 score=`0.0471`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6745`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`知识库里 CPU 使用率的 PromQL 示例是什么？，`, queryFusionScore=`0.0471`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"知识库里 CPU 使用率的 PromQL 示例是什么？，"},{"index":2,"rank":4,"text":"CPU使用率 PromQL 示例 node_cpu_seconds_total rate"},{"index":3,"rank":3,"text":"PromQL查询 计算CPU使用率 公式 步骤 参数"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #4 score=`0.0318`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0320`, vectorRank=`3`, vectorScore=`0.7014`, keywordRank=`2`, keywordScore=`0.2000`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`2`, queryVariantText=`CPU使用率 PromQL 示例 node_cpu_seconds_total rate`, queryFusionScore=`0.0318`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`2`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":2,"text":"CPU使用率 PromQL 示例 node_cpu_seconds_total rate"},{"index":3,"rank":4,"text":"PromQL查询 计算CPU使用率 公式 步骤 参数"}]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #5 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6863`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`知识库里 CPU 使用率的 PromQL 示例是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"知识库里 CPU 使用率的 PromQL 示例是什么？，"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
  - documents:
    - #1 score=`0.0492`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7252`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`知识库里 CPU 使用率的 PromQL 示例是什么？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"知识库里 CPU 使用率的 PromQL 示例是什么？，"},{"index":2,"rank":1,"text":"CPU使用率 PromQL 示例 node_cpu_seconds_total rate"},{"index":3,"rank":1,"text":"PromQL查询 计算CPU使用率 公式 步骤 参数"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #2 score=`0.0481`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0320`, vectorRank=`3`, vectorScore=`0.7014`, keywordRank=`2`, keywordScore=`0.2000`, keywordSkippedReason=`—`, beforeRerankRank=`4`, rerankRank=`2`, rerankScore=`0.0481`, rerankFusionScore=`0.0481`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":3},{"index":3,"rank":2}]`, queryVariantIndex=`2`, queryVariantRank=`2`, queryVariantText=`CPU使用率 PromQL 示例 node_cpu_seconds_total rate`, queryFusionScore=`0.0318`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`2`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":2,"text":"CPU使用率 PromQL 示例 node_cpu_seconds_total rate"},{"index":3,"rank":4,"text":"PromQL查询 计算CPU使用率 公式 步骤 参数"}]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #3 score=`0.0471`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6745`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0471`, rerankFusionScore=`0.0471`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`知识库里 CPU 使用率的 PromQL 示例是什么？，`, queryFusionScore=`0.0471`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"知识库里 CPU 使用率的 PromQL 示例是什么？，"},{"index":2,"rank":4,"text":"CPU使用率 PromQL 示例 node_cpu_seconds_total rate"},{"index":3,"rank":3,"text":"PromQL查询 计算CPU使用率 公式 步骤 参数"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #4 score=`0.0469`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6976`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":5},{"index":2,"rank":2},{"index":3,"rank":5}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`知识库里 CPU 使用率的 PromQL 示例是什么？，`, queryFusionScore=`0.0481`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"知识库里 CPU 使用率的 PromQL 示例是什么？，"},{"index":2,"rank":3,"text":"CPU使用率 PromQL 示例 node_cpu_seconds_total rate"},{"index":3,"rank":2,"text":"PromQL查询 计算CPU使用率 公式 步骤 参数"}]`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...

answer:

```text
知识库里提供了多个 CPU 使用率的 PromQL 查询示例，适用于不同分析场景：

1. **整体 CPU 使用率（5 分钟平均）**[1]
   ```promql
   100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
   ```

2. **按实例分组的 CPU 使用率**[1]
   ```promql
   100 - (avg by (instance)(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
   ```

3. **过去 1 小时的 CPU 使用率**[3]
   ```promql
   100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100)
   ```

4. **24 小时内 CPU 峰值时间（用于识别高负载时段）**[2]
   ```promql
   max_over_time((100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100))[24h:1h])
   ```

5. **CPU 与内存关联分析中的 CPU 部分**[2]
   ```promql
   (100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100))
   ```
   这个表达式常与内存使用率表达式组合，以综合评估资源关联性。

以上示例分别来自知识库的不同部分，覆盖了即时查询、分组聚合、历史峰值分析和关联分析等典型场景。
```

### RAG-04 - answerable_deep_section

- question: 磁盘 7 天增长趋势预测用哪个 PromQL？
- should_answer: `true`
- expected_source_section: 趋势预测
- completed: `true`
- duration_ms: `16079`
- score_mode: `literal`
- expected_points:
  - [x] predict_linear
  - [x] node_filesystem_avail_bytes
  - [x] 7*24*3600

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0469 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `3023` / max `6000`
  - context_salience: cues `formula \| range \| judgement \| procedure` / expansions `1`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `7` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `磁盘 7 天增长趋势预测用哪个 PromQL？，<br>predict_linear node_filesystem_avail_bytes disk_usage 7d growth trend PromQL<br>磁盘使用率预测 predict_linear 示例 参数 步骤 阈值`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `5004` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `磁盘 7 天增长趋势预测用哪个 PromQL？， \| predict_linear node_filesystem_avail_bytes disk_usage 7d growth trend PromQL \| 磁盘使用率预测 predict_linear 示例 参数 步骤 阈值`
  - profile_hints: version `hybrid-v2` / source `AUTO_PROFILE` / selected `PromQL \| node_filesystem_avail_bytes \| node_filesystem_size_bytes \| predict_linear \| 磁盘使用率查询 \| disk_available \| disk_status \| disk_usage`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`grafana-mcp-tools-guide.md`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7406`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"磁盘 7 天增长趋势预测用哪个 PromQL？，"},{"index":2,"rank":1,"text":"predict_linear node_filesystem_avail_bytes disk_usage 7d growth trend PromQL"},{"index":3,"rank":1,"text":"磁盘使用率预测 predict_linear 示例 参数 步骤 阈值"}]`
      - preview: # 磁盘使用增长趋势 predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) ``` ## 数据解释和分析标准 ### CPU数据解释 - **数值范围**: 0-100% - **正常范围**: 0-70% - **警告范围**: 70-90% - **危险范...
    - #2 score=`0.0476`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6639`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0476`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"磁盘 7 天增长趋势预测用哪个 PromQL？，"},{"index":2,"rank":3,"text":"predict_linear node_filesystem_avail_bytes disk_usage 7d growth trend PromQL"},{"index":3,"rank":4,"text":"磁盘使用率预测 predict_linear 示例 参数 步骤 阈值"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #3 score=`0.0315`, source=`grafana-mcp-tools-guide.md`, chunk=`7`, retrievalSource=`KEYWORD`, rrfScore=`0.0156`, vectorRank=`—`, vectorScore=`—`, keywordRank=`4`, keywordScore=`0.1000`, keywordSkippedReason=`—`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`5`, queryVariantText=`predict_linear node_filesystem_avail_bytes disk_usage 7d growth trend PromQL`, queryFusionScore=`0.0315`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`2`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":5,"text":"predict_linear node_filesystem_avail_bytes disk_usage 7d growth trend PromQL"},{"index":3,"rank":2,"text":"磁盘使用率预测 predict_linear 示例 参数 步骤 阈值"}]`
      - preview: - **当前使用率**: {memory_usage}% - **可用内存**: {memory_available}GB - **状态评估**: {memory_status} - **使用趋势**: {memory_trend} ### 💿 磁盘使用情况 - **使用率**: {disk_usage}% - **可用空间**: {disk_availab...
    - #4 score=`0.0313`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6481`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0313`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":4,"text":"磁盘 7 天增长趋势预测用哪个 PromQL？，"},{"index":2,"rank":4,"text":"predict_linear node_filesystem_avail_bytes disk_usage 7d growth trend PromQL"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #5 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0325`, vectorRank=`2`, vectorScore=`0.6939`, keywordRank=`1`, keywordScore=`0.2000`, keywordSkippedReason=`—`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`2`, queryVariantText=`predict_linear node_filesystem_avail_bytes disk_usage 7d growth trend PromQL`, queryFusionScore=`0.0161`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":2,"text":"predict_linear node_filesystem_avail_bytes disk_usage 7d growth trend PromQL"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #6 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6563`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`6`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0159`, queryFusionRank=`6`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"磁盘 7 天增长趋势预测用哪个 PromQL？，"}]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #7 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6286`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`7`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`3`, queryVariantRank=`3`, queryVariantText=`磁盘使用率预测 predict_linear 示例 参数 步骤 阈值`, queryFusionScore=`0.0159`, queryFusionRank=`7`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":3,"text":"磁盘使用率预测 predict_linear 示例 参数 步骤 阈值"}]`
      - preview: - **危险范围**: 95-100% ### 网络数据解释 - **单位**: bps (bits per second) - **正常范围**: 根据网络带宽确定 - **异常指标**: 突然的流量峰值或持续的高流量 ## 报告生成模板 ### 系统健康报告模板 ```markdown ## 系统运行状态分析报告 ### 📊 监控数据概览 - **监控时...
  - documents:
    - #1 score=`0.0492`, source=`grafana-mcp-tools-guide.md`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7406`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"磁盘 7 天增长趋势预测用哪个 PromQL？，"},{"index":2,"rank":1,"text":"predict_linear node_filesystem_avail_bytes disk_usage 7d growth trend PromQL"},{"index":3,"rank":1,"text":"磁盘使用率预测 predict_linear 示例 参数 步骤 阈值"}]`
      - preview: # 磁盘使用增长趋势 predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) ``` ## 数据解释和分析标准 ### CPU数据解释 - **数值范围**: 0-100% - **正常范围**: 0-70% - **警告范围**: 70-90% - **危险范...
    - #2 score=`0.0481`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR_KEYWORD`, rrfScore=`0.0325`, vectorRank=`2`, vectorScore=`0.6939`, keywordRank=`1`, keywordScore=`0.2000`, keywordSkippedReason=`—`, beforeRerankRank=`5`, rerankRank=`2`, rerankScore=`0.0481`, rerankFusionScore=`0.0481`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`2`, queryVariantRank=`2`, queryVariantText=`predict_linear node_filesystem_avail_bytes disk_usage 7d growth trend PromQL`, queryFusionScore=`0.0161`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":2,"text":"predict_linear node_filesystem_avail_bytes disk_usage 7d growth trend PromQL"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #3 score=`0.0471`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6563`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`6`, rerankRank=`3`, rerankScore=`0.0471`, rerankFusionScore=`0.0471`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":4},{"index":3,"rank":5}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0159`, queryFusionRank=`6`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"磁盘 7 天增长趋势预测用哪个 PromQL？，"}]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #4 score=`0.0469`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6639`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":5},{"index":2,"rank":3},{"index":3,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`磁盘 7 天增长趋势预测用哪个 PromQL？，`, queryFusionScore=`0.0476`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"磁盘 7 天增长趋势预测用哪个 PromQL？，"},{"index":2,"rank":3,"text":"predict_linear node_filesystem_avail_bytes disk_usage 7d growth trend PromQL"},{"index":3,"rank":4,"text":"磁盘使用率预测 predict_linear 示例 参数 步骤 阈值"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...

answer:

```text
根据提供的上下文，磁盘 7 天增长趋势预测使用的 PromQL 是：

```promql
predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600)
```

该查询基于过去 24 小时 (`[24h]`) 的变化趋势，预测未来 7 天（`7*24*3600` 秒）后的可用空间。如果结果为负数，说明预测可用空间会在 7 天内耗尽（即磁盘使用率可能达到 100%）。 [1]
```

### RAG-05 - answerable_long_context

- question: 系统健康报告模板包含哪些部分？
- should_answer: `true`
- expected_source_section: 系统健康报告模板
- completed: `true`
- duration_ms: `14668`
- score_mode: `literal`
- expected_points:
  - [x] CPU
  - [x] 内存
  - [x] 磁盘
  - [x] 网络
  - [x] 综合评估

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0471 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `3148` / max `6000`
  - context_salience: cues `formula \| range \| judgement \| procedure` / expansions `2`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `系统健康报告模板包含哪些部分？，<br>System Health Report Template 组成部分、章节结构、字段定义<br>系统健康报告模板 各模块 示例 配置项 阈值`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2432` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `系统健康报告模板包含哪些部分？， \| System Health Report Template 组成部分、章节结构、字段定义 \| 系统健康报告模板 各模块 示例 配置项 阈值`
  - profile_hints: version `hybrid-v2` / source `AUTO_PROFILE` / selected `系统健康报告模板 \| 模板 \| 该文档是实验材料 不包含正式标准`
  - pre_rerank_documents:
    - #1 score=`0.0489`, source=`grafana-mcp-tools-guide.md`, chunk=`7`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7195`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`系统健康报告模板包含哪些部分？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"系统健康报告模板包含哪些部分？，"},{"index":2,"rank":1,"text":"System Health Report Template 组成部分、章节结构、字段定义"},{"index":3,"rank":2,"text":"系统健康报告模板 各模块 示例 配置项 阈值"}]`
      - preview: - **当前使用率**: {memory_usage}% - **可用内存**: {memory_available}GB - **状态评估**: {memory_status} - **使用趋势**: {memory_trend} ### 💿 磁盘使用情况 - **使用率**: {disk_usage}% - **可用空间**: {disk_availab...
    - #2 score=`0.0486`, source=`grafana-mcp-tools-guide.md`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7172`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`系统健康报告模板包含哪些部分？，`, queryFusionScore=`0.0486`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"系统健康报告模板包含哪些部分？，"},{"index":2,"rank":2,"text":"System Health Report Template 组成部分、章节结构、字段定义"},{"index":3,"rank":1,"text":"系统健康报告模板 各模块 示例 配置项 阈值"}]`
      - preview: - **危险范围**: 95-100% ### 网络数据解释 - **单位**: bps (bits per second) - **正常范围**: 根据网络带宽确定 - **异常指标**: 突然的流量峰值或持续的高流量 ## 报告生成模板 ### 系统健康报告模板 ```markdown ## 系统运行状态分析报告 ### 📊 监控数据概览 - **监控时...
    - #3 score=`0.0476`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6314`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`系统健康报告模板包含哪些部分？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"系统健康报告模板包含哪些部分？，"},{"index":2,"rank":3,"text":"System Health Report Template 组成部分、章节结构、字段定义"},{"index":3,"rank":3,"text":"系统健康报告模板 各模块 示例 配置项 阈值"}]`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...
    - #4 score=`0.0313`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6178`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`系统健康报告模板包含哪些部分？，`, queryFusionScore=`0.0313`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"系统健康报告模板包含哪些部分？，"},{"index":3,"rank":4,"text":"系统健康报告模板 各模块 示例 配置项 阈值"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
  - documents:
    - #1 score=`0.0492`, source=`grafana-mcp-tools-guide.md`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7172`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`系统健康报告模板包含哪些部分？，`, queryFusionScore=`0.0486`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"系统健康报告模板包含哪些部分？，"},{"index":2,"rank":2,"text":"System Health Report Template 组成部分、章节结构、字段定义"},{"index":3,"rank":1,"text":"系统健康报告模板 各模块 示例 配置项 阈值"}]`
      - preview: - **危险范围**: 95-100% ### 网络数据解释 - **单位**: bps (bits per second) - **正常范围**: 根据网络带宽确定 - **异常指标**: 突然的流量峰值或持续的高流量 ## 报告生成模板 ### 系统健康报告模板 ```markdown ## 系统运行状态分析报告 ### 📊 监控数据概览 - **监控时...
    - #2 score=`0.0484`, source=`grafana-mcp-tools-guide.md`, chunk=`7`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7195`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`系统健康报告模板包含哪些部分？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"系统健康报告模板包含哪些部分？，"},{"index":2,"rank":1,"text":"System Health Report Template 组成部分、章节结构、字段定义"},{"index":3,"rank":2,"text":"系统健康报告模板 各模块 示例 配置项 阈值"}]`
      - preview: - **当前使用率**: {memory_usage}% - **可用内存**: {memory_available}GB - **状态评估**: {memory_status} - **使用趋势**: {memory_trend} ### 💿 磁盘使用情况 - **使用率**: {disk_usage}% - **可用空间**: {disk_availab...
    - #3 score=`0.0474`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6314`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`3`, rerankScore=`0.0474`, rerankFusionScore=`0.0474`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`系统健康报告模板包含哪些部分？，`, queryFusionScore=`0.0476`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"系统健康报告模板包含哪些部分？，"},{"index":2,"rank":3,"text":"System Health Report Template 组成部分、章节结构、字段定义"},{"index":3,"rank":3,"text":"系统健康报告模板 各模块 示例 配置项 阈值"}]`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...
    - #4 score=`0.0471`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6178`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`4`, rerankScore=`0.0471`, rerankFusionScore=`0.0471`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`系统健康报告模板包含哪些部分？，`, queryFusionScore=`0.0313`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"系统健康报告模板包含哪些部分？，"},{"index":3,"rank":4,"text":"系统健康报告模板 各模块 示例 配置项 阈值"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...

answer:

```text
根据上下文提供的系统健康报告模板**（来源 [1]）**，一份完整的系统健康报告应包含以下部分：

- **📊 监控数据概览**
  包括：监控时间范围、数据源信息、分析时间。

- **🖥️ CPU 使用情况**
  包括：平均使用率、峰值使用率、状态评估、趋势分析。

- **💾 内存使用情况**
  包括：当前使用率、可用内存、状态评估、使用趋势。

- **💿 磁盘使用情况**
  包括：使用率、可用空间、状态评估、空间预警。

- **🌐 网络流量情况**
  包括：接收速率、发送速率、状态评估、流量模式。

- **📈 综合评估**
  包括：系统健康度、关键发现、优化建议、告警建议。
```

### RAG-06 - unanswerable

- question: 根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `19152`
- score_mode: `manual`
- expected_points:
  - 无法基于当前知识库回答
  - 当前知识库没有相关上下文

retrieval:
- event #1
  - retrieved_document_count: `1`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0492 .. 0.0492`
  - context_selected: `1` / dropped: `0` / truncated: `false`
  - context_chars: actual `281` / max `6000`
  - context_salience: cues `range \| procedure` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `1` / final `1`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？，<br>InnoDB MVCC undo log 版本链 实现原理 read_view trx_id roll_ptr<br>InnoDB undo log 版本链 构造步骤 示例 回滚段 异常情况 限制`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3663` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `根据知识库，MySQL InnoDB 的 MVCC undo log 版本链是怎么实现的？， \| InnoDB MVCC undo log 版本链 实现原理 read_view trx_id roll_ptr \| InnoDB undo log 版本链 构造步骤 示例 回滚段 异常情况 限制`
  - profile_hints: version `hybrid-v2` / source `AUTO_PROFILE` / selected `文档未说明Grafana版本或其他类型数据源`
  - pre_rerank_documents:
    - #1 score=`0.0328`, source=`control-cn-low-newline.txt`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6101`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`1`, queryVariantText=`InnoDB MVCC undo log 版本链 实现原理 read_view trx_id roll_ptr`, queryFusionScore=`0.0328`, queryFusionRank=`1`, queryVariantHitCount=`2`, bestQueryVariantRank=`1`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":1,"text":"InnoDB MVCC undo log 版本链 实现原理 read_view trx_id roll_ptr"},{"index":3,"rank":1,"text":"InnoDB undo log 版本链 构造步骤 示例 回滚段 异常情况 限制"}]`
      - preview: 归档执行过程分为预扫描、冻结快照、分批搬迁、校验回填和灰度释放五段。预扫描阶段只读取元数据，不移动正文。冻结快照阶段会记录候选明细的主键范围、最近更新时间、摘要哈希和原始分区。分批搬迁阶段按租户和业务类型做小批量提交，每批上限为三千条。三千条是单次提交上限，不是迁移阈值。校验回填阶段会对摘要索引、审计索引和归档位置做三方比对。灰度释放阶段会在查询网关里开启双...
  - documents:
    - #1 score=`0.0492`, source=`control-cn-low-newline.txt`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6101`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`2`, queryVariantRank=`1`, queryVariantText=`InnoDB MVCC undo log 版本链 实现原理 read_view trx_id roll_ptr`, queryFusionScore=`0.0328`, queryFusionRank=`1`, queryVariantHitCount=`2`, bestQueryVariantRank=`1`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":1,"text":"InnoDB MVCC undo log 版本链 实现原理 read_view trx_id roll_ptr"},{"index":3,"rank":1,"text":"InnoDB undo log 版本链 构造步骤 示例 回滚段 异常情况 限制"}]`
      - preview: 归档执行过程分为预扫描、冻结快照、分批搬迁、校验回填和灰度释放五段。预扫描阶段只读取元数据，不移动正文。冻结快照阶段会记录候选明细的主键范围、最近更新时间、摘要哈希和原始分区。分批搬迁阶段按租户和业务类型做小批量提交，每批上限为三千条。三千条是单次提交上限，不是迁移阈值。校验回填阶段会对摘要索引、审计索引和归档位置做三方比对。灰度释放阶段会在查询网关里开启双...

answer:

```text
根据当前的知识库内容，无法找到关于 MySQL InnoDB 的 MVCC undo log 版本链实现的信息。知识库中提供的资料仅涉及“归档执行过程”的步骤描述（预扫描、冻结快照等），与您询问的数据库内部机制无关。如果需要解答这个问题，建议补充相关的知识内容。
```

### RAG-07 - weak_related

- question: 根据知识库，Grafana dashboard 变量模板怎么配置？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `13185`
- score_mode: `manual`
- expected_points:
  - 知识库未涉及 dashboard 变量模板
  - 无法基于当前知识库回答

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0471 .. 0.0489`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2363` / max `6000`
  - context_salience: cues `formula \| judgement \| procedure \| range \| parameter \| example` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `根据知识库，Grafana dashboard 变量模板怎么配置？，<br>Grafana dashboard variable template configuration syntax<br>Grafana dashboard template variables setup steps example`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2386` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `根据知识库，Grafana dashboard 变量模板怎么配置？， \| Grafana dashboard variable template configuration syntax \| Grafana dashboard template variables setup steps example`
  - profile_hints: version `hybrid-v2` / source `AUTO_PROFILE` / selected `模板 \| grafana/list_datasources \| grafana/query_prometheus \| Grafana MCP \| Grafana MCP工具 \| 如何列出Grafana数据源？ \| 文档未说明Grafana版本或其他类型数据源 \| 1. grafana/list_datasources`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6726`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据知识库，Grafana dashboard 变量模板怎么配置？，"},{"index":2,"rank":1,"text":"Grafana dashboard variable template configuration syntax"},{"index":3,"rank":1,"text":"Grafana dashboard template variables setup steps example"}]`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #2 score=`0.0484`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6648`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据知识库，Grafana dashboard 变量模板怎么配置？，"},{"index":2,"rank":2,"text":"Grafana dashboard variable template configuration syntax"},{"index":3,"rank":2,"text":"Grafana dashboard template variables setup steps example"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #3 score=`0.0474`, source=`grafana-mcp-tools-guide.md`, chunk=`8`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6464`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0474`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"根据知识库，Grafana dashboard 变量模板怎么配置？，"},{"index":2,"rank":3,"text":"Grafana dashboard variable template configuration syntax"},{"index":3,"rank":3,"text":"Grafana dashboard template variables setup steps example"}]`
      - preview: ### 常见错误 1. **数据源连接失败** - 检查Prometheus服务状态 - 验证网络连接 - 确认认证配置 2. **查询语法错误** - 验证PromQL语法 - 检查指标名称拼写 - 确认标签选择器 3. **数据缺失** - 检查时间范围设置 - 验证指标采集状态 - 确认数据保留策略 ### 故障排查步骤 1. **验证数据源** ``...
    - #4 score=`0.0471`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6647`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0471`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据知识库，Grafana dashboard 变量模板怎么配置？，"},{"index":2,"rank":4,"text":"Grafana dashboard variable template configuration syntax"},{"index":3,"rank":4,"text":"Grafana dashboard template variables setup steps example"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
  - documents:
    - #1 score=`0.0489`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6647`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`1`, rerankScore=`0.0489`, rerankFusionScore=`0.0489`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0471`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"根据知识库，Grafana dashboard 变量模板怎么配置？，"},{"index":2,"rank":4,"text":"Grafana dashboard variable template configuration syntax"},{"index":3,"rank":4,"text":"Grafana dashboard template variables setup steps example"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #2 score=`0.0484`, source=`grafana-mcp-tools-guide.md`, chunk=`8`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6464`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":2},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0474`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"根据知识库，Grafana dashboard 变量模板怎么配置？，"},{"index":2,"rank":3,"text":"Grafana dashboard variable template configuration syntax"},{"index":3,"rank":3,"text":"Grafana dashboard template variables setup steps example"}]`
      - preview: ### 常见错误 1. **数据源连接失败** - 检查Prometheus服务状态 - 验证网络连接 - 确认认证配置 2. **查询语法错误** - 验证PromQL语法 - 检查指标名称拼写 - 确认标签选择器 3. **数据缺失** - 检查时间范围设置 - 验证指标采集状态 - 确认数据保留策略 ### 故障排查步骤 1. **验证数据源** ``...
    - #3 score=`0.0476`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6726`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":3},{"index":3,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"根据知识库，Grafana dashboard 变量模板怎么配置？，"},{"index":2,"rank":1,"text":"Grafana dashboard variable template configuration syntax"},{"index":3,"rank":1,"text":"Grafana dashboard template variables setup steps example"}]`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #4 score=`0.0471`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6648`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`4`, rerankScore=`0.0471`, rerankFusionScore=`0.0471`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`根据知识库，Grafana dashboard 变量模板怎么配置？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"根据知识库，Grafana dashboard 变量模板怎么配置？，"},{"index":2,"rank":2,"text":"Grafana dashboard variable template configuration syntax"},{"index":3,"rank":2,"text":"Grafana dashboard template variables setup steps example"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...

answer:

```text
根据现有的知识库内容，没有提供关于 Grafana dashboard 变量模板配置的信息。知识库主要介绍的是 Grafana MCP 工具（如 `list_datasources`、`query_prometheus`）的用法、PromQL 查询示例和故障排查步骤，并未涉及 dashboard 变量模板的配置方法。

建议你查阅 Grafana 官方文档中关于“变量（Variables）”和“仪表板模板（Dashboard templates）”的章节，那里会有详细的配置说明。如果你需要，我也可以基于一般知识为你介绍配置变量的基本步骤。
```

### RAG-08 - weak_related

- question: 根据知识库，Alertmanager 的 silence 应该怎么创建？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `11603`
- score_mode: `manual`
- expected_points:
  - 知识库未涉及 Alertmanager silence
  - 无法基于当前知识库回答

retrieval:
- event #1
  - retrieved_document_count: `1`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0492 .. 0.0492`
  - context_selected: `1` / dropped: `0` / truncated: `false`
  - context_chars: actual `624` / max `6000`
  - context_salience: cues `formula \| range \| procedure \| parameter \| example` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `1` / final `1`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `根据知识库，Alertmanager 的 silence 应该怎么创建？，<br>Alertmanager silence 创建 matchers duration comment created_by API<br>Alertmanager silence 创建步骤 配置示例 过期时间 匹配规则 限制`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3138` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `根据知识库，Alertmanager 的 silence 应该怎么创建？， \| Alertmanager silence 创建 matchers duration comment created_by API \| Alertmanager silence 创建步骤 配置示例 过期时间 匹配规则 限制`
  - profile_hints: version `hybrid-v2` / source `AUTO_PROFILE` / selected `该文档不是正式业务规范 不能作为生产依据`
  - pre_rerank_documents:
    - #1 score=`0.0164`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6370`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`1`, queryVariantText=`Alertmanager silence 创建 matchers duration comment created_by API`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":1,"text":"Alertmanager silence 创建 matchers duration comment created_by API"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
  - documents:
    - #1 score=`0.0492`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6370`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_RESULTS`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`2`, queryVariantRank=`1`, queryVariantText=`Alertmanager silence 创建 matchers duration comment created_by API`, queryFusionScore=`0.0164`, queryFusionRank=`1`, queryVariantHitCount=`1`, bestQueryVariantRank=`1`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":1,"text":"Alertmanager silence 创建 matchers duration comment created_by API"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...

answer:

```text
根据提供的知识库内容，没有与 Alertmanager 的 silence 创建方式相关的信息。知识库主要介绍了 Grafana 中 Prometheus 查询的用法和示例，并未涉及 Alertmanager 配置或 silence 的管理操作，因此我无法回答您的问题。
```

### RAG-09 - paraphrase

- question: 我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？
- should_answer: `true`
- expected_source_section: 标准监控分析流程 / 数据源检查
- completed: `true`
- duration_ms: `16534`
- score_mode: `manual`
- expected_points:
  - grafana/list_datasources
  - 确认监控数据来源
  - 数据源检查

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0469 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `3005` / max `6000`
  - context_salience: cues `formula \| procedure \| parameter \| range \| judgement` / expansions `1`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `5` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，<br>Grafana MCP 工具 list_datasources 数据源 监控数据来源<br>调用 Grafana MCP 工具查看数据源 步骤 示例`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2263` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？， \| Grafana MCP 工具 list_datasources 数据源 监控数据来源 \| 调用 Grafana MCP 工具查看数据源 步骤 示例`
  - profile_hints: version `hybrid-v2` / source `AUTO_PROFILE` / selected `Grafana MCP \| MCP \| Grafana MCP 工具使用指南 \| Grafana MCP工具 \| 如何列出Grafana数据源？ \| 文档未说明Grafana版本或其他类型数据源 \| 列出所有可用的数据源 \| grafana/list_datasources`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.8186`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，"},{"index":2,"rank":1,"text":"Grafana MCP 工具 list_datasources 数据源 监控数据来源"},{"index":3,"rank":1,"text":"调用 Grafana MCP 工具查看数据源 步骤 示例"}]`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #2 score=`0.0484`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6867`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，"},{"index":2,"rank":2,"text":"Grafana MCP 工具 list_datasources 数据源 监控数据来源"},{"index":3,"rank":2,"text":"调用 Grafana MCP 工具查看数据源 步骤 示例"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #3 score=`0.0474`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6810`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，`, queryFusionScore=`0.0474`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，"},{"index":2,"rank":3,"text":"Grafana MCP 工具 list_datasources 数据源 监控数据来源"},{"index":3,"rank":3,"text":"调用 Grafana MCP 工具查看数据源 步骤 示例"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #4 score=`0.0315`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6820`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，`, queryFusionScore=`0.0315`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":3,"text":"我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，"},{"index":2,"rank":4,"text":"Grafana MCP 工具 list_datasources 数据源 监控数据来源"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #5 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`8`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6564`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`调用 Grafana MCP 工具查看数据源 步骤 示例`, queryFusionScore=`0.0156`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"调用 Grafana MCP 工具查看数据源 步骤 示例"}]`
      - preview: ### 常见错误 1. **数据源连接失败** - 检查Prometheus服务状态 - 验证网络连接 - 确认认证配置 2. **查询语法错误** - 验证PromQL语法 - 检查指标名称拼写 - 确认标签选择器 3. **数据缺失** - 检查时间范围设置 - 验证指标采集状态 - 确认数据保留策略 ### 故障排查步骤 1. **验证数据源** ``...
  - documents:
    - #1 score=`0.0492`, source=`grafana-mcp-tools-guide.md`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.8186`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，"},{"index":2,"rank":1,"text":"Grafana MCP 工具 list_datasources 数据源 监控数据来源"},{"index":3,"rank":1,"text":"调用 Grafana MCP 工具查看数据源 步骤 示例"}]`
      - preview: # Grafana MCP 工具使用指南 ## 概述 本文档详细介绍了如何使用Grafana MCP工具进行自动化监控分析，包括工具调用方法、参数配置和实际应用场景。 ## 可用的MCP工具 ### 1. grafana/list_datasources 列出所有可用的数据源 #### 功能描述 - 获取Grafana中配置的所有数据源 - 返回数据源的基本...
    - #2 score=`0.0484`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6820`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，`, queryFusionScore=`0.0315`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":3,"text":"我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，"},{"index":2,"rank":4,"text":"Grafana MCP 工具 list_datasources 数据源 监控数据来源"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #3 score=`0.0474`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6867`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`3`, rerankScore=`0.0474`, rerankFusionScore=`0.0474`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":4},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"我想先确认监控数据来源，应该调用哪个 Grafana MCP 工具？，"},{"index":2,"rank":2,"text":"Grafana MCP 工具 list_datasources 数据源 监控数据来源"},{"index":3,"rank":2,"text":"调用 Grafana MCP 工具查看数据源 步骤 示例"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #4 score=`0.0469`, source=`grafana-mcp-tools-guide.md`, chunk=`8`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6564`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`5`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":5},{"index":2,"rank":3},{"index":3,"rank":4}]`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`调用 Grafana MCP 工具查看数据源 步骤 示例`, queryFusionScore=`0.0156`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"调用 Grafana MCP 工具查看数据源 步骤 示例"}]`
      - preview: ### 常见错误 1. **数据源连接失败** - 检查Prometheus服务状态 - 验证网络连接 - 确认认证配置 2. **查询语法错误** - 验证PromQL语法 - 检查指标名称拼写 - 确认标签选择器 3. **数据缺失** - 检查时间范围设置 - 验证指标采集状态 - 确认数据保留策略 ### 故障排查步骤 1. **验证数据源** ``...

answer:

```text
要确认监控数据的来源，您应该调用 **`grafana/list_datasources`** 工具[1]。

该工具会列出 Grafana 中配置的所有数据源，返回每个数据源的 ID、名称、类型和 URL 等信息，帮助您确认 Prometheus 或其他监控数据源的可用性与基本配置[1][2]。根据标准自动化分析流程，这也是进行任何监控查询之前的第一步，用于验证数据源是否正常连接[3]。
```

### RAG-10 - paraphrase

- question: 如果我要查内存压力，知识库给出的查询思路是什么？
- should_answer: `true`
- expected_source_section: 内存使用率查询 / 内存数据解释
- completed: `true`
- duration_ms: `30266`
- score_mode: `literal`
- expected_points:
  - [x] node_memory_MemAvailable_bytes
  - [x] node_memory_MemTotal_bytes
  - [ ] 正常范围
  - [ ] 警告范围
  - [ ] 危险范围

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0469 .. 0.0489`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `2552` / max `6000`
  - context_salience: cues `formula \| judgement \| procedure \| range \| parameter \| example` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `8` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `如果我要查内存压力，知识库给出的查询思路是什么？，<br>内存压力 memory_pressure memory_available memory_usage 指标<br>内存压力 查询思路 判断标准 阈值 七天检查窗口 步骤`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3206` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `如果我要查内存压力，知识库给出的查询思路是什么？， \| 内存压力 memory_pressure memory_available memory_usage 指标 \| 内存压力 查询思路 判断标准 阈值 七天检查窗口 步骤`
  - profile_hints: version `hybrid-v2` / source `AUTO_PROFILE` / selected `node_memory_MemAvailable_bytes \| node_memory_MemTotal_bytes \| memory_available \| memory_status \| memory_trend \| memory_usage \| 内存使用率查询 \| 七天检查窗口的作用是什么？`
  - pre_rerank_documents:
    - #1 score=`0.0479`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6819`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0479`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"},{"index":2,"rank":5,"text":"内存压力 memory_pressure memory_available memory_usage 指标"},{"index":3,"rank":2,"text":"内存压力 查询思路 判断标准 阈值 七天检查窗口 步骤"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #2 score=`0.0323`, source=`grafana-mcp-tools-guide.md`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6692`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`3`, queryVariantText=`内存压力 memory_pressure memory_available memory_usage 指标`, queryFusionScore=`0.0323`, queryFusionRank=`2`, queryVariantHitCount=`2`, bestQueryVariantRank=`1`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":3,"text":"内存压力 memory_pressure memory_available memory_usage 指标"},{"index":3,"rank":1,"text":"内存压力 查询思路 判断标准 阈值 七天检查窗口 步骤"}]`
      - preview: # 磁盘使用增长趋势 predict_linear(node_filesystem_avail_bytes{fstype!="tmpfs"}[24h], 7*24*3600) ``` ## 数据解释和分析标准 ### CPU数据解释 - **数值范围**: 0-100% - **正常范围**: 0-70% - **警告范围**: 70-90% - **危险范...
    - #3 score=`0.0320`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6684`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0320`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":4,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"},{"index":2,"rank":1,"text":"内存压力 memory_pressure memory_available memory_usage 指标"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...
    - #4 score=`0.0320`, source=`grafana-mcp-tools-guide.md`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6741`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0320`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"},{"index":3,"rank":3,"text":"内存压力 查询思路 判断标准 阈值 七天检查窗口 步骤"}]`
      - preview: **网络分析** ``` grafana/query_prometheus query: irate(node_network_receive_bytes_total{device!="lo"}[1h]) * 8 ``` - 查询网络流量状况 - 分析网络性能指标 ### 深度分析流程 #### 性能瓶颈分析 1. **识别高负载时段** ```promql...
    - #5 score=`0.0161`, source=`grafana-mcp-tools-guide.md`, chunk=`7`, retrievalSource=`KEYWORD`, rrfScore=`0.0164`, vectorRank=`—`, vectorScore=`—`, keywordRank=`1`, keywordScore=`0.2000`, keywordSkippedReason=`—`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`2`, queryVariantText=`内存压力 memory_pressure memory_available memory_usage 指标`, queryFusionScore=`0.0161`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":2,"text":"内存压力 memory_pressure memory_available memory_usage 指标"}]`
      - preview: - **当前使用率**: {memory_usage}% - **可用内存**: {memory_available}GB - **状态评估**: {memory_status} - **使用趋势**: {memory_trend} ### 💿 磁盘使用情况 - **使用率**: {disk_usage}% - **可用空间**: {disk_availab...
    - #6 score=`0.0159`, source=`grafana-mcp-tools-guide.md`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6692`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`6`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0159`, queryFusionRank=`6`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"}]`
      - preview: query: node_cpu_seconds_total start: now-1h end: now ``` ## 最佳实践 1. **查询优化** - 使用适当的时间范围 - 避免过于复杂的查询 - 合理使用聚合函数 2. **数据解释** - 结合业务场景分析 - 考虑历史基线对比 - 关注趋势而非瞬时值 3. **报告生成** - 提供清晰的状态评...
    - #7 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6542`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`7`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`内存压力 memory_pressure memory_available memory_usage 指标`, queryFusionScore=`0.0156`, queryFusionRank=`7`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":4,"text":"内存压力 memory_pressure memory_available memory_usage 指标"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #8 score=`0.0156`, source=`grafana-mcp-tools-guide.md`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6336`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`8`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`内存压力 查询思路 判断标准 阈值 七天检查窗口 步骤`, queryFusionScore=`0.0156`, queryFusionRank=`8`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"内存压力 查询思路 判断标准 阈值 七天检查窗口 步骤"}]`
      - preview: - **危险范围**: 95-100% ### 网络数据解释 - **单位**: bps (bits per second) - **正常范围**: 根据网络带宽确定 - **异常指标**: 突然的流量峰值或持续的高流量 ## 报告生成模板 ### 系统健康报告模板 ```markdown ## 系统运行状态分析报告 ### 📊 监控数据概览 - **监控时...
  - documents:
    - #1 score=`0.0489`, source=`grafana-mcp-tools-guide.md`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.6819`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0489`, rerankFusionScore=`0.0489`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":2},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0479`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"},{"index":2,"rank":5,"text":"内存压力 memory_pressure memory_available memory_usage 指标"},{"index":3,"rank":2,"text":"内存压力 查询思路 判断标准 阈值 七天检查窗口 步骤"}]`
      - preview: - 确认Prometheus数据源可用 - 获取数据源配置信息 2. **CPU分析** ``` grafana/query_prometheus query: 100 - (avg(irate(node_cpu_seconds_total{mode="idle"}[1h])) * 100) ``` - 查询过去1小时的CPU使用率 - 分析CPU负载趋势 ...
    - #2 score=`0.0474`, source=`grafana-mcp-tools-guide.md`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6542`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`—`, beforeRerankRank=`7`, rerankRank=`2`, rerankScore=`0.0474`, rerankFusionScore=`0.0474`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":5},{"index":3,"rank":3}]`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`内存压力 memory_pressure memory_available memory_usage 指标`, queryFusionScore=`0.0156`, queryFusionRank=`7`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":4,"text":"内存压力 memory_pressure memory_available memory_usage 指标"}]`
      - preview: grafana/query_prometheus 执行Prometheus查询 #### 功能描述 - 执行PromQL查询语句 - 支持时间范围查询 - 返回结构化的监控数据 #### 参数说明 - `query`: PromQL查询语句 - `start`: 查询开始时间（可选） - `end`: 查询结束时间（可选） - `step`: 查询步长（可选...
    - #3 score=`0.0472`, source=`grafana-mcp-tools-guide.md`, chunk=`7`, retrievalSource=`KEYWORD`, rrfScore=`0.0164`, vectorRank=`—`, vectorScore=`—`, keywordRank=`1`, keywordScore=`0.2000`, keywordSkippedReason=`—`, beforeRerankRank=`5`, rerankRank=`3`, rerankScore=`0.0472`, rerankFusionScore=`0.0472`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":1},{"index":3,"rank":7}]`, queryVariantIndex=`2`, queryVariantRank=`2`, queryVariantText=`内存压力 memory_pressure memory_available memory_usage 指标`, queryFusionScore=`0.0161`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":2,"text":"内存压力 memory_pressure memory_available memory_usage 指标"}]`
      - preview: - **当前使用率**: {memory_usage}% - **可用内存**: {memory_available}GB - **状态评估**: {memory_status} - **使用趋势**: {memory_trend} ### 💿 磁盘使用情况 - **使用率**: {disk_usage}% - **可用空间**: {disk_availab...
    - #4 score=`0.0469`, source=`grafana-mcp-tools-guide.md`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6684`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":6},{"index":2,"rank":4},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`如果我要查内存压力，知识库给出的查询思路是什么？，`, queryFusionScore=`0.0320`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":4,"text":"如果我要查内存压力，知识库给出的查询思路是什么？，"},{"index":2,"rank":1,"text":"内存压力 memory_pressure memory_available memory_usage 指标"}]`
      - preview: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024 ``` ##### 磁盘使用率查询 ```promql # 磁盘使用率 (1 - (node_filesystem_avail_bytes{fstype!="tmpfs"} / node_fil...

answer:

```text
根据知识库，查询内存压力的思路主要分为以下几步：

1. **使用 PromQL 查询内存使用率**[1][2]
   通过 `grafana/query_prometheus` 工具执行查询，PromQL 为：
   ```promql
   (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
   ```
   该查询直接得出当前内存使用百分比，是评估内存压力的核心指标。

2. **（可选）获取内存绝对使用量（GB）**[4]
   如需更直观的用量数据，可补充查询：
   ```promql
   (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / 1024 / 1024 / 1024
   ```
   结果以 GB 为单位，便于快速感知消耗量。

3. **结合参数调整查询范围**[2]
   在调用 `query_prometheus` 时，可按需设置 `start`、`end`、`step` 等参数，观察内存压力随时间的变化趋势，而非仅看单点值。

通过上述查询，即可完成对内存压力状况的基础评估。
```

### RAG-11 - control_plaintext_cn_answerable

- question: 控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？
- should_answer: `true`
- expected_source_section: control-cn-low-newline.txt / 容量治理控制组
- completed: `true`
- duration_ms: `14146`
- score_mode: `literal`
- expected_points:
  - [x] 连续二十一天未访问
  - [x] ARCHIVE-CN-7319

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0469 .. 0.0492`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `1161` / max `6000`
  - context_salience: cues `range \| judgement \| procedure` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `5` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，<br>控制组 cold data migration threshold 参数名 归档窗口编号 archive window ID<br>控制组 冷数据迁移阈值 配置步骤 判断标准 归档窗口编号 示例 限制`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2406` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？， \| 控制组 cold data migration threshold 参数名 归档窗口编号 archive window ID \| 控制组 冷数据迁移阈值 配置步骤 判断标准 归档窗口编号 示例 限制`
  - profile_hints: version `hybrid-v2` / source `AUTO_PROFILE` / selected `阈值 \| 容量治理控制组文档 \| 七天检查窗口的作用是什么？ \| 文档未说明Grafana版本或其他类型数据源 \| 该文档不是正式业务规范 不能作为生产依据 \| 数据解释 \| 数据解释和分析标准 \| 列出所有可用的数据源`
  - pre_rerank_documents:
    - #1 score=`0.0489`, source=`control-cn-low-newline.txt`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7763`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，"},{"index":2,"rank":2,"text":"控制组 cold data migration threshold 参数名 归档窗口编号 archive window ID"},{"index":3,"rank":1,"text":"控制组 冷数据迁移阈值 配置步骤 判断标准 归档窗口编号 示例 限制"}]`
      - preview: �过移动。若某条明细已经进入候选池但还没有搬迁，用户查询仍然走主库。若某条明细已经搬迁且摘要索引校验通过，用户查询可以走归档读路径。若摘要索引存在但正文块缺失，系统必须返回内部错误并触发隔离，而不是把问题解释成知识不存在。控制组还刻意加入一个容易误读的术语：容量观察期。容量观察期用于评估增长趋势，长度为十天，但它不参与冷数据迁移判断。冷数据迁移阈值仍然是连续...
    - #2 score=`0.0481`, source=`control-cn-low-newline.txt`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7298`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，`, queryFusionScore=`0.0481`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，"},{"index":2,"rank":3,"text":"控制组 cold data migration threshold 参数名 归档窗口编号 archive window ID"},{"index":3,"rank":2,"text":"控制组 冷数据迁移阈值 配置步骤 判断标准 归档窗口编号 示例 限制"}]`
      - preview: 过异步索引回填，冷数据只保留查询摘要和审计索引。这里的冷数据迁移阈值被明确写成：连续二十一天未访问。这个阈值只用于判断明细是否进入候选池，不等于删除条件，也不等于归档任务立即执行。为了避免误判，系统还会检查最近七天是否存在人工置顶、账单核对、客服补单、风控复查等保护标记。七天检查窗口只是安全保护项，不是冷数据迁移阈值。另一个常见误解是把十四天低频访问当成归档...
    - #3 score=`0.0479`, source=`control-cn-low-newline.txt`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7277`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，`, queryFusionScore=`0.0479`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，"},{"index":2,"rank":1,"text":"控制组 cold data migration threshold 参数名 归档窗口编号 archive window ID"},{"index":3,"rank":4,"text":"控制组 冷数据迁移阈值 配置步骤 判断标准 归档窗口编号 示例 限制"}]`
      - preview: �队列之前，调度器还会读取业务线的保留策略，确认租户等级、合规标签、冻结状态和最近一次补偿任务结果。为了便于回放，本次控制组把归档窗口编号固定为 ARCHIVE-CN-7319。这个编号应该出现在检索答案中，因为它是文档里唯一一个用于识别迁移窗口的字符串。窗口编号不是审批单号，也不是工单编号。文档没有指定审批人姓名，没有给出审批系统地址，也没有要求用户在回答...
    - #4 score=`0.0313`, source=`control-cn-low-newline.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7175`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，`, queryFusionScore=`0.0313`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":4,"text":"控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，"},{"index":2,"rank":4,"text":"控制组 cold data migration threshold 参数名 归档窗口编号 archive window ID"}]`
      - preview: 弱相关问题更隐蔽，因为用户可能问审批人、成本中心、云厂商、负责人电话、紧急群组这些文档未覆盖的信息。控制组要求系统在这些问题上明确说明文档未提供，不应编造人名、部门、平台名称或外部链接。本文档只说明容量治理策略、归档窗口编号、冷数据迁移阈值、单批提交上限、隔离队列重放次数和回滚观察期。本文档没有说明审批人是谁，没有说明云厂商是谁，没有说明预算归属，也没有说明...
    - #5 score=`0.0159`, source=`control-cn-low-newline.txt`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6835`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`3`, queryVariantRank=`3`, queryVariantText=`控制组 冷数据迁移阈值 配置步骤 判断标准 归档窗口编号 示例 限制`, queryFusionScore=`0.0159`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":3,"text":"控制组 冷数据迁移阈值 配置步骤 判断标准 归档窗口编号 示例 限制"}]`
      - preview: 归档执行过程分为预扫描、冻结快照、分批搬迁、校验回填和灰度释放五段。预扫描阶段只读取元数据，不移动正文。冻结快照阶段会记录候选明细的主键范围、最近更新时间、摘要哈希和原始分区。分批搬迁阶段按租户和业务类型做小批量提交，每批上限为三千条。三千条是单次提交上限，不是迁移阈值。校验回填阶段会对摘要索引、审计索引和归档位置做三方比对。灰度释放阶段会在查询网关里开启双...
  - documents:
    - #1 score=`0.0492`, source=`control-cn-low-newline.txt`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7763`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0492`, rerankFusionScore=`0.0492`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，`, queryFusionScore=`0.0489`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，"},{"index":2,"rank":2,"text":"控制组 cold data migration threshold 参数名 归档窗口编号 archive window ID"},{"index":3,"rank":1,"text":"控制组 冷数据迁移阈值 配置步骤 判断标准 归档窗口编号 示例 限制"}]`
      - preview: �过移动。若某条明细已经进入候选池但还没有搬迁，用户查询仍然走主库。若某条明细已经搬迁且摘要索引校验通过，用户查询可以走归档读路径。若摘要索引存在但正文块缺失，系统必须返回内部错误并触发隔离，而不是把问题解释成知识不存在。控制组还刻意加入一个容易误读的术语：容量观察期。容量观察期用于评估增长趋势，长度为十天，但它不参与冷数据迁移判断。冷数据迁移阈值仍然是连续...
    - #2 score=`0.0481`, source=`control-cn-low-newline.txt`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7277`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`0.0481`, rerankFusionScore=`0.0481`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，`, queryFusionScore=`0.0479`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，"},{"index":2,"rank":1,"text":"控制组 cold data migration threshold 参数名 归档窗口编号 archive window ID"},{"index":3,"rank":4,"text":"控制组 冷数据迁移阈值 配置步骤 判断标准 归档窗口编号 示例 限制"}]`
      - preview: �队列之前，调度器还会读取业务线的保留策略，确认租户等级、合规标签、冻结状态和最近一次补偿任务结果。为了便于回放，本次控制组把归档窗口编号固定为 ARCHIVE-CN-7319。这个编号应该出现在检索答案中，因为它是文档里唯一一个用于识别迁移窗口的字符串。窗口编号不是审批单号，也不是工单编号。文档没有指定审批人姓名，没有给出审批系统地址，也没有要求用户在回答...
    - #3 score=`0.0479`, source=`control-cn-low-newline.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7175`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`3`, rerankScore=`0.0479`, rerankFusionScore=`0.0479`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，`, queryFusionScore=`0.0313`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`4`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":4,"text":"控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，"},{"index":2,"rank":4,"text":"控制组 cold data migration threshold 参数名 归档窗口编号 archive window ID"}]`
      - preview: 弱相关问题更隐蔽，因为用户可能问审批人、成本中心、云厂商、负责人电话、紧急群组这些文档未覆盖的信息。控制组要求系统在这些问题上明确说明文档未提供，不应编造人名、部门、平台名称或外部链接。本文档只说明容量治理策略、归档窗口编号、冷数据迁移阈值、单批提交上限、隔离队列重放次数和回滚观察期。本文档没有说明审批人是谁，没有说明云厂商是谁，没有说明预算归属，也没有说明...
    - #4 score=`0.0469`, source=`control-cn-low-newline.txt`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7298`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，`, queryFusionScore=`0.0481`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"控制组文档里冷数据迁移阈值是什么？归档窗口编号是什么？，"},{"index":2,"rank":3,"text":"控制组 cold data migration threshold 参数名 归档窗口编号 archive window ID"},{"index":3,"rank":2,"text":"控制组 冷数据迁移阈值 配置步骤 判断标准 归档窗口编号 示例 限制"}]`
      - preview: 过异步索引回填，冷数据只保留查询摘要和审计索引。这里的冷数据迁移阈值被明确写成：连续二十一天未访问。这个阈值只用于判断明细是否进入候选池，不等于删除条件，也不等于归档任务立即执行。为了避免误判，系统还会检查最近七天是否存在人工置顶、账单核对、客服补单、风控复查等保护标记。七天检查窗口只是安全保护项，不是冷数据迁移阈值。另一个常见误解是把十四天低频访问当成归档...

answer:

```text
根据控制组文档：

*   **冷数据迁移阈值**：是**连续二十一天未访问**[1][4]。这个阈值用于判断明细是否进入候选池，不等于删除条件或归档任务立即执行[4]。文档中还特别指出，七天检查窗口是安全保护项，而十四天低频访问只会触发容量预警，这两者都不是冷数据迁移阈值[4]。
*   **归档窗口编号**：是 **ARCHIVE-CN-7319**[1][2]。这是文档中唯一用于识别迁移窗口的字符串，不是审批单号，也不是工单编号[2]。
```

### RAG-12 - control_plaintext_cn_weak_related

- question: 控制组文档指定的归档审批人是谁？
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `14003`
- score_mode: `manual`
- expected_points:
  - 文档没有指定审批人
  - 无法基于当前知识库回答

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0161 .. 0.0486`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `1155` / max `6000`
  - context_salience: cues `range \| procedure` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `6` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `控制组文档指定的归档审批人是谁？，<br>capacity governance control group document archival approver role definition<br>控制组 归档审批人配置项 角色分配 审批流程`
  - coverage_guard: applied `true` / added `1`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `3180` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `控制组文档指定的归档审批人是谁？， \| capacity governance control group document archival approver role definition \| 控制组 归档审批人配置项 角色分配 审批流程`
  - profile_hints: version `hybrid-v2` / source `AUTO_PROFILE` / selected `容量治理控制组文档 \| 容量治理文档 \| 文档未说明Grafana版本或其他类型数据源 \| 该文档不是正式业务规范 不能作为生产依据 \| 该文档是实验材料 不包含正式标准`
  - pre_rerank_documents:
    - #1 score=`0.0486`, source=`control-cn-low-newline.txt`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7169`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`控制组文档指定的归档审批人是谁？，`, queryFusionScore=`0.0486`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"控制组文档指定的归档审批人是谁？，"},{"index":2,"rank":3,"text":"capacity governance control group document archival approver role definition"},{"index":3,"rank":1,"text":"控制组 归档审批人配置项 角色分配 审批流程"}]`
      - preview: �队列之前，调度器还会读取业务线的保留策略，确认租户等级、合规标签、冻结状态和最近一次补偿任务结果。为了便于回放，本次控制组把归档窗口编号固定为 ARCHIVE-CN-7319。这个编号应该出现在检索答案中，因为它是文档里唯一一个用于识别迁移窗口的字符串。窗口编号不是审批单号，也不是工单编号。文档没有指定审批人姓名，没有给出审批系统地址，也没有要求用户在回答...
    - #2 score=`0.0481`, source=`control-cn-low-newline.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6741`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`控制组文档指定的归档审批人是谁？，`, queryFusionScore=`0.0481`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"控制组文档指定的归档审批人是谁？，"},{"index":2,"rank":1,"text":"capacity governance control group document archival approver role definition"},{"index":3,"rank":4,"text":"控制组 归档审批人配置项 角色分配 审批流程"}]`
      - preview: 弱相关问题更隐蔽，因为用户可能问审批人、成本中心、云厂商、负责人电话、紧急群组这些文档未覆盖的信息。控制组要求系统在这些问题上明确说明文档未提供，不应编造人名、部门、平台名称或外部链接。本文档只说明容量治理策略、归档窗口编号、冷数据迁移阈值、单批提交上限、隔离队列重放次数和回滚观察期。本文档没有说明审批人是谁，没有说明云厂商是谁，没有说明预算归属，也没有说明...
    - #3 score=`0.0318`, source=`control-cn-low-newline.txt`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6475`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`控制组文档指定的归档审批人是谁？，`, queryFusionScore=`0.0318`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"控制组文档指定的归档审批人是谁？，"},{"index":3,"rank":2,"text":"控制组 归档审批人配置项 角色分配 审批流程"}]`
      - preview: 归档执行过程分为预扫描、冻结快照、分批搬迁、校验回填和灰度释放五段。预扫描阶段只读取元数据，不移动正文。冻结快照阶段会记录候选明细的主键范围、最近更新时间、摘要哈希和原始分区。分批搬迁阶段按租户和业务类型做小批量提交，每批上限为三千条。三千条是单次提交上限，不是迁移阈值。校验回填阶段会对摘要索引、审计索引和归档位置做三方比对。灰度释放阶段会在查询网关里开启双...
    - #4 score=`0.0317`, source=`control-cn-low-newline.txt`, chunk=`9`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.6687`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`控制组文档指定的归档审批人是谁？，`, queryFusionScore=`0.0317`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"控制组文档指定的归档审批人是谁？，"},{"index":3,"rank":3,"text":"控制组 归档审批人配置项 角色分配 审批流程"}]`
      - preview: �过移动。若某条明细已经进入候选池但还没有搬迁，用户查询仍然走主库。若某条明细已经搬迁且摘要索引校验通过，用户查询可以走归档读路径。若摘要索引存在但正文块缺失，系统必须返回内部错误并触发隔离，而不是把问题解释成知识不存在。控制组还刻意加入一个容易误读的术语：容量观察期。容量观察期用于评估增长趋势，长度为十天，但它不参与冷数据迁移判断。冷数据迁移阈值仍然是连续...
    - #5 score=`0.0161`, source=`control-cn-low-newline.txt`, chunk=`8`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6584`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`2`, queryVariantText=`capacity governance control group document archival approver role definition`, queryFusionScore=`0.0161`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":2,"text":"capacity governance control group document archival approver role definition"}]`
      - preview: 为了进一步拉长低换行场景，本文档继续补充一段只服务于检索控制的说明。容量治理系统不会因为一次查询失败就移动数据，也不会因为磁盘水位超过百分之八十就立即归档正文。磁盘水位超过百分之八十只会触发容量看板的黄色提示。磁盘水位超过百分之九十会触发值班提醒，但仍然不能绕过冷数据迁移阈值。若某租户最近存在争议工单，归档器会暂停该租户候选池的提交，直到争议状态消失。若某业...
    - #6 score=`0.0156`, source=`control-cn-low-newline.txt`, chunk=`10`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6462`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`6`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`capacity governance control group document archival approver role definition`, queryFusionScore=`0.0156`, queryFusionRank=`6`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":4,"text":"capacity governance control group document archival approver role definition"}]`
      - preview: 是四十五分钟。文档没有给出审批人，没有给出云厂商，没有给出成本中心，没有给出负责人电话。这个重复声明不是业务写法，而是为了让评测在不同 chunk 命中情况下仍能看到正例和反例，并观察模型是否把相邻但不同含义的数字混在一起。最后再补一个边界说明：若用户只问容量治理是否支持人工复核，文档可以回答支持人工复核；若用户进一步追问复核人是谁，文档仍然没有答案。若用户...
  - documents:
    - #1 score=`0.0486`, source=`control-cn-low-newline.txt`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7169`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`1`, rerankScore=`0.0486`, rerankFusionScore=`0.0486`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":3},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`控制组文档指定的归档审批人是谁？，`, queryFusionScore=`0.0486`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"控制组文档指定的归档审批人是谁？，"},{"index":2,"rank":3,"text":"capacity governance control group document archival approver role definition"},{"index":3,"rank":1,"text":"控制组 归档审批人配置项 角色分配 审批流程"}]`
      - preview: �队列之前，调度器还会读取业务线的保留策略，确认租户等级、合规标签、冻结状态和最近一次补偿任务结果。为了便于回放，本次控制组把归档窗口编号固定为 ARCHIVE-CN-7319。这个编号应该出现在检索答案中，因为它是文档里唯一一个用于识别迁移窗口的字符串。窗口编号不是审批单号，也不是工单编号。文档没有指定审批人姓名，没有给出审批系统地址，也没有要求用户在回答...
    - #2 score=`0.0484`, source=`control-cn-low-newline.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6741`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`控制组文档指定的归档审批人是谁？，`, queryFusionScore=`0.0481`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"控制组文档指定的归档审批人是谁？，"},{"index":2,"rank":1,"text":"capacity governance control group document archival approver role definition"},{"index":3,"rank":4,"text":"控制组 归档审批人配置项 角色分配 审批流程"}]`
      - preview: 弱相关问题更隐蔽，因为用户可能问审批人、成本中心、云厂商、负责人电话、紧急群组这些文档未覆盖的信息。控制组要求系统在这些问题上明确说明文档未提供，不应编造人名、部门、平台名称或外部链接。本文档只说明容量治理策略、归档窗口编号、冷数据迁移阈值、单批提交上限、隔离队列重放次数和回滚观察期。本文档没有说明审批人是谁，没有说明云厂商是谁，没有说明预算归属，也没有说明...
    - #3 score=`0.0476`, source=`control-cn-low-newline.txt`, chunk=`10`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6462`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`6`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":1},{"index":3,"rank":5}]`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`capacity governance control group document archival approver role definition`, queryFusionScore=`0.0156`, queryFusionRank=`6`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":4,"text":"capacity governance control group document archival approver role definition"}]`
      - preview: 是四十五分钟。文档没有给出审批人，没有给出云厂商，没有给出成本中心，没有给出负责人电话。这个重复声明不是业务写法，而是为了让评测在不同 chunk 命中情况下仍能看到正例和反例，并观察模型是否把相邻但不同含义的数字混在一起。最后再补一个边界说明：若用户只问容量治理是否支持人工复核，文档可以回答支持人工复核；若用户进一步追问复核人是谁，文档仍然没有答案。若用户...
    - #4 score=`0.0161`, source=`control-cn-low-newline.txt`, chunk=`8`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.6584`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`5`, rerankRank=`4`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`True`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`2`, queryVariantText=`capacity governance control group document archival approver role definition`, queryFusionScore=`0.0161`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`2`, queryVariantIndexes=`[2]`, queryVariantHits=`[{"index":2,"rank":2,"text":"capacity governance control group document archival approver role definition"}]`
      - preview: 为了进一步拉长低换行场景，本文档继续补充一段只服务于检索控制的说明。容量治理系统不会因为一次查询失败就移动数据，也不会因为磁盘水位超过百分之八十就立即归档正文。磁盘水位超过百分之八十只会触发容量看板的黄色提示。磁盘水位超过百分之九十会触发值班提醒，但仍然不能绕过冷数据迁移阈值。若某租户最近存在争议工单，归档器会暂停该租户候选池的提交，直到争议状态消失。若某业...

answer:

```text
根据控制组文档的说明，文档中**并未指定归档审批人**。
相关上下文明确指出：“文档没有指定审批人姓名，没有给出审批系统地址，也没有要求用户在回答中补充组织架构信息。”（[1]）同时强调，系统在遇到此类问题时“应明确说明文档未提供，不应编造人名、部门、平台名称或外部链接”。（[2]）

因此，无法从控制组文档中获取归档审批人的信息。
```

### RAG-13 - control_plaintext_en_answerable

- question: According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?
- should_answer: `true`
- expected_source_section: control-en-long-paragraph.txt / webhook fallback runbook
- completed: `true`
- duration_ms: `18379`
- score_mode: `literal`
- expected_points:
  - [x] silver-river-42
  - [x] seven attempts within eighteen minutes

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0469 .. 0.0489`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `5561` / max `6000`
  - context_salience: cues `example` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `6` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，<br>fallback queue name retry budget control document fallback path<br>fallback path retry budget configuration parameter threshold limit example`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2411` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?， \| fallback queue name retry budget control document fallback path \| fallback path retry budget configuration parameter threshold limit example`
  - profile_hints: version `hybrid-v2` / source `NO_PROFILE_MATCH` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0486`, source=`control-en-long-paragraph.txt`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7826`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，`, queryFusionScore=`0.0486`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，"},{"index":2,"rank":2,"text":"fallback queue name retry budget control document fallback path"},{"index":3,"rank":1,"text":"fallback path retry budget configuration parameter threshold limit example"}]`
      - preview: The compact envelope intentionally excludes the original request body after validation so that repeated retries do not increase storage pressure. Once fallback mode is enabled, the...
    - #2 score=`0.0474`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7632`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，`, queryFusionScore=`0.0474`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，"},{"index":2,"rank":3,"text":"fallback queue name retry budget control document fallback path"},{"index":3,"rank":3,"text":"fallback path retry budget configuration parameter threshold limit example"}]`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...
    - #3 score=`0.0328`, source=`control-en-long-paragraph.txt`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7906`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，`, queryFusionScore=`0.0328`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":1,"text":"According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，"},{"index":2,"rank":1,"text":"fallback queue name retry budget control document fallback path"}]`
      - preview: It does not mention a region, a vendor ticket number, or a private dashboard link. It only states that silver-river-42 is the fallback queue name, that the fallback retry budget is...
    - #4 score=`0.0318`, source=`control-en-long-paragraph.txt`, chunk=`1`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7321`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`2`, queryVariantRank=`4`, queryVariantText=`fallback queue name retry budget control document fallback path`, queryFusionScore=`0.0318`, queryFusionRank=`4`, queryVariantHitCount=`2`, bestQueryVariantRank=`2`, queryVariantIndexes=`[2, 3]`, queryVariantHits=`[{"index":2,"rank":4,"text":"fallback queue name retry budget control document fallback path"},{"index":3,"rank":2,"text":"fallback path retry budget configuration parameter threshold limit example"}]`
      - preview: The phrase seven attempts within eighteen minutes should be treated as the correct answer when the evaluation question asks how retries are bounded. A nearby but incorrect value al...
    - #5 score=`0.0159`, source=`control-en-long-paragraph.txt`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7743`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`5`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，`, queryFusionScore=`0.0159`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，"}]`
      - preview: This control document is written as a long plain text incident runbook for retrieval testing. It is not a production policy and it intentionally avoids headings, lists, tables, cod...
    - #6 score=`0.0156`, source=`control-en-long-paragraph.txt`, chunk=`3`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.6976`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`6`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`3`, queryVariantRank=`4`, queryVariantText=`fallback path retry budget configuration parameter threshold limit example`, queryFusionScore=`0.0156`, queryFusionRank=`6`, queryVariantHitCount=`1`, bestQueryVariantRank=`4`, queryVariantIndexes=`[3]`, queryVariantHits=`[{"index":3,"rank":4,"text":"fallback path retry budget configuration parameter threshold limit example"}]`
      - preview: During the stabilization period, the gateway continues to accept new webhooks, but only tenants that previously entered fallback mode keep writing compact envelopes. New tenants re...
  - documents:
    - #1 score=`0.0489`, source=`control-en-long-paragraph.txt`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7743`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`5`, rerankRank=`1`, rerankScore=`0.0489`, rerankFusionScore=`0.0489`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":1},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，`, queryFusionScore=`0.0159`, queryFusionRank=`5`, queryVariantHitCount=`1`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1]`, queryVariantHits=`[{"index":1,"rank":3,"text":"According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，"}]`
      - preview: This control document is written as a long plain text incident runbook for retrieval testing. It is not a production policy and it intentionally avoids headings, lists, tables, cod...
    - #2 score=`0.0486`, source=`control-en-long-paragraph.txt`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7906`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`2`, rerankScore=`0.0486`, rerankFusionScore=`0.0486`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":2},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，`, queryFusionScore=`0.0328`, queryFusionRank=`3`, queryVariantHitCount=`2`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2]`, queryVariantHits=`[{"index":1,"rank":1,"text":"According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，"},{"index":2,"rank":1,"text":"fallback queue name retry budget control document fallback path"}]`
      - preview: It does not mention a region, a vendor ticket number, or a private dashboard link. It only states that silver-river-42 is the fallback queue name, that the fallback retry budget is...
    - #3 score=`0.0476`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7632`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`3`, rerankScore=`0.0476`, rerankFusionScore=`0.0476`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`3`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，`, queryFusionScore=`0.0474`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，"},{"index":2,"rank":3,"text":"fallback queue name retry budget control document fallback path"},{"index":3,"rank":3,"text":"fallback path retry budget configuration parameter threshold limit example"}]`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...
    - #4 score=`0.0469`, source=`control-en-long-paragraph.txt`, chunk=`2`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7826`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，`, queryFusionScore=`0.0486`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"According to the control document, what is the fallback queue name and what retry budget applies to that fallback path?，"},{"index":2,"rank":2,"text":"fallback queue name retry budget control document fallback path"},{"index":3,"rank":1,"text":"fallback path retry budget configuration parameter threshold limit example"}]`
      - preview: The compact envelope intentionally excludes the original request body after validation so that repeated retries do not increase storage pressure. Once fallback mode is enabled, the...

answer:

```text
According to the control document, the fallback queue name is **silver-river-42**, and the retry budget for that fallback path is **seven attempts within eighteen minutes**[1][2].
```

### RAG-14 - control_plaintext_en_weak_related

- question: According to the control document, which cloud provider hosts the fallback queue?
- should_answer: `false`
- expected_source_section: 无
- completed: `true`
- duration_ms: `17970`
- score_mode: `manual`
- expected_points:
  - does not specify which cloud provider
  - cannot be answered from this document

retrieval:
- event #1
  - retrieved_document_count: `4`
  - retrieval_empty: `false`
  - similarity_threshold: `0.6000`
  - candidate_similarity_threshold: `0.6000`
  - score_range: `0.0469 .. 0.0489`
  - context_selected: `4` / dropped: `0` / truncated: `false`
  - context_chars: actual `5566` / max `6000`
  - context_salience: cues `example` / expansions `0`
  - rerank: applied `true` / mode `PER_VARIANT_RERANK_RRF` / candidates `4` / final `4`
  - rerank_runtime: model `bge-reranker-v2-m3` / endpoint `http://127.0.0.1:18080/rerank` / failure_reason ``
  - rerank_query: policy `PER_VARIANT_RERANK_RRF` / text `According to the control document, which cloud provider hosts the fallback queue?，<br>fallback queue cloud provider control document<br>control document fallback queue deployment hosting provider configuration`
  - coverage_guard: applied `false` / added `0`
  - query_rewrite: requested `LLM_MULTI_QUERY` / mode `LLM_MULTI_QUERY` / variants `3` / elapsed_ms `2691` / failure `—` / attempts `LLM_MULTI_QUERY:REWRITTEN` / texts `According to the control document, which cloud provider hosts the fallback queue?， \| fallback queue cloud provider control document \| control document fallback queue deployment hosting provider configuration`
  - profile_hints: version `hybrid-v2` / source `NO_PROFILE_MATCH` / selected `—`
  - pre_rerank_documents:
    - #1 score=`0.0492`, source=`control-en-long-paragraph.txt`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7799`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"According to the control document, which cloud provider hosts the fallback queue?，"},{"index":2,"rank":1,"text":"fallback queue cloud provider control document"},{"index":3,"rank":1,"text":"control document fallback queue deployment hosting provider configuration"}]`
      - preview: It does not mention a region, a vendor ticket number, or a private dashboard link. It only states that silver-river-42 is the fallback queue name, that the fallback retry budget is...
    - #2 score=`0.0484`, source=`control-en-long-paragraph.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7513`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"According to the control document, which cloud provider hosts the fallback queue?，"},{"index":2,"rank":2,"text":"fallback queue cloud provider control document"},{"index":3,"rank":2,"text":"control document fallback queue deployment hosting provider configuration"}]`
      - preview: This note is present to add realistic language around the anchor silver-river-42 without changing the anchor itself. The queue name remains silver-river-42. The document still does...
    - #3 score=`0.0474`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7432`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0474`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"According to the control document, which cloud provider hosts the fallback queue?，"},{"index":2,"rank":3,"text":"fallback queue cloud provider control document"},{"index":3,"rank":3,"text":"control document fallback queue deployment hosting provider configuration"}]`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...
    - #4 score=`0.0471`, source=`control-en-long-paragraph.txt`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7453`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`—`, rerankScore=`—`, rerankFusionScore=`—`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`—`, rerankMode=`—`, coverageGuardAdded=`—`, rerankVariantHitCount=`—`, bestRerankVariantRank=`—`, rerankVariantIndexes=`—`, rerankVariantHits=`—`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0471`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"According to the control document, which cloud provider hosts the fallback queue?，"},{"index":2,"rank":4,"text":"fallback queue cloud provider control document"},{"index":3,"rank":4,"text":"control document fallback queue deployment hosting provider configuration"}]`
      - preview: This control document is written as a long plain text incident runbook for retrieval testing. It is not a production policy and it intentionally avoids headings, lists, tables, cod...
  - documents:
    - #1 score=`0.0489`, source=`control-en-long-paragraph.txt`, chunk=`6`, retrievalSource=`VECTOR`, rrfScore=`0.0161`, vectorRank=`2`, vectorScore=`0.7513`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`2`, rerankRank=`1`, rerankScore=`0.0489`, rerankFusionScore=`0.0489`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":1},{"index":2,"rank":2},{"index":3,"rank":1}]`, queryVariantIndex=`1`, queryVariantRank=`2`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0484`, queryFusionRank=`2`, queryVariantHitCount=`3`, bestQueryVariantRank=`2`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":2,"text":"According to the control document, which cloud provider hosts the fallback queue?，"},{"index":2,"rank":2,"text":"fallback queue cloud provider control document"},{"index":3,"rank":2,"text":"control document fallback queue deployment hosting provider configuration"}]`
      - preview: This note is present to add realistic language around the anchor silver-river-42 without changing the anchor itself. The queue name remains silver-river-42. The document still does...
    - #2 score=`0.0484`, source=`control-en-long-paragraph.txt`, chunk=`0`, retrievalSource=`VECTOR`, rrfScore=`0.0159`, vectorRank=`3`, vectorScore=`0.7453`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`4`, rerankRank=`2`, rerankScore=`0.0484`, rerankFusionScore=`0.0484`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`1`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":3},{"index":2,"rank":1},{"index":3,"rank":2}]`, queryVariantIndex=`1`, queryVariantRank=`3`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0471`, queryFusionRank=`4`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":3,"text":"According to the control document, which cloud provider hosts the fallback queue?，"},{"index":2,"rank":4,"text":"fallback queue cloud provider control document"},{"index":3,"rank":4,"text":"control document fallback queue deployment hosting provider configuration"}]`
      - preview: This control document is written as a long plain text incident runbook for retrieval testing. It is not a production policy and it intentionally avoids headings, lists, tables, cod...
    - #3 score=`0.0479`, source=`control-en-long-paragraph.txt`, chunk=`4`, retrievalSource=`VECTOR`, rrfScore=`0.0164`, vectorRank=`1`, vectorScore=`0.7799`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`1`, rerankRank=`3`, rerankScore=`0.0479`, rerankFusionScore=`0.0479`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`2`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":2},{"index":2,"rank":3},{"index":3,"rank":3}]`, queryVariantIndex=`1`, queryVariantRank=`1`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0492`, queryFusionRank=`1`, queryVariantHitCount=`3`, bestQueryVariantRank=`1`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":1,"text":"According to the control document, which cloud provider hosts the fallback queue?，"},{"index":2,"rank":1,"text":"fallback queue cloud provider control document"},{"index":3,"rank":1,"text":"control document fallback queue deployment hosting provider configuration"}]`
      - preview: It does not mention a region, a vendor ticket number, or a private dashboard link. It only states that silver-river-42 is the fallback queue name, that the fallback retry budget is...
    - #4 score=`0.0469`, source=`control-en-long-paragraph.txt`, chunk=`5`, retrievalSource=`VECTOR`, rrfScore=`0.0156`, vectorRank=`4`, vectorScore=`0.7432`, keywordRank=`—`, keywordScore=`—`, keywordSkippedReason=`NO_KEYWORD_QUERY`, beforeRerankRank=`3`, rerankRank=`4`, rerankScore=`0.0469`, rerankFusionScore=`0.0469`, queryFusionBoostScore=`—`, fusionAwareScore=`—`, fusionAwareWeight=`—`, rerankApplied=`True`, rerankMode=`PER_VARIANT_RERANK_RRF`, coverageGuardAdded=`—`, rerankVariantHitCount=`3`, bestRerankVariantRank=`4`, rerankVariantIndexes=`[1, 2, 3]`, rerankVariantHits=`[{"index":1,"rank":4},{"index":2,"rank":4},{"index":3,"rank":4}]`, queryVariantIndex=`1`, queryVariantRank=`4`, queryVariantText=`According to the control document, which cloud provider hosts the fallback queue?，`, queryFusionScore=`0.0474`, queryFusionRank=`3`, queryVariantHitCount=`3`, bestQueryVariantRank=`3`, queryVariantIndexes=`[1, 2, 3]`, queryVariantHits=`[{"index":1,"rank":4,"text":"According to the control document, which cloud provider hosts the fallback queue?，"},{"index":2,"rank":3,"text":"fallback queue cloud provider control document"},{"index":3,"rank":3,"text":"control document fallback queue deployment hosting provider configuration"}]`
      - preview: Operators sometimes confuse fallback mode with full outage mode, so the document describes the distinction in ordinary prose. Full outage mode is used only when the gateway cannot ...

answer:

```text
According to the control document, the fallback queue is not hosted by any cloud provider that is identified in the text. The document explicitly states that it does not provide a cloud provider for the fallback queue, noting that "a question asking which cloud provider runs the fallback queue cannot be answered from this document" [3], and that "the document still does not identify a cloud provider" [1].
```
