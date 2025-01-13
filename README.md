## Spring-Bit application

Spring 3 webflux microservices app for getting crypto chain info


### Building and running

Api gateway and crypto service connects securely to a spring config server do get the production profile settings used in the docker container deployment.

The development environment is a host Ubuntu 22 amd64 machine with openjdk23 and Intellij.

First we need to create the PKI self-signed certificates keystores and trust stores that hold them.

We also need to set up the vault server

```consoles
cd scripts

chmod a+x gen_key_stores.sh init_vault.sh

./gen_key_stores.sh

./init_vault.sh

cd ..
```

Then we can actually build the jars and deploy them in the docker containers.

```console
cd web-app; ng build --base-href=/web-app/ --configuration=production ; cd ..

./mvnw -DskipTests clean install
 
export VAULT_USER_TOKEN=$(grep vault.token data/secrets.prop | cut -d'=' -f 2-)
docker-compose build --no-cache
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

### Kubernates deployment

Install Minikube
```console
curl -LO https://github.com/kubernetes/minikube/releases/latest/download/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube && rm minikube-linux-amd64


# Install kubectl
minikube kubectl -- get po -A
# You can set the below command in ~/.bash_aliases
alias kubectl="minikube kubectl --"
kubectl version --client
```

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


### Development


#### Vault install

Vault needs to be installed to store our secrets

On Ubuntu you can install it
```console
wget -O - https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install vault

```

#### Web development

An angular 19 app is provided for nice visualization in [spring-bit-gateway](spring-bit-gateway/src/main/resources/web) module.

Nodejs 20 will be needed.
```console
curl -sL https://deb.nodesource.com/setup_20.x -o nodesource_setup.sh
sudo bash nodesource_setup.sh ; rm nodesource_setup.sh

sudo apt-get install nodejs

sudo npm install -g @angular/cli@19

cd web-app
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init
npm install jasmine-core --save-dev

ng build --base-href=/web-app/ --configuration=production

# To run all tests
ng test
```

Some useful angular commands
```console
ng new web-app
ng generate service crypto
ng generate component graph
ng generate environments
```


### Terraform AWS deployment

```console
wget -O - https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install terraform
terraform -install-autocomplete
```

Some usefull commands
```console
cd terraform

terraform init
terraform validate
terraform plan
terraform apply
terraform destroy

systemctl status kubelet
journalctl -xeu kubelet

```