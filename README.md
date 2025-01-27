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

First we need to create the PKI self-signed certificates keystores and trust stores that hold them.

```shell
sudo rm -rf data/ ; mkdir -p data
cd scripts
chmod a+x gen_key_stores.sh init_vault.sh init_keycloak.sh
./gen_key_stores.sh

# Add tokens
echo "bitquery.api-token=REPACE_ME" >> ../data/secrets.prop
echo "coinmarketcap.api-token=REPLACE_ME" >> ../data/secrets.prop
echo "mysql.password=spring-bit-mysql" >> ../data/secrets.prop

./init_vault.sh

sudo rm -rf ../data/mysql/storage/ ; ./init_keycloak.sh
cd ..
```

Then we can actually build the jars and deploy them in the docker containers.

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
kubectl apply -f k8s -R 
kubectl -n springbit set env deployment/config-service VAULT_USER_TOKEN=$(grep vault.token data/secrets.prop | cut -d'=' -f 2-)
kubectl -n springbit rollout restart deployment config-service

# Check out deployment
kubectl get nodes --show-labels
kubectl get pods -A -o wide
kubectl get pv 
```

4. Set up development communication channels to the services
```shell
# Tunnel gateway service pod port to hosts localhost:8080
kubectl -n springbit port-forward deployment/gateway-service 8080:8080 &
kubectl -n springbit port-forward deployment/auth-server 9443:9443 &
```
Now you can access the app through localhost:8080 like before see [Service table](#Services)

Some useful k8s commands can be found at [README.devel k8s development](README.devel.md#kubernetes-development)

5. When you're done just issue
```shell
minikube stop
```
