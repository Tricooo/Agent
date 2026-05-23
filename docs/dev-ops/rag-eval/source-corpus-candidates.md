# RAG 外部测试语料候选

> 目标：扩大 `rag_demo` 的评测样本宽度，覆盖更多 RAG 失败模式。本文只记录候选来源和 case 草案，不直接修改 `cases.json`。
> 检索核对时间：2026-05-23。

## 选型原则

- 优先官方、稳定、结构清晰的 HTML / PDF，避免博客二手解释。
- 每个来源至少能设计 3 类问题：精确抽取、深层段落、弱相关或不可回答。
- 动态页面要记录访问日期；如果后续用于正式回归，建议落成本地快照或固定版本 PDF。
- 不把 benchmark 直接替代业务语料；benchmark 用来借鉴 case 设计和做后续独立评测。

## 候选来源

| 优先级 | 来源 | URL | 主要覆盖点 |
|---|---|---|---|
| P0 | NASA Artemis I Mission Timeline | https://www.nasa.gov/reference/artemis-i-mission-timeline | 时间线、表格、日期、精确数字、长页深段落 |
| P0 | NASA Apollo 13 Mission Details | https://www.nasa.gov/mission_pages/apollo/missions/apollo13.html | 长叙事、因果链、任务目标、人物实体 |
| P0 | RFC 9110 HTTP Semantics | https://www.rfc-editor.org/rfc/rfc9110 | 技术规范、章节定位、状态码语义、精确措辞 |
| P0 | Kubernetes Persistent Volumes | https://kubernetes.io/docs/concepts/storage/persistent-volumes/ | 长技术文档、feature state、步骤、废弃项 |
| P0 | Kubernetes Rolling Update Tutorial | https://kubernetes.io/docs/tasks/run-application/update-deployment-rolling/ | 操作流程、命令、顺序依赖 |
| P0 | OWASP Top 10 A01 Broken Access Control | https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control/ | 安全分类、表格指标、例子、缓解措施 |
| P0 | OWASP Top 10 A03 Injection | https://owasp.org/Top10/2021/A03_2021-Injection/ | 相似安全概念区分、漏洞条件 |
| P0 | OWASP Top 10 A05 Security Misconfiguration | https://owasp.org/Top10/en/A05_2021-Security_Misconfiguration/ | 配置错误、预防措施、弱相关干扰 |
| P1 | NIST AI RMF 1.0 | https://www.nist.gov/publications/artificial-intelligence-risk-management-framework-ai-rmf-10 | 政策长文、定义、目标、风险管理框架 |
| P1 | NIST AI RMF Overview | https://www.nist.gov/itl/ai-risk-management-framework | 版本/日期区分、相关资源、动态公告 |
| P1 | PostgreSQL 18 Generated Columns | https://www.postgresql.org/docs/18/ddl-generated-columns.html | 版本差异、stored vs virtual、限制条件 |
| P1 | PostgreSQL 16 CREATE TABLE | https://www.postgresql.org/docs/16/sql-createtable.html | 同主题旧版本对照、版本冲突检索 |
| P1 | CDC Measles Symptoms and Complications | https://www.cdc.gov/measles/signs-symptoms/index.html | 时间窗口、医学数字、分层风险、拒答边界 |
| P1 | NOAA ENSO Overview | https://www.climate.gov/enso | 概念对照、阈值条件、动态状态 |
| P1 | NOAA ENSO FAQ | https://www.climate.gov/news-features/understanding-climate/el-nino-and-la-nina-frequently-asked-questions | 多段合成、定义、预测能力 |
| P1 | 中华人民共和国民法典 | https://flk.npc.gov.cn/detail?id=ff808081729d1efe01729d50b5c500bf&title=%E4%B8%AD%E5%8D%8E%E4%BA%BA%E6%B0%91%E5%85%B1%E5%92%8C%E5%9B%BD%E6%B0%91%E6%B3%95%E5%85%B8 | 中文法律长文、条款定位、目录层级 |
| P1 | 中华人民共和国数据安全法 | https://flk.npc.gov.cn/detail?fileId=&id=ff80818179f5e0800179f885c7e70392&title=%E4%B8%AD%E5%8D%8E%E4%BA%BA%E6%B0%91%E5%85%B1%E5%92%8C%E5%9B%BD%E6%95%B0%E6%8D%AE%E5%AE%89%E5%85%A8%E6%B3%95&type= | 中文法律、义务/责任/制度类问答 |
| P1 | 中华人民共和国个人信息保护法 | https://www.npc.gov.cn/WZWSREL25wYy9jMi9jMzA4MzQvMjAyMTA4L3QyMDIxMDgyMF8zMTMwODguaHRtbD9yZWY9aW1i | 中文法律、列举条件、跨境规则 |
| P2 | HotpotQA | https://hotpotqa.github.io/wiki-readme.html | 多跳、supporting facts、跨文档证据 |
| P2 | MultiHop-RAG | https://github.com/yixuantt/MultiHop-RAG/ | RAG 专用多跳、2-4 文档证据、metadata |
| P2 | QASPER | https://allenai.org/data/qasper | 科研论文 QA、表格/章节、不可回答问题 |
| P2 | Natural Questions | https://github.com/google-research-datasets/natural-questions | 真实搜索问题、Wikipedia 长短答案 |

