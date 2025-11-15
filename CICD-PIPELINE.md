# Eflo Platform - CI/CD Pipeline Documentation

Complete guide to the Continuous Integration and Continuous Deployment pipeline for the Eflo microservices platform.

## Overview

The Eflo platform uses GitHub Actions for automated CI/CD workflows that:
- Build and test all microservices
- Create and publish Docker images
- Deploy to Kubernetes environments
- Monitor code quality and security

## Workflow Architecture

```
┌─────────────────┐
│  Code Push/PR   │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────────────────┐
│              CI Workflows (Parallel)                 │
├─────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │Java Services│  │  Frontend   │  │   Security  │ │
│  │Build & Test │  │Build & Test │  │   Scanning  │ │
│  └─────────────┘  └─────────────┘  └─────────────┘ │
└────────┬────────────────┬────────────────┬──────────┘
         │                │                │
         ▼                ▼                ▼
┌─────────────────────────────────────────────────────┐
│           Docker Build & Push (on main)             │
├─────────────────────────────────────────────────────┤
│  • Multi-platform builds (amd64, arm64)             │
│  • Push to GitHub Container Registry                │
│  • Generate SBOM & Security Scan                    │
└────────┬────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────┐
│           Kubernetes Deployment (CD)                │
├─────────────────────────────────────────────────────┤
│  • Deploy to environment (dev/staging/prod)         │
│  • Run smoke tests                                  │
│  • Automatic rollback on failure                    │
└─────────────────────────────────────────────────────┘
```

## GitHub Actions Workflows

### 1. CI - Java Services

**File**: `.github/workflows/ci-java-services.yml`

**Triggers:**
- Push to `main`, `develop`, `feature/**`, `claude/**`
- Pull requests to `main`, `develop`
- Changes to any `**-service/`, `**-gateway/`, `**-server/` directories

**Jobs:**

#### detect-changes
Detects which services have changed to optimize build matrix.

```yaml
services: [eureka-server, config-server, order-service, ...]
```

#### build-and-test (Matrix Job)
Runs in parallel for each changed service:

1. **Setup**: JDK 21, Maven cache
2. **Compile**: `mvn clean compile -DskipTests`
3. **Unit Tests**: `mvn test -Dtest=!*Integration*`
4. **Integration Tests**: `mvn test -Dtest=*Integration*`
5. **Package**: `mvn package -DskipTests`
6. **Coverage**: JaCoCo report generation
7. **Artifacts**: Upload JAR and test results

#### code-quality (PR only)
- SonarCloud analysis for code quality metrics
- Runs only if `SONAR_TOKEN` secret is configured

#### security-scan
- OWASP Dependency Check for vulnerability scanning
- Generates security reports for all services

**Environment Variables:**
```yaml
JAVA_VERSION: '21'
MAVEN_OPTS: -Xmx3072m
```

**Example Run:**
```bash
# Manually trigger workflow
gh workflow run ci-java-services.yml
```

### 2. CI - Frontend

**File**: `.github/workflows/ci-frontend.yml`

**Triggers:**
- Changes to `workflow-admin/` directory
- Push to main branches and PRs

**Jobs:**

#### build-and-test
1. **Setup**: Node.js 20, pnpm 8
2. **Dependencies**: `pnpm install --frozen-lockfile`
3. **Linting**: `pnpm run lint`
4. **Type Check**: `pnpm run type-check`
5. **Tests**: `pnpm run test`
6. **Build**: `pnpm run build`
7. **Artifacts**: Upload Next.js build

#### e2e-tests
- Playwright browser tests
- Runs after successful build
- Uploads test reports

#### lighthouse-audit (PR only)
- Performance audit using Lighthouse CI
- Checks for web vitals and performance regressions

#### dependency-review (PR only)
- Reviews dependencies for security vulnerabilities
- Blocks PRs with critical vulnerabilities

**Environment Variables:**
```yaml
NODE_VERSION: '20'
PNPM_VERSION: '8'
```

### 3. Docker Build & Push

**File**: `.github/workflows/docker-build-push.yml`

**Triggers:**
- Push to `main`, `develop`
- Git tags matching `v*.*.*`
- Manual dispatch with service selection

**Jobs:**

#### setup
Determines:
- Version/tag for images
- Which services to build (from input or all)

#### build-java-services (Matrix Job)
For each service:
1. **Setup**: Docker Buildx for multi-platform builds
2. **Login**: GitHub Container Registry authentication
3. **Metadata**: Extract tags and labels
4. **Build & Push**:
   - Platforms: `linux/amd64`, `linux/arm64`
   - Cache: GitHub Actions cache
   - Tags: latest, version, SHA, branch
5. **SBOM**: Generate Software Bill of Materials
6. **Security Scan**: Trivy vulnerability scanning
7. **Upload**: SARIF results to GitHub Security

#### build-frontend
Similar process for workflow-admin with additional build args.

**Image Tags Generated:**
```
ghcr.io/eflo/order-service:latest
ghcr.io/eflo/order-service:develop
ghcr.io/eflo/order-service:v1.2.3
ghcr.io/eflo/order-service:main-abc123
```

