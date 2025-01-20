#!/bin/bash
if [[ $# -ne 2 ]]; then
  echo "Usage: $0 springbit-domain auth-server-domain"
  echo "Eg: $0 https://springbit.org  springbit.org:9443"
  exit 1
fi

kubectl create namespace springbit

kubectl apply -f /hostdata/k8s -R
sleep 2

# Set the environment values required for public access
kubectl -n springbit set env deployment/config-service   VAULT_USER_TOKEN=$(sudo grep vault.token /hostdata/secrets.prop | cut -d'=' -f 2-)
kubectl -n springbit set env deployment/auth-server      SPRINGBIT_DOMAIN=$1
kubectl -n springbit set env deployment/auth-server      SPRINGBIT_AUTH_DOMAIN=$2
kubectl -n springbit set env deployment/gateway-service  SPRINGBIT_DOMAIN=$1
kubectl -n springbit set env deployment/gateway-service  SPRINGBIT_AUTH_DOMAIN=$2

# Restart the services to load the above envs
kubectl -n springbit rollout restart deployment config-service
kubectl -n springbit rollout restart deployment auth-server
kubectl -n springbit rollout restart deployment gateway-service


kubectl get pods -A -o wide
echo ""
kubectl get nodes -o wide
echo ""
kubectl -n springbit get pv
echo ""
kubectl -n springbit get pvc
