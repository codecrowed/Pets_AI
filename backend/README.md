# PawPal 宠物 AI 助手 — 后端服务

## 项目简介

PawPal 后端是基于 Spring Boot 3.4 + Java 17 的 DDD 多模块架构服务，提供宠物 AI 对话（多智能体编排）、宠物档案管理、饮食记录与营养分析、文件存储等核心能力。AI 对话层集成 DeepSeek 大模型，通过 Spring AI 框架实现流式响应与多智能体协同。

## 核心功能

| 模块 | 功能描述 |
|------|----------|
| **AI 对话** | 多智能体编排（主 Agent 分发 + 子 Agent 执行）、SSE 流式响应、对话记忆、模型选择 |
| **宠物档案** | 宠物信息 CRUD、头像 OSS 直传（presigned URL）、软删除 |
| **饮食管理** | 餐食/饮水记录、食物库搜索、每日营养统计、周热量趋势、AI 营养分析 |
| **用户认证** | 邀请码注册、JWT 令牌（Access + Refresh）、Redis 黑名单注销 |
| **文件存储** | 阿里云 OSS 上传/下载/删除、presigned URL 直传 |

## 快速开始

### 环境要求

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Redis 6+

### 本地运行

```bash
cd backend

# 编译所有模块
mvn clean install -DskipTests

# 启动服务
mvn -pl pets-ai-app spring-boot:run
```

服务默认运行在 `http://localhost:8081/pet-ai-app`。

### 配置说明

开发环境配置位于 `pets-ai-app/src/main/resources/application-dev.yml`，需配置以下关键项：

| 配置项 | 说明 |
|--------|------|
| `spring.datasource.url` | PostgreSQL 连接地址 |
| `spring.datasource.username/password` | 数据库凭据 |
| `spring.data.redis.host/port` | Redis 地址 |
| `spring.ai.openai.base-url` | AI 模型 API 地址（DeepSeek） |
| `spring.ai.openai.api-key` | AI 模型 API Key |
| `aliyun.oss.*` | 阿里云 OSS 配置 |
| `app.jwt.secret` | JWT 签名密钥 |

生产部署使用 `application-tmp.yml` 的环境变量模板：

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  ai:
    openai:
      api-key: ${AI_API_KEY}
```

### 数据库初始化

SQL 脚本位于 `pets-ai-app/src/main/resources/db/migration/`：

| 脚本 | 说明 |
|------|------|
| `V001__create_pets_table.sql` | 宠物表 |
| `V002__create_diet_tables.sql` | 饮食相关表（食物库、餐食记录、饮水记录等） |
| `V003__seed_diet_data.sql` | 食物库种子数据 |

## 项目结构

```
backend/
├── pom.xml                          # 父 POM（依赖管理）
├── README.md                        # 项目文档（本文件）
├── TECHNICAL.md                     # 技术文档
├── docs/
│   ├── BOUNDED_CONTEXTS.md          # 限界上下文说明
│   ├── DDD_REFACTORING.md           # DDD 重构记录
│   └── postman/                     # Postman 测试集合
├── pets-ai-types/                   # 共享内核（异常、基类、上下文）
├── pets-ai-domain/                  # 领域层（实体、仓储接口、领域服务）
├── pets-ai-application/             # 应用层（应用服务、DTO、Command）
├── pets-ai-infrastructure/          # 基础设施层（MyBatis、安全、OSS、AI）
├── pets-ai-adapter-web/             # 适配层（REST 控制器）
└── pets-ai-app/                     # 启动模块（配置、Main Class、测试）
```

## API 概览

**Base URL:** `http://localhost:8081/pet-ai-app`

所有认证接口需携带 `Authorization: Bearer <accessToken>` 请求头。

| 模块 | 路径前缀 | 认证 | 说明 |
|------|----------|------|------|
| 认证 | `/api/v1/auth/*` | 公开 | 登录、注册、刷新、注销 |
| 用户 | `/api/v1/users/*` | 需要 | 个人信息、修改密码 |
| 对话 | `/api/v1/chat/*` | 需要 | 会话 CRUD、搜索 |
| 消息 | `/api/v1/chats/{id}/messages/*` | 需要 | 消息发送（同步/流式）、重生成、反馈 |
| 模型 | `/api/v1/models` | 公开 | 可用 AI 模型列表 |
| 文件 | `/api/v1/files/*` | 需要 | 文件上传、下载、删除 |
| 宠物 | `/api/v1/pets/*` | 需要 | 宠物 CRUD、头像上传 |
| 饮食 | `/api/v1/diet-records/*` | 需要 | 餐食记录 CRUD |
| 饮水 | `/api/v1/water-records/*` | 需要 | 饮水记录 CRUD |
| 统计 | `/api/v1/diet-stats/*` | 需要 | 每日/周营养统计 |
| 食物 | `/api/v1/foods/*` | 需要 | 食物搜索、常用食物 |
| AI 分析 | `/api/v1/ai/diet-analysis` | 需要 | AI 营养分析 |

**Swagger UI:** `http://localhost:8081/pet-ai-app/swagger-ui.html`

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "success": true,
  "timestamp": 1716700000000,
  "data": { ... }
}
```

## 模块依赖关系

```
pets-ai-app (启动)
  ├── pets-ai-adapter-web (REST 控制器)
  │     └── pets-ai-application (应用服务)
  │           └── pets-ai-domain (领域模型)
  │                 └── pets-ai-types (共享内核)
  └── pets-ai-infrastructure (基础设施实现)
        ├── pets-ai-application
        ├── pets-ai-domain
        └── pets-ai-types
```

**依赖规则（ArchUnit 强制）：**
- `domain` 不得依赖 `infrastructure` 或 `adapter-web`
- `application` 不得依赖 `infrastructure`

## 部署

### JAR 方式

```bash
cd backend
mvn clean package -DskipTests
java -jar pets-ai-app/target/pets-ai-app-1.0-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

### 环境变量（生产）

| 变量 | 说明 |
|------|------|
| `DB_URL` | PostgreSQL JDBC URL |
| `DB_USERNAME` | 数据库用户名 |
| `DB_PASSWORD` | 数据库密码 |
| `REDIS_HOST` | Redis 主机 |
| `REDIS_PORT` | Redis 端口 |
| `REDIS_PASSWORD` | Redis 密码 |
| `AI_API_KEY` | DeepSeek API Key |
| `JWT_SECRET` | JWT 签名密钥 |
| `OSS_ACCESS_KEY_ID` | 阿里云 AccessKey ID |
| `OSS_ACCESS_KEY_SECRET` | 阿里云 AccessKey Secret |

## 监控

- 健康检查：`GET /actuator/health`
- 应用信息：`GET /actuator/info`
- Prometheus 指标：`GET /actuator/prometheus`

## 协作约定

- 遵循 DDD 分层架构，领域逻辑必须在 `domain` 层
- 新增 API 需在对应 Controller 添加 OpenAPI 注解
- 数据库变更通过 `db/migration/` 添加版本化 SQL 脚本
- 配置敏感信息使用环境变量，禁止硬编码至代码
