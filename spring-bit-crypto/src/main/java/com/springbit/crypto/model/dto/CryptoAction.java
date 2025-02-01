package com.springbit.crypto.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record CryptoAction(
    @JsonProperty("name") String name,
    @JsonProperty("symbol") String symbol,
    @JsonProperty("operation") String operation,
    @JsonProperty("quantity") Float quantity,
    @JsonProperty("quoteDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "UTC")
    Instant quoteDate,
    @JsonProperty("quotePrice") Float quotePrice
) {}