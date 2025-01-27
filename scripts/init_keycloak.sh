#!/bin/bash

KEYCLOACK_PASSWORD="${1:-keycloak-pass}"

# Create an internal network for DNS resolution to work
docker network create keycloak-network

rm -rf ../data/mysql/storage
docker build -t springbit/mysql-server ../docker/mysql

docker stop keycloak-server
docker stop mysql-server
docker rm mysql-server
docker rm keycloak-server

#1. Start mysql using local data dir
echo "Running mysql..."
docker run -d \
  --name mysql-server \
  --publish 3306:3306 \
  --mount type=bind,source="$(pwd)"/../data/mysql,target=/mysql-data \
  --network keycloak-network \
  springbit/mysql-server

sleep 2

echo "Running keycloak..."
docker run -d \
  --name keycloak-server \
  --publish 9080:8080 \
  --network keycloak-network \
  --mount type=bind,source="$(pwd)"/realm-export.json,target=/opt/keycloak/data/import/realm-export.json \
  quay.io/keycloak/keycloak:26.1.0 \
  start --http-enabled true \
        --hostname http://localhost:8080/keycloak --hostname-admin http://localhost:9080/keycloak \
        --http-relative-path /keycloak \
        --db mysql --db-url jdbc:mysql://mysql-server:3306/keycloak?ssl-mode=DISABLED \
        --db-username keycloak-user --db-password "$KEYCLOACK_PASSWORD" \
        --bootstrap-admin-username admin --bootstrap-admin-password admin \
        --import-realm

#docker exec -it keycloak-server /opt/keycloak/bin/kc.sh build
#docker exec -it keycloak-server /opt/keycloak/bin/kc.sh show-config

until curl http://localhost:9080/auth/realms/master/health > /dev/null 2>&1 ; [[ $? -eq 0 ]]
do
    echo "Trying to connect to keycloak..."
    sleep 3
done

docker stop keycloak-server
docker stop mysql-server
docker rm mysql-server
docker rm keycloak-server
