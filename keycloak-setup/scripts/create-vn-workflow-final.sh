#!/bin/bash

##############################################################################
# Create Complete VN Workflow via workflow-service API
#
# Prerequisites:
# 1. Keycloak must be running on http://localhost:8180
# 2. Workflow service must be running on http://localhost:8091
# 3. You must have a Keycloak client with password grant enabled
#
# Setup Instructions:
# To create a test client in Keycloak Admin Console:
# 1. Go to http://localhost:8180/admin
# 2. Login as admin
# 3. Select 'eflo' realm
# 4. Go to Clients -> Create Client
# 5. Client ID: workflow-test-client
# 6. Client authentication: ON
# 7. Direct access grants: ENABLED
# 8. Save
# 9. Go to Credentials tab and set Secret: test-secret-12345
# 10. Go to Service account roles tab and assign appropriate roles
##############################################################################

set -e

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
WORKFLOW_API="http://localhost:8091/api/v1"
KEYCLOAK_URL="http://localhost:8180/realms/eflo/protocol/openid-connect/token"
CLIENT_ID="${CLIENT_ID:-workflow-test-client}"
CLIENT_SECRET="${CLIENT_SECRET:-test-secret-12345}"
USERNAME="${USERNAME:-admin@eflo.com}"
PASSWORD="${PASSWORD:-QDOwpDuOgsbfEsgw3FhDrLhyX}"

# Logging functions
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

# Get authentication token
get_token() {
    log_info "Obtaining authentication token..."

    TOKEN_RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "client_id=$CLIENT_ID" \
        -d "client_secret=$CLIENT_SECRET" \
        -d "username=$USERNAME" \
        -d "password=$PASSWORD" \
        -d "grant_type=password" 2>&1)

    ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

    if [ -z "$ACCESS_TOKEN" ]; then
        log_error "Failed to obtain access token"
        log_error "Response: $TOKEN_RESPONSE"
        log_warning ""
        log_warning "Please ensure:"
        log_warning "1. Keycloak client '$CLIENT_ID' exists with password grant enabled"
        log_warning "2. Client secret is set to '$CLIENT_SECRET'"
        log_warning "3. User credentials are correct"
        log_warning ""
        log_warning "To create the client, see instructions at the top of this script"
        exit 1
    fi

    log_success "Token obtained successfully"
}

