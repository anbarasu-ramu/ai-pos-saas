# AI POS SaaS Design Document

## Purpose

This document explains the current design of AI POS SaaS as it exists in the codebase today.
It is written to help you walk through the implementation, understand how the major parts fit
together, and know where to look when changing behavior.

This is not only a roadmap document anymore. It is a guided technical walkthrough of:

- the runtime architecture
- the backend layering
- the authentication and tenant model
- the AI assistant request lifecycle
- the frontend assistant contract and rendering model
- the main extension points
- the current limitations and deferred work

The project is now centered on a read-first, tenant-aware AI operator over structured POS data.
It is not a full RAG system yet. The current AI layer is designed to safely query products,
orders, summaries, and tenant/user context, while keeping write actions tightly constrained.

## System Overview

### Runtime Components

- `frontend`
  - Angular Nx application, currently `pos-app`
  - owns login flow, admin screens, AI assistant UI, and API calls
- `backend`
  - Spring Boot application
  - owns authentication validation, tenant context, business logic, and AI orchestration
- `postgres`
  - primary transactional database
  - stores tenants, products, orders, users, and related business data
- `keycloak`
  - identity provider for login and JWT issuance
  - source of truth for authentication
- `ollama`
  - local model runtime used by the AI planner and semantic product selector
- `pgadmin`
  - local inspection tool

### Architectural Direction

The product uses a controlled AI orchestration model, not an unconstrained chatbot model.

That means:

- the frontend sends plain language requests to one backend endpoint
- the backend turns the request into an explicit plan
- the plan maps to a bounded set of known tools
- tools execute against tenant-scoped business services
- the backend returns structured payloads that the UI can render intentionally

The key idea is that the model does not directly “answer from memory” for operational data.
Instead, it selects backend capabilities and executes them safely.

## Current Product Shape

### What Is Working Today

The application already has real implementations for:

- tenant bootstrap and authentication
- tenant-aware product and order APIs
- an AI chat endpoint at `/api/ai/chat`
- AI planning through Ollama with local fallback heuristics
- tool execution for:
  - product search
  - product lookup
  - low-stock retrieval
  - product listing
  - order listing
  - order detail
  - daily sales summary
  - current user context
  - current tenant context
- confirmation-gated checkout behavior
- a frontend assistant page that renders structured read results and clarification responses

### What Is Intentionally Deferred

The following are not the focus of the current design:

- full document RAG
- pgvector-backed embedding retrieval
- PDF, FAQ, or SOP ingestion
- broad AI mutation workflows
- long-running multi-step agent state

The current AI layer is best understood as “operator AI over structured POS data”.

## Backend Design

### Backend Package Shape

The backend follows a layered structure rather than putting all logic in controllers.

Main areas:

- `com.anbu.aipos.adapters.in.web`
  - HTTP controllers and API DTOs
- `com.anbu.aipos.application`
  - service layer orchestration for products, orders, tenants, and users
- `com.anbu.aipos.core`
  - domain and port abstractions
- `com.anbu.aipos.adapters.out.persistence`
  - JPA repositories and persistence adapters
- `com.anbu.aipos.ai`
  - AI orchestration, planning, tool execution, parsing, and model integration
- `com.anbu.aipos.config`
  - security and tenant-related cross-cutting configuration

This division matters because the AI tool layer does not directly talk to the database.
It talks to application services, which keeps business rules centralized.

### Authentication and Tenant Model

Authentication is handled by Keycloak and enforced in Spring Security.

Current design:

- the frontend performs OIDC Authorization Code Flow with PKCE
- access tokens are attached to backend API calls
- the backend validates JWTs as a resource server
- tenant identity is expected in claims such as `tenant_id`
- role data is read from `realm_access.roles`

The backend builds an `AiExecutionContext` from the authenticated JWT. That context contains:

- tenant id
- tenant name if resolvable
- subject
- username
- email
- roles

