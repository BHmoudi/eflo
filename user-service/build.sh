#!/bin/bash

# Build script for User Service
# This script builds the User Service Docker image

set -e

echo "========================================"
echo "Building User Service"
echo "========================================"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Get the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo -e "${YELLOW}Step 1: Cleaning previous builds...${NC}"
cd "$SCRIPT_DIR"
./mvnw clean

echo -e "${YELLOW}Step 2: Running tests...${NC}"
./mvnw test

echo -e "${YELLOW}Step 3: Building application...${NC}"
./mvnw package -DskipTests

echo -e "${YELLOW}Step 4: Building Docker image...${NC}"
docker build -t eflo/user-service:1.0.0 .
docker tag eflo/user-service:1.0.0 eflo/user-service:latest

echo -e "${GREEN}========================================"
echo "User Service build completed successfully!"
echo "========================================${NC}"
echo ""
echo "Available images:"
docker images | grep eflo/user-service
echo ""
echo "To run the service:"
echo "  docker-compose up user-service"
echo ""
echo "To run with all dependencies:"
echo "  docker-compose up -d"