# Create workflow process
create_process() {
    log_info "Creating VN_COMPLETE workflow process..."

    PROCESS_RESPONSE=$(curl -s -X POST "$WORKFLOW_API/workflow/processes" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $ACCESS_TOKEN" \
        -d '{
            "processCode": "VN_COMPLETE",
            "processName": "Workflow Complet VN",
            "orderType": "VN",
            "maxDurationDays": 90
        }')

    PROCESS_ID=$(echo "$PROCESS_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

    if [ -z "$PROCESS_ID" ]; then
        log_error "Failed to create workflow process"
        echo "Response: $PROCESS_RESPONSE"
        exit 1
    fi

    log_success "Process created with ID: $PROCESS_ID"
    echo "$PROCESS_ID"
}

# Create state
create_state() {
    local PROCESS_ID=$1
    local STATE_CODE=$2
    local STATE_NAME=$3
    local STATE_ORDER=$4
    local IS_FINAL=$5

    log_info "Creating state: $STATE_CODE ($STATE_NAME)..."

    STATE_RESPONSE=$(curl -s -X POST "$WORKFLOW_API/workflow/states" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $ACCESS_TOKEN" \
        -d "{
            \"processId\": $PROCESS_ID,
            \"stateCode\": \"$STATE_CODE\",
            \"stateName\": \"$STATE_NAME\",
            \"stateOrder\": $STATE_ORDER,
            \"isFinal\": $IS_FINAL
        }")

    STATE_ID=$(echo "$STATE_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

    if [ -z "$STATE_ID" ]; then
        log_error "Failed to create state $STATE_CODE"
        echo "Response: $STATE_RESPONSE"
        return 1
    fi

    log_success "State $STATE_CODE created with ID: $STATE_ID"
    echo "$STATE_ID"
}

# Create task
create_task() {
    local STATE_ID=$1
    local TASK_CODE=$2
    local TASK_NAME=$3
    local TASK_TYPE=$4
    local TASK_ORDER=$5
    local IS_MANDATORY=$6
    local REQUIRED_DOCS=$7

    log_info "  Creating task: $TASK_CODE..."

    TASK_JSON="{
        \"stateId\": $STATE_ID,
        \"taskCode\": \"$TASK_CODE\",
        \"taskName\": \"$TASK_NAME\",
        \"taskType\": \"$TASK_TYPE\",
        \"taskOrder\": $TASK_ORDER,
        \"isMandatory\": $IS_MANDATORY"

    if [ -n "$REQUIRED_DOCS" ]; then
        TASK_JSON="$TASK_JSON,
        \"requiredDocuments\": $REQUIRED_DOCS"
    fi

    TASK_JSON="$TASK_JSON
    }"

    TASK_RESPONSE=$(curl -s -X POST "$WORKFLOW_API/workflow/tasks" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $ACCESS_TOKEN" \
        -d "$TASK_JSON")

    TASK_ID=$(echo "$TASK_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

    if [ -z "$TASK_ID" ]; then
        log_error "  Failed to create task $TASK_CODE"
        echo "Response: $TASK_RESPONSE"
        return 1
    fi

    log_success "  Task $TASK_CODE created with ID: $TASK_ID"
}

# Create transition
create_transition() {
    local PROCESS_ID=$1
    local FROM_STATE_ID=$2
    local TO_STATE_ID=$3
    local TRANSITION_NAME=$4

    log_info "Creating transition: $TRANSITION_NAME..."

    TRANSITION_RESPONSE=$(curl -s -X POST "$WORKFLOW_API/workflow/transitions" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $ACCESS_TOKEN" \
        -d "{
            \"processId\": $PROCESS_ID,
            \"fromStateId\": $FROM_STATE_ID,
            \"toStateId\": $TO_STATE_ID,
            \"transitionName\": \"$TRANSITION_NAME\"
        }")

    TRANSITION_ID=$(echo "$TRANSITION_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

    if [ -z "$TRANSITION_ID" ]; then
        log_error "Failed to create transition"
        echo "Response: $TRANSITION_RESPONSE"
        return 1
    fi

    log_success "Transition created with ID: $TRANSITION_ID"
}

# Print banner
print_banner() {
    echo -e "${BLUE}"
    echo "╔═══════════════════════════════════════════════════════════╗"
    echo "║                                                           ║"
    echo "║        VN WORKFLOW CREATION - WORKFLOW SERVICE           ║"
    echo "║                                                           ║"
    echo "╚═══════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

# Display summary
display_summary() {
    local PROCESS_ID=$1

    echo ""
    echo -e "${GREEN}╔═══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║                                                           ║${NC}"
    echo -e "${GREEN}║        VN WORKFLOW CREATED SUCCESSFULLY!                 ║${NC}"
    echo -e "${GREEN}║                                                           ║${NC}"
    echo -e "${GREEN}╚═══════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${BLUE}WORKFLOW SUMMARY:${NC}"
    echo -e "  ${YELLOW}Process ID:${NC} $PROCESS_ID"
    echo -e "  ${YELLOW}Process Code:${NC} VN_COMPLETE"
    echo -e "  ${YELLOW}Process Name:${NC} Workflow Complet VN"
    echo -e "  ${YELLOW}Order Type:${NC} VN"
    echo -e "  ${YELLOW}Max Duration:${NC} 90 days"
    echo ""
    echo -e "${BLUE}STATES CREATED (7):${NC}"
    echo -e "  1. COMMANDE (ID: $STATE_1) - 4 tasks"
    echo -e "  2. VALIDATION_CDV (ID: $STATE_2) - 4 tasks"
    echo -e "  3. PREPARATION_FINANCEMENT (ID: $STATE_3) - 4 tasks"
    echo -e "  4. VALIDATION_FG (ID: $STATE_4) - 3 tasks"
    echo -e "  5. PREPARATION_LIVRAISON (ID: $STATE_5) - 3 tasks"
    echo -e "  6. LIVRAISON (ID: $STATE_6) - 2 tasks"
    echo -e "  7. ARCHIVEE (ID: $STATE_7) - 0 tasks (final)"
    echo ""
    echo -e "${BLUE}WORKFLOW DETAILS:${NC}"
    echo ""
    echo -e "${YELLOW}State 1: COMMANDE${NC}"
    echo -e "  - COMMANDE_CLIENT (ACTION): Enregistrement commande client"
    echo -e "  - DOC_PIECE_IDENTITE (DOCUMENT): Pièce d'identité client"
    echo -e "  - DOC_JUSTIF_DOMICILE (DOCUMENT): Justificatif de domicile"
    echo -e "  - VERIFICATION_STOCK (CONTROL): Vérification disponibilité stock"
    echo ""
    echo -e "${YELLOW}State 2: VALIDATION_CDV${NC}"
    echo -e "  - CONTROLE_DOSSIER (CONTROL): Contrôle complétude dossier"
    echo -e "  - VALIDATION_PRIX (ACTION): Validation des prix et remises"
    echo -e "  - DOC_BON_COMMANDE (DOCUMENT): Bon de commande signé"
    echo -e "  - APPROBATION_CDV (ACTION): Approbation Chef des Ventes"
    echo ""
    echo -e "${YELLOW}State 3: PREPARATION_FINANCEMENT${NC}"
    echo -e "  - DOC_BULLETINS_SALAIRE (DOCUMENT): Bulletins de salaire (3 derniers mois)"
    echo -e "  - DOC_RELEVES_BANCAIRES (DOCUMENT): Relevés bancaires (3 derniers mois)"
    echo -e "  - MONTAGE_DOSSIER_CREDIT (ACTION): Montage dossier de crédit"
    echo -e "  - SIMULATION_FINANCEMENT (CONTROL): Simulation et validation financière"
    echo ""
    echo -e "${YELLOW}State 4: VALIDATION_FG${NC}"
    echo -e "  - ANALYSE_RISQUE (CONTROL): Analyse risque crédit"
    echo -e "  - DOC_CONTRAT_FINANCEMENT (DOCUMENT): Contrat de financement signé"
    echo -e "  - APPROBATION_FG (ACTION): Approbation Financière Groupe"
    echo ""
    echo -e "${YELLOW}State 5: PREPARATION_LIVRAISON${NC}"
    echo -e "  - COMMANDE_VEHICULE (ACTION): Commande véhicule au constructeur"
    echo -e "  - PREPARATION_VEHICULE (ACTION): Préparation et contrôle technique véhicule"
    echo -e "  - DOC_CARTE_GRISE (DOCUMENT): Demande de carte grise"
    echo ""
    echo -e "${YELLOW}State 6: LIVRAISON${NC}"
    echo -e "  - DOC_PROCES_VERBAL (DOCUMENT): Procès-verbal de livraison signé"
    echo -e "  - LIVRAISON_CLIENT (ACTION): Livraison effective au client"
    echo ""
    echo -e "${YELLOW}State 7: ARCHIVEE${NC}"
    echo -e "  Final state - no tasks"
    echo ""
    echo -e "${BLUE}TRANSITIONS CREATED (8):${NC}"
    echo -e "  - COMMANDE → VALIDATION_CDV"
    echo -e "  - VALIDATION_CDV → PREPARATION_FINANCEMENT"
    echo -e "  - PREPARATION_FINANCEMENT → VALIDATION_FG"
    echo -e "  - VALIDATION_FG → PREPARATION_LIVRAISON"
    echo -e "  - PREPARATION_LIVRAISON → LIVRAISON"
    echo -e "  - LIVRAISON → ARCHIVEE"
    echo -e "  - VALIDATION_CDV → COMMANDE (rejection)"
    echo -e "  - VALIDATION_FG → PREPARATION_FINANCEMENT (rejection)"
    echo ""
    echo -e "${BLUE}TOTAL:${NC}"
    echo -e "  ${YELLOW}Tasks:${NC} 20"
    echo -e "  ${YELLOW}Transitions:${NC} 8"
    echo ""
    echo -e "${YELLOW}API Endpoint:${NC} $WORKFLOW_API"
    echo -e "${YELLOW}View Process:${NC} GET $WORKFLOW_API/workflow/processes/$PROCESS_ID"
    echo ""
}

# Main execution
main() {
    print_banner

    # Get authentication token
    get_token

    # Create workflow process
    PROCESS_ID=$(create_process)

    echo ""
    log_info "Creating workflow states..."

    # Create states
    STATE_1=$(create_state "$PROCESS_ID" "COMMANDE" "Commande" 1 "false")
    STATE_2=$(create_state "$PROCESS_ID" "VALIDATION_CDV" "Validation CDV" 2 "false")
    STATE_3=$(create_state "$PROCESS_ID" "PREPARATION_FINANCEMENT" "Préparation Financement" 3 "false")
    STATE_4=$(create_state "$PROCESS_ID" "VALIDATION_FG" "Validation FG" 4 "false")
    STATE_5=$(create_state "$PROCESS_ID" "PREPARATION_LIVRAISON" "Préparation Livraison" 5 "false")
    STATE_6=$(create_state "$PROCESS_ID" "LIVRAISON" "Livraison" 6 "false")
    STATE_7=$(create_state "$PROCESS_ID" "ARCHIVEE" "Archivée" 7 "true")

    echo ""
    log_info "Creating tasks for each state..."

    # COMMANDE tasks (4 tasks)
    log_info "State 1: COMMANDE (4 tasks)"
    create_task "$STATE_1" "COMMANDE_CLIENT" "Enregistrement commande client" "ACTION" 1 "true" ""
    create_task "$STATE_1" "DOC_PIECE_IDENTITE" "Pièce d'identité client" "DOCUMENT" 2 "true" '["PIECE_IDENTITE"]'
    create_task "$STATE_1" "DOC_JUSTIF_DOMICILE" "Justificatif de domicile" "DOCUMENT" 3 "true" '["JUSTIFICATIF_DOMICILE"]'
    create_task "$STATE_1" "VERIFICATION_STOCK" "Vérification disponibilité stock" "CONTROL" 4 "true" ""

    # VALIDATION_CDV tasks (4 tasks)
    log_info "State 2: VALIDATION_CDV (4 tasks)"
    create_task "$STATE_2" "CONTROLE_DOSSIER" "Contrôle complétude dossier" "CONTROL" 1 "true" ""
    create_task "$STATE_2" "VALIDATION_PRIX" "Validation des prix et remises" "ACTION" 2 "true" ""
    create_task "$STATE_2" "DOC_BON_COMMANDE" "Bon de commande signé" "DOCUMENT" 3 "true" '["BON_COMMANDE_SIGNE"]'
    create_task "$STATE_2" "APPROBATION_CDV" "Approbation Chef des Ventes" "ACTION" 4 "true" ""

    # PREPARATION_FINANCEMENT tasks (4 tasks)
    log_info "State 3: PREPARATION_FINANCEMENT (4 tasks)"
    create_task "$STATE_3" "DOC_BULLETINS_SALAIRE" "Bulletins de salaire (3 derniers mois)" "DOCUMENT" 1 "true" '["BULLETIN_SALAIRE_1", "BULLETIN_SALAIRE_2", "BULLETIN_SALAIRE_3"]'
    create_task "$STATE_3" "DOC_RELEVES_BANCAIRES" "Relevés bancaires (3 derniers mois)" "DOCUMENT" 2 "true" '["RELEVE_BANCAIRE_1", "RELEVE_BANCAIRE_2", "RELEVE_BANCAIRE_3"]'
    create_task "$STATE_3" "MONTAGE_DOSSIER_CREDIT" "Montage dossier de crédit" "ACTION" 3 "true" ""
    create_task "$STATE_3" "SIMULATION_FINANCEMENT" "Simulation et validation financière" "CONTROL" 4 "true" ""

    # VALIDATION_FG tasks (3 tasks)
    log_info "State 4: VALIDATION_FG (3 tasks)"
    create_task "$STATE_4" "ANALYSE_RISQUE" "Analyse risque crédit" "CONTROL" 1 "true" ""
    create_task "$STATE_4" "DOC_CONTRAT_FINANCEMENT" "Contrat de financement signé" "DOCUMENT" 2 "true" '["CONTRAT_FINANCEMENT"]'
    create_task "$STATE_4" "APPROBATION_FG" "Approbation Financière Groupe" "ACTION" 3 "true" ""

    # PREPARATION_LIVRAISON tasks (3 tasks)
    log_info "State 5: PREPARATION_LIVRAISON (3 tasks)"
    create_task "$STATE_5" "COMMANDE_VEHICULE" "Commande véhicule au constructeur" "ACTION" 1 "true" ""
    create_task "$STATE_5" "PREPARATION_VEHICULE" "Préparation et contrôle technique véhicule" "ACTION" 2 "true" ""
    create_task "$STATE_5" "DOC_CARTE_GRISE" "Demande de carte grise" "DOCUMENT" 3 "true" '["DEMANDE_CARTE_GRISE"]'

    # LIVRAISON tasks (2 tasks)
    log_info "State 6: LIVRAISON (2 tasks)"
    create_task "$STATE_6" "DOC_PROCES_VERBAL" "Procès-verbal de livraison signé" "DOCUMENT" 1 "true" '["PV_LIVRAISON"]'
    create_task "$STATE_6" "LIVRAISON_CLIENT" "Livraison effective au client" "ACTION" 2 "true" ""

    # ARCHIVEE has no tasks
    log_info "State 7: ARCHIVEE (0 tasks - final state)"

    echo ""
    log_info "Creating transitions between states..."

    # Create transitions
    create_transition "$PROCESS_ID" "$STATE_1" "$STATE_2" "Commande vers Validation CDV"
    create_transition "$PROCESS_ID" "$STATE_2" "$STATE_3" "Validation CDV vers Préparation Financement"
    create_transition "$PROCESS_ID" "$STATE_3" "$STATE_4" "Préparation Financement vers Validation FG"
    create_transition "$PROCESS_ID" "$STATE_4" "$STATE_5" "Validation FG vers Préparation Livraison"
    create_transition "$PROCESS_ID" "$STATE_5" "$STATE_6" "Préparation Livraison vers Livraison"
    create_transition "$PROCESS_ID" "$STATE_6" "$STATE_7" "Livraison vers Archivée"

    # Add some backward transitions for rejections
    create_transition "$PROCESS_ID" "$STATE_2" "$STATE_1" "Retour Validation CDV vers Commande"
    create_transition "$PROCESS_ID" "$STATE_4" "$STATE_3" "Retour Validation FG vers Préparation Financement"

    # Display summary
    display_summary "$PROCESS_ID"
}

# Run main function
main
