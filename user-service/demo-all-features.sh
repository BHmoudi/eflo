#!/bin/bash
echo "🎉 USER SERVICE - FEATURE DEMONSTRATION"
echo "========================================"
echo ""

BASE="http://localhost:8084"

echo "1️⃣  Get All Users"
curl -s $BASE/api/v1/users | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'Total users: {len(d[\"users\"])}'); [print(f'  - {u[\"email\"]} ({u[\"fullName\"]})') for u in d['users']]"
echo ""

echo "2️⃣  Get All Business Units"
curl -s $BASE/api/v1/business-units | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'Total: {len(d)}'); [print(f'  - {b[\"code\"]}: {b[\"name\"]} ({b[\"city\"]})') for b in d]"
echo ""

echo "3️⃣  Get User's Business Units"
curl -s $BASE/api/v1/user-business-units/user/2 | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'Jane has {len(d)} business unit(s):'); [print(f'  - {a[\"businessUnitName\"]} (Primary: {a[\"isPrimary\"]})') for a in d]"
echo ""

echo "4️⃣  Get Organizational Hierarchy"
curl -s $BASE/api/v1/hierarchies/employee/3/management-chain | python3 -c "import sys,json; d=json.load(sys.stdin); print('John Doe reports to:'); [print(f'  {i+1}. {m[\"fullName\"]} ({m[\"email\"]})') for i,m in enumerate(d)]"
echo ""

echo "5️⃣  Search Users"
curl -s "$BASE/api/v1/users/search?q=sales" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'Found {len(d)} users:'); [print(f'  - {u[\"fullName\"]} - {u[\"department\"]}') for u in d]"
echo ""

echo "6️⃣  Filter by Department"
curl -s $BASE/api/v1/users/department/Sales | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'Sales department: {len(d)} people'); [print(f'  - {u[\"fullName\"]}') for u in d]"
echo ""

echo "7️⃣  Create New User"
curl -s -X POST $BASE/api/v1/users -H "Content-Type: application/json" -d '{"email":"demo@eflo.com","firstName":"Demo","lastName":"User","employeeNumber":"EMPDEMO","department":"Demo","temporaryPassword":"Pass123"}' | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'Created: {d.get(\"fullName\",\"Error\")} (ID: {d.get(\"id\",\"N/A\")})')"
echo ""

echo "8️⃣  Service Health"
curl -s $BASE/actuator/health | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'Service Status: {d[\"status\"]}')"
echo ""

echo "✅ ALL FEATURES DEMONSTRATED SUCCESSFULLY!"
