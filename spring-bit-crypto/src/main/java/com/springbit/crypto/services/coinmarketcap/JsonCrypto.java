package com.springbit.crypto.services.coinmarketcap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonCrypto
{
    static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    @JsonProperty("name")
    private String name;

    @JsonProperty("symbol")
    private String symbol;

    private LocalDateTime quoteDate;

    private Float quotePrice;

    @JsonProperty("quote")
    private void unpackQuote(JsonNode json) {
        json = json.get("USD");
        quoteDate = LocalDateTime.parse(json.get("last_updated").asText(), JsonCrypto.dateFormatter);
        quotePrice = json.get("price").floatValue();
    }
}
