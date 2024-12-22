package com.springbit.crypto.web;

import com.springbit.crypto.web.crypto.DodgeNetwork;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.client.FieldAccessException;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class GraphQlClient {
    @Value("${spring-bit.bitquery.apitoken}")
    private String apiToken;

    public Mono<List<DodgeNetwork>> getChain() {
        WebClient client = WebClient.builder()
                .baseUrl("https://graphql.bitquery.io")
                .defaultHeader("X-API-KEY", apiToken)
                .build();
        var graphQlClient = HttpGraphQlClient.builder(client).build();

        return graphQlClient.document(DodgeNetwork.getGraphQlQuery())
                .retrieve("bitcoin.blocks")
                .toEntityList(DodgeNetwork.class)
                .onErrorResume(FieldAccessException.class,
                        ex -> Mono.just(new ArrayList<>()));
    }
}
