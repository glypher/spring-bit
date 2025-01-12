#!/bin/bash
# To run pods on control-plane
kubectl taint nodes control-plane node-role.kubernetes.io/control-plane:NoSchedule-

kubectl delete namespace springbit
kubectl create namespace springbit

kubectl -n springbit set env deployment/config-service VAULT_USER_TOKEN=$(grep vault.token /hostdata/secrets.prop | cut -d'=' -f 2-)
kubectl apply -f /hostdata/k8s -R

kubectl get pods -A -o wide
echo ""
kubectl get nodes -o wide
echo ""
kubectl get pv
