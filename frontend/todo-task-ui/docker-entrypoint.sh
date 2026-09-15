#!/bin/sh
set -e

cat > /usr/share/nginx/html/assets/config.json <<JSON
{
  "apiUrl": "${API_URL:-http://localhost:8080}",
  "issuer": "${OAUTH_ISSUER:-http://localhost:8180/jeap-oauth-mock-server}",
  "clientId": "${OAUTH_CLIENT_ID:-todo-task-ui}"
}
JSON
