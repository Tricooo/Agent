trigger（接口层）：HTTP/SSE/Job，仅做入参校验、鉴权、调用应用服务。

application（用例层）：编排用例/事务/权限/幂等，组合多个领域服务和仓储，屏蔽持久化细节。
用例层，如 AiAgentDrawAdminService.saveDrawConfig 解析前端 JSON → 组装 AiAgentAggregate/AiClientAggregate/FlowConfig → 调用仓储保存，属于典型编排。

domain（领域层）：聚合、领域服务、领域事件，不碰技术细节；依赖抽象仓储接口，不关心存储/协议

infrastructure（基础设施层）：仓储实现（MyBatis/Redis）、外部系统/MCP/DB 适配、配置。

types（共享内核）：通用异常、响应码、链式/调度框架。

api（契约）：DTO/接口定义，供上层调用或第三方集成。

boot (启动/组装模块) ：只负责 Application 启动和基础环境配置（数据源、线程池、Spring AI 配置）。

层间关系（自上而下依赖抽象）：
Controller/Job → Application Service → Domain Service/Aggregate (依赖 Repository 接口) → 
Infrastructure RepositoryImpl/Adapter 实现接口；Boot 配置把 Bean 装好，API/Types 提供共享契约与基础类型。


先补DDD合规与结构稳定：
✅抽出应用服务门面，避免 Controller 直接调领域（为执行/装配加 Application Facade）；
✅将 ChatClient/SSE 适配下沉到基础设施或接口层，领域层只暴露端口；
✅聚合保存保证不变式（保存 Agent 时一起写 flow config、client config）。

性能与稳定性兜底：
给 Agent/FlowConfig/Client 做本地或 Redis 缓存，避免每次查库；
ChatClient 调用支持流式/并行（可先在 Auto/Flow 的 Step 节点拆出异步调用）；
线程池、重试超时、限流监控补齐，防止流式接口撑爆。

业务拓展（在1、2稳后做）：
多角色/多 Agent 协同（Planner/Executor/Critic 并行）；
会话记忆与用户画像；
工作流化（步骤持久化、补偿/重试）；
观测审计与多租户配置中心。

抽象 LLM/工具调用层
定义 LLMGateway、ToolRegistry 接口（domain），在 infra 实现 ChatClient/OpenAI/MCP 适配；领域节点不直接拿 ChatClient。
Prompt 模板与工具描述落到配置/模板层，节点只传业务变量，减少重复拼接。

做一层缓存与幂等
在 IAgentRepository 查询 Agent/FlowConfig 处加本地缓存（Caffeine/Guava）+ 版本号失效，避免每次执行查库。
Armory 注册 Bean 前检查已存在，防止重复注册导致污染。

优化执行链路
去掉 Step4ExecuteStepsNode 的 Thread.sleep，为工具/LLM 调用增加超时与重试（线程池/CompletableFuture）。
对 Flow 场景可并发执行相互独立的步骤；Auto 场景可将分析/执行/监督的工具调用拆成可并发子任务。

监控与审计
增加调用耗时、token 消耗、异常的埋点（策略开始/结束、每次 LLM 调用、每次工具调用），按 sessionId/agentId 关联。
为 SSE 增加心跳和超时，防止连接泄漏。

知识库与会话
把 conversationId 对话内容、用户画像持久化（可存 Redis/DB），并在 Prompt 构造时只拉需要的上下文。
RAG 入库流程做异步批处理，分片入库并打 tag 去重。

DDD 拆分与命名收敛
拆分上下文：运行（Runtime）、配置/装配（Config/Armory）、知识库（Knowledge）；应用层负责用例编排，领域聚合避免 DTO 命名。
为聚合增加不变式校验方法（如 FlowConfig 序列唯一、合法策略值）。