#!/bin/bash
if [[ $# -ne 2 ]]; then
  echo "Usage: $0 springbit-domain auth-server-domain"
  echo "Eg: $0 https://springbit.org  springbit.org:9443"
  exit 1
fi

kubectl create namespace springbit

kubectl create secret docker-registry docker-secret \
  --docker-server=docker.io \
  --docker-username=$(grep docker.username /hostdata/secrets.prop | cut -d'=' -f 2-) \
  --docker-password=$(grep docker.password /hostdata/secrets.prop | cut -d'=' -f 2-) \
  --namespace springbit
kubectl create secret generic springbit-secret \
  --from-literal=vault-token=$(grep vault.token /hostdata/secrets.prop | cut -d'=' -f 2-) \
  --from-literal=public-domain=$(grep spring-bit.public-domain /hostdata/secrets.prop | cut -d'=' -f 2-) \
  --namespace springbit

kubectl apply -f /hostdata/k8s -R
sleep 2

# Debug logging
kubectl -n springbit set env deployment/config-service   LOG_LEVEL=DEBUG
kubectl -n springbit set env deployment/gateway-service  LOG_LEVEL=DEBUG

# Restart the services to load the above envs
#kubectl -n springbit rollout restart deployment config-service
#kubectl -n springbit rollout restart deployment keycloak-server


kubectl get pods -A -o wide
echo ""
kubectl get nodes -o wide
echo ""
kubectl -n springbit get pv
echo ""
kubectl -n springbit get pvc
