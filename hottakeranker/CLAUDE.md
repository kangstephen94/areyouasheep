# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
./gradlew build          # Compile and package
./gradlew bootRun        # Run the application
./gradlew test           # Run all tests
./gradlew clean build    # Clean rebuild
```

Run a single test class:
```bash
./gradlew test --tests "com.hottakeranker.SomeTest"
```

## Architecture

**Hot Take Ranker** is a crowdsourced ranking app. Users rank 4 options on a topic; the app aggregates results using the **Borda Count** algorithm (1st=4pts, 2nd=3pts, 3rd=2pts, 4th=1pt) and shows demographic breakdowns.

**Stack:** Spring Boot 4.0.3 · Java 21 · PostgreSQL · Spring Data JPA · Spring Security (JWT) · Redis (caching)

**Planned domain model:**
- `Topic` — a question with 4 rankable options; status: PENDING/ACTIVE/ARCHIVED; options stored as JSONB column
- `Vote` — a user's ordered ranking for one topic (one vote per user per topic)
- `User` — registered user with demographic fields (gender, ageGroup, region)
- `TopicSuggestion` — user-submitted topic proposals

**Core services:**
- `RankingAggregationService` — Borda Count aggregation over votes
- `DemographicService` — result breakdowns grouped by demographic field
- `ShareCardService` — generates Wordle-style emoji share cards (🟩 exact match, 🟨 off by 1, 🟥 off by 2+)

**Key design decisions (from build guide):**
- Options stored in a JSONB column using `@JdbcTypeCode` — not a join table
- Auth is stateless JWT via Spring Security 6 filter chain
- Redis cache uses event-driven eviction (not TTL) so results stay consistent after new votes
- Use `spring.jpa.hibernate.ddl-auto=create-drop` in dev; migrate to Flyway for prod
- Integration tests use Testcontainers against a real PostgreSQL instance

**Planned API surface:**
- `GET /api/health`
- `GET /api/topics`, `GET /api/topics/{id}/results`
- `GET /api/topics/{id}/results/demographics?groupBy={gender|ageGroup|region}`
- `POST /api/votes`, `POST /api/auth/register`, `POST /api/auth/login`
- `POST /api/topics/suggest`, `GET /api/share/{topicId}`

## Setup

Requires a local PostgreSQL database named `hottakeranker`. Add connection config to `application.properties` (not yet committed). The detailed step-by-step build plan is in `hot-take-ranker-build-guide.md`.
