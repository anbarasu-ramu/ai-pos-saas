# ai-pos-saas

## Environment Configs

This project now separates shared Compose definitions from environment-specific overrides.

Copy the example env files before starting:

```bash
cp .env.example .env
cp .env.dev.example .env.dev
cp .env.prod.example .env.prod
```

Use the development stack for local work:

```bash
docker compose --env-file .env.dev -f docker-compose.yml -f docker-compose.dev.yml up -d
```

Use the production-oriented stack to validate `prod` profile wiring:

```bash
docker compose --env-file .env.prod -f docker-compose.yml -f docker-compose.prod.yml up -d
```

Supported local env files:
- `.env`: optional shared values used by both environments
- `.env.dev`: local development values, ports, and dev tooling credentials
- `.env.prod`: production-oriented values and external URLs

Key variables:
- Database: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_HOST`, `POSTGRES_PORT`
- Backend: `SPRING_PROFILES_ACTIVE`, `BACKEND_PORT`, `API_BASE_URL`, `APP_CORS_ALLOWED_ORIGINS`
- Keycloak: `KEYCLOAK_ADMIN`, `KEYCLOAK_ADMIN_PASSWORD`, `KEYCLOAK_BASE_URL`, `KEYCLOAK_ISSUER_URI`, `KEYCLOAK_REALM`, `KEYCLOAK_CLIENT_ID`
- Dev-only tools: `PGADMIN_DEFAULT_EMAIL`, `PGADMIN_DEFAULT_PASSWORD`, `PGADMIN_PORT`, `OLLAMA_BASE_URL`, `OLLAMA_PORT`
