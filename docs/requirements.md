# AI POS SaaS Requirements

## Purpose

This document defines the current product and technical requirements for AI POS SaaS.
It is intentionally aligned with the implemented codebase, not just the original wishlist.

Use this document for three things:

- to understand what the system is supposed to do today
- to distinguish completed scope from partial or deferred scope
- to evaluate future work against the current product direction

The current product direction is:

- multi-tenant POS application
- Keycloak-backed authentication
- tenant-aware product and order operations
- read-first AI operator for Tenant Admins over structured POS data
- no full RAG requirement in the current milestone

## Product Scope

### Primary Users

- `TENANT_ADMIN`
  - owner or manager
  - manages products, inventory, users, orders, and AI-assisted operational insight
- `CASHIER`
  - uses day-to-day sales and checkout functions
  - currently not the primary audience for the AI milestone

### Current AI Scope

The current AI milestone is intentionally limited to a read-first admin copilot.

The assistant must reliably support:

- low-stock inspection
- product search
- recent order history
- order detail lookup
- daily sales summary
- current user context
- current tenant context

The assistant may support checkout only behind explicit confirmation.

The assistant must not behave like a general-purpose hallucination-prone chatbot for business operations.

## Requirement Status Legend

- `Completed`
  - implemented and verified in the current codebase
- `In Progress`
  - partially implemented or implemented with meaningful gaps
- `Deferred`
  - intentionally not in the current milestone
- `Planned`
  - expected later but not yet implemented

## 1. Infrastructure and DevOps

| ID | Requirement | Status | Notes |
|---|---|---|---|
| INF-1 | Docker and Docker Compose setup | Completed | Local multi-service development environment exists. |
| INF-2 | Multi-service runtime for backend, frontend, database, auth, and AI | Completed | Compose includes backend, frontend, PostgreSQL, Keycloak, Ollama, and pgAdmin. |
| INF-3 | PostgreSQL with persistent storage | Completed | Postgres is the primary transactional store. |
| INF-4 | Local Ollama runtime | Completed | Backend AI layer integrates with Ollama. |
| INF-5 | Internal service networking | Completed | Docker services are wired for local development. |
| INF-6 | Environment-based configuration | In Progress | Core environment variables exist, but production hardening is still limited. |
| INF-7 | One-command local startup | Completed | Compose-based startup is available. |

## 2. Authentication and Authorization

| ID | Requirement | Status | Notes |
|---|---|---|---|
| AUTH-1 | Tenant bootstrap registration | Completed | Backend creates tenant plus first tenant admin through app-managed registration. |
| AUTH-2 | Browser login with JWT/OIDC | Completed | Frontend uses Authorization Code Flow with PKCE against Keycloak. |
| AUTH-3 | Password handling through identity provider | Completed | Password lifecycle is handled via Keycloak rather than custom backend hashing logic. |
| AUTH-4 | Backend JWT validation | Completed | Spring Security resource server validates bearer tokens. |
| AUTH-5 | Role-based authorization | In Progress | Roles exist and are propagated, but not every screen and endpoint is fully hardened yet. |
| AUTH-6 | Secure APIs through Spring Security | Completed | Authenticated API flows are enforced for protected endpoints. |
| AUTH-7 | Tenant identification from JWT | Completed | Tenant identity is read from claims like `tenant_id`. |
| AUTH-8 | Token refresh and renewal | Deferred | Session restoration exists, but refresh flow is not the current milestone. |

## 3. Multi-Tenant Architecture

| ID | Requirement | Status | Notes |
|---|---|---|---|
| TEN-1 | `tenant_id` or equivalent tenant scoping in business tables | Completed | Tenant-aware data model exists for core business entities. |
| TEN-2 | Tenant-aware repository and service queries | In Progress | Core product and order flows are tenant-aware; full coverage still needs continued review. |
| TEN-3 | Strict cross-tenant isolation | In Progress | The design intent is clear and many flows are scoped, but this remains a high-value verification area. |
| TEN-4 | Tenant identity in user context and AI execution | Completed | AI execution context is built from JWT tenant data and used across tools. |

## 4. Product and Inventory Management

