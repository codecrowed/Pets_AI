# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build (skip tests)
./mvnw clean package -DskipTests

# Build (with tests)
./mvnw clean package

# Run application (default profile: dev, port 8080)
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=SpringAiServiceTest
```

Server context path: `/pet-ai-app`. Base API URL: `http://localhost:8080/pet-ai-app/api/v1/`.

## Architecture

**DDD (Domain-Driven Design) with four bounded contexts**, each following a strict layered structure:

```
{bounded-context}/
  interfaces/rest/    # REST controllers
  application/        # Application services, commands, DTOs
  domain/             # Domain models, repository interfaces, domain services
  infrastructure/     # JPA entities, Spring adapters, external integrations
```

**Bounded Contexts:**
- `identity` — User registration (invitation-code-gated), JWT auth, OAuth, user profiles
- `conversation` — Chat sessions and messages, SSE streaming for AI responses
- `ai` — AI model registry, Spring AI adapter for chat completions
- `storage` — File upload/download via Aliyun OSS
- `shared` — Cross-cutting kernel: BaseEntity, AggregateRoot, domain events, error codes, CORS/Redis config, rate limiting

## Key Patterns

- **Dual ID strategy**: Every entity has a `Long id` (internal DB PK) and a `Uid` (UUID string, exposed in APIs)
- **AggregateRoot**: Base class with `registerEvent()`/`getDomainEvents()`/`clearDomainEvents()` for domain event publishing via Spring's `ApplicationEventPublisher`
- **BaseEntity**: JPA base with auto-managed `createdAt`/`updatedAt` via `@PrePersist`/`@PreUpdate`
- **Command pattern**: Input modeled as Java records (e.g., `CreateChatCommand`, `SendMessageCommand`)
- **SSE Streaming**: `StreamingChatService` returns `Flux<ServerSentEvent<String>>` using Project Reactor
- **Rate Limiting**: Redis-backed sliding window (30 req/min normal, 10 req/min streaming) via `RateLimitFilter`
- **JWT + Redis blacklist**: Access tokens (1h) + refresh tokens (7d); logout invalidates via Redis blacklist
- **Error codes**: Centralized in `ErrorCode` enum with HTTP status mappings; messages are in Chinese

## Tech Stack

- Java 17, Spring Boot 3.4.1, Spring AI 1.0.0
- PostgreSQL (JPA/Hibernate, `ddl-auto: validate`), Redis (rate limiting + JWT blacklist)
- Spring Security 6 (stateless JWT), JJWT 0.12.6
- Aliyun OSS for file storage
- AI provider: DeepSeek API (OpenAI-compatible endpoint), with deepseek/Claude models registered in `SpringAiModelRegistry`
- Lombok, Jackson, Spring Actuator + Prometheus

## Environment Configuration

Uses Spring profiles (`application.yml` → `application-dev.yml`). Production template in `application-tmp.yml` with env vars:

`AI_API_KEY`, `AI_API_BASE_URL`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PASSWORD`, `JWT_SECRET`, `CORS_ORIGINS`, `ALIYUN_OSS_*`

## REST API Endpoints

All under `/api/v1/`:
- `AuthController` — `/auth/register`, `/login`, `/refresh`, `/logout`
- `UserController` — User profile CRUD
- `ChatController` — `/chats` CRUD
- `MessageController` — `/chats/{chatId}/messages`, `.../stream` (SSE)
- `ModelController` — `/models` (public, no auth)
- `FileController` — `/files/upload`, `/{fileId}`, `/{fileId}/download`
