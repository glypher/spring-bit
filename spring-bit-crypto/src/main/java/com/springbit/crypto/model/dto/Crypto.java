package com.springbit.crypto.model.dto;

import java.time.LocalDateTime;

public record Crypto(String name, String symbol, LocalDateTime quoteDate, Float quotePrice) {

}
