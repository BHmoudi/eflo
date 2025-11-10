#!/bin/bash
set -e

KEYCLOAK="http://localhost:8180"

echo "════════════════════════════════════════════════════════════════"
echo "  KEYCLOAK - Configure Custom Claims (user_ipn, rrf)"
echo "════════════════════════════════════════════════════════════════"
echo ""

# Get admin token
echo "Step 1: Getting Keycloak admin token..."
echo "────────────────────────────────────────────────────────────────"
ADMIN_TOKEN=$(curl -s -X POST "${KEYCLOAK}/realms/master/protocol/openid-connect/token" \
  -d "client_id=admin-cli" \
  -d "grant_type=password" \
  -d "username=admin" \
  -d "password=change_me_keycloak_admin_password_2025" | jq -r '.access_token')

if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" == "null" ]; then
    echo "❌ Failed to get admin token"
    exit 1
fi
echo "✅ Admin token obtained"
echo ""

# Get client ID for eflo-web-app
echo "Step 2: Getting eflo-web-app client ID..."
echo "────────────────────────────────────────────────────────────────"
CLIENT_ID=$(curl -s "${KEYCLOAK}/admin/realms/eflo/clients" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | \
  jq -r '.[] | select(.clientId=="eflo-web-app") | .id')

if [ -z "$CLIENT_ID" ] || [ "$CLIENT_ID" == "null" ]; then
    echo "❌ Could not find eflo-web-app client"
    exit 1
fi
echo "✅ Client ID: $CLIENT_ID"
echo ""

# Create user_ipn mapper
echo "Step 3: Creating user_ipn token mapper..."
echo "────────────────────────────────────────────────────────────────"
USER_IPN_MAPPER='{
  "name": "user_ipn",
  "protocol": "openid-connect",
  "protocolMapper": "oidc-usermodel-attribute-mapper",
  "consentRequired": false,
  "config": {
    "user.attribute": "user_ipn",
    "claim.name": "user_ipn",
    "jsonType.label": "String",
    "id.token.claim": "true",
    "access.token.claim": "true",
    "userinfo.token.claim": "true"
  }
}'

curl -s -X POST "${KEYCLOAK}/admin/realms/eflo/clients/${CLIENT_ID}/protocol-mappers/models" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$USER_IPN_MAPPER"

echo "✅ user_ipn mapper created"
echo ""

# Create rrf mapper
echo "Step 4: Creating rrf token mapper..."
echo "────────────────────────────────────────────────────────────────"
RRF_MAPPER='{
  "name": "rrf",
  "protocol": "openid-connect",
  "protocolMapper": "oidc-usermodel-attribute-mapper",
  "consentRequired": false,
  "config": {
    "user.attribute": "rrf",
    "claim.name": "rrf",
    "jsonType.label": "String",
    "id.token.claim": "true",
    "access.token.claim": "true",
    "userinfo.token.claim": "true"
  }
}'

curl -s -X POST "${KEYCLOAK}/admin/realms/eflo/clients/${CLIENT_ID}/protocol-mappers/models" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$RRF_MAPPER"

echo "✅ rrf mapper created"
echo ""

# Update john.doe user with attributes
echo "Step 5: Updating john.doe user attributes..."
echo "────────────────────────────────────────────────────────────────"

# Get john.doe user ID
JOHN_USER_ID=$(curl -s "${KEYCLOAK}/admin/realms/eflo/users?username=john.doe" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[0].id')

if [ -z "$JOHN_USER_ID" ] || [ "$JOHN_USER_ID" == "null" ]; then
    echo "⚠️  john.doe user not found in Keycloak"
else
    echo "✅ Found john.doe user: $JOHN_USER_ID"

    # Get current user data
    JOHN_DATA=$(curl -s "${KEYCLOAK}/admin/realms/eflo/users/${JOHN_USER_ID}" \
      -H "Authorization: Bearer $ADMIN_TOKEN")

    # Update with custom attributes
    UPDATED_JOHN=$(echo $JOHN_DATA | jq '.attributes += {"user_ipn": ["d179003"], "rrf": ["RRF75"]}')

    curl -s -X PUT "${KEYCLOAK}/admin/realms/eflo/users/${JOHN_USER_ID}" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -H "Content-Type: application/json" \
      -d "$UPDATED_JOHN"

    echo "✅ john.doe updated with user_ipn=d179003 and rrf=RRF75"
fi

echo ""

# Update jane.smith user
echo "Step 6: Updating jane.smith user attributes..."
echo "────────────────────────────────────────────────────────────────"

JANE_USER_ID=$(curl -s "${KEYCLOAK}/admin/realms/eflo/users?username=jane.smith" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[0].id')

if [ -z "$JANE_USER_ID" ] || [ "$JANE_USER_ID" == "null" ]; then
    echo "⚠️  jane.smith user not found in Keycloak"
else
    echo "✅ Found jane.smith user: $JANE_USER_ID"

    JANE_DATA=$(curl -s "${KEYCLOAK}/admin/realms/eflo/users/${JANE_USER_ID}" \
      -H "Authorization: Bearer $ADMIN_TOKEN")

    UPDATED_JANE=$(echo $JANE_DATA | jq '.attributes += {"user_ipn": ["d179002"], "rrf": ["RRF75"]}')

    curl -s -X PUT "${KEYCLOAK}/admin/realms/eflo/users/${JANE_USER_ID}" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -H "Content-Type: application/json" \
      -d "$UPDATED_JANE"

    echo "✅ jane.smith updated with user_ipn=d179002 and rrf=RRF75"
fi

echo ""

# Verification
echo "Step 7: Verification - Get new token and decode..."
echo "────────────────────────────────────────────────────────────────"

# Get fresh token for john.doe
JOHN_TOKEN=$(curl -s -X POST "${KEYCLOAK}/realms/eflo/protocol/openid-connect/token" \
  --data-urlencode "client_id=eflo-web-app" \
  --data-urlencode "username=john.doe@eflo.com" \
  --data-urlencode "password=JohnDoe123!" \
  --data-urlencode "grant_type=password" | jq -r '.access_token')

if [ -n "$JOHN_TOKEN" ] && [ "$JOHN_TOKEN" != "null" ]; then
    echo "✅ New token obtained"
    echo ""
    echo "Decoding token payload..."
    PAYLOAD=$(echo $JOHN_TOKEN | cut -d'.' -f2)

    # Add padding if needed
    case $((${#PAYLOAD} % 4)) in
      2) PAYLOAD="${PAYLOAD}==" ;;
      3) PAYLOAD="${PAYLOAD}=" ;;
    esac

    echo ""
    echo "Custom claims in token:"
    echo "$PAYLOAD" | base64 -d 2>/dev/null | jq '{user_ipn, rrf, roles: .realm_access.roles, email, preferred_username}' || echo "Could not decode"
fi

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "  CONFIGURATION COMPLETE"
echo "════════════════════════════════════════════════════════════════"
echo ""
echo "✅ Token mappers created for user_ipn and rrf"
echo "✅ User attributes updated in Keycloak"
echo "✅ New tokens will contain custom claims"
echo ""
echo "Test with:"
echo "  1. Get new token for john.doe@eflo.com"
echo "  2. Decode token to see user_ipn and rrf claims"
echo "  3. Use token with user-service API"
echo ""