**Manual Trigger Example:**
```bash
# Build specific services
gh workflow run docker-build-push.yml \
  -f services="order-service,workflow-service"

# Build all services
gh workflow run docker-build-push.yml \
  -f services="all"
```

### 4. CD - Kubernetes Deploy

**File**: `.github/workflows/cd-kubernetes-deploy.yml`

**Triggers:**
- Successful completion of Docker Build & Push
- Manual dispatch with environment selection

**Jobs:**

#### deploy
1. **Setup**: kubectl, Helm
2. **Configure**: Kubernetes context from secrets
3. **Namespace**: Create if not exists
4. **Secrets**:
   - Create image pull secret
   - Create application secrets from GitHub Secrets
5. **Helm Repos**: Add Bitnami and other charts
6. **Infrastructure Deploy**:
   - Deploy with Helm
   - Wait for pods ready (timeout: 10m)
7. **Application Deploy**:
   - Deploy services with Helm
   - Wait for readiness (timeout: 15m)
8. **Smoke Tests**: Basic health checks
9. **Status**: Display deployment status
10. **Rollback**: Automatic on failure

#### post-deployment
- Success notifications
- Create deployment tags for production

**Environment Inputs:**
- `environment`: development | staging | production
- `namespace`: Kubernetes namespace

**Required GitHub Secrets:**
```
KUBECONFIG                    # Base64 encoded kubeconfig
K8S_CONTEXT                   # Kubernetes context name
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
N8N_AI_API_KEY
```

**Manual Deployment:**
```bash
gh workflow run cd-kubernetes-deploy.yml \
  -f environment=production \
  -f namespace=eflo
```

## GitHub Secrets Configuration

### Repository Secrets Setup

Navigate to: **Settings → Secrets and variables → Actions → New repository secret**

#### Kubernetes Configuration

```bash
# Encode your kubeconfig
cat ~/.kube/config | base64 | pbcopy

# Add as KUBECONFIG secret
```

#### Database Passwords

Generate strong passwords:
```bash
# Generate random passwords
openssl rand -base64 32
```

Add each as a secret:
- `KEYCLOAK_DB_PASSWORD`
- `ORDER_DB_PASSWORD`
- `WORKFLOW_DB_PASSWORD`
- `COMMISSION_DB_PASSWORD`
- `DOCUMENT_DB_PASSWORD`
- `USER_DB_PASSWORD`
- `N8N_DB_PASSWORD`

#### Application Secrets

- `KEYCLOAK_ADMIN_PASSWORD`: Keycloak admin password
- `MINIO_ROOT_PASSWORD`: MinIO root password
- `N8N_BASIC_AUTH_PASSWORD`: n8n authentication
- `N8N_AI_API_KEY`: OpenAI API key (optional)
- `TWILIO_ACCOUNT_SID`: Twilio SID (optional)
- `TWILIO_AUTH_TOKEN`: Twilio token (optional)

#### Optional: Code Quality

- `SONAR_TOKEN`: SonarCloud authentication token

### Environment Secrets

For environment-specific secrets:
**Settings → Environments → New environment**

Create environments:
- `development`
- `staging`
- `production`

Add protection rules:
- Required reviewers (production)
- Wait timer (staging/production)
- Deployment branches (main only for production)

## Workflow Best Practices

### 1. Branch Strategy

```
main          ──────●──────●──────●──────●──────
                     ↑      ↑
develop       ──●────┴──●───┴──●──────
               ↑         ↑
feature/xxx   ─┴─────────┘
```

- `main`: Production-ready code, triggers production deployments
- `develop`: Integration branch, triggers development deployments
- `feature/*`: Feature branches, triggers CI only
- `claude/*`: AI-assisted development, triggers CI only

### 2. Pull Request Workflow

1. **Create Feature Branch**: `feature/add-new-api`
2. **Commit Changes**: CI runs on push
3. **Create PR**: Additional PR checks run
   - Code quality analysis
   - Security scanning
   - Dependency review
4. **Review & Approve**: Required for main/develop
5. **Merge**: Triggers Docker build and deployment

### 3. Versioning Strategy

**Semantic Versioning**: `v<major>.<minor>.<patch>`

```bash
# Create a release
git tag -a v1.2.3 -m "Release version 1.2.3"
git push origin v1.2.3

# Triggers:
# - Docker build with v1.2.3 tag
# - Production deployment (if configured)
```

### 4. Deployment Strategy

**Development:**
- Auto-deploy on push to `develop`
- Latest images
- Single replicas
- Reduced resources

**Staging:**
- Manual approval required
- Semantic versioned images
- Production-like configuration
- Full testing suite

**Production:**
- Manual approval required
- Specific version tags only
- Multi-replica, auto-scaled
- Blue-green deployment support

## Monitoring Workflows

### GitHub Actions Dashboard

View workflow runs:
```bash
# List recent workflow runs
gh run list --limit 10

# View specific run
gh run view <run-id>

# View logs
gh run view <run-id> --log

# Cancel running workflow
gh run cancel <run-id>

# Re-run failed workflow
gh run rerun <run-id>
```

