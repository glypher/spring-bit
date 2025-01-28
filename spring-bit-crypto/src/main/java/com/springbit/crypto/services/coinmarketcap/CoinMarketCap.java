package com.springbit.crypto.services.coinmarketcap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springbit.crypto.model.BlockRepository;
import com.springbit.crypto.model.QuoteRepository;
import com.springbit.crypto.model.dto.Block;
import com.springbit.crypto.services.CryptoType;
import com.springbit.crypto.model.dto.Crypto;
import com.springbit.crypto.services.CryptoServiceDirectory;
import com.springbit.crypto.services.ICryptoService;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;


@Service
@ConditionalOnProperty(name="spring-bit.services.coinmarketcap.enabled")
public class CoinMarketCap implements ICryptoService {

    private final DtoMapper mapper;

    private final ObjectMapper objectMapper;

    private final WebClient webClient;

    private static final Logger logger = LoggerFactory.getLogger(CoinMarketCap.class);

    CoinMarketCap(
            CryptoServiceDirectory directory,
            @Value("${spring-bit.services.coinmarketcap.url}") String serviceUrl,
            @Value("${spring-bit.services.coinmarketcap.api-token}") String apiToken) {

        webClient = WebClient.builder()
                .baseUrl(serviceUrl)
                .defaultHeader("X-CMC_PRO_API_KEY", apiToken)
                .build();

        mapper = Mappers.getMapper(DtoMapper.class);
        objectMapper = new ObjectMapper();

        directory.registerService(getName(), this);
    }

    @Override
    public String getName() {
        return "coinmarketcap";
    }


    @Override
    public Flux<Crypto> getLiveQuote(List<CryptoType> symbols) {
        return webClient.get()
                .uri(uri -> uri.path("/v2/cryptocurrency/quotes/latest")
                        .queryParam("symbol",
                                String.join(",", symbols.stream().map(mapper::map).toList()))
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMapMany(node ->
                        Flux.fromIterable(node.get("data")))
                .mapNotNull(node -> {
                    try {
                        return objectMapper.treeToValue(node.get(0), JsonCrypto.class);
                    } catch (JsonProcessingException e) {
                        logger.warn("Exception while parsing json", e);
                        return null;
                    }
                })
                .map(mapper::mapD)
                .onErrorResume(error -> {
                    logger.error("Endpoint error:", error);
                    return Flux.empty();
                });
    }

    @Override
    public Flux<Block> getBlocks(CryptoType symbol) {
        throw new UnsupportedOperationException();
    }
}
