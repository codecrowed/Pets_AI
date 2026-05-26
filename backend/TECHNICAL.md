# PawPal 后端技术文档

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.4.1 |
| 构建 | Maven | 多模块 POM |
| 持久层 | MyBatis | 3.0.4 |
| 数据库 | PostgreSQL | 14+ |
| 缓存 | Redis | 6+ |
| AI 框架 | Spring AI (OpenAI-compatible) | 1.0.0 |
| AI 模型 | DeepSeek | deepseek-chat |
| 认证 | JWT (jjwt) | 0.12.6 |
| 安全 | Spring Security | 无状态 JWT |
| 对象存储 | 阿里云 OSS | 3.18.1 |
| API 文档 | springdoc-openapi | 2.8.9 |
| 监控 | Actuator + Prometheus (Micrometer) | — |
| JSON | Fastjson | 2.0.28 |
| 架构测试 | ArchUnit | 1.3.0 |

## DDD 分层架构

### 架构总览

```
┌─────────────────────────────────────────────────────────────┐
│                    pets-ai-app (Bootstrap)                    │
├─────────────────────────────────────────────────────────────┤
│                 pets-ai-adapter-web (REST)                    │
│  AuthController │ ChatController │ PetController │ ...        │
├─────────────────────────────────────────────────────────────┤
│              pets-ai-application (Use Cases)                  │
│  ChatApplicationService │ PetApplicationService │ ...         │
├─────────────────────────────────────────────────────────────┤
│                pets-ai-domain (Domain Model)                  │
│  Entities │ Value Objects │ Repository Interfaces │ Services  │
├─────────────────────────────────────────────────────────────┤
│           pets-ai-infrastructure (Adapters)                   │
│  MyBatis Repos │ SecurityConfig │ OssAdapter │ AiConfig       │
├─────────────────────────────────────────────────────────────┤
│               pets-ai-types (Shared Kernel)                   │
│  ErrorCode │ BusinessException │ BaseEntity │ RequestContext  │
└─────────────────────────────────────────────────────────────┘
```

### 限界上下文

| 上下文 | 包路径 | 核心职责 |
|--------|--------|----------|
| Identity | `jiangxiaopeng.ai.identity` | 用户注册/登录、JWT 令牌管理、邀请码验证 |
| Conversation | `jiangxiaopeng.ai.conversation` | 对话会话管理、消息收发、流式响应、消息反馈 |
| AI | `jiangxiaopeng.ai.ai` | 模型配置、多智能体编排、Spring AI 集成 |
| Storage | `jiangxiaopeng.ai.storage` | 文件上传/下载、OSS 适配 |
| Pet | `jiangxiaopeng.ai.pet` | 宠物档案 CRUD、头像管理 |
| Diet | `jiangxiaopeng.ai.diet` | 食物库、餐食/饮水记录、营养统计、AI 饮食分析 |
| Shared | `jiangxiaopeng.ai.shared` | 错误码、全局异常处理、请求上下文 |

### 各模块详解

#### pets-ai-types（共享内核）

为所有模块提供基础类型：

- `ErrorCode` — 统一错误码枚举
- `BusinessException` — 业务异常基类
- `BaseEntity` — 实体基类（id, createdAt, updatedAt）
- `Uid` / `UserId` — 值对象
- `RequestContext` — 线程级请求上下文（userId, petId）

#### pets-ai-domain（领域层）

定义纯领域模型，不依赖任何框架：

- 领域实体（User, Chat, Message, Pet, DietRecord 等）
- 仓储接口（`Repository` interface）
- 领域服务（业务规则校验）
- 值对象和枚举

#### pets-ai-application（应用层）

编排业务用例，协调领域对象：

- 应用服务（Application Service）
- Command / Query DTO
- DTO 映射与组装
- 事务边界管理

#### pets-ai-infrastructure（基础设施层）

提供技术实现：

- MyBatis Mapper 实现仓储接口
- Spring Security 配置
- JWT 令牌服务
- 阿里云 OSS 适配器
- Spring AI 配置
- Redis 配置
- OpenAPI / Swagger 配置

#### pets-ai-adapter-web（Web 适配层）

HTTP 入口，REST 控制器：

- 12 个 Controller 类
- 请求验证与参数绑定
- 响应包装（`ApiResponseBodyAdvice`）
- 全局异常处理

## 认证与安全

### JWT 认证流程

```
客户端                         服务端
  │                              │
  │── POST /auth/login ─────────►│ 验证凭据
  │◄── { accessToken, refresh } ─│ 签发 JWT
  │                              │
  │── GET /api/* ───────────────►│ JwtAuthenticationFilter
  │   Authorization: Bearer xxx  │   ├── 解析 Token
  │                              │   ├── 验证签名 & 过期
  │                              │   ├── 检查 Redis 黑名单
  │                              │   └── 设置 SecurityContext
  │◄── 200 OK ──────────────────│
  │                              │
  │── POST /auth/logout ────────►│ Token 加入 Redis 黑名单
  │◄── 200 OK ──────────────────│
```