## 第一批入库建议

先不要一次灌满全部来源。建议第一批只选 8 个来源，形成 30 条左右 case：

1. NASA Artemis I Mission Timeline
2. NASA Apollo 13 Mission Details
3. RFC 9110 HTTP Semantics
4. Kubernetes Persistent Volumes
5. Kubernetes Rolling Update Tutorial
6. OWASP A01 / A03 / A05 作为一个安全文档包
7. NIST AI RMF 1.0
8. PostgreSQL 18 + PostgreSQL 16 作为版本对照包

这样能覆盖时间线、长叙事、规范、技术操作、安全分类、政策长文、版本冲突。等这批能稳定评测后，再补 CDC/NOAA/中文法律/benchmark。

## Case 草案

这些 case 还不能直接进 `cases.json`，因为对应文章尚未入库。正式落地前需要先确认 chunk、source section 和 expected points 来自真实入库文本。

### NASA Artemis I

- EXT-ARTEMIS-01 | `answerable` | `literal`
  - Question: Artemis I 的发射日期、溅落日期和任务持续时间分别是什么？
  - Expected section: Mission Overview
  - Expected points: `Nov. 16, 2022`, `Dec. 11, 2022`, `25 days, 10 hours, 53 minutes`

- EXT-ARTEMIS-02 | `answerable_deep_section` | `literal`
  - Question: Artemis I Flight Day 26 的关键事件顺序是什么？
  - Expected section: Flight Day 26
  - Expected points: `Return trajectory correction burn-6`, `Crew module/service module separation`, `Splashdown`

- EXT-ARTEMIS-03 | `answerable_long_context` | `manual`
  - Question: Artemis I 在返回阶段如何逐步准备溅落？
  - Expected section: Flight Day 20-26
  - Expected points: 返回月球近旁飞越、轨道修正、系统检查、回收团队到位、再入和溅落

- EXT-ARTEMIS-04 | `weak_related` | `manual`
  - Question: 根据 Artemis I timeline，Artemis II 的四名宇航员分别是谁？
  - Expected section: 无
  - Expected points: 当前 timeline 不足以回答；需要其他 Artemis II 资料

### NASA Apollo 13

- EXT-APOLLO13-01 | `answerable` | `manual`
  - Question: Apollo 13 原计划的任务目标是什么，为什么最终没有登月？
  - Expected section: Mission Objective
  - Expected points: Fra Mauro 区域、第三次登月尝试、爆炸/氧气罐事故、绕月返回

- EXT-APOLLO13-02 | `answerable_deep_section` | `literal`
  - Question: Apollo 13 发射早期 S-II 中央发动机发生了什么，后续发动机如何补偿？
  - Expected section: Mission Highlights
  - Expected points: center engine shut down early, remaining four engines burned 34 seconds longer, S-IVB burned nine seconds longer

- EXT-APOLLO13-03 | `paraphrase` | `manual`
  - Question: 用自己的话说明 Apollo 13 为什么常被视为一次失败任务中的成功救援。
  - Expected section: Mission Highlights / Landing
  - Expected points: 登月失败、乘员绕月、地面与飞船协作、安全返回

- EXT-APOLLO13-04 | `unanswerable` | `manual`
  - Question: 根据这篇 Apollo 13 文档，事故后每名宇航员的长期健康检查结果是什么？
  - Expected section: 无
  - Expected points: 文档未提供长期健康检查结果；不能编造

### RFC 9110 HTTP Semantics

