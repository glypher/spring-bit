## Spring-Bit application

Spring 3 webflux microservices app for getting crypto chain info


### Building and running

Api gateway and crypto service connects securely to a spring config server do get the production profile settings used in the docker container deployment.

First we need to create the PKI self-signed certificates keystores and truststores that hold them.

```console
cd spring-bit-config/scripts

chmod a+x gen_key_stores.sh

./gen_key_stores.sh

cd ../..
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
