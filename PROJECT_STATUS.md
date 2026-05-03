# Polify Backend - Project Status

Last updated: 2026-05-03

## 1) Idea / Purpose

Polify is a backend system for a "paid surveys" platform.

High-level flow:
1. User registers and logs in.
2. User sees a list of available surveys.
3. User starts an attempt for a survey.
4. User submits answers to questions of different types.
5. User completes the attempt.
6. System creates a reward (ledger entry) for that completed attempt.

MVP focus: survey execution (start/answer/resume/complete) with strict data integrity and analyzable (normalized) answers.
Future scope (not in MVP): analytics over collected answers, advanced targeting, versioning/publishing UI, etc.

## 2) Modules / Components

Conceptual layers (business architecture):
- Identity layer: users, authentication (JWT).
- Survey definition layer (immutable in MVP): surveys -> questions -> options.
- Execution layer: attempts -> answers -> (answer_text / answer_options / answer_priority).
- Financial layer: ledger entries (rewards), 1:1 with completed attempt.

Technical components in this repository:
- Spring Boot REST API (Java 21).
- PostgreSQL as source of truth.
- Flyway migrations (schema + seed).
- JWT-based stateless auth.
- Centralized error handling with a unified error format (ApiError).
- RequestId correlation (header + MDC).
- Logback logging split into application/error/audit/security logs (no PII).
- OpenAPI/Swagger UI via springdoc.

Repository structure:
- `PolifyBackend/` - backend service (this document).
- `PolifyFront/` - frontend (separate, not covered here).

## 3) Data Model (PostgreSQL)

PostgreSQL is the source of truth. Answers are NOT stored as JSON - they are normalized tables to support analytics.

Schema is created by Flyway:
- `src/main/resources/db/migration/V1__init_schema.sql`

Seed data:
- `src/main/resources/db/migration/V2__seed_data.sql` - example surveys.
- `src/main/resources/db/migration/V3__seed_all_types_survey.sql` - "E2E All Types" survey with all supported question types.
- `src/main/resources/db/migration/V4__attempts_single_in_progress_per_user.sql` - DB constraint for only one IN_PROGRESS attempt per user.

### Core tables (conceptual)

Identity:
- `users`
  - login, password_hash, phone_number (E.164), optional email, profile fields (may be extended)

Survey definition (immutable in MVP):
- `surveys`
- `questions`
  - type: TEXT | RADIO | CHECKBOX | SELECT | PRIORITY
  - position, is_required
- `question_options`
  - per-question options, not reusable across questions

Execution:
- `attempts`
  - status: IN_PROGRESS | COMPLETED | ABANDONED
  - started_at, completed_at
- `answers` (header)
  - one row per (attempt_id, question_id)
- answer details (typed):
  - `answer_text` (TEXT)
  - `answer_options` (RADIO/SELECT/CHECKBOX)
  - `answer_priority` (PRIORITY with ranks)

Financial:
- `ledger_entries`
  - one row per attempt (unique attempt_id), stores payout amount in bani, currency (MDL), status.

### Critical integrity rules enforced (DB + service layer)

- Strict FK integrity between all layers.
- Surveys are treated as immutable after publish in MVP (no update endpoints).
- Answers are normalized and validated by type, not free-form blobs.
- Options must belong to the exact question (prevents cross-survey injections).
- One payout per attempt: ledger has UNIQUE constraint on attempt_id.
- Attempt TTL: an IN_PROGRESS attempt expires after configured time and becomes ABANDONED.
- Only one IN_PROGRESS attempt per user at a time (enforced by DB partial unique index + service guard).

## 4) Backend API (Spring Boot)

Base package: `org.example.polify`

Controllers (public REST endpoints):
- `org.example.polify.auth.AuthController` - `/auth/*`
- `org.example.polify.auth.MeController` - `/me`
- `org.example.polify.survey.SurveyController` - `/surveys/*`
- `org.example.polify.attempt.AttemptController` - `/attempts/*`
- `org.example.polify.ledger.LedgerController` - `/ledger/*`

Swagger:
- UI: `/swagger-ui/index.html`
- OpenAPI JSON: `/v3/api-docs`
- Config: `src/main/java/org/example/polify/common/openapi/OpenApiConfig.java`

### 4.1 Auth

Endpoints:
- `POST /auth/register` (201)
  - Creates user and returns JWT access token.
