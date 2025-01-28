#!/usr/bin/dumb-init /bin/sh

echo "Delaying unseal after server start"

chown -R vault:vault /vault-data

chmod 755 /vault-data/unseal/unseal.sh
/vault-data/unseal/unseal.sh &


echo "Starting server"
exec "$@"