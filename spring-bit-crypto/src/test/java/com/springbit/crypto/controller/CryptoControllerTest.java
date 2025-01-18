package com.springbit.crypto.controller;

import com.springbit.crypto.configuration.ModelConfig;
import com.springbit.crypto.model.BlockRepository;
import com.springbit.crypto.model.QuoteRepository;
import com.springbit.crypto.model.dto.Block;
import com.springbit.crypto.model.dto.Crypto;
import com.springbit.crypto.model.entity.EBlock;
import com.springbit.crypto.model.entity.ECrypto;
import com.springbit.crypto.services.CryptoServiceDirectory;
import com.springbit.crypto.services.CryptoType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@Import({ModelConfig.class})
@WebFluxTest(CryptoController.class)
@ActiveProfiles("test")
public class CryptoControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private BlockRepository blockRepository;

    @MockitoBean
    private QuoteRepository quoteRepository;

    @MockitoBean
    private CryptoServiceDirectory cryptoServiceDirectory;

    @Test
    void testIsAlive() {
        webTestClient.get()
                .uri("/crypto/live")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Crypto Service is alive!");
    }

    @Test
    void testGetAvailableCryptos() {
        webTestClient.get()
                .uri("/crypto/cryptos")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CryptoType.class)
                .hasSize(CryptoType.values().length - 1) // Excluding UNKNOWN
                .contains(CryptoType.BITCOIN, CryptoType.ETHEREUM); // Example specific cryptos
    }

    @Test
    void testGetBlocks() {
        EBlock mockBlock = new EBlock();
        mockBlock.setSymbol(CryptoType.BITCOIN.getSymbol());
        mockBlock.setBlockHash("testBlockHash");
        mockBlock.setBlockSize(10L);
        when(blockRepository.findAll()).thenReturn(Flux.just(mockBlock));

        webTestClient.get()
                .uri("/crypto/BTC/blocks")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Block.class)
                .hasSize(1)
                .contains(new Block(mockBlock.getSymbol(),
                        mockBlock.getBlockHash(), mockBlock.getBlockSize()))
                .hasSize(1);
    }

    @Test
    void testGetBlocksFromServiceDirectory() {
        Block mockBlock = new Block(CryptoType.BITCOIN.getSymbol(), "testBlockHash", 10L);
        when(blockRepository.findAll()).thenReturn(Flux.empty());
        when(cryptoServiceDirectory.getBlocks(any())).thenReturn(Flux.just(mockBlock));

        webTestClient.get()
                .uri("/crypto/BTC/blocks")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Block.class)
                .hasSize(1)
                .contains(mockBlock);
    }

    @Test
    void testGetLatestQuote() {
        ECrypto mockCrypto = new ECrypto();
        mockCrypto.setName(CryptoType.BITCOIN.name());
        mockCrypto.setSymbol(CryptoType.BITCOIN.getSymbol());
        mockCrypto.setQuoteDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        mockCrypto.setQuotePrice(5f);
        when(quoteRepository.findBySymbol("BTC")).thenReturn(Flux.just(mockCrypto));

        webTestClient.get()
                .uri("/crypto/BTC/quote")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Crypto.class)
                .hasSize(1)
                .contains(new Crypto(mockCrypto.getName(), mockCrypto.getSymbol(),
                        mockCrypto.getQuoteDate(), mockCrypto.getQuotePrice()));
    }

    @Test
    void testGetLatestQuoteFromServiceDirectory() {
        Crypto mockCrypto = new Crypto(CryptoType.BITCOIN.name(), CryptoType.BITCOIN.getSymbol(),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), 5f);
        when(quoteRepository.findBySymbol("BTC")).thenReturn(Flux.empty());
        when(cryptoServiceDirectory.getLiveQuote(any())).thenReturn(Flux.just(mockCrypto));

        webTestClient.get()
                .uri("/crypto/BTC/quote")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Crypto.class)
                .hasSize(1)
                .contains(mockCrypto);
    }
}
