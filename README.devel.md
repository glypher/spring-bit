
## Development


### Vault install

Vault needs to be installed to store our secrets

On Ubuntu you can install it
```shell
wget -O - https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install vault

```

#### Web development

An angular 19 app is provided for nice visualization in [spring-bit-gateway](spring-bit-gateway/src/main/resources/web) module.

Nodejs 20 will be needed.
```shell
curl -sL https://deb.nodesource.com/setup_20.x -o nodesource_setup.sh
sudo bash nodesource_setup.sh ; rm nodesource_setup.sh

sudo apt-get install nodejs

sudo npm install -g @angular/cli@19

cd web-app
npm install -D tailwindcss postcss autoprefixer
npm install @swimlane/ngx-charts

npx tailwindcss init
npm install jasmine-core --save-dev

ng build --base-href=/web-app/ --configuration=production

# To run all tests
ng test
```

Some useful angular commands
```shell
ng new web-app
ng generate service crypto
ng generate component graph
ng generate environments
```

### Kubernetes development

#### Install Minikube
```shell
curl -LO https://github.com/kubernetes/minikube/releases/latest/download/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube && rm minikube-linux-amd64

# Install kubectl
minikube kubectl -- get po -A
# You can set the below command in ~/.bash_aliases
alias kubectl="minikube kubectl --"
kubectl version --client
```


#### Useful commands
```shell
kubectl delete sts --all; kubectl delete pods --all ; kubectl delete pvc --all ; kubectl delete pv --all

kubectl logs <pod-name>

kubectl taint nodes <node-name> node-role.kubernetes.io/control-plane:NoSchedule-

kubectl -n springbit describe pod <pod name>

minikube stop
```


### Terraform AWS deployment

```shell
wget -O - https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install terraform
terraform -install-autocomplete
```

Some usefull commands
```shell
cd terraform

terraform init
terraform validate
terraform plan
terraform apply
terraform destroy

systemctl status kubelet
journalctl -xeu kubelet
```

### Keycloak

To export a real and user start keycloak-server docker image 

```shell
docker-compose up mysql-server keycloak-server

docker exec -it keycloak-server bash
/opt/keycloak/bin/kc.sh export --dir /opt/keycloak/data/import --users realm_file --realm springbit \
  --db mysql --db-url jdbc:mysql://mysql-server:3306/keycloak?ssl-mode=DISABLED \
  --db-username keycloak-user --db-password keycloak-pass

exit

docker cp keycloak-server:/opt/keycloak/data/import/springbit-realm.json scripts/config/springbit-realm.json
```