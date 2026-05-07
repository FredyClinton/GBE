# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**GBE** (Gestion Budgétaire Électronique) is the backend API for the electronic budget management system of the
Ministère des Finances (MINFI) of Cameroon. It manages budget credits, user mandates, and financial workflows across
administrative sections and programs.

## Commands

```bash
# Build
./mvnw clean package             # Full build with tests
./mvnw clean package -DskipTests # Build without tests

# Run
./mvnw spring-boot:run           # Start dev server (requires PostgreSQL)
docker-compose up -d             # Start PostgreSQL dependency

# Test
./mvnw test                      # All tests
./mvnw test -Dtest=ClassName     # Single test class
```

Environment variables required: `DB_URL`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` (see `.env.example`).

API docs available at `http://localhost:8080/swagger-ui.html` when running.

## Architecture

**Stack:** Spring Boot 4.0.3 · Java 17 · PostgreSQL · Spring Security + JWT (RSA) · Spring Data JPA · Lombok · SpringDoc
OpenAPI

**Package root:** `gov.cmr.minfi.db.gbe.app`

### Module Layout

| Module           | Purpose                                                                                            |
|------------------|----------------------------------------------------------------------------------------------------|
| `auth/`          | Login, JWT issuance, TOTP-based MFA                                                                |
| `security/`      | `JwtService`, `JwtFilter`, RSA key loading                                                         |
| `iam/`           | `RoleSysteme` enum, `Permission` entities                                                          |
| `user/`          | User entity (implements `UserDetails`)                                                             |
| `agent/`         | Government agent records                                                                           |
| `mandat/`        | User ↔ Section/Programme assignments with scoped permissions                                       |
| `budget/credit/` | `CreditBudgetaire` — core budget entity                                                            |
| `referentiel/`   | Master data: `Section`, `Programme`, `Action`, `Chapitre`, `NatureEconomique`, `Exercice`          |
| `admin/`         | Admin-only endpoints (role listing, etc.)                                                          |
| `common/`        | `BaseEntity` (auditing), `BusinessException`/`ErrorCode`, global exception handler, Spring configs |

### Key Architectural Patterns

**Layered structure per module:** `Controller → Service (interface + impl) → Repository → Entity/DTO`

**Authorization model:** A `Mandat` links a `User` to a `Section` (and optionally a `Programme`) with a `RoleSysteme`
and a set of fine-grained `Permission`s. The `MandatScopeValidator` enforces that operations stay within the user's
mandated scope.

**Budget state machine:** `CreditBudgetaire` implements `StatutTransitionable`, `AEManageable`, and `CPManageable`.
Status transitions: `DISPONIBLE → ENGAGE → ORDONNANCE → PAYEE` or `CANTONNE` (locked). Business rules are enforced in
`CreditBudgetaireServiceImpl`.

**Exception handling:** Throw `BusinessException(ErrorCode.XXX)` from service layer; `ApplicationExceptionHandler`
translates to HTTP responses.

**Auditing:** All entities extend `BaseEntity` which provides `createdAt`, `createdBy`, `updatedAt`, `updatedBy` via
Spring Data JPA auditing.

**Authentication flow:** Password login → if MFA enabled, issue short-lived MFA token → TOTP verification → issue
access + refresh tokens. First-login flag triggers password change flow.

### Security Config

- Stateless JWT sessions; RSA key pair loaded via `KeyUtils`
- Public routes: `/api/v1/auth/**`, Swagger endpoints
- CORS: `localhost:3000` and `*.vercel.app`
- Protected routes require `Authorization: Bearer <token>`

### Data Initialization
