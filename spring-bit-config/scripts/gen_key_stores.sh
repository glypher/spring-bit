#!/bin/bash
CONFIG_DIR=../src/main/resources/stores
CRYPTO_DIR=../../spring-bit-crypto/src/main/resources/stores
GATEWAY_DIR=../../spring-bit-gateway/src/main/resources/stores



# Config server key pair
rm -rf $CONFIG_DIR ; mkdir -p $CONFIG_DIR
keytool -genkeypair -noprompt -keyalg EC -alias spring-bit-config -dname "CN=config-service" -validity 365 -keypass springbit -keystore $CONFIG_DIR/config-keystore.p12 -storepass springbit  -deststoretype pkcs12

# Crypto service key pair
rm -rf $CRYPTO_DIR ; mkdir -p $CRYPTO_DIR
keytool -genkeypair -noprompt -keyalg EC -alias spring-bit-crypto -dname "CN=crypto-service" -validity 365 -keypass springbit -keystore $CRYPTO_DIR/crypto-keystore.p12 -storepass springbit  -deststoretype pkcs12

# Api Gateway service key pair
rm -rf $GATEWAY_DIR ; mkdir -p $GATEWAY_DIR
keytool -genkeypair -noprompt -keyalg EC -alias spring-bit-gateway -dname "CN=gateway-service" -validity 365 -keypass springbit -keystore $GATEWAY_DIR/gateway-keystore.p12 -storepass springbit  -deststoretype pkcs12

# Trust store config service
keytool -exportcert -noprompt -rfc -alias spring-bit-crypto -file spring-bit-crypto.crt -keystore $CRYPTO_DIR/crypto-keystore.p12 -storepass springbit
keytool -importcert -noprompt -alias spring-bit-crypto -file spring-bit-crypto.crt -keystore $CONFIG_DIR/config-truststore.p12 -storepass springbit -deststoretype pkcs12
rm spring-bit-crypto.crt

keytool -exportcert -noprompt -rfc -alias spring-bit-gateway -file spring-bit-gateway -keystore $GATEWAY_DIR/gateway-keystore.p12 -storepass springbit
keytool -importcert -noprompt -alias spring-bit-gateway -file spring-bit-gateway -keystore $CONFIG_DIR/config-truststore.p12 -storepass springbit -deststoretype pkcs12
rm spring-bit-gateway

# Trust store crypto service
keytool -exportcert -noprompt -rfc -alias spring-bit-config -file spring-bit-config.crt -keystore $CONFIG_DIR/config-keystore.p12 -storepass springbit
keytool -importcert -noprompt -alias spring-bit-config -file spring-bit-config.crt -keystore $CRYPTO_DIR/crypto-truststore.p12 -storepass springbit -deststoretype pkcs12
rm spring-bit-config.crt

# Trust store gateway service
keytool -exportcert -noprompt -rfc -alias spring-bit-config -file spring-bit-config.crt -keystore $CONFIG_DIR/config-keystore.p12 -storepass springbit
keytool -importcert -noprompt -alias spring-bit-config -file spring-bit-config.crt -keystore $GATEWAY_DIR/gateway-truststore.p12 -storepass springbit -deststoretype pkcs12
rm spring-bit-config.crt