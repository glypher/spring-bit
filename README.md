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
./mvnw -DskipTests clean install
 
 docker-compose up
```

### Services

All web interfaces of all services are available through the API gateway.


| Spring Bit components  | Resources                                                                                                           |
|------------------------|---------------------------------------------------------------------------------------------------------------------|
| API Gateway            | [localhost:8080](http://localhost:8080)                                                                                  |
| Service Discovery      | [localhost:8080/discovery](http://localhost:8080/discovery)                                                              |
| Crypto Service OpenApi | [localhost:8080/api/v1/docs/ui](http://localhost:8080/api/v1/docs/ui)                                                    |
| Prometheus server      | [localhost:8080/prometheus](http://localhost:8080/prometheus)                                                            |
| Graphana Dashboard     | [localhost:8080/grafana/d/spingbit/spring-bit-metrics](http://localhost:8080/grafana/d/spingbit/spring-bit-metrics) |
| Tracing server         | [localhost:8080/tracing](http://localhost:8080/tracing)                                                                  |



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
cd spring-bit-gateway/src/main/resources/

sudo npm install -g @angular/cli@19

ng new web-app
cd web-app

npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init

ng generate service crypto
```