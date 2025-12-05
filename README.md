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
