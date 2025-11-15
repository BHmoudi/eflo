#!/bin/bash

# Eflo Platform Kubernetes Deployment Script
# This script deploys the entire Eflo platform to Kubernetes

set -e

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
NAMESPACE="${NAMESPACE:-eflo}"
ENVIRONMENT="${ENVIRONMENT:-development}"
HELM_RELEASE="${HELM_RELEASE:-eflo-platform}"
KUBECTL_CONTEXT="${KUBECTL_CONTEXT:-}"

# Functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    log_info "Checking prerequisites..."

    # Check kubectl
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed"
        exit 1
    fi

    # Check helm
    if ! command -v helm &> /dev/null; then
        log_error "helm is not installed"
        exit 1
    fi

    # Check if .env file exists
    if [ ! -f .env ]; then
        log_error ".env file not found. Please create it from .env.example"
        exit 1
    fi

    log_info "Prerequisites check passed"
}

setup_kubernetes_context() {
    if [ -n "$KUBECTL_CONTEXT" ]; then
        log_info "Setting Kubernetes context to: $KUBECTL_CONTEXT"
        kubectl config use-context "$KUBECTL_CONTEXT"
    fi

    log_info "Current Kubernetes context:"
    kubectl config current-context
}

create_namespace() {
    log_info "Creating namespace: $NAMESPACE"
    kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -
}

create_secrets() {
    log_info "Creating secrets from .env file..."

    # Source .env file
    set -a
    source .env
    set +a

    # Create secret
    kubectl create secret generic eflo-secrets \
        --from-literal=KEYCLOAK_DB_PASSWORD="$KEYCLOAK_DB_PASSWORD" \
        --from-literal=ORDER_DB_PASSWORD="$ORDER_DB_PASSWORD" \
        --from-literal=WORKFLOW_DB_PASSWORD="$WORKFLOW_DB_PASSWORD" \
        --from-literal=COMMISSION_DB_PASSWORD="$COMMISSION_DB_PASSWORD" \
        --from-literal=DOCUMENT_DB_PASSWORD="$DOCUMENT_DB_PASSWORD" \
        --from-literal=USER_DB_PASSWORD="$USER_DB_PASSWORD" \
        --from-literal=N8N_DB_PASSWORD="$N8N_DB_PASSWORD" \
        --from-literal=DOCGEN_DB_PASSWORD="${DOCGEN_DB_PASSWORD:-docgenpass}" \
        --from-literal=KEYCLOAK_ADMIN_USERNAME="$KEYCLOAK_ADMIN_USERNAME" \
        --from-literal=KEYCLOAK_ADMIN_PASSWORD="$KEYCLOAK_ADMIN_PASSWORD" \
        --from-literal=MINIO_ROOT_USER="$MINIO_ROOT_USER" \
        --from-literal=MINIO_ROOT_PASSWORD="$MINIO_ROOT_PASSWORD" \
        --from-literal=N8N_BASIC_AUTH_USER="${N8N_BASIC_AUTH_USER:-admin}" \
        --from-literal=N8N_BASIC_AUTH_PASSWORD="$N8N_BASIC_AUTH_PASSWORD" \
        --from-literal=N8N_AI_API_KEY="${N8N_AI_API_KEY:-}" \
        --from-literal=TWILIO_ACCOUNT_SID="${TWILIO_ACCOUNT_SID:-}" \
        --from-literal=TWILIO_AUTH_TOKEN="${TWILIO_AUTH_TOKEN:-}" \
        --from-literal=TWILIO_WHATSAPP_NUMBER="${TWILIO_WHATSAPP_NUMBER:-}" \
        --from-literal=EUREKA_USERNAME="${EUREKA_USERNAME:-eureka}" \
        --from-literal=EUREKA_PASSWORD="${EUREKA_PASSWORD:-eurekapass}" \
        --namespace="$NAMESPACE" \
        --dry-run=client -o yaml | kubectl apply -f -

    log_info "Secrets created successfully"
}

deploy_base_manifests() {
    log_info "Deploying base Kubernetes manifests..."

    kubectl apply -f k8s/base/namespace.yaml
    kubectl apply -f k8s/base/configmap.yaml -n "$NAMESPACE"
    kubectl apply -f k8s/base/postgres.yaml -n "$NAMESPACE"
    kubectl apply -f k8s/base/kafka.yaml -n "$NAMESPACE"
    kubectl apply -f k8s/base/minio.yaml -n "$NAMESPACE"
    kubectl apply -f k8s/base/keycloak.yaml -n "$NAMESPACE"
    kubectl apply -f k8s/base/eureka-server.yaml -n "$NAMESPACE"
    kubectl apply -f k8s/base/config-server.yaml -n "$NAMESPACE"

    log_info "Waiting for infrastructure services to be ready..."
    kubectl wait --for=condition=ready pod -l app=eureka-server -n "$NAMESPACE" --timeout=300s || log_warn "Eureka timeout"
    kubectl wait --for=condition=ready pod -l app=kafka -n "$NAMESPACE" --timeout=300s || log_warn "Kafka timeout"

    kubectl apply -f k8s/base/api-gateways.yaml -n "$NAMESPACE"
    kubectl apply -f k8s/base/order-service.yaml -n "$NAMESPACE"
    kubectl apply -f k8s/base/business-services.yaml -n "$NAMESPACE"
    kubectl apply -f k8s/base/n8n.yaml -n "$NAMESPACE"
    kubectl apply -f k8s/base/workflow-admin.yaml -n "$NAMESPACE"

    log_info "Base manifests deployed successfully"
}