This context becomes the shared tenant-scoped identity used by the AI orchestrator and tools.

## AI Assistant Design

### High-Level Flow

The AI assistant uses a request pipeline with five main stages:

1. HTTP entry
2. planning
3. confirmation check
4. tool execution
5. structured response rendering

The main code path is:

- `AiChatController`
- `AiOrchestrator`
- `OllamaClient` or `LocalAiPlanner`
- `AiToolExecutionPolicy`
- `AiToolExecutor`

### 1. HTTP Entry

The backend entrypoint is `POST /api/ai/chat`.

The controller accepts a simple request shape:

```json
{
  "message": "show low stock under 3"
}
```

The controller itself is intentionally thin. It delegates the entire decision to `AiOrchestrator`.

Responsibility of the controller:

- validate request shape
- inject authenticated `Jwt`
- return `ApiResponse<AiChatResponse>`

### 2. Orchestrator

`AiOrchestrator` is the core coordinator of the AI backend.

It performs these steps in order:

1. build `AiExecutionContext` from the JWT
2. log the request in a safe truncated form
3. plan the user message
4. handle no-tool responses directly
5. apply confirmation policy for mutating actions
6. execute tool calls sequentially
7. convert results into a single `AiChatResponse`

This class is the best place to start when tracing an assistant request end-to-end.

### 3. Planning

Planning currently has two sources:

- `OllamaClient`
  - preferred planner
  - sends a prompt containing:
    - user message
    - tenant/user context
    - allowed tools
- `LocalAiPlanner`
  - fallback planner
  - used when Ollama is unavailable

#### Ollama Planner

`OllamaClient.plan(...)` sends a prompt that instructs the model to return JSON only.
The returned JSON is parsed by `AiModelDecisionParser` into an `AiModelDecision`.

Important planner rule:

- the model can only choose from tools registered in `AiToolRegistry`

This keeps the model bounded and prevents it from inventing capabilities.

#### Local Planner

`LocalAiPlanner` is a deterministic fallback based on simple text heuristics.

It currently supports:

- checkout intent
- order detail by `order #123`
- recent orders
- low stock with default threshold
- product search
- product listing
- daily sales summary
- current user context
- current tenant context

It also handles some clarification-like cases itself:

- missing product query for search
- missing business date for generic “sales summary”

This means the assistant still works in a degraded but useful way even if Ollama is down.

### 4. Tool Registry

`AiToolRegistry` is the explicit catalog of what the model is allowed to do.

Each tool descriptor includes:

- tool name
- whether it is read-only
- description
- example arguments

Current tools:

- `SEARCH_PRODUCTS`
- `GET_PRODUCT_BY_ID`
- `GET_LOW_STOCK_PRODUCTS`
- `LIST_PRODUCTS`
- `GET_ORDERS`
- `GET_ORDER_DETAIL`
- `GET_DAILY_ORDER_SUMMARY`
- `CREATE_CHECKOUT_ORDER`
- `GET_CURRENT_USER_CONTEXT`
- `GET_CURRENT_TENANT_CONTEXT`

This registry is part prompt-building infrastructure and part safety boundary.

### 5. Confirmation Policy

`AiToolExecutionPolicy` exists to stop the model from executing writes just because it planned them.

Current rule:

- read-only tools can execute immediately
- mutating tools, especially checkout, require explicit user confirmation unless the user message already clearly confirms the action

This is why the system is described as read-first.

### 6. Tool Execution

`AiToolExecutor` maps an `AiToolCall` to real backend application services.

This class is where AI intent becomes business behavior.

It depends on:

- `ProductService`
- `OrderQueryService`
- `CheckoutUseCase`
- `TenantQueryService`
- `AiProductSelectionAgent`

The executor is responsible for:

- validating and normalizing tool arguments
- calling tenant-aware services
- shaping stable assistant result payloads
- returning clarification payloads when needed

### Structured Result Shape

