# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**GBE** (Gestion Budgétaire Électronique) is the backend API for the electronic budget management system of the
Ministère des Finances (MINFI) of Cameroon. It manages budget credits (AE/CP), user mandates, and financial workflows
across administrative sections and programmes. Code, comments, enums and error messages are in French.

## Commands

```bash
docker-compose up -d             # PostgreSQL 17 on host port 5433 (db gbe_db, user/pass: username/password)
./mvnw spring-boot:run           # Start dev server on :8080 (profile "dev")
./mvnw clean package -DskipTests # Build without tests
./mvnw test                      # All tests
./mvnw test -Dtest=ClassName     # Single test class
./mvnw test -Dtest=ClassName#method
```

DB settings come from env vars `DB_URL`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`. `application.yaml` imports
them from a local `.env` file (`spring.config.import: optional:file:.env[.properties]`); see `.env.example`.
The only test is `GbeApplicationTests` (context load), which needs a reachable database.

Swagger UI: `http://localhost:8080/swagger-ui.html`.

## Architecture

**Stack:** Spring Boot 4.0.3 · Java 17 · PostgreSQL · Spring Security + JWT (jjwt, RSA) · Spring Data JPA · Lombok ·
MapStruct · SpringDoc OpenAPI · TOTP (`dev.samstevens.totp`)

**Package root:** `gov.cmr.minfi.db.gbe.app`

### Modules

| Module           | Purpose                                                                                   |
|------------------|-------------------------------------------------------------------------------------------|
| `auth/`          | Login, JWT issuance, TOTP MFA (`auth/tfa`)                                                |
| `security/`      | `SecurityConfig`, `JwtService`, `JwtFilter`, `KeyUtils` (RSA PEM loading)                 |
| `iam/`           | `RoleSysteme` enum (with default permissions), `Permission` enum, `Role` entity           |
| `user/`          | `User` entity (implements `UserDetails`)                                                  |
| `agent/`         | Government agent records                                                                  |
| `mandat/`        | User ↔ Section/Programme assignments with scoped permissions; `MandatScopeValidator`      |
| `budget/credit/` | `CreditBudgetaire` — core budget entity and its state machine                             |
| `exercice/`      | Fiscal year (`Exercice`)                                                                  |
| `referentiel/`   | Master data: `administratif` (Section, Chapitre), `programmatique` (Programme, Action), `economique` (NatureEconomique), `fonctionnel` |
| `admin/`         | Admin-only endpoints                                                                      |
| `common/`        | `BaseEntity` auditing, `BusinessException`/`ErrorCode`, `ApplicationExceptionHandler`, configs (CORS in `BeansConfig`) |

### Key Patterns

**Layering:** `Controller → Service (interface + impl/ subpackage) → Repository`; DTOs in `dto/`, entity↔DTO mapping via
MapStruct `*Mapper` interfaces.

**Two-level authorization:**
1. *Coarse, endpoint-level* — `@EnableMethodSecurity` + `@PreAuthorize("hasAuthority('<PERMISSION>')")` on controllers.
   Authorities come from `User.getAuthorities()`: the user's `Role` name plus `User.grantedPermissions` (names of
   `Permission` enum values).
2. *Fine, data-scope level* — a `Mandat` links a `User` to a `Section` (optionally a `Programme`) with a `RoleSysteme`
   and an `@ElementCollection` of `Permission`s (initialised from `RoleSysteme.getDefaultPermissions()`). Services call
   `MandatScopeValidator.checkAccessCredit(...)` / `checkAccessCreditWithPermission(...)` /
   `getProgrammeIdsAutorises(...)` to restrict operations to the current user's valid mandates.

**Budget state machine:** Lives in the `CreditBudgetaire` entity itself (implements `StatutTransitionable`,
`AEManageable`, `CPManageable`); each transition method calls `validerTransition(...)` and throws `BusinessException` on
illegal moves. `StatutCredit`: `DISPONIBLE, CANTONNE, ENGAGE, SUSPENDU, SOLDE, ANNULE`.
- `DISPONIBLE ⇄ CANTONNE` (cantonner / decantionner) — a CANTONNE credit cannot be engaged
- `DISPONIBLE → ENGAGE → SOLDE`, `ENGAGE → SUSPENDU → DISPONIBLE` (debloquer)
- AE/CP consumption (`consommerAE/CP`, `libererAE/CP`) updates consumed/available amounts and rejects overdrafts.

**Errors:** Throw `BusinessException(ErrorCode.XXX)` from services; add new codes to `ErrorCode`.
`ApplicationExceptionHandler` maps them to `ErrorResponse`.

**Auditing:** Entities extend `BaseEntity` (`createdAt/By`, `updatedAt/By`), populated through `JpaConfig` +
`ApplicatorAuditoreAware`.

**Auth flow:** Password login → if MFA enabled, short-lived MFA token → TOTP verification → access + refresh tokens.
A first-login flag forces a password change. Token lifetimes are in `app.security.jwt.*` in `application.yaml`.

### Security Config

- Stateless JWT; CSRF disabled. RSA keys loaded from classpath `/keys/local-only/{private,public}_key.pem` (hardcoded in
  `JwtService`).
- Public routes: `/api/v1/auth/**` and Swagger paths (`PUBLIC_URLS` in `SecurityConfig`); everything else requires
  `Authorization: Bearer <token>`.
- CORS origins are an explicit list in `BeansConfig.corsFilter()` (localhost:3000 + the Vercel frontend).

### Database

Schema is managed by Hibernate with `ddl-auto: update` — there are no migrations and no seed scripts. To reset, switch
to `create` and uncomment the `spring.sql.init` block in `application.yaml`.

### Docs

Functional analysis (LaTeX report, UML use-case diagrams in draw.io) is in `docs/analyse-fonctionnelle/`;
domain model/lifecycle diagram in `docs/modele-metier-et-cycle-de-vie.drawio`.
