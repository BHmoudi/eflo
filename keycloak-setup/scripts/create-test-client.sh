#!/bin/bash

# Get admin token
ADMIN_TOKEN=$(curl -s -X POST "http://localhost:8180/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=admin-cli" \
  -d "username=admin" \
  -d "password=QDOwpDuOgsbfEsgw3FhDrLhyX" \
  -d "grant_type=password" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

if [ -z "$ADMIN_TOKEN" ]; then
  echo "Failed to get admin token"
  exit 1
fi

echo "Admin token obtained"

# Create test client
curl -v -X POST "http://localhost:8180/admin/realms/eflo/clients" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "clientId": "workflow-test-client",
    "enabled": true,
    "publicClient": false,
    "directAccessGrantsEnabled": true,
    "serviceAccountsEnabled": true,
    "secret": "test-secret-12345",
    "standardFlowEnabled": false,
    "fullScopeAllowed": true
  }'

echo ""
echo "Test client created"

# Now try to get a token with this client
echo "Getting token with test client..."
curl -s -X POST "http://localhost:8180/realms/eflo/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=workflow-test-client" \
  -d "client_secret=test-secret-12345" \
  -d "username=admin@eflo.com" \
  -d "password=QDOwpDuOgsbfEsgw3FhDrLhyX" \
  -d "grant_type=password"
