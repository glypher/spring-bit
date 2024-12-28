#!/bin/bash

echo "Starting to unseal vault..."
export VAULT_ADDR=http://127.0.0.1:8201

export VAULT_TOKEN=REPLACE_TOKEN

sleep 1

vault status

# Unseal vault
vault operator unseal REPLACE_KEY_1
vault operator unseal REPLACE_KEY_2
vault operator unseal REPLACE_KEY_3

echo "Unsealed vault..."