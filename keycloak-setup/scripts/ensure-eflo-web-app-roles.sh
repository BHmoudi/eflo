#!/usr/bin/env bash
set -euo pipefail

# Ensures eflo-web-app client has roles in access tokens
# - Adds default client scope "roles"
# - Adds/ensures a realm role mapper that writes to claim "roles"
# - Optionally creates the client from JSON if missing

KC_URL=${KC_URL:-http://localhost:8180}
REALM=${REALM:-eflo}
ADMIN_USER=${KEYCLOAK_ADMIN_USERNAME:-${ADMIN_USER:-admin}}
ADMIN_PASS=${KEYCLOAK_ADMIN_PASSWORD:-${ADMIN_PASS:-admin}}

CLIENT_ID=eflo-web-app
CLIENT_JSON_PATH=${CLIENT_JSON_PATH:-"$(cd "$(dirname "$0")/.." && pwd)/config/client-configs/eflo-web-app.json"}

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required. Please install jq." >&2
  exit 1
fi

echo "Obtaining admin token from $KC_URL ..."
ADMIN_TOKEN=$(curl -s -X POST "$KC_URL/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=admin-cli" \
  -d "username=$ADMIN_USER" \
  -d "password=$ADMIN_PASS" \
  -d "grant_type=password" | jq -r .access_token)

if [ -z "${ADMIN_TOKEN:-}" ] || [ "$ADMIN_TOKEN" = "null" ]; then
  echo "Failed to obtain admin token. Check credentials and KC_URL." >&2
  exit 1
fi

echo "Looking up client '$CLIENT_ID' in realm '$REALM' ..."
CLIENT_LOOKUP=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" \
  "$KC_URL/admin/realms/$REALM/clients?clientId=$CLIENT_ID")
CID=$(echo "$CLIENT_LOOKUP" | jq -r '.[0].id // empty')

if [ -z "$CID" ]; then
  echo "Client not found. Creating from JSON: $CLIENT_JSON_PATH"
  curl -s -o /dev/null -w "%{http_code}" -X POST \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d @"$CLIENT_JSON_PATH" \
    "$KC_URL/admin/realms/$REALM/clients" | grep -qE '201|204' || {
      echo "Failed to create client from JSON." >&2; exit 1; }
  # Re-fetch ID
  CLIENT_LOOKUP=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" \
    "$KC_URL/admin/realms/$REALM/clients?clientId=$CLIENT_ID")
  CID=$(echo "$CLIENT_LOOKUP" | jq -r '.[0].id')
  echo "Client created with id: $CID"
else
  echo "Client exists with id: $CID"
fi

# Ensure default client scope 'roles' is assigned
echo "Ensuring default client scope 'roles' is assigned ..."
ROLES_SCOPE_ID=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" \
  "$KC_URL/admin/realms/$REALM/client-scopes?search=roles" | jq -r '.[] | select(.name=="roles") | .id' | head -n1)
if [ -z "$ROLES_SCOPE_ID" ]; then
  echo "Could not find client scope 'roles' in realm $REALM" >&2
  exit 1
fi

HAS_ROLES_SCOPE=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" \
  "$KC_URL/admin/realms/$REALM/clients/$CID/default-client-scopes" | jq -r '.[] | select(.name=="roles") | .id // empty')

if [ -z "$HAS_ROLES_SCOPE" ]; then
  echo "Adding 'roles' to default client scopes ..."
  # Add default scope to client (Keycloak uses PUT for this endpoint)
  curl -s -o /dev/null -w "%{http_code}" -X PUT \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    "$KC_URL/admin/realms/$REALM/clients/$CID/default-client-scopes/$ROLES_SCOPE_ID" | grep -qE '204|201' || {
      echo "Failed to add 'roles' default scope" >&2; exit 1; }
else
  echo "Default scope 'roles' already assigned."
fi

# Ensure protocol mapper 'roles' exists on client (realm role → claim 'roles')
echo "Ensuring protocol mapper 'roles' exists on client ..."
MAPPERS=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" \
  "$KC_URL/admin/realms/$REALM/clients/$CID/protocol-mappers/models")
HAS_ROLES_MAPPER=$(echo "$MAPPERS" | jq -r '.[] | select(.name=="roles" and .protocolMapper=="oidc-usermodel-realm-role-mapper") | .id // empty')

if [ -z "$HAS_ROLES_MAPPER" ]; then
  echo "Creating 'roles' protocol mapper on client ..."
  cat > /tmp/mapper-roles.json <<'JSON'
{
  "name": "roles",
  "protocol": "openid-connect",
  "protocolMapper": "oidc-usermodel-realm-role-mapper",
  "consentRequired": false,
  "config": {
    "userinfo.token.claim": "true",
    "id.token.claim": "true",
    "access.token.claim": "true",
    "claim.name": "roles",
    "jsonType.label": "String",
    "multivalued": "true"
  }
}
JSON
  curl -s -o /dev/null -w "%{http_code}" -X POST \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d @/tmp/mapper-roles.json \
    "$KC_URL/admin/realms/$REALM/clients/$CID/protocol-mappers/models" | grep -qE '201|204' || {
      echo "Failed to create roles mapper" >&2; exit 1; }
else
  echo "Protocol mapper 'roles' already present."
fi

echo "Done. Issue a new token and verify it includes 'roles' or 'realm_access'."