- EXT-RFC9110-01 | `answerable` | `literal`
  - Question: RFC 9110 中 status code 的基本定义是什么？
  - Expected section: Status Codes
  - Expected points: three-digit integer code, describes result of request, semantics of response

- EXT-RFC9110-02 | `answerable_deep_section` | `literal`
  - Question: RFC 9110 如何说明 HTTP core semantics 和 wire format 的关系？
  - Expected section: Introduction / HTTP Semantics
  - Expected points: semantics do not change between protocol versions, expression on the wire can change, extension points

- EXT-RFC9110-03 | `answerable_formula` | `literal`
  - Question: RFC 9110 对 1xx 状态码和 HTTP/1.0 客户端有什么限制？
  - Expected section: Informational 1xx
  - Expected points: HTTP/1.0 did not define 1xx, server MUST NOT send 1xx to HTTP/1.0 client

- EXT-RFC9110-04 | `weak_related` | `manual`
  - Question: 根据 RFC 9110，Spring Boot `@ControllerAdvice` 应该怎么写？
  - Expected section: 无
  - Expected points: RFC 9110 不包含 Spring Boot 实现细节；只能回答 HTTP 语义

### Kubernetes Persistent Volumes

- EXT-K8S-PV-01 | `answerable` | `manual`
  - Question: Kubernetes 中 PV 和 PVC 分别是什么？
  - Expected section: Introduction
  - Expected points: PV 是集群存储资源，PVC 是用户存储请求，PV 生命周期独立于 Pod

- EXT-K8S-PV-02 | `answerable_deep_section` | `literal`
  - Question: Retain reclaim policy 下管理员需要做哪些手动回收步骤？
  - Expected section: Reclaiming / Retain
  - Expected points: delete PersistentVolume, clean up data on storage asset, delete storage asset or reuse with new PV

- EXT-K8S-PV-03 | `answerable_long_context` | `manual`
  - Question: PVC 扩容失败时，文档给出的恢复流程是什么？
  - Expected section: Recovering from Failure when Expanding Volumes
  - Expected points: 标记 Retain、删除 PVC、删除 claimRef、用较小尺寸重建 PVC、恢复 reclaim policy

- EXT-K8S-PV-04 | `unanswerable` | `manual`
  - Question: 根据 PV 文档，MySQL InnoDB undo log 应该如何调优？
  - Expected section: 无
  - Expected points: 文档是 Kubernetes 存储对象说明，不涉及 InnoDB undo log 调优

### Kubernetes Rolling Update

- EXT-K8S-ROLL-01 | `answerable` | `manual`
  - Question: Kubernetes rolling update 的目标是什么？
  - Expected section: Objectives
  - Expected points: 更新 Deployment、监控 rollout、暂停/恢复、配置策略参数、必要时回滚

- EXT-K8S-ROLL-02 | `answerable_deep_section` | `literal`
  - Question: Deployment 的什么字段变化会触发 rolling update？
  - Expected section: Update a Deployment
  - Expected points: `.spec.template`

- EXT-K8S-ROLL-03 | `paraphrase` | `manual`
  - Question: 用自己的话解释 rolling update 为什么能减少停机风险。
  - Expected section: Update a Deployment Without Downtime
  - Expected points: 逐步替换旧 Pod、新旧 Pod 过渡、持续可用、可监控和回滚

### OWASP Top 10

- EXT-OWASP-01 | `answerable` | `literal`
  - Question: OWASP A01 Broken Access Control 的典型失败形式有哪些？
  - Expected section: A01 Description
  - Expected points: least privilege violation, URL/parameter tampering, IDOR, missing API access controls, privilege escalation

- EXT-OWASP-02 | `answerable` | `literal`
  - Question: OWASP A03 Injection 中应用在哪些情况下容易受到注入攻击？
  - Expected section: A03 Description
  - Expected points: user-supplied data not validated/filtered/sanitized, dynamic queries, non-parameterized calls

- EXT-OWASP-03 | `answerable_deep_section` | `manual`
  - Question: Broken Access Control、Injection、Security Misconfiguration 三类风险的区别是什么？
  - Expected section: A01 / A03 / A05 Description
  - Expected points: 权限策略失败、解释器输入污染、配置/默认项/安全头/错误信息等配置问题

- EXT-OWASP-04 | `weak_related` | `manual`
  - Question: 根据 OWASP A01/A03/A05，OAuth 2.1 authorization code flow 的完整报文是什么？
  - Expected section: 无
  - Expected points: OWASP Top 10 页面不是 OAuth 协议规范；不能补全协议报文

