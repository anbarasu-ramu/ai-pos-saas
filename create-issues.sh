#!/bin/bash

create_issue () {
  gh issue create --title "$1" --body "$2"
}

# INFRA
create_issue "[INF-1] Docker setup" "Setup Docker and Docker Compose"
create_issue "[INF-2] docker-compose setup" "Create multi-service docker-compose"
create_issue "[INF-3] PostgreSQL setup" "Setup DB with volume"
create_issue "[INF-4] Ollama setup" "Setup Ollama container"
create_issue "[INF-5] Docker networking" "Configure service networking"
create_issue "[INF-6] Env configs" "Setup dev/prod configs"
create_issue "[INF-7] One-command startup" "docker-compose up"

# AUTH
create_issue "[AUTH-1] User registration" "Implement registration API"
create_issue "[AUTH-2] Login JWT" "Implement login with JWT"
create_issue "[AUTH-3] Password hashing" "Secure passwords"
create_issue "[AUTH-4] JWT filter" "Validate JWT"
create_issue "[AUTH-5] Roles" "Admin/Cashier roles"
create_issue "[AUTH-6] Secure APIs" "Spring Security"
create_issue "[AUTH-7] Tenant in JWT" "Extract tenant"
create_issue "[AUTH-8] Token expiry" "Handle expiration"

# TENANT
create_issue "[TEN-1] tenant_id column" "Add tenant_id to tables"
create_issue "[TEN-2] Tenant queries" "Tenant-aware queries"
create_issue "[TEN-3] Data isolation" "Ensure separation"
create_issue "[TEN-4] Prevent leakage" "No cross-tenant access"

# INVENTORY
create_issue "[INV-1] Product CRUD" "Create product APIs"
create_issue "[INV-2] Stock field" "Add stock to product"
create_issue "[INV-3] Restock" "Update stock"
create_issue "[INV-4] Prevent oversell" "Validate stock"
create_issue "[INV-5] Deduct stock" "On checkout"
create_issue "[INV-6] Low stock" "Detect low inventory"
create_issue "[INV-7] Validation APIs" "Inventory validation"

# ORDER
create_issue "[ORD-1] Cart" "Cart management"
create_issue "[ORD-2] Add/remove items" "Modify cart"
create_issue "[ORD-3] Create order" "Checkout flow"
create_issue "[ORD-4] Total calc" "Price calculation"
create_issue "[ORD-5] Persist order" "Save order"
create_issue "[ORD-6] Order history" "Fetch orders"
create_issue "[ORD-7] Tenant link" "Link tenant"

# AI
create_issue "[AI-1] Chat API" "Create chat endpoint"
create_issue "[AI-2] Ollama integration" "Connect AI"
create_issue "[AI-3] Prompt design" "POS prompts"
create_issue "[AI-4] JSON output" "Structured response"
create_issue "[AI-5] Error handling" "Handle bad output"

# TOOL
create_issue "[TOOL-1] Parse JSON" "Parse AI response"
create_issue "[TOOL-2] Map actions" "Service mapping"
create_issue "[TOOL-3] Validate input" "Check AI inputs"
create_issue "[TOOL-4] Execute actions" "Run backend logic"
create_issue "[TOOL-5] Return result" "Send to UI"

# RAG
create_issue "[RAG-1] Ingestion" "Load documents"
create_issue "[RAG-2] Chunking" "Split text"
create_issue "[RAG-3] Embeddings" "Generate vectors"
create_issue "[RAG-4] Store vectors" "pgvector storage"
create_issue "[RAG-5] Retrieval" "Search pipeline"
create_issue "[RAG-6] Context injection" "Add to prompt"
create_issue "[RAG-7] Grounding" "Accurate answers"

# AGENT
create_issue "[AG-1] Multi-step flow" "Task orchestration"
create_issue "[AG-2] Decision logic" "AI next step"
create_issue "[AG-3] Ask user" "Missing info"
create_issue "[AG-4] State management" "Maintain context"
create_issue "[AG-5] Tool chaining" "Multiple actions"
create_issue "[AG-6] Retry logic" "Recovery handling"