Every tool now returns a top-level `type` inside `result`.
This is the key contract that lets the frontend render specific UI instead of only generic JSON.

Current result types:

- `product_search`
- `clarification`
- `low_stock`
- `product_list`
- `order_list`
- `order_detail`
- `daily_summary`
- `user_context`
- `tenant_context`
- `checkout_result`

The assistant response envelope remains:

```json
{
  "assistantMessage": "Found 2 low-stock products.",
  "intent": "GET_LOW_STOCK_PRODUCTS",
  "toolInvocations": [],
  "result": {},
  "requiresConfirmation": false
}
```

The important design decision is that `result` is still technically `unknown` at the API boundary,
but it is stable by convention through its `type` field and corresponding payload shape.

### Clarification Behavior

Clarification is now a first-class non-error outcome.

The backend uses clarification when:

- a search found no strong exact winner
- several product matches are too close in score

In that case, the backend returns:

- `requiresConfirmation = false`
- `intent = SEARCH_PRODUCTS`
- `result.type = clarification`
- a short assistant message asking the user which item they meant

This is important because clarification is not treated as a failure and not treated as a write confirmation.

### Product Retrieval Strategy

`AiProductSelectionAgent` implements the current hybrid retrieval logic.

It uses two ranking paths:

- deterministic ranking
- semantic ranking

#### Deterministic Ranking

Deterministic ranking checks:

- exact product name match
- exact category match
- name prefix
- category prefix
- name contains query
- category contains query
- token overlap

Each ranking path yields:

- confidence
- reason
- match type

Example match types:

- `EXACT_NAME`
- `EXACT_CATEGORY`
- `NAME_PREFIX`
- `CATEGORY_PREFIX`
- `NAME_CONTAINS`
- `CATEGORY_CONTAINS`
- `TOKEN_OVERLAP`

#### Semantic Ranking

Semantic ranking uses `OllamaClient.selectProducts(...)`.

Important guardrails:

- only candidate products already in scope can be returned
- unknown product IDs are discarded
- if Ollama is unavailable or invalid here, the system falls back to deterministic ranking

#### Merge Policy

The agent merges deterministic and semantic matches by product id, then keeps the better match.

This is the project’s current “RAG-lite” strategy:

- structured database retrieval first
- semantic assistance second
- no embedding infrastructure yet

### Reporting and Timezone Handling

`GET_DAILY_ORDER_SUMMARY` currently accepts:

- `date`
- `zone`

`AiToolExecutor` resolves the zone and calls `OrderQueryService.getDailySummary(...)`.

Current default:

- if `zone` is not provided, the backend falls back to `UTC`

This is intentionally simple for now. The design still leaves room for future tenant-configured business timezones.

### Observability

The orchestrator now logs:

- incoming AI request
- planning source
  - `OLLAMA`
  - `LOCAL`
- selected intent
- tool execution start
- tool success
- clarification outcomes
- execution failures

The logging policy intentionally avoids dumping full tokens or full raw prompts into logs.

## Business Service Interaction

The AI layer does not bypass the regular service layer.

Important service relationships:

- `ProductService`
  - product listing
  - product lookup
  - low-stock retrieval
  - search support
- `OrderQueryService`
  - order history
  - order detail
  - daily summary
- `CheckoutUseCase`
  - checkout execution
- `TenantQueryService`
  - tenant name lookup

This keeps AI behavior aligned with the same business rules used elsewhere in the app.

## Frontend Design

### Frontend Role

The frontend does not implement planning logic.
It acts as:

- request sender
- response renderer
- confirmation initiator

The main frontend AI files are:

- `app/core/api/pos-api.service.ts`
- `app/core/api/pos.models.ts`
- `app/features/ai/ai-assistant-page.component.ts`
- `app/features/ai/ai-assistant-page.component.html`
- `app/features/ai/ai-assistant-page.component.css`

### API Service Contract

