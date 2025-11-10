#!/bin/bash
# Get token and decode it to see roles

TOKEN=$(curl -s -X POST "http://localhost:8180/realms/eflo/protocol/openid-connect/token" \
  -d "client_id=eflo-microservices" \
  -d "client_secret=9rZBFV8e4RJ2nLdok5AcBauRrAkma21c" \
  -d "grant_type=client_credentials" \
  | jq -r '.access_token')

echo "Token obtained"
echo ""

# Decode JWT payload (base64 decode the middle part)
PAYLOAD=$(echo $TOKEN | cut -d'.' -f2)

# Add padding if needed
case $((${#PAYLOAD} % 4)) in
  2) PAYLOAD="${PAYLOAD}==" ;;
  3) PAYLOAD="${PAYLOAD}=" ;;
esac

echo "Decoded JWT payload:"
echo "$PAYLOAD" | base64 -d 2>/dev/null | jq '.' || echo "Failed to decode"

echo ""
echo "Roles in token:"
echo "$PAYLOAD" | base64 -d 2>/dev/null | jq '.realm_access.roles' || echo "No roles found"

echo ""
echo "Resource access:"
echo "$PAYLOAD" | base64 -d 2>/dev/null | jq '.resource_access' || echo "No resource access found"
