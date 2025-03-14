## Spring-Bit application

Spring 3 webflux microservices app for getting crypto chain info

Api gateway and crypto service connects securely to a spring config server do get the production profile settings used in the docker container deployment.


## Building

The development environment requires:

- Ubuntu 22/24 amd64 virtual machine (I used VirtualBox)
- openjdk23
- Docker and docker-compose
- Hashicorp Vault Server - see [README.devel vault](README.devel.md#vault-install)
- Node 20 and Angular 19 - see [README.devel web development](README.devel.md#web-development)
- [optional] K8s Minikube - see [README.devel k8s development](README.devel.md#kubernetes-development)
- [optional] Terraform and AWS client - see [README.devel web development](README.devel.md#terraform-aws-deployment)
- [optional] Intellij

1. Configure secrets in config.properties file
```shell
cp scripts/config/config.properties.template scripts/config/config.properties
# REPLACE SECRETS to fully configure all services like oauth2
```

2. Create PKI self-signed certificates and init vault and keycloak server
```shell
sudo rm -rf data/ ; mkdir -p data
cd scripts
chmod a+x gen_key_stores.sh init_vault.sh init_keycloak.sh
# generate key pairs
./gen_key_stores.sh

./init_vault.sh

# Initialize keycloak
./init_keycloak.sh
cd ..

sudo chown -R $USER data
```

3. Build all jars and create docker containers

```shell
cd web-app; ng build --base-href=/web-app/ --configuration=production ; cd ..

docker system prune -a

./mvnw -DskipTests clean install

docker-compose build --no-cache
```

## Running locally

### Docker Compose

After running the steps in building you can just issue

```shell
export VAULT_USER_TOKEN=$(grep vault.token data/secrets.prop | cut -d'=' -f 2-)
export OPENAI_API_KEY=$(grep openai.key data/secrets.prop | cut -d'=' -f 2-)
# Kafka user id
sudo chown -R 1001:1001 ./data/kafka
docker-compose up
```

### Services

All web interfaces of all services are available through the API gateway.


| Spring Bit components  | Resources                                                                                                           |
|------------------------|---------------------------------------------------------------------------------------------------------------------|
| API Gateway Web App    | [localhost:8080/web-app](http://localhost:8080/web-app)                                                             |
| Service Discovery      | [localhost:8080/discovery](http://localhost:8080/discovery)                                                         |
| Crypto Service OpenApi | [localhost:8080/api/v1/docs/ui](http://localhost:8080/api/v1/docs/ui)                                               |
| Prometheus server      | [localhost:8080/prometheus](http://localhost:8080/prometheus)                                                       |
| Graphana Dashboard     | [localhost:8080/grafana/d/spingbit/spring-bit-metrics](http://localhost:8080/grafana/d/spingbit/spring-bit-metrics) |
| Tracing server         | [localhost:8080/tracing](http://localhost:8080/tracing)                                                             |

### Minikube local k8s

Install Minikube as in [README.devel k8s development](README.devel.md#kubernetes-development) with kubectl alias

1. Setup Minikube environment(container) and mount the volume to the container
```shell
sudo chown -R $USER data/
# Kafka user id
sudo chown -R 1001:1001 ./data/kafka
minikube delete
minikube start --mount --mount-string="./data:/hostdata" --driver=docker
minikube status
minikube ssh -- ls /hostdata
```
2. Build all images to minikube's docker container
```shell
# Configure docker to point to minikube's docker daemon
eval $(minikube docker-env)
./mvnw -DskipTests clean install
docker-compose build --no-cache
# Check built images..
minikube ssh -- docker images
```

3. Run the k8s deployment
```shell
# First label Minikube only node so that all k8s springbit services are deployed to it
kubectl label nodes --all springbit.org/publichost=yes
kubectl label nodes --all springbit.org/monitoring=yes
kubectl label nodes --all springbit.org/volume=yes
kubectl label nodes --all springbit.org/frontend=yes
kubectl label nodes --all springbit.org/backend=yes

kubectl create namespace springbit

kubectl create secret docker-registry docker-secret \
  --docker-server=docker.io \
  --docker-username=$(grep docker.username data/secrets.prop | cut -d'=' -f 2-) \
  --docker-password=$(grep docker.password data/secrets.prop | cut -d'=' -f 2-) \
  --namespace springbit
kubectl create secret generic springbit-secret \
  --from-literal=vault-token=$(grep vault.token data/secrets.prop | cut -d'=' -f 2-) \
  --from-literal=public-domain=$(grep spring-bit.public-domain data/secrets.prop | cut -d'=' -f 2-) \
  --from-literal=openai-key=$(grep openai.key data/secrets.prop | cut -d'=' -f 2-) \
  --namespace springbit

# Apply the deployment
kubectl apply -f k8s -R 

# Check out deployment
kubectl get nodes --show-labels
kubectl get pods -A -o wide
kubectl get pv
```

4. Set up development communication channels to the services
```shell
# Tunnel gateway service pod port to hosts localhost:8080
kubectl -n springbit port-forward deployment/gateway-service 8080:8080
```
Now you can access the app through localhost:8080 like before see [Service table](#Services)

Some useful k8s commands can be found at [README.devel k8s development](README.devel.md#kubernetes-development)

5. When you're done just issue
```shell
minikube stop
```
