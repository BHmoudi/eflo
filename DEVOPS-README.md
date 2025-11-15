# Eflo Platform - DevOps Infrastructure

Complete DevOps infrastructure for the Eflo microservices platform including CI/CD pipelines, Kubernetes deployment, and monitoring.

## 📋 Table of Contents

- [Overview](#overview)
- [Quick Start](#quick-start)
- [Directory Structure](#directory-structure)
- [CI/CD Pipeline](#cicd-pipeline)
- [Kubernetes Deployment](#kubernetes-deployment)
- [Monitoring & Observability](#monitoring--observability)
- [Documentation](#documentation)

## 🎯 Overview

This repository contains a complete DevOps setup for the Eflo platform:

- **GitHub Actions** workflows for CI/CD
- **Kubernetes** manifests for cloud-native deployment
- **Helm** charts for package management
- **Prometheus & Grafana** for monitoring
- **Automated** testing, building, and deployment
- **Multi-environment** support (dev, staging, production)

### Key Features

✅ Automated CI/CD pipeline with GitHub Actions
✅ Kubernetes-native deployment with Helm
✅ Multi-platform Docker images (amd64, arm64)
✅ Horizontal pod autoscaling
✅ Prometheus monitoring with alerts
✅ Security scanning (Trivy, OWASP)
✅ Code quality analysis (SonarCloud)
✅ Automated rollback on failure
✅ Multi-environment support

## 🚀 Quick Start

### Prerequisites

Install required tools:
```bash
# Kubernetes CLI
brew install kubectl

# Helm package manager
brew install helm

# GitHub CLI (optional)
brew install gh
```

### 1. Configure Secrets

Create `.env` file:
```bash
cp .env.example .env
# Edit .env with your credentials
```

### 2. Deploy to Kubernetes

**Development environment:**
```bash
./scripts/deploy-kubernetes.sh \
  --environment development \
  --namespace eflo-dev \
  --enable-monitoring
```

**Production environment:**
```bash
./scripts/deploy-kubernetes.sh \
  --environment production \
  --namespace eflo
```

### 3. Access Services

```bash
# Eureka Dashboard
kubectl port-forward svc/eureka-server 8761:8761 -n eflo

# Workflow Admin
kubectl port-forward svc/workflow-admin 3001:3001 -n eflo

# API Gateway
kubectl port-forward svc/internal-api-gateway 8080:8080 -n eflo
```

## 📁 Directory Structure

```
eflo/
├── .github/
│   └── workflows/                 # GitHub Actions workflows
│       ├── ci-java-services.yml   # Java microservices CI
│       ├── ci-frontend.yml        # Frontend CI
│       ├── docker-build-push.yml  # Docker image builds
│       └── cd-kubernetes-deploy.yml # Kubernetes deployment
│
├── k8s/                           # Kubernetes resources
│   ├── base/                      # Base manifests
│   │   ├── namespace.yaml
│   │   ├── configmap.yaml
│   │   ├── secrets.yaml
│   │   ├── postgres.yaml
│   │   ├── kafka.yaml
│   │   ├── minio.yaml
│   │   ├── keycloak.yaml
│   │   ├── eureka-server.yaml
│   │   ├── config-server.yaml
│   │   ├── api-gateways.yaml
│   │   ├── order-service.yaml
│   │   ├── business-services.yaml
│   │   ├── n8n.yaml
│   │   ├── workflow-admin.yaml
│   │   └── kustomization.yaml
│   │
│   ├── overlays/                  # Environment-specific overlays
│   │   ├── development/
│   │   ├── staging/
│   │   └── production/
│   │
│   ├── helm/                      # Helm charts
│   │   └── eflo-platform/
│   │       ├── Chart.yaml
│   │       ├── values.yaml
│   │       └── values-*.yaml
│   │
│   └── monitoring/                # Monitoring resources
│       └── prometheus.yaml
│
├── scripts/                       # Deployment scripts
│   └── deploy-kubernetes.sh      # Kubernetes deployment script
│
└── Documentation
    ├── KUBERNETES-DEPLOYMENT.md   # Kubernetes deployment guide
    ├── CICD-PIPELINE.md          # CI/CD pipeline docs
    └── DEVOPS-README.md          # This file
```

## 🔄 CI/CD Pipeline

### Workflow Overview

```
Code Push/PR
    ↓
┌─────────────────────────┐
│  CI: Build & Test       │
│  - Java Services        │
│  - Frontend (Next.js)   │
│  - Security Scanning    │
└─────────┬───────────────┘
          ↓
┌─────────────────────────┐
│  Docker Build & Push    │
│  - Multi-platform       │
│  - SBOM Generation      │
│  - Vulnerability Scan   │
└─────────┬───────────────┘
          ↓
┌─────────────────────────┐
│  Deploy to Kubernetes   │
│  - Dev/Staging/Prod     │
│  - Smoke Tests          │
│  - Auto Rollback        │
└─────────────────────────┘
```

### GitHub Actions Workflows

#### 1. **ci-java-services.yml**
- Detects changed microservices
- Parallel builds with Maven
- Unit & integration tests
- JaCoCo coverage reports
- Security scanning (OWASP)
- SonarCloud analysis (optional)

#### 2. **ci-frontend.yml**
- Next.js application build
- ESLint & TypeScript checks
- Unit tests with Jest
- E2E tests with Playwright
- Lighthouse performance audit

#### 3. **docker-build-push.yml**
- Multi-platform builds (amd64, arm64)
- Push to GitHub Container Registry
- Generate SBOM (Software Bill of Materials)
- Trivy security scanning
- Upload SARIF to GitHub Security

#### 4. **cd-kubernetes-deploy.yml**
- Deploy to Kubernetes cluster
- Environment-specific configuration
- Health checks & smoke tests
- Automatic rollback on failure
- Deployment notifications

### Setting Up CI/CD

**1. Configure GitHub Secrets:**

```bash
# Required secrets
KUBECONFIG                  # Base64 encoded kubeconfig
KEYCLOAK_DB_PASSWORD
ORDER_DB_PASSWORD
WORKFLOW_DB_PASSWORD
COMMISSION_DB_PASSWORD
DOCUMENT_DB_PASSWORD
USER_DB_PASSWORD
N8N_DB_PASSWORD
KEYCLOAK_ADMIN_PASSWORD
MINIO_ROOT_PASSWORD
N8N_BASIC_AUTH_PASSWORD

# Optional
SONAR_TOKEN                 # For SonarCloud
N8N_AI_API_KEY             # For n8n AI features
```

**2. Trigger Workflows:**

```bash
# Via git push
git push origin main

# Manually via GitHub CLI
gh workflow run cd-kubernetes-deploy.yml \
  -f environment=production \
  -f namespace=eflo

# Via GitHub Actions UI
# Actions → Select Workflow → Run workflow
```

See [CICD-PIPELINE.md](./CICD-PIPELINE.md) for detailed documentation.

## ☸️ Kubernetes Deployment

### Architecture

**Infrastructure Services:**
- Eureka Server (Service Discovery)
- Config Server (Configuration Management)
- Kafka + Zookeeper (Event Streaming)
- PostgreSQL x8 (Databases)
- Keycloak (Identity Management)
- MinIO (Object Storage)

**Application Services:**
- Internal/External API Gateways
- Order Service
- Workflow Service
- Commission Service
- Document Service
- User Service
- Document Generation Service

**Automation & Frontend:**
- n8n (Workflow Automation)
- Workflow Admin (Next.js Dashboard)

### Deployment Methods

**Method 1: Automated Script (Recommended)**
```bash
./scripts/deploy-kubernetes.sh --environment production
```

**Method 2: Helm**
```bash
helm install eflo-platform ./k8s/helm/eflo-platform \
  --namespace eflo \
  --values ./k8s/helm/eflo-platform/values-production.yaml
```

**Method 3: kubectl + Kustomize**
```bash
kubectl apply -k k8s/base
```

**Method 4: Raw Manifests**
```bash
kubectl apply -f k8s/base/
```

### Environment Configuration

**Development:**
- Single replicas
- Reduced resources (6 CPU, 16GB RAM)
- Smaller storage (100GB total)
- Monitoring enabled
- Auto-deploy on `develop` branch

**Staging:**
- Multi-replicas
- Production-like resources
- Full monitoring stack
- Manual approval required

**Production:**
- High availability (2-10 replicas)
- Horizontal pod autoscaling
- Large storage allocation
- Multi-AZ deployment
- Strict change control

See [KUBERNETES-DEPLOYMENT.md](./KUBERNETES-DEPLOYMENT.md) for detailed guide.

## 📊 Monitoring & Observability

### Prometheus & Grafana

**Deploy monitoring stack:**
```bash
# Install Prometheus Operator
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace

# Apply Eflo ServiceMonitors
kubectl apply -f k8s/monitoring/prometheus.yaml
```

**Access dashboards:**
```bash
# Prometheus
kubectl port-forward svc/prometheus-kube-prometheus-prometheus 9090:9090 -n monitoring

# Grafana
kubectl port-forward svc/prometheus-grafana 3000:80 -n monitoring
# Default: admin / prom-operator
```

### Key Metrics

Monitor these critical metrics:

**Application Metrics:**
- Request rate: `rate(http_server_requests_seconds_count[5m])`
- Error rate: `rate(http_server_requests_seconds_count{status=~"5.."}[5m])`
- Response time (p95): `histogram_quantile(0.95, ...)`
- JVM memory usage: `jvm_memory_used_bytes`

**Infrastructure Metrics:**
- Pod CPU/Memory usage
- Database connection pools
- Kafka consumer lag
- Persistent volume usage

**Alerts Configured:**
- Service down > 5 minutes
- High memory usage > 90%
- High CPU usage > 80%
- Pod crash looping
- High error rate > 5%
- Database pool exhausted
- Kafka consumer lag > 1000

### Logging

**View logs:**
```bash
# Single service
kubectl logs -f deployment/order-service -n eflo

# All pods with label
kubectl logs -f -l app=order-service -n eflo

# Previous container (crashed)
kubectl logs deployment/order-service --previous -n eflo

# Multiple services
stern order-service -n eflo  # Requires stern
```

## 📚 Documentation

Comprehensive documentation is available:

| Document | Description |
|----------|-------------|
| [KUBERNETES-DEPLOYMENT.md](./KUBERNETES-DEPLOYMENT.md) | Complete Kubernetes deployment guide |
| [CICD-PIPELINE.md](./CICD-PIPELINE.md) | CI/CD pipeline documentation |
| [CLAUDE.md](./CLAUDE.md) | Application architecture & development guide |
| [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md) | General deployment instructions |
| [TEST-COVERAGE-IMPROVEMENTS.md](./TEST-COVERAGE-IMPROVEMENTS.md) | Testing documentation |

## 🛠️ Common Operations

### Scaling Services

```bash
# Manual scaling
kubectl scale deployment order-service --replicas=5 -n eflo

# Update HPA
kubectl autoscale deployment order-service \
  --min=2 --max=10 --cpu-percent=70 -n eflo
```

### Updating Services

```bash
# Update image version
kubectl set image deployment/order-service \
  order-service=ghcr.io/eflo/order-service:v1.2.3 \
  -n eflo

# Rollout status
kubectl rollout status deployment/order-service -n eflo

# Rollback
kubectl rollout undo deployment/order-service -n eflo
```

### Database Operations

```bash
# Connect to database
kubectl exec -it postgres-order-0 -n eflo -- \
  psql -U order_user -d order_db

# Backup database
kubectl exec postgres-order-0 -n eflo -- \
  pg_dump -U order_user order_db > backup.sql

# Restore database
kubectl exec -i postgres-order-0 -n eflo -- \
  psql -U order_user order_db < backup.sql
```

### Debugging

```bash
# Check pod status
kubectl get pods -n eflo

# Describe pod
kubectl describe pod <pod-name> -n eflo

# Shell into pod
kubectl exec -it deployment/order-service -n eflo -- /bin/sh

# Check events
kubectl get events -n eflo --sort-by='.lastTimestamp'

# Resource usage
kubectl top pods -n eflo
kubectl top nodes
```

## 🔒 Security

### Best Practices Implemented

✅ **Secrets Management**: All sensitive data in Kubernetes Secrets
✅ **RBAC**: Role-based access control
✅ **Network Policies**: Pod-to-pod communication rules
✅ **Image Scanning**: Trivy vulnerability scanning
✅ **Dependency Scanning**: OWASP Dependency Check
✅ **Pod Security Standards**: Enforced security contexts
✅ **TLS**: Encrypted communication (when configured)
✅ **Least Privilege**: Minimal container permissions

### Security Scanning

**Container Images:**
```bash
# Local scan
trivy image ghcr.io/eflo/order-service:latest

# In CI/CD
# Automatically scans all images before deployment
```

**Dependencies:**
```bash
# Java services
mvn org.owasp:dependency-check-maven:check

# Frontend
npm audit
```

## 🚨 Troubleshooting

### Common Issues

**Pods not starting:**
```bash
kubectl describe pod <pod-name> -n eflo
kubectl logs <pod-name> -n eflo
```

**Database connection issues:**
```bash
# Check database pod
kubectl get pods -l tier=database -n eflo

# Test connection
kubectl exec -it postgres-order-0 -n eflo -- pg_isready
```

**Service discovery issues:**
```bash
# Check Eureka dashboard
kubectl port-forward svc/eureka-server 8761:8761 -n eflo
# Visit: http://localhost:8761
```

**Insufficient resources:**
```bash
# Check node resources
kubectl top nodes

# Check pod resources
kubectl top pods -n eflo

# Describe node
kubectl describe node <node-name>
```

See [KUBERNETES-DEPLOYMENT.md](./KUBERNETES-DEPLOYMENT.md#troubleshooting) for more troubleshooting guides.

## 📈 Performance Tuning

### JVM Tuning

Add to deployment:
```yaml
env:
  - name: JAVA_OPTS
    value: "-Xms512m -Xmx1024m -XX:+UseG1GC"
```

### Database Connection Pool

```yaml
env:
  - name: SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE
    value: "20"
```

### Horizontal Pod Autoscaling

```yaml
hpa:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
  targetCPU: 70
  targetMemory: 80
```

## 🤝 Contributing

When contributing DevOps improvements:

1. **Test locally** with Minikube/kind
2. **Update documentation**
3. **Test in development** environment
4. **Create PR** with detailed description
5. **Request review** from DevOps team

## 📞 Support

For DevOps-related issues:

- **GitHub Issues**: https://github.com/BHmoudi/eflo/issues
- **Documentation**: Check the docs in this directory
- **Logs**: Always include pod logs when reporting issues

## 📝 License

Copyright © 2024 Eflo Team
