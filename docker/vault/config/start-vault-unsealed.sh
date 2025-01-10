#!/usr/bin/dumb-init /bin/sh

echo "Delaying unseal after server start"
/usr/local/bin/unseal.sh &

chown -R vault:vault /vault-data

echo "Starting server"
exec "$@"