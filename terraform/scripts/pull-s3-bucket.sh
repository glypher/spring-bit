#!/bin/bash
if [[ $# -ne 1 ]]; then
  echo "Usage: $0 s3-bucket-name"
  exit 1
fi

sudo mkdir -p /hostdata

sudo aws s3 sync s3://"$1" /hostdata
