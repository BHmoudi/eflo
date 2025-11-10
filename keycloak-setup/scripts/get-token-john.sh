#!/bin/bash

# Try different clients to get a token for john.doe
echo "Trying eflo-admin-ui..."
TOKEN=$(curl -s -X POST "http://localhost:8180/realms/eflo/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=eflo-admin-ui" \
  -d "username=john.doe@eflo.com" \
  -d "password=test" \
  -d "grant_type=password" 2>&1)

echo "Response: $TOKEN"
echo ""

# Try extracting token
ACCESS_TOKEN=$(echo "$TOKEN" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)
if [ -n "$ACCESS_TOKEN" ]; then
  echo "✓ Token obtained successfully!"
  echo "Token: ${ACCESS_TOKEN:0:50}..."
else
  echo "✗ Failed to get token"
fi