### Workflow Status Badges

Add to README.md:

```markdown
![CI - Java Services](https://github.com/BHmoudi/eflo/actions/workflows/ci-java-services.yml/badge.svg)
![CI - Frontend](https://github.com/BHmoudi/eflo/actions/workflows/ci-frontend.yml/badge.svg)
![Docker Build](https://github.com/BHmoudi/eflo/actions/workflows/docker-build-push.yml/badge.svg)
![K8s Deploy](https://github.com/BHmoudi/eflo/actions/workflows/cd-kubernetes-deploy.yml/badge.svg)
```

## Troubleshooting

### Common Issues

#### 1. Docker Build Failing

**Error**: "buildx: command not found"
**Solution**: Update `docker/setup-buildx-action` to latest version

**Error**: "failed to push to registry"
**Solution**: Check GITHUB_TOKEN permissions in workflow

#### 2. Kubernetes Deployment Failing

**Error**: "context deadline exceeded"
**Solution**: Increase timeout in workflow:
```yaml
--timeout=30m  # Increase from 15m
```

**Error**: "secret not found"
**Solution**: Verify all required secrets are configured

#### 3. Test Failures

**Error**: Integration tests timing out
**Solution**: Add `continue-on-error: true` for integration tests in development

#### 4. SonarCloud Issues

**Error**: "Could not find a default branch"
**Solution**: Configure default branch in SonarCloud project settings

### Debug Mode

Enable debug logging:

**Settings → Secrets → New repository secret**
- Name: `ACTIONS_STEP_DEBUG`
- Value: `true`

View runner diagnostic logs:
- Name: `ACTIONS_RUNNER_DEBUG`
- Value: `true`

## Performance Optimization

### 1. Caching Strategy

**Maven Dependencies:**
```yaml
- uses: actions/cache@v4
  with:
    path: ~/.m2/repository
    key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
```

**Docker Layers:**
```yaml
cache-from: type=gha
cache-to: type=gha,mode=max
```

**pnpm Dependencies:**
```yaml
- uses: pnpm/action-setup@v3
  with:
    version: 8
- uses: actions/cache@v4
  with:
    path: ${{ steps.pnpm-cache.outputs.STORE_PATH }}
```

### 2. Matrix Strategy

Run services in parallel:
```yaml
strategy:
  matrix:
    service: [order-service, workflow-service, ...]
  max-parallel: 10  # Limit concurrent jobs
```

### 3. Conditional Execution

Skip unnecessary jobs:
```yaml
if: github.event_name == 'push' && github.ref == 'refs/heads/main'
```

## Security Considerations

### 1. Secret Management

- ✅ Use GitHub Secrets for sensitive data
- ✅ Rotate secrets regularly
- ✅ Use environment-specific secrets
- ❌ Never commit secrets to repository
- ❌ Never log secrets in workflow output

### 2. Dependency Scanning

Automated scans:
- **OWASP Dependency Check**: Java dependencies
- **Trivy**: Container images
- **Dependabot**: Automated dependency updates
- **GitHub Advanced Security**: Code scanning

### 3. Image Security

- Use official base images
- Multi-stage builds to reduce attack surface
- Scan images before deployment
- Sign images with cosign (optional)

### 4. Access Control

- Restrict who can approve deployments
- Use environment protection rules
- Enable required reviews for main branch
- Use branch protection rules

## Maintenance

### Regular Tasks

**Weekly:**
- Review failed workflow runs
- Check dependency updates
- Monitor deployment metrics

**Monthly:**
- Rotate secrets
- Review and update workflow configurations
- Clean up old workflow runs
- Update action versions

**Quarterly:**
- Security audit
- Review access controls
- Update documentation

### Updating Workflows

1. **Create Feature Branch**
   ```bash
   git checkout -b feature/update-ci-workflow
   ```

2. **Modify Workflow**
   ```bash
   vim .github/workflows/ci-java-services.yml
   ```

3. **Test Changes**
   - Push to feature branch
   - Monitor workflow execution
   - Verify all jobs pass

4. **Create PR**
   - Request review
   - Merge to main

## Best Practices Summary

1. ✅ **Use matrix builds** for parallel execution
2. ✅ **Cache dependencies** to speed up builds
3. ✅ **Fail fast** for quick feedback
4. ✅ **Use specific action versions** (not @main)
5. ✅ **Implement proper error handling**
6. ✅ **Add meaningful job names**
7. ✅ **Use artifacts** for build outputs
8. ✅ **Monitor workflow costs** (GitHub Actions minutes)
9. ✅ **Keep workflows DRY** (reusable workflows)
10. ✅ **Document custom workflows**

## Support

For CI/CD related issues:
- Check GitHub Actions logs first
- Review workflow configuration
- Verify secrets are configured
- Check runner availability
- Open issue with workflow run URL

## References

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Helm Documentation](https://helm.sh/docs/)
- [Docker Documentation](https://docs.docker.com/)
