#!/usr/bin/dumb-init /bin/sh

echo "Starting to unseal vault..."
export VAULT_ADDR=http://127.0.0.1:8201

export VAULT_TOKEN=REPLACE_TOKEN

until vault status ; [[ $? -ne 1 ]]
do
    echo "Trying to connect to vault..."
    sleep 2
done

# Unseal vault
vault operator unseal REPLACE_KEY_1
vault operator unseal REPLACE_KEY_2
vault operator unseal REPLACE_KEY_3

echo "Unsealed vault..."

rm -- "$0"
