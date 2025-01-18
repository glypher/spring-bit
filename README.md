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

```consoles
sudo chown -R $USER data/
cd scripts
chmod a+x gen_key_stores.sh init_vault.sh
./gen_key_stores.sh

# Add tokens
echo "bitquery.api-token=REPACE_ME" >> ../data/secrets.prop
echo "coinmarketcap.api-token=REPLACE_ME" >> ../data/secrets.prop
echo "mysql.password=spring-bit-mysql" >> ../data/secrets.prop

./init_vault.sh
cd ..
```

Then we can actually build the jars and deploy them in the docker containers.

```console
cd web-app; ng build --base-href=/web-app/ --configuration=production ; cd ..

docker system prune -a

./mvnw -DskipTests clean install

export VAULT_USER_TOKEN=$(grep vault.token data/secrets.prop | cut -d'=' -f 2-)
docker-compose build --no-cache
```

## Running locally

### Docker Compose

After running the steps in building you can just issue

```console
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

Next need to build all images to minikube's docker container
```console
sudo chown -R $USER data/
minikube delete
minikube start --mount --mount-string="./data:/hostdata" --driver=docker
minikube status
minikube ssh -- ls /hostdata

# Configure docker to point to minikube's docker daemon
eval $(minikube docker-env)

./mvnw -DskipTests clean install ; docker-compose build --no-cache
# Check built images..
minikube ssh -- docker images
```

Now run the k8s deployment
```console
kubectl delete pods --all
kubectl label nodes --all springbit.org/volume=yes

kubectl create namespace springbit
kubectl apply -f k8s -R 
kubectl set env deployment/config-service VAULT_USER_TOKEN=$(grep vault.token data/secrets.prop | cut -d'=' -f 2-)

kubectl get nodes --show-labels
kubectl get pods -A -o wide
kubectl get pv 


# Tunnel gateway service pod port to hosts localhost:8080
kubectl -n springbit port-forward deployment/gateway-service 8080:8080
```
Now you can access the app through localhost:8080 like before see [Service table](#Services)

Some useful commands
```console
kubectl delete sts --all; kubectl delete pods --all ; kubectl delete pvc --all ; kubectl delete pv --all

kubectl logs <pod-name>

kubectl taint nodes <node-name> node-role.kubernetes.io/control-plane:NoSchedule-

kubectl -n springbit describe pod <pod name>

minikube stop
```

