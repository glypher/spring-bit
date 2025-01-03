package com.springbit.crypto.services;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
public enum CryptoType {
    BITCOIN("BTC"),
    ETHEREUM("ETH"),
    DOGECOIN("DOGE"),
    UNKNOWN("UNKNOWN");

    private final String symbol;

    CryptoType(String symbol) {
        this.symbol = symbol;
    }

    @JsonProperty("name")
    private String getName() {
        return this.name();
    }

    public static CryptoType map(String type) {
        for (CryptoType cryptoType : CryptoType.values()) {
            if (cryptoType.getSymbol().equalsIgnoreCase(type)) {
                return cryptoType;
            }
        }
        return UNKNOWN;
    }
}
