#!/bin/bash

##############################################################################
# Keycloak User Creation Script
#
# This script creates additional users in the Eflo realm
#
# Usage: ./create-users.sh
##############################################################################

set -e

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
DOCKER_DIR="$PROJECT_ROOT/docker"

# Load environment variables
if [ -f "$DOCKER_DIR/.env" ]; then
    export $(cat "$DOCKER_DIR/.env" | grep -v '^#' | xargs)
fi

# Keycloak configuration
KEYCLOAK_URL="http://localhost:8180"
REALM_NAME="eflo"
ADMIN_USERNAME=${KEYCLOAK_ADMIN_USERNAME:-admin}
ADMIN_PASSWORD=${KEYCLOAK_ADMIN_PASSWORD:-admin}

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Get admin access token
get_admin_token() {
    log_info "Getting admin access token..."

    TOKEN_RESPONSE=$(curl -s -X POST "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "username=${ADMIN_USERNAME}" \
        -d "password=${ADMIN_PASSWORD}" \
        -d "grant_type=password" \
        -d "client_id=admin-cli")

    ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.access_token')

    if [ "$ACCESS_TOKEN" == "null" ] || [ -z "$ACCESS_TOKEN" ]; then
        log_error "Failed to get access token"
        exit 1
    fi

    log_success "Access token obtained"
}

# Get role ID
get_role_id() {
    local role_name=$1

    ROLE_INFO=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/roles/${role_name}" \
        -H "Authorization: Bearer ${ACCESS_TOKEN}")

    ROLE_ID=$(echo "$ROLE_INFO" | jq -r '.id')
    echo "$ROLE_ID"
}

# Create user
create_user() {
    local username=$1
    local email=$2
    local first_name=$3
    local last_name=$4
    local role=$5
    local employee_number=$6
    local business_units=$7
    local region=$8
    local department=$9

    log_info "Creating user: $email"

    # Check if user exists
    EXISTING_USER=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/users?username=${username}" \
        -H "Authorization: Bearer ${ACCESS_TOKEN}")

    USER_COUNT=$(echo "$EXISTING_USER" | jq '. | length')

    if [ "$USER_COUNT" -gt 0 ]; then
        log_warning "User $username already exists, skipping..."
        return
    fi

    # Create user JSON
    USER_JSON=$(cat <<EOF
{
  "username": "$username",
  "email": "$email",
  "firstName": "$first_name",
  "lastName": "$last_name",
  "enabled": true,
  "emailVerified": true,
  "credentials": [{
    "type": "password",
    "value": "TempPassword123!",
    "temporary": true
  }],
  "attributes": {
    "employeeNumber": ["$employee_number"],
    "businessUnitIds": ["$business_units"],
    "region": ["$region"],
    "department": ["$department"]
  }
}
EOF
)

    # Create user
    CREATE_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
        "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/users" \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -H "Content-Type: application/json" \
        -d "$USER_JSON")

    if [ "$CREATE_RESPONSE" == "201" ]; then
        log_success "User created: $email"

        # Get user ID
        USER_INFO=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/users?username=${username}" \
            -H "Authorization: Bearer ${ACCESS_TOKEN}")

        USER_ID=$(echo "$USER_INFO" | jq -r '.[0].id')

        # Assign role
        ROLE_ID=$(get_role_id "$role")

        if [ "$ROLE_ID" != "null" ] && [ -n "$ROLE_ID" ]; then
            ROLE_ASSIGNMENT=$(cat <<EOF
[{
  "id": "$ROLE_ID",
  "name": "$role"
}]
EOF
)

            curl -s -X POST "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/users/${USER_ID}/role-mappings/realm" \
                -H "Authorization: Bearer ${ACCESS_TOKEN}" \
                -H "Content-Type: application/json" \
                -d "$ROLE_ASSIGNMENT"

            log_success "Role assigned: $role"
        fi
    else
        log_error "Failed to create user: $email (HTTP $CREATE_RESPONSE)"
    fi
}

# Main execution
main() {
    echo ""
    echo -e "${BLUE}╔═══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║         KEYCLOAK USER CREATION UTILITY                   ║${NC}"
    echo -e "${BLUE}╚═══════════════════════════════════════════════════════════╝${NC}"
    echo ""

    get_admin_token

    # Create additional users
    log_info "Creating additional users..."

    # Local Admin
    create_user \
        "alice.admin@eflo.com" \
        "alice.admin@eflo.com" \
        "Alice" \
        "Admin" \
        "ADMIN_LOCAL" \
        "EMP20001" \
        "[1, 2]" \
        "EU-WEST" \
        "Administration"

    # Document Manager
    create_user \
        "david.docs@eflo.com" \
        "david.docs@eflo.com" \
        "David" \
        "Documents" \
        "DOCUMENT_MANAGER" \
        "EMP20002" \
        "[1]" \
        "EU-WEST" \
        "Administration"

    # Secretary
    create_user \
        "susan.secretary@eflo.com" \
        "susan.secretary@eflo.com" \
        "Susan" \
        "Secretary" \
        "SECRETARY" \
        "EMP20003" \
        "[1, 2]" \
        "EU-WEST" \
        "Administration"

    # Viewer
    create_user \
        "viewer.readonly@eflo.com" \
        "viewer.readonly@eflo.com" \
        "Read" \
        "Only" \
        "VIEWER" \
        "EMP20004" \
        "[1]" \
        "GLOBAL" \
        "General"

    # Additional Salesperson
    create_user \
        "mike.sales@eflo.com" \
        "mike.sales@eflo.com" \
        "Mike" \
        "Sales" \
        "SALESPERSON" \
        "EMP20005" \
        "[2, 3, 4]" \
        "US-EAST" \
        "Sales"

    echo ""
    log_success "User creation completed!"
    echo ""
    echo -e "${YELLOW}Created Users (Default password: TempPassword123!):${NC}"
    echo -e "  - alice.admin@eflo.com      (ADMIN_LOCAL)"
    echo -e "  - david.docs@eflo.com       (DOCUMENT_MANAGER)"
    echo -e "  - susan.secretary@eflo.com  (SECRETARY)"
    echo -e "  - viewer.readonly@eflo.com  (VIEWER)"
    echo -e "  - mike.sales@eflo.com       (SALESPERSON)"
    echo ""
    echo -e "${BLUE}Note: Users must change their password on first login${NC}"
    echo ""
}

main
