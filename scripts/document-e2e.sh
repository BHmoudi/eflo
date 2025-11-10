#!/usr/bin/env bash
set -euo pipefail

# Document Service E2E smoke: exercises all public endpoints with a valid JWT

HOST_DOC=${HOST_DOC:-http://localhost:8083}
HOST_KEY=${HOST_KEY:-http://localhost:8180}

REALM=${REALM:-eflo}
CLIENT_ID=${CLIENT_ID:-eflo-web-app}
USERNAME=${USERNAME:-john.doe@eflo.com}
PASSWORD=${PASSWORD:-test}

PASS=()
FAIL=()

check() {
  local name="$1" code="$2"
  if [[ "$code" =~ ^2[0-9][0-9]$ ]]; then
    PASS+=("$name:$code")
  else
    FAIL+=("$name:$code")
  fi
}

wait_ready() {
  echo "Waiting for services to be ready..."
  for i in {1..60}; do
    local c=$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/actuator/health" || true)
    [[ "$c" == "200" ]] && break || sleep 1
    [[ $i -eq 60 ]] && { echo "document-service not ready"; exit 1; }
  done
  for i in {1..60}; do
    local c=$(curl -s -o /dev/null -w "%{http_code}" "$HOST_KEY/realms/$REALM" || true)
    [[ "$c" == "200" ]] && break || sleep 1
    [[ $i -eq 60 ]] && { echo "keycloak not ready"; exit 1; }
  done
}

token() {
  curl -s -X POST "$HOST_KEY/realms/$REALM/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "grant_type=password" \
    -d "client_id=$CLIENT_ID" \
    -d "username=$USERNAME" \
    -d "password=$PASSWORD" | python3 -c 'import sys,json;print(json.load(sys.stdin).get("access_token",""))'
}

main() {
  wait_ready
  TOKEN=$(token)
  [[ -z "$TOKEN" ]] && { echo "Failed to acquire token"; exit 1; }

  # 1) Document Types
  C=$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/document-types" -H "Authorization: Bearer $TOKEN"); check "TYPES_LIST" "$C"
  TYPES=$(curl -s "$HOST_DOC/api/v1/document-types" -H "Authorization: Bearer $TOKEN")
  TYPE_ID=$(python3 - <<'PY'
import sys,json
try:
  d=json.load(sys.stdin)
  print(d[0]['id'] if isinstance(d,list) and d else '')
except Exception:
  print('')
PY
<<<"$TYPES")
  if [[ -z "$TYPE_ID" ]]; then echo "No TYPE_ID found"; exit 1; fi

  # Create + Update + Get + Duplicate + Mandatory + Category + Templates
  NEW_CODE="AUTO_TYPE_$(date +%s)"
  C=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$HOST_DOC/api/v1/document-types" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
    -d '{"typeCode":"'"$NEW_CODE"'","typeName":"Auto Type","description":"Auto gen","category":"IDENTITY","isMandatory":false,"minDocuments":0,"maxDocuments":1,"allowedFormats":["PDF"],"maxFileSizeMb":5.0,"requiresValidation":false,"validatorRoles":[],"hasExpiration":false,"isActive":true,"displayOrder":999}'); check "TYPE_CREATE" "$C"
  NEW_ID=$(curl -s "$HOST_DOC/api/v1/document-types" -H "Authorization: Bearer $TOKEN" | python3 - <<'PY'
import sys,json
d=json.load(sys.stdin)
print(next((x['id'] for x in d if x.get('typeCode','').startswith('AUTO_TYPE_')),''))
PY
)
  C=$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/document-types/$NEW_ID" -H "Authorization: Bearer $TOKEN"); check "TYPE_GET" "$C"
  C=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$HOST_DOC/api/v1/document-types/$NEW_ID" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"typeName":"Auto Type Updated"}'); check "TYPE_UPDATE" "$C"
  C=$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/document-types/mandatory" -H "Authorization: Bearer $TOKEN"); check "TYPE_MANDATORY" "$C"
  C=$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/document-types/category/IDENTITY" -H "Authorization: Bearer $TOKEN"); check "TYPE_BY_CATEGORY" "$C"
  C=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$HOST_DOC/api/v1/document-types/$NEW_ID/duplicate?newTypeCode=${NEW_CODE}_COPY&newTypeName=AutoCopy" -H "Authorization: Bearer $TOKEN"); check "TYPE_DUP" "$C"
  C=$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/document-types/templates" -H "Authorization: Bearer $TOKEN"); check "TYPE_TEMPLATES" "$C"

  # 2) Upload flows
  B='iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGMAAQAABQABDQottAAAAABJRU5ErkJggg=='
  printf "%s" "$B" | base64 -d > one.png 2>/dev/null || printf "%s" "$B" | base64 -D > one.png 2>/dev/null || true
  printf "%s" "$B" | base64 -d > two.png 2>/dev/null || printf "%s" "$B" | base64 -D > two.png 2>/dev/null || true

  UP=$(curl -s -w "\nHTTP:%{http_code}\n" -X POST "$HOST_DOC/api/v1/documents/upload" -H "Authorization: Bearer $TOKEN" -F "file=@one.png" -F "documentTypeId=${TYPE_ID}" -F "orderId=2001" -F "orderNumber=ORD-2001" -F "description=E2E upload")
  check "DOC_UPLOAD" "$(echo "$UP" | tail -n1 | sed 's/HTTP://')"
  DOC_ID=$(echo "$UP" | sed '$d' | python3 - <<'PY'
import sys,json
print(json.load(sys.stdin).get('documentId',''))
PY
)
  [[ -z "$DOC_ID" ]] && { echo "Upload failed, no DOC_ID"; exit 1; }

  # Multiple
  C=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$HOST_DOC/api/v1/documents/upload-multiple" -H "Authorization: Bearer $TOKEN" -F "files=@one.png" -F "files=@two.png" -F "documentTypeId=${TYPE_ID}" -F "orderId=2001" -F "orderNumber=ORD-2001"); check "DOC_UPLOAD_MULTI" "$C"

  # 3) Document operations
  check "DOC_GET" "$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/documents/$DOC_ID" -H "Authorization: Bearer $TOKEN")"
  check "DOC_DOWNLOAD" "$(curl -s -o /dev/null -w "%{http_code}" -OJ "$HOST_DOC/api/v1/documents/$DOC_ID/download" -H "Authorization: Bearer $TOKEN")"
  check "DOC_PREVIEW" "$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/documents/$DOC_ID/preview" -H "Authorization: Bearer $TOKEN")"
  check "DOC_VALIDATE" "$(curl -s -o /dev/null -w "%{http_code}" -X POST "$HOST_DOC/api/v1/documents/$DOC_ID/validate" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"isApproved":true,"comments":"OK","validatedBy":"john.doe@eflo.com"}')"
  check "DOC_REPLACE" "$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$HOST_DOC/api/v1/documents/$DOC_ID/replace" -H "Authorization: Bearer $TOKEN" -F "file=@two.png" -F "reason=update")"
  check "DOC_ARCHIVE" "$(curl -s -o /dev/null -w "%{http_code}" -X POST "$HOST_DOC/api/v1/documents/$DOC_ID/archive" -H "Authorization: Bearer $TOKEN")"
  check "DOC_VERSIONS" "$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/documents/$DOC_ID/versions" -H "Authorization: Bearer $TOKEN")"

  # Order docs and filters
  check "DOC_ORDER_DOCS" "$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/documents/order/2001" -H "Authorization: Bearer $TOKEN")"
  TYPE_CODE=$(curl -s "$HOST_DOC/api/v1/document-types/$TYPE_ID" -H "Authorization: Bearer $TOKEN" | python3 - <<'PY'
import sys,json
try:
  d=json.load(sys.stdin)
  print(d.get('typeCode',''))
except: print('')
PY
)
  check "DOC_BY_TYPE" "$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/documents/order/2001/type/$TYPE_CODE" -H "Authorization: Bearer $TOKEN")"
  check "DOC_PENDING" "$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/documents/pending-validation" -H "Authorization: Bearer $TOKEN")"
  check "DOC_EXPIRING" "$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/documents/expiring?days=30" -H "Authorization: Bearer $TOKEN")"
  check "DOC_STATS_BASIC" "$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/documents/statistics" -H "Authorization: Bearer $TOKEN")"

  # Validation controller batch, rescan, status
  check "VAL_BATCH" "$(curl -s -o /dev/null -w "%{http_code}" -X POST "$HOST_DOC/api/v1/documents/validate-batch?documentIds=$DOC_ID" -H "Authorization: Bearer $TOKEN")"
  check "VAL_ORDER_STATUS" "$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/documents/validation-status/2001" -H "Authorization: Bearer $TOKEN")"
  check "VAL_RESCAN" "$(curl -s -o /dev/null -w "%{http_code}" -X POST "$HOST_DOC/api/v1/documents/$DOC_ID/rescan" -H "Authorization: Bearer $TOKEN")"
  check "VAL_SCAN_STATUS" "$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/documents/scan-status?documentIds=$DOC_ID" -H "Authorization: Bearer $TOKEN")"

  # Search controller
  check "SEARCH_ADV" "$(curl -s -o /dev/null -w "%{http_code}" -X POST "$HOST_DOC/api/v1/documents/search" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"searchTerm":"ORD-2001","latestVersionOnly":true,"page":0,"size":10}')"
  check "SEARCH_RECENT" "$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/documents/search/recent?days=7" -H "Authorization: Bearer $TOKEN")"
  check "SEARCH_MY" "$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/documents/search/my-uploads" -H "Authorization: Bearer $TOKEN")"
  check "SEARCH_META" "$(curl -s -o /dev/null -w "%{http_code}" -X POST "$HOST_DOC/api/v1/documents/search/metadata" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"orderNumber":"ORD-2001"}')"

  # Reports
  check "R_STATS" "$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/documents/stats" -H "Authorization: Bearer $TOKEN")"
  check "R_COMP" "$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/documents/report/compliance?orderId=2001" -H "Authorization: Bearer $TOKEN")"
  check "R_VREP" "$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/documents/report/validation" -H "Authorization: Bearer $TOKEN")"
  check "R_ACCS" "$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/documents/report/access" -H "Authorization: Bearer $TOKEN")"
  check "R_DASH" "$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/documents/dashboard" -H "Authorization: Bearer $TOKEN")"
  FROM=$(date -u +%Y-%m-%dT00:00:00)
  TO=$(date -u +%Y-%m-%dT23:59:59)
  check "R_AUDIT" "$(curl -s -o /dev/null -w "%{http_code}" "$HOST_DOC/api/v1/documents/export/audit?fromDate=$FROM&toDate=$TO&format=csv" -H "Authorization: Bearer $TOKEN")"

  echo
  echo "==== E2E RESULTS ===="
  for p in "${PASS[@]}"; do echo "PASS $p"; done
  for f in "${FAIL[@]}"; do echo "FAIL $f"; done
  [[ ${#FAIL[@]} -gt 0 ]] && exit 2 || exit 0
}

main "$@"

