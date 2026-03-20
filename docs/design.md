# AI POS SaaS Design Document

## Purpose

This document tracks the evolving technical design for AI POS SaaS and records what is already scaffolded versus what is still planned.

## Current Status

### Completed Foundation

- Docker Compose scaffolding for PostgreSQL with pgvector, Ollama, Keycloak, backend, frontend, and pgAdmin
- Spring Boot backend entrypoint and package structure
- Angular Nx frontend app renamed and scaffolded as `pos-app`
- Flyway baseline migration with tenant, product, order, stock, audit, and RAG placeholder tables
- Basic Spring Security resource server configuration
- Starter frontend routes and screens for login, dashboard, products, cart, checkout, and AI assistant
- Local Keycloak realm import with starter clients, roles, test users, and tenant claim mapping

### Still In Progress

- Frontend callback/token exchange flow after Keycloak login
- Full domain CRUD and validation rules
- Tenant isolation enforcement across all queries and writes
- Ollama integration and structured tool-calling pipeline
- RAG ingestion, embeddings, retrieval, and grounded response flow
- Observability, analytics, and end-to-end tests

## Architecture Overview

### Runtime Components

- `frontend`: Nx Angular app served in Docker through nginx
- `backend`: Spring Boot API and orchestration layer
- `postgres`: transactional store plus pgvector extension for semantic search
- `ollama`: local LLM runtime
- `keycloak`: identity provider for authentication and JWT issuance
- `pgadmin`: local database inspection tool

### Backend Structure

- `com.anbu.aipos.config`: cross-cutting configuration such as security
- `com.anbu.aipos.auth`: authentication-facing endpoints and integration points
- `com.anbu.aipos.tenant`: tenant lookup and tenant context
- `com.anbu.aipos.product`: product and inventory-facing domain scaffolding
- `com.anbu.aipos.order`: order and order item scaffolding
- `com.anbu.aipos.ai`: AI chat entrypoint and future orchestration layer
- `com.anbu.aipos.common`: shared base entities and API response types

### Frontend Structure

- `app/core`: shared client-side models and API service stubs
- `app/features/auth`: login flow shell
- `app/features/dashboard`: high-level operational dashboard shell
- `app/features/products`: product and stock management shell
- `app/features/cart`: cart interaction shell
- `app/features/checkout`: checkout and guardrail shell
- `app/features/ai`: AI assistant shell

## Design Decisions

### Authentication

- Keycloak is the source of truth for authentication
- Spring Boot is configured as an OAuth2 resource server
- Tenant identity is expected to arrive through JWT claims such as `tenant_id`
- Local development imports a predefined `ai-pos` realm with `ADMIN` and `CASHIER` roles
- A public `pos-frontend` client is used for browser login during local development

### Multi-Tenancy

- Business entities should carry `tenant_id`
- Service and repository layers should become tenant-aware by default
- Cross-tenant reads and writes must fail closed

### Data Layer

- PostgreSQL is the primary transactional store
- Flyway manages schema evolution
- pgvector is reserved for embeddings and semantic retrieval

### AI Layer

- The backend owns AI orchestration and guardrails
- Ollama is treated as the local model endpoint
- AI responses should move toward structured JSON for safe tool execution

### Frontend Delivery

- Nx builds the Angular app
- Docker serves the compiled frontend as static assets through nginx
- Feature pages are scaffolded early so API contracts can be wired incrementally

## Progress Tracker

| Area | Status | Notes |
|---|---|---|
| Infrastructure | In progress | Compose is scaffolded and validated; full end-to-end container bring-up still needs final verification |
| Authentication | In progress | Realm import, users, roles, client setup, and backend JWT role mapping now exist; frontend callback handling is still pending |
| Multi-tenancy | In progress | Base tenant-aware entity pattern exists; enforcement is still partial |
| Inventory | In progress | Product and stock schemas/entities exist; CRUD logic is not complete |
| Orders | In progress | Order scaffolding exists; checkout workflow is not implemented |
| AI chat | In progress | Placeholder endpoint exists; Ollama integration is pending |
| Tool calling | Not started | No execution pipeline yet |
| Guardrails | Not started | Only design intent and page/API placeholders exist |
| Frontend UX | In progress | Shell pages and routing exist; real state and API integration are pending |
| Analytics | Not started | No aggregation services yet |
| RAG | Not started | Placeholder tables exist; ingestion and retrieval are pending |
| Observability | Not started | Logging/audit strategy still needs implementation |
| Testing | In progress | Frontend build and backend compile/test pass; feature tests are still missing |

## Next Build Steps

1. Complete frontend callback handling and token storage/refresh flow.
2. Implement product, stock, and order CRUD with tenant-aware validation.
3. Add Ollama client integration and structured AI action parsing.
4. Introduce service-layer guardrails and audit logging.
5. Connect frontend feature pages to real backend APIs.
6. Add integration tests for auth, stock validation, and checkout flows.
