package com.springbit.crypto.services;

import com.springbit.crypto.model.dto.Block;
import com.springbit.crypto.model.dto.Crypto;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ICryptoService {
    String getName();

    Flux<Crypto> getLiveQuote(List<CryptoType> symbols);

    Flux<Block> getBlocks(CryptoType symbol);
}
