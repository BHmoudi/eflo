#!/bin/bash
set -e

echo "=================================================="
echo "Building User Service - Local Build Method"
echo "=================================================="

# Navigate to user-service directory
cd "$(dirname "$0")"

echo ""
echo "Step 1: Clean previous builds..."
./mvnw clean

echo ""
echo "Step 2: Build application (skipping tests for faster build)..."
./mvnw package -DskipTests -B

echo ""
echo "Step 3: Verify JAR file exists..."
if [ -f target/*.jar ]; then
    echo "✅ JAR file created successfully:"
    ls -lh target/*.jar
else
    echo "❌ ERROR: JAR file not found!"
    exit 1
fi

echo ""
echo "Step 4: Build Docker image using simple Dockerfile..."
docker build -f Dockerfile.simple -t eflo/user-service:1.0.0 -t eflo/user-service:latest .

echo ""
echo "=================================================="
echo "✅ Build Complete!"
echo "=================================================="
echo ""
echo "Docker image: eflo/user-service:1.0.0"
echo ""
echo "To run:"
echo "  docker run -p 8084:8084 eflo/user-service:1.0.0"
echo ""
echo "Or with docker-compose:"
echo "  cd /Users/bacem/eflo-new-ai/files"
echo "  docker-compose up -d user-service"
echo ""