| ID | Requirement | Status | Notes |
|---|---|---|---|
| INV-1 | Product create, update, activate, deactivate | In Progress | Core service and controller behavior exists. |
| INV-2 | Stock tracked at product level | Completed | Product stock quantity is part of the domain and response models. |
| INV-3 | Admin stock updates through product editing | In Progress | Supported through product update flow, though UX can still be refined. |
| INV-4 | Prevent invalid stock mutations | In Progress | Domain and service validations exist, but broader scenario coverage can be improved. |
| INV-5 | Reduce stock during checkout | In Progress | Checkout path exists and is tied to stock changes. |
| INV-6 | Low-stock detection | Completed | Backend exposes deterministic low-stock retrieval by threshold. |
| INV-7 | Inventory validation APIs and admin visibility | In Progress | Core endpoints exist; broader reporting and alerting are still limited. |

## 5. Orders and Billing

| ID | Requirement | Status | Notes |
|---|---|---|---|
| ORD-1 | Cart state in frontend | In Progress | Client-side cart exists for current flows. |
| ORD-2 | Add and remove items from cart | Completed | Frontend cart interactions are implemented. |
| ORD-3 | Checkout order creation | In Progress | Backend checkout exists; broader workflow polish is still needed. |
| ORD-4 | Total price calculation | Completed | Order and cart totals are calculated and surfaced. |
| ORD-5 | Persist order and items | Completed | Order and order item persistence exists. |
| ORD-6 | Order history retrieval | Completed | Recent orders and order detail are supported. |
| ORD-7 | Tenant-linked orders | Completed | Order retrieval and execution are tenant-scoped. |

## 6. AI Chat Foundation

| ID | Requirement | Status | Notes |
|---|---|---|---|
| AI-1 | Single backend chat endpoint | Completed | `POST /api/ai/chat` is the current assistant entrypoint. |
| AI-2 | Ollama integration | Completed | Ollama is used for planning and semantic product matching. |
| AI-3 | Prompt design for bounded POS actions | Completed | Prompt includes available tools, user context, and output shape rules. |
| AI-4 | Structured AI response contract | Completed | Assistant returns `assistantMessage`, `intent`, `toolInvocations`, `result`, and `requiresConfirmation`. |
| AI-5 | Handle malformed model responses safely | Completed | Invalid model output is rejected and converted to non-executing safe responses. |

## 7. AI Tool Calling and Execution

| ID | Requirement | Status | Notes |
|---|---|---|---|
| TOOL-1 | Parse AI planner JSON into executable tool calls | Completed | Model output is parsed into `AiModelDecision` and `AiToolCall`. |
| TOOL-2 | Map tool calls to backend services | Completed | `AiToolExecutor` bridges AI tools to product, order, tenant, and checkout services. |
| TOOL-3 | Validate AI inputs before execution | Completed | Executor validates ids, thresholds, checkout items, and payment inputs. |
| TOOL-4 | Execute read operations safely | Completed | Current AI milestone fully supports read-first operational tools. |
| TOOL-5 | Return structured execution results to UI | Completed | Each major read tool returns a stable `result.type` payload. |

## 8. Guardrails and Validation

| ID | Requirement | Status | Notes |
|---|---|---|---|
| SAFE-1 | Restrict AI to known tools only | Completed | Tool registry is the allowed capability boundary. |
| SAFE-2 | Validate product existence | Completed | Product lookups and checkout resolution validate product existence. |
| SAFE-3 | Validate stock-aware operations | In Progress | Core validation exists; broader operational edge-case coverage can improve. |
| SAFE-4 | Prevent invalid quantities and malformed checkout input | Completed | Executor rejects missing or invalid quantity data. |
| SAFE-5 | Graceful AI fallback on model failure | Completed | Local planner fallback and safe text response behavior are in place. |
| SAFE-6 | Require confirmation for mutating AI actions | Completed | Checkout remains confirmation-gated. |
| SAFE-7 | Clarify ambiguous requests instead of guessing | Completed | Product search can return a clarification result rather than a wrong answer. |

## 9. Frontend Application

| ID | Requirement | Status | Notes |
|---|---|---|---|
| UI-1 | Login and auth session flow | Completed | Frontend login, callback handling, token storage, and authenticated API use exist. |
| UI-2 | Product management screen | In Progress | Product and inventory pages exist and are wired to backend behavior. |
| UI-3 | Cart and billing screens | In Progress | Cart and checkout UX exists with room for refinement. |
| UI-4 | Orders screen | Completed | Orders listing and detail-oriented UX are present. |
| UI-5 | Dedicated AI assistant screen | Completed | Assistant is a real page, not only a placeholder shell. |
| UI-6 | Render AI assistant messages | Completed | Transcript and assistant messaging are implemented. |
| UI-7 | Render typed structured AI results | Completed | Assistant page branches on `result.type` and renders specific UI cards. |
| UI-8 | API integration layer | Completed | Shared API services and response models exist. |