### Token 配置

| 类型 | 有效期 | Claims |
|------|--------|--------|
| Access Token | 3600s (1小时) | uid, email, plan |
| Refresh Token | 604800s (7天) | type: refresh |

### 安全过滤链

```java
SecurityFilterChain:
  ├── CSRF: disabled
  ├── Session: STATELESS
  ├── CORS: CorsConfig 注入
  ├── Public paths: /auth/**, /models, /swagger-ui/**, /actuator/health
  ├── Other paths: authenticated()
  └── addFilterBefore: JwtAuthenticationFilter
```

### 宠物上下文

`PetContextInterceptor` 拦截 `/api/**` 请求，解析 `X-Pet-Id` 请求头：
1. 验证宠物归属当前用户
2. 设置 `PetContext` 到 `RequestContext` 线程变量
3. 下游服务可通过 `RequestContext.getPetId()` 获取

## AI 多智能体系统

### 架构设计

```
用户消息
  │
  ▼
MultiAgentChatOrchestrator
  │
  ├── 加载 Agent 配置（从数据库）
  │     ├── pet_ai_agent_config
  │     ├── pet_ai_skill_config
  │     ├── pet_ai_tool_config
  │     └── pet_ai_prompt_config
  │
  ├── PetAiAgentRuntimeAssembler（组装运行时 Agent）
  │
  ▼
主 Agent (id: 20000001)
  │
  ├── 意图识别 & 路由
  │
  ├──► SubAgentDispatchTool（子 Agent 分发工具）
  │       ├── 健康咨询子 Agent
  │       ├── 饮食建议子 Agent
  │       └── 通用对话子 Agent
  │
  ▼
流式响应 (Flux<String>) → SSE → 客户端
```

### 关键组件

| 组件 | 职责 |
|------|------|
| `MultiAgentChatOrchestrator` | 编排入口，管理对话上下文与 Agent 调度 |
| `PetAiAgentRuntimeAssembler` | 从数据库配置组装运行时 Agent 实例 |
| `SubAgentDispatchTool` | Spring AI Tool，主 Agent 调用此工具分发至子 Agent |
| `DomainMessageChatMemoryAdvisor` | 对话记忆，从 MessageRepository 加载历史 |

### 配置驱动

Agent 系统完全由数据库配置驱动（`pet_ai_*` 表），支持：
- 动态添加/修改 Agent 角色与提示词
- 热更新工具绑定（Agent ↔ Tool 关系）
- 技能组合（Agent ↔ Skill ↔ Tool 三级关联）

### 开关配置

```yaml
app:
  ai:
    multi-agent:
      enabled: true                  # 多智能体开关
      main-agent-id: 20000001       # 主 Agent ID
```

## 数据持久化

### MyBatis 配置

```java
@Configuration
@MapperScan(basePackages = {
    "jiangxiaopeng.ai.identity.infrastructure.persistence.mapper",
    "jiangxiaopeng.ai.conversation.infrastructure.persistence.mapper",
    "jiangxiaopeng.ai.storage.infrastructure.persistence.mapper",
    "jiangxiaopeng.ai.ai.infrastructure.persistence.mapper",
    "jiangxiaopeng.ai.diet.infrastructure.persistence.mapper",
    "jiangxiaopeng.ai.pet.infrastructure.persistence.mapper"
})
public class MybatisConfig {
    // 逻辑删除拦截器
    @Bean
    public MybatisLogicalDeleteInterceptor logicalDeleteInterceptor() { ... }
}
```

### 逻辑删除

通过 `MybatisLogicalDeleteInterceptor` 全局拦截：
- `SELECT` 自动追加 `WHERE deleted_at IS NULL`
- `DELETE` 转换为 `UPDATE SET deleted_at = NOW()`

### 数据库表一览

| 分类 | 表名 | 说明 |
|------|------|------|
| 用户 | `users` | 用户账号 |
| 用户 | `invitation_codes` | 邀请码 |
| 对话 | `chat_sessions` | 对话会话 |
| 对话 | `messages` | 消息记录 |
| 对话 | `message_feedbacks` | 消息反馈 |
| 对话 | `usage_records` | 使用量记录 |
| 存储 | `attachments` | 文件附件 |
| AI | `pet_ai_agent_config` | Agent 配置 |
| AI | `pet_ai_client_config` | AI 客户端配置 |
| AI | `pet_ai_skill_config` | 技能配置 |
| AI | `pet_ai_tool_config` | 工具配置 |
| AI | `pet_ai_prompt_config` | 提示词配置 |
| AI | `pet_ai_agent_tool_relation` | Agent-Tool 关联 |
| AI | `pet_ai_skill_tool_relation` | Skill-Tool 关联 |
| AI | `pet_ai_agent_skill_relation` | Agent-Skill 关联 |
| 宠物 | `pets` | 宠物档案 |
| 饮食 | `staple_foods` | 主粮库 |
| 饮食 | `foods` | 食物库 |
| 饮食 | `pet_diet_records` | 饮食记录 |
| 饮食 | `pet_water_records` | 饮水记录 |
| 饮食 | `user_frequent_foods` | 用户常用食物 |

