#!/bin/bash
if [[ $# -ne 1 ]]; then
  echo "Usage: $0 s3-bucket-name"
  exit 1
fi

kubeadm token create --print-join-command --ttl 12h > k8s_join.sh

aws s3 cp k8s_join.sh s3://"$1" --quiet

rm k8s_join.sh
