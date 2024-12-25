## Spring-Bit application

Spring 3 webflux microservices app for getting crypto chain info


### Building and running

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
