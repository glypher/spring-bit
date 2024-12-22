package com.springbit.crypto.model;

import com.springbit.crypto.web.GraphQlClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QlLoader {
    private final EntMapper modelMapper;

    private final DodgeRepository repository;

    private final GraphQlClient graphQlClient;

    @Value("${spring-bit.init-graphql}") boolean initQl;

    @PostConstruct
    public void initDb() {
        if (initQl) {
            var dodges = graphQlClient.getChain();
            dodges.subscribe(lstDodge -> {
                repository.saveAll(lstDodge.stream().map(modelMapper::map).toList());
            });

        }
    }
}
