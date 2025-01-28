#!/bin/bash

load_properties() {
  local file=$1
  while IFS='=' read -r key value; do
    # Skip empty lines and lines starting with a comment
    if [[ -n "$key" && ! "$key" =~ ^\s*# ]]; then
      # Replace hyphens and point with underscores and export the variable
      export "${key//[-.]/_}=$value"
    fi
  done < "$file"
}

load_properties config/config.properties

# Create an internal network for DNS resolution to work
docker network create keycloak-network

rm -rf ../data/mysql/storage
docker build -t springbit/mysql-server ../docker/mysql --no-cache

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

# Change redirect-uri in realm to public domain
sed -e "s|http://localhost:8080|${spring_bit_public_domain}|g" config/springbit-realm.json  > config/toimport-realm.json


echo "Running keycloak with hostname ${spring_bit_public_domain}..."
docker run -d \
  --name keycloak-server \
  --publish 9080:8080 \
  --network keycloak-network \
  --mount type=bind,source="$(pwd)"/config/toimport-realm.json,target=/opt/keycloak/data/import/realm-export.json \
  quay.io/keycloak/keycloak:26.1.0 \
  start --http-enabled true \
        --hostname "${spring_bit_public_domain}"/keycloak --hostname-admin "${spring_bit_keycloak_admin_url}"/keycloak \
        --http-relative-path /keycloak \
        --db mysql --db-url jdbc:mysql://mysql-server:3306/keycloak?ssl-mode=DISABLED \
        --db-username keycloak-user --db-password "${spring_bit_db_mysql_keycloak_password}" \
        --bootstrap-admin-username admin --bootstrap-admin-password admin \
        --import-realm

#docker exec -it keycloak-server /opt/keycloak/bin/kc.sh build
#docker exec -it keycloak-server /opt/keycloak/bin/kc.sh show-config

until curl "${spring_bit_keycloak_admin_url}"/auth/realms/master/health > /dev/null 2>&1 ; [[ $? -eq 0 ]]
do
    echo "Waiting for keycloak to finish init..."
    sleep 3
done

docker stop keycloak-server
docker stop mysql-server
docker rm mysql-server
docker rm keycloak-server
