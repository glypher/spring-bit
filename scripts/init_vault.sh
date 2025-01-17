#!/bin/bash

DATA_DIR=$PWD/../data

VAULT_DATA=$DATA_DIR/vault/storage

VAULT_KEYS=$DATA_DIR/vault-keys

VAULT_UNSEAL_DIR=../docker/vault/unseal
VAULT_CONFIG_DIR=../docker/vault/config
VAULT_CONFIG=$VAULT_CONFIG_DIR/vault-server-init.hcl
VAULT_POLICY=$VAULT_CONFIG_DIR/spring-bit-policy.hcl

mkdir -p $DATA_DIR/mysql/storage

rm -rf $VAULT_DATA ; mkdir -p $VAULT_DATA

sed -e "s|REPLACE_ME|\"$VAULT_DATA\"|g" $VAULT_CONFIG > tmp-config.hcl

vault server -config=tmp-config.hcl &
sleep 2
rm tmp-config.hcl

export VAULT_ADDR=http://127.0.0.1:8200
vault status

# generate unseal keys
rm -rf $VAULT_KEYS ; mkdir -p $VAULT_KEYS
vault operator init -key-shares=3 -key-threshold=3  &> $VAULT_KEYS/unseal-keys.txt

# Unseal vault
mapfile -t keyArray < <( grep "Unseal Key " < $VAULT_KEYS/unseal-keys.txt  | cut -c15- )
vault operator unseal ${keyArray[0]}
vault operator unseal ${keyArray[1]}
vault operator unseal ${keyArray[2]}

mapfile -t rootToken < <(grep "Initial Root Token: " < $VAULT_KEYS/unseal-keys.txt  | cut -c21- )
export VAULT_TOKEN=${rootToken[0]}

# Enable kv engine and set key
vault secrets enable -version=2 -path=spring-bit-config kv

# create config-service auth token
vault policy write spring-bit-policy $VAULT_POLICY
vault token create -policy=spring-bit-policy &> $VAULT_KEYS/auth-spring-bit-token.txt

mapfile -t userToken < <(grep "token     " < $VAULT_KEYS/auth-spring-bit-token.txt  | cut -c21- | sed 's/^\ *//')

sed -i -e "s|^vault.token=.*||g" ./secrets.prop

# PUT ALL SECRETS
vault kv put -mount=spring-bit-config keys spring.bit=init
while IFS='=' read -r key value; do
  echo "Setting key $key..."
  [[ -n $key ]] && [[ -n $value ]] && vault kv patch -mount=spring-bit-config keys $key=$value
done < "$DATA_DIR/secrets.prop"
#echo "Keys...."
#vault kv get -mount spring-bit-config keys

# replace it in the spring config server's configuration
echo "vault.token=${userToken[0]}" > $DATA_DIR/secrets.prop


# Copy useal script as well
rm -rf $VAULT_UNSEAL_DIR ; mkdir -p $VAULT_UNSEAL_DIR
cp $VAULT_CONFIG_DIR/unseal.sh $VAULT_UNSEAL_DIR/.

sed -i -e "s|REPLACE_TOKEN|${rootToken[0]}|g" $VAULT_UNSEAL_DIR/unseal.sh
sed -i -e "s|REPLACE_KEY_1|${keyArray[0]}|g"  $VAULT_UNSEAL_DIR/unseal.sh
sed -i -e "s|REPLACE_KEY_2|${keyArray[1]}|g"  $VAULT_UNSEAL_DIR/unseal.sh
sed -i -e "s|REPLACE_KEY_3|${keyArray[2]}|g"  $VAULT_UNSEAL_DIR/unseal.sh

cp $VAULT_UNSEAL_DIR/unseal.sh $VAULT_KEYS/unseal.sh

pkill -P $$
sleep 1