### NIST AI RMF

- EXT-NIST-01 | `answerable` | `manual`
  - Question: NIST AI RMF 1.0 的目标是什么？
  - Expected section: Abstract / Overview
  - Expected points: 帮助组织管理 AI 风险、促进可信和负责任 AI、适用于设计/开发/部署/使用 AI 系统

- EXT-NIST-02 | `answerable_deep_section` | `literal`
  - Question: NIST AI RMF 1.0 被描述成哪些性质？
  - Expected section: Abstract
  - Expected points: voluntary, rights-preserving, non-sector specific, use-case agnostic

- EXT-NIST-03 | `answerable_long_context` | `manual`
  - Question: NIST AI RMF 页面列出了哪些配套资源或后续 profile？
  - Expected section: Overview / Resources
  - Expected points: Playbook、Roadmap、Crosswalk、AI Resource Center、Generative AI Profile

- EXT-NIST-04 | `unanswerable` | `manual`
  - Question: 根据 AI RMF 1.0，某个具体公司的模型上线审批单该怎么填？
  - Expected section: 无
  - Expected points: 框架不包含该公司的内部审批单；只能给通用风险管理方向

### PostgreSQL Version Contrast

- EXT-PG-01 | `answerable` | `literal`
  - Question: PostgreSQL 18 文档中 generated column 有哪两类？
  - Expected section: Generated Columns
  - Expected points: stored, virtual

- EXT-PG-02 | `answerable_deep_section` | `literal`
  - Question: PostgreSQL 18 中 stored generated column 和 virtual generated column 的区别是什么？
  - Expected section: Generated Columns
  - Expected points: stored write 时计算并占用存储，virtual read 时计算且不占用存储

- EXT-PG-03 | `answerable_deep_section` | `manual`
  - Question: PostgreSQL 18 generated column 的表达式有哪些限制？
  - Expected section: Generated Columns restrictions
  - Expected points: immutable functions、不能用 subqueries、不能引用其他 generated column、不能作为 partition key

- EXT-PG-04 | `answerable_version_conflict` | `manual`
  - Question: 如果知识库同时有 PostgreSQL 16 和 18 文档，关于 generated column 的 virtual/stored 差异应该如何回答？
  - Expected section: PostgreSQL 16 CREATE TABLE + PostgreSQL 18 Generated Columns
  - Expected points: 需要区分版本；PG18 文档有 virtual/stored 两类和默认 virtual；PG16 语境不能直接套用 PG18 结论

## 覆盖矩阵

| 失败模式 | 对应来源 | 对应 case |
|---|---|---|
| 精确数字/日期抽取 | NASA Artemis I, CDC, SEC 10-K 后续可补 | EXT-ARTEMIS-01 |
| 深层段落召回 | NASA Artemis I, Kubernetes PV, PostgreSQL | EXT-ARTEMIS-02, EXT-K8S-PV-02, EXT-PG-03 |
| 长上下文综合 | Apollo 13, NIST AI RMF, Kubernetes PV | EXT-APOLLO13-03, EXT-NIST-03, EXT-K8S-PV-03 |
| 弱相关拒答 | RFC 9110, OWASP, Artemis I | EXT-RFC9110-04, EXT-OWASP-04, EXT-ARTEMIS-04 |
| 完全不可回答 | Apollo 13, Kubernetes PV, NIST | EXT-APOLLO13-04, EXT-K8S-PV-04, EXT-NIST-04 |
| 版本冲突 | PostgreSQL 16 vs 18 | EXT-PG-04 |
| 相似概念区分 | OWASP A01/A03/A05, NOAA ENSO 后续可补 | EXT-OWASP-03 |
| 操作流程顺序 | Kubernetes Rolling Update, PV recovery | EXT-K8S-ROLL-01, EXT-K8S-PV-03 |

## 下一步

1. 先从 P0 第一批 8 个来源中挑 3-5 个，保存为本地固定语料。
2. 入库后跑一次小集，只看 `retrieved/score/empty`、`pre_rerank_documents` 和 final `documents`。
3. 再把通过人工核验的 case 转入 `cases.json`，每次最多新增 8-10 条，避免评测面突然变宽导致归因困难。
4. 对动态来源单独记录快照日期；如果页面后续更新，不和旧结果直接互比。
