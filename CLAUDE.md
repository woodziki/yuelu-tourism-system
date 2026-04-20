# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Yuelu Mountain Tourism Recommendation System (岳麓山景点推荐系统) — a graduation project providing personalized scenic spot recommendations using User-based Collaborative Filtering. Includes an AI chatbot powered by the DeepSeek API.

The PRD at `docs/Project_PRD.md` is the single source of truth for requirements.

## Tech Stack (Strict — do not upgrade)

- **Backend**: Java 18, Spring Boot 2.7.18, MyBatis-Plus 3.5.5, MySQL 8.0
- **Frontend**: Vue 2 with Vue Router 3 (hash mode), Element UI, Axios
- **Algorithm**: Pure Java User-CF with cosine similarity (no ML libraries)

## Build & Run

```bash
# Backend (port 8081)
cd backend && mvn spring-boot:run

# Frontend dev server (port 8080, proxies /api -> localhost:8081)
cd frontend && npm run serve

# Frontend lint
cd frontend && npm run lint

# Backend package
cd backend && mvn package
```

No test suite exists. Manual API testing via `test.http` (IntelliJ HTTP Client format).

## Architecture

Standard Spring Boot layered architecture with a Vue 2 SPA frontend.

### Backend (`backend/src/main/java/com/yuelu/`)

Controller → Service (interface extending `IService<T>`) → ServiceImpl (extending `ServiceImpl<Mapper, T>`) → Mapper (extending `BaseMapper<T>`)

- **Auth**: JWT via Hutool's JWTUtil, not Spring Security. `JwtInterceptor` checks `Authorization: Bearer <token>` on all paths except a whitelist in `WebMvcConfig`. Password hashing uses BCrypt from `spring-security-crypto` only.
- **Response format**: All endpoints return `Result<T>` with `{ code, message, data }`. Codes: 200 success, 401 auth failure, 500 error.
- **Recommendation engine**: `RecommendServiceImpl` builds a user-behavior matrix (views×1.0, favorites×3.0, ratings×2.0) with time-decay, calls `RecommendUtils` for cosine-similarity CF, diversifies by tag quota (max 2 per tag), falls back to hot spots for cold start.
- **AI chat**: `AiController` uses `RestTemplate` to call DeepSeek API (`POST /ai/chat`). Config in `application.yml` under `ai.*`.
- **Queries**: All via MyBatis-Plus `LambdaQueryWrapper` or annotations — no XML mapper files.

### Frontend (`frontend/src/`)

- **HTTP client**: `utils/request.js` — Axios instance with `/api` baseURL, auto-attaches JWT, unwraps `Result.data`, handles 401 redirect.
- **State**: No Vuex — uses `localStorage` for `token`, `userId`, `username`, `nickname`.
- **Routes**: `/login`, `/` (home with recommendations), `/all`, `/favorites`, `/routes`, `/spot/:id`, `/admin/*` (spots, routes, comments, users).
- **Components call API directly** from views using `request()` — no separate API layer.

### Database

MySQL database `yuelu_tourism`. Tables prefixed with `t_`: `t_user`, `t_spot`, `t_route`, `t_route_spot`, `t_comment`, `t_favorite`, `t_view_record`. Schema with seed data in `docs/schema.sql`.

## Conventions

- **Language**: Javadoc and code comments in Chinese (academic requirement). Commit messages in Chinese.
- **Entity naming**: DB tables use `t_` prefix with snake_case; Java entities use camelCase with `@TableName` annotation.
- **Simplicity first**: Prefer simple, working code over complex patterns — this is a student project.
- **No crawlers**: Use SQL scripts for test/mock data, never web scrapers.
- **Server ports**: Backend 8081, Frontend dev 8080. Vue CLI proxy rewrites `/api/*` → backend.
