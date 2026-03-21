# AI POS SaaS — Requirement Matrix

---

## 🧱 1. Infrastructure & DevOps

| ID | Requirement |
|----|------------|
| INF-1 | Docker & Docker Compose setup |
| INF-2 | Multi-service docker-compose (backend, frontend, DB, AI) |
| INF-3 | PostgreSQL with persistent volume |
| INF-4 | Ollama container setup |
| INF-5 | Internal service networking (Docker network) |
| INF-6 | Environment-based configs (dev/prod) |
| INF-7 | One-command startup (docker-compose up) |

---

## 🔐 2. Authentication & Authorization

| ID | Requirement |
|----|------------|
| AUTH-1 | User registration |
| AUTH-2 | Login with JWT |
| AUTH-3 | Password hashing |
| AUTH-4 | JWT validation filter |
| AUTH-5 | Role-based access (ADMIN, CASHIER) |
| AUTH-6 | Secure APIs via Spring Security |
| AUTH-7 | Tenant identification from JWT |
| AUTH-8 | Token expiry & refresh |

---



## 🏢 3. Multi-Tenant Architecture

| ID | Requirement |
|----|------------|
| TEN-1 | tenant_id in all business tables |
| TEN-2 | Tenant-aware repository queries |
| TEN-3 | Strict data isolation |
| TEN-4 | Prevent cross-tenant access |

---



## 🏪 4. Product & Inventory Management

| ID | Requirement |
|----|------------|
| INV-1 | Product CRUD (name, price) |
| INV-2 | Maintain stock at product level |
| INV-3 | Stock update (manual restock) |
| INV-4 | Prevent overselling |
| INV-5 | Reduce stock on order checkout |
| INV-6 | Low-stock detection |
| INV-7 | Inventory validation APIs |

---

## 🧾 5. Order & Billing System

| ID | Requirement |
|----|------------|
| ORD-1 | Cart creation & management |
| ORD-2 | Add/remove items in cart |
| ORD-3 | Order creation from cart |
| ORD-4 | Total price calculation |
| ORD-5 | Persist order + items |
| ORD-6 | Order history retrieval |
| ORD-7 | Order linked to tenant |

---

## 🤖 6. AI Chat Foundation

| ID | Requirement |
|----|------------|
| AI-1 | Chat API endpoint |
| AI-2 | Integration with Ollama |
| AI-3 | Prompt design for POS actions |
| AI-4 | Structured JSON response format |
| AI-5 | Handle malformed AI responses |

---

## ⚙️ 7. AI → Tool Calling (Execution Layer)

| ID | Requirement |
|----|------------|
| TOOL-1 | Parse AI JSON response |
| TOOL-2 | Map actions to backend services |
| TOOL-3 | Validate AI inputs (product, qty) |
| TOOL-4 | Execute safe backend operations |
| TOOL-5 | Return execution results to UI |

---

## ⚠️ 8. Guardrails & Validation

| ID | Requirement |
|----|------------|
| SAFE-1 | Reject unknown AI actions |
| SAFE-2 | Validate product existence |
| SAFE-3 | Validate stock availability |
| SAFE-4 | Prevent negative/invalid quantities |
| SAFE-5 | Graceful fallback on AI failure |

---

## 🖥️ 9. Frontend (Angular UI)

| ID | Requirement |
|----|------------|
| UI-1 | Login & auth flow |
| UI-2 | Product management UI |
| UI-3 | Cart & billing UI |
| UI-4 | Checkout flow |
| UI-5 | Chat overlay (floating UI) |
| UI-6 | Display AI messages |
| UI-7 | API integration layer |

---

## ⚡ 10. Real-time UX Enhancements

| ID | Requirement |
|----|------------|
| UX-1 | Streaming AI responses (SSE) |
| UX-2 | Typing/loading indicator |
| UX-3 | Auto-scroll chat window |

---

## 📊 11. Analytics & Insights

| ID | Requirement |
|----|------------|
| INS-1 | Sales aggregation APIs |
| INS-2 | Top-selling products |
| INS-3 | Low-stock insights |
| INS-4 | AI-based explanation of trends |

---

## 🔵 12. RAG (Retrieval-Augmented Generation)

| ID | Requirement |
|----|------------|
| RAG-1 | Document ingestion (policies, FAQs) |
| RAG-2 | Chunking strategy |
| RAG-3 | Embedding generation |
| RAG-4 | Store embeddings in pgvector |
| RAG-5 | Semantic retrieval pipeline |
| RAG-6 | Inject retrieved context into prompt |
| RAG-7 | Grounded AI responses |

---

## 🟡 13. RAG-lite (Hybrid Search)

| ID | Requirement |
|----|------------|
| RAG-L-1 | Combine DB queries + semantic search |
| RAG-L-2 | Ranking & scoring strategy |
| RAG-L-3 | Merge structured + unstructured data |
| RAG-L-4 | Fallback to deterministic DB queries |

---

## 🔴 14. Agentic Workflow

| ID | Requirement |
|----|------------|
| AG-1 | Multi-step task orchestration |
| AG-2 | AI decides next step |
| AG-3 | Ask user for missing inputs |
| AG-4 | Maintain workflow state |
| AG-5 | Chain multiple tool calls |
| AG-6 | Retry & recovery logic |

---

## 🧠 15. Observability & Debugging

| ID | Requirement |
|----|------------|
| OBS-1 | Log AI prompts |
| OBS-2 | Log AI responses |
| OBS-3 | Log executed actions |
| OBS-4 | Centralized error logging |

---

## 🧪 16. Testing Strategy

| ID | Requirement |
|----|------------|
| TEST-1 | Unit tests (services) |
| TEST-2 | API tests |
| TEST-3 | AI output validation tests |
| TEST-4 | Integration tests |

---

## 🐳 17. Production Readiness

| ID | Requirement |
|----|------------|
| PROD-1 | Config profiles (dev/prod) |
| PROD-2 | Secure secrets handling |
| PROD-3 | Health check endpoints |
| PROD-4 | Graceful shutdown |

---

## 👥 18. User Management

| ID | Requirement |
|----|------------|
| USR-1 | Admin can create cashier users |
| USR-2 | Assign roles (ADMIN, CASHIER) |
| USR-3 | Users inherit tenant_id from admin |
| USR-4 | Prevent cross-tenant user creation |
| USR-5 | List users per tenant |
| USR-6 | Disable/delete users |

---

## 🏁 Definition of Done

- User login + multi-tenant isolation works  
- Product + inventory fully functional  
- Orders processed with stock validation  
- AI can execute POS actions safely  
- AI provides insights (analytics + inventory)  
- RAG answers knowledge-based queries  
- Agent handles multi-step interactions  
- Entire system runs via Docker Compose  