- `POST /auth/login` (200)
  - Validates credentials and returns JWT access token.
- `GET /me` (200)
  - Returns current authenticated principal (userId, login).

JWT:
- Stateless, sent as `Authorization: Bearer <token>`.
- Config: `polify.jwt.*` in `src/main/resources/application.yaml`.
- Security rules: `/auth/**` and `/surveys/**` are public; all other endpoints require JWT.
  - Config: `src/main/java/org/example/polify/auth/SecurityConfig.java`

DTO:
- `RegisterRequest`, `LoginRequest`, `AuthResponse` in `org.example.polify.auth.dto`.

### 4.2 Surveys (read-only in MVP)

Endpoints:
- `GET /surveys` (200)
  - Returns list of surveys (short fields).
- `GET /surveys/{id}` (200/404)
  - Returns full survey structure: questions + options in correct order.

DTO:
- `SurveyListItem`, `SurveyDetailsResponse` in `org.example.polify.survey.dto`.

Note: There are no "create/update survey" endpoints in MVP - surveys are provided by seed migrations for now.

### 4.3 Attempts (execution flow)

Attempt TTL:
- Config: `polify.attempts.ttl-seconds` in `src/main/resources/application.yaml`
  - Current value: 14400 seconds (4 hours).

Endpoints:
- `POST /attempts/start` (201/404/409)
  - Starts an IN_PROGRESS attempt for a survey.
  - Denies start if user already has any IN_PROGRESS attempt (global rule).
- `POST /attempts/{id}/answers` (204/400/404/409)
  - Submits or updates answer for one question in that attempt.
  - Enforces strict payload validation per question type.
  - Enforces sequential required rule: cannot answer a later question if earlier required questions are missing.
- `GET /attempts/{id}` (200/404)
  - Returns attempt details and saved answers.
  - Returns `nextQuestionId` for resume UX: first required question that is still incomplete.
  - Also applies TTL guard (may auto-mark IN_PROGRESS as ABANDONED before returning).
- `GET /attempts` (200)
  - Lists user attempts (optionally filtered by `surveyId`).
- `GET /attempts/active?surveyId=...` (200)
  - Returns current IN_PROGRESS attempt for the survey, or null.
- `POST /attempts/{id}/complete` (204/400/409)
  - Validates all required answers are present.
  - Marks attempt COMPLETED.
  - Creates ledger entry atomically in the same transaction (reward amount selected from `surveys`).

Core attempt logic:
- Service: `src/main/java/org/example/polify/attempt/AttemptService.java`
- TTL/state guard: `src/main/java/org/example/polify/attempt/AttemptStateGuard.java`

Attempt status behavior:
- IN_PROGRESS: user can submit/update answers and complete.
- COMPLETED: answering/complete again is rejected with 409.
- ABANDONED: answering/complete is rejected with 409 ("expired, start again").

### 4.4 Ledger (rewards, read-only in MVP)

Endpoint:
- `GET /ledger?attemptId=...` (200)
  - Lists ledger entries for current user (optional filter by attempt).

DTO:
- `LedgerEntryResponse` in `org.example.polify.ledger.dto`.

## 5) Answer Types and Payload Validation

All validation is done in the service layer (not DB triggers).

Submit answer payload (`POST /attempts/{id}/answers`) uses a single DTO with fields that must match the question type:
- TEXT:
  - allowed: `textValue` (trimmed), max 500 chars.
  - forbidden: `optionId`, `optionIds`, `priority`.
  - if required: text must be non-empty.
- RADIO / SELECT:
  - allowed: `optionId` (single).
  - forbidden: `textValue`, `optionIds`, `priority`.
  - if required: must provide exactly one option.
- CHECKBOX:
  - allowed: `optionIds` (list).
  - forbidden: `textValue`, `optionId`, `priority`.
  - if required: list must contain at least one option.
- PRIORITY:
  - allowed: `priority` list of `{optionId, rank}`.
  - forbidden: `textValue`, `optionId`, `optionIds`.
  - required PRIORITY rule (current MVP rule): must rank ALL active options with ranks 1..N (no gaps, no duplicates).

Additionally, sequential required rule:
- User cannot submit an answer to a later question position if there exists an earlier required question without required details.

## 6) Error Handling (Unified ApiError)