## 10. AI UX and Interaction Quality

| ID | Requirement | Status | Notes |
|---|---|---|---|
| UX-1 | Loading and in-flight assistant feedback | Completed | Assistant page shows working state while requests are in progress. |
| UX-2 | Confirmation CTA for mutating actions | Completed | Confirm button is shown only when backend requests confirmation. |
| UX-3 | Clarification rendering in UI | Completed | Ambiguous product search options are rendered intentionally. |
| UX-4 | Raw JSON fallback for debugging | Completed | Assistant page still exposes raw payload view for debugging and development. |
| UX-5 | Streaming assistant responses | Deferred | No SSE or streaming delivery in current milestone. |

## 11. Analytics and Operational Insight

| ID | Requirement | Status | Notes |
|---|---|---|---|
| INS-1 | Daily sales aggregation | Completed | Backend supports daily order summary. |
| INS-2 | Low-stock operational insight | Completed | Assistant and backend can retrieve low-stock items by threshold. |
| INS-3 | Order history insight for admins | Completed | Recent orders and order detail support admin review flows. |
| INS-4 | Trend explanation and richer analytics narratives | Planned | Current assistant is operational, not yet an advanced analytics narrator. |
| INS-5 | Top-selling product analytics | Planned | Not currently implemented as a dedicated AI tool or reporting flow. |

## 12. Current AI Milestone: Read-First Tenant Admin Copilot

This section defines the most important current requirement set. These are the requirements the implementation should optimize for before new AI scope is added.

| ID | Requirement | Status | Notes |
|---|---|---|---|
| ADM-AI-1 | Natural language low-stock retrieval | Completed | Example: `get me low stock products under 20`. |
| ADM-AI-2 | Natural language product search | Completed | Uses hybrid deterministic plus semantic ranking. |
| ADM-AI-3 | Natural language order list and order detail | Completed | Supports recent order review and specific order lookup. |
| ADM-AI-4 | Natural language daily sales summary | Completed | Supports explicit date and safe defaults for obvious cases. |
| ADM-AI-5 | Structured result payloads for each read intent | Completed | Required for stable frontend rendering. |
| ADM-AI-6 | Clarification instead of wrong product guess | Completed | Ambiguous product search produces `clarification` result type. |
| ADM-AI-7 | Deterministic handling for obvious operational prompts | Completed | Explicit prompts like low-stock requests are resolved locally before model planning. |
| ADM-AI-8 | Keep mutation scope narrow | Completed | No new broad AI mutation surface is added in this milestone. |

## 13. RAG-lite and Retrieval

The project currently supports a limited hybrid retrieval strategy, but not full document RAG.

| ID | Requirement | Status | Notes |
|---|---|---|---|
| RAG-L-1 | Combine deterministic database retrieval with semantic assistance | Completed | Product selection uses deterministic ranking plus semantic fallback. |
| RAG-L-2 | Provide explicit ranking and scoring behavior | Completed | Match confidence, reason, and match type are included. |
| RAG-L-3 | Fallback to deterministic retrieval when model help is unavailable | Completed | Product selection degrades gracefully without Ollama. |
| RAG-L-4 | Use retrieval for structured POS data first | Completed | Current AI milestone is built around structured operational data. |
| RAG-L-5 | Merge structured and unstructured retrieval | Deferred | Unstructured document retrieval is not part of the current milestone. |

## 14. Full RAG

Full RAG is not a current delivery requirement.

It becomes a requirement only when the assistant must answer from unstructured sources such as:

- store SOPs
- FAQs
- policy documents
- vendor catalogs
- PDFs

| ID | Requirement | Status | Notes |
|---|---|---|---|
| RAG-1 | Document ingestion pipeline | Deferred | Not part of current milestone. |
| RAG-2 | Chunking strategy | Deferred | Not part of current milestone. |
| RAG-3 | Embedding generation | Deferred | Not part of current milestone. |
| RAG-4 | pgvector-backed embedding storage | Deferred | Not part of current milestone. |
| RAG-5 | Semantic retrieval over document chunks | Deferred | Not part of current milestone. |
| RAG-6 | Prompt grounding from retrieved documents | Deferred | Not part of current milestone. |
| RAG-7 | Grounded document answers with source-aware behavior | Deferred | Not part of current milestone. |

## 15. Agentic Workflow

