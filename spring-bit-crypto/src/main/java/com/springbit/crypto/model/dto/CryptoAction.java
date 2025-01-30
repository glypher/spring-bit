package com.springbit.crypto.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record CryptoAction(
    String name,
    String symbol,
    String operation,
    Float quantity,
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime quoteDate,
    Float quotePrice
) {}
