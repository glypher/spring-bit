package com.springbit.crypto.services.bitquery;

import com.springbit.crypto.model.dto.Block;
import com.springbit.crypto.model.dto.Crypto;
import com.springbit.crypto.services.CryptoServiceDirectory;
import com.springbit.crypto.services.CryptoType;
import com.springbit.crypto.services.ICryptoService;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import java.util.List;

@Service
@ConditionalOnProperty(name="spring-bit.services.bitquery.enable")
public class BitqueryService implements ICryptoService {

    private final WebClient webClient;

    private final DtoMapper dtoMapper = Mappers.getMapper(DtoMapper.class);

    public BitqueryService(
            CryptoServiceDirectory directory,
            @Value("${spring-bit.services.bitquery.url}") String url,
            @Value("${spring-bit.services.bitquery.api-token}") String apiToken) {
        webClient = WebClient.builder()
                .baseUrl(url)
                .defaultHeader("X-API-KEY", apiToken)
                .build();

        directory.registerService(getName(), this);
    }

    private static String getGraphQlQuery(CryptoType cryptoType) {
        return String.format("""
                query DodgeChain {
                bitcoin(network: %s) {
                    blocks(options: {limit: 10, desc: "timestamp.iso8601"}) {
                        blockHash
                                blockSize
                        blockSizeBigInt
                                blockStrippedSize
                        blockVersion
                                blockWeight
                        chainwork
                                difficulty
                        height
                        timestamp {
                            iso8601
                        }
                    }
                } }
                """, cryptoType.name().toLowerCase());
    }

    @Override
    public String getName() {
        return "bitquery";
    }

    @Override
    public Flux<Crypto> getLiveQuote(List<CryptoType> symbols) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Flux<Block> getBlocks(CryptoType symbol) {
        var graphQlClient = HttpGraphQlClient.builder(webClient).build();

        return graphQlClient.document(BitqueryService.getGraphQlQuery(symbol))
                .retrieve("bitcoin.blocks")
                .toEntityList(JsonBlock.class)
                .flatMapMany(Flux::fromIterable)
                .map(dtoMapper::mapD);

    }
}