## SSE 流式响应

### 实现方案

```
Spring AI ChatClient
  │
  ▼
Flux<String> (Reactor 响应流)
  │
  ▼
SseEmitterHelper / ResponseBodyEmitter
  │
  ▼
HTTP Response (Content-Type: text/event-stream)
  │
  ▼
客户端 ReadableStream 逐块解析
```

### 关键设计

- 使用 `spring-boot-starter-webflux` 提供 `Flux` 支持，但整体仍为 Servlet 架构
- `ResponseBodyEmitter` 在 Servlet 线程中创建，AI 回调在独立线程写入
- `RequestAttributeSecurityContextRepository` 解决异步 dispatch 时 SecurityContext 丢失问题
- `AsyncConfig` 配置异步线程池，确保 `RequestContext` 跨线程传播

## 文件存储

### 阿里云 OSS 集成

```
OssStorageAdapter implements StorageService
  ├── upload(file) → 生成唯一路径 → PutObject → 返回 fileId
  ├── download(fileId) → GetObject → InputStream
  ├── delete(fileId) → DeleteObject
  └── presignedUrl(path) → GeneratePresignedUrl（客户端直传）
```

### 头像上传流程

```
前端                              后端                         OSS
 │                                 │                           │
 │── GET /avatar/presigned-url ──►│                           │
 │◄── { uploadUrl, objectKey } ──│                           │
 │                                 │                           │
 │── PUT file ─────────────────────────────────────────────►│
 │◄── 200 OK ──────────────────────────────────────────────│
 │                                 │                           │
 │── POST /avatar/confirm ───────►│── 更新 pet.avatarUrl ──►│
 │◄── { avatarUrl } ────────────│                           │
```

## CORS 配置

```java
@Configuration
public class CorsConfig {
    // 允许来源：app.cors.allowed-origins
    // 默认：localhost:3000, localhost:5173
    // 作用路径：/api/**
    // 允许方法：GET, POST, PUT, DELETE, OPTIONS
    // 允许 Headers：Authorization, Content-Type, X-Pet-Id
}
```

## 日志配置

### Logback 设置

- **控制台输出**：彩色格式，开发环境使用
- **文件输出**：`logs/application.log`
  - 单文件最大：50MB
  - 保留天数：30 天
  - 总大小上限：1GB

### 日志级别（开发环境）

```yaml
logging:
  level:
    jiangxiaopeng.ai: DEBUG
    org.springframework.ai: DEBUG
    org.mybatis: DEBUG
    org.springframework.security: DEBUG
```

## 测试

### 架构规则测试（ArchUnit）

```java
@AnalyzeClasses(packages = "jiangxiaopeng.ai")
class LayerDependencyRulesTest {
    // domain 不得依赖 infrastructure
    // domain 不得依赖 interfaces (adapter-web)
    // application 不得依赖 infrastructure
}
```

### 测试数据库

- 测试环境使用 H2 内存数据库
- 配置在 `pets-ai-app/src/test/resources/`

## 请求处理流程

完整请求链路：

```
HTTP 请求
  │
  ├── CorsFilter（跨域处理）
  ├── RateLimitFilter（限流 — 当前未启用）
  ├── JwtAuthenticationFilter（JWT 认证）
  │     ├── 解析 Bearer Token
  │     ├── 验证 & 提取 Claims
  │     ├── 设置 SecurityContext
  │     └── 设置 RequestContext
  ├── PetContextInterceptor（宠物上下文 — 可选）
  │
  ▼
  REST Controller (adapter-web)
  │
  ▼
  Application Service (application)
  │  ├── DTO ↔ Domain 转换
  │  ├── 事务管理
  │  └── 编排领域服务
  │
  ▼
  Domain Service / Repository (domain)
  │
  ▼
  Infrastructure Implementation
  │  ├── MyBatis Mapper (DB)
  │  ├── OssStorageAdapter (文件)
  │  ├── Spring AI ChatClient (AI)
  │  └── Redis (缓存/黑名单)
  │
  ▼
  ApiResponseBodyAdvice（统一响应包装）
  │
  ▼
HTTP 响应 { code, message, success, timestamp, data }
```

## 已知限制与后续规划

| 项目 | 现状 | 规划 |
|------|------|------|
| OAuth2 | 依赖已引入，域模型有 OAuthIdentity，但未实现 | 待实现第三方登录 |
| 限流 | RateLimitFilter 已编写但禁用 | 待启用并测试 |
| 数据库迁移 | 手动 SQL 脚本，无 Flyway | 建议引入 Flyway |
| 缓存 | 仅用于 JWT 黑名单 | 可扩展热数据缓存 |
| 单元测试 | 仅架构规则测试 | 需补充业务逻辑测试 |
| 容器化 | 无 Dockerfile | 建议添加 |
| CI/CD | 无 | 建议引入 GitHub Actions |
| 消息队列 | 无 | AI 异步任务可引入 |
