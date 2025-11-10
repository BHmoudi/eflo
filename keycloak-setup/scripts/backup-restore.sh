#!/bin/bash

##############################################################################
# Keycloak Backup and Restore Script
#
# This script provides backup and restore functionality for Keycloak
#
# Usage:
#   ./backup-restore.sh backup
#   ./backup-restore.sh restore <backup-file>
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
BACKUP_DIR="$PROJECT_ROOT/backups"
DOCKER_DIR="$PROJECT_ROOT/docker"

# Timestamp
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

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

# Create backup directory
create_backup_dir() {
    if [ ! -d "$BACKUP_DIR" ]; then
        mkdir -p "$BACKUP_DIR"
        log_info "Created backup directory: $BACKUP_DIR"
    fi
}

# Backup database
backup_database() {
    log_info "Starting database backup..."

    create_backup_dir

    BACKUP_FILE="$BACKUP_DIR/keycloak_db_${TIMESTAMP}.sql"

    cd "$DOCKER_DIR"

    # Create PostgreSQL dump
    docker-compose exec -T postgres-keycloak pg_dump -U keycloak_user keycloak_db > "$BACKUP_FILE"

    if [ $? -eq 0 ]; then
        # Compress backup
        gzip "$BACKUP_FILE"
        BACKUP_FILE="${BACKUP_FILE}.gz"

        BACKUP_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
        log_success "Database backup created: $BACKUP_FILE ($BACKUP_SIZE)"
    else
        log_error "Database backup failed"
        exit 1
    fi
}

# Backup realm configuration
backup_realm() {
    log_info "Starting realm configuration backup..."

    create_backup_dir

    # Load environment variables
    if [ -f "$DOCKER_DIR/.env" ]; then
        export $(cat "$DOCKER_DIR/.env" | grep -v '^#' | xargs)
    fi

    KEYCLOAK_URL="http://localhost:8180"
    REALM_NAME="eflo"
    ADMIN_USERNAME=${KEYCLOAK_ADMIN_USERNAME:-admin}
    ADMIN_PASSWORD=${KEYCLOAK_ADMIN_PASSWORD:-admin}

    # Get admin token
    TOKEN_RESPONSE=$(curl -s -X POST "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "username=${ADMIN_USERNAME}" \
        -d "password=${ADMIN_PASSWORD}" \
        -d "grant_type=password" \
        -d "client_id=admin-cli")

    ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.access_token')

    if [ "$ACCESS_TOKEN" == "null" ] || [ -z "$ACCESS_TOKEN" ]; then
        log_error "Failed to get access token for realm backup"
        return 1
    fi

    # Export realm
    REALM_BACKUP_FILE="$BACKUP_DIR/eflo_realm_${TIMESTAMP}.json"

    curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}" \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" | jq '.' > "$REALM_BACKUP_FILE"

    if [ $? -eq 0 ]; then
        log_success "Realm configuration backup created: $REALM_BACKUP_FILE"
    else
        log_error "Realm backup failed"
        return 1
    fi
}

# Full backup
full_backup() {
    echo ""
    echo -e "${BLUE}╔═══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║         KEYCLOAK BACKUP UTILITY                          ║${NC}"
    echo -e "${BLUE}╚═══════════════════════════════════════════════════════════╝${NC}"
    echo ""

    log_info "Starting full Keycloak backup..."

    backup_database
    backup_realm

    # Backup environment file
    if [ -f "$DOCKER_DIR/.env" ]; then
        cp "$DOCKER_DIR/.env" "$BACKUP_DIR/.env_${TIMESTAMP}"
        log_success "Environment file backed up"
    fi

    # Create backup manifest
    MANIFEST_FILE="$BACKUP_DIR/backup_manifest_${TIMESTAMP}.txt"
    cat > "$MANIFEST_FILE" << EOF
Keycloak Backup Manifest
========================
Backup Date: $(date)
Backup ID: ${TIMESTAMP}

Files:
- Database: keycloak_db_${TIMESTAMP}.sql.gz
- Realm Config: eflo_realm_${TIMESTAMP}.json
- Environment: .env_${TIMESTAMP}

Restore Command:
./backup-restore.sh restore ${TIMESTAMP}
EOF

    log_success "Backup manifest created: $MANIFEST_FILE"

    echo ""
    log_success "Full backup completed successfully!"
    echo ""
    echo -e "${YELLOW}Backup Location:${NC} $BACKUP_DIR"
    echo -e "${YELLOW}Backup ID:${NC} ${TIMESTAMP}"
    echo ""
}

# Restore database
restore_database() {
    local backup_id=$1

    log_info "Restoring database from backup: $backup_id"

    BACKUP_FILE="$BACKUP_DIR/keycloak_db_${backup_id}.sql.gz"

    if [ ! -f "$BACKUP_FILE" ]; then
        log_error "Backup file not found: $BACKUP_FILE"
        exit 1
    fi

    # Stop Keycloak
    log_info "Stopping Keycloak..."
    cd "$DOCKER_DIR"
    docker-compose stop keycloak

    # Restore database
    log_info "Restoring database..."
    gunzip -c "$BACKUP_FILE" | docker-compose exec -T postgres-keycloak psql -U keycloak_user -d keycloak_db

    if [ $? -eq 0 ]; then
        log_success "Database restored successfully"
    else
        log_error "Database restore failed"
        exit 1
    fi

    # Start Keycloak
    log_info "Starting Keycloak..."
    docker-compose start keycloak

    log_success "Keycloak restarted"
}

# List backups
list_backups() {
    echo ""
    echo -e "${BLUE}╔═══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║         AVAILABLE BACKUPS                                ║${NC}"
    echo -e "${BLUE}╚═══════════════════════════════════════════════════════════╝${NC}"
    echo ""

    if [ ! -d "$BACKUP_DIR" ] || [ -z "$(ls -A $BACKUP_DIR)" ]; then
        log_warning "No backups found"
        exit 0
    fi

    log_info "Available backups:"
    echo ""

    for manifest in "$BACKUP_DIR"/backup_manifest_*.txt; do
        if [ -f "$manifest" ]; then
            echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
            cat "$manifest"
            echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
            echo ""
        fi
    done
}

# Show usage
show_usage() {
    echo ""
    echo -e "${BLUE}Usage:${NC}"
    echo -e "  $0 backup          - Create a full backup"
    echo -e "  $0 restore <id>    - Restore from backup ID"
    echo -e "  $0 list            - List available backups"
    echo ""
}

# Main execution
main() {
    case "$1" in
        backup)
            full_backup
            ;;
        restore)
            if [ -z "$2" ]; then
                log_error "Backup ID is required"
                show_usage
                exit 1
            fi
            restore_database "$2"
            ;;
        list)
            list_backups
            ;;
        *)
            show_usage
            exit 1
            ;;
    esac
}

main "$@"
