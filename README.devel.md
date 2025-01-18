
## Development


### Vault install

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

### Kubernetes development

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