The assistant UI calls:

- `PosApiService.chatWithAssistant(message)`

That sends:

- `POST /api/ai/chat`

and expects:

- `ApiResponse<AiAssistantResponse>`

### Client-Side Models

`pos.models.ts` defines a discriminated union for assistant results.

Important types:

- `AiAssistantResponse`
- `AiStructuredResult`
- `AiClarificationResult`
- `AiProductSearchResult`
- `AiLowStockResult`
- `AiOrderListResult`
- `AiOrderDetailResult`
- `AiDailySummaryResult`
- `AiUserContextResult`
- `AiTenantContextResult`

The design here mirrors the backend `result.type` convention.

This means the frontend can safely branch on `result.type` without guessing payload structure.

### Assistant Page Behavior

`AiAssistantPageComponent` manages:

- conversation state
- request submission
- loading state
- confirmation action
- result parsing
- result-specific highlight rendering
- raw JSON fallback display

Important computed values:

- `latestResponse`
- `latestStructuredResult`
- `latestHighlights`

The page currently renders:

- transcript bubbles
- suggestion prompts
- summary cards
- clarification option cards
- product match cards
- low-stock lists
- order lists
- order detail rows
- daily summary highlights
- user/tenant context cards
- raw JSON payload

### Confirmation UI

The frontend shows the “Confirm last action” button only when:

- `latestResponse.requiresConfirmation === true`

When clicked, the UI prepends `confirm` to the last user message and resubmits it.

This is a simple but effective contract with the backend confirmation policy.

## End-to-End Request Walkthrough

This section is the fastest way to mentally trace the code.

### Example: “show low stock under 3”

1. User types the message in the assistant page.
2. `AiAssistantPageComponent.sendMessage()` calls `PosApiService.chatWithAssistant(...)`.
3. Backend receives `POST /api/ai/chat`.
4. `AiChatController` forwards the message and JWT to `AiOrchestrator`.
5. `AiOrchestrator` builds execution context from JWT.
6. Planner chooses `GET_LOW_STOCK_PRODUCTS` with threshold `3`.
7. Confirmation policy allows immediate execution because the tool is read-only.
8. `AiToolExecutor.getLowStockProducts(...)` calls `ProductService.getLowStockProducts(...)`.
9. Executor returns:
   - `result.type = low_stock`
   - `threshold`
   - `count`
   - `items`
10. Frontend receives the response and renders the low-stock result card.

### Example: “find coffee”

1. Planner chooses `SEARCH_PRODUCTS`.
2. Executor loads catalog for the tenant.
3. `AiProductSelectionAgent` ranks product candidates.
4. If top matches are too close, executor returns:
   - `result.type = clarification`
   - `reason = AMBIGUOUS_PRODUCT_QUERY`
   - `options = [...]`
5. Frontend renders a clarification card instead of pretending there was a final answer.

### Example: “create order for 2 cappuccinos”

1. Planner chooses `CREATE_CHECKOUT_ORDER`.
2. Confirmation policy intercepts before execution.
3. Backend returns:
   - `requiresConfirmation = true`
   - confirmation-style assistant message
4. Frontend shows the confirm button.
5. Only a clearly confirmed follow-up should proceed to execution.

## Design Decisions That Matter

### 1. Read-First Before Full RAG

The project intentionally prioritizes structured operational data before document retrieval.

Why:

- products, orders, and sales already live in transactional storage
- the highest-value admin questions are about live business data
- tool calling plus service-layer guardrails is safer than free-form document QA for operations

### 2. Structured Results Over Free Text

The backend is designed to produce machine-renderable payloads, not only prose.

Why:

- safer UI rendering
- easier testing
- easier future analytics and observability
- easier to add additional cards and views later

### 3. Clarification Is a Valid Outcome

The system now treats ambiguity as part of normal operation.

Why:

- avoids false precision
- improves trust
- matches how real operators work

