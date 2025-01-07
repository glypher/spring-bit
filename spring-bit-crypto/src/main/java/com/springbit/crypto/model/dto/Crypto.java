package com.springbit.crypto.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record Crypto(
        String name,
        String symbol,
        @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime quoteDate,
        Float quotePrice
) {}
