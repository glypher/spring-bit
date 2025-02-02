#!/bin/bash
if [[ $# -ne 1 ]]; then
  echo "Usage: $0 s3-bucket-name"
  exit 1
fi

sudo mkdir -p /hostdata

sudo aws s3 sync s3://"$1" /hostdata --quiet

sudo mkdir -p /hostdata/mysql/storage
sudo mkdir -p /hostdata/kafka

sudo chmod u+rw -R /hostdata

# Kafka user id
sudo chown -R 1001:1001 /hostdata/kafka
