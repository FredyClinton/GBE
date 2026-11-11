# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**GBE** (Gestion Budgétaire Électronique) is the backend API for the electronic budget management system of the
Ministère des Finances (MINFI) of Cameroon. It manages budget credits (AE/CP), user mandates, and financial workflows
across administrative sections and programmes. Code, comments, enums, commit messages and error messages are in French.

A frontend (localhost:3000 / Vercel) consumes this API: **never change existing URLs or response shapes** without
coordinating.

## Commands

```bash
docker-compose up -d             # PostgreSQL 17 on host port 5433 (db gbe_db, user/pass: username/password)
./mvnw spring-boot:run           # Start dev server on :8080 (profile "dev")
./mvnw clean package -DskipTests # Build without tests
./mvnw test                      # All tests (Docker required: integration test uses Testcontainers)
./mvnw test -Dtest=ClassName     # Single test class
./mvnw test -Dtest=ClassName#method
```

DB settings come from env vars `DB_URL`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, read from a local `.env`
file (`spring.config.import: optional:file:.env[.properties]`); see `.env.example` (also lists prod-only variables).

Swagger UI: `http://localhost:8080/swagger-ui.html`.

## Architecture

**Stack:** Spring Boot 4.0.3 · Java 17 · PostgreSQL · Flyway · Spring Security + JWT (jjwt, RSA) · Spring Data JPA ·
Lombok · MapStruct · SpringDoc OpenAPI · TOTP (`dev.samstevens.totp`) · Testcontainers

**Package root:** `gov.cmr.minfi.db.gbe.app`

### Modules

| Module                  | Purpose                                                                                 |
|-------------------------|-----------------------------------------------------------------------------------------|
| `security/`             | `SecurityConfig`, `JwtService`, `JwtFilter`, `KeyUtils` (RSA PEM loading)               |
| `identite/auth/`        | Login, JWT issuance, TOTP MFA (`tfa/`)                                                  |
| `identite/user/`        | `User` entity (implements `UserDetails`)                                                |
| `identite/agent/`       | Government agent records                                                                |
| `identite/mandat/`      | User ↔ Section/Programme assignments; `MandatScopeValidator`                            |
| `identite/iam/`         | `RoleSysteme` enum (with default permissions), `Permission` enum, `Role` entity         |
| `referentiel/`          | Master data, one sub-package per domain: `exercice`, `administratif` (Section, Chapitre), `programmatique` (Programme, Action), `economique` (NatureEconomique), `fonctionnel`. `ReferentielMapper` is shared. |
| `budget/credit/`        | `CreditBudgetaire` — core budget entity and its state machine                           |
| `admin/`                | Admin-only endpoints (user creation, role listing)                                      |
| `common/`               | `BaseEntity` auditing, `BusinessException`/`ErrorCode`, `ApplicationExceptionHandler`, configs (CORS + beans in `BeansConfig`) |

### Module conventions

Each module: `Entity`, `XxxRepository`, `XxxService` interface + `impl/XxxServiceImpl`, `XxxController`, MapStruct
`XxxMapper`, and a single `dto/` package for requests and responses. Controllers never use repositories directly.
Read-only services are annotated `@Transactional(readOnly = true)`.

Read-only referentiel endpoints are split across several controllers that all share the prefix `/api/v1/referentiel`
(e.g. `/sections/{id}/programmes` lives in `ProgrammeController`). Action CRUD is separate, under `/api/v1/admin/actions`.

### Authorization (two levels)

1. *Endpoint level*: `@EnableMethodSecurity` + `@PreAuthorize("hasAuthority('<PERMISSION>')")` on controllers.
   Authorities come from `User.getAuthorities()`: the user's `Role` name (`ROLE_<RoleSysteme>`) plus
   `User.grantedPermissions` (names of `Permission` values).
2. *Data scope level*: a `Mandat` links a `User` to a `Section` (optionally a `Programme`) with a `RoleSysteme` and a set
   of `Permission`s (initialised from `RoleSysteme.getDefaultPermissions()`). Services call `MandatScopeValidator`
   (`checkAccessCredit`, `checkAccessCreditWithPermission`, `checkSectionAccess`, `getProgrammeIdsAutorises`).
   Rule: a MINISTRE mandate without a programme covers the whole section; every other mandate covers only its programme.

