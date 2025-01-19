#!/bin/bash
CONFIG_DIR=../spring-bit-config/src/main/resources/stores
CRYPTO_DIR=../spring-bit-crypto/src/main/resources/stores
GATEWAY_DIR=../spring-bit-gateway/src/main/resources/stores
AUTH_SERVER_DIR=../spring-bit-auth-server/src/main/resources/stores
VAULT_DIR=../docker/vault/certs
MYSQL_DIR=../docker/mysql/certs

# Vault server
rm -rf $VAULT_DIR ; mkdir -p $VAULT_DIR
keytool -genkeypair -noprompt -keyalg EC -alias vault-server -dname "CN=vault-server" -validity 365 -keystore vault-keystore.p12 -storepass springbit -deststoretype pkcs12
openssl pkcs12 -in vault-keystore.p12 -nodes -nocerts -out $VAULT_DIR/vault-key.pem -passin pass:springbit
openssl pkcs12 -in vault-keystore.p12 -nokeys -out $VAULT_DIR/vault-cert.pem -passin pass:springbit
rm vault-keystore.p12

# Config server key pair
rm -rf $CONFIG_DIR ; mkdir -p $CONFIG_DIR
keytool -genkeypair -noprompt -keyalg EC -alias spring-bit-config -dname "CN=config-service" -validity 365 -keypass springbit -keystore $CONFIG_DIR/config-keystore.p12 -storepass springbit  -deststoretype pkcs12

# Crypto service key pair
rm -rf $CRYPTO_DIR ; mkdir -p $CRYPTO_DIR
keytool -genkeypair -noprompt -keyalg EC -alias spring-bit-crypto -dname "CN=crypto-service" -validity 365 -keypass springbit -keystore $CRYPTO_DIR/crypto-keystore.p12 -storepass springbit  -deststoretype pkcs12

# Api Gateway service key pair
rm -rf $GATEWAY_DIR ; mkdir -p $GATEWAY_DIR
keytool -genkeypair -noprompt -keyalg EC -alias spring-bit-gateway -dname "CN=gateway-service" -validity 365 -keypass springbit -keystore $GATEWAY_DIR/gateway-keystore.p12 -storepass springbit  -deststoretype pkcs12

# Auth Server key pair
rm -rf $AUTH_SERVER_DIR ; mkdir -p $AUTH_SERVER_DIR
keytool -genkeypair -noprompt -keyalg EC -alias spring-bit-auth-server -dname "CN=auth-server" -validity 365 -keypass springbit -keystore $AUTH_SERVER_DIR/auth-server-keystore.p12 -storepass springbit  -deststoretype pkcs12

# Trust store config service
keytool -exportcert -noprompt -rfc -alias spring-bit-crypto -file spring-bit-crypto.crt -keystore $CRYPTO_DIR/crypto-keystore.p12 -storepass springbit
keytool -importcert -noprompt -alias spring-bit-crypto -file spring-bit-crypto.crt -keystore $CONFIG_DIR/config-truststore.p12 -storepass springbit -deststoretype pkcs12
rm spring-bit-crypto.crt

keytool -exportcert -noprompt -rfc -alias spring-bit-gateway -file spring-bit-gateway.crt -keystore $GATEWAY_DIR/gateway-keystore.p12 -storepass springbit
keytool -importcert -noprompt -alias spring-bit-gateway -file spring-bit-gateway.crt -keystore $CONFIG_DIR/config-truststore.p12 -storepass springbit -deststoretype pkcs12
rm spring-bit-gateway.crt

keytool -exportcert -noprompt -rfc -alias spring-bit-auth-server -file spring-bit-auth-server.crt -keystore $AUTH_SERVER_DIR/auth-server-keystore.p12 -storepass springbit
keytool -importcert -noprompt -alias spring-bit-auth-server -file spring-bit-auth-server.crt -keystore $CONFIG_DIR/config-truststore.p12 -storepass springbit -deststoretype pkcs12
rm spring-bit-auth-server.crt

openssl x509 -outform der -in $VAULT_DIR/vault-cert.pem -out vault-server.crt
keytool -importcert -noprompt -alias spring-bit-vault-server -file vault-server.crt -keystore $CONFIG_DIR/config-truststore.p12 -storepass springbit -deststoretype pkcs12
rm vault-server.crt

# Add config server certificate to all truststores needed
keytool -exportcert -noprompt -rfc -alias spring-bit-config -file spring-bit-config.crt -keystore $CONFIG_DIR/config-keystore.p12 -storepass springbit

keytool -importcert -noprompt -alias spring-bit-config -file spring-bit-config.crt -keystore $CRYPTO_DIR/crypto-truststore.p12 -storepass springbit -deststoretype pkcs12

keytool -importcert -noprompt -alias spring-bit-config -file spring-bit-config.crt -keystore $GATEWAY_DIR/gateway-truststore.p12 -storepass springbit -deststoretype pkcs12

keytool -importcert -noprompt -alias spring-bit-config -file spring-bit-config.crt -keystore $AUTH_SERVER_DIR/auth-server-truststore.p12 -storepass springbit -deststoretype pkcs12

rm spring-bit-config.crt

# Mysql server
rm -rf $MYSQL_DIR ; mkdir -p $MYSQL_DIR
keytool -genkeypair -noprompt -keyalg EC -alias mysql-server -dname "CN=mysql-server" -validity 365 -keystore mysql-keystore.p12 -storepass springbit -deststoretype pkcs12
openssl pkcs12 -in mysql-keystore.p12 -nodes -nocerts -out $MYSQL_DIR/mysql-key.pem -passin pass:springbit
openssl pkcs12 -in mysql-keystore.p12 -nokeys -out $MYSQL_DIR/mysql-cert.pem -passin pass:springbit
rm mysql-keystore.p12
# Add crypto service cert as ca
openssl pkcs12 -in $CRYPTO_DIR/crypto-keystore.p12 -nokeys -out $MYSQL_DIR/mysql-ca-cert.pem -passin pass:springbit
