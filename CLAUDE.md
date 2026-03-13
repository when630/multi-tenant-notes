# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Multi-Tenant Notes is a SaaS note-taking application built to learn multi-tenant architecture. It uses **shared database, shared schema, row-level isolation** with `tenant_id` columns and PostgreSQL RLS as a safety net. Tenants are identified via subdomain (`{subdomain}.localhost:3000`) and the backend receives tenant identity through the `X-Tenant-Id` header, stored in a ThreadLocal `TenantContext`.

## Build & Run Commands

The Gradle wrapper lives in `backend/`. Run all Gradle commands from there:

```bash
cd backend

# Build
./gradlew build

# Run the application
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.when630.multi_tenant_notes.SomeTest"

# Run a single test method
./gradlew test --tests "com.when630.multi_tenant_notes.SomeTest.methodName"

# Clean build
./gradlew clean build
```

## Architecture

### Multi-Module Gradle Project

The root `settings.gradle` declares planned modules:
- **backend** — Spring Boot application (main module, currently the only one with code)
- **notes-common** — Shared utilities (planned, not yet created)
- **notes-storage / notes-storage-file / notes-storage-s3** — Storage abstractions (planned, not yet created)

### Tech Stack

- **Java 17**, **Spring Boot 3.5.x**, **Gradle**
- **Spring Data JPA** + **PostgreSQL** (H2 for tests)
- **Flyway** for database migrations
- **Lombok** for boilerplate reduction
- **JWT** (Access + Refresh tokens) for authentication
- **Spring Validation** for request validation

### Package: `com.when630.multi_tenant_notes`

Base package under `backend/src/main/java/`.

### Multi-Tenancy Data Isolation (Three Levels)

1. **Application level** — All tenant-scoped entities carry `tenant_id`; repositories filter by it automatically
2. **TenantContext (ThreadLocal)** — `TenantInterceptor` extracts `X-Tenant-Id` header and stores it; `TenantBaseEntity` auto-injects on `@PrePersist`
3. **Database level** — PostgreSQL Row-Level Security policies

### Data Isolation Categories

- **Direct tenant_id**: TENANT_MEMBERS, NOTES, TAGS, COMMENTS, NOTIFICATIONS, INVITATIONS, ACTIVITY_HISTORY
- **Parent-join isolation**: NOTE_TAGS, NOTE_SHARES, NOTE_PUBLIC_LINKS (via NOTES.tenant_id)
- **Tenant-independent**: USERS, REFRESH_TOKENS

### Roles

- **SUPER_ADMIN** — System-wide, manages all tenants (no X-Tenant-Id needed)
- **OWNER** — One per tenant, full control
- **ADMIN** — Manages members and content within a tenant
- **MEMBER** — Creates and manages own notes

### Plan Limits

| Feature | Free | Pro |
|---------|------|-----|
| Notes | 50 | Unlimited |
| Members | 5 | 50 |
| External share links | No | Yes |

### DTO Naming Conventions

- `*InfoCreate` — Creation request
- `*InfoUpdate` — Update request
- `*InfoSearch` — Query/filter criteria
- `*Info` — Single item response
- `*InfoList` — Paginated list response

### API Response Envelope

```json
{ "code": "SUCCESS", "data": { ... }, "message": null }
```

Error responses use specific error codes with a message field.

## Key Design Documents

Detailed specs are in `docs/`:
- `requirements.md` — Full requirements and feature matrix
- `api-design.md` — All 40 endpoint specifications with request/response DTOs
- `erd.md` — Database schema (13 tables) with relationships
- `development-plan.md` — 11-phase implementation roadmap
- `screen-design.md` — UI/screen specifications

Always consult these docs before implementing new features to stay aligned with the planned design.
