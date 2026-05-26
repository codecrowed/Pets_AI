# 限界上下文与依赖说明

本文档落实 [DDD_REFACTORING.md](./DDD_REFACTORING.md) 中「梳理 `ai` 与 `conversation`、`shared` 边界」的约定，供新增功能时对齐依赖方向。

## 上下文一览

| 包前缀 | 职责 | 分层 |
|--------|------|------|
| `jiangxiaopeng.ai.identity` | 用户、注册登录、JWT、安全过滤器 | domain / application / infrastructure / interfaces |
| `jiangxiaopeng.ai.conversation` | 会话、消息、流式对话、记忆、用量相关监听 | 同上 |
| `jiangxiaopeng.ai.ai` | 模型清单、补全抽象（`ChatCompletionService`）、Spring AI 适配、用量埋点 | 同上 |
| `jiangxiaopeng.ai.storage` | 附件元数据、OSS 存储适配 | 同上 |
| `jiangxiaopeng.ai.pet` | 宠物领域（当前多为占位 API） | 同上 |
| `jiangxiaopeng.ai.shared` | 共享内核：标识（`UserId`/`Uid`）、领域事件基类、应用端口（`DomainEventPublisher`）、横切 Web/配置 | `domain` / `application` / `exception` / `infrastructure` |

## `conversation` 与 `ai` 的边界

- **conversation**：对话聚合、消息生命周期、会话权限、与「对话」直接相关的领域服务（如 `ChatDomainService`）、对外的路由抽象 **`AiModelRouter`（接口，位于 conversation.domain）**。
- **ai**：**模型域**：可用模型与 Provider、向大模型发起补全的 **`ChatCompletionService`（接口，位于 ai.domain）**、Spring AI 具体实现与注册表（`ai.infrastructure.springai`）。
- **集成方式**：`conversation.infrastructure.ai.AiModelRouterImpl` 依赖 **`ai.domain.service.ChatCompletionService`**，在基础设施层完成「对话上下文 → 模型调用」的编排。这是**跨上下文调用**，当前通过接口倒置实现；若未来模型策略变复杂，可在此类实现前后增加显式防腐对象（ACL），而不在 conversation 领域实体中直接出现 Spring AI 类型。

## `shared` 的定位

- **属于共享内核**：`UserId`、`Uid`、`DomainEvent`、`AggregateRoot`、统一业务异常与错误码等，各上下文可依赖。
- **仅为技术横切**：`shared.infrastructure.web`（统一响应、过滤器）、`openapi`、`Redis` 配置等——允许被 `interfaces` 与启动类间接使用；**业务 domain 包不应依赖 `shared.infrastructure`**（当前由 ArchUnit 守护 domain→infrastructure，同包名下的 shared 分层仍应自觉保持 domain 纯净）。

## 上下文间依赖原则

1. **领域层（`..domain..`）**不得依赖本工程中的 **`..infrastructure..`**、**`..interfaces..`**（由 `LayerDependencyRulesTest` 强制执行）。
2. **应用层（`..application..`）**不得依赖本工程中的 **`..infrastructure..`**（同上）。
3. **跨上下文**：优先依赖**对方 domain 的接口或值对象**；需要编排时放在 **application** 或 **infrastructure 适配器**中，避免在 domain 实体中引用其他上下文的实现类。

## Maven 模块映射（阶段 B）

| 包前缀 | 所在模块 |
|--------|----------|
| `jiangxiaopeng.ai.shared`（exception / domain） | `pets-ai-types` |
| `jiangxiaopeng.ai.*.domain` | `pets-ai-domain` |
| `jiangxiaopeng.ai.*.application`、`shared.application` | `pets-ai-application` |
| `jiangxiaopeng.ai.*.infrastructure`、`shared.infrastructure` | `pets-ai-infrastructure` |
| `jiangxiaopeng.ai.*.interfaces` | `pets-ai-adapter-web` |
| `jiangxiaopeng.ai.AiChatApplication`、配置与测试 | `pets-ai-app` |

## 修订记录

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0 | 2026-04-08 | 初版，与阶段 A 架构守护同步 |
| 1.1 | 2026-04-08 | 增加 Maven 模块映射 |
