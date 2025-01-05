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
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/crypto")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CryptoController {
    private final EntMapper modelMapper;

    private final BlockRepository blockRepository;

    private final QuoteRepository quoteRepository;

    private final CryptoServiceDirectory cryptoServiceDirectory;

    @Observed(name = "crypto", contextualName = "getting-cryptos")
    @GetMapping("/cryptos")
    public Flux<CryptoType> getAvailableCryptos() {
       return Flux.fromArray(CryptoType.values())
               .filter(
                       ct -> ct != CryptoType.UNKNOWN);
    }

    @Observed(name = "crypto", contextualName = "getting-crypto-blocks")
    @GetMapping("/{symbol}/blocks")
    public Flux<Block> getBlocks(@PathVariable String symbol) {
        return blockRepository.findAll().map(modelMapper::mapBlock)
                .switchIfEmpty(Flux.defer(
                        () -> cryptoServiceDirectory.getBlocks(CryptoType.map(symbol)))
                );
    }

    @Observed(name = "crypto", contextualName = "getting-crypto-quote")
    @GetMapping("/{symbol}/quote")
    public Flux<Crypto> getLatestQuote(@PathVariable String symbol) {
        CryptoType cryptoType = CryptoType.map(symbol);
        return quoteRepository.findBySymbol(cryptoType.getSymbol()).map(modelMapper::mapCrypto)
                .switchIfEmpty(Flux.defer(
                        () -> cryptoServiceDirectory.getLiveQuote(List.of(CryptoType.map(symbol))))
                );
    }
}



