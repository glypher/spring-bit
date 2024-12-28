package com.springbit.crypto.model;

import com.springbit.crypto.model.entity.ECrypto;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface QuoteRepository extends ReactiveCrudRepository<ECrypto,Integer> {
}
