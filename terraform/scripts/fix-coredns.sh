#!/bin/bash

# The Aws ec2 instance /etc/resolv.conf points to 127.0.0.53 ip which does not correctly get resolved in pods
# Add 8.8.8.8 to the resolution instead
kubectl get configmap coredns -n kube-system -o yaml > coredns-backup.yaml

sed -i -e "s|forward . /etc/resolv.conf.*|forward . 8.8.8.8 {|g" coredns-backup.yaml
kubectl apply -f coredns-backup.yaml
rm coredns-backup.yaml

kubectl rollout restart deployment coredns -n kube-system