### 4. AI Goes Through Services, Not Around Them

All meaningful work still goes through product/order/tenant services.

Why:

- consistent business rules
- easier auditability
- reduced duplication

## Testing Strategy

The current AI design is backed by unit and component tests.

Backend tests cover:

- orchestrator behavior
- planner fallback behavior
- product selection merging and fallback
- tool executor payload shaping
- confirmation policy

Frontend tests cover:

- assistant result rendering
- clarification rendering
- confirmation CTA behavior
- assistant error handling

The practical rule is:

- if you change an AI result shape, update both the backend test and the frontend type/rendering test

## How To Extend The System

### Add a New Read-Only AI Capability

Typical steps:

1. add a new `AiTool`
2. register it in `AiToolRegistry`
3. teach `OllamaClient` prompt contract about it automatically through the registry
4. add fallback logic in `LocalAiPlanner` if needed
5. implement execution in `AiToolExecutor`
6. call an existing application service or add one
7. return a stable `result.type`
8. add a frontend type in `pos.models.ts`
9. add rendering logic in `AiAssistantPageComponent`
10. add backend and frontend tests

### Add a New Mutating Capability

Do the same as above, plus:

- mark it non-read-only
- define confirmation semantics explicitly
- make failure modes strict and user-visible
- keep guardrails in `AiToolExecutionPolicy`

### Add Full RAG Later

Full RAG should be added only when the assistant needs answers from unstructured sources such as:

- SOPs
- FAQs
- manuals
- policies
- PDFs

When that happens, it should be introduced as a separate retrieval subsystem, not mixed blindly into the current structured operator flow.

Recommended future boundary:

- keep operational data tools as-is
- add document retrieval as a separate tool or pre-tool context enrichment step
- keep grounded document answers distinguishable from live business data queries

## Known Gaps and Next Steps

The current design is intentionally useful but not final.

Main gaps:

- checkout flow is still not the focus of the assistant
- tenant/business timezone is not yet configurable in AI context
- no conversational memory beyond the current UI thread
- no RAG document ingestion pipeline
- no audit-grade AI event store yet
- planner is still prompt-and-heuristic driven rather than workflow-state driven

Recommended next work:

1. deepen read-only admin insight flows
2. improve follow-up clarification handling
3. add richer analytics tools
4. introduce tenant-configured business timezone
5. add more explicit AI audit records
6. add document RAG only when there is a real unstructured knowledge use case

## Quick Code Map

If you want to walk the code in the right order, use this sequence:

1. `backend/src/main/java/com/anbu/aipos/ai/web/AiChatController.java`
2. `backend/src/main/java/com/anbu/aipos/ai/application/AiOrchestrator.java`
3. `backend/src/main/java/com/anbu/aipos/ai/application/OllamaClient.java`
4. `backend/src/main/java/com/anbu/aipos/ai/application/LocalAiPlanner.java`
5. `backend/src/main/java/com/anbu/aipos/ai/application/AiToolRegistry.java`
6. `backend/src/main/java/com/anbu/aipos/ai/application/AiToolExecutionPolicy.java`
7. `backend/src/main/java/com/anbu/aipos/ai/application/AiToolExecutor.java`
8. `backend/src/main/java/com/anbu/aipos/ai/application/AiProductSelectionAgent.java`
9. `backend/src/main/java/com/anbu/aipos/application/product/ProductService.java`
10. `backend/src/main/java/com/anbu/aipos/application/order/OrderQueryService.java`
11. `frontend/apps/pos-app/src/app/core/api/pos.models.ts`
12. `frontend/apps/pos-app/src/app/core/api/pos-api.service.ts`
13. `frontend/apps/pos-app/src/app/features/ai/ai-assistant-page.component.ts`
14. `frontend/apps/pos-app/src/app/features/ai/ai-assistant-page.component.html`

That path will show you the assistant from HTTP entry, through planning and execution, all the way to UI rendering.
