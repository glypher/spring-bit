package com.springbit.crypto.model;

import com.springbit.crypto.model.entity.ECrypto;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface QuoteRepository extends ReactiveCrudRepository<ECrypto,Integer> {
    Flux<ECrypto> findBySymbol(String symbol);

}
