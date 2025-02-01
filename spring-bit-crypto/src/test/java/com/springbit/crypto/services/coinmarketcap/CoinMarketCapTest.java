package com.springbit.crypto.services.coinmarketcap;

import com.springbit.crypto.model.BlockRepository;
import com.springbit.crypto.model.QuoteRepository;
import com.springbit.crypto.model.dto.Crypto;
import com.springbit.crypto.services.CryptoServiceDirectory;
import com.springbit.crypto.services.CryptoType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@WebFluxTest({CoinMarketCap.class, CryptoServiceDirectory.class})
@ActiveProfiles("test")
class CoinMarketCapTest {

    private MockWebServer mockWebServer;

    @MockitoBean
    private CryptoServiceDirectory cryptoServiceDirectory;

    @MockitoBean
    private BlockRepository blockRepository;
    @MockitoBean
    private QuoteRepository quoteRepository;

    @Value("${spring-bit.services.coinmarketcap.url}")
    private String serviceUrl;

    @Value("${spring-bit.services.coinmarketcap.api-token}")
    private String apiToken;

    @Autowired
    private CoinMarketCap coinMarketCap;

    @BeforeEach
    void setUp() throws IOException {
        verify(cryptoServiceDirectory, times(1)).registerService(coinMarketCap.getName(), coinMarketCap);

        when(cryptoServiceDirectory.getService(Optional.ofNullable(coinMarketCap.getName()))).thenReturn(coinMarketCap);

        mockWebServer = new MockWebServer();
        mockWebServer.start(UriComponentsBuilder.fromUriString(serviceUrl).build().getPort());

        String baseUrl = mockWebServer.url("/").toString();

    String a = baseUrl;
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testGetLiveQuote() throws InterruptedException {
        List<CryptoType> symbols = List.of(CryptoType.BITCOIN, CryptoType.ETHEREUM);

        ZonedDateTime now = ZonedDateTime.now();
        String mockResponse = """
            { "data": [
                [{
                    "name": "BITCOIN",
                    "symbol": "BTC",
                    "quote": { "USD": {
                        "last_updated": "%s",
                        "price": 5000.0
                    }}
                }],
                [{
                    "name": "ETHEREUM",
                    "symbol": "ETH",
                    "quote": {"USD": {
                        "last_updated": "%s",
                        "price": 3000.0
                    }}
                }]
            ]}""".formatted(now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), now.minusHours(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json"));


        // Start request
        Flux<Crypto> liveQuotes = coinMarketCap.getLiveQuote(symbols);


        StepVerifier.create(liveQuotes)
                .expectNextMatches(crypto -> crypto.name().equals("BITCOIN") && crypto.symbol().equals("BTC")
                        && crypto.quoteDate().equals(now.toInstant())
                        && crypto.quotePrice() == 5000.0)
                .expectNextMatches(crypto -> crypto.name().equals("ETHEREUM") && crypto.symbol().equals("ETH")
                        && crypto.quoteDate().equals(now.minusHours(1).toInstant())
                        && crypto.quotePrice() == 3000.0)
                .verifyComplete();

        // Verify the mock server received the correct request
        var recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).contains("/v2/cryptocurrency/quotes/latest");
        assertThat(recordedRequest.getHeader("X-CMC_PRO_API_KEY")).isEqualTo(apiToken);
    }
}
