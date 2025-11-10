#!/bin/bash
curl -s -X POST "http://localhost:8180/realms/eflo/protocol/openid-connect/token" -H "Content-Type: application/x-www-form-urlencoded" -d "grant_type=client_credentials" -d "client_id=eflo-microservices" -d "client_secret=secret" | jq -r .access_token