deploy_with_helm() {
    log_info "Deploying with Helm..."

    # Add required Helm repositories
    helm repo add bitnami https://charts.bitnami.com/bitnami
    helm repo update

    # Deploy using Helm
    helm upgrade --install "$HELM_RELEASE" ./k8s/helm/eflo-platform \
        --namespace="$NAMESPACE" \
        --values="./k8s/helm/eflo-platform/values.yaml" \
        --values="./k8s/helm/eflo-platform/values-${ENVIRONMENT}.yaml" \
        --create-namespace \
        --wait \
        --timeout=20m

    log_info "Helm deployment completed"
}

deploy_monitoring() {
    log_info "Deploying monitoring stack..."

    # Check if monitoring is enabled
    if [ "$ENABLE_MONITORING" = "true" ]; then
        kubectl apply -f k8s/monitoring/prometheus.yaml -n "$NAMESPACE"
        log_info "Monitoring deployed"
    else
        log_warn "Monitoring is disabled. Set ENABLE_MONITORING=true to enable"
    fi
}

verify_deployment() {
    log_info "Verifying deployment..."

    echo ""
    log_info "=== Deployment Status ==="
    kubectl get deployments -n "$NAMESPACE"

    echo ""
    log_info "=== StatefulSets ==="
    kubectl get statefulsets -n "$NAMESPACE"

    echo ""
    log_info "=== Services ==="
    kubectl get svc -n "$NAMESPACE"

    echo ""
    log_info "=== Pods ==="
    kubectl get pods -n "$NAMESPACE"

    # Check for failed pods
    FAILED_PODS=$(kubectl get pods -n "$NAMESPACE" --field-selector=status.phase!=Running,status.phase!=Succeeded -o name)
    if [ -n "$FAILED_PODS" ]; then
        log_warn "Some pods are not running:"
        echo "$FAILED_PODS"
    else
        log_info "All pods are running successfully!"
    fi
}

port_forward_services() {
    log_info "Starting port forwarding for local access..."

    log_info "To access services locally, run:"
    echo "  kubectl port-forward svc/eureka-server 8761:8761 -n $NAMESPACE"
    echo "  kubectl port-forward svc/internal-api-gateway 8080:8080 -n $NAMESPACE"
    echo "  kubectl port-forward svc/keycloak 8180:8180 -n $NAMESPACE"
    echo "  kubectl port-forward svc/workflow-admin 3001:3001 -n $NAMESPACE"
    echo "  kubectl port-forward svc/minio 9000:9000 9001:9001 -n $NAMESPACE"
}

print_access_info() {
    log_info "=== Eflo Platform Access Information ==="

    echo ""
    echo "External API Gateway:"
    kubectl get svc external-api-gateway -n "$NAMESPACE" -o jsonpath='{.status.loadBalancer.ingress[0].ip}' || echo "Pending..."

    echo ""
    echo "Workflow Admin:"
    kubectl get svc workflow-admin -n "$NAMESPACE" -o jsonpath='{.status.loadBalancer.ingress[0].ip}' || echo "Pending..."

    echo ""
    echo "Keycloak:"
    kubectl get svc keycloak-lb -n "$NAMESPACE" -o jsonpath='{.status.loadBalancer.ingress[0].ip}' || echo "Pending..."

    echo ""
    echo "MinIO Console:"
    kubectl get svc minio-lb -n "$NAMESPACE" -o jsonpath='{.status.loadBalancer.ingress[0].ip}' || echo "Pending..."
}

# Main deployment flow
main() {
    log_info "Starting Eflo Platform deployment to Kubernetes"
    log_info "Environment: $ENVIRONMENT"
    log_info "Namespace: $NAMESPACE"

    check_prerequisites
    setup_kubernetes_context
    create_namespace
    create_secrets

    # Choose deployment method
    if [ "${USE_HELM:-true}" = "true" ]; then
        deploy_with_helm
    else
        deploy_base_manifests
    fi

    deploy_monitoring
    verify_deployment
    port_forward_services
    print_access_info

    log_info "Deployment completed successfully!"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        --no-helm)
            USE_HELM=false
            shift
            ;;
        --enable-monitoring)
            ENABLE_MONITORING=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  -e, --environment ENV    Environment (development|staging|production)"
            echo "  -n, --namespace NS       Kubernetes namespace"
            echo "  --no-helm                Deploy using plain manifests instead of Helm"
            echo "  --enable-monitoring      Enable monitoring stack"
            echo "  -h, --help               Show this help message"
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Run main function
main