### Budget state machine

Lives in the `CreditBudgetaire` entity (implements `StatutTransitionable`, `AEManageable`, `CPManageable`); each
transition calls `validerTransition(...)` and throws `BusinessException(TRANSITION_STATUT_INVALIDE)` on illegal moves.
- `DISPONIBLE ⇄ CANTONNE`: a CANTONNE credit cannot be engaged
- `DISPONIBLE → ENGAGE → SOLDE`, `ENGAGE → SUSPENDU → DISPONIBLE`
- any non-terminal status → `ANNULE`; `SOLDE` and `ANNULE` are terminal
- `consommerAE/CP` reject amounts above the available balance; `libererAE/CP` give amounts back.

Covered by `CreditBudgetaireTest`. Keep it in sync when adding transitions.

### Errors, auditing, auth flow

- Throw `BusinessException(ErrorCode.XXX, args...)` from services; add new codes to `ErrorCode`.
- Entities extend `BaseEntity` (UUID string id, `createdDate/By`, `lastModifiedAt/By`), filled by `JpaConfig` +
  `ApplicatorAuditoreAware`.
- Password login → if MFA enabled, short-lived MFA token → TOTP verification → access + refresh tokens. `firstLogin`
  forces a password change; a missing MFA secret is generated at login.

### Configuration and profiles

- `application.yaml` (common), `application-dev.yaml` (active by default), `application-prod.yaml`
  (`SPRING_PROFILES_ACTIVE=prod`).
- JWT keys: `app.security.jwt.private-key` / `public-key` are Spring `Resource`s. In dev they are
  `classpath:keys/local-only/*.pem`, which is committed to the repo and therefore **dev only**. In prod they come from
  `JWT_PRIVATE_KEY_PATH` / `JWT_PUBLIC_KEY_PATH`.
- CORS: `app.cors.allowed-origins` (comma-separated, no trailing `/`), wired into Spring Security via `.cors()` so
  preflight requests pass before authentication.
- Public routes: `/api/v1/auth/**` and Swagger (`PUBLIC_URLS` in `SecurityConfig`).

### Database (Flyway)

The schema is owned by Flyway (`src/main/resources/db/migration`); Hibernate only runs `ddl-auto: validate`.
- `V1__schema_initial.sql`: schema; `V2__donnees_referentiel_2026.sql`: 2026 reference data;
  `V3__roles_et_admin_initial.sql`: the 7 `ROLE_*` roles plus the initial super-admin `admin@gbe.cm` / `Admin@1234`
  (`first_login = true`). It is idempotent.
  `V4__contrainte_unicite_imputation_credit.sql`: unique constraint `uk_credit_imputation`.
  `V5__nature_economique_credit.sql`: adds `credit_budgetaire.nature_economique_id`. The imputation becomes
  exercice + section + programme + action + chapitre + nature économique (`UNIQUE NULLS NOT DISTINCT`). The column is
  NULL for credits created before V5, and the API requires it on creation.
  `V6__supprimer_table_engagement_orpheline.sql`: drops the orphan `engagement` table left by an abandoned draft.
- Any entity change needs a new `V<n>__description.sql`. Never edit an applied migration.
- Pre-Flyway databases get a baseline at version 2 (`baseline-on-migrate`), so only V3+ run on them.

### Tests

- `CreditBudgetaireTest`, `MandatScopeValidatorTest`: plain unit tests (Mockito, no Spring context).
- `GbeApplicationTests`: full context on a Testcontainers `postgres:17.9`. It checks the migrations, auth on protected
  routes, CORS preflight and the initial admin login.

### Docs

Functional analysis (LaTeX report, UML use-case diagrams in draw.io) is in `docs/analyse-fonctionnelle/`;
domain model/lifecycle diagram in `docs/modele-metier-et-cycle-de-vie.drawio`.