| ID | Requirement | Status | Notes |
|---|---|---|---|
| AG-1 | Multi-step planning over known tools | In Progress | The backend already supports tool planning, but not full stateful workflows. |
| AG-2 | Ask user for missing information | Completed | Clarification and missing-input behavior exist for several flows. |
| AG-3 | Chain multiple backend steps automatically | Planned | Current design prefers single-tool plans unless clearly necessary. |
| AG-4 | Maintain workflow state across multi-turn operations | Deferred | No durable agent state yet. |
| AG-5 | Retry and recovery logic for longer workflows | Planned | Not a major current capability. |

## 16. Observability and Debugging

| ID | Requirement | Status | Notes |
|---|---|---|---|
| OBS-1 | Log incoming AI requests safely | Completed | Orchestrator logs request metadata with truncated message content. |
| OBS-2 | Log planner source and intent | Completed | Logs distinguish local deterministic, local fallback, and Ollama paths. |
| OBS-3 | Log tool execution and failures | Completed | Tool invocation outcomes are logged. |
| OBS-4 | Avoid logging secrets and tokens | Completed | Current logs avoid raw auth token dumping. |
| OBS-5 | Centralized audit-grade AI event history | Planned | Not yet implemented as a formal audit store. |

## 17. Testing Strategy

| ID | Requirement | Status | Notes |
|---|---|---|---|
| TEST-1 | Unit tests for backend services and AI orchestration | Completed | AI planner, orchestrator, selection, and tool execution tests exist. |
| TEST-2 | Frontend component tests for assistant rendering | Completed | Assistant rendering and interaction tests exist. |
| TEST-3 | AI output validation tests | Completed | Parser and structured behavior tests are present. |
| TEST-4 | Full API integration tests | In Progress | Some coverage exists, but end-to-end AI flows can still expand. |
| TEST-5 | End-to-end admin AI acceptance scenarios | In Progress | Not yet fully formalized across the whole stack. |

## 18. Production Readiness

| ID | Requirement | Status | Notes |
|---|---|---|---|
| PROD-1 | Environment-specific configuration | In Progress | Current configuration supports local and configurable deployment paths. |
| PROD-2 | Secure secret handling | In Progress | Better production secret management is still needed. |
| PROD-3 | Health checks | Completed | Health endpoints are exposed. |
| PROD-4 | Graceful shutdown and operational resilience | In Progress | Baseline platform support exists, but more production polish is needed. |
| PROD-5 | Bundle and performance budget review | In Progress | Frontend currently builds successfully but exceeds the configured initial bundle budget. |

## 19. User Management

| ID | Requirement | Status | Notes |
|---|---|---|---|
| USR-1 | Admin can create additional tenant users | In Progress | User creation flow exists. |
| USR-2 | Roles assigned through identity provider model | Completed | Roles are managed through Keycloak realm roles. |
| USR-3 | Users inherit tenant scope from admin provisioning flow | Completed | Tenant-aware user provisioning exists. |
| USR-4 | Prevent cross-tenant user creation | In Progress | This is the design intent and should remain a verification focus. |
| USR-5 | Tenant user listing | Planned | Not yet clearly implemented as a complete admin capability. |
| USR-6 | Disable or delete users | Planned | Not yet implemented as a complete admin capability. |

## Functional Acceptance Criteria

The current milestone is considered functionally correct when all of the following are true:

- authenticated tenant users can reach protected backend APIs through Keycloak-issued bearer tokens
- core business reads are tenant-scoped
- admins can inspect products, stock, orders, and daily sales
- the AI assistant can safely answer operational read questions
- explicit low-stock prompts route to low-stock retrieval rather than generic product search
- ambiguous product queries trigger clarification rather than incorrect answers
- checkout remains confirmation-gated
- frontend renders typed assistant results, not only free-form text
- backend and frontend automated tests for the assistant pass

## Out of Scope for This Milestone

These items are intentionally not required for the current milestone:

- full document RAG
- document ingestion and embeddings
- streaming AI responses
- long-lived agent workflow state
- broad autonomous mutation by the assistant
- full analytics suite with advanced trend explanation
- production-grade refresh-token lifecycle

## Recommended Next Requirement Increments

After the current read-first admin copilot milestone, the next most valuable requirement increments are:

1. richer analytics tools for admins
2. better follow-up clarification handling across multiple turns
3. tenant-configured business timezone for reporting
4. stronger audit trail for AI decisions and execution
5. broader end-to-end test coverage
6. full RAG only when unstructured document retrieval becomes a real product need
