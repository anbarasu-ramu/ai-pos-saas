#!/bin/sh
set -eu

cat <<EOF >/usr/share/nginx/html/runtime-config.js
window.__APP_CONFIG__ = {
  apiBaseUrl: "${API_BASE_URL:-http://localhost:8080}",
  keycloakBaseUrl: "${KEYCLOAK_BASE_URL:-http://localhost:8081}",
  realm: "${KEYCLOAK_REALM:-ai-pos}",
  clientId: "${KEYCLOAK_CLIENT_ID:-pos-frontend}"
};
EOF
