#!/bin/bash
# This is sourced from docker_process_init_files
# https://github.com/docker-library/mysql/blob/df3a5c483a5e8c3c4d1eae61678fa5372c403bf0/docker-entrypoint.sh

mysql_note "Creating database keycloak"
docker_process_sql --database=mysql <<<"CREATE DATABASE IF NOT EXISTS keycloak;"

mysql_note "Creating user keycloak-user"
docker_process_sql --database=mysql <<<"CREATE USER 'keycloak-user'@'%' IDENTIFIED BY '$KEYCLOAK_PASSWORD' ;"

mysql_note "Giving user keycloak-user access to schema keycloak"
docker_process_sql --database=mysql <<<"GRANT ALL ON keycloak.* TO 'keycloak-user'@'%' ;"
