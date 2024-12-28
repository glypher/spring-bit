package com.springbit.crypto.model;

import com.springbit.crypto.model.entity.EBlock;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;


public interface BlockRepository extends ReactiveCrudRepository<EBlock,Integer> {
}
