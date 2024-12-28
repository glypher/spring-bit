package com.springbit.crypto.controller;

import com.springbit.crypto.model.QuoteRepository;
import com.springbit.crypto.model.dto.Block;
import com.springbit.crypto.model.BlockRepository;
import com.springbit.crypto.model.dto.Crypto;
import com.springbit.crypto.model.mappers.EntMapper;
import com.springbit.crypto.services.CryptoServiceDirectory;
import com.springbit.crypto.services.CryptoType;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/crypto")
@RequiredArgsConstructor
public class CryptoController {
    private final EntMapper modelMapper;

    private final BlockRepository blockRepository;

    private final QuoteRepository quoteRepository;

    private final CryptoServiceDirectory cryptoServiceDirectory;

    @Observed(name = "crypto.blocks", contextualName = "getting-crypto-blocks")
    @GetMapping("/{symbol}/blocks")
    public Flux<Block> getBlocks(@PathVariable String symbol) {
        return blockRepository.findAll().map(modelMapper::mapBlock)
                .switchIfEmpty(Flux.defer(
                        () -> cryptoServiceDirectory.getBlocks(CryptoType.map(symbol)))
                );
    }

    @Observed(name = "crypto.blocks", contextualName = "getting-crypto-quote")
    @GetMapping("/{symbol}/quote")
    public Flux<Crypto> getLatestQuote(@PathVariable String symbol) {
        return quoteRepository.findAll().map(modelMapper::mapCrypto)
                .switchIfEmpty(Flux.defer(
                        () -> cryptoServiceDirectory.getLiveQuote(List.of(CryptoType.map(symbol))))
                );
    }
}



