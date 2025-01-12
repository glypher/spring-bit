#!/bin/bash
# To run pods on control-plane
kubectl taint nodes control-plane node-role.kubernetes.io/control-plane:NoSchedule-

kubectl delete namespace springbit
kubectl create namespace springbit
kubectl delete pv --all

kubectl apply -f /hostdata/k8s -R
sleep 2

kubectl -n springbit set env deployment/config-service VAULT_USER_TOKEN=$(grep vault.token /hostdata/secrets.prop | cut -d'=' -f 2-)
kubectl apply -f /hostdata/k8s/config-service -R

kubectl get pods -A -o wide
echo ""
kubectl get nodes -o wide
echo ""
kubectl -n springbit get pv
echo ""
kubectl -n springbit get pvc

# Set port forwarding
sudo iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 31000
