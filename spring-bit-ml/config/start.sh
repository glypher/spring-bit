#!/bin/bash
echo "Starting promtail..."
su promtail -c "/usr/local/bin/promtail -config.file /etc/promtail/promtail-config.yml" &

echo "Starting uvicorn fastapi..."
exec su springbit --preserve-environment -c "$*"
