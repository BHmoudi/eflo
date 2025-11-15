# Eflo Platform - Kubernetes Deployment Guide

This guide provides comprehensive instructions for deploying the Eflo microservices platform to Kubernetes.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Architecture Overview](#architecture-overview)
- [Quick Start](#quick-start)
- [Deployment Options](#deployment-options)
- [Configuration](#configuration)
- [Monitoring](#monitoring)
- [CI/CD Pipeline](#cicd-pipeline)
- [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Tools

- **kubectl** (v1.27+): Kubernetes command-line tool
- **Helm** (v3.13+): Kubernetes package manager
- **Docker** (v24+): For building images locally
- **Git**: For source code management

### Kubernetes Cluster

You need a Kubernetes cluster with:
- **Kubernetes version**: 1.27 or higher
- **Minimum nodes**: 3 worker nodes
- **Recommended resources per node**:
  - CPU: 4 cores
  - Memory: 16GB RAM
  - Storage: 100GB SSD

Supported Kubernetes platforms:
- Google Kubernetes Engine (GKE)
- Amazon Elastic Kubernetes Service (EKS)
- Azure Kubernetes Service (AKS)
- On-premises clusters
- Local development (Minikube, kind, k3s)

### Storage Class

Ensure your cluster has a default StorageClass configured for persistent volumes:

```bash
kubectl get storageclass
```

## Architecture Overview

### Kubernetes Resources

The platform deploys the following components:

**Infrastructure Layer:**
- 1x Eureka Server (Service Discovery)
- 1x Config Server (Centralized Configuration)
- 1x Zookeeper + 1x Kafka (Event Streaming)
- 8x PostgreSQL StatefulSets (Databases)
- 1x Keycloak (Identity & Access Management)
- 1x MinIO (Object Storage)

**Application Layer:**
- 2x Internal API Gateway (Load Balanced)
- 2x External API Gateway (Load Balanced)
- 2x Order Service (Auto-scaled)
- 2x Workflow Service (Auto-scaled)
- 2x Commission Service
- 2x Document Service
- 2x User Service
- 2x Document Generation Service

**Automation & Frontend:**
- 1x n8n (Workflow Automation)
- 2x Workflow Admin (Next.js Frontend)

### Resource Requirements

**Minimum Production Cluster:**
- Total CPU: 12 cores
- Total Memory: 32GB
- Total Storage: 200GB

**Development Cluster:**
- Total CPU: 6 cores
- Total Memory: 16GB
- Total Storage: 100GB

## Quick Start

### 1. Clone Repository

```bash
git clone https://github.com/BHmoudi/eflo.git
cd eflo
```

### 2. Configure Secrets

Create a `.env` file from the example:

```bash
cp .env.example .env
```

Edit `.env` and set all required passwords and credentials:

```env
# Database Passwords
KEYCLOAK_DB_PASSWORD=your-secure-password
ORDER_DB_PASSWORD=your-secure-password
WORKFLOW_DB_PASSWORD=your-secure-password
COMMISSION_DB_PASSWORD=your-secure-password
DOCUMENT_DB_PASSWORD=your-secure-password
USER_DB_PASSWORD=your-secure-password
N8N_DB_PASSWORD=your-secure-password
DOCGEN_DB_PASSWORD=your-secure-password

# Keycloak
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=your-admin-password

# MinIO
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=your-minio-password

# n8n
N8N_BASIC_AUTH_USER=admin
N8N_BASIC_AUTH_PASSWORD=your-n8n-password
N8N_AI_API_KEY=your-openai-key  # Optional
TWILIO_ACCOUNT_SID=your-twilio-sid  # Optional
TWILIO_AUTH_TOKEN=your-twilio-token  # Optional

# Eureka
EUREKA_USERNAME=eureka
EUREKA_PASSWORD=your-eureka-password
```

### 3. Deploy to Kubernetes

**Option A: Automated Deployment (Recommended)**

```bash
# Deploy to development environment
./scripts/deploy-kubernetes.sh --environment development --enable-monitoring

# Deploy to production environment
./scripts/deploy-kubernetes.sh --environment production --namespace eflo
```

**Option B: Manual Helm Deployment**

```bash
# Add Helm repositories
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# Create namespace
kubectl create namespace eflo

# Create secrets
kubectl create secret generic eflo-secrets \
  --from-env-file=.env \
  --namespace=eflo

# Install platform
helm install eflo-platform ./k8s/helm/eflo-platform \
  --namespace=eflo \
  --values=./k8s/helm/eflo-platform/values-production.yaml \
  --wait \
  --timeout=20m
```

**Option C: Raw Kubernetes Manifests**

```bash
# Apply manifests in order
kubectl apply -f k8s/base/namespace.yaml
kubectl apply -f k8s/base/configmap.yaml
kubectl apply -f k8s/base/secrets.yaml
kubectl apply -f k8s/base/postgres.yaml
kubectl apply -f k8s/base/kafka.yaml
kubectl apply -f k8s/base/minio.yaml
kubectl apply -f k8s/base/keycloak.yaml
kubectl apply -f k8s/base/eureka-server.yaml
kubectl apply -f k8s/base/config-server.yaml
kubectl apply -f k8s/base/api-gateways.yaml
kubectl apply -f k8s/base/order-service.yaml
kubectl apply -f k8s/base/business-services.yaml
kubectl apply -f k8s/base/n8n.yaml
kubectl apply -f k8s/base/workflow-admin.yaml
```

### 4. Verify Deployment

```bash
# Check all pods are running
kubectl get pods -n eflo

# Check services
kubectl get svc -n eflo

# View logs
kubectl logs -f deployment/order-service -n eflo
```

### 5. Access Services

**Port Forwarding (Local Access):**

```bash
# Eureka Dashboard
kubectl port-forward svc/eureka-server 8761:8761 -n eflo
# Access: http://localhost:8761

# Internal API Gateway
kubectl port-forward svc/internal-api-gateway 8080:8080 -n eflo
# Access: http://localhost:8080

# Workflow Admin
kubectl port-forward svc/workflow-admin 3001:3001 -n eflo
# Access: http://localhost:3001

# Keycloak
kubectl port-forward svc/keycloak 8180:8180 -n eflo
# Access: http://localhost:8180

# MinIO Console
kubectl port-forward svc/minio 9001:9001 -n eflo
# Access: http://localhost:9001
```

**LoadBalancer Access (Cloud Deployments):**

```bash
# Get external IPs
kubectl get svc -n eflo | grep LoadBalancer
```

## Deployment Options

### Development Environment

Optimized for local development with reduced resources:

```bash
./scripts/deploy-kubernetes.sh \
  --environment development \
  --namespace eflo-dev \
  --enable-monitoring
```

Features:
- Single replica for all services
- Reduced resource requests/limits
- Smaller persistent volumes
- Monitoring enabled by default
- No autoscaling

### Staging Environment

Pre-production testing environment:

```bash
./scripts/deploy-kubernetes.sh \
  --environment staging \
  --namespace eflo-staging
```

Features:
- 2 replicas for critical services
- Production-like resources
- Horizontal Pod Autoscaling enabled
- Full monitoring stack

### Production Environment

Optimized for high availability and performance:

```bash
./scripts/deploy-kubernetes.sh \
  --environment production \
  --namespace eflo
```

Features:
- Multi-replica deployments
- Horizontal Pod Autoscaling (2-10 replicas)
- Resource limits enforced
- Pod Disruption Budgets
- Network Policies
- Full observability stack

## Configuration

### Environment Variables

All services are configured via:
1. **ConfigMap** (`k8s/base/configmap.yaml`): Non-sensitive configuration
2. **Secrets** (`eflo-secrets`): Sensitive data (passwords, API keys)

### Helm Values

Customize deployment by editing:
- `k8s/helm/eflo-platform/values.yaml` (defaults)
- `k8s/helm/eflo-platform/values-development.yaml`
- `k8s/helm/eflo-platform/values-staging.yaml`
- `k8s/helm/eflo-platform/values-production.yaml`

Example customization:

```yaml
# Custom values-production.yaml
orderService:
  replicaCount: 5
  hpa:
    minReplicas: 3
    maxReplicas: 20
    targetCPU: 60

workflowAdmin:
  image:
    tag: v1.2.3
  service:
    type: LoadBalancer
    annotations:
      service.beta.kubernetes.io/aws-load-balancer-type: nlb
```

### Resource Management

**CPU/Memory Requests & Limits:**

```yaml
resources:
  requests:
    memory: "512Mi"  # Guaranteed resources
    cpu: "250m"
  limits:
    memory: "1Gi"    # Maximum allowed
    cpu: "1000m"
```

**Horizontal Pod Autoscaling:**

```yaml
hpa:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
  targetCPU: 70
  targetMemory: 80
```

### Persistent Storage

**Storage Classes:**

```bash
# List available storage classes
kubectl get storageclass

# Set default storage class
kubectl patch storageclass <name> -p '{"metadata": {"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'
```

**Volume Sizes (Production):**
- PostgreSQL databases: 10-50Gi each
- Kafka: 20-100Gi
- MinIO: 50-500Gi
- n8n: 10Gi

## Monitoring

### Prometheus & Grafana

Deploy monitoring stack:

```bash
# Install Prometheus Operator
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace

# Apply Eflo ServiceMonitors
kubectl apply -f k8s/monitoring/prometheus.yaml
```

### Access Monitoring

```bash
# Prometheus
kubectl port-forward svc/prometheus-kube-prometheus-prometheus 9090:9090 -n monitoring

# Grafana
kubectl port-forward svc/prometheus-grafana 3000:80 -n monitoring
# Default credentials: admin / prom-operator
```

### Key Metrics

- **Service Health**: `up{job=~".*eflo.*"}`
- **Request Rate**: `rate(http_server_requests_seconds_count[5m])`
- **Error Rate**: `rate(http_server_requests_seconds_count{status=~"5.."}[5m])`
- **Response Time (p95)**: `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))`
- **JVM Memory**: `jvm_memory_used_bytes`
- **Database Connections**: `hikaricp_connections_active`
- **Kafka Lag**: `kafka_consumer_lag`

### Alerts

Pre-configured alerts in `k8s/monitoring/prometheus.yaml`:
- Service Down
- High Memory Usage (>90%)
- High CPU Usage (>80%)
- Pod Crash Looping
- High Error Rate (>5%)
- Database Connection Pool Exhausted
- High Kafka Consumer Lag

## CI/CD Pipeline

### GitHub Actions Workflows

The platform includes automated CI/CD pipelines:

**1. CI - Java Services** (`.github/workflows/ci-java-services.yml`)
- Triggers: Push to main/develop, PRs
- Actions:
  - Detect changed services
  - Build & compile
  - Run unit tests
  - Run integration tests
  - Generate test coverage
  - Security scanning (OWASP Dependency Check)
  - Code quality analysis (SonarCloud)

**2. CI - Frontend** (`.github/workflows/ci-frontend.yml`)
- Triggers: Changes to workflow-admin
- Actions:
  - Install dependencies
  - ESLint & TypeScript checks
  - Run tests
  - Build application
  - E2E tests (Playwright)
  - Lighthouse performance audit

**3. Docker Build & Push** (`.github/workflows/docker-build-push.yml`)
- Triggers: Push to main/develop, tags
- Actions:
  - Build Docker images
  - Multi-platform builds (amd64, arm64)
  - Push to GitHub Container Registry
  - Generate SBOM (Software Bill of Materials)
  - Vulnerability scanning (Trivy)

**4. CD - Kubernetes Deploy** (`.github/workflows/cd-kubernetes-deploy.yml`)
- Triggers: Successful Docker builds, manual dispatch
- Actions:
  - Deploy to Kubernetes
  - Run smoke tests
  - Automatic rollback on failure

### Setting Up CI/CD

**1. GitHub Secrets:**

Configure the following secrets in your GitHub repository:

```
# Kubernetes
KUBECONFIG: <base64-encoded-kubeconfig>
K8S_CONTEXT: <kubernetes-context-name>

# Database Passwords
KEYCLOAK_DB_PASSWORD
ORDER_DB_PASSWORD
WORKFLOW_DB_PASSWORD
COMMISSION_DB_PASSWORD
DOCUMENT_DB_PASSWORD
USER_DB_PASSWORD
N8N_DB_PASSWORD

# Application Secrets
KEYCLOAK_ADMIN_PASSWORD
MINIO_ROOT_PASSWORD
N8N_BASIC_AUTH_PASSWORD
N8N_AI_API_KEY

# Optional: SonarCloud
SONAR_TOKEN
```

**2. Manual Deployment:**

```bash
# Trigger deployment via GitHub Actions
gh workflow run cd-kubernetes-deploy.yml \
  -f environment=production \
  -f namespace=eflo
```

### Image Versioning Strategy

- **Latest**: `ghcr.io/eflo/order-service:latest` (from main branch)
- **Develop**: `ghcr.io/eflo/order-service:develop`
- **Semantic**: `ghcr.io/eflo/order-service:v1.2.3` (from tags)
- **SHA**: `ghcr.io/eflo/order-service:main-abc123`

## Troubleshooting

### Common Issues

**1. Pods Not Starting**

```bash
# Check pod status
kubectl get pods -n eflo

# Describe pod for events
kubectl describe pod <pod-name> -n eflo

# Check logs
kubectl logs <pod-name> -n eflo --previous
```

**2. Database Connection Issues**

```bash
# Check PostgreSQL pods
kubectl get pods -l tier=database -n eflo

# Test database connection
kubectl exec -it postgres-order-0 -n eflo -- psql -U order_user -d order_db
```

**3. Service Discovery Issues**

```bash
# Check Eureka dashboard
kubectl port-forward svc/eureka-server 8761:8761 -n eflo
# Open: http://localhost:8761

# Verify service registration
kubectl logs deployment/order-service -n eflo | grep "Eureka"
```

**4. Memory Issues (OOMKilled)**

```bash
# Check pod resource usage
kubectl top pods -n eflo

# Increase memory limits
kubectl set resources deployment/order-service \
  --limits=memory=2Gi \
  -n eflo
```

**5. Persistent Volume Issues**

```bash
# Check PVCs
kubectl get pvc -n eflo

# Check PVs
kubectl get pv

# Delete and recreate PVC (data loss!)
kubectl delete pvc <pvc-name> -n eflo
```

### Debugging Commands

```bash
# Shell into a pod
kubectl exec -it deployment/order-service -n eflo -- /bin/sh

# Copy files from pod
kubectl cp eflo/<pod-name>:/app/logs/app.log ./local-app.log

# Get all resources
kubectl get all -n eflo

# Watch pod status
kubectl get pods -n eflo --watch

# Get events
kubectl get events -n eflo --sort-by='.lastTimestamp'
```

### Rolling Back

**Helm Rollback:**

```bash
# List releases
helm list -n eflo

# Show history
helm history eflo-platform -n eflo

# Rollback to previous version
helm rollback eflo-platform -n eflo

# Rollback to specific revision
helm rollback eflo-platform 3 -n eflo
```

**kubectl Rollback:**

```bash
# Check rollout history
kubectl rollout history deployment/order-service -n eflo

# Rollback to previous version
kubectl rollout undo deployment/order-service -n eflo

# Rollback to specific revision
kubectl rollout undo deployment/order-service --to-revision=2 -n eflo
```

### Performance Tuning

**1. JVM Tuning:**

Add to deployment environment variables:

```yaml
env:
  - name: JAVA_OPTS
    value: "-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

**2. Database Connection Pool:**

```yaml
env:
  - name: SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE
    value: "20"
  - name: SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE
    value: "5"
```

**3. Kafka Configuration:**

```yaml
env:
  - name: SPRING_KAFKA_CONSUMER_MAX_POLL_RECORDS
    value: "500"
  - name: SPRING_KAFKA_CONSUMER_FETCH_MIN_SIZE
    value: "1048576"
```

## Best Practices

1. **Always use resource limits** to prevent resource exhaustion
2. **Enable liveness and readiness probes** for all services
3. **Use ConfigMaps and Secrets** instead of hardcoding values
4. **Implement PodDisruptionBudgets** for critical services
5. **Enable autoscaling** for variable workloads
6. **Monitor resource usage** and adjust limits accordingly
7. **Regular backups** of databases and persistent volumes
8. **Use namespaces** to separate environments
9. **Implement network policies** for security
10. **Keep images updated** and scan for vulnerabilities

## Support

For issues and questions:
- GitHub Issues: https://github.com/BHmoudi/eflo/issues
- Documentation: See CLAUDE.md for application-specific details
- Logs: Always check pod logs first for debugging

## License

Copyright © 2024 Eflo Team
