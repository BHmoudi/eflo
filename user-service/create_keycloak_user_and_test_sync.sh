#!/bin/bash
set -e

KEYCLOAK="http://localhost:8180"
USER_SERVICE="http://localhost:8084"

echo "════════════════════════════════════════════════════════════════"
echo "  KEYCLOAK SYNC TEST - Create User and Verify Sync"
echo "════════════════════════════════════════════════════════════════"
echo ""

# Step 1: Get admin token
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

# Step 2: Check users before
echo "Step 2: Checking users in database BEFORE sync..."
echo "────────────────────────────────────────────────────────────────"
BEFORE_COUNT=$(docker exec postgres-user psql -U user_user -d user_db -t -c "SELECT COUNT(*) FROM users;")
echo "Users in database: $BEFORE_COUNT"
echo ""

# Step 3: Create user in Keycloak
echo "Step 3: Creating test user in Keycloak eflo realm..."
echo "────────────────────────────────────────────────────────────────"

USER_DATA='{
  "username": "sync.test",
  "email": "sync.test@eflo.com",
  "firstName": "Sync",
  "lastName": "Test",
  "enabled": true,
  "emailVerified": true,
  "credentials": [{
    "type": "password",
    "value": "SyncTest123!",
    "temporary": false
  }],
  "attributes": {
    "employeeNumber": ["EMPSYNC"],
    "department": ["Testing"]
  }
}'

CREATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST \
  "${KEYCLOAK}/admin/realms/eflo/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$USER_DATA")

HTTP_CODE=$(echo "$CREATE_RESPONSE" | tail -n1)
BODY=$(echo "$CREATE_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" == "201" ]; then
    echo "✅ User created in Keycloak successfully"
    # Extract user ID from Location header
    LOCATION=$(curl -s -i -X POST \
      "${KEYCLOAK}/admin/realms/eflo/users" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -H "Content-Type: application/json" \
      -d "$USER_DATA" 2>&1 | grep -i "location:" | cut -d'/' -f9 | tr -d '\r')
    echo "   Keycloak User ID: $LOCATION"
else
    echo "⚠️  User creation returned HTTP $HTTP_CODE"
    if [ "$HTTP_CODE" == "409" ]; then
        echo "   User already exists in Keycloak (this is OK)"
    else
        echo "   Response: $BODY"
    fi
fi

echo ""

# Step 4: Verify user in Keycloak
echo "Step 4: Verifying user exists in Keycloak..."
echo "────────────────────────────────────────────────────────────────"

KEYCLOAK_USERS=$(curl -s "${KEYCLOAK}/admin/realms/eflo/users?username=sync.test" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq 'length')

echo "Users named 'sync.test' in Keycloak eflo realm: $KEYCLOAK_USERS"

if [ "$KEYCLOAK_USERS" -gt 0 ]; then
    echo "✅ User confirmed in Keycloak"
    KEYCLOAK_USER_ID=$(curl -s "${KEYCLOAK}/admin/realms/eflo/users?username=sync.test" \
      -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[0].id')
    echo "   Keycloak UUID: $KEYCLOAK_USER_ID"
else
    echo "❌ User not found in Keycloak"
fi

echo ""

# Step 5: Trigger manual sync
echo "Step 5: Triggering manual Keycloak sync..."
echo "────────────────────────────────────────────────────────────────"

# Get user service token (need SUPER_ADMIN for sync endpoint)
SERVICE_TOKEN=$(curl -s -X POST "${KEYCLOAK}/realms/eflo/protocol/openid-connect/token" \
  -d "client_id=eflo-web-app" \
  -d "grant_type=password" \
  -d "username=admin@eflo.com" \
  -d "password=change_me_keycloak_admin_password_2025" | jq -r '.access_token')

if [ -z "$SERVICE_TOKEN" ] || [ "$SERVICE_TOKEN" == "null" ]; then
    echo "⚠️  Could not get SUPER_ADMIN token, triggering sync directly..."
    # Trigger sync by calling the service directly (works without auth since we disabled it)
    SYNC_RESULT=$(curl -s -X POST "${USER_SERVICE}/api/v1/keycloak-sync/sync-all")
    echo "$SYNC_RESULT" | jq '.' 2>/dev/null || echo "$SYNC_RESULT"
else
    echo "✅ SUPER_ADMIN token obtained"
    SYNC_RESULT=$(curl -s -X POST "${USER_SERVICE}/api/v1/keycloak-sync/sync-all" \
      -H "Authorization: Bearer $SERVICE_TOKEN")
    echo "$SYNC_RESULT" | jq '.' 2>/dev/null || echo "$SYNC_RESULT"
fi

echo ""

# Step 6: Check database after sync
echo "Step 6: Checking users in database AFTER sync..."
echo "────────────────────────────────────────────────────────────────"
sleep 3

AFTER_COUNT=$(docker exec postgres-user psql -U user_user -d user_db -t -c "SELECT COUNT(*) FROM users;")
echo "Users in database: $AFTER_COUNT"

if [ "$AFTER_COUNT" -gt "$BEFORE_COUNT" ]; then
    echo "✅ SUCCESS! User synced from Keycloak to database!"
    echo "   Before: $BEFORE_COUNT users"
    echo "   After: $AFTER_COUNT users"
    echo "   New users synced: $((AFTER_COUNT - BEFORE_COUNT))"
    echo ""

    # Show the synced user
    echo "Synced user details:"
    docker exec postgres-user psql -U user_user -d user_db -c \
      "SELECT id, email, first_name, last_name, employee_number, department FROM users WHERE email = 'sync.test@eflo.com';"
else
    echo "⚠️  No new users synced"
    echo "   This might mean:"
    echo "   - User already existed in database"
    echo "   - Sync didn't pick up the new user yet"
    echo "   - Check logs: docker logs user-service | grep sync"
fi

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "  SYNC TEST COMPLETE"
echo "════════════════════════════════════════════════════════════════"
