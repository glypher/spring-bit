package com.springbit.crypto.services.coinmarketcap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.Instant;
import java.time.ZonedDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonCrypto
{
    @JsonProperty("name")
    private String name;

    @JsonProperty("symbol")
    private String symbol;

    private Instant quoteDate;

    private Float quotePrice;

    @JsonProperty("quote")
    private void unpackQuote(JsonNode json) {
        json = json.get("USD");
        quoteDate = ZonedDateTime.parse(json.get("last_updated").asText()).toInstant();
        quotePrice = json.get("price").floatValue();
    }
}
