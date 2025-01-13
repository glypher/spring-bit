#!/bin/bash
if [[ $# -ne 1 ]]; then
  echo "Usage: $0 s3-bucket-name"
  exit 1
fi

mkdir -p certs

aws s3 sync s3://"$1" certs

kubectl create namespace springbit

kubectl create secret tls sprinbit-tls \
  --cert=certs/springbit.crt \
  --key=certs/springbit.key \
  --namespace springbit

rm -rf certs
