#!/bin/bash
if [[ $# -ne 1 ]]; then
  echo "Usage: $0 s3-bucket-name"
  exit 1
fi

mkdir -p certs

aws s3 sync s3://"$1" certs --quiet

kubectl create namespace springbit

kubectl create secret tls springbit-tls \
  --key=certs/springbit.key \
  --cert=certs/springbit.crt \
  --namespace springbit

rm -rf certs
