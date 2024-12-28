package com.springbit.crypto.services;

import com.springbit.crypto.model.BlockRepository;
import com.springbit.crypto.model.QuoteRepository;
import com.springbit.crypto.model.dto.Block;
import com.springbit.crypto.model.dto.Crypto;
import com.springbit.crypto.model.mappers.EntMapper;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CryptoServiceDirectory implements ICryptoService {
    Map<String, ICryptoService> services = new HashMap<>();

    public final static String DEFAULT_SERVICE = "coinmarketcap";

    private final EntMapper entMapper = Mappers.getMapper(EntMapper.class);

    private final QuoteRepository quoteRepository;

    private final BlockRepository blockRepository;

    public void registerService(String name, ICryptoService service) {
        services.put(name, service);
    }

    public ICryptoService getService(Optional<String> name) {
        return services.get(name.orElseGet( () -> DEFAULT_SERVICE));
    }


    @Override
    public String getName() {
        return "directory";
    }

    @Override
    public Flux<Crypto> getLiveQuote(List<CryptoType> symbols) {
        var flux = this.getService(Optional.of("coinmarketcap")).getLiveQuote(symbols);

        return quoteRepository.saveAll(flux.map(entMapper::mapCryptoE)).map(entMapper::mapCrypto);
    }

    @Override
    public Flux<Block> getBlocks(CryptoType symbol) {
        var flux = this.getService(Optional.of("bitquery")).getBlocks(symbol);

        return blockRepository.saveAll(flux.map(entMapper::mapBlockE)).map(entMapper::mapBlock);
    }

}
