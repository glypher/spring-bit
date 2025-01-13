#!/bin/bash

kubectl create namespace springbit
kubectl delete pv --all

kubectl apply -f /hostdata/k8s -R
sleep 2

kubectl -n springbit set env deployment/config-service VAULT_USER_TOKEN=$(sudo grep vault.token /hostdata/secrets.prop | cut -d'=' -f 2-)

kubectl get pods -A -o wide
echo ""
kubectl get nodes -o wide
echo ""
kubectl -n springbit get pv
echo ""
kubectl -n springbit get pvc

# Set port forwarding
kubectl -n springbit port-forward svc/nginx-ingress-controller 80:80 443:443