All errors are returned in a single JSON format (no try/catch in controllers):
```json
{
  "timestamp": "2026-04-28T14:30:12.123Z",
  "status": 409,
  "error": "Conflict",
  "code": "ATTEMPT_NOT_ALLOWED",
  "message": "You already completed this survey",
  "path": "/attempts/123/complete",
  "requestId": "7fd7c4b3-1798-4a7a-b2be-8c17b0e1b9df",
  "details": []
}
```

Components:
- DTO: `src/main/java/org/example/polify/common/error/ApiError.java`
- Field validation details: `src/main/java/org/example/polify/common/error/FieldErrorResponse.java`
- Error codes: `src/main/java/org/example/polify/common/error/ErrorCode.java`
- Central handler: `src/main/java/org/example/polify/common/error/GlobalExceptionHandler.java`

Auth errors:
- 401 is produced by `ApiAuthenticationEntryPoint` (JSON ApiError).
- 403 is produced by `ApiAccessDeniedHandler` (JSON ApiError).

404/405/type mismatch:
- Configured to also return ApiError for unknown routes and unsupported methods.
  - `spring.mvc.throw-exception-if-no-handler-found=true`
  - `spring.web.resources.add-mappings=false`

## 7) RequestId Correlation

Every HTTP response includes header `X-Request-Id`.
If client provides `X-Request-Id`, it is reused; otherwise UUID is generated.

It is included:
- in response header `X-Request-Id`
- in `ApiError.requestId`
- in logs via MDC

Filter:
- `src/main/java/org/example/polify/common/request/RequestIdFilter.java`

Additional MDC enrichment:
- `src/main/java/org/example/polify/common/request/MdcEnrichmentFilter.java`
  - adds `path`, `ip`, `userAgent` to MDC.

## 8) Logging (No PII)

Logging is configured in:
- `src/main/resources/logback-spring.xml`

Rules:
- Do not log sensitive data (passwords, tokens, phone numbers, raw email).
- Avoid logging answer content / option selections (too noisy).
- Store logs near the jar in `logs/`.
- Retention: 14 days (time-based rolling, gzip).
- Async appenders enabled to reduce request latency impact.

Log files:
1. `logs/application.log`
   - general application/system events.
2. `logs/error.log`
   - ERROR-level only (plus stack traces). Intended for real failures, not normal 4xx business flow.
3. `logs/audit.log`
   - important business events: attempt started/completed, ledger created, expiration events.
4. `logs/security.log`
   - auth/security events: login/register success/fail, 401/403, includes ip/userAgent; email is hashed if present.

Structured business/security loggers:
- `src/main/java/org/example/polify/common/log/AuditLogger.java` (logger name `AUDIT`)
- `src/main/java/org/example/polify/common/log/SecurityLogger.java` (logger name `SECURITY`)

## 9) Privacy / Legal / Safety Notes

Project intention: minimize legal risk by minimizing data exposure.

Guidelines enforced by design and expected by contributors:
- Do not expose PII in logs (especially phone number). Tokens/passwords must never be logged.
- API error messages must not leak DB/internal details (no stack traces to clients).
- Keep user-facing messages safe and generic.
- PostgreSQL is the source of truth; avoid duplicating or exporting sensitive user data unnecessarily.

## 10) Current MVP Status (What Works)

Ready now (end-to-end):
- Registration/login with JWT.
- Survey listing and survey details retrieval.
- Attempt lifecycle:
  - start attempt
  - submit answers with strict type validation
  - resume attempt via `GET /attempts/{id}` using `nextQuestionId`
  - complete attempt
  - create ledger entry for reward in same transaction
- TTL expiration guard:
  - expired attempts are auto-marked ABANDONED upon interaction
  - answering/completing expired or completed attempts is blocked with 409
- Unified ApiError responses with RequestId.
- Swagger UI with endpoint + schema descriptions.
- Log splitting into application/error/audit/security without PII.

## 11) Known Gaps / Next Steps

Backend (logical next steps):
- Implement "create/publish survey" endpoints (currently surveys are seeded only).
- Add role/permission model if needed (currently most protected endpoints are only "authenticated").
- Add automated tests:
  - integration tests with Testcontainers(Postgres) for full attempt flow and error contracts.
- Consider improving `GET /attempts/active` response to return an explicit JSON `null` vs empty body consistently for all clients.

Frontend:
- Build UI around:
  - auth
  - survey list/details
  - attempt runner that uses `nextQuestionId` + saved answers for resume
  - error handling using `ApiError.code` + `requestId` for support

