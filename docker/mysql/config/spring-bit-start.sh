#!/bin/bash
chown -R mysql:mysql /mysql-data

su - promtail -c "/usr/local/bin/promtail -config.file /etc/promtail/promtail-config.yml" &

exec "$